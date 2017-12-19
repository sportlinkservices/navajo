package com.dexels.navajo.document.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dexels.utils.Base64;
import org.dexels.utils.Base64DecodingFinishedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.metadata.FormatDescription;
import com.dexels.navajo.document.saximpl.qdxml.QDParser;

/**
 * <p>
 * Title: Binary
 * </p>
 * <p>
 * Description: Binary datacontainer
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Dexels BV
 * </p>
 * 
 * @author aphilip
 * @version $Id$
 */

public final class Binary extends NavajoType implements Serializable,Comparable<Binary> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6392612802747438142L;

	// private byte [] data;
    private String mimetype = null; //"text/plain";

    private transient File dataFile = null;
    private transient File lazySourceFile = null;
//    private transient InputStream lazyInputStream = null;
    private String myRef = null;
    private String extension = null;
    
    private byte[] inMemory = null;
    
    public final static String MSEXCEL = "application/msexcel";

    public final static String MSWORD = "application/msword";

    public final static String PDF = "application/pdf";

    public final static String GIF = "image/gif";

    public final static String TEXT = "plain/text";

    private FormatDescription currentFormatDescription;

	private transient MessageDigest messageDigest;

	private byte[] digest;

	private final URL lazyURL;
    
    private final static HashMap<String,Binary> persistedBinaries = new HashMap<String,Binary>();
    
    private Map<String,List<String>> urlMetaData = null;

	private Writer pushWriter;

	private OutputStream pushStream;

    private static final Logger logger = LoggerFactory.getLogger(Binary.class);
    /**
     * Construct a new Binary object with data from an InputStream It does close
     * the stream. I don't like it, but as yet I don't see any situation where
     * this would be a problem
     * 
     * @param is
     *            InputStream
     */
    public Binary(InputStream is) {
        this(is,false);
    }

    public Binary(InputStream is, boolean lazy) {
//        super(Property.BINARY_PROPERTY);
//        createDigest();
        this();
        if (is == null) {
            return;
        }
        if (lazy) {
        	throw new UnsupportedOperationException("Constructing lazy binary based on a stream. This is not working.");
//            lazyInputStream = is;
        } else {
            try {
                loadBinaryFromStream(is);
            } catch (Exception e) {
            	logger.error("Error: ", e);
            }
            this.mimetype = guessContentType();
        }
     }

    public Binary() {
        super(Property.BINARY_PROPERTY);
        this.lazyURL = null;
        MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			logger.warn("Failed creating messageDigest in binary. Expect problems", e1);
		}
		this.messageDigest = md;
        setMimeType(guessContentType());
     }    
    
    public boolean isResolved() {
    	if(inMemory!=null) {
    		return true;
    	}
    	if(dataFile!=null) {
    		return true;
    	}
    	return false;
    }
    
    public String getHandle() {
        if (dataFile!=null) {
            return dataFile.getName();
        }
        if (lazySourceFile!=null) {
            return lazySourceFile.getAbsolutePath();
        }
        return "none";
    }
    
    /**
     * Returns an outputstream. Write the data for this binary to the stream, flush and close it.
     * @return
     */
    public OutputStream getOutputStream() {
        try {
            return createTempFileOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Can not create tempfile?!",e);
        }
    }
        
    
    /**
     * Some components like a URL to point to their data.
     * Don't forget that this is a local (file) url, and is only valid on this machine
     * When using with a lazy url, return URL to local data if it is available
     * Alternatively, we could also resolve the lazy URL
     * @return
     * @throws MalformedURLException
     */
    public URL getURL() throws MalformedURLException {
        if (lazySourceFile != null && lazySourceFile.exists()) {
            return lazySourceFile.toURI().toURL();
        } else {
            if (dataFile != null && dataFile.exists()) {
                return dataFile.toURI().toURL();
            } else {
            	if( lazyURL!=null) {
            		return lazyURL;
            	}
            	return null;
            }
        }
    }
	        
    private void loadBinaryFromStream(InputStream is) throws IOException, FileNotFoundException {
    	loadBinaryFromStream(is, true);
    }
    
    private void loadBinaryFromStream(InputStream is, boolean close) throws IOException, FileNotFoundException {
        OutputStream os = createTempFileOutputStream();
        copyResource(os, is,close);


//		setDigest(messageDigest.digest());
//    	if(NavajoFactory.getInstance().isSandboxMode()) {
//    		inMemory = ((ByteArrayOutputStream)os).toByteArray();
//    	}
    	if ( this.mimetype == null ) {
        	this.mimetype = getSubType("mime");
        	this.mimetype = (mimetype == null || mimetype.equals("") ? guessContentType() : mimetype);
        }
        
    }
    
    @Override
	public int hashCode() {
    	if(getDigest()!=null) {
    		return getHexDigest().hashCode();
    	}
    	return super.hashCode();
    }
    
    public String getHexDigest() {
		return getDigest().hex();
    }

    public void setDigest(byte[] digest) {
    	this.digest = digest;
	}

//    public static String bytesToHex(byte[] bytes) {
//        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
//        char[] hexChars = new char[bytes.length * 2];
//        int v;
//        for ( int j = 0; j < bytes.length; j++ ) {
//            v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
    
	private OutputStream createTempFileOutputStream() throws IOException, FileNotFoundException {
		if(messageDigest==null) {
	        MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				logger.warn("Failed creating messageDigest in binary. Expect problems", e1);
			}
			this.messageDigest = md;
		}
		messageDigest.reset();
    	if(NavajoFactory.getInstance().isSandboxMode()) {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream() {
    			@Override
				public void close() {
    				try {
						super.close();
					} catch (IOException e) {
						logger.error("Error: ", e);
					}
	    			inMemory = toByteArray();
    			}
    		};
    		OutputStream dos = new DigestOutputStream(baos, messageDigest) {

				@Override
				public void close() throws IOException {
					setDigest(messageDigest.digest());
					super.close();
				}
    			
    		};
    		
    		return dos;
    	} else {
            dataFile = File.createTempFile("binary_object", "navajo", NavajoFactory.getInstance().getTempDir());
            
            FileOutputStream fos = new FileOutputStream(dataFile);
            OutputStream dos = new DigestOutputStream(fos, messageDigest) {

				@Override
				public void close() throws IOException {
					super.close();
					setDigest(messageDigest.digest());
				}
            	
            };
            return dos;
    	}
    }
	
	public BinaryDigest getDigest() {
		if(!isResolved() && this.digest ==null) {
			try {
				resolveData();
			} catch (IOException e) {
				logger.error("Error: ", e);
			}
		}
		if(this.digest==null) {
			return null;
		}
		return new BinaryDigest(this.digest);
	}

    public Binary(File f) throws IOException {
        this(f, false);
    }

    public Binary(File f, boolean lazy) throws IOException {
    	this();
    	if (lazy) {
            lazySourceFile = f;
        } else {
            loadBinaryFromStream(new FileInputStream(f));
        }
        this.mimetype = guessContentType();
    }
    
//    private final void init(File f, boolean lazy) throws IOException {
//    	 expectedLength = f.length();
//         if (lazy) {
//             lazySourceFile = f;
//         } else {
//             loadBinaryFromStream(new FileInputStream(f));
//         }
//         this.mimetype = guessContentType();
//    }

    /**
     * Construct a new Binary object from a byte array
     * 
     * @param data
     *            byte[]
     *           
     */
    public Binary(byte[] data) {

    	this();
    	// TODO: For sandbox, set inMemory directly to data
        if (data != null) {
            OutputStream fos =null;
            try {
                fos = createTempFileOutputStream();
                fos.write(data);
            } catch (IOException e) {
            	logger.error("Error: ", e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                    }
                }
            }
            
            this.mimetype = guessContentType();
         }
    }

    /**
     * Does NOT close the reader, it is shared with the qdXML parser
     * 
     * @param reader
     * @throws IOException 
     */
    public Binary(Reader reader) throws IOException {
    	this();
    	parseFromReader(reader);
    }

    public Binary(URL u, boolean lazyData) throws IOException {
        super(Property.BINARY_PROPERTY);
    	this.lazyURL = u;
    	if(lazyData) {
    		// nothing to do now
    		return;
    	}
    	URLConnection uc = lazyURL.openConnection();
    	if(!(uc instanceof HttpURLConnection)) {
    		// not http, metadata is unavailable.
    		this.urlMetaData = Collections.emptyMap();
    		if(!lazyData) {
    			loadBinaryFromStream(uc.getInputStream());
    		} else {
    			return;
    		}
    	}
    	HttpURLConnection huc = (HttpURLConnection)uc;
    	if(lazyData) {
    		huc.setRequestMethod("HEAD");
        	this.urlMetaData = Collections.unmodifiableMap(huc.getHeaderFields());
    	} else {
    		// so a get
    		this.urlMetaData = Collections.unmodifiableMap(huc.getHeaderFields());
        	loadBinaryFromStream(huc.getInputStream());
    	}
    }

    public void startPushRead() throws FileNotFoundException, IOException {
        OutputStream fos = null;
        fos = createTempFileOutputStream();
        this.pushWriter = Base64.newDecoder(fos);
    }

    public void pushContent(String content) throws IOException {
    	if(this.pushWriter==null) {
    		logger.error("Huh?");
    		throw new NullPointerException("No pushwriter found");
    	}
    	this.pushWriter.write(content);
    }
    
    public void finishPushContent() throws IOException  {
		if(this.pushWriter!=null) {
			this.pushWriter.flush();
			this.pushWriter.close();
			this.pushWriter = null;
		} else if(this.pushStream!=null) {
			this.pushStream.flush();
			this.pushStream.close();
			this.pushStream = null;
		} else {
			logger.error("Con not finish push as none seem to have started");
		}
    }

    public void startBinaryPush()  {
        try {
			pushStream = null;
			pushStream = createTempFileOutputStream();
		} catch (IOException e) {
			logger.error("Error starting binary push:",e);
			throw new RuntimeException(e);
		}
    }
    
    public void pushContent(byte[] data) throws RuntimeException {
        try {
			pushStream.write(data);
		} catch (IOException e) {
			logger.error("Error pushing data into binary:",e);
			throw new RuntimeException(e);
		}
    }


    private void parseFromReader(Reader reader) throws IOException {
        OutputStream fos = null;
        try {
            fos = createTempFileOutputStream();
            PushbackReader pr = null;
            if (reader instanceof PushbackReader) {
                pr = (PushbackReader) reader;
            } else {
                pr = new PushbackReader(reader, QDParser.PUSHBACK_SIZE);
            }

            Writer w = Base64.newDecoder(fos);
            try {
                copyBufferedBase64Resource(w, pr);
            } catch (Base64DecodingFinishedException e) {
            }
            w.flush();
        } finally {
            if (fos != null) {
                try{ 
                    fos.close();
                } catch (Exception e) {
                    
                }
            }
        }
      
        this.mimetype = getSubType("mime");
        this.mimetype = (mimetype == null || mimetype.equals("") ? guessContentType() : mimetype);
//		setDigest(messageDigest.digest());
   }


    /**
     * Buffered
     * @param out
     * @param in
     * @throws IOException
     */
    private void copyBufferedBase64Resource(Writer out, PushbackReader in) throws IOException {
        int read;
        int iterations = 0;
        char[] buffer = new char[QDParser.PUSHBACK_SIZE];
        while ((read = in.read(buffer, 0 ,buffer.length)) > -1) {
//            progress +=read;
            iterations++;
            if (iterations % 100 == 0) {
//                logger.info("Reading data. "+progress+"/"+expectedLength);
            }
            int ii = getIndexOf(buffer, '<');
//            logger.info("Buffer size: "+buffer.length+" '<' index: "+ii+" read: "+read);
            if (ii==-1) {
                out.write(buffer,0,read);
            } else {
//                debug("Writing", buffer, 0, ii);
                out.write(buffer, 0, ii);
//                debug("Pushback", buffer, ii, read-1);
                in.unread(buffer, ii, read-ii);
                break;
            }
            
//             if (read == '<') {
//                in.unread('<');
//                break;
//            } else {
//                out.write(read);
//            }
        }
        
        out.flush();
    }


    private int getIndexOf(char[] buffer, char c) {
        for (int i = 0; i < buffer.length; i++) {
            if (c==buffer[i]) {
                return i;
            }
        }
        return -1;
    }
    
//    private Map<String,List<String>> getURLMetaData() {
//    	if(urlMetaData==null) {
//    		
//    	}
//    }
    
    public long getLength() {
    	if(urlMetaData!=null && !isResolved()) {
    		System.err.println("URLMETA: "+urlMetaData);
    		List<String> lengthHeaders = urlMetaData.get("Content-Length");
    		if(lengthHeaders!=null && !lengthHeaders.isEmpty()) {
    			return Long.parseLong(lengthHeaders.get(0));
    		}
        	try {
				resolveData();
			} catch (IOException e) {
				logger.error("Error: ", e);
				return -1;
			}
    	}
    	if(!isResolved()) {
    		return -1;
    	}
    	if (NavajoFactory.getInstance().isSandboxMode()) {
    		if(inMemory!=null) {
    			return inMemory.length;
    		}
    		byte[] b = NavajoFactory.getInstance().getHandle(dataFile.getName());
    		if (b==null) {
				return 0;
			} else {
				return b.length;
			}
		} else {
	        if (lazySourceFile != null && lazySourceFile.exists()) {
	            return lazySourceFile.length();
	        } else {
	            if (dataFile != null && dataFile.exists()) {
	            	if (NavajoFactory.getInstance().isSandboxMode()) {
	            		byte[] res = NavajoFactory.getInstance().getHandle(dataFile.getName());
	            		if(res==null) {
	            			return 0;
	            		} else {
	            			return res.length;
	            		}
	            	} else {
		            	return dataFile.length();
					}
	            } else {
	                return 0;
	            }
	        }
		}
    }

    private void resolveData() throws IOException {
    	if(lazyURL!=null) {
	    	try {
				URLConnection uc = lazyURL.openConnection();
				this.urlMetaData = uc.getHeaderFields();
				loadBinaryFromStream(uc.getInputStream());
			} catch (IOException e) {
				throw new RuntimeException("Error resolving binary from URL", e);
			}
    	}
    	if(lazySourceFile!=null) {
    		
    		try(FileInputStream fis = new FileInputStream(lazySourceFile)) {
    			copyResource(createTempFileOutputStream(), fis, true);
			}
    		
    		
    	}
    }
    
    private InputStream resolveDirectly() {
    	try {
			URLConnection uc = lazyURL.openConnection();
			this.urlMetaData = uc.getHeaderFields();
			return uc.getInputStream();
    	} catch (IOException e) {
			throw new RuntimeException("Error resolving binary", e);
		}
    }

	/**
     * Guess the internal data's mimetype
     * 
     * @return String
     */
    public final String guessContentType() {
//        if (mimetype != null && !mimetype.equals("")) {
//            return mimetype;
//        } else {
            File f;
            if (lazySourceFile!=null) {
                f = lazySourceFile;
            } else {
                f = dataFile;
            }
            if ( f != null ) {
            	if(NavajoFactory.getInstance().isSandboxMode()) {
                	currentFormatDescription = com.dexels.navajo.document.metadata.FormatIdentification.identify(inMemory);
            	} else {
                	currentFormatDescription = com.dexels.navajo.document.metadata.FormatIdentification.identify(f);
            	}
            }
//            logger.info("Guessed: "+currentFormatDescription.getMimeType());
//            logger.info("Guessed: "+currentFormatDescription.getFileExtensions());
            if (currentFormatDescription == null) {
                return "application/unknown";
            } else if (currentFormatDescription.getMimeType() != null) {
                return currentFormatDescription.getMimeType();
            } else {
                return currentFormatDescription.getShortName();
            }
//        }
    }

    
    public FormatDescription getFormatDescription() {
        return currentFormatDescription;
        
    }
    
    public void setExtension(String e) {
    	this.extension = e;
    }
    
    public String getExtension() {
    	if(extension!=null) {
    		return extension;
    	}
        if(currentFormatDescription==null) {
            guessContentType();
        }
        if (currentFormatDescription!=null) {
            List<String> exts = currentFormatDescription.getFileExtensions();
            if (exts!=null && !exts.isEmpty()) {
                return exts.get(0);
            }
        }
        return "dat";
    }
    
    /**
     * Get this Binary's data
     * 
     * This one is a bit odd. Why on earth not simply get the data as a stream, and copyResource to a ByteArrayOutputStream?
     * @return byte[]
     */
    public final byte[] getData() {
		if(!isResolved()) {
			try {
				resolveData();
			} catch (IOException e) {
				logger.error("Error: ", e);
			}
		}
		if (inMemory != null) {
			return inMemory;
		}


		File file = null;
		if (lazySourceFile != null) {
			file = lazySourceFile;
		} else {
			file = dataFile;
		}
		RandomAccessFile in = null;
		try {
			if (file != null) {
				in = new RandomAccessFile(file, "r");
				byte[] data = new byte[(int) file.length()];// + 1 ];
				in.readFully(data);
				return data;
			}
		} catch (Exception e) {
			logger.error("Error: ", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error("Error: ", e);
			}
		}
		return null;

    }

    private final void copyResource(OutputStream out, InputStream in, boolean closeInput) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        try {
			while ((read = in.read(buffer)) > -1) {
			    out.write(buffer, 0, read);
			}
		} finally {
			if(closeInput) {
			    if (in != null) {
			        in.close();
			    }
			    if (out != null) {
			        out.close();
			    }
			}
		}
    }

    public final void write(OutputStream to, boolean close) throws IOException {
        InputStream in = getDataAsStream();
        if (in != null) {
            try {
                copyResource(to, in, close);
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    // Don't care
                }
            }

        }
    }

    public final void write(OutputStream to) throws IOException {
        write( to, true );
    }

    public File getFile() {
    	if (lazySourceFile != null) {
    		return lazySourceFile;
    	} else {
    		return dataFile;
    	}
    }
    /**
     * Will return a
     * @param bufferSize
     * @return
     */
    public Iterable<byte[]> getDataAsIterable(final int bufferSize) {
		if(inMemory!=null) {
			logger.info("Binary: in memory detected bytes: "+inMemory.length);
			if(inMemory.length> bufferSize) {
				logger.warn("Should split, this array is too long!");
			}
			List<byte[]> result = new LinkedList<>();
			result.add(inMemory);
			return result;
		}
		final FileChannel channel = getDataAsChannel();
		// TODO Warning, I think this leaks filepointers, as this channel will never get closed,
		// better to emit this thing as an Observable so we can close it on unsubscribe;
		
		final ByteBuffer outputBuffer = ByteBuffer.allocate(bufferSize);
    	return new Iterable<byte[]>(){

			@Override
			public Iterator<byte[]> iterator() {
				return new Iterator<byte[]>(){

					@Override
					public boolean hasNext() {
						try {
							if(channel.position() >= channel.size()) {
								channel.close();
								return false;
							}
							return true;
						} catch (IOException e) {
							logger.error("Error: ", e);
						}
						return false;
					}

					@Override
					public byte[] next() {
						try {
							int dataRead = channel.read(outputBuffer);
							outputBuffer.flip();
							byte[] result = new byte[dataRead];
							outputBuffer.get(result);
							return result;
						} catch (IOException e) {
							logger.error("Error: ", e);
						}
							
						return null;
					}
				};
			}
		};
    }
    
    @SuppressWarnings("resource")
	public FileChannel getDataAsChannel() {
    
//    	if (NavajoFactory.getInstance().isSandboxMode()) {
//		ByteArrayInputStream bais = new ByteArrayInputStream(NavajoFactory.getInstance().getHandle(dataFile.getName()));
//		return bais;
//	} else {
		if(inMemory!=null) {
			logger.info("Binary: in memory detected bytes: "+inMemory.length);
			throw new UnsupportedOperationException("Not yet implemented: Streaming from memory");
		}
        if (lazySourceFile != null) {
            try {
            	RandomAccessFile rf = new RandomAccessFile(lazySourceFile, "r");
            	return rf.getChannel();
            } catch (FileNotFoundException e) {
            	logger.error("Error: ", e);
            	return null;
            }
        }
        if (dataFile != null) {
            try {
            	RandomAccessFile rf = new RandomAccessFile(dataFile, "r");
            	return rf.getChannel();
            } catch (FileNotFoundException e) {
            	logger.error("Error: ", e);
            	return null;
            }
        } else if(this.lazyURL!=null) {
			throw new UnsupportedOperationException("Not yet implemented: Remote direct streaming from URL");
        } else {        
            return null;
        }

    }
    
    
    public final InputStream getDataAsStream() {
//    	if (NavajoFactory.getInstance().isSandboxMode()) {
//    		ByteArrayInputStream bais = new ByteArrayInputStream(NavajoFactory.getInstance().getHandle(dataFile.getName()));
//    		return bais;
//    	} else {
    		if(inMemory!=null) {
    			logger.info("Binary: in memory detected bytes: "+inMemory.length);
    			return new ByteArrayInputStream(inMemory);
    		}
	        if (lazySourceFile != null) {
	            try {
	                return new FileInputStream(lazySourceFile);
	            } catch (FileNotFoundException e) {
	            	logger.error("Error: ", e);
	            	return null;
	            }
	        }
	        if (dataFile != null) {
	            try {
	                return new FileInputStream(dataFile);
	            } catch (FileNotFoundException e) {
	            	logger.error("Error: ", e);
	            	return null;
	            }
	        } else if (lazySourceFile!=null) {
	            try {
	                return new FileInputStream(lazySourceFile);
	            } catch (FileNotFoundException e) {
	            	logger.error("Error: ", e);
	            	return null;
	            }	        	
	        }else if(this.lazyURL!=null) {
        		return resolveDirectly();
	        } else {        
	            return null;
	        }
//		}
    }

    /**
     * Get this Binary's mimetype
     * 
     * @return String
     */
    public final String getMimeType() {
        return this.mimetype;
    }

    /**
     * Set this Binary's mimetype
     * 
     * @param mime
     *            String
     */
    public final void setMimeType(String mime) {
        this.mimetype = mime;
    }

    
    @Override
	public final boolean equals(Object o) {
    	if(!(o instanceof Binary)) {
        	return false;
    	} 
    	Binary b = (Binary)o;
    	
    	if(b.getDigest()!=null && getDigest()!=null) {
    		logger.debug("Digests found, comparing those");
    		final String other = b.getHexDigest();
			final String mine = getHexDigest();
			return mine.equals(other);
    	}
    
    	logger.warn("Digest based equality failed. Comparing binary sizes: "+b.getLength()+" with "+getLength());
    	if(b.getLength()!=getLength()) {
    		return false;
    	}
    	logger.info("Comparing binary mime: "+b.getMimeType()+" with "+getMimeType());
    	if(!(getMimeType().equals(b.getMimeType()))) {
    		return false;
    	}
    	logger.info("DUBIOUS EQUALITY IN BINARY!");
    	
    	Thread.dumpStack();
    	return true;
    	
    }

    
    
    // for sorting. Not really much to sort
    @Override
	public final int compareTo(Binary o) {
    	int h = hashCode();
    	int i = o.hashCode();
    	if(h>i) {
    		return 1;
    	}
    	if(h<i) {
    		return -1;
    	}
        return 0;
    }

    public  Binary cloneWithExtension() throws IOException {
    	File f = File.createTempFile("navajo", "."+getExtension());
    	FileOutputStream fos = new FileOutputStream(f);
    	InputStream dataAsStream = getDataAsStream();
		copyResource(fos, dataAsStream,true);
    	fos.flush();
    	dataAsStream.close();
    	fos.close();
    	return new Binary(f,true);
    }
    
    @Override
	public String toString() {
    	if(digest!=null) {
    		return getDigest().hex();
    	}
    	return super.toString();
    }
    
    @Override
	protected void finalize() {
    
    	if (NavajoFactory.getInstance().isSandboxMode()) {
    		if(dataFile!=null) {
    			NavajoFactory.getInstance().removeHandle(dataFile.getName());
    		}
		} else {
	        if (dataFile != null && dataFile.exists() ) {
	            try {
	                dataFile.delete();
	            } catch (Throwable t) {
	            	logger.error("Error: ", t);
	            }
	        }
		}
    }

    /**
     * @deprecated Returns base64. Preferrably don't use this, unless you REALLY
     *             want the data in a string, as it is quite memory hungry
     *             Even then, rather write to a StringWriter or something. 
     *             This is an old-school function, and it uses the Sun base64 encoder
     *             so it is quite possible that large binaries will result in out of
     *             memory exceptions
     */
    @Deprecated
    public final String getBase64() {
        final StringWriter sw = new StringWriter();
        try {
           writeBase64(sw, true);
       } catch (IOException e) {
           logger.error("IOexception on getting base64 encoding!", e);
       }
        return sw.toString();
   }

   /**
    * writes the base64 to a Writer. Will not close the writer.
    * 
    * @throws IOException
    */
   public final void writeBase64(Writer sw) throws IOException {
       writeBase64(sw, true);
   }
   
   public final void writeBase64(Writer sw, boolean newline) throws IOException {
       final OutputStream os;
       if (newline) {
           os = Base64.newEncoder(sw);
       } else {
           os = Base64.newEncoder(sw, 0, "");
       }
       if(!isResolved()) {
           resolveData();
       }
       InputStream dataAsStream = getDataAsStream();
       if (dataAsStream!=null) {
           copyResource(os, dataAsStream,true);
       }
       os.close();
   }

    /**
     * Get the file reference to a binary.
     * If persist is set, the binary object is put in a persistent store to
     * prevent it from being garbage collected.
     * 
     * @param persist
     * @return
     */
    public String getTempFileName(boolean persist) {
    	
    	String ref = null;
    	if (lazySourceFile != null) {
    		ref = lazySourceFile.getAbsolutePath();
    	} else if (dataFile != null) {    		
    		ref =  dataFile.getAbsolutePath();
    	} 
    	
    	if ( persist && ref != null && !persistedBinaries.containsKey(ref) ) {
    		persistedBinaries.put(ref, this);
    	}
    	myRef = ref;
    	
    	return ref;
    }	
    
    public void removeRef() {
    	logger.info("In removeRef(), myRef = " + myRef);
    	if ( myRef != null ) {
    		persistedBinaries.remove(myRef);
    	}
    }
    /**
     * Remove a persisted binary.
     * 
     * @param ref
     */
    public static void removeRef(String ref) {
    	if ( ref != null ) {
    	persistedBinaries.remove(ref);
    	}
    }

    @Override
	public boolean isEmpty() {
        if (dataFile==null && lazySourceFile==null) {
            return true;
        }
        return false;
    }
    
    public boolean isEqual(Binary other ) {
    	
    	if ( other == null ) {
    		return false;
    	}
    	
    	if ( getLength() != other.getLength() ) {
    		return false;
    	}
    	
    	// compare byte by byte.
    	RandomAccessFile otherFile = null;
    	RandomAccessFile myFile = null;
    	try {
    		otherFile = new RandomAccessFile( other.getFile(), "r");
    		myFile = new RandomAccessFile( getFile(), "r" );
    		byte [] otherBuffer = new byte[1024];
    		byte [] myBuffer = new byte[1024];
    		
    		for (int i = 0; i < ( other.getLength() / 1024 ); i++) {
    			otherFile.readFully(otherBuffer);
    			myFile.readFully(myBuffer);
    			// compare buffers.
    			for (int j = 0; j < otherBuffer.length; j++) {
    				if ( otherBuffer[j] != myBuffer[j]) {
    					return false;
    				}
    			}
    		}
    		
    		long otherP = otherFile.getFilePointer();
    		long myP = myFile.getFilePointer();
    		
    		otherFile.readFully( otherBuffer, 0, (int) (other.getLength() - otherP) );
    		myFile.readFully( myBuffer, 0, (int) (getLength() - myP) );
    		
    		for (int j = 0; j < (other.getLength() - otherP); j++) {
				if ( otherBuffer[j] != myBuffer[j]) {
					return false;
				}
			}
    		
    		return true;
    		
    	} catch (Exception e) {
    		logger.error("Error: ", e);
    		return false;
    	} finally {
    		if ( otherFile != null ) {
    			try {
    				otherFile.close();
    			} catch (IOException e) {
    				
    			}
    		}
    		if ( myFile != null ) {
    			try {
    				myFile.close();
    			} catch (IOException e) {
    				
    			}
    		}
    	}
    }


    
    /**
     * Custom deserialization is needed.
     */
	private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
		aStream.defaultReadObject();
		// manually deserialize
		loadBinaryFromStream(aStream, false);
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream aStream) throws IOException {
		aStream.defaultWriteObject();
		// manually serialize superclass
		if (dataFile != null) {
			copyResource(aStream, new FileInputStream(dataFile), true);
		} else if (lazySourceFile != null) {
			copyResource(aStream, new FileInputStream(lazySourceFile), true);
		}
		aStream.flush();
	}

    public void setFormatDescriptor(FormatDescription fd) {
		currentFormatDescription = fd;
	}    	
}
