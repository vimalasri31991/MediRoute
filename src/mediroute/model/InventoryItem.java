package mediroute.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing an inventory item: stock of a medicine batch stored at a warehouse.
 */
public class InventoryItem {
    private final String warehouseId;
    private final String medicineId;
    private final String batchId;
    private int availableQuantity;
    private int reservedQuantity;
    private int reorderLevel;
    private LocalDate lastUpdated;

    public InventoryItem(String warehouseId, String medicineId, String batchId,
                         int availableQuantity, int reservedQuantity, int reorderLevel, LocalDate lastUpdated) {
        this.warehouseId = Objects.requireNonNull(warehouseId, "warehouseId cannot be null").trim();
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        this.batchId = batchId == null ? "DEF-BATCH" : batchId.trim();
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative: " + availableQuantity);
        }
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = Math.max(0, reservedQuantity);
        this.reorderLevel = Math.max(0, reorderLevel);
        this.lastUpdated = lastUpdated == null ? LocalDate.now() : lastUpdated;
    }

    /**
     * Backward-compatible 3-field constructor.
     */
    public InventoryItem(String warehouseId, String medicineId, int availableQuantity) {
        this(warehouseId, medicineId, "DEF-BATCH", availableQuantity, 0, 50, LocalDate.now());
    }

    public String getWarehouseId() { return warehouseId; }
    public String getMedicineId() { return medicineId; }
    public String getBatchId() { return batchId; }
    public synchronized int getAvailableQuantity() { return availableQuantity; }
    public synchronized int getReservedQuantity() { return reservedQuantity; }
    public int getReorderLevel() { return reorderLevel; }
    public LocalDate getLastUpdated() { return lastUpdated; }

    public synchronized void deductQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deduction amount cannot be negative: " + amount);
        }
        if (amount > availableQuantity) {
            throw new IllegalStateException(String.format("Insufficient stock at warehouse %s for medicine %s. Requested: %d, Available: %d",
                    warehouseId, medicineId, amount, availableQuantity));
        }
        this.availableQuantity -= amount;
        this.lastUpdated = LocalDate.now();
    }

    public synchronized void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Addition amount cannot be negative: " + amount);
        }
        this.availableQuantity += amount;
        this.lastUpdated = LocalDate.now();
    }

    public synchronized void reserveQuantity(int amount) {
        if (amount < 0 || amount > availableQuantity) {
            throw new IllegalArgumentException("Cannot reserve " + amount + " units when " + availableQuantity + " available.");
        }
        this.availableQuantity -= amount;
        this.reservedQuantity += amount;
        this.lastUpdated = LocalDate.now();
    }

    public synchronized void releaseReserved(int amount) {
        int release = Math.min(amount, reservedQuantity);
        this.reservedQuantity -= release;
        this.availableQuantity += release;
        this.lastUpdated = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(warehouseId, that.warehouseId) &&
               Objects.equals(medicineId, that.medicineId) &&
               Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, medicineId, batchId);
    }

    @Override
    public String toString() {
        return String.format("[Wh: %s | Med: %s | Batch: %s | Qty: %d | Res: %d]",
                warehouseId, medicineId, batchId, availableQuantity, reservedQuantity);
    }
}
