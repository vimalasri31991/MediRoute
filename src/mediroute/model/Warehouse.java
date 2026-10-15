package mediroute.model;

import java.util.Objects;

/**
 * Model representing a medical warehouse or logistics distribution hub.
 */
public class Warehouse {
    private final String warehouseId;
    private final String name;
    private final String warehouseType; // Central, Regional, Cold Chain Storage, Emergency Depot
    private final String location;
    private final String district;
    private final String state;
    private final double latitude;
    private final double longitude;
    private final int storageCapacity;
    private final boolean coldChainCapable;
    private final String operatingStatus; // OPERATIONAL, EXPANDING, LIMITED

    public Warehouse(String warehouseId, String name, String warehouseType, String location,
                     String district, String state, double latitude, double longitude,
                     int storageCapacity, boolean coldChainCapable, String operatingStatus) {
        this.warehouseId = Objects.requireNonNull(warehouseId, "warehouseId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.warehouseType = warehouseType == null ? "Regional Depot" : warehouseType.trim();
        this.location = Objects.requireNonNull(location, "location cannot be null").trim();
        this.district = district == null ? "Industrial Corridor" : district.trim();
        this.state = state == null ? "State" : state.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.storageCapacity = Math.max(100, storageCapacity);
        this.coldChainCapable = coldChainCapable;
        this.operatingStatus = operatingStatus == null ? "OPERATIONAL" : operatingStatus.trim();
    }

    /**
     * Backward-compatible 3-field constructor.
     */
    public Warehouse(String warehouseId, String name, String location) {
        this(warehouseId, name, "Depot", location, "Central", "State", 28.5, 77.2, 50000, true, "OPERATIONAL");
    }

    public String getWarehouseId() { return warehouseId; }
    public String getName() { return name; }
    public String getWarehouseType() { return warehouseType; }
    public String getLocation() { return location; }
    public String getDistrict() { return district; }
    public String getState() { return state; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getStorageCapacity() { return storageCapacity; }
    public boolean isColdChainCapable() { return coldChainCapable; }
    public String getOperatingStatus() { return operatingStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(warehouseId, warehouse.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, ColdChain: %s)", warehouseId, name, location, coldChainCapable ? "YES" : "NO");
    }
}
