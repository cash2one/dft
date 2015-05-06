var editor_dtSource=new Ext.form.ComboBox({
	fieldLabel:'取数方式',
	name: 'editor_dtSource',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[1, 'SQL'],
			[2, '存储过程'],
			[3, '扩展类']
		] 
	}),
	mode: 'local'
});
//其他数据源grid
var dts_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var dts_cm = new Ext.grid.ColumnModel({
	columns: [
	dts_sm,
	{
	    header: "数据源名称",
	    dataIndex: 'name',
	    menuDisabled:true,
	    width: 100,
	    align:'left',
	    editor : new Ext.form.TextField({
			selectOnFocus : true,
			maxLength : 200
		}),
	    renderer: renderFoo
	},{
		header: "取数方式",
	    dataIndex: 'sourceType',
	    width: 100,
	    align:'left',
	    editor : editor_dtSource,
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "SQL";
	    	}else if(v==2){
		    	return "存储过程";
	    	}else if(v==3){
		    	return "扩展类";
	    	}
	    	return "";
		}
	},{
		header: "详情",
	    width: 100,
	    align:'center',
 		css:"color:black;font-size:12px;",
 		renderer: function(v,p,r){
 			return "<input type='button' value='详情' onclick='showDtDetails(\""+r.get("name")+"\")' style='width:50px;height:20px;font-size:12px'>" ;    
 		}
	}],
	defaultSortable: false
});
var dts_Record = Ext.data.Record.create([                             
    {name: 'sourceType', type: 'int'},
    {name: 'sql', type: 'string'},
    {name: 'procedure', type: 'string'},
    {name: 'implClass', type: 'string'},
    {name: 'name', type: 'string'}
]);
var dts_ds = new Ext.data.Store({
    reader: new Ext.data.JsonReader({
    	 id:'name'
	},dts_Record)
});
var dtsGrid = new Ext.grid.EditorGridPanel({
	title:'',
	store: dts_ds,
    cm: dts_cm,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: dts_sm,
    clicksToEdit : 1,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加',
		iconCls: 'add',
        handler : function(){
			var cdt = new dts_Record({
				sourceType: 1,
				sql:'',
				procedure:'',
				implClass:'',
				name:''
	        });
			dtsGrid.stopEditing();
			dts_ds.insert(dts_ds.getCount(), cdt);
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = dtsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的数据源!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					dts_ds.getModifiedRecords().remove(records[rc]);
					dts_ds.remove(records[rc]);
				}
	        }
		}
	},{
		text: '设为默认',
		iconCls: 'viewMenu',
        handler : function(){
			var records = dtsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择数据源!");
				return;
			}	
        	//根据当前dtForm的内容构造成一条记录，把记录添加进来；
        	var st = 1;
	        var rd_sql = Ext.getCmp("rd_dfDtSql").getValue();
	    	var rd_pro = Ext.getCmp("rd_dfDtPro").getValue();
	    	var rd_class = Ext.getCmp("rd_dfDtClass").getValue();
	    	if(rd_sql=="on"||rd_sql){
	    		st=1;
	    	}else if(rd_pro=="on"||rd_pro){
	    		st=2;
	    	}else if(rd_class=="on"||rd_pro){
	    		st=3;
	    	}
	    	var df = dtForm.getForm();
        	var cdt = new dts_Record({
				sourceType: st,
				sql:df.findField("dfDtSql").getValue(),
				procedure:df.findField("dfDtPro").getValue(),
				implClass:df.findField("dfDtClass").getValue(),
				name:df.findField("dfDtName").getValue()
	        });
			//把当前记录填充到form，再删除当前记录。
	       	var rd =records[0];
			df.findField("dfDtSql").setValue(rd.get("sql"));
			df.findField("dfDtPro").setValue(rd.get("procedure"));
			df.findField("dfDtClass").setValue(rd.get("implClass"));
			df.findField("dfDtName").setValue(rd.get("name"));
			df.findField("dfCanPaging").setValue(0);
			df.findField("dfDefaultPageSize").setValue(40);
			df.findField("dfMaxSize").setValue(100);
			df.findField("dfDefaultPageSize").disable();
			df.findField("dfMaxSize").disable();
			var st = rd.get("sourceType");
			if(st==1){
				Ext.getCmp("rd_dfDtSql").setValue(true);
				Ext.getCmp("rd_dfDtPro").setValue(false);
				Ext.getCmp("rd_dfDtClass").setValue(false);
				Ext.getCmp("btn_dfDtSql").enable();
				Ext.getCmp("btn_dfDtPro").disable();
				dtForm.getForm().findField("dfDtClass").disable();
			}else if(st==2){
				Ext.getCmp("rd_dfDtSql").setValue(false);
				Ext.getCmp("rd_dfDtPro").setValue(true); 
				Ext.getCmp("rd_dfDtClass").setValue(false);
				Ext.getCmp("btn_dfDtSql").disable();
				Ext.getCmp("btn_dfDtPro").enable();
				dtForm.getForm().findField("dfDtClass").disable();
			}else if(st==3){
				Ext.getCmp("rd_dfDtSql").setValue(false);
				Ext.getCmp("rd_dfDtPro").setValue(false); 
				Ext.getCmp("rd_dfDtClass").setValue(true);
				Ext.getCmp("btn_dfDtSql").disable();
				Ext.getCmp("btn_dfDtPro").disable();
				dtForm.getForm().findField("dfDtClass").enable();
			}
			dts_ds.remove(rd);
			dtsGrid.stopEditing();
			dts_ds.insert(dts_ds.getCount(), cdt);
			//将当前dtsGrid内容组织成otherDts
			encodeOtherDts();
		}
	}]
});
function encodeOtherDts(){
	var otherDts = new Array();
	for(var i=0;i<dts_ds.getCount();i++){
		var tmprd = dts_ds.getAt(i);
		var tmpdt = new Object();
		tmpdt.name=tmprd.get("name");
		tmpdt.sourceType=tmprd.get("sourceType");
		tmpdt.sql=tmprd.get("sql");
		tmpdt.procedure=tmprd.get("procedure");
		tmpdt.implClass=tmprd.get("implClass");
		otherDts.push(tmpdt);
	}
	var dataSets = Ext.encode(otherDts);
	dtForm.getForm().findField("otherDts").setValue(dataSets);
}
var otherDtsWin = new Ext.Window({
    title : '其他数据源',
    width : 380,
    height : 300,
    layout : 'fit',
    items : [dtsGrid],
    closeAction:'hide',
    buttons : [{
        text:'确定',
        handler: function(){
        	//把grid中的行encode，放入dtForm的otherDts字段
        	encodeOtherDts();
        	otherDtsWin.hide();
    	}
    },{
        text:'关闭',
        handler: function(){
        	otherDtsWin.hide();
    	}
    }]
});
otherDtsWin.on("show",function(){
	//将otherDtsWin字段的内容解析到grid中
	var otherDts = dtForm.getForm().findField("otherDts").getValue();
	var dts = Ext.decode(otherDts);
	if(dts){
		dts_ds.removeAll();
		dts_ds.loadData(dts);
	}
});
function showDtDetails(dtName){
	var rd = dts_ds.getById(dtName);
	if(!rd){
		var records = dtsGrid.getSelectionModel().getSelections();
		if(records){
			rd = records[0];
		}
	}
	var st = rd?rd.get("sourceType"):1;
	if(st==1){
		var sv = rd?rd.get("sql"):"";
		sqlWindow.setSql(sv);
		sqlWindow.onHideSetValue=function(v){
			if(rd){
				rd.set('sql',v);
			}
		}
		sqlWindow.show();
	}else if(st==2){
		var sv = rd?rd.get("procedure"):"";
		procedureWindow.setProInfo(sv);
		procedureWindow.onHideSetValue=function(v){
			if(rd){
				rd.set('procedure',v);
			}
		}
		procedureWindow.show();
	}else if(st==3){
		var sv = rd?rd.get("implClass"):"";
		implClassWindow.setClsPath(sv);
		implClassWindow.onHideSetValue=function(v){
			if(rd){
				rd.set('implClass',v);
			}
		}
		implClassWindow.show();
	}
}
var dtForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 70,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
	layout : 'form',
	items:[{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .52,
			layout : 'form',
			labelWidth: 110,
			items : [{
				fieldLabel: '记录集名称',
				xtype: 'textfield',
				name: 'dfDtName',
				width: 120
			}]
		},{
			columnWidth : .48,
			layout : 'form',
			labelWidth: 90,
			items : [{
				text: '其他数据源',
				name : 'btn_otherDts',
				id:'btn_otherDts',
                xtype: 'button',
				handler : function(){
				 	otherDtsWin.show();
				}
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '分页',
        layout:'form',
        items: [{
    		hideLabel: true,
    		boxLabel: '可分页',
    		xtype: 'checkbox',
    		name: 'dfCanPaging',
    		width: 100,
    		checked : false,
    		listeners :{
    			check: function(ckbox,checked){
    				var df = dtForm.getForm();
    				if(checked){
    					df.findField("dfDefaultPageSize").enable();
    					df.findField("dfMaxSize").enable();
    				}else{
    					df.findField("dfDefaultPageSize").disable();
    					df.findField("dfMaxSize").disable();
    				}
        		}
        	}
    	},{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 100,
				items : [{
					fieldLabel: '默认每页记录数',
					xtype: 'numberfield',
					name: 'dfDefaultPageSize',
					disabled :true,
					width: 120
				}]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 100,
				items : [{
					fieldLabel: '最大每页记录数',
					xtype: 'numberfield',
					name: 'dfMaxSize',
					disabled :true,
					width: 120
				}]
			}]
        }]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '数据源',
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
					boxLabel: 'SQL取数',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_dfDtSql',  
					width: 120,
					checked : true,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_dfDtSql").enable();
								Ext.getCmp("btn_dfDtPro").disable();
								dtForm.getForm().findField("dfDtClass").disable();
							}else{
								Ext.getCmp("btn_dfDtSql").disable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'dfDtSql' 
				}]
			},{
				columnWidth : .32,
				layout : 'form',
				items : [{
					name : 'btn_dfDtSql',
					id:'btn_dfDtSql',
	                xtype: 'button',
	                text: '详情...',
	                handler: function(){
	                	var sv = dtForm.getForm().findField("dfDtSql").getValue();
						sqlWindow.setSql(sv);
						sqlWindow.onHideSetValue=function(v){
							dtForm.getForm().findField("dfDtSql").setValue(v);
						}
						sqlWindow.show();
					}
				}]
			},{
				columnWidth : .21,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: '存储过程取数',
					xtype: 'radio',
					name: 'sourceType',
					id: 'rd_dfDtPro',
					width: 120,
					checked : false,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_dfDtPro").enable();
								Ext.getCmp("btn_dfDtSql").disable();
								dtForm.getForm().findField("dfDtClass").disable();
							}else{
								Ext.getCmp("btn_dfDtPro").disable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'dfDtPro' 
				}]
			},{
				columnWidth : .26,
				layout : 'form',
				items : [{
					name : 'btn_dfDtPro',
					id:'btn_dfDtPro',
		            xtype: 'button',
		            text: '详情...',
		            handler: function(){
						procedureWindow.setProInfo(dtForm.getForm().findField("dfDtPro").getValue());
						procedureWindow.onHideSetValue=function(v){
							dtForm.getForm().findField("dfDtPro").setValue(v);
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
					id: 'rd_dfDtClass',
					width: 180,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_dfDtPro").disable();
								Ext.getCmp("btn_dfDtSql").disable();
								dtForm.getForm().findField("dfDtClass").enable();
							}else{
								dtForm.getForm().findField("dfDtClass").disable();
							}
						}
					}
				}]
			},{
				columnWidth : .75,
				layout : 'form',
				items : [{
					hideLabel: true,
					xtype: 'textfield',
					name: 'dfDtClass',
					width: 300
				}]
			}]
		}]
	},{
		xtype: 'hidden',
		name: 'otherDts' 
	}]
});
function loadDtForm(){
	loadReportPart("dataSets",function(info){
		var df = dtForm.getForm();
		var dfdt = info.defaultDt;
		if(dfdt){
			df.findField("dfDtName").setValue(dfdt.name);
			df.findField("dfCanPaging").setValue(dfdt.canPaging);
			df.findField("dfDefaultPageSize").setValue(dfdt.defaultPageSize);
			df.findField("dfMaxSize").setValue(dfdt.maxSize);
			if(dfdt.canPaging==1){
				df.findField("dfDefaultPageSize").enable();
				df.findField("dfMaxSize").enable();
			}else{
				df.findField("dfDefaultPageSize").disable();
				df.findField("dfMaxSize").disable();
			}
			if(dfdt.sourceType==1){
				Ext.getCmp("rd_dfDtSql").setValue(true);
				Ext.getCmp("rd_dfDtPro").setValue(false);
				Ext.getCmp("rd_dfDtClass").setValue(false);
				Ext.getCmp("btn_dfDtSql").enable();
				Ext.getCmp("btn_dfDtPro").disable();
				dtForm.getForm().findField("dfDtClass").disable();
			}else if(dfdt.sourceType==2){
				Ext.getCmp("rd_dfDtSql").setValue(false);
				Ext.getCmp("rd_dfDtPro").setValue(true); 
				Ext.getCmp("rd_dfDtClass").setValue(false);
				Ext.getCmp("btn_dfDtSql").disable();
				Ext.getCmp("btn_dfDtPro").enable();
				dtForm.getForm().findField("dfDtClass").disable();
			}else if(dfdt.sourceType==3){
				Ext.getCmp("rd_dfDtSql").setValue(false);
				Ext.getCmp("rd_dfDtPro").setValue(false); 
				Ext.getCmp("rd_dfDtClass").setValue(true);
				Ext.getCmp("btn_dfDtSql").disable();
				Ext.getCmp("btn_dfDtPro").disable();
				dtForm.getForm().findField("dfDtClass").enable();
			}
			df.findField("dfDtSql").setValue(dfdt.sql);
			df.findField("dfDtPro").setValue(Ext.encode(dfdt.procedure));
			df.findField("dfDtClass").setValue(dfdt.implClass);
		}
		var dataSets = Ext.encode(info.dataSets);
		df.findField("otherDts").setValue(dataSets);
	}); 
}
function buildDataSets(){
	var df = dtForm.getForm();
	var dfdt = new Object();
	var upinfo = new Object();
	dfdt.name=df.findField("dfDtName").getValue();
	var cp =df.findField("dfCanPaging").getValue();
	dfdt.defaultPageSize=df.findField("dfDefaultPageSize").getValue();
	dfdt.maxSize=df.findField("dfMaxSize").getValue();
	if(cp=="on"||cp){
		dfdt.canPaging = 1;
	}else{
		dfdt.canPaging = 0;
	}
	var rd_sql = Ext.getCmp("rd_dfDtSql").getValue();
	var rd_pro = Ext.getCmp("rd_dfDtPro").getValue();
	var rd_class = Ext.getCmp("rd_dfDtClass").getValue();
	if(rd_sql=="on"||rd_sql){
		dfdt.sourceType=1;
		dfdt.sql = df.findField("dfDtSql").getValue();
	}else if(rd_pro=="on"||rd_pro){
		dfdt.sourceType=2;
		dfdt.procedure = df.findField("dfDtPro").getValue();
	}else if(rd_class=="on"||rd_class){
		dfdt.sourceType=3;
		dfdt.implClass=df.findField("dfDtClass").getValue();
	}
	upinfo.defaultDt=dfdt;
	upinfo.dataSets=df.findField("otherDts").getValue();
	return upinfo;
}