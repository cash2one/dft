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
		StringBuffer sql = new StringBuffer("select a.colname,a.coldesc,nvl(a.coltype,0)coltype,nvl(b.excelcolindex,-1)+1 excelcol from ");
		sql.append("exceltb_columns a,excelmap b where a.tbname=b.tbname(+) and a.colname=b.colname(+) and a.tbname='");
		sql.append("TMP_").append(tbname.toUpperCase()).append("' and a.rptkey=0 and a.isrindex=0 order by showorder");
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
			return "{result:false}";
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
			List cols = new ArrayList();
			JSONArray jcols = tbinfo.getJSONArray("cols");
			for(int i=0;i<jcols.length();i++){
				JSONObject col = jcols.getJSONObject(i);
				DestField fld = new DestField();
				fld.setColname(col.getString("colname"));
				fld.setColdesc(col.getString("coldesc"));
				fld.setColtype(col.getInt("coltype"));
				try{
					boolean hasExcelCol = col.has("excelcol");
					int excelCol = hasExcelCol? col.getInt("excelcol"): 0;
					fld.setExcelcol(excelCol-1);
				}catch(Exception e){
					fld.setExcelcol(-1);
				}
				cols.add(fld);
			}
			tbtmp.setColumns(cols);
		}catch(Exception e){
			log.error(e.toString());
		}
		return tbtmp;
	}
	@DirectMethod
	public String saveExtendTables(String tb){
		StringBuffer result = new StringBuffer("{result:");
		if(tb==null||tb.equals("")){
			return "{result:true}";
		}
		ExcelTemplate parsedtb = parseTable(tb);
		if(parsedtb==null){
			return "{result:true}"; 
		}
		try{
			boolean done = dpDao.saveExtendTables(parsedtb);
			result.append(done).append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
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
	/**
	 * 导入资金申报时，检查相同项目已经存在多少申报记录
	* @param iid
	* @return
	 */
	@DirectMethod
	public String checkApplyOfIid(int iid){
		int count = 0;
		StringBuffer result = new StringBuffer("{result:");
		if(iid<0){
			result.append("true}");
			return result.toString();
		}
		count = dpDao.CheckAppCount(iid);
		result.append("true,appCount:").append(count).append("}");
		return result.toString();
	}
	/**
	 * 组织申报的元数据
	 */
	@DirectMethod
	public String getAppTemplate(int iid){
		String appTb = cg.getString("applyTmpTable");
		String infos = dpDao.getAppTemplate(iid,appTb);
		return infos;
	}
	/**
	 * 获取已经录入的正式申报记录
	 */
	@DirectMethod
	public Map getFormalAppData(int iid,int start,int limit){
		Map infos = new HashMap();
		String appTb = cg.getString("applyTable");
		StringBuffer sql = new StringBuffer("select t.id,e.swdjzh,e.mc qymc,p.mc czfp,to_char(inputtime,'YYYY-MM-DD HH24:MI:SS')");
		sql.append("inputtime,t.item_content itemcont,t.approvaldate,nvl(t.money,0)money,nvl(sszj,0)sszj,nvl(qptzj,0)qptzj,t.remark from ");
		sql.append(appTb).append(" t,dj_cz e,");
		sql.append("(select distinct bm,mc from bm_cont where table_bm='BM_CZFP')p where e.czfpbm=p.bm(+) and ");
		sql.append("t.swdjzh=e.swdjzh(+)");
		sql.append(" and t.iid=").append(iid).append(" order by t.id");
		int count = dpDao.queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = dpDao.queryForPage(sql.toString(),start,limit,Application.class);
		infos.put("rows", ens);
		return infos;
	}
	/**
	 * 删除正式申报记录
	* @param delType 0：删除部分，1：全部清空
	* @param iid 要删除关于哪个项目的记录
	* @param delRows 要删除的行，清空操作时，不需要该参数
	* @return
	 */
	@DirectMethod
	public String deleteFormalApps(int delType,int iid,String delRows){
		StringBuffer result = new StringBuffer("{result:");
		if(delType==0&&(delRows==null||delRows.equals(""))){
			result.append("true}");
			return result.toString();
		}
		try{
			String appTb = cg.getString("applyTable");
			boolean done = dpDao.deleteFormalApps(appTb,delType,iid,delRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	/**
	 * 获取导入临时表中的申报信息
	 */
	@DirectMethod
	public Map getImportedAppData(int iid,String matchType,int year,int start,int limit){
		Map infos = null;
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String userid = (String) request.getSession().getAttribute("userid");
		String appTmpTb = cg.getString("applyTmpTable");
		String appTb = cg.getString("applyTable");
		infos = dpDao.getImportedAppData(userid,appTmpTb,appTb,iid,matchType,year,start,limit);
		return infos;
	}
	//删除导入临时表表中的申报信息
	@DirectMethod
	public String deleteImportedApps(int iid,String delRows){
		StringBuffer result = new StringBuffer("{result:");
		if(delRows==null||delRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String appTmpTb = cg.getString("applyTmpTable");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String) request.getSession().getAttribute("userid");
			boolean done = dpDao.deleteImportedApps(iid,userid,appTmpTb,delRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//申报信息保存到临时表，主要是保存匹配后的内容
	@DirectMethod
	public String saveTempApps(int iid,String strRows){
		StringBuffer result = new StringBuffer("{result:");
		if(strRows==null||strRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String appTmpTb = cg.getString("applyTmpTable");
			boolean done = dpDao.saveTempApps(appTmpTb,iid,strRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//申报信息向正式库迁移，主要是保存当前页，传递的是序号。调用存储过程来做。
	@DirectMethod
	public String saveFormalApps(int iid,String strRows){
		StringBuffer result = new StringBuffer("{result:");
		if(strRows==null||strRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String proName = cg.getString("pro_importApply");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String) request.getSession().getAttribute("userid");
			String[] infos = dpDao.saveFormalApps(userid,proName,iid,strRows);
			if(infos==null||infos.length<1){
				result.append("true}");
			}else{
				boolean done = "1".equals(infos[0]);
				result.append(done);
				result.append(",info:'").append(infos[1]).append("'}");
			}
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
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
	@DirectMethod
	public String queryContributeOfEn(String year,String swdjzh){
		StringBuffer result = new StringBuffer("{result:");
		if(swdjzh==null||swdjzh.equals("")){
			result.append("false}");
			return result.toString();
		}
		try{
			Map info = dpDao.queryContributeOfEn(year,swdjzh);
			String contribute = "0",contribute_lst="0";
			if(info!=null){
				contribute = ((BigDecimal)info.get("QGX")).toString();
				contribute_lst = ((BigDecimal)info.get("SNQGX")).toString();
			}
			result.append("true,contribute:").append(contribute).append(",contribute_lst:").append(contribute_lst).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//将匹配信息回写到临时表
	@DirectMethod
	public String matchEn(int iid,int xh,String swdjzh){
		StringBuffer result = new StringBuffer("{result:");
		if(swdjzh==null||swdjzh.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String appTmpTb = cg.getString("applyTmpTable");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid=(String)request.getSession().getAttribute("userid");
			boolean done = dpDao.matchEn(appTmpTb,xh,iid,userid,swdjzh);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//获取相同企业相同项目的已申报信息
	@DirectMethod
	public String getSameAppCount(int iid,String swdjzh){
		StringBuffer result = new StringBuffer("{count:");
		int count =0;
		if(swdjzh==null||swdjzh.equals("")){
			result.append("0}");
			return result.toString();
		}
		try{
			String appTb = cg.getString("applyTable");
			count = dpDao.getSameAppCount(appTb,iid,swdjzh);
			result.append(count).append("}");
		}catch(Exception e){
			result.append("0}");
		}
		return result.toString();
	}
	//获取指定项目、支付依据的已资助记录数
	@DirectMethod
	public String checkDoneAid(int iid, String pfileno){
		StringBuffer result = new StringBuffer("{result:");
		if(iid<0){
			result.append("true}");
			return result.toString();
		}
		Map info = dpDao.CheckDoneAidInfo(iid,pfileno);
		String scount = "0",shj="0";
		if(info!=null){
			scount = ((BigDecimal)info.get("CC")).toString();
			shj = ((BigDecimal)info.get("HJ")).toString();
		}
		result.append("true,aidCount:").append(scount).append(",hj:").append(shj).append("}");
		return result.toString();
	}
	@DirectMethod
	public Map getFormalAidData(int iid,String pfileno,int start,int limit){
		Map infos = new HashMap();
		String appTb = cg.getString("aidTable");
		infos = dpDao.getFormalAidData(appTb,iid,pfileno,start,limit);
		return infos;
	}
	@DirectMethod
	public String deleteFormalAids(int delType,int iid,String fileno,String delRows){
		StringBuffer result = new StringBuffer("{result:");
		if(delType==0&&(delRows==null||delRows.equals(""))){
			result.append("true}");
			return result.toString();
		}
		try{
			String aidTb = cg.getString("aidTable");
			boolean done = dpDao.deleteFormalAids(aidTb,delType,iid,fileno,delRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public Map getImportedAidData(int iid,String matchType,String pfileno,int start,int limit){
		Map infos = null;
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String userid = (String) request.getSession().getAttribute("userid");
		String aidTmpTb = cg.getString("aidTmpTable");
		String aidTb = cg.getString("aidTable");
		infos = dpDao.getImportedAidData(userid,aidTmpTb,aidTb,pfileno,iid,matchType,start,limit);
		return infos;
	}
	@DirectMethod
	public String deleteImportedAids(int iid,String pfileno,String delRows){
		StringBuffer result = new StringBuffer("{result:");
		if(delRows==null||delRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String aidTmpTb = cg.getString("aidTmpTable");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String) request.getSession().getAttribute("userid");
			boolean done = dpDao.deleteImportedAids(userid,iid,pfileno,aidTmpTb,delRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public String saveTempAids(String aidInfo,String strRows){
		StringBuffer result = new StringBuffer("{result:");
		if(strRows==null||strRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String aidTmpTb = cg.getString("aidTmpTable");
			boolean done = dpDao.saveTempAids(aidTmpTb,aidInfo,strRows);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public String saveFormalAids(String aidInfo,String strRows){
		StringBuffer result = new StringBuffer("{result:");
		if(strRows==null||strRows.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String proName = cg.getString("pro_importAid");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String) request.getSession().getAttribute("userid");
			String[] infos = dpDao.saveFormalAids(userid,proName,aidInfo,strRows);
			if(infos==null||infos.length<1){
				result.append("true}");
			}else{
				boolean done = "1".equals(infos[0]);
				result.append(done);
				result.append(",info:'").append(infos[1]).append("'}");
			}
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//将客户手工匹配的结果及时更新到临时表中
	@DirectMethod
	public String matchEnOfTmpAid(int iid,String fileno,int xh,String swdjzh){
		StringBuffer result = new StringBuffer("{result:");
		if(swdjzh==null||swdjzh.equals("")){
			result.append("true}");
			return result.toString();
		}
		try{
			String aidTmpTb = cg.getString("aidTmpTable");
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid=(String)request.getSession().getAttribute("userid");
			boolean done = dpDao.matchEnOfTmpAid(aidTmpTb,fileno,iid,xh,userid,swdjzh);
			result.append(done).append("}");
		}catch(Exception e){
			result.append("false}");
		}
		return result.toString();
	}
	//查询企业历年的申报和贡献情况
	@DirectMethod
	public List getEnHistoryData(String swdjzh){
		List infos = null;
		String proName = cg.getString("pro_enHistoryTaxApply");
		infos = dpDao.getEnHistoryData(proName,swdjzh);
		return infos;
	}
}
