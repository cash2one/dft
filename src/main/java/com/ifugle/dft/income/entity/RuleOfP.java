package com.ifugle.dft.income.entity;

public class RuleOfP extends AbstractRuleDetail{
	private String swdjzh;
	private String szbm;
	private String szmc;
	private double sebl;
	private String czfpmc;
	private int inuse;
	public int getInuse() {
		return inuse;
	}
	public void setInuse(int inuse) {
		this.inuse = inuse;
	}
	public String getSwdjzh() {
		return swdjzh;
	}
	public void setSwdjzh(String swdjzh) {
		this.swdjzh = swdjzh;
	}
	public String getSzbm() {
		return szbm;
	}
	public void setSzbm(String szbm) {
		this.szbm = szbm;
	}
	public String getSzmc() {
		return szmc;
	}
	public void setSzmc(String szmc) {
		this.szmc = szmc;
	}
	public double getSebl() {
		return sebl;
	}
	public void setSebl(double sebl) {
		this.sebl = sebl;
	}
	public String getCzfpmc() {
		return czfpmc;
	}
	public void setCzfpmc(String czfpmc) {
		this.czfpmc = czfpmc;
	}    
}
