package com.mipt.team4.antivirus_scanner_service.config;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean
  public Queue tasksQueue(AntivirusProps antivirusProps) {
    return QueueBuilder.durable(antivirusProps.queues().tasks()).build();
  }

  @Bean
  public Queue resultsQueue(AntivirusProps antivirusProps) {
    return QueueBuilder.durable(antivirusProps.queues().results()).build();
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
