package mediroute.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing a historical demand record in the regional health system.
 */
public class DemandRecord {
    private final String demandId;
    private final LocalDate date;
    private final String hospitalId;
    private final String medicineId;
    private final int quantityRequested;
    private final int quantityFulfilled;
    private final UrgencyLevel urgency;
    private final double seasonalFactor;

    public DemandRecord(String demandId, LocalDate date, String hospitalId, String medicineId,
                        int quantityRequested, int quantityFulfilled, UrgencyLevel urgency, double seasonalFactor) {
        this.demandId = demandId == null ? "DEM-" + System.nanoTime() : demandId.trim();
        this.date = Objects.requireNonNull(date, "date cannot be null");
        this.hospitalId = Objects.requireNonNull(hospitalId, "hospitalId cannot be null").trim();
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        if (quantityRequested <= 0) {
            throw new IllegalArgumentException("Quantity requested must be positive: " + quantityRequested);
        }
        this.quantityRequested = quantityRequested;
        this.quantityFulfilled = Math.max(0, quantityFulfilled);
        this.urgency = Objects.requireNonNull(urgency, "urgency cannot be null");
        this.seasonalFactor = Math.max(0.1, seasonalFactor);
    }

    /**
     * Backward-compatible 5-field constructor.
     */
    public DemandRecord(LocalDate date, String hospitalId, String medicineId, int quantityRequested, UrgencyLevel urgency) {
        this("DEM-HIST", date, hospitalId, medicineId, quantityRequested, quantityRequested, urgency, 1.0);
    }

    public String getDemandId() { return demandId; }
    public LocalDate getDate() { return date; }
    public String getHospitalId() { return hospitalId; }
    public String getMedicineId() { return medicineId; }
    public int getQuantityRequested() { return quantityRequested; }
    public int getQuantityFulfilled() { return quantityFulfilled; }
    public UrgencyLevel getUrgency() { return urgency; }
    public double getSeasonalFactor() { return seasonalFactor; }

    @Override
    public String toString() {
        return String.format("[%s | %s] Hosp: %s | Med: %s | Req: %d | Fulfilled: %d | %s",
                demandId, date, hospitalId, medicineId, quantityRequested, quantityFulfilled, urgency);
    }
}
