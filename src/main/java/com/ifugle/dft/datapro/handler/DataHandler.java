package com.ifugle.dft.datapro.handler;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.ExpExcelHelper;
import com.ifugle.dft.utils.entity.DestField;
import com.ifugle.dft.utils.entity.ExcelTable;
import com.ifugle.dft.utils.entity.ExcelTemplate;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;
import com.ifugle.dft.datapro.entity.Application;
import com.ifugle.dft.datapro.dao.*;
import com.ifugle.dft.utils.entity.*;
import com.ifugle.dft.utils.ImpExcelHelper;

public class DataHandler {
	private static Logger log = Logger.getLogger(DataHandler.class);
	private DataProcessDao dpDao;
	private Configuration cg ;
	private ImpExcelHelper excelImp;
	private ExpExcelHelper excelExp;
	WebContext context ;
	
	@SuppressWarnings("unchecked")
	public DataHandler(){
		dpDao = (DataProcessDao)ContextUtil.getBean("dpDao");
		cg = (Configuration)ContextUtil.getBean("config");
		excelImp = (ImpExcelHelper) ContextUtil.getBean("excelImp");
		excelExp = (ExpExcelHelper) ContextUtil.getBean("excelExp");
		if(cg!=null){
			cg.getHandlersMap().put("DataHandler","com.ifugle.dft.datapro.handler.DataHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public Map getTbs(int start,int limit){
		Map infos = new HashMap();
		String sql = "select decode(substr(tbname,1,4),'TMP_',substr(tbname,5),tbname)tbname,tbdesc,tid,proname,ttype,remark from exceltables";
		int count = dpDao.queryCount(sql);
		infos.put("totalCount", new Integer(count));
		List ens = dpDao.getTbs(sql,start,limit,ExcelTable.class);
		infos.put("rows", ens);
		return infos;
	}
	@DirectMethod
	public List getTbList(){
		String sql = "select tbname,tbdesc,tid,proname,ttype,remark from exceltables";
		List infos = dpDao.getTbs(sql,0,1000,ExcelTable.class);
		return infos;
	}
	@DirectMethod
	public String deleteTb(String tbs){
		StringBuffer result = new StringBuffer("{result:");
		if(tbs==null||tbs.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String[] stbs = tbs.split(",");
			boolean done = dpDao.deleteTables(stbs);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public List getTbCols(String tbname){
		List cols = null;
		StringBuffer sql = new StringBuffer("select 1 isold,a.colname o_colname,a.colname,a.coldesc,nvl(a.coltype,0)coltype,");
		sql.append("nvl(a.showorder,0)showorder from exceltb_columns a,excelmap b where a.tbname=b.tbname(+) and a.colname=b.colname(+) ");
		sql.append("and a.tbname='TMP_").append(tbname.toUpperCase()).append("' and a.rptkey=0 and a.isrindex=0 order by showorder");
		cols = dpDao.getTbCols(sql.toString(),DestField.class);
		return cols;
	}
	@DirectMethod
	public String addExtendTables(String tb){
		if(tb==null||tb.equals("")){
			return "{result:true}";
		}
		String addInfo ="";
		try{
			ExcelTemplate parsedtb = parseTable(tb);
			if(parsedtb==null){
				return "{result:true}"; 
			}
			addInfo = dpDao.addExtendTables(parsedtb);
		}catch(Exception e){
			StringBuffer info = new StringBuffer("{result:false,info:'");
			info.append(e.toString()).append("'}");
			return info.toString();
		}
		return addInfo;
	}
	
	private ExcelTemplate parseTable(String strTb){
		ExcelTemplate tbtmp = null;
		JSONObject tbinfo = null;
		try{
			tbinfo = new JSONObject(strTb);
		}catch(Exception e){
			log.error(e.toString());
		}
		if(tbinfo==null){
			return null;
		}
		tbtmp = new ExcelTemplate();
		try{
			ExcelTable tb = new ExcelTable();
			tb.setTbname(tbinfo.getString("tbname"));
			tb.setTbdesc(tbinfo.getString("tbdesc"));
			tb.setTtype(tbinfo.getInt("ttype"));
			tb.setProname(tbinfo.getString("proname"));
			tb.setRemark(tbinfo.getString("remark"));
			tb.setTid(tbinfo.getInt("tid"));
			tbtmp.setTb(tb);
			if(tbinfo.has("cols")){
				List cols = new ArrayList();
				JSONArray jcols = tbinfo.getJSONArray("cols");
				for(int i=0;i<jcols.length();i++){
					JSONObject col = jcols.getJSONObject(i);
					DestField fld = new DestField();
					fld.setColname(col.getString("colname"));
					fld.setColdesc(col.getString("coldesc"));
					fld.setColtype(col.getInt("coltype"));
					fld.setShoworder(col.getInt("showorder"));
					cols.add(fld);
				}
				tbtmp.setColumns(cols);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return tbtmp;
	}
	@DirectMethod
	public String saveExtendTables(String tname,String tbinfo,String edtInfo){
		StringBuffer result = new StringBuffer("{result:");
		try{
			boolean done = dpDao.saveExtendTables(tname,tbinfo,edtInfo);
			result.append(done).append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false,info:'").append(e.toString()).append("'}");
			
		}
		return result.toString();
	}
	@DirectMethod
	public String deleteColumn(String tb,String col){
		StringBuffer result = new StringBuffer("{result:");
		if(StringUtils.isEmpty(tb)||StringUtils.isEmpty(col)){
			return "{result:true}";
		}
		try{
			boolean done = dpDao.deleteColumn(tb,col);
			result.append(done).append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false,info:'").append(e.toString()).append("'}");
		}
		return result.toString();
	}
	@DirectFormPostMethod
	public SubmitResult impData(Map params, Map fileFields) {
		SubmitResult result = new SubmitResult();
		Map errors = new HashMap();
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String userid = (String) request.getSession().getAttribute("userid");
		String tid = (String) params.get("tid");
		FileItem fi = (FileItem) fileFields.get("filepath");
		String path = excelImp.saveEnExcel(fi, userid+"_"+tid);
		if (path == null || "".equals(path)) {
			result.setSuccess(false);
			errors.put("msg", "服务端保存文件失败！");
			result.setErrors(errors);
			return result;
		}
		StringBuffer strResult = null;
		String sBeginRow = (String) params.get("beginRow");
		if (sBeginRow == null) {
			sBeginRow = cg.getString("importStartRow", "3");
		}
		Map infos = new HashMap();
		int beginRow = 3;
		try {
			beginRow = Integer.parseInt(sBeginRow);
		} catch (Exception e) {
		}
		try {
			ExcelTemplate tmp = excelImp.getTemplate(tid);
			if (tmp == null) {
				result.setSuccess(false);
				errors.put("msg", "未找到Excel导入的配置信息！");
				result.setErrors(errors);
				return result;
			}
			// 根据模板中的列设置，找到报表键值，这些值一个文件就提供一次，由外部参数传入，一般是年月等
			List keyVals = new ArrayList();
			List cols = tmp.getColumns();
			for (int i = 0; i < cols.size(); i++) {
				DestField tfld = (DestField) cols.get(i);
				if (tfld != null && tfld.getRptkey() == 1) {
					KeyValuePair kp = new KeyValuePair();
					kp.setKey(tfld.getColname());
					String sval = (String)params.get(tfld.getColname().toLowerCase());
					sval = sval==null?"":sval;
					sval = new String(sval.getBytes("ISO-8859-1"), "UTF-8");
					kp.setValue(sval);
					keyVals.add(kp);
				}
			}
			String[] doneinfo = excelImp.ImportRecordFromXsl(path, tmp, beginRow,userid, keyVals);
			
			if (doneinfo!=null&&Integer.parseInt(doneinfo[0]) > 0) {
				// 统计导入后的数量
				strResult = new StringBuffer("本次操作从文件中导入了");
				strResult.append(doneinfo[1]).append("行有效记录！").append(doneinfo[2]==null?"":doneinfo[2]);
				result.setSuccess(true);
				infos.put("msg", strResult.toString());
				result.setInfos(infos);
			} else {
				/*if (Integer.parseInt(doneinfo[0]) == -9) {
					strResult = new StringBuffer("文件打开失败！");
				} else if (Integer.parseInt(doneinfo[0]) == -2) {
					strResult = new StringBuffer("excel文件路径不存在！");
				} else if (Integer.parseInt(doneinfo[0]) == -3) {
					strResult = new StringBuffer("未找到导入的列匹配信息！");
				} else if (Integer.parseInt(doneinfo[0]) == -4) {
					strResult = new StringBuffer("导入到中间表后，从中间表向数据表迁移过程发生错误！");
				} else {
					strResult = new StringBuffer("向中间表导入Excel记录时发生错误！中断记录在第"
							+ (doneinfo[1] + beginRow) + "行！");
				}*/
				result.setSuccess(false);
				infos.put("msg", doneinfo[2]);
				result.setErrors(infos);
			}
		} catch (Throwable e) {
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "导入Excel时发生错误。错误信息：" + e.toString());
			result.setErrors(errors);
		}
		return result;
	}
	@DirectMethod
	public String CheckTableName(String tbname){
		StringBuffer result = new StringBuffer("{duplicate:");
		if(tbname==null||tbname.equals("")){
			result.append("false}");
			return result.toString();
		}
		boolean done = dpDao.checkTbname(tbname);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String CheckTemplateDownload(int tid){
		StringBuffer result = new StringBuffer("{hasRecord:");
		if(tid<0){
			result.append("false}");
			return result.toString();
		}
		boolean done = dpDao.CheckTemplateDownload(tid);
		result.append(done).append("}");
		return result.toString();
	}
	public void exportTemplate(HttpServletRequest request,HttpServletResponse response) {
		String stid = request.getParameter("expTid");
		int tid = 0;
		try{
			tid = Integer.parseInt(stid);
		}catch(Exception e){
		}
		String sformat =request.getParameter("format")==null?"":request.getParameter("format");
		int format = "xlsx".equals(sformat)?1:0;
		String agent = request.getHeader("USER-AGENT");
		excelExp.exportFile(tid, format,agent,response);
	}

	//获取单位列表，按名称税号匹配查找
	@DirectMethod
	public Map getEns(int start,int limit,String pField,String pValue){
		Map infos = null;
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		infos = dpDao.getEns(user,start,limit,pField,pValue);
		return infos;
	}
}
