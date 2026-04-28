package com.mipt.team4.antivirus_scanner_service.service.scan;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.exception.s3.CloseFileStreamException;
import com.mipt.team4.antivirus_scanner_service.exception.scan.ScanExecutionException;
import com.mipt.team4.antivirus_scanner_service.exception.scan.ScanStageTimeoutException;
import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.dto.ScanTaskDto;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanStage;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.model.redis.ScanResultCache;
import com.mipt.team4.antivirus_scanner_service.service.s3.S3Service;
import com.mipt.team4.antivirus_scanner_service.service.scan.cache.ClamavSignatureProvider;
import com.mipt.team4.antivirus_scanner_service.service.scan.cache.ScanCacheService;
import com.mipt.team4.antivirus_scanner_service.service.scan.deep.DeepScanService;
import com.mipt.team4.antivirus_scanner_service.service.scan.fast.FastScanService;
import com.mipt.team4.antivirus_scanner_service.service.scan.structural.StructuralScanService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
  private final ClamavSignatureProvider clamavSignatureProvider;
  private final ExecutorService scanExecutor;

  public ScanVerdict scan(ScanTaskDto scanTask) {
    if (scanTask.size() == 0) {
      return ScanVerdict.EMPTY_FILE;
    }

    return getFromCache(scanTask).orElseGet(() -> performFullScanCycle(scanTask));
  }

  private Optional<ScanVerdict> getFromCache(ScanTaskDto scanTask) {
    if (!antivirusProps.redis().enabled()) {
      return Optional.empty();
    }

    Optional<ScanResultCache> cachedResult = cacheService.getResult(scanTask.hash());
    String currentSignatureVersion = clamavSignatureProvider.getVersion();

    return cachedResult
        .filter(
            result ->
                currentSignatureVersion != null
                    && currentSignatureVersion.equals(result.signatureVersion()))
        .map(ScanResultCache::verdict);
  }

  private ScanVerdict performFullScanCycle(ScanTaskDto scanTask) {
    ScanContext ctx = createContext(scanTask);
    int fastScanReadLimit = antivirusProps.scan().fast().readLimit();

    ScanVerdict fastVerdict =
        executeStage(
            ScanStage.FAST,
            ctx,
            () -> s3Service.getPartialStream(scanTask.s3Key(), fastScanReadLimit),
            fastScanService::scan);

    if (fastVerdict != ScanVerdict.CLEAN || isFullScanNotRequired(ctx.size())) {
      return fastVerdict;
    }

    if (structuralScanService.isRequired(scanTask.mimeType())) {
      ScanVerdict structVerdict =
          executeStage(
              ScanStage.STRUCTURAL,
              ctx,
              () -> s3Service.getFullStream(scanTask.s3Key()),
              structuralScanService::scan);

      if (structVerdict != ScanVerdict.CLEAN) {
        return structVerdict;
      }
    }

    return executeStage(
        ScanStage.DEEP,
        ctx,
        () -> s3Service.getFullStream(scanTask.s3Key()),
        deepScanService::scan);
  }

  private ScanVerdict executeStage(
      ScanStage stage,
      ScanContext ctx,
      Supplier<InputStream> streamSupplier,
      ScanFunction scanFunction) {
    CompletableFuture<ScanVerdict> scanFuture =
        getScanFuture(stage, ctx, streamSupplier, scanFunction);

    try {
      return scanFuture.get(antivirusProps.scan().stageTimeoutSec(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ScanExecutionException(stage, ctx.fileId(), e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }

      throw new ScanExecutionException(stage, ctx.fileId(), e);
    } catch (TimeoutException e) {
      scanFuture.cancel(true);
      throw new ScanStageTimeoutException(stage, ctx.fileId(), e);
    }
  }

  private CompletableFuture<ScanVerdict> getScanFuture(
      ScanStage stage,
      ScanContext ctx,
      Supplier<InputStream> streamSupplier,
      ScanFunction scanFunction) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (InputStream inputStream = streamSupplier.get()) {
            ScanVerdict verdict = scanFunction.apply(ctx, inputStream);

            if (needCache(stage, ctx.size(), verdict)) {
              cacheService.cacheResult(ctx.hash(), verdict, clamavSignatureProvider.getVersion());
            }

            return verdict;
          } catch (IOException e) {
            throw new CloseFileStreamException(ctx.fileId(), e);
          }
        },
        scanExecutor);
  }

  private boolean needCache(ScanStage stage, long fileSize, ScanVerdict verdict) {
    return (antivirusProps.redis().enabled()
            && verdict != ScanVerdict.CLEAN
            && verdict != ScanVerdict.CONTENT_MISMATCH)
        || isStageFinal(stage, fileSize);
  }

  private boolean isStageFinal(ScanStage stage, long fileSize) {
    return stage == ScanStage.DEEP || (stage == ScanStage.FAST && isFullScanNotRequired(fileSize));
  }

  private boolean isFullScanNotRequired(long fileSize) {
    return fileSize > antivirusProps.scan().fullScanThreshold();
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

  @FunctionalInterface
  private interface ScanFunction {
    ScanVerdict apply(ScanContext ctx, InputStream inputStream);
  }
}
