package fr.jixter.badasign.config;

import fr.jixter.badasign.util.PdfTemplateGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Initializes PDF templates on application startup */
@Component
public class PdfTemplateInitializer implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(PdfTemplateInitializer.class);
  private static final String TEMPLATE_FILENAME = "contract-template.pdf";
  private static final String TEMPLATES_DIR = "src/main/resources/templates";

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Initializing PDF Templates ===");

    try {
      // Check if template already exists
      ClassPathResource resource = new ClassPathResource("templates/" + TEMPLATE_FILENAME);
      if (resource.exists()) {
        logger.info("✓ PDF template already exists: {}", TEMPLATE_FILENAME);
        return;
      }

      // Create templates directory if it doesn't exist
      Path templatesDir = Paths.get(TEMPLATES_DIR);
      if (!Files.exists(templatesDir)) {
        Files.createDirectories(templatesDir);
        logger.info("Created templates directory: {}", templatesDir);
      }

      // Generate the PDF template
      Path templatePath = templatesDir.resolve(TEMPLATE_FILENAME);
      PdfTemplateGenerator.createContractTemplate(templatePath);

      logger.info("✓ PDF template created successfully: {}", templatePath.toAbsolutePath());
      logger.info("=== PDF Template Initialization Complete ===");

    } catch (final IOException e) {
      logger.error("✗ Failed to create PDF template: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to initialize PDF template", e);
    }
  }
}
