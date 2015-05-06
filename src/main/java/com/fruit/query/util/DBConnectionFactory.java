package com.fruit.query.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
/**
 * 
 * @author wxh
 *2009-3-13
 *TODO 获取数据库连接。与不同的应用耦合时，需要调整该类中获取数据源的方法。这里提供默认实现
 */
public class DBConnectionFactory {
	private static DataSource dataSource = null;

	private static QueryConfig config = null;
	
	private static String password;

	private static String user;

	private static String url;

	private static String driver;
	
	private static int connMode;
	private static String connPool;
	static {
		loadConfiguration();
	}

	private DBConnectionFactory() {
	}

	/**
	 * 创建dataSource
	 * @return DataSource对象。
	 */
	public static DataSource setupDataSource(){
		if(connMode==0) {//自定义连接池
			if(dataSource == null){
				BasicDataSource ds = new BasicDataSource();
				ds.setDriverClassName(driver);
				ds.setUsername(user);
				ds.setPassword(password);
				ds.setUrl(url);
				dataSource = ds;
				System.out.print("建立了自定义连接池！");
			}
		}else{//外部，容器连接池
			if(dataSource == null){
				try{
					javax.naming.InitialContext ctx = new javax.naming.InitialContext();
					dataSource = (DataSource)ctx.lookup(connPool);
				}catch(Exception e){
					dataSource=null;
				}
				System.out.print("使用容器连接池："+connPool);
			}
		}
		return dataSource;
	}

	/**
	 * 
	 * 
	 * 显示当前数据源的状态.
	 */
	public static String getDataSourceStats() {
		BasicDataSource bds = (BasicDataSource) setupDataSource();
		StringBuffer info = new StringBuffer();

		info.append("Active connection numbers: " + bds.getNumActive());
		info.append("\n");
		info.append("Idle connection numbers: " + bds.getNumIdle());

		return info.toString();
	}

	/**
	 * 获取数据库连接，如果数据源连接获取不成功，则用jdbc驱动的方式连接
	 * 
	 * @return Connection conn
	 */
	public static Connection getConnection() throws Exception{
		Connection conn = null;
		try {
			conn = setupDataSource().getConnection();
		} catch (Exception ex) {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
				conn = DriverManager.getConnection(url, user, password);
				return conn;
			} catch (Exception e) {
				System.out.println("连接数据库发生错误！数据库URL:" + url + "；数据库用户名：" + user + "；密码"
						+ password+"，错误信息："+e.toString());
			}
		}
		return conn;
	}

	/**
	 * 
	 * 关闭数据源
	 */
	public static void shutdownDataSource() {
		BasicDataSource bds = (BasicDataSource) setupDataSource();
		try {
			bds.close();
		} catch (SQLException e) {
			System.out.println("关闭数据源发生错误！");
		}
	}

	/**
	 * 
	 * 重新启动数据源.
	 */
	public static void restartDataSource() {
		shutdownDataSource();
		setupDataSource();
	}

	/**
	 * 初始化配置
	 * 
	 */
	public static void loadConfiguration() {
		config = QueryConfig.getConfig();
	
		url = config.getString("oraServerURL",
				"jdbc:oracle:thin:@127.0.0.1:1521:dnft");
		user = config.getString("UserID", "dnft4");
		password = config.getString("Password", "Sq8KSyG6FdkU");
		driver = config.getString("driverClassName",
				"oracle.jdbc.driver.OracleDriver");
		try{
			connMode=Integer.parseInt(config.getString("connMode","0"));
		}catch(Exception e){
			
		}
		connPool=config.getString("connPool","dnftPool");
	}

	/**
	 * 关闭连接资源
	 * @param conn 连接
	 * @param rs 数据集
	 * @param stmt Statement
	 */
	public static void endDBConnect(Connection conn, ResultSet rs,
			Statement stmt) {

		try {
			if (conn != null) {
				conn.rollback();
			}

			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			rs = null;
			stmt = null;
			conn = null;
		}
	}
	
	//关闭连接
	public static void connRollBack(Connection conn) {

		try {
			if (conn != null) {
				conn.rollback();
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			conn = null;
		}
	}
}
