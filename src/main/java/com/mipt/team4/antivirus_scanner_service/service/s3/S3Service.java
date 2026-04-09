package com.mipt.team4.antivirus_scanner_service.service.s3;

import com.mipt.team4.antivirus_scanner_service.config.props.AntivirusProps;
import com.mipt.team4.antivirus_scanner_service.exception.s3.DownloadFileException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Slf4j
@Service
public class S3Service {
  private final S3Client s3Client;
  private final String bucketName;

  @Autowired
  public S3Service(AntivirusProps props, S3Client s3Client) {
    this.bucketName = props.s3().userDataBucket().name();
    this.s3Client = s3Client;
  }

  public InputStream getFileStream(String s3Key) {
    try {
      return s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(s3Key).build());
    } catch (Exception e) {
      throw new DownloadFileException(s3Key, e);
    }
  }
}
