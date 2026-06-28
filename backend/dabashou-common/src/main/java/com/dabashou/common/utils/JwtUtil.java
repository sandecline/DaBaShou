package com.dabashou.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT工具类 — 生成/解析含roles claim的token
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 生成访问token
     *
     * @param userId       用户ID
     * @param roles        角色列表(如["USER","ADMIN"])
     * @param secret       密钥(≥32字节)
     * @param expirationMs 过期时间(毫秒)
     */
    public static String generateToken(Long userId, List<String> roles, String secret, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * 生成刷新token(不含roles，过期时间更长)
     */
    public static String generateRefreshToken(Long userId, String secret, long refreshExpirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("refresh", true)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * 解析token，返回Claims；token无效时抛异常
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Claims提取userId
     */
    public static Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    /**
     * 从Claims提取roles列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /**
     * 判断是否为刷新token
     */
    public static boolean isRefreshToken(Claims claims) {
        return Boolean.TRUE.equals(claims.get("refresh", Boolean.class));
    }
}
