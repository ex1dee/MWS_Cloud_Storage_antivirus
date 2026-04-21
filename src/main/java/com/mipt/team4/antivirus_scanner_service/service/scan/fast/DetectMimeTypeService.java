package com.mipt.team4.antivirus_scanner_service.service.scan.fast;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.exception.scan.tika.TikaDetectIOException;
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

    tikaReadLimit = props.scan().fast().readLimit();
  }

  public String detectByName(String filename) {
    return tika.detect(filename);
  }

  public String detectByStream(InputStream inputStream, String filename, UUID fileId) {
    BoundedInputStream boundedStream = new BoundedInputStream(tikaReadLimit, inputStream);

    try {
      return tika.detect(boundedStream, filename);
    } catch (IOException e) {
      throw new TikaDetectIOException(filename, e);
    }
  }
}
