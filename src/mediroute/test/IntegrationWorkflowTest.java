package mediroute.test;

import mediroute.data.DataRepository;
import mediroute.model.*;
import mediroute.service.*;

import java.nio.file.Path;

public class IntegrationWorkflowTest {

    public static void run() {
        System.out.println("-> Running Integrated Workflow Tests...");

        Path dataDir = DataRepository.resolveDefaultDataDirectory();
        DataRepository repo = new DataRepository(dataDir);
        boolean loaded = repo.loadAll();
        TestRunner.assertTrue(loaded, "DataRepository should load all CSV files without validation errors");
        TestRunner.assertTrue(!repo.getMedicines().isEmpty(), "Medicines dataset must not be empty");
        TestRunner.assertTrue(!repo.getHospitals().isEmpty(), "Hospitals dataset must not be empty");
        TestRunner.assertTrue(!repo.getWarehouses().isEmpty(), "Warehouses dataset must not be empty");
        TestRunner.assertTrue(!repo.getRoutes().isEmpty(), "Routes dataset must not be empty");
        TestRunner.assertTrue(!repo.getDemandHistory().isEmpty(), "Demand history dataset must not be empty");

        // 1. Search Service Workflow
        MedicineSearchService searchService = new MedicineSearchService(repo);
        var searchResults = searchService.search("Paracetamol");
        TestRunner.assertTrue(!searchResults.isEmpty(), "Search for 'Paracetamol' should return matching records");
        TestRunner.assertTrue(searchResults.get(0).getMedicine().getMedicineId().equals("M001"),
                "First result should be M001");

        // 2. Request Service & Emergency Keyword Detection Workflow
        RequestService reqService = new RequestService(repo);
        var val = reqService.validateRequest("H001", "M004", 25, UrgencyLevel.NORMAL,
                "Immediate trauma ICU oxygen resuscitation required");
        TestRunner.assertTrue(val.isValid(), "Valid request parameters must pass validation");
        TestRunner.assertTrue(val.getRecommendedUrgency() == UrgencyLevel.CRITICAL,
                "Emergency terms ('trauma', 'resuscitation', 'oxygen') must elevate urgency to CRITICAL");

        SupplyRequest req = reqService.createRequest("H001", "M004", 25, val.getRecommendedUrgency(), "ICU emergency");
        TestRunner.assertTrue(req.getStatus() == SupplyRequest.RequestStatus.PENDING,
                "Newly registered request status must be PENDING");

        // 3. Allocation Workflow with Network Flow
        AllocationService allocService = new AllocationService(repo);

        // Record stock before allocation
        int initialStockW1 = repo.getInventoryItem("W001", "M004").getAvailableQuantity();

        AllocationService.AllocationPlan plan = allocService.computeAllocation(req);
        TestRunner.assertTrue(plan.getTotalAllocated() > 0, "Allocation flow must find feasible routes for M004 to H001");

        // Verify stock has NOT changed before confirmation
        int stockBeforeConfirm = repo.getInventoryItem("W001", "M004").getAvailableQuantity();
        TestRunner.assertTrue(initialStockW1 == stockBeforeConfirm,
                "Inventory stock must NOT be deducted before explicit confirmation");

        // Confirm allocation
        int initialShipments = repo.getShipments().size();
        boolean confirmed = allocService.confirmAndApplyAllocation(plan);
        TestRunner.assertTrue(confirmed, "Allocation confirmation must succeed");

        // Verify stock is now deducted
        int stockAfterConfirm = repo.getInventoryItem("W001", "M004").getAvailableQuantity();
        TestRunner.assertTrue(stockAfterConfirm < initialStockW1 || plan.getWarehouseAllocations().get("W001") == null,
                "Inventory stock must be accurately deducted after confirmation");
        TestRunner.assertTrue(repo.getShipments().size() > initialShipments,
                "Simulated shipments must be recorded upon confirmation");

        // Verify double-confirmation prevention
        boolean doubleConfirm = allocService.confirmAndApplyAllocation(plan);
        TestRunner.assertTrue(!doubleConfirm, "Duplicate allocation confirmation must be rejected");

        // 4. Insufficient Stock Handling
        SupplyRequest hugeReq = reqService.createRequest("H001", "M005", 99999, UrgencyLevel.NORMAL, "Excessive bulk order");
        AllocationService.AllocationPlan hugePlan = allocService.computeAllocation(hugeReq);
        TestRunner.assertTrue(hugePlan.getShortage() > 0,
                "System must detect shortage when requested quantity exceeds available supply or route capacity");

        System.out.println("  [PASS] Integrated Workflow Tests Completed.");
    }
}
