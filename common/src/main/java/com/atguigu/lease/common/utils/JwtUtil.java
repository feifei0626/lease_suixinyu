package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;


public class JwtUtil {
    private static long tokenExpiration = 60 * 60 * 1000L;
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("g1JhPGby5FKpi4autOONuahEvKzw4U5N".getBytes());

    //生成Token
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder().
                setSubject("LOGIN_USER").
                setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).//设置Token过期时长
                claim("userId", userId).
                claim("username", username).
                signWith(tokenSignKey, SignatureAlgorithm.HS256).
                compact();
        return token;
    }

    //解析Token
    public static Claims parseToken(String token){

        if (token==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            return jwtParser.parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    //生成测试用的Token
    public static void main(String[] args) {
        System.out.println(createToken(8L,"13711036706"));
    }

}
