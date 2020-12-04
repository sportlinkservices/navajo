/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on Jul 11, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.dexels.navajo.echoclient.components;

import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Style;

public class TableHeaderImpl extends ButtonImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9192980190733037816L;

	public TableHeaderImpl() {
		super();
		setup();
	}

	public TableHeaderImpl(String arg0) {
		super(arg0);
		setup();
	}

	public TableHeaderImpl(ImageReference arg0) {
		super(arg0);
		setup();
	}

	public TableHeaderImpl(String arg0, ImageReference arg1) {
		super(arg0, arg1);
		setup();
	}

	private void setup() {
		// setStyleName("Default");
		Style ss = Styles.DEFAULT_STYLE_SHEET.getStyle(this.getClass(),
				"TableHeader");
		setLineWrap(false);
		// logger.info("Background: "+PROPERTY_BACKGROUND+" value: "+
		// ss.getProperty(this.PROPERTY_BACKGROUND));
		setStyle(ss);

		// setTextAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
		// setAlignment(new Alignment(Alignment.CENTER,Alignment.DEFAULT));
		// setBorder(new Border(1,new
		// Color(0x88,0x88,0x88),Border.STYLE_SOLID));
		// setRolloverBackground(new Color(0xff,0xff,0xff));
		// setRolloverForeground(new Color(0,0,0));
		// setRolloverBorder(new Border(1,new
		// Color(0x88,0x88,0x88),Border.STYLE_SOLID));
	}
}
