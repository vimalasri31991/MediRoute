package mediroute.model;

import java.util.Objects;

/**
 * Model representing a transportation link between nodes in the distribution network.
 */
public class Route {
    private final String routeId;
    private final String sourceId;
    private final String destinationId;
    private final double distanceKm;
    private final int estimatedTravelMinutes;
    private final int capacity;
    private final String transportMode; // REFRIGERATED_VAN, CARGO_TRUCK, EXPRESS_COURIER, AIR_FREIGHT
    private final String routeStatus; // ACTIVE, RESTRICTED, CLOSED
    private final double costPerKm;

    public Route(String routeId, String sourceId, String destinationId, double distanceKm,
                 int estimatedTravelMinutes, int capacity, String transportMode,
                 String routeStatus, double costPerKm) {
        this.routeId = routeId == null ? "R-" + sourceId + "-" + destinationId : routeId.trim();
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId cannot be null").trim();
        this.destinationId = Objects.requireNonNull(destinationId, "destinationId cannot be null").trim();
        this.distanceKm = Math.max(1.0, distanceKm);
        this.estimatedTravelMinutes = Math.max(1, estimatedTravelMinutes);
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative: " + capacity);
        }
        this.capacity = capacity;
        this.transportMode = transportMode == null ? "CARGO_TRUCK" : transportMode.trim();
        this.routeStatus = routeStatus == null ? "ACTIVE" : routeStatus.trim();
        this.costPerKm = Math.max(0.5, costPerKm);
    }

    /**
     * Backward-compatible 3-field constructor.
     */
    public Route(String sourceId, String destinationId, int capacity) {
        this("R-" + sourceId + "-" + destinationId, sourceId, destinationId, 50.0, 60, capacity, "CARGO_TRUCK", "ACTIVE", 2.0);
    }

    public String getRouteId() { return routeId; }
    public String getSourceId() { return sourceId; }
    public String getDestinationId() { return destinationId; }
    public double getDistanceKm() { return distanceKm; }
    public int getEstimatedTravelMinutes() { return estimatedTravelMinutes; }
    public int getCapacity() { return capacity; }
    public String getTransportMode() { return transportMode; }
    public String getRouteStatus() { return routeStatus; }
    public double getCostPerKm() { return costPerKm; }

    public boolean isOperable() {
        return "ACTIVE".equalsIgnoreCase(routeStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(sourceId, route.sourceId) &&
               Objects.equals(destinationId, route.destinationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, destinationId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s (Cap: %d, Dist: %.1f km, Status: %s)",
                routeId, sourceId, destinationId, capacity, distanceKm, routeStatus);
    }
}
