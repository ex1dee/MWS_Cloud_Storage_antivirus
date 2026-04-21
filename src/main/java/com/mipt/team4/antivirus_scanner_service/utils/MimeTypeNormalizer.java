package com.mipt.team4.antivirus_scanner_service.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@UtilityClass
public class MimeTypeNormalizer {
  private static final String BASE_MIME = "application/octet-stream";

  private static final Map<String, String> MIME_ALIASES =
      Map.of(
          "image/jpg", "image/jpeg",
          "text/javascript", "application/javascript",
          "application/x-javascript", "application/javascript",
          "application/x-zip-compressed", "application/zip",
          "audio/mp3", "audio/mpeg",
          "text/xml", "application/xml",
          "application/x-xml", "application/xml");

  public static String normalize(String mimeTypeStr) {
    if (mimeTypeStr == null || mimeTypeStr.isBlank()) {
      return BASE_MIME;
    }

    try {
      MimeType mimeType = MimeTypeUtils.parseMimeType(mimeTypeStr);
      String baseMime = (mimeType.getType() + "/" + mimeType.getSubtype()).toLowerCase();

      return MIME_ALIASES.getOrDefault(baseMime, baseMime);
    } catch (InvalidMimeTypeException exception) {
      return BASE_MIME;
    }
  }
}
