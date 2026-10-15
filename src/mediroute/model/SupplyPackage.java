package mediroute.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a pre-configured medical supply package/kit for Bitmask DP selection.
 */
public class SupplyPackage {
    private final String packageId;
    private final String name;
    private final double cost;
    private final int categoryMask; // Bitmask of covered categories
    private final List<String> coveredCategoryNames;

    public SupplyPackage(String packageId, String name, double cost, int categoryMask, List<String> coveredCategoryNames) {
        this.packageId = Objects.requireNonNull(packageId, "packageId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.cost = cost;
        this.categoryMask = categoryMask;
        this.coveredCategoryNames = coveredCategoryNames == null ? Collections.emptyList() : coveredCategoryNames;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public int getCategoryMask() {
        return categoryMask;
    }

    public List<String> getCoveredCategoryNames() {
        return coveredCategoryNames;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s ($%.2f) - Covers: %s", packageId, name, cost, coveredCategoryNames);
    }
}
