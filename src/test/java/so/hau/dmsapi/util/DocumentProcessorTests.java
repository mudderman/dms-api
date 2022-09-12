package so.hau.dmsapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import so.hau.dmsapi.domain.DocumentOperation;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentProcessorTests {

    private DocumentProcessor documentProcessor;

    @BeforeEach
    public void init() {
        documentProcessor = new DocumentProcessor(5);
    }

    @Test
    public void testProcessOneDocument() {
        DocumentOperation operation =
                documentProcessor.processDocument("Test document", "Test content");
        assertEquals("not_started", operation.getStatus());
    }

    @Test
    public void testProcessSixDocuments() {
        for (int i = 1; i < 11; i++) {
            DocumentOperation operation =
                    documentProcessor.processDocument("Test document " + i, "Test content");
            assertEquals("not_started", operation.getStatus());
        }

        // There should be 5 operations that are "pending" and 1 that is "not_started"
        assertEquals(5, documentProcessor.getOperationCount("pending"));
        assertEquals(5, documentProcessor.getOperationCount("not_started"));
    }

    @Test
    public void testPollStatus() {
        DocumentOperation operation =
                documentProcessor.processDocument("Test document", "Test content");
        assertEquals("not_started", operation.getStatus());

        Optional<DocumentOperation> getOperation = documentProcessor.getOperation(operation.getOperationId());
        assertTrue(getOperation.isPresent());

        operation = getOperation.get();
        assertEquals("pending", operation.getStatus());
    }

    @Test
    public void testPollStatusUntilCompleted() throws Exception {
        DocumentOperation operation =
                documentProcessor.processDocument("Test document", "Test content");
        assertEquals("not_started", operation.getStatus());

        // Since the DocumentProcessor.processDocument doesn't return the Future it queues
        // we have to poll until the operation is complete
        while (!operation.getStatus().equals("completed")) {
            Optional<DocumentOperation> getOperation = documentProcessor.getOperation(operation.getOperationId());
            assertTrue(getOperation.isPresent());
            operation = getOperation.get();
            Thread.sleep(1000);
        }

        assertEquals("completed", operation.getStatus());
    }
}
