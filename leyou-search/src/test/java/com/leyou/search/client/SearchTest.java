package com.leyou.search.client;

import com.leyou.LeyouSearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.GoodsRepository;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class SearchTest {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository goodsRepository;



    @Autowired
    private SearchService searchService;

    @Test
    public void test(){
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);
        Integer page = 1;
        Integer rows = 100;
        do {
            PageResult<SpuBo> spuBoPageResult = this.goodsClient.querySpuBoByPage(null, true, page, rows);
            List<SpuBo> items = spuBoPageResult.getItems();
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            // 导入到es中
            this.goodsRepository.saveAll(goodsList);

            ++page;
            rows = items.size();
        }while (rows == 100);


    }
}
