package com.mipt.team4.antivirus_scanner_service.messaging;

import com.mipt.team4.antivirus_scanner_service.model.dto.ScanTaskDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ScanTaskConsumer {
  @RabbitListener(queues = "${antivirus.queues.tasks")
  public void handleScanTask(ScanTaskDto task) {
    // exceptions
  }
}
