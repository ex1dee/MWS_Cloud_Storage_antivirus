package com.mipt.team4.antivirus_scanner_service.service.scan.cache;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.model.redis.ScanResultCache;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScanCacheService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final String cachePrefix;
  private final Duration cacheTtl;

  public ScanCacheService(RedisTemplate<String, Object> redisTemplate, AntivirusProps props) {
    this.redisTemplate = redisTemplate;

    cachePrefix = props.redis().prefix();
    cacheTtl = Duration.ofHours(props.redis().ttlHours());
  }

  public Optional<ScanResultCache> getResult(String hash) {
    String key = getKey(hash);
    return Optional.ofNullable((ScanResultCache) redisTemplate.opsForValue().get(key));
  }

  public void cacheResult(String hash, ScanVerdict verdict, String signatureVersion) {
    ScanResultCache result =
        ScanResultCache.builder().verdict(verdict).signatureVersion(signatureVersion).build();

    String key = getKey(hash);
    redisTemplate.opsForValue().set(key, result, cacheTtl);
  }

  private String getKey(String hash) {
    return cachePrefix + hash;
  }
}
