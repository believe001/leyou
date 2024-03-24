package com.leyou.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云发送消息id和secret的配置
 */
@ConfigurationProperties(prefix = "leyou.sms")
public class SmsProperties {
    String accessKeyId;

    String accessKeySecret;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
}
