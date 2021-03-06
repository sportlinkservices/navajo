/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.base;

/**
 * <p>Title: ShellApplet</p>
 * <p>Description: </p>
 * <p>Part of the Navajo mini client, based on the NanoXML parser</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels </p>
 * @author Frank Lyaruu
 * @version 1.0
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.ExpressionChangedException;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.MessageMappable;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.comparatormanager.ComparatorManager;
import com.dexels.navajo.document.comparatormanager.ComparatorManagerFactory;

public class BaseMessageImpl extends BaseNode implements Message, Comparable<Message> {

    private static final long serialVersionUID = 4404496830881606856L;

    protected String myName = "";

    private String myType = "";

    private String myMode = "";

    private String myExtends = "";

    private String myScope = "";

    private String myMethod = "";

    private int myIndex = -1;

    protected transient Map<String, Property> propertyMap = null;

    protected transient List<Property> propertyList = null;

    protected transient Map<String, Message> messageMap = null;

    private transient List<Message> messageList = null;

    private BaseMessageImpl myParent = null;

    private MessageMappable myStringMap = null;

    private int startIndex = -1;

    private int endIndex = -1;

    private String orderBy = "";

    private String eTag = null;

    private List<PropertyChangeListener> myPropertyDataListeners;

    private static final Logger logger = LoggerFactory.getLogger(BaseMessageImpl.class);

    protected BaseMessageImpl definitionMessage = null;

    private String subType;

    private Map<String, String> subtypeMap;

    public BaseMessageImpl(Navajo n) {
        super(n);
        myType = Message.MSG_TYPE_SIMPLE;
    }

    public BaseMessageImpl(Navajo n, String name) {
        super(n);
        myName = name;
        myType = Message.MSG_TYPE_SIMPLE;
    }

    @Override
    public final String getType() {
        return myType;
    }

    @Override
    public final void setType(String type) {
        myType = type;
        if (Message.MSG_TYPE_DEFINITION.equals(type) && getArrayParentMessage() != null) {
            getArrayParentMessage().setDefinitionMessage(this);
        }
    }

    @Override
    public final String getOrderBy() {
        return orderBy;
    }

    @Override
    public final void setOrderBy(String order) {
        orderBy = order;
    }

    @Override
    public final String getName() {
        return myName;
    }

    @Override
    public final void setCondition(String condition) {
    }

    /**
     * For internal use only, sets the initial message name.
     * 
     * @param name
     */
    protected final void setNameInitially(String name) {
        myName = name;
    }

    /**
     * Changes the name of a message.
     * 
     * @param name
     */
    @Override
    public final void setName(String name) {
        // Fix the messageMap collection to account for the changed message name.
        if (getParentMessage() != null) {
            if (((BaseMessageImpl) getParentMessage()).messageMap != null) {
                ((BaseMessageImpl) getParentMessage()).messageMap.remove(myName);
            }
            if (((BaseMessageImpl) getParentMessage()).messageMap != null) {
                ((BaseMessageImpl) getParentMessage()).messageMap.put(name, this);
            }
        }
        myName = name;
    }

    @Override
    public final void setMode(String mode) {
        myMode = mode;
    }

    @Override
    public final String getMode() {
        return myMode;
    }

    @Override
    public final void setExtends(String ext) {
        myExtends = ext;
    }

    @Override
    public final String getExtends() {
        return myExtends;
    }

    @Override
    public final void setScope(String s) {
        myScope = s;
    }

    @Override
    public final String getScope() {
        return myScope;
    }

    public final void clearAllSelections()  {
        if (propertyList != null) {

            for (int i = 0; i < propertyList.size(); i++) {
                Property p = propertyList.get(i);
                if (p.getType().equals(Property.SELECTION_PROPERTY)) {
                    p.clearSelections();
                }
            }
        }

        if (messageList != null) {
            for (int i = 0; i < messageList.size(); i++) {
                BaseMessageImpl p = (BaseMessageImpl) messageList.get(i);
                p.clearAllSelections();
            }
        }

    }

    @Override
    public Message addMessage(Message m) {
        if (m == null) {
            return null;
        }

        if (this.getType().equals(Message.MSG_TYPE_ARRAY)) {
            return addMessage(m, false);
        } else {
            return addMessage(m, true);
        }
    }

    @Override
    public String generateEtag() {
        MessageDigest md5;
        try {
            StringWriter sw = new StringWriter();
            this.write(sw);
            md5 = MessageDigest.getInstance("MD5");
            md5.update(sw.toString().getBytes());
            byte[] array = md5.digest();
            BigInteger bigInt = new BigInteger(1, array);
            String output = bigInt.toString(16);
            eTag = output;
            return output;
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Error: ", ex);
        }
        return null;

    }

    @Override
    public void clearEtag() {
        eTag = null;
    }

    @Override
    public void setEtag(String value) {
        eTag = value;
    }

    @Override
    public String getEtag() {
        return eTag;
    }

    @Override
    public final Message addMessage(Message m, boolean overwrite) {
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        if (messageMap == null) {
            messageMap = new TreeMap<>();
        }
        
        
        int counterIndex = 0;
        int counterIgnoreMode = 0; //how many times got into ignore mode
        boolean isIgnore = false;
        
        

        m.setParent(this);

        String name = m.getName();

        Message foundMsg = getMessage(name);
        if (foundMsg != null && !overwrite && !this.getType().equals(Message.MSG_TYPE_ARRAY)
                && !Message.MSG_MODE_IGNORE.equals(getMode())) {
            return foundMsg;
        }

        
        
        if (getMessage(name) != null && (overwrite || Message.MSG_MODE_IGNORE.equals(getMode()))) {
            removeChildMessage(foundMsg);
        }
        /**
         * If message is array type, insert new message as "element".
         */

        if (getType().equals(MSG_TYPE_ARRAY)) {
            if(Message.MSG_MODE_IGNORE.equals(m.getMode())) {
                isIgnore = true;
                counterIgnoreMode++; 
            }
            
            if (!m.getType().equals(MSG_TYPE_DEFINITION)) {
                if (messageList.size() > 0) {
                    counterIndex = messageList.size() - counterIgnoreMode; 
                }
                m.setIndex(counterIndex);
            }
            
            
            ((BaseMessageImpl) m).setNameInitially(getName());
        } else {
            messageMap.put(name, m);
        }
        // When mode="ignore" is set always clear array children before
        // inserting new one.
        if (getType().equals(MSG_TYPE_ARRAY) && Message.MSG_MODE_IGNORE.equals(getMode())) {
            messageList.clear();
        }
        
        
       
       //isIgnore boolean will be true only if the element enters the ignore mode check, and it wont be added to the messageList
       if (isIgnore == false) {
            messageList.add(m);
        }
       
       //updates the index of the messages in an array. If has ignore modes will sub the counter from the initial index value
        
        
        
       return m;
    }

    @Override
    public final void addMessage(Message m, int index)  {
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        if (messageMap == null) {
            messageMap = new TreeMap<>();
        }

        if (!getType().equals(Message.MSG_TYPE_ARRAY)) {
            throw new NavajoExceptionImpl(
                    "Can not add to with index to messages, if it is not an array message. Is that clear?");
        }

        messageList.add(index, m);
        m.setIndex(index);
        ((BaseMessageImpl) m).setNameInitially(getName());
        m.setParent(this);
    }

    @Override
    public ArrayList<Message> getAllMessages() {
        if (messageList == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messageList);
    }

    @Override
    public final void addProperty(Property q) {
        addProperty(q, false);
    }

    @Override
    public final void addProperty(Property q, boolean preferExistingPropertyValue) {
        if (this.getType().equals(Message.MSG_TYPE_ARRAY)) {
            logger.warn("Adding property {} to array instead of array_element!", q.getName());
        }
        if (q == null) {
            throw new NullPointerException("Message: can not add null property");
        }
        if (propertyMap == null) {
            propertyMap = new TreeMap<>();
        }
        if (propertyList == null) {
            propertyList = new ArrayList<>();
        }

        BasePropertyImpl p = (BasePropertyImpl) q;
        Property oldProperty = propertyMap.get(p.getName());
        if (oldProperty == q) {
            // ignore
            return;
        }
        if (oldProperty == null) {
            propertyList.add(q);
            propertyMap.put(p.getName(), p);
            p.setParent(this);
        } else if (!preferExistingPropertyValue) {
            this.removeProperty(oldProperty);
            propertyList.add(q);
            propertyMap.put(p.getName(), p);
        }
    }

    /**
     * LEAVE THIS METHOD (SEE COMMENT ABOVE)
     * 
     * @param q
     */
    // private void initPropertyFromDefinition(Property q) {
    // // Set default values from definition message.
    // BaseMessageImpl parentArrayMessage = (BaseMessageImpl)
    // getArrayParentMessage();
    // if (parentArrayMessage != null) {
    //
    // Property definitionProperty =
    // parentArrayMessage.getPropertyDefinition(q.getName());
    //
    // if (definitionProperty != null) {
    // if (q.getDescription() == null || "".equals(q.getDescription())) {
    // q.setDescription(definitionProperty.getDescription());
    // }
    // if (q.getDirection() == null || "".equals(q.getDirection())) {
    // q.setDirection(definitionProperty.getDirection());
    // }
    // if (q.getType() == null || "".equals(q.getType())) {
    // q.setType(definitionProperty.getType());
    // }
    // // if (q.getLength() == null) {
    // // length = definitionProperty.getLength();
    // // }
    // if (q.getSubType() == null) {
    // if (definitionProperty.getSubType() != null) {
    // q.setSubType(definitionProperty.getSubType());
    // } else {
    // q.setSubType(null);
    // }
    // } else {
    // if (definitionProperty.getSubType() != null) {
    // /**
    // * Concatenated subtypes. The if the same key of a subtype is
    // * present in both the property and the definition property.
    // */
    // q.setSubType(definitionProperty.getSubType() + "," + q.getSubType());
    // }
    // }
    //
    // if (q.getValue() == null || "".equals(q.getValue())) {
    // q.setValue(definitionProperty.getValue());
    // }
    // }
    // }
    // }

    @Override
    public final ArrayList<Property> getAllProperties() {
        if (propertyList == null) {
            propertyList = new ArrayList<>();
        }
        return new ArrayList<>(propertyList);
    }

    @Override
    public final List<Property> getProperties(String regularExpression)  {

        if (regularExpression.startsWith(Navajo.PARENT_MESSAGE + Navajo.MESSAGE_SEPARATOR)) {
            regularExpression = regularExpression
                    .substring((Navajo.PARENT_MESSAGE + Navajo.MESSAGE_SEPARATOR).length());
            return getParentMessage().getProperties(regularExpression);
        } else if (regularExpression.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We
            // have
            // an
            // absolute
            // offset
            Navajo d = getRootDoc();
            return d.getProperties(regularExpression.substring(1));
        } else {
            ArrayList<Property> props = new ArrayList<>();
            List<Message> messages = null;
            String property = null;
            Message message = null;

            StringTokenizer tok = new StringTokenizer(regularExpression, Navajo.MESSAGE_SEPARATOR);
            String messageList = "";

            int count = tok.countTokens();

            for (int i = 0; i < count - 1; i++) {
                property = tok.nextToken();
                messageList += property;
                if ((i + 1) < count - 1) {
                    messageList += Navajo.MESSAGE_SEPARATOR;
                }
            }
            String realProperty = tok.nextToken();

            if (!messageList.equals("")) {
                messages = this.getMessages(messageList);
            } else {
                messages = new ArrayList<>();
                messages.add(this);
            }

            Pattern pattern = Pattern.compile(realProperty);
            for (int i = 0; i < messages.size(); i++) {
                message = messages.get(i);
                List<Property> allProps = message.getAllProperties();
                try {
                    for (int j = 0; j < allProps.size(); j++) {
                        String name = allProps.get(j).getName();
                        if (pattern.matcher(name).matches()) {
                            props.add(allProps.get(j));
                        }
                    }
                } catch (Exception re) {
                    throw new NavajoExceptionImpl(re.getMessage());
                }
            }
            return props;
        }
    }

    @Override
    public void refreshExpression() throws ExpressionChangedException {

        if (messageList != null) {
            for (int i = 0; i < messageList.size(); i++) {
                Message current = messageList.get(i);
                current.refreshExpression();
            }
        }
        for (Property current : getAllProperties()) {
            current.refreshExpression();
        }

    }

    @Override
    public Message getMessage(String name) {

        // Check self reference.
        if (name.equals(".")) {
            return this;
        } else if (name.startsWith("../")) { // Check parent reference.
            if (getParentMessage() == null) {
                return null;
            } else {
                return getParentMessage().getMessage(name.substring(3));
            }
        } else // Check starting with self reference.
        if (name.startsWith("./")) {
            name = name.substring(2);
        }

        if (name.length() > 0 && name.charAt(0) == '/') {
            return getRootDoc().getMessage(name.substring(1));
        }
        if (name.indexOf("/") >= 0) {
            return getByPath(name);
        }

        if (name.indexOf("@") >= 0) {
            StringTokenizer arEl = new StringTokenizer(name, "@");
            String realName = arEl.nextToken();
            Message array = getMessage(realName);
            if (array != null) {
                if ((array.getType() != null) && (array.getType().equals(Message.MSG_TYPE_ARRAY))) {
                    if (arEl.hasMoreTokens()) {
                        String index = arEl.nextToken();

                        if (index.indexOf("=") >= 0) {
                            String propertyName = index.split("=")[0];
                            String propertyValue = index.split("=")[1];
                            // Find array element.
                            for (int x = 0; x < array.getArraySize(); x++) {
                                Message am = array.getMessage(x);
                                if (am.getProperty(propertyName) != null) {
                                    if (am.getProperty(propertyName).getValue().equals(propertyValue)) {
                                        return am;
                                    }
                                }
                            }
                        } else {
                            int i = 0;
                            try {
                                i = Integer.parseInt(index);
                            } catch (NumberFormatException ex) {
                                logger.error("Error: ", ex);
                            }
                            return array.getMessage(i);
                        }
                    }
                }
            }
        }

        if (messageMap == null) {
            return null;
        }
        return messageMap.get(name);
    }

    /**
     * Return all messages that match a given regular expression. Regular expression
     * may include sub-messages and even absolute message references starting at the
     * root level.
     */
    @Override
    public List<Message> getMessages(String regularExpression)  {

        List<Message> sub = null;
        List<Message> sub2 = null;

        if (regularExpression.startsWith(Navajo.PARENT_MESSAGE + Navajo.MESSAGE_SEPARATOR)) {
            regularExpression = regularExpression
                    .substring((Navajo.PARENT_MESSAGE + Navajo.MESSAGE_SEPARATOR).length());
            return getParentMessage().getMessages(regularExpression);
        } else if (regularExpression.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We
            // have an absolute offset

            return myDocRoot.getMessages(regularExpression);
        } else // Contains submessages.
        if (regularExpression.indexOf(Navajo.MESSAGE_SEPARATOR) != -1) { // contains
            // a path, descent it first
            StringTokenizer tok = new StringTokenizer(regularExpression, Navajo.MESSAGE_SEPARATOR);
            Message m = null;

            while (tok.hasMoreElements()) {
                String msgName = tok.nextToken();

                if (sub == null) { // First message in path.
                    sub = getMessages(msgName);
                } else { // Subsequent submessages in path.
                    ArrayList<Message> messages = new ArrayList<Message>();
                    for (int i = 0; i < sub.size(); i++) {
                        m = sub.get(i);
                        sub2 = m.getMessages(msgName);
                        messages.addAll(sub2);
                    }
                    sub = messages;
                }
            }
            return sub;
        } else {

            ArrayList<Message> result = new ArrayList<>();
            try {
                String index = null;

                if (regularExpression.indexOf("@") != -1) {
                    StringTokenizer arEl = new StringTokenizer(regularExpression, "@");
                    regularExpression = arEl.nextToken();
                    index = arEl.nextToken();

                }
                Pattern pattern = Pattern.compile(regularExpression);
                if (messageList != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        BaseMessageImpl m = (BaseMessageImpl) messageList.get(i);
                        String name = m.getName();
                        String type = m.getType();
                        if (type.equals(Message.MSG_TYPE_ARRAY) && pattern.matcher(name).matches()) { // If
                            // message is array type add all children.
                            if (index == null) {
                                if (m.messageList != null) {
                                    result.addAll(m.messageList);
                                }
                            } else {

                                if (index.indexOf("=") >= 0) {
                                    String propertyName = index.split("=")[0];
                                    String propertyValue = index.split("=")[1];
                                    // Find array element.
                                    for (int x = 0; x < m.getArraySize(); x++) {
                                        Message am = m.getMessage(x);
                                        if (am.getProperty(propertyName) != null) {
                                            if (am.getProperty(propertyName).getValue().equals(propertyValue)) {
                                                result.add(am);
                                            }
                                        }
                                    }
                                } else {

                                    try {
                                        if (m.getMessage(Integer.parseInt(index)) != null) {
                                            result.add(m.getMessage(Integer.parseInt(index)));
                                        }

                                    } catch (Exception pe) {
                                        throw new NavajoExceptionImpl("Could not parse array index: " + index);
                                    }
                                }
                            }
                        } else {
                            if (pattern.matcher(name).matches()) {
                                result.add(messageList.get(i));
                            }
                        }
                    }
                }

            } catch (Exception re) {
                throw new NavajoExceptionImpl(re);
            }
            return result;
        }
    }

    protected final boolean compliesWith(Message m, String expression) {
        return m.getName().startsWith(expression);
    }

    protected final boolean compliesWith(Property p, String expression) {
        return p.getName().startsWith(expression);
    }

    @Override
    public final Property getProperty(String s) {
        if (s.length() > 0 && s.charAt(0) == '/') {
            return getRootDoc().getProperty(s.substring(1));
        }

        return getPropertyByPath(s);
    }

    @Override
    public final int getIndex() {
        return myIndex;
    }

    @Override
    public final void setIndex(int index) {
        myType = Message.MSG_TYPE_ARRAY_ELEMENT;
        myIndex = index;
    }

    @Override
    public final Message getMessage(int i) {
        if (messageMap == null || messageList == null) {
            return null;
        }
        if (i >= messageList.size()) {
            return null;
        }
        return messageList.get(i);
    }

    // Returns an array element
    public final Message getMessage(String name, int index) {
        Message m = getMessage(name);
        if (m == null) {
            return null;
        }
        if (!m.getType().equals(Message.MSG_TYPE_ARRAY)) {
            logger.info("Found a non array message, when querying for an array element");
            return null;
        }
        return m.getMessage(index);
    }

    public final void removeChildMessage(Message child) {
        if (messageList == null || messageMap == null) {
            return;
        }
        if(child == null) {
        	return;
        }
        if (messageList.contains(child)) {
            messageList.remove(child);
            messageMap.remove(child.getName());
        } else if (child.getParentMessage() != null && child.getParentMessage() != this) {
            // Some other message's child, ask parent of child to remove it..
            child.getParentMessage().removeMessage(child);
        }
    }

    /**
     * Add a collection of properties to a message. Properties that already exist
     * are overwritten.
     * 
     * @param m
     * @param properties
     */
    private final void addProperties(Message m, List<Property> properties, boolean preferMyProperties) {

        for (int i = 0; i < properties.size(); i++) {
            m.addProperty(properties.get(i), preferMyProperties);
        }
    }

    /**
     * Merges messsage with another message. Properties and submessages are merged.
     * Properties of other message have precedence.
     * 
     * @param origMsg
     * @param mergeThisMsg
     */
    private final void mergeMessage(Message origMsg, Message mergeThisMsg, boolean preferOrigMessage) {

        if (messageList == null || messageMap == null) {
            return;
        }

        // Add all properties of new message.
        addProperties(origMsg, mergeThisMsg.getAllProperties(), preferOrigMessage);

        // Find scope, extends properties in mergeThisMsg...
        if (mergeThisMsg.getScope() != null && (origMsg.getScope() == null || origMsg.getScope().equals(""))) {
            origMsg.setScope(mergeThisMsg.getScope());
        }
        if (mergeThisMsg.getExtends() != null && (origMsg.getExtends() == null || origMsg.getExtends().equals(""))) {
            origMsg.setExtends(mergeThisMsg.getExtends());
        }
        if (mergeThisMsg.getSubType() != null && (origMsg.getSubType() == null || origMsg.getSubType().equals(""))) {
            origMsg.setSubType(mergeThisMsg.getSubType());
        }

        // Find overlapping children.
        List<Message> childrenPrev = origMsg.getAllMessages();
        List<Message> childrenNew = mergeThisMsg.getAllMessages();
        for (int i = 0; i < childrenPrev.size(); i++) {
            Message childPrev = childrenPrev.get(i);
            for (int j = 0; j < childrenNew.size(); j++) {
                if (childrenNew.get(j).getName().equals(childPrev.getName())) {
                    origMsg.mergeMessage(childrenNew.get(j));
                    j = childrenNew.size() + 1;
                }
            }
        }
        // Find additional children.
        for (int i = 0; i < childrenNew.size(); i++) {
            Message childNew = childrenNew.get(i);
            boolean checkNew = true;
            for (int j = 0; j < childrenPrev.size(); j++) {
                if (childrenPrev.get(j).getName().equals(childNew.getName())) {
                    checkNew = false;
                    j = childrenPrev.size() + 1;
                }
            }
            if (checkNew) {
                origMsg.addMessage(childNew);
            }
        }

    }

    private int currentTotal = -1;

    @Override
    public final int getCurrentTotal() {
        return currentTotal;
    }

    @Override
    public final void setCurrentTotal(int aap) {
        currentTotal = aap;
    }

    public Property getPropertyDefinition(String name) {
        if (definitionMessage != null) {
            return definitionMessage.getProperty(name);
        } else {
            return null;
        }
    }

    public final int getChildMessageCount() {
        if (messageList == null) {
            return 0;
        }
        return messageList.size();
    }

    public final void addArrayMessage(Message m) {
        if (!MSG_TYPE_ARRAY.equals(getType())) {
            throw new RuntimeException("Adding array element to non-array message");
        }
        ((BaseMessageImpl) m).setNameInitially(getName());
        addMessage(m);
    }

    @Override
    public final Message copy()  {
        Navajo empty = NavajoFactory.getInstance().createNavajo();
        Message result = copy(empty);
        empty.addMessage(result);
        return result;
    }

    @Override
    public final Message copy(Navajo n) {

        BaseMessageImpl cp = (BaseMessageImpl) NavajoFactory.getInstance().createMessage(n, getName());
        cp.setRootDoc(n);

        cp.setEndIndex(getEndIndex());
        cp.setStartIndex(getStartIndex());
        cp.setIndex(getIndex());
        cp.setMode(getMode());
        cp.setType(getType());
        cp.setEtag(getEtag());
        cp.setExtends(getExtends());
        cp.setScope(getScope());
        cp.setMethod(getMethod());
        cp.setSubType(getSubType());
        cp.setOrderBy(getOrderBy());

        // If definition message is available, copy it as well.
        if (isArrayMessage() && getDefinitionMessage() != null) {
            cp.setDefinitionMessage(getDefinitionMessage().copy(n));
        }

        if (messageList != null) {

            for (int i = 0; i < messageList.size(); i++) {

                BaseMessageImpl current = (BaseMessageImpl) messageList.get(i);
                if (current == this) {
                    throw new RuntimeException("CYCLIC Message copy found!");
                }
                Message cc = current.copy(n);
                cp.addMessage(cc);
            }
        }

        for (Property current : getAllProperties()) {
            Property copy = current.copy(n);

            cp.addProperty(copy);
        }
        return cp;
    }

    public final void prune() {

        if (messageList != null) {
            for (int i = 0; i < messageList.size(); i++) {
                BaseMessageImpl current = (BaseMessageImpl) messageList.get(i);
                current.prune();
            }
        }

        for (Property current : getAllProperties()) {
            ((BasePropertyImpl) current).prune();
        }
    }

    @Override
    public final void setMessageMap(MessageMappable m) {
        myStringMap = m;

        if (messageList != null) {
            for (int i = 0; i < messageList.size(); i++) {
                BaseMessageImpl current = (BaseMessageImpl) messageList.get(i);
                if (current != null) {
                    current.setMessageMap(m);
                }
            }
        }
    }

    public final MessageMappable getMessageMap() {
        return myStringMap;
    }

    @Override
    public final String toString() {
        if (myStringMap != null) {
            return myStringMap.getMessageLabel(this);
        }
        return getName();
    }

    @Override
    public final void setParent(Message m) {
        if (m == null) {
            return;
        }
        myParent = (BaseMessageImpl) m;
    }

    public final Message getByPath(String path) {
        if (path.startsWith("../")) {
        	return getParentMessage().getMessage(path.substring(3));
            // I THINK! It did not make sense at all
        }

        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }

        int slash = path.indexOf("/");
        if (slash < 0) {
            return getMessage(path);
        } else {
            String messagename = path.substring(0, slash);
            Message m = getMessage(messagename);
            if (m != null) {
                return m.getMessage(path.substring(slash + 1));
            } else {
                return null;
            }
        }
    }

    public final Property getPropertyByPath(String pth) {
        String path = null;
        if (pth.length() > 0 && pth.charAt(0) == '/') {
            path = pth.substring(1);
        } else {
            path = pth;
        }
        if (path.startsWith("..")) {
            if (getParentMessage() == null) {
                return null;
            }
            return getParentMessage().getProperty(path.substring(3));
        }

        int slash = path.indexOf("/");
        if (slash < 0) {
            if (propertyList == null || propertyMap == null) {
                return null;
            }
            if (propertyList.size() != propertyMap.size()) {
                logger.info("Warning: Propertymap sizE: " + propertyMap.size() + " listsize: " + propertyList.size());
            }

            Property pp = propertyMap.get(path);
            if (pp == null && !Message.MSG_TYPE_DEFINITION.equals(getType())) {
                // check for definition messages (except if I'm a definition
                // message myself)
                Message arrayP = getArrayParentMessage();
                if (arrayP != null) {
                    Message def = arrayP.getDefinitionMessage();
                    if (def != null && def.getProperty(path) != null) {
                        Property res = def.getProperty(path).copy(getRootDoc());
                        if (def.getType() == null || "".equals(def.getType())) {
                            throw new IllegalStateException("DEFINITION PROPERTY FOUND WITHOUT TYPE!");
                        }
                        res.setType(res.getType());

                        addProperty(res);
                        return res;
                    }
                }
            }
            return pp;
        } else {
            String msgname = path.substring(0, slash);
            String propname = path.substring(slash, path.length());
            BaseMessageImpl ms = (BaseMessageImpl) getMessage(msgname);
            if (ms != null) {
                return ms.getPropertyByPath(propname);
            }
            return null;
        }
    }

    @Override
    public final String getPath() {
        if (myParent != null) {
            if (myParent.getType().equals(Message.MSG_TYPE_ARRAY)) {
                return myParent.getPath() + "@" + getIndex();
            }
            return myParent.getPath() + "/" + getName();
        } else {
            return getName();
        }
    }

    public final int getStartIndex() {
        return startIndex;
    }

    @Override
    public Message instantiateFromDefinition() {
        Message copy = null;
        if (getDefinitionMessage() != null) {
            copy = getDefinitionMessage().copy(this.getRootDoc());
        } else {
            copy = NavajoFactory.getInstance().createMessage(getRootDoc(), getName(), MSG_TYPE_ARRAY_ELEMENT);
        }

        addElement(copy);
        return copy;
    }

    public final int getEndIndex() {
        return endIndex;
    }

    public final void setStartIndex(int i) {
        startIndex = i;
    }

    public void setEndIndex(int i) {
        endIndex = i;
    }

    @Override
    public final Message getParentMessage() {
        if (myParent == null) {
            return null;
        }
        if (Message.MSG_TYPE_ARRAY.equals(myParent.getType())) {
            return myParent.getParentMessage();
        }
        return myParent;
    }

    /**
     * Added this method to bridge the difference between nano and jaxp
     */
    @Override
    public final Message getArrayParentMessage() {
        return myParent;
    }

    @Override
    public final Message addElement(Message m) {
        if (!getType().equals(Message.MSG_TYPE_ARRAY)) {
            throw new IllegalArgumentException("Can not add element to non-array type message!");
        }
        if (!m.getType().equals(MSG_TYPE_DEFINITION)) {
            m.setIndex(getArraySize());
        }
        addMessage(m);
        return m;
    }

    @Override
    public final int getArraySize() {
        if (messageList == null || messageMap == null) {
            return 0;
        }
        return messageList.size();
    }

    @Override
    public final void setArraySize(int i) {
        throw new UnsupportedOperationException("Dont know what this method should do.");
    }

    @Override
    public final boolean isArrayMessage() {
        return MSG_TYPE_ARRAY.equals(getType());
    }

    @Override
    public final String getFullMessageName() {
        return getPath();
    }

    @Override
    public final Property getPathProperty(String path) {
        return getPropertyByPath(path);
    }

    @Override
    public final void removeMessage(Message msg) {
        removeChildMessage(msg);
    }

    public final void removeMessage(String msg) {
        removeChildMessage(getMessage(msg));
    }

    @Override
    public final void removeProperty(Property p) {
        if (propertyList != null) {
            propertyList.remove(p);
        }
        if (propertyMap != null) {
            propertyMap.remove(p.getName());
        }
    }

    @Override
    public final void setLazyRemaining(int c) {
    }

    @Override
    public final void setLazyTotal(int c) {
    }

    @Override
    public final boolean contains(String name) {
        boolean b = getMessage(name) != null;
        if (!b) {
            return getProperty(name) != null;
        }
        return b;
    }

    @Override
    public final boolean isEqual(Message o) {
        return isEqual(o, "");
    }

    @Override
    public final boolean isEqual(Message o, String skipProperties) {
        BaseMessageImpl other = (BaseMessageImpl) o;
        if (!other.getName().equals(this.getName())) {
            return false;
        }
        // Check sub message structure.
        List<Message> allOther = other.messageList;
        if (allOther != null && messageList == null) {
            return false;
        }
        if (messageList != null && allOther == null) {
            return false;
        }
        if (allOther != null && allOther.size() != messageList.size()) {
            return false;
        }
        if (allOther != null) {
            for (int i = 0; i < allOther.size(); i++) {
                Message otherMsg = allOther.get(i);
                boolean match = false;
                for (int j = 0; j < messageList.size(); j++) {
                    Message myMsg = messageList.get(j);
                    if (myMsg.isEqual(otherMsg, skipProperties)) {
                        match = true;
                        j = messageList.size() + 1;
                    }
                }
                if (!match) {
                    return false;
                }
            }
        }
        // Check property structure.
        ArrayList<Property> allOtherProps = other.getAllProperties();
        ArrayList<Property> allMyProps = this.getAllProperties();
        if (allOtherProps.size() != allMyProps.size()) {
            return false;
        }

        for (int i = 0; i < allOtherProps.size(); i++) {
            Property otherProp = allOtherProps.get(i);
            boolean match = false;
            // Check whether property name exists in skipProperties list.
            if (skipProperties.indexOf(otherProp.getName()) != -1) {
                match = true;
            } else {
                for (int j = 0; j < allMyProps.size(); j++) {
                    Property myProp = allMyProps.get(j);
                    if (myProp.isEqual(otherProp)) {
                        match = true;
                        j = allMyProps.size() + 1;
                    }
                }
            }

            if (!match) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final Message getDefinitionMessage() {
        return definitionMessage;
    }

    @Override
    public final void setDefinitionMessage(Message m) {
        if (!m.getType().equals(Message.MSG_TYPE_DEFINITION)) {
            m.setType(Message.MSG_TYPE_DEFINITION);
        }
        this.definitionMessage = (BaseMessageImpl) m;
        // Remove from child list, to be sure.
        if (messageList != null) {
            messageList.remove(m);
        }
        m.setParent(null);
    }

    @Override
    public final Map<String, String> getAttributes() {
        Map<String, String> m = new HashMap<>();
        m.put("name", myName);
        if (!"".equals(orderBy)) {
            m.put("orderby", orderBy);
        }
        if (eTag != null) {
            m.put(Message.MSG_ETAG, eTag);
        }
        // don't write type="simple", as it's default
        if (myType != null && !Message.MSG_TYPE_SIMPLE.equals(myType)) {
            m.put("type", myType);
            if (Message.MSG_TYPE_ARRAY_ELEMENT.equals(myType)) {
                m.put("index", "" + myIndex);
            }
        }
        if (myMode != null && !myMode.equals("")) {
            m.put("mode", myMode);
        }
        if (myExtends != null && !myExtends.equals("")) {
            m.put(Message.MSG_EXTENDS, myExtends);
        }
        if (myMethod != null && !myMethod.equals("")) {
            m.put(Message.MSG_METHOD, myMethod);
        }
        if (myScope != null && !myScope.equals("")) {
            m.put(Message.MSG_SCOPE, myScope);
        }
        if (subType != null && !subType.equals("")) {
            m.put(MSG_SUBTYPE, subType);
        }
        return m;
    }

    @Override
    public final List<BaseNode> getChildren() {
        ArrayList<BaseNode> al = new ArrayList<>();
        if (propertyList == null) {

        } else {
            for (Property p : propertyList) {
                BasePropertyImpl pmi = (BasePropertyImpl) p;
                al.add(pmi);
            }
        }
        if (getDefinitionMessage() != null) {
            al.add((BaseMessageImpl) getDefinitionMessage());
        }
        if (messageList != null) {
            if (!"".equals(orderBy) && getType().equals(Message.MSG_TYPE_ARRAY)) {
                Collections.sort(messageList);
            }
            for (Message m : messageList) {
                al.add((BaseNode) m);
            }
        }
        return al;
    }

    @Override
    public Object getRef() {
        throw new UnsupportedOperationException("getRef not possible on base type. Override it if you need it");
    }

    /**
     * @param newName the name of the new message
     */
    public Object clone(String newName) {
        throw new UnsupportedOperationException("Can not clone properties (yet)");
    }

    @Override
    public String getTagName() {
        return Message.MSG_DEFINITION;
    }

    public final int getChildCount() {
        return getAllProperties().size() + (messageList != null ? messageList.size() : 0);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final int compareTo(Message m) {
        if (m != null) {
            if (getType().equals(Message.MSG_TYPE_ARRAY_ELEMENT)) {
                if (getArrayParentMessage() != null) {
                    String order = this.getArrayParentMessage().getOrderBy();

                    if (!"".equals(order)) {

                        // Parse the orderby attribute
                        // Put them in a set
                        StringTokenizer tok = new StringTokenizer(order, ",");
                        List<String> orderValues = new LinkedList<>();
                        while (tok.hasMoreTokens()) {
                            String token = tok.nextToken();
                            orderValues.add(token.trim());
                        }

                        // while messages are equal and there are more
                        // orderValues
                        // keep ordering
                        int compare = 0;
                        Iterator<String> it = orderValues.iterator();
                        while (it.hasNext() && compare == 0) {
                            String oV = it.next();
                            // If DESC we flip the direction
                            int desc = -1;
                            if (oV.indexOf(' ') > 0) {
                                String sort = oV.substring(oV.indexOf(' ') + 1);
                                oV = oV.substring(0, oV.indexOf(' '));
                                if ("DESC".equalsIgnoreCase(sort)) {
                                    desc = 1;
                                }
                            }

                            // Check whether oV is a function instead of a property.
                            if (oV.indexOf("(") != -1) {
                                // It is a function.

                                String compareFunction = oV.substring(0, oV.indexOf("("));
                                Comparator c = null;
                                try {
                                    final ComparatorManager instance = ComparatorManagerFactory.getInstance();
                                    c = instance.getComparator(compareFunction);
                                    if (c == null) {
                                        logger.error("Comparator not found: {}. Not sorting.", compareFunction);
                                        compare = 0;
                                    } else {
                                        compare = c.compare(this, m);
                                    }
                                } catch (Exception e) {
                                    logger.error("Error on compare message using {}", compareFunction, e);
                                    compare = 0;
                                }

                            } else {
                                // Now we assume oV is an existing property in both messages
                                Property myOvProp = getProperty(oV);
                                if (myOvProp == null) {
                                    logger.info("WARNING: error while sorting message. Could not sort property named: "
                                            + oV);
                                    return 0;
                                }
                                Property compOvProp = m.getProperty(oV);
                                compare = desc * compOvProp.compareTo(myOvProp);
                            }
                        }
                        return compare;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void firePropertyDataChanged(Property p, Object oldValue, Object newValue) {
        if (getArrayParentMessage() != null) {
            getArrayParentMessage().firePropertyDataChanged(p, oldValue, newValue);
        } else if (getRootDoc() != null) {
            getRootDoc().firePropertyDataChanged(p, oldValue, newValue);
        }
        if (myPropertyDataListeners != null) {
            for (int i = 0; i < myPropertyDataListeners.size(); i++) {
                PropertyChangeListener c = myPropertyDataListeners.get(i);
                c.propertyChange(new PropertyChangeEvent(p, "value", oldValue, newValue));
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        if (myPropertyDataListeners == null) {
            myPropertyDataListeners = new ArrayList<PropertyChangeListener>();
        }
        myPropertyDataListeners.add(p);

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        if (myPropertyDataListeners == null) {
            return;
        }
        myPropertyDataListeners.remove(p);
    }



    @Override
    public void write(Writer w) {
        try {
            this.printElement(w, 2);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    @Override
    public void printElementJSONTypeless(final Writer sw) throws IOException {
        printElementJSONTypeless(getName(), sw, null);
    }

    private void printElementJSONTypeless(String name, final Writer sw, String[] propertyFilter) throws IOException {
        ArrayList<Message> messages = getAllMessages();
        ArrayList<Property> properties = getAllProperties();

        // all overridden

        if (getType().equals(Message.MSG_TYPE_ARRAY)) {
            writeElement(sw, "\"" + name + "\" : [");
            int cnt = 0;
            if (propertyFilter != null) {
                for (Message m : messages) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    ((BaseMessageImpl) m).printElementJSONTypeless(name, sw, propertyFilter);
                    cnt++;
                }
            } else {
                for (Message m : messages) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    ((BaseNode) m).printElementJSONTypeless(sw);
                    cnt++;
                }
            }
            writeElement(sw, "]");
        } else if (getType().equals(Message.MSG_TYPE_ARRAY_ELEMENT)) {
            writeElement(sw, "{");
            int cnt = 0;
            if (propertyFilter != null) {
                for (String s : propertyFilter) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    Property p = getProperty(s);
                    if (p != null) {
                        ((BaseNode) p).printElementJSONTypeless(sw);
                        cnt++;
                    }
                }
            } else {
                for (Property p : properties) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    ((BaseNode) p).printElementJSONTypeless(sw);
                    cnt++;
                }
            }
            writeElement(sw, "}");

        } else if (getType().equals(Message.MSG_TYPE_SIMPLE)) {
            writeElement(sw, "\"" + getName() + "\" : {");
            int cnt = 0;
            if (propertyFilter != null) {
                for (String s : propertyFilter) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    Property p = getProperty(s);
                    ((BaseNode) p).printElementJSONTypeless(sw);
                    cnt++;
                }
            } else {
                for (Property p : properties) {
                    if (cnt > 0) {
                        writeElement(sw, ", ");
                    }
                    ((BaseNode) p).printElementJSONTypeless(sw);
                    cnt++;
                }
            }
            writeElement(sw, "}");

            cnt = 0;
            for (Message m : messages) {
                if (cnt > 0) {
                    writeElement(sw, ", ");
                }
                ((BaseNode) m).printElementJSONTypeless(sw);
                cnt++;
            }
            writeElement(sw, "}");
        }
    }

    @Override
    public Message mergeMessage(Message m) {
        return mergeMessage(m, false);
    }

    @Override
    public Message mergeMessage(Message m, boolean preferThisMessage) {
        Message prevMsg = getMessage(m.getName());
        if (prevMsg != null) {
            mergeMessage(prevMsg, m, preferThisMessage);
            return m;
        } else {
            return null;
        }

    }

    @Override
    public Map<String, Property> getProperties() {
        if (propertyMap == null) {
            return new HashMap<>();
        } else {
            return new HashMap<>(propertyMap);
        }
    }

    @Override
    public Map<String, Message> getMessages() {
        if (messageMap == null) {
            return new HashMap<>();
        } else {
            return new HashMap<>(messageMap);
        }
    }

    @Override
    public List<Message> getElements() {
        if (messageList == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(messageList);
        }
    }

    @Override
    public List<Message> getSortedElements() {
        if (messageList == null) {
            return new ArrayList<>();
        } else {
            ArrayList<Message> result = new ArrayList<>(messageList);
            if (!"".equals(orderBy)) {
                Collections.sort(result);
            }
            return result;
        }
    }

    @Override
    public void merge(Message incoming, boolean preferThis) {
        merge(incoming, preferThis, true);
    }

    @Override
    public void merge(Message incoming, boolean preferThis, boolean applySubType) {
        if (this.isArrayMessage() && incoming.isArrayMessage() && incoming.getDefinitionMessage() != null) {
            // Perform merge for all my children with the definition message
            for (Message child : this.getElements()) {
                child.merge(incoming.getDefinitionMessage(), preferThis);
            }
        }

        if (incoming.getScope() != null && (this.getScope() == null || this.getScope().equals(""))) {
            this.setScope(incoming.getScope());
        }
        if (incoming.getSubType() != null && (this.getSubType() == null || this.getSubType().equals(""))) {
            this.setSubType(incoming.getSubType());
        }

        // Check if message with incoming name exists.
        if (!getName().equals(incoming.getName())) {
            incoming.setName(getName());
        }

        List<Property> properties = incoming.getAllProperties();
        for (int i = 0; i < properties.size(); i++) {
            Property p = (Property) properties.get(i).clone();
            Property otherProperty = null;
            if (preferThis) {
                otherProperty = getProperty(p.getName());
            }
            if (!preferThis || otherProperty == null) {
                addProperty(p);
            }
            // If we don't have a method set, use the incoming method
            if (otherProperty != null && otherProperty.getMethod().equals("")) {
                otherProperty.setMethod(p.getMethod());
            }

        }
        List<Message> subMessages = incoming.getAllMessages();
        for (int i = 0; i < subMessages.size(); i++) {
            String newMsgName = subMessages.get(i).getName();
            Message existing = this.getMessage(newMsgName);
            if (existing == null) {
                // if we dont have this message ourselves and incoming message has it marked as
                // nullable, then we should NOT add it (because we explicitly allow the message
                // to not exist)
                String nullableString = subMessages.get(i).getSubType("nullable");
                boolean nullable = nullableString != null && Boolean.parseBoolean(nullableString);
                if (nullable && applySubType) {
                    continue;
                }
                try {
                    Message newMsg = subMessages.get(i).copy();
                    Message otherMessage = null;
                    if (preferThis) {
                        otherMessage = getMessage(newMsg.getName());
                    }
                    if (!preferThis || otherMessage == null) {
                        this.addMessage(newMsg);
                    }
                    if (otherMessage != null && otherMessage.getMethod().equals("")) {
                        otherMessage.setMethod(newMsg.getMethod());
                    }

                } catch (NavajoException e) {
                    logger.error("Navajo Exception on merge: {}", e);
                }
            } else {
                existing.merge(subMessages.get(i), preferThis);
            }
        }

    }

    /**
     * TODO: Can we deprecate this method or the other merge method such that we
     * have only one merge method?
     */
    @Override
    public void merge(Message incoming) {
        merge(incoming, false);
    }

    @Override
    public void maskMessage(Message mask) {
        maskMessage(mask, "");
    }

    @Override
    public void maskMessage(Message mask, String method) {
        if (isArrayMessage() && !mask.isArrayMessage()) {
            // No need to check any properties or submessages
            if (this.getParentMessage() != null) {
                this.getParentMessage().removeMessage(this);
            } else {
                this.getRootDoc().removeMessage(this);
            }

            return;
        }
        // Mask all properties.
        Iterator<Property> allProperties = new ArrayList<Property>(this.getAllProperties()).iterator();

        while (allProperties.hasNext()) {
            Property p = allProperties.next();
            Property m_p = mask.getProperty(p.getName());

            // If we didn't find a mask property but we are an array element, check for
            // definition message
            if (m_p == null && this.getIndex() > -1) {
                m_p = ((BaseMessageImpl) mask).getPropertyDefinition(p.getName());
            }

            if (m_p == null) {
                removeProperty(p);
                continue;
            }

            p.setSubType(m_p.getSubType());
            p.setDirection(m_p.getDirection());

            // A method that is null or "" is considered to always match
            boolean matchMethod = method.equals("") || m_p.getMethod() == null || m_p.getMethod().equals("")
                    || m_p.getMethod().equals(method);

            if (!matchMethod) {
                removeProperty(p);
            } else if (!p.getType().equals(m_p.getType()) && !m_p.getType().equals(Property.SELECTION_PROPERTY)) {
                if (p.getValue() != null) {
                    logger.debug("Overriding property type for {} - {} to {}", p.getFullPropertyName(), p.getType(),
                            m_p.getType());
                }
                p.setType(m_p.getType());
            }
        }

        // If we are an array message, mask all submessages with definition message in
        // mask
        if (isArrayMessage() && mask.isArrayMessage()) {
        	setOrderBy(mask.getOrderBy());
            for (Message child : getElements()) {
                child.maskMessage(mask, method);
            }
            return;
        }

        // Mask all messages.
        Iterator<Message> allMessages = new ArrayList<Message>(this.getAllMessages()).iterator();
        while (allMessages.hasNext()) {

            Message m = allMessages.next();
            Message maskMessage = mask.getMessage(m.getName());
            if (maskMessage == null && this.getIndex() > -1 && mask.getDefinitionMessage() != null) {
                maskMessage = mask.getDefinitionMessage().getMessage(m.getName());
            }

            if (maskMessage == null) {
                removeMessage(m);
                continue;
            }

            boolean matchMethod = method.equals("") || maskMessage.getMethod().equals("")
                    || maskMessage.getMethod().equals(method);

            if (!matchMethod) {
                removeMessage(m);
                continue;
            }

            // If message m is an array message, mask each element
            if (m.isArrayMessage()) {
            	m.setOrderBy(mask.getOrderBy());
                Message definitionMessage = maskMessage.getDefinitionMessage();
                if (definitionMessage == null) {
                    logger.debug("Unable to mask {} since the mask has no definition message", m.getName());

                } else {

                    for (int i = 0; i < m.getElements().size(); i++) {
                        m.getElements().get(i).maskMessage(definitionMessage, method);
                    }
                    // also mask the definition message
                    if (m.getDefinitionMessage() != null)
                    {
                    	m.getDefinitionMessage().maskMessage(definitionMessage, method);
                    }
                }
            } else {
                m.maskMessage(maskMessage, method);
            }

        }
    }

    @Override
    public void writeJSON(Writer writer) throws IOException {
        super.printElementJSON(writer, isArrayMessage());

    }

    @Override
    public void writeSimpleJSON(Writer writer) throws IOException {
        writer.write("{");
        printElementJSONTypeless(writer);
    }

    @Override
    public void writeSimpleJSON(Writer writer, String[] properties) throws IOException {
        writer.write("{");
        printElementJSONTypeless(getName(), writer, properties);
        writer.write("}");
    }

    @Override
    public void writeSimpleJSON(String name, Writer writer, String[] properties) throws IOException {
        printElementJSONTypeless(name, writer, properties);
    }

    // copied from original MergeUtils, can use a rewrite
    @Override
    public void writeAsCSV(Writer writer, String delimiter) throws IOException {
        if (getType().equals(Message.MSG_TYPE_ARRAY)) {
            if (getArraySize() > 0) {
                String header = "";
                Message h = getMessage(0);
                List<Property> props = h.getAllProperties();
                for (int i = 0; i < props.size(); i++) {
                    Property p = props.get(i);
                    String head = p.getDescription();
                    if ("null".equals(head) || head == null) {
                        head = "";
                    }
                    header = header + head + delimiter;
                }
                writer.write(header + "\n");

                for (int i = 0; i < getArraySize(); i++) {
                    String line = "";
                    Message current = getMessage(i);
                    if (current.getType() == Message.MSG_TYPE_DEFINITION) {
                        continue;
                    }
                    List<Property> prop = current.getAllProperties();
                    for (int j = 0; j < prop.size(); j++) {
                        Property p = prop.get(j);
                        if (p.getType().equals(Property.SELECTION_PROPERTY)) {
                            String value = p.getSelected().getName();
                            if ("null".equals(value) || value == null) {
                                value = "";
                            }
                            line = line + value + delimiter;
                        } else if (p.getType().equals(Property.DATE_PROPERTY)) {
                            String value = p.toString();
                            if ("null".equals(value) || value == null) {
                                value = "";
                            }
                            line = line + value + delimiter;
                        } else {
                            String value = p.getValue();
                            if ("null".equals(value) || value == null) {
                                value = "";
                            }
                            line = line + value + delimiter;
                        }
                    }
                    if (i < getArraySize() - 1) {
                        writer.write(line + "\n");
                    } else {
                        writer.write(line);
                    }
                }
            }
        } else {
            String header = "";
            String line = "";
            List<Property> props = getAllProperties();
            for (int i = 0; i < props.size(); i++) {
                Property p = props.get(i);
                header = header + p.getDescription() + delimiter;
                String value = p.getValue();
                if ("null".equals(value) || value == null) {
                    value = "";
                }
                line = line + value + delimiter;
            }
            writer.write(header + "\n");
            writer.write(line);
        }
    }

    @Override
    public void setMethod(String s) {
        myMethod = s;

    }

    @Override
    public String getMethod() {
        return myMethod;
    }

    @Override
    public Map<String, Object> getValueMap() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Property> e : propertyMap.entrySet()) {
            Object o = e.getValue().getTypedValue();
            if (o != null) {
                result.put(e.getKey(), o);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void printElement(final Writer sw, int indent) throws IOException {

        // Do not serialized message that have mode="ignore" or messages that
        // start with "__" (reserved for internal messages)
        if (Message.MSG_MODE_IGNORE.equals(getMode())) {
            return;
        }
        super.printElement(sw, indent);
    }

    @Override
    public void setValue(String propertyName, Object value) {
        Property p = getProperty(propertyName);
        if (p == null) {
            p = NavajoFactory.getInstance().createProperty(getRootDoc(), propertyName, Property.STRING_PROPERTY, "", 0,
                    "", Property.DIR_OUT);
            addProperty(p);
        }
        p.setAnyValue(value);
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public final String getSubType(String key) {
        if (subtypeMap != null) {
            return subtypeMap.get(key);
        }
        return null;

    }

    @Override
    public final Map<String, String> getSubTypes() {
        if (subtypeMap == null) {
            return new HashMap<>();
        }
        return new HashMap<>(subtypeMap);
    }

    @Override
    public void setSubType(String subType) {
        this.subType = subType;
        subtypeMap = NavajoFactory.getInstance().parseSubTypes(subType);
    }

    /**
     * isInternal: message name equals one of the five constants starting with __
     * Require this message is on rootlevel of the Navajo
     */
	@Override
	public boolean isInternal() {
		if( this.getParentMessage() != null && this.getRootDoc().getRootMessage().equals( this.getParentMessage() ) )
		{
			switch(myName) {
				case MSG_GLOBALS_BLOCK: return true;
				case MSG_PARAMETERS_BLOCK: return true;
				case MSG_AAA_BLOCK: return true;
				case MSG_ENTITY_BLOCK: return true;
				case MSG_TOKEN_BLOCK: return true;
				default: return false;
			}
		}
		else
		{
			return false;
		}
	}

}
