package com.dexels.navajo.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.config.runtime.TestConfig;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.elasticsearch.impl.ElasticSearchComponent;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestConnection {
	
	
	private static final Logger logger = LoggerFactory.getLogger(TestConnection.class);


	private static Navajo tmlDoc;
	@BeforeClass
	public static void parseTml() throws IOException {
		logger.info("parsing tml..");
		try(InputStream resourceAsStream = TestConnection.class.getClassLoader().getResourceAsStream("tmlexample.xml")) {
			tmlDoc = NavajoFactory.getInstance().createNavajo(resourceAsStream);
		}
	}
	@Test
	public void testInsert() throws IOException {
		ElasticSearchComponent esc = new ElasticSearchComponent();
		Map<String,Object> settings = new HashMap<>();
		String server = TestConfig.ELASTICSEARCH_SERVER.getValue();
		if(server==null) {
			logger.warn("No server defined, skipping test");
			return;
		}
		settings.put("url", server);
		settings.put("index", TestConfig.ELASTICSEARCH_INDEX);
		settings.put("type", TestConfig.ELASTICSEARCH_TYPE);
		settings.put("id_property", "_id");

		esc.activate(settings);
		Message m = tmlDoc.getMessage("Transaction");
		for (Message e : m.getAllMessages()) {
			ElasticSearchFactory.getInstance().insert(e);
		}
	}

	@Test
	public void testMessageToJSON() throws IOException {
		Message m = tmlDoc.getMessage("Transaction");
		ObjectMapper objectMapper = new ObjectMapper();
		ElasticSearchComponent e = new ElasticSearchComponent();
		e.activate(Collections.emptyMap());
		ArrayNode nn = (ArrayNode) e.messageToJSON(m);
		objectMapper.writer().withDefaultPrettyPrinter().writeValue(System.err, nn);
		Assert.assertEquals(m.getArraySize(), nn.size());
	}
}
