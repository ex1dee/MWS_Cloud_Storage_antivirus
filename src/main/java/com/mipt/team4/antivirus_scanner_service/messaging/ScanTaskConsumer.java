package com.mipt.team4.antivirus_scanner_service.messaging;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;
import com.mipt.team4.antivirus_scanner_service.model.dto.ScanTaskDto;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.service.scan.ScanOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanTaskConsumer {
  private final ScanOrchestrator orchestrator;
  private final ScanResultProducer resultProducer;

  @RabbitListener(queues = "${antivirus.queues.tasks}")
  public void handleScanTask(ScanTaskDto task) {
    try {
      ScanVerdict verdict = orchestrator.scan(task);
      resultProducer.sendResult(task.fileId(), verdict);
    } catch (RecoverableException e) {
      log.warn("Transient error scanning file {}. Retrying...", task.fileId(), e);
      throw e;
    } catch (Exception e) {
      log.error("Fatal error scanning file {}", task.fileId(), e);
      resultProducer.sendError(task.fileId());
    }
  }
}
