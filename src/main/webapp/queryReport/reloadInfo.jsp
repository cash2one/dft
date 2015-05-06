<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*"%>
<%@ page import="com.fruit.query.parser.*"%>
<%
	TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
	int cc = ltmp.getRptTemplates()==null?0:ltmp.getRptTemplates().size();
%>
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<title>查询——参数</title>
</head>
<body>
<span>刷新报表，共有<%=cc%>个报表模板！</span>
</body>
</html>