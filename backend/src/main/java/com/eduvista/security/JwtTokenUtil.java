package com.eduvista.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
// 注意：0.12.x 推荐不再直接引用 SignatureAlgorithm 这种枚举，它会自动根据 Key 的长度推断算法
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        // 确保你的 jwt.secret 在配置文件中长度足够（HS512要求至少64字节/512位）
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // --- 核心修改点 1: 解析器 ---
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()                 // 1. parserBuilder() 换成 parser()
            .verifyWith(getSigningKey())     // 2. setSigningKey() 换成 verifyWith()
            .build()                         // 3. 构建
            .parseSignedClaims(token)        // 4. parseClaimsJws() 换成 parseSignedClaims()
            .getPayload();                   // 5. getBody() 换成 getPayload()
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    // --- 核心修改点 2: 生成器 ---
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .claims(claims)                  // setClaims() 换成 claims()
            .subject(subject)                // setSubject() 换成 subject()
            .issuedAt(new Date(System.currentTimeMillis())) // setIssuedAt() 换成 issuedAt()
            .expiration(new Date(System.currentTimeMillis() + expiration)) // setExpiration 换成 expiration
            .signWith(getSigningKey())       // signWith(Key, Algorithm) 换成 signWith(Key)，会自动匹配算法
            .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}