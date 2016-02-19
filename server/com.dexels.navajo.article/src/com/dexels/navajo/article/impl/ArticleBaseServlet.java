package com.dexels.navajo.article.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.article.APIErrorCode;
import com.dexels.navajo.article.APIException;
import com.dexels.navajo.article.ArticleContext;
import com.dexels.navajo.article.ValidationStore;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.server.ConditionErrorException;

public abstract class ArticleBaseServlet extends HttpServlet implements Servlet {
	private final static long serialVersionUID = -6895324256139435015L;
	private final static Logger logger = LoggerFactory.getLogger(ArticleBaseServlet.class);
	private ValidationStore validationStore;
	
	private ArticleContext context;
	
	public ArticleBaseServlet() {

	}
	
	public ArticleContext getContext() {
		return context;
	}

	public void setArticleContext(ArticleContext context) {
		this.context = context;
	}

	public void clearArticleContext(ArticleContext context) {
		this.context = null;
	}
	
	public void setValidationStore(ValidationStore validationStore) {
		this.validationStore = validationStore;
	}
	
	public void clearValidationStore() {
		this.validationStore = null;
	}

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setContentType("application/json; charset=utf-8");
			
			doServiceImpl(request, response);
		} catch (Throwable t1) {
			APIException exception = (t1 instanceof APIException) ? (APIException)t1 : new APIException(t1.getMessage(), t1, APIErrorCode.InternalError);
			
			//If we get an internal error, we want to know about it in our logging system.
			if (exception.getErrorCode().getHttpStatusCode() >= 500) {
				logger.error("Error {}", t1);
			}
			
			try {
				writeJSONErrorResponse(exception, response);
			} catch (Throwable t2) {
				logger.error("Failed to error in JSON {}", t2);
				
				try {
					APIErrorCode internal = APIErrorCode.InternalError;
					response.sendError(internal.getHttpStatusCode(), internal.getDescription());
				} catch (Throwable t3) {
					//We've failed to return the error to the user. We cannot do anything anymore.
					logger.error("Failed to deliver error {}", t3);
				}
			}
		}
	}
	
	private void writeJSONErrorResponse(APIException exception, HttpServletResponse response) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode error = mapper.createObjectNode();
		
		//We want all condition errors to be returned to the user. 
		if (exception.getCause() != null && exception.getCause() instanceof ConditionErrorException) {
			ConditionErrorException conditionErrorException = (ConditionErrorException) exception.getCause();
			
			ArrayNode messages = mapper.createArrayNode();
			for (Message message : conditionErrorException.getNavajo().getMessage("ConditionErrors").getElements()) {
				ObjectNode conditionError = mapper.createObjectNode();
				
				//It should always be there
				String id = message.getProperty("Id").getValue();
				conditionError.put("id", id);
				
				//Try to find the localized description.
				String description = validationStore.getDescriptionById(id);
				if (description != null) {
					conditionError.put("description", description);	
				} else {
					conditionError.put("description", message.getProperty("Description").getValue());
				}
				
				
								
				messages.add(conditionError);
			}
			error.put("messages", messages);
			
		} else {
			error.put("message", exception.getErrorCode().getDescription());
		}
		
		error.put("code", exception.getErrorCode().getExternalCode());
		rootNode.put("error", error);
		response.setStatus(exception.getErrorCode().getHttpStatusCode());
		PrintWriter pw = response.getWriter();
		mapper.writer().writeValue(pw, rootNode);
		response.getWriter().close();
	}
	
	protected abstract void doServiceImpl(HttpServletRequest request, HttpServletResponse response) throws APIException;
}
