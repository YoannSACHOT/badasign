package fr.jixter.badasign.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class to generate PDF templates with AcroForm fields
 */
public class PdfTemplateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PdfTemplateGenerator.class);

    /**
     * Creates a contract template PDF with AcroForm fields
     *
     * @param outputPath Path where the PDF template will be saved
     * @throws IOException if there's an error creating the PDF
     */
    public static void createContractTemplate(Path outputPath) throws IOException {
        logger.info("Creating contract template PDF at: {}", outputPath);

        PDDocument document = new PDDocument();
        try {
            // Create a page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create content stream for drawing text
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Set font
            PDType1Font font = PDType1Font.HELVETICA;
            PDType1Font boldFont = PDType1Font.HELVETICA_BOLD;
            
            // Draw title
            contentStream.beginText();
            contentStream.setFont(boldFont, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("CONTRAT DE TRAVAIL");
            contentStream.endText();

            // Draw form labels and field placeholders
            float yPosition = 700;
            float labelX = 50;
            float fieldX = 200;
            
            // Personal information section
            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(labelX, yPosition);
            contentStream.showText("INFORMATIONS PERSONNELLES");
            contentStream.endText();
            yPosition -= 30;

            // First Name
            drawFieldLabel(contentStream, font, labelX, yPosition, "Prénom:");
            yPosition -= 30;

            // Last Name
            drawFieldLabel(contentStream, font, labelX, yPosition, "Nom:");
            yPosition -= 30;

            // Email
            drawFieldLabel(contentStream, font, labelX, yPosition, "Email:");
            yPosition -= 50;

            // Contract information section
            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(labelX, yPosition);
            contentStream.showText("INFORMATIONS CONTRACTUELLES");
            contentStream.endText();
            yPosition -= 30;

            // Company
            drawFieldLabel(contentStream, font, labelX, yPosition, "Entreprise:");
            yPosition -= 30;

            // Position
            drawFieldLabel(contentStream, font, labelX, yPosition, "Poste:");
            yPosition -= 30;

            // Amount
            drawFieldLabel(contentStream, font, labelX, yPosition, "Salaire:");
            yPosition -= 30;

            // Start Date
            drawFieldLabel(contentStream, font, labelX, yPosition, "Date de début:");
            yPosition -= 30;

            // Contract Number
            drawFieldLabel(contentStream, font, labelX, yPosition, "Numéro de contrat:");
            yPosition -= 30;

            // Date
            drawFieldLabel(contentStream, font, labelX, yPosition, "Date de signature:");
            yPosition -= 50;

            // Signature section
            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(labelX, yPosition);
            contentStream.showText("SIGNATURE");
            contentStream.endText();
            yPosition -= 30;

            drawFieldLabel(contentStream, font, labelX, yPosition, "Signature de l'employé:");

            contentStream.close();

            // Create AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Create form fields
            createTextField(acroForm, page, "firstName", fieldX, 670, 200, 20);
            createTextField(acroForm, page, "lastName", fieldX, 640, 200, 20);
            createTextField(acroForm, page, "email", fieldX, 610, 200, 20);
            createTextField(acroForm, page, "company", fieldX, 530, 200, 20);
            createTextField(acroForm, page, "position", fieldX, 500, 200, 20);
            createTextField(acroForm, page, "amount", fieldX, 470, 200, 20);
            createTextField(acroForm, page, "startDate", fieldX, 440, 200, 20);
            createTextField(acroForm, page, "contractNumber", fieldX, 410, 200, 20);
            createTextField(acroForm, page, "date", fieldX, 380, 200, 20);
            createTextField(acroForm, page, "signature", fieldX, 300, 200, 40);

            // Save the document
            document.save(outputPath.toFile());
            logger.info("Contract template PDF created successfully at: {}", outputPath);

        } finally {
            document.close();
        }
    }

    /**
     * Draws a field label on the PDF
     */
    private static void drawFieldLabel(PDPageContentStream contentStream, PDType1Font font, 
                                     float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Creates a text field in the AcroForm
     */
    private static void createTextField(PDAcroForm acroForm, PDPage page, String fieldName, 
                                      float x, float y, float width, float height) throws IOException {
        PDTextField textField = new PDTextField(acroForm);
        textField.setPartialName(fieldName);
        
        // Set field appearance
        PDRectangle rect = new PDRectangle(x, y, width, height);
        textField.getWidgets().get(0).setRectangle(rect);
        textField.getWidgets().get(0).setPage(page);
        
        // Add field to form
        acroForm.getFields().add(textField);
        
        // Add widget annotation to page
        page.getAnnotations().add(textField.getWidgets().get(0));
        
        logger.debug("Created text field: {} at position ({}, {})", fieldName, x, y);
    }
}