package mediroute.service;

import mediroute.data.DataRepository;
import mediroute.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Service for querying and updating medical inventory records across warehouses.
 */
public class InventoryService {

    private final DataRepository repository;

    public InventoryService(DataRepository repository) {
        this.repository = repository;
    }

    public static class InventorySummaryRow {
        public final String warehouseId;
        public final String warehouseName;
        public final String medicineId;
        public final String medicineName;
        public final int availableQuantity;

        public InventorySummaryRow(String warehouseId, String warehouseName, String medicineId,
                                   String medicineName, int availableQuantity) {
            this.warehouseId = warehouseId;
            this.warehouseName = warehouseName;
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.availableQuantity = availableQuantity;
        }
    }

    public List<InventorySummaryRow> getDetailedInventory() {
        List<InventorySummaryRow> rows = new ArrayList<>();
        for (Warehouse w : repository.getWarehouses().values()) {
            for (Medicine m : repository.getMedicines().values()) {
                int stock = repository.getTotalStock(w.getWarehouseId(), m.getMedicineId());
                if (stock > 0) {
                    rows.add(new InventorySummaryRow(w.getWarehouseId(), w.getName(), m.getMedicineId(), m.getName(), stock));
                }
            }
        }
        return rows;
    }

    public Map<String, Integer> getStockByMedicineForWarehouse(String warehouseId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Medicine m : repository.getMedicines().values()) {
            int stock = repository.getTotalStock(warehouseId, m.getMedicineId());
            if (stock > 0) {
                map.put(m.getName(), stock);
            }
        }
        return map;
    }

    public Map<String, Integer> getTotalStockPerMedicine() {
        Map<String, Integer> stockMap = new LinkedHashMap<>();
        for (Medicine m : repository.getMedicines().values()) {
            int total = repository.getTotalStock(m.getMedicineId());
            stockMap.put(m.getMedicineId(), total);
        }
        return stockMap;
    }

    public Map<String, Integer> getTotalStockPerWarehouse() {
        Map<String, Integer> stockMap = new LinkedHashMap<>();
        for (Warehouse w : repository.getWarehouses().values()) {
            int total = 0;
            for (Medicine m : repository.getMedicines().values()) {
                total += repository.getTotalStock(w.getWarehouseId(), m.getMedicineId());
            }
            stockMap.put(w.getWarehouseId(), total);
        }
        return stockMap;
    }

    public void exportInventory(Path outputPath) throws IOException {
        repository.saveInventoryToCsv();
    }

    public void exportShipments(Path outputPath) throws IOException {
        repository.saveShipmentsToCsv();
    }
}
