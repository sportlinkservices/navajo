package com.dexels.navajo.tipi.vaadin.touch.components;

import com.dexels.navajo.tipi.vaadin.components.base.TipiVaadinComponentImpl;
import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class TipiNavigationManager extends TipiVaadinComponentImpl {

	private static final long serialVersionUID = -5043895594246857632L;

	@Override
	public Object createContainer() {
		NavigationManager nm = new NavigationManager();
//		nm.setHeight(getMainWindow().getHeight(), getMainWindow().getHeightUnits());
		return nm;
	}

	@Override
	public void setComponentValue(final String name, final Object object) {
	    super.setComponentValue(name, object);
	        if (name.equals("title")) {
	           getVaadinContainer().setCaption( (String) object);
	        }
	}

	@Override
	protected void addToVaadinContainer(ComponentContainer currentContainer,
			Component component, Object constraints) {
		NavigationManager nm = (NavigationManager)currentContainer;
		nm.navigateTo(component);
		}
	
	
}
