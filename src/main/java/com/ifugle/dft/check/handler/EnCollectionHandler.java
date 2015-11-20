package com.ifugle.dft.check.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.ifugle.dft.check.dao.CheckDao;
import com.ifugle.dft.check.dao.EnCollectionDao;
import com.ifugle.dft.check.entity.EnCollection;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.ImpExcelHelper;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class EnCollectionHandler {
	private static Logger log = Logger.getLogger(EnCollectionHandler.class);
	private EnCollectionDao ecDao;
	private CheckDao ckDao;
	private Configuration cg ;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public EnCollectionHandler(){
		ecDao = (EnCollectionDao)ContextUtil.getBean("ecDao");
		ckDao = (CheckDao)ContextUtil.getBean("ckDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("EnCollectionHandler","com.ifugle.dft.check.handler.EnCollectionHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public List getEnCollection(String nodeid){
		List ecs = null;
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		ecs = ecDao.getEnCollection(nodeid,user);
		return ecs;
	}
	@DirectMethod
	public String checkCollectionNode(String id){
		StringBuffer result = new StringBuffer("{hasChild:");
		if(id==null||"".equals(id)){
			result.append("true,isPrivate:true}");
			return result.toString();
		}
		boolean has = ecDao.hasChildren(id);
		result.append(has).append(",isPrivate:");
		boolean isprivate =ecDao.isPrivateNode(id); 
		result.append(isprivate).append("}");
		return result.toString();
	}
	@DirectMethod
	public EnCollection getEnCollectionById(String id){
		EnCollection ec = null;
		ec= ecDao.getEnCollectionById(id);
		return ec;
	}
	@DirectMethod
	public String deleteEnCollection(String id){
		StringBuffer result = new StringBuffer("{result:");
		if(id==null||"".equals(id)){
			result.append("true}");
			return result.toString();
		}
		boolean done = ecDao.deleteEnCollection(id);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String checkCode(String tmpBm,String bm){
		StringBuffer result = new StringBuffer("{exist:");  
		int exist = ecDao.checkCode(tmpBm,bm);
		result.append(exist).append("}");
		return result.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult saveEnCollection(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();	
		boolean done = false;
		try{
			WebContext context =WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			User user=(User)request.getSession().getAttribute("user");
			int[] saveResult = ecDao.saveEnCollection(params,fileFields,user.getUserid());
			result.setSuccess(saveResult==null?false:saveResult[0]>0);
			Map infos = new HashMap();
			infos.put("msg", "保存编码成功！");
			infos.put("newID", String.valueOf(saveResult[1]));
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存编码时发生错误，错误："+e.toString());
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public Map getCollectionEns(int start,int limit,String pField,String pValue,String enId){
		Map enInfos = null;
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		enInfos = ecDao.getCollectionEns(start,limit, pField, pValue,enId,user);
		return enInfos;
	}
	@DirectMethod
	public String removeEn(String ecid,String ens){
		StringBuffer result = new StringBuffer("{result:");
		if(ens==null||"".equals(ens)){
			result.append("true}");
			return result.toString();
		}
		String[] eids =ens.split(",");
		boolean done = ecDao.removeEn(ecid,eids);
		result.append(done).append("}");
		return result.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult importEnExcel(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();		
		FileItem fi = (FileItem)fileFields.get("filepath");
		String path = ImpExcelHelper.getImpExcelHelper().saveEnExcel(fi,"filePathQyjh");
		if(path==null||"".equals(path)){
			result.setSuccess(false);
			errors.put("msg", "服务端保存文件失败！");
			result.setErrors(errors);
			return result;
		}
		String sMatchCol = (String)params.get("matchCol");
		String sBeginRow = (String)params.get("beginRow");
		String d_type = (String)params.get("d_type");
		int matchCol = 0;
		try{
			matchCol = Integer.parseInt(sMatchCol);
		}catch(Exception e){}
		StringBuffer strResult = null;
		int beginRow = 0;
		try{
			beginRow = Integer.parseInt(sBeginRow);
		}catch(Exception e){}
		try{
			//解析excel获取待匹配字段值集合
		    List matchFlds=ckDao.readExcel(path,beginRow,matchCol);
		    WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String)request.getSession().getAttribute("userid");
		    //获取匹配后的结果
		    int rcount = ecDao.importEnExcel(matchFlds,userid,d_type);
		    strResult=new StringBuffer("本次共导入");
		    strResult.append(rcount).append("条有效记录！");
			result.setSuccess(true);
			Map infos = new HashMap();
			infos.put("msg", strResult.toString());
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "导入企业Excel时发生错误。错误信息："+e.toString());
			result.setErrors(errors);
		}
		return result; 
	}
	@DirectMethod
	@SuppressWarnings("unchecked")
	public Map getImportedEns(int start,int limit){
		Map infos = new HashMap();
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		infos = ecDao.getImportedEns(start,limit,user);
		return infos;
	}
	@DirectMethod
	public String delImportedEns(String strXhs){
		StringBuffer result = new StringBuffer("{result:");
		if(strXhs==null||"".equals(strXhs)){
			result.append("true}");
			return result.toString();
		}
		String[] xhs =strXhs.split(",");
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		boolean done = ecDao.delImportedEns(xhs,user.getUserid());
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String addExcelMatchEns(String cid,String shs,String strXhs){
		StringBuffer result = new StringBuffer("{result:");
		if(shs==null||"".equals(shs)){
			result.append("true}");
			return result.toString();
		}
		String[] swdjzhs = shs.split(",");
		String[] xhs = strXhs.split(",");
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		boolean done = ecDao.addExcelMatchEns(cid,swdjzhs,xhs,user.getUserid());
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	@SuppressWarnings("unchecked")
	public Map getEnterprisesToAdd(int start,int limit,String pField,String pValue,String cID){
		Map enInfos = null;
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		enInfos = ecDao.getEnterprisesToAdd(start,limit,pField,pValue,cID,user);
		return enInfos ; 
	}
	@DirectMethod
	public String addEn(String cid,String swdjzhs){
		StringBuffer result = new StringBuffer("{result:");
		if(swdjzhs==null||"".equals(swdjzhs)){
			result.append("true}");
			return result.toString();
		}
		String[] eids =swdjzhs.split(",");
		boolean done = ecDao.addEn(cid,eids);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String toggleQybj(String cid,String swdjzh,int cQybj){
		StringBuffer result = new StringBuffer("{result:");
		boolean done = ecDao.toggleQybj(cid,swdjzh,cQybj);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String saveEnOrder(String cid,String enObjs){
		StringBuffer result = new StringBuffer("{result:");
		JSONArray jarr = null;
		try{
			jarr = new JSONArray(enObjs);
		}catch(Exception e){
		}
		if(jarr==null||jarr.length()==0){
			result.append("true}");
			return result.toString();
		}
		boolean done = ecDao.saveEnOrder(cid,jarr);
		result.append(done).append("}");
		return result.toString();
		
	}
}
