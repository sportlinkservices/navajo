package com.dexels.navajo.entity.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.adapter.NavajoMap;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Operation;
import com.dexels.navajo.entity.Entity;
import com.dexels.navajo.entity.EntityException;
import com.dexels.navajo.entity.EntityManager;
import com.dexels.navajo.entity.impl.ServiceEntityOperation;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.SystemException;
import com.dexels.navajo.script.api.UserException;
import com.dexels.navajo.server.ConditionErrorException;


public class EntityMap extends NavajoMap {

	private EntityManager myManager;
	private String entityName;
	private String method;
	private Entity myEntity;
	private boolean breakOnNoResult = false;
    private final static Logger logger = LoggerFactory.getLogger(EntityMap.class);
    private String myVersion = Entity.DEFAULT_VERSION;
	
	@Override
	public void load(Access access) throws MappableException, UserException {
		super.load(access);
		myManager = EntityManager.getInstance();
	}

	@Override
	public void setDoSend(String s, Navajo n) throws UserException, ConditionErrorException, SystemException {
		serviceCalled = false;
		serviceFinished = false;
		super.setDoSend(s, n);
	}
	
	public void setCall(boolean v) throws Exception {
		
		if ( entityName != null && method != null ) {
			Operation o = myManager.getOperation(entityName, method);
			o.setTenant(access.getTenant());
            ServiceEntityOperation seo = new ServiceEntityOperation(myManager, o, getVersion());
			Navajo request = prepareOutDoc();
			if ( request.getHeader() == null ) {
				Header h = NavajoFactory.getInstance().createHeader(request, myEntity.getName(), access.rpcUser, access.rpcName, -1);
				request.addHeader(h);
			}
			
			// If we do not have an input we add it. Because not all entities 
			// need an input but the perform requires a message with the entity
			// name.
			if (method.equals("GET")) {
				Message inputEntity = request.getMessage(myEntity.getMessageName());
				if (inputEntity == null) {
					inputEntity = NavajoFactory.getInstance().createMessage(request, myEntity.getMessageName());
					request.addMessage(inputEntity);
				}
			}

			Navajo result;
			try {
			    result = seo.perform(request);
			} catch (EntityException e) {
			    if (e.getCode() == EntityException.VALIDATION_ERROR) {
			        throw new ConditionErrorException(e.getNavajo());
			    }
			    throw e;
            } finally {
                // Set back default version
                setVersion("0");
                
			}
			
			if (breakOnNoResult && (result == null  || result.getMessage(seo.getMyEntity().getMessageName()) == null)) {
                throw new UserException(-1, "Entity "+ entityName + " not found or no output from operation");
            }
			
			this.serviceCalled = true;
			this.serviceFinished = true;
			this.inDoc = result;

		}
	}
	
	public void setBreakOnNoResult(boolean breaky) {
		this.breakOnNoResult = breaky;
	}
	
	@Override
	public void setPropertyName(String s) throws UserException {
		// Check if property name does not contain '/' character, i.e. it is a relative property name: append the entity name
		if ( s.indexOf("/") == -1 ) {
			s = "/" + myEntity.getMessageName() + "/" + s; 
		}
		super.setPropertyName(s);
	}
	
	@Override 
	public synchronized void waitForResult() throws UserException {
		super.waitForResult();
	}
	
	public String getName() {
		return entityName;
	}

	public void setName(String entityname) throws UserException{
		this.entityName = entityname.replace("/", ".");;
		this.myEntity = myManager.getEntity(this.entityName);
		if (this.myEntity == null) {
		    throw new UserException(-1, "Entity " + entityName + " not found!");
		}
	}

	public String getMethod() {
		return method;
	}

	@Override
	public void setMethod(String method) {
		this.method = method;
	}
	
	@Override
	public void kill() {
	}

    public void setVersion(String ver) {
        myVersion = ver;
    }

    public String getVersion() {
        logger.debug("Requesting version {} of entity {}", myVersion, myEntity.getName());
        return myVersion;
    }

}
