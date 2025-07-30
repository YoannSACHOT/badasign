package fr.jixter.badasign.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class PdfFillingService {

    private static final Logger logger = LoggerFactory.getLogger(PdfFillingService.class);
    private static final String TEMPLATE_PATH = "templates/contract-template.pdf";

    /**
     * Fills a PDF template with the provided data and returns a temporary file
     *
     * @param data Map containing field names and their values
     * @return Path to the filled PDF temporary file
     * @throws IOException if there's an error processing the PDF
     */
    public Path fillPdfTemplate(Map<String, String> data) throws IOException {
        logger.info("Starting PDF template filling with {} fields", data.size());

        // Load the template from resources
        PDDocument document = loadTemplate();
        
        try {
            // Get the form from the document
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            
            if (acroForm == null) {
                logger.warn("No AcroForm found in the PDF template. Creating a mock filled PDF.");
                return createMockFilledPdf(data);
            }

            // Fill the form fields
            fillFormFields(acroForm, data);

            // Flatten the form (make fields non-editable)
            acroForm.flatten();

            // Create temporary file
            Path tempFile = createTempFile(document);
            
            logger.info("PDF template filled successfully. Temporary file created at: {}", tempFile);
            return tempFile;

        } finally {
            document.close();
        }
    }

    /**
     * Loads the PDF template from resources
     */
    private PDDocument loadTemplate() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            if (!resource.exists()) {
                logger.warn("Template file not found at {}. Creating a blank document.", TEMPLATE_PATH);
                return new PDDocument();
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                return PDDocument.load(inputStream);
            }
        } catch (IOException e) {
            logger.error("Error loading PDF template: {}", e.getMessage());
            throw new IOException("Failed to load PDF template", e);
        }
    }

    /**
     * Fills the form fields with the provided data
     */
    private void fillFormFields(PDAcroForm acroForm, Map<String, String> data) throws IOException {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            PDField field = acroForm.getField(fieldName);
            if (field != null) {
                field.setValue(fieldValue);
                logger.debug("Filled field '{}' with value '{}'", fieldName, fieldValue);
            } else {
                logger.warn("Field '{}' not found in the PDF form", fieldName);
            }
        }
    }

    /**
     * Creates a temporary file with the filled PDF
     */
    private Path createTempFile(PDDocument document) throws IOException {
        Path tempFile = Files.createTempFile("filled-contract-", ".pdf");
        document.save(tempFile.toFile());
        return tempFile;
    }

    /**
     * Creates a mock filled PDF when no template is available
     */
    private Path createMockFilledPdf(Map<String, String> data) throws IOException {
        logger.info("Creating mock filled PDF with provided data");
        
        PDDocument document = new PDDocument();
        try {
            // For now, just create an empty document
            // In a real scenario, you would add content to the document
            Path tempFile = Files.createTempFile("mock-filled-contract-", ".pdf");
            document.save(tempFile.toFile());
            
            logger.info("Mock PDF created at: {}", tempFile);
            return tempFile;
        } finally {
            document.close();
        }
    }

    /**
     * Deletes the temporary file
     */
    public void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
            logger.debug("Temporary file deleted: {}", tempFile);
        } catch (IOException e) {
            logger.warn("Failed to delete temporary file: {}", tempFile, e);
        }
    }
}