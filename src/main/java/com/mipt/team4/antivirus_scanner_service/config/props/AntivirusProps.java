package com.mipt.team4.antivirus_scanner_service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "antivirus")
public record AntivirusProps(Redis redis, Rabbitmq rabbitmq, Clamav clamav, Scan scan, S3 s3) {
  public record Redis(String prefix, int ttlHours) {}

  public record Rabbitmq(Retry retry, RoutingKeys routingKeys, Exchanges exchanges, Queues queues) {
    public record Retry(
        int maxAttempts, long initialInterval, double multiplier, long maxInterval) {}

    public record RoutingKeys(String tasks, String tasksDlq, String results) {}

    public record Exchanges(String tasks, String tasksDlx, String results) {}

    public record Queues(String tasks, String tasksDlq, String results) {}
  }

  public record Clamav(String host, int port) {}

  public record Scan(Fast fast, long fullScanThresholdMb) {
    public record Fast(int readLimit) {}
  }

  public record S3(
      String url,
      String accessKey,
      String secretKey,
      String region,
      UserDataBucket userDataBucket) {
    public record UserDataBucket(String name) {}
  }
}
