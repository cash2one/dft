package com.fruit.query.data;

import java.util.ArrayList;

/**
 * 
 * @author wxh
 * 2009-3-11
 * TODO 存储列节点信息
 */
public class Column {
	private String colId;        //列的唯一标识
	private String colName;      //列头的显示名称
	private String dataIndex;    //列与记录集字段匹配
	private String pid;		     //列的父列
	private int isleaf=1;        //是否底级列
	private int level;           //列节点在表头所在层次，复杂表头有用 
	private int dataType;        //数据类型，dataType:参数的数据类型。0：字符；1：整数；2：浮点数
	private String renderer;     //该列数据显示时渲染函数，比如显示成千分位
	private int width = 110;     //列宽度
	private int calculate_mode;  //列的计算模式。0：不计算。1：自动小计下级列数据；2：利用其它公式计算
	private String colFunction;  //列计算公式
	private int funcPosition;
	private int isHidden;        //是否隐藏，隐藏的列不参与显示。
	private int  readOnly;
	private int isOrder;          //是否可远程排序（后台排序）
	private int isMultiUnit ;     //是否可以切换单位来显示
	//2013-08-30 链接报表用属性
	private int isLink;           //是否在当前字段上链接
	//2015-10-29
	private int linkAction;       //链接报表以什么方式展现。0：默认，打开新窗体。1：在原页面中弹出模态window
	private int popHeight = 480;  //如果链接报表以弹出方式打开，弹出窗体的高宽
	private int popWidth = 640;
	private ArrayList<LinkTab> LinkTabs;       //如果链接报表以弹出窗体打开，弹出窗体中的各个tab定义（每个tab都可以链接一报表）
	
	private String linkParams;    //链接时传递的字段，逗号分隔；
	private String target;        //链接报表打开的地方。参考js，_blank,_self
	private String linkTo;        //链接到哪张报表
	private int isGroup;          //是否分组字段，如果是（1），相邻的行，如果该列值相同，则合并单元格，形成分组视觉效果。
	private String editor;
	private int hideZero;         //该列如果是数值类型的，是否可以隐藏0值不显示
	private String align;         //2015-03-20 对齐方式。
	private int defaultHide;      //2015-03-20 初始时是否隐藏该列。这功能和hidden不同，hidden的列是辅助性质，完全不参与显示。
	public String getAlign() {
		return align;
	}
	public void setAlign(String align) {
		this.align = align;
	}
	public int getDefaultHide() {
		return defaultHide;
	}
	public void setDefaultHide(int defaultHide) {
		this.defaultHide = defaultHide;
	}
	public int getHideZero() {
		return hideZero;
	}
	public void setHideZero(int hideZero) {
		this.hideZero = hideZero;
	}
	public String getEditor() {
		return editor;
	}
	public void setEditor(String editor) {
		this.editor = editor;
	}
	public int getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}
	public int getIsLink() {
		return isLink;
	}
	public void setIsLink(int isLink) {
		this.isLink = isLink;
	}
	public String getLinkParams() {
		return linkParams;
	}
	public void setLinkParams(String linkParams) {
		this.linkParams = linkParams;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getLinkTo() {
		return linkTo;
	}
	public void setLinkTo(String linkTo) {
		this.linkTo = linkTo;
	}
	public int getIsMultiUnit() {
		return isMultiUnit;
	}
	public void setIsMultiUnit(int isMultiUnit) {
		this.isMultiUnit = isMultiUnit;
	}
	public int getIsOrder() {
		return isOrder;
	}
	public void setIsOrder(int isOrder) {
		this.isOrder = isOrder;
	}
	public int getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(int readOnly) {
		this.readOnly = readOnly;
	}
	public int getIsHidden() {
		return isHidden;
	}
	public void setIsHidden(int isHidden) {
		this.isHidden = isHidden;
	}
	/**
	 * 返回自动小计列的位置。
	 * 自动小计列在列定义中不占位，要用该属性描述位置。<br>
	 * 0：自动小计列出现在其他子列之后；1：小计列作为第一个子列
	 * @return 自动小计列出现的位置。 
	 */
	public int getFuncPosition() {
		return funcPosition;
	}
	/**
	 * 
	 * @param funcPosition 自动小计列出现的位置。
	 * @see #getFuncPosition()
	 */
	public void setFuncPosition(int funcPosition) {
		this.funcPosition = funcPosition;
	}
	/**
	 * 返回列的计算模式。
	 * 0：默认，非计算列；1：由下级节点自动合计；9：其他计算公式。
	 * @return 列计算模式
	 */
	public int getCalculate_mode() {
		return calculate_mode;
	}
	/**
	 * 
	 * @param calculate_mode
	 * @see #getCalculate_mode()
	 */
	public void setCalculate_mode(int calculate_mode) {
		this.calculate_mode = calculate_mode;
	}
	/**
	 * 返回列的计算公式。
	 * 列数据用"r.data"+列名(DataIndex)的形式引用。
	 * @return 列公式
	 */
	public String getColFunction() {
		return colFunction;
	}
	/**
	 * 
	 * @param colFunction 列公式
	 * @see #getColFunction()
	 */
	public void setColFunction(String colFunction) {
		this.colFunction = colFunction;
	}
	/**
	 * 返回列的唯一标识ID。
	 * @return 列ID
	 */
	public String getColId() {
		return colId;
	}
	/**
	 * 
	 * @param colId 列标识
	 */
	public void setColId(String colId) {
		this.colId = colId;
	}
	/**
	 * 返回列的中文描述名。
	 * @return 列中文名
	 */
	public String getColName() {
		return colName;
	}
	/**
	 * 
	 * @param colName 列中文名
	 */
	public void setColName(String colName) {
		this.colName = colName;
	}
	/**
	 * 返回列对应的字段名。
	 * 即该列将显示记录集中那个字段的内容。复杂表头中的非底级节点，本属性为null或""。
	 * dataIndex使用大写字母。
	 * @return 列对应的字段名。
	 */
	public String getDataIndex() {
		return dataIndex;
	}
	/**
	 * 
	 * @param dataIndex 列对应的字段名 
	 * @see #getDataIndex()
	 */
	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}
	/**
	 * 是否底级列。<br>
	 * 0：非底级；1：底级。<br>
	 * 只有底级列才是真正的显示列，和记录集字段做对应。只有复杂列会出现非底级列。系统插入的自动小计列也是底级列。
	 * @return 是否底级
	 *
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
	 * 当前列的父列ID。用于复杂表头
	 * @return 父列的colId
	 */
	public String getPid() {
		return pid;
	}
	/**
	 * 
	 * @param pid
	 * @see #getPid()
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}
	/**
	 * 当前列的数据渲染为什么格式。
	 * 其值应是一个有固定参数、返回数据显示内容的js函数名。<br>
	 * 系统默认提供regMoney,regInt,reg2Decimal，<br>
	 * 分别用于货币、整数、保留两位小数三种格式。
	 * @return 当前列数据渲染格式
	 */
	public String getRenderer() {
		return renderer;
	}
	/**
	 * 
	 * @param renderer 格式渲染函数
	 * @see #getRenderer()
	 */
	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}
	/**
	 * 列的宽度，单位：像素。
	 * @return 列的宽度
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * 
	 * @param width
	 * @see #getWidth()
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * 列节点的层次。
	 * 复杂表头经过排列后，根据节点所在位置的深度不同设置不同的level数。<br>
	 * 自顶向下，level值增大，根节点level为1。
	 * @return 列节点的层次 
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * 
	 * @param level 
	 * @see #getLevel()
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	/**
	 * 本列数据的数据类型。
	 * 0：string；1：int；2：double；
	 * 本系统中，小数统一用双精度表示。
	 * @return 数据类型
	 */
	public int getDataType() {
		return dataType;
	}
	/**
	 * 
	 * @param dataType
	 * @see #getDataType()
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public int getLinkAction() {
		return linkAction;
	}
	public void setLinkAction(int linkAction) {
		this.linkAction = linkAction;
	}
	
	public int getPopHeight() {
		return popHeight;
	}
	public void setPopHeight(int popHeight) {
		this.popHeight = popHeight;
	}
	public int getPopWidth() {
		return popWidth;
	}
	public void setPopWidth(int popWidth) {
		this.popWidth = popWidth;
	}
	public ArrayList<LinkTab> getLinkTabs() {
		return LinkTabs;
	}
	public void setLinkTabs(ArrayList<LinkTab> linkTabs) {
		LinkTabs = linkTabs;
	}
	
}
