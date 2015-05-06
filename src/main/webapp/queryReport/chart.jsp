<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.parser.*"%>
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	String rptID = request.getParameter("rptID");
	Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
	Chart chart = rpt.getChart();
	if(chart==null){
		return ;
	}
	String swf =chart.getChartFile();
	String chartID = chart.getId();
	int width = chart.getWidth();
	int height = chart.getHeight();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <script language="JavaScript" src="<%=request.getContextPath()%>/queryReport/charts/FusionCharts.js"></script>
    </head>
	<body bgcolor="#ffffff">
	    <div id="chartdiv" align="center"></div>
	    <script type="text/javascript">
	        var myChart = new FusionCharts("<%=request.getContextPath()%>/queryReport/charts/<%=swf%>", "<%=chartID%>", "<%=width%>", "<%=height%>");
	        myChart.setDataURL("chart.query?doType=getChartData&rptID=<%=rptID%>&dataTemplate=<%=chart.getDataTemplateName()%>");
	        myChart.render("chartdiv");
	    </script>
	</body>
</html>