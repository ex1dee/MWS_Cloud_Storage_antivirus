package com.mipt.team4.antivirus_scanner_service.service.scan.structural;

import com.mipt.team4.antivirus_scanner_service.exception.scan.tika.TikaParseException;
import com.mipt.team4.antivirus_scanner_service.exception.scan.tika.TikaParseIOException;
import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.TikaMemoryLimitException;
import org.apache.tika.exception.ZeroByteFileException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructuralScanService {
  private final Parser autoDetectParser;

  public boolean isRequired(String mimeType) {
    if (mimeType == null) return true;

    return mimeType.startsWith("application/")
        || mimeType.contains("xml")
        || mimeType.contains("html")
        || mimeType.endsWith("pdf")
        || mimeType.contains("zip")
        || mimeType.contains("rar")
        || mimeType.contains("tar")
        || mimeType.contains("7z")
        || mimeType.contains("compressed");
  }

  public ScanVerdict scan(ScanContext ctx, InputStream inputStream) {
    Metadata metadata = new Metadata();
    ParseContext parseContext = new ParseContext();
    parseContext.set(Parser.class, autoDetectParser);

    try {
      autoDetectParser.parse(inputStream, new DefaultHandler(), metadata, parseContext);
      log.info("Tika metadata for {}: {}", ctx.fileId(), Arrays.toString(metadata.names()));
      return ScanVerdict.CLEAN;
    } catch (EncryptedDocumentException e) {
      return ScanVerdict.PASSWORD_PROTECTED;
    } catch (ZeroByteFileException e) {
      return ScanVerdict.EMPTY_FILE;
    } catch (TikaMemoryLimitException e) {
      log.warn("Potential ZIP-bomb detected! File: {}", ctx.fileId());
      return ScanVerdict.RESOURCE_EXHAUSTED;
    } catch (SAXException e) {
      if (e.getMessage() != null && e.getMessage().contains("limit")) {
        log.warn(
            "Resource limit reached during SAX parsing. File: {}, Size: {}",
            ctx.fileId(),
            ctx.size(),
            e);
        return ScanVerdict.RESOURCE_EXHAUSTED;
      }

      throw new TikaParseException(ctx.fileId(), e);
    } catch (TikaException e) {
      throw new TikaParseException(ctx.fileId(), e);
    } catch (IOException e) {
      throw new TikaParseIOException(ctx.fileId(), e);
    }
  }
}
