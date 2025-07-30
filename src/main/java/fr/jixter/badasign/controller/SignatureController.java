package fr.jixter.badasign.controller;

import fr.jixter.badasign.service.YousignServiceV3;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/signature")
@RequiredArgsConstructor
public class SignatureController {

  private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);
  public static final String DOCUMENT_ID = "documentId";
  public static final String STATUS = "status";
  public static final String ERROR = "error";

  private final YousignServiceV3 yousignService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("fileName") String fileName,
      @RequestParam("email") String email,
      @RequestParam("name") String name) {

    logger.info("Received request to upload document: {}", fileName);

    if (file.isEmpty()) {
      logger.warn("Empty file received");
      return ResponseEntity.badRequest().body(Map.of(ERROR, "File is empty"));
    }

    try {
      // Save uploaded file temporarily
      Path tempFile = Files.createTempFile("upload-", ".pdf");
      file.transferTo(tempFile.toFile());

      // Upload to Yousign
      String documentId =
          yousignService.processDocumentForSignature(tempFile, fileName, email, name);

      // Clean up temporary file
      Files.deleteIfExists(tempFile);

      logger.info("Document uploaded successfully with ID: {}", documentId);
      return ResponseEntity.ok(
          Map.of(DOCUMENT_ID, documentId, "fileName", fileName, STATUS, "uploaded"));

    } catch (IOException e) {
      logger.error("Error uploading document: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(ERROR, "Failed to upload document: " + e.getMessage()));
    }
  }
}
