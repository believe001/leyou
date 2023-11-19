package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 根据spuid 返回 spu,categories, brand 等组成的key -value 结构
     * @param id
     * @return
     */
    public Map<String, Object> loadData(Long id) {
        Map<String, Object> object = new HashMap<>();
        // 查询spu
        Spu spu = this.goodsClient.querySpuById(id);
        // 查询categories
        List<Long> categoryIds = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> categoryNames = this.categoryClient.queryNamesByIds(categoryIds);
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < categoryIds.size(); i++) {
            HashMap<String, Object> cidToName = new HashMap<>();
            cidToName.put("id", categoryIds.get(i));
            cidToName.put("name", categoryNames.get(i));
            categories.add(cidToName);
        }
        // 查询brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        // 查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        // 查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        // 查询speGroups
        List<SpecGroup> specGroups = this.specificationClient.queryGroupsWithParams(spu.getCid3());
        // 查询paramMap
        Map<Long, String> pidToName = new HashMap<>();
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), false, null);
        for (SpecParam param : params) {
            pidToName.put(param.getId(), param.getName());
        }


        object.put("spu", spu);
        object.put("categories", categories);
        object.put("brand", brand);
        object.put("skus", skus);
        object.put("spuDetail", spuDetail);
        object.put("specGroups", specGroups);
        object.put("paramMap", pidToName);

        return object;
    }
}
