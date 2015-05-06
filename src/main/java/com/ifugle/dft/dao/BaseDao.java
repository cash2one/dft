package com.ifugle.dft.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public abstract class BaseDao {
	protected JdbcTemplate jdbcTemplate;
	public int queryCount (String initSql){
    	String countsql = "select count(*) count from ("+initSql+")";
    	System.out.println(countsql);
    	int count =jdbcTemplate.queryForInt(countsql);
    	return count;
    }
    
    @SuppressWarnings("unchecked")
	public List queryForPage (String initSql,int start,int limit,Class cls){
    	StringBuffer rSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
		rSql.append(initSql);
		rSql.append(") A WHERE rownum<=");
		rSql.append((start+limit));
		rSql.append(") B WHERE r>");
		rSql.append(start);
		System.out.println(rSql);
    	List rsts = jdbcTemplate.query(rSql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(cls));
    	return rsts;
    }
    public List queryForList(String initSql,Class cls){
    	Object[] params = null;
    	List rsts=jdbcTemplate.query(initSql,params, ParameterizedBeanPropertyRowMapper.newInstance(cls));
    	return rsts;
    }
    public boolean doUpdate(String sql,Object[] paraVals){
    	int count= jdbcTemplate.update(sql, paraVals);
    	return count>=0;
    }
    
    public String queryForString(String sql){
    	String result = "";
    	try{
    		result = (String)jdbcTemplate.queryForObject(sql.toString(),null,String.class);
    	}catch(Exception e){
    		result="";
    	}
    	return result;
    }
}
