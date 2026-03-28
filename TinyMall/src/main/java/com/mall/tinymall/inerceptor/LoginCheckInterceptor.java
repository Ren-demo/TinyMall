package com.mall.tinymall.inerceptor;

import com.mall.tinymall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

/**
 * @date 2025-05-09 13:38
 * @description TODO: 拦截器
 */
@Component
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求uri
        String uri = request.getRequestURI();
        log.info("uri: {}", uri);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        //获取请求头中的令牌（token）
        String token = request.getHeader("token");
        //判断令牌是否存在
        if(token == null){  //不存在返回401且不放行
            log.warn("令牌为空");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        //解析令牌
        try {
            Claims claims = JwtUtils.parseJWT(token);
            log.info("claims = {}", claims);
            if(new Date(System.currentTimeMillis()).getTime()-claims.getExpiration().getTime() <= 2*3600*1000
                && new Date(System.currentTimeMillis()).getTime()-claims.getExpiration().getTime() >= 0){
                response.setHeader("token", JwtUtils.generateToken(claims));
            }
        } catch (Exception e) {
            log.error("令牌解析失败");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        //无异常则放行
        return true;
    }
}
     