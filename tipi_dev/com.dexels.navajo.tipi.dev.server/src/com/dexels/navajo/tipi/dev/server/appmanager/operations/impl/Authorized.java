package com.dexels.navajo.tipi.dev.server.appmanager.operations.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dexels.navajo.repository.api.AppStoreOperation;
import com.dexels.navajo.repository.api.RepositoryInstance;

public class Authorized extends BaseOperation implements AppStoreOperation {

	
	private static final long serialVersionUID = 8640712571228602628L;
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession();
		
		resp.setContentType("application/json");
		Boolean authorized = (Boolean) session.getAttribute("authorized");
		String username = (String) session.getAttribute("username");
		String image = (String) session.getAttribute("image");
		String company = (String) session.getAttribute("company");
		String email = (String) session.getAttribute("email");
		Map<String,Object> result = new HashMap<String, Object>();
		if(authorized==null) {
			authorized = false;
		}

		result.put("username", username);
		result.put("company", company);
		result.put("image", image);
		result.put("email", email);
		result.put("authorized", authorized);
		result.put("clientid", appStoreManager.getClientId());
		final String state = generateRandom();
		result.put("state", state);
		session.setAttribute("state", state);
//		https://github.com/login/oauth/authorize
 		writeValueToJsonArray(resp.getOutputStream(),result);
			
	}
	
	// TODO Re-use random, it's expensive to initialize
	private String generateRandom() {
		 SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

	@Override
	public void build(RepositoryInstance a) throws IOException {

	}
}
