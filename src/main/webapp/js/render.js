Ext.ns('App.rpt');
App.rpt.HIDEZERO=false;
function renderLink(v,col,r){
	var sParams =col.linkParams;
	var pms = sParams?sParams.split(","):new Array();
	var tg = col.target?col.target:"_blank";
	var aStr = "<a target='"+tg+"' href='";
	var url = RPTROOT+"&rptID="+col.linkTo
	for(var i=0;i<pms.length;i++){
		var p = pms[i];
		if(r.get(p)){
			url+="&"+p+"="+r.get(p);
		}
	}
	url = encodeURI(url);
	aStr =aStr+url+"'>"+v+"</a>";
	return aStr;
}
App.rpt.Renders = {
		rMoney: function(v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			v = (Math.round((v - 0) * 100)) / 100;
			v = (v == Math.floor(v)) ? v + ".00" : ((v * 10 == Math.floor(v * 10)) ? v
					+ "0" : v);
			v = String(v);
			var ps = v.split('.');
			var whole = ps[0];
			var sub = ps[1] ? '.' + ps[1] : '.00';
			var r = /(\d+)(\d{3})/;
			while (r.test(whole)) {
				whole = whole.replace(r, '$1' + ',' + '$2');
			}
			v = whole + sub;
			p.attr = 'title=' + v;// 增加属性
			if (v.charAt(0) == '-') {
				v='-' + v.substr(1);
			}
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		//缩小1万倍（万元），保留2位小数
		rWan2Decimals: function (v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			v = v/10000;
			v = (Math.round((v - 0) * 100)) / 100;
			v = (v == Math.floor(v)) ? v + ".00" : ((v * 10 == Math.floor(v * 10)) ? v
					+ "0" : v);
			v = String(v);
			var ps = v.split('.');
			var whole = ps[0];
			var sub = ps[1] ? '.' + ps[1] : '.00';
			var r = /(\d+)(\d{3})/;
			while (r.test(whole)) {
				whole = whole.replace(r, '$1' + ',' + '$2');
			}
			v = whole + sub;
			p.attr = 'title=' + v;// 增加属性
			if (v.charAt(0) == '-') {
				v='-' + v.substr(1);
			}
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return  renderLink(v,col,record);
			}
			return v;
		},
		//万元，整数
		rWanInt: function (v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			var strV = String(v/10000);
			var ps = strV.split('.');
			v= ps[0];
			p.attr = 'title=' + ps[0];
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return  renderLink(ps[0],col,record);
			}
			return v;
		},
		//缩小1亿倍（亿元），保留2位小数
		rYi2Decimals: function (v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			v = v/100000000;
			v = (Math.round((v - 0) * 100)) / 100;
			v = (v == Math.floor(v)) ? v + ".00" : ((v * 10 == Math.floor(v * 10)) ? v
					+ "0" : v);
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		//亿元，整数
		rYiInt: function (v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			var strV = String(v/100000000);
			var ps = strV.split('.');
			v= ps[0];
			p.attr = 'title=' + ps[0];
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return  renderLink(ps[0],col,record);
			}
			return v;
		},
		//最多保留6位小数
		rMoney6: function (v,p,r,rindex,cindex){
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			v = v/10000;
			v = (Math.round((v - 0) * 1000000)) / 1000000;
			//v = (v == Math.floor(v)) ? v + ".00" : ((v * 10 == Math.floor(v * 10)) ? v + "0" : v);
			v = String(v);
			var ps = v.split('.');
			var whole = ps[0];
			var sub = ps[1] ? '.' + ps[1] : '';
			var r = /(\d+)(\d{3})/;
			while (r.test(whole)) {
				whole = whole.replace(r, '$1' + ',' + '$2');
			}
			v = whole + sub;
			if (v.charAt(0) == '-') {
				v= '-' + v.substr(1);
			}
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		rDate: function (value, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&value==0){
				return "";
			}
			if(value==""){
				return "";
			}
			var dt = new Date(value);
			var v = dt.format('Y-m-d');    
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v
		},
		r2Decimal: function(value, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&value==0){
				return "";
			}
			p.attr = 'title=' + value;
			if (parseFloat(value) == value){
				v= Math.round(value * 100) / 100;
			}else{
				v= 0;
			}
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		renderFoo: function(v, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&v==0){
				return "";
			}
			p.attr = 'title=' + v;
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		renderInt :function(value, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&value==0){
				return "";
			}
			var v = String(value);
			var ps = v.split('.');
			var v = ps[0];
			p.attr = 'title=' + v;
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(v,col,record);
			}
			return v;
		},
		hideZero: function(value, p, record,rindex,cindex) {
			var cols ;
			var st = record.store;
			if(st.grid){
				cols = st.grid.getColumnModel().config;
			}else{
				cols = REPORTGRID.getColumnModel().config;
			}
			if(App.rpt.HIDEZERO&&cols[cindex].hideZero&&cols[cindex].hideZero>0&&value==0){
				return "";
			}
			p.attr = 'title=' + value;
			if(cols[cindex]&&cols[cindex].isLink>0){
				var col = cols[cindex];
				return renderLink(value,col,record);
			}
			return value;
		},
		searchMap: function (v,p,r){
			var aStr = "<a target='_blank' href='";
			var url="http://map.baidu.com/?newmap=1&ie=utf-8&s=s%26wd%3D";
			var addr = encodeURI(v);
			aStr =aStr+url+addr+"'>"+v+"</a>";
			return aStr;
		}
};