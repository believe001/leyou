package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 在这里定义监听器，获取消息队列的消息，获
 * 取到了消息就自行对应的创建静态页面的逻辑
 */
@Component
public class GoodsListener {
    @Autowired
    private GoodsHtmlService goodsHtmlService;
    @Autowired
    private AmqpTemplate amqpTemplate;// rabbitmp模版
    /**
     * 定义一个方法，获取到了消息就创建静态页面
     *  绑定交换机,定义通配符都用注解实现
     */
    @RabbitListener( bindings = @QueueBinding(value = @Queue(value = "LEYOU.CREATE.WEB.QUEUE", durable = "true"),
                                              exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE", durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
                                              key = {"item.insert", "item.update"}))
    public void listenCreate(Long spuId) throws Exception{// 这里一旦抛出异常，rabbitMQ就会自动设置为手动确认ACK机制
        if(spuId == null) return;
        System.out.println("接受到了修改的id,修改页面成功！");
        this.goodsHtmlService.createHtml(spuId);
    }
}
