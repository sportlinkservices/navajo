package com.dexels.navajo.tipi.vaadin.layout;


import com.dexels.navajo.tipi.*;
import com.dexels.navajo.tipi.components.core.*;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TipiFlowLayout
    extends TipiLayoutImpl {
	HorizontalLayout layout = null;
  public TipiFlowLayout() {
  }

  public void createLayout() {
	 layout =  new HorizontalLayout();
	  setLayout(layout);
  }

  protected void setValue(String name, TipiValue tv) {
    throw new UnsupportedOperationException("Not implemented.");
  }
}