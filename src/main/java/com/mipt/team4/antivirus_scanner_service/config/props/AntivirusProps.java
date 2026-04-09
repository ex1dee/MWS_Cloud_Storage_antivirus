package com.mipt.team4.antivirus_scanner_service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "antivirus")
public record AntivirusProps(Redis redis, Queues queues, Clamav clamav, FastScan fastScan) {
  public record Redis(String prefix, int ttlHours) {}

  public record Queues(String tasks, String results) {}

  public record Clamav(String host, int port) {}

  public record FastScan(int tikaReadLimit) {}
}
