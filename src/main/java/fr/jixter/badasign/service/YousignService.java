package fr.jixter.badasign.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.jixter.badasign.config.YousignConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class YousignService {

  private static final Logger logger = LoggerFactory.getLogger(YousignService.class);

  @Autowired private RestTemplate yousignRestTemplate;

  @Autowired private YousignConfig yousignConfig;

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Uploads a document to Yousign and returns the document ID
   *
   * @param pdfPath Path to the PDF file to upload
   * @param fileName Name for the document
   * @return Document ID from Yousign
   * @throws IOException if there's an error reading the file or calling the API
   */
  public String uploadDocument(Path pdfPath, String fileName) throws IOException {
    logger.info("Uploading document to Yousign: {}", fileName);

    // Read and encode the PDF file
    byte[] pdfBytes = Files.readAllBytes(pdfPath);
    String encodedContent = Base64.getEncoder().encodeToString(pdfBytes);

    // Prepare the request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", fileName);
    requestBody.put("content", encodedContent);
    requestBody.put("content_type", "application/pdf");

    // Make the API call
    String url = yousignConfig.getBaseUrl() + "/documents";

    try {
      ResponseEntity<String> response =
          yousignRestTemplate.postForEntity(url, requestBody, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String documentId = responseJson.get("id").asText();

        logger.info("Document uploaded successfully. Document ID: {}", documentId);
        return documentId;
      } else {
        throw new IOException("Failed to upload document. Status: " + response.getStatusCode());
      }
    } catch (Exception e) {
      logger.error("Error uploading document to Yousign: {}", e.getMessage(), e);
      throw new IOException("Failed to upload document to Yousign", e);
    }
  }

  /**
   * Creates a signature procedure with the uploaded document
   *
   * @param documentId ID of the uploaded document
   * @param signerEmail Email of the signer
   * @param signerName Name of the signer
   * @return Signature procedure ID
   * @throws IOException if there's an error calling the API
   */
  public String createSignatureProcedure(String documentId, String signerEmail, String signerName)
      throws IOException {
    logger.info(
        "Creating signature procedure for document: {} with signer: {}", documentId, signerEmail);

    // Prepare signer information
    Map<String, Object> signer = new HashMap<>();
    signer.put(
        "info",
        Map.of(
            "first_name",
            signerName.split(" ")[0],
            "last_name",
            signerName.contains(" ") ? signerName.substring(signerName.indexOf(" ") + 1) : "",
            "email",
            signerEmail));
    signer.put(
        "fields",
        List.of(
            Map.of(
                "document_id",
                documentId,
                "type",
                "signature",
                "page",
                1,
                "position",
                Map.of("x", 100, "y", 100),
                "size",
                Map.of("width", 200, "height", 50))));

    // Prepare the request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", "Contract Signature - " + signerName);
    requestBody.put("delivery_mode", "email");
    requestBody.put("documents", List.of(Map.of("id", documentId)));
    requestBody.put("signers", List.of(signer));

    // Make the API call
    String url = yousignConfig.getBaseUrl() + "/signature_requests";

    try {
      ResponseEntity<String> response =
          yousignRestTemplate.postForEntity(url, requestBody, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String procedureId = responseJson.get("id").asText();

        logger.info("Signature procedure created successfully. Procedure ID: {}", procedureId);
        return procedureId;
      } else {
        logger.error(
            "Failed to create signature procedure. Status: {}, Response: {}",
            response.getStatusCode(),
            response.getBody());
        throw new IOException(
            "Failed to create signature procedure. Status: " + response.getStatusCode());
      }
    } catch (Exception e) {
      logger.error("Error creating signature procedure: {}", e.getMessage(), e);
      throw new IOException("Failed to create signature procedure", e);
    }
  }

  /**
   * Activates a signature procedure
   *
   * @param procedureId ID of the signature procedure to activate
   * @throws IOException if there's an error calling the API
   */
  public void activateSignatureProcedure(String procedureId) throws IOException {
    logger.info("Activating signature procedure: {}", procedureId);

    String url = yousignConfig.getBaseUrl() + "/signature_requests/" + procedureId + "/activate";

    try {
      HttpEntity<String> entity = new HttpEntity<>("");
      ResponseEntity<String> response =
          yousignRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        logger.info("Signature procedure activated successfully: {}", procedureId);
      } else {
        logger.error(
            "Failed to activate signature procedure. Status: {}, Response: {}",
            response.getStatusCode(),
            response.getBody());
        throw new IOException(
            "Failed to activate signature procedure. Status: " + response.getStatusCode());
      }
    } catch (Exception e) {
      logger.error("Error activating signature procedure: {}", e.getMessage(), e);
      throw new IOException("Failed to activate signature procedure", e);
    }
  }

  /**
   * Complete workflow: upload document, create procedure, and activate it
   *
   * @param pdfPath Path to the PDF file
   * @param fileName Name for the document
   * @param signerEmail Email of the signer
   * @param signerName Name of the signer
   * @return Signature procedure ID
   * @throws IOException if there's an error in any step
   */
  public String processDocumentForSignature(
      Path pdfPath, String fileName, String signerEmail, String signerName) throws IOException {
    logger.info("Starting complete signature process for: {}", fileName);

    try {
      // Step 1: Upload document
      String documentId = uploadDocument(pdfPath, fileName);

      // Step 2: Create signature procedure
      String procedureId = createSignatureProcedure(documentId, signerEmail, signerName);

      // Step 3: Activate procedure
      activateSignatureProcedure(procedureId);

      logger.info(
          "Complete signature process finished successfully. Procedure ID: {}", procedureId);
      return procedureId;

    } catch (IOException e) {
      logger.error("Error in complete signature process: {}", e.getMessage(), e);
      throw e;
    }
  }
}
