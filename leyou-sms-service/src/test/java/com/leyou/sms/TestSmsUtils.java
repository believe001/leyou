package com.leyou.sms;

import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.leyou.sms.utils.SmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = LeyouSmsApplication.class)
@RunWith(SpringRunner.class)
public class TestSmsUtils {
    @Autowired
    private SmsUtils smsUtils;
    @Test
    public void testSms(){
        SendSmsResponse sendSmsResponse = this.smsUtils.sendSms("15528375761", "8888");
        System.out.println(sendSmsResponse);
    }
}
