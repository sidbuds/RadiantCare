package com.xixin.health.publicapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class PublicPackageCacheService {

    private static final String VERSION_KEY = "public:packages:version";
    private static final String LIST_PREFIX = "public:packages:list:";
    private static final String DETAIL_PREFIX = "public:packages:detail:";
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE =
            new TypeReference<List<Map<String, Object>>>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<Map<String, Object>>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public PublicPackageCacheService(StringRedisTemplate redisTemplate,
                                     ObjectMapper objectMapper,
                                     @Value("${app.cache.package-ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(Math.max(ttlSeconds, 1));
    }

    public List<Map<String, Object>> getPackageList(String centerCode) {
        return read(listKey(centerCode), LIST_TYPE);
    }

    public void cachePackageList(String centerCode, List<Map<String, Object>> packages) {
        write(listKey(centerCode), packages);
    }

    public Map<String, Object> getPackageDetail(String packageCode, String centerCode) {
        return read(detailKey(packageCode, centerCode), MAP_TYPE);
    }

    public void cachePackageDetail(String packageCode, String centerCode, Map<String, Object> detail) {
        write(detailKey(packageCode, centerCode), detail);
    }

    public void evictAll() {
        redisTemplate.opsForValue().increment(VERSION_KEY);
    }

    private String listKey(String centerCode) {
        return LIST_PREFIX + currentVersion() + ":" + normalize(centerCode);
    }

    private String detailKey(String packageCode, String centerCode) {
        return DETAIL_PREFIX + currentVersion() + ":" + packageCode + ":" + normalize(centerCode);
    }

    private long currentVersion() {
        String value = redisTemplate.opsForValue().get(VERSION_KEY);
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? "all" : value.trim();
    }

    private <T> T read(String key, TypeReference<T> typeReference) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception ex) {
            return null;
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
    }
}
