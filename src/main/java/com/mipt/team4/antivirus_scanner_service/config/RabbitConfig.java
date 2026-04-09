package com.mipt.team4.antivirus_scanner_service.config;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean
  public Exchange tasksExchange(AntivirusProps antivirusProps) {
    return new DirectExchange(antivirusProps.rabbitmq().exchanges().tasks());
  }

  @Bean
  public Exchange resultsExchange(AntivirusProps antivirusProps) {
    return new DirectExchange(antivirusProps.rabbitmq().exchanges().results());
  }

  @Bean
  public Queue tasksQueue(AntivirusProps antivirusProps) {
    return QueueBuilder.durable(antivirusProps.rabbitmq().queues().tasks()).build();
  }

  @Bean
  public Queue resultsQueue(AntivirusProps antivirusProps) {
    return QueueBuilder.durable(antivirusProps.rabbitmq().queues().results()).build();
  }

  @Bean
  public Binding tasksBinding(
      Queue tasksQueue, Exchange tasksExchange, AntivirusProps antivirusProps) {
    return BindingBuilder.bind(tasksQueue)
        .to(tasksExchange)
        .with(antivirusProps.rabbitmq().routingKeys().tasks())
        .noargs();
  }

  @Bean
  public Binding resultsBinding(
      Queue resultsQueue, Exchange resultsExchange, AntivirusProps antivirusProps) {
    return BindingBuilder.bind(resultsQueue)
        .to(resultsExchange)
        .with(antivirusProps.rabbitmq().routingKeys().tasks())
        .noargs();
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
