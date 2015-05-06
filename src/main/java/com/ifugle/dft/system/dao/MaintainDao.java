package com.ifugle.dft.system.dao;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.ifugle.dft.dao.*;
import com.ifugle.dft.income.entity.Rule;
import com.ifugle.dft.income.entity.RuleOfP;
import com.ifugle.dft.system.entity.Bb_data_index;
import com.ifugle.dft.system.entity.Code;
import com.ifugle.dft.system.entity.SimpleBean;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.system.handler.CodeHandler;
import com.ifugle.dft.utils.JsonHelper;
import com.ifugle.dft.utils.entity.SimpleValue;
import com.ifugle.dft.utils.entity.TreeNode;
@Transactional
public class MaintainDao extends BaseDao{
	private static Logger log = Logger.getLogger(MaintainDao.class);
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public String getAlias(String loginId){
		StringBuffer sql = new StringBuffer("select name from users where userid ='");
		sql.append(loginId).append("'");
		System.out.println("sql语句："+sql.toString());
		String alias = queryForString(sql.toString());
		System.out.println("别名："+alias);
		return alias;
	}
	public User authenticUser(String userid, String pswd) {
		User user = null;
		StringBuffer sql = new StringBuffer("select userid,name,userdesc,ismanager,qybj from users ");
		sql.append(" where userid=? and password=?");
		List users = jdbcTemplate.query(sql.toString(),new Object[]{userid,pswd},ParameterizedBeanPropertyRowMapper.newInstance(User.class));
		if(users!=null&&users.size()>0){
			user = (User)users.get(0);
		}
		return user;
	}
	
	public List getUser_xzs(User user) {
		List xzs = null;
		if(user==null){
			return null;
		}
		StringBuffer sql=new StringBuffer("SELECT BM,MC FROM  ");
    	sql.append("(SELECT * FROM BM_CONT WHERE QYBJ<>0 AND TABLE_BM='BM_CZFP'");
    	sql.append(")A,(SELECT * FROM USER_XZ WHERE USERID=?)B WHERE TO_CHAR(A.BM)=B.CZFPBM");
    	sql.append(" CONNECT BY PRIOR BM=PID START WITH PID IS NULL ");
    	xzs = jdbcTemplate.query(sql.toString(),new Object[]{user.getUserid()},ParameterizedBeanPropertyRowMapper.newInstance(SimpleValue.class));
		return xzs;
	}
	/**
	 * 根据用户获得权限模块集合。
	* @param user
	* @return
	 */
	public List getAccessableModules(String userid,String pid) {
		List modules = null;
		StringBuffer sql=new StringBuffer("select distinct moduleid bm,name mc,pid,href,target,isleaf,dorder from(");
    	sql.append("select b.* from modules b " );
    	sql.append("start with moduleid in(select distinct moduleid from user_post u,post_module p where p.postid = u.postid and u.userid=?)");
    	sql.append("connect by prior b.pid=moduleid )a where qybj=1 and pid ");
    	if(pid==null||"".equals(pid)){
    		sql.append(" is null");
    	}else {
    		sql.append("='").append(pid).append("'");
    	}
    	sql.append(" order by dorder");
    	modules = jdbcTemplate.query(sql.toString(),new Object[]{userid},ParameterizedBeanPropertyRowMapper.newInstance(SimpleBean.class));
		return modules;
	}
	/**
	 * 无权限限制的模块获取方法
	* @param pid
	* @return
	 */
	public List getModules(String pid) {
		List modules = null;
		StringBuffer sql=new StringBuffer("select moduleid bm,name mc,pid,href,target,isleaf,dorder from( ");
    	sql.append("select * from modules start with pid is null connect by prior moduleid=pid ");
    	sql.append(") where qybj=1 and pid ");
    	if(pid==null||"".equals(pid)){
    		sql.append(" is null");
    	}else {
    		sql.append("='").append(pid).append("'");
    	}
    	sql.append(" order by dorder");
    	modules = jdbcTemplate.query(sql.toString(),ParameterizedBeanPropertyRowMapper.newInstance(SimpleBean.class));
		return modules;
	}
	
	public boolean deleteUsers(String[] users) {
		StringBuffer xzsql = new StringBuffer("delete from user_xz where userid =?");
		StringBuffer msql = new StringBuffer("delete from user_post where userid =?");
		StringBuffer sql = new StringBuffer("delete from users where userid =?");
		for(int i = 0;i<users.length;i++){
			String user = users[i];
			jdbcTemplate.update(xzsql.toString(),new Object[]{user});
			jdbcTemplate.update(msql.toString(),new Object[]{user});
			jdbcTemplate.update(sql.toString(),new Object[]{user});
		}
		return true;
	}
	/**
	 * 检查用户id是否已经存在
	* @param newUserid 新增/修改后的用户id
	* @param cUserid 用户原先的id，对于新增用户，该项应为null或""
	* @return
	 */
	public boolean checkUseridDup(String newUserid, String cUserid) {
		boolean dup = false;
		try{
			StringBuffer sql = new StringBuffer("select count(userid) count from users where userid=?");
			if(cUserid!=null&&!"".equals(cUserid)){
				sql.append(" and userid<>'").append(cUserid).append("'");
			}
			int count = jdbcTemplate.queryForInt(sql.toString(), new Object[]{newUserid});
			dup = count>0;
		}catch(Exception e){
			log.error(e.toString());
			return false;
		}
		return dup;
	}
	/**
	 * 获取指定用户信息
	* @param userid
	* @return
	 */
	public User getUserInfo(String userid) {
		User user = null;
		StringBuffer sql = new StringBuffer("select userid,name,userdesc,ismanager,qybj,password from users where userid = ?");
		StringBuffer xzsql = new StringBuffer("select a.czfpbm bm,b.mc from user_xz a,");
		xzsql.append("(select bm,mc from bm_cont where table_bm='BM_CZFP')b where a.czfpbm=b.bm(+) and a.userid=?");
		try{
			user = (User)jdbcTemplate.queryForObject(sql.toString(),new Object[]{userid},ParameterizedBeanPropertyRowMapper.newInstance(User.class));
			if(user!=null){
				List xzs = jdbcTemplate.query(xzsql.toString(),new Object[]{userid},ParameterizedBeanPropertyRowMapper.newInstance(SimpleValue.class));
				user.setXzs(xzs);
			}
		}catch(Throwable e){
			log.error(e.toString());
		}
		return user;
	}
	/**
	 * 设置用户岗位对应关系
	* @param userid
	* @param posts
	* @return
	 */
	public boolean setUserPosts(String userid, String[] posts) {
		boolean done = false;
		StringBuffer sql = new StringBuffer("delete from user_post where userid=?");
		StringBuffer isql = new StringBuffer("insert into user_post(userid,postid)values(?,?)");
		jdbcTemplate.update(sql.toString(),new Object[]{userid});
		for(int i=0;i<posts.length;i++){
			String pst = posts[i];
			int ipst = 0;
			try{
				ipst = Integer.parseInt(pst);
			}catch(Exception e){}
			jdbcTemplate.update(isql.toString(),new Object[]{userid,new Integer(ipst)});
		}
		return done;
	}
	/**
	 * 保存用户
	* @param name
	* @param userdesc
	* @param pswd
	* @param ismanager
	* @param qybj
	* @param userid
	* @param khrq
	* @param fps
	* @param cMode
	* @return
	 */
	public boolean saveUser(String name,String userdesc,String pswd,int ismanager,int qybj,String userid,String khrq,String[] fps,String cMode) {
		StringBuffer sql = new StringBuffer("update users set name=?,userdesc=?,password=?,ismanager=?,qybj=? where userid=?");
		if(cMode!=null&&"add".equals(cMode)){
			sql = new StringBuffer("insert into users(name,userdesc,password,ismanager,qybj,userid,khrq)values(?,?,?,?,?,?,'");
			sql.append(khrq).append("')");
		}
		Object[] paras = new Object[]{name,userdesc,pswd,new Integer(ismanager),new Integer(qybj),userid};
		jdbcTemplate.update(sql.toString(), paras);
		sql = new StringBuffer("delete from user_xz where userid=?");
		jdbcTemplate.update(sql.toString(), new Object[]{userid});
		if(fps!=null&&fps.length>0){
			sql = new StringBuffer("insert into user_xz(userid,czfpbm)values(?,?)");
			for(int i=0;i<fps.length;i++){
				String fp = fps[i];
				jdbcTemplate.update(sql.toString(), new Object[]{userid,fp});
			}
		}
		return true;
	}
	/**
	 * 保存用户和岗位的对应关系
	* @param userid
	* @param posts
	* @return
	 */
	public boolean saveUserPosts(String userid, String[] posts) {
		StringBuffer sql = new StringBuffer("delete from user_post where userid=?");
		jdbcTemplate.update(sql.toString(), new Object[]{userid});
		if(posts!=null&&posts.length>0){
			StringBuffer isql = new StringBuffer("insert into user_post(userid,postid)values(?,?)");
			for(int i=0;i<posts.length;i++){
				String p = posts[i];
				jdbcTemplate.update(isql.toString(), new Object[]{userid,p});
			}
		}
		return true;
	}
	/**
	 * 删除指定的岗位
	* @param posts
	* @return
	 */
	public boolean deletePost(String[] posts) {
		StringBuffer pmsql = new StringBuffer("delete from post_module where postid =?");
		StringBuffer upsql = new StringBuffer("delete from user_post where postid =?");
		StringBuffer sql = new StringBuffer("delete from post where postid =?");
		for(int i = 0;i<posts.length;i++){
			String post = posts[i];
			int p = 0;
			try{
				p = Integer.parseInt(post);
			}catch(Exception e){}
			jdbcTemplate.update(pmsql.toString(),new Object[]{new Integer(p)});
			jdbcTemplate.update(upsql.toString(),new Object[]{new Integer(p)});
			jdbcTemplate.update(sql.toString(),new Object[]{new Integer(p)});
		}
		return true;
	}
	public boolean addPost(String postname, String remark) {
		StringBuffer sql = new StringBuffer("insert into post(postid,postname,remark)values(sq_post.nextval,?,?)");
		jdbcTemplate.update(sql.toString(),new Object[]{postname,remark});
		return true;
	}
	public boolean updatePost(int postid,String postname, String remark) {
		StringBuffer sql = new StringBuffer("update post set postname=?,remark=? where postid=?");
		jdbcTemplate.update(sql.toString(),new Object[]{postname,remark,new Integer(postid)});
		return true;
	}
	public List getModulesOfPost(int postid) {
		List mds = null;
		StringBuffer sql = new StringBuffer("select moduleid from post_module where postid=?");
		mds = jdbcTemplate.queryForList(sql.toString(), new Object[]{new Integer(postid)}, String.class);
		return mds;
	}
	public List getModuleTree(String pid) {
		List modules = null;
		StringBuffer sql = new StringBuffer("select moduleid id,name text,pid,isleaf leaf,decode(isleaf,1,'file','folder') cls ");
		sql.append(" from modules where qybj=1 and pid ");
		if(pid==null||"".equals(pid)||"module_root".equals(pid)){
			sql.append(" is null");
		}else{
			sql.append(" = '").append(pid).append("'");
		}
		sql.append(" order by moduleid");
		modules = jdbcTemplate.query(sql.toString(),ParameterizedBeanPropertyRowMapper.newInstance(TreeNode.class));
		return modules;
	}
	public boolean setPostModules(int postid, String[] modules) {
		boolean done = false;
		StringBuffer sql = new StringBuffer("delete from post_module where postid=?");
		jdbcTemplate.update(sql.toString(), new Object[]{new Integer(postid)});
		if(modules!=null&&modules.length>0){
			StringBuffer isql = new StringBuffer("insert into post_module(postid,moduleid)values(?,?)");
			for(int i=0;i<modules.length;i++){
				String m = modules[i];
				jdbcTemplate.update(isql.toString(), new Object[]{new Integer(postid),m});
			}
		}
		done = true;
		return done;
	}
	
	public String getModulePath(String[] arrVals){
		String pathes="";
		if(arrVals!=null&&arrVals.length>0){
			JSONArray jpathes = new JSONArray();
			for(int i=0;i<arrVals.length;i++){
				String val = arrVals[i];
				StringBuffer sql = new StringBuffer("select moduleid bm,name mc from modules ");
				sql.append("connect by prior pid=moduleid start with moduleid='").append(val).append("' order by level desc");
				String path = getFullPath(sql.toString());
				jpathes.put(path);
			}
			try{
				pathes = JsonHelper.getJsonHelper().toJSONString(jpathes);
			}catch(Exception e){
			}
		}
		return pathes;
	}
	private String getFullPath(String sql){
		String path="";
		List lst = jdbcTemplate.query(sql, new Object[]{}, ParameterizedBeanPropertyRowMapper.newInstance(Code.class));
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
	public Map getPostInfo(int postid) {
		Map postInfos = null;
		StringBuffer sql = new StringBuffer("select postname,remark from post where postid = ");
		sql.append(postid);
		postInfos = jdbcTemplate.queryForMap(sql.toString(), null);
		return postInfos;
	}
	public boolean publishReports(String strRpts,String userid,int doType) {
		boolean done = false;
		String sql = "update bb_data_index set checks=?,userid =? where bb_id=? and ny=?";
		List rpts = parseRptsToAudit(strRpts);
		for(int i =0;i<rpts.size();i++){
			Bb_data_index bb = (Bb_data_index)rpts.get(i);
			jdbcTemplate.update(sql, new Object[]{new Integer(doType),userid,bb.getBb_id(),bb.getNy()});
		}
		done = true;
		return done;
	}
	private List parseRptsToAudit(String strRpts) {
		List rpts =new ArrayList();
		JSONArray jsRpts =null;
		try{
			jsRpts = new JSONArray(strRpts);
		}catch(Exception e){
		}
		if(jsRpts!=null&&jsRpts.length()>0){
			for(int i=0;i<jsRpts.length();i++){
				try{
					JSONObject jsrpt = jsRpts.getJSONObject(i);
					Bb_data_index bb = new Bb_data_index();
					bb.setNy(jsrpt.getInt("ny"));
					bb.setBb_id(jsrpt.getString("bb_id"));
					rpts.add(bb);
				}catch(Exception e){}
			}
		}
		return rpts;
	}
}
