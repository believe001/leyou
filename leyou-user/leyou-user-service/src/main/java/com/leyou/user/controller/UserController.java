package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据是否合法（电话号码和用户名是否已经存在）
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable("type") Integer type){
        Boolean boo = this.userService.checkData(data, type);
        if (boo == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(boo);
    }

    /**
     * 发送验证码并且将验证码保存到redis中
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone){
//        System.out.println("经过了");
        if(phone == null || StringUtils.isEmpty(phone)) return ResponseEntity.badRequest().build();

        Boolean boo = this.userService.sendVerifyCode(phone);
        if (boo == false) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    /**
     * 用户注册：先验证验证码然后保存用户信息
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code){
        Boolean boo = this.userService.register(user, code);
        if (boo == null || !boo) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username, @RequestParam("password") String password){
        User user = this.userService.query(username, password);
        if (user == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(user);
    }
}
