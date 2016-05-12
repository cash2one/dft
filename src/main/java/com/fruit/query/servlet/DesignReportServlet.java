package com.fruit.query.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fruit.query.util.ParseJsonHelper;
import com.fruit.query.parser.TemplatesLoader;
import com.fruit.query.report.Report;
import com.fruit.query.report.ReportBase;
import com.fruit.query.service.RptDesignService;
import com.fruit.query.util.JsonHelper;
import com.fruit.query.view.RptDataJsonParser;
import com.jspsmart.upload.SmartUpload;
import org.apache.commons.lang.StringUtils;

public class DesignReportServlet extends HttpServlet{
	protected void doGet(HttpServletRequest request, HttpServletResponse respond)throws ServletException, IOException {
		doPost(request,respond);
	}
	private ServletConfig svlConfig;
    /**
     * Init the servlet
     */
    final public void init(ServletConfig config) throws ServletException
    {
        this.svlConfig = config;
    }

    final public ServletConfig getServletConfig()
    {
        return svlConfig;
    }
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String action = request.getParameter("doType");
		String destination = null;
		RequestDispatcher dispatcher;
		String msg = null;
		try {
			if("getRptTemplates".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				List rpts = null;
				String slimit = request.getParameter("limit");
				String sstart = request.getParameter("start");
				int limit = 0;
				int start = 0;
				try{
					limit = Integer.parseInt(slimit);
				}catch(Exception t){}
				try{
					start = Integer.parseInt(sstart);
				}catch(Exception t){}
				rpts = RptDesignService.getRptDesignService().getAllReports();
				Collections.sort(rpts);
				int size = rpts==null?0:rpts.size();
				if(start<size){
					if(start+limit<=size){
						rpts = rpts.subList(start, start+limit);
					}else{
						rpts = rpts.subList(start, size);
					}
				}
				StringBuffer json=new StringBuffer("{totalCount:");
				json.append(size).append(",rows:");
				String sList ="";
				try {
					sList =  ParseJsonHelper.getParseJsonHelper().toJSONString(rpts);
				} catch (Exception e) {
				}
				json.append(sList).append("}");
				out.print(json);
				out.close();
				return;
			}else if("checkRptId".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{isDuplicated:");
				String rptid = request.getParameter("rid");
				boolean dup = RptDesignService.getRptDesignService().checkRptIdDuplicated(rptid);
				json.append(dup).append("}");
				out.print(json);
				out.close();
				return;
			}else if("getAllUnits".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				String checkedUnits = request.getParameter("checkedUnits");
				String sList= RptDesignService.getRptDesignService().getAllUnits(checkedUnits);
				try {
				} catch (Exception e) {
				}
				out.print(sList);
				out.close();
				return;
			}else if("loadRptForm".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					try{
						ReportBase rpt = RptDesignService.getRptDesignService().loadRptBaseInfo(rptid);
						String strRpt = ParseJsonHelper.toJSONString(rpt);
						json.append(true).append(",rpt:").append(strRpt).append("}");
					}catch(Exception e){
						json.append(false).append(",errorInfo:'").append(e.toString()).append("'}");
					}
				}
				out.print(json);
				out.close();
				return;
			}else if("loadReportPart".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					try{
						//根据指定的部分返回内容，如果为空则报错
						String partName = request.getParameter("partName");
 						JSONObject info = RptDesignService.getRptDesignService().loadRptPartInfo(rptid,partName);
						if(info==null||info.has("NO_SUCH_REPORT")){
							json.append(false).append(",errorInfo:'未找到指定报表ID或指定部分的内容!'}");
						}else{
							json.append(true).append(",partInfo:").append(info.toString()).append("}");
						}
					}catch(Exception e){
						json.append(false).append(",errorInfo:'").append(e.toString()).append("'}");
					}
				}
				out.print(json);
				out.close();
				return;
			}else if("updateRptBase".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				String rptName = request.getParameter("rptName");
				int saveType = 1;
				String ssType = request.getParameter("saveType");
				try{
					saveType = Integer.parseInt(ssType);
				}catch(Exception e){
				}
				if(StringUtils.isEmpty(rptid)||StringUtils.isEmpty(rptName)){
					json.append(false).append(",errorInfo:'报表ID或名称不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					Report rpt = rs.buildReportBaseInfo(request);
					boolean done = rs.saveReport(rpt,saveType);
					json.append(done).append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("updateReportPart".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					Report rpt = rs.buildReportPartInfo(request);
					boolean done = rs.saveReport(rpt,1);
					json.append(done).append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("deleteReport".equals(action)){//删除报表
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				String sSynDelete = request.getParameter("synDelete");
				boolean synDelete = "1".equals(sSynDelete);
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					boolean done = rs.deleteReport(rptid,synDelete);
					json.append(done).append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("getAllParams".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				String rptId = request.getParameter("rptId");
				String sList= RptDesignService.getRptDesignService().getAllParams(rptId);
				try {
				} catch (Exception e) {
				}
				out.print(sList);
				out.close();
				return;
			}else if("deleteParamDefine".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				String pname = request.getParameter("pname");
				if(StringUtils.isEmpty(rptid)||StringUtils.isEmpty(pname)){
					json.append(false).append(",errorInfo:'发生错误：报表ID或参数名为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					boolean done = rs.deleteParamDefine(rptid,pname);
					json.append(done).append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("getParamDefine".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				String pname = request.getParameter("pname");
				if(StringUtils.isEmpty(rptid)||StringUtils.isEmpty(pname)){
					json.append(false).append(",errorInfo:'发生错误：报表ID或参数名为空!'}");
				}else{
					try{
 						JSONObject info = RptDesignService.getRptDesignService().getParamDefine(rptid,pname);
 						if(info==null||info.has("NO_SUCH_REPORT")){
 							json.append(false).append(",errorInfo:'未找到指定报表ID或指定参数的定义信息!'}");
 						}else{
 							json.append(true).append(",parameter:").append(info.toString()).append("}");
 						}
					}catch(Exception e){
						json.append(false).append(",errorInfo:'").append(e.toString()).append("'}");
					}
				}
				out.print(json);
				out.close();
				return;
			}else if("updateParam".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				String pname = request.getParameter("pname");
				if(StringUtils.isEmpty(rptid)||StringUtils.isEmpty(pname)){
					json.append(false).append(",errorInfo:'报表ID或参数名称不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					Report rpt = rs.updateParam(request);
					boolean done = rs.saveReport(rpt,1);
					json.append(done).append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("commitRpt".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					String[] result= rs.commitRpt(rptid,"edit2product");
					json.append("1".equals(result[0]));
					if(!"1".equals(result[0])){
						json.append(",errorInfo:'").append(result[1]).append("'");
					}
					json.append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("getRptTemplatesOfProduct".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				List rpts = null;
				String slimit = request.getParameter("limit");
				String sstart = request.getParameter("start");
				int limit = 0;
				int start = 0;
				try{
					limit = Integer.parseInt(slimit);
				}catch(Exception t){}
				try{
					start = Integer.parseInt(sstart);
				}catch(Exception t){}
				rpts = RptDesignService.getRptDesignService().getAllReportsOfProduct();
				int size = rpts==null?0:rpts.size();
				if(start<size){
					if(start+limit<=size){
						rpts = rpts.subList(start, start+limit);
					}else{
						rpts = rpts.subList(start, size);
					}
				}
				StringBuffer json=new StringBuffer("{totalCount:");
				json.append(size).append(",rows:");
				String sList ="";
				try {
					sList =  ParseJsonHelper.toJSONString(rpts);
				} catch (Exception e) {
				}
				json.append(sList).append("}");
				out.print(json);
				out.close();
				return;
			}else if("pullRptsFromProduct".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer json=new StringBuffer("{success:");
				String rptid = request.getParameter("rptId");
				if(StringUtils.isEmpty(rptid)){
					json.append(false).append(",errorInfo:'报表ID不能为空!'}");
				}else{
					RptDesignService rs = RptDesignService.getRptDesignService();
					String[] result= rs.commitRpt(rptid,"product2edit");
					json.append("1".equals(result[0]));
					if(!"1".equals(result[0])){
						json.append(",errorInfo:'").append(result[1]).append("'");
					}
					json.append("}");
				}
				out.print(json);
				out.close();
				return;
			}else if("import".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				StringBuffer strResult = null;
				String filePath = "";
				SmartUpload mySmartUpload=new SmartUpload();
				mySmartUpload.initialize(svlConfig,request,response);
				RptDesignService rs = RptDesignService.getRptDesignService();
				try {
					filePath=rs.saveUploadedFile(mySmartUpload,svlConfig, request, response);
				} catch (Exception e) {
					strResult=new StringBuffer("{success:true,impResult:'failed',info:'");
			    	strResult.append(e.toString());
			    	strResult.append("'}");
			    	out.print(strResult.toString());
					out.close();
					return;
				}
				strResult = new StringBuffer("{success:true,impResult:'success',info:'");
				strResult.append("导入成功！'}");
				out.print(strResult.toString());
				out.close();
				return;
			}else if("export".equals(action)){
				String rptid = request.getParameter("rptId");
				String path = "";
				RptDesignService rs = RptDesignService.getRptDesignService();
				File file = rs.findTemplateFile(rptid);
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				if(file!=null&&file.exists()){
					try {
						bos = new BufferedOutputStream(response.getOutputStream());
						response.setHeader("Content-type:", "application/octet-stream");
						response.setHeader("Accept-Ranges:", "bytes");
						response.setContentLength((int) file.length());   
						response.setHeader("Content-Disposition", "attachment; filename=" + new String(file.getName().getBytes("GB2312"), "ISO8859_1"));   
						bis = new BufferedInputStream(new FileInputStream(file));	 
						byte[] b = new byte[1024];    
						int i = 0;    
					    while((i = bis.read(b)) > 0){
							bos.write(b, 0 ,i);
					    }
					    bis.close();
					    bos.flush();
					    bos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				out.print("<p>文件不存在！</p>");
				out.close();
				return;
				
			}
		}catch(Exception e) {
			destination = "/failed.jsp?source=design";
			msg = e.getMessage();
			request.getSession().setAttribute("failedInfo", msg);
		}
		dispatcher = getServletContext().getRequestDispatcher(destination);
		dispatcher.forward(request, response);
	}

}
