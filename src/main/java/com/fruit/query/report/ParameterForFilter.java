package com.fruit.query.report;

public class ParameterForFilter extends Parameter{
	private String filterFld;
	//筛选字段和值之间的操作符，默认是等于 equ，支持：
	//等于:equ, 大于:gt, 小于:lt, 大于等于:gt_e, 小于等于:lt_e, 不等于:not_e, 匹配:like, 包含:in
	private String valueOprator="equ"; 
	
	public String getValueOprator() {
		return valueOprator;
	}

	public void setValueOprator(String valueOprator) {
		this.valueOprator = valueOprator;
	}

	public String getFilterFld() {
		return filterFld;
	}

	public void setFilterFld(String filterFld) {
		this.filterFld = filterFld;
	}
	
	public ParameterForFilter(){
		
	}
	public ParameterForFilter(Parameter pa){
		this();
		this.setAffectCallBack(pa.getAffectCallBack());
		this.setAffectedByParas(pa.getAffectedByParas());
		this.setAutoAll(pa.getAutoAll());
		this.setBindMode(pa.getBindMode());
		this.setBindTo(pa.getBindTo());
		this.setDataType(pa.getDataType());
		this.setDateFormat(pa.getDateFormat());
		this.setDefaultValBindMode(pa.getDefaultValBindMode());
		this.setDefaultValue(pa.getDefaultValue());
		this.setDefaultValueBindTo(pa.getDefaultValueBindTo());
		this.setDesc(pa.getDesc());
		this.setImplClass(pa.getImplClass());
		this.setIsHidden(pa.getIsHidden());
		this.setIsMulti(pa.getIsMulti());
		this.setLeafOnly(pa.getLeafOnly());
		this.setName(pa.getName());
		this.setParaOptions(pa.getParaOptions());
		this.setProcedure(pa.getProcedure());
		this.setRenderType(pa.getRenderType());
		this.setSourceType(pa.getSourceType());
		this.setSql(pa.getSql());
		this.setValidates(pa.getValidates());
		this.setDefaultRule(pa.getDefaultRule());
		this.setShowMode(pa.getShowMode());
	}
}
