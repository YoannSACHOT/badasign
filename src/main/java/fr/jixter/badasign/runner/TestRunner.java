package fr.jixter.badasign.runner;

import fr.jixter.badasign.service.PdfFillingService;
import fr.jixter.badasign.service.YousignService;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after PdfTemplateInitializer
public class TestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Autowired
    private PdfFillingService pdfFillingService;

    @Autowired
    private YousignService yousignService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Starting Badasign Test Runner ===");

        try {
            // Step 1: Prepare test data for PDF filling
            Map<String, String> testData = prepareTestData();
            logger.info("Test data prepared: {}", testData);

            // Step 2: Fill PDF template
            logger.info("Step 1: Filling PDF template...");
            Path filledPdfPath = pdfFillingService.fillPdfTemplate(testData);
            logger.info("✓ PDF template filled successfully. File location: {}", filledPdfPath.toAbsolutePath());

            // Step 3: Process document for signature (if API key is configured)
            String apiKey = System.getProperty("yousign.api.api-key", System.getenv("YOUSIGN_API_KEY"));
            if (apiKey != null && !apiKey.equals("your-api-key-here") && !apiKey.trim().isEmpty()) {
                logger.info("Step 2: Processing document for signature...");
                
                String fileName = "Contract_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                String signerEmail = testData.get("email");
                String signerName = testData.get("firstName") + " " + testData.get("lastName");

                try {
                    String procedureId = yousignService.processDocumentForSignature(
                        filledPdfPath, 
                        fileName, 
                        signerEmail, 
                        signerName
                    );
                    
                    logger.info("✓ Document processed for signature successfully!");
                    logger.info("✓ Signature procedure ID: {}", procedureId);
                    logger.info("✓ Signer will receive an email at: {}", signerEmail);
                    
                } catch (Exception e) {
                    logger.error("✗ Failed to process document for signature: {}", e.getMessage());
                    logger.info("This might be due to API configuration issues or network connectivity.");
                }
            } else {
                logger.warn("⚠ Yousign API key not configured. Skipping signature process.");
                logger.info("To test the complete workflow, set the YOUSIGN_API_KEY environment variable or update application.yml");
            }

            // Step 4: Cleanup (optional - keep file for inspection)
            logger.info("Step 3: Cleanup...");
            logger.info("✓ Temporary PDF file kept for inspection: {}", filledPdfPath.toAbsolutePath());
            // Uncomment the next line if you want to delete the temporary file
            // pdfFillingService.deleteTempFile(filledPdfPath);

            logger.info("=== Badasign Test Runner Completed Successfully ===");

        } catch (Exception e) {
            logger.error("=== Badasign Test Runner Failed ===", e);
            throw e;
        }
    }

    /**
     * Prepares test data for PDF filling
     */
    private Map<String, String> prepareTestData() {
        Map<String, String> data = new HashMap<>();
        
        // Personal information
        data.put("firstName", "Jean");
        data.put("lastName", "Dupont");
        data.put("email", "jean.dupont@example.com");
        
        // Contract information
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        data.put("contractNumber", "CTR-" + System.currentTimeMillis());
        data.put("amount", "1500.00 €");
        
        // Additional fields that might be in the PDF template
        data.put("company", "Jixter Solutions");
        data.put("position", "Développeur Senior");
        data.put("startDate", LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        return data;
    }
}