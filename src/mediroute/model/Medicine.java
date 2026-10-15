package mediroute.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing a medicine or medical equipment item in the catalogue.
 * Upgraded with comprehensive supply-chain, regulatory, and storage attributes.
 */
public class Medicine {
    private final String medicineId;
    private final String name;
    private final String productCode;
    private final String category;
    private final String subCategory;
    private final String description;
    private final String dosageForm;
    private final String strength;
    private final String unitOfMeasure;
    private final String storageType;
    private final boolean coldChainRequired;
    private final boolean controlledItem;
    private final boolean expiryTracking;
    private final double unitCost;
    private final String supplierId;
    private final String status; // ACTIVE, DISCONTINUED, RESTRICTED
    private LocalDate defaultExpiryDate; // Earliest batch expiry or catalog default

    public Medicine(String medicineId, String name, String productCode, String category,
                    String subCategory, String description, String dosageForm, String strength,
                    String unitOfMeasure, String storageType, boolean coldChainRequired,
                    boolean controlledItem, boolean expiryTracking, double unitCost,
                    String supplierId, String status) {
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.productCode = Objects.requireNonNull(productCode, "productCode cannot be null").trim();
        this.category = Objects.requireNonNull(category, "category cannot be null").trim();
        this.subCategory = subCategory == null ? "" : subCategory.trim();
        this.description = Objects.requireNonNull(description, "description cannot be null").trim();
        this.dosageForm = dosageForm == null ? "N/A" : dosageForm.trim();
        this.strength = strength == null ? "N/A" : strength.trim();
        this.unitOfMeasure = unitOfMeasure == null ? "Unit" : unitOfMeasure.trim();
        this.storageType = storageType == null ? "Ambient" : storageType.trim();
        this.coldChainRequired = coldChainRequired;
        this.controlledItem = controlledItem;
        this.expiryTracking = expiryTracking;
        this.unitCost = Math.max(0.0, unitCost);
        this.supplierId = supplierId == null ? "" : supplierId.trim();
        this.status = status == null ? "ACTIVE" : status.trim();
        this.defaultExpiryDate = LocalDate.now().plusYears(2);
    }

    /**
     * Backward-compatible constructor for standard 6-field catalog entries.
     */
    public Medicine(String medicineId, String name, String productCode, String category,
                    String description, LocalDate expiryDate) {
        this(medicineId, name, productCode, category, "General", description, "Standard",
                "N/A", "Unit", "Ambient", false, false, true, 10.0, "SUP001", "ACTIVE");
        this.defaultExpiryDate = expiryDate;
    }

    public String getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public String getProductCode() { return productCode; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getDescription() { return description; }
    public String getDosageForm() { return dosageForm; }
    public String getStrength() { return strength; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public String getStorageType() { return storageType; }
    public boolean isColdChainRequired() { return coldChainRequired; }
    public boolean isControlledItem() { return controlledItem; }
    public boolean isExpiryTracking() { return expiryTracking; }
    public double getUnitCost() { return unitCost; }
    public String getSupplierId() { return supplierId; }
    public String getStatus() { return status; }

    public LocalDate getExpiryDate() { return defaultExpiryDate; }
    public void setDefaultExpiryDate(LocalDate date) { this.defaultExpiryDate = date; }

    public boolean isExpired() {
        return expiryTracking && defaultExpiryDate != null && defaultExpiryDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicine medicine = (Medicine) o;
        return Objects.equals(medicineId, medicine.medicineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicineId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s | Cost: $%.2f",
                medicineId, name, productCode, category, unitCost);
    }
}
