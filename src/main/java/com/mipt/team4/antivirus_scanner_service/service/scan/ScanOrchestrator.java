package com.mipt.team4.antivirus_scanner_service.service.scan;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.exception.s3.CloseFileStreamException;
import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.dto.ScanTaskDto;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.model.redis.ScanResultCache;
import com.mipt.team4.antivirus_scanner_service.service.s3.S3Service;
import com.mipt.team4.antivirus_scanner_service.service.scan.cache.ScanCacheService;
import com.mipt.team4.antivirus_scanner_service.service.scan.deep.DeepScanService;
import com.mipt.team4.antivirus_scanner_service.service.scan.fast.FastScanService;
import com.mipt.team4.antivirus_scanner_service.service.scan.structural.StructuralScanService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScanOrchestrator {
  private final S3Service s3Service;
  private final ScanCacheService cacheService;
  private final FastScanService fastScanService;
  private final StructuralScanService structuralScanService;
  private final DeepScanService deepScanService;
  private final AntivirusProps antivirusProps;

  public ScanVerdict scan(ScanTaskDto scanTask) {
    if (scanTask.size() == 0) {
      return ScanVerdict.EMPTY_FILE;
    }

    return getFromCache(scanTask).orElseGet(() -> performFullScanCycle(scanTask));
  }

  private Optional<ScanVerdict> getFromCache(ScanTaskDto scanTask) {
    return cacheService.getResult(scanTask.hash()).map(ScanResultCache::verdict);
  }

  private ScanVerdict performFullScanCycle(ScanTaskDto scanTask) {
    ScanContext ctx = createContext(scanTask);
    int fastScanReadLimit = antivirusProps.scan().fast().readLimit();

    ScanVerdict fastVerdict =
        executeStage(
            Stage.FAST,
            ctx,
            () -> s3Service.getPartialStream(scanTask.s3Key(), fastScanReadLimit),
            fastScanService::scan);

    if (fastVerdict != ScanVerdict.CLEAN) {
      return fastVerdict;
    }

    if (structuralScanService.isRequired(scanTask.mimeType())) {
      ScanVerdict structVerdict =
          executeStage(
              Stage.STRUCTURAL,
              ctx,
              () -> s3Service.getFullStream(scanTask.s3Key()),
              structuralScanService::scan);

      if (structVerdict != ScanVerdict.CLEAN) {
        return structVerdict;
      }
    }

    return executeStage(
        Stage.DEEP, ctx, () -> s3Service.getFullStream(scanTask.s3Key()), deepScanService::scan);
  }

  private ScanVerdict executeStage(
      Stage stage,
      ScanContext ctx,
      Supplier<InputStream> streamSupplier,
      ScanFunction scanFunction) {
    try (InputStream inputStream = streamSupplier.get()) {
      ScanVerdict verdict = scanFunction.apply(ctx, inputStream);

      if (needCache(stage, ctx.size(), verdict)) {
        cacheService.cacheResult(ctx.hash(), ctx.size(), verdict);
      }

      return verdict;
    } catch (IOException e) {
      throw new CloseFileStreamException(ctx.fileId(), e);
    }
  }

  private boolean needCache(Stage stage, long fileSize, ScanVerdict verdict) {
    return verdict != ScanVerdict.CLEAN || isStageFinal(stage, fileSize);
  }

  private boolean isStageFinal(Stage stage, long fileSize) {
    return stage == Stage.DEEP || (stage == Stage.FAST && !isFullScanRequired(fileSize));
  }

  private boolean isFullScanRequired(long fileSize) {
    return fileSize > antivirusProps.scan().fullScanThresholdMb();
  }

  private ScanContext createContext(ScanTaskDto scanTask) {
    return ScanContext.builder()
        .fileId(scanTask.fileId())
        .hash(scanTask.hash())
        .originalName(scanTask.name())
        .declaredMimeType(scanTask.mimeType())
        .size(scanTask.size())
        .build();
  }

  private enum Stage {
    FAST,
    STRUCTURAL,
    DEEP
  }

  @FunctionalInterface
  private interface ScanFunction {
    ScanVerdict apply(ScanContext ctx, InputStream inputStream);
  }
}
