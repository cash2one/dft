<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*"%>
<%@ page import="com.fruit.query.data.*"%>
<%@ page import="com.fruit.query.parser.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.service.*"%>
<%@ page import="org.apache.commons.lang.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Report rpt=(Report)request.getAttribute("cReport");
	if(rpt==null){
		out.print("</head><body>");
		out.print("<p>未找到相应的报表定义！</p>");
		out.print("</body></html>");
		return;
	}
	RptMultiHeader header=(RptMultiHeader)request.getAttribute("rptHeader");
	//RptMultiHeader header=(RptMultiHeader)session.getAttribute("rptHeader"); 
	if(header==null){
		out.print("</head><body>");
		out.print("<p>报表设计文件中表头定义错误！</p>");
		out.print("</body></html>");
		return;
	}
	Map mPavalues=(Map)session.getAttribute("paraVals");
	List paras=rpt.getParas();
	RptDataService rsvr = RptDataService.getReportDataService();
	String strTitle = "",stLeft ="",stCenter="",stRight="",fLeft="",fCenter="",fRight="";
	//解析
	if(rpt.getTitle()!=null){
		strTitle = rsvr.parseParaExp(rpt.getTitle().getTitleExp(),rpt,mPavalues);
	}
	if(rpt.getHead()!=null&&rpt.getHead().getSubTitle()!=null){
		stLeft = rsvr.parseParaExp(rpt.getHead().getSubTitle().getLeftExp(),rpt, mPavalues);
		stCenter =rsvr.parseParaExp(rpt.getHead().getSubTitle().getCenterExp(),rpt,mPavalues);
		String stRightExp = rpt.getHead().getSubTitle().getRightExp();
		stRight = rsvr.parseParaExp(rpt.getHead().getSubTitle().getRightExp(),rpt, mPavalues);
	}
	if(rpt.getFoot()!=null){
		fLeft =rsvr.parseParaExp(rpt.getFoot().getLeftExp(),rpt,mPavalues);
		fCenter =rsvr.parseParaExp(rpt.getFoot().getCenterExp(),rpt,mPavalues);
		fRight =rsvr.parseParaExp(rpt.getFoot().getRightExp(),rpt,mPavalues);
	}
	StringBuffer strParas=new StringBuffer("&rptID=");
	strParas.append(rpt.getId());
	String pidFld=rpt.getDefaultDataDef().getParentID();
	String ridFld=rpt.getDefaultDataDef().getRecordID();
	//如果是动态列，在session中获取参与自动合计的列，如果是静态列，从report模板中读取设置。
	String[] totalFlds=rpt.getTotalFields();
	if(rpt.getColumnDef().getSourceType()!=0){
		List tflds=new ArrayList();
		for(int i=0;i<header.getSortedNodes().size();i++){
    		Column col=(Column)header.getSortedNodes().get(i);
    	    if(col.getDataType()==1||col.getDataType()==2){
    	    	tflds.add(col.getDataIndex());
    	    }
		}
		totalFlds=new String[tflds.size()];
		for(int i=0;i<tflds.size();i++){
			totalFlds[i]=(String)tflds.get(i);
		}
	}
	Map muts = TemplatesLoader.getTemplatesLoader().getUnitsMap();
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	StringBuffer strHead = new StringBuffer("");
	StringBuffer strFoot = new StringBuffer("");
	boolean hasHead = false, hasFoot = false ,titleInHead = false;
	if(rpt!=null){
		if(rpt.getHead()!=null){
			hasHead = true;
			Head head = rpt.getHead();
			titleInHead = head.getTitleInHead()==1;
			if(head.getSubTitle()==null&&head.getTitleInHead()==1){//无副标题且标题在表头
				strHead.append("<p id='headTitle' class='>").append(head.getStyle()).append("'>").append(strTitle).append("</p>");
			}else{//有副标题
				SubTitle st = head.getSubTitle();
				strHead.append("<table width='100%' border='0' cellspacing='0' cellpadding='0'><tr><td colspan=3 align='center'>");
				strHead.append("<span id='headTitle' class='").append(head.getStyle()).append("'>").append(strTitle);
				strHead.append("</span></td></tr><tr><td width='33%' align='left><span id='headSLeft' class='").append(st.getlStyle()).append("'>");
				strHead.append(stLeft);
				strHead.append("</span></td><td width='33%' align='center'><span id='headSCenter' class='").append(st.getcStyle()).append("'>");
				strHead.append(stCenter);
				strHead.append("</span></td><td width='33%' align='right'><span id='headSRight' class='>").append(st.getrStyle()).append("'>");
				strHead.append(stRight).append("</span></td></tr></table>");
			}
		}
		if(rpt.getFoot()!=null){
			hasFoot = true;
			Foot foot = rpt.getFoot();
			strFoot.append("<table width='100%' border=0' cellspacing='0' cellpadding='0'><tr height='").append(foot.getHeight());
			strFoot.append("'><td width='33%' align='left' ><span id='footLeft' class='").append(foot.getlStyle()).append("'>");
			strFoot.append(fLeft);
			strFoot.append("</span></td><td width='33%' align='center'><span id='footCenter' class='").append(foot.getcStyle()).append("'>");
			strFoot.append(fCenter);
			strFoot.append("</span></td><td width='33%' align='right'><span id='footRight' class='").append(foot.getrStyle()).append("'>");
			strFoot.append(fRight).append("</span></td></tr></table>");
		}
	}
%>
<html>
<head>
<title><%=strTitle%></title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/GroupHeaderPlugin.css" />
<style type="text/css">
.rptInfo {
    margin:8px;
}
.x-grid3-cell-inner{
    border-right:1px none #eceff6;
}
.rptGrid .x-grid3-row {
	border-bottom : 1px none;
}
</style>
<!-- LIBS -->
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<%if(header.getMaxLevel()<=1){%>
<link href="<%=request.getContextPath()%>/css/LockingGridView.css" rel="stylesheet" type="text/css" />
<%}%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/dfCommon.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<%if(header.getMaxLevel()<=1){%>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/LockingGridView.js"></script>
<%}%>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/GroupHeaderPlugin.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>	
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>	
<script>
/*
 * Ext JS Library 2.0.2
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = 'libs/ext-3.4.0/resources/images/default/s.gif';
var RPTROOT="doQuery.query?doType=getReport";
	var defaultUnit = "<%=rpt.getDefaultUnit()%>";
	var totalFlds=new Array();
	<%if(totalFlds!=null){
		for(int i=0;i<totalFlds.length;i++){%>
			totalFlds.push("<%=totalFlds[i]%>");
	<%}}%>
    Ext.QuickTips.init();
    /***金额单位切换***********************************************/
    var unCbRcd = Ext.data.Record.create([
        {name : 'id',type : 'string'}, 
        {name : 'name',type : 'string'},
        {name : 'renderFun',type : 'string'}
    ]);
    var unProxy=new Ext.data.HttpProxy({
    	url:'getRptData.query?doType=getUnits&rptID=<%=rpt.getId()%>',
    	timeout: 90000
    }); 
    var unStore = new Ext.data.Store({
    	proxy: unProxy, 
    	reader : new Ext.data.JsonReader({
            idProperty : 'id'
        },unCbRcd)
    });
    unStore.on("load",function(){
    	for(var i=0;i<unStore.getCount();i++){
    		var urd = unStore.getAt(i);
    		if(urd.get("id")==defaultUnit){
    			unitsCombo.setValue(urd.data.id);
    			break;
    		}
    	}
    });
    unStore.load();
    var unitsCombo = new Ext.form.ComboBox({ 
    	displayField : 'name',
    	valueField : 'id',
    	typeAhead : true,
    	width: 90,
    	id: 'units',
    	mode : 'local',
    	triggerAction : 'all',
    	emptyText : '选择...',
    	selectOnFocus : true,
    	editable : false,
    	store : unStore,
    	value: "",
    	destroy : Ext.emptyFn,
    	listeners:{
    		select: function(combo, record, index) {
    			var uf = unStore.getAt(index).get("renderFun");
    			var renderer = App.rpt.Renders[uf];
    			var cols = grid.getColumnModel().columns;
    			for(var i=0;i<cols.length;i++){
        			if(cols[i].isMultiUnit&&cols[i].isMultiUnit>0){
    					cols[i].renderer=renderer;
        			}
    			}
    			 grid.view.refresh();
    		}
    	}
    });
    var fForm = new Ext.FormPanel({
        id: 'formatForm',
        frame: true,
        labelAlign:'right',
        width: 340,
        height: 250,
        layout: 'absolute', 
        items: [
    	    {
    	        x: 5,
    	        y: 5,
    	        xtype:'label',   
                text:'导出格式'   
    	    },{
    	        x: 5,
    	        y: 25,
    	        xtype:'radio',   
                boxLabel:'Excel 97-2003(xls)',   
                name:'ftype',   
                id:'formatXls',
                checked: true,
                hideLabel:true
    	    },{
    	        x: 125,
    	        y: 25,
    	        xtype:'radio',   
                boxLabel:'Excel 2007(xlsx)',   
                name:'ftype',   
                id:'formatXlsx', 
                hideLabel:true
    	    },{
    	        x: 5,
    	        y: 70,
    	        xtype:'label',   
                text:'导出范围'   
    	    },{
    	        x: 5,
    	        y: 90,
    	        xtype:'radio',   
                boxLabel:'全部',   
                name:'exportRange',   
                id:'allRds',
                checked: true,
                hideLabel:true
    	    },{
    	        x: 125,
    	        y: 90,
    	        xtype:'radio',   
                boxLabel:'部分',   
                name:'exportRange',   
                id:'partRds',
                hideLabel:true,
                listeners:{
        		    check: function(obj,checked){
        				if(checked){
        					fForm.getForm().findField("start").enable();
        					fForm.getForm().findField("end").enable();
        				}else{
        					fForm.getForm().findField("start").disable();
        					fForm.getForm().findField("end").disable();
        				}
        		    }
        	    }
    	    },{
    	        x: 125,
    	        y: 120,
    	        xtype:'label',   
                text:'第'
    	    },{
    	        x: 140,
    	        y: 115,
    	        xtype:'numberfield',
    	        width: 40, 
    	        disabled : true,  
                name:'start'
    	    },{
    	        x: 185,
    	        y: 120,
    	        xtype:'label', 
                text:'条到'
    	    },{
    	        x: 215,
    	        y: 115,
    	        xtype:'numberfield',
    	        width: 40, 
    	        disabled : true,   
                name:'end'
    	    },{
    	        x: 260,
    	        y: 120,
    	        xtype:'label',   
                text:'条'
    	    }
    	]
    }); 
	var winFormat = new Ext.Window({
	    title: '格式',
	    width: 340,
	    height: 250,
	    layout: 'fit',
	    buttonAlign:'center',
	    items: fForm,
	    buttons: [{
	        text: '确定',
	        handler:function(){
		    	var rangeMode = 0,expStart=0, expEnd=0,eformat = 0;
			    if(Ext.getCmp('partRds').checked){
			    	expStart = fForm.getForm().findField("start").getValue();
					expEnd = fForm.getForm().findField("end").getValue();
					rangeMode = 1;
			    }else{
			    	rangeMode = 0;
			    }
		        if(Ext.getCmp('formatXls').checked){
		        	eformat = 0;
			    }else{
			    	eformat = 1;
			    }
			    var unit = Ext.getCmp("units").getValue();
		        var expUrl='getRptData.query?doType=toExcel&eformat='+eformat+'&rangeMode='+rangeMode+'&expStart='+expStart+'&expEnd='+expEnd+'&unit='+unit;
		        expUrl = expUrl+'<%=strParas.toString()%>';
		        window.open(expUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
		        winFormat.hide(); 
			}
	    },{
	        text: '取消',
	        handler:function(){
		        winFormat.hide(); 
		    }
	    }]
	});
    var cm = <%=header.getMaxLevel()<=1?"new Ext.ux.grid.LockingColumnModel":"new Ext.grid.ColumnModel"%>({
				columns: [
			<%
				int tcount=0;
				for(int i=0;i<header.getSortedNodes().size();i++){
					Column col=(Column)header.getSortedNodes().get(i);
					if(col==null||col.getIsleaf()==0){continue;}%>
			{
				id:'<%=col.getColId()%>',header:"<%=col.getColName()%>",width:<%=col.getWidth()%>,dataIndex:'<%=col.getDataIndex()==null||"".equals(col.getDataIndex())?"":col.getDataIndex()%>',
				align:'<%=StringUtils.isEmpty(col.getAlign())?(col.getDataType()>0?"right":"left"):col.getAlign()%>'
				,tooltip:"<%=col.getColName()%>"
				,hideable :true
				,hidden:<%=col.getDefaultHide()==1?"true":"false"%>
				,isGroup: <%=col.getIsGroup()%>
					<%
					String strV = "";
					boolean fun = false;
					if(!StringUtils.isEmpty(col.getColFunction())){
						strV = col.getColFunction();
						fun = true;
					}
					if(col.getIsMultiUnit()>0&&rpt.getMultiUnit()>0&&rpt.getDefaultUnit()!=""){
						String render = rpt.getDefaultUnit();
						Unit un = muts==null?null:(Unit)muts.get(render);
					%>
				,renderer: <%=fun?"function(v,p,r){App.rpt.Renders['"+un.getRenderFun()+"'].createDelegate("+strV+")}":"App.rpt.Renders['"+un.getRenderFun()+"']"%>
					<%}else if(!"".equals(col.getRenderer())){%>
				,renderer :<%=fun?"function(v,p,r){App.rpt.Renders['"+col.getRenderer()+"'].createDelegate("+strV+")}":"App.rpt.Renders['"+col.getRenderer()+"']"%>
					<%}else if(col.getIsLink()>0){%>
				,renderer :<%=fun?"function(v,p,r){App.rpt.Renders['renderFoo'].createDelegate("+strV+")}":"App.rpt.Renders['renderFoo']"%>
					<%}%>
				,isMultiUnit : '<%=col.getIsMultiUnit()%>'
				,isLink: '<%=col.getIsLink()%>'
				,linkParams: '<%=col.getLinkParams()==null?"":col.getLinkParams()%>'
				,target: '<%=col.getTarget()==null?"":col.getTarget()%>'
				,linkTo: '<%=col.getLinkTo()==null?"":col.getLinkTo()%>'
			}
			  <%
			    	tcount++;
			  		if(tcount<header.getLeafCount()){out.print(",");}
			  	}%>
				],
				defaultSortable: false
				<%//如果复杂表头
				if(header.getMaxLevel()>1){
					//复杂表头写为二维数组，为方便、动态、用list。除叶子列外，每个level构造列头的一行
					List hRows=new ArrayList();
					for(int i=1;i<header.getMaxLevel();i++){
						List row=new ArrayList();
						hRows.add(row);
					}
					//排序后的节点循环。底级列如果跨行，则在“被跨越”的表头行增加列的占位符{}-即空列
					for(int i=0;i<header.getSortedNodes().size();i++){
						Column col=(Column)header.getSortedNodes().get(i);
						int lv=col.getLevel();
						if(col.getIsleaf()>0){
							for(int j=header.getMaxLevel()-1;j>=lv;j--){
								List cRow=(List)hRows.get(j-1);
								cRow.add("{}");
							}
						}else{
							//非底级的节点，增加到其对应的行中。
							List cRow=(List)hRows.get(lv-1);
							String scol="{header: '"+col.getColName()+"', colspan: "+header.getColSpan(i+1,lv)+", align: 'center'}";
							cRow.add(scol);
						}
					}%>
				,rows:[
				  <%for(int i=0;i<hRows.size();i++){
				  		List aRow=(List)hRows.get(i);%>
				  	[
				  		<%for(int j=0;j<aRow.size();j++){
				  			String str=(String)aRow.get(j);
				  			out.print(str);
				  			if(j<aRow.size()-1){out.print(",");}
				  		  }%>
				 	]
				      <%if(i<hRows.size()-1){out.print(",");}%>
				  <%}%>
				]
			  <%}%>
	});

    // 定义一个记录对象
    var sf_Record = Ext.data.Record.create([
    	<%
    	int hn=0;
    	if(header.getHiddenNodes()!=null&&header.getHiddenNodes().size()>0){
    		for(int i=0;i<header.getHiddenNodes().size();i++){
    			Column col=(Column)header.getHiddenNodes().get(i);
        		if(col.getDataIndex()==null||"".equals(col.getDataIndex()))continue;
        		hn++;
    		}
    	}
    	for(int i=0;i<header.getSortedNodes().size();i++){
    		Column col=(Column)header.getSortedNodes().get(i);
    		if(col.getDataIndex()==null||"".equals(col.getDataIndex()))continue;
    		hn++;
    	}
    	int cCount=0;
    	for(int i=0;i<header.getHiddenNodes().size();i++){
    		Column col=(Column)header.getHiddenNodes().get(i);
    		if(col.getDataIndex()==null||"".equals(col.getDataIndex()))continue;
    		cCount++;%>
    	   {name: '<%=col.getDataIndex()%>', 
    	    type: '<%=col.getDataType()==1?"int":(col.getDataType()==2?"float":(col.getDataType()==9?"date":"string"))%>'
    	    <%if(col.getDataType()==9){out.print(",dateFormat:'Y-m-d'");}%> }
    	    <%if(cCount<hn){out.print(",");}%>
    	<%}
    	for(int i=0;i<header.getSortedNodes().size();i++){
    		Column col=(Column)header.getSortedNodes().get(i);
    		if(col.getDataIndex()==null||"".equals(col.getDataIndex()))continue;
    		cCount++;%>
    	   {name: '<%=col.getDataIndex()%>', 
    	    type: '<%=col.getDataType()==1?"int":(col.getDataType()==2?"float":(col.getDataType()==9?"date":"string"))%>'
    	    <%if(col.getDataType()==9){out.print(",dateFormat:'Y-m-d'");}%> }
    	    <%if(cCount<hn){out.print(",");}%>
      <%}%>
    ]);
    var mProxy=new Ext.data.HttpProxy({
    	url:'getRptData.query?doType=getQueryData<%=strParas.toString()%>',
    	timeout:90000
    }); 
    // 创建 Data Store
    var store = new Ext.data.Store({
    	proxy: mProxy, 
        reader: new Ext.data.JsonReader({
        	 <%if(rpt.getDefaultDataDef()!=null&&rpt.getDefaultDataDef().getCanPaging()==1){%>
               root: 'rptData',
               totalProperty: 'totalCount'
             <%}%>
           }, sf_Record)
    });
    // 创建编辑器表格
    var grid = new Ext.grid.GridPanel({
        cls : 'rptGrid',
        store: store,
        cm: cm,
        frame:false,
        <%if(hasHead||hasFoot){%>
    	region: 'center',
    	<%}%>
    	title: "<%=titleInHead?"":strTitle%>",
        enableColumnMove :true,
        view: <%=header.getMaxLevel()<=1?"new Ext.ux.grid.LockingGridView()":"new Ext.grid.GridView()"%>,
		stripeRows: true,
        loadMask: {msg:'正在加载数据，请稍侯……'},
        collapsible: false,
        <%if(header.getMaxLevel()>1){%>
		plugins: [new Ext.ux.plugins.GroupHeaderGrid()],
		<%}%>
		//顶部工具栏按钮
        tbar: [
        {
			text: '帮助',
	        iconCls: 'help',
	        handler : function(){
	        	remarkWin.show();
			}
		},{
	        text: '导出',
			iconCls: 'expExcel',
	        handler : function(){
	        	winFormat.show();
	        }
        }
		<%if(rpt.getHasChart()>0){%>
        ,{
            text: '图表',
			iconCls: 'chart',
            handler : function(){
            	var cUrl='<%=request.getContextPath()%>/queryReport/chart.jsp?rptID=<%=rpt.getId()%>';
            	window.open(cUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
        	}
        }
        <%}%>
        /*,{
            text: '导出到PDF',
			iconCls: 'expExcel',
            handler : function(){
            	var expUrl='getRptData.query?doType=toPdf<%=strParas.toString()%>';
            	window.open(expUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
            }
    	}*/
    	<%if(rpt.getMultiUnit()>0){%>
    	,new Ext.Toolbar.Separator(),{
			xtype: "label",
	    	text: "金额单位:"
	    },unitsCombo
    	<%}%>]
    <%//有分页时
      if(rpt.getDefaultDataDef()!=null&&rpt.getDefaultDataDef().getCanPaging()==1){%>
		,bbar: new Ext.PagingToolbar({
            pageSize: <%=((ParaValue)mPavalues.get("pageSize")).getValue()%>,
	        store: store,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
    <%}%>  
    });
    //store.on("load",function(){gridSpan(grid,"row");});  
    var paramForm = new Ext.FormPanel({
    	layout : 'form',
    	id : 'paramForm',
    	autoScroll: true,
    	frame: true,
    	border : false,
    	bodyStyle:'padding:2px',
    	labelWidth :90,
    	labelAlign:'right',
    	items : [ 
    	]
    });
    var tabPanel = new Ext.TabPanel({  
		id:'tabPanel',  
		region:'center',   
		activeTab:0,  
		enableTabScroll:true,
		layoutOnTabChange:true,
		items:[
		{
			id: 'rptInfo',
			layout:'fit',
            title: '口径说明',
            closable: false,
            html: "<p></p>" 
         },{
            id: 'paramsInfo', 
            layout:'fit',
            title: '报表参数',
            closable: false, 
            items: paramForm 
        }]
	});   
    var remarkWin = new Ext.Window({
    	width : 400,
    	height : 300,
    	title : "帮助",
    	layout : 'fit',
    	closeAction :"hide",
    	autoScroll: true,
    	items : [tabPanel],
    	buttons : [
    	{
    		text : "关闭",
    		handler : function() {
    			remarkWin.hide();
    		}
    	}],
    	buttonAlign : "center"
    });
    remarkWin.on("show",function(){
    	Ext.Ajax.request({
			url : 'rpt.query?doType=getReportRemark',
			params : {rptID: '<%=rpt.getId()%>'},
			success : function(response, options) {
				if(response.responseText!=null&&response.responseText!=""){
					var result = Ext.util.JSON.decode(response.responseText);
					if(result){
						var desc = result.desc;
						Ext.getCmp('rptInfo').body.update("<div><p class=rptInfo>"+desc+"</p></div>");
						var vals = result.paramVals;
						paramForm.removeAll();
						for(var i=0;i<vals.length;i++){
							var val = vals[i];
							var _fld = new Ext.form.TextField({
                               width: 200,
                               name: val.pname,
                               fieldLabel: val.ptext,
                               readOnly:true,
						       style:'background:none;border:1px;',
                               value: val.pvalue
                            });
							paramForm.add(_fld);
						}
			        	paramForm.ownerCt.doLayout(); 
					}
				}
			},
			failure : function(response,option) {
				Ext.Msg.alert("失败","加载报表信息失败！");		
            }
		});
    });
    var head = new Ext.Panel({
        region: 'north',
        height: <%=rpt.getHead()==null?50:rpt.getHead().getHeight()%>, 
        frame : true,
        html: "<%=strHead%>"
    });
    var foot = new Ext.Panel({
        region: 'south',
        height: <%=rpt.getFoot()==null?50:rpt.getFoot().getHeight()%>, 
    	frame : true,
    	html: "<%=strFoot%>"
    });
Ext.onReady(function(){
	Ext.QuickTips.init();
	Ext.Ajax.timeout = 600000;  
	new Ext.Viewport({
		<%if(!hasHead&&!hasFoot){%>
		layout:'fit',
        items:[grid]
        <%}else{%>
        layout: 'border',
        items:[
        	<%if(hasHead){%>
        	head,
        	<%}
			if(hasFoot){%>
        	foot,
        	<%}%>
			grid
		]
        <%}%>
	});
	<%
    StringBuffer sPaging=new StringBuffer("");
    //有分页时的额外参数
    if(rpt.getDefaultDataDef()!=null&&rpt.getDefaultDataDef().getCanPaging()==1){
    	sPaging.append("{params:{start:0, limit:");
    	sPaging.append(((ParaValue)mPavalues.get("pageSize")).getValue());
    	sPaging.append("}}");
    }
    %>
    store.load(<%=sPaging.toString()%>); 
});
function gridSpan(grid,rowOrCol){  
	var groupStarts = new Array();
	var rc = grid.getStore().getCount();
	var cc = grid.getColumnModel().getColumnCount();
	var gstart = 0;
	var preValue = undefined;  
    var ng = rc-1,minGIndex = rc-1; 
    var spCells = [];
	for(var i=0;i<rc;i=ng){
		minGIndex = rc-1;
		for(var j=0;j<cc;j++){
			var col= grid.getColumnModel().columns[j];
			if(col.isGroup==0){
				continue;
			}
			var colName = grid.getColumnModel().getDataIndex(j);
			preValue = grid.getStore().getAt(i).get(colName);
			for(var k = i+1;k<rc;k++){
            	var cv = grid.getStore().getAt(k).get(colName);
				if(cv!=preValue){
					if(k<=minGIndex){
						minGIndex = k;
					}
					break;
				}else if(k==rc-1){
					minGIndex = rc;
					break;
				}
			}
		}
		groupStarts.push(minGIndex);
		ng=minGIndex;
	}
	var preg = 0;
	for(var i=0;i<groupStarts.length;i++){
		var ng = groupStarts[i];
		for(var j=0;j<cc;j++){
			var col= grid.getColumnModel().columns[j];
			if(!spCells[j]){
				spCells[j] = [];
			}
			if(col.isGroup==0){
				continue;
			}
			var colName = grid.getColumnModel().getDataIndex(j); 
	        preValue = grid.getStore().getAt(preg).get(colName); 
			for(var k=preg;k<ng;k++){
				var cell = grid.getView().getCell(k,j);
		        
		        grid.getStore().getAt(k).set(colName, "&nbsp;"); 
				if(k!=preg){
		        	spCells[j].push(k);
				}
			}
			var gindex = preg + Math.round((ng - preg) / 2 - 1);  
			grid.getStore().getAt(gindex).set(colName, preValue); 
		}
		preg = ng;
	}
	grid.getStore().commitChanges(); 
	//添加所有分隔线  
    for(i = 0; i < rc; i ++){  
        for(j = 0; j < grid.getColumnModel().getColumnCount(); j ++){
            var aRow = grid.getView().getCell(i,j);  
            if(i == 0){  
                aRow.style.borderTop = "none";  
            }else if(i == rc - 1){  
                aRow.style.borderTop = "1px solid #eceff6";  
                aRow.style.borderBottom = "1px solid #ccc";  
            }else{  
                aRow.style.borderTop = "1px solid #eceff6";  
            }  
            if(j == grid.getColumnModel().getColumnCount()-1)  
                aRow.style.borderRight = "1px solid #eceff6";  
            if(i == rc-1)       
            	aRow.style.borderBottom = "1px solid #eceff6";  
        }
    }
    //去除合并的单元格的分隔线  
    for(i = 0; i < spCells.length; i++){  
        if(!Ext.isEmpty(spCells[i])){  
            for(j = 0; j < spCells[i].length; j++){  
                aRow = grid.getView().getCell(spCells[i][j], i);  
                aRow.style.borderTop = "none";  
            }
       	}  
    }  
	
	
}
function gridSpan1(grid, rowOrCol, cols, sepCol){  
    var array1 = new Array();  
    var arraySep = new Array();  
    var count1 = 0;  
    var count2 = 0;  
    var index1 = 0;  
    var index2 = 0;  
    var aRow = undefined;  
    var preValue = undefined;  
    var firstSameCell = 0;  
    var allRecs = grid.getStore().getRange();
    var minRowSpan = 1;  
    if(rowOrCol == "row"){  
        count1 = grid.getColumnModel().getColumnCount();  
        count2 = grid.getStore().getCount();  
    } else {  
        count1 = grid.getStore().getCount();  
        count2 = grid.getColumnModel().getColumnCount();  
    }  
    for(i = 0; i < count1; i++){ 
        if(rowOrCol == "row"){ 
            var col= grid.getColumnModel().columns[i];
    		if(col.isGroup==0){
    			continue;
    		}
        }  
        preValue = undefined;  
        firstSameCell = 0;  
        array1[i] = new Array();  
        for(j = 0; j < count2; j++){  
            if(rowOrCol == "row"){  
                index1 = j;  
                index2 = i;  
            } else {  
                index1 = i;  
                index2 = j;  
            }  
            var colName = grid.getColumnModel().getDataIndex(index2);  
            if(sepCol && colName == sepCol)  
            	arraySep[index1] = allRecs[index1].get(sepCol);  
            var seqOldValue = seqCurValue = "1";  
            if(sepCol && index1 > 0){  
                seqOldValue = arraySep[index1 - 1];  
                seqCurValue = arraySep[index1];  
            }  
               
            if(allRecs[index1].get(colName) == preValue && (colName == sepCol || seqOldValue == seqCurValue)){  
                allRecs[index1].set(colName, "");  
                array1[i].push(j);  
                if(j == count2 - 1){  
                    var index = firstSameCell + Math.round((j + 1 - firstSameCell) / 2 - 1);  
                    if(rowOrCol == "row"){  
                        allRecs[index].set(colName, preValue);  
                    } else {  
                        allRecs[index1].set(grid.getColumnModel().getColumnId(index), preValue);  
                    }  
                }  
            }else {  
                if(j != 0){  
                    var index = firstSameCell + Math.round((j + 1 - firstSameCell) / 2 - 1);  
                    if(rowOrCol == "row"){  
                        allRecs[index].set(colName, preValue);  
                    } else {  
                        allRecs[index1].set(grid.getColumnModel().getColumnId(index), preValue);  
                	}  
            	}  
	            firstSameCell = j;  
	            preValue = allRecs[index1].get(colName);  
	            allRecs[index1].set(colName, "");  
	            if(j == count2 - 1){  
	                ballRecs[index1].set(colName, preValue);  
	            }  
        	} 
        }  
    }
    grid.getStore().commitChanges();  
    //添加所有分隔线  
    var rCount = grid.getStore().getCount();  
    for(i = 0; i < rCount; i ++){  
        for(j = 0; j < grid.getColumnModel().getColumnCount(); j ++){
            aRow = grid.getView().getCell(i,j);  
            if(i == 0){  
                aRow.style.borderTop = "none";  
                //aRow.style.borderLeft = "1px solid #ccc";  
            }else if(i == rCount - 1){  
                aRow.style.borderTop = "1px solid #eceff6";  
                //aRow.style.borderLeft = "1px solid #ccc";  
                aRow.style.borderBottom = "1px solid #ccc";  
            }else{  
                aRow.style.borderTop = "1px solid #eceff6";  
                //aRow.style.borderLeft = "1px solid #ccc";  
            }  
            if(j == grid.getColumnModel().getColumnCount()-1)  
                aRow.style.borderRight = "1px solid #eceff6";  
            if(i == rCount-1)       
            	aRow.style.borderBottom = "1px solid #eceff6";  
        }
    }
    //去除合并的单元格的分隔线  
    for(i = 0; i < array1.length; i++){  
        if(!Ext.isEmpty(array1[i])){  
            for(j = 0; j < array1[i].length; j++){  
            	if(rowOrCol == "row"){  
                    aRow = grid.getView().getCell(array1[i][j],i);  
                    aRow.style.borderTop = "none";
                } else {  
                    aRow = grid.getView().getCell(i, array1[i][j]);  
                    aRow.style.borderLeft = "none";  
                }
            }  
       	}  
    }  
}  
var REPORTGRID = grid;
</script>
</head>
<body>
</body>
</html>