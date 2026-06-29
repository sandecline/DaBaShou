package com.dabashou.api.filter;

import com.dabashou.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT认证过滤器
 * 从Authorization头解析Bearer token，提取userId和roles构造认证对象
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BLACKLIST_PREFIX = "dbs:blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    @Value("${dabashou.jwt.header:Authorization}")
    private String header;

    @Value("${dabashou.jwt.prefix:Bearer }")
    private String prefix;

    public JwtAuthFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(header);
        if (authHeader != null && authHeader.startsWith(prefix)) {
            String token = authHeader.substring(prefix.length());
            try {
                // 检查token是否在黑名单中
                if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
                    log.debug("Token已被注销(黑名单)");
                    filterChain.doFilter(request, response);
                    return;
                }
                Claims claims = JwtUtil.parseToken(token, jwtSecret);
                Long userId = JwtUtil.getUserId(claims);
                List<String> roles = JwtUtil.getRoles(claims);

                if (userId != null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.debug("JWT已过期: {}", e.getMessage());
            } catch (io.jsonwebtoken.JwtException e) {
                log.debug("JWT无效: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("JWT解析异常: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
