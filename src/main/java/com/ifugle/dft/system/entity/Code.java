package com.ifugle.dft.system.entity;

/**
 * 表示一个编码的信息
 * 
 * @author wangxiaohui 2007-12-28 TODO
 */
public class Code {
	private String bm;
	private String mc;
	private String table_bm; // 所在编码表
	private String pid;
	private int qybj;
	private int isleaf; // 是否底级编码
	private int hasChild;
	private int codelevel; // 编码的层次，便于取出某个层次的编码
	private String mappedFCode; // 对应的财政编码，当code对象表示一个税务端编码时有用
	private String who;      //编码来源，0表示地税，1表示国税，不用int是避免默认值是0，和地税含义冲突
	private int status;      //表示税务编码时，代表税务编码的状态，是否新增
	private int xh;
	private String fullPath; //编码的全路径，以/划分层级
	private String fpName;   //路径的中文名表达
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getFpName() {
		return fpName;
	}

	public void setFpName(String fpName) {
		this.fpName = fpName;
	}

	public int getXh() {
		return xh;
	}

	public void setXh(int xh) {
		this.xh = xh;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getWho() {
		return who;
	}

	public void setWho(String who) {
		this.who = who;
	}

	public String getMappedFCode() {
		return mappedFCode;
	}

	public void setMappedFCode(String mappedFCode) {
		this.mappedFCode = mappedFCode;
	}

	public int getCodelevel() {
		return codelevel;
	}

	public void setCodelevel(int codelevel) {
		this.codelevel = codelevel;
	}

	public String getBm() {
		return bm;
	}

	public void setBm(String bm) {
		this.bm = bm;
	}

	public String getMc() {
		return mc;
	}

	public void setMc(String mc) {
		this.mc = mc;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public int getQybj() {
		return qybj;
	}

	public void setQybj(int qybj) {
		this.qybj = qybj;
	}

	public String getTable_bm() {
		return table_bm;
	}

	public void setTable_bm(String table_bm) {
		this.table_bm = table_bm;
	}

	public int getHasChild() {
		return hasChild;
	}

	public void setHasChild(int hasChild) {
		this.hasChild = hasChild;
	}

	public int getIsleaf() {
		return isleaf;
	}

	public void setIsleaf(int isleaf) {
		this.isleaf = isleaf;
	}

}
