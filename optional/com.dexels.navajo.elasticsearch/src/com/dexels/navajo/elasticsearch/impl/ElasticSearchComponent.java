/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.elasticsearch.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.elasticsearch.ElasticSearchFactory;
import com.dexels.navajo.elasticsearch.ElasticSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ElasticSearchComponent implements ElasticSearchService {

	private final static Logger logger = LoggerFactory.getLogger(ElasticSearchComponent.class);
	
	private CloseableHttpClient httpclient;

	private String url;
	private String index;
	private String id_property;

	DateFormat df = null;
	private String type;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public void activate(Map<String,Object> settings) {
	     df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	    
	     
	     
		logger.info("Activating elasticsearch");
		httpclient = HttpClients.createDefault();
		this.url = (String)settings.get("url");
		this.index = (String)settings.get("index");
		this.type = (String)settings.get("type");
		this.id_property = (String)settings.get("id_property");
		ElasticSearchFactory.setInstance(this);
	}
	
	public void deactivate() {
		logger.info("Deactivating elasticsearch");
		ElasticSearchFactory.setInstance(null);
		if(httpclient!=null) {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error("Error: ", e);
			}
		}

	}
	
	@Override
	public void insert(Message m) throws IOException {
		ObjectNode mm = (ObjectNode) messageToJSON(m);
		String id = m.getProperty(this.id_property).getValue();
		try {
			putJSON(id,mm);
		} catch (URISyntaxException e) {
			throw new IOException("Error putting to URI",e);
		}
	}
	@Override
    public void insertJson(String jsonString) throws IOException {
        ObjectNode mm =  (ObjectNode)  objectMapper.readTree(jsonString);
		String id = mm.get(this.id_property).asText();

        mm.put("@timestamp", df.format(new Date()));
        try {
            putJSON(id,mm);
        } catch (URISyntaxException e) {
            throw new IOException("Error putting to URI",e);
        }
    }


	public JsonNode messageToJSON(Message message) {
		if (Message.MSG_TYPE_ARRAY.equals(message.getType())) {
			List<Message> msgs = message.getAllMessages();
			ArrayNode an = objectMapper.createArrayNode();
			for (Message m : msgs) {
				an.add(messageToJSON(m));
			}
			return an;
		} else {
			List<Message> msgs = message.getAllMessages();
			ObjectNode an = objectMapper.createObjectNode();
			for (Message m : msgs) {
				an.set(m.getName(), messageToJSON(m));
			}
			List<Property> properties = message.getAllProperties();
			for (Property property : properties) {
				if(!property.getName().equals(id_property)) {
					setPropertyValue(an,property,objectMapper);
				}
			}
			
			an.put("@timestamp", df.format(new Date()));
			return an;
		}

	}

	private void setPropertyValue(ObjectNode an, Property property,ObjectMapper objectMapper) {
		if(Property.DATE_PROPERTY.equals(property.getType())) {
			Date d = (Date) property.getTypedValue();
			
			
			an.put(property.getName(),df.format(d));
		} else if(Property.INTEGER_PROPERTY.equals(property.getType())) {
			Integer d = (Integer) property.getTypedValue();
			an.put(property.getName(),d);
		} else if(Property.BOOLEAN_PROPERTY.equals(property.getType())) {
			Boolean d = (Boolean) property.getTypedValue();
			an.put(property.getName(),d);
		} else if(Property.BINARY_PROPERTY.equals(property.getType())) {
			Binary b = (Binary) property.getTypedValue();
			an.put(property.getName(),b.getData());
		} else {
			an.put(property.getName(), property.getValue());
		}
		
	}

	
	private void putJSON(String id, ObjectNode node) throws ClientProtocolException, IOException, URISyntaxException {
	    replaceDots(node);
		byte[] requestBytes = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsBytes(node);
//		String id = node.get(this.id_property).asText();
		HttpPut httpPut = new HttpPut(assembleURI(id));
		HttpEntity he = new ByteArrayEntity(requestBytes);
		httpPut.setEntity(he);
		CloseableHttpResponse response1 = httpclient.execute(httpPut);
		//HttpEntity respe = response1.getEntity();
		//EntityUtils.toString(respe);
		// response1.getEntity().getContent()
		response1.close();
	}
	
	 private void replaceDots(ObjectNode mm) {
	        Iterator<Entry<String, JsonNode>> it = mm.fields();
	        Set<String> fieldsWithDots = new HashSet<>();
	        Set<String> nestedFields = new HashSet<>();
	        
	        while (it.hasNext()) {
	            Entry<String, JsonNode> entry = it.next();
	            if (entry.getValue().size() > 0)  {
	                nestedFields.add(entry.getKey());
	            }
	            if (entry.getKey().contains(".")) {
	                fieldsWithDots.add(entry.getKey());
	            }
	        }
	        for (String key : nestedFields) {
	            replaceDots((ObjectNode) mm.get(key));
	        }
	        for (String key : fieldsWithDots) {
	            String newKey = key.replace(".", "_");
	            JsonNode value = mm.get(key);
	            mm.set(newKey, value);
	            mm.remove(key);
	        }
	    }

	private URI assembleURI(String id) throws URISyntaxException {
		StringBuilder sb = new StringBuilder(url);
		if(!url.endsWith("/")) {
			sb.append("/");
		}
		sb.append(index);
		if(!index.endsWith("/")) {
			sb.append("/");
		}
		sb.append(type);
		if(!type.endsWith("/")) {
			sb.append("/");
		}
		sb.append(id);
		return new URI(sb.toString());
	}

}
