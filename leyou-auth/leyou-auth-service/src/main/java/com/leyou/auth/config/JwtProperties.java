package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "leyou.jwt")
public class JwtProperties {
    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private PublicKey pubKey;
    private PrivateKey priKey;
    private Integer expire;
    private String cookieName;
    private Integer cookieMaxAge;
    private static final Logger logger = LoggerFactory.getLogger(JwtProperties.class);
    @PostConstruct// 构造函数之后执行
    public void init(){
        try{
            File pubKeyFile = new File(pubKeyPath);
            File priKeyFile = new File(priKeyPath);
            if (!pubKeyFile.exists() || !priKeyFile.exists()){
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            this.pubKey = RsaUtils.getPublicKey(pubKeyPath);
            this.priKey = RsaUtils.getPrivateKey(priKeyPath);
        }catch (Exception e){
            logger.error("创建密钥失败！");
            throw new RuntimeException();
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPubKeyPath() {
        return pubKeyPath;
    }

    public void setPubKeyPath(String pubKeyPath) {
        this.pubKeyPath = pubKeyPath;
    }

    public String getPriKeyPath() {
        return priKeyPath;
    }

    public void setPriKeyPath(String priKeyPath) {
        this.priKeyPath = priKeyPath;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public PrivateKey getPriKey() {
        return priKey;
    }

    public void setPriKey(PrivateKey priKey) {
        this.priKey = priKey;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public Integer getCookieMaxAge() {
        return cookieMaxAge;
    }

    public void setCookieMaxAge(Integer cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }
}
