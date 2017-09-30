/* Generated By:JJTree&JavaCC: Do not edit this line. ASTTmlNode.java */
package com.dexels.navajo.parser.compiled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.document.types.ClockTime;
import com.dexels.navajo.document.types.NavajoType;
import com.dexels.navajo.parser.TMLExpressionException;
import com.dexels.navajo.parser.compiled.api.ContextExpression;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.tipilink.TipiLink;
import com.dexels.navajo.util.Util;
import com.dexels.replication.api.ReplicationMessage;

/**
 *
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version $Id$
 */
public final class ASTTmlNode extends SimpleNode {
    String val = "";
//    Navajo doc = null;
//    Message parentMsg = null;
//    Message parentParamMsg = null;
//    Selection parentSel = null;
    String option = "";
    String selectionOption = "";
    boolean exists = false;
    
    public ASTTmlNode(int id) {
        super(id);
    }

    @Override
	public final ContextExpression interpretToLambda() {
		return new ContextExpression() {
	
			@Override
			public boolean isLiteral() {
				return false;
			}
			
			@Override
			public Object apply(Navajo doc, Message parentMsg, Message parentParamMsg, Selection parentSel,
					MappableTreeNode mapNode, TipiLink tipiLink, Access access, Optional<ReplicationMessage> immutableMessage) {
//				System.err.println(" exists: "+exists+" selectionOopt: "+parentSel+" selectionOption: "+selectionOption);
				List<Property> match = null;
				List<Object> resultList = new ArrayList<Object>();
		        boolean singleMatch = true;
		       // boolean selectionProp = false;
		        String parts[] = val.split("\\|");
		        
		        String text = parts.length > 1 ? parts[1] : val;
		        String document = parts.length > 1 ? parts[0].substring(1) : null;
//		        System.err.println("Document: "+document);
//		        if(document.)
		        boolean isParam = false;
//		        String selectionOption = selectionOpt == null ? "" :selectionOpt;
		        Property prop = null;
//		        System.err.println("Interpreting TMLNODE... val= "+val);
		        
				if (parentSel != null) {
					String dum = text;
					if (dum.length() > 1) {
						if(dum.startsWith("[")) {
							dum = dum.substring(1, text.length());
						}
					}
					if (dum.equals("name") || selectionOption.equals("name")) {
						return parentSel.getName();
					} else if (dum.equals("value") || selectionOption.equals("value")) {
						return parentSel.getValue();
					} else if (dum.equals("selected") || selectionOption.equals("selected")) {
						return Boolean.valueOf(parentSel.isSelected());
					}
				}

		        if (!exists) {
					if(text.startsWith("[")) {
			            text = text.substring(1, text.length());
					}
		        }  else {
					if(text.startsWith("[")) {
			            text = text.substring(2, text.length());
					}
//		            text = text.substring(2, text.length());
		        }
		        if (text.length() > 0 && text.charAt(0) == '@') { // relative param property.
		        		isParam = true;
		        		text = text.substring(1);
		        }
		        
		        if (text.startsWith("/@")) { // Absolute param property.
		        		parentParamMsg = doc.getMessage("__parms__");
		        		isParam = true;
		        		text = text.substring(2);
		        }
		        if (text.contains("__globals__")) { // Absolute globals property.
		            parentMsg = doc.getMessage("__globals__");
		            int length = "__globals__".length();
		            if (text.startsWith("/")) {
		                length += 1;
		            }
		            length += 1; // trailing /
		            text = text.substring(length);
		        }
		    
		        if (Util.isRegularExpression(text))
		            singleMatch = false;
		        else
		            singleMatch = true;

		        try {
		            if (parentMsg == null && !isParam) {
		                if (text.indexOf(Navajo.MESSAGE_SEPARATOR) != -1) {
		                	if(doc==null) {
		                		throw new NullPointerException("Can't evaluate TML node: No parent message and no document found.");
		                	}
		                	match = doc.getProperties(text);
		                    if (match.size() > 1) {
		                      singleMatch = false;
		                    }
		                }
		                else {
		                   throw new RuntimeException("No parent message present for property: " + text);
		                }
		            } else if (parentParamMsg == null && isParam) {
		            	parentParamMsg = doc.getMessage("__parms__");
		            	 if (text.indexOf(Navajo.MESSAGE_SEPARATOR) != -1) {
		                    match = doc.getProperties(text);
		                    if (match.size() > 1) {
		                       singleMatch = false;
		                    }  
		                }
		                else
		                    throw new RuntimeException("No parent message present for param: " + text);
		            } else {
		                //System.err.println("Looking for properties: "+val+" parentMessage: "+parentMsg.getFullMessageName());

		                if (text.indexOf(Navajo.MESSAGE_SEPARATOR) != -1) {
		                  match = (!isParam ? parentMsg.getProperties(text) : parentParamMsg.getProperties(text));
		                  if (match.size() > 1)
		                    singleMatch = false;

		                }
		                else {
		                    match = new ArrayList<>();
		                    match.add((!isParam ? parentMsg.getProperty(text) : parentParamMsg.getProperty(text)));
		                }
//		                System.err.println("# of matches: "+match.size());
		            }
		        } catch (NavajoException te) {
		            throw new RuntimeException(te.getMessage(),te);
		        }
		         for (int j = 0; j < match.size(); j++) {
		            prop = (Property) match.get(j);
		              if (!exists && (prop == null))
		            	  if (parentMsg!=null) {
		                      throw new RuntimeException("TML property does not exist: " + text+" parent message: "+parentMsg.getFullMessageName());
						} else {
			                throw new RuntimeException("TML property does not exist: " + text+" exists? "+exists);
						}
		            else if (exists) { // Check for existence and datatype validity.
		                if (prop != null) {
		                    // Check type. If integer, float or date type and if is empty
		                    String type = prop.getType();
		                   
		                    // I changed getValue into getTypedValue, as it resulted in a serialization
		                    // of binary properties. Should be equivalent, and MUCH faster.
		                    if (prop.getTypedValue() == null && !type.equals(Property.SELECTION_PROPERTY)) {
		                        return Boolean.valueOf(false);
		                    }

		                    if (type.equals(Property.INTEGER_PROPERTY)) {
		                       try {
		                          Integer.parseInt(prop.getValue());
		                          return Boolean.valueOf(true);
		                       } catch (Exception e) {
		                          return Boolean.valueOf(false);
		                       }
		                    } else if (type.equals(Property.FLOAT_PROPERTY)) {
		                      try {
		                          Double.parseDouble(prop.getValue());
		                          return Boolean.valueOf(true);
		                       } catch (Exception e) {
		                          return Boolean.valueOf(false);
		                       }
		                    } else if (type.equals(Property.DATE_PROPERTY)) {
		                    	try {
		                    		if ( prop.getTypedValue() instanceof Date ) {
		                    			return Boolean.valueOf(true);
		                    		} else {
		                    			return Boolean.valueOf(false);
		                    		}
		                    	} catch (Exception e) {
		                    		return Boolean.valueOf(false);
		                    	}
		                    } else if ( type.equals(Property.CLOCKTIME_PROPERTY)) {
		                    	try {
		                            ClockTime ct = new ClockTime(prop.getValue());
		                            if ( ct.calendarValue() == null ) {
		                            	return Boolean.valueOf(false);
		                            }
		                            return Boolean.valueOf(true);
		                         } catch (Exception e) {
		                            return Boolean.valueOf(false);
		                         }
		                    } else
		                        return Boolean.valueOf(true);
		                } else
		                    return Boolean.valueOf(false);
		            }
		              
		              
		            String type = prop.getType();
		              
		            Object value = prop.getTypedValue();

		            /** 
		             * LEGACY MODE! 
		             */
		            if ( value instanceof NavajoType && ((NavajoType) value).isEmpty() ) {
		            	value = null;
		            }
		            /**
		             * END OF LEGACY MODE!
		             */
		            
		            if (value == null && !type.equals(Property.SELECTION_PROPERTY)) {  // If value attribute does not exist AND property is not selection property assume null value
		               resultList.add(null);
		            } else
		            if (type.equals(Property.SELECTION_PROPERTY)) {
		                if (!prop.getCardinality().equals("+")) { // Uni-selection property.
		                    try {
		                        ArrayList<Selection> list = prop.getAllSelectedSelections();

		                        if (list.size() > 0) {
		                            Selection sel = (Selection) list.get(0);
//		                            System.err.println(">>"+sel+"<<>>"+selectionOption+"<<");
		                            resultList.add((selectionOption.equals("name") ? sel.getName() : sel.getValue()));
		                        } else {
		                          return null;
		                        }
		                    } catch (com.dexels.navajo.document.NavajoException te) {
		                        throw new RuntimeException(te.getMessage());
		                    }
		                } else { // Multi-selection property.
		                    try {
		                        List<Selection> list = prop.getAllSelectedSelections();
		                        List<Object> result = new ArrayList<Object>();
		                        for (int i = 0; i < list.size(); i++) {
		                            Selection sel = (Selection) list.get(i);
		                            Object o = (selectionOption.equals("name")) ? sel.getName() : sel.getValue();
		                            result.add(o);
		                        }
		                        resultList.add(result);
		                    } catch (com.dexels.navajo.document.NavajoException te) {
		                        throw new RuntimeException(te.getMessage());
		                    }
		                }
		            } else
		            if (type.equals(Property.DATE_PROPERTY)) {
		                if (value == null )
		                  resultList.add(null);
		                else {
		                  if (!option.equals("")) {
		                    try {
		                      Date a = (Date) prop.getTypedValue();
		                      Calendar cal = Calendar.getInstance();

		                      cal.setTime(a);
		                      int altA = 0;

		                      if (option.equals("month")) {
		                        altA = cal.get(Calendar.MONTH) + 1;
		                      }
		                      else if (option.equals("day")) {
		                        altA = cal.get(Calendar.DAY_OF_MONTH);
		                      }
		                      else if (option.equals("year")) {
		                        altA = cal.get(Calendar.YEAR);
		                      }
		                      else if (option.equals("hour")) {
		                        altA = cal.get(Calendar.HOUR_OF_DAY);
		                      }
		                      else if (option.equals("minute")) {
		                        altA = cal.get(Calendar.MINUTE);
		                      }
		                      else if (option.equals("second")) {
		                        altA = cal.get(Calendar.SECOND);
		                      }
		                      else {
		                        throw new TMLExpressionException("Option not supported: " +
		                                                         option + ", for type: " + type);
		                      }
		                      try {
		                        resultList.add(Integer.valueOf(altA));
		                      }
		                      catch (Exception e) {
		                        throw new TMLExpressionException(e.getMessage());
		                      }
		                    }
		                    catch (Exception ue) {
		                      throw new RuntimeException("Invalid date: " + prop.getValue());
		                    }
		                  }
		                  else {

		                    try {
		                      Date a = (Date) prop.getTypedValue();
		                      resultList.add(a);
		                    }
		                    catch (java.lang.Exception pe) {
		                      resultList.add(null);
		                    }
		                  }
		                }
		            } else if(type.equals(Property.EXPRESSION_PROPERTY)) {
		              resultList.add(prop.getTypedValue());
		            } else {
		                try {
		                    resultList.add(value);
		                } catch (Exception e) {
		                    throw new RuntimeException(e.getMessage());
		                }
		            }
		        }

		        if (!singleMatch)
		            return resultList;
		        else if (resultList.size() > 0)
		            return resultList.get(0);
		        else if (!exists)
		            throw new RuntimeException("Property does not exist: " + text);
		        else
		            return Boolean.valueOf(false);
			}
		};
	}
}
