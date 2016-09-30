package com.fruit.query.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;
import com.fruit.query.report.*;
import com.fruit.query.data.*;
import com.fruit.query.service.*;
import com.fruit.query.view.*;
import com.fruit.query.parser.*;
import com.fruit.query.util.*;
import org.apache.commons.lang.StringUtils;
/**
 * 
 * @author wxh
 *2009-3-24
 *TODO 处理报表查询请求
 */
public class QueryReportServlet extends HttpServlet{
	protected void doGet(HttpServletRequest request, HttpServletResponse respond)
	    throws ServletException, IOException {
			doPost(request,respond);
	}
	/**
	 * 处理报表的各种请求。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String action = request.getParameter("doType");
		String destination = null;
		RequestDispatcher dispatcher;
		String msg = null;
		try {
			if("getParameters".equals(action)){
				String rptID=request.getParameter("rptID");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				request.setAttribute("queryReport",rpt);
				destination = "/queryReport/parameters.jsp";
			}else if("getReport".equals(action)){
				String rptID=request.getParameter("rptID");
				RptMultiHeader mheader=null;
				Map paraVals=null;
				Report rpt=null;
				if(rptID!=null&&!"".equals(rptID)){
					rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
					if(rpt!=null){
						paraVals=getParaValues(rpt,request);
						//如果能分页，还有分页数的参数
						if(rpt.getDefaultDataDef()!=null&&rpt.getDefaultDataDef().getCanPaging()==1){
							String sVal=request.getParameter("pageSize");
							if(sVal==null||"0".equals(sVal)){
								sVal=String.valueOf(rpt.getDefaultDataDef().getDefaultPageSize());
							}
							paraVals.put("pageSize", new ParaValue(sVal,sVal));
						}
						ColumnDefine cd=rpt.getColumnDef();
						List colNodes=ColumnsService.getReportDataService().getColumnNodes(rpt, paraVals);
						//如果是动态列，设置了自动总结行或分组小计，则所有数字类型的字段都作为总计或小计字段
						if(cd.getSourceType()!=0&&colNodes!=null&&colNodes.size()>0){
							List tflds=new ArrayList();
							for(int i=0;i<colNodes.size();i++){
								Column col=(Column)colNodes.get(i);
								if(col.getDataType()!=0&&col.getIsleaf()>0){
									tflds.add(col.getDataIndex());
								}
							}
							String[] totalFields=new String[tflds.size()];
							for(int i=0;i<tflds.size();i++){
								totalFields[i]=(String)tflds.get(i);
							}
							if(rpt.getTotalRow()>0){
								request.getSession().setAttribute("autoTotalFields",totalFields);
							}
							if(rpt.getGroupDef()!=null&&rpt.getGroupDef().getEnabled()>0){
								rpt.getGroupDef().setFeidsCalculated(tflds);
								request.getSession().setAttribute("autoGroupSumFlds",tflds);
							}
						}
						mheader=new RptMultiHeader(colNodes,cd);
					}
				}
				//复杂表头，各个列
				request.setAttribute("rptHeader", mheader);
				//用于报表页面的参数引用，比如title的构造
				request.getSession().setAttribute(rptID+"_paraVals", paraVals);
				//报表模板
				request.setAttribute("cReport", rpt);
				destination = "/queryReport/report.jsp?rptID="+rptID;
			}else if("getQueryData".equals(action)){
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				String rptID=request.getParameter("rptID");
				String dsStr="";
				Report rpt=null;
				if(rptID!=null&&!"".equals(rptID)){
					rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
					if(rpt!=null){
						Map paraVals=(Map)request.getSession().getAttribute(rptID+"_paraVals");
						//如果远程排序，还有排序参数
						if(rpt.getRemoteSort()==1){
							String sVal=request.getParameter("sort");
							if(sVal!=null&&!"".equals(sVal)){
								paraVals.put("sort",  new ParaValue(sVal,sVal));
							}
							sVal=request.getParameter("dir");
							if(sVal!=null&&!"".equals(sVal)){				
								paraVals.put("dir", new ParaValue(sVal,sVal));
							}
						}
						DataDefine df=rpt.getDefaultDataDef();
						DataSet ds=null;
						RptDataJsonParser parser=RptDataJsonParser.getRptDataParser();
						//区分分页还是不分页。分页时，需要额外知晓start和limit两个参数
						if(df!=null&&df.getCanPaging()==1){
							String sStart=request.getParameter("start");
							String sLimit=request.getParameter("limit");
							int start=0;
							int limit=0;
							try{
								start=Integer.parseInt(sStart);
							}catch(Exception e){}
							try{
								limit=Integer.parseInt(sLimit);
							}catch(Exception e){}
							paraVals.put("start", new ParaValue(String.valueOf(start),String.valueOf(start)));
							paraVals.put("limit", new ParaValue(String.valueOf(limit),String.valueOf(limit)));
							//区分数据库分页还是引擎分页。
							if(df.getPagingMode()==0){
								ds=RptDataService.getReportDataService().getReportDataPaging(rpt, paraVals, start, limit);
								int count=0;
								if(df.getSourceType()==2){
									count=ds.getTotalCount();
								}else {
									count=RptDataService.getReportDataService().getTotalCount(rpt, paraVals);
								}
								dsStr=parser.parseReportDataPaging(count, rpt, ds,start);
							}else{
								//引擎负责分页：获取全部记录，加工（如插入分组行），再按指定范围获取局部对象集，构造输出的json对象
								ds=RptDataService.getReportDataService().getReportData(rpt, paraVals);
								List tflds=(List)request.getSession().getAttribute("autoGroupSumFlds");
								String[] totalFields=(String[])request.getSession().getAttribute("autoTotalFields");
								JSONArray jrows=parser.parseReportData(rpt, ds,tflds,totalFields);
								JSONArray jpart=new JSONArray();
								for(int i=start;(i<start+limit&&i<jrows.length());i++){
									jpart.put(jrows.getJSONObject(i));
								}
								JSONObject allRecords=new JSONObject();
								allRecords.put("totalCount", jrows.length());
								allRecords.put("rptData", jpart);
								dsStr=allRecords.toString();
							}
						}else{
							ds=RptDataService.getReportDataService().getReportData(rpt, paraVals);
							List tflds=(List)request.getSession().getAttribute("autoGroupSumFlds");
							String[] totalFields=(String[])request.getSession().getAttribute("autoTotalFields");
							JSONArray jrows=parser.parseReportData(rpt, ds,tflds,totalFields);
							dsStr=jrows==null?"":jrows.toString();
						}
					}
				}
				out.print(dsStr);
	    		out.close();
	    		return;
			}else if("getOptions".equals(action)){
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				String sTree="";
				String treeParaName=request.getParameter("paraName");
				String rptID=request.getParameter("rptID");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				List items=null;
				if(rpt!=null){
					Map paraMap=rpt.getParasMap();
					Parameter para=(Parameter)paraMap.get(treeParaName);
					if(para.getAffectedByParas()!=null&&!"".equals(para.getAffectedByParas())){
						Map paVals=new HashMap();
						String[] arrPas = para.getAffectedByParas().split(",");
						for(int i=0;i<arrPas.length;i++){
							String val = request.getParameter(arrPas[i]); 
							ParaValue pv=new ParaValue(val,val);
							paVals.put(arrPas[i], pv);
						}
						items = ParaOptionsService.getParaOptionsService().getOptions(rpt,para,paVals);
					}else{
						Map opItemsMap=(Map)request.getSession().getAttribute("opItemsMap");
						items=(List)opItemsMap.get(treeParaName);
					}
					sTree=RptDataJsonParser.getRptDataParser().parseOptionItemsToTree(items,para);
				}
				out.print(sTree);
	    		out.close();
	    		return;
			}else if("toExcel".equals(action)){
				String rptID=request.getParameter("rptID");
				String excelformat = request.getParameter("eformat");
				String onlyExp = request.getParameter("ONLYEXP");
				String unit = request.getParameter("unit");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				if(rpt!=null){
					Map paraVals=(Map)request.getSession().getAttribute(rptID+"_paraVals");
					if("1".equals(onlyExp)){
						paraVals=getParaValues(rpt,request);
					}
					int limit = 65530;
					if("1".equals(excelformat)){
						limit = 100000;
					}
					String sRangeMode = request.getParameter("rangeMode")==null?"0":request.getParameter("rangeMode");
					String sExpStart = request.getParameter("expStart")==null?"0":request.getParameter("expStart");
					String sExpEnd = request.getParameter("expEnd")==null?"0":request.getParameter("expEnd");
					int rangeMode = 0;
			        try{
			        	rangeMode = Integer.parseInt(sRangeMode);
			        }catch(Exception e){
			        	rangeMode = 0;
					}
			        int expStart = 0;
			        try{
			        	expStart = rangeMode==0?0:Integer.parseInt(sExpStart); 
			        }catch(Exception e){
			        	expStart = 0;
					}
			        int expEnd = 0;
			        try{
			        	expEnd = rangeMode==0?limit:Integer.parseInt(sExpEnd); 
			        }catch(Exception e){
			        	expEnd = limit; 
					}
					response.reset();
					RptDataExcelParser eparser = RptDataExcelParser.getExcelParser();
					eparser.exportToExcel(excelformat,request,response,rpt,paraVals,null,expStart-1,expEnd-expStart+1,unit);
					return;
				}
			}else if("reloadAll".equals(action)){
				TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
		    	try{
		    		int repoType=ltmp.getReportRepositoryType();
		    		String path=ltmp.getReportRepositoryPath();
		    		if(repoType==0){
		    			ltmp.loadTemplatesFromFile(path);
		    		}else{
		    			ltmp.loadTemplatesFromDb(path);
		    		}
		    		try{
		    			PortalInfoParser.getParser().loadPortalInfosFromFs();
		    		}catch(Exception e){
		    			e.printStackTrace();
		    		}
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
		    	destination = "/login.jsp";
			}else if("reloadReports".equals(action)){
				TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
				try{
		    		int repoType=ltmp.getReportRepositoryType();
		    		String path=ltmp.getReportRepositoryPath();
		    		if(repoType==0){
		    			ltmp.loadTemplatesFromFile(path);
		    		}else{
		    			ltmp.loadTemplatesFromDb(path);
		    		}
		    		try{
		    			PortalInfoParser.getParser().loadPortalInfosFromFs();
		    		}catch(Exception e){
		    			e.printStackTrace();
		    		}
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
		    	destination = "/queryReport/reloadInfo.jsp";
			}else if("toPdf".equals(action)){
				String rptID=request.getParameter("rptID");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				if(rpt!=null){
					Map paraVals=(Map)request.getSession().getAttribute(rptID+"_paraVals");
					paraVals.put("start", new ParaValue(String.valueOf(0),String.valueOf(0)));
					paraVals.put("limit", new ParaValue(String.valueOf(65534),String.valueOf(65534)));//不分页时限制分页的记录数
					response.reset();
					response.setContentType("application/pdf");
					RptDataPdfParser eparser=RptDataPdfParser.getPdfParser();
					eparser.exportPdf(response.getOutputStream(), rpt, paraVals,null);
					return;
				}
			}else if("getUnits".equals(action)){
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				String rptID=request.getParameter("rptID");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				List items=RptDataService.getReportDataService().getReportUnits(rpt);
				String json = "";
				if(rpt!=null){
					json = RptDataJsonParser.getRptDataParser().parseUnits(items);
				}
				out.print(json);
	    		out.close();
	    		return;
			}else if("getReportRemark".equals(action)){
				String rptID=request.getParameter("rptID");
				Report rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
				Map paraVals=(Map)request.getSession().getAttribute(rptID+"_paraVals");
				String json = RptDataService.getReportDataService().getReportInfo(rpt,paraVals);
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				out.print(json);
	    		out.close();
	    		return;
			}else if("getChartData".equals(action)){
				String rptID=request.getParameter("rptID");
				Report rpt=null;
				String oStream = "";
				if(rptID!=null&&!"".equals(rptID)){
					rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
					if(rpt!=null){
						Chart chart = rpt.getChart();
						if(chart!=null){
							Map paraVals=(Map)request.getSession().getAttribute(rptID+"_paraVals");
							ChartDataInfo dt = ChartService.getChartService().getChartData(rpt, paraVals);
							if("vm".equals(chart.getTemplateFormat())){
								ChartDataParser.getParser().buildXmlDataForVelocity(chart, dt,response);
							}else{
								oStream = ChartDataParser.getParser().buildXmlData(chart, dt);
								try {
						 			response.setContentType("text/xml;charset=UTF-8");
									PrintWriter out=response.getWriter();
									out.print(oStream);
						    		out.close();
								}catch(IOException e1) {
									System.out.println("输出错误！！！" + e1.getMessage());
								}	
							}
						}
					}
				}
				return;				
			}else if("directExport".equals(action)){
				String rptID=request.getParameter("rptID");
				Map paraVals=null;
				Report rpt=null;
				if(rptID!=null&&!"".equals(rptID)){
					rpt=TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
					String format = request.getParameter("format");
					int iformat = 0;
					try{
						iformat = Integer.parseInt(format);
					}catch(Exception e){}
					if(rpt!=null){
						paraVals=getParaValues(rpt,request);
						response.reset();
						RptDataExporter exp =new RptDataExporter(rpt,iformat,paraVals);
						String agent = request.getHeader("USER-AGENT");
						exp.export(agent,response);
					}else{
						PrintWriter out = response.getWriter();
						out.print("未指定要导出报表！");	
						out.close();
			    	}
				}
				return;
			}else if("loadPortlets".equals(action)){
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				String jpls="";
				String portalID=request.getParameter("portalID");
				if(portalID!=null&&!"".equals(portalID)){
					PortalService ps = PortalService.getPortalService();
					jpls=ps.getPortlets(portalID); 
				}else{
					jpls = "{result:false,info:'portalID为空！'}";
				}
				out.print(jpls);
	    		out.close();
	    		return;
			}else if("getChartInfo2Create".equals(action)){
				response.setContentType("text/json;charset=UTF-8");
				PrintWriter out=response.getWriter();
				String jc="";
				String rptID=request.getParameter("id");
				if(rptID!=null&&!"".equals(rptID)){
					PortalService ps = PortalService.getPortalService();
					jc=ps.getChartInfo2Create(rptID); 
				}else{
					jc = "{result:false,info:'id为空！'}";
				}
				out.print(jc);
	    		out.close();
	    		return;
			}
		}catch(Exception e) {
			destination = "/failed.jsp?source=query";
			msg = e.getMessage();
			request.getSession().setAttribute("failedInfo", msg);
		}
		dispatcher = getServletContext().getRequestDispatcher(destination);
		dispatcher.forward(request, response);
	}
	
	private Map getParaValues(Report rpt,HttpServletRequest request)throws Exception{
		//获取报表模板中定义的参数集合。按各个参数名，在请求中获值，构造本次查询的参数值的Map
		List paras=rpt.getParas();
		Map paraVals=new HashMap();
		String sVal=null;
		String sDesc=null;
		if(paras!=null&&paras.size()>0){
			paraVals=new HashMap();
			//如果能分页，还有分页数的参数
			if(rpt.getDefaultDataDef()!=null&&rpt.getDefaultDataDef().getCanPaging()==1){
				sVal=request.getParameter("pageSize");
				if(sVal==null||"".equals(sVal)){
					sVal=String.valueOf(rpt.getDefaultDataDef().getDefaultPageSize());
				}
				paraVals.put("pageSize", new ParaValue(sVal,sVal));
			}
			//如果远程排序，还有排序参数
			if(rpt.getRemoteSort()==1){
				sVal=request.getParameter("sort");
				if(sVal!=null&&!"".equals(sVal)){
					paraVals.put("sort",  new ParaValue(sVal,sVal));
				}
				sVal=request.getParameter("dir");
				if(sVal!=null&&!"".equals(sVal)){				
					paraVals.put("dir", new ParaValue(sVal,sVal));
				}
			}
			for(int i=0;i<paras.size();i++){
				Parameter pa=(Parameter)paras.get(i);
				if(pa==null)continue;
				ParaValue pval=new ParaValue();
				if(pa.getIsHidden()==1){
					if(pa.getBindMode()==0){
						sDesc=sVal=pa.getBindTo();
					}else if(pa.getBindMode()==1){
						sVal=request.getParameter(pa.getName());
						sDesc=request.getParameter(pa.getName()+"_desc");
					}else if(pa.getBindMode()==2){
						sDesc=sVal=(String)request.getSession().getAttribute(pa.getBindTo());
					}else{
						String path=pa.getBindTo();
						try{
							IParaDataBind pdGetInstance=(IParaDataBind)Class.forName(path).newInstance();
							ParaValue tpv=pdGetInstance.getParaValue(request, rpt, pa);
							if(tpv!=null){
								sVal=(String)tpv.getValue();
								sDesc=tpv.getDesc();
							}
						}catch(Exception e){
							System.out.println("未能正确加载报表取值类!错误信息:"+path+e.toString());
						}
					}
				}else{
					sVal=request.getParameter(pa.getName())==null?"":request.getParameter(pa.getName());
					sDesc=request.getParameter(pa.getName()+"_desc")==null?"":request.getParameter(pa.getName()+"_desc");
				}
				if(pa.getRenderType()==0||pa.getRenderType()==3){
					pval=new ParaValue(sVal,sVal);
				}else{
					pval=new ParaValue(sVal,sDesc);
				}
				paraVals.put(pa.getName(), pval);
			}		
		}
		return paraVals;
	}
}
