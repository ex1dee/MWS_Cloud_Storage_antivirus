package com.mipt.team4.antivirus_scanner_service.config;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
