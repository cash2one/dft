package com.fruit.query.data;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 参数待选项
 */
public class OptionItem {
	/**
	 * bm:相当于选项的值
	 */
	private String bm; 
	/**
	 * 相当于选项的显示名，description
	 */
	private String name;
	/**
	 * 对于树型选项集合有效，是否底级节点
	 */
	private int isleaf;
	/**
	 * 对于树型选项集合有效，父节点
	 */
	private String pid;
	
	private int isDefault=0;
	
	/**
	 * 返回选项的值。
	 * 每个待选项应有不同的值。
	 * @return 选项值。
	 */
	public String getBm() {
		return bm;
	}
	/**
	 * 
	 * @param bm 选项值。
	 */
	public void setBm(String bm) {
		this.bm = bm;
	}
	/**
	 * 返回是否底级选项。
	 * 对于构造树形的参数选择控件有用。
	 * @return 是否底级。
	 */
	public int getIsleaf() {
		return isleaf;
	}
	/**
	 * 
	 * @param isleaf 是否底级
	 */
	public void setIsleaf(int isleaf) {
		this.isleaf = isleaf;
	}
	/**
	 * 选项的中文描述名。
	 * 提交选项值时，name也会被提交，可被title等表达式用$$的形式引用。
	 * @return 选项中文名。
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name 中文名。
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 返回父选项的ID。
	 * 即bm――选项集合中其实是以bm属性充当ID的。
	 * @return 父项的bm
	 */
	public String getPid() {
		return pid;
	}
	/**
	 * 
	 * @param pid
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}
	/**
	 * 是否默认选中项。
	 * @return 是否默认选中的选项。
	 */
	public int getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(int isDefault) {
		this.isDefault = isDefault;
	}
}
