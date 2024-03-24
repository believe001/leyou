package com.leyou.gateway.filter;

import com.leyou.auth.utils.CookieUtils;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
@Component
public class LoginFilter extends ZuulFilter{
    @Autowired
    private JwtProperties jwtPro;
    @Autowired
    private FilterProperties filterPro;
    @Override
    public String filterType() {
        return "pre"; // 在路由之前执行，还有route,post,error
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        // 如果是白名单里面的就放行
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String requestURI = request.getRequestURI();
        for (String path : filterPro.getAllowPaths()){
            if (requestURI.startsWith(path)){
                return false;// 放行：白名单里面的都会被放行（因为他们是不需要token也可以访问的服务）
            }
        }
//        System.out.println("拦截了。。。");
        return true;
    }

    @Override//过滤器里面的具体逻辑
    public Object run() throws ZuulException {
        // 验证cookie如果不行的话返回
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        String token = CookieUtils.getCookieValue(request, jwtPro.getCookieName());
        try {
            JwtUtils.getInfoFromToken(token, jwtPro.getPublicKey());
            // 什么都不用做，可以路由
        } catch (Exception e) {
            // token验证失败
            context.setSendZuulResponse(false); // 不进行路由
            context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
            e.printStackTrace();
        }
        return null;
    }
}
