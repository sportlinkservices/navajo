package com.dexels.navajo.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.script.api.UserException;
import com.dexels.navajo.server.Dispatcher;
import com.dexels.navajo.server.DispatcherFactory;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableException;

import static com.dexels.navajo.document.Property.INTEGER_PROPERTY;;

public class TestNavajoMap {
    private static final Logger logger = LoggerFactory.getLogger(TestNavajoMap.class);

    private NavajoMap map;
    private Navajo outDoc;

    @Before
    public void setup() {

        map = new NavajoMap();
        outDoc = NavajoFactory.getInstance().createNavajo();
        map.setOutDoc(outDoc);
    }

    @After
    public void destroy() {

        map = null;
        outDoc = null;
    }

    @Test
    public void testCheckSubArray() throws UserException {

        String testCase = "/parentMessage/ArrayMessage@0/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        // System.out.println(map.outDoc);
        if (map.outDoc.getMessage("parentMessage").getMessage("ArrayMessage").getMessage(0).getProperty("newProperty").getName()
                .equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testCheckSubArrayWithChildren() throws UserException {

        String testCase = "/parentMessage/ArrayMessage@0/arrayMessageChild/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        // System.out.println(map.outDoc);
        if (map.outDoc.getMessage("parentMessage").getMessage("ArrayMessage").getMessage(0).getMessage("arrayMessageChild")
                .getProperty("newProperty").getName().equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testTopLevelArray() throws UserException {

        String testCase = "/ArrayMessage@0/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        // System.out.println(map.outDoc);
        if (map.outDoc.getMessage("ArrayMessage").getMessage(0).getProperty("newProperty").getName()
                .equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testTopLevelArrayWithMultipleChildrenMessages() throws UserException {

        String testCase = "/ArrayMessage@0/arrayMessageChild/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        if (map.outDoc.getMessage("ArrayMessage").getMessage(0).getMessage("arrayMessageChild").getProperty("newProperty").getName()
                .equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testNestedLevelArrays() throws UserException {

        String testCase = "/ArrayMessageParrent@0/ArrayMessageChild@0/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        if (map.outDoc.getMessage("ArrayMessageParrent").getMessage(0).getMessage("ArrayMessageChild").getMessage(0)
                .getProperty("newProperty").getName().equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testSimplePropertyMessage() throws UserException {

        String testCase = "/SimpleMessage/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        if (map.outDoc.getMessage("SimpleMessage").getProperty("newProperty").getName().equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testSimplePropertyNestedMessage() throws UserException {

        String testCase = "/SimpleMessageParent/SimpleMessageChild/newProperty";
        map.setPropertyName(testCase);
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(3);

        if (map.outDoc.getMessage("SimpleMessageParent").getMessage("SimpleMessageChild").getProperty("newProperty").getName()
                .equals("newProperty")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testCopyInputMessage() throws UserException {

        DispatcherFactory.createDispatcher(new Dispatcher());
        final String TEST_INTEGER = "6";
        final String ALTERNATE_TEST_INTEGER = "3";

        // Create a message on the in doc with a test value.
        Navajo inDoc = NavajoFactory.getInstance().createNavajo();
        Message m = NavajoFactory.getInstance().createMessage(outDoc, "SimpleMessage");
        Property p = NavajoFactory.getInstance().createProperty(outDoc, "newProperty", INTEGER_PROPERTY, TEST_INTEGER, 0, "", "out");
        m.setType(Message.MSG_TYPE_SIMPLE);
        m.addProperty(p);
        inDoc.addMessage(m);

        // Initialize the navajomap.
        Access access = new Access();
        access.setInDoc(inDoc);
        access.setOutputDoc(outDoc);
        try {
            map.load(access);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }

        // Create a property on the navajomap with a different value that could be overwritten with the test value.
        map.setPropertyName("/SimpleMessage/newProperty");
        map.setPropertyType(INTEGER_PROPERTY);
        map.setProperty(ALTERNATE_TEST_INTEGER);

        // Test the method while copying the input message.
        map.setCopyInputMessages("SimpleMessage");
        map.prepareOutDoc();

        // Ensure that the property created on the navajomap is not overwritten.
        if (map.outDoc.getMessage("SimpleMessage").getProperty("newProperty").getValue().equals(ALTERNATE_TEST_INTEGER)) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testSendThroughNotSet() throws MappableException, UserException {

        DispatcherFactory.createDispatcher(new Dispatcher());
        Navajo inDoc = NavajoFactory.getInstance().createNavajo();

        Property property = NavajoFactory.getInstance().createProperty(inDoc, "Meaning",
                INTEGER_PROPERTY, "42", 0, "", "out");
        Message message = NavajoFactory.getInstance().createMessage(inDoc, "SendThrough");
        message.addProperty(property);
        inDoc.addMessage(message);

        Message global = NavajoFactory.getInstance().createMessage(inDoc, "Global");
        global.setScope(Message.MSG_SCOPE_GLOBAL);
        inDoc.addMessage(global);

        Access access = new Access();
        access.setInDoc(inDoc);
        access.setOutputDoc(outDoc);

        map.load(access);
        map.prepareOutDoc();

        assertFalse(map.getSendThrough());
        assertNull(map.outDoc.getMessage("SendThrough"));
        assertEquals(Message.MSG_SCOPE_GLOBAL, map.outDoc.getMessage("Global").getScope());
    }

    @Test
    public void testSendThroughSetFalse() throws MappableException, UserException {

        DispatcherFactory.createDispatcher(new Dispatcher());
        Navajo inDoc = NavajoFactory.getInstance().createNavajo();

        Property property = NavajoFactory.getInstance().createProperty(inDoc, "Meaning",
                INTEGER_PROPERTY, "42", 0, "", "out");
        Message message = NavajoFactory.getInstance().createMessage(inDoc, "SendThrough");
        message.addProperty(property);
        inDoc.addMessage(message);

        Message global = NavajoFactory.getInstance().createMessage(inDoc, "Global");
        global.setScope(Message.MSG_SCOPE_GLOBAL);
        inDoc.addMessage(global);

        Access access = new Access();
        access.setInDoc(inDoc);
        access.setOutputDoc(outDoc);

        map.load(access);
        map.setSendThrough(false);
        map.prepareOutDoc();

        assertFalse(map.getSendThrough());
        assertNull(map.outDoc.getMessage("SendThrough"));
        assertEquals(Message.MSG_SCOPE_GLOBAL, map.outDoc.getMessage("Global").getScope());
    }

    @Test
    public void testSendThroughSetTrue() throws MappableException, UserException {

        DispatcherFactory.createDispatcher(new Dispatcher());
        Navajo inDoc = NavajoFactory.getInstance().createNavajo();

        Property property = NavajoFactory.getInstance().createProperty(inDoc, "Meaning",
                INTEGER_PROPERTY, "42", 0, "", "out");
        Message message = NavajoFactory.getInstance().createMessage(inDoc, "SendThrough");
        message.addProperty(property);
        inDoc.addMessage(message);

        Message local = NavajoFactory.getInstance().createMessage(inDoc, "Local");
        local.setScope(Message.MSG_SCOPE_LOCAL);
        inDoc.addMessage(local);

        Access access = new Access();
        access.setInDoc(inDoc);
        access.setOutputDoc(outDoc);

        map.load(access);
        map.setSendThrough(true);
        map.prepareOutDoc();

        assertTrue(map.getSendThrough());
        assertEquals("42", map.outDoc.getMessage("SendThrough").getProperty("Meaning").getValue());
        assertNull(map.outDoc.getMessage("Local"));
    }

}
