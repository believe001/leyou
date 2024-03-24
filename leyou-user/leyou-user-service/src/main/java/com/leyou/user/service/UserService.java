package com.leyou.user.service;

import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 检验data数据是否可用。type为1 用户名、type为2 手机号
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                return null;
        }

        return 0 == this.userMapper.selectCount(user);

    }

    /**
     * 发送验证码并且将验证码保存到redis中
     * @param phone
     */
    static final String KEY_PREFIX = "user:code:phone:";
    public Boolean sendVerifyCode(String phone) {
        final int LEN = 6;//验证码的长度
        String code = NumberUtils.generateCode(LEN);
        try {
            // 保存到redis中
            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 10, TimeUnit.MINUTES);
            // 向消息队列发送phone 和 code hashMap结构
            HashMap<String, String> msg = new HashMap<>();
            msg.put("phone", phone);
            msg.put("code", code);
            this.amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.verify.code", msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    public Boolean register(User user, String code) {
        // 校验验证码
        String phone = user.getPhone();
        String serverCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + phone);
        if (!code.equals(serverCode)) return false;
        // 保存用户信息
        // 密码加密
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        String md5HexPwd = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(md5HexPwd);
        // 强制id为null!!!
        user.setId(null);
        user.setCreated(new Date());
        boolean boo = this.userMapper.insertSelective(user) == 1;

        // 注册成功要删除redis中的记录
        if (boo){
            this.redisTemplate.delete(KEY_PREFIX + phone);
        }
        return boo;
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    public User query(String username, String password) {

        User record = new User();
        record.setUsername(username);
        // 校验姓名
        User user = this.userMapper.selectOne(record);
        if (user == null) return null;
        // 校验密码
        if(!CodecUtils.md5Hex(password, user.getSalt()).equals(user.getPassword())){
            return null;
        }
        return user;
    }
}
