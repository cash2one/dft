package com.ifugle.dft;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DbTest {
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate=jdbcTemplate;
	}
	public int queryInfo(){
		String transCountSql = "select count(*) from dj_cz where czfpbm='0103'";
		int count = jdbcTemplate.queryForInt(transCountSql);
		return count;
	}
	
	public boolean update(){
		boolean done = false;
		String updateTrans = "update bm_index set TABLE_BM=? where table_bm=? ";
		Object[] paras = new Object[]{"bm_wdly_11","BM_WDLY"};
		jdbcTemplate.update(updateTrans, paras);
		return done;
	}
	
	public boolean insert(){
		boolean done = false;
		String insertTrans = "insert into bm_index (TABLE_BM, NAME, QYBJ, TBTYPE, TBREF)values (?, ? ,?,0,'')";
		Object[] paras = new Object[]{"BM_TEST","测试1",new Integer(1)};
		jdbcTemplate.update(insertTrans, paras);
		return done;
	}
	
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext(
		        new String[] {"conf/applicationContext.xml"});
		DbTest db = (DbTest)context.getBean("db");
		int count = db.queryInfo();
		System.out.println("一共"+count+"家企业");
		boolean updated = db.update();
		System.out.println("更新了编码索引表");
		boolean inserted = db.insert();
		System.out.println("增加了编码表的记录");
	}
}
