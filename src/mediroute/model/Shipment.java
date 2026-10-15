package mediroute.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing a physical or simulated shipment of allocated medicine from a warehouse to a hospital.
 */
public class Shipment {
    private final String shipmentId;
    private final String requestId;
    private final String warehouseId; // source_warehouse
    private final String hospitalId;
    private final String medicineId;
    private final int quantity;
    private final String routeId;
    private final LocalDate shipmentDate;
    private final LocalDate estimatedArrival;
    private final LocalDateTime dispatchTime;
    private final String status; // IN_TRANSIT, DELIVERED, DISPATCHED, DELAYED

    public Shipment(String shipmentId, String requestId, String warehouseId, String hospitalId,
                    String medicineId, int quantity, String routeId, LocalDate shipmentDate,
                    LocalDate estimatedArrival, String status) {
        this.shipmentId = Objects.requireNonNull(shipmentId, "shipmentId cannot be null").trim();
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null").trim();
        this.warehouseId = Objects.requireNonNull(warehouseId, "warehouseId cannot be null").trim();
        this.hospitalId = hospitalId == null ? "H001" : hospitalId.trim();
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId cannot be null").trim();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Shipment quantity must be positive: " + quantity);
        }
        this.quantity = quantity;
        this.routeId = routeId == null ? "R-" + warehouseId + "-" + hospitalId : routeId.trim();
        this.shipmentDate = shipmentDate == null ? LocalDate.now() : shipmentDate;
        this.estimatedArrival = estimatedArrival == null ? this.shipmentDate.plusDays(1) : estimatedArrival;
        this.dispatchTime = LocalDateTime.now();
        this.status = status == null ? "DISPATCHED" : status;
    }

    /**
     * Backward-compatible 7-field constructor.
     */
    public Shipment(String shipmentId, String requestId, String warehouseId, String hospitalId,
                    String medicineId, int quantity, String status) {
        this(shipmentId, requestId, warehouseId, hospitalId, medicineId, quantity,
                "R-" + warehouseId + "-" + hospitalId, LocalDate.now(), LocalDate.now().plusDays(1), status);
    }

    public String getShipmentId() { return shipmentId; }
    public String getRequestId() { return requestId; }
    public String getWarehouseId() { return warehouseId; }
    public String getSourceWarehouse() { return warehouseId; }
    public String getHospitalId() { return hospitalId; }
    public String getMedicineId() { return medicineId; }
    public int getQuantity() { return quantity; }
    public String getRouteId() { return routeId; }
    public LocalDate getShipmentDate() { return shipmentDate; }
    public LocalDate getEstimatedArrival() { return estimatedArrival; }
    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("[%s] Req: %s | Wh: %s -> Hosp: %s | Med: %s | Qty: %d | Route: %s | Status: %s",
                shipmentId, requestId, warehouseId, hospitalId, medicineId, quantity, routeId, status);
    }
}
