package so.hau.dmsapi.domain;

import java.util.UUID;

public class DocumentOperation {

    private String operationId;

    private String createdTimestamp;

    private String status;

    private String message;

    public DocumentOperation() {
        this.operationId = String.valueOf(UUID.randomUUID());
        this.createdTimestamp = String.valueOf(System.currentTimeMillis());
        this.status = "not_started";
        this.message = "The document processing has not started.";
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentOperation that = (DocumentOperation) o;

        return operationId.equals(that.operationId);
    }

    @Override
    public int hashCode() {
        return operationId.hashCode();
    }
}
