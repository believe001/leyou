package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 定义一个监听器获取消息队列的内容，并且根据内容对es数据进行修改
 */
@Component
public class GoodsListener {
    @Autowired
    private SearchService searchService;
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "LEYOU.CREATE.INDEX.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}))
    public void listenCreate(Long spuId) throws IOException {
       if (spuId == null) return;

       this.searchService.createIndex(spuId);


    }
}
