package com.mipt.team4.antivirus_scanner_service.service.scan.fast;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.exception.scan.tika.TikaDetectIOException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.io.BoundedInputStream;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DetectMimeTypeService {
  private final Tika tika;
  private final int tikaReadLimit;

  public DetectMimeTypeService(Tika tika, AntivirusProps props) {
    this.tika = tika;

    tikaReadLimit = props.fastScan().tikaReadLimit();
  }

  public String detectByName(String filename) {
    return tika.detect(filename);
  }

  public String detectByStream(InputStream inputStream, String filename, UUID fileId) {
    BufferedInputStream bufferedStream = getBufferedStream(inputStream);
    bufferedStream.mark(tikaReadLimit);

    BoundedInputStream boundedStream = new BoundedInputStream(tikaReadLimit, bufferedStream);

    try {
      return tika.detect(boundedStream, filename);
    } catch (IOException e) {
      throw new TikaDetectIOException(filename, e);
    } finally {
      try {
        bufferedStream.reset();
      } catch (IOException e) {
        log.error("Failed to reset stream after Tika detection for file: {}", fileId);
      }
    }
  }

  private BufferedInputStream getBufferedStream(InputStream inputStream) {
    return inputStream instanceof BufferedInputStream
        ? (BufferedInputStream) inputStream
        : new BufferedInputStream(inputStream);
  }
}
