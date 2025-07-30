package fr.jixter.badasign.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.jixter.badasign.config.YousignConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class YousignServiceTest {

  @Mock private RestTemplate yousignRestTemplate;

  @Mock private YousignConfig yousignConfig;

  @InjectMocks private YousignService yousignService;

  private Path testPdfPath;

  @BeforeEach
  void setUp() throws IOException {
    // Create a temporary test PDF file
    testPdfPath = Files.createTempFile("test-document", ".pdf");
    Files.write(testPdfPath, "Test PDF content".getBytes());
  }

  @Test
  void testUploadDocument_Success() throws IOException {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    String expectedDocumentId = "doc_123456";
    String responseBody = "{\"id\":\"" + expectedDocumentId + "\",\"name\":\"test.pdf\"}";

    ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act
    String documentId = yousignService.uploadDocument(testPdfPath, "test.pdf");

    // Assert
    assertEquals(expectedDocumentId, documentId);
    verify(yousignRestTemplate)
        .postForEntity(eq("https://api.yousign.app/v3/documents"), any(), eq(String.class));
  }

  @Test
  void testUploadDocument_ApiError() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    ResponseEntity<String> mockResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class, () -> yousignService.uploadDocument(testPdfPath, "test.pdf"));

    assertTrue(exception.getMessage().contains("Failed to upload document"));
  }

  @Test
  void testUploadDocument_NetworkError() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenThrow(new RestClientException("Network error"));

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class, () -> yousignService.uploadDocument(testPdfPath, "test.pdf"));

    assertTrue(exception.getMessage().contains("Failed to upload document to Yousign"));
  }

  @Test
  void testCreateSignatureProcedure_Success() throws IOException {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    String expectedProcedureId = "proc_123456";
    String responseBody = "{\"id\":\"" + expectedProcedureId + "\",\"status\":\"draft\"}";

    ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act
    String procedureId =
        yousignService.createSignatureProcedure("doc_123", "test@example.com", "Test User");

    // Assert
    assertEquals(expectedProcedureId, procedureId);
    verify(yousignRestTemplate)
        .postForEntity(
            eq("https://api.yousign.app/v3/signature_requests"), any(), eq(String.class));
  }

  @Test
  void testCreateSignatureProcedure_ApiError() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    ResponseEntity<String> mockResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class,
            () ->
                yousignService.createSignatureProcedure(
                    "doc_123", "test@example.com", "Test User"));

    assertTrue(exception.getMessage().contains("Failed to create signature procedure"));
  }

  @Test
  void testActivateSignatureProcedure_Success() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    String responseBody = "{\"id\":\"proc_123\",\"status\":\"ongoing\"}";
    ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
    when(yousignRestTemplate.exchange(
            anyString(), eq(org.springframework.http.HttpMethod.POST), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    assertDoesNotThrow(() -> yousignService.activateSignatureProcedure("proc_123"));

    verify(yousignRestTemplate)
        .exchange(
            eq("https://api.yousign.app/v3/signature_requests/proc_123/activate"),
            eq(org.springframework.http.HttpMethod.POST),
            any(),
            eq(String.class));
  }

  @Test
  void testActivateSignatureProcedure_ApiError() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    ResponseEntity<String> mockResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    when(yousignRestTemplate.exchange(
            anyString(), eq(org.springframework.http.HttpMethod.POST), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class, () -> yousignService.activateSignatureProcedure("proc_123"));

    assertTrue(exception.getMessage().contains("Failed to activate signature procedure"));
  }

  @Test
  void testProcessDocumentForSignature_Success() throws IOException {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    String documentId = "doc_123456";
    String procedureId = "proc_123456";

    // Mock upload document response
    String uploadResponse = "{\"id\":\"" + documentId + "\",\"name\":\"test.pdf\"}";
    ResponseEntity<String> mockUploadResponse = new ResponseEntity<>(uploadResponse, HttpStatus.OK);

    // Mock create procedure response
    String procedureResponse = "{\"id\":\"" + procedureId + "\",\"status\":\"draft\"}";
    ResponseEntity<String> mockProcedureResponse =
        new ResponseEntity<>(procedureResponse, HttpStatus.CREATED);

    // Mock activate response
    String activateResponse = "{\"id\":\"" + procedureId + "\",\"status\":\"ongoing\"}";
    ResponseEntity<String> mockActivateResponse =
        new ResponseEntity<>(activateResponse, HttpStatus.OK);

    when(yousignRestTemplate.postForEntity(contains("/documents"), any(), eq(String.class)))
        .thenReturn(mockUploadResponse);
    when(yousignRestTemplate.postForEntity(
            contains("/signature_requests"), any(), eq(String.class)))
        .thenReturn(mockProcedureResponse);
    when(yousignRestTemplate.exchange(
            contains("/activate"),
            eq(org.springframework.http.HttpMethod.POST),
            any(),
            eq(String.class)))
        .thenReturn(mockActivateResponse);

    // Act
    String result =
        yousignService.processDocumentForSignature(
            testPdfPath, "test.pdf", "test@example.com", "Test User");

    // Assert
    assertEquals(procedureId, result);
    verify(yousignRestTemplate).postForEntity(contains("/documents"), any(), eq(String.class));
    verify(yousignRestTemplate)
        .postForEntity(contains("/signature_requests"), any(), eq(String.class));
    verify(yousignRestTemplate)
        .exchange(
            contains("/activate"),
            eq(org.springframework.http.HttpMethod.POST),
            any(),
            eq(String.class));
  }

  @Test
  void testProcessDocumentForSignature_UploadFails() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    when(yousignRestTemplate.postForEntity(contains("/documents"), any(), eq(String.class)))
        .thenThrow(new RestClientException("Upload failed"));

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class,
            () ->
                yousignService.processDocumentForSignature(
                    testPdfPath, "test.pdf", "test@example.com", "Test User"));

    assertTrue(exception.getMessage().contains("Failed to upload document to Yousign"));
  }

  @Test
  void testUploadDocument_FileNotFound() {
    // Arrange
    Path nonExistentPath = Paths.get("non-existent-file.pdf");

    // Act & Assert
    IOException exception =
        assertThrows(
            IOException.class, () -> yousignService.uploadDocument(nonExistentPath, "test.pdf"));

    // The exception should be thrown when trying to read the non-existent file
    assertNotNull(exception);
  }

  @Test
  void testUploadDocument_EmptyFileName() throws IOException {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");
    String responseBody = "{\"id\":\"doc_123456\",\"name\":\"\"}";
    ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
    when(yousignRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(mockResponse);

    // Act
    String documentId = yousignService.uploadDocument(testPdfPath, "");

    // Assert
    assertEquals("doc_123456", documentId);
  }

  @Test
  void testCreateSignatureProcedure_EmptyParameters() {
    // Arrange
    when(yousignConfig.getBaseUrl()).thenReturn("https://api.yousign.app/v3");

    // Act & Assert
    assertThrows(IOException.class, () -> yousignService.createSignatureProcedure("", "", ""));
  }
}
