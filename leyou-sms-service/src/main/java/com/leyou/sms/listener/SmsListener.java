package com.leyou.sms.listener;

import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.leyou.sms.utils.SmsUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;
    @RabbitListener
        (bindings =
                    @QueueBinding(
                                    value = @Queue(value = "leyou.sms.queue",durable = "true"),
                                    exchange = @Exchange(value = "leyou.sms.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
                                    key = "sms.verify.code"
                                )
        )
    public void listenSms(Map<String, String> msg){ // 接受到的值自动放到map中去。传送的值是电话号码+code
        if (msg == null || CollectionUtils.isEmpty(msg)){
            return;
        }

        String phone = msg.get("phone");
        String code = msg.get("code");
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            return; // 放弃处理
        }
        SendSmsResponse sendSmsResponse = smsUtils.sendSms(phone, code);

    }
}
