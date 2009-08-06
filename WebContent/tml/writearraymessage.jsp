<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="com.dexels.navajo.tipi.appmanager.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="java.util.*"%>
<%@ taglib prefix="c" uri="../WEB-INF/tags/c.tld"%>
<%@ taglib prefix="nav" uri="../WEB-INF/tags/navajo.tld"%>
<%@ page import="com.dexels.navajo.jsp.NavajoContext"%>
<jsp:useBean id="navajoContext" class="com.dexels.navajo.jsp.NavajoContext" scope="session" />
<jsp:useBean id="manager" class="com.dexels.navajo.tipi.appmanager.ApplicationManager" scope="page" />

<div class="messagetree">
<a href="#${navajoContext.messagePath}">${navajoContext.message.name}</a>
<table>
<c:forEach var="msg" items="${navajoContext.message.allMessages}">
	<nav:message message="${msg}">
		<c:import url="tml/writemessageelement.jsp"/>
	</nav:message>
</c:forEach>
</table>
</div>