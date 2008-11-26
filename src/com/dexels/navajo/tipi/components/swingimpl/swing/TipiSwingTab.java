package com.dexels.navajo.tipi.components.swingimpl.swing;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;

import com.dexels.navajo.document.types.*;
import com.dexels.navajo.tipi.components.swingimpl.parsers.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class TipiSwingTab extends JPanel implements TipiTabbable {

	private String tabTooltip;
	private Icon tabIcon;
	private int index;
	private Color tabForegroundColor = null;
	private Color tabBackgroundColor = null;

	public String getTabTooltip() {
		return tabTooltip;
	}

	public void setTabTooltip(String tabToolTip) {
		String old = this.tabTooltip;
		this.tabTooltip = tabToolTip;
		firePropertyChange("tabToolTip", old, tabToolTip);
	}

	public Icon getTabIcon() {
		System.err.println("Returing icon: "+tabIcon);
		return tabIcon;
	}

	public void setTabIcon(Icon tabIcon) {
		Icon old = this.tabIcon;
		this.tabIcon = tabIcon;
		if(old==tabIcon) {
			System.err.println("whoops, identical");
		}
		firePropertyChange("tabIcon", old, tabIcon);
		System.err.println("Tab set!"+tabIcon);

	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Color getTabForegroundColor() {
		return tabForegroundColor;
	}

	public void setTabForegroundColor(Color tabForegroundColor) {
		Color old = this.tabForegroundColor;
		System.err.println("Setting fg");
		this.tabForegroundColor = tabForegroundColor;
		firePropertyChange("tabForegroundColor", old, tabBackgroundColor);
	}

	public Color getTabBackgroundColor() {
		System.err.println("GEtting bg: "+tabBackgroundColor);
		return tabBackgroundColor;
	}

	public void setTabBackgroundColor(Color c) {
		Color old = tabBackgroundColor;
		System.err.println("Setting bg");
		firePropertyChange("tabBackgroundColor", old, tabBackgroundColor);
	}

	public void setIconUrl(Object u) {
		setTabIcon(getIcon(u));
	}

	protected ImageIcon getIcon(Object u) {
		if (u == null) {
			return null;
		}
		if (u instanceof URL) {
			return new ImageIcon((URL) u);
		}
		if (u instanceof Binary) {
			Image i;
			try {
				i = ImageIO.read(((Binary) u).getDataAsStream());
				ImageIcon ii = new ImageIcon(i);
				return ii;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
