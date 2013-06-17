package com.dexels.navajo.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Property;

public class Entity  {

	public static final String NAVAJO_URI = "navajo://";

	private Message myMessage;
	private Set<Key> myKeys = new HashSet<Key>();
	private EntityManager em = null;
	private boolean activated = false;

	// Keep track of entities that are derived from this entity.
	private Set<Entity> subEntities = new HashSet<Entity>();
	private Set<Entity> superEntities = new HashSet<Entity>();
	
	public Entity(Message msg, EntityManager m) {
		myMessage = msg;
		em = m;
	}
	public Entity() {
	}
	/**
	 * Inject a new entity message.
	 */
	public void setMessage(Message entity) throws Exception {
		if ( myMessage != null && !myMessage.isEqual(entity) ) {
			// First deactivate.
			deactivate();
			myMessage = entity;
			activate();
		}
		myMessage = entity;
	}
	
	private void addSubEntity(Entity sub) throws EntityException {
		System.err.println("In addSubEntity: " + getName() + " <- " + sub.getName());
		if ( sub.equals(this) ) {
			Thread.dumpStack();
			throw new EntityException("Cannot add myself as subentity");
		}
		subEntities.add(sub);
	}
	
	public void addSuperEntity(Entity sub) throws EntityException {
		superEntities.add(sub);
		sub.addSubEntity(this);
	}
	
	
	/**
	 * When entity de-activates make sure that sub entities are deactivated.
	 * 
	 * @throws Exception
	 */
	protected synchronized void deactivate() throws EntityException {
		for ( Entity sub : subEntities ) {
			sub.deactivate();
		}
		activated = false;
	}
	
	protected synchronized void activate() throws EntityException {
		if ( activated ) {
			return;
		}
		// Set activate flag immediately to prevent looping
		activated = true;
		findSuperEntities();
		findKeys();
		// Activate subEntities (if not already activated) 
		for ( Entity sub : subEntities ) {
			sub.activate();
		}
	}

	private Property getExtendedProperty(String ext) throws EntityException {
		if ( ext.startsWith(NAVAJO_URI) ) {
			String s = ext.substring(NAVAJO_URI.length());
			String entityName = s.split("/")[0];
			String propertyName = s.split("/")[1];
			Message msg = em.getEntity(entityName).getMessage();
			return msg.getProperty(propertyName);
		} else {
			throw new EntityException("Extension type not implemented: " + ext);
		}
	}

	private void processExtendedProperties(Message m) throws EntityException {
		for ( Property p : m.getAllProperties() ) {
			if ( p.getExtends() != null ) {
				Property ep = getExtendedProperty(p.getExtends());
				m.removeProperty(m.getProperty(ep.getName()));
				if ( p.getKey() == null && ep.getKey() != null ) {
					p.setKey(ep.getKey());
				}
			}
		}
		if ( m.isArrayMessage() && m.getDefinitionMessage() != null ) {
			processExtendedProperties(m.getDefinitionMessage());
		}
		for ( Message c : m.getAllMessages() ) {
			processExtendedProperties(c);
		}
		
	}
	
	private void processExtendedEntity(Message m, String extendedEntity) throws EntityException {
		if ( em.getEntity(extendedEntity) != null ) {
			// Copy properties/messages from superEntity.
			Entity superEntity = em.getEntity(extendedEntity);
			addSuperEntity(superEntity);
			myMessage.merge(superEntity.getMessage());
			// Check extended properties.
			processExtendedProperties(myMessage);
		} else {
			throw new EntityException("Could not find super entity: " + extendedEntity + " for entity: " + getName());
		}
	}
	
	private void findSuperEntities() throws EntityException {

		if ( myMessage.getExtends() != null ) {
			if ( !"".equals(myMessage.getExtends()) && myMessage.getExtends().startsWith(NAVAJO_URI) ) {
				String ext = myMessage.getExtends().substring(NAVAJO_URI.length());
				String [] superEntities = ext.split(",");
				for ( int i = 0; i < superEntities.length; i++ ) {
					processExtendedEntity(myMessage, superEntities[i]);
				}
			} else if (!"".equals(myMessage.getExtends()) ){
				throw new EntityException("Extension type not supported: " + myMessage.getExtends());
			}
		}
	}

	private void findKeys() {
		
		myKeys.clear();
		HashMap<String,Key> foundKeys = new HashMap<String,Key>();
		List<Property> allProps = myMessage.getAllProperties();
		int keySequence = 0;
		
		for ( Property p : allProps ) {
			if ( p.getKey() != null && p.getKey().indexOf("true") != -1 ) {
				String id = Key.getKeyId(p.getKey());
								if ( id == null ) {
					id = ""+(keySequence++);
				}
				Key key = null;
				if ( ( key = foundKeys.get(id) ) == null ) {
					key = new Key(id, this);
					foundKeys.put(id, key);
					myKeys.add(key);
				} 
				key.addProperty(p);
			}
		}

	}

	public Message getMessage() {
		return myMessage;
	}

	public String getName() {
		if(myMessage==null) {
			return "bullshit";
		}
		return myMessage.getName();
	}

	public Key getKey(Set<Property > p) {
		for ( Key k: myKeys ) {
			if ( k.keyMatch(p) ) {
				return k;
			}
		}
		return null;
	}
	
	public Key getKey(List<Property > p) {
		return getKey(new HashSet<Property>(p));
	}

	public Key getKey(String id) {
		for ( Key k: myKeys ) {
			if ( k.getId().equals(id) ) {
				return k;
			}
		}
		return null;
	}

	public Set<Key> getKeys() {
		return myKeys;
	}

	@Override
	public int hashCode() {
		if ( getName() != null ) {
			return getName().hashCode();
		} else {
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o == null ) {
			return false;
		}
		if ( o instanceof Entity ) {
			Entity other = (Entity) o;
			if ( other.getName() == null && getName() == null ) {
				return true;
			}
			if ( other.getName() == null || getName() == null ) {
				return false;
			}
			if ( this.getName().equals(other.getName())) {
				return true;
			} else {
				return false;
			}
		} 
		return false;
	}

	public boolean isInsertable(Navajo input) {
		return true;
	}

	public boolean isUpdateable(Navajo input) {
		return true;
	}
}
