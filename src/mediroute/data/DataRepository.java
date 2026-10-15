package mediroute.data;

import mediroute.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Repository responsible for loading, validating, and persisting MediRoute datasets.
 * Integrates all 10 datasets: suppliers, medicines, hospitals, warehouses, batches,
 * inventory, routes, demand_history, requests, and shipments.
 */
public class DataRepository {

    private Path dataDirectory;

    private final Map<String, Supplier> suppliers = new LinkedHashMap<>();
    private final Map<String, Medicine> medicines = new LinkedHashMap<>();
    private final Map<String, Hospital> hospitals = new LinkedHashMap<>();
    private final Map<String, Warehouse> warehouses = new LinkedHashMap<>();
    private final Map<String, Batch> batches = new LinkedHashMap<>();
    // Inventory items keyed by "warehouseId:medicineId:batchId"
    private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();
    private final List<Route> routes = new ArrayList<>();
    private final List<DemandRecord> demandHistory = new ArrayList<>();
    private final List<SupplyRequest> supplyRequests = new ArrayList<>();
    private final List<Shipment> shipments = new ArrayList<>();

    private final List<String> validationErrors = new ArrayList<>();

    public DataRepository() {
        this(resolveDefaultDataDirectory());
    }

    public DataRepository(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Resolves the data directory relative to project root or parent.
     */
    public static Path resolveDefaultDataDirectory() {
        Path direct = Paths.get("data");
        if (Files.exists(direct) && Files.isDirectory(direct)) {
            return direct;
        }
        Path parent = Paths.get("..", "data");
        if (Files.exists(parent) && Files.isDirectory(parent)) {
            return parent;
        }
        return direct;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    /**
     * Loads and validates all CSV files from the configured data directory.
     */
    public boolean loadAll() {
        validationErrors.clear();
        suppliers.clear();
        medicines.clear();
        hospitals.clear();
        warehouses.clear();
        batches.clear();
        inventory.clear();
        routes.clear();
        demandHistory.clear();
        supplyRequests.clear();
        shipments.clear();

        boolean success = true;
        // Optional suppliers file
        loadSuppliers();
        success &= loadMedicines();
        success &= loadHospitals();
        success &= loadWarehouses();
        loadBatches();
        success &= loadInventory();
        success &= loadRoutes();
        success &= loadDemandHistory();
        loadRequests();
        loadShipments();

        return success && validationErrors.isEmpty();
    }

    private void loadSuppliers() {
        Path path = dataDirectory.resolve("suppliers.csv");
        if (!Files.exists(path)) return;
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 8) continue;
                String id = r[0].trim();
                String name = r[1].trim();
                String contact = r[2].trim();
                String phone = r[3].trim();
                String email = r[4].trim();
                String addr = r[5].trim();
                double rating = 4.0;
                try { rating = Double.parseDouble(r[6].trim()); } catch (NumberFormatException ignored) {}
                String status = r[7].trim();
                if (!id.isEmpty()) {
                    suppliers.put(id, new Supplier(id, name, contact, phone, email, addr, rating, status));
                }
            }
        } catch (IOException e) {
            validationErrors.add("Notice: Optional suppliers.csv not loaded: " + e.getMessage());
        }
    }

    private boolean loadMedicines() {
        Path path = dataDirectory.resolve("medicines.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("medicines.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 6) {
                    validationErrors.add("medicines.csv line " + (i + 1) + ": Insufficient columns (" + r.length + " < 6)");
                    continue;
                }
                String id = r[0].trim();
                String name = r[1].trim();
                String code = r[2].trim();
                String category = r[3].trim();

                if (id.isEmpty() || name.isEmpty() || code.isEmpty()) {
                    validationErrors.add("medicines.csv line " + (i + 1) + ": Missing required identifier or name");
                    continue;
                }

                if (r.length >= 16) {
                    // Full 16-column schema
                    String subCat = r[4].trim();
                    String desc = r[5].trim();
                    String form = r[6].trim();
                    String strength = r[7].trim();
                    String uom = r[8].trim();
                    String storage = r[9].trim();
                    boolean coldChain = Boolean.parseBoolean(r[10].trim());
                    boolean controlled = Boolean.parseBoolean(r[11].trim());
                    boolean expTrack = Boolean.parseBoolean(r[12].trim());
                    double cost = 0.0;
                    try { cost = Double.parseDouble(r[13].trim()); } catch (NumberFormatException ignored) {}
                    String supId = r[14].trim();
                    String status = r[15].trim();

                    Medicine med = new Medicine(id, name, code, category, subCat, desc, form, strength,
                            uom, storage, coldChain, controlled, expTrack, cost, supId, status);
                    medicines.put(id, med);
                } else {
                    // Backward-compatible 6-column schema
                    String desc = r[4].trim();
                    String expStr = r[5].trim();
                    LocalDate expiry = null;
                    try { expiry = LocalDate.parse(expStr); } catch (DateTimeParseException ignored) {}
                    Medicine med = new Medicine(id, name, code, category, desc, expiry);
                    medicines.put(id, med);
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read medicines.csv: " + e.getMessage());
            return false;
        }
    }

    private boolean loadHospitals() {
        Path path = dataDirectory.resolve("hospitals.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("hospitals.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 4) {
                    validationErrors.add("hospitals.csv line " + (i + 1) + ": Insufficient columns");
                    continue;
                }
                String id = r[0].trim();
                String name = r[1].trim();

                if (r.length >= 12) {
                    // Full 12-column schema
                    String type = r[2].trim();
                    String loc = r[3].trim();
                    String district = r[4].trim();
                    String state = r[5].trim();
                    String pin = r[6].trim();
                    double lat = 0.0, lon = 0.0;
                    int beds = 100, priority = 3;
                    try { lat = Double.parseDouble(r[7].trim()); } catch (NumberFormatException ignored) {}
                    try { lon = Double.parseDouble(r[8].trim()); } catch (NumberFormatException ignored) {}
                    try { beds = Integer.parseInt(r[9].trim()); } catch (NumberFormatException ignored) {}
                    try { priority = Integer.parseInt(r[10].trim()); } catch (NumberFormatException ignored) {}
                    String status = r[11].trim();

                    if (!id.isEmpty()) {
                        hospitals.put(id, new Hospital(id, name, type, loc, district, state, pin, lat, lon, beds, priority, status));
                    }
                } else {
                    // Backward-compatible 4-column schema
                    String loc = r[2].trim();
                    int priority = 3;
                    try { priority = Integer.parseInt(r[3].trim()); } catch (NumberFormatException ignored) {}
                    if (!id.isEmpty()) {
                        hospitals.put(id, new Hospital(id, name, loc, priority));
                    }
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read hospitals.csv: " + e.getMessage());
            return false;
        }
    }

    private boolean loadWarehouses() {
        Path path = dataDirectory.resolve("warehouses.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("warehouses.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 3) {
                    validationErrors.add("warehouses.csv line " + (i + 1) + ": Insufficient columns");
                    continue;
                }
                String id = r[0].trim();
                String name = r[1].trim();

                if (r.length >= 11) {
                    // Full 11-column schema
                    String type = r[2].trim();
                    String loc = r[3].trim();
                    String dist = r[4].trim();
                    String state = r[5].trim();
                    double lat = 0.0, lon = 0.0;
                    int cap = 50000;
                    boolean cold = Boolean.parseBoolean(r[9].trim());
                    String status = r[10].trim();
                    try { lat = Double.parseDouble(r[6].trim()); } catch (NumberFormatException ignored) {}
                    try { lon = Double.parseDouble(r[7].trim()); } catch (NumberFormatException ignored) {}
                    try { cap = Integer.parseInt(r[8].trim()); } catch (NumberFormatException ignored) {}

                    if (!id.isEmpty()) {
                        warehouses.put(id, new Warehouse(id, name, type, loc, dist, state, lat, lon, cap, cold, status));
                    }
                } else {
                    // Backward-compatible 3-column schema
                    String loc = r[2].trim();
                    if (!id.isEmpty()) {
                        warehouses.put(id, new Warehouse(id, name, loc));
                    }
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read warehouses.csv: " + e.getMessage());
            return false;
        }
    }

    private void loadBatches() {
        Path path = dataDirectory.resolve("batches.csv");
        if (!Files.exists(path)) return;
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 8) continue;
                String bId = r[0].trim();
                String mId = r[1].trim();
                String sId = r[2].trim();
                LocalDate mfg = null, exp = null;
                try { mfg = LocalDate.parse(r[3].trim()); } catch (DateTimeParseException ignored) {}
                try { exp = LocalDate.parse(r[4].trim()); } catch (DateTimeParseException ignored) {}
                int qty = 0;
                double cost = 0.0;
                try { qty = Integer.parseInt(r[5].trim()); } catch (NumberFormatException ignored) {}
                try { cost = Double.parseDouble(r[6].trim()); } catch (NumberFormatException ignored) {}
                String status = r[7].trim();

                Batch batch = new Batch(bId, mId, sId, mfg, exp, qty, cost, status);
                batches.put(bId, batch);

                // Update medicine default expiry date if earlier
                Medicine m = medicines.get(mId);
                if (m != null && exp != null) {
                    if (m.getExpiryDate() == null || exp.isBefore(m.getExpiryDate())) {
                        m.setDefaultExpiryDate(exp);
                    }
                }
            }
        } catch (IOException e) {
            validationErrors.add("Notice: batches.csv not loaded: " + e.getMessage());
        }
    }

    private boolean loadInventory() {
        Path path = dataDirectory.resolve("inventory.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("inventory.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 3) {
                    validationErrors.add("inventory.csv line " + (i + 1) + ": Insufficient columns");
                    continue;
                }
                String wId = r[0].trim();
                String mId = r[1].trim();

                if (!warehouses.containsKey(wId)) {
                    validationErrors.add("inventory.csv line " + (i + 1) + ": Referenced warehouse '" + wId + "' does not exist");
                    continue;
                }
                if (!medicines.containsKey(mId)) {
                    validationErrors.add("inventory.csv line " + (i + 1) + ": Referenced medicine '" + mId + "' does not exist");
                    continue;
                }

                if (r.length >= 7) {
                    // Full 7-column schema: warehouse_id,medicine_id,batch_id,available_quantity,reserved_quantity,reorder_level,last_updated
                    String bId = r[2].trim();
                    int avail = 0, res = 0, reorder = 10;
                    try { avail = Integer.parseInt(r[3].trim()); } catch (NumberFormatException ignored) {}
                    try { res = Integer.parseInt(r[4].trim()); } catch (NumberFormatException ignored) {}
                    try { reorder = Integer.parseInt(r[5].trim()); } catch (NumberFormatException ignored) {}
                    LocalDate date = null;
                    try { date = LocalDate.parse(r[6].trim()); } catch (DateTimeParseException ignored) {}

                    String key = wId + ":" + mId + ":" + bId;
                    inventory.put(key, new InventoryItem(wId, mId, bId, avail, res, reorder, date));
                } else {
                    // Backward-compatible 3-column schema: warehouse_id,medicine_id,available_quantity
                    int qty = 0;
                    try { qty = Integer.parseInt(r[2].trim()); } catch (NumberFormatException ignored) {}
                    String key = wId + ":" + mId + ":DEF-BATCH";
                    inventory.put(key, new InventoryItem(wId, mId, "DEF-BATCH", qty, 0, 10, LocalDate.now()));
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read inventory.csv: " + e.getMessage());
            return false;
        }
    }

    private boolean loadRoutes() {
        Path path = dataDirectory.resolve("routes.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("routes.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 3) {
                    validationErrors.add("routes.csv line " + (i + 1) + ": Insufficient columns");
                    continue;
                }

                if (r.length >= 9) {
                    // Full 9-column schema: route_id,source_id,destination_id,distance_km,estimated_travel_minutes,capacity,transport_mode,route_status,cost_per_km
                    String rId = r[0].trim();
                    String src = r[1].trim();
                    String dst = r[2].trim();
                    double dist = 20.0;
                    int mins = 30, cap = 100;
                    try { dist = Double.parseDouble(r[3].trim()); } catch (NumberFormatException ignored) {}
                    try { mins = Integer.parseInt(r[4].trim()); } catch (NumberFormatException ignored) {}
                    try { cap = Integer.parseInt(r[5].trim()); } catch (NumberFormatException ignored) {}
                    String mode = r[6].trim();
                    String status = r[7].trim();
                    double cost = 2.0;
                    try { cost = Double.parseDouble(r[8].trim()); } catch (NumberFormatException ignored) {}

                    routes.add(new Route(rId, src, dst, dist, mins, cap, mode, status, cost));
                } else {
                    // Backward-compatible 3-column schema: source_id,destination_id,capacity
                    String src = r[0].trim();
                    String dst = r[1].trim();
                    int cap = 0;
                    try { cap = Integer.parseInt(r[2].trim()); } catch (NumberFormatException ignored) {}
                    routes.add(new Route(src, dst, cap));
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read routes.csv: " + e.getMessage());
            return false;
        }
    }

    private boolean loadDemandHistory() {
        Path path = dataDirectory.resolve("demand_history.csv");
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            if (rows.isEmpty()) {
                validationErrors.add("demand_history.csv is empty");
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 5) {
                    validationErrors.add("demand_history.csv line " + (i + 1) + ": Insufficient columns");
                    continue;
                }

                if (r.length >= 8) {
                    // Full 8-column schema: demand_id,date,hospital_id,medicine_id,quantity_requested,quantity_fulfilled,urgency,seasonal_factor
                    String dId = r[0].trim();
                    LocalDate date = LocalDate.parse(r[1].trim());
                    String hId = r[2].trim();
                    String mId = r[3].trim();
                    int reqQty = Integer.parseInt(r[4].trim());
                    int fulQty = Integer.parseInt(r[5].trim());
                    UrgencyLevel urg = UrgencyLevel.fromString(r[6].trim());
                    double season = Double.parseDouble(r[7].trim());

                    demandHistory.add(new DemandRecord(dId, date, hId, mId, reqQty, fulQty, urg, season));
                } else {
                    // Backward-compatible 5-column schema: date,hospital_id,medicine_id,quantity_requested,urgency
                    LocalDate date = LocalDate.parse(r[0].trim());
                    String hId = r[1].trim();
                    String mId = r[2].trim();
                    int qty = Integer.parseInt(r[3].trim());
                    UrgencyLevel urg = UrgencyLevel.fromString(r[4].trim());

                    demandHistory.add(new DemandRecord(date, hId, mId, qty, urg));
                }
            }
            return true;
        } catch (IOException e) {
            validationErrors.add("Failed to read demand_history.csv: " + e.getMessage());
            return false;
        }
    }

    private void loadRequests() {
        Path path = dataDirectory.resolve("requests.csv");
        if (!Files.exists(path)) return;
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 8) continue;
                String reqId = r[0].trim();
                String hId = r[1].trim();
                String mId = r[2].trim();
                int qty = Integer.parseInt(r[3].trim());
                UrgencyLevel urg = UrgencyLevel.fromString(r[4].trim());
                String desc = r[5].trim();
                LocalDate date = null;
                try { date = LocalDate.parse(r[6].trim()); } catch (DateTimeParseException ignored) {}
                String statusStr = r[7].trim().toUpperCase();
                SupplyRequest.RequestStatus status = SupplyRequest.RequestStatus.PENDING;
                try { status = SupplyRequest.RequestStatus.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}

                SupplyRequest req = new SupplyRequest(reqId, hId, mId, qty, urg, desc, date, status);
                supplyRequests.add(req);
            }
        } catch (Exception ignored) {}
    }

    private void loadShipments() {
        Path path = dataDirectory.resolve("shipments.csv");
        if (!Files.exists(path)) return;
        try {
            List<String[]> rows = CsvReader.readCsv(path);
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 8) continue;
                String sId = r[0].trim();
                String reqId = r[1].trim();
                String wId = r[2].trim();
                String mId = r[3].trim();
                int qty = Integer.parseInt(r[4].trim());
                String rId = r[5].trim();
                LocalDate shipDate = null, arrDate = null;
                try { shipDate = LocalDate.parse(r[6].trim()); } catch (DateTimeParseException ignored) {}
                try { arrDate = LocalDate.parse(r[7].trim()); } catch (DateTimeParseException ignored) {}
                String status = (r.length > 8) ? r[8].trim() : "DISPATCHED";

                // Lookup hospital from request if available
                String hId = "H001";
                for (SupplyRequest req : supplyRequests) {
                    if (req.getRequestId().equals(reqId)) {
                        hId = req.getHospitalId();
                        break;
                    }
                }

                Shipment shp = new Shipment(sId, reqId, wId, hId, mId, qty, rId, shipDate, arrDate, status);
                shipments.add(shp);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Returns total available stock for a medicine at a warehouse across all non-expired batches.
     */
    public synchronized int getTotalStock(String warehouseId, String medicineId) {
        int sum = 0;
        for (InventoryItem item : inventory.values()) {
            if (item.getWarehouseId().equals(warehouseId) && item.getMedicineId().equals(medicineId)) {
                Batch b = batches.get(item.getBatchId());
                if (b == null || !b.isExpired()) {
                    sum += item.getAvailableQuantity();
                }
            }
        }
        return sum;
    }

    /**
     * Returns total available stock for a medicine across all warehouses.
     */
    public synchronized int getTotalStock(String medicineId) {
        int sum = 0;
        for (Warehouse w : warehouses.values()) {
            sum += getTotalStock(w.getWarehouseId(), medicineId);
        }
        return sum;
    }

    /**
     * Returns stock per warehouse map for a specific medicine.
     */
    public synchronized Map<String, Integer> getStockByWarehouse(String medicineId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Warehouse w : warehouses.values()) {
            int stock = getTotalStock(w.getWarehouseId(), medicineId);
            if (stock > 0 || warehouses.size() <= 10 || w.getWarehouseId().compareTo("W003") <= 0) {
                map.put(w.getWarehouseId(), stock);
            }
        }
        return map;
    }

    /**
     * Deducts quantity from a warehouse for a medicine using First-Expiry-First-Out (FEFO).
     */
    public synchronized boolean deductStockFEFO(String warehouseId, String medicineId, int amountToDeduct) {
        if (amountToDeduct <= 0) return true;
        if (getTotalStock(warehouseId, medicineId) < amountToDeduct) {
            return false;
        }

        // Gather all eligible inventory items
        List<InventoryItem> items = new ArrayList<>();
        for (InventoryItem it : inventory.values()) {
            if (it.getWarehouseId().equals(warehouseId) && it.getMedicineId().equals(medicineId)) {
                Batch b = batches.get(it.getBatchId());
                if (b == null || !b.isExpired()) {
                    items.add(it);
                }
            }
        }

        // Sort items by batch expiry date ascending (FEFO)
        items.sort((a, b) -> {
            Batch b1 = batches.get(a.getBatchId());
            Batch b2 = batches.get(b.getBatchId());
            LocalDate d1 = (b1 != null) ? b1.getExpiryDate() : LocalDate.MAX;
            LocalDate d2 = (b2 != null) ? b2.getExpiryDate() : LocalDate.MAX;
            return d1.compareTo(d2);
        });

        int remaining = amountToDeduct;
        for (InventoryItem it : items) {
            int canDeduct = Math.min(it.getAvailableQuantity(), remaining);
            it.deductQuantity(canDeduct);
            remaining -= canDeduct;
            if (remaining == 0) break;
        }

        return remaining == 0;
    }

    /**
     * Backward-compatible helper that returns a virtual or primary InventoryItem representing total stock.
     */
    public synchronized InventoryItem getInventoryItem(String warehouseId, String medicineId) {
        int total = getTotalStock(warehouseId, medicineId);
        return new InventoryItem(warehouseId, medicineId, total);
    }

    public synchronized void saveInventoryToCsv() throws IOException {
        Path path = dataDirectory.resolve("inventory.csv");
        String[] header = {"warehouse_id", "medicine_id", "batch_id", "available_quantity", "reserved_quantity", "reorder_level", "last_updated"};
        List<String[]> rows = new ArrayList<>();
        for (InventoryItem item : inventory.values()) {
            rows.add(new String[]{
                    item.getWarehouseId(), item.getMedicineId(), item.getBatchId(),
                    String.valueOf(item.getAvailableQuantity()), String.valueOf(item.getReservedQuantity()),
                    String.valueOf(item.getReorderLevel()), item.getLastUpdated().toString()
            });
        }
        CsvWriter.writeCsv(path, header, rows);
    }

    public synchronized void saveRequestsToCsv() throws IOException {
        Path path = dataDirectory.resolve("requests.csv");
        String[] header = {"request_id", "hospital_id", "medicine_id", "quantity_requested", "urgency", "request_description", "request_date", "status"};
        List<String[]> rows = new ArrayList<>();
        for (SupplyRequest req : supplyRequests) {
            rows.add(new String[]{
                    req.getRequestId(), req.getHospitalId(), req.getMedicineId(),
                    String.valueOf(req.getQuantityRequested()), req.getUrgency().name(),
                    req.getNotes(), req.getRequestDate().toString(), req.getStatus().name()
            });
        }
        CsvWriter.writeCsv(path, header, rows);
    }

    public synchronized void saveShipmentsToCsv() throws IOException {
        Path path = dataDirectory.resolve("shipments.csv");
        String[] header = {"shipment_id", "request_id", "source_warehouse", "medicine_id", "quantity", "route_id", "shipment_date", "estimated_arrival", "status"};
        List<String[]> rows = new ArrayList<>();
        for (Shipment s : shipments) {
            rows.add(new String[]{
                    s.getShipmentId(), s.getRequestId(), s.getSourceWarehouse(),
                    s.getMedicineId(), String.valueOf(s.getQuantity()), s.getRouteId(),
                    s.getShipmentDate().toString(), s.getEstimatedArrival().toString(), s.getStatus()
            });
        }
        CsvWriter.writeCsv(path, header, rows);
    }

    public void exportInventoryToCsv(Path outputPath) throws IOException {
        saveInventoryToCsv();
    }

    public void exportShipmentsToCsv(Path outputPath) throws IOException {
        saveShipmentsToCsv();
    }

    // Getters
    public Map<String, Supplier> getSuppliers() { return suppliers; }
    public Map<String, Medicine> getMedicines() { return medicines; }
    public Map<String, Hospital> getHospitals() { return hospitals; }
    public Map<String, Warehouse> getWarehouses() { return warehouses; }
    public Map<String, Batch> getBatches() { return batches; }
    public Map<String, InventoryItem> getInventory() { return inventory; }
    public List<Route> getRoutes() { return routes; }
    public List<DemandRecord> getDemandHistory() { return demandHistory; }
    public List<SupplyRequest> getSupplyRequests() { return supplyRequests; }
    public List<Shipment> getShipments() { return shipments; }

    public Medicine getMedicine(String medicineId) {
        if (medicineId == null) return null;
        Medicine direct = medicines.get(medicineId);
        if (direct != null) return direct;
        for (Medicine m : medicines.values()) {
            if (m.getMedicineId().equalsIgnoreCase(medicineId) ||
                m.getName().equalsIgnoreCase(medicineId) ||
                m.getProductCode().equalsIgnoreCase(medicineId)) {
                return m;
            }
        }
        return null;
    }

    public Hospital getHospital(String hospitalId) {
        if (hospitalId == null) return null;
        Hospital direct = hospitals.get(hospitalId);
        if (direct != null) return direct;
        for (Hospital h : hospitals.values()) {
            if (h.getHospitalId().equalsIgnoreCase(hospitalId) ||
                h.getName().equalsIgnoreCase(hospitalId)) {
                return h;
            }
        }
        return null;
    }

    public Warehouse getWarehouse(String warehouseId) {
        return warehouses.get(warehouseId);
    }

    public void addSupplyRequest(SupplyRequest request) {
        supplyRequests.add(request);
        try { saveRequestsToCsv(); } catch (IOException ignored) {}
    }

    public void addShipment(Shipment shipment) {
        shipments.add(shipment);
        try { saveShipmentsToCsv(); } catch (IOException ignored) {}
    }
}
