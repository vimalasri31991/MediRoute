package mediroute.model;

import java.util.Objects;

/**
 * Model representing a healthcare facility in the MediRoute distribution network.
 */
public class Hospital {
    private final String hospitalId;
    private final String name;
    private final String facilityType;
    private final String location;
    private final String district;
    private final String state;
    private final String pincode;
    private final double latitude;
    private final double longitude;
    private final int bedCapacity;
    private final int priority; // 1 = High/Emergency, 2 = Medium, 3 = Standard
    private final String operatingStatus; // OPERATIONAL, EMERGENCY_ONLY, MAINTENANCE

    public Hospital(String hospitalId, String name, String facilityType, String location,
                    String district, String state, String pincode, double latitude,
                    double longitude, int bedCapacity, int priority, String operatingStatus) {
        this.hospitalId = Objects.requireNonNull(hospitalId, "hospitalId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.facilityType = facilityType == null ? "General Hospital" : facilityType.trim();
        this.location = Objects.requireNonNull(location, "location cannot be null").trim();
        this.district = district == null ? "Central" : district.trim();
        this.state = state == null ? "National" : state.trim();
        this.pincode = pincode == null ? "110001" : pincode.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.bedCapacity = Math.max(10, bedCapacity);
        this.priority = priority;
        this.operatingStatus = operatingStatus == null ? "OPERATIONAL" : operatingStatus.trim();
    }

    /**
     * Backward-compatible 4-field constructor.
     */
    public Hospital(String hospitalId, String name, String location, int priority) {
        this(hospitalId, name, "Hospital Facility", location, "Central", "State", "110001",
                28.6139, 77.2090, 250, priority, "OPERATIONAL");
    }

    public String getHospitalId() { return hospitalId; }
    public String getName() { return name; }
    public String getFacilityType() { return facilityType; }
    public String getLocation() { return location; }
    public String getDistrict() { return district; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getBedCapacity() { return bedCapacity; }
    public int getPriority() { return priority; }
    public String getOperatingStatus() { return operatingStatus; }

    public String getPriorityLabel() {
        return switch (priority) {
            case 1 -> "High Priority (Emergency Center)";
            case 2 -> "Medium Priority (Regional Hospital)";
            default -> "Standard Priority (Community Clinic)";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hospital hospital = (Hospital) o;
        return Objects.equals(hospitalId, hospital.hospitalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hospitalId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, P%d)", hospitalId, name, location, priority);
    }
}
