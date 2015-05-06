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
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
 	TaxGrid = function(config) {
	    Ext.apply(this, config);
	    TaxGrid.superclass.constructor.call(this, {
	        region: 'center',
	        layout:"fit"
	    });
	};
	Ext.extend(TaxGrid, Ext.grid.GridPanel);
	
	Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
	Ext.sys.REMOTING_API.enableBuffer = 0;  
	Ext.Direct.addProvider(Ext.sys.REMOTING_API);
	var cMode = "modify";
	var cRd ;
	var cWho = 9;
	var cDRow,cGRow,cFRow;
	var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect:true});
	ssm.handleMouseDown = Ext.emptyFn;
	var cm = new Ext.grid.ColumnModel({
		columns: [
			ssm,
		    {
		        header: "编码表",
		        dataIndex: 'table_bm',
		        width: 100,
		        align: 'left'    
		    },{
		        header: "编码表名",
		        dataIndex: 'name',
		        width: 180,
		        align: 'left'    
		    },{
		        header: "是否启用",
		        dataIndex: 'qybj',
		        width: 70,
		        align: 'left',
		        renderer: function(v,p,r){
					if(v==0){
						return "未启用";
					}else{
						return "启用";
					}
				}        
		    },{
		    	header: "备注",
		        dataIndex: 'remark',
		        width: 250,
		        align: 'left'
			}
		],
		defaultSortable: false
	});
	var cRecord = Ext.data.Record.create([  
		{name: 'table_bm', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'qybj', type: 'int'},
		{name: 'remark', type: 'string'}
	]);
	var cds = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: CodeHandler.getCodeTables,
			paramOrder: ['who'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty: "table_bm"
		}, cRecord)
	});
	cds.on("beforeload",function(){
		cds.baseParams.who=9;
	});
	var view = new Ext.grid.GridView();
	var cGrid = new Ext.grid.GridPanel({
			title:'财政编码表',
			store: cds,
		    cm: cm,
		    frame:false,
		    stripeRows: true,
		    loadMask: {msg:'正在加载数据....'},
		    enableColumnMove: false,
			view : view,
			stripeRows: true,
			selModel: ssm,
			tbar: [
			{
				text: '增加',
				iconCls: 'add',
	            handler : function(){
	            	cWho=9;
	            	cMode = "add";
	            	ctWin.show();
				}
			},{
				text: '修改',
				iconCls: 'edit',
	            handler : function(){
					var records = cGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
		            if(!records||records.length<1){
						Ext.Msg.alert("提示","请先选择要修改的记录!");
						return;
					}	
		            cWho=9;
					cMode = "modify";
					cRd = records[0];
	            	ctWin.show();
				}
			},{
				text: '删除',
				iconCls: 'remove',
	            handler : function(){
					deleteCodeTable(cGrid,9);
				}
			}]
	});
	ssm.on("rowselect",function(sl,rowIndex,r){
		var ftb = r.get("table_bm");
		var gtb = "",dtb = "";
		var fTitle = "当前选中编码表:"+ftb;
		CodeHandler.getTableMappingInfo(ftb,9,0,function(data){
			if(data!=""){
			   	var result = Ext.util.JSON.decode(data);
			   	if(result){
			   		gtb = result.gs;
			   		dtb = result.ds;
			   		fTitle += ";映射地税编码:"+dtb+";映射国税编码:"+gtb;
			   		cGrid.setTitle(fTitle);
			   		dsGrid.setTitle("地税编码表");
			   		gsGrid.setTitle("国税编码表");
			   		if(cFRow){
			   			cFRow.style.backgroundColor=''; 
		   			}
			   		//国税
		   			if(cGRow){
		   				cGRow.style.backgroundColor=''; 
		   			}
			   		if(gtb==""){
			   			cGRow=null;
			   		}else{
			   			cGRow = gsGrid.getView().getRow(gsDs.indexOfId(gtb));
				   		if(cGRow){
				   			cGRow.style.backgroundColor='#F7FE2E'; 
				   		}
			   		}
			   		//地税
			   		if(cDRow){
			   			cDRow.style.backgroundColor=''; 
		   			}
			   		if(dtb==""){
			   			cDRow=null;
			   		}else{
			   			cDRow = dsGrid.getView().getRow(dsDs.indexOfId(dtb));
			   			if(cDRow){
			   				cDRow.style.backgroundColor='#F7FE2E'; 
			   			}
			   		}
			   	}
			}
		});
	});
	var ctForm = new Ext.FormPanel({
		//id: 'ctForm',
		frame: true,
		labelAlign:'right',
	    api: {
	        submit: CodeHandler.saveCodeTable
	    },
	    //paramOrder: ['cMode','table_bm'],
	    layout : 'column',
		items: [{
			layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			labelAlign: 'left',
			labelWidth: 60,
			items:[
			new Ext.form.TextField({	
				name:'table_bm',
				id: 'table_name',
			    fieldLabel : '编码表ID',
			    width:100,
			    maxLength: 20
			}),
			new Ext.form.TextField({
				name:'name',
				width:100,
			    fieldLabel : '编码表名',
			    maxLength: 50
			}),
			new Ext.form.Checkbox({
				name:'qybj',
				xtype: 'checkbox',
				fieldLabel : '',
				boxLabel :'启用',
				checked : true
			})]
		},{
			layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			labelAlign: 'left',
			labelWidth: 40,
			items:[
			new Ext.form.TextArea({
				name:'remark',
		        fieldLabel: '备注',
		        width: 130,
				height: 50
			})]
		}]
	});
	var ctWin = new Ext.Window({
		title : '编码表',
		width : 400,
		height : 180,
		autoScroll : true,
		layout : 'fit',
		items : [ctForm],
		closeAction:'hide',
		buttons : [{
			text : "确定",
			handler:function(){
				var f = ctForm.getForm();
				if(!ctForm.getForm().isValid()){
					return;
				}
				var tb_bm=Ext.getCmp("table_name").getValue();
				ctForm.getForm().submit({
					waitMsg:'执行中,请稍候...',
					params: {cMode: cMode,table_bm: tb_bm,who: cWho},
					success: function(form,action){
						var obj = action.result;
						if(obj&&obj.infos){
							Ext.Msg.show({title:'成功',
								msg: obj.infos.msg,
								buttons: Ext.Msg.OK,
								icon: Ext.MessageBox.INFO});
							if(cWho==0){
								dsDs.load();
							}else if(cWho==1){
								gsDs.load();
							}else{
								cds.load();
							}
							ctWin.hide();
						}
					},
					failure: function(form,action){
						var obj = action.result;
						if(obj&&obj.errors){
							if(obj.errors.table_bm){
								Ext.Msg.alert("警告",obj.errors.table_bm);
							}else{
								Ext.Msg.alert("警告",obj.errors.msg);
							}
						}
					}
				});
			}
		},{
            text : "取消",
            handler : function() {
				ctWin.hide();
            }
        }]
	});
	ctWin.on("show",function(){
		//修改模式时，编码表ID不能修改,增加模式时，清空
		if(cMode=="add"){
			ctForm.getForm().findField('table_bm').setDisabled(false);
			ctForm.getForm().findField('table_bm').setValue("");
			ctForm.getForm().findField('name').setValue("");
			ctForm.getForm().findField('qybj').setValue(true);
			ctForm.getForm().findField('remark').setValue("");
		}else{
			if(cRd){
				ctForm.getForm().findField('table_bm').setDisabled(true);
				ctForm.getForm().findField('table_bm').setValue(cRd.get("table_bm"));
				ctForm.getForm().findField('name').setValue(cRd.get("name"));
				ctForm.getForm().findField('qybj').setValue(cRd.get("qybj"));
				ctForm.getForm().findField('remark').setValue(cRd.get("remark"));
			}
		}
	});	

	//地税编码表grid
	var dssm = new Ext.grid.CheckboxSelectionModel({singleSelect:true});
	dssm.handleMouseDown = Ext.emptyFn;
	var dscm = new Ext.grid.ColumnModel({
		columns: [
			dssm,
		    {
		        header: "编码表",
		        dataIndex: 'table_bm',
		        width: 100,
		        align: 'left'    
		    },{
		        header: "编码表名",
		        dataIndex: 'name',
		        width: 180,
		        align: 'left'    
		    },{
		        header: "是否启用",
		        dataIndex: 'qybj',
		        width: 70,
		        align: 'left',
		        renderer: function(v,p,r){
					if(v==0){
						return "未启用";
					}else{
						return "启用";
					}
				}        
		    },{
		    	header: "备注",
		        dataIndex: 'remark',
		        width: 250,
		        align: 'left'
			}
		],
		defaultSortable: false
	});
	var dsRecord = Ext.data.Record.create([  
		{name: 'table_bm', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'qybj', type: 'int'},
		{name: 'remark', type: 'string'}
	]);
	var dsDs = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: CodeHandler.getCodeTables,
			paramOrder: ['who'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty: "table_bm"
		}, dsRecord)
	});
	dsDs.on("beforeload",function(){
		dsDs.baseParams.who=0;
	});
	var dsview = new Ext.grid.GridView();
	var dsGrid = new TaxGrid({
			title:'地税编码表',
			store: dsDs,
		    cm: dscm,
		    frame:false,
		    stripeRows: true,
		    loadMask: {msg:'正在加载数据....'},
		    enableColumnMove: false,
			view : dsview,
			stripeRows: true,
			selModel: dssm,
			tbar: [
			{
				text: '增加',
				iconCls: 'add',
	            handler : function(){
					cWho=0;
	            	cMode = "add";
	            	ctWin.show();
				}
			},{
				text: '修改',
				iconCls: 'edit',
	            handler : function(){
					var records = dsGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
		            if(!records||records.length<1){
						Ext.Msg.alert("提示","请先选择要修改的记录!");
						return;
					}	
		            cWho=0;
					cMode = "modify";
					cRd = records[0];
	            	ctWin.show();
				}
			},{
				text: '删除',
				iconCls: 'remove',
	            handler : function(){
					deleteCodeTable(dsGrid,0);
				}
			},{
				text: '解除映射',
				iconCls: 'unMap',
	            handler : function(){
					deleteMapping(dsGrid,0);
				}
			},{
				text: '保存映射',
				iconCls: 'autoMap',
	            handler : function(){
					saveMapping(dsGrid,0);
				}
			}]
	});
	dssm.on("rowselect",function(sl,rowIndex,r){
		var dtb = r.get("table_bm");
		var gtb = "",ftb = "";
		var dTitle = "当前选中编码表:"+dtb;
		CodeHandler.getTableMappingInfo(dtb,0,0,function(data){
			if(data!=""){
			   	var result = Ext.util.JSON.decode(data);
			   	if(result){
			   		gtb = result.gs;
			   		ftb = result.f;
			   		dTitle += ";映射财政编码:"+ftb+";映射国税编码:"+gtb;
			   		dsGrid.setTitle(dTitle);
			   		cGrid.setTitle("财政编码表");
			   		gsGrid.setTitle("国税编码表");
			   		if(cDRow){
		   				cDRow.style.backgroundColor=''; 
		   			}
			   		//国税
		   			if(cGRow){
		   				cGRow.style.backgroundColor=''; 
		   			}
			   		if(gtb==""){
			   			cGRow=null;
			   		}else{
			   			cGRow = gsGrid.getView().getRow(gsDs.indexOfId(gtb));
				   		if(cGRow){
				   			cGRow.style.backgroundColor='#F7FE2E'; 
				   		}
			   		}
			   		//财政
			   		if(cFRow){
			   			cFRow.style.backgroundColor=''; 
		   			}
			   		if(ftb==""){
			   			cFRow=null;
			   		}else{
			   			cFRow = cGrid.getView().getRow(cds.indexOfId(ftb));
			   			if(cFRow){
			   				cFRow.style.backgroundColor='#F7FE2E'; 
			   			}
			   		}
			   	}
			}
		});
	});
	//国税编码表grid
	var gssm = new Ext.grid.CheckboxSelectionModel({singleSelect:true});
	gssm.handleMouseDown = Ext.emptyFn;
	var gscm = new Ext.grid.ColumnModel({
		columns: [
			gssm,
		    {
		        header: "编码表",
		        dataIndex: 'table_bm',
		        width: 100,
		        align: 'left'    
		    },{
		        header: "编码表名",
		        dataIndex: 'name',
		        width: 180,
		        align: 'left'    
		    },{
		        header: "是否启用",
		        dataIndex: 'qybj',
		        width: 70,
		        align: 'left',
		        renderer: function(v,p,r){
					if(v==0){
						return "未启用";
					}else{
						return "启用";
					}
				}        
		    },{
		    	header: "备注",
		        dataIndex: 'remark',
		        width: 250,
		        align: 'left'
			}
		],
		defaultSortable: false
	});
	var gsRecord = Ext.data.Record.create([  
		{name: 'table_bm', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'qybj', type: 'int'},
		{name: 'remark', type: 'string'}
	]);
	var gsDs = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: CodeHandler.getCodeTables,
			paramOrder: ['who'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty: "table_bm"
		}, gsRecord)
	});
	gsDs.on("beforeload",function(){
		gsDs.baseParams.who=1;
	});
	var gsview = new Ext.grid.GridView();
	var gsGrid = new Ext.grid.GridPanel({
			title:'国税编码表',
			store: gsDs,
			region: 'center',
	        layout:"fit",
		    cm: gscm,
		    frame:false,
		    stripeRows: true,
		    loadMask: {msg:'正在加载数据....'},
		    enableColumnMove: false,
			view : gsview,
			stripeRows: true,
			selModel: gssm,
			tbar: [
			{
				text: '增加',
				iconCls: 'add',
	            handler : function(){
					cWho=1;
	            	cMode = "add";
	            	ctWin.show();
				}
			},{
				text: '修改',
				iconCls: 'edit',
	            handler : function(){
					var records = gsGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
		            if(!records||records.length<1){
						Ext.Msg.alert("提示","请先选择要修改的记录!");
						return;
					}	
		            cWho=1;
					cMode = "modify";
					cRd = records[0];
	            	ctWin.show();
				}
			},{
				text: '删除',
				iconCls: 'remove',
	            handler : function(){
					deleteCodeTable(gsGrid,1);
				}
			},{
				text: '解除映射',
				iconCls: 'unMap',
	            handler : function(){
					deleteMapping(gsGrid,1);
				}
			},{
				text: '保存映射',
				iconCls: 'autoMap',
	            handler : function(){
					saveMapping(gsGrid,1);
				}
			}]
	});
	gssm.on("rowselect",function(sl,rowIndex,r){
		var gtb = r.get("table_bm");
		var dtb = "",ftb = "";
		var gTitle = "当前选中编码表:"+gtb;
		CodeHandler.getTableMappingInfo(gtb,1,0,function(data){
			if(data!=""){
			   	var result = Ext.util.JSON.decode(data);
			   	if(result){
			   		dtb = result.ds;
			   		ftb = result.f;
			   		gTitle += ";映射财政编码:"+ftb+";映射地税编码:"+dtb;
			   		gsGrid.setTitle(gTitle);
			   		cGrid.setTitle("财政编码表");
			   		dsGrid.setTitle("地税编码表");
			   		if(cGRow){
		   				cGRow.style.backgroundColor=''; 
		   			}
			   		//地税
		   			if(cDRow){
		   				cDRow.style.backgroundColor=''; 
		   			}
			   		if(dtb==""){
			   			cDRow=null;
			   		}else{
			   			cDRow = dsGrid.getView().getRow(dsDs.indexOfId(dtb));
				   		if(cDRow){
				   			cDRow.style.backgroundColor='#F7FE2E'; 
				   		}
			   		}
			   		//财政
			   		if(cFRow){
			   			cFRow.style.backgroundColor=''; 
		   			}
			   		if(ftb==""){
			   			cFRow=null;
			   		}else{
			   			cFRow = cGrid.getView().getRow(cds.indexOfId(ftb));
			   			if(cFRow){
			   				cFRow.style.backgroundColor='#F7FE2E'; 
			   			}
			   		}
			   	}
			}
		});
	});
	function deleteCodeTable(grid,who){
		var records = grid.getSelectionModel().getSelections();// 返回值为 Record 类型
        if(!records||records.length<1){
			Ext.Msg.alert("提示","请先选择要删除的行!");
			return;
		}		
		// 弹出对话框警告
		if(records){
			Ext.MessageBox.confirm('确认删除', '删除编码表的同时会删除相关的编码和映射信息，确定要删除吗?', 
	    	    function(btn){
			    	if(btn == 'yes') {// 选中了是按钮
				    	var tb = records[0].get("table_bm");
			    		CodeHandler.deleteCodeTable(tb,who,function(data){
			    			if(data!=""){
			    			   	var result = Ext.util.JSON.decode(data);
			    			   	if(result&&result.success){
			    			   		cds.load();
			    			   		Ext.Msg.show({title:'成功',
										msg: "编码表已删除",
										buttons: Ext.Msg.OK,
										icon: Ext.MessageBox.INFO});
			    			   		cds.load();
			    			   		dsDs.load();
			    			   		gsDs.load();
			    			   	}
			    			}
			    		});
			    	}
				}	
			);
		}
	}
	function deleteMapping(grid,who){
		var records = grid.getSelectionModel().getSelections();// 返回值为 Record 类型
        if(!records||records.length<1){
			Ext.Msg.alert("提示","请先选择要操作的记录!");
			return;
		}
        if(records){
			Ext.MessageBox.confirm('确认删除', '删除映射关系并不编码表记录本身，只解除映射关系，确定操作吗?', 
	    	    function(btn){
			    	if(btn == 'yes') {// 选中了是按钮
				    	var tb = records[0].get("table_bm");
				        CodeHandler.deleteCodeTableMapping(tb,who,function(data){
							if(data!=""){
							   	var result = Ext.util.JSON.decode(data);
							   	if(result&&result.success){
							   		cGrid.getSelectionModel().clearSelections();
							   		if(who==0){
							   			gsGrid.getSelectionModel().clearSelections();
							   		}else{
							   			dsGrid.getSelectionModel().clearSelections();
							   		}
							   		Ext.Msg.show({title:'成功',
										msg: "编码表映射关系已解除",
										buttons: Ext.Msg.OK,
										icon: Ext.MessageBox.INFO});
							   	}
							}
						});
			    	}
				}
			);
        }	
	}
	function saveMapping(grid,who){
		var records = grid.getSelectionModel().getSelections();
        if(!records||records.length<1){
			Ext.Msg.alert("提示","请先选择要保存的记录!");
			return;
		}
		var frds = cGrid.getSelectionModel().getSelections();
		if(!frds||frds.length<1){
			Ext.Msg.alert("提示","请选择要对应的财政编码表!");
			return;
		}
		
		var tb = records[0].get("table_bm");
		var ftb = frds[0].get("table_bm");
		Ext.MessageBox.confirm('确认', '该操作将对当前<b>勾选</b>的财政编码表和'+(who==0?'地税':'国税')+'编码表进行映射，确定映射?',function(btn){
			if(btn == 'yes') {// 选中了是按钮
				CodeHandler.saveCodeTableMapping(tb,ftb,who,function(data){
					if(data!=""){
						var result = Ext.util.JSON.decode(data);
						if(result&&result.success){
							Ext.Msg.show({title:'成功',
								msg: "编码表映射关系已保存",
								buttons: Ext.Msg.OK,
								icon: Ext.MessageBox.INFO});
						}
					}
				});
			}
		});
	}
	Ext.onReady(function(){
		Ext.QuickTips.init();
		new Ext.Viewport({
	    	layout:'border',
            autoScroll:true,
            items:[{
                region:"west",
                layout: 'fit',
                width:450,
                items:[cGrid]
            },{
                region:"center",
                layout: 'border',
                items:[
                dsGrid,
				{
					id:'detail',
		            layout:'fit',
		            items: [gsGrid],
		            height: 260,
		            split: true,
		            border:false,
		            region:'south'
		        }]
            }]
	    });
		cds.load();
		gsDs.load();
		dsDs.load();
	});        
</script>
</head>
<body>
</body>
</html>