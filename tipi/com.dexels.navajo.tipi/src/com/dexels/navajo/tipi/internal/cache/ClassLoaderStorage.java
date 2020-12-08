/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.internal.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface ClassLoaderStorage {
	public boolean hasLocal(String location) throws IOException;

	public InputStream getLocalData(String location) throws IOException;

	public URL getURL(String location, InputStream is) throws IOException;
	
	public void invalidate(String location);
}
