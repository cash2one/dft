<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*"%>
<%@ page import="org.apache.commons.lang.*"%>
<%@ page import="com.fruit.query.data.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.service.*"%>
<%@ page import="com.fruit.query.util.*"%>
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<title>查询——参数</title>
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Report rpt=(Report)request.getAttribute("queryReport");
	if(rpt==null){
		out.print("</head><body>");
		out.print("<p>未找到相应的报表定义！</p>");
		out.print("</body></html>");
		return;
	}
	List paras=rpt.getParas();
	//如果报表无参数定义，直接跳转到报表输出。
	if(paras==null||paras.size()==0){%>
		<jsp:forward page="doQuery.query">
			<jsp:param name="doType" value="getReport"/>
			<jsp:param name="rptID" value="<%=rpt.getId()%>"/>
		</jsp:forward>
	<%}
	//检查是否需要用户交互，非隐藏参数需要用户交互。
	boolean userInput=false;
	for(int i=0;i<paras.size();i++){
		if(((Parameter)paras.get(i)).getIsHidden()==0){
			userInput=true;
			break;
		}
	}
	//如果没有“显式”参数，也可以直接跳转到报表解析、输出
	if(!userInput){%>
	<jsp:forward page="doQuery.query">
		<jsp:param name="doType" value="getReport"/>
		<jsp:param name="rptID" value="<%=rpt.getId()%>"/>
	</jsp:forward>	
  <%}
	int top=5;
	int left=0;
	int pCount=0;
	//显示的要交互的控件项目
	int showCount=0;
	for(int i=0;i<paras.size();i++){
		Parameter cp=(Parameter)paras.get(i);
		if(cp!=null&&cp.getIsHidden()==0){
			showCount++;
		}
	}
	if(rpt.getDefaultDataDef().getCanPaging()==1){
		pCount=1;
		showCount++;
	}
	String defaultValue="";
	String defaultText="";
	//有一些参数隐藏，预先有值，可以被构造参数选项时引用。
	Map paVals=new HashMap();
	for(int k=0;k<rpt.getParas().size();k++){
		Parameter tmpPa=(Parameter)rpt.getParas().get(k);
		if(tmpPa.getIsHidden()==1){
			String sDesc="",sVal="";
			if(tmpPa.getBindMode()==0){
				sDesc=sVal=tmpPa.getBindTo();
			}else if(tmpPa.getBindMode()==1){
				sVal=request.getParameter(tmpPa.getBindTo());
				sDesc=request.getParameter(tmpPa.getBindTo()+"_desc");
			}else if(tmpPa.getBindMode()==2){
				sDesc=sVal=(String)request.getSession().getAttribute(tmpPa.getBindTo());
			}else{
				String path=tmpPa.getBindTo();
				try{
					IParaDataBind pdGetInstance=(IParaDataBind)Class.forName(path).newInstance();
					ParaValue tpv=pdGetInstance.getParaValue(request, rpt, tmpPa);
					if(tpv!=null){
						sVal=tpv.getValue();
						sDesc=tpv.getDesc();
					}
				}catch(Exception e){
					System.out.println("未能正确加载报表取值类!错误信息:"+path+e.toString());
				}
			}
			ParaValue pv=new ParaValue(sVal,sDesc);
			paVals.put(tmpPa.getName(),pv);
		}
	}
	Map opItemsMap=new HashMap();
	//先获取所有下拉框或树参数的选项集合。按参数名称索引对应的选项集合
	for(int i=0;i<paras.size();i++){
		Parameter para=(Parameter)paras.get(i);
		//如果是联动参数中的被动者，先不取数。
		if(para.getAffectedByParas()!=null&&!"".equals(para.getAffectedByParas())){
			continue;	
		}
    	//if(para==null||para.getIsHidden()==1||para.getRenderType()==0||para.getRenderType()==3){continue;}
		if(para==null||para.getIsHidden()==1){continue;}
    	List items=null;
    	if(para.getSourceType()==0){
			items=para.getParaOptions();
		}else{
			items=ParaOptionsService.getParaOptionsService().getOptions(rpt,para,paVals);
		}
    	opItemsMap.put(para.getName(),items);
	}
	session.setAttribute("opItemsMap",opItemsMap);
	Map dfValMap=new HashMap();
	Map dfDesMap=new HashMap();
%>
<!-- Common Js and Styles for the dataFill -->  
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>	
<script>
/*
 * Ext JS Library 2.0.2
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * http://extjs.com/license
 */
 Ext.BLANK_IMAGE_URL = 'libs/ext-3.4.0/resources/images/default/s.gif';
    
    //根据参数渲染类型，动态构造的控件变量
    <%for(int i=0;i<paras.size();i++){
    	Parameter para=(Parameter)paras.get(i);
    	defaultValue="";
    	defaultText="";
    	if(para==null||para.getIsHidden()==1){continue;}
    	//如果是下拉框
    	if(para.getRenderType()==1){
    		List choices=(List)opItemsMap.get(para.getName());
			//既定的默认值规则(first)优先于普通动态规则或者选项中自定的isdefault
			if(choices!=null){
				if(!StringUtils.isEmpty(para.getDefaultRule())){
					String dr = para.getDefaultRule();
					if("_first".equals(dr)){
						OptionItem oi=(OptionItem)choices.get(0);
						defaultValue=oi.getBm();
						defaultText=oi.getName();
					}
				}else {
					if(para.getDefaultRuleDefine()!=null){
						try{
							OptionItem oi = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,para,paVals);
							defaultValue=oi.getBm();
							defaultText=oi.getName();
						}catch(Exception e){
						}
					}else{
						for(int j=0;j<choices.size();j++){
							OptionItem oi=(OptionItem)choices.get(j);
							if(oi.getIsDefault()>0){
								defaultValue=oi.getBm();
								defaultText=oi.getName();
							}
						}
					}
				}
				dfDesMap.put(para.getName(),defaultText);
			}%>
    		var <%=para.getName()%>_combo=new Ext.form.ComboBox({
				x: <%=left+70%>,
		        y: <%=(top-5)+pCount*30%>,
			    //xtype: 'combo',
			    displayField:'text',
				valueField:'value',
				width: <%=para.getWidth()%>,
			    //id:'<%=para.getName()%>_drop',
			    name: '<%=para.getName()%>_drop',
			    mode : 'local', 
				editable: false, 
				triggerAction : 'all',
				emptyText:'请选择...',
				allowBlank:true,
				width: <%=para.getWidth()%>,
				hiddenId:'<%=para.getName()%>',
				hiddenName:'<%=para.getName()%>',
				value:'<%=defaultValue%>',
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [
					<%
						for(int j=0;j<choices.size();j++){
							 OptionItem oi=(OptionItem)choices.get(j);%>
						['<%=oi.getBm()%>', '<%=oi.getName()%>']
							<%if(j<choices.size()-1){out.print(",");}%> 
					  <%}%>
					]
			    })
			});
			//设置隐含的、选项对应的中文值
			<%=para.getName()%>_combo.on("select",function(combo,record){
				var descFld = Ext.getCmp('<%=para.getName()%>_desc');
				descFld.setValue(record.get("text")); 
				<%if(para.getAffectCallBack()!=null&&!"".equals(para.getAffectCallBack())){%>
					<%=para.getAffectCallBack()%>(record.get("value"));
				<%}%>
			});
      <%}
    	if(para.getRenderType()==2){
    		List choices=(List)opItemsMap.get(para.getName());
    		int dfCount = 0;
			if(choices!=null){
				if(!StringUtils.isEmpty(para.getDefaultRule())){
					String dr = para.getDefaultRule();
					if("_first".equals(dr)){
						OptionItem oi=(OptionItem)choices.get(0);
						defaultValue=oi.getBm();
						defaultText=oi.getName();
						dfCount++;
					}
				}else{
					if(para.getDefaultRuleDefine()!=null){
						try{
							OptionItem oi = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,para,paVals);
							defaultValue=oi.getBm();
							defaultText=oi.getName();
							dfCount++;
						}catch(Exception e){
						}
					}else{
						for(int j=0;j<choices.size();j++){
							OptionItem oi=(OptionItem)choices.get(j);
							if(para.getIsMulti()==0){
								if(oi.getIsDefault()>0){
									defaultValue=oi.getBm();
									defaultText=oi.getName();
									dfCount++;
								}
							}else{
								if(oi.getIsDefault()>0){
									defaultValue+=oi.getBm()+",";
									defaultText+=oi.getName()+",";
									dfCount++;
								}
							}
						}
					}
				}
				if(para.getIsMulti()>0&&defaultValue.length()>0&&dfCount>1){
					defaultValue=defaultValue.substring(0,defaultValue.length()-1);
				}
				dfValMap.put(para.getName(),defaultValue);
				if(para.getIsMulti()>0&&defaultText.length()>0&&dfCount>1){
					defaultText=defaultText.substring(0,defaultText.length()-1);
				}			
			}%>
      		var <%=para.getName()%>_root=new Ext.tree.AsyncTreeNode({
          		id:'<%=para.getName()%>_root',    
			    text : '<%=para.getDesc()%>选项',
	        	draggable : false,
	        	uiProvider:Ext.tree.TreeCheckNodeUI
			}); 
			         
			var <%=para.getName()%>_tree=new Ext.tree.TreePanel({    
			    frame:false,  
			    //id: '<%=para.getName()%>_tree',       
			    root:<%=para.getName()%>_root, 
			    checkModel: '<%=para.getIsMulti()==1?"multiple":(para.getIsMulti()==2?"cascade":"single")%>',   //树节点是否多选   
		   		onlyLeafCheckable: <%=para.getLeafOnly()==1?"true":"false"%>,//对树所有结点都可选   
			    animate:false,    
			    enableDD:false,    
			    border:false,    
			    rootVisible:true,    
			    autoScroll:true,
			    loader : new Ext.tree.TreeLoader({
		            dataUrl : 'getData.query?doType=getOptions&rptID=<%=rpt.getId()%>&paraName=<%=para.getName()%>',
		            baseParams : {},
		            baseAttrs: { uiProvider: Ext.tree.TreeCheckNodeUI } //添加 uiProvider 属性   
		        })   
			});  
			//节点的开合
		    <%=para.getName()%>_tree.on('click',function(node){ 
		    	if(!node.isLeaf()){ 
		    		node.toggle(); 
		    	} 
		    });
		    //注册check事件   	   
			<%=para.getName()%>_tree.on("check",function(node,checked){
				//记录选中节点
			}); 
			//加载前把右边框复位
			<%=para.getName()%>_tree.on("beforeload",function(node){
				var hVal = Ext.getCmp('<%=para.getName()%>');
				hVal.setValue(""); 
				var hText = Ext.getCmp('<%=para.getName()%>_desc');
				hText.setValue(""); 
			}); 
			var <%=para.getName()%>_wc = {
		        title : '<%=para.getDesc()%>选择',
		        width : 375,
		        height : 350,
		        autoScroll : true,
		        bodyStyle : 'background:white;padding:5px;',
		        layout : 'fit',
		        items : [<%=para.getName()%>_tree],
		        buttons : [{
		            		text : "确定",
		            		handler : function() {	
			            		var chosenNodes = <%=para.getName()%>_tree;
								var hVal = Ext.getCmp('<%=para.getName()%>');
								var hText = Ext.getCmp('<%=para.getName()%>_desc');
								hVal.setValue(chosenNodes.getChecked('id')); 
								hText.setValue(chosenNodes.getChecked('text')); 
								<%if(para.getAffectCallBack()!=null&&!"".equals(para.getAffectCallBack())){%>
									<%=para.getAffectCallBack()%>(chosenNodes.getChecked('id'));
								<%}%>	            		
			                	<%=para.getName()%>_win.hide();
		            		}
		        	},{
		            	text : "置空",
		            	handler : function() {
			        		var hVal = Ext.getCmp('<%=para.getName()%>');
							var hText = Ext.getCmp('<%=para.getName()%>_desc');
							hVal.setValue(""); 
							hText.setValue("");
							<%if(para.getAffectCallBack()!=null&&!"".equals(para.getAffectCallBack())){%>
								<%=para.getAffectCallBack()%>("");
							<%}%>
							var cnodes = <%=para.getName()%>_tree.getChecked();
							if(cnodes){
								for(var i=0;i<cnodes.length;i++){
									var cn = cnodes[i];
									cn.getUI().toggleCheck(false);  
								}
							}
		                	<%=para.getName()%>_win.hide();
		                }
		            },{
		            	text : "取消",
		            	handler : function() {
		                	<%=para.getName()%>_win.hide();
		                }
		            }
		        ]
	    	};
        	var <%=para.getName()%>_win = new Ext.Window(<%=para.getName()%>_wc);
        	var <%=para.getName()%>_tg = new Ext.form.TriggerField({
        		x: <%=left+70%>,
		        y: <%=(top-5)+pCount*30%>,
		        width: <%=para.getWidth()%>,
	            editable :false,
	            width: <%=para.getWidth()%>,
		        name: '<%=para.getName()%>_desc',
		        value:'<%=defaultText%>',
		        id: '<%=para.getName()%>_desc'
        	});
			<%=para.getName()%>_tg.onTriggerClick=<%=para.getName()%>_trigFun;
			function <%=para.getName()%>_trigFun(e){
				if(Ext.getCmp("<%=para.getName()%>_desc").disabled){
					return;
				}
				<%=para.getName()%>_win.show();
			}
      <%}
    	pCount++;
    }%>
    //参数form
    var cForm = new Ext.FormPanel({
	        id: 'qForm',
	        frame: true,
	        labelAlign: 'right',
	        bodyStyle:'padding:5px',
	        width: 400,
	        height:400,
	        layout: 'absolute', 
	        items: [
	        <%if(rpt.getDefaultDataDef().getCanPaging()==1){%>
	        	{
		            x: <%=left%>,
		            y: <%=top%>,
		            xtype:'label',
		            text: '每页记录数:'
		        },{
		            x: <%=left+70%>,
		            y: <%=top-5%>,
		            width: 60,
		            xtype: 'textfield',
		            name: 'pageSize',
		            value: '<%=rpt.getDefaultDataDef().getDefaultPageSize()%>',
		            id:'pageSize'
		        },
		    <%	pCount=1;
		    }else{
		    	pCount=0;
		    }%>
	        <%for(int i=0;i<paras.size();i++){
	        	Parameter pa=(Parameter)paras.get(i);
	        	if(pa==null||pa.getIsHidden()==1){
	        		continue;
	        	}
	        	int rType=pa.getRenderType();
	        %>
		        {
		            x: <%=left%>,
		            y: <%=top+pCount*30%>,
		            xtype:'label',
		            text: '<%=pa.getDesc()%>:'
		        },
		        <%if(rType==0){
		        	String pdfVal="";
		        	if(pa.getDefaultValue()!=null&&!"".equals(pa.getDefaultValue())){
		        		pdfVal = pa.getDefaultValue();
		        	}else if(pa.getDefaultValueBindTo()!=null&&!"".equals(pa.getDefaultValueBindTo())){
		        		int dvmode = pa.getDefaultValBindMode();
		        		if(dvmode==0){
		        			pdfVal=pa.getDefaultValueBindTo();
						}else if(dvmode==1){
							pdfVal=request.getParameter(pa.getDefaultValueBindTo());
						}else if(dvmode==2){
							pdfVal=(String)request.getSession().getAttribute(pa.getDefaultValueBindTo());
						}else{
							String path=pa.getDefaultValueBindTo();
							try{
								IParaDataBind pdGetInstance=(IParaDataBind)Class.forName(path).newInstance();
								ParaValue tpv=pdGetInstance.getParaValue(request, rpt, pa);
								if(tpv!=null){
									pdfVal=(String)tpv.getValue();
								}
							}catch(Exception e){
								System.out.println("未能正确加载报表取值类!错误信息:"+path+e.toString());
							}
						}
		        	}else if(pa.getDefaultRuleDefine()!=null){//2015-12 增加动态的默认值
		        		try{
							OptionItem op = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,pa,paVals);
							pdfVal = op.getBm();
						}catch(Exception e){
						}
		        	}
    			%>
		        {
		            x: <%=left+70%>,
		            y: <%=(top-5)+pCount*30%>,
		            xtype: 'textfield',
		            width: <%=pa.getWidth()%>,
		            name: '<%=pa.getName()%>',
		            id: '<%=pa.getName()%>',
		            value:'<%=pdfVal%>'
		        }
		        <%}else if(rType==1){%>
		        {
			        xtype:'textfield',
					hidden: true,
					hideLabel: true,
					name: '<%=pa.getName()%>_desc',
					value:'<%=(String)dfDesMap.get(pa.getName())==null?"":(String)dfDesMap.get(pa.getName())%>',
					id: '<%=pa.getName()%>_desc'
				},
				<%=pa.getName()%>_combo
			    <%}else if(rType==2){%>
			    {
			        xtype:'textfield',
					hidden: true,
					hideLabel: true,
					id:'<%=pa.getName()%>',
					value:'<%=(String)dfValMap.get(pa.getName())==null?"":(String)dfValMap.get(pa.getName())%>',
					name: '<%=pa.getName()%>'
				},<%=pa.getName()%>_tg
                <%}else if(rType==3){
                	String ddfval = pa.getDefaultValue();
                	if(StringUtils.isEmpty(ddfval)&&pa.getDefaultRuleDefine()!=null){//2015-12 增加动态的默认值
		        		try{
							OptionItem op = ParaDefaultOptionService.getParaDefaultOptionService().getParaDefaultOption(rpt,pa,paVals);
							ddfval = op.getBm();
						}catch(Exception e){
						}
		        	}
                %>
                {
		            x: <%=left+70%>,
		            y: <%=(top-5)+pCount*30%>,
		            xtype: 'datefield',
		            format:'<%=pa.getDateFormat()%>',
		            name: '<%=pa.getName()%>',
		            id: '<%=pa.getName()%>',
		            value:<%=StringUtils.isEmpty(ddfval)?"''":"Date.parseDate('"+ddfval+"','"+pa.getDateFormat()+"')"%>
		        }
                <%}
		        pCount++;  
                if(pCount<showCount){out.print(",");}%>
            <%}%>
	        ]
    	});
    	//参数窗体
    	var cParaWin = new Ext.Window({
	        title: '<%=rpt.getName()%>',
	        width: 600,
	        height:480,
	        closable:false,
	        minWidth: 300,
	        minHeight: 200,
	        layout: 'fit',
	        plain:true,
	        bodyStyle:'padding:5px;',
	        buttonAlign:'center',
	        items: cForm,
	        buttons: [{
	            text: '确定',
	            handler:function(){
		            var form = cForm.getForm().getEl().dom;  
		            var result="";
		            <%if(rpt.getDefaultDataDef().getCanPaging()==1){%>
		            	var ps = Ext.getCmp('pageSize');
						if(parseInt(ps.getValue())><%=rpt.getDefaultDataDef().getMaxSize()%>){
							Ext.Msg.alert("警告","每页的行数不能超过<%=rpt.getDefaultDataDef().getMaxSize()%>!");
							return;
						}
		            <%}
		            for(int i=0;i<paras.size();i++){
		        		Parameter para=(Parameter)paras.get(i);
		            	if(para==null||para.getIsHidden()==1){
		            		continue;
		            	}
		            	String[] vds=para.getValidates();
		            	if(vds!=null&&vds.length>0){
		            		for(int j=0;j<vds.length;j++){%>
		            			var result=<%=vds[j]%>('<%=para.getName()%>');
		     					if(!result.success){
		     						Ext.Msg.alert("警告","参数<b><%=para.getDesc()%></b>"+result.remark+"!");
		     						return;
		     					}
		           	  	  <%}
		           	  	}
		            }
		            if(rpt.getDirectExport()==1){%> 
		            form.action = 'doQuery.query?doType=directExport&rptID=<%=rpt.getId()%>'; 
		            <%}else{%>
			        form.action = 'doQuery.query?doType=getReport&rptID=<%=rpt.getId()%>'; 
			        <%}%>  
			        form.target='_blank'; 
			        form.method='POST';  
			        form.submit();
	            }
	        }]
	    });
Ext.onReady(function(){
    cParaWin.show();	
});
</script>
</head>
<body>
</body>
</html>
