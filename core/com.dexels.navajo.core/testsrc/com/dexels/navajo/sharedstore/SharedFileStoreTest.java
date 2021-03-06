/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.sharedstore;

import java.io.File;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dexels.navajo.server.DispatcherFactory;
import com.dexels.navajo.server.enterprise.tribe.DefaultTribeManager;
import com.dexels.navajo.server.enterprise.tribe.TribeManagerFactory;
import com.dexels.navajo.server.test.TestDispatcher;
import com.dexels.navajo.server.test.TestNavajoConfig;

public class SharedFileStoreTest {

	SharedFileStore si;
	
	@Before
	public void setUp() throws Exception {
		TribeManagerFactory.setInstance(new DefaultTribeManager());
		DispatcherFactory.createDispatcher(new TestDispatcher(new TestNavajoConfig()));
		DispatcherFactory.getInstance().setUseAuthorisation(false);
		si = new SharedFileStore(new File("sharedstore"), new TestNavajoConfig());
	}
	
	@Test
	public void testInit() {
		System.err.println("si: " + si);
	}
	
	@Test 
	public void testStore() throws Exception {
		si.store("parent", "aap", "noot", false, false);
		String r = (String) si.get("parent", "aap");
		Assert.assertEquals("noot", r);
	}
	
	@Test
	public void testPerformance() throws Exception {
		int count = 1000;
		long start = System.currentTimeMillis();
		for ( int i = 0; i < count; i++ ) {
			si.store("PARENT", "Key"+i, Integer.valueOf(i), false, false);
		}
		long end = System.currentTimeMillis();
		//Assert.assertEquals(count, si.getSize());
		System.err.println("STORED " + count + " values in " + ( (end - start) / (double) count ) + " millis");
		
		start = System.currentTimeMillis();
		for ( int i = 0; i < count; i++ ) {
			Serializable s = si.get("PARENT", "Key"+i);
		}
		end = System.currentTimeMillis();
		System.err.println("GOT " + count + " values in "  + ( (end - start) / (double) count ) + " millis");
		//Assert.assertEquals(true, ( end - start ) < 200 );
	}
}
