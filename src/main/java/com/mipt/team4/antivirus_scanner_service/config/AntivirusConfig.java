package com.mipt.team4.antivirus_scanner_service.config;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import xyz.capybara.clamav.ClamavClient;

@Configuration
public class AntivirusConfig {
  @Bean
  public ClamavClient clamavClient(AntivirusProps props) {
    return new ClamavClient(props.clamav().host(), props.clamav().port());
  }

  @Bean
  public Tika tika() {
    return new Tika();
  }

  @Bean
  public Parser autoDetectParser() {
    return new AutoDetectParser();
  }

  @Bean
  public ExecutorService scanExecutor(
      @Value("${spring.rabbitmq.listener.simple.concurrency}") int concurrency) {
    return Executors.newFixedThreadPool(concurrency);
  }

  @Bean
  public S3Client s3Client(AntivirusProps props) {
    AntivirusProps.S3 s3Props = props.s3();

    AwsBasicCredentials credentials =
        AwsBasicCredentials.create(s3Props.accessKey(), s3Props.secretKey());

    return S3Client.builder()
        .endpointOverride(URI.create(s3Props.url()))
        .overrideConfiguration(
            conf ->
                conf.retryStrategy(
                        AwsRetryStrategy.standardRetryStrategy().toBuilder()
                            .maxAttempts(s3Props.retryMaxAttempts())
                            .build())
                    .apiCallTimeout(Duration.ofSeconds(s3Props.timeoutSec().call()))
                    .apiCallAttemptTimeout(Duration.ofSeconds(s3Props.timeoutSec().callAttempt())))
        .region(Region.of(s3Props.region()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .forcePathStyle(true)
        .build();
  }
}
