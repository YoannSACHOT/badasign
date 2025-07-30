package fr.jixter.badasign.controller;

import fr.jixter.badasign.service.PdfFillingService;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

  private static final Logger logger = LoggerFactory.getLogger(PdfController.class);

  @Autowired private PdfFillingService pdfFillingService;

  @PostMapping(value = "/fill", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<Resource> fillPdfTemplate(@RequestBody Map<String, String> formData) {

    logger.info("Received request to fill PDF template with {} fields", formData.size());

    try {
      // Fill the PDF template
      Path filledPdfPath = pdfFillingService.fillPdfTemplate(formData);

      // Create resource from the filled PDF
      Resource resource = new FileSystemResource(filledPdfPath);

      // Generate filename with timestamp
      String filename =
          "filled-contract-"
              + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
              + ".pdf";

      // Set response headers
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

      logger.info("PDF template filled successfully, returning file: {}", filename);

      return ResponseEntity.ok().headers(headers).body(resource);

    } catch (IOException e) {
      logger.error("Error filling PDF template: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/sample-data")
  public ResponseEntity<Map<String, String>> getSampleFormData() {
    logger.info("Received request for sample form data");

    Map<String, String> sampleData =
        Map.of(
            "firstName", "Jean",
            "lastName", "Dupont",
            "email", "jean.dupont@example.com",
            "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            "contractNumber", "CTR-" + System.currentTimeMillis(),
            "amount", "1500.00 €",
            "company", "Jixter Solutions",
            "position", "Développeur Senior",
            "startDate",
                LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

    logger.info("Returning sample form data with {} fields", sampleData.size());
    return ResponseEntity.ok(sampleData);
  }
}
