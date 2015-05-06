package com.ifugle.dft.system.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.ifugle.dft.system.dao.CodeDao;
import com.ifugle.dft.system.entity.AidItem;
import com.ifugle.dft.system.entity.CheckTreeNode;
import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.system.entity.CodeTable;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.entity.SimpleValue;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.ifugle.dft.utils.entity.TreeNode;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import org.json.*;

public class CodeHandler {
	private static Logger log = Logger.getLogger(CodeHandler.class);
	private CodeDao cdao;
	public CodeHandler(){
		cdao = (CodeDao)ContextUtil.getBean("codeDao");
	}
	@DirectMethod
	public List getCodeTables(int who){
		List tbs = null;
		StringBuffer sql = new StringBuffer("select table_bm,name,qybj,remark from ");
		if(who<9){
			sql.append("bm_index_tax where who=").append(who);
		}else{
			sql.append("bm_index");
		}
    	tbs = cdao.queryForList(sql.toString(),CodeTable.class);
		return tbs ;
	}
	
	@DirectMethod
	public String deleteCodeTable(String table_bm,int who){
		StringBuffer result = new StringBuffer("{success:");
		boolean done = false;
		try{
			done = cdao.deleteCodeTable(table_bm,who);
			result.append(done);
			result.append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
		}
		return result.toString();
	}
	@DirectFormPostMethod
	public SubmitResult saveCodeTable(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();		
		try{
			String sWho = (String)params.get("who");
			String table = "bm_index";
			int who = 9;
			try{
				who = Integer.parseInt(sWho);
			}catch(Exception e){
				who=9;
			}
			if(who==0||who==1){
				table = "bm_index_tax";
			}
			String table_bm = (String)params.get("table_bm");
			String mc = (String)params.get("name");
			String remark = (String)params.get("remark");
			String sqy = (String)params.get("qybj");
			int qybj = 0;
			if(sqy!=null&&!"".equals(sqy)&&!"false".equals(sqy)){
				qybj= 1;
			}
			String cMode = (String)params.get("cMode");
			//新增时检查是否编码重复
			if(cMode!=null&&"add".equals(cMode)){
				String dpSql = "select table_bm from "+table+" where table_bm='"+table_bm+"'";
				int count = cdao.queryCount(dpSql);
				if(count>0){
					result.setSuccess(false);
					errors.put("table_bm", "当前编码ID已存在，不能重复！");
					result.setErrors(errors);
					return result;
				}
			}
			StringBuffer sql = new StringBuffer("update ");
			sql.append(table).append(" set name=?,qybj=?,remark=? where table_bm=?");
			Object[] paras = new Object[]{mc,new Integer(qybj),remark,table_bm};
			if(cMode!=null&&"add".equals(cMode)){
				sql = new StringBuffer("insert into ").append(table).append("(name,qybj,remark,table_bm");
				if(who==0||who==1){
					sql.append(",who");
				}
				sql.append(")values(?,?,?,?");
				if(who==0||who==1){
					sql.append(",").append(who);
				}
				sql.append(")");
			}
			cdao.doUpdate(sql.toString(), paras);
			result.setSuccess(true);
			Map infos = new HashMap();
			infos.put("msg", "保存编码表成功！");
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存编码表时发生错误："+e.toString());
		}
		return result;
	}
	/**
	 * 删除编码表的对应关系
	* @param table 选择的税务编码表名
	* @param who 来源，0/1：地税/国税
	* @return
	 */
	@DirectMethod
	public String deleteCodeTableMapping(String table,int who){
		StringBuffer result = new StringBuffer("{success:");
		try{
			Object[] paras = new Object[]{table,new Integer(who)};
			String sql="delete from bm_table_map where t_table=? and who=?";
			cdao.doUpdate(sql, paras);
			result.append("true}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
		}
		return result.toString();
	}
	/**
	 * 保存编码表对应关系
	* @param table
	* @param ftable
	* @param who
	* @return
	 */
	@DirectMethod
	public String saveCodeTableMapping(String table,String ftable,int who){
		StringBuffer result = new StringBuffer("{success:");
		boolean done = false;
		try{
			done = cdao.saveCodeTableMapping(table,ftable,who);
			result.append(done);
			result.append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
		}
		return result.toString();
	}
	/**
	 * 获取财政编码树
	* @param nodeid 当前树节点
	* @param table 编码表名
	* @param pid 父节点id
	* @param mapDir 映射方向。如果是财政-税务映射，财政树不需要带复选框，否则需要复选框
	* @return
	 */
	@DirectMethod
	public List getFCodesTree(String nodeid,String table,String pid,int mapDir){
		List nodes = null;
		StringBuffer sql = new StringBuffer("select bm id,pid,mc||'('||bm||')' text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded");
		sql.append(" from bm_cont where table_bm='").append(table).append("' and pid ");
		if(pid==null||"".equals(pid)){
			sql.append(" is null");
		}else{
			sql.append("='").append(pid).append("'");
		}
		sql.append(" order by bm");
		nodes = cdao.queryForList(sql.toString(),TreeNode.class);
		List nds=nodes; 
		if(mapDir!=9&&nodes!=null){
			nds = new ArrayList();
			for(int i=0;i<nodes.size();i++){
				CheckTreeNode cn = new CheckTreeNode();
				TreeNode n=(TreeNode)nodes.get(i);
				cn.setId(n.getId());
				cn.setPid(n.getPid());
				cn.setText(n.getText());
				cn.setLeaf(n.isLeaf());
				cn.setCls(n.getCls());
				cn.setChecked(false);
				nds.add(cn);
			}
		}
		return nds;
	}

	/**
	 * 获取税务编码树
	* @param nodeid 页面默认传递的节点id，当前点击的节点id
	* @param table 指定编码表
	* @param who (0/1)地税/国税
	* @param pid 父节点id
	* @param mapDir 映射方向(0:地税-财政,1:国税-财政,9:财政-税务)。财政-税务映射，税务树都需要带复选框；税务-财政：税务树不需要复选框。
	* @return
	 */
	@DirectMethod
	public List getTCodesTree(String nodeid,String fTable,int who,String pid,int mapDir){
		List nodes = null;
		//String table = getMappedTaxTable(fTable,who);
		StringBuffer sql = new StringBuffer("select bm id,mc||'('||bm||')' text,pid,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded from ");
		sql.append("bm_cont_tax WHERE table_bm='").append(fTable).append("' and who=").append(who);
		sql.append(" and pid ");
		if(pid==null||"".equals(pid)){
			sql.append(" is null");
		}else{
			sql.append("='").append(pid).append("'");
		}
		sql.append(" order by bm");
		nodes = cdao.queryForList(sql.toString(),TreeNode.class);
		List nds=nodes; 
		if(mapDir==9&&nodes!=null){
			nds = new ArrayList();
			for(int i=0;i<nodes.size();i++){
				CheckTreeNode cn = new CheckTreeNode();
				TreeNode n=(TreeNode)nodes.get(i);
				cn.setId(n.getId());
				cn.setPid(n.getPid());
				cn.setText(n.getText());
				cn.setLeaf(n.isLeaf());
				cn.setCls(n.getCls());
				cn.setChecked(false);
				nds.add(cn);
			}
		}
		return nds;
	}
	/**
	 * 根据指定的编码表名和编码获取编码详情
	* @param table
	* @param bm
	* @return
	 */
	@DirectMethod
	public Code getCode(String table,int who,String bm){
		Code cd = null;
		String mainTb = "bm_cont";
		if(who<9){
			mainTb="bm_cont_tax";
		}
		StringBuffer sql = new StringBuffer("select table_bm,bm,mc,pid,qybj,isleaf,codelevel from ").append(mainTb).append(" where table_bm='");
		sql.append(table).append("' and bm='").append(bm).append("'");
		cd = cdao.getCode(sql.toString());
		return cd;
	}
	/**
	 * 删除指定的编码
	 */
	@DirectMethod
	public String deleteCode(String ftable,String table,String bm,int who,String pid){
		StringBuffer result = new StringBuffer("{success:");
		boolean done = cdao.deleteCode(ftable,table,bm,who,pid);
		result.append(done).append("}");
		return result.toString();
	}
	/**
	 * 保存编码
	* @param params
	* @param fileFields
	* @return
	 */
	@DirectFormPostMethod
	public SubmitResult saveCode(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();
		String table_bm = (String)params.get("table_bm");
		String bm = (String)params.get("bm");
		boolean done = false;
		try{
			done = cdao.saveCode(params,fileFields);
			result.setSuccess(done);
			Map infos = new HashMap();
			infos.put("msg", "保存编码成功！");
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存编码时发生错误，编码表:"+table_bm+",编码:"+bm+"。错误："+e.toString());
		}
		return result; 
	}
	/**
	 * 获取映射的编码的展开路径
	* @param mapDir 映射方向。0：地税-财政；1：国税-财政；9：财政-税务
	* @param ftable 财政编码表
	* @param bm 要映射的编码
	* @return
	 */
	@DirectMethod
	public String getMappingPath(int mapDir,String ftable,String bm){
		StringBuffer result=new StringBuffer("{map:1");
		if(mapDir<9){
			String path = getMappingBmT2F(ftable,bm,mapDir);
			result.append(",fPath:'").append(path).append("'");
		}else{
			String mapTable = getMappedTaxTable(ftable,0);
			String path = getMappingBmF2T(ftable,mapTable,bm,0);
			result.append(",dPaths:").append(path).append("");
			mapTable = getMappedTaxTable(ftable,1);
			path = getMappingBmF2T(ftable,mapTable,bm,1);
			result.append(",gPaths:").append(path).append("");
		}
		result.append("}");
		return result.toString();
	}
	//获取财政-税务映射编码的路径
	private String getMappingBmF2T(String table,String tTable,String bm,int who){
		JSONArray paths = new JSONArray();
		String mbm="";
		StringBuffer sql = new StringBuffer("select to_char(t_bm) bm from bm where name='");
		sql.append(table).append("' and to_char(f_bm)='").append(bm).append("' and who=").append(who);
		List lst = cdao.queryForList(sql.toString(),Code.class);
		if(lst!=null&&lst.size()>0){
			for(int i=0;i<lst.size();i++){
				Code pCode=(Code)lst.get(i);
				mbm = pCode.getBm();
				sql = new StringBuffer("select to_char(bm)bm,mc from bm_cont_tax ");
				sql.append(" where table_bm='").append(tTable).append("' connect by prior pid=bm start with to_char(bm)='").append(mbm).append("' order by level desc");
				String path = getFullPath(sql.toString());
				paths.put(path);
			}
		}
		return paths.toString();
	}
	//获取税务-财政映射编码的路径
	private String getMappingBmT2F(String table,String bm,int who){
		String path ="";
		StringBuffer sql = new StringBuffer("select to_char(f_bm) bm from bm where name='");
		sql.append(table).append("' and to_char(t_bm)='").append(bm).append("' and who=").append(who);
		List lst = cdao.queryForList(sql.toString(),Code.class);
		if(lst!=null&&lst.size()>0){
			Code pCode=(Code)lst.get(0);
			String mbm = pCode.getBm();
			sql = new StringBuffer("select to_char(bm)bm,mc from bm_cont ");
			sql.append(" where table_bm='").append(table).append("' connect by prior pid=bm start with bm='").append(mbm).append("' order by level desc");
			path = getFullPath(sql.toString());
		}
		return path;
	}
	/**
	 * 组织编码全路径
	* @param sql
	* @return
	 */
	private String getFullPath(String sql){
		String path="";
		List lst = cdao.queryForList(sql,Code.class);
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
	 * 根据名称模糊查找节点
	* @param table 所在的编码表
	* @param who 来源，国、地、财政
	* @param cont 匹配内容
	* @param startid 从哪个节点起始向后查找
	* @return
	 */
	@DirectMethod
	public String searchForCode(String table,int who,String cont,String startid){
		StringBuffer result = new StringBuffer("{");
		String path="";
		String mainTb = "bm_cont";
		if(who<9){
			mainTb ="bm_cont_tax";
		}
		String findBm=null;
		StringBuffer treeView = new StringBuffer("select rownum as r,o.* from (select bm,mc,pid from (select * from ").append(mainTb);
		treeView.append(" where table_bm='").append(table).append("') connect by prior bm=pid start with pid is null)o");
		StringBuffer sql = new StringBuffer("select to_char(b.bm)bm,to_char(b.pid)pid,b.mc from( ");
		sql.append(treeView);
		sql.append(") b where mc").append(" like '%").append(cont).append("%' and rownum=1");
		if(startid !=null&&!"".equals(startid)){
			sql.append(" and r>(select a.r from(").append(treeView).append(")a where to_char(bm)='").append(startid).append("')");
		}
		try{
			List lst = cdao.queryForList(sql.toString(),Code.class);
			if(lst!=null&&lst.size()>0){
				Code pCode=(Code)lst.get(0);
				findBm =pCode.getBm();
		    }
			if(findBm!=null&&!"".equals(findBm)){
				sql = new StringBuffer("select to_char(bm)bm,mc from ").append(mainTb);
				sql.append(" where table_bm='").append(table).append("' connect by prior pid=bm start with to_char(bm)='").append(findBm).append("' order by level desc");
				path = getFullPath(sql.toString());
				result.append("match:'yes',path:'").append(path).append("'}");
			}else{
				result.append("match:'no'}");
			}
		}catch (Throwable e) {
			log.error(e.toString());
		}
		return result.toString();
	}
	@DirectMethod
	public String getNotMappingCount(){
		StringBuffer result = new StringBuffer("{");
		int ds = 0,gs = 0;
		try{
			StringBuffer sql = new StringBuffer("select bm from bm_cont_tax where who=0 and status=0");
			ds = cdao.queryCount(sql.toString());
			sql = new StringBuffer("select bm from bm_cont_tax where who=1 and status=0");
			gs = cdao.queryCount(sql.toString());
		}catch(Exception e){
			log.error(e.toString());
		}
		result.append("ds:").append(ds).append(",gs:").append(gs);
		result.append("}");
		return result.toString();
	}
	
	@DirectMethod
	public Map getNotMappedTaxCodes(int who,int start,int limit){
		Map infos = new HashMap();
		List bms = null;
		try{
			StringBuffer sql = new StringBuffer("select table_bm,to_char(bm)bm,mc from bm_cont_tax where ");
			sql.append(" status=0 and who=").append(who).append(" order by table_bm");
			int count = cdao.queryCount(sql.toString());
			infos.put("totalCount", new Integer(count));
			bms = cdao.queryForPage(sql.toString(), start, limit, Code.class);
			infos.put("rows", bms);
			if(bms!=null&&bms.size()>0){
				for(int i=0;i<bms.size();i++){
					Code cd = (Code)bms.get(i);
					//从税务端的BM_CONT_TAX获取全路径
					StringBuffer pSql = new StringBuffer("select table_bm,to_char(bm)bm,mc from bm_cont_tax where table_bm='");
					pSql.append(cd.getTable_bm());
					pSql.append("' connect by prior pid=bm start with to_char(bm)='").append(cd.getBm()).append("' order by level desc");
					List lst = cdao.queryForList(pSql.toString(),Code.class);
					String path="", pName="";
					if(lst!=null&&lst.size()>0){
						StringBuffer sp =new StringBuffer("");
						StringBuffer spn =new StringBuffer("");
					    for(int j=0;j<lst.size();j++){
						    Code pCode=(Code)lst.get(j);
						    sp.append(pCode.getBm());
						    spn.append(pCode.getMc());
						    if(j<lst.size()-1){
						    	sp.append("/");
						    	spn.append("/");
						    }
					    }
					    path = sp.toString();
					    pName = spn.toString();
					    cd.setFullPath(path);
					    cd.setFpName(pName);
				    }
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}	
		return infos;
	}
	
	@DirectMethod
	public String saveMappingF2T(String table,String dsTable,String gsTable,String fbm,String codes){
		StringBuffer result = new StringBuffer("{success:");
		if(codes==null||"".equals(codes)){
			result.append("true}");
			return result.toString();
		}
		JSONObject jwhos = null;
		JSONArray jdss = null,jgss=null;
		try{
			jwhos = new JSONObject(codes);
			if(jwhos==null){
				result.append("true}");
				return result.toString();
			}
			jdss =jwhos.getJSONArray("ds");
			jgss = jwhos.getJSONArray("gs");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
			return result.toString();
		}
		boolean done = cdao.saveMappingF2T(table,dsTable,gsTable,fbm,jdss,jgss);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public String saveMappingT2F(int who,String table,String tTable,String fbm,String t_bm)throws Exception{
		StringBuffer result = new StringBuffer("{success:");
		boolean done = cdao.saveMappingT2F(who, table, tTable, fbm, t_bm);
		if(done){
			result.append("true}");
		}else{
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public String getTableMappingInfo(String table,int who,int all){
		StringBuffer info = new StringBuffer("{");
		String gs = "",ds="",fn="";
		String gname = "",dname="",fname="";
		if(who==0){
			fn = getMappedFTable(table,0);
			ds = table;
			gs = getMappedTaxTable(fn,1);
		}else if(who==1){
			fn = getMappedFTable(table,1);
			ds = getMappedTaxTable(fn,0);
			gs = table;
		}else{
			fn = table;
			ds = getMappedTaxTable(fn,0);
			gs = getMappedTaxTable(fn,1);
		}
		if(all==1){
			gname = getTbName(gs,1);
			dname = getTbName(ds,0);
			fname = getTbName(fn,9);
		}
		info.append("f:'").append(fn).append("',ds:'").append(ds);
		info.append("',gs:'").append(gs).append("'");
		if(all==1){
			info.append(",fname:'").append(fname).append("',dname:'");
			info.append(dname).append("',gname:'").append(gname).append("'");
		}
		info.append("}");
		return info.toString();
	}
	//根据编码获取编码表名
	private String getTbName(String tb,int who){
		String name = "";
		StringBuffer sql =new StringBuffer("select name from ");
		if(who<9){
			sql.append(" bm_index_tax where table_bm='").append(tb).append("' and who=").append(who);
		}else{
			sql.append(" bm_index where table_bm='").append(tb).append("'");
		}
		name = cdao.queryForString(sql.toString());
		return name;
	}
	/**
	 * 根据已知税务编码表名，获取对应的财政编码表名
	* @param Ttable 税务编码表名
	* @param taxType 税务类型，0:地税,1:国税
	* @return
	 */
	@DirectMethod
	public String getMappedFTable(String Ttable,int taxType){
		String tb = "";
		StringBuffer sql = new StringBuffer("select f_table from bm_table_map where ");
		sql.append("t_table='").append(Ttable).append("' and who=").append(taxType);
		tb = cdao.queryForString(sql.toString());
		return tb;
	}
	/**
	 * 根据已知财政编码表，获取对应的税务编码表名
	* @param ftable 财政编码表名
	* @param taxType 税务类型，0:地税,1:国税
	* @return
	 */
	public String getMappedTaxTable(String ftable,int taxType){
		String tb = "";
		StringBuffer sql = new StringBuffer("select t_table from bm_table_map where ");
		sql.append("f_table='").append(ftable).append("' and who=").append(taxType);
		tb = cdao.queryForString(sql.toString());
		return tb;
	}
	@DirectMethod
	public String moveCode(int who,String table_bm,String nodeId,String oldPid,String newPid){
		StringBuffer result = new StringBuffer("{success:");
		boolean done = false;
		try{
			done = cdao.moveCode(who,table_bm,nodeId,oldPid,newPid);
			result.append(done);
			result.append("}");
		}catch(Exception e){
			log.error(e.toString());
			result.append("false}");
		}
		return result.toString();
	}
	@DirectMethod
	public List getAidItems(String nodeid){
		List nodes = null;
		StringBuffer sql = new StringBuffer("select id,pid,mc text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded");
		sql.append(" from items where pid ");
		if(nodeid==null||"tree-root".equals(nodeid)){
			sql.append(" is null");
		}else{
			sql.append("=").append(nodeid);
		}
		sql.append(" order by id");
		nodes = cdao.queryForList(sql.toString(),TreeNode.class);
		List nds=new ArrayList(); 
		if(nodes!=null&&nodes.size()>0){
			for(int i=0;i<nodes.size();i++){
				CheckTreeNode cn = new CheckTreeNode();
				TreeNode n=(TreeNode)nodes.get(i);
				cn.setId(n.getId());
				cn.setPid(n.getPid());
				cn.setText(n.getText());
				cn.setLeaf(n.isLeaf());
				cn.setCls(n.getCls());
				cn.setChecked(false);
				nds.add(cn);
			}
		}
		return nds;
	}
	@DirectMethod
	public List getAidItemsMtTree(String nodeid){
		List nodes = null;
		StringBuffer sql = new StringBuffer("select id,pid,mc text,isleaf leaf,decode(isleaf,1,'file','folder') cls,0 expanded");
		sql.append(" from items where pid ");
		if(nodeid==null||"tree-root".equals(nodeid)){
			sql.append(" is null");
		}else{
			sql.append("=").append(nodeid);
		}
		sql.append(" order by id");
		nodes = cdao.queryForList(sql.toString(),TreeNode.class);
		return nodes;
	}
	@DirectMethod
	public String searchForAidItem(String cont,String startid){
		StringBuffer result = new StringBuffer("{");
		String path="";
		String findBm=null;
		StringBuffer treeView = new StringBuffer("select rownum as r,o.* from (select id,mc, pid from items ");
		treeView.append(" connect by prior id=pid start with pid is null)o");
		StringBuffer sql = new StringBuffer("select to_char(id)bm,to_char(pid)pid,b.mc from( ");
		sql.append(treeView);
		sql.append(") b where mc").append(" like '%").append(cont).append("%' and rownum=1");
		if(startid !=null&&!"".equals(startid)){
			sql.append(" and r>(select a.r from(").append(treeView).append(")a where to_char(id)='").append(startid).append("')");
		}
		try{
			List lst = cdao.queryForList(sql.toString(),Code.class);
			if(lst!=null&&lst.size()>0){
				Code pCode=(Code)lst.get(0);
				findBm =pCode.getBm();
		    }
			if(findBm!=null&&!"".equals(findBm)){
				sql = new StringBuffer("select to_char(id)bm,mc from items");
				sql.append(" connect by prior pid=id start with to_char(id)='").append(findBm).append("' order by level desc");
				path = getFullPath(sql.toString());
				result.append("match:'yes',path:'").append(path).append("'}");
			}else{
				result.append("match:'no'}");
			}
		}catch (Throwable e) {
			log.error(e.toString());
		}
		return result.toString();
	}
	@DirectMethod
	public AidItem getAidItem(String nodeid){
		AidItem aitem = null;
		aitem = cdao.getAidItem(nodeid);
		return aitem;
	}
	@DirectMethod
	public String delAidItems(String iid,String pid){
		StringBuffer result = new StringBuffer("{result:");
		boolean done = cdao.delAidItems(iid,pid);
		result.append(done).append("}");
		return result.toString();
	}
	@DirectMethod
	public List getGrades(){
		List grades = null;
		StringBuffer sql = new StringBuffer("select bm,mc from bm_cont where table_bm='BM_GRADE'");
		grades = cdao.queryForList(sql.toString(),SimpleValue.class);
		return grades;
	}
	@DirectFormPostMethod
	public SubmitResult saveAidItem(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();
		String mc = (String)params.get("pname");
		try{
			int updatedID = cdao.saveAidItem(params,fileFields);
			result.setSuccess(updatedID>0);
			Map infos = new HashMap();
			infos.put("msg", "保存资助项目信息成功！");
			infos.put("updatedID", String.valueOf(updatedID));
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存资助项目信息时发生错误，项目:\""+mc+"\"。错误："+e.toString());
		}
		return result; 
	}
}
