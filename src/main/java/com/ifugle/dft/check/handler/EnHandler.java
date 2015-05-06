package com.ifugle.dft.check.handler;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ifugle.dft.check.dao.EnDao;
import com.ifugle.dft.check.entity.Enterprise;
import com.ifugle.dft.check.entity.Rtk_Hdlr;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.ImpExcelHelper;
import com.ifugle.dft.utils.JsonHelper;
import com.ifugle.dft.utils.entity.DestField;
import com.ifugle.dft.utils.entity.ExcelTemplate;
import com.ifugle.dft.utils.entity.KeyValuePair;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class EnHandler {
	private static Logger log = Logger.getLogger(EnHandler.class);
	private EnDao enDao;
	private Configuration cg ;
	private ImpExcelHelper excelImp;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public EnHandler(){
		enDao = (EnDao)ContextUtil.getBean("enDao");
		cg = (Configuration)ContextUtil.getBean("config");
		excelImp = (ImpExcelHelper)ContextUtil.getBean("excelImp");
		if(cg!=null){
			cg.getHandlersMap().put("EnHandler","com.ifugle.dft.check.handler.EnHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	
	@DirectMethod
	public String doAutoMap(){
		StringBuffer result = new StringBuffer("{result:");
		String proName = cg.getString("pro_autoMapEnterprises","PKG_CHECK.ENS_AUTOMAP");
		String sql = "{call "+proName+"(?,?)}"; 
		String[] results =  enDao.autoMapEnterprises(sql);
		result.append(results[0]).append(",info:'").append(results[1]);
		result.append("'}");
		return result.toString();
	}
	
	@DirectMethod
	public String undoMapping(String ens){
		StringBuffer result = new StringBuffer("{result:");
		if(ens==null||"".equals(ens)){
			result.append("'0'}");
			return result.toString();
		}
		String proName = cg.getString("pro_undoMapping","PKG_CHECK.UNDO_MAP");
		String sql = "{call "+proName+"(?,?,?)}"; 
		String[] results =  enDao.undoMapping(sql,ens);
		result.append(results[0]).append(",info:'").append(results[1]);
		result.append("'}");
		return result.toString();
	}
	@DirectMethod
	public String mapEns(int dsxh,int gsxh){
		StringBuffer result = new StringBuffer("{result:");
		if(dsxh==0||gsxh ==0){
			result.append("'0'}");
			return result.toString();
		}
		String proName = cg.getString("pro_mapEns","PKG_CHECK.DO_MAP");
		String sql = "{call "+proName+"(?,?,?,?)}"; 
		String[] results =  enDao.mapEns(sql,dsxh,gsxh);
		result.append(results[0]).append(",info:'").append(results[1]);
		result.append("'}");
		return result.toString();
	}
	@DirectMethod
	public String deleteVEn(String xhs){
		StringBuffer result = new StringBuffer("{result:");
		if(xhs==null||"".equals(xhs)){
			result.append("true}");
		}
		String proName = cg.getString("pro_deleteVEns","PKG_CHECK.DELETE_VEN");
		String sql = "{call "+proName+"(?,?,?)}"; 
		String[] results =  enDao.deleteVEn(sql,xhs);
		result.append("1".equals(results[0])).append(",info:'").append(results[1]);
		result.append("'}");
		return result.toString();
	}
	@DirectMethod
	public String checkSwdjzh(int xh,String swdjzh){
		StringBuffer result = new StringBuffer("{duplicate:");
		boolean duplicate =  enDao.checkSwdjzh(xh,swdjzh);
		result.append(duplicate).append("}");
		return result.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult saveVirtualEn(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();
		boolean done = false;
		try{
			done = enDao.saveVirtualEn(params,fileFields);
			result.setSuccess(done);
			Map infos = new HashMap();
			infos.put("msg", "保存编码成功！");
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存编码时发生错误，错误："+e.toString());
		}
		return result;
	}
	//获取指定的虚拟企业登记信息
	@SuppressWarnings("unchecked")
	@DirectMethod
	public String getVirtualEn(String swdjzh){
		StringBuffer json = new StringBuffer("{result:");
		if(swdjzh==null||"".equals(swdjzh)){
			json.append("true}");
			return json.toString();
		} 
		StringBuffer sql = new StringBuffer("select a.xh,a.mc,czfpbm czfpbm_bm,hybm hybm_bm,ztbm ztbm_bm,jjxzbm jjxzbm_bm");
		sql.append(",b.mc czfpbm,d.mc hybm,e.mc ztbm,g.mc jjxzbm,swdjzh,dz,fddbr,to_char(bgrq)bgrq");
		sql.append(" from dj_cz a,(select bm,mc from bm_cont where table_bm='BM_CZFP')b, ");
		sql.append(" (select bm,mc from bm_cont where table_bm='BM_HY')d, (select bm,mc from bm_cont where table_bm='BM_ZT')e,");
		sql.append(" (select bm,mc from bm_cont where table_bm='BM_JJXZ')g where ");
		sql.append("a.czfpbm=b.bm(+) and a.hybm=d.bm(+) and a.ztbm=e.bm(+) and a.jjxzbm=g.bm(+) and swdjzh=?");
		Map en = enDao.getVirtualEnInfo(sql.toString(),swdjzh);
		if(en!=null){
			json.append("true,en:");
			try{
				json.append(JsonHelper.getJsonHelper().toJSONString(en));
			}catch(Exception e){
				json.append("null");
			}
		}
		json.append("}");
		return json.toString();
	}
	
	@DirectMethod
	public List getNewPzDetail(){
		List ndts = null;
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String userid=(String)request.getSession().getAttribute("userid");
		ndts = enDao.getNewPzDetail(userid);
		return ndts;
	}
	//导入凭证明细
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult importPzDetail(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();		
		FileItem fi = (FileItem)fileFields.get("filepath");
		String path = savePzDetailExcel(fi);
		if(path==null||"".equals(path)){
			result.setSuccess(false);
			errors.put("msg", "服务端保存文件失败！");
			result.setErrors(errors);
			return result;
		}
		String sMatchCol = (String)params.get("matchCol");
		String sBeginRow = (String)params.get("beginRow");
		String tid = (String)params.get("tid");
		int matchCol = 0;
		try{
			matchCol = Integer.parseInt(sMatchCol);
		}catch(Exception e){}
		StringBuffer strResult = null;
		Map infos = new HashMap();
		int beginRow = 0;
		try{
			beginRow = Integer.parseInt(sBeginRow);
		}catch(Exception e){}
		List keyVals = new ArrayList();
		KeyValuePair kp = new KeyValuePair();
		kp.setKey("SWDJZH");
		String sval = (String)params.get("swdjzh");
		kp.setValue(sval);
		keyVals.add(kp);
		try{
			WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String)request.getSession().getAttribute("userid");
			//文件数据保存到数据库中间表
		    String[] doneinfo=addPzdt2Tmp(path,userid,tid,beginRow,matchCol,keyVals);
		    if(doneinfo!=null&&Integer.parseInt(doneinfo[0])>0){
			    //统计导入后的数量
			    strResult=new StringBuffer("本次共导入");
			    strResult.append(doneinfo[1]).append("条有效记录！");
				result.setSuccess(true);
				infos.put("msg", strResult.toString());
				result.setInfos(infos);
		    }else{
		    	result.setSuccess(false);
				infos.put("msg", doneinfo[2]);
				result.setErrors(infos);
		    }
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "导入凭证明细时发生错误。错误信息："+e.toString());
			result.setErrors(errors);
		}
		return result; 
	}
	private String savePzDetailExcel(FileItem fi){
		String trace=cg.getString("filePath", "c:/DNFT_upload/");
		try{
			//在指定的上传目录中创建文件。
		    java.io.File dir=new java.io.File(trace);
		    if(!dir.exists()){//检查目录是否存在
		    	dir.mkdir();
		    }
		    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");     
			java.util.Date cDate = new java.util.Date();     
			String cTime=formatter.format(cDate);
		    trace=trace+"PZDETAIL_"+cTime+".xls";
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
	private String[] addPzdt2Tmp(String path, String userid,String tid,int beginRow, int matchCol,List keyVals) {
		String[] result =new String[]{"-1","0",""};
		ExcelTemplate tmp = excelImp.getTemplate(tid);
		try{
			result = excelImp.ImportRecordFromXsl(path, tmp, beginRow, userid,keyVals);
		}catch(Exception e){
		}
		return result;
	}
	@DirectMethod
	public Map getEns(int start,int limit,String qymc){
		Map infos = new HashMap();
		StringBuffer sql = new StringBuffer("select * from(select xh,swdjzh,a.mc,dz,a.qysx,a.czfpbm czfpbm_bm,b.mc czfpbm from dj_cz a,");
		sql.append("(select * from bm_cont where table_bm='BM_CZFP')b where a.czfpbm=b.bm(+) and a.qysx=3 union ");
		sql.append(" select d.xh,r.swdjzh,d.mc,dz,d.qysx,d.czfpbm czfpbm_bm,bm.mc czfpbm from ");
		sql.append("(select distinct swdjzh from RTK_HDLR) r,(select * from bm_cont where table_bm='BM_CZFP')bm,dj_cz d ");
		sql.append(" where r.swdjzh=d.swdjzh and d.czfpbm=bm.bm(+))");
		if(qymc!=null&&!"".equals(qymc)){//有查询条件的就到整个登记中搜索
			sql = new StringBuffer("select xh,swdjzh,a.mc,dz,a.qysx,a.czfpbm czfpbm_bm,b.mc czfpbm from dj_cz a,");
			sql.append("(select * from bm_cont where table_bm='BM_CZFP')b where a.czfpbm=b.bm(+)");
			sql.append(" and a.mc like '%").append(qymc).append("%' order by qysx desc");
		}
		int count = enDao.queryCount(sql.toString());
		infos.put("totalCount", new Integer(count));
		List ens = enDao.getEns(sql.toString(),start,limit);
		infos.put("rows", ens);
		return infos ;
	}
	@DirectMethod
	public List getEnPzhBySwdjzh(String swdjzh){
		List infos = null;
		infos = enDao.getEnPzhs(swdjzh);
		return infos;
	}
	@DirectMethod
	public String savePzDetail(String swdjzh,String sph,String strRkrq,String remark,String jPzs){
		StringBuffer results = new StringBuffer("{result:");
		List pzs = parsePzDetail(jPzs);
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String userid = (String)request.getSession().getAttribute("userid");
		String[] infos = enDao.savePzDetail(swdjzh,sph,strRkrq,remark,userid,pzs);
		results.append(infos!=null && "1".equals(infos[0])).append(",sph:'");
		results.append(infos==null||infos[1]==null?"":infos[1]).append("',info:'");
		results.append(infos.length>2?infos[2]:"").append("'}");
		return results.toString();
	}
	@SuppressWarnings("unchecked")
	private List parsePzDetail(String jPzs){
		List pzs = null;
		if(jPzs==null||"".equals(jPzs)){
			return null;
		}
		try{
			JSONArray jarr = new JSONArray(jPzs);
			if(jarr!=null&&jarr.length()>0){
				pzs = new ArrayList();
				for(int i=0;i<jarr.length();i++){
					JSONObject jobj = jarr.getJSONObject(i);
					Rtk_Hdlr rtk = new Rtk_Hdlr();
					rtk.setFpbm(jobj.getString("fpbm"));
					rtk.setSzbm(jobj.getString("szbm"));
					rtk.setYsjcbm(jobj.getString("ysjcbm"));
					rtk.setYskmbm(jobj.getString("yskmbm"));
					rtk.setJe(jobj.getDouble("je"));
					pzs.add(rtk);
				}
			}
		}catch(Exception e){
		}
		return pzs;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public List getPzDetail(String swdjzh,String pzh){
		List dts = null;
		dts = enDao.getPzDetails(swdjzh,pzh);
		return dts;
	}
	@DirectMethod
	public String delPz(String swdjzh,String pzh){
		StringBuffer results = new StringBuffer("{result:");
		String[] info = enDao.deletePz(swdjzh,pzh);
		results.append("1".equals(info[0])).append(",info:'");
		results.append(info[1]).append("'}");
		return results.toString();
	}
}
