/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on Jun 29, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.dexels.navajo.tipi.vaadin.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.dexels.navajo.tipi.TipiBreakException;
import com.dexels.navajo.tipi.TipiException;
import com.dexels.navajo.tipi.TipiSuspendException;
import com.dexels.navajo.tipi.internal.TipiEvent;
import com.dexels.navajo.tipi.vaadin.actions.base.TipiVaadinActionImpl;

/**
 * @author Administrator
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TipiShowQuestion extends TipiVaadinActionImpl {

	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiShowQuestion.class);
	
   private static final long serialVersionUID = -7235686945038762814L;

	/*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.tipi.internal.TipiAction#execute(com.dexels.navajo.tipi.internal.TipiEvent)
     */
    @Override
	protected void execute(TipiEvent event) throws TipiBreakException, TipiException, TipiSuspendException {
        String text  = (String) getEvaluatedParameterValue("text", event);
        ConfirmDialog.show(getApplication().getMainWindow(),"",text,"Bevestigen","Annuleren",new ConfirmDialog.Listener(){

			@Override
			public void onClose(ConfirmDialog dialog) {
				if(dialog.isConfirmed()) {
					logger.info("CONFIRMed question");
					try {
						continueAction(getEvent());
					} catch (TipiBreakException e) {
						logger.debug("Error: ",e);
					} catch (TipiException e) {
						logger.error("Error: ",e);
					} catch (TipiSuspendException e) {
					}
				} else {
					// shouldn't do this: Vaadin framework has no idea how to handle a tipibreak exception
//					throw new TipiBreakException();
				}
			}});
        suspend();
    }




    
}
