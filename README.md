# MediRoute – Medical Supply Delivery & Allocation Platform
*Academic DSA3 Simulation System (Algorithms in Practice)*

---

## 1. Project Overview & Objective

**MediRoute** is an interactive, CSV-backed simulation platform that models regional management, distribution, and allocation of essential medical supplies (pharmaceuticals, vaccines, respiratory gear, emergency resuscitation equipment, consumables, and hospital devices).

Rather than presenting isolated algorithm demos, MediRoute connects **25 foundational and advanced Data Structures and Algorithms (DSA)** directly into authentic operational workflows:
- **String Algorithms (CO1)**: Search medicine catalogues, match product codes via rolling hashes, and scan emergency requests for critical clinical terms.
- **Suffix Structures (CO2)**: Index medical document corpora, build linear-time suffix arrays, extract repeating text fragments, and query suffix trees/automata.
- **Advanced Dynamic Programming (CO3)**: Correct misspelled supply and hospital names with transposition-aware edit distance, optimize multi-matrix analytics dataflow pipelines, and compute minimum-cost emergency supply package covers using bitmask state compression.
- **Network Flow (CO4)**: Model multi-echelon capacitated logistics networks (Warehouses $\to$ Transit Hubs $\to$ Hospitals), route shipments using Dinic's, Edmonds-Karp, and Ford-Fulkerson maximum flow algorithms, perform unit bipartite matching, and locate bottlenecks via the Max-Flow Min-Cut theorem.
- **NP-Completeness & Approximation (CO5)**: Model dispatch mutual-exclusion constraints as Boolean 3-CNF formulas, solve instances via DPLL, verify mathematical equivalence across standard polynomial reductions ($3\text{-SAT} \le_p \text{CLIQUE} \le_p \text{INDEPENDENT SET} \le_p \text{VERTEX COVER}$), and compute 2-approximate vertex covers on allocation conflict graphs.
- **Randomized & Parallel Algorithms (CO6)**: Rank historical product demand via Randomized QuickSort, stream-sample historical records with Reservoir Sampling, test batch security tokens with Miller-Rabin, compute cumulative demand with Blelloch's work-efficient parallel scan, perform ForkJoin parallel reductions, and estimate asymptotic speedup bounds via Brent's Scheduling Theorem.

> **Disclaimer**: MediRoute is an academic simulation designed for learning and demonstrating DSA concepts. All data represents fictional medical facilities and synthetic inventory models. It does not dispatch real-world vehicles, interface with clinical telemetry, or provide real medical diagnoses.

---

## 2. System Architecture & Folder Layout

MediRoute is built exclusively with Java Standard Libraries (`java.io`, `java.nio.file`, `java.util`, `java.time`, `java.util.concurrent`) without external frameworks or third-party dependencies.

```
MediRoute/
├── data/
│   ├── medicines.csv           # 320 medical catalogue items (16 fields)
│   ├── hospitals.csv           # 80 healthcare facilities across India (12 fields)
│   ├── warehouses.csv          # 25 multi-level warehouses & depots (11 fields)
│   ├── batches.csv             # 650 production batches with expiry tracking (8 fields)
│   ├── inventory.csv           # 2,679 batch-level warehouse stock records (7 fields)
│   ├── routes.csv              # 185 capacitated transport routes (9 fields)
│   ├── demand_history.csv      # 5,200 historical transaction events across 12 months (8 fields)
│   ├── suppliers.csv           # 45 pharmaceutical & equipment manufacturers (8 fields)
│   ├── requests.csv            # 220 hospital supply requests (8 fields)
│   └── shipments.csv           # 210 simulated shipment dispatch records (8 fields)
├── src/
│   └── mediroute/
│       ├── Main.java           # Application entry point
│       ├── app/
│       │   ├── MediRouteApp.java   # Lifecycle coordinator and dataset bootstrap
│       │   └── ConsoleMenu.java    # Interactive console interface and workflows
│       ├── model/
│       │   ├── Medicine.java       # 16-field medical product entity
│       │   ├── Hospital.java       # 12-field healthcare facility entity
│       │   ├── Warehouse.java      # 11-field storage hub entity with cold-chain flag
│       │   ├── Batch.java          # 8-field batch entity with expiry and FEFO sorting
│       │   ├── InventoryItem.java  # 7-field inventory item with batch and reservation tracking
│       │   ├── Route.java          # 9-field capacitated transport route entity
│       │   ├── SupplyRequest.java  # 8-field hospital supply request entity
│       │   ├── Shipment.java       # 8-field simulated dispatch log entity
│       │   ├── DemandRecord.java   # 8-field historical transaction entity
│       │   ├── Supplier.java       # 8-field pharmaceutical supplier entity
│       │   ├── SupplyPackage.java  # Pre-bundled kit for Bitmask DP cover
│       │   └── UrgencyLevel.java   # Urgency enumeration (CRITICAL, URGENT, NORMAL)
│       ├── data/
│       │   ├── CsvReader.java      # RFC-4180 aware CSV parser (quotes, escapes, whitespace)
│       │   ├── CsvWriter.java      # Safe CSV exporter
│       │   ├── DataRepository.java # Referential integrity validation and FEFO inventory deduction
│       │   └── DatasetGenerator.java # Deterministic, reproducible dataset generator (Seed 42)
│       ├── algorithms/
│       │   ├── strings/
│       │   │   ├── KMPMatcher.java         # Knuth-Morris-Pratt string matcher
│       │   │   ├── ZAlgorithm.java         # Z-Function prefix pattern analyzer
│       │   │   ├── RabinKarpMatcher.java   # Rolling-hash matcher with collision verification
│       │   │   └── AhoCorasickMatcher.java # Multi-pattern keyword trie with failure links
│       │   ├── suffix/
│       │   │   ├── SuffixArray.java        # Prefix-doubling suffix array with binary search
│       │   │   ├── SAIS.java               # Genuine linear-time O(N) SA-IS induced sorting
│       │   │   ├── KasaiLCP.java           # O(N) Kasai LCP array and repeated fragment finder
│       │   │   ├── SuffixTree.java         # Multi-document suffix tree with boundary tokens
│       │   │   └── SuffixAutomaton.java    # Suffix Automaton (minimal DAWG, O(N) construction)
│       │   ├── dp/
│       │   │   ├── EditDistance.java       # Levenshtein, Damerau-Levenshtein, Weighted Edit Dist
│       │   │   ├── BitmaskPackageCover.java# Minimum-cost category cover via bitmask DP
│       │   │   └── MatrixChainMultiplication.java # MCM DP for analytics transformation pipelines
│       │   ├── flow/
│       │   │   ├── FlowNetwork.java        # Directed residual graph representation
│       │   │   ├── FordFulkerson.java      # DFS augmenting path maximum flow
│       │   │   ├── EdmondsKarp.java        # BFS shortest augmenting path maximum flow
│       │   │   ├── DinicAlgorithm.java     # Level graph + blocking flow maximum flow
│       │   │   ├── BipartiteMatching.java  # Unit matching for hospital-depot assignment
│       │   │   └── MinCutAnalyzer.java     # S-T residual reachability & bottleneck cut edges
│       │   ├── complexity/
│       │   │   ├── SATInstance.java        # CNF / 3-CNF Boolean formula model
│       │   │   ├── SATSolver.java          # DPLL solver (backtracking + unit propagation)
│       │   │   ├── Reductions.java         # 3-SAT -> CLIQUE -> IndepSet -> VertexCover reductions
│       │   │   └── VertexCoverApproximation.java # 2-Approximation via maximal matching
│       │   └── parallel/
│       │       ├── RandomizedQuickSort.java# In-place randomized-pivot quicksort
│       │       ├── ReservoirSampler.java   # Single-pass streaming reservoir sampling
│       │       ├── MillerRabinPrimality.java# Modular exponentiation & probabilistic primality
│       │       ├── BlellochScan.java       # Parallel upsweep/downsweep exclusive/inclusive scan
│       │       ├── ParallelReducer.java    # ForkJoinPool recursive parallel reduction
│       │       └── BrentsTheorem.java      # Theoretical speedup bound analysis
│       ├── service/
│       │   ├── MedicineSearchService.java
│       │   ├── RequestService.java
│       │   ├── AllocationService.java
│       │   ├── InventoryService.java
│       │   ├── DemandAnalyticsService.java
│       │   ├── DocumentAnalysisService.java
│       │   ├── NetworkAnalysisService.java
│       │   └── ConstraintAnalysisService.java
│       ├── util/
│       │   ├── InputValidator.java
│       │   └── TableFormatter.java
│       └── test/
│           ├── TestRunner.java             # Standalone test runner with 98 automated assertions
│           ├── StringAlgorithmsTest.java
│           ├── SuffixStructuresTest.java
│           ├── DynamicProgrammingTest.java
│           ├── NetworkFlowTest.java
│           ├── ComplexityAlgorithmsTest.java
│           ├── ParallelRandomizedTest.java
│           └── IntegrationWorkflowTest.java
├── bin/                        # Compiled .class bytecode
└── README.md                   # Complete documentation and setup manual
```

---

## 3. Comprehensive CSV Datasets & Schemas

All 10 CSV datasets are stored in `data/` and loaded dynamically using relative paths. Every dataset was generated deterministically using `mediroute.data.DatasetGenerator` with seed `42` to ensure realistic, referentially sound Indian healthcare distribution scenarios.

### 1. `medicines.csv` (320 records)
- **Columns**: `medicine_id,name,product_code,category,sub_category,description,dosage_form,strength,unit_of_measure,storage_type,cold_chain_required,controlled_item,expiry_tracking,unit_cost,supplier_id,status`
- **Coverage**: Over-the-counter pain relievers, antibiotics, cardiovascular agents, emergency resuscitation drugs, vaccines (cold chain), IV fluids, surgical consumables, PPE, and heavy equipment (Oxygen Cylinders, Portable Ventilators, Infusion Pumps).
- **Key Examples**: `M001` (Paracetamol 500mg), `M002` (Insulin 100IU, cold chain required), `M003` (Amoxicillin 500mg), `M004` (Saline 500ml), `M005` (Oxygen Cylinder 40L).

### 2. `hospitals.csv` (80 records)
- **Columns**: `hospital_id,hospital_name,facility_type,location,district,state,pincode,latitude,longitude,bed_capacity,priority,operating_status`
- **Coverage**: Primary Health Centres (PHCs), Community Health Centres (CHCs), Sub-District Hospitals, Government District Hospitals, Teaching Medical Colleges, and Trauma Centers across 8 Indian states.
- **Key Examples**: `H001` (City Hospital, Priority 1), `H002` (General Hospital), `H003` (Community Hospital).

### 3. `warehouses.csv` (25 records)
- **Columns**: `warehouse_id,warehouse_name,warehouse_type,location,district,state,latitude,longitude,storage_capacity,cold_chain_capable,operating_status`
- **Coverage**: Central Medical Supply Depots, Regional Distribution Centres, Cold-Chain Transshipment Hubs, and Emergency Disaster Depots.
- **Key Examples**: `W001` (Metro Central Depot, cold-chain capable), `W002` (Regional Supply Hub, cold-chain capable), `W003` (East District Depot).

### 4. `batches.csv` (650 records)
- **Columns**: `batch_id,medicine_id,supplier_id,manufacture_date,expiry_date,quantity_received,unit_cost,batch_status`
- **Features**: Tracks manufacturing and expiration dates for First-Expiry-First-Out (FEFO) allocation. Includes near-expiry and quarantine flags.

### 5. `inventory.csv` (2,679 records)
- **Columns**: `warehouse_id,medicine_id,batch_id,available_quantity,reserved_quantity,reorder_level,last_updated`
- **Features**: Batch-level inventory tracking per warehouse. Deductions strictly follow FEFO order, prioritizing earliest-expiring active batches.

### 6. `routes.csv` (185 records)
- **Columns**: `route_id,source_id,destination_id,distance_km,estimated_travel_minutes,capacity,transport_mode,route_status,cost_per_km`
- **Features**: Warehouse-to-Warehouse and Warehouse-to-Hospital capacitated routes with distance, travel time, and operational statuses (`ACTIVE`, `MAINTENANCE`, `CONGESTED`).

### 7. `demand_history.csv` (5,200 records)
- **Columns**: `demand_id,date,hospital_id,medicine_id,quantity_requested,quantity_fulfilled,urgency,seasonal_factor`
- **Features**: 12 months of daily historical orders covering routine demand, epidemic seasonal spikes (e.g. monsoon/winter), and emergency surges.

### 8. `suppliers.csv` (45 records)
- **Columns**: `supplier_id,supplier_name,contact_person,phone,email,address,city,reliability_score`
- **Features**: Accredited pharmaceutical manufacturers and medical device distributors with reliability scores.

### 9. `requests.csv` (220 records)
- **Columns**: `request_id,hospital_id,medicine_id,quantity_requested,urgency,request_date,status,notes`
- **Features**: Initial queue of pending and fulfilled hospital requests with clinical notes.

### 10. `shipments.csv` (210 records)
- **Columns**: `shipment_id,request_id,route_id,origin_warehouse_id,destination_hospital_id,quantity,dispatched_date,status`
- **Features**: Historical and real-time simulated dispatches logging physical fulfillment legs.

---

## 4. Algorithms Implementation & Operations Mapping

| Module / CO | Algorithm | Real Application Operation | Purpose & Integration | Time Complexity | Space Complexity |
|---|---|---|---|---|---|
| **CO1: Strings** | **Knuth-Morris-Pratt (KMP)** | Op 1 & 4: Search supplies & documents | Linear-time substring search across medicine names and pharmacopeia descriptions using precomputed LPS array. | $O(N + M)$ | $O(M)$ |
| **CO1: Strings** | **Z-Function** | Op 4: Analyze medical documents | Detects repeating prefix patterns and periodic substrings in pharmacopeia texts. | $O(N)$ | $O(N)$ |
| **CO1: Strings** | **Rabin-Karp** | Op 1: Search by product code | Rolling polynomial hash ($B=257, M=10^9+7$) for fast product code matching (`MED1001`), with character verification. | $O(N + M)$ avg | $O(1)$ |
| **CO1: Strings** | **Aho-Corasick** | Op 2: Submit hospital request | Multi-pattern trie with BFS failure and dictionary links. Scans clinical request notes simultaneously for emergency keywords (`oxygen`, `insulin`, `emergency`, `urgent`, `icu`, `trauma`) and auto-elevates urgency to `CRITICAL`. | $O(N + K)$ | $O(\Sigma \cdot \text{len})$ |
| **CO2: Suffix** | **Suffix Array** | Op 4: Analyze medical documents | Prefix-doubling suffix array with $O(M \log N)$ binary-search substring querying over medical records. | $O(N \log^2 N)$ | $O(N)$ |
| **CO2: Suffix** | **SA-IS Algorithm** | Op 4: Analyze medical documents | Genuine linear-time suffix array construction using S/L-type classification, LMS substrings, bucket sorting, and induced sorting recursion. | $O(N)$ | $O(N)$ |
| **CO2: Suffix** | **Kasai's LCP Array** | Op 4: Analyze medical documents | $O(N)$ LCP construction using suffix ranks. Automatically detects repeated description fragments and shared clinical notes. | $O(N)$ | $O(N)$ |
| **CO2: Suffix** | **Suffix Tree** | Op 4: Analyze medical documents | Generalized suffix tree over concatenated records with unique boundary sentinel delimiters (`\uE000` + id) preventing cross-document false matches. | $O(N)$ | $O(N)$ |
| **CO2: Suffix** | **Suffix Automaton (SAM)** | Op 1 & 4: Substring matching | Minimal Directed Acyclic Word Graph (DAWG) recognizing all substrings in $O(M)$ time and tracking first occurrences. | $O(N)$ constr., $O(M)$ query | $O(N)$ |
| **CO3: DP** | **Levenshtein Distance** | Op 1: Search medical supplies | Classical insert, delete, substitution dynamic programming for typo correction. | $O(N \cdot M)$ | $O(N \cdot M)$ |
| **CO3: DP** | **Damerau-Levenshtein** | Op 1 & 2: Typo correction | Extends Levenshtein by allowing cost-1 adjacent transpositions (e.g., typing `Paracetmol` $\to$ `Paracetamol`). | $O(N \cdot M)$ | $O(N \cdot M)$ |
| **CO3: DP** | **Weighted Edit Distance** | Op 1: Search medical supplies | Configurable keyboard-biased error penalties ranking spelling suggestions. | $O(N \cdot M)$ | $O(N \cdot M)$ |
| **CO3: DP** | **Bitmask DP** | Op 7: Analyze constraints | Computes the minimum-cost bundle of pre-packaged emergency medical kits to cover a required subset of supply categories (state space $2^K$). | $O(M \cdot 2^K)$ | $O(2^K)$ |
| **CO3: DP** | **Matrix-Chain Multiplication** | Op 7: Analyze constraints | Standard MCM DP optimizing the parenthesization of analytics pipeline matrices (Demands $\times$ Inventory $\times$ Routes $\times$ Projections). | $O(N^3)$ | $O(N^2)$ |
| **CO4: Flow** | **Ford-Fulkerson** | Op 3 & 8: Allocation & flow | DFS augmenting path search on residual graph; tracks flow augmentations and cross-checks flow consistency. | $O(E \cdot F)$ | $O(V + E)$ |
| **CO4: Flow** | **Edmonds-Karp** | Op 3 & 8: Allocation & flow | BFS shortest augmenting paths maximum flow; provides path logs. | $O(V \cdot E^2)$ | $O(V + E)$ |
| **CO4: Flow** | **Dinic's Algorithm** | Op 3 & 8: Allocation & flow | Builds BFS level graphs and pushes blocking flows via DFS with dead-end pointer pruning. Powers the primary allocation engine. | $O(V^2 E)$ | $O(V + E)$ |
| **CO4: Flow** | **Bipartite Matching** | Op 8: Distribution network | Maximum cardinality bipartite matching assigning hospitals to regional warehouses under unit dispatch rules. | $O(V \cdot E)$ | $O(V + E)$ |
| **CO4: Flow** | **Min-Cut Analyzer** | Op 3 & 8: Allocation bottlenecks | Residual BFS identifying the source cut set $S$ and sink set $T$. Discovers saturated bottleneck edges where capacity restricts delivery. | $O(V + E)$ | $O(V)$ |
| **CO5: NP-Comp** | **DPLL 3-SAT Solver** | Op 7: Analyze constraints | Small exact Boolean satisfiability solver using unit propagation and backtracking to verify dispatch mutual exclusion constraints. | $O(2^N)$ | $O(N + M)$ |
| **CO5: NP-Comp** | **3-SAT $\le_p$ CLIQUE** | Op 7: Analyze constraints | Standard reduction transforming $m$ 3-CNF clauses into a $3m$-vertex compatibility graph. Proves SAT $\iff$ $m$-clique exists. | $O(M^2)$ | $O(M)$ |
| **CO5: NP-Comp** | **CLIQUE $\to$ IS $\to$ VC** | Op 7: Analyze constraints | Polynomial reductions utilizing complement graphs ($\overline{G}$) and set complementation ($V \setminus S$). | $O(V^2)$ | $O(V^2)$ |
| **CO5: NP-Comp** | **Vertex Cover 2-Approx** | Op 7: Allocation conflicts | Maximal matching 2-approximation selecting both endpoints of uncovered conflict edges; guarantees $|C| \le 2 \cdot OPT$. | $O(V + E)$ | $O(V)$ |
| **CO6: Parallel** | **Randomized QuickSort** | Op 6: Demand analytics | In-place randomized pivot selection to sort products by demand, avoiding $O(N^2)$ worst-case traps. | $O(N \log N)$ avg | $O(\log N)$ |
| **CO6: Parallel** | **Reservoir Sampling** | Op 6: Demand analytics | Online streaming sampling of $k$ historical records from unbounded streams without storing all records in memory. | $O(N)$ | $O(K)$ |
| **CO6: Parallel** | **Miller-Rabin Primality** | Op 6: Numerical demonstration | Modular exponentiation with repeated squaring testing probabilistic primality for batch verification tokens. | $O(K \log^3 N)$ | $O(1)$ |
| **CO6: Parallel** | **Blelloch Parallel Scan** | Op 6: Demand analytics | Work-efficient parallel prefix sum with upsweep (reduce) and downsweep phases, handling arbitrary lengths via power-of-2 padding. | $O(N)$ work, $O(\log N)$ span | $O(N)$ |
| **CO6: Parallel** | **ForkJoin Parallel Reduce** | Op 6: Demand analytics | Multi-threaded divide-and-conquer aggregation calculating aggregate demand and warehouse inventory totals. | $O(N)$ work, $O(\log N)$ span | $O(\log N)$ |
| **CO6: Parallel** | **Brent's Theorem** | Op 6: Demand analytics | Theoretical scheduling analysis calculating $T_P \le \frac{W - D}{P} + D$, speedup, and efficiency bounds across processor counts. | $O(1)$ | $O(1)$ |

---

## 5. End-to-End Operational Workflows

### 1. Medical Supply Search & Typo Correction
- Enter a query such as `Paracetmol`.
- Exact name match is evaluated via KMP; exact code match is evaluated via Rabin-Karp.
- If not found, **Damerau-Levenshtein** evaluates candidate names and suggests ranked corrections (e.g., `M001: Paracetamol (Edit Distance: 1)`).
- Selecting `M001` displays product details, category, unit specifications, and per-warehouse available stock.

### 2. Hospital Emergency Supply Request
- Enter Hospital ID (`H001`), Medicine (`M002` / Insulin), quantity (`100`), and request description (`Emergency insulin supply required urgently.`).
- **Aho-Corasick** scans the clinical description in one pass and detects keywords `emergency`, `insulin`, and `urgent`.
- Urgency is automatically escalated to `HIGH` (`CRITICAL`).
- The system checks medicine existence, hospital eligibility, and current warehouse stock.
- Generates a pending request with ID `REQ1001` (or next sequence). **No inventory is deducted at this stage.**

### 3. Supply Allocation & Multi-Algorithm Verification
- Navigate to Allocation Center and enter `REQ1001`.
- Eligible warehouses with available stock are queried (`W001: 60`, `W002: 50`, `W003: 20`, etc.).
- A multi-source, single-sink capacitated network is constructed with route capacities from `routes.csv`.
- **Ford-Fulkerson**, **Edmonds-Karp**, and **Dinic's Algorithm** are all executed to compute the maximum flow and verify consistency.
- A proposed allocation is displayed:
  - `Warehouse W001 -> City Hospital: 60 units`
  - `Warehouse W002 -> City Hospital: 40 units`
- Upon explicit confirmation (`Y`), inventory is deducted using **FEFO (First-Expiry-First-Out)** logic from active batches, and simulated shipment records are logged.

### 4. Inventory, Shipments & Demand Analytics
- View stock by warehouse to observe deductions in real-time (`W001: 60 - 60 = 0`, `W002: 50 - 40 = 10`).
- View shipment history to inspect the newly recorded dispatch record (`SHP1001`).
- Run Demand Analytics to rank products via **Randomized QuickSort**, run **Reservoir Sampling**, compute cumulative demand via **Blelloch Parallel Scan**, and inspect parallel execution bounds via **Brent's Theorem**.

---

## 6. How to Build, Run, and Test

### Prerequisites
- Java Development Kit (JDK 17, 21, or 25).
- Works out-of-the-box on Windows, macOS, and Linux without third-party dependencies.

### Method 1: Command Line / Terminal

1. **Compile all source files**:
   ```bash
   # Windows (CMD)
   dir /s /b src\*.java > sources.txt && javac --release 17 -encoding UTF-8 -d bin @sources.txt && del sources.txt

   # Windows (PowerShell)
   $files = (Get-ChildItem -Recurse -Filter *.java src).FullName; javac --release 17 -encoding UTF-8 -d bin $files

   # Linux / macOS (Bash)
   mkdir -p bin && javac --release 17 -encoding UTF-8 -d bin $(find src -name "*.java")
   ```

2. **Run the Automated Test Suite (98 checks across CO1–CO6)**:
   ```bash
   java -cp bin mediroute.test.TestRunner
   ```

3. **Launch the Interactive Console Application**:
   ```bash
   java -cp bin mediroute.Main
   ```

4. *(Optional)* **Re-generate Datasets Deterministically**:
   ```bash
   java -cp bin mediroute.data.DatasetGenerator
   ```

---

### Method 2: Visual Studio Code (VS Code)

1. Open VS Code.
2. Select **File $\to$ Open Folder...** and select `c:\Users\HP\Documents\MediRoute`.
3. Install the **Extension Pack for Java** (by Microsoft) if not already installed.
4. VS Code automatically discovers the source files in `src/`:
   - To run tests: Open [`TestRunner.java`](file:///c:/Users/HP/Documents/MediRoute/src/mediroute/test/TestRunner.java) and click **Run** above `public static void main(String[] args)`.
   - To run the application: Open [`Main.java`](file:///c:/Users/HP/Documents/MediRoute/src/mediroute/Main.java) and click **Run**.
5. Use the integrated terminal for interactive console inputs.

---

### Method 3: Eclipse IDE

1. Open Eclipse IDE.
2. Select **File $\to$ New $\to$ Java Project**.
3. Set **Project Name** to `MediRoute`.
4. Uncheck **Use default location** and browse to `c:\Users\HP\Documents\MediRoute`.
5. Ensure Java execution environment is set to Java 17 or higher.
6. Click **Finish**.
7. In the **Package Explorer**:
   - Right-click `src/mediroute/test/TestRunner.java` $\to$ **Run As $\to$ Java Application**.
   - Right-click `src/mediroute/Main.java` $\to$ **Run As $\to$ Java Application**.

---

## 7. Automated Test Suite Execution

Running `java -cp bin mediroute.test.TestRunner` executes 98 verification assertions:

```
================================================================================
                 MEDIROUTE AUTOMATED DSA3 TEST SUITE RUNNER                     
================================================================================
-> Running String Algorithms (CO1) Tests...
  [PASS] String Algorithms Tests Completed.
-> Running Suffix Structures (CO2) Tests...
  [PASS] Suffix Structures Tests Completed.
-> Running Dynamic Programming (CO3) Tests...
  [PASS] Dynamic Programming Tests Completed.
-> Running Network Flow (CO4) Tests...
  [PASS] Network Flow Tests Completed.
-> Running Complexity and NP-Reduction (CO5) Tests...
  [PASS] Complexity Algorithms Tests Completed.
-> Running Parallel and Randomized (CO6) Tests...
  [PASS] Parallel and Randomized Tests Completed.
-> Running Integrated Workflow Tests...
  [PASS] Integrated Workflow Tests Completed.
--------------------------------------------------------------------------------
TEST EXECUTION SUMMARY:
  - Total Checks Executed:  98
  - Checks Passed:          98
  - Checks Failed:          0
  - Execution Time:         372 ms

>>> ALL TESTS PASSED SUCCESSFULLY! (100% CORRECTNESS VERIFIED) <<<
================================================================================
```

---

## 8. Academic Integrity & Defense Guide

When presenting this project for your DSA3 evaluation or viva:
1. **End-to-End Problem Context**: MediRoute solves a tangible logistical problem — distributing scarce, time-sensitive medical supplies across geographically dispersed hospitals under road capacity constraints.
2. **True Algorithmic Integration**: No algorithm is a static mock. When a user creates a request, **Aho-Corasick** detects urgency, **Damerau-Levenshtein** corrects hospital/medicine typos, **Dinic's Algorithm** calculates max flow, **Min-Cut** pinpoints delivery bottlenecks, and **FEFO** handles batch deductions.
3. **Linear Suffix Array Construction (SA-IS)**: Highlight that `SAIS.java` provides true $O(N)$ induced sorting (S/L classification and LMS substring recursion), contrasted with the $O(N \log^2 N)$ prefix-doubling suffix array.
4. **Max-Flow Min-Cut Duality**: Demonstrate that the capacity of the bottleneck cut edges ($S$ vs $T$ partition) computed by `MinCutAnalyzer.java` strictly matches the maximum flow computed by Dinic's and Ford-Fulkerson.
5. **Parallel Theory & Practice**: Point out the difference between practical parallel speedup (Java ForkJoin) and theoretical bounds analyzed via **Brent's Theorem** ($T_P \le \frac{W - D}{P} + D$).
