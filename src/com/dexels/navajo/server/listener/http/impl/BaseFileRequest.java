package com.dexels.navajo.server.listener.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dexels.navajo.server.listener.http.AsyncRequest;

public abstract class BaseFileRequest implements AsyncRequest {


   private OutputStream os = null; // = new ByteArrayOutputStream();
//	private int readCount = 0;
	private int contentLength;
	private File tempFile;
	  
  protected BaseFileRequest() {
	  try {
		tempFile = File.createTempFile("navajoRequest_", ".xml");
		  os = new FileOutputStream(tempFile);
		  System.err.println("Created tempfile: "+tempFile.getAbsolutePath());
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
    
	public void appendData(byte[] buffer, int length) throws IOException {
		getRequestOutputStream().write(buffer, 0, length);
//		getRequestOutputStream().flush();

	}
//
//	public int getReadCount() {
//		return readCount;
//	}
//
//
//	public void setReadCount(int readCount) {
//		this.readCount = readCount;
//	}
//	
//   
	public OutputStream getRequestOutputStream() throws IOException {
		return os;
	}
	
	
	public InputStream getRequestInputStream() throws IOException {
		getRequestOutputStream().flush();
		getRequestOutputStream().close();
		System.err.println("Getting file: "+tempFile.getAbsolutePath());
		FileInputStream fis = new FileInputStream(tempFile);
//		 return new ByteArrayInputStream(os.toByteArray());
		return fis;
	}
	@Override
	public void dumpBuffer() {

	}
	

	public int getRequestSize() {
		return this.contentLength;
	}

	public void setRequestSize(int contentLength) {
		this.contentLength = contentLength;
	}

	public void submitComplete() {
		// TODO Delete temp file!
	}


}
