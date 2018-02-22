package cn.et.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.ognl.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import cn.et.entity.Cookbook;

public class SolrUtil {
	private static String urlString = "http://localhost:8080/solr/core2";
	private static SolrClient solr;
	static{
		//初始化solr客户端并指定core
		solr = new HttpSolrClient(urlString);
	}
	/**
	 * 将数据写入索引库
	 * 
	 * @param sid
	 *            一条数据
	 * @throws IOException
	 */
	public static void write(SolrInputDocument sid) throws SolrServerException, IOException{   
        solr.add(sid);
        solr.commit();
    }

	/**
	 * 搜索 并高亮显示搜索内容
	 * 
	 * @return
	 * @throws SolrServerException 
	 * @throws InvalidTokenOffsetsException
	 */
	public static List<Cookbook> search(String field, String value)
			throws IOException, ParseException, SolrServerException {
		List<Cookbook> list = new ArrayList<Cookbook>();
		//新建搜索器  并定义搜索条件
		SolrQuery sq = new SolrQuery(field + ":" + value);
		/**
		 * sq.setQuery("a_ik:炒蛋");
		 * 过滤查询   与普通查询区别在于过滤查询不会根据得分排序
		 * sq.setFilterQueries("a_ik:炒蛋");
		 */
		/**
		 * 分页   开始下标
		 * sq.setStart(start);
		 * 查询的行数
		 * sq.setRows(rows);
		 */
		//是否开启高亮
        sq.setHighlight(true);
        //添加需要高亮的字段
        sq.addHighlightField(field);//sq.set("hl.fl", field);
        //前缀
        sq.setHighlightSimplePre("<font color=red>");
        //后缀
        sq.setHighlightSimplePost("</font>");
		//定义结果集排序规则
		//sq.setSort("id", ORDER.asc);
		//搜索响应体
		QueryResponse query = solr.query(sq);
		//获取高亮结果
		Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
		//获取结果   query.getBeans(Cookbook.class);//返回实体类集合
		List<SolrDocument> sdList =  query.getResults();//返回document集合
		for (SolrDocument sd : sdList) {
			String id = sd.get("id").toString();
			//获取高亮结果
			Map<String, List<String>> map = highlighting.get(id);
			List<String> dishes = map.get("dishes_ik");
			String highDishes = dishes.get(0);
			Cookbook cookbook = new Cookbook();
			cookbook.setId(id);
			cookbook.setDishes(highDishes);
			cookbook.setIllustrate(sd.get("illustrate_s").toString());
			cookbook.setType(sd.get("type_s").toString());
			list.add(cookbook);
		}
		return list;
	}
}
