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
      Map.ofEntries(
          Map.entry("application/x-rar", "application/vnd.rar"),
          Map.entry("application/x-rar-compressed", "application/vnd.rar"),
          Map.entry("application/x-zip-compressed", "application/zip"),
          Map.entry("application/x-zip", "application/zip"),
          Map.entry("image/jpg", "image/jpeg"),
          Map.entry("text/javascript", "application/javascript"),
          Map.entry("application/x-javascript", "application/javascript"),
          Map.entry("audio/mp3", "audio/mpeg"),
          Map.entry("text/xml", "application/xml"),
          Map.entry("application/x-xml", "application/xml"),
          Map.entry("application/x-tar", "application/x-tar"));

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
