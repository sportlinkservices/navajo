package com.dexels.navajo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.NavajoDoneException;
import com.dexels.navajo.script.api.SystemException;
import com.dexels.navajo.script.api.UserException;

public class StressTestHandler  extends ServiceHandler {
    private final static Logger logger = LoggerFactory.getLogger(StressTestHandler.class);
    @Override
    public Navajo doService()
            throws NavajoException, UserException, SystemException, AuthorizationException, NavajoDoneException {
        if (access.getInDoc().getMessage("stress") == null || access.getInDoc().getMessage("stress").getProperty("sleep") == null) {
            throw new UserException(-1, "Stresstest mode but no sleep parameter set!");
        }
        Long sleep = (Long) access.getInDoc().getMessage("stress").getProperty("sleep").getTypedValue();
        logger.debug("Running: {}; Going to sleep {}",access.getRpcName(), sleep);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return NavajoFactory.getInstance().createNavajo();
    }

    @Override
    public boolean needsRecompile() throws Exception {
        return false;
    }

}
