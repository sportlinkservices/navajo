package com.dexels.navajo.tipi.components.echoimpl;

import java.net.URL;

import com.dexels.navajo.echoclient.components.*;
import com.dexels.navajo.tipi.*;
import com.dexels.navajo.tipi.components.echoimpl.impl.*;
import com.dexels.navajo.tipi.components.echoimpl.parsers.*;
import com.dexels.navajo.tipi.internal.TipiEvent;

import nextapp.echo2.app.*;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import echopointng.*;
import echopointng.image.URLImageReference;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Frank Lyaruu
 * @version 1.0
 */

public class TipiButton extends TipiEchoComponentImpl {
    private Button myButton;

    public TipiButton() {
    }

    public Object createContainer() {
        // ContainerEx ex = new ContainerEx();
        myButton = new ButtonImpl();
//        myButton.setTextAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
//        myButton.setAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
        myButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				try {
					performTipiEvent("onActionPerformed",null, false);
				} catch (TipiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});
        return myButton;
    }

//    public void processStyles() {
////        System.err.println("Processing styles.... "+styleHintMap);
//        super.processStyles();
//        Color c = ColorParser.parseColor(getStyle("foreground"));
//        if (c!=null) {
//            myButton.setForeground(c);
//        }
//        c = ColorParser.parseColor(getStyle("background"));
//        if (c!=null) {
//            myButton.setBackground(c);
//        }
//        c = ColorParser.parseColor(getStyle("pressedforeground"));
//        if (c!=null) {
//            myButton.setPressedForeground(c);
//        }
//        c = ColorParser.parseColor(getStyle("pressedbackground"));
//        if (c!=null) {
//            myButton.setPressedBackground(c);
//        }
//        c = ColorParser.parseColor(getStyle("rolloverbackground"));
//        if (c!=null) {
//            myButton.setRolloverBackground(c);
//        }
//        c = ColorParser.parseColor(getStyle("rolloverforeground"));
//        if (c!=null) {
//            myButton.setRolloverForeground(c);
//        }
//      
//    }
    /**
     * getComponentValue
     * 
     * @param name
     *            String
     * @return Object
     * @todo Implement this
     *       com.dexels.navajo.tipi.components.core.TipiComponentImpl method
     */
    protected Object getComponentValue(String name) {
        return "";
    }
    
    protected void performComponentMethod(String name, TipiComponentMethod compMeth, TipiEvent event) {
        if ("fireAction".equals(name)) {
          for (int i = 0; i < getEventList().size(); i++) {
            TipiEvent current = getEventList().get(i);
            if (current.isTrigger("onActionPerformed", "aap")) {
              try {
//            	  System.err.println("Button performing action (explicit fire): "+current.getEventName());
                current.performAction(current,current,0);
              }
              catch (TipiException ex) {
                ex.printStackTrace();
              } catch (TipiBreakException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
          }
        }
      }

    public Object getActualComponent() {
        return myButton;
    }

    protected void setComponentValue(String name, Object object) {
        // Button b = (Button) getContainer();
       if ("text".equals(name)) {
            myButton.setText("" + object);
        }
       if ("visible".equals(name)) {
    	   System.err.println("Setting visible to: "+object);
           myButton.setVisible((Boolean)object);
       }

        if ("tooltip".equals(name)) {
            myButton.setToolTipText("" + object);
        }
//        if ("style".equals(name)) {
//            myButton.setStyle(Styles.DEFAULT_STYLE_SHEET.getStyle(Button.class, (String)object));
//        }
        if ("icon".equals(name)) {
            if (object instanceof URL) {
                URL u = (URL) object;
//                System.err.println("Setting URL icon for button: "+u);
                myButton.setIcon(new URLImageReference(u));
            } else {
                System.err.println("Can not set button icon: I guess it failed to parse (TipiButton)");
            }
        }
        
         super.setComponentValue(name, object);
    }

}
