package so.hau.dmsapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import so.hau.dmsapi.domain.DocumentOperation;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessor.class);

    private static final int MAX_WAIT = 16;
    private static final int MIN_WAIT = 3;

    private final ExecutorService executor;

    private final Queue<DocumentOperation> queuedOperations;

    private final Map<String, DocumentOperation> documentOperations;

    public DocumentProcessor(@Value("${application.import-limit:5}") int importLimit) {
        this.executor = Executors.newFixedThreadPool(importLimit);
        this.queuedOperations = new LinkedBlockingQueue<>();
        this.documentOperations = new HashMap<>();
    }

    /**
     * Process a document into the system.
     * This method will add a new DocumentOperation to an internal queue to be processed.
     * Therefore, the method will immediately return a DocumentOperation object, with the status "not_started".
     * Sometime in the near future the operation will be removed from the queue, receive the status "pending" and
     * begin processing. It will later receive the status "completed" regardless if there was an error during the
     * processing or not.
     *
     * @param documentName The name of the document to process
     * @param documentContent The document content
     * @return A DocumentOperation object.
     */
    public DocumentOperation processDocument(String documentName, String documentContent) {
        // In this implementation, we just ignore the document data as we don't do anything with it anyway

        // Create a transaction and add it to the queue
        // at the same time we add it to the "main" operations collection
        DocumentOperation operation = new DocumentOperation();
        queuedOperations.add(operation);
        documentOperations.put(operation.getOperationId(), operation);

        log.info("Queued operation {} for document '{}'",
                operation.getOperationId(),
                documentName);

        // Submit a task to the executor to be executed sometime in the future
        executor.submit(() -> {
            // grab the top item in the queue
            // since the ratio of transactions and tasks are 1:1, we're not bothered to do any error handling here
            DocumentOperation taskOperation = queuedOperations.remove();
            taskOperation.setStatus("pending");
            taskOperation.setMessage("The document is pending processing.");

            // We need to make sure that the operation is changed in the "main" collection
            documentOperations.put(taskOperation.getOperationId(), taskOperation);

            log.info("Starting document processing of operation {}", taskOperation.getOperationId());

            logOperations();

            // Random time between 3 and 15 seconds to wait
            Thread.sleep((new Random().nextInt(MAX_WAIT - MIN_WAIT) + MIN_WAIT) * 1000);

            // "Flip a coin" to see whether the transaction will fail or not, just to log some errors
            boolean failed = new Random().nextBoolean();
            if (failed) {
                taskOperation.setMessage(
                        String.format("The document '%s' with operationId %s failed to process!",
                                documentName,
                                taskOperation.getOperationId()));

                log.error("Operation {} has finished processing, but resulted in an error.",
                        taskOperation.getOperationId());
            } else {
                taskOperation.setMessage(
                        String.format("The document '%s' with operationId %s was processed successfully.",
                                documentName,
                                taskOperation.getOperationId()));

                log.info("Operation {} has finished processing.",
                        taskOperation.getOperationId());
            }

            taskOperation.setStatus("completed");

            // Again, since the operation has changed, we need to update the "main" collection
            documentOperations.put(taskOperation.getOperationId(), taskOperation);

            logOperations();

            return taskOperation;
        });

        return operation;
    }

    private void logOperations() {
        log.info("{} operation(s) in the queue. {} operation(s) pending, {} not started, {} completed.",
                queuedOperations.size(),
                getOperationCount("pending"),
                getOperationCount("not_started"),
                getOperationCount("completed"));
    }

    /**
     * Get the DocumentOperation from the internal storage based on the operationId.
     *
     * @param operationId ID of the operation.
     * @return Optional with DocumentOperation if found, or empty if not found.
     */
    public Optional<DocumentOperation> getOperation(String operationId) {
        return Optional.ofNullable(documentOperations.get(operationId));
    }

    /**
     * Return the number of operations with a certain status.
     *
     * @param status The status to count.
     * @return Number of operations with the specified status.
     */
    public int getOperationCount(String status) {
        return (int) documentOperations
                .values()
                .stream()
                .filter((operation) -> operation.getStatus().equals(status))
                .count();
    }
}
