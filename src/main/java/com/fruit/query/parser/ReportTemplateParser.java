package com.fruit.query.parser;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import java.util.*;
import java.io.*;
import com.fruit.query.report.*;
import com.fruit.query.util.*;
import com.fruit.query.data.*;
import com.softwarementors.extjs.djn.StringUtils;
/**
 * 
 * @author wxh
 *2009-3-12
 *TODO 报表模板（设计内容）向内存对象的解析
 */
public class ReportTemplateParser {
	private static ReportTemplateParser tmpParser;
	private ReportTemplateParser(){
		
	}
	/**
	 * 获取解析器实例。
	 * singleton模式，调用返回的是同一个解析器实例。 
	 * @return 解析器实例。
	 */
	public static ReportTemplateParser getParser(){
		if(tmpParser==null)
			tmpParser=new ReportTemplateParser();
		return tmpParser;
	}
	/**
	 * 根据报表设计内容解析成内存的报表对象
	 * @param tmpContent 报表设计内容的字符串，xml格式。
	 * @return 经过解析的报表对象。
	 * @throws ParseReportException
	 */
	public Report parseTemplateToReport(String tmpContent)throws ParseReportException{
		Report rpt=null;
		if(tmpContent==null||"".equals(tmpContent))return null;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tmpContent.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return null;
		    rpt=new Report();
		    rpt.setId(root.attributeValue("id"));
		    rpt.setName(root.attributeValue("name"));
		    //rpt.setDescription(root.attributeValue("description"));
		    int multiUnit = 0;
		    try{
		    	multiUnit = Integer.parseInt(root.attributeValue("multiUnit"));
		    }catch(Exception e){}
		    rpt.setMultiUnit(multiUnit);
		    rpt.setDefaultUnit(root.attributeValue("defaultUnit"));
		    rpt.setSupportUnits(root.attributeValue("supportUnits"));
		    int totalRow=0;
		    try{
		    	totalRow=Integer.parseInt(root.attributeValue("totalRow"));
		    }catch(Exception e){}
		    rpt.setTotalRow(totalRow);
		    int totalPosition=0;
		    try{
		    	totalPosition=Integer.parseInt(root.attributeValue("totalPosition"));
		    }catch(Exception e){}
		    rpt.setTotalPosition(totalPosition);
		    String tFlds=root.attributeValue("totalFields");
		    String[] totalFlds=tFlds==null?null:tFlds.split(",");
		    rpt.setTotalFields(totalFlds);
		    
		    String tLabelIndex=root.attributeValue("totalLabelIndex");
		    rpt.setTotalLabelIndex(tLabelIndex);
		    //是否远程排序
		    String srSort=root.attributeValue("remoteSort"); 
		    int remoteSort=0;
		    try{
		    	remoteSort=Integer.parseInt(srSort);
		    }catch(Exception e){}
		    rpt.setRemoteSort(remoteSort);
		    //2013-12-12 是否可以隐藏0
		    String sZeroHide=root.attributeValue("zeroCanHide"); 
		    int zeroCanHide = 0;
		    try{
		    	zeroCanHide=Integer.parseInt(sZeroHide);
		    }catch(Exception e){}
		    rpt.setZeroCanHide(zeroCanHide);
		    //2013-06-05 是否有图表
		    int hasChart = 0;
		    try{
		    	hasChart = Integer.parseInt(root.attributeValue("hasChart"));
		    }catch(Exception e){}
		    rpt.setHasChart(hasChart);
		    //2014-11-14 是否是直接导出到Excel
		    int directExport = 0;
		    try{
		    	directExport = Integer.parseInt(root.attributeValue("directExport"));
		    }catch(Exception e){}
		    rpt.setDirectExport(directExport);
		    
		    Element dscnode=root.element("description");
		    if(dscnode!=null){
		    	rpt.setDescription(dscnode.getText());
		    }
    		
		    Element psnodes=root.element("parameters");
		    List paras=parseParas(psnodes);
		    rpt.setParas(paras);
		    Element pfnodes=root.element("parametersForFilter");
		    List parasForFilter = null;
    		if(pfnodes!=null){
    			parasForFilter = parseParas4Filter(pfnodes);
    			rpt.setParasForFilter(parasForFilter);
    		}
    		//也构建一份参数定义的map
		    if(paras!=null){
		    	Map parasMap=new HashMap();
		    	for(int i=0;i<paras.size();i++){
		    		Parameter para=(Parameter)paras.get(i);
		    		if(para==null){
		    			continue;
		    		}
		    		parasMap.put(para.getName(), para);
		    	}
		    	rpt.setParasMap(parasMap);
		    }
		    if(parasForFilter!=null){
		    	Map fparasMap=new HashMap();
		    	for(int i=0;i<parasForFilter.size();i++){
		    		ParameterForFilter fpara=(ParameterForFilter)parasForFilter.get(i);
		    		if(fpara==null){
		    			continue;
		    		}
		    		fparasMap.put(fpara.getName(), fpara);
		    	}
		    	rpt.setParas4FilterMap(fparasMap);
		    }
		    /**
		     * 2009-05-14 增加参数的进一步加工功能，对参数加工节点的解析
		     */
		    Element proNodes=root.element("paraProcesses");
		    Map paraPros=parseParaProcesses(proNodes);
    		rpt.setParaProcesses(paraPros);
    		//end 2009-05-14
    		
    		Element tnode=root.element("title");
    		Title title=parseTitle(tnode);
    		rpt.setTitle(title);
    		//20131011 增加表头区域
    		Element hnode=root.element("head");
    		Head head=parseHead(hnode);
    		rpt.setHead(head);
    		
    		Element colnode=root.element("columns");
    		ColumnDefine columnDef=parseColumnDef(colnode);
    		rpt.setColumnDef(columnDef);
    		
    		Element dataNode=root.element("rptData");
    		DataDefine dataDef=parseDataDef(dataNode);
    		rpt.setDefaultDataDef(dataDef);
    		//2014-11-12增加多个数据集定义的支持。
    		//如果没有定义原先单数的rptData节点，以DataSets的第一个子节点作为defaultDataSet；
    		Element dtsNode = root.element("DataSets");
    		if(dtsNode!=null&&dtsNode.elementIterator("rptData")!=null){
    			List dts = new ArrayList();
    			for(Iterator it=dtsNode.elementIterator("rptData");it.hasNext();){
					Element dtNode=(Element)it.next();
					DataDefine subdtDef=parseDataDef(dtNode);
					dts.add(subdtDef);
    			}
    			rpt.setDataDefines(dts);
    			if(rpt.getDefaultDataDef()==null){
    				rpt.setDefaultDataDef((DataDefine)dts.get(0));
    			}
    		}
    		//20131011 增加表尾区域
    		Element fnode=root.element("foot");
    		Foot foot=parseFoot(fnode);
    		rpt.setFoot(foot);
    		
    		Element groupNode=root.element("grouping");
    		Grouping groupDef=parseGrouping(groupNode);
    		rpt.setGroupDef(groupDef);
    		//引擎负责分组时，强制取数后分页
    		if(groupDef!=null&&groupDef.getEnabled()==1){
    			rpt.getDefaultDataDef().setPagingMode(1);
    		}
    		//2014-06-04 增加图表功能
    		Element chartNode = root.element("chart");
    		Chart chart = parseChart(chartNode);
    		rpt.setChart(chart);
    		
    		//2014-11-12 增加按模板导出功能
    		Element expNode = root.element("export");
    		Export exp = parseExport(expNode);
    		rpt.setExportInfo(exp);
		}catch(Exception e){
			throw new ParseReportException(e.toString());
		}
		return rpt;
	}
	private Export parseExport(Element expNode) {
		if(expNode==null){
			return null;
		}
		Export exp = new Export();
		exp.setTemplate(expNode.attributeValue("template"));
		exp.setExpFileName(expNode.attributeValue("expName"));
		return exp;
	}
	/**
	 * 2014-06-04 解析图表报表
	* @param chartNode
	* @return
	 */
	private Chart parseChart(Element cnode) {
		if(cnode==null){
			return null;
		}
		Chart chart = new Chart();
		chart.setId(cnode.attributeValue("id"));
		String chartType = cnode.attributeValue("chartType");
		chart.setChartType(chartType);
		if(!StringUtils.isEmpty(chartType)){
			String cFile = QueryConfig.getConfig().getChartFile(chartType);
			chart.setChartFile(cFile);
		}
		int width=450;
		try{
			String sWidth=cnode.attributeValue("width");
			width=Integer.parseInt(sWidth);
		}catch(Exception e){
			width=450;
		}
		chart.setWidth(width);
		int height=300;
		try{
			String sHeight=cnode.attributeValue("height");
			height=Integer.parseInt(sHeight);
		}catch(Exception e){
			height=300;
		}
		chart.setHeight(height);
		chart.setDataTemplateName(cnode.attributeValue("dataTemplateName"));
		String tf = cnode.attributeValue("templateFormat");
		if(StringUtils.isEmpty(tf)){
			tf = "xml";
		}else{
			tf = "vm";
		}
		chart.setTemplateFormat(tf);
		//数据描述
		Element dnode=cnode.element("data");
		if(dnode!=null){
			int sourceType=0;
			try{
				String sSrcType=dnode.attributeValue("sourceType");
				sourceType=Integer.parseInt(sSrcType);
			}catch(Exception e){
				sourceType=0;
			}
			chart.setSourceType(sourceType);
			if(sourceType==1){//如果是sql语句取数，读取sql语句配置
				Element sqlNode=dnode.element("sql");
				if(sqlNode!=null){
					String sql=sqlNode.getText();
					chart.setSql(sql);
				}
			}else if(sourceType==2){//如果是存储过程取数，读取存储过程配置
				Element proNode=dnode.element("procedure");
				if(proNode!=null){
					ProcedureBean pro=parseProcedure(proNode);
					chart.setProcedure(pro);
				}
			}
			int dataFormat=0;
			try{
				String sdformat=dnode.attributeValue("dataFormat");
				dataFormat=Integer.parseInt(sdformat);
			}catch(Exception e){
				dataFormat=0;
			}
			chart.setDataFormat(dataFormat);
			int isMultiSeries=0;
			try{
				String sMultiSeries=dnode.attributeValue("isMultiSeries");
				isMultiSeries=Integer.parseInt(sMultiSeries);
			}catch(Exception e){
				isMultiSeries=0;
			}
			chart.setIsMultiSeries(isMultiSeries);
			chart.setCategoryIndex(dnode.attributeValue("categoryIndex"));
			chart.setDataIndex(dnode.attributeValue("dataIndex"));
			chart.setSeriesIndex(dnode.attributeValue("seriesIndex"));
		}
		//2015-08-20 combine图表时，按series名对应其要渲染成的图表类型
		Element snode=cnode.element("seriesRender");
		if(snode!=null&&snode.elementIterator("series")!=null){
			Map renderMap = new HashMap();
			for(Iterator iit=snode.elementIterator("series");iit.hasNext();){
				Element tn=(Element)iit.next();
				String sname=tn.attributeValue("sname");
				String rd=tn.attributeValue("renderAs");
				renderMap.put(sname, rd);
			}
			chart.setRenderMap(renderMap);
		}
		return chart;
	}
	private Foot parseFoot(Element fnode) {
		if(fnode==null){
			return null;
		}
		Foot foot = new Foot();
		int height = 50;
		try{
			String sHeight=fnode.attributeValue("height");
			height=Integer.parseInt(sHeight);
		}catch(Exception e){
			height=50;
		}
		foot.setHeight(height);
		foot.setLeftExp(fnode.attributeValue("left"));
		foot.setCenterExp(fnode.attributeValue("center"));
		foot.setRightExp(fnode.attributeValue("right"));
		foot.setlStyle(fnode.attributeValue("lStyle"));
		foot.setcStyle(fnode.attributeValue("cStyle"));
		foot.setrStyle(fnode.attributeValue("rStyle"));
		return foot;
	}
	private Head parseHead(Element hnode) {
		if(hnode==null){
			return null;
		}
		Head head = new Head();
		int titleInHead = 1;
		try{
			String sTitleInHead=hnode.attributeValue("titleInHead");
			titleInHead=Integer.parseInt(sTitleInHead);
		}catch(Exception e){
			titleInHead=1;
		}
		head.setTitleInHead(titleInHead);
		int height = 50;
		try{
			String sHeight=hnode.attributeValue("height");
			height=Integer.parseInt(sHeight);
		}catch(Exception e){
			height=50;
		}
		head.setHeight(height);
		head.setStyle(hnode.attributeValue("style"));
		Element snode=hnode.element("subTitle");
		if(snode!=null){
			SubTitle subTitle = new SubTitle();
			subTitle.setLeftExp(snode.attributeValue("left"));
			subTitle.setCenterExp(snode.attributeValue("center"));
			subTitle.setRightExp(snode.attributeValue("right"));
			subTitle.setlStyle(snode.attributeValue("lStyle"));
			subTitle.setcStyle(snode.attributeValue("cStyle"));
			subTitle.setrStyle(snode.attributeValue("rStyle"));
			head.setSubTitle(subTitle);
		}	
		return head;
	}
	private Parameter parseParameter(Element pnode,boolean isForFilter){
		if(pnode==null){
			return null;
		}
		Parameter para=new Parameter();
		para.setName(pnode.attributeValue("name"));
		para.setDesc(pnode.attributeValue("desc"));
		int isHidden=0;
		try{
			String sHidden=pnode.attributeValue("hidden")==null?"0":pnode.attributeValue("hidden");
			isHidden=Integer.parseInt(sHidden);
		}catch(Exception e){
			isHidden=0;
		}
		para.setIsHidden(isHidden);
		int bMode=1;
		try{
			String sBind=pnode.attributeValue("bindMode")==null?"1":pnode.attributeValue("bindMode");
			bMode=Integer.parseInt(sBind);
		}catch(Exception e){
			bMode=0;
		}
		para.setBindMode(bMode);
		para.setBindTo(pnode.attributeValue("bindTo"));
		//2011-04-14 增加autoAll属性。默认为0.如果为1，自动增加一个“全部”选项。值为-1
		String sAll = pnode.attributeValue("autoAll");
		int autoAll = 0;
		if("1".equals(sAll)){
			autoAll=1;
		}
		para.setAutoAll(autoAll);
		
		int dType=0;
		String st=pnode.attributeValue("dataType");
		if("int".equalsIgnoreCase(st)||"1".equals(st)){
			dType=1;
		}else if("double".equalsIgnoreCase(st)||"2".equals(st)){
			dType=2;				
		}else if("cursor".equalsIgnoreCase(st)||"3".equals(st)){
			dType=3;
		}else{
			dType=0;
		}
		para.setDataType(dType);
		//2009-04-30 默认值
		para.setDefaultValue(pnode.attributeValue("defaultValue"));
		//2011-04-25 默认值和哪个参数绑定。
		para.setDefaultValueBindTo(pnode.attributeValue("defaultValueBindTo"));
		//绑定的参数来自哪里。与bindTo属性同
		String sDefaultValBindMode = pnode.attributeValue("defaultValBindMode");
		int defaultValBindMode =0;
		try{
			defaultValBindMode = Integer.parseInt(sDefaultValBindMode);
		}catch(Exception e){}
		para.setDefaultValBindMode(defaultValBindMode);
		//2013-06-20 设定哪个选项为默认值的规则
		para.setDefaultRule(pnode.attributeValue("defaultRule"));
		//2013-07-23默认值的解析
		DefaultRuleDefine drd= parseParamDefaultDefine(pnode.element("defaultRule"));
		para.setDefaultRuleDefine(drd);
		//2013-04-11
		para.setAffectCallBack(pnode.attributeValue("affectCallBack"));
		para.setAffect(pnode.attributeValue("affect"));
		para.setAffectedByParas((pnode.attributeValue("affectedByParas")));
		//2009-05-04 参数验证方法的添加
		String svds=pnode.attributeValue("validates");
		String[] vds=svds==null?null:svds.split(",");
		para.setValidates(vds);
		
		int renderType=0;
		try{
			String srType=pnode.attributeValue("renderType")==null?"0":pnode.attributeValue("renderType");
			renderType=Integer.parseInt(srType);
		}catch(Exception e){
			renderType=0;
		}
		para.setRenderType(renderType);
		//2009-05-11 设置日期格式，日期格式对于renderType=3有意义
		String dateFormat=pnode.attributeValue("dateFormat")==null?"Y-m-d":pnode.attributeValue("dateFormat");
		para.setDateFormat(dateFormat);
		//2013-05-17
		int showMode=isForFilter?1:2;
		String sShowMode=pnode.attributeValue("showMode");
		try{
			showMode = Integer.parseInt(sShowMode);
		}catch(Exception e){}
		para.setShowMode(showMode);
		//2013-09-11  参数输入框的宽度
		int width = 100;
		String sWith=pnode.attributeValue("width");
		try{
			width = Integer.parseInt(sWith);
		}catch(Exception e){}
		para.setWidth(width);
		
		//如果参数有选项数据子节点
		Element pd=pnode.element("paraDetail");
		if (pd != null) {
			int isMulti = 0;
			try {
				String sm = pd.attributeValue("multi");
				isMulti = Integer.parseInt(sm);
			} catch (Exception e) {
				isMulti = 0;
			}
			para.setIsMulti(isMulti);
			int leafOnly = 1;
			try {
				String lo = pd.attributeValue("leafOnly");
				leafOnly = Integer.parseInt(lo);
			} catch (Exception e) {
				leafOnly = 1;
			}
			para.setLeafOnly(leafOnly);
			int sourceType = 0;
			try {
				String sSrcType = pd.attributeValue("sourceType");
				sourceType = Integer.parseInt(sSrcType);
			} catch (Exception e) {
				sourceType = 0;
			}
			para.setSourceType(sourceType);
			// 如果是静态数据源，当下就读取待选项数据
			if (sourceType == 0) {
				Element pitems = pd.element("paraItems");
				if (pitems != null && pitems.elementIterator("item") != null) {
					List paraOptions = new ArrayList();
					for (Iterator pit = pitems.elementIterator("item"); pit.hasNext();) {
						OptionItem oi = new OptionItem();
						Element pi = (Element) pit.next();
						oi.setBm(pi.attributeValue("bm"));
						oi.setName(pi.attributeValue("name"));
						oi.setPid(pi.attributeValue("pid"));
						int isleaf = 1;
						try {
							String sLeaf = pi.attributeValue("isleaf");
							isleaf = Integer.parseInt(sLeaf);
						} catch (Exception e) {
							isleaf = 1;
						}
						oi.setIsleaf(isleaf);
						// 2009-04-30 默认值设置
						int isDefault = 0;
						try {
							String sdf = pi.attributeValue("isDefault");
							isDefault = Integer.parseInt(sdf);
						} catch (Exception e) {
						}
						oi.setIsDefault(isDefault);

						paraOptions.add(oi);
					}
					para.setParaOptions(paraOptions);
				}
			} else if (sourceType == 1) {// 如果是sql语句取数，读取sql语句配置
				Element sqlNode = pd.element("sql");
				if (sqlNode != null) {
					String sql = sqlNode.getText();
					para.setSql(sql);
				}
			} else if (sourceType == 2) {// 如果是存储过程取数，读取存储过程配置
				Element proNode = pd.element("procedure");
				if (proNode != null) {
					ProcedureBean pro = parseProcedure(proNode);
					para.setProcedure(pro);
				}
			} else {
				Element clNode = pd.element("class");
				if (clNode != null) {
					para.setImplClass(clNode.attributeValue("path"));
				}
			}
		}
		return para;
	}
	private DefaultRuleDefine parseParamDefaultDefine(Element dn) {
		if(dn==null){
			return null;
		}
		DefaultRuleDefine drd = new DefaultRuleDefine();
		int sourceType=0;
		try{
			String sSrcType=dn.attributeValue("sourceType");
			sourceType=Integer.parseInt(sSrcType);
		}catch(Exception e){
			sourceType=0;
		}
		drd.setSourceType(sourceType);
		if(sourceType==1){//如果是sql语句取数，读取sql语句配置
			Element sqlNode=dn.element("sql");
			if(sqlNode!=null){
				String sql=sqlNode.getText();
				drd.setSql(sql);
			}
		}else if(sourceType==2){//如果是存储过程取数，读取存储过程配置
			Element proNode=dn.element("procedure");
			if(proNode!=null){
				ProcedureBean pro=parseProcedure(proNode);
				drd.setProcedure(pro);
			}
		}else{
			Element clNode=dn.element("class");
			if(clNode!=null){
				drd.setImplClass(clNode.attributeValue("path"));
			}
		}
		return drd;
	}
	/**
	 * 参数节点解析
	 * @param pnodes 参数节点parameters
	 * @return 参数集合。其中的每个元素都是Parameter对象。
	 * @see com.datanew.query.report.Parameter
	 */
	public List parseParas(Element pnodes){
		if(pnodes==null){
			return null;
		}
		List paras=null;
		//参数节点解析
		if(pnodes!=null&&pnodes.elementIterator("para")!=null){
			paras=new ArrayList();
			for(Iterator it=pnodes.elementIterator("para");it.hasNext();){
				Element pnode=(Element)it.next();
				Parameter para=parseParameter(pnode,false);
				paras.add(para);
			}
		}
		return paras;
	}
	
	public List parseParas4Filter(Element pnodes){
		if(pnodes==null){
			return null;
		}
		List paras=null;
		if(pnodes!=null&&pnodes.elementIterator("para")!=null){
			paras=new ArrayList();
			for(Iterator it=pnodes.elementIterator("para");it.hasNext();){
				Element pnode=(Element)it.next();
				Parameter para=parseParameter(pnode,true);
				ParameterForFilter fpara = new ParameterForFilter(para);
				fpara.setFilterFld(pnode.attributeValue("filterFld"));
				fpara.setValueOprator(pnode.attributeValue("valueOprator"));
				paras.add(fpara);
			}
		}
		return paras;
	}
	/**
	 * 报表的title解析
	 * @param tnode title节点。
	 * @return Title对象
	 * @see com.datanew.query.report.Title
	 */
	public Title parseTitle(Element tnode){
		if(tnode==null){
			return null;
		}
		Title t=new Title();
		String cont=tnode.getText();
		t.setTitleExp(cont);
		return t;
	}
	
	/**
	 * 分析列定义节点
	 * @param cnode 列集合节点columns
	 * @return 报表的列定义信息对象。
	 * @see com.datanew.query.report.ColumnDefine
	 */
	public ColumnDefine parseColumnDef(Element cnode){
		if(cnode==null){
			return null;
		}
		ColumnDefine cd=new ColumnDefine();
		int complex=0;
		try{
			complex=Integer.parseInt(cnode.attributeValue("complex")); 
		}catch(Exception e){}
		cd.setIsComplex(complex);
		
		int sType=0;
		try{
			sType=Integer.parseInt(cnode.attributeValue("sourceType")); 
		}catch(Exception e){}
		cd.setSourceType(sType);
		
		int totalCol=0;
		try{
			totalCol=Integer.parseInt(cnode.attributeValue("totalCol")); 
		}catch(Exception e){}
		cd.setTotalCol(totalCol);
		
		int totalPos=0;
		try{
			totalPos=Integer.parseInt(cnode.attributeValue("totalPos")); 
		}catch(Exception e){}
		cd.setTotalPos(totalPos);
		
		int totalColWidth=0;
		try{
			totalColWidth=Integer.parseInt(cnode.attributeValue("totalColWidth")); 
		}catch(Exception e){}
		cd.setTotalColWidth(totalColWidth);
		cd.setTotalColRenderer(cnode.attributeValue("totalColRenderer"));
		int rowNumber = 0;
		try{
			rowNumber=Integer.parseInt(cnode.attributeValue("rowNumber")); 
		}catch(Exception e){}
		cd.setRowNumber(rowNumber);
		if(sType==0){//静态数据来源，都在模板加载时构造列信息
			cd.setColumnBuild(1);
			if(cnode!=null&&cnode.elementIterator("column")!=null){
				List columns=new ArrayList();
				for(Iterator it=cnode.elementIterator("column");it.hasNext();){
					Column col=new Column();
					Element cn=(Element)it.next();
					int cMode=0;
					try{
						cMode=Integer.parseInt(cn.attributeValue("calculate_mode"));
					}catch(Exception e){}
					col.setCalculate_mode(cMode);
					if(cMode==1){
						int fPos=0;
						try{
							String sfPos=cn.attributeValue("funcPositon");
							fPos=Integer.parseInt(sfPos);
						}catch(Exception e){}
						col.setFuncPosition(fPos);
					}
					col.setColFunction(cn.attributeValue("colFunction")==null?"":cn.attributeValue("colFunction"));
					col.setColId(cn.attributeValue("colId"));
					col.setColName(cn.attributeValue("colName"));
					col.setDataIndex(cn.attributeValue("dataIndex"));
					
					int dtype=0;
					String st=cn.attributeValue("dataType");
					if("int".equalsIgnoreCase(st)||"1".equals(st)){
						dtype=1;
					}else if("double".equalsIgnoreCase(st)||"2".equals(st)){
						dtype=2;				
					}else if("cursor".equalsIgnoreCase(st)||"3".equals(st)){
						dtype=3;
					}else if("date".equalsIgnoreCase(st)||"9".equals(st)){
						dtype=9;
					}else{
						dtype=0;
					}
					col.setDataType(dtype);	
					
					int isleaf=1;
					try{
						isleaf=Integer.parseInt(cn.attributeValue("isleaf"));
					}catch(Exception e){}
					col.setIsleaf(isleaf);	
					col.setPid(cn.attributeValue("pid")==null?"":cn.attributeValue("pid"));
					col.setRenderer(cn.attributeValue("renderer")==null?"":cn.attributeValue("renderer"));
					int width=100;
					try{
						width=Integer.parseInt(cn.attributeValue("width"));
					}catch(Exception e){}
					col.setWidth(width);
					
					int hidden=0;
					try{
						hidden=Integer.parseInt(cn.attributeValue("hidden"));
					}catch(Exception e){}
					col.setIsHidden(hidden);
					
					int readOnly=0;
					try{
						readOnly=Integer.parseInt(cn.attributeValue("readOnly"));
					}catch(Exception e){}
					col.setReadOnly(readOnly);
					//2013-05-17
					int order = 0;
					try{
						order=Integer.parseInt(cn.attributeValue("isOrder"));
					}catch(Exception e){
					}
					col.setIsOrder(order);
					//2013-06-24
					col.setEditor(cn.attributeValue("editor")==null?"":cn.attributeValue("editor"));
					//2013-08-15 是否可以多单位显示
					int isMultiUnit = 0;
					try{
						isMultiUnit = Integer.parseInt(cn.attributeValue("isMultiUnit"));
					}catch(Exception e){
					}
					col.setIsMultiUnit(isMultiUnit);
					//2013-08-30 链接报表
					int isLink = 0;
					try{
						isLink = Integer.parseInt(cn.attributeValue("isLink"));
					}catch(Exception e){
					}
					col.setIsLink(isLink);
					//2015-10-29 修改链接报表功能，允许以弹出框方式打开，弹出窗体可以分tab显示多张链接报表
					String linkAction = cn.attributeValue("linkAction");
					if("popup".equalsIgnoreCase(linkAction)||"1".equals(linkAction)){
						col.setLinkAction(1);
					}else{
						col.setLinkAction(0);
					}
					if(col.getIsLink()==1){
						if(col.getLinkAction()==0){
							col.setLinkParams(cn.attributeValue("linkParams"));
							col.setTarget(cn.attributeValue("target"));
							col.setLinkTo(cn.attributeValue("linkTo"));
						}else{
							String sPopHeight = cn.attributeValue("popHeight");
							String sPopWidth = cn.attributeValue("popWidth");
							int popHeight = 480;
							int popWidth = 640;
							try{
								popHeight = Integer.parseInt(sPopHeight);
							}catch(Exception e){
							}
							try{
								popWidth = Integer.parseInt(sPopWidth);
							}catch(Exception e){
							}
							col.setPopHeight(popHeight);
							col.setPopWidth(popWidth);
							//读取分tab的信息
							ArrayList<LinkTab> linkTabs = null;
							if(cn!=null&&cn.elementIterator("linkTab")!=null){
								linkTabs=new ArrayList<LinkTab>();
								for(Iterator lit=cn.elementIterator("linkTab");lit.hasNext();){
									Element ltnode=(Element)lit.next();
									LinkTab ltb=parseLinkTab(ltnode);
									linkTabs.add(ltb);
								}
							}
							col.setLinkTabs(linkTabs);
						}
					}
					//2013-09-10 增加属性，是否分组字段。
					int isGroup = 0;
					try{
						isGroup = Integer.parseInt(cn.attributeValue("isGroup"));
					}catch(Exception e){
					}
					col.setIsGroup(isGroup);
					//2013-12-12 增加属性，是否可以隐藏0值
					int hideZero=0;
					try{
						hideZero = Integer.parseInt(cn.attributeValue("hideZero"));
					}catch(Exception e){
					}
					col.setHideZero(hideZero);
					//2015-03-20 增加属性，对齐方式，默认是否显示
					col.setAlign(cn.attributeValue("align"));
					int defaultHide = 0;
					try{
						defaultHide = Integer.parseInt(cn.attributeValue("defaultHide"));
					}catch(Exception e){
					}
					col.setDefaultHide(defaultHide);
					columns.add(col);
				}
				cd.setColumns(columns);
			}
		}else{
			int cb=0;
			try{
				cb=Integer.parseInt(cnode.attributeValue("columnBuild")); 
			}catch(Exception e){}
			cd.setColumnBuild(cb);
			
			if(sType==1){
				Element sqlNode=cnode.element("sql");
				if(sqlNode!=null){
					String sql=sqlNode.getText();
					cd.setSql(sql);
				}
			}else if(sType==2){
				Element proNode=cnode.element("procedure");
				if(proNode!=null){
					ProcedureBean pro=parseProcedure(proNode);
					cd.setProcedure(pro);
				}
			}else{
				Element clNode=cnode.element("class");
				if(clNode!=null){
					cd.setImplClass(clNode.attributeValue("path"));
				}
			}
		}
		return cd;
	}
	/**
	 * 2015-10-29 解析链接弹出窗体的各个tab信息
	 * @return
	 */
	private LinkTab parseLinkTab(Element ltnode){
		if(ltnode==null){
			return null;
		}
		LinkTab ltb=new LinkTab();
		ltb.setTitle(ltnode.attributeValue("title"));
		ltb.setLinkParams(ltnode.attributeValue("linkParams"));
		ltb.setLinkTo(ltnode.attributeValue("linkTo"));
		return ltb;
	}
	/**
	 * 解析取数描述信息
	 * @param dnode 报表数据定义节点rptData
	 * @return 报表数据定义信息对象
	 * @see com.datanew.query.report.DataDefine
	 */
	public DataDefine parseDataDef(Element dnode){
		if(dnode==null){
			return null;
		}
		DataDefine df=new DataDefine();
		int canPaging=0;
		try{
			canPaging=Integer.parseInt(dnode.attributeValue("canPaging")); 
		}catch(Exception e){}
		df.setCanPaging(canPaging);
		
		int pMode=0;
		try{
			pMode=Integer.parseInt(dnode.attributeValue("pagingMode")); 
		}catch(Exception e){}
		df.setPagingMode(pMode);
		
		int ps=20;
		try{
			ps=Integer.parseInt(dnode.attributeValue("defaultPageSize")); 
		}catch(Exception e){}
		df.setDefaultPageSize(ps);
		
		int ms=200;
		try{
			ms=Integer.parseInt(dnode.attributeValue("maxSize")); 
		}catch(Exception e){}
		df.setMaxSize(ms);
		//2009-05-05 若默认值超过了每页记录数限制，以每页记录数限制值作为默认值。
		if(df.getMaxSize()<df.getDefaultPageSize()){
			df.setDefaultPageSize(df.getMaxSize());
		}
		//2009-05-23 增加功能，如果不分页，解析是否自动逐层小计的设置。
		if(df.getPagingMode()==0){
			int autoSubTotal=0;
			try{
				autoSubTotal=Integer.parseInt(dnode.attributeValue("aotuSubTotal")); 
			}catch(Exception e){}
			df.setAutoSubTotal(autoSubTotal);
			df.setRecordID(dnode.attributeValue("recordID"));
			df.setParentID(dnode.attributeValue("parentID"));
		}
		int st=1;
		try{
			st=Integer.parseInt(dnode.attributeValue("sourceType")); 
		}catch(Exception e){}
		df.setSourceType(st);
		//2014-11-12
		String name = dnode.attributeValue("name");
		df.setName(name);
		if(st==1){
			Element snode=dnode.element("sql");
			if(snode!=null){
				df.setSql(snode.getText());
			}	
		}else if(st==2){
			Element proNode=dnode.element("procedure");
			if(proNode!=null){
				ProcedureBean pro=parseProcedure(proNode);
				df.setProcedure(pro);
			}
		}else{
			Element clNode=dnode.element("class");
			if(clNode!=null){
				df.setImplClass(clNode.attributeValue("path"));
			}
		}
		return df;
	}
	
	/**
	 * 解析分组信息
	 * @param gnode 报表分组信息定义节点grouping
	 * @return 分组定义对象
	 * @see com.datanew.query.report.Grouping
	 */
	public Grouping parseGrouping(Element gnode){
		if(gnode==null){
			return null;
		}
		Grouping gf=new Grouping();
		//如果有分组的设置，默认情况就认为是启用的。
		int enabled=1;
		try{
			enabled=Integer.parseInt(gnode.attributeValue("enabled")); 
		}catch(Exception e){}
		gf.setEnabled(enabled);
		
		gf.setGroupBy(gnode.attributeValue("groupBy"));
		
		int gPos=1;
		try{
			gPos=Integer.parseInt(gnode.attributeValue("enabled")); 
		}catch(Exception e){}
		gf.setGroupPosition(gPos);
		
		gf.setLabel(gnode.attributeValue("label"));
		gf.setLabelColIndex(gnode.attributeValue("labelColIndex"));
		//参与分组小计的字段。
		if(gnode!=null&&gnode.elementIterator("gField")!=null){
			List calFlds=new ArrayList();
			for(Iterator it=gnode.elementIterator("gField");it.hasNext();){
				Element gfNode=(Element)it.next();
				calFlds.add(gfNode.getText());
			}
			gf.setFeidsCalculated(calFlds);
		}
		return gf;
	} 
	/**
	 * 解析存储过程的描述。
	 * 该步骤在多个报表组成部分都用到
	 * @param proNode 
	 * @return 存储过程定义对象
	 */
	private ProcedureBean parseProcedure(Element proNode){
		if(proNode==null)return null;
		
		ProcedureBean pro=new ProcedureBean();
		pro.setName(proNode.attributeValue("name"));
		int dsi=1;
		try{
			String sdIndex=proNode.attributeValue("datasetIndex");
			dsi=Integer.parseInt(sdIndex);
		}catch(Exception e){
			dsi=1;
		}
		pro.setDataSetIndex(dsi);
		/**
		 * 2010-07-01增加属性，用于取数分页时。如果取数前分页，该属性表示总记录数输出参数的索引
		 */
		int ti=1;
		try{
			String tIndex=proNode.attributeValue("totalIndex");
			ti=Integer.parseInt(tIndex);
		}catch(Exception e){
			ti=1;
		}
		pro.setTotalIndex(ti);
		/**
		 * 2009-05-14 增加属性，用于参数加工的存储过程，该属性代表加工后的字符串在过程输出参数中的索引
		 */
		int opIndex=1;
		try{
			String sOpIndex=proNode.attributeValue("outPutInfoIndex");
			opIndex=Integer.parseInt(sOpIndex);
		}catch(Exception e){
			opIndex=0;
		}
		pro.setOutPutInfoIndex(opIndex);
		
		//过程的输入参数
		if(proNode!=null&&proNode.elementIterator("in")!=null){
			List proIns=new ArrayList();
			for(Iterator iit=proNode.elementIterator("in");iit.hasNext();){
				ProParaIn ppi=new ProParaIn();
				Element piNode=(Element)iit.next();
				int refMode=1;
				try{
					refMode=Integer.parseInt(piNode.attributeValue("referMode"));
				}catch(Exception e){}
				ppi.setReferMode(refMode);
				
				if(refMode==1){
					ppi.setReferTo(piNode.attributeValue("referTo"));
				}else{
					ppi.setValue(piNode.attributeValue("value"));
					int piDt=0;
					String sPidt=piNode.attributeValue("dataType");
					if("int".equalsIgnoreCase(sPidt)||"1".equals(sPidt)){
						piDt=1;
					}else if("double".equalsIgnoreCase(sPidt)||"2".equals(sPidt)){
						piDt=2;
					}else if("cursor".equalsIgnoreCase(sPidt)||"3".equals(sPidt)){
						piDt=3;
					}
					ppi.setDataType(piDt);
				}									
				proIns.add(ppi);
			}
			pro.setInParas(proIns);
		}
		//过程的输出参数
		if(proNode!=null&&proNode.elementIterator("out")!=null){
			List proOuts=new ArrayList();
			for(Iterator oit=proNode.elementIterator("out");oit.hasNext();){
				ProParaOut ppo=new ProParaOut();
				Element poNode=(Element)oit.next();
				int poDt=0;
				String sPodt=poNode.attributeValue("dataType");
				if("int".equalsIgnoreCase(sPodt)||"1".equals(sPodt)){
					poDt=1;
				}else if("double".equalsIgnoreCase(sPodt)||"2".equals(sPodt)){
					poDt=2;
				}else if("cursor".equalsIgnoreCase(sPodt)||"3".equals(sPodt)){
					poDt=3;
				}
				ppo.setDataType(poDt);
				proOuts.add(ppo);
			}
			pro.setOutParas(proOuts);
		}
		return pro;
	}
	/**
	 * 
	 * @param proNode 参数加工过程的总节点
	 * @return 参数加工过程的Map，以加工过程名称为索引
	 */
	private Map parseParaProcesses(Element proNodes){
		if(proNodes==null){
			return null;
		}
		Map paraPros=null;
		//参数节点解析
		if(proNodes!=null&&proNodes.elementIterator("paraPro")!=null){
			paraPros=new HashMap();
			for(Iterator it=proNodes.elementIterator("paraPro");it.hasNext();){
				ParaProcess pro=new ParaProcess();
				Element pnode=(Element)it.next();
				pro.setName(pnode.attributeValue("name"));
				pro.setDesc(pnode.attributeValue("desc"));
				int proMode=0;
				try{
					String sMode=pnode.attributeValue("proMode")==null?"1":pnode.attributeValue("proMode");
					proMode=Integer.parseInt(sMode);
				}catch(Exception e){
					proMode=0;
				}
				pro.setProMode(proMode);
				if(proMode==1){//如果是sql语句取数，读取sql语句配置
					Element sqlNode=pnode.element("sql");
					if(sqlNode!=null){
						String sql=sqlNode.getText();
						pro.setSql(sql);
					}
				}else if(proMode==2){//如果是存储过程取数，读取存储过程配置
					Element proNode=pnode.element("procedure");
					if(proNode!=null){
						ProcedureBean procedure=parseProcedure(proNode);
						pro.setProcedure(procedure);
					}
				}else{
					Element clNode=pnode.element("class");
					if(clNode!=null){
						pro.setImplClass(clNode.attributeValue("path"));
					}
				}
				paraPros.put(pro.getName(), pro);
			}	
		}
		return paraPros;
	}
	//只解析报表简单的概要信息就返回
	public ReportBase parseTemplateSummary(String rptDesignInfo)throws ParseReportException{
		ReportBase rpt=null;
		if(rptDesignInfo==null||"".equals(rptDesignInfo))return null;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(rptDesignInfo.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return null;
		    rpt=new Report();
		    rpt.setId(root.attributeValue("id"));
		    rpt.setName(root.attributeValue("name"));
		    Element dscnode=root.element("description");
		    if(dscnode!=null){
		    	rpt.setDescription(dscnode.getText());
		    }
		    int multiUnit = 0;
		    try{
		    	multiUnit = Integer.parseInt(root.attributeValue("multiUnit"));
		    }catch(Exception e){}
		    rpt.setMultiUnit(multiUnit);
		    rpt.setDefaultUnit(root.attributeValue("defaultUnit"));
		    rpt.setSupportUnits(root.attributeValue("supportUnits"));
		    String sZeroHide=root.attributeValue("zeroCanHide"); 
		    int zeroCanHide = 0;
		    try{
		    	zeroCanHide=Integer.parseInt(sZeroHide);
		    }catch(Exception e){}
		    rpt.setZeroCanHide(zeroCanHide);
		    //2013-06-05 是否有图表
		    int hasChart = 0;
		    try{
		    	hasChart = Integer.parseInt(root.attributeValue("hasChart"));
		    }catch(Exception e){}
		    rpt.setHasChart(hasChart);
		    //2014-11-14 是否是直接导出到Excel
		    int directExport = 0;
		    try{
		    	directExport = Integer.parseInt(root.attributeValue("directExport"));
		    }catch(Exception e){}
		    rpt.setDirectExport(directExport);
		    
		}catch(Exception e){
			throw new ParseReportException(e.toString());
		}
		return rpt;
	}
}
