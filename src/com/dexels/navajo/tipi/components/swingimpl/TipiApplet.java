package com.dexels.navajo.tipi.components.swingimpl;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import tipi.*;

public class TipiApplet extends JApplet {
	private SwingTipiContext myContext;
	public TipiApplet() throws Exception {
 }

	public void destroy() {
		myContext.shutdown();
		super.destroy();
	}

	public void init() {
		super.init();
		List<String> arguments = new ArrayList<String>();
		String init = this.getParameter("init");
	//		System.err.println("LocationOnScreen: "+getLocationOnScreen());
		String laf = this.getParameter("tipilaf");
		String tipiCodeBase = this.getParameter("tipiCodeBase");
		String resourceCodeBase = this.getParameter("resourceCodeBase");
		String switches = getParameter("switch");
		if(switches!=null) {
			StringTokenizer st = new StringTokenizer(switches," ");
			while (st.hasMoreTokens()) {
				arguments.add(st.nextToken());
			}
		}
		if(tipiCodeBase!=null) {
			try {
				String tipiCode = new URL(getCodeBase(),tipiCodeBase).toString();
				arguments.add("tipiCodeBase="+tipiCode);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		if(resourceCodeBase!=null) {
			try {
				String resourceCode = new URL(getCodeBase(),resourceCodeBase).toString();
				arguments.add("resourceCodeBase="+resourceCode);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		if(laf!=null) {
			try {
				UIManager.setLookAndFeel(laf);
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Throwable e) {
				e.printStackTrace();
			} 
		}
		if (init!=null) {
			arguments.add(init);
		} else {
			throw new IllegalArgumentException("Missing argument: Add 'init' argument to applet.");
		}
		try {
			myContext = MainApplication.initialize(init, null,arguments,this,null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void reload() {
//		TipiComponent tc = myContext.getDefaultTopLevel();
//		if(tc!=null) {
//			tc.disposeComponent();
//		}
//		removeAll();
		myContext.shutdown();
		try {
		// TODO: Setup in applet parameter
			getAppletContext().showDocument(new URL("http://penelope1.dexels.com/sportlink/club"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
//		init();
//		repaint();
	}
	
	 public Point getCenteredPoint(Dimension dlgSize) {
		 	Point base = getLocationOnScreen();
//		 Rectangle r = getRootPaneContainer().getRootPane().getBounds();
		    Dimension frmSize = new Dimension(getWidth(), getHeight());
//		    Point loc = getRootPaneContainer().getRootPane().getLocation();
		    int x =  Math.max(0, (frmSize.width - dlgSize.width) / 2 + base.x);
		    int y = Math.max(0, (frmSize.height - dlgSize.height) / 2+ base.y);
		    return new Point(x, y);

//
//		    if (dlgSize.height>(Toolkit.getDefaultToolkit().getScreenSize().height)) {
//		      dlgSize.height = Toolkit.getDefaultToolkit().getScreenSize().height;
//		      dlg.setSize(dlgSize);
//		    }
//
//		    if (dlgSize.width>Toolkit.getDefaultToolkit().getScreenSize().width) {
//		      dlgSize.width = Toolkit.getDefaultToolkit().getScreenSize().width;
//		      dlg.setSize(dlgSize);
//		   }

		  }
	
}
