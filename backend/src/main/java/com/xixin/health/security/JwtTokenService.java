package com.xixin.health.security;

import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.util.AuthContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * JWT令牌服务 - 生成和解析Token
 */
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /** 生成JWT令牌 */
    public String generateToken(Long accountId, Long userId, String username, String displayName, RoleType role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expireAt = new Date(now + jwtProperties.getExpireSeconds() * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(accountId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("displayName", displayName)
                .claim("role", role.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getJwtSecret())
                .compact();
    }

    /** 解析JWT令牌 */
    public AuthContext.LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getJwtSecret())
                .parseClaimsJws(token)
                .getBody();
        return new AuthContext.LoginUser(
                Long.valueOf(claims.getSubject()),
                claims.get("userId", Long.class),
                claims.get("username", String.class),
                claims.get("displayName", String.class),
                RoleType.valueOf(claims.get("role", String.class))
        );
    }
}
