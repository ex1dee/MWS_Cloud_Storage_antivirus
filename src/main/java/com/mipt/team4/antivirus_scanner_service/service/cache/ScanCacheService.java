package com.mipt.team4.antivirus_scanner_service.service.cache;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
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
    return Optional.ofNullable((ScanResultCache) redisTemplate.opsForValue().get(hash));
  }

  public void cacheResult(String hash, ScanResultCache result) {
    redisTemplate.opsForValue().set(cachePrefix + hash, result, cacheTtl);
  }
}
