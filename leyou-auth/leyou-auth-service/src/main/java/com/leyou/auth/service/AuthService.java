package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties pro;
    /**
     * 给用户授权，返回token
     * @param username
     * @param password
     * @return
     */
    public String authentication(String username, String password) {
        try {
            // 查询用户，看用户是否存在
            User user = this.userClient.queryUser(username, password);
            if (user == null){
                return null;
            }
            UserInfo userInfo = new UserInfo();
            BeanUtils.copyProperties(user, userInfo);
            // 创建token(用「私钥」给用户信息加密)
            String token = JwtUtils.generateToken(userInfo, pro.getPriKey(), pro.getExpire());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
