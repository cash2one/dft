package com.ifugle.dft.treasury.handler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.treasury.dao.TreasuryDao;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class TreasuryHandler {
	private static Logger log = Logger.getLogger(TreasuryHandler.class);
	private TreasuryDao treasuryDao;
	private Configuration cg ;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public TreasuryHandler(){
		treasuryDao = (TreasuryDao)ContextUtil.getBean("treasuryDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("TreasuryHandler","com.ifugle.dft.treasury.handler.TreasuryHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@DirectMethod
	public List getJks(){
		List jks = null;
		StringBuffer sql = new StringBuffer("select skgkdm bm, mc from jk");
		jks = treasuryDao.queryForList(sql.toString(),Code.class);
		return jks ;
	}
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult impTreasury(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map infos = new HashMap();
		Map errors = new HashMap();		
		FileItem fi = (FileItem)fileFields.get("filepath");
		String mtype = (String)params.get("mtype");
		//保存
		String path = saveTreasuryExcel(fi,mtype);
		if(path==null||"".equals(path)){
			result.setSuccess(false);
			errors.put("msg", "服务端保存文件失败！");
			result.setErrors(errors);
			return result;
		}
		try{
		    WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String)request.getSession().getAttribute("userid");
			String paramJk = (String)params.get("skgkdm");
		    //导入记录
		    Map impInfo = treasuryDao.importData(path,paramJk,mtype,userid);
		    if(impInfo!=null&&impInfo.containsKey("exceptionInfo")){
		    	result.setSuccess(false);
				infos.put("msg",(String)impInfo.get("exceptionInfo"));
		    }else{
			    StringBuffer strResult=new StringBuffer("本次共导入");
			    strResult.append((Integer)impInfo.get("count")).append("条记录，收款国库代码：");
			    strResult.append((String)impInfo.get("skgkdm")).append("，账务日期：");
			    strResult.append((String)impInfo.get("zwrq"));
				infos.put("msg", strResult.toString());
				result.setSuccess(true);
		    }
		    result.setInfos(infos);
		}catch(Throwable e){
			log.error("导入金库数据过程中发生错误",e);
			result.setSuccess(false);
			errors.put("msg", "导入金库数据过程中发生错误。错误信息:"+e.toString());
			result.setErrors(errors);
		}
		return result; 
	}
	private String saveTreasuryExcel(FileItem fi,String mtype){
		String trace=cg.getString("filePathTreasury", "c:/DNFT_upload/jk");
		try{
			//在指定的上传目录中创建文件。
		    java.io.File dir=new java.io.File(trace);
		    if(!dir.exists()){//检查目录是否存在
		    	dir.mkdir();
		    }
		    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");     
			java.util.Date cDate = new java.util.Date();     
			String cTime=formatter.format(cDate);
		    trace=trace+"TREASURY_"+cTime+("eksml".equals(mtype)?".xml":".txt");
		    long ss = System.currentTimeMillis();
			File f = new File(trace);  
			FileUtils.copyInputStreamToFile(fi.getInputStream(),f);
			long es = System.currentTimeMillis();
			log.info("上传共费时:"+(es-ss)+"毫秒");
		}catch(Exception e){
			log.error(e.toString());
			return "";
		}
		return trace;
	}
}
