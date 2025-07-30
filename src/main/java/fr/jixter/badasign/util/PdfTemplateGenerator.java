package fr.jixter.badasign.util;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class to generate PDF templates with AcroForm fields */
public class PdfTemplateGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PdfTemplateGenerator.class);

  /**
   * Creates a professional French contract template PDF with AcroForm fields
   *
   * @param outputPath Path where the PDF template will be saved
   * @throws IOException if there's an error creating the PDF
   */
  public static void createContractTemplate(Path outputPath) throws IOException {
    logger.info("Creating enhanced French contract template PDF at: {}", outputPath);

    PDDocument document = new PDDocument();
    try {
      // Create a page
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      // Create content stream for drawing text
      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      // Define fonts
      PDType1Font titleFont = PDType1Font.TIMES_BOLD;
      PDType1Font headerFont = PDType1Font.TIMES_BOLD;
      PDType1Font bodyFont = PDType1Font.TIMES_ROMAN;
      PDType1Font labelFont = PDType1Font.HELVETICA;

      // Define colors
      Color primaryBlue = new Color(25, 55, 109);
      Color accentGold = new Color(184, 134, 11);
      Color textGray = new Color(55, 65, 81);
      Color lightGray = new Color(243, 244, 246);

      // Page dimensions
      float pageWidth = page.getMediaBox().getWidth();
      float pageHeight = page.getMediaBox().getHeight();
      float margin = 50;

      // Draw elegant header background
      contentStream.setNonStrokingColor(primaryBlue);
      contentStream.addRect(0, pageHeight - 120, pageWidth, 120);
      contentStream.fill();

      // Draw accent line
      contentStream.setNonStrokingColor(accentGold);
      contentStream.addRect(0, pageHeight - 125, pageWidth, 5);
      contentStream.fill();

      // Main title
      contentStream.beginText();
      contentStream.setNonStrokingColor(Color.WHITE);
      contentStream.setFont(titleFont, 28);
      float titleWidth = titleFont.getStringWidth("CONTRAT DE TRAVAIL") / 1000 * 28;
      contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, pageHeight - 70);
      contentStream.showText("CONTRAT DE TRAVAIL");
      contentStream.endText();

      // Subtitle
      contentStream.beginText();
      contentStream.setFont(bodyFont, 14);
      String subtitle = "Contrat à Durée Indéterminée";
      float subtitleWidth = bodyFont.getStringWidth(subtitle) / 1000 * 14;
      contentStream.newLineAtOffset((pageWidth - subtitleWidth) / 2, pageHeight - 95);
      contentStream.showText(subtitle);
      contentStream.endText();

      // Reset color for body content
      contentStream.setNonStrokingColor(textGray);

      float yPosition = pageHeight - 160;
      float leftColumn = margin;
      float rightColumn = pageWidth / 2 + 20;
      float fieldWidth = 180;
      float fieldHeight = 22;

      // Article I - Parties contractantes
      yPosition =
          drawSectionHeader(
              contentStream,
              headerFont,
              primaryBlue,
              leftColumn,
              yPosition,
              "ARTICLE I - PARTIES CONTRACTANTES");
      yPosition -= 25;

      contentStream.beginText();
      contentStream.setFont(bodyFont, 11);
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText("Entre les soussignés :");
      contentStream.endText();
      yPosition -= 20;

      contentStream.beginText();
      contentStream.setFont(headerFont, 11);
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText("L'EMPLOYEUR :");
      contentStream.endText();
      yPosition -= 15;

      // Company field
      drawStyledFieldLabel(contentStream, labelFont, leftColumn, yPosition, "Entreprise :");
      yPosition -= 35;

      contentStream.beginText();
      contentStream.setFont(headerFont, 11);
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText("L'EMPLOYÉ :");
      contentStream.endText();
      yPosition -= 15;

      // Personal information in two columns
      float leftFieldY = yPosition;
      drawStyledFieldLabel(contentStream, labelFont, leftColumn, leftFieldY, "Prénom :");
      leftFieldY -= 35;
      drawStyledFieldLabel(contentStream, labelFont, leftColumn, leftFieldY, "Nom de famille :");
      leftFieldY -= 35;
      drawStyledFieldLabel(
          contentStream, labelFont, leftColumn, leftFieldY, "Adresse électronique :");

      // Article II - Objet du contrat
      yPosition -= 120;
      yPosition =
          drawSectionHeader(
              contentStream,
              headerFont,
              primaryBlue,
              leftColumn,
              yPosition,
              "ARTICLE II - OBJET DU CONTRAT");
      yPosition -= 25;

      drawStyledFieldLabel(contentStream, labelFont, leftColumn, yPosition, "Poste occupé :");
      yPosition -= 35;

      drawStyledFieldLabel(
          contentStream, labelFont, leftColumn, yPosition, "Rémunération brute mensuelle :");
      yPosition -= 35;

      drawStyledFieldLabel(
          contentStream, labelFont, leftColumn, yPosition, "Date de prise d'effet :");
      yPosition -= 50;

      // Article III - Dispositions générales
      yPosition =
          drawSectionHeader(
              contentStream,
              headerFont,
              primaryBlue,
              leftColumn,
              yPosition,
              "ARTICLE III - DISPOSITIONS GÉNÉRALES");
      yPosition -= 20;

      contentStream.beginText();
      contentStream.setFont(bodyFont, 10);
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText(
          "Le présent contrat est soumis aux dispositions du Code du travail français.");
      contentStream.endText();
      yPosition -= 15;

      contentStream.beginText();
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText(
          "La période d'essai est fixée conformément aux dispositions légales en vigueur.");
      contentStream.endText();
      yPosition -= 40;

      // Signature section with elegant styling
      yPosition =
          drawSectionHeader(
              contentStream, headerFont, primaryBlue, leftColumn, yPosition, "SIGNATURES");
      yPosition -= 30;

      // Contract details
      drawStyledFieldLabel(contentStream, labelFont, leftColumn, yPosition, "Numéro de contrat :");
      drawStyledFieldLabel(contentStream, labelFont, rightColumn, yPosition, "Date de signature :");
      yPosition -= 50;

      // Signature boxes
      contentStream.beginText();
      contentStream.setFont(labelFont, 10);
      contentStream.newLineAtOffset(leftColumn, yPosition);
      contentStream.showText("Signature de l'employeur");
      contentStream.endText();

      contentStream.beginText();
      contentStream.newLineAtOffset(rightColumn, yPosition);
      contentStream.showText("Signature de l'employé");
      contentStream.endText();
      yPosition -= 15;

      // Draw signature boxes
      contentStream.setStrokingColor(lightGray);
      contentStream.addRect(leftColumn, yPosition - 40, 150, 40);
      contentStream.addRect(rightColumn, yPosition - 40, 150, 40);
      contentStream.stroke();

      contentStream.close();

      // Create AcroForm with enhanced styling
      PDAcroForm acroForm = new PDAcroForm(document);
      document.getDocumentCatalog().setAcroForm(acroForm);

      // Create form fields with proper positioning
      createStyledTextField(
          acroForm, page, "company", leftColumn + 80, pageHeight - 245, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "firstName", leftColumn + 80, pageHeight - 315, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "lastName", leftColumn + 120, pageHeight - 350, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "email", leftColumn + 140, pageHeight - 385, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "position", leftColumn + 100, pageHeight - 465, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "amount", leftColumn + 180, pageHeight - 500, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "startDate", leftColumn + 130, pageHeight - 535, fieldWidth, fieldHeight);
      createStyledTextField(
          acroForm, page, "contractNumber", leftColumn + 130, pageHeight - 645, 120, fieldHeight);
      createStyledTextField(
          acroForm, page, "date", rightColumn + 130, pageHeight - 645, 120, fieldHeight);
      createStyledTextField(acroForm, page, "signature", rightColumn, pageHeight - 735, 150, 40);

      // Save the document
      document.save(outputPath.toFile());
      logger.info("Enhanced French contract template PDF created successfully at: {}", outputPath);

    } finally {
      document.close();
    }
  }

  /** Draws a section header with enhanced styling */
  private static float drawSectionHeader(
      PDPageContentStream contentStream,
      PDType1Font font,
      Color color,
      float x,
      float y,
      String text)
      throws IOException {
    // Draw colored underline
    contentStream.setNonStrokingColor(color);
    contentStream.addRect(x, y - 5, font.getStringWidth(text) / 1000 * 13 + 10, 2);
    contentStream.fill();

    // Draw section title
    contentStream.beginText();
    contentStream.setNonStrokingColor(color);
    contentStream.setFont(font, 13);
    contentStream.newLineAtOffset(x, y);
    contentStream.showText(text);
    contentStream.endText();

    return y;
  }

  /** Draws a styled field label on the PDF */
  private static void drawStyledFieldLabel(
      PDPageContentStream contentStream, PDType1Font font, float x, float y, String text)
      throws IOException {
    contentStream.beginText();
    contentStream.setNonStrokingColor(new Color(55, 65, 81));
    contentStream.setFont(font, 11);
    contentStream.newLineAtOffset(x, y);
    contentStream.showText(text);
    contentStream.endText();
  }

  /** Creates a styled text field in the AcroForm with enhanced appearance */
  private static void createStyledTextField(
      PDAcroForm acroForm,
      PDPage page,
      String fieldName,
      float x,
      float y,
      float width,
      float height)
      throws IOException {
    PDTextField textField = new PDTextField(acroForm);
    textField.setPartialName(fieldName);

    // Set field appearance
    PDRectangle rect = new PDRectangle(x, y, width, height);
    textField.getWidgets().get(0).setRectangle(rect);
    textField.getWidgets().get(0).setPage(page);

    // Enhanced styling for form fields
    try {
      // Set default appearance with professional styling
      textField.setDefaultAppearance("/Helv 11 Tf 0.2 0.3 0.4 rg");

      // Set field properties for better user experience
      if ("signature".equals(fieldName)) {
        // Special styling for signature field
        textField.setMultiline(true);
        textField.setDefaultAppearance("/Helv 10 Tf 0.1 0.2 0.5 rg");
      } else if ("email".equals(fieldName)) {
        // Email field validation could be added here
        textField.setDefaultAppearance("/Helv 10 Tf 0.2 0.3 0.4 rg");
      } else if ("amount".equals(fieldName)) {
        // Currency field styling
        textField.setDefaultAppearance("/Helv 11 Tf 0.1 0.4 0.1 rg");
      }

      // Set field as required for important fields
      if ("firstName".equals(fieldName)
          || "lastName".equals(fieldName)
          || "email".equals(fieldName)
          || "signature".equals(fieldName)) {
        textField.setRequired(true);
      }

    } catch (Exception e) {
      logger.warn("Could not set enhanced styling for field {}: {}", fieldName, e.getMessage());
      // Fallback to basic styling
      textField.setDefaultAppearance("/Helv 11 Tf 0 g");
    }

    // Add field to form
    acroForm.getFields().add(textField);

    // Add widget annotation to page
    page.getAnnotations().add(textField.getWidgets().get(0));

    logger.debug("Created styled text field: {} at position ({}, {})", fieldName, x, y);
  }
}
