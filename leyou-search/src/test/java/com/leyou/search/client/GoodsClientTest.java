package com.leyou.search.client;

import com.leyou.LeyouSearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class GoodsClientTest {
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public void testSpu(){
//        PageResult<SpuBo> spuBoPageResult = this.goodsClient.querySpuBoByPage(null, true, 1, 5);
//        spuBoPageResult.getItems().forEach(spuBo -> System.out.println("内容"+spuBo.getBname()));

        List<Sku> skus = this.goodsClient.querySkusBySpuId(1l);
        System.out.println("内容" + skus.get(0).getTitle());
    }
}