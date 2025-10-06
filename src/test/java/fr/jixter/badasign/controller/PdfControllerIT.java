package fr.jixter.badasign.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PdfControllerIT {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("GET /api/pdf/sample-data returns a sample map")
  void sampleData_returnsMap() throws Exception {
    mockMvc
        .perform(get("/api/pdf/sample-data"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.firstName").exists())
        .andExpect(jsonPath("$.lastName").exists())
        .andExpect(jsonPath("$.email").exists());
  }

  @Test
  @DisplayName("POST /api/pdf/fill fills the PDF and returns application/pdf stream")
  void fill_returnsPdf() throws Exception {
    Map<String, String> payload =
        Map.of(
            "firstName", "Jane",
            "lastName", "Doe",
            "email", "jane.doe@example.com",
            "contractNumber", "CTR-123");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/pdf/fill")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
            .andReturn();

    byte[] bytes = result.getResponse().getContentAsByteArray();
    assertThat(bytes.length).isGreaterThan(0);
  }
}
