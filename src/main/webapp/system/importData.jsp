<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
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
<script type="text/javascript" src="<%=request.getContextPath()%>/js/datapro.js"></script>
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.datapro.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.datapro.REMOTING_API);
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
var tbRecord =  Ext.data.Record.create([
    {name: 'tbname', type: 'string' },
    {name: 'tbdesc', type: 'string' },
    {name: 'ttype', type: 'int'},
    {name: 'tid', type: 'int'}
]);  
var tbStore = new Ext.data.Store({
    proxy: new Ext.data.DirectProxy({
        directFn: DataHandler.getTbList,
        paramsAsHash: false
    }), 
    reader: new Ext.data.JsonReader({}, tbRecord)
});
tbStore.load();	
var mStore = new Ext.data.SimpleStore({ 
	fields : ["mid", "mname"], 
	data : [ 
	['1', '1月'], ['2', '2月'],['3', '3月'],['4', '4月'],['5', '5月'],['6', '6月'],
	['7', '7月'], ['8', '8月'],['9', '9月'],['10', '10月'],['11', '11月'],['12', '12月']
 	] 
});
var sStore = new Ext.data.SimpleStore({ 
	fields : ["sid", "sname"], 
	data : [ 
	['1', '1季度'], ['2', '2季度'],['3', '3季度'],['4', '4季度']
 	] 
});
var flag = false;
var tableName = '';
var tid=-1;
var ttype = 0;
var cForm = new Ext.form.FormPanel({    
	labelWidth: 120,    
	title:'',
	labelAlign: 'left',
	frame:true,
    buttonAlign : 'center',
	api: {
	    submit: DataHandler.impData
	},
	layout: 'form', 
	fileUpload: true,    
    items: [
		new Ext.form.ComboBox({
			width:250,
			fieldLabel : '数据表',
			name : 'tbname',
			id: 'dataFile',
			editable : false,
			hiddenName:'tid',
			valueField : "tid", 
			displayField : "tbdesc", 
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus:true,
			emptyText : '请选择...',
			store: tbStore
			,listeners :{
				select : function(combo,record){
					tableName = record.get('tbname');
					tid = record.get('tid');
					if(record.get('ttype')>0){
						flag = true;
						ttype = record.get("ttype");
						cForm.getForm().findField('nian').enable();    
						cForm.getForm().findField('nian').show();  
						if(ttype==1){
						    cForm.getForm().findField('yue').enable();    
						    cForm.getForm().findField('yue').show(); 
						    cForm.getForm().findField('ji').disable();    
						    cForm.getForm().findField('ji').hide(); 
						}else if(ttype==2){
						    cForm.getForm().findField('ji').enable();    
						    cForm.getForm().findField('ji').show(); 
						    cForm.getForm().findField('yue').disable();    
						    cForm.getForm().findField('yue').hide(); 
						}else if(ttype==3){
						    cForm.getForm().findField('yue').disable();    
						    cForm.getForm().findField('yue').hide();
						    cForm.getForm().findField('ji').disable();    
						    cForm.getForm().findField('ji').hide();  
						}   
					}else{
						flag = false;
						ttype = 0;
						cForm.getForm().findField('nian').disable();     
						cForm.getForm().findField('nian').hide(); 
						cForm.getForm().findField('yue').disable();     
						cForm.getForm().findField('yue').hide(); 
						cForm.getForm().findField('ji').disable();     
						cForm.getForm().findField('ji').hide();      
					}
				}
			}
		}),
		new Ext.form.NumberField({
			name:'nian',
			width:120,
			maxLength: 4,
			hidden: true,
			fieldLabel: '年份'
		}),
		new Ext.form.ComboBox({ 
			fieldLabel: '月份',
			name: 'yue_cb',
			hiddenName:'yue',
			width: 120,
			mode : 'local', 
			hidden: true,
			triggerAction : 'all', 
			forceSelection:true,
			store : mStore,
			valueField : "mid", 
			displayField : "mname", 
			editable: true
		}),	
		new Ext.form.ComboBox({ 
			fieldLabel: '季度',
			name: 'ji_cb',
			hiddenName:'ji',
			width: 120,
			mode : 'local', 
			hidden: true,
			triggerAction : 'all', 
			forceSelection:true,
			store : sStore,
			valueField : "sid", 
			displayField : "sname", 
			editable: true
		}),	
		new Ext.form.TextField({
			fieldLabel : '请选择要导入的文件',
			name: 'filepath',
			id: 'filepath',
			width:320,
			height:25,
			inputType:'file'
		})
	],
    buttons: [{
	    text: '模板下载',    
		handler: function(){
	    	if(tid<0){
	      		Ext.Msg.alert("提示","请选择数据表!");
	      		return;
	      	}
	    	fwin.show();
    	}
    },{    
	    text: '导入',    
	    handler: function(){
		    if(tid<0){
	      		Ext.Msg.alert("提示","请选择要导入的数据表!");
	      		return;
	      	}
		    var nian="";
	    	if(flag){
	    		nian= cForm.getForm().findField("nian").getValue();
				if(nian==""){
				    Ext.Msg.alert("提示","请输入年份！");
				    return;
				}
				if(ttype ==1){
				    if(cForm.getForm().findField('yue').getValue()==""){
				      	Ext.Msg.alert("提示","请输入月份！");
					    return;
				    }
				}else  if(ttype ==2){
				    if(cForm.getForm().findField('ji').getValue()==""){
				      	Ext.Msg.alert("提示","请输入季度！");
					    return;
				    }
				}
	      	}
	      	if(required('filepath')!='success'){
	      		Ext.Msg.alert("提示","请选择要导入的文件!");
	      		return;
	      	}
	      	var x=document.getElementById('filepath').value;
	      	if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLS'&&x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLSX'){
	      		Ext.Msg.alert("提示","请选择Excel文件导入！");
	      		return;
			}  
	      	Ext.Msg.wait("正在导入...");
	      	cForm.getForm().submit({
		       	timeout: 10*60*1000,
		       	params:{ttype: ttype},
		       	success: function(form, action) {
			       	Ext.Msg.hide();
			       	var obj = action.result;
					if(obj&&obj.infos){
						document.getElementById("infoDiv").innerHTML=obj.infos.msg;
					}else if(obj&&obj.errors){
						document.getElementById("infoDiv").innerHTML=="<font color=red>"+obj.errors.msg+"</font>";
					}
			    },
			    failure: function(form,action){
			       	Ext.Msg.hide();
					var obj = action.result;
					if(obj&&obj.errors){
						document.getElementById("infoDiv").innerHTML="<font color=red>"+obj.errors.msg+"</font>";
					}
				},
				exceptionHandler : function(msg){
					Ext.Msg.hide();
					Ext.Msg.alert('提示',msg);
					return ; 
				}
		    });
		}    
	}]   
});  
var fForm = new Ext.FormPanel({
    id: 'formatForm',
    frame: true,
    labelAlign: 'left',
    width: 240,
    height: 200,
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
	    }
	]
}); 
var fwin = new Ext.Window({
	title: '格式',
    width: 340,
    height: 250,
    layout: 'fit',
    buttonAlign:'center',
    items: fForm,
    buttons: [{
        text: '确定',
        handler:function(){
	    	DataHandler.CheckTemplateDownload(tid,function(data){
				var obj = Ext.decode(data);
				Ext.Msg.hide();
				if(obj&&obj.hasRecord){
					var downloadForm = document.getElementById("fileDownloadForm"); 
    				document.getElementById("expTid").value=tid;
    				if(Ext.getCmp('formatXlsx').checked){
    		    		document.getElementById("format").value="xlsx";
    			    }else{
    			    	document.getElementById("format").value="xls";
    			    }
    				downloadForm.action = 'downfile.mt?doType=exportExtendTemplate'; 
    				downloadForm.method = "POST"; 
    				downloadForm.submit(); 		
				}
	    	});

			
			fwin.hide();
    	}
    },{
    	text: '取消',
        handler:function(){
    		fwin.hide();
    	}
    }]
});
Ext.onReady(function(){
	Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	var viewport = new Ext.Viewport({
		layout:'border',
		items:[{	
		    id:'north-panel',
		    region:'north',
		    margins:'2 0 0 2',
		    layout:'fit',
		    frame:true,
		    height:250,
		    title:"扩展表导入",
		    items: cForm
		},{
		    region:'center',
		    id: 'infoPanel',
		    margins:'0 0 5 0',
		    layout:'column',
		    frame:true,
		    autoScroll:false,
		    items:[
		        {contentEl: 'infoDiv'}
		    ]
		}]
	}); 	   
});
function required(para){
	var obj=document.getElementById(para);
	if(obj.getAttribute("value")&&obj.getAttribute("value")!=""){
		return "success";
	}else{
		return "不能为空！";
	}
}

</script>
</head>
<body>
<form id="fileDownloadForm" action="">
<input type="hidden" name="expTid" id="expTid" value="">
<input type="hidden" id="format" name="format" value=""/> 
</form>
<div id='infoDiv'></div>
</body>
</html>