package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.CookieUtils;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtPro;
    /**
     * 给用户授权，返回token
     * @param username
     * @param password
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response){
        String token = this.authService.authentication(username, password);
        if (org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 在cookie中写入token, 将httpOnly设置为true 防止js读取和修改
        CookieUtils.setCookie(request, response, jwtPro.getCookieName(),
                token, jwtPro.getCookieMaxAge() * 60, null, true);//30min
        return ResponseEntity.ok().build();
    }

    /**
     * 验证cookie,如果cookie正确返回userinfo 否则返回401
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token, HttpServletRequest request, HttpServletResponse response){

       try{
           // 用「公钥」对token解密
           UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtPro.getPubKey());
           if (userInfo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
           // 要注意重置cookie！相当于用户有操作，因此要重新设置cookie
           // 刷新jwt中的有效时间：重新用私钥生成token
           String newToken = JwtUtils.generateToken(userInfo, jwtPro.getPriKey(), jwtPro.getExpire());

           // 刷新cookie中的有效时间： 重新保存到cookie中
           CookieUtils.setCookie(request, response, jwtPro.getCookieName(),
                   newToken, jwtPro.getCookieMaxAge() * 60);//jwtPro.getCookieMaxAge() * 60 为30min
           return ResponseEntity.ok(userInfo);

       }catch (Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }

    }
}
