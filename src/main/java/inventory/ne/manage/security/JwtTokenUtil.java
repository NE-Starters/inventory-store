package inventory.ne.manage.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import inventory.ne.manage.model.User;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import javax.crypto.SecretKey;

import java.util.Date;
import java.util.UUID;

@Component
@NoArgsConstructor
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms:900000}") // 15 minutes
    private int accessTokenExpirationMs;

    private Cache<String, Boolean> tokenBlacklist;

    public JwtTokenUtil(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        this.tokenBlacklist = initializeBlacklistCache();
    }

    private Cache<String, Boolean> initializeBlacklistCache() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        MutableConfiguration<String, Boolean> config = new MutableConfiguration<String, Boolean>()
                .setTypes(String.class, Boolean.class)
                .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(Duration.ONE_DAY))
                .setStoreByValue(false);
        return cacheManager.createCache("tokenBlacklist", config);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public UUID getUserIdFromJwtToken(String token) {
        String userId = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
        return UUID.fromString(userId);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            if (isTokenBlacklisted(authToken)) {
                logger.warn("Attempt to use blacklisted token");
                return false;
            }
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist != null && tokenBlacklist.containsKey(token);
    }

    public void blacklistToken(String token, long expirationMs) {
        if (tokenBlacklist != null) {
            tokenBlacklist.put(token, true);
            logger.info("Token blacklisted: {}", token);
        }
    }
}