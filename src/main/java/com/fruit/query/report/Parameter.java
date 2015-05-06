package com.fruit.query.report;
import com.fruit.query.data.*;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 参数定义
 */
public class Parameter {
	private String name;
	private String desc;
	//参数是否隐藏，如果隐藏，不需要用户交互传递。
	private int isHidden=0;
	//参数的录入界面渲染为何种类型。0：文本框；1：下拉框；2：树，3：日期
	private int renderType=0;
	//如果参数渲染为日期控件，日期的格式，默认是年-月-日
	private String dateFormat="Y-m-d";
	//dataType:参数的数据类型。0：string；1：int；2：double；3：cursor
	private int dataType=0;
	//默认值，参数渲染为文本框，默认显示在文本框中内容。
	private String defaultValue="";
	//2013-06-20 默认值规则设置，参数设置成选择录入的，提供规则，用于判定选项中哪个作为默认值。
	//目前提供"_first"，规则，即将第一个选项作为默认值
	private String defaultRule;
	//2013-05-17 参数显示交互时，显示在哪里。isHidden=0时有效。
	//可控制参数交互界面在默认、高级筛选等不同交互界面显示。
	//1：弹出后框里出现；2：工具条中出现；
	private int showMode;
	private String[] validates;
	//2011-04-25 当前参数的默认值绑定到某个特定参数的值，及绑定方式
	private String defaultValueBindTo="";
	private int defaultValBindMode;
	//2013-04-11 参数联动时，影响者设置：需回调的js函数名。
	private String affectCallBack;
	//2013-07-24 参数联动时，影响者设置：影响哪些变量，多个用逗号分隔。
	private String affect;
	//2013-04-11 参数联动时，被影响的参数中设置。受哪个参数的值的影响，多个时以“,”分隔
	private String affectedByParas;
	//默认值设定的规则定义
	private DefaultRuleDefine defaultRuleDefine;
	/**
	 * 如果参数是隐藏的，不需用户交互，其值通过其他方式提供。
	 * bindMode表示值提供的方式。0：固定值；
	 * 1：和请求头中某个请求参数的值绑定，request.getParameter获取值
	 * 2：系统会话中获取session
	 * 3：由实现特定接口的类方法返回值
	 */
	private int bindMode;
	/**
	 * 隐藏参数的值由具体哪个对象提供。bindMode相关。
	 * bindMode=0时，bindTo指参数的具体值；
	 * bindMode=1时，bindTo指request中的某参数名；
	 * bindMode=2时，bindTo指参数值由session中哪个Attribute提供；
	 * bindMode=3时，bindTo指计算参数值所用的接口实现类。
	 */
	private String bindTo;
	//2011-04-14 是否自动为选项添加“全部”选项，该项在renderType为1、2时有意义
	private int autoAll = 0;
	
	//是否能多选。对于renderType=2时有意义
	private int isMulti;
	//是否只能选择底级节点。对于renderType=2时有意义
	private int leafOnly;
	//参数选项的取数来源。0：静态文本；1：sql取数；2：存储过程取数
	private int sourceType;
	//参数选项取数用sql，可以含{}方式的参数
	private String sql;
	//参数选项取数用存储过程时，该存储过程的描述
	private ProcedureBean procedure;
	//2013-09-11 参数输入框的宽度
	private int width = 100;
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public DefaultRuleDefine getDefaultRuleDefine() {
		return defaultRuleDefine;
	}
	public void setDefaultRuleDefine(DefaultRuleDefine defaultRuleDefine) {
		this.defaultRuleDefine = defaultRuleDefine;
	}
	
	/**
	 * 待选项集合
	 * 集合中的每一项是具有固定数据结构的节点
	 * 
	 */
	private List paraOptions;
	private String implClass;
	
	/**
	 * 获取：用于参数待选项集合取数的自定义类。
	 * 该类应实现com.datanew.query.util.IParaItemsService
	 * @return 类全路径
	 * @see com.datanew.query.util.IParaItemsService
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
	 * 参数的数据类型。
	 * 0：string；1：int；2：double；3：cursor
	 * @return 参数类型
	 */
	public int getDataType() {
		return dataType;
	}
	/**
	 * 
	 * @param dataType
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	/**
	 * 获取参数的中文名。
	 * 对于预先提供选项的参数（渲染成下拉框或树），往往有bm-name一对值。
	 * @return 中文名。
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
	/**
	 * 是否隐藏参数。
	 * 0：否，1：是。隐藏参数不渲染在用户交互的参数页面。
	 * @return 是否隐藏
	 */
	public int getIsHidden() {
		return isHidden;
	}
	/**
	 * 
	 * @param isHidden
	 */
	public void setIsHidden(int isHidden) {
		this.isHidden = isHidden;
	}
	/**
	 * 参数选项是否可以多选。
	 * 0：不可多选，1：可多选。<br>
	 * 该属性应用于渲染为树的参数，选中多个选项时，各个值以逗号分隔。
	 * @return 参数选项是否可以多选
	 */
	public int getIsMulti() {
		return isMulti;
	}
	/**
	 * 
	 * @param isMulti
	 */
	public void setIsMulti(int isMulti) {
		this.isMulti = isMulti;
	}
	/**
	 * 是否仅能选中底级节点。
	 * 0：否，能选非底级节点；1：是，只能选底级节点；<br>
	 * 该属性应用于渲染为树的参数。
	 * @return 是否仅能选中底级节点
	 */
	public int getLeafOnly() {
		return leafOnly;
	}
	/**
	 * 
	 * @param leafOnly
	 */
	public void setLeafOnly(int leafOnly) {
		this.leafOnly = leafOnly;
	}
	/**
	 * 获取参数名。
	 * 参数名应符合变量名的规范，以下划线或字母开头，内容为字母、数字、下划线等；
	 * @return 参数名。
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
	 * 获取参数待选项集合取数的存储过程。
	 * sourceType=2时，参数待选项集合取数由存储过程定义。
	 * @return ProcedureBean对象。
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
	 * 获取参数在页面中的交互类型。
	 * 0：文本输入框；1：下拉框；2：树；3：日期
	 * @return 参数录入界面渲染为何种类型。
	 */
	public int getRenderType() {
		return renderType;
	}
	/**
	 * 
	 * @param renderType
	 */
	public void setRenderType(int renderType) {
		this.renderType = renderType;
	}
	/**
	 * 参数待选项集合取数方式。
	 * 0:静态；1：sql语句取数；2：存储过程取数；3：自定义类取数；<br>
	 * 只有在参数渲染类型为下拉框或树时(renderType为1或2)，才需要构造参数待选项集合。<br>
	 * @return 参数待选项取数方式。
	 */
	public int getSourceType() {
		return sourceType;
	}
	/**
	 * 
	 * @param sourceType
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	/**
	 * 参数待选项集合取数sql
	 * @return sql语句
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
	 * 获取参数值绑定方式。
	 * 该属性对于隐藏参数有意义，隐藏参数无交互，其值要和特定内容绑定。<br>
	 * 0：固定值；<br>1：和请求头中某个请求参数的值绑定，request.getParameter获取值；<br>
	 * 2：系统会话中获取session；<br>
	 * 3：由实现接口com.datanew.query.util.IParaDataBind的类提供值。
	 * @return 参数值绑定方式(0,1,2,3)
	 */
	public int getBindMode() {
		return bindMode;
	}
	/**
	 * 
	 * @param bindMode
	 */
	public void setBindMode(int bindMode) {
		this.bindMode = bindMode;
	}
	/**
	 * 参数值与之绑定的具体内容。
	 * 根据bindMode的不同，bindTo有不同的含义：
	 * bindMode=0时，bindTo指参数的具体值；<br>
	 * bindMode=1时，bindTo指request中的某参数名；<br>
	 * bindMode=2时，bindTo指参数值由session中哪个Attribute提供；<br>
	 * bindMode=3时，bindTo指计算参数值所用的接口实现类。
	 * @return 参数绑定的内容
	 */
	public String getBindTo() {
		return bindTo;
	}
	/**
	 * 
	 * @param bindTo
	 */
	public void setBindTo(String bindTo) {
		this.bindTo = bindTo;
	}
	/**
	 * 获取静态参数待选项集合内容。
	 * @return 静态参数待选项集合
	 */
	public List getParaOptions() {
		return paraOptions;
	}
	/**
	 * 
	 * @param paraOptions
	 */
	public void setParaOptions(List paraOptions) {
		this.paraOptions = paraOptions;
	}
	/**
	 * 参数的默认值。
	 * @return 如果参数渲染为文本框输入，该属性返回默认显示在文本框中内容。
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * 对参数输入的验证方法，可多个，用分号分隔。
	 * @return 参数的验证方法，js方法。
	 */
	public String[] getValidates() {
		return validates;
	}
	/**
	 * 
	 * @param validates
	 */
	public void setValidates(String[] validates) {
		this.validates = validates;
	}
	/**
	 * 参数渲染为日期控件时，日期的格式，默认是Y-m-d，年-月-日
	 * @return 日期的格式
	 */
	public String getDateFormat() {
		return dateFormat;
	}
	/**
	 * 
	 * @param dateFormat
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public String getAffectCallBack() {
		return affectCallBack;
	}
	public void setAffectCallBack(String affectCallBack) {
		this.affectCallBack = affectCallBack;
	}
	public String getAffectedByParas() {
		return affectedByParas;
	}
	public void setAffectedByParas(String affectedByParas) {
		this.affectedByParas = affectedByParas;
	}
	public String getDefaultValueBindTo() {
		return defaultValueBindTo;
	}
	public void setDefaultValueBindTo(String defaultValueBindTo) {
		this.defaultValueBindTo = defaultValueBindTo;
	}
	public int getDefaultValBindMode() {
		return defaultValBindMode;
	}
	public void setDefaultValBindMode(int defaultValBindMode) {
		this.defaultValBindMode = defaultValBindMode;
	}
	public int getAutoAll() {
		return autoAll;
	}
	public void setAutoAll(int autoAll) {
		this.autoAll = autoAll;
	}
	public String getDefaultRule() {
		return defaultRule;
	}
	public void setDefaultRule(String defaultRule) {
		this.defaultRule = defaultRule;
	}
	public String getAffect() {
		return affect;
	}
	public void setAffect(String affect) {
		this.affect = affect;
	}
	public int getShowMode() {
		return showMode;
	}
	public void setShowMode(int showMode) {
		this.showMode = showMode;
	}
}
