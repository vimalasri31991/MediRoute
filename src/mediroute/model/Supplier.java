package mediroute.model;

import java.util.Objects;

/**
 * Model representing an authorized pharmaceutical/medical equipment supplier.
 */
public class Supplier {
    private final String supplierId;
    private final String supplierName;
    private final String contactPerson;
    private final String phone;
    private final String email;
    private final String address;
    private final double rating;
    private final String status; // ACTIVE, AUDIT_PENDING, INACTIVE

    public Supplier(String supplierId, String supplierName, String contactPerson,
                    String phone, String email, String address, double rating, String status) {
        this.supplierId = Objects.requireNonNull(supplierId, "supplierId cannot be null").trim();
        this.supplierName = Objects.requireNonNull(supplierName, "supplierName cannot be null").trim();
        this.contactPerson = contactPerson == null ? "" : contactPerson.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.email = email == null ? "" : email.trim();
        this.address = address == null ? "" : address.trim();
        this.rating = rating;
        this.status = status == null ? "ACTIVE" : status.trim();
    }

    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public String getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(supplierId, supplier.supplierId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Rating: %.1f, Status: %s)", supplierId, supplierName, rating, status);
    }
}
