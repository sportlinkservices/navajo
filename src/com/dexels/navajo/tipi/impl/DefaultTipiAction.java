package com.dexels.navajo.tipi.impl;

import com.dexels.navajo.tipi.*;
import com.dexels.navajo.document.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import tipi.*;
import com.dexels.navajo.tipi.tipixml.*;
import com.dexels.navajo.parser.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DefaultTipiAction extends TipiAction {

  public void execute(Navajo n, TipiContext context, Object source, Object event) throws TipiBreakException,TipiException {
    boolean validCondition = false;
    if(myCondition != null){
      validCondition = myCondition.evaluate(n, context, source, event);
    }else{
      validCondition = true;
    }
    if(validCondition){
      String path;
      Map params;
      switch (myType) {
        case TYPE_BREAK:
          throw new TipiBreakException(n, context);
        case TYPE_PERFORMMETHOD:
          performMethod(n, context, source);
          break;
        case TYPE_CALLSERVICE:
          callService(context, source);
          break;
        case TYPE_SETPROPERTYVALUE:
          setPropertyValue(n, context, source);
          break;
        case TYPE_INFO:
          showInfo(n, context, source);
          break;
        case TYPE_SHOWQUESTION:
          showQuestion(n, context, source);
          break;
        case TYPE_EXIT:
          System.exit(0);
          break;
//      case TYPE_SETVISIBLE:
//        setVisible(context, source);
//        break;
//      case TYPE_SETENABLED:
//        setEnabled(context, source);
//        break;
        case TYPE_LOADUI:
          loadUI(context, source);
          break;
        case TYPE_SETVALUE:
          setValue(context, source);
          break;
        case TYPE_COPYVALUE:
          copyValue(context, source);
          break;
        case TYPE_INSTANTIATE:
          instantiateTipi(context, source);
          break;
        case TYPE_COPYVALUETOMESSAGE:
          copyValueToMessage(context, source);
          break;
        case TYPE_PERFORMTIPIMETHOD:
          performTipiMethod(context, source);
          break;
        case TYPE_EVALUATEEXPRESSION:
          evaluateExpression(context, source);
          break;
        case TYPE_DISPOSE:
          disposeTipiComponent(context, source);
          break;
        case TYPE_DEBUG:
          debug(context, source);
          break;
      }
    }else{
      //System.err.println("Condition returned false, not performing this action");
    }
  }


  private void evaluateExpression(TipiContext context, Object source) throws TipiException {
//from_path

    // Change the urls to modern ones, from_path, to_path

    String from_path = (String)myParams.get("from_path");
    String to_path = (String)myParams.get("to_path");
    String to_name = (String)myParams.get("to_name");
    String from_name = (String)myParams.get("from_name");
    String expr = (String)getValueByPath(null,context,from_path);
    TipiComponent dest = getTipiComponentByPath(source,context, to_path);
//    (String)myParams.get("expression");
    String destname = (String)myParams.get("dest_value");
    TipiComponent destination = getTipiComponentByPath(source,context,to_path);
    Operand o;
    try {
      if (myComponent.getNavajo()==null) {
        System.err.println("Null navajo!");
      }

      o = Expression.evaluate(expr, myComponent.getNearestNavajo());
      //System.err.println("Type: "+o.type);
      dest.setValue(to_name, o.value.toString());

    }
    catch (TMLExpressionException ex) {
      ex.printStackTrace();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void debug(TipiContext context, Object source){
    String type = (String)myParams.get("type");
    String value = (String)myParams.get("value");
    if("object".equals(type)){
      TipiPathParser pp = new TipiPathParser((TipiComponent)source, context, value);
      int object_type = pp.getPathType();
      switch(object_type){
        case TipiPathParser.PATH_TO_COMPONENT:
          TipiComponent tp = pp.getComponent();
          Set valueSet = tp.getPossibleValues();
          Object[] values = valueSet.toArray();
          System.err.println(" ==> DEBUG, TipiComponent " + tp.getName() + " has values: ");
          for(int i=0;i<values.length;i++){
            String vv = (String)tp.getValue((String)values[i]);
            System.err.println("  - " + values[i] + " = " + vv);
          }
          break;
        case TipiPathParser.PATH_TO_MESSAGE:
          Message m = pp.getMessage();
          System.err.println(" ==> DEBUG MESSAGE: ");
          m.write(System.err);
          break;
        case TipiPathParser.PATH_TO_PROPERTY:
          Property p = pp.getProperty();
          System.err.println(" ==> DEBUG, Property " + p.getName() + " has value " + p.getValue().toString() + " and is of type " + p.getType());
          break;
        case TipiPathParser.PATH_TO_ATTRIBUTE:
          Object attr = pp.getAttribute();
          System.err.println(" ==> DEBUG, Attribute " + attr.toString());
          break;

        case TipiPathParser.PATH_TO_TIPI:
          TipiComponent tc = pp.getComponent();
          Set valueSet2 = tc.getPossibleValues();
          Object[] values2 = valueSet2.toArray();
          System.err.println(" ==> DEBUG, TipiComponent " + tc.getName() + " has values: ");
          for(int i=0;i<values2.length;i++){
            String vv = (String)tc.getValue((String)values2[i]);
            System.err.println("  - " + values2[i] + " = " + vv);
          }
          break;
        case TipiPathParser.PATH_TO_UNKNOWN:
          System.err.println("==> DEBUG: " + value);
          break;
        default:
          System.err.println("==> DEBUG: " + value);
          break;
      }
    }else{
      try {
        Operand o;
        context.setCurrentComponent((TipiComponent) source);
        o = Expression.evaluate(value, ((TipiComponent) source).getNearestNavajo(), null, null, null, context);
        if(o.value != null){
          value = o.value.toString();
        }else{
          value = "ERROR: Expression returned NULL";
        }
      }
      catch (Exception ex) {
        System.err.println("Error evaluating[" + value + "] inserting as plain text only");
        ex.printStackTrace();
      }

      System.err.println("==> DEBUG: " + value);
    }
  }

  private void instantiateTipi(TipiContext context, Object source) throws TipiException {
    String defname = (String)myParams.get("name");
    String id = (String)myParams.get("id");
    String location = (String)myParams.get("location");
    String forceString = (String)myParams.get("force");
    boolean force;
    if (forceString==null) {
      force = false;
    } else {
      force = forceString.equals("true");
    }
//    Operand o = evaluate((TipiComponent)source,context,value);
//    Object sourceObject = o.value;

    String componentPath = location + "/"+id;

    TipiPathParser tp = new TipiPathParser((TipiComponent)source,context,componentPath);
    TipiComponent comp =  (TipiComponent)tp.getTipi();
//    System.err.println("Force? "+forceString);
//    System.err.println("comp null? "+(comp==null));
    if (comp!=null) {
      if (force) {
//        System.err.println("Disposing: "+componentPath);
        context.disposeTipi(comp);
      } else {
        comp.reUse();
//        System.err.println("reusing");
        return;
      }
    }
//    System.err.println("Proceeding with instantiate");
    XMLElement xe = new CaseSensitiveXMLElement();
    xe.setName("component-instance");
    xe.setAttribute("name",defname);
    Iterator it = myParams.keySet().iterator();
    while (it.hasNext()) {
      String current = (String)it.next();
      xe.setAttribute(current,myParams.get(current));
    }
//    System.err.println("Instantiating: "+xe.toString());
    TipiComponent inst = context.instantiateComponent(xe);
    inst.setId(id);
    TipiComponent dest = getTipiComponentByPath(source,context,location);
    dest.addComponent(inst,context,null);
 }

  private void copyValueToMessage(TipiContext context, Object source){
    String from_path = (String)myParams.get("from_path");
    String to_path = (String)myParams.get("to_path");
    Object value = getValueByPath(null,context, from_path);
    //System.err.println("Value: " + value.toString());
    TipiPathParser tp = new TipiPathParser(null, context, to_path);
    tp.getProperty().setValue((String)value);
  }

  private void copyValue(TipiContext context, Object source) throws TipiException{
    String from_path = (String)myParams.get("from_path");
    String to_path = (String)myParams.get("to_path");

    TipiPathParser tp = new TipiPathParser((TipiComponent)source, context, to_path);
    TipiComponent targetComponent = tp.getComponent();

    if(tp.getPathType() == tp.PATH_TO_ATTRIBUTE){
      targetComponent.setValue(tp.getAttributeName(), from_path);
    }else if(tp.getPathType() == tp.PATH_TO_PROPERTY){
      Operand o = evaluate((TipiComponent)source,context,from_path);
      Object sourceObject = o.value;
      if (o.type.equals(Property.FLOAT_PROPERTY))
        tp.getProperty().setValue((Double) sourceObject);
      else if (o.type.equals(Property.INTEGER_PROPERTY))
        tp.getProperty().setValue((Integer) sourceObject);
      else if (o.type.equals(Property.DATE_PROPERTY))
        tp.getProperty().setValue((java.util.Date) sourceObject);
      else if (o.type.equals(Property.BOOLEAN_PROPERTY))
        tp.getProperty().setValue((Double) sourceObject);
      else {
        tp.getProperty().setValue(sourceObject.toString());
      }
    }else{
      throw new TipiException("Illegal copy operation: target should either be a property or an attribute");
    }
  }

  private void setValue(TipiContext context, Object source) throws TipiException {
    String path = (String)myParams.get("path");
    String name = (String)myParams.get("name");
    String value = (String)myParams.get("value");
    TipiPathParser pp = new TipiPathParser((TipiComponent)source, context, path);
    if(pp.getPathType() == pp.PATH_TO_PROPERTY){
      Operand o = evaluate((TipiComponent)source,context,value);
      Object sourceObject = o.value;

      if (o.type.equals(Property.FLOAT_PROPERTY))
        pp.getProperty().setValue( (Double) sourceObject);
      else if (o.type.equals(Property.INTEGER_PROPERTY))
        pp.getProperty().setValue( (Integer) sourceObject);
      else if (o.type.equals(Property.DATE_PROPERTY))
        pp.getProperty().setValue( (java.util.Date) sourceObject);
      else if (o.type.equals(Property.BOOLEAN_PROPERTY))
        pp.getProperty().setValue( (Double) sourceObject);
      else {
        pp.getProperty().setValue(sourceObject.toString());
      }
    }else{
      TipiComponent tc = pp.getComponent();
      //System.err.println("Got component: " + tc.getName());
      tc.setValue(name, value);
    }
  }

  private Operand evaluate(TipiComponent source, TipiContext context, String expr) {
    Operand o = null;
    try {
      context.setCurrentComponent((TipiComponent) source);
      o = Expression.evaluate(expr, ((TipiComponent) source).getNearestNavajo(), null, null, null, context);
    }
    catch (Exception ex) {
      System.err.println("Not happy while evaluating expression: "+expr+" message: "+ex.getMessage());
      Operand op = new Operand(expr,Property.STRING_PROPERTY,"");
      return o;
    }
    return o;
  }

  private void performTipiMethod(TipiContext context, Object source) throws TipiException {
    String path = (String)myParams.get("path");
    String name = (String)myParams.get("name");
    TipiComponent tc = getTipiComponentByPath(source,context,path);
    tc.performMethod(name,actionElement);
  }

  private Object getValueByPath(TipiComponent source, TipiContext c, String path){
    TipiPathParser pp = new TipiPathParser(null, c, path);
    switch(pp.getPathType()){
      case TipiPathParser.PATH_TO_MESSAGE:
        return path;
//        throw new RuntimeException("ERROR: Cannot request value of a Message path!");
      case TipiPathParser.PATH_TO_PROPERTY:
        if (pp.getProperty()==null) {
          System.err.println("No such property...");
          return null;
        }
        return pp.getProperty().getValue();

      case TipiPathParser.PATH_TO_TIPI:
//        throw new RuntimeException("ERROR: Cannot request value of a Tipi path!");
        return path;
      case TipiPathParser.PATH_TO_UNKNOWN:
//        throw new RuntimeException("ERROR: Cannot request value of a Unknown-Tipi path!");
        return path;
      case TipiPathParser.PATH_TO_ATTRIBUTE:
        TipiComponent tc = pp.getComponent();
        System.err.println("Path to attribute");
        System.err.println(">>"+tc.getValue(pp.getAttributeName()));
        return tc.getValue(pp.getAttributeName());

    }
    return null;
  }

  private void loadUI(TipiContext context, Object source) {
    //System.err.println("loadUI called: " + source);
    String file = (String) myParams.get("file");
    if (file != null) {
      /** @todo Fix this again. Remember to close all the toplevel screens. */
      MainApplication.loadXML(file);
    }else{
      System.err.println("WARNING! File is NULL");
    }
  }

  private TipiComponent getTipiComponentByPath(Object source, TipiContext context, String path) {
    TipiPathParser pp = new TipiPathParser((TipiComponent)source, context, path);
    return pp.getComponent();
  }

  private void performMethod(Navajo n, TipiContext context, Object source) throws TipiBreakException {

    String componentPath = (String) myParams.get("tipipath");
    String method = (String) myParams.get("method");
    TipiPathParser pp = new TipiPathParser((TipiComponent)source, context, componentPath);
    Tipi t = pp.getTipi();
    if (t == null) {
      System.err.println("Can not find tipi for: " + componentPath);
      return;
    }

    try {
      t.performService(context, method);
    }
    catch (TipiException ex) {
      System.err.println("Error preforming method!");
      ex.printStackTrace();
    }
  }

  private void callService(TipiContext context, Object source) throws TipiBreakException {
    String service = (String) myParams.get("service");
    if (service != null) {
      try {
        context.performMethod(service);
      }
      catch (TipiException ex) {
        System.err.println("Error executing call service:");
        ex.printStackTrace();
      }
    }
  }

  private void setPropertyValue(Navajo n, TipiContext context, Object source) throws TipiBreakException {
    String path = (String) myParams.get("path");
    String value = (String) myParams.get("value");
    if (path != null && value != null) {
      Property prop = context.getPropertyByPath(path);
//      Property prop = n.getProperty(path);
      prop.setValue(value);
    }
  }

  private void showInfo(Navajo n, TipiContext context, Object source) throws TipiBreakException {
    System.err.println("showInfo!");
    String txt = (String)myParams.get("text");
    try {
        Operand o;
        context.setCurrentComponent((TipiComponent) source);
        o = Expression.evaluate(txt, ((TipiComponent) source).getNearestNavajo(), null, null, null, context);
        txt = o.value.toString();
      }
      catch (Exception ex) {
        System.err.println("Error evaluating[" + txt + "] inserting as plain text only");
        ex.printStackTrace();
      }
    JOptionPane.showMessageDialog((Component)context.getTopLevel(), txt, "Info", JOptionPane.PLAIN_MESSAGE);
  }

  private void showQuestion(Navajo n, TipiContext context, Object source) throws TipiBreakException {
    String txt = (String)myParams.get("text");
    Object[] options = {"Ja", "Nee"};
    try {
        Operand o;
        context.setCurrentComponent((TipiComponent) source);
        o = Expression.evaluate(txt, ((TipiComponent) source).getNearestNavajo(), null, null, null, context);
        txt = o.value.toString();
      }
      catch (Exception ex) {
        System.err.println("Error evaluating[" + txt + "] inserting as plain text only");
        ex.printStackTrace();
      }

    int response = JOptionPane.showOptionDialog((Component)context.getTopLevel(), txt, "Vraag", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    if (response != 0) {
      throw new TipiBreakException(n, source);
    }
  }

  private void disposeTipiComponent(TipiContext context, Object source) throws TipiBreakException {
    String path = (String) myParams.get("path");
    TipiPathParser tp = new TipiPathParser((TipiComponent)source,context,path);
    context.disposeTipi((TipiComponent)(tp.getTipi()));
  }
}