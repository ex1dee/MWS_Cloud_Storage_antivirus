package com.mipt.team4.antivirus_scanner_service.config;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitConfig {
  @Bean
  public Exchange tasksExchange(AntivirusProps props) {
    return new DirectExchange(props.rabbitmq().exchanges().tasks());
  }

  @Bean
  public Exchange deadLetterExchange(AntivirusProps props) {
    return new DirectExchange(props.rabbitmq().exchanges().tasksDlx());
  }

  @Bean
  public Exchange resultsExchange(AntivirusProps props) {
    return new DirectExchange(props.rabbitmq().exchanges().results());
  }

  @Bean
  public Queue tasksQueue(AntivirusProps props) {
    return QueueBuilder.durable(props.rabbitmq().queues().tasks())
        .withArgument("x-dead-letter-exchange", props.rabbitmq().exchanges().tasksDlx())
        .withArgument("x-dead-letter-routing-key", props.rabbitmq().routingKeys().tasksDlq())
        .build();
  }

  @Bean
  public Queue deadLetterQueue(AntivirusProps props) {
    return QueueBuilder.durable(props.rabbitmq().queues().tasksDlq()).build();
  }

  @Bean
  public Queue resultsQueue(AntivirusProps props) {
    return QueueBuilder.durable(props.rabbitmq().queues().results()).build();
  }

  @Bean
  public Binding tasksBinding(Queue tasksQueue, Exchange tasksExchange, AntivirusProps props) {
    return BindingBuilder.bind(tasksQueue)
        .to(tasksExchange)
        .with(props.rabbitmq().routingKeys().tasks())
        .noargs();
  }

  @Bean
  public Binding deadLetterBinding(
      Queue deadLetterQueue, Exchange deadLetterExchange, AntivirusProps props) {
    return BindingBuilder.bind(deadLetterQueue)
        .to(deadLetterExchange)
        .with(props.rabbitmq().routingKeys().tasksDlq())
        .noargs();
  }

  @Bean
  public Binding resultsBinding(
      Queue resultsQueue, Exchange resultsExchange, AntivirusProps props) {
    return BindingBuilder.bind(resultsQueue)
        .to(resultsExchange)
        .with(props.rabbitmq().routingKeys().tasks())
        .noargs();
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RetryOperationsInterceptor retryInterceptor(AntivirusProps props) {
    var retryProps = props.rabbitmq().retry();

    return RetryInterceptorBuilder.stateless()
        .maxAttempts(retryProps.maxAttempts())
        .backOffOptions(
            retryProps.initialInterval(), retryProps.multiplier(), retryProps.maxInterval())
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      SimpleRabbitListenerContainerFactoryConfigurer configurer,
      RetryOperationsInterceptor retryOperationsInterceptor,
      ConnectionFactory connectionFactory) {
    var factory = new SimpleRabbitListenerContainerFactory();
    configurer.configure(factory, connectionFactory);

    Advice[] adviceChain = {retryOperationsInterceptor};
    factory.setAdviceChain(adviceChain);

    return factory;
  }
}
