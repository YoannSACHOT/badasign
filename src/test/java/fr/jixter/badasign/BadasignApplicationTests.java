package fr.jixter.badasign;

import fr.jixter.badasign.service.PdfFillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BadasignApplicationTests {

  @Autowired private PdfFillingService pdfFillingService;

  @Test
  void contextLoads() {
    // Verify that the Spring context loads successfully
    assert pdfFillingService != null : "PdfFillingService should be autowired and not null";
  }
}
