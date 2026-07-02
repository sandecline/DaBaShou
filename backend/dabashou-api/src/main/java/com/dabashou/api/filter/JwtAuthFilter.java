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
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 * 从Authorization头解析Bearer token，提取userId和roles构造认证对象
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    @Value("${dabashou.jwt.header:Authorization}")
    private String header;

    @Value("${dabashou.jwt.prefix:Bearer }")
    private String prefix;

    private final JdbcTemplate jdbcTemplate;

    public JwtAuthFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(header);
        if (authHeader != null && authHeader.startsWith(prefix)) {
            String token = authHeader.substring(prefix.length());
            try {
                Claims claims = JwtUtil.parseToken(token, jwtSecret);
                Long userId = JwtUtil.getUserId(claims);
                List<String> roles = JwtUtil.getRoles(claims);
                Integer tokenVersion = JwtUtil.getTokenVersion(claims);

                if (userId != null && isTokenActive(userId, tokenVersion)) {
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

    private boolean isTokenActive(Long userId, Integer tokenVersion) {
        try {
            Map<String, Object> user = jdbcTemplate.queryForMap("SELECT status FROM dbs_user WHERE id = ?", userId);
            Object statusValue = user.get("status");
            int status = statusValue instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(statusValue));
            int currentVersion = loadTokenVersion(userId);
            return status == 1 && currentVersion == (tokenVersion == null ? 0 : tokenVersion);
        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            log.warn("JWT用户状态校验异常: {}", e.getMessage());
            return false;
        }
    }

    private int loadTokenVersion(Long userId) {
        try {
            Integer version = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(token_version, 0) FROM dbs_user WHERE id = ?",
                    Integer.class,
                    userId);
            return version == null ? 0 : version;
        } catch (DataAccessException e) {
            log.warn("token_version字段不可用，按兼容模式校验Token: {}", e.getMessage());
            return 0;
        }
    }
}
