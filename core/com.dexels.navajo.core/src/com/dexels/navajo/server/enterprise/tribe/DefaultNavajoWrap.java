package com.dexels.navajo.server.enterprise.tribe;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.sharedstore.SerializationUtil;

/**
 * Wraps a Navajo object in a nice rug for cross Tribal transportation purposes
 * 
 * @TODO: Register DefaultNavajoWrap object with Garbage Collector to remove 'old' objects.
 * 
 * @author arjenschoneveld
 * 
 *
 */
public class DefaultNavajoWrap implements NavajoRug {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4689405438923422437L;
	private transient Navajo myNavajo = null;
	private final static AtomicLong uniqueId = new AtomicLong();
	
	public final String reference;
	
	private final static Logger logger = LoggerFactory.getLogger(DefaultNavajoWrap.class);
	
	public DefaultNavajoWrap(Navajo n) {
		if ( n == null ) {
			logger.error("Cannot wrap null Navajo");
		}
		reference = SerializationUtil.serializeNavajo(n, uniqueId.incrementAndGet() + ".xml");
		logger.info("Created DefaultNavajoWrap: " + reference);
	}
	
	public DefaultNavajoWrap(Navajo n, String id) {
		if ( n == null ) {
			logger.error("Cannot wrap null Navajo: " + id);
		}
		if ( !id.endsWith(".xml") ) {
			id = id + ".xml";
		}
		reference = SerializationUtil.serializeNavajo(n, id + ".xml");
		logger.info("Created DefaultNavajoWrap: " + reference);
	}
	
	public Navajo getNavajo() {
		if ( myNavajo == null ) {
			myNavajo = SerializationUtil.deserializeNavajo(reference);
			if ( myNavajo == null ) {
				logger.error("Could not de-serialize Navajo object: " + reference);
			}
		}
		logger.info("Getting DefaultNavajoWrap: " + reference);
		return myNavajo;
	}
	
}
