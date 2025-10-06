package fr.jixter.badasign.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import fr.jixter.badasign.service.YousignServiceV3;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

@SpringBootTest
@AutoConfigureMockMvc
class SignatureControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockBean private YousignServiceV3 yousignService;

  @Test
  @DisplayName("POST /api/signature/upload returns 200 with documentId on success")
  void upload_success() throws Exception {
    Mockito.when(
            yousignService.processDocumentForSignature(
                Mockito.any(), Mockito.eq("contract.pdf"), Mockito.eq("john@example.com"), Mockito.eq("John Doe")))
        .thenReturn("req-123");

    MockMultipartFile file =
        new MockMultipartFile("file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/api/signature/upload")
                .file(file)
                .param("fileName", "contract.pdf")
                .param("email", "john@example.com")
                .param("name", "John Doe"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.documentId", is("req-123")))
        .andExpect(jsonPath("$.status", is("uploaded")))
        .andExpect(jsonPath("$.fileName", is("contract.pdf")));
  }

  @Test
  @DisplayName("POST /api/signature/upload returns 400 when file is empty")
  void upload_emptyFile_returnsBadRequest() throws Exception {
    MockMultipartFile empty =
        new MockMultipartFile("file", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[] {});

    mockMvc
        .perform(
            multipart("/api/signature/upload")
                .file(empty)
                .param("fileName", "empty.pdf")
                .param("email", "john@example.com")
                .param("name", "John Doe"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("POST /api/signature/upload returns 500 when service throws IOException")
  void upload_serviceError_returnsInternalServerError() throws Exception {
    Mockito.when(
            yousignService.processDocumentForSignature(
                Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenThrow(new IOException("boom"));

    MockMultipartFile file =
        new MockMultipartFile("file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[] {1});

    mockMvc
        .perform(
            multipart("/api/signature/upload")
                .file(file)
                .param("fileName", "contract.pdf")
                .param("email", "john@example.com")
                .param("name", "John Doe"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error", containsString("Failed to upload document")));
  }
}
