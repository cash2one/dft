package com.ifugle.dft.check.entity;

public class En_field {
	private String tname; //所在表名
	private String field; //字段名
	private String mc;    //字段中文名称
	private int f_type;   
	/*
	 * 数据库中字段值的来源类型； 0：有少数简单的值，比如门征/非门征 1：值需要用户手工录入，比如地址、电话属性
	 * 2：值由固定的编码选项提供，由用户选择，比如经济性质属性
	 */
	private int val_src;
	private String mapbm;//字段对应的编码表，用于val_src为2时
	private int showmod;  //显示模式，0不显示；1显示，只读；2显示，可逐户核定，3，显示，可批量核定
	private int isrtk;   //是否影响入退库
	private int sort;    //排序
	private int valuetype;
	private int loadAll=0;
	//2015-11-17 增加属性，字段是否默认显示在列表中，宽度
	private int showinlist=0;
	private int colwidth = 100;
	
	public int getShowinlist() {
		return showinlist;
	}
	public void setShowinlist(int showinlist) {
		this.showinlist = showinlist;
	}
	public int getColwidth() {
		return colwidth;
	}
	public void setColwidth(int colwidth) {
		this.colwidth = colwidth;
	}
	public En_field(){
		
	}
	public En_field(String field,String mc){
		this.field = field;
		this.mc = mc;
	}
	public String getTname() {
		return tname;
	}
	public void setTname(String tname) {
		this.tname = tname;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getMc() {
		return mc;
	}
	public void setMc(String mc) {
		this.mc = mc;
	}
	public int getF_type() {
		return f_type;
	}
	public void setF_type(int fType) {
		f_type = fType;
	}
	public int getVal_src() {
		return val_src;
	}
	public void setVal_src(int valSrc) {
		val_src = valSrc;
	}
	public String getMapbm() {
		return mapbm;
	}
	public void setMapbm(String mapbm) {
		this.mapbm = mapbm;
	}
	public int getShowmod() {
		return showmod;
	}
	public void setShowmod(int showmod) {
		this.showmod = showmod;
	}
	public int getIsrtk() {
		return isrtk;
	}
	public void setIsrtk(int isrtk) {
		this.isrtk = isrtk;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public int getLoadAll() {
		return loadAll;
	}
	public void setLoadAll(int loadAll) {
		this.loadAll = loadAll;
	}
	public int getValuetype() {
		return valuetype;
	}
	public void setValuetype(int valuetype) {
		this.valuetype = valuetype;
	}
}
