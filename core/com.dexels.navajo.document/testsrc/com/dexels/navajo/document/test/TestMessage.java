/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;

public class TestMessage {

  NavajoDocumentTestFicture navajodocumenttestfictureInst = new NavajoDocumentTestFicture();
  private Navajo testDoc = null;
  
  private static final Logger logger = LoggerFactory.getLogger(TestMessage.class);


  @Before
  public void setUp() {
	System.setProperty("com.dexels.navajo.DocumentImplementation", "com.dexels.navajo.document.base.BaseNavajoFactoryImpl");
	    navajodocumenttestfictureInst.setUp();
    testDoc = navajodocumenttestfictureInst.testDoc;
  }

  @After
  public void tearDown() {
    navajodocumenttestfictureInst.tearDown();
    NavajoFactory.resetImplementation();
  }

  @Test
  public void testAddMessage() {
    Message msg = NavajoFactory.getInstance().createMessage(testDoc, "testmsg");
    Message sub = NavajoFactory.getInstance().createMessage(testDoc, "submsg");
    Message subsub = NavajoFactory.getInstance().createMessage(testDoc, "subsubmsg");
    msg.addMessage(sub);
    sub.addMessage(subsub);

    Message r = msg.getMessage("submsg");
    Assert.assertEquals("submsg", r.getName());
    Message r2 = r.getMessage("subsubmsg");
    Assert.assertNotNull(r2);

    Message sub2 = NavajoFactory.getInstance().createMessage(testDoc, "submsg");
    msg.addMessage(sub2);
    r = msg.getMessage("submsg");
    r2 = r.getMessage("subsubmsg");
    Assert.assertNull(r2);
    // Check robustness.
    sub2.addMessage(null);
  }

  @Test
  public void testAddMessage1() {

    // Same as before only now messages with name are not overwritten!
    Message msg = NavajoFactory.getInstance().createMessage(testDoc, "testmsg");
    Message sub = NavajoFactory.getInstance().createMessage(testDoc, "submsg");
    Message subsub = NavajoFactory.getInstance().createMessage(testDoc, "subsubmsg");
    msg.addMessage(sub, false);
    sub.addMessage(subsub, false);

    Message r = msg.getMessage("submsg");
    Assert.assertEquals("submsg", r.getName());
    Message r2 = r.getMessage("subsubmsg");
    Assert.assertNotNull(r2);

    Message sub2 = NavajoFactory.getInstance().createMessage(testDoc, "submsg");
    msg.addMessage(sub2, false);
    r = msg.getMessage("submsg");
    r2 = r.getMessage("subsubmsg");
    Assert.assertNotNull(r2);

  }

  @Test
  public void testAddProperty() {
      Message m = testDoc.getMessage("testmessage");
      Property p = NavajoFactory.getInstance().createProperty(testDoc, "testprop", Property.STRING_PROPERTY, "navajo rules", 120, "", Property.DIR_OUT);
      m.addProperty(p);
      Property p2 = m.getProperty("testprop");
      Assert.assertEquals("testprop", p2.getName());
      Assert.assertEquals("navajo rules", p2.getValue());
      Property p3 =  NavajoFactory.getInstance().createProperty(testDoc, "testprop", Property.STRING_PROPERTY, "navajo rules big time", 120, "", Property.DIR_OUT);
      m.addProperty(p3);
      p2 = m.getProperty("testprop");
      Assert.assertEquals("navajo rules big time", p2.getValue());
      // Check robustness.
      try {
		m.addProperty(null);
		Assert.fail();
      } catch (NullPointerException e) {
		// ok
	}
  }

  @Test
  public void testContains() {
    Message m = testDoc.getMessage("testmessage");
    boolean b = m.contains("testprop1");
    Assert.assertTrue(b);
    b = m.contains("testprop2");
    Assert.assertTrue(!b);
  }

  @Test
  public void testCreate() {
    Message m = NavajoFactory.getInstance().createMessage(testDoc, "bliep");
    Assert.assertNotNull(m);
    Assert.assertEquals("bliep", m.getName());
  }

  @Test
  public void testGetParentMessage() {
    Message m = testDoc.getMessage("testmessage/testmessage_sub1");
    Assert.assertEquals("testmessage_sub1", m.getName());
    Message p = m.getParentMessage();
    Assert.assertEquals("testmessage", p.getName());
  }

  @Test
  public void testGetAllMessages() {
    Message m = testDoc.getMessage("testmessage");
    List<Message> all = m.getAllMessages();
    Assert.assertEquals(3, all.size());
    Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
    Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());
    Assert.assertEquals("testmessage_sub3", (all.get(2)).getName());
    m = testDoc.getMessage("testmessage2");
    all = m.getAllMessages();
    Assert.assertNotNull(all);
    Assert.assertEquals(0, all.size());
  }

  @Test
  public void testGetAllProperties() {
    Message m = testDoc.getMessage("testmessage");
    List<Property> all = m.getAllProperties();
    Assert.assertEquals(6, all.size());
    Assert.assertEquals("testprop1", (all.get(0)).getName());
    Assert.assertEquals("stringprop", (all.get(1)).getName());
    Assert.assertEquals("integerprop", (all.get(2)).getName());
    Assert.assertEquals("propfloat", (all.get(3)).getName());
    m = testDoc.getMessage("testmessage2");
    all = m.getAllProperties();
    Assert.assertNotNull(all);
    Assert.assertEquals(0, all.size());
  }

  @Test
  public void testGetFullMessageName() {
    Message m = testDoc.getMessage("testmessage").getMessage("testmessage_sub1").getMessage("testmessage_sub1_sub1");
    Assert.assertEquals("/testmessage/testmessage_sub1/testmessage_sub1_sub1", m.getFullMessageName());
  }

  @Test
  public void testGetMessage() {
    Message m = testDoc.getMessage("testmessage");
    Message m2 = m.getMessage("testmessage_sub1");
    Assert.assertNotNull(m2);
    Assert.assertEquals("testmessage_sub1", m2.getName());
    Message m3 = m2.getMessage("../testmessage_sub2");
    Assert.assertEquals("testmessage_sub2", m3.getName());
  }
  
  @Test
  public void testGetMessageDot() {
	  Message m = testDoc.getMessage("testmessage");
	  Message m2 = m.getMessage(".");
	  Assert.assertNotNull(m2);
	  Assert.assertEquals("testmessage", m2.getName());
  }
  
  @Test
  public void testGetMessageDot2() {
	  Message m = testDoc.getMessage("testmessage");
	  Message m2 = m.getMessage("./"); // Bogus.
	  // This should give null.
	  Assert.assertNull(m2);
  }
  
  @Test
  public void testGetMessageDot3() {
	  Message m = testDoc.getMessage("testmessage");
	  Message m2 = m.getMessage("./testmessage_sub1");
	  Assert.assertNotNull(m2);
	  Assert.assertEquals("testmessage_sub1", m2.getName());
  }
  
  @Test
  public void testGetMessages() {

    // Regular expression message name match testing.
      Message m = testDoc.getMessage("testmessage");

      List<Message> all = m.getMessages("testmessage_sub.*");
      Assert.assertEquals(3, all.size());
      Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());
      Assert.assertEquals("testmessage_sub3", (all.get(2)).getName());

      all = m.getMessages("testmessage_sub");
      Assert.assertEquals(0, all.size());

      all = m.getMessages("testmessage_sub[0-3]");
      Assert.assertEquals(3, all.size());
      Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());
      Assert.assertEquals("testmessage_sub3", (all.get(2)).getName());

      all = m.getMessages("testmessage_sub[0-2]");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());

      all = m.getMessages("test[A-z]*_sub[0-2]");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());

      all = m.getMessages("test[A-d]*_sub[0-2]");
      Assert.assertEquals(0, all.size());

      // Tests with absolute message offset (starting at root of TML)
      all = m.getMessages("/testmessage_sub[0-2]");
      Assert.assertEquals(0, all.size());

      all = m.getMessages("/testmessage/test[A-z]*_sub[0-2]");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("testmessage_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub2", (all.get(1)).getName());

      all = m.getMessages("/testmessage/testmessage_sub.*/testmessage_sub1_sub[2-3]");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("testmessage_sub1_sub2", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub1_sub3", (all.get(1)).getName());

      all = m.getMessages("/testmessage/testmessage_sub.*/testmessage_sub.*_sub[2-3]");
      Assert.assertEquals(4, all.size());
      Assert.assertEquals("testmessage_sub1_sub2", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub1_sub3", (all.get(1)).getName());
      Assert.assertEquals("testmessage_sub2_sub2", (all.get(2)).getName());
      Assert.assertEquals("testmessage_sub2_sub3", (all.get(3)).getName());

      all = m.getMessages("testmessage_sub1/testmessage_sub1_sub.*");
      Assert.assertEquals(4, all.size());
      Assert.assertEquals("testmessage_sub1_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub1_sub2", (all.get(1)).getName());
      Assert.assertEquals("testmessage_sub1_subje", (all.get(2)).getName());
      Assert.assertEquals("testmessage_sub1_sub3", (all.get(3)).getName());

      all = m.getMessages("testmessage_sub1/(testmessage){0,1}_sub1_sub.*");
      Assert.assertEquals(4, all.size());
      Assert.assertEquals("testmessage_sub1_sub1", (all.get(0)).getName());
      Assert.assertEquals("testmessage_sub1_sub2", (all.get(1)).getName());
      Assert.assertEquals("testmessage_sub1_subje", (all.get(2)).getName());
      Assert.assertEquals("testmessage_sub1_sub3", (all.get(3)).getName());

  }

  @Test
  public void testGetName() {
    Message m = testDoc.getMessage("testmessage");
    Assert.assertEquals("testmessage", m.getName());
  }

  @Test
  public void testGetProperties() {

      Message m = testDoc.getMessage("testmessage");

      List<Property> all = m.getProperties("[A-z]*prop");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("stringprop", (all.get(0)).getName());
      Assert.assertEquals("integerprop", (all.get(1)).getName());

      all = m.getProperties("[A-z]*propjes");
      Assert.assertEquals(0, all.size());

      all = m.getProperties("testmessage_sub1/testmessage_sub1_sub.*/[A-z]*");
      Assert.assertEquals(1, all.size());
      Assert.assertEquals("proppie", (all.get(0)).getName());

      m = m.getMessage("testmessage_sub1");
      all = m.getProperties("../[A-z]*prop");
      Assert.assertEquals(2, all.size());
      Assert.assertEquals("stringprop", (all.get(0)).getName());
      Assert.assertEquals("integerprop", (all.get(1)).getName());

      m = m.getMessage("testmessage_sub1_sub2");
      all = m.getProperties("../testmessage_sub1_sub1/[A-z]*");
      Assert.assertEquals(1, all.size());
      Assert.assertEquals("proppie", (all.get(0)).getName());

   
  }

  @Test
  public void testGetProperty() {
    Message m = testDoc.getMessage("testmessage");
    Property p = m.getProperty("integerprop");
    Assert.assertNotNull(p);
    Assert.assertEquals("integerprop", p.getName());
    p = m.getProperty("integerpropje");
    Assert.assertNull(p);
  }

  @Test
  public void testRemoveMessage() {
    Message m = testDoc.getMessage("testmessage");
    Message m2 = m.getMessage("testmessage_sub2");
    Assert.assertEquals("testmessage_sub2", m2.getName());
    m.removeMessage(m.getMessage("testmessage_sub2"));
    m2 = m.getMessage("testmessage_sub2");
    Assert.assertNull(m2);
  }
  
  @Test
  public void testRemoveMessage2() {
	    Message m = testDoc.getMessage("testmessage/testmessage_sub1");
	    Assert.assertNotNull(m);
	    testDoc.removeMessage(m);
	    Message m2 = testDoc.getMessage("testmessage/testmessage_sub1");
	    Assert.assertNull(m2);
	    Message m3 = testDoc.getMessage("testmessage");
	    Assert.assertNotNull(m3);
	  }

  @Test
  public void testRemoveProperty() {
     Message m = testDoc.getMessage("testmessage");
     Property p = m.getProperty("propfloat");
     Assert.assertEquals("propfloat", p.getName());
     m.removeProperty(m.getProperty("propfloat"));
     p = m.getProperty("propfloat");
     Assert.assertNull(p);
  }

  @Test
  public void testArrayMessages() {
	 
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message a = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage", "array");
	  m.addMessage(a);
	  for (int i = 0; i < 5; i++) {
		  Message a1 = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
		  a.addMessage(a1);
		  Property p = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", "string", "noot" + i, 0, "", "in");
		  a1.addProperty(p);
	  }
	  assertEquals("array", testDoc.getMessage("/MyTop/MyArrayMessage").getType());
	  assertEquals(5, testDoc.getMessage("/MyTop/MyArrayMessage").getArraySize());
	  assertNotNull(testDoc.getProperty("/MyTop/MyArrayMessage@1/MyProp"));
	  assertEquals("noot3", testDoc.getProperty("/MyTop/MyArrayMessage@3/MyProp").getValue());
	  
	  List<Message> al = testDoc.getMessages("/MyTop/MyArrayMessage");
	  for (int i = 0; i < al.size(); i++) {
		  assertEquals("noot"+i, (al.get(i)).getProperty("MyProp").getValue());
		  // MyTop is my parent message(!) parent array is ignored!!
		  assertEquals("MyTop", (al.get(i)).getParentMessage().getName());
	  }
  }
  
  @Test
  public void testArrayMessagesWithModeIsIgnore() {
		 
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message a = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage", "array");
	  a.setMode(Message.MSG_MODE_IGNORE);
	  m.addMessage(a);
	  for (int i = 0; i < 5; i++) {
		  Message a1 = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
		  a.addMessage(a1);
		  Property p = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", "string", "noot" + i, 0, "", "in");
		  a1.addProperty(p);
	  }
	  assertEquals("array", testDoc.getMessage("/MyTop/MyArrayMessage").getType());
	  assertEquals(1, testDoc.getMessage("/MyTop/MyArrayMessage").getArraySize());
	  
  }
  
  @Test
  public void testArrayMessagesWithHash() {
		 
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message a = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage", "array");
	  m.addMessage(a);
	  for (int i = 0; i < 5; i++) {
		  Message a1 = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
		  a.addMessage(a1);
		  Property p = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", "string", "noot" + i, 0, "", "in");
		  a1.addProperty(p);
		  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "aap" + i, 0, "", "in");
		  a1.addProperty(p2);
	  }
	  assertEquals("array", testDoc.getMessage("/MyTop/MyArrayMessage").getType());
	  assertEquals(5, testDoc.getMessage("/MyTop/MyArrayMessage").getArraySize());
	  assertNotNull(testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot2/MyProp2"));
	  assertEquals("aap2", testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot2/MyProp2").getValue());
	  
	  assertNotNull(testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot3/MyProp2"));
	  assertEquals("aap3", testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot3/MyProp2").getValue());
	  
	  assertNotNull(testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot4/MyProp2"));
	  assertNotSame("aap3", testDoc.getProperty("/MyTop/MyArrayMessage@MyProp=noot4/MyProp2").getValue());
	  
  }
  
  @Test
  public void testSetName(){
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  m.setName("MyOtherTop");
	  assertNull(testDoc.getMessage("MyTop"));
	  assertNotNull(testDoc.getMessage("MyOtherTop"));
  }
  
  @Test
  public void testSetScope() {
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  m.setScope(Message.MSG_SCOPE_GLOBAL);
	  assertNotNull(m.getScope());
	  assertEquals(Message.MSG_SCOPE_GLOBAL, m.getScope());
  }

  @Test
  public void testAddIgnoreMessage() {
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message m2 = NavajoFactory.getInstance().createMessage(testDoc, "MyIgnoredMessage");
	  m2.setMode("ignore");
	  m.addMessage(m2);
	  assertNotNull(testDoc.getMessage("/MyTop/MyIgnoredMessage"));
	  StringWriter sw = new StringWriter();
	  assertEquals(sw.toString().indexOf("MyIgnoredMessage"), -1);
  }
  
  @Test 
  public void testSetType() {
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message a = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage", "array");
	  m.addMessage(a);
	  for (int i = 0; i < 2; i++) {
		  Message a1 = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
		  a.addMessage(a1);
		  String type = ( i == 0 ? "integer" : "" );
		  Property p = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", type, ""+i, 0, "", "in");
		  p.setAnyValue(i);
		  a1.addProperty(p);
		  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", type, ""+i, 0, "", "in");
		  p2.setAnyValue(i);
		  a1.addProperty(p2);
		  // Set type of first child to definition.
		  if ( i == 0 ) {
			  a1.setType(Message.MSG_TYPE_DEFINITION);
		  }
	  }
	  
	  Assert.assertEquals(1, a.getArraySize());
	  Assert.assertNotNull(a.getDefinitionMessage());
	  Assert.assertEquals("integer", a.getAllMessages().get(0).getProperty("MyProp").getType());
	
  }
  
  @Test
  public void testAddIgnoreArrayMessageElements() {
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(m);
	  Message m2 = NavajoFactory.getInstance().createMessage(testDoc, "MyIgnoredMessage");
	  m2.setMode("ignore");
	  m2.setType(Message.MSG_TYPE_ARRAY);
	  m.addMessage(m2);
	  assertNotNull(testDoc.getMessage("/MyTop/MyIgnoredMessage"));
	  StringWriter sw = new StringWriter();
	  
	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyIgnoredMessage");
	  m3.setMode("ignore");
	  m2.addMessage(m3);
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", "string", "AAPJES", 0, "", "in", "");
	  m3.addProperty(p1);
	  
	  Message m4 =  NavajoFactory.getInstance().createMessage(testDoc, "MyIgnoredMessage");
	  m4.setMode("ignore");
	  m2.addMessage(m4);
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp", "string", "NOOTJES", 0, "", "in", "");
	  m4.addProperty(p2);
	  
	  testDoc.write(sw);
	  
	  logger.info(sw.toString());
	  assertTrue(sw.toString().indexOf("AAPJES") == -1);
	  assertTrue(sw.toString().indexOf("NOOTJES") == -1);
  }
  
  @Test
  public void testMaskMessage() {
	  Message myMessage = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  testDoc.addMessage(myMessage);
	  
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAP", 0, "", "in", "");
	  myMessage.addProperty(p1);
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "NOOT", 0, "", "in", "");
	  myMessage.addProperty(p2);
	 
	  Message arrayMsg = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
	  arrayMsg.setType(Message.MSG_TYPE_ARRAY);
	  myMessage.addMessage(arrayMsg);
	  
	  for ( int i = 0; i < 5; i++ ) {
		  Message child = NavajoFactory.getInstance().createMessage(testDoc, "MyArrayMessage");
		  arrayMsg.addElement(child);
		  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp3", "string", "AAP"+i, 0, "", "in", "");
		  child.addProperty(p3);
		  Property p4 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp4", "string", "NOOT"+i, 0, "", "in", "");
		  child.addProperty(p4);
		  Property p5 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp5", "string", "MIES"+i, 0, "", "in", "");
		  child.addProperty(p5);
		  Property p6 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp6", "string", "VUUR"+i, 0, "", "in", "");
		  child.addProperty(p6);
	  }
	  
	  Message someChildMsg = NavajoFactory.getInstance().createMessage(testDoc, "SomeChildMessage");
	  myMessage.addMessage(someChildMsg);
	  
	  Message someValidChildMsg = NavajoFactory.getInstance().createMessage(testDoc, "SomeValidChildMessage");
	  myMessage.addMessage(someValidChildMsg);
	  Property p7 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp7", "string", "VUUR", 0, "", "in", "");
	  someValidChildMsg.addProperty(p7);
	  Property p8 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp8", "string", "VUUR", 0, "", "in", "");
	  someValidChildMsg.addProperty(p8);
	  
	  // Construct mask.
	  Navajo maskDoc = NavajoFactory.getInstance().createNavajo();
	  Message myMask = NavajoFactory.getInstance().createMessage(maskDoc, "MyTop");
	  maskDoc.addMessage(myMask);
	  Property mp2 = NavajoFactory.getInstance().createProperty(maskDoc, "MyProp2", "string", "NOOT", 0, "", "in", "");
	  myMask.addProperty(mp2);
	  Message maskArrayMsg = NavajoFactory.getInstance().createMessage(maskDoc, "MyArrayMessage");
	  maskArrayMsg.setType(Message.MSG_TYPE_ARRAY);
	  myMask.addMessage(maskArrayMsg);
	  Message defMsg = NavajoFactory.getInstance().createMessage(maskDoc, "MyArrayMessage");
	  Property mp4 = NavajoFactory.getInstance().createProperty(maskDoc, "MyProp4", "integer", "", 0, "", "in", "");
	  defMsg.addProperty(mp4);
	  Property mp5 = NavajoFactory.getInstance().createProperty(maskDoc, "MyProp5", "string", "", 0, "", "in", "");
	  defMsg.addProperty(mp5);
	  maskArrayMsg.setDefinitionMessage(defMsg);
	  
	  Message validChild = NavajoFactory.getInstance().createMessage(maskDoc, "SomeValidChildMessage");
	  myMask.addMessage(validChild);
	  Property mp7 = NavajoFactory.getInstance().createProperty(maskDoc, "MyProp7", "string", "", 0, "", "in", "");
	  validChild.addProperty(mp7);
	  
	  Assert.assertNotNull(myMessage.getProperty("MyProp1"));
	  Assert.assertNotNull(myMessage.getProperty("MyProp2"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp3"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp6"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp4"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp5"));
	  Assert.assertNotNull(myMessage.getMessage("SomeChildMessage"));
	  Assert.assertNotNull(myMessage.getProperty("SomeValidChildMessage/MyProp7"));
	  Assert.assertNotNull(myMessage.getProperty("SomeValidChildMessage/MyProp8"));
	  
	  myMessage.maskMessage(myMask);
	  Assert.assertNull(myMessage.getProperty("MyProp1"));
	  Assert.assertNotNull(myMessage.getProperty("MyProp2"));
	  Assert.assertNull(myMessage.getProperty("MyArrayMessage@0/MyProp3"));
	  Assert.assertNull(myMessage.getProperty("MyArrayMessage@0/MyProp6"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp4"));
	  Assert.assertNotNull(myMessage.getProperty("MyArrayMessage@0/MyProp5"));
	  Assert.assertEquals("integer", myMessage.getProperty("MyArrayMessage@0/MyProp4").getType());
	  Assert.assertNull(myMessage.getMessage("SomeChildMessage"));
	  Assert.assertNotNull(myMessage.getProperty("SomeValidChildMessage/MyProp7"));
	  Assert.assertNull(myMessage.getProperty("SomeValidChildMessage/MyProp8"));
	  
  }
  
  @Test
  public void testMergeMessage() {
	  
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(1)", 0, "", "in", "");
	  m.addProperty(p1);
	  testDoc.addMessage(m);
	  
	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "AAPJES(2)", 0, "", "in", "");
	  m3.addProperty(p2);
	  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp3", "string", "AAPJES(3)", 0, "", "in", "");
	  m3.addProperty(p3);
	  
	  m.merge(m3);
	  
	  assertNotNull(m.getProperty("MyProp3"));
	  assertEquals(m.getProperty("MyProp3").getValue(), "AAPJES(3)");
	  
  }
  
  @Test
  public void testMergeMessageWithPreferThis() {
	  
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(1)", 0, "", "in", "");
	  m.addProperty(p1);
	  testDoc.addMessage(m);
	  
	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(2)", 0, "", "in", "");
	  m3.addProperty(p2);
	  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "AAPJES(3)", 0, "", "in", "");
	  m3.addProperty(p3);
	  
	  m.merge(m3, true);
	  
	  assertNotNull(m.getProperty("MyProp1"));
	  assertEquals(m.getProperty("MyProp1").getValue(), "AAPJES(1)");
	  
  }
  
  @Test
  public void testMergeMessageWithPreferThisFalse() {
	  
	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(1)", 0, "", "in", "");
	  m.addProperty(p1);
	  testDoc.addMessage(m);
	  
	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(2)", 0, "", "in", "");
	  m3.addProperty(p2);
	  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "AAPJES(3)", 0, "", "in", "");
	  m3.addProperty(p3);
	  
	  m.merge(m3, false);
	  
	  assertNotNull(m.getProperty("MyProp1"));
	  assertEquals(m.getProperty("MyProp1").getValue(), "AAPJES(2)");
	  
  }
  
  @Test
  public void testMergeMessage2() {

	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(1)", 0, "", "in", "");
	  m.addProperty(p1);
	  testDoc.addMessage(m);

	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTop2");
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "AAPJES(2)", 0, "", "in", "");
	  m3.addProperty(p2);
	  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp3", "string", "AAPJES(3)", 0, "", "in", "");
	  m3.addProperty(p3);

	  m.merge(m3);

	  // MyProp3 should be merged althoug m3 has name "MyTop2" and m has name "MyTop".
	  assertNotNull(m.getProperty("MyProp3"));
	  // message "MyTop2" should not exist at all...
	  assertNull(m.getMessage("MyTop2"));

  }
  
  @Test
  public void testMergeMessage3() {

	  Message m = NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p1 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp1", "string", "AAPJES(1)", 0, "", "in", "");
	  m.addProperty(p1);
	  testDoc.addMessage(m);

	  Message m3 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTop");
	  Property p2 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp2", "string", "AAPJES(2)", 0, "", "in", "");
	  m3.addProperty(p2);
	  Property p3 = NavajoFactory.getInstance().createProperty(testDoc, "MyProp3", "string", "AAPJES(3)", 0, "", "in", "");
	  m3.addProperty(p3);
	  Message m4 =  NavajoFactory.getInstance().createMessage(testDoc, "MyTopSub");
	  Property p4 = NavajoFactory.getInstance().createProperty(testDoc, "MyPropSub1", "string", "AAPJES(3)", 0, "", "in", "");
	  m4.addProperty(p4);
	  m3.addMessage(m4);
	  
	  m.merge(m3);

	  assertNotNull(m.getMessage("MyTopSub"));
	  assertNotNull(m.getMessage("MyTopSub").getProperty("MyPropSub1"));

  }

  @Test
 public void testIsEqual()  {

	  Navajo n2 = NavajoFactory.getInstance().createNavajo();
	  n2 = testDoc.copy();
	   
	  logger.info("THIS IS N2");
	  
	  Message m1 = testDoc.getMessage("testmessage");
	  Message m2 = n2.getMessage("testmessage");
	  
	  assertTrue(m1.isEqual(m2));
	  
	  m2.getProperty("stringprop").setValue("ietsanders");
	  
	  assertFalse(m1.isEqual(m2));
	  
  }
  
	
	@Test
	public void testValueMap() {
		Navajo n = NavajoFactory.getInstance().createNavajo(getClass().getResourceAsStream("message.xml"));
		Message m = n.getMessage("SimpleMessage");
		Map<String,Object> vv = m.getValueMap();
		Assert.assertNotNull(vv);
		Object pString = vv.get("pString");
		Assert.assertNotNull(pString);
		Assert.assertTrue(pString instanceof String);
		Object pBoolean = vv.get("pBoolean");
		Assert.assertNotNull(pBoolean);
		Assert.assertTrue(pBoolean instanceof Boolean);
		Assert.assertEquals(3,vv.size());
	}

	@Test
	public void testMessageSubType() {
		Navajo n = NavajoFactory.getInstance().createNavajo(getClass().getResourceAsStream("subtypemessage.xml"));
		Message m = n.getMessage("SubTypeMessage");
		Map<String,String> stypes = m.getSubTypes();
		logger.info("Subtypes: {}", stypes);
		Assert.assertEquals(2, stypes.size());
		Assert.assertEquals("fa", stypes.get("mi"));
		Assert.assertEquals("fa", m.getSubType("mi"));
		m.setSubType("sol=la");
		Assert.assertEquals("la", m.getSubType("sol"));
		Assert.assertEquals(1, m.getSubTypes().size());
		StringWriter sw = new StringWriter();
		m.write(sw);
		logger.info("sw: {}", sw);
		Assert.assertTrue(sw.toString().indexOf("sol")!=-1);
		Assert.assertTrue(sw.toString().indexOf("fa")==-1);
		
	}
}
