<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.utils.entity.*"%>
<%@ page import="com.ifugle.dft.check.entity.*"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	String today = sdf.format(new Date());
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>DNFT</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/CodeTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript">

/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.ck.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.ck.REMOTING_API);
var cSwdjzh = "";
var pMc = "";
var conditions = {};
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "地址",
	        dataIndex: 'dz',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "企业属性",
	        dataIndex: 'qysx',
	        width: 100,
	        align: 'left',
	        renderer: function(v,p,r){
	        	if(r.get("qysx") == 3){
		        	return "虚拟企业";
	        	}else{
		        	return "";
	        	}
	    	}
		}
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'xh', type: 'int'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'dz', type: 'string'},
	{name: 'qysx', type: 'int'},
	{name: 'czfpbm_bm', type: 'string'},
	{name: 'czfpbm', type: 'string'}
]);
var enDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: EnHandler.getEns,
		paramOrder: ['start','limit','qymc'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
enDs.on("beforeload",function(){
	var value = Ext.getCmp('paras').getValue();
	enDs.baseParams.qymc = value;
});
var view = new Ext.grid.GridView({});
var enGrid = new Ext.grid.GridPanel({
		title:'收入凭证',
		store: enDs,
		height:250,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		selModel: ssm,
		stripeRows: true,
		tbar: [{
			xtype:'label',
			text:'企业名称：'
		},{
			xtype:'textfield',
			id:'paras',
			width:120,
			enableKeyEvent:true,
			name:'paras',
			hideLabel:true
			,listeners:{   
				specialkey:function(field,e){   
					if (e.getKey()==Ext.EventObject.ENTER){  
						enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
					}   
				}   
			}   
		},{
            text: '搜索',
            iconCls: 'filter',
            handler : function(){
				enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
            }
		},new Ext.Toolbar.Separator(),
		{
			text: '添加凭证',
			iconCls: 'addPz',
            handler : function(){
				var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要添加凭证的企业!");
					return;
				}
		        cSwdjzh = records[0].get("swdjzh");
				pzWin.show();
			}
		},{
			text: '凭证信息',
			iconCls: 'pzInfo',
            handler : function(){
				var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择企业!");
					return;
				}
		        cSwdjzh = records[0].get("swdjzh");
				viewWin.show();
			}
		}
		],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: enDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});

var treeWin;
function showTreeWin(table,cVal,callback){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn: CheckHandler.getBmCodesTree,
			onlyLeafCheckable: true,
			codeTable: table,
			defaultValue: cVal
		});
	}
	var p = {table: table,selectedVals: cVal};
	treeWin.onSelect = callback;
	treeWin.setTreeParams( p);
	treeWin.refreshTree();
	treeWin.show();
}
/*******************************凭证操作******************************************/
var cPzRecord ;
function setPzDetailBm(tField,hField){
	return function(value){
		cPzRecord.set(hField,value.id);
		cPzRecord.set(tField,value.text);
	}
};

var yskm_tg = new Ext.form.TriggerField({
	width:120,
	editable: false,
    id :'yskm_tg',
    name: 'yskm_tg',
    value:''
});
yskm_tg.onTriggerClick=yskmFun;
function yskmFun(e){
	var cVal = cPzRecord.get("yskmbm");
	showTreeWin("BM_YSKM",cVal,setPzDetailBm("yskm","yskmbm"));
}
var ysjc_tg = new Ext.form.TriggerField({
	width:100,
	editable: false,
    id :'ysjc_tg',
    name: 'ysjc_tg',
    value:''
});
ysjc_tg.onTriggerClick=ysjcFun;
function ysjcFun(e){
	var cVal = cPzRecord.get("ysjcbm");
	showTreeWin("BM_YSJC",cVal,setPzDetailBm("ysjc","ysjcbm"));
}
var sz_tg = new Ext.form.TriggerField({
	width:90,
	editable: false,
    id :'sz_tg',
    name: 'sz_tg',
    value:''
});
sz_tg.onTriggerClick=szFun;
function szFun(e){
	var cVal = cPzRecord.get("szbm");
	showTreeWin("BM_SZ",cVal,setPzDetailBm("szmc","szbm"));
}
var czfp_tg_pz = new Ext.form.TriggerField({
	width:90,
	editable: false,
    id :'czfp_tg_pz',
    name: 'czfp_tg_pz',
    value:''
});
czfp_tg_pz.onTriggerClick=czfpPzFun;
function czfpPzFun(e){
	var cVal = cPzRecord.get("fpbm");
	showTreeWin("BM_CZFP",cVal,setPzDetailBm("fpmc","fpbm"));
}
var pzForm= new Ext.FormPanel({
    id: 'pzForm',
    border:false,
    frame:true,
    labelWidth :60,
    labelAlign: 'right',
	layout:'form',
    items: [{
       	layout:'column',
       	frame:false,
        border:false,
       	items:[
       	{
           	layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			items:[
				new Ext.form.Hidden({ 
	 				fieldLabel: '税号',
	 				width:150,
	 				readOnly: true,
	 				name: 'swdjzh'
	 			}),new Ext.form.TextField({ 
   					fieldLabel: '企业名称',
   					width:150,
   					allowBlank:true,
   					readOnly: true,
   					style:'background:none;border:0px;',
   					name: 'mc'
   				}),new Ext.form.DateField({ 
					fieldLabel: '入库日期',
					width:150,
					allowBlank: true,
					value: '<%=today%>' ,
					format: 'Y-m-d',
					name: 'rkrq'
				})
			]
        },
        {
            layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			items:[
			    new Ext.form.TextField({ 
					fieldLabel: '经济性质',
					width:150,
					allowBlank: true,
					readOnly: true,
					style:'background:none;border:0px;',
					name: 'jjxz'
				}),new Ext.form.TextField({ 
    	 			fieldLabel: '行&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;业',
    	 			width:150,
    	 			allowBlank:true,
    	 			readOnly: true,
    	 			style:'background:none;border:0px;',
    	 			name: 'hy'
    	 		}),new Ext.form.Hidden({ 
    	 			name: 'sph',
    	 			value:""
    	 		})
			]
        }]
    },new Ext.form.TextArea({ 
			fieldLabel: '备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注',
				width:400,
				height:70,
				allowBlank: true,
				name: 'remark'
			})
    ]
});
var checkmodel = new Ext.grid.CheckboxSelectionModel();
checkmodel.handleMouseDown = Ext.emptyFn;
var pz_cm = new Ext.grid.ColumnModel({
	columns: [
		checkmodel,
	    {
	       header: "财政分片",
	       dataIndex: 'fpmc',
	       width: 90,
	       align:'left',
	       editor: czfp_tg_pz 
	    },
	    {
	       header: "税种",
	       dataIndex: 'szmc',
	       width: 90,
	       align: 'left',
	       editor: sz_tg
	    },
	    {
	        header: "预算级次",
	        dataIndex: 'ysjc',
	        width: 100,
	        align: 'left',
	        editor: ysjc_tg
	    },{
		    header: "预算科目",
		    dataIndex: 'yskm',
		    width: 120,
		    align: 'left',
		    editor: yskm_tg
		},{
		    header: "金额",
		    dataIndex: 'je',
		    width: 90,
		    align: 'right',
		    editor: new Ext.form.NumberField({selectOnFocus:true,maxLength:16}),
			renderer :function(v,p,r){
				return regMoney(v,p,r);
			}
		}
	],
	defaultSortable: false
});
var pz_Record = Ext.data.Record.create([   
       {name: 'xh', type: 'string'},
       {name: 'fpbm', type: 'string'},
       {name: 'fpmc', type: 'string'},
       {name: 'szbm', type: 'string'},
       {name: 'szmc', type: 'string'},
       {name: 'ysjcbm', type: 'string'},
       {name: 'ysjc', type: 'string'},
       {name: 'yskmbm', type: 'string'},     
       {name: 'yskm',type:'string'},
       {name: 'je',type:'float'}
 ]);
var pz_grid_ds = new Ext.data.Store({ 
	proxy: new Ext.data.DirectProxy({
		directFn: EnHandler.getNewPzDetail,
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({},pz_Record)
});
var pzGrid = new Ext.grid.EditorGridPanel({
	title:'凭证明细',
	store: pz_grid_ds,
    cm: pz_cm,
    frame:false,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    clicksToEdit:1,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
    selModel: checkmodel,
    tbar: [
    {
	    text: '增加行',
	    id:'addBtn',
	    iconCls: 'add',
	    handler : function(){
			var pz = new pz_Record({
	        	fpbm:'',
	        	fpmc:'',
	            szbm:'',
	            szmc:"",
	            ysjc:"",
	            ysjcbm:"",
	            yskmbm:"",
	            yskm:"",
	            je: 0
	        });
			var cc = pz_grid_ds.getCount();
			if(cc>0){
				var fr = pz_grid_ds.getAt(0);
				pz.set('fpbm',fr.data.fpbm);
				pz.set('fpmc',fr.data.fpmc);
				pz.set('szbm',fr.data.szbm);
				pz.set('szmc',fr.data.szmc);
				pz.set('ysjc',fr.data.ysjc);
				pz.set('ysjcbm',fr.data.ysjcbm);
				pz.set('yskmbm',fr.data.yskmbm);
				pz.set('yskm',fr.data.yskm);
				pz.set('je',fr.data.je);
			}
	        pzGrid.stopEditing();
	        pzGrid.getStore().insert(cc, pz);
	        pzGrid.startEditing(cc, 0);
	        pzGrid.getStore().commitChanges();
	    }
	},{
	    text: '删除行',
	    id:'removeBtn',
	    iconCls: 'remove',
		handler :function(){
	        var records = pzGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的行!");
				return;
			}	
			if(records){
				Ext.MessageBox.confirm('确认删除', '你真的要删除所选行吗?', 
			    	function(btn){
					    if(btn == 'yes') {// 选中了是按钮							    	    										 
							for(var rc=0;rc<records.length;rc++){						    	    	
								pz_grid_ds.getModifiedRecords().remove(records[rc]);
								pz_grid_ds.remove(records[rc]);
							}
						}
					}
				);
			}
	    }
	},{
		text: '导入',
	    iconCls: 'impExcel',
		handler :function(){
			excelWin.show();
		}
	},{
		text: '保存',
	    iconCls: 'save',
		handler :function(){
			saveDetails();
		}
	}
    ]
});
	pzGrid.on('beforeedit',function(e){ 
		cPzRecord = e.record;
	    return;   
	}); 
	
	var pzWinConfig = {
		title : '录入凭证',
		width : 600,
		height : 480,
		autoScroll : true,
		maximizable: false,
		layout : 'border',
		items:[{	
		    id:'north',
		    region:'north',
		    height:180,
		    frame:true,
		    layout:'fit',
		   	title:"",
		   	items: pzForm
		 },{  
		   	region:'center',  
		   	layout:'fit',  
		   	margins:'0 0 0 0',  
		   	items: pzGrid  
		}],
		closeAction:'hide',
		modal:true,
		buttons : [{
		    text : "关闭",
		    handler : function(){
		    	pzWin.hide();	
		    }
		}
		]
	};	
	var pzWin = new Ext.Window(pzWinConfig);
	
	function saveDetails(){
		var cc = pz_grid_ds.getCount();
		if(cc==0){
			Ext.Msg.alert('信息','请录入凭证的明细信息！');
			return;
		}
		for(var i=0;i<cc;i++){
			var rc = pz_grid_ds.getAt(i);
			if(rc.get("fpbm")==null||rc.get("fpbm")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的分片为空，请录入！");
				return;
			}
			if(rc.get("szbm")==null||rc.get("szbm")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的税种为空，请录入！");
				return;
			}
			if(rc.get("ysjcbm")==null||rc.get("ysjcbm")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的预算级次为空，请录入！");
				return;
			}
			if(rc.get("yskmbm")==null||rc.get("yskmbm")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的预算科目为空，请录入！");
				return;
			}
		}
		Ext.MessageBox.confirm('确认保存', '你真的要保存录入的凭证吗?', function(btn){
			if(btn == 'yes') {// 选中了是按钮
			    var swdjzh = pzForm.getForm().findField("swdjzh").getValue();
			    //初始为空，保存后将生成的税票号回写。之后的重复保存都按税票号先删后增。
			    var sph = pzForm.getForm().findField("sph").getValue();
			    var remark = pzForm.getForm().findField("remark").getValue();
			    var rkrq = pzForm.getForm().findField("rkrq").getValue();
			    var strRkrq =rkrq.format("Ymd");
			    var rows=new Array();
			    pz_grid_ds.each(function(rs){ 		
		   			var row=new Object();
		   			row.fpbm=rs.get("fpbm");
		   			row.szbm=rs.get("szbm");
		   			row.ysjcbm=rs.get("ysjcbm");
		   			row.yskmbm = rs.get("yskmbm");
		   			row.je = rs.get("je");
		   	    	rows.push(row);
		   	    });  	
			    var pzs = Ext.util.JSON.encode(rows);
			    EnHandler.savePzDetail(swdjzh,sph,strRkrq,remark,pzs,function(data){
					var obj = Ext.decode(data);
					if(!obj)return;
					if(obj.result){
						var vsph = obj.sph;
						pzForm.getForm().findField("sph").setValue(vsph);
						Ext.Msg.alert("提示","凭证保存成功!");
						pz_grid_ds.commitChanges();
					}else{
						Ext.Msg.alert("提示",obj.info);
					}
				});
			}
		});	
	}
	pzWin.on("show",function(){
		EnHandler.getVirtualEn(cSwdjzh,function(data){
			var obj = Ext.decode(data);
			if(obj&&obj.result){
				var en = obj.en;
				if(en){
					pzForm.getForm().findField('swdjzh').setValue(en.SWDJZH);
					pzForm.getForm().findField('mc').setValue(en.MC==null?"未知":en.MC);
					pzForm.getForm().findField('jjxz').setValue(en.JJXZBM==null?"未知":en.JJXZBM);
					pzForm.getForm().findField('hy').setValue(en.HYBM==null?"未知":en.HYBM);
					pzForm.getForm().findField("sph").setValue("");
				}	
			}else{
				Ext.Msg.alert("提示","载入企业信息出错!");
			}
		});
		pzForm.getForm().findField('remark').setValue("");
		pzForm.getForm().findField('rkrq').setValue("<%=today%>");
		pz_grid_ds.removeAll();
	});
	var excelForm = new Ext.FormPanel({    
		id: 'excelForm',
		frame:true,
		labelWidth:80,
		labelAlign:'right',
		api: {
	        submit: EnHandler.importPzDetail
	    },
		bodyStyle:'padding-top:5px',
		layout : 'form',
		fileUpload : true,
		items:[
		{
			fieldLabel: 'Excel文件',
			inputType:'file',
			width:150,
			height:25,
			xtype: 'textfield',
			name: 'filepath',
			id: 'filepath'
		},{ 
			fieldLabel:'起始行',
			id : 'beginRow',
			name:'beginRow',
			xtype:'numberfield',
			width:100,
			value:1,
			allowDecimals:false,
			allowBlank:false
		}
		]
	});
	var excelWin = new Ext.Window({
	    title : 'Excel导入',
	    width : 300,
	    height : 200,
	    layout : 'fit',
		autoScroll : true,
		modal:true,
	    items : [excelForm],
	    closeAction:'hide',
	    buttons : [
	   	{
	   		name: 'import',
			id: 'import',
			text: '导入',
			handler : function() {
		   		if(required('filepath')!='success'){
		  			Ext.Msg.alert("提示","请选择要导入的文件!");
		  			return;
		  		}
		  		var x=document.getElementById('filepath').value;
		  		if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLS'&&x.substr(x.lastIndexOf(".")).toUpperCase()!=".XLSX"){
		  			Ext.Msg.alert("提示","请选择Excel文件导入！");
		  			return;
				}  
		  		excelForm.getForm().submit({
		       		timeout: 10*60*1000,
		       		params:{
		       			tid:'0',
		       			swdjzh:cSwdjzh 
			       	},
		       		success: function(form, action) {
		       			Ext.Msg.hide();
		       			var obj = action.result;
						if(obj&&obj.infos){
							Ext.Msg.show({title:'成功',
								msg: obj.infos.msg,
								buttons: Ext.Msg.OK,
								icon: Ext.MessageBox.INFO});
							pz_grid_ds.load();
						}else{
							Ext.Msg.show({title:'错误',
								msg: obj.errors.msg,
								buttons: Ext.Msg.OK,
								icon: Ext.MessageBox.ERROR});
						}
		       		},
		       		failure: function(form,action){
						var obj = action.result;
						if(obj&&obj.errors){
							Ext.Msg.alert("警告",obj.errors.msg);
						}
					},
					exceptionHandler : function(msg){
						Ext.Msg.hide();
						Ext.Msg.alert('提示',msg);
						return ; 
					}
		       	});
				excelWin.hide();
	   		}
		},{
	    	text : "取消",
		    handler:function(){
	    		excelWin.hide();
		    }
	    }]
	});	
	//定义类型数据
	var sphRecord =  Ext.data.Record.create([
	    {name: 'sph', type: 'string' },
	    {name: 'rkrq', type: 'string' },
	    {name: 'remark', type: 'string'},
	    {name: 'username', type: 'string'}
	]);  
	var sphStore = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: EnHandler.getEnPzhBySwdjzh,
			paramOrder: ['swdjzh'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({}, sphRecord)
	});
	var cb_pzh = new Ext.form.ComboBox({
		fieldLabel:'凭&nbsp;&nbsp;证&nbsp;&nbsp;号',
		name: 'pzhs',
	    id : 'pzhs',
	    width : 150,
		displayField:'sph',
		valueField:'sph',
		editable: false, 
		triggerAction : 'all',
		emptyText:'请选择...',
		allowBlank:true,
		value:'',
		store : sphStore,
		mode: 'local',
		listeners:{
			select : function(combo,record,idx){
				var sph = sphStore.getAt(idx).get("sph");
				v_ds.baseParams.swdjzh=cSwdjzh;
				v_ds.baseParams.sph=sph;
				v_ds.load();
				Ext.getCmp("delBtn").enable();
				var crkrq = sphStore.getAt(idx).get("rkrq");
				if(crkrq!=null&&crkrq!=""){
					var year = crkrq.substring(0,4);
					var month = crkrq.substring(4,6);
					var day = crkrq.substring(6);
					var s = year+'-'+month+"-"+day;
					viewForm.getForm().findField("rkrq").setValue(s);
				}
				var username = sphStore.getAt(idx).get("username");
				viewForm.getForm().findField("operator").setValue(username);
				var cremark = sphStore.getAt(idx).get("remark");
				viewForm.getForm().findField("remark").setValue(cremark);
			}
		}
	});	
var viewForm= new Ext.FormPanel({
    id: 'viewForm',
    border:false,
    frame:true,
    labelWidth :60,
    labelAlign: 'right',
	layout:'form',
    items: [{
       	layout:'column',
       	frame:false,
        border:false,
       	items:[
       	{
           	layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			items:[
				new Ext.form.Hidden({ 
	 				fieldLabel: '税号',
	 				width:150,
	 				readOnly: true,
	 				name: 'swdjzh'
	 			}),new Ext.form.TextField({ 
   					fieldLabel: '企业名称',
   					width:150,
   					allowBlank:true,
   					readOnly: true,
   					style:'background:none;border:0px;',
   					name: 'mc'
   				}),new Ext.form.TextField({ 
					fieldLabel: '入库日期',
					width:150,
					allowBlank: true,
					readOnly: true,
					style:'background:none;border:0px;',
					name: 'rkrq'
				}),cb_pzh
			]
        },
        {
            layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			items:[
			    new Ext.form.TextField({ 
					fieldLabel: '经济性质',
					width:150,
					allowBlank: true,
					readOnly: true,
					style:'background:none;border:0px;',
					name: 'jjxz'
				}),new Ext.form.TextField({ 
    	 			fieldLabel: '行&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;业',
    	 			width:150,
    	 			allowBlank:true,
    	 			readOnly: true,
    	 			style:'background:none;border:0px;',
    	 			name: 'hy'
    	 		}),new Ext.form.TextField({ 
   					fieldLabel: '操&nbsp;&nbsp;作&nbsp;&nbsp;者',
   					width:150,
   					allowBlank: true,
   					readOnly: true,
   					style:'background:none;border:0px;',
   					value :'',
   					name: 'operator'
   				})
			]
        }]
    },new Ext.form.TextArea({ 
			fieldLabel: '备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注',
				width:400,
				height:60,
				allowBlank: true,
				readOnly: true,
				name: 'remark'
			})
    ]
});
var view_cm = new Ext.grid.ColumnModel({
	columns: [
	    {
	       header: "财政分片",
	       dataIndex: 'fpmc',
	       width: 100,
	       align:'left'
	    },
	    {
	       header: "税种",
	       dataIndex: 'szmc',
	       width: 100,
	       align: 'left'
	    },
	    {
	        header: "预算级次",
	        dataIndex: 'ysjc',
	        width: 120,
	        align: 'left'
	    },{
		    header: "预算科目",
		    dataIndex: 'yskm',
		    width: 150,
		    hidden :false,
		    align: 'left'
		},{
		    header: "金额",
		    dataIndex: 'je',
		    width: 100,
		    hidden :false,
		    align: 'right',
			renderer :function(v,p,r){
				return regMoney(v,p,r);
			}
		}
	],
	defaultSortable: false
});
var view_Record = Ext.data.Record.create([                             
       {name: 'fpbm', type: 'string'},
       {name: 'fpmc', type: 'string'},
       {name: 'szbm', type: 'string'},
       {name: 'szmc', type: 'string'},
       {name: 'ysjcbm', type: 'string'},
       {name: 'ysjc', type: 'string'},
       {name: 'yskmbm', type: 'string'},     
       {name: 'yskm',type:'string'},
       {name: 'je',type:'float'}
 ]);
var v_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: EnHandler.getPzDetail,
		paramOrder: ['swdjzh','sph'],
		paramsAsHash: false
	}),  
	reader : new Ext.data.JsonReader({},view_Record)
});

var viewGrid = new Ext.grid.GridPanel({
	title:'凭证明细',
	store: v_ds,
    cm: view_cm,
    frame:false,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
    tbar: [
    {
	    text: '删除当前凭证',
	    disabled: true,
	    id:"delBtn",
	    iconCls: 'removePz',
	    handler : function(){
	    	var cpzh = Ext.getCmp("pzhs").getValue();
	    	Ext.MessageBox.confirm('确认删除', '你真的要删除当前凭证信息吗?', function(btn){
				if(btn == 'yes') {// 选中了是按钮	
					EnHandler.delPz(cSwdjzh,cpzh,function(data){
						var obj = Ext.decode(data);
						if(obj.result){
							//重载grid记录，重载凭证号记录sphStore
							Ext.Msg.alert('系统提示','当前凭证删除成功');
							v_ds.removeAll();
							sphStore.baseParams.swdjzh=cSwdjzh;
							sphStore.load();
							viewForm.getForm().findField("pzhs").setValue("");
						}else{
							Ext.Msg.alert('警告','删除凭证未成功:'+obj.info);
						}
					});
				}
	    	});
		}
	}]
});
var vWinConfig = {
	title : '查看凭证',
	width : 600,
	height : 480,
	autoScroll : true,
	maximizable: false,
	layout : 'border',
	items:[{	
		id:'north',
		region:'north',
		height:200,
		frame:true,
		layout:'fit',
		title:"",
		items: viewForm
	},{  
		region:'center',  
		layout:'fit',  
		margins:'0 0 0 0',  
		items: viewGrid  
	}],
	closeAction:'hide',
	modal:true,
	buttons : [{
		text : "确定",
		handler : function() {	
			viewWin.hide();
		}		                	
	}
	]
};	
var viewWin = new Ext.Window(vWinConfig);
viewWin.on("show",function(){
	EnHandler.getVirtualEn(cSwdjzh,function(data){
		var obj = Ext.decode(data);
		if(obj&&obj.result){
			var en = obj.en;
			viewForm.getForm().findField('swdjzh').setValue(en.SWDJZH);
			viewForm.getForm().findField('mc').setValue(en.MC==null?"未知":en.MC);
			viewForm.getForm().findField('jjxz').setValue(en.JJXZBM==null?"未知":en.JJXZBM);
			viewForm.getForm().findField('hy').setValue(en.HYBM==null?"未知":en.HYBM);
		}else{
			Ext.Msg.alert("提示","载入企业信息出错!");
		}
	});
	viewForm.getForm().findField('pzhs').setValue("");
	viewForm.getForm().findField('operator').setValue("");
	viewForm.getForm().findField('remark').setValue("");
	viewForm.getForm().findField('rkrq').setValue("");
	v_ds.removeAll();
	sphStore.baseParams.swdjzh=cSwdjzh;
	sphStore.load();
});
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[enGrid]
	});
	enDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
</script>
</head>
<body>
</body>
</html>