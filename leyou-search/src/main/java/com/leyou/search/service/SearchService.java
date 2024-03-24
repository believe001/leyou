package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.GoodsRepository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 根据封装的request对象（本质是key）搜索es中all字段数据，并分页
     * @param request
     * @return
     */
    public SearchResult search(SearchRequest request) {
        // 自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件
        BoolQueryBuilder boolQueryBuilder = buildBooleanQueryBuilder(request);
//        QueryBuilder basicQuery = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        queryBuilder.withQuery(boolQueryBuilder);
        // 添加分页,注意es中page第一页为0
        queryBuilder.withPageable(PageRequest.of(request.getPage() -1 , request.getDefaultSize()));
        // 添加结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        // 因为要对搜索结果对cid（分类id）和bid(品牌)分别进行聚合，因此补充
        String categoryAggName = "categories";//定义聚合名称
        String brandAggName = "brands";
        // 添加聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 执行搜索
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());
        List<Map<String, Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        // 当种类只有一种时，才会聚合规格参数
        List<Map<String, Object>> specs = new ArrayList<>();
        if(!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            specs = getParamAggResult((Long)categories.get(0).get("id"), boolQueryBuilder); // 将分类的id传入进行查询所有的参数，然后进行聚合(还需要查询的条件)
        }
        List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));

        return new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent(),categories, brands, specs);

    }

    /**
     * 根据请求的参数进行构建bool查询构建器,这里是为了聚合后的参数也就是过滤参数进行过滤查询
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 添加基本查询条件matchQuery会对搜索词分词,.operator(Operator.AND)表示必须包含所有的分词结果。must表示必须满足括号中的条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));

        // 添加过滤查询条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = null;
            // 如果key为分类，用cid3去查
            if(StringUtils.equals(entry.getKey(), "分类")){
                key = "cid3";
            // 如果key是品牌，用brandId去查
            }else if(StringUtils.equals(entry.getKey(), "品牌")){
                key = "brandId";
            }else{
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs." + entry.getKey() + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));// termQuery不会对搜索词分词
        }
        return boolQueryBuilder;
    }

    /**
     * 一旦商品分类确定了，就可以根据商品分类查询出哪些参数。
     * 根据分类id和查询条件进行查询，并根据spec进行分别聚合，返回聚合后的列表
     * @param id
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long id, QueryBuilder basicQuery) {
        // 创建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件
        queryBuilder.withQuery(basicQuery);
        // 查询要聚合的参数
        List<SpecParam> params = this.specificationClient.queryParams(null, id, null, true);
        // 添加聚合
        params.forEach(param->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));// 注意参数名没有分词，需要加keyword
        });

        // 执行查询
        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());
        Map<String, Aggregation> stringAggregationMap = aggregatedPage.getAggregations().asMap();
        // 遍历所有的聚合，对返回值进行赋值
        List<Map<String, Object>> specs = new ArrayList<>();
        for(Map.Entry<String, Aggregation> entry : stringAggregationMap.entrySet()){
            Map<String, Object> spec = new HashMap<>();
            spec.put("k", entry.getKey());

            StringTerms term = (StringTerms)entry.getValue();
            List<Object> options = new ArrayList<>();
            List<StringTerms.Bucket> buckets = term.getBuckets();
            buckets.forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            spec.put("options", options);
            specs.add(spec);
        }
        return specs;
    }

    /**
     * 根据pid聚合结果查询品牌列表
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        List<Brand> brands = new ArrayList<>();
        LongTerms terms = (LongTerms)aggregation;
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            Brand brand = this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });

        return brands;
    }

    /**
     * 根据cid聚合查询品牌{id:null, name:null}列表
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        List<Map<String, Object>> categories = new ArrayList<>();
        LongTerms term = (LongTerms)aggregation;
        List<LongTerms.Bucket> buckets = term.getBuckets();
        List<Long> cids = new ArrayList<>();
        buckets.forEach(bucket -> {
            long cid = bucket.getKeyAsNumber().longValue();
            cids.add(cid);
        });
        List<String> cnames = this.categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", cnames.get(i));
            categories.add(map);
        }
        return categories;

    }

    /**
     * 将spu转换为Goods
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
        // 将共有的字段赋值给goods：id,brandId,cid, subTitle,createTime

        BeanUtils.copyProperties(spu, goods);

        // 根据品牌id查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        // 根据cid查询分类名称
        List<String> cnames = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // title + 品牌名称 + 分类名称
        goods.setAll(spu.getTitle() + " " + brand.getName() + " " + StringUtils.join(cnames, " "));

        // 根据id查询所有sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        // 初始化价格集合
        List<Long> prices = new ArrayList<>();
        skus.forEach(sku -> prices.add(sku.getPrice()));
        goods.setPrice(prices);

        // 只需要将sku部分值保存到es，因此用map重新构造数据
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku ->{
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("images", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            map.put("price", sku.getPrice());
            skuMapList.add(map);
        });
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));


        // 根据spuid查询参数的值，查spudetail表。为了拿到参数对应的value
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        HashMap<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {});
        HashMap<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {});

        List<SpecParam> specParamsList = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        Map<String, Object> specParamsMap = new HashMap<>();
        specParamsList.forEach(specParam -> {
            // 通用的规格参数值从genericSpecMap中取
            if(specParam.getGeneric()){
                String value = genericSpecMap.get(specParam.getId()).toString();
                // 判断是否是数字类型
                if(specParam.getNumeric()){
                    // 如果是数字类型，返回范围
                    value = chooseSegment(value, specParam);
                }
                specParamsMap.put(specParam.getName(), value);

            }else{// 特殊的规格参数值从specialSpecMap中取
                List<Object> value = specialSpecMap.get(specParam.getId());

                specParamsMap.put(specParam.getName(), value);
            }


        });
        goods.setSpecs(specParamsMap);
        return goods;
    }

    /**
     * 将数字类型的参数值转化到参数的划分范围中去
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 根据spuId查询数据库封装Goods对象，并保存到es中
     * @param spuId
     */
    public void createIndex(Long spuId) throws IOException {
        Spu spu = this.goodsClient.querySpuById(spuId);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }
}
