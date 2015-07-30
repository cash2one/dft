<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.fruit.query.data.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.service.*"%>
<%@ page import="com.fruit.query.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.fruit.query.parser.TemplatesLoader"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration) ContextUtil.getBean("config");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>Portal</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<link href="<%=request.getContextPath()%>/css/Portal.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/LockingGridView.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/queryReport/charts/FusionCharts.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/portal/Portal.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/portal/PortalColumn.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/portal/Portlet.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/DynamicMultiGrid.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ParamTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/query.js"></script>
<script type="text/javascript">
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.query.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.query.REMOTING_API);
App.ux.defaultPageSize=<%=cg.getString("pageSize","40")%>;
var GRIDS = new Array();
var CHARTS = new Array();
var conditions ={};
var commonCbRcd = Ext.data.Record.create([
    {name : 'bm',type : 'string'}, 
    {name : 'name',type : 'string'}
]);
var cbStore = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
	    directFn : PortalHandler.getOptionItems,
	    paramOrder: ['rptID','pName','affectedBy'],
	    paramsAsHash: false
	}),
	reader : new Ext.data.JsonReader({
		idProperty : 'bm'
	}, commonCbRcd)
});

//工具条中的triggerField的触发函数
var qpTreeSingleWin;
var qpTreeMultiWin;
var qpTreeCascWin;
var qpTreeWin;
function showQparamTree(rptID,cQueryParam,cMulti,cOnlyLeaf){
	if(cMulti==1){
		if(!qpTreeMultiWin){
			qpTreeMultiWin = new App.widget.ParamTreeWindow({
				directFn: PortalHandler.getOptionItemsOfTree,
				checkModel : 'multiple',
				treeId: 'm_'+ cQueryParam,
				rptID: rptID,
				onlyLeafCheckable: cOnlyLeaf,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeMultiWin;
	}else if(cMulti==2){
		if(!qpTreeCascWin){
			qpTreeCascWin = new App.widget.ParamTreeWindow({
				directFn: PortalHandler.getOptionItemsOfTree,
				checkModel : 'cascade',
				treeId: 'c_'+ cQueryParam,
				rptID: rptID,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeCascWin;
	}else{
		if(!qpTreeSingleWin){
			qpTreeSingleWin = new App.widget.ParamTreeWindow({
				directFn: PortalHandler.getOptionItemsOfTree,
				checkModel : 'single',
				treeId: 's_'+ cQueryParam,
				rptID: rptID,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeSingleWin;
	}
	var cmpPara = Ext.getCmp("q_"+cQueryParam);
	var tmpPost={},mps = {};
	if(cmpPara){
		var aBy = cmpPara.affectedBy;
		if(aBy){
			var aparas = aBy.split(",");
			mps = new Object();
			for(var i = 0;i<aparas.length;i++){
				var tp = aparas[i];
				var tcmp = Ext.getCmp("q_h_"+tp);
				if(tcmp){
					var val =tcmp.getValue();
					mps[aparas]=val;
				}
			}
			tmpPost.macroParams = mps;
		}
	}
	var p = {rptID: rptID,pName: cQueryParam,affectedBy: Ext.encode(tmpPost)};
	qpTreeWin.onSelect = function(value){
		if(!value)return;
		Ext.getCmp("q_h_"+cQueryParam).setValue(value.id); 
		Ext.getCmp("q_"+cQueryParam).setValue(value.text); 
	};
	qpTreeWin.setTreeParams(p);
	qpTreeWin.refreshTree();
	qpTreeWin.show();
};
function buildCondition(gridID){
	var dParams = new Object();
	var cdts = new Object();
	var fldNames = new Array();
	var fldValues = new Array();
	var relations = new Array();
	var connections = new Array();
	var grid = Ext.getCmp(gridID);
	//筛选。来自工具条
	var tbItems = grid.getTopToolbar().items;
	for(var i = 0;i<tbItems.length;i++){
		var it = tbItems.item(i);
		if(!it.filterFld||it.filterFld==""){
			continue;
		}
		var val ="";
		if(it.xtype=="textfield"||it.xtype=="datefield"){
			val =it.getValue();
		}else if(it.xtype=="trigger"||it.xtype=="combo"){
			val =Ext.getCmp("q_h_"+it.id.substring(2)).getValue();
		}else{
			continue;
		}
		if(!val||val==""){
			continue;
		}
		fldNames.push(it.filterFld);
		fldValues.push(val);
		relations.push(it.vop?it.vop:"equ");
		connections.push("_and");
	}
	if(connections.length>0){
		connections[connections.length-1]="empty"; 
	}
	cdts.fldNames=fldNames.join();
	cdts.fldValues=fldValues.join();
	cdts.relations=relations.join();
	cdts.connections=connections.join();
	dParams.filter = cdts;
	//工具条中，用于宏替换的
	var mps = new Object();
	for(var i = 0;i<tbItems.length;i++){
		var it = tbItems.item(i);
		if(it.filterFld&&it.filterFld!=""){
			continue;
		}
		var val ="";
		if(it.xtype=="textfield"||it.xtype=="datefield"){
			val =it.getValue();
		}else if(it.xtype=="trigger"||it.xtype=="combo"){
			val =Ext.getCmp("q_h_"+it.id.substring(2)).getValue();
		}else{
			continue;
		}
		mps[it.id.substring(2)]=val;
	}
	dParams.macroParams = mps;
	conditions[gridID]=dParams;
	grid.getStore().load({params:{rptID: gridID,start:0, limit:<%=cg.getString("pageSize","40")%>}});
}
Ext.onReady(function(){
	var viewport = new Ext.Viewport({
		layout:'fit',
	    items:[{
	    	id :"THE_PORTAL",
	    	xtype:'portal',
	        margins:'35 5 5 0',
	    	items:[{}/*{
	        	items:[{"id":"text_00","title":"面板1","height":300,"layout":"fit","html":"just a minute!","ptype":"text"},
	        	        {"id":"report_01","title":"面板2","height":200,"layout":"fit","html":"qydjcx","ptype":"report"}],
	        	columnWidth:0.33
	        },{
	            "items":[{"id":"chart_10","title":"面板3","height":200,"layout":"fit","html":"dj","ptype":"chart"},
	                     {"id":"report_11","title":"面板4","height":400,"layout":"fit","html":"dj","ptype":"report"}],
	            columnWidth:0.33
	        },{
	            "items":[{"id":"text_20","title":"面板5","height":100,"layout":"fit","html":"last one","ptype":"text"}],
	            columnWidth:0.33
	        }*/
	        ]
		}]
	});
	Ext.Ajax.request({
		url : 'rpt.query?doType=loadPortlets',
		params : {portalID: 'test'},
		success : function(response, options) {
			if(response.responseText!=null&&response.responseText!=""){
				var obj = Ext.util.JSON.decode(response.responseText);
				if(obj.result){
					var cols = obj.columns;
					if(cols!=null&&cols.length>0){
						for(var i=0;i<cols.length;i++){
							var col = cols[i];
							var pts = col.items;
							if(pts!=null){
								for(var j=0;j<pts.length;j++){
									var pt = pts[j];
									//根据类型构建grid或者chart，放入portlet中
									if(pt.ptype=="report"){
										pt.items = createGrid(pt.items,pt.loadInPortal);
									}else if(pt.ptype=="chart"){
										pt.items = createChart(pt.items,pt.id,pt.loadInPortal);
									}
								}
							}
							Ext.getCmp("THE_PORTAL").add(col);
						}
					}
					Ext.getCmp("THE_PORTAL").doLayout();
				}else{
					Ext.Msg.alert("失败",""+obj.info);	
				}
			}
		},
		failure : function(response,option) {
			Ext.Msg.alert("失败","加载Portal信息失败！");		
        }
	});
});

function createGrid(id,loadInPortal){
	var grid =GRIDS[id];
	if(grid==null){
		 var grid = new App.ux.DynamicGridPanelMulti({
			id: id,
			columns : [],
			store : new Ext.data.DirectStore({
				directFn : PortalHandler.queryGeneralDataDynamic,
				remoteSort: true,
				paramsAsHash : false,
				paramOrder: ['rptID','start','limit','condition'],
				fields : []
			}),
			tbar: []
		});
		grid.getStore().on("beforeload",function(ds,op){
			ds.baseParams.rptID = id;
			var tmpCdts = Ext.apply({},conditions[id]);
			tmpCdts.metaDataLoaded = grid.metaDataLoaded;
			Ext.copyTo(tmpCdts,op.params,'sort,dir');
			delete op.params.sort;
			delete op.params.dir;
			op.params.condition = Ext.encode(tmpCdts);
		});	
		grid.getStore().load({params:{rptID:id,start:0, limit:App.ux.defaultPageSize,condition:''}});
		GRIDS[id]=grid;
	}
	return grid;
}
function createChart(id,panelID,loadInPortal){
	var chart = CHARTS[id];
	if(chart==null){
		Ext.Ajax.request({
			url : 'rpt.query?doType=getChartInfo2Create',
			params : {id: id},
			success : function(response, options) {
				if(response.responseText!=null&&response.responseText!=""){
					var obj = Ext.util.JSON.decode(response.responseText);
					if(obj.result){
						var ci = obj.chartInfo;
						alert(ci.swf);
						chart = new FusionCharts("charts/"+ci.swf,ci.cid,ci.width,ci.height);
						chart.setDataURL(ci.dataUrl);
						chart.render(panelID);
					}else{
						chart=null;
					}
				}
			}
		});
		CHARTS[id]=chart;
	}
	return chart;
}
</script>
</head>
<body>
<iframe name='ifmExport' src='' frameborder=0  marginwidth=0></iframe>
</body>
</html>