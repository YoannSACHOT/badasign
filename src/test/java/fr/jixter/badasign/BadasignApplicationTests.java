package fr.jixter.badasign;

import fr.jixter.badasign.service.PdfFillingService;
import fr.jixter.badasign.util.PdfTemplateGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BadasignApplicationTests {

  @Autowired
  private PdfFillingService pdfFillingService;

  @Test
  void contextLoads() {
  }

  @Test
  void testEnhancedTemplateGeneration() throws Exception {
    // Generate the enhanced template directly
    Path templatePath = Paths.get("src/main/resources/templates/contract-template.pdf");
    PdfTemplateGenerator.createContractTemplate(templatePath);
    
    System.out.println("Enhanced French contract template generated successfully at: " + templatePath.toAbsolutePath());
  }

  @Test
  void testEnhancedTemplateFilling() throws Exception {
    // Prepare sample French contract data
    Map<String, String> sampleData = Map.of(
        "firstName", "Marie",
        "lastName", "Dubois",
        "email", "marie.dubois@entreprise.fr",
        "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        "contractNumber", "CTR-2025-001",
        "amount", "3500.00 €",
        "company", "Société Française Innovation",
        "position", "Ingénieure Logiciel Senior",
        "startDate", LocalDateTime.now().plusDays(15).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    );

    // Fill the enhanced template
    Path filledPdf = pdfFillingService.fillPdfTemplate(sampleData);
    
    // Verify the filled PDF was created
    assert Files.exists(filledPdf) : "Filled PDF should exist";
    assert Files.size(filledPdf) > 0 : "Filled PDF should not be empty";
    
    System.out.println("Enhanced French contract template filled successfully!");
    System.out.println("Filled PDF created at: " + filledPdf.toAbsolutePath());
    System.out.println("PDF size: " + Files.size(filledPdf) + " bytes");
    
    // Clean up temporary file
    pdfFillingService.deleteTempFile(filledPdf);
  }

}
