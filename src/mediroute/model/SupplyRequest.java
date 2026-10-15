package mediroute.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing an active or completed hospital supply request.
 */
public class SupplyRequest {
    public enum RequestStatus {
        PENDING,
        ALLOCATED,
        PARTIALLY_ALLOCATED,
        REJECTED,
        FULFILLED,
        CANCELLED
    }

    private final String requestId;
    private final String hospitalId;
    private final String medicineId;
    private final int quantityRequested;
    private UrgencyLevel urgency;
    private final String notes;
    private final LocalDate requestDate;
    private final LocalDateTime requestTimestamp;

    private int allocatedQuantity;
    private RequestStatus status;

    public SupplyRequest(String requestId, String hospitalId, String medicineId, int quantityRequested,
                         UrgencyLevel urgency, String notes, LocalDate requestDate, RequestStatus status) {
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null").trim();
        this.hospitalId = Objects.requireNonNull(hospitalId, "hospitalId cannot be null").trim();
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        if (quantityRequested <= 0) {
            throw new IllegalArgumentException("Quantity requested must be positive: " + quantityRequested);
        }
        this.quantityRequested = quantityRequested;
        this.urgency = Objects.requireNonNull(urgency, "urgency cannot be null");
        this.notes = notes == null ? "" : notes.trim();
        this.requestDate = requestDate == null ? LocalDate.now() : requestDate;
        this.requestTimestamp = LocalDateTime.now();
        this.allocatedQuantity = (status == RequestStatus.FULFILLED) ? quantityRequested : 0;
        this.status = status == null ? RequestStatus.PENDING : status;
    }

    public SupplyRequest(String requestId, String hospitalId, String medicineId, int quantityRequested,
                         UrgencyLevel urgency, String notes) {
        this(requestId, hospitalId, medicineId, quantityRequested, urgency, notes, LocalDate.now(), RequestStatus.PENDING);
    }

    public String getRequestId() { return requestId; }
    public String getHospitalId() { return hospitalId; }
    public String getMedicineId() { return medicineId; }
    public int getQuantityRequested() { return quantityRequested; }
    public UrgencyLevel getUrgency() { return urgency; }
    public void setUrgency(UrgencyLevel urgency) { this.urgency = urgency; }
    public String getNotes() { return notes; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDateTime getRequestTimestamp() { return requestTimestamp; }

    public int getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(int allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }
    public int getShortage() { return Math.max(0, quantityRequested - allocatedQuantity); }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%s] Hosp: %s | Med: %s | Qty: %d | Allocated: %d | Status: %s | Urgency: %s",
                requestId, hospitalId, medicineId, quantityRequested, allocatedQuantity, status, urgency);
    }
}
