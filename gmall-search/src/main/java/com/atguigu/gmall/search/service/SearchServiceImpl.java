package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.*;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date:2021/7/3
 * Author：ZHOU_World
 * Description:构建DSL语句
 */
@Service
public class SearchServiceImpl {
    @Autowired //原生restHighLevelClient
    private RestHighLevelClient restHighLevelClient;
    //查询结果集
    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            /*//初始化请求对象
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));
            //结果集搜索方法
            this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);*/
            //合并成以下写法
            SearchResponse response = this.restHighLevelClient.search(new SearchRequest(new String[]{"goods"},
                            buildDsl(paramVo)),
                            RequestOptions.DEFAULT);
            System.out.println("得出的dsl解析后的结果集"+response);
            //TODO:解析结果集
            SearchResponseVo responseVo = this.parseResult(response);
            //获取页码和当前显示数,分页参数在请求参数中
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());

            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //封装解析结果集方法
    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //获取hits结果集
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits());//总记录数
        SearchHit[] hitsHits = hits.getHits();
        //遍历hitshits中的元素，将它转化为List<Goods>
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit->{
            String json = hitsHit.getSourceAsString();//获取_source
            Goods goods = JSON.parseObject(json, Goods.class);//反序列化为goods对象
            System.out.println("2222222");
            //获取高亮结果集
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            //覆盖普通标题
            goods.setTitle(highlightField.fragments()[0].string());
            return goods;

        }).collect(Collectors.toList());
        System.out.println("333333333333333333333"+goodsList);
        responseVo.setGoodsList(goodsList);
        //获取集合结果集
        Aggregations aggregations = response.getAggregations();
        //1、获取品牌brandIdAgg聚合
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregations.get("brandIdAgg");//转为可解析的
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();//品牌id聚合结果集中的桶
        if(!CollectionUtils.isEmpty(brandBuckets)){
            //将桶转为为list<BrandEntities>
            List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket ->{
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());//桶中的key就是品牌id
                //获取品牌名称的子集合
                Aggregations subBrandAggs = ((Terms.Bucket) bucket).getAggregations();
                //获取子聚合中的品牌名称
                ParsedStringTerms brandNameAgg = (ParsedStringTerms)subBrandAggs.get("brandNameAgg");
                //获取子聚合brandNameAgg中的桶
                List<? extends Terms.Bucket> buckets = brandNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(buckets)){
                    //获得桶里第一个元素（key）设置给brandEntity的名字
                    brandEntity.setName(buckets.get(0).getKeyAsString());
                }
                //获取品牌id聚合中的logo子聚合
                ParsedStringTerms logoAgg = (ParsedStringTerms)subBrandAggs.get("logoAgg");
                //获取子聚合logoAgg中的桶
                List<? extends Terms.Bucket> logoBuckets = logoAgg.getBuckets();
                if(!CollectionUtils.isEmpty(logoBuckets)){
                    brandEntity.setLogo(logoBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brandEntities);
        }
        //2、获取分类categoryAgg聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregations.get("categoryIdAgg");//转为可解析的
        //获取categoryAgg中的桶
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        //将桶集合转换为分类集合
        if(!CollectionUtils.isEmpty(categoryBuckets)){
            List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                //分类id就是categoryAgg中桶的key值
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取分类聚合中的categoryNameAgg子聚合
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms)((Terms.Bucket) bucket)
                                                    .getAggregations().get("categoryNameAgg");
                //获取子聚合categoryNameAgg中的桶
                List<? extends Terms.Bucket> buckets = categoryNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(buckets)){
                    categoryEntity.setName(buckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntities);
        }
        //3、获取规格参数的嵌套聚合，解析
        ParsedNested attrAgg = (ParsedNested)aggregations.get("attrAgg");
        //获取嵌套聚合中的规格参数id的子聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //获取子聚合attrIdAgg中的桶
        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        //把集合attrIdBuckets转换为SearchResponseAttrValueVo集合
        if(!CollectionUtils.isEmpty(attrIdBuckets)){
            List<SearchResponseAttrValueVo> searchResponseAttrValueVos =attrIdBuckets.stream().map(bucket->{
                SearchResponseAttrValueVo attrValueVo = new SearchResponseAttrValueVo();
                attrValueVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取attrId聚合中的子聚合attrNameAgg
                Aggregations aggs = ((Terms.Bucket) bucket).getAggregations();
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)aggs.get("attrNameAgg");
                //获取attrNameAgg中的桶
                List<? extends Terms.Bucket> buckets = attrNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(buckets)){
                    attrValueVo.setAttrName(buckets.get(0).getKeyAsString());
                }
                //获取attrId聚合中的子聚合attrValueAgg
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)aggs.get("attrValueAgg");
                //获取attrValueAgg中的桶
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(valueBuckets)){
                    //将桶集合转换成为字符串集合
                    List<String> attrValues = valueBuckets.stream().map(Terms.Bucket::getKeyAsString)
                                                                    .collect(Collectors.toList());
                    attrValueVo.setAttrValues(attrValues);
                }
                return attrValueVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseAttrValueVos);
        }
        return responseVo;
    }
    //搜寻条件构建器,创建DSL语句
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo){
        //初始化搜寻条件构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = paramVo.getKeyword();
        //判空
        if(StringUtils.isBlank(keyword)){
            //TODO:打广告
            return sourceBuilder;
        }
        //1、构建查询及过滤
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //通过bool查询构建匹配查询和范围查询
        sourceBuilder.query(boolQueryBuilder);
        //1.1匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        //1.2过滤
                //1.2.1品牌过滤
        List<Long> brandId = paramVo.getBrandId();//获取品牌id(品牌过滤条件)
        if(!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));

        }
                //1.2.2分类过滤
        List<Long> categoryId = paramVo.getCategoryId();//获取分类id(分类过滤条件)
        if(!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }
                //1.2.3价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if(priceFrom!=null || priceTo !=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);
            if(priceFrom!=null){
                rangeQuery.gte(priceFrom);
            }
            if(priceTo!=null){
                rangeQuery.lte(priceTo);
            }
        }
                //1.2.4仅显示有货过滤
        Boolean store = paramVo.getStore();
        if(store!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
                //1.2.5规格参数过滤
        List<String> props = paramVo.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.forEach(prop -> {//4:8G-12G
                //对规格参数（4:8G-12G）进行字符串截取，获得attrId(4)和attrValue(8G-12G)
                String[] attr = StringUtils.split(prop, ":");
                if(attr!=null && attr.length==2){//对规格参数进行判断，数组长度为2且不为空时进行处理
                    //每个规格中含有一个嵌套查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery, ScoreMode.None));

                    //bool查询中含有两个查询，之间是must关系
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attr[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",StringUtils.split(attr[1],"-")));
                }
            });
        }
        //2、构建排序
        Integer sort = paramVo.getSort();
        //排序：0-默认排序 1-价格降序 2-价格升序 3-销量的降序 4-新品降序
        switch(sort){
            case 0 : sourceBuilder.sort("_score", SortOrder.DESC);break;
            case 1 : sourceBuilder.sort("price", SortOrder.DESC);break;
            case 2 : sourceBuilder.sort("price", SortOrder.ASC);break;
            case 3 : sourceBuilder.sort("sales", SortOrder.DESC);break;
            case 4 : sourceBuilder.sort("createTime", SortOrder.DESC);break;
            default:
                throw new RuntimeException("搜索条件不合法");
        }
        //3、分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        //4、高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title")
                                    .preTags("<font style='color:red;'>")
                                    .postTags("</font>"));
        //5、聚合
            //5.1品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
            //5.2分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
            //5.3规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );
        //6、结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","price","defaultImage"},null);
        System.out.println("DSL查询语句："+sourceBuilder);
        return sourceBuilder;
    }
}
