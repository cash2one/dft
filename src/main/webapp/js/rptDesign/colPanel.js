var colForm =new Ext.FormPanel({
	frame: true,
	labelWidth: 70,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
	layout : 'form',
	items:[{
		hideLabel: true,
		boxLabel: '复杂表头',
		xtype: 'checkbox',
		name: 'isComplex',
		width: 100,
		checked : false
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '列头构造方式',
        layout:'form',
        items: [{
            xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .21,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: 'SQL构造',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_colSql', 
					width: 120,
					checked : true,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_colSql").enable();
								Ext.getCmp("btn_colPro").disable();
								colForm.getForm().findField("colClass").disable();
								var tbtns=colsGrid.getTopToolbar().items;
								for(var i=0;i<tbtns.getCount();i++){
									tbtns.itemAt(i).disable();
								}
							}else{
								Ext.getCmp("btn_colSql").disable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'colSql' 
				}]
			},{
				columnWidth : .32,
				layout : 'form',
				items : [{
					name : 'btn_colSql',
					id:'btn_colSql',
	                xtype: 'button',
	                text: '详情...',
	                handler: function(){
	                	var sv = colForm.getForm().findField("colSql").getValue();
						sqlWindow.setSql(sv);
						sqlWindow.onHideSetValue=function(v){
							colForm.getForm().findField("colSql").setValue(v);
						}
						sqlWindow.show();
					}
				}]
			},{
				columnWidth : .21,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: '存储过程构造',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_colPro',
					width: 120,
					checked : false,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_colPro").enable();
								Ext.getCmp("btn_colSql").disable();
								colForm.getForm().findField("colClass").disable();
								var tbtns=colsGrid.getTopToolbar().items;
								for(var i=0;i<tbtns.getCount();i++){
									tbtns.itemAt(i).disable();
								}
							}else{
								Ext.getCmp("btn_colPro").disable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'colPro' 
				}]
			},{
				columnWidth : .26,
				layout : 'form',
				items : [{
					name : 'btn_colPro',
					id:'btn_colPro',
		            xtype: 'button',
		            text: '详情...',
		            handler: function(){
						procedureWindow.setProInfo(colForm.getForm().findField("colPro").getValue());
						procedureWindow.onHideSetValue=function(v){
							colForm.getForm().findField("colPro").setValue(v);
						}
						procedureWindow.show();
					}
				}]
			}]
		},{
            xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .21,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: '扩展类取数',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_colClass',
					width: 160,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_colPro").disable();
								Ext.getCmp("btn_colSql").disable();
								colForm.getForm().findField("colClass").enable();
								var tbtns=colsGrid.getTopToolbar().items;
								for(var i=0;i<tbtns.getCount();i++){
									tbtns.itemAt(i).disable();
								}
							}else{
								colForm.getForm().findField("colClass").disable();
							}
						}
					}
				}]
			},{
				columnWidth : .32,
				layout : 'form',
				items : [{
					hideLabel: true,
					xtype: 'textfield',
					name: 'colClass',
					width: 120
				}]
			},{
				columnWidth : .21,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: '静态数据',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_colStatic',
					width: 120,
					checked : false,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								//grid可用
								Ext.getCmp("btn_colPro").disable();
								Ext.getCmp("btn_colSql").disable();
								colForm.getForm().findField("colClass").disable();
								var tbtns=colsGrid.getTopToolbar().items;
								for(var i=0;i<tbtns.getCount();i++){
									tbtns.itemAt(i).enable();
								}
							}else{
								//grid不可用
								var tbtns=colsGrid.getTopToolbar().items;
								for(var i=0;i<tbtns.getCount();i++){
									tbtns.itemAt(i).disable();
								}
							}
						}
					}
				}]
			}]
		}]
	}]
});
var cols_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var cols_cm = new Ext.grid.ColumnModel({
	columns: [
	cols_sm,
	{
	    header: "列ID",
	    dataIndex: 'colId',
	    width: 60,
	    align:'left'
	},{
		header: "列头名称",
	    dataIndex: 'colName',
	    width: 90,
	    align:'left',
	    renderer: renderFoo
	},{
	    header: "数据字段",
	    dataIndex: 'dataIndex',
	    width: 70,
	    align:'left'
	},{
	    header: "底级节点",
	    dataIndex: 'isleaf',
	    width: 60,
	    align:'left',
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "父列ID",
	    dataIndex: 'pid',
	    width: 70,
	    align:'left'
	},{
	    header: "数据类型",
	    dataIndex: 'dataType',
	    width: 70,
	    align:'left',
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "整数";
	    	}else if(v==2){
		    	return "小数";
	    	}else {
		    	return "字符串";
	    	}
		}
	},{
	    header: "列宽",
	    dataIndex: 'width',
	    width: 50,
	    align:'left'
	},{
	    header: "隐藏列",
	    dataIndex: 'isHidden',
	    width: 50,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "可排序",
	    dataIndex: 'isOrder',
	    width: 50,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "可切换单位",
	    dataIndex: 'isMultiUnit',
	    width: 70,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "链接列",
	    dataIndex: 'isLink',
	    width: 50,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "链接到",
	    dataIndex: 'linkTo',
	    width: 70,
	    align:'left',
	    renderer: renderFoo
	},{
	    header: "链接target",
	    dataIndex: 'target',
	    width: 60,
	    align:'left'
	},{
	    header: "链接参数",
	    dataIndex: 'linkParams',
	    width: 90,
	    align:'left',
	    renderer: renderFoo
	},{
	    header: "可隐藏0",
	    dataIndex: 'hideZero', 
	    width: 60,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "渲染函数", 
	    dataIndex: 'renderer',
	    width: 90,
	    align:'left',
	    renderer: renderFoo
	}],
	defaultSortable: false
});
var cols_Record = Ext.data.Record.create([    
	{name: 'colId', type: 'string'},
	{name: 'colName', type: 'string'},
	{name: 'dataIndex', type: 'string'},
	{name: 'pid', type: 'string'},
	{name: 'isleaf', type: 'int'},
	{name: 'dataType', type: 'int'},
	{name: 'renderer', type: 'string'},
	{name: 'width', type: 'int'},
	{name: 'isHidden', type: 'int'},
	{name: 'isOrder', type: 'int'},
	{name: 'isMultiUnit', type: 'int'},
	{name: 'isLink', type: 'int'},
	{name: 'linkParams', type: 'string'},
	{name: 'target', type: 'string'},
	{name: 'linkTo', type: 'string'},
	{name: 'hideZero', type: 'int'},
	{name: 'align', type: 'string'},
	{name: 'defaultHide', type: 'int'}
]);
var cols_ds = new Ext.data.Store({
    reader: new Ext.data.JsonReader({
    	 id:'colId'
	},cols_Record)
});
var colsGrid = new Ext.grid.GridPanel({
	title:'静态列头',
	store: cols_ds,
    cm: cols_cm,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: cols_sm,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加',
		iconCls: 'add',
        handler : function(){
			var scf = singleColForm.getForm();
			scf.findField("colId").setValue("");
			scf.findField("colName").setValue("");
			scf.findField("dataIndex").setValue("");
			scf.findField("pid").setValue("");
			scf.findField("isleaf").setValue(1);
			scf.findField("dataType").setValue(0);
			scf.findField("renderer").setValue("");
			scf.findField("width").setValue(100);
			scf.findField("isHidden").setValue(0);
			scf.findField("isOrder").setValue(0);
			scf.findField("isLink").setValue(0);
			scf.findField("linkParams").setValue("");
			scf.findField("target").setValue("");
			scf.findField("linkTo").setValue("");
			scf.findField("isMultiUnit").setValue(0);
			scf.findField("hideZero").setValue(0);
			scf.findField("align").setValue("");
			scf.findField("defaultHide").setValue(0);
			editColWin.addMode="add";
			editColWin.show();
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = colsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的列头定义!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					cols_ds.getModifiedRecords().remove(records[rc]);
					cols_ds.remove(records[rc]);
				}
	        }
		}
	},{
		text: '修改',
		iconCls: 'edit',
        handler : function(){
			var records = colsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择列定义!");
				return;
			}
	        var rd = records[0];
	        var scf = singleColForm.getForm();
	        scf.findField("colId").setValue(rd.get("colId"));
			scf.findField("colName").setValue(rd.get("colName"));
			scf.findField("dataIndex").setValue(rd.get("dataIndex"));
			scf.findField("pid").setValue(rd.get("pid"));
			scf.findField("isleaf").setValue(rd.get("isleaf"));
			scf.findField("dataType").setValue(rd.get("dataType"));
			scf.findField("renderer").setValue(rd.get("renderer"));
			scf.findField("width").setValue(rd.get("width"));
			scf.findField("isHidden").setValue(rd.get("isHidden"));
			scf.findField("isOrder").setValue(rd.get("isOrder"));
			scf.findField("isLink").setValue(rd.get("isLink"));
			scf.findField("linkParams").setValue(rd.get("linkParams"));
			scf.findField("target").setValue(rd.get("target"));
			scf.findField("linkTo").setValue(rd.get("linkTo"));
			scf.findField("isMultiUnit").setValue(rd.get("isMultiUnit"));
			scf.findField("hideZero").setValue(rd.get("hideZero"));
			scf.findField("align").setValue(rd.get("align"));
			scf.findField("defaultHide").setValue(rd.get("defaultHide"));
			editColWin.addMode="modify";
	        editColWin.show();	
		}
	}]
});
var singleColForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 60,
	labelAlign: 'left',
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .5,
			layout : 'form',
			items : [{
				fieldLabel: '列ID',
				xtype: 'textfield',
				name: 'colId',
				width: 120
			},{
				fieldLabel: '数据字段',
				xtype: 'textfield',
				name: 'dataIndex',
				width: 120
			},new Ext.form.ComboBox({
				fieldLabel:'数据类型',
				name: 'cb_dataType',
				width : 120,
				displayField:'text',
				valueField:'id',
				editable: false, 
				triggerAction : 'all',
				allowBlank:false,
				hiddenName:'dataType',
				store : new Ext.data.SimpleStore({ 
					fields : ["id", "text"], 
					data : [ 
						[0, '字符串'], 
						[1, '整数'],
						[2, '小数']
					] 
				}),
				mode: 'local'
			}),{
				fieldLabel: '列宽',
				xtype: 'numberfield',
				name: 'width',
				width: 120
			}]
		},{
			columnWidth : .5,
			layout : 'form',
			items : [{
				fieldLabel: '列头名称',
				xtype: 'textfield',
				name: 'colName',
				width: 120
			},{
				fieldLabel: '父列ID',
				xtype: 'textfield',
				name: 'pid',
				width: 120
			},new Ext.form.ComboBox({
				fieldLabel:'对齐方式',
				name: 'cb_align',
				width : 120,
				displayField:'text',
				valueField:'id',
				editable: false, 
				triggerAction : 'all',
				allowBlank:false,
				hiddenName:'align',
				store : new Ext.data.SimpleStore({ 
					fields : ["id", "text"], 
					data : [ 
						['', '自动'], 
						['left', '左对齐'],
						['center', '居中'],
						['right', '右对齐']
					] 
				}),
				mode: 'local'
			}),{
				fieldLabel: '渲染函数',
				xtype: 'textfield',
				name: 'renderer',
				width: 120
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '',
        layout:'form',
        items: [{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .33,
				layout : 'form',
				items : [{
					hideLabel: true,
		    		boxLabel: '底级节点',
		    		xtype: 'checkbox',
		    		name: 'isleaf',
		    		width: 100,
		    		checked : false
				},{
					hideLabel: true,
		    		boxLabel: '可排序',
		    		xtype: 'checkbox',
		    		name: 'isOrder',
		    		width: 100,
		    		checked : false
				},{
					fieldLabel: '初始不显示',
					hideLabel: true,
		    		boxLabel: '初始不显示',
		    		xtype: 'checkbox',
		    		name: 'defaultHide',
		    		width: 100,
		    		checked : false
				}]
			},{
				columnWidth : .33,
				layout : 'form',
				items : [{
					hideLabel: true,
		    		boxLabel: '可切换单位',
		    		xtype: 'checkbox',
		    		name: 'isMultiUnit',
		    		width: 100,
		    		checked : false
				},{
					fieldLabel: '可隐藏0',
					hideLabel: true,
		    		boxLabel: '可隐藏0',
		    		xtype: 'checkbox',
		    		name: 'hideZero',
		    		width: 100,
		    		checked : false
				}]
			},{
				columnWidth : .33,
				layout : 'form',
				items : [{
					hideLabel: true,
					boxLabel: '隐藏列',
					xtype: 'checkbox',
					name: 'isHidden',
					width: 100,
					checked : false
				},{
					hideLabel: true,
		    		boxLabel: '链接列',
		    		xtype: 'checkbox',
		    		name: 'isLink',
		    		width: 100,
		    		checked : false,
		    		listeners :{
		    			check: function(ckbox,checked){
		    				var sf = singleColForm.getForm();
		    				if(checked){
		    					sf.findField("target").enable();
		    					sf.findField("linkTo").enable();
		    					sf.findField("linkParams").enable();
		    				}else{
		    					sf.findField("target").disable();
		    					sf.findField("linkTo").disable();
		    					sf.findField("linkParams").disable();
		    				}
		        		}
		        	}
				}]
			}]
        }]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '链接报表',
        layout:'form',
        items: [{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 60,
				items : [{
					fieldLabel: '链接到',
					xtype: 'textfield',
					name: 'linkTo',
					disabled :true,
					width: 110
				}]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 70,
				items : [{
					fieldLabel: '链接target',
					xtype: 'textfield',
					name: 'target',
					disabled :true,
					width: 100
				}]
			}]
        },{
			fieldLabel: '链接参数',
			xtype: 'textfield',
			name: 'linkParams',
			disabled :true,
			width: 310
		}]
	}]
});
var editColWin = new Ext.Window({
	id : 'singleColForm',
	title : '列定义',
	items : [singleColForm],
	layout : 'fit',
	width : 450,
	height : 360,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var scf = singleColForm.getForm();
			if(editColWin.addMode=="add"){
				//增加一条记录
				var rd = new cols_Record({
					colId: scf.findField("colId").getValue(),
					colName: scf.findField("colName").getValue(),
					dataIndex: scf.findField("dataIndex").getValue(),
					pid: scf.findField("pid").getValue(),
					isleaf: (scf.findField("isleaf").getValue()=="on"||scf.findField("isleaf").getValue())?1:0,
					dataType: scf.findField("dataType").getValue(),
					renderer: scf.findField("renderer").getValue(),
					width: scf.findField("width").getValue(),
					isHidden: (scf.findField("isHidden").getValue()=="on"||scf.findField("isHidden").getValue())?1:0,
					isOrder: (scf.findField("isOrder").getValue()=="on"||scf.findField("isOrder").getValue())?1:0,
					isMultiUnit: (scf.findField("isMultiUnit").getValue()=="on"||scf.findField("isMultiUnit").getValue())?1:0,
					isLink: (scf.findField("isLink").getValue()=="on"||scf.findField("isLink").getValue())?1:0,
					linkParams: scf.findField("linkParams").getValue(),
					target: scf.findField("target").getValue(),
					linkTo: scf.findField("linkTo").getValue(),
					hideZero: (scf.findField("hideZero").getValue()=="on"||scf.findField("hideZero").getValue())?1:0,
					align: scf.findField("align").getValue(),
					defaultHide: scf.findField("defaultHide").getValue()
			    });
				colsGrid.stopEditing();
				cols_ds.insert(cols_ds.getCount(), rd);
			}else{
				var records = colsGrid.getSelectionModel().getSelections();
				if (!records||records.length==0) {
					editColWin.hide();
					return;
				}
				var rd = records[0];
				rd.set("colId", scf.findField("colId").getValue());
				rd.set("colName", scf.findField("colName").getValue());
				rd.set("dataIndex", scf.findField("dataIndex").getValue());
				rd.set("pid", scf.findField("pid").getValue());
				rd.set("isleaf",(scf.findField("isleaf").getValue()=="on"||scf.findField("isleaf").getValue())?1:0);
				rd.set("dataType",scf.findField("dataType").getValue());
				rd.set("renderer",scf.findField("renderer").getValue());
				rd.set("width",scf.findField("width").getValue()) ;
				rd.set("isHidden",(scf.findField("isHidden").getValue()=="on"||scf.findField("isHidden").getValue())?1:0); 
				rd.set("isOrder",(scf.findField("isOrder").getValue()=="on"||scf.findField("isOrder").getValue())?1:0);
				rd.set("isMultiUnit",(scf.findField("isMultiUnit").getValue()=="on"||scf.findField("isMultiUnit").getValue())?1:0);
				rd.set("isLink",(scf.findField("isLink").getValue()=="on"||scf.findField("isLink").getValue())?1:0);
				rd.set("linkParams", scf.findField("linkParams").getValue());
				rd.set("target", scf.findField("target").getValue());
				rd.set("linkTo", scf.findField("linkTo").getValue());
				rd.set("hideZero",(scf.findField("hideZero").getValue()=="on"||scf.findField("hideZero").getValue())?1:0),
				rd.set("align",scf.findField("align").getValue());
				rd.set("defaultHide",(scf.findField("defaultHide").getValue()=="on"||scf.findField("defaultHide").getValue())?1:0);
			}
			editColWin.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			editColWin.hide();
		}
	}]
});
var colPanel = new Ext.Panel({
	frame: false,
	layout:'border',
	border: false,
	items:[{	
        id:'north',
        region:'north', 
        layout:'fit',
        height:130,
        frame:false,
	    border:false,
		items: colForm
    },{  
		region:'center',  
		layout:'fit',  
		frame:false,
	    border:false,  
		autoScroll:true, //自动滚动条
		items: colsGrid  
	}]
});
function loadColPanel(){
	loadReportPart("columnDefine",function(info){
		var cf = colForm.getForm();
		cf.findField("isComplex").setValue(info.isComplex);
		if(info.sourceType==0){
			//加载列内容
			var cols = info.columns;
			cols_ds.removeAll();
			cols_ds.loadData(cols);
			Ext.getCmp("rd_colStatic").setValue(true);
			var tbtns=colsGrid.getTopToolbar().items;
			for(var i=0;i<tbtns.getCount();i++){
				tbtns.itemAt(i).enable();
			}
			Ext.getCmp("rd_colSql").setValue(false);
			Ext.getCmp("rd_colPro").setValue(false);
			Ext.getCmp("rd_colClass").setValue(false)
			Ext.getCmp("btn_colSql").disable();
			Ext.getCmp("btn_colPro").disable();
			colForm.getForm().findField("colClass").disable();
		}else if(info.sourceType==1){
			Ext.getCmp("rd_colStatic").setValue(false);
			var tbtns=colsGrid.getTopToolbar().items;
			for(var i=0;i<tbtns.getCount();i++){
				tbtns.itemAt(i).disable();
			}
			Ext.getCmp("rd_colSql").setValue(true);
			Ext.getCmp("rd_colPro").setValue(false);
			Ext.getCmp("rd_colClass").setValue(false)
			Ext.getCmp("btn_colSql").enable();
			Ext.getCmp("btn_colPro").disable();
			colForm.getForm().findField("colClass").disable();
		}else if(info.sourceType==2){
			Ext.getCmp("rd_colStatic").setValue(false);
			var tbtns=colsGrid.getTopToolbar().items;
			for(var i=0;i<tbtns.getCount();i++){
				tbtns.itemAt(i).disable();
			}
			Ext.getCmp("rd_colSql").setValue(false);
			Ext.getCmp("rd_colPro").setValue(true);
			Ext.getCmp("rd_colClass").setValue(false)
			Ext.getCmp("btn_colSql").disable();
			Ext.getCmp("btn_colPro").enable();
			colForm.getForm().findField("colClass").disable();
		}else if(info.sourceType==3){
			Ext.getCmp("rd_colStatic").setValue(false);
			var tbtns=colsGrid.getTopToolbar().items;
			for(var i=0;i<tbtns.getCount();i++){
				tbtns.itemAt(i).disable();
			}
			Ext.getCmp("rd_colSql").setValue(false);
			Ext.getCmp("rd_colPro").setValue(false);
			Ext.getCmp("rd_colClass").setValue(true)
			Ext.getCmp("btn_colSql").disable();
			Ext.getCmp("btn_colPro").disable();
			colForm.getForm().findField("colClass").enable();
		}
		cf.findField("colSql").setValue(info.sql);
		cf.findField("colPro").setValue(Ext.encode(info.procedure));
		cf.findField("colClass").setValue(info.implClass);
	}); 
}
function buildColumns(){
	var cf = colForm.getForm();
	var upinfo = new Object();
	var cdf = new Object();
	var complex=cf.findField("isComplex").getValue();
	if(complex=="on"||complex){
		cdf.complex =1;
	}else{
		cdf.complex =0;
	}
	var rd_sql = Ext.getCmp("rd_colSql").getValue();
	var rd_pro = Ext.getCmp("rd_colPro").getValue();
	var rd_class = Ext.getCmp("rd_colClass").getValue();
	var rd_static = Ext.getCmp("rd_colStatic").getValue();
	if(rd_static=="on"||rd_static){
		cdf.sourceType=0;
		var cols = new Array();
		for(var i=0;i<cols_ds.getCount();i++){
			var col = new Object();
			var rd = cols_ds.getAt(i);
			col.colId=rd.get("colId");
			col.colName=rd.get("colName");
			col.dataIndex=rd.get("dataIndex");
			col.dataType=rd.get("dataType");
			col.hideZero=rd.get("hideZero");
			col.isHidden=rd.get("isHidden");
			col.isleaf=rd.get("isleaf");
			col.isLink=rd.get("isLink");
			col.isMultiUnit=rd.get("isMultiUnit");
			col.isOrder=rd.get("isOrder");
			col.linkParams=rd.get("linkParams");
			col.linkTo=rd.get("linkTo");
			col.pid=rd.get("pid");
			col.renderer=rd.get("renderer");
			col.target=rd.get("target");
			col.width=rd.get("width");
			col.align=rd.get("align");
			col.defaultHide=rd.get("defaultHide");
			cols.push(col);
		}
		cdf.cols = cols;
	}else if(rd_sql=="on"||rd_sql){
		cdf.sourceType=1;
		cdf.sql = cf.findField("colSql").getValue();
	}else if(rd_pro=="on"||rd_pro){
		cdf.sourceType=2;
		cdf.procedure = cf.findField("colPro").getValue();
	}else if(rd_class=="on"||rd_class){
		cdf.sourceType=3;
		cdf.implClass=cf.findField("colClass").getValue();
	}
	upinfo.columnDefine=cdf;
	return upinfo;
}