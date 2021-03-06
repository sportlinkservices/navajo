/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.server.enterprise.tribe;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.dexels.navajo.sharedstore.map.IntroductionRequest;
import com.dexels.navajo.sharedstore.map.SharedTribalMap;
import com.dexels.navajo.util.AuditLog;

/**
 * The MembershipSmokeSignal is used by a new Tribal Member to introduce itself.
 * 
 * @author arjen
 *
 */
public class MembershipSmokeSignal extends SmokeSignal implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4808643364226896337L;

	public static final String INTRODUCTION = "introduction";
	public static final String REMOVAL = "removal";
	
	public MembershipSmokeSignal(String key, Serializable value) {
		super(key, value);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void processMessage() {

		AuditLog.log(AuditLog.AUDIT_MESSAGE_TRIBEMANAGER, "MembershipSmokeSignal: PROCESS MESSAGE (" + getKey() + "/" + getValue() + ")");

		if ( getKey().equals(INTRODUCTION)  ) {
			TribeMemberInterface tm = (TribeMemberInterface) getValue();
			TribeManagerFactory.getInstance().addTribeMember(tm);
			
			/**
			 * The Chief is responsible to sharing the active 'tribal maps' by sending an IntroductionRequest
			 * to the new member.
			 * 
			 */
			if ( TribeManagerFactory.getInstance().getIsChief() ) {
				Collection<SharedTribalMap> c = SharedTribalMap.getAllTribalMaps();
				Iterator<SharedTribalMap> iter = c.iterator();
				while ( iter.hasNext() ) {
					IntroductionRequest ir = new IntroductionRequest(iter.next());
					TribeManagerFactory.getInstance().askSomebody(ir, tm.getAddress());
				}
			}
			
		}
	}

}
