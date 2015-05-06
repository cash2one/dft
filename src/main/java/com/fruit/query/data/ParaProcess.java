package com.fruit.query.data;

public class ParaProcess {
	private String name;
	private String desc;
	private int proMode;
	//参数加工用sql时，sql语句
	private String sql;
	//参数加工用存储过程时，该存储过程的描述
	private ProcedureBean procedure;
	//参数加工用扩展类时，类的全路径
	private String implClass;
	/**
	 * 参数加工的具体实现类名，该类扩展com.datanew.query.util.IParaProcess接口
	 * @return 参数加工的具体实现类的全路径名
	 */
	public String getImplClass() {
		return implClass;
	}
	/**
	 * 
	 * @param implClass
	 */
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}
	/**
	 * 加工过程的名称，应唯一。
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 参数加工过程由存储过程实现时，存储过程的描述信息
	 * @return
	 */
	public ProcedureBean getProcedure() {
		return procedure;
	}
	/**
	 * 
	 * @param procedure
	 */
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	/**
	 * 参数加工的模式。
	 * 1：通过执行sql语句加工；
	 * 2：通过执行存储过程加工；
	 * 3：通过调用自定义扩展类加工。
	 * @return 参数的加工模式
	 */
	public int getProMode() {
		return proMode;
	}
	/**
	 * 
	 * @param proMode
	 */
	public void setProMode(int proMode) {
		this.proMode = proMode;
	}
	/**
	 * 参数加工所使用的sql
	 * @return
	 */
	public String getSql() {
		return sql;
	}
	/**
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	/**
	 * 参数加工过程的描述、说明
	 * @return
	 */
	public String getDesc() {
		return desc;
	}
	/**
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
