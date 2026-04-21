package com.mipt.team4.antivirus_scanner_service.messaging;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.model.dto.ScanResultDto;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScanResultProducer {
  private final RabbitTemplate rabbitTemplate;
  private final AntivirusProps antivirusProps;

  public void sendError(UUID fileId) {
    sendResult(fileId, ScanVerdict.ERROR);
  }

  public void sendResult(UUID fileId, ScanVerdict verdict) {
    ScanResultDto result = ScanResultDto.builder().fileId(fileId).verdict(verdict).build();
    send(result);
  }

  private void send(ScanResultDto result) {
    String exchange = antivirusProps.rabbitmq().exchanges().results();
    String routingKey = antivirusProps.rabbitmq().routingKeys().results();

    rabbitTemplate.convertAndSend(exchange, routingKey, result);
  }
}
