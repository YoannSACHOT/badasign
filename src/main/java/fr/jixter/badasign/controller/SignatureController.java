package fr.jixter.badasign.controller;

import fr.jixter.badasign.service.YousignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/signature")
public class SignatureController {

    private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

    @Autowired
    private YousignService yousignService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName) {
        
        logger.info("Received request to upload document: {}", fileName);
        
        if (file.isEmpty()) {
            logger.warn("Empty file received");
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File is empty"));
        }
        
        try {
            // Save uploaded file temporarily
            Path tempFile = Files.createTempFile("upload-", ".pdf");
            file.transferTo(tempFile.toFile());
            
            // Upload to Yousign
            String documentId = yousignService.uploadDocument(tempFile, fileName);
            
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
            
            logger.info("Document uploaded successfully with ID: {}", documentId);
            return ResponseEntity.ok(Map.of(
                "documentId", documentId,
                "fileName", fileName,
                "status", "uploaded"
            ));
            
        } catch (IOException e) {
            logger.error("Error uploading document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload document: " + e.getMessage()));
        }
    }

    @PostMapping("/create-procedure")
    public ResponseEntity<Map<String, String>> createSignatureProcedure(
            @RequestBody Map<String, String> request) {
        
        String documentId = request.get("documentId");
        String signerEmail = request.get("signerEmail");
        String signerName = request.get("signerName");
        
        logger.info("Creating signature procedure for document: {} with signer: {}", documentId, signerEmail);
        
        if (documentId == null || signerEmail == null || signerName == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing required fields: documentId, signerEmail, signerName"));
        }
        
        try {
            String procedureId = yousignService.createSignatureProcedure(documentId, signerEmail, signerName);
            
            logger.info("Signature procedure created successfully with ID: {}", procedureId);
            return ResponseEntity.ok(Map.of(
                "procedureId", procedureId,
                "documentId", documentId,
                "signerEmail", signerEmail,
                "status", "created"
            ));
            
        } catch (IOException e) {
            logger.error("Error creating signature procedure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create signature procedure: " + e.getMessage()));
        }
    }

    @PostMapping("/activate/{procedureId}")
    public ResponseEntity<Map<String, String>> activateSignatureProcedure(
            @PathVariable String procedureId) {
        
        logger.info("Activating signature procedure: {}", procedureId);
        
        try {
            yousignService.activateSignatureProcedure(procedureId);
            
            logger.info("Signature procedure activated successfully: {}", procedureId);
            return ResponseEntity.ok(Map.of(
                "procedureId", procedureId,
                "status", "activated"
            ));
            
        } catch (IOException e) {
            logger.error("Error activating signature procedure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to activate signature procedure: " + e.getMessage()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processDocumentForSignature(
            @RequestParam("file") MultipartFile file,
            @RequestParam("signerEmail") String signerEmail,
            @RequestParam("signerName") String signerName) {
        
        logger.info("Processing complete signature workflow for: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File is empty"));
        }
        
        try {
            // Save uploaded file temporarily
            Path tempFile = Files.createTempFile("process-", ".pdf");
            file.transferTo(tempFile.toFile());
            
            // Generate filename with timestamp
            String fileName = "Contract_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            // Process complete workflow
            String procedureId = yousignService.processDocumentForSignature(
                tempFile, fileName, signerEmail, signerName);
            
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
            
            logger.info("Complete signature process finished successfully. Procedure ID: {}", procedureId);
            return ResponseEntity.ok(Map.of(
                "procedureId", procedureId,
                "fileName", fileName,
                "signerEmail", signerEmail,
                "signerName", signerName,
                "status", "processed_and_activated"
            ));
            
        } catch (IOException e) {
            logger.error("Error processing document for signature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process document: " + e.getMessage()));
        }
    }
}