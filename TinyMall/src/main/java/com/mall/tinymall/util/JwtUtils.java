package com.mall.tinymall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
* @date 2025-05-09 1:43
* @description TODO: Jwt工具类
*/
@Slf4j
public class JwtUtils {

    private static final SecretKey SECRET_KEY =
            Keys.secretKeyFor(SignatureAlgorithm.HS256); // 自动生成256位密钥

    //生成令牌
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)  // 传入有效载荷
                .expiration(new Date(System.currentTimeMillis() + 12 * 3600 * 1000))
                .signWith(SECRET_KEY) // 直接传入密钥
                .compact();
    }

    //解析令牌，获取有效载荷
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }


}
     