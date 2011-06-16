package com.dexels.navajo.tipi.vaadin.components;

import com.dexels.navajo.tipi.vaadin.components.base.TipiVaadinComponentImpl;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class TipiWindow extends TipiVaadinComponentImpl {

	private Window window;

	@Override
	public Object createContainer() {
		window = new Window();
		window.setPositionX(30);
		window.setPositionY(30);
		window.setWidth(200, Sizeable.UNITS_PIXELS);
		window.setHeight(200, Sizeable.UNITS_PIXELS);
		getVaadinApplication().getMainWindow().addWindow(window);
		return window;
	}

	
	
	  @Override
	public void addToContainer(Object c, Object constraints) {
		  
		  super.addToContainer(c, constraints);
		  Component cc = (Component)c;
		  cc.setWidth("100%");
	  }



	public void setComponentValue(final String name, final Object object) {
		    super.setComponentValue(name, object);
		        if (name.equals("title")) {
		          window.setCaption( (String) object);
		        }
		        if ("icon".equals(name)) {
		        	window.setIcon( getResource(object));
		        }
		        if ("h".equals(name)) {
		        	window.setHeight(""+object+"px");
		        }
		        if ("w".equals(name)) {
		        	window.setWidth(""+object+"px");
		        }
		        if ("x".equals(name)) {
		        	window.setPositionX((Integer) object);
		        }
		        if ("y".equals(name)) {
		        	window.setPositionY((Integer) object);
		        }
		        if ("visible".equals(name)) {
		        	window.setVisible((Boolean) object);
		        }

		       
		  }
	

}
