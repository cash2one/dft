package com.fruit.query.report;

import com.fruit.query.data.DataSet;
import com.fruit.query.data.ProcedureBean;

public class DefaultRuleDefine {
	private int sourceType;//取数方式
	private String sql;   //取数sql，如果取数方式是1时使用。
	private ProcedureBean procedure;  //取数用存储过程
	private String implClass;//报表取数由自定义类实现
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public ProcedureBean getProcedure() {
		return procedure;
	}
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	public String getImplClass() {
		return implClass;
	}
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}
	
}
