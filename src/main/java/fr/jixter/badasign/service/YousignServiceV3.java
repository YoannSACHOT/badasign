package fr.jixter.badasign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.jixter.badasign.config.YousignConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class YousignServiceV3 {

  public static final String SIGNATURE_REQUESTS = "/signature_requests/";
  private final YousignConfig yousignConfig;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestTemplate restTemplate = new RestTemplate();

  /** 1) Initiate an empty Signature Request */
  public String initiateSignatureRequest(String requestName) throws IOException {
    Map<String, Object> body = Map.of("name", requestName, "delivery_mode", "email");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(yousignConfig.getApiKey());
    HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

    String url = yousignConfig.getBaseUrl() + "/signature_requests";
    ResponseEntity<String> resp = restTemplate.postForEntity(url, req, String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new IOException("Failed to initiate signature request: " + resp.getStatusCode());
    }
    return objectMapper.readTree(resp.getBody()).get("id").asText();
  }

  /** 2) Upload the PDF *into* that Signature Request */
  public String uploadDocumentToRequest(String signatureRequestId, Path pdfPath, String fileName)
      throws IOException {

    byte[] pdfBytes = Files.readAllBytes(pdfPath);
    HttpEntity<MultiValueMap<String, Object>> req = getMultiValueMapHttpEntity(fileName, pdfBytes);

    String url =
        yousignConfig.getBaseUrl() + SIGNATURE_REQUESTS + signatureRequestId + "/documents";
    ResponseEntity<String> resp = restTemplate.postForEntity(url, req, String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new IOException("Failed to upload document: " + resp.getStatusCode());
    }
    return objectMapper.readTree(resp.getBody()).get("id").asText();
  }

  private HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity(
      String fileName, byte[] pdfBytes) {

    // 1a) wrap the PDF bytes in a Resource
    ByteArrayResource fileAsResource =
        new ByteArrayResource(pdfBytes) {
          @Override
          public String getFilename() {
            return fileName;
          }
        };

    // 1b) build the file–part headers
    HttpHeaders filePartHeaders = new HttpHeaders();
    filePartHeaders.setContentDispositionFormData("file", fileName);
    filePartHeaders.setContentType(MediaType.APPLICATION_PDF);

    // 1c) put the Resource + its headers into an HttpEntity
    HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileAsResource, filePartHeaders);

    // 1d) likewise wrap your “nature” field (optional, but explicit)
    HttpHeaders naturePartHeaders = new HttpHeaders();
    naturePartHeaders.setContentDispositionFormData("nature", null);
    HttpEntity<String> naturePart = new HttpEntity<>("signable_document", naturePartHeaders);

    // 1e) assemble the multipart map
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.add("file", filePart);
    form.add("nature", naturePart);

    // 1f) only set your auth header at the top level
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(yousignConfig.getApiKey());

    return new HttpEntity<>(form, headers);
  }

  /** 3) Add your signer and fields */
  public void addSigner(
      String signatureRequestId, String documentId, String signerEmail, String signerName)
      throws IOException {
    Map<String, Object> body = getStringObjectMap(documentId, signerEmail, signerName);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(yousignConfig.getApiKey());
    HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

    String url = yousignConfig.getBaseUrl() + SIGNATURE_REQUESTS + signatureRequestId + "/signers";
    ResponseEntity<String> resp = restTemplate.postForEntity(url, req, String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new IOException("Failed to add signer: " + resp.getStatusCode());
    }
    objectMapper.readTree(resp.getBody()).get("id").asText();
  }

  private static Map<String, Object> getStringObjectMap(
      String documentId, String signerEmail, String signerName) {
    String[] names = signerName.split(" ", 2);
    Map<String, Object> info =
        Map.of(
            "first_name",
            names[0],
            "last_name",
            names.length > 1 ? names[1] : "",
            "email",
            signerEmail,
            "locale",
            "fr");
    Map<String, Object> field =
        Map.of(
            "document_id", documentId,
            "type", "signature",
            "page", 1,
            "x", 100,
            "y", 100);

    return Map.of(
        "info",
        info,
        "signature_level",
        "electronic_signature",
        "signature_authentication_mode",
        "no_otp",
        "fields",
        List.of(field));
  }

  /** 4) Activate */
  public void activate(String signatureRequestId) throws IOException {
    String url = yousignConfig.getBaseUrl() + SIGNATURE_REQUESTS + signatureRequestId + "/activate";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(yousignConfig.getApiKey());
    HttpEntity<Void> req = new HttpEntity<>(headers);

    ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new IOException("Failed to activate: " + resp.getStatusCode());
    }
  }

  /** Complete flow */
  public String processDocumentForSignature(
      Path pdfPath, String fileName, String signerEmail, String signerName) throws IOException {

    String requestId = initiateSignatureRequest("Contract – " + signerName);
    String documentId = uploadDocumentToRequest(requestId, pdfPath, fileName);
    addSigner(requestId, documentId, signerEmail, signerName);
    activate(requestId);
    return requestId;
  }
}
