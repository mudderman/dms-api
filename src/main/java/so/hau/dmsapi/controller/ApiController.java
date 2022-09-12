package so.hau.dmsapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import so.hau.dmsapi.domain.DocumentOperation;
import so.hau.dmsapi.util.DocumentProcessor;

import java.io.IOException;
import java.util.Optional;


@RestController
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private final DocumentProcessor documentProcessor;

    @Autowired
    public ApiController(DocumentProcessor documentProcessor) {
        this.documentProcessor = documentProcessor;
    }

    @PostMapping("/import")
    @CrossOrigin(origins = "*")
    public ResponseEntity<DocumentOperation> importDocument(
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file) {

        String documentContent;
        try {
            documentContent = new String(file.getBytes());
        } catch (IOException e) {
            log.error("Error processing the uploaded file", e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(documentProcessor.processDocument(documentName, documentContent));
    }

    @GetMapping("/status/{operationId}/**")
    @CrossOrigin(origins = "*")
    public ResponseEntity<DocumentOperation> getOperation(@PathVariable("operationId") String operationId) {
        Optional<DocumentOperation> operation = documentProcessor.getOperation(operationId);
        return operation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
