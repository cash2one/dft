package com.ifugle.dft.datapro.entity;

public class Application {
	private int id;
	private String approvaldate;
	private String userid;
	private int xh;
	private int iid;
	//初始导入时匹配情况
	private int nomatch;
	private int enappcount;
	private String czfp;
	private String swdjzh;
	private String qymc;
	private String itemcont; 
	private double money;
	private double sszj;
	private double qptzj;
	private double contribute_lst;
	private double contribute;
	private String investment;
	private String remark;
	private String inputtime;
	
	public double getContribute_lst() {
		return contribute_lst;
	}
	public void setContribute_lst(double contributeLst) {
		contribute_lst = contributeLst;
	}
	public double getContribute() {
		return contribute;
	}
	public void setContribute(double contribute) {
		this.contribute = contribute;
	}
	public String getItemcont() {
		return itemcont;
	}
	public void setItemcont(String itemcont) {
		this.itemcont = itemcont;
	}
	public String getInputtime() {
		return inputtime;
	}
	public void setInputtime(String inputtime) {
		this.inputtime = inputtime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getApprovaldate() {
		return approvaldate;
	}
	public void setApprovaldate(String approvaldate) {
		this.approvaldate = approvaldate;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getXh() {
		return xh;
	}
	public void setXh(int xh) {
		this.xh = xh;
	}
	public int getIid() {
		return iid;
	}
	public void setIid(int iid) {
		this.iid = iid;
	}
	public String getSwdjzh() {
		return swdjzh;
	}
	public void setSwdjzh(String swdjzh) {
		this.swdjzh = swdjzh;
	}
	public String getQymc() {
		return qymc;
	}
	public void setQymc(String qymc) {
		this.qymc = qymc;
	}
	public double getMoney() {
		return money;
	}
	public void setMoney(double money) {
		this.money = money;
	}
	public String getInvestment() {
		return investment;
	}
	public void setInvestment(String investment) {
		this.investment = investment;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getNomatch() {
		return nomatch;
	}
	public void setNomatch(int nomatch) {
		this.nomatch = nomatch;
	}
	public String getCzfp() {
		return czfp;
	}
	public void setCzfp(String czfp) {
		this.czfp = czfp;
	}
	public int getEnappcount() {
		return enappcount;
	}
	public void setEnappcount(int enappcount) {
		this.enappcount = enappcount;
	}
	public double getSszj() {
		return sszj;
	}
	public void setSszj(double sszj) {
		this.sszj = sszj;
	}
	public double getQptzj() {
		return qptzj;
	}
	public void setQptzj(double qptzj) {
		this.qptzj = qptzj;
	}
}
