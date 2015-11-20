package com.ifugle.dft.check.handler;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import java.net.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.softwarementors.extjs.djn.StringUtils;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;
import com.ifugle.dft.check.dao.*;
import com.ifugle.dft.check.entity.En_field;
import com.ifugle.dft.check.entity.Enterprise;
import com.ifugle.dft.check.entity.Hd_log;
import com.ifugle.dft.system.entity.CheckTreeNode;
import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.ImpExcelHelper;
import com.ifugle.dft.utils.JsonHelper;
import com.ifugle.dft.utils.entity.SimpleValue;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.ifugle.dft.utils.entity.TreeNode;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
public class CheckHandler {
	private static Logger log = Logger.getLogger(CheckHandler.class);
	private CheckDao ckDao;
	private Configuration cg ;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public CheckHandler(){
		ckDao = (CheckDao)ContextUtil.getBean("ckDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("CheckHandler","com.ifugle.dft.check.handler.CheckHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public Map getEns(int enType,int start,int limit,String conditions){
		Map infos = new HashMap();
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String sql = "";
		User user=(User)request.getSession().getAttribute("user");
		sql = buildEnSql(enType,conditions,user);
		log.info(sql);
		System.out.println(sql);
		int count = ckDao.queryCount(sql);
		infos.put("totalCount", new Integer(count));
		List ens = ckDao.getEns(sql,start,limit,Enterprise.class);
		infos.put("rows", ens);
		return infos ;
	}
	@DirectMethod
	public String tryGetEns(int enType,int opType,String conditions){
		StringBuffer result = new StringBuffer("{canOp:");
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		String sql = buildEnSql(enType,conditions,user);
		boolean canOp = ckDao.tryExcute(sql);
		result.append(canOp);
		if(opType==0){
			result.append(",sql:\"");
			result.append(sql).append("\"");
		}
		result.append("}");
		return result.toString();
	}
	
	@SuppressWarnings("unchecked")
	private String buildEnSql(int enType,String conditions,User user){
		Map mFlds =cg.getFinanceFieldsMap();
		if(mFlds==null){
			log.error("没有设置登记字典表！");
			return "";
		}
		//条件相关的各个数组
		String[] heads = null,orderFlds=null,descOrAsc = null;
		String[] flds=null,ops = null,vals = null,rltns = null;
		boolean matchExcel= false;
		int ismap = -1;
		int qysx = 0;
		//解析条件
		try{
			JSONObject jcdts = new JSONObject(conditions);
			if(jcdts!=null){
				//对导入Excel匹配的处理
				if(jcdts.has("matchExcel")){
					matchExcel = jcdts.getBoolean("matchExcel");
				}
				if(jcdts.has("ismap")){
					ismap = jcdts.getInt("ismap");
				}
				if(jcdts.has("qysx")){
					qysx = jcdts.getInt("qysx");
				}
				if(jcdts.has("tHeads")){
					String paraHeads= jcdts.getString("tHeads")==null?"":jcdts.getString("tHeads");
					heads = "".equals(paraHeads)?null:paraHeads.split(",");
				}
				if(jcdts.has("orderFlds")){
					String paraOderflds = jcdts.getString("orderFlds");
					orderFlds = "".equals(paraOderflds)?null:paraOderflds.split(",");
				}
				if(jcdts.has("descOrAsc")){
					String paraDescOrAsc = jcdts.getString("descOrAsc");
					descOrAsc = "".equals(paraDescOrAsc)?null:paraDescOrAsc.split(",");
				}
				if(jcdts.has("fldNames")){
					String paraFlds = jcdts.getString("fldNames");
					flds = "".equals(paraFlds)?null:paraFlds.split(",");
				}
				if(jcdts.has("relations")){
					String paraOps = jcdts.getString("relations");
					ops = "".equals(paraOps)?null:paraOps.split(",");
				}
				if(jcdts.has("fldValues")){
					String paraVals = jcdts.getString("fldValues");
					vals = "".equals(paraVals)?null:paraVals.split(",");
				}
				if(jcdts.has("connections")){
					String paraRltns = jcdts.getString("connections");
					rltns = "".equals(paraRltns)?null:paraRltns.split(",");
				}
			}
		}catch(Exception e){
		}
		StringBuffer sql = new StringBuffer("");
		StringBuffer sSelect=new StringBuffer("");
		StringBuffer sFrom=new StringBuffer(" DJ_CZ");
		StringBuffer sWhere=new StringBuffer("");
		StringBuffer sOrderBy=new StringBuffer("");
		//获取表头，如果没有指定表头，则使用字典表里的字段。如果有，则显示指定表头字段（一般来自筛选）
		String[] tHeads=getTableHeads(heads,ismap);	
		String mainTb="DJ_CZ";
		for(int i=0;i<tHeads.length;i++){
			En_field en_table=(En_field)mFlds.get(tHeads[i]);
			if(en_table!=null&&en_table.getVal_src()!=1&&!"".equals(en_table.getMapbm())){
				sSelect.append(en_table.getMapbm().toUpperCase()).append(".MC AS ").append(tHeads[i]);
				sSelect.append(",").append(en_table.getMapbm().toUpperCase()).append(".BM AS ").append(tHeads[i]+"_BM");
				sFrom.append(", (SELECT BM ,MC FROM BM_CONT WHERE BM_CONT.TABLE_BM='"+en_table.getMapbm().toUpperCase()+"')"+en_table.getMapbm().toUpperCase() );
				sWhere.append(mainTb+"."+tHeads[i]+"="+en_table.getMapbm().toUpperCase()+".BM(+) AND ");
			}else{
				sSelect.append(mainTb+"."+tHeads[i]);
			}
			if(i<tHeads.length-1)sSelect.append(",");
		}
		boolean order=(orderFlds!=null);
		if(order){
			//排序方向，默认升序——0：ASC
			if(orderFlds!=null&&orderFlds.length>0){
				sOrderBy.append(" ORDER BY ");
				for(int i=0;i<orderFlds.length;i++){
					En_field en_table=(En_field)mFlds.get(orderFlds[i]);
					if(en_table!=null)continue;
					sOrderBy.append(mainTb+"."+orderFlds[i]);
					int od=0;
					try{
						od=Integer.parseInt(descOrAsc[i]);
					}catch(Exception e){
						
					}
					sOrderBy.append(od==0?" ASC":" DESC");
					if(i<orderFlds.length-1)sOrderBy.append(",");
				}
			}
		}else{//如果有默认的排序设置。
			String dOrderBy = cg.getString("defaultOrderBy");
			if(dOrderBy!=null&&!"".equals(dOrderBy)){
				sOrderBy.append(" ORDER BY ").append(dOrderBy);
			}
		}
		//不同模块加上不同的条件
		switch(enType)
		{
			case(1):{//新增
				//2010-05-07 如果是绍兴发布版本，所谓新增核定列出的，其实是分片编码等于“未知”分片的企业。
				if("shaoxing".equals(cg.getString("dnftLocation"))){
					sWhere.append(" DJ_CZ.CZFPBM IN (SELECT WZFPBM FROM BM_CZFP_UNKNOWN) ");
				}else{
					sWhere.append(" QYZT=1 ");
				}
				break;
			}
			case(2):{//变更
				sWhere.append(" QYZT=2 ");
				break;
			}
			/*case(Constants.CHECK_COMMON):{//日常
				sWhere.append(" ISSWBG=0 AND ISNEW=0 ");
				 break;
			}*/
			default:
				sWhere.append(" 1=1 ");
		}
		//检查乡镇权限
		if(user.getIsManager()!=1){   //如果不是管理员，则检查用户能操作哪些乡镇数据
			List xzs=user.getXzs();
			sFrom.append(",(SELECT BM,PID AS DLBM FROM BM_CONT WHERE TABLE_BM='BM_CZFP') BM_CZFP_QX ");
			sWhere.append(" AND BM_CZFP_QX.BM=DJ_CZ.CZFPBM ");
			if(xzs==null||xzs.size()==0){                        //如果不是管理员，且没有对应乡镇，则没有任何乡镇数据可看
				sWhere.append(" AND 1=2");
			}else{
				sWhere.append(" AND BM_CZFP_QX.BM IN(");
				for(int i=0;i<xzs.size();i++){
					sWhere.append("'");
					sWhere.append(((SimpleValue)xzs.get(i)).getBm());
					if(i!=xzs.size()-1)
						sWhere.append("',");
					else
						sWhere.append("'");
				}
				sWhere.append(")");
			}
		}
		if(ismap>=0){
			sWhere.append(" AND ISMAP=").append(ismap);
		}
		if(qysx>0){
			sWhere.append(" AND QYSX=").append(qysx);
		}
		//没有筛选条件的，sql组织到这里就可以了
		if(flds==null||ops==null||vals==null||rltns==null){
			sql =new StringBuffer("SELECT ").append(sSelect).append(" FROM ").append(sFrom).append(" WHERE ");
			sql.append(sWhere).append(sOrderBy);
		}else{
			String sVal="";
			String sFld="";
			//如果有筛选条件：
			sWhere.append(" AND (");
			String[] tOps=transOpsSymbol(ops);
			String[] tRltns=transRltnsSymbol(rltns);
			for (int i=0;i<flds.length;i++){
				sVal=vals[i]==null?"":vals[i].replace("'", "''");
				sFld=flds[i]==null?"":flds[i].toUpperCase();
				if("IN".equals(tOps[i].toUpperCase())){
					sVal=sVal.replace("|", "','");
					sVal=" ('"+sVal+"') ";
				}else if("LIKE".equals(tOps[i].toUpperCase())){
					sVal=" '%"+sVal+"%' ";
				}else{
					sVal=" '"+sVal+"' ";
				}
				sWhere.append(mainTb).append(".").append(sFld).append(" ").append(tOps[i]).append(" ").append(sVal).append(" ").append(tRltns[i]).append(" ");	
			}
			sWhere.append(")");
			sql = new StringBuffer("SELECT ").append(sSelect).append(" FROM ").append(sFrom).append(" WHERE ").append(sWhere).append(sOrderBy);
		}
		//对导入Excel匹配的处理
		StringBuffer excel = sql;
		if(matchExcel){
			excel = new StringBuffer("SELECT E.* FROM (SELECT * FROM MATCH_ENTERPRISE WHERE S_ID='").append(user.getUserid());
			excel.append("') M,(").append(sql).append(")E WHERE E.SWDJZH=M.SWDJZH");
		}
		return excel.toString();
	}
	/**
	 * 获取表头
	* @param request
	* @return
	 */
	@SuppressWarnings("unchecked")
	private String[] getTableHeads(String[] tHeads,int ismap){
		String[] aHeads=null;
		/*如果request中发现表头参数，则请求的是filter；
		 *如果request中没有表头参数， 字段从字典表中获取。
		 *
		 */
		if(tHeads==null||tHeads.length==0){
			tHeads=cg.getDefaultFldsArray();
		}
		if(tHeads==null||tHeads.length==0){
			tHeads=new String[]{"xh","swdjzh","mc","czfpbm"};
		}
		List heads=new ArrayList();
		boolean hasXh = false,hasSh = false,hasMc = false,hasCzfp = false;
		
		if(ismap>=0){
			heads.add("qynm");
			heads.add("dzdah");
		}
		//检查是否设置了swdjzh字段，这个字段是必要的。
		for(int i=0;i<tHeads.length;i++){
			if("xh".equals(tHeads[i].toLowerCase())){
				hasXh = true;
			}
			if("swdjzh".equals(tHeads[i].toLowerCase())){
				hasSh = true;
			}
			if("mc".equals(tHeads[i].toLowerCase())){
				hasMc = true;
			}
			if("czfpbm".equals(tHeads[i].toLowerCase())){
				hasCzfp = true;
			}
			heads.add(tHeads[i]);	
		}
		//这四个字段如果没有配置，必须补上
		if(!hasXh)heads.add("xh");
		if(!hasSh)heads.add("swdjzh");
		if(!hasMc)heads.add("mc");
		if(!hasCzfp)heads.add("czfpbm");
		
		aHeads=new String[heads.size()];
		for(int i=0;i<heads.size();i++){
			aHeads[i]=(String)heads.get(i);
		}
		return aHeads;
	}
	/**
	 * 将请求中的操作符参数转化为实际的参数
	 * @param ops
	 * @return
	 */
	private String[] transOpsSymbol(String[] ops){
		String[] sTrans=null;
		if (ops==null) return null;
		sTrans=new String[ops.length];
		for(int i=0;i<ops.length;i++){
			if("equ".equals(ops[i]))sTrans[i]="=";
			if("gt".equals(ops[i]))sTrans[i]=">";
			if("lt".equals(ops[i]))sTrans[i]="<";
			if("gt_e".equals(ops[i]))sTrans[i]=">=";
			if("lt_e".equals(ops[i]))sTrans[i]="<=";
			if("not_e".equals(ops[i]))sTrans[i]="<>";
			if("like".equals(ops[i]))sTrans[i]="LIKE";
			if("in".equals(ops[i]))sTrans[i]="IN";	
		}
		return sTrans;
	}
	/**
	 * 将请求中的关系符参数转化为实际的参数
	 * @param rltns
	 * @return
	 */
	private String[] transRltnsSymbol(String[] rltns){
		String[] sTrans=null;
		if (rltns==null) return null;
		sTrans=new String[rltns.length];
		for(int i=0;i<rltns.length;i++){
			if("empty".equals(rltns[i]))sTrans[i]="";
			if("_and".equals(rltns[i]))sTrans[i]="AND";
			if("_or".equals(rltns[i]))sTrans[i]="OR";
			if("andL".equals(rltns[i]))sTrans[i]="AND (";
			if("orL".equals(rltns[i]))sTrans[i]="OR (";
			if("Rand".equals(rltns[i]))sTrans[i]=") AND";
			if("Ror".equals(rltns[i]))sTrans[i]=") OR";
			if("RandL".equals(rltns[i]))sTrans[i]=") AND (";	
			if("RorL".equals(rltns[i]))sTrans[i]=") OR (";
			if("RBr".equals(rltns[i]))sTrans[i]=")";	
		}
		return sTrans;
	}
	/**
	 * 获取单户企业的信息
	* @param enid
	* @param infoType 要获取的登记类型。0:财政,1:地税,2:国税,9:全部
	* @return
	 */
	@SuppressWarnings("unchecked")
	@DirectMethod
	public String getEnDjInfo(int enid,int infoType){
		StringBuffer json = new StringBuffer("{");
		String fnSql = buildEnInfoSql(String.valueOf(enid),"DJ_CZ");
		Map fEn = ckDao.getEnInfo(fnSql);
		String qynm = fEn==null?"0":(String)fEn.get("QYNM");
		String dzdah = fEn==null?"0":(String)fEn.get("DZDAH");
		Map dsEn = null;
		if(infoType==1||infoType==9){
			//获取地税信息
			String dsSql = buildEnInfoSql(qynm,"DJ_DS"); 
			if(!StringUtils.isEmpty(dsSql)){
				dsEn = ckDao.getEnInfo(dsSql); 
			}
		}
		Map gsEn = null;
		if(infoType==2||infoType==9){
			//获取国税信息
			String gsSql = buildEnInfoSql(dzdah,"DJ_GS"); 
			if(!StringUtils.isEmpty(gsSql)){
				gsEn = ckDao.getEnInfo(gsSql);
			}
		}
		try{
			Map dictionary = cg.getDJFieldsToShow();
			if(fEn!=null){
				List flds = (List)dictionary.get("DJ_CZ");
				if(flds!=null&&flds.size()>0){
					json.append("finance:");
					json.append(JsonHelper.getJsonHelper().toJSONString(fEn));
				}
			}
			if(dsEn!=null){
				List flds = (List)dictionary.get("DJ_DS");
				if(flds!=null&&flds.size()>0){
					json.append(",ds:");
					json.append(JsonHelper.getJsonHelper().toJSONString(dsEn));
				}
			}
			if(gsEn!=null){
				List flds = (List)dictionary.get("DJ_GS");
				if(flds!=null&&flds.size()>0){
					json.append(",gs:");
					json.append(JsonHelper.getJsonHelper().toJSONString(gsEn));
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		json.append("}");
		return json.toString();
	}
	
	@SuppressWarnings("unchecked")
	private String buildEnInfoSql(String enid,String djType){
		StringBuffer sql = new StringBuffer("select ");
		StringBuffer from = new StringBuffer(" from ").append(djType);
		StringBuffer where = new StringBuffer(" where ");
		if("DJ_CZ".equals(djType)){
			sql.append("qynm,dzdah,");
			where.append(" xh=").append(enid);
		}else if("DJ_DS".equals(djType)){
			where.append(" qynm='").append(enid).append("'");
		}else{
			where.append(" dzdah='").append(enid).append("'");
		}
		Map dictionary = cg.getDJFieldsToShow();
		List flds = (List)dictionary.get(djType);
		if(flds==null||flds.size()==0){
			return "";
		}
		for(int i=0;i<flds.size();i++){
			En_field fld = (En_field)flds.get(i);
			if(fld==null){
				continue;
			}
			if(fld.getVal_src()==2){
				sql.append(djType).append(".").append(fld.getField());
				sql.append(",").append(fld.getMapbm()).append(".MC AS ").append(fld.getField()).append("_MC");
				from.append(",(select * from bm_cont where table_bm='").append(fld.getMapbm()).append("') ").append(fld.getMapbm());
				where.append(" and ").append(fld.getField()).append("=").append(fld.getMapbm()).append(".BM(+)");
			}else{
				sql.append(djType).append(".").append(fld.getField());
			}
			if(i<flds.size()-1){
				sql.append(",");
			}
		}
		sql.append(from).append(where);
		return sql.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public List getBmCodesTree(String nodeid,String table,String value){
		List results = null;
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		StringBuffer sql = new StringBuffer("select to_char(bm) id,to_char(nvl(pid,-1))pid,mc||'('||bm||')' text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded");
		sql.append(" from bm_cont where table_bm='").append(table).append("' and to_char(nvl(pid,-1)) ");
		//如果是构造分片树且不是管理员，检查乡镇权限
		if("BM_CZFP".equals(table)&&user.getIsManager()!=1){
			sql = new StringBuffer("select distinct * from(select to_char(bm)id,to_char(nvl(pid,-1))pid,");
			sql.append("mc||'('||bm||')' text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded");
			sql.append(" from (select * from bm_cont where table_bm='BM_CZFP') a start with  bm in ");
			sql.append("(select czfpbm from user_xz where userid='").append(user.getUserid()).append("') connect by prior pid = bm)");
			sql.append(" where to_char(nvl(pid,-1)) ");
		}
		if(nodeid==null||"tree-root".equals(nodeid)||"-1".equals(nodeid)){
			//sql.append(" is null");
			sql.append("='-1'");
		}else{
			sql.append("='").append(nodeid).append("'");
		}
		sql.append(" order by id");
		List nodes = ckDao.queryForList(sql.toString(),TreeNode.class);
		if(nodes!=null&&nodes.size()>0){
			results = new ArrayList();
			for(int i=0;i<nodes.size();i++){
				CheckTreeNode cn = new CheckTreeNode();
				TreeNode n=(TreeNode)nodes.get(i);
				cn.setId(n.getId());
				cn.setPid(n.getPid());
				cn.setText(n.getText());
				cn.setLeaf(n.isLeaf());
				cn.setCls(n.getCls());
				if(n.getId().equals(value)){
					cn.setChecked(true);
				}else{
					cn.setChecked(false);
				}
				results.add(cn);
			}
		}
		return results; 
	}
	@DirectMethod
	public String getBmPath(String table,String vals){
		StringBuffer result=new StringBuffer("{pathes:");
		if(vals!=null&&!"".equals(vals)){
			String[] arrVals = vals.split(",");
			JSONArray jpathes = new JSONArray();
			for(int i=0;i<arrVals.length;i++){
				String val = arrVals[i];
				StringBuffer sql = new StringBuffer("select to_char(bm)bm,mc from (select * from bm_cont ");
				sql.append(" where table_bm='").append(table).append("') connect by prior pid=bm start with bm='").append(val).append("' order by level desc");
				String path = getFullPath(sql.toString());
				jpathes.put(path);
			}
			try{
				result.append(JsonHelper.getJsonHelper().toJSONString(jpathes));
			}catch(Exception e){
			}
		}
		result.append("}");
		return result.toString();
	}
	/**
	 * 组织编码全路径
	* @param sql
	* @return
	 */
	@SuppressWarnings("unchecked")
	private String getFullPath(String sql){
		String path="";
		List lst = ckDao.queryForList(sql,Code.class);
		if(lst!=null&&lst.size()>0){
			StringBuffer sp =new StringBuffer("");
		    for(int j=0;j<lst.size();j++){
			    Code pCode=(Code)lst.get(j);
			    sp.append(pCode.getBm());
			    if(j<lst.size()-1){
			    	sp.append("/");
			    }
		    }
		    path = sp.toString();
	    }
		return path;
	}
	/**
	 * 核定
	* @param enids
	* @param fldName
	* @param newVal
	* @param affect
	* @param from
	* @param to
	* @param executeType
	* @param affactMode
	* @param remark
	* @return
	 */
	@DirectMethod
	public String check(String enids,String fldName,String newVal,int affect,int from,int to,int executeType,int affactMode,String checkType,String remark){
		StringBuffer result = new StringBuffer("{result:");
		if(enids==null||"".equals(enids)){
			result.append("'0'}");
			return result.toString();
		}
		WebContext context = WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		String cUser = (String)request.getSession().getAttribute("userid");
		String ip  =  request.getHeader("x-forwarded-for");
    	if (ip == null||ip.length()== 0 ||"unknown".equalsIgnoreCase(ip))  {
    		ip = request.getHeader("Proxy-Client-IP");
    	} 
    	if (ip == null||ip.length()== 0||"unknown" .equalsIgnoreCase(ip))  {
    		ip = request.getHeader("WL-Proxy-Client-IP");
    	} 
    	if (ip == null||ip.length()== 0||"unknown".equalsIgnoreCase(ip))  {
    		ip = request.getRemoteAddr();
    	} 
    	//String mac = getMacAddress(ip);
    	String mac ="";
		String[] arrEnids = enids.split(",");
		String proName = cg.getString("pro_Check","PKG_CHECK.EN_CHECK");
		String sql = "{call "+proName+"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}"; 
		String[] doneinfo = new String[]{"0",""};
		for(int i = 0;i<arrEnids.length;i++){
			String xh = arrEnids[i];
			doneinfo = ckDao.excuteCheck(sql,cUser,xh,fldName,newVal,affect,from,to,affactMode,executeType,ip,mac,checkType,remark);
			if(doneinfo==null||!"1".equals(doneinfo[0])){
				break;
			}
		}
		result.append("'").append(doneinfo[0]).append("',info:'").append(StringUtils.isEmpty(doneinfo[1])?"":doneinfo[1]);
		result.append("'}");
		return result.toString();
	}
	public static String getMacAddress_0(String host){  
        String mac = "";  
        StringBuffer sb = new StringBuffer();  
        try{  
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(host));  
            byte[] macs = ni.getHardwareAddress();  
            if(macs!=null){
	            for(int i=0; i<macs.length; i++){  
	                mac = Integer.toHexString(macs[i] & 0xFF);   
	                 if (mac.length() == 1){   
	                     mac = '0' + mac;   
	                 }   
	                sb.append(mac + "-");  
	            }  
            }
        }catch (SocketException e){  
            e.printStackTrace();  
        }catch (UnknownHostException e){  
            e.printStackTrace();  
        }  
        mac = sb.toString();  
        mac = mac.length()>0?mac.substring(0, mac.length()-1):"";  
        return mac;  
    } 
	public String getMacAddress(String ip){
		String str = ""; 
        String macAddress = ""; 
        try { 
            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip); 
            InputStreamReader ir = new InputStreamReader(p.getInputStream()); 
            LineNumberReader input = new LineNumberReader(ir); 
            for (int i = 1; i < 100; i++) { 
                str = input.readLine(); 
                if (str != null) { 
                    if (str.indexOf("MAC Address") > 1) { 
                        macAddress = str.substring(str.indexOf("MAC Address") + 14, str.length()); 
                        break; 
                    } 
                } 
            } 
        } catch (IOException e) { 
            e.printStackTrace(System.out); 
        } 
		return macAddress;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	/**
	 * 获取所有批量核定字段。showmod==3
	 */
	public List getBatchCheckFields(){
		List flds = null;
		Map showFlds = cg.getDJFieldsToShow();
		if(showFlds!=null&&showFlds.containsKey("DJ_CZ")){
			List lst = (List)showFlds.get("DJ_CZ");
			if(lst!=null&&lst.size()>0){
				flds = new ArrayList();
				for(int i = 0;i<lst.size();i++){
					En_field fld = (En_field)lst.get(i);
					if(fld.getShowmod()==3){
						flds.add(fld);
					}
				}
			}
		}
		return flds;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	/**
	 * 获得所有登记字段，用于筛选
	 */
	public List getFieldsFilter(String usage){
		List flds = null;
		List tmpFlds = null;
		Map allFlds = cg.getDJDictionary();
		if(allFlds!=null&&allFlds.containsKey("DJ_CZ")){
			tmpFlds = (List)allFlds.get("DJ_CZ");
		}
		if(tmpFlds!=null){
			flds = new ArrayList();
			for(int i=0;i<tmpFlds.size();i++){
				En_field fld = (En_field)tmpFlds.get(i);
				String fn = fld.getField().toLowerCase();
				if("header".equals(usage)){//做列头可选字段时，剔除必显示的字段
					if("swdjzh".equals(fn)||"mc".equals(fn)||
							"czfpbm".equals(fn)||"fddbr".equals(fn)||"dz".equals(fn)){
						continue;
					}
				}
				flds.add(fld);
			}
		}
		return flds;
	}
	@DirectMethod
	public List getFields2Show(){
		List flds = new ArrayList();
		En_field fld = new En_field("SWDJZH","税号");
		flds.add(fld);
		fld = new En_field("MC","名称");
		flds.add(fld);
		fld = new En_field("DZ","地址");
		flds.add(fld);
		fld = new En_field("FDDBR","法定代表人");
		flds.add(fld);
		fld = new En_field("CZFPBM","财政分片");
		flds.add(fld);
		return flds;
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public String rebuildEnGrid(String heads){
		JSONObject obj = new JSONObject();
		String[] paraHeads = (heads==null||"".equals(heads))?null:heads.split(",");
		String[] arrHeads = getTableHeads(paraHeads,-1);
		Map fldsMap = cg.getFinanceFieldsMap();
		List fields = new ArrayList();
		for(int i=0;i<arrHeads.length;i++){
			String h = arrHeads[i];
			En_field fld = (En_field)fldsMap.get(h);
			fields.add(fld);
		}
		try{
			JSONArray cols = buildColumnModel(fields);
			obj.put("columnModel", cols);
			JSONArray sts = buildStore(fields);
			obj.put("store",sts);
		}catch(Exception e){
			log.error(e.toString());
		}
		return obj.toString();
	}
	@SuppressWarnings("unchecked")
	private JSONArray buildStore(List fields){
		JSONArray jarr = new JSONArray();
		try{
			JSONObject jo = new JSONObject();
			jo.put("name","xh");
			jo.put("type", "int");
			jarr.put(jo);
			for(int i=0;i<fields.size();i++){
				jo = new JSONObject();
				En_field fld=(En_field)fields.get(i);
				if(fld==null||"xh".equals(fld.getField().toLowerCase())){
					continue;
				}
				jo.put("name",fld.getField().toLowerCase());
				jo.put("type", "string");
				jarr.put(jo);
			}
		}catch(Exception e){
		}
		return jarr;
	}
	/**
	 * 动态构造导入表的表头
	* @param dfMap
	* @param extInfos
	* @return
	 */
	@SuppressWarnings("unchecked")
	private JSONArray buildColumnModel(List fields){
		JSONArray jarr = new JSONArray();
		try{
			for(int i=0;i<fields.size();i++){
				JSONObject ejo = new JSONObject();
				En_field df=(En_field)fields.get(i);
				if(df==null){
					continue;
				}
				ejo.put("header", df.getMc());
				ejo.put("dataIndex",df.getField().toLowerCase());
				ejo.put("align", "left");
				ejo.put("width","100");
				jarr.put(ejo);
			}
		}catch(Exception e){
		}
		return jarr;
	}
	
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult importEnExcel(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();		
		FileItem fi = (FileItem)fileFields.get("filepath");
		String path = ImpExcelHelper.getImpExcelHelper().saveEnExcel(fi,"filePathCheck");
		if(path==null||"".equals(path)){
			result.setSuccess(false);
			errors.put("msg", "服务端保存文件失败！");
			result.setErrors(errors);
			return result;
		}
		String sMatchCol = (String)params.get("matchCol");
		String sBeginRow = (String)params.get("beginRow");
		String d_type = (String)params.get("type");
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
		    //去除导入的数据重复
		    //HashSet h = new HashSet(matchFlds);
		    //matchFlds.clear();
		    //matchFlds.addAll(h);
		    Map infos = new HashMap();
		    WebContext context = WebContextManager.get();
			HttpServletRequest request = context.getRequest();
			String userid = (String)request.getSession().getAttribute("userid");
		    //获取匹配后的结果
		    int rcount = ckDao.matchData(matchFlds,userid,d_type);
		    strResult=new StringBuffer("本次共导入Excel文件有效行数为");
		    strResult.append(matchFlds.size()).append("行，匹配成功");
		    strResult.append(rcount).append("行。");
		    if(matchFlds.size()>rcount){
		    	infos.put("noMatchCount",String.valueOf((matchFlds.size()-rcount)));
		    }
		    //2014-04-29 下沙需求，导入的企业需要标注出非本级的（即已经被划归到乡镇）企业。
		    //增加判断，如果设置了本级编码，就表示有这项业务，即需要提示多少企业是已经核定过的（非本级）
		    if(!StringUtils.isEmpty(cg.getString("czfpbm_BJ"))){
		    	//转换数据
		    	StringBuffer csql = new StringBuffer("select m.* from match_enterprise m,(select d.* from dj_cz d,(select max(xh)xh,");
		    	csql.append("sh".equals(d_type)?"swdjzh":"mc");
		    	csql.append(" from dj_cz group by ").append("sh".equals(d_type)?"swdjzh":"mc");
		    	csql.append(")x where d.xh=x.xh)e");
				if("sh".equals(d_type)){
					csql.append(" where m.swdjzh=e.swdjzh");
				}else{
					csql.append(" where m.qymc=e.mc ");
				}
				csql.append(" and czfpbm <> '").append(cg.getString("czfpbm_BJ")).append("' and s_id='").append(userid).append("'");
		    	int count = ckDao.queryCount(csql.toString());
		    	infos.put("bjCount", String.valueOf(count));
		    }
			result.setSuccess(true);
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
	
	/**
	 * 接受新增或变更
	* @param swdjzhs
	* @return
	 */
	@DirectMethod
	public String acceptEn(String xhs,int aType){
		StringBuffer result = new StringBuffer("{result:");
		if(xhs==null||xhs.equals("")){
			result.append("true}");
			return result.toString();
		}
		String[] shs = xhs.split(",");
		boolean done = ckDao.acceptEnterprise(shs,aType);
		result.append(done).append("}");
		return result.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public List getEnChangeInfo(int xh){
		List infos = null;
		infos = ckDao.getChangeInfos(xh);
		return infos;
	}
	@DirectMethod
	public String checkChangeEn(int xh,String fld,String newVal){
		StringBuffer result = new StringBuffer("{result:");
		if(xh==0){
			result.append("true}");
			return result.toString();
		}
		boolean done = ckDao.checkChangeEn(xh,fld,newVal);
		result.append(done).append("}");
		return result.toString();
	}
	
	@DirectMethod
	public List getComboBms(String tbname){
		List bms = null;
		if(tbname==null){
			return null;
		}
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		StringBuffer sql = new StringBuffer("select bm,mc from bm_cont where table_bm='");
		sql.append(tbname).append("'");
		if("BM_CZFP".equals(tbname)&&user.getIsManager()!=1){
			sql = new StringBuffer("select a.bm,a.mc from (select * from bm_cont where table_bm='BM_CZFP')a,");
			sql.append("(select czfpbm from user_xz where userid='").append(user.getUserid()).append("') b where ");
			sql.append(" a.bm=b.czfpbm");
		}
		bms = ckDao.queryForList(sql.toString(),SimpleValue.class);
		return bms;
	}
	@DirectMethod
	public String getCheckLogInfo(int xh){
		StringBuffer json = new StringBuffer("{success:");
		Map info = null;
		StringBuffer sql = new StringBuffer("select hid,fldname,newval,oldval,yxrtk,");
		sql.append("decode(yxrtk,0,'不影响',1,'影响所有',2,'影响部分')yxrtkname,");
		sql.append("fromday,today,yxtype,decode(yxtype,0,'是','否')yxtypename,");
		sql.append("hdlx,c.mc hdlxname,b.name username,doip,domac,to_char(dotime,'YYYY-MM-DD HH24:MI:SS')dotime,remark from hd_info a,");
		sql.append("(select * from users)b,(select * from bm_cont where table_bm='BM_HDLX')c where a.userid=b.userid(+)");
		sql.append(" and to_char(a.hdlx)=c.bm(+) and a.hid=").append(xh);
		info = ckDao.getCheckLogInfo(sql.toString());
		try{
			json.append("true,data:").append(JsonHelper.getJsonHelper().toJSONString(info));
		}catch(Exception e){
			log.error(e.toString());
		}
		json.append("}");
		return json.toString();
	}
	
	public static void main(String[] args)throws IOException{
		String ip = "192.168.1.32";
		String str = ""; 
        String macAddress = ""; 
        try { 
            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip); 
            InputStreamReader ir = new InputStreamReader(p.getInputStream()); 
            LineNumberReader input = new LineNumberReader(ir); 
            for (int i = 1; i < 100; i++) { 
                str = input.readLine(); 
                if (str != null) { 
                    if (str.indexOf("MAC Address") > 1) { 
                        macAddress = str.substring(str.indexOf("MAC Address") + 14, str.length()); 
                        break; 
                    } 
                } 
            } 
        } catch (IOException e) { 
            e.printStackTrace(System.out); 
        } 
		System.out.println(macAddress);
	}
}
