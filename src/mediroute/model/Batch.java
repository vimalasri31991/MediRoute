package mediroute.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing a manufactured batch of a medicine or medical product.
 */
public class Batch {
    private final String batchId;
    private final String medicineId;
    private final String supplierId;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private final int quantityReceived;
    private final double unitCost;
    private final String batchStatus; // RELEASED, QUARANTINED, RECALLED, EXPIRED

    public Batch(String batchId, String medicineId, String supplierId,
                 LocalDate manufactureDate, LocalDate expiryDate,
                 int quantityReceived, double unitCost, String batchStatus) {
        this.batchId = Objects.requireNonNull(batchId, "batchId cannot be null").trim();
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        this.supplierId = Objects.requireNonNull(supplierId, "supplierId cannot be null").trim();
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.quantityReceived = quantityReceived;
        this.unitCost = unitCost;
        this.batchStatus = batchStatus == null ? "RELEASED" : batchStatus.trim();
    }

    public String getBatchId() { return batchId; }
    public String getMedicineId() { return medicineId; }
    public String getSupplierId() { return supplierId; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQuantityReceived() { return quantityReceived; }
    public double getUnitCost() { return unitCost; }
    public String getBatchStatus() { return batchStatus; }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(batchId, batch.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchId);
    }

    @Override
    public String toString() {
        return String.format("Batch [%s] Med: %s | Exp: %s | Status: %s", batchId, medicineId, expiryDate, batchStatus);
    }
}
