package com.dexels.navajo.jsp.proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dexels.navajo.client.ClientException;
import com.dexels.navajo.client.ClientInterface;
import com.dexels.navajo.client.NavajoClientFactory;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;

public class ProxyServlet extends HttpServlet {

	private static final long serialVersionUID = 2618272459465144500L;

	private static final String COMPRESS_GZIP = "gzip";
	private static final String COMPRESS_JZLIB = "jzlib";
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProxyServlet.class);

	private ClientInterface myClient;

	/**
	 * Handle a request.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		MDC.clear();
		logger.debug("Performing POST");
//		String service = request.getParameter("service");
		String acceptEncoding = request.getHeader("Accept-Encoding");
		String contentEncoding = request.getHeader("Content-Encoding");

		if(acceptEncoding!=null) {
			MDC.put("Accept-Encoding", acceptEncoding);
		}
		if(contentEncoding!=null) {
			MDC.put("Content-Encoding", contentEncoding);
		}
		BufferedReader r = null;
		BufferedWriter out = null;
		try {

			Navajo in = null;

				if (contentEncoding != null && contentEncoding.equals(COMPRESS_JZLIB)) {
					r = new BufferedReader(new java.io.InputStreamReader(
							new InflaterInputStream(request.getInputStream()),"UTF-8"));
				} else if (contentEncoding != null
						&& contentEncoding.equals(COMPRESS_GZIP)) {
					r = new BufferedReader(new java.io.InputStreamReader(
							new java.util.zip.GZIPInputStream(
									request.getInputStream()), "UTF-8"));
				} else {
					r = new BufferedReader(request.getReader());
				}
				in = NavajoFactory.getInstance().createNavajo(r);
				r.close();
				r = null;


			if (in == null) {
				throw new ServletException("Invalid request.");
			}

			Header header = in.getHeader();
			if (header == null) {
				throw new ServletException("Empty Navajo header.");
			}
            logger.debug("Retrieved INDOC");

			Navajo outDoc = doProxy( in);

			response.setContentType("text/xml; charset=UTF-8");
			response.setHeader("Accept-Ranges", "none");
			// Why do we want this?
			//response.setHeader("Connection", "close");
			// TODO: support multiple accept encoding
			logger.debug("Got OUTDOC - going to create BufferedWriter to outputstream");
			if (acceptEncoding != null && acceptEncoding.equals(COMPRESS_JZLIB)) {
				response.setHeader("Content-Encoding", COMPRESS_JZLIB);
				out = new BufferedWriter(new OutputStreamWriter(
						new DeflaterOutputStream(response.getOutputStream()), "UTF-8"));
			} else if (acceptEncoding != null
					&& acceptEncoding.equals(COMPRESS_GZIP)) {
				response.setHeader("Content-Encoding", COMPRESS_GZIP);
				out = new BufferedWriter(new OutputStreamWriter(
						new java.util.zip.GZIPOutputStream(
								response.getOutputStream()), "UTF-8"));
			} else {
				out = new BufferedWriter(response.getWriter());
			}
            logger.debug("Going to write to output stream");

			outDoc.write(out);
			out.flush();
			out.close();

			out = null;
            logger.debug("Finished POST request");


		} catch (Throwable e) {
			throw new ServletException(e);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
					// NOT INTERESTED.
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// NOT INTERESTED.
				}
			}
		}
	}

	private Navajo doProxy(Navajo in) throws ClientException {
		Header h = in.getHeader();
		if(h==null) {
			logger.error("Call without header");
			return null;
		}
		String service = h.getRPCName();
		return myClient.doSimpleSend(in,service);
	}
	
	private String getApplicationAttribute(String key) {
		ServletContext servletContext = this.getServletContext();
		String value = null;
		if(servletContext!=null) {
			value = servletContext.getInitParameter(key);
		}
		if(value!=null) {
			return value;
		}
		value = System.getenv(key);
		if(value!=null) {
			return value;
		}
		return System.getProperty(key);
	}

	private void setupClient(String server, String username, String password) {
		NavajoClientFactory.resetClient();
		myClient = NavajoClientFactory.getClient();
		if (username == null) {
			username = "demo";
		}
		myClient.setUsername(username);
		if (password == null) {
			password = "demo";
		}
		myClient.setPassword(password);
		if (server == null) {
			logger.info("No server supplied.");
			return;
		}
		myClient.setServerUrl(server);		
		myClient.setRetryAttempts(0);
	}
	

	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("Starting init");
		String server = getApplicationAttribute("NavajoServer");
		String username = getApplicationAttribute("NavajoUser");
		String password = getApplicationAttribute("NavajoPassword");
		setupClient(server, username, password);
		logger.debug("Finished init");
	}

}
