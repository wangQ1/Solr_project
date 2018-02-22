package cn.et.controller;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.et.entity.Cookbook;
import cn.et.mapper.CookbookMapper;
import cn.et.util.SolrUtil;
@RestController//简化代码  该注解中继承了Controller 与  RequestBody注解
public class Controller {
	@Autowired
	private CookbookMapper am;
	/**
	 * 数据库批量查询数据并写入索引库
	 * @return 
	 * @throws SolrServerException 
	 */
	@RequestMapping("/ci")
	public String createIndex() throws SolrServerException{
		try {
			//总记录数
			int count = am.queryCount();
			//开始下标
			int startIndex = 0;
			//每次查询的记录数量
			int length = 1000;
			while(startIndex <= count){
				List<Cookbook> cbList = am.queryAll(startIndex, length);
				startIndex += count;
				for(int i = 0; i < cbList.size(); i++){
					Cookbook cb = cbList.get(i);
					SolrInputDocument sid = new SolrInputDocument();
					sid.addField("id", cb.getId());
					sid.addField("dishes_ik", cb.getDishes());
					sid.addField("illustrate_s", cb.getIllustrate());
					sid.addField("type_s", cb.getType());
					SolrUtil.write(sid);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
		return "success";
	}
	/**
	 * 搜索
	 * @param keyword 搜索关键字
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@GetMapping("/seek")
	public List<Cookbook> search(String keyword) throws Exception{
		return SolrUtil.search("dishes_ik", keyword);
	}
}
