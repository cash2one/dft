package com.ifugle.dft.system.handler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import com.ifugle.dft.system.entity.Bb_data_index;
import com.ifugle.dft.system.entity.CheckTreeNode;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.system.dao.*;
import com.ifugle.dft.utils.Configuration;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.JsonHelper;
import com.ifugle.dft.utils.entity.SimpleValue;
import com.ifugle.dft.utils.entity.SubmitResult;
import com.ifugle.dft.utils.entity.TreeNode;
import com.google.gson.*;
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class MaintainHandler {
	private static Logger log = Logger.getLogger(MaintainDao.class);
	private MaintainDao mdao;
	private Configuration cg ;
	WebContext context ;
	@SuppressWarnings("unchecked")
	public MaintainHandler(){
		mdao = (MaintainDao)ContextUtil.getBean("sysDao");
		cg = (Configuration)ContextUtil.getBean("config");
		if(cg!=null){
			cg.getHandlersMap().put("MaintainHandler","com.ifugle.dft.system.handler.MaintainHandler");
		}
	}
	public void setWebContext(WebContext ctx){
		if(context==null){
			context = ctx;
		}
	}
	@DirectMethod
	public String getAlias(String loginId){
		StringBuffer info = new StringBuffer("{alias:'");
		String alias = "";
		try{
			alias = mdao.getAlias(loginId);
		}catch(Exception e){
			log.error(e.toString());
		}
		info.append(alias);
		info.append("'}");
		System.out.println(info.toString());
		return info.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectMethod
	public Map getUsers(int start,int limit){
    	Map infos = new HashMap();
    	String initSql = "select userid,name,userdesc,ismanager,qybj from users ";
    	int count = mdao.queryCount(initSql);
    	infos.put("totalCount", new Integer(count));
    	List users = mdao.queryForPage(initSql,start,limit,User.class);
    	infos.put("rows", users);
    	return infos;
    }
	
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult changePswd(Map params,Map fileFields){
		SubmitResult obj = new SubmitResult();
		Map errors = new HashMap();		
		try{
			String userid = (String)params.get("userid");
			String oPswd = (String)params.get("oPswd");
			StringBuffer sql = new StringBuffer("select name from users where userid ='");
			sql.append(userid).append("'");
			String name = mdao.queryForString(sql.toString());
			//未找到当前用户；
			if(name==null||"".equals(name)){
				obj.setSuccess(false);
				errors.put("msg", "当前用户不存在或已停用！");
				obj.setErrors(errors);
				return obj;
			}
			User user =mdao.authenticUser(userid,oPswd);
			//输入密码不正确；
			if(user==null){
				obj.setSuccess(false);
				errors.put("msg", "输入原密码不正确！");
				obj.setErrors(errors);
				return obj;
			}
			sql = new StringBuffer("update users set password=? where userid=?");
			String nPswd = (String)params.get("newPswd");
			mdao.doUpdate(sql.toString(), new Object[]{nPswd,userid});
			obj.setSuccess(true);
			Map infos = new HashMap();
			infos.put("msg", "密码修改成功！");
			obj.setInfos(infos);
		}catch(Throwable e){
			obj.setSuccess(false);
			errors.put("msg", "修改密码时发生错误："+e.toString());
		}
		return obj;
	}
	@DirectMethod
	public String deleteUser(String strUsers){
		StringBuffer json = new StringBuffer("{result:");
		if(strUsers==null||"".equals(strUsers)){
			json.append("true}");
			return json.toString();
		}
		String[] users = strUsers.split(",");
		if(users==null||users.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.deleteUsers(users);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public String checkUserid(String newUserid,String cUserid){
		StringBuffer result = new StringBuffer("{duplicate:");
		boolean duplicate =  mdao.checkUseridDup(newUserid,cUserid);
		result.append(duplicate).append("}");
		return result.toString();
	}
	@SuppressWarnings("unchecked")
	@DirectFormPostMethod
	public SubmitResult saveUser(Map params,Map fileFields){
		SubmitResult result =  new SubmitResult();
		Map errors = new HashMap();		
		String userid = (String)params.get("userid");
		String name = (String)params.get("name");
		String userdesc = (String)params.get("userdesc");
		String pswd = (String)params.get("pswd");
		String sManager = (String)params.get("ismanager");
		String sQybj = (String)params.get("qybj");
		String czfps = (String)params.get("czfpbms")==null?"":(String)params.get("czfpbms");
		String[] fps = "".equals(czfps)? null: czfps.split(",");
		int qybj = 0;
		try{
			qybj = Integer.parseInt(sQybj);
		}catch(Exception e){}
		int ismanager = 0;
		try{
			ismanager = Integer.parseInt(sManager);
		}catch(Exception e){}
		String cMode = (String)params.get("cMode");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String khrq = sdf.format(date);
		try{
			//新增时检查是否编码重复
			if(cMode!=null&&"add".equals(cMode)){
				StringBuffer dpSql = new StringBuffer("select userid from users where userid='").append(userid).append("'");
				int count = mdao.queryCount(dpSql.toString());
				if(count>0){
					result.setSuccess(false);
					errors.put("userid", "当前用户ID已存在，不能重复！");
					result.setErrors(errors);
					return result;
				}
			}
			boolean done = mdao.saveUser(name,userdesc,pswd,ismanager,qybj,userid,khrq,fps,cMode);
			result.setSuccess(done);
			Map infos = new HashMap();
			infos.put("msg", "保存用户信息成功！");
			result.setInfos(infos);
		}catch(Throwable e){
			log.error(e.toString());
			result.setSuccess(false);
			errors.put("msg", "保存用户信息时发生错误，错误："+e.toString());
		}
		return result; 
	}
	@DirectMethod
	public User getUserInfo(String userid){
		User user = null;
		user = mdao.getUserInfo(userid);
		return user;
	}
	@DirectMethod
	public String setUserPosts(String userid,String strPosts){
		StringBuffer json = new StringBuffer("{result:");
		if(strPosts==null||"".equals(strPosts)){
			json.append("true}");
			return json.toString();
		}
		String[] posts = strPosts.split(",");
		if(posts==null||posts.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.setUserPosts(userid,posts);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public List getPostsTreeByUserid(String nodeid,String userid){
		List results = null;
		StringBuffer sql = new StringBuffer("select to_char(a.postid) id,a.postname text,1 leaf,''pid,decode(b.postid,null,0,1) checked");
		sql.append(" from post a,(select * from user_post where userid='").append(userid).append("')b");
		sql.append(" where a.postid=b.postid(+) order by a.postid");
		results = mdao.queryForList(sql.toString(),CheckTreeNode.class);
		return results; 
	}
	@DirectMethod
	public String saveUserPosts(String userid,String strPosts){
		StringBuffer json = new StringBuffer("{result:");
		if(strPosts==null||"".equals(strPosts)){
			json.append("true}");
			return json.toString();
		}
		String[] posts = strPosts.split(",");
		if(posts==null||posts.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.saveUserPosts(userid,posts);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public List getPostsTree(String nodeid){
		List results = null;
		StringBuffer sql = new StringBuffer("select to_char(postid) id,postname text,1 leaf");
		sql.append(" from post order by postid");
		results = mdao.queryForList(sql.toString(),TreeNode.class);
		return results; 
	}
	@DirectMethod
	public String deletePost(String strPostIds){
		StringBuffer json = new StringBuffer("{result:");
		if(strPostIds==null||"".equals(strPostIds)){
			json.append("true}");
			return json.toString();
		}
		String[] posts = strPostIds.split(",");
		if(posts==null||posts.length==0){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.deletePost(posts);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public String addPost(String postname,String remark){
		StringBuffer json = new StringBuffer("{result:");
		boolean done = mdao.addPost(postname,remark);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public String updatePost(String strPostid,String postname,String remark){
		StringBuffer json = new StringBuffer("{result:");
		int postid=-1;
		try{
			postid = Integer.parseInt(strPostid);
		}catch(Exception e){}
		boolean done = mdao.updatePost(postid,postname,remark);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public String getModuleInfo(String strPostid){
		StringBuffer json = new StringBuffer("");
		int postid = -1;
		try{
			postid = Integer.parseInt(strPostid);
		}catch(Exception e){}
		Map postInfo = mdao.getPostInfo(postid); 
		try{
			json.append(postInfo==null?"":JsonHelper.getJsonHelper().toJSONString(postInfo));	
		}catch(Exception e){}
		return json.toString();
	}
	@DirectMethod
	public String getModulesOfPost(String postid){
		StringBuffer json = new StringBuffer("{result:");
		int pid = -1;
		try{
			pid = Integer.parseInt(postid);
		}catch(Exception e){}
		List modules = mdao.getModulesOfPost(pid);
		if(modules==null||modules.size()==0){
			json.append("true,modules:'");
		}else{
			json.append("true,modules:'");
			for(int i=0;i<modules.size();i++){
				String m = (String)modules.get(i);
				json.append(m);
				if(i<modules.size()-1){
					json.append(",");
				}
			}
		}
		json.append("'}");
		return json.toString();
	}
	@DirectMethod
	public List getModuleTree(String pid){
		List results = null;
		results = mdao.getModuleTree(pid);
		return results; 
	}
	@DirectMethod
	public String getModuleExpandPathes(String moduleids){
		StringBuffer result=new StringBuffer("{pathes:");
		if(moduleids==null||"".equals(moduleids)){
			result.append("''}");
		}
		String[] arrMds = moduleids.split(",");
		String pathes = mdao.getModulePath(arrMds);
		result.append(pathes).append("}");
		return result.toString();
	}
	@DirectMethod
	public String setPostModules(String strPostid,String strPostids){
		StringBuffer json = new StringBuffer("{result:");
		if(strPostids==null||"".equals(strPostids)){
			json.append("true}");
			return json.toString();
		}
		String[] posts = strPostids.split(",");
		if(posts==null||posts.length==0){
			json.append("true}");
			return json.toString();
		}
		int postid=-1;
		try{
			postid = Integer.parseInt(strPostid);
		}catch(Exception e){}
		boolean done = mdao.setPostModules(postid,posts);
		json.append(done).append("}");
		return json.toString();
	}
	@DirectMethod
	public Map getReportsToAudit(int start,int limit){
	    Map infos = new HashMap();
	    String initSql = "select bb_id,bb_desc,to_char(dotime)czsj,userid,checks,ny from bb_data_index where qybj=1 order by bb_id,ny desc";
	    int count = mdao.queryCount(initSql);
	    infos.put("totalCount", new Integer(count));
	    List rpts = mdao.queryForPage(initSql,start,limit,Bb_data_index.class);
	    infos.put("rows", rpts);
	    return infos;
	}
	
	@DirectMethod
	public String publishReports(String jRpts){
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		StringBuffer json = new StringBuffer("{result:");
		if(jRpts==null||"".equals(jRpts)){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.publishReports(jRpts,user==null?"":user.getUserid(),1);
		json.append(done).append("}");
		return json.toString();
	}
	
	@DirectMethod
	public String undoPublishReports(String jRpts){
		WebContext context =WebContextManager.get();
		HttpServletRequest request = context.getRequest();
		User user=(User)request.getSession().getAttribute("user");
		StringBuffer json = new StringBuffer("{result:");
		if(jRpts==null||"".equals(jRpts)){
			json.append("true}");
			return json.toString();
		}
		boolean done = mdao.publishReports(jRpts,user==null?"":user.getUserid(),0);
		json.append(done).append("}");
		return json.toString();
	}
}
