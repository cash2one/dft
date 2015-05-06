package com.ifugle.dft.system.entity;

public class Bb_data_index {
	private String bb_id;
	private String bb_desc;
	private int ny;
	private int checks;
	private String userid;
	private String czsj;
	private String bb_ext;
	public String getBb_ext() {
		return bb_ext;
	}
	public void setBb_ext(String bbExt) {
		bb_ext = bbExt;
	}
	public String getBb_id() {
		return bb_id;
	}
	public void setBb_id(String bbId) {
		bb_id = bbId;
	}
	public String getBb_desc() {
		return bb_desc;
	}
	public void setBb_desc(String bbDesc) {
		bb_desc = bbDesc;
	}
	public int getNy() {
		return ny;
	}
	public void setNy(int ny) {
		this.ny = ny;
	}
	public int getChecks() {
		return checks;
	}
	public void setChecks(int checks) {
		this.checks = checks;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getCzsj() {
		return czsj;
	}
	public void setCzsj(String czsj) {
		this.czsj = czsj;
	}
}
