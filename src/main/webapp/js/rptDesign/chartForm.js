var chartForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 70,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
	layout : 'form',
	items:[{
		hideLabel: true,
		boxLabel: '支持图表',
		xtype: 'checkbox',
		name: 'hasChart',
		width: 100,
		checked : false,
		listeners :{
			check: function(ckbox,checked){
				var cf = chartForm.getForm();
				if(checked){
					cf.findField("chartId").enable();
					cf.findField("chartDataTmplate").enable();
					cf.findField("chartHeight").enable();
					cf.findField("chartType").enable();
					cf.findField("chartDataIndex").enable();
					cf.findField("chartWidth").enable();
					cf.findField("isMultiSeries").enable();
					if(cf.findField("isMultiSeries").getValue()=="on"||cf.findField("isMultiSeries").getValue()){
						cf.findField("categoryIndex").enable();
						cf.findField("seriesIndex").enable();
					}else{
						cf.findField("categoryIndex").disable();
						cf.findField("seriesIndex").disable();
					}
					Ext.getCmp("chartDtSql").enable();
					Ext.getCmp("chartDtPro").enable();
					if(Ext.getCmp("chartDtSql").getValue()=="on"||Ext.getCmp("chartDtSql").getValue()){
						Ext.getCmp("btn_chartSql").enable();
						Ext.getCmp("btn_chartPro").disable();
					}else{
						Ext.getCmp("btn_chartSql").disable();
						Ext.getCmp("btn_chartPro").enable();
					}
				}else{
					cf.findField("chartId").disable();
					cf.findField("chartDataTmplate").disable();
					cf.findField("chartHeight").disable();
					cf.findField("chartType").disable();
					cf.findField("chartDataIndex").disable();
					cf.findField("chartWidth").disable();
					cf.findField("isMultiSeries").disable();
					cf.findField("categoryIndex").disable();
					cf.findField("seriesIndex").disable();
					Ext.getCmp("chartDtSql").disable();
					Ext.getCmp("chartDtPro").disable();
					Ext.getCmp("btn_chartPro").disable();
					Ext.getCmp("btn_chartSql").disable();
				}
			}
		}
	},{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .33,
			layout : 'form',
			items : [{
				fieldLabel: '图表ID',
				xtype: 'textfield',
				name: 'chartId',
				disabled :true,
				width: 100
			},{
				fieldLabel: '模板文件',
				xtype: 'textfield',
				name: 'chartDataTmplate',
				disabled :true,
				width: 100
			}]
		},{
			columnWidth : .33,
			layout : 'form',
			items : [{
				fieldLabel: '图表高度',
				xtype: 'textfield',
				name: 'chartHeight',
				disabled :true,
				width: 100
			},{
				fieldLabel: '图表类型',
				xtype: 'textfield',
				name: 'chartType',
				disabled :true,
				width: 100
			}]
		},{
			columnWidth : .33,
			layout : 'form',
			items : [{
				fieldLabel: '图表宽度',
				xtype: 'textfield',
				disabled :true,
				name: 'chartWidth',
				width: 100
			},{
				fieldLabel: '数据字段',
				xtype: 'textfield',
				name: 'chartDataIndex',
				disabled :true,
				width: 100
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '多指标',
        layout:'form',
        items: [{
    		hideLabel: true,
    		boxLabel: '多指标图表',
    		xtype: 'checkbox',
    		name: 'isMultiSeries',
    		width: 100,
    		checked : false,
    		listeners :{
    			check: function(ckbox,checked){
    				var cf = chartForm.getForm();
    				if(checked){
    					cf.findField("categoryIndex").enable();
    					cf.findField("seriesIndex").enable();
    				}else{
    					cf.findField("categoryIndex").disable();
    					cf.findField("seriesIndex").disable();
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
					fieldLabel: 'category指标字段',
					xtype: 'textfield',
					name: 'categoryIndex',
					disabled :true,
					width: 120
				}]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 100,
				items : [{
					fieldLabel: 'series指标字段',
					xtype: 'textfield',
					name: 'seriesIndex',
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
				columnWidth : .15,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: 'SQL取数',
					xtype: 'radio',
					name: 'sourceType',
					id: 'chartDtSql',
					width: 120,
					disabled :true,
					checked : true,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_chartSql").enable();
								Ext.getCmp("btn_chartPro").disable();
							}else{
								Ext.getCmp("btn_chartSql").disable();
								Ext.getCmp("btn_chartPro").enable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'chartSql' 
				},{
					xtype: 'hidden',
					name: 'dataFormat' ,
					value: 0
				}]
			},{
				columnWidth : .35,
				layout : 'form',
				items : [{
					name : 'btn_chartSql',
					id:'btn_chartSql',
	                xtype: 'button',
	                text: '详情...',
	                disabled : true,
	                handler: function(){
	                	var sv = chartForm.getForm().findField("chartSql").getValue();
						sqlWindow.setSql(sv);
						sqlWindow.onHideSetValue=function(v){
							chartForm.getForm().findField("chartSql").setValue(v);
						}
						sqlWindow.show();
					}
				}]
			},{
				columnWidth : .2,
				layout : 'form',
				items : [{
		        	hideLabel: true,
					boxLabel: '存储过程取数',
					xtype: 'radio',
					name: 'sourceType',
					id: 'chartDtPro',
					width: 120,
					checked : false,
					disabled :true,
					listeners :{
						check: function(ckbox,checked){
							if(checked){
								Ext.getCmp("btn_chartPro").enable();
								Ext.getCmp("btn_chartSql").disable();
							}else{
								Ext.getCmp("btn_chartPro").disable();
								Ext.getCmp("btn_chartSql").enable();
							}
						}
					}
				},{
					xtype: 'hidden',
					name: 'chartProcedure' 
				}]
			},{
				columnWidth : .2,
				layout : 'form',
				items : [{
					name : 'btn_chartPro',
					id:'btn_chartPro',
		            xtype: 'button',
		            text: '详情...',
		            disabled : true,
		            handler: function(){
						procedureWindow.setProInfo(chartForm.getForm().findField("chartProcedure").getValue());
						procedureWindow.onHideSetValue=function(v){
							chartForm.getForm().findField("chartProcedure").setValue(v);
						}
						procedureWindow.show();
					}
				}]
			}]
		}]
	}]
});
function loadChartForm(){
	loadReportPart("chart",function(info){
		var cf = chartForm.getForm();
		cf.findField("hasChart").setValue(info.hasChart);
		cf.findField("chartId").setValue(info.id);
		cf.findField("chartDataTmplate").setValue(info.dataTemplateName);
		cf.findField("chartHeight").setValue(info.height);
		cf.findField("chartType").setValue(info.chartType);
		cf.findField("chartDataIndex").setValue(info.dataIndex);
		cf.findField("chartWidth").setValue(info.width);
		cf.findField("isMultiSeries").setValue(info.isMultiSeries);
		cf.findField("categoryIndex").setValue(info.categoryIndex);
		cf.findField("seriesIndex").setValue(info.seriesIndex);
		if(info.hasChart==1){
			cf.findField("chartId").enable();
			cf.findField("chartDataTmplate").enable();
			cf.findField("chartHeight").enable();
			cf.findField("chartType").enable();
			cf.findField("chartDataIndex").enable();
			cf.findField("chartWidth").enable();
			cf.findField("isMultiSeries").enable();
			if(info.isMultiSeries==1){
				cf.findField("categoryIndex").enable();
				cf.findField("seriesIndex").enable();
			}else{
				cf.findField("categoryIndex").disable();
				cf.findField("seriesIndex").disable();
			}
			Ext.getCmp("chartDtSql").enable();
			Ext.getCmp("chartDtPro").enable();
		}else{
			cf.findField("chartId").disable();
			cf.findField("chartDataTmplate").disable();
			cf.findField("chartHeight").disable();
			cf.findField("chartType").disable();
			cf.findField("chartDataIndex").disable();
			cf.findField("chartWidth").disable();
			cf.findField("isMultiSeries").disable();
			cf.findField("categoryIndex").disable();
			cf.findField("seriesIndex").disable();
			Ext.getCmp("chartDtSql").disable();
			Ext.getCmp("chartDtPro").disable();
		}
		if(info.sourceType==1){
			Ext.getCmp("chartDtSql").setValue(true);
			Ext.getCmp("chartDtPro").setValue(false);
			Ext.getCmp("btn_chartSql").enable();
			Ext.getCmp("btn_chartPro").disable();
		}else if(info.sourceType==2){
			Ext.getCmp("chartDtSql").setValue(false);
			Ext.getCmp("chartDtPro").setValue(true);
			Ext.getCmp("btn_chartSql").disable();
			Ext.getCmp("btn_chartPro").enable();
		}
		cf.findField("chartSql").setValue(info.sql);
		cf.findField("chartProcedure").setValue(Ext.encode(info.procedure));
	}); 
}
function buildChart(){
	var cf = chartForm.getForm();
	var chart = new Object();
	var upinfo = new Object();
	var haschart = cf.findField("hasChart").getValue();
	if(haschart=="on"||haschart==true){
		upinfo.hasChart=true;
		var chart = new Object();
		chart.id = cf.findField("chartId").getValue();
		chart.dataTemplateName = cf.findField("chartDataTmplate").getValue();
		chart.height = cf.findField("chartHeight").getValue();
		chart.chartType=cf.findField("chartType").getValue();
		chart.dataIndex=cf.findField("chartDataIndex").getValue();
		chart.width=cf.findField("chartWidth").getValue();
		var isMultiSeries = cf.findField("isMultiSeries").getValue();
		if(isMultiSeries=="on"||isMultiSeries==true){
			chart.isMultiSeries=1;
		}else{
			chart.isMultiSeries=0;
		}
		chart.categoryIndex=cf.findField("categoryIndex").getValue();
		chart.seriesIndex=cf.findField("seriesIndex").getValue();
		chart.dataFormat=cf.findField("dataFormat").getValue();
		if(Ext.getCmp("chartDtSql").getValue()=="on"||Ext.getCmp("chartDtSql").getValue()){
			chart.sourceType=1;
		}else{
			chart.sourceType=2;
		}
		chart.sql=cf.findField("chartSql").getValue();
		chart.procedure=cf.findField("chartProcedure").getValue();
		upinfo.chart = chart;
	}else{
		upinfo.hasChart=false;
	}
	return upinfo;
}