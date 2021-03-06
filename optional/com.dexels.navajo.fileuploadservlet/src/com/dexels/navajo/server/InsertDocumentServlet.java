/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.client.NavajoClientFactory;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.types.Binary;

public class InsertDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	private final static Logger logger = LoggerFactory
			.getLogger(InsertDocumentServlet.class);
	
	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		PrintWriter pw = new PrintWriter(response.getWriter());
		
		String server = getApplicationAttribute("NavajoServer");
		String username = getApplicationAttribute("NavajoUser");

		pw.write("Upload servlet up and running\n");
		pw.write("Server: " + server + "\n");
		pw.write("User  : " + username);
		pw.close();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//		PrintWriter pw = new PrintWriter(response.getWriter());
		if (isMultipart) {
			try {
				
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				List<FileItem> items = upload.parseRequest(request);

				String author = request.getParameter("author");
				if(author == null){
					author = "unknown";
				}
				
				String lucene = request.getParameter("lucene");
				boolean useLucene = true;
				if(lucene != null && "false".equals(lucene)){
					useLucene = false;
				}
				
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
				    FileItem item = iter.next();				
				    if(!item.isFormField()){
				    	processFile(author, item, useLucene);
				    }				   
				}
				response.setStatus(HttpServletResponse.SC_OK);
				
				

				
			} catch (Exception e) {
				logger.error("Error: ", e);
			}
		} else {

		}
		
		//response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
//		pw.write("<html><head><script type=\"text/javascript\" src=\"js/fields.js\"></script><script type=\"text/javascript\">$(document).ready(function() { uploadReady(); }</script></head></html>");
//		pw.close();
	}
	
	private final void processFile(String author, FileItem item, boolean useLucene){
		try{
			String server = getApplicationAttribute("NavajoServer");
			String user = getApplicationAttribute("NavajoUser");
			String password = getApplicationAttribute("NavajoPassword");
			
			NavajoClientFactory.getClient().setServerUrl(server);
		    NavajoClientFactory.getClient().setUsername(user);
		    NavajoClientFactory.getClient().setPassword(password);
			
			String fileName = item.getName();
		    InputStream is = item.getInputStream();
		    Binary b = new Binary(is);	    
		    
		    if(useLucene){
			    Navajo ins = NavajoClientFactory.getClient().doSimpleSend(null, "lucene/InitInsertDocument");
			    Message insert = ins.getMessage("DocumentData");
			    insert.getProperty("AuthorName").setValue(author);
	
			    insert.getProperty("Name").setValue(fileName);
			    insert.getProperty("Data").setValue(b);
			    
			    NavajoClientFactory.getClient().doSimpleSend(ins, "lucene/ProcessInsertDocument");
		    } else {
		    	Navajo ins = NavajoClientFactory.getClient().doSimpleSend(null, "documents/InitInsertDocument");
			    Message insert = ins.getMessage("NewDocument");
			    
			    insert.getProperty("ObjectId").setValue(author);
			    insert.getProperty("ObjectType").setValue("DOCUMENT");
			    insert.getProperty("Name").setValue(fileName);
			    insert.getProperty("Data").setValue(b);
			    
			    NavajoClientFactory.getClient().doSimpleSend(ins, "documents/ProcessInsertDocument");
			    
		    }
		   
		}catch(Exception e){
			logger.error("Error: ", e);
		}
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

}
