package com.mipt.team4.antivirus_scanner_service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "antivirus")
public record AntivirusProps(Redis redis, Queues queues, Clamav clamav, FastScan fastScan, S3 s3) {
  public record Redis(String prefix, int ttlHours) {}

  public record Queues(String tasks, String results) {}

  public record Clamav(String host, int port) {}

  public record FastScan(int tikaReadLimit) {}

  public record S3(
      String url,
      String accessKey,
      String secretKey,
      String region,
      UserDataBucket userDataBucket) {
    public record UserDataBucket(String name) {}
  }
}
