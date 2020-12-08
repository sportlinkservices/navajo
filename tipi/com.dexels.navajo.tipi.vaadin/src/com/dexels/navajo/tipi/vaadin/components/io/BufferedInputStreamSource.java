/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.vaadin.components.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.terminal.StreamResource.StreamSource;

public class BufferedInputStreamSource implements StreamSource {

	private static final long serialVersionUID = -3296924859018619919L;
	private final byte[] data;
	
	public BufferedInputStreamSource(byte[] data) {
		this.data = data;
	}
	
	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(data);
	}

}
