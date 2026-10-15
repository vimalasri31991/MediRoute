package mediroute.service;

import mediroute.algorithms.flow.*;
import mediroute.data.DataRepository;
import mediroute.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Core Service for Allocating and Distributing Medical Supplies.
 * Synthesizes inventory availability, route capacities, and Maximum Flow algorithms
 * (Ford-Fulkerson, Edmonds-Karp, Dinic) to compute physically feasible distribution allocations.
 */
public class AllocationService {

    private final DataRepository repository;
    private int shipmentCounter = 1000;

    public AllocationService(DataRepository repository) {
        this.repository = repository;
    }

    public static class AllocationPlan {
        private final SupplyRequest request;
        private final Medicine medicine;
        private final Hospital hospital;
        private final int requestedQuantity;
        private final int fordFulkersonFlow;
        private final int edmondsKarpFlow;
        private final int dinicFlow;
        private final boolean flowConsistent;
        private final int totalAllocated;
        private final int shortage;
        private final Map<String, Integer> eligibleStock; // WarehouseId -> Available stock before allocation
        private final Map<String, Integer> warehouseAllocations; // WarehouseId -> allocated quantity
        private final MinCutAnalyzer.MinCutResult minCutResult;
        private final List<String> explanation;
        private boolean confirmed = false;
        private String generatedShipmentId;

        public AllocationPlan(SupplyRequest request, Medicine medicine, Hospital hospital,
                              int requestedQuantity, int fordFulkersonFlow, int edmondsKarpFlow,
                              int dinicFlow, boolean flowConsistent, int totalAllocated, int shortage,
                              Map<String, Integer> eligibleStock,
                              Map<String, Integer> warehouseAllocations, MinCutAnalyzer.MinCutResult minCutResult,
                              List<String> explanation) {
            this.request = request;
            this.medicine = medicine;
            this.hospital = hospital;
            this.requestedQuantity = requestedQuantity;
            this.fordFulkersonFlow = fordFulkersonFlow;
            this.edmondsKarpFlow = edmondsKarpFlow;
            this.dinicFlow = dinicFlow;
            this.flowConsistent = flowConsistent;
            this.totalAllocated = totalAllocated;
            this.shortage = shortage;
            this.eligibleStock = eligibleStock;
            this.warehouseAllocations = warehouseAllocations;
            this.minCutResult = minCutResult;
            this.explanation = explanation;
        }

        public SupplyRequest getRequest() { return request; }
        public Medicine getMedicine() { return medicine; }
        public Hospital getHospital() { return hospital; }
        public int getRequestedQuantity() { return requestedQuantity; }
        public int getFordFulkersonFlow() { return fordFulkersonFlow; }
        public int getEdmondsKarpFlow() { return edmondsKarpFlow; }
        public int getDinicFlow() { return dinicFlow; }
        public boolean isFlowConsistent() { return flowConsistent; }
        public int getTotalAllocated() { return totalAllocated; }
        public int getShortage() { return shortage; }
        public Map<String, Integer> getEligibleStock() { return eligibleStock; }
        public Map<String, Integer> getWarehouseAllocations() { return warehouseAllocations; }
        public MinCutAnalyzer.MinCutResult getMinCutResult() { return minCutResult; }
        public List<String> getExplanation() { return explanation; }
        public boolean isConfirmed() { return confirmed; }
        public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
        public String getGeneratedShipmentId() { return generatedShipmentId; }
        public void setGeneratedShipmentId(String id) { this.generatedShipmentId = id; }
    }

    /**
     * Calculates a feasible allocation plan without modifying inventory.
     */
    public AllocationPlan computeAllocation(SupplyRequest request) {
        Medicine med = repository.getMedicine(request.getMedicineId());
        Hospital hosp = repository.getHospital(request.getHospitalId());

        if (med == null || hosp == null) {
            throw new IllegalArgumentException("Invalid medicine or hospital in request.");
        }

        // 1. Identify all nodes in the network
        Set<String> allNodes = new LinkedHashSet<>();
        allNodes.add("SOURCE");
        allNodes.add("SINK");
        allNodes.addAll(repository.getWarehouses().keySet());
        allNodes.addAll(repository.getHospitals().keySet());

        for (Route r : repository.getRoutes()) {
            allNodes.add(r.getSourceId());
            allNodes.add(r.getDestinationId());
        }

        List<String> nodeList = new ArrayList<>(allNodes);
        Map<String, Integer> nodeIndexMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            nodeIndexMap.put(nodeList.get(i), i);
        }

        int sourceIdx = nodeIndexMap.get("SOURCE");
        int sinkIdx = nodeIndexMap.get("SINK");
        int targetHospIdx = nodeIndexMap.get(hosp.getHospitalId());

        Map<String, Integer> eligibleStock = new LinkedHashMap<>();
        for (Warehouse w : repository.getWarehouses().values()) {
            int stock = repository.getTotalStock(w.getWarehouseId(), med.getMedicineId());
            if (stock > 0) {
                // If medicine requires cold chain, warehouse must be cold-chain capable
                if (!med.isColdChainRequired() || w.isColdChainCapable()) {
                    eligibleStock.put(w.getWarehouseId(), stock);
                }
            }
        }

        // Build networks for FF, EK, and Dinic
        FlowNetwork netFF = createAllocationNetwork(nodeList, nodeIndexMap, sourceIdx, sinkIdx, targetHospIdx, eligibleStock, request.getQuantityRequested());
        FlowNetwork netEK = createAllocationNetwork(nodeList, nodeIndexMap, sourceIdx, sinkIdx, targetHospIdx, eligibleStock, request.getQuantityRequested());
        FlowNetwork netDinic = createAllocationNetwork(nodeList, nodeIndexMap, sourceIdx, sinkIdx, targetHospIdx, eligibleStock, request.getQuantityRequested());

        FordFulkerson.FlowResult ff = FordFulkerson.computeMaxFlow(netFF, sourceIdx, sinkIdx);
        EdmondsKarp.FlowResult ek = EdmondsKarp.computeMaxFlow(netEK, sourceIdx, sinkIdx);
        int dinicFlow = DinicAlgorithm.computeMaxFlow(netDinic, sourceIdx, sinkIdx);

        boolean flowConsistent = (ff.getMaxFlow() == ek.getMaxFlow()) && (ek.getMaxFlow() == dinicFlow);

        // Inspect warehouse draws from Dinic's residual flow network
        Map<String, Integer> allocations = new LinkedHashMap<>();
        for (FlowNetwork.FlowEdge e : netDinic.getEdgesFrom(sourceIdx)) {
            if (e.flow > 0) {
                String wId = netDinic.getLabel(e.to);
                allocations.put(wId, e.flow);
            }
        }

        // Min-Cut Bottleneck analysis
        MinCutAnalyzer.MinCutResult minCut = MinCutAnalyzer.analyzeMinCut(netDinic, sourceIdx, sinkIdx);

        int shortage = Math.max(0, request.getQuantityRequested() - dinicFlow);
        List<String> explanation = new ArrayList<>();

        if (dinicFlow == request.getQuantityRequested()) {
            explanation.add("Full allocation feasible: All requested " + dinicFlow + " units can be routed to " + hosp.getName() + ".");
        } else if (dinicFlow > 0) {
            explanation.add("Partial allocation: Only " + dinicFlow + " units could be routed out of " + request.getQuantityRequested() + " requested.");
            explanation.add("Shortage of " + shortage + " units due to route capacity or regional warehouse inventory limits.");
        } else {
            explanation.add("Allocation unfeasible: No route with available stock reaches " + hosp.getName() + ".");
        }

        return new AllocationPlan(request, med, hosp, request.getQuantityRequested(),
                ff.getMaxFlow(), ek.getMaxFlow(), dinicFlow, flowConsistent, dinicFlow, shortage,
                eligibleStock, allocations, minCut, explanation);
    }

    private FlowNetwork createAllocationNetwork(List<String> nodeList, Map<String, Integer> nodeIndexMap,
                                               int sourceIdx, int sinkIdx, int targetHospIdx,
                                               Map<String, Integer> eligibleStock, int requestedQuantity) {
        FlowNetwork net = new FlowNetwork(nodeList.size());
        for (int i = 0; i < nodeList.size(); i++) {
            net.setLabel(i, nodeList.get(i));
        }

        // Connect SOURCE to warehouses with available inventory
        for (Map.Entry<String, Integer> entry : eligibleStock.entrySet()) {
            Integer wIdx = nodeIndexMap.get(entry.getKey());
            if (wIdx != null) {
                net.addEdge(sourceIdx, wIdx, entry.getValue());
            }
        }

        // Connect distribution routes
        for (Route r : repository.getRoutes()) {
            if (r.isOperable()) {
                Integer u = nodeIndexMap.get(r.getSourceId());
                Integer v = nodeIndexMap.get(r.getDestinationId());
                if (u != null && v != null) {
                    net.addEdge(u, v, r.getCapacity());
                }
            }
        }

        // Connect target hospital to SINK with requested quantity
        net.addEdge(targetHospIdx, sinkIdx, requestedQuantity);
        return net;
    }

    /**
     * Confirms and applies an allocation plan: deducts inventory using FEFO and records simulated shipments.
     */
    public synchronized boolean confirmAndApplyAllocation(AllocationPlan plan) {
        if (plan == null || plan.isConfirmed() || plan.getTotalAllocated() <= 0) {
            return false;
        }

        // Verify stock is still present
        for (Map.Entry<String, Integer> entry : plan.getWarehouseAllocations().entrySet()) {
            String wId = entry.getKey();
            int qty = entry.getValue();
            if (repository.getTotalStock(wId, plan.getMedicine().getMedicineId()) < qty) {
                return false;
            }
        }

        String shipmentId = "SHP" + (++shipmentCounter);
        plan.setGeneratedShipmentId(shipmentId);

        // Deduct stock via FEFO and record shipments
        for (Map.Entry<String, Integer> entry : plan.getWarehouseAllocations().entrySet()) {
            String wId = entry.getKey();
            int qty = entry.getValue();
            repository.deductStockFEFO(wId, plan.getMedicine().getMedicineId(), qty);

            // Record simulated shipment
            Shipment shipment = new Shipment(shipmentId, plan.getRequest().getRequestId(), wId,
                    plan.getHospital().getHospitalId(), plan.getMedicine().getMedicineId(), qty,
                    "R-" + wId + "-" + plan.getHospital().getHospitalId(),
                    LocalDate.now(), LocalDate.now().plusDays(1), "SIMULATED ALLOCATION CONFIRMED");
            repository.addShipment(shipment);
        }

        plan.getRequest().setAllocatedQuantity(plan.getTotalAllocated());
        if (plan.getShortage() == 0) {
            plan.getRequest().setStatus(SupplyRequest.RequestStatus.FULFILLED);
        } else {
            plan.getRequest().setStatus(SupplyRequest.RequestStatus.PARTIALLY_ALLOCATED);
        }

        plan.setConfirmed(true);

        try {
            repository.saveInventoryToCsv();
            repository.saveRequestsToCsv();
            repository.saveShipmentsToCsv();
        } catch (IOException e) {
            System.err.println("[WARN] Failed to persist transaction to disk: " + e.getMessage());
        }

        return true;
    }
}
