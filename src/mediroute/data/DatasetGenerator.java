package mediroute.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Deterministic synthetic dataset generator for MediRoute.
 * Generates rich, internally consistent medical supply chain datasets using a fixed seed (SEED = 42).
 */
public class DatasetGenerator {

    private static final long SEED = 42L;

    public static void main(String[] args) {
        Path targetDir = DataRepository.resolveDefaultDataDirectory();
        if (args.length > 0) {
            targetDir = java.nio.file.Paths.get(args[0]);
        }
        System.out.println("Generating synthetic MediRoute datasets in: " + targetDir.toAbsolutePath());
        generateAll(targetDir);
        System.out.println("Dataset generation completed successfully.");
    }

    public static void generateAll(Path dataDir) {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            Random rng = new Random(SEED);

            // 1. Suppliers (45 suppliers)
            List<String[]> suppliers = generateSuppliers(45, rng);
            CsvWriter.writeCsv(dataDir.resolve("suppliers.csv"),
                    new String[]{"supplier_id", "supplier_name", "contact_person", "phone", "email", "address", "rating", "status"},
                    suppliers);

            // 2. Medicines (320 items across 11 major categories)
            List<String[]> medicines = generateMedicines(320, suppliers, rng);
            CsvWriter.writeCsv(dataDir.resolve("medicines.csv"),
                    new String[]{"medicine_id", "name", "product_code", "category", "sub_category",
                            "description", "dosage_form", "strength", "unit_of_measure", "storage_type",
                            "cold_chain_required", "controlled_item", "expiry_tracking", "unit_cost",
                            "supplier_id", "status"},
                    medicines);

            // 3. Hospitals (80 healthcare facilities)
            List<String[]> hospitals = generateHospitals(80, rng);
            CsvWriter.writeCsv(dataDir.resolve("hospitals.csv"),
                    new String[]{"hospital_id", "hospital_name", "facility_type", "location", "district",
                            "state", "pincode", "latitude", "longitude", "bed_capacity", "priority", "operating_status"},
                    hospitals);

            // 4. Warehouses (25 supply depots)
            List<String[]> warehouses = generateWarehouses(25, rng);
            CsvWriter.writeCsv(dataDir.resolve("warehouses.csv"),
                    new String[]{"warehouse_id", "warehouse_name", "warehouse_type", "location", "district",
                            "state", "latitude", "longitude", "storage_capacity", "cold_chain_capable", "operating_status"},
                    warehouses);

            // 5. Batches (650 batches)
            List<String[]> batches = generateBatches(650, medicines, suppliers, rng);
            CsvWriter.writeCsv(dataDir.resolve("batches.csv"),
                    new String[]{"batch_id", "medicine_id", "supplier_id", "manufacture_date", "expiry_date",
                            "quantity_received", "unit_cost", "batch_status"},
                    batches);

            // 6. Inventory (1,150 inventory records)
            List<String[]> inventory = generateInventory(warehouses, medicines, batches, rng);
            CsvWriter.writeCsv(dataDir.resolve("inventory.csv"),
                    new String[]{"warehouse_id", "medicine_id", "batch_id", "available_quantity",
                            "reserved_quantity", "reorder_level", "last_updated"},
                    inventory);

            // 7. Routes (185 routes)
            List<String[]> routes = generateRoutes(warehouses, hospitals, rng);
            CsvWriter.writeCsv(dataDir.resolve("routes.csv"),
                    new String[]{"route_id", "source_id", "destination_id", "distance_km",
                            "estimated_travel_minutes", "capacity", "transport_mode", "route_status", "cost_per_km"},
                    routes);

            // 8. Demand History (5,200 records)
            List<String[]> demandHistory = generateDemandHistory(5200, hospitals, medicines, rng);
            CsvWriter.writeCsv(dataDir.resolve("demand_history.csv"),
                    new String[]{"demand_id", "date", "hospital_id", "medicine_id",
                            "quantity_requested", "quantity_fulfilled", "urgency", "seasonal_factor"},
                    demandHistory);

            // 9. Supply Requests (220 records)
            List<String[]> requests = generateSupplyRequests(220, hospitals, medicines, rng);
            CsvWriter.writeCsv(dataDir.resolve("requests.csv"),
                    new String[]{"request_id", "hospital_id", "medicine_id", "quantity_requested",
                            "urgency", "request_description", "request_date", "status"},
                    requests);

            // 10. Shipments (210 records)
            List<String[]> shipments = generateShipments(requests, warehouses, routes, rng);
            CsvWriter.writeCsv(dataDir.resolve("shipments.csv"),
                    new String[]{"shipment_id", "request_id", "source_warehouse", "medicine_id",
                            "quantity", "route_id", "shipment_date", "estimated_arrival", "status"},
                    shipments);

            System.out.printf("Summary: %d Meds, %d Hosps, %d Whs, %d Batches, %d Inv, %d Routes, %d Demands, %d Reqs, %d Ships\n",
                    medicines.size(), hospitals.size(), warehouses.size(), batches.size(),
                    inventory.size(), routes.size(), demandHistory.size(), requests.size(), shipments.size());

        } catch (IOException e) {
            System.err.println("Failed to write synthetic datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String[]> generateSuppliers(int count, Random rng) {
        List<String[]> list = new ArrayList<>();
        String[] companies = {
                "Apex Pharma Labs", "Bharat Healthcare Bio", "Cadila Therapeutics", "Dr Reddy Lifesciences",
                "Emcure Formulations", "Fortis MedEquip Supplies", "Glenmark Generic", "Himalaya Healthtech",
                "Intas Biologicals", "JB Chemicals", "Khandelwal BioMed", "Lupin Care",
                "Mankind Generics", "Natco Life", "Omega Surgical Devices", "Piramal Critical Care",
                "Quest Diagnostic Solutions", "Ranbaxy Healthcare", "Sun Medical Systems", "Torrent Biologicals",
                "Unichem Medical", "Viatris BioTech", "Wockhardt Pharma", "Zenith Hospital Devices"
        };
        String[] cities = {"Mumbai", "Hyderabad", "Bengaluru", "Ahmedabad", "New Delhi", "Pune", "Chennai", "Kolkata"};

        for (int i = 1; i <= count; i++) {
            String sId = String.format("SUP%03d", i);
            String name = companies[(i - 1) % companies.length] + (i > companies.length ? " Division " + (i / companies.length + 1) : "");
            String contact = "Manager " + (char) ('A' + (i % 26)) + ". Sharma";
            String phone = "+91-" + (9800000000L + rng.nextInt(199999999));
            String email = "orders@" + name.toLowerCase().replaceAll("[^a-z]", "") + ".com";
            String city = cities[rng.nextInt(cities.length)];
            String address = "Plot " + (10 + i) + ", Industrial Estate, " + city;
            double rating = 3.5 + (rng.nextDouble() * 1.5);
            String status = (i % 20 == 0) ? "AUDIT_PENDING" : "ACTIVE";

            list.add(new String[]{sId, name, contact, phone, email, address, String.format(Locale.US, "%.1f", rating), status});
        }
        return list;
    }

    private static List<String[]> generateMedicines(int totalCount, List<String[]> suppliers, Random rng) {
        List<String[]> list = new ArrayList<>();

        // Core required medicines with exact IDs from specifications:
        // M001: Paracetamol, M002: Insulin, M003: Amoxicillin, M004: Saline, M005: Oxygen Cylinder
        list.add(new String[]{
                "M001", "Paracetamol", "MED1001", "Tablet", "Analgesics & Antipyretics",
                "Medicine for pain and fever", "Tablet", "500mg", "Strip of 10", "Ambient",
                "false", "false", "true", "1.50", "SUP001", "ACTIVE"
        });
        list.add(new String[]{
                "M002", "Insulin", "MED1002", "Injection", "Endocrine & Diabetes",
                "Fast-acting sterile insulin solution for emergency glycemic control", "Vial", "100IU/ml", "10ml Vial", "Refrigerated (2-8C)",
                "true", "false", "true", "12.00", "SUP002", "ACTIVE"
        });
        list.add(new String[]{
                "M003", "Amoxicillin", "MED1003", "Capsule", "Broad-spectrum Antibiotics",
                "Beta-lactam antibiotic capsules for bacterial infections", "Capsule", "250mg", "Strip of 10", "Ambient",
                "false", "false", "true", "3.20", "SUP003", "ACTIVE"
        });
        list.add(new String[]{
                "M004", "Saline", "MED1004", "IV Fluid", "Electrolytes & Fluids",
                "Sterile isotonic normal sodium chloride IV infusion bag 500ml", "IV Bag", "0.9% w/v", "500ml Bag", "Ambient",
                "false", "false", "true", "2.10", "SUP004", "ACTIVE"
        });
        list.add(new String[]{
                "M005", "Oxygen Cylinder", "MED1005", "Equipment", "Respiratory Care",
                "Medical grade high-pressure compressed oxygen cylinder with safety regulator", "Cylinder", "40L", "Cylinder", "Well-ventilated Dry",
                "false", "false", "false", "85.00", "SUP005", "ACTIVE"
        });

        // Expanded medical catalog templates covering all 11 categories:
        // Category 1: Over-the-counter & Common Formulations
        addCatalogGroup(list, suppliers, rng, "OTC Formulations", "General Health",
                new String[]{"Ibuprofen 400mg", "Cetirizine 10mg", "Omeprazole 20mg", "Antacid Chewable", "Cough Expectorant Syrup 100ml",
                        "Paracetamol Syrup 120mg/5ml", "Loperamide 2mg", "Multivitamin & Zinc", "Calcium Vitamin D3", "Oral Rehydration Salts 20.5g"},
                "Tablet", "Ambient", false, false, 0.8, 4.5);

        // Category 2: Prescription & Cardiovascular
        addCatalogGroup(list, suppliers, rng, "Cardiovascular", "Prescription Chronic",
                new String[]{"Amlodipine 5mg", "Atorvastatin 20mg", "Losartan 50mg", "Metoprolol Succinate 25mg", "Clopidogrel 75mg",
                        "Enalapril 5mg", "Telmisartan 40mg", "Rosuvastatin 10mg", "Hydrochlorothiazide 12.5mg", "Digoxin 0.25mg"},
                "Tablet", "Ambient", false, false, 2.5, 9.0);

        // Category 3: Antibiotics & Antimicrobials
        addCatalogGroup(list, suppliers, rng, "Antibiotics", "Infection Control",
                new String[]{"Azithromycin 500mg", "Ceftriaxone 1g Injection", "Ciprofloxacin 500mg", "Doxycycline 100mg", "Metronidazole 400mg",
                        "Levofloxacin 500mg", "Piperacillin Tazobactam 4.5g", "Meropenem 1g Injection", "Vancomycin 500mg", "Clindamycin 300mg"},
                "Vial", "Ambient", false, false, 5.0, 35.0);

        // Category 4: Diabetes & Endocrine
        addCatalogGroup(list, suppliers, rng, "Endocrine", "Diabetes Care",
                new String[]{"Metformin 500mg", "Glimepiride 2mg", "Insulin Glargine 100IU", "Dapagliflozin 10mg", "Sitagliptin 100mg",
                        "Thyroxine Sodium 50mcg", "Vildagliptin 50mg", "Gliclazide 80mg", "Glucagon 1mg Kit", "Pioglitazone 15mg"},
                "Tablet", "Refrigerated (2-8C)", true, false, 3.0, 28.0);

        // Category 5: Emergency & Critical-Care
        addCatalogGroup(list, suppliers, rng, "Emergency", "Resuscitation & ICU",
                new String[]{"Epinephrine 1mg/ml Ampoule", "Atropine Sulfate 0.6mg", "Norepinephrine 4mg/2ml", "Dopamine 200mg/5ml",
                        "Morphine Sulfate 10mg Ampoule", "Furosemide 20mg/2ml", "Amiodarone 150mg/3ml", "Hydrocortisone 100mg Injection",
                        "Tranexamic Acid 500mg", "Sodium Bicarbonate 8.4% 10ml"},
                "Ampoule", "Ambient", false, true, 4.0, 22.0);

        // Category 6: Vaccines & Temperature-Sensitive Biologics
        addCatalogGroup(list, suppliers, rng, "Vaccines", "Immunization Biologics",
                new String[]{"Rabies Inactivated Vaccine", "Hepatitis B Recombinant Vaccine", "Tetanus Toxoid 0.5ml", "MMR Vaccine Lyophilized",
                        "Inactivated Polio Vaccine IPV", "Typhoid Conjugate Vaccine", "Influenza Quadrivalent Vaccine", "BCG Vaccine 0.1ml",
                        "Pneumococcal Conjugate Vaccine", "DPT Booster Injection"},
                "Vial", "Ultra-Cold / Cold Chain (-20C to 4C)", true, false, 12.0, 65.0);

        // Category 7: IV Fluids, Electrolytes, Sterile Infusions
        addCatalogGroup(list, suppliers, rng, "IV Fluids", "Intravenous Infusion",
                new String[]{"Ringer Lactate Infusion 500ml", "Dextrose 5% Infusion 500ml", "Dextrose Normal Saline DNS 500ml", "Mannitol 20% Infusion 100ml",
                        "Potassium Chloride 15% Ampoule", "Sterile Water for Injection 10ml", "Sodium Chloride 3% Hypertonic 100ml", "Gelatin Plasma Expander 500ml",
                        "Albumin Human 20% 100ml", "Sodium Lactate Compound 500ml"},
                "IV Bag", "Ambient", false, false, 1.8, 45.0);

        // Category 8: Medical Consumables & Dressings
        addCatalogGroup(list, suppliers, rng, "Consumables", "Clinical Supplies",
                new String[]{"Surgical Nitrile Gloves Box of 100", "Sterile Gauze Bandage Pack", "Disposable Syringes 5ml with Needle", "IV Cannula 20G Pink",
                        "Foley Catheter 2-Way 16Fr", "N95 Particulate Respirator Mask Box", "Absorbent Cotton Wool 500g", "Surgical Microporous Paper Tape",
                        "Urine Drainage Bag 2000ml", "Blood Giving Infusion Set"},
                "Box/Pack", "Ambient", false, false, 1.2, 18.0);

        // Category 9: Diagnostic Reagents & Lab Test Kits
        addCatalogGroup(list, suppliers, rng, "Diagnostics", "Point-of-Care Laboratory",
                new String[]{"Blood Glucose Test Strips Box of 50", "COVID-19 Rapid Antigen Detection Kit", "Malaria Antigen Rapid Card Test",
                        "Troponin I Rapid Qualitative Test", "Dengue NS1 Antigen Card", "Urine Multi-Dipstick 10-Parameter", "Blood Grouping Sera Anti-A Anti-B",
                        "Pregnancy Detection HCG Strip", "HIV 1 and 2 Rapid Antibody Test", "Prothrombin Time PT Reagent Vial"},
                "Kit", "Cool Dry (15-25C)", false, false, 8.0, 48.0);

        // Category 10: Medical Equipment & Monitors
        addCatalogGroup(list, suppliers, rng, "Equipment", "Biomedical Devices",
                new String[]{"Ventilator Portable Turbine Driven", "Defibrillator Automated External AED", "Infusion Syringe Pump Microprocessor",
                        "Multi-Parameter Patient Monitor 5-Para", "Pulse Oximeter Fingertip Grade", "ECG 12-Channel Machine Digital",
                        "Suction Apparatus Electric High Vacuum", "Nebulizer Heavy Duty Compressor", "Digital Infrared Thermometer", "Laryngoscope Set with 4 Blades"},
                "Device Unit", "Electronic Storage", false, false, 75.0, 850.0);

        // Category 11: Advanced & Specialized Hospital Supplies
        addCatalogGroup(list, suppliers, rng, "Specialized", "Critical Operative Supplies",
                new String[]{"Hemodialysis Acid Concentrate Solution 5L", "Haemostatic Gelatin Sponge Matrix", "Sterile Bone Wax 2.5g Pack",
                        "Central Venous Catheter Triple Lumen 7Fr", "Endotracheal Tube Cuffed 7.5mm", "Thoracic Drainage Trocar Catheter 28Fr",
                        "Epidural Anesthesia Minipack 18G", "Skin Stapler Sterile Disposable 35W", "Peritoneal Dialysis Twin Bag 2L", "Suture Polyglactin 910 Size 2-0"},
                "Specialized Set", "Ambient Controlled", false, false, 15.0, 120.0);

        // Fill remaining up to totalCount (e.g. 320 items) with synthetic numbered formulations
        int currentSize = list.size();
        for (int i = currentSize + 1; i <= totalCount; i++) {
            String mId = String.format("M%03d", i);
            String name = "Therapeutic Spec " + (1000 + i);
            String code = "MED" + (2000 + i);
            String cat = (i % 2 == 0) ? "Prescription" : "Consumable";
            String sub = "Specialty Formulation";
            String desc = "Standard clinical pharmaceutical preparation grade " + i;
            String form = "Formulation Unit";
            String strength = (5 * (i % 20 + 1)) + "mg";
            String uom = "Pack";
            String storage = (i % 5 == 0) ? "Refrigerated (2-8C)" : "Ambient";
            boolean coldChain = (i % 5 == 0);
            boolean controlled = (i % 15 == 0);
            boolean expiry = true;
            double cost = 2.0 + (rng.nextDouble() * 40.0);
            String supId = suppliers.get(rng.nextInt(suppliers.size()))[0];
            String status = "ACTIVE";

            list.add(new String[]{mId, name, code, cat, sub, desc, form, strength, uom, storage,
                    String.valueOf(coldChain), String.valueOf(controlled), String.valueOf(expiry),
                    String.format(Locale.US, "%.2f", cost), supId, status});
        }

        return list;
    }

    private static void addCatalogGroup(List<String[]> list, List<String[]> suppliers, Random rng,
                                        String category, String subCat, String[] itemNames,
                                        String dosageForm, String storage, boolean coldChain,
                                        boolean controlled, double minCost, double maxCost) {
        for (String itemName : itemNames) {
            int idNum = list.size() + 1;
            String mId = String.format("M%03d", idNum);
            String code = "MED" + (1000 + idNum);
            String desc = "Standard certified supply of " + itemName + " for healthcare facilities.";
            String strength = itemName.replaceAll("[^0-9a-zA-Z/%]", " ").trim();
            if (strength.isEmpty()) strength = "Standard";
            double cost = minCost + rng.nextDouble() * (maxCost - minCost);
            String supId = suppliers.get(rng.nextInt(suppliers.size()))[0];

            list.add(new String[]{
                    mId, itemName, code, category, subCat, desc, dosageForm, strength, "Unit/Pack",
                    storage, String.valueOf(coldChain), String.valueOf(controlled), "true",
                    String.format(Locale.US, "%.2f", cost), supId, "ACTIVE"
            });
        }
    }

    private static List<String[]> generateHospitals(int count, Random rng) {
        List<String[]> list = new ArrayList<>();

        // Core required hospitals: H001: City Hospital, H002: General Hospital, H003: Community Hospital
        list.add(new String[]{"H001", "City Hospital", "General Hospital", "Central District", "Central Delhi", "Delhi", "110001", "28.6139", "77.2090", "500", "1", "OPERATIONAL"});
        list.add(new String[]{"H002", "General Hospital", "Teaching Hospital", "North District", "North Delhi", "Delhi", "110054", "28.6923", "77.2112", "750", "1", "OPERATIONAL"});
        list.add(new String[]{"H003", "Community Hospital", "Community Health Centre", "East Suburb", "East Delhi", "Delhi", "110092", "28.6315", "77.3001", "150", "2", "OPERATIONAL"});

        String[] prefixes = {"Metro", "Apex", "St Jude", "LifeCare", "Fortis Trust", "Apollo Reach", "Max Relief", "Narayana Health", "Sunrise", "Shanti Memorial", "Civil", "District Memorial", "Red Cross"};
        String[] types = {"District Hospital", "Community Health Centre", "Primary Health Centre", "Super Specialty Hospital", "Teaching Hospital", "Maternity & Child Hospital", "Trauma Emergency Center"};
        String[] locations = {
                "South Extension, New Delhi, Delhi, 110049, 28.5700, 77.2200",
                "Andheri West, Mumbai, Maharashtra, 400058, 19.1136, 72.8697",
                "Whitefield, Bengaluru, Karnataka, 560066, 12.9698, 77.7500",
                "T Nagar, Chennai, Tamil Nadu, 600017, 13.0418, 80.2341",
                "Salt Lake Sector V, Kolkata, West Bengal, 700091, 22.5800, 88.4300",
                "Banjara Hills, Hyderabad, Telangana, 500034, 17.4156, 78.4350",
                "Kothrud, Pune, Maharashtra, 411038, 18.5074, 73.8077",
                "Navrangpura, Ahmedabad, Gujarat, 380009, 23.0365, 72.5611",
                "Gomti Nagar, Lucknow, Uttar Pradesh, 226010, 26.8500, 81.0000",
                "Malviya Nagar, Jaipur, Rajasthan, 302017, 26.8500, 75.8200"
        };

        for (int i = 4; i <= count; i++) {
            String hId = String.format("H%03d", i);
            String pfx = prefixes[rng.nextInt(prefixes.length)];
            String type = types[rng.nextInt(types.length)];
            String name = pfx + " " + type;

            String[] locParts = locations[rng.nextInt(locations.length)].split(", ");
            String location = locParts[0];
            String district = locParts[1];
            String state = locParts[2];
            String pin = locParts[3];
            double lat = Double.parseDouble(locParts[4]) + (rng.nextDouble() - 0.5) * 0.08;
            double lon = Double.parseDouble(locParts[5]) + (rng.nextDouble() - 0.5) * 0.08;
            int beds = 40 + rng.nextInt(600);
            int priority = (type.contains("Emergency") || type.contains("Super") || type.contains("District")) ? 1 : (type.contains("Community") ? 2 : 3);
            String status = (i % 25 == 0) ? "LIMITED" : "OPERATIONAL";

            list.add(new String[]{hId, name, type, location, district, state, pin,
                    String.format(Locale.US, "%.4f", lat), String.format(Locale.US, "%.4f", lon),
                    String.valueOf(beds), String.valueOf(priority), status});
        }
        return list;
    }

    private static List<String[]> generateWarehouses(int count, Random rng) {
        List<String[]> list = new ArrayList<>();

        // Core required warehouses: W001, W002, W003
        list.add(new String[]{"W001", "Central Medical Depot", "Central Warehouse", "Industrial Corridor East", "Central Delhi", "Delhi", "28.6300", "77.2400", "120000", "true", "OPERATIONAL"});
        list.add(new String[]{"W002", "Regional Logistics Hub", "Regional Distribution Centre", "Northern Expressway Km12", "North Delhi", "Delhi", "28.7200", "77.1800", "85000", "true", "OPERATIONAL"});
        list.add(new String[]{"W003", "Cold Chain Storage", "Cold-Chain Facility", "Harbor Road Industrial Area", "West Delhi", "Delhi", "28.6000", "77.1000", "50000", "true", "OPERATIONAL"});

        String[] types = {"Regional Depot", "Cold Storage Hub", "Emergency Buffer Depot", "District Supply Point", "Transit Pharmacy"};
        String[] locations = {
                "Okhla Industrial Area, South Delhi, Delhi, 28.5300, 77.2700",
                "Bhiwandi Logistics Park, Thane, Maharashtra, 19.2900, 73.0600",
                "Peenya Industrial Estate, Bengaluru, Karnataka, 13.0300, 77.5200",
                "Sriperumbudur Hub, Kanchipuram, Tamil Nadu, 12.9700, 79.9400",
                "Dankuni Freight Complex, Hooghly, West Bengal, 22.6800, 88.2800",
                "Shamshabad Cargo Terminal, Hyderabad, Telangana, 17.2400, 78.4300",
                "Chakan Auto & Logistics Zone, Pune, Maharashtra, 18.7500, 73.8500",
                "Sanand Industrial Estate, Ahmedabad, Gujarat, 22.9800, 72.3800",
                "Transport Nagar, Lucknow, Uttar Pradesh, 26.7800, 80.8900",
                "Sitapura Industrial Area, Jaipur, Rajasthan, 26.7700, 75.8300"
        };

        for (int i = 4; i <= count; i++) {
            String wId = String.format("W%03d", i);
            String type = types[rng.nextInt(types.length)];
            String name = "Apex " + type + " " + i;
            String[] locParts = locations[rng.nextInt(locations.length)].split(", ");
            String location = locParts[0];
            String district = locParts[1];
            String state = locParts[2];
            double lat = Double.parseDouble(locParts[3]) + (rng.nextDouble() - 0.5) * 0.05;
            double lon = Double.parseDouble(locParts[4]) + (rng.nextDouble() - 0.5) * 0.05;
            int cap = 20000 + rng.nextInt(100000);
            boolean coldChain = type.contains("Cold") || (i % 3 == 0);

            list.add(new String[]{wId, name, type, location, district, state,
                    String.format(Locale.US, "%.4f", lat), String.format(Locale.US, "%.4f", lon),
                    String.valueOf(cap), String.valueOf(coldChain), "OPERATIONAL"});
        }
        return list;
    }

    private static List<String[]> generateBatches(int count, List<String[]> medicines, List<String[]> suppliers, Random rng) {
        List<String[]> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 1; i <= count; i++) {
            String bId = String.format("BAT%04d", i);
            String mId = medicines.get((i - 1) % medicines.size())[0];
            String supId = suppliers.get(rng.nextInt(suppliers.size()))[0];

            int mfgMonthsAgo = 2 + rng.nextInt(18);
            LocalDate mfgDate = now.minusMonths(mfgMonthsAgo);
            // Most batches valid for 18-36 months; a small percentage near expiry or expired to test constraints
            LocalDate expDate;
            if (i % 35 == 0) {
                expDate = now.minusDays(10 + rng.nextInt(60)); // Expired batch
            } else if (i % 25 == 0) {
                expDate = now.plusDays(5 + rng.nextInt(30)); // Near expiry
            } else {
                expDate = mfgDate.plusMonths(24 + rng.nextInt(12));
            }

            int qtyReceived = 500 + rng.nextInt(4500);
            double cost = Double.parseDouble(medicines.get((i - 1) % medicines.size())[13]);
            String status = expDate.isBefore(now) ? "EXPIRED" : ((i % 50 == 0) ? "QUARANTINED" : "RELEASED");

            list.add(new String[]{bId, mId, supId, mfgDate.toString(), expDate.toString(),
                    String.valueOf(qtyReceived), String.format(Locale.US, "%.2f", cost), status});
        }
        return list;
    }

    private static List<String[]> generateInventory(List<String[]> warehouses, List<String[]> medicines,
                                                    List<String[]> batches, Random rng) {
        List<String[]> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Specific inventory amounts matching the prompt example:
        // W001: Paracetamol (M001) = 40, Insulin (M002) = 60, Amoxicillin (M003) = 30, Saline (M004) = 50, Oxygen Cylinder (M005) = 15
        // W002: Paracetamol (M001) = 25, Insulin (M002) = 50, Amoxicillin (M003) = 20, Saline (M004) = 35, Oxygen Cylinder (M005) = 10
        // W003: Paracetamol (M001) = 15, Insulin (M002) = 20, Amoxicillin (M003) = 10, Saline (M004) = 20, Oxygen Cylinder (M005) = 8
        addInventoryRow(list, "W001", "M001", "BAT0001", 40, 0, 10, now);
        addInventoryRow(list, "W001", "M002", "BAT0002", 60, 0, 15, now);
        addInventoryRow(list, "W001", "M003", "BAT0003", 30, 0, 10, now);
        addInventoryRow(list, "W001", "M004", "BAT0004", 50, 0, 15, now);
        addInventoryRow(list, "W001", "M005", "BAT0005", 15, 0, 5, now);

        addInventoryRow(list, "W002", "M001", "BAT0006", 25, 0, 10, now);
        addInventoryRow(list, "W002", "M002", "BAT0007", 50, 0, 15, now);
        addInventoryRow(list, "W002", "M003", "BAT0008", 20, 0, 10, now);
        addInventoryRow(list, "W002", "M004", "BAT0009", 35, 0, 15, now);
        addInventoryRow(list, "W002", "M005", "BAT0010", 10, 0, 5, now);

        addInventoryRow(list, "W003", "M001", "BAT0011", 15, 0, 5, now);
        addInventoryRow(list, "W003", "M002", "BAT0012", 20, 0, 10, now);
        addInventoryRow(list, "W003", "M003", "BAT0013", 10, 0, 5, now);
        addInventoryRow(list, "W003", "M004", "BAT0014", 20, 0, 10, now);
        addInventoryRow(list, "W003", "M005", "BAT0015", 8, 0, 5, now);

        Set<String> assignedKeys = new HashSet<>();
        for (String[] row : list) {
            assignedKeys.add(row[0] + ":" + row[1]);
        }

        // Generate at least 1,150 total records
        int targetRecords = 1150;
        int batchIdx = 15;

        for (String[] w : warehouses) {
            String wId = w[0];
            for (String[] m : medicines) {
                String mId = m[0];
                String key = wId + ":" + mId;
                if (!assignedKeys.contains(key) && (rng.nextDouble() < 0.22 || list.size() < targetRecords)) {
                    assignedKeys.add(key);
                    String bId = batches.get(batchIdx % batches.size())[0];
                    batchIdx++;

                    int available = (rng.nextDouble() < 0.05) ? 0 : (20 + rng.nextInt(400));
                    int reserved = (available > 10 && rng.nextDouble() < 0.3) ? rng.nextInt(available / 3) : 0;
                    int reorder = 10 + rng.nextInt(30);

                    addInventoryRow(list, wId, mId, bId, available, reserved, reorder, now);
                }
            }
        }

        return list;
    }

    private static void addInventoryRow(List<String[]> list, String wId, String mId, String bId,
                                        int avail, int res, int reorder, LocalDate date) {
        list.add(new String[]{wId, mId, bId, String.valueOf(avail), String.valueOf(res),
                String.valueOf(reorder), date.toString()});
    }

    private static List<String[]> generateRoutes(List<String[]> warehouses, List<String[]> hospitals, Random rng) {
        List<String[]> list = new ArrayList<>();
        Set<String> connected = new HashSet<>();

        // Core routes directly connecting W001, W002, W003 to H001, H002, H003
        addRouteRow(list, connected, "R001", "W001", "H001", 18.5, 35, 120, "CARGO_TRUCK", "ACTIVE", 2.2);
        addRouteRow(list, connected, "R002", "W002", "H001", 24.0, 45, 100, "CARGO_TRUCK", "ACTIVE", 2.0);
        addRouteRow(list, connected, "R003", "W003", "H001", 32.0, 55, 60, "REFRIGERATED_VAN", "ACTIVE", 3.0);

        addRouteRow(list, connected, "R004", "W001", "H002", 28.0, 50, 110, "CARGO_TRUCK", "ACTIVE", 2.2);
        addRouteRow(list, connected, "R005", "W002", "H002", 15.0, 30, 150, "CARGO_TRUCK", "ACTIVE", 1.8);
        addRouteRow(list, connected, "R006", "W003", "H003", 22.0, 40, 80, "REFRIGERATED_VAN", "ACTIVE", 2.8);

        // Add Distribution Hub connections
        addRouteRow(list, connected, "R007", "W001", "HUB1", 12.0, 25, 400, "HEAVY_FREIGHT", "ACTIVE", 1.5);
        addRouteRow(list, connected, "R008", "W002", "HUB1", 18.0, 35, 350, "HEAVY_FREIGHT", "ACTIVE", 1.5);
        addRouteRow(list, connected, "R009", "W003", "HUB2", 20.0, 40, 300, "REFRIGERATED_TRUCK", "ACTIVE", 2.5);
        addRouteRow(list, connected, "R010", "HUB1", "H001", 10.0, 20, 250, "EXPRESS_VAN", "ACTIVE", 2.0);
        addRouteRow(list, connected, "R011", "HUB1", "H002", 14.0, 28, 220, "EXPRESS_VAN", "ACTIVE", 2.0);
        addRouteRow(list, connected, "R012", "HUB2", "H003", 16.0, 32, 180, "EXPRESS_VAN", "ACTIVE", 2.0);

        int routeNum = 13;
        String[] modes = {"CARGO_TRUCK", "REFRIGERATED_VAN", "EXPRESS_COURIER", "HEAVY_FREIGHT"};

        // Connect other warehouses to hospitals
        for (String[] w : warehouses) {
            String wId = w[0];
            for (String[] h : hospitals) {
                String hId = h[0];
                String pair = wId + "->" + hId;
                if (!connected.contains(pair) && rng.nextDouble() < 0.08) {
                    double dist = 15.0 + rng.nextDouble() * 120.0;
                    int mins = (int) (dist * (1.2 + rng.nextDouble() * 0.8));
                    int cap = 40 + rng.nextInt(260);
                    String mode = modes[rng.nextInt(modes.length)];
                    String status = (routeNum % 25 == 0) ? "RESTRICTED" : "ACTIVE";
                    double costPerKm = 1.5 + rng.nextDouble() * 2.0;

                    addRouteRow(list, connected, String.format("R%03d", routeNum++),
                            wId, hId, dist, mins, cap, mode, status, costPerKm);
                }
            }
        }

        // Ensure at least 185 routes total
        while (list.size() < 185) {
            String wId = warehouses.get(rng.nextInt(warehouses.size()))[0];
            String hId = hospitals.get(rng.nextInt(hospitals.size()))[0];
            String pair = wId + "->" + hId;
            if (!connected.contains(pair)) {
                double dist = 20.0 + rng.nextDouble() * 80.0;
                int mins = (int) (dist * 1.5);
                int cap = 50 + rng.nextInt(200);
                addRouteRow(list, connected, String.format("R%03d", routeNum++),
                        wId, hId, dist, mins, cap, "CARGO_TRUCK", "ACTIVE", 2.0);
            }
        }

        return list;
    }

    private static void addRouteRow(List<String[]> list, Set<String> connected, String rId,
                                    String src, String dst, double dist, int mins, int cap,
                                    String mode, String status, double cost) {
        connected.add(src + "->" + dst);
        list.add(new String[]{rId, src, dst, String.format(Locale.US, "%.1f", dist),
                String.valueOf(mins), String.valueOf(cap), mode, status, String.format(Locale.US, "%.2f", cost)});
    }

    private static List<String[]> generateDemandHistory(int count, List<String[]> hospitals, List<String[]> medicines, Random rng) {
        List<String[]> list = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusMonths(12);

        // Core products demand totals targeted in prompt:
        // 1. Insulin (M002): ~1250 units
        // 2. Paracetamol (M001): ~980 units
        // 3. Saline (M004): ~850 units
        // 4. Amoxicillin (M003): ~720 units
        // 5. Oxygen Cylinder (M005): ~600 units
        seedDemandForProduct(list, "M002", 1250, hospitals, baseDate, rng);
        seedDemandForProduct(list, "M001", 980, hospitals, baseDate, rng);
        seedDemandForProduct(list, "M004", 850, hospitals, baseDate, rng);
        seedDemandForProduct(list, "M003", 720, hospitals, baseDate, rng);
        seedDemandForProduct(list, "M005", 600, hospitals, baseDate, rng);

        int demId = list.size() + 1;
        String[] urgencies = {"NORMAL", "URGENT", "CRITICAL"};

        while (list.size() < count) {
            String dId = String.format("DEM%05d", demId++);
            int daysOffset = rng.nextInt(365);
            LocalDate date = baseDate.plusDays(daysOffset);
            String hId = hospitals.get(rng.nextInt(hospitals.size()))[0];
            // Pick other medicines (M006 to M320) so they don't surpass the top 5
            int medIdx = 5 + rng.nextInt(medicines.size() - 5);
            String mId = medicines.get(medIdx)[0];

            int qtyReq = 5 + rng.nextInt(45);
            int qtyFulfilled = (rng.nextDouble() < 0.9) ? qtyReq : (qtyReq - rng.nextInt(qtyReq / 2 + 1));
            String urg = urgencies[rng.nextInt(urgencies.length)];
            double seasonal = 0.8 + rng.nextDouble() * 0.5;

            list.add(new String[]{dId, date.toString(), hId, mId,
                    String.valueOf(qtyReq), String.valueOf(qtyFulfilled), urg, String.format(Locale.US, "%.2f", seasonal)});
        }

        return list;
    }

    private static void seedDemandForProduct(List<String[]> list, String medId, int targetSum,
                                            List<String[]> hospitals, LocalDate baseDate, Random rng) {
        int accumulated = 0;
        int dId = list.size() + 1;
        while (accumulated < targetSum) {
            int remaining = targetSum - accumulated;
            int qty = Math.min(remaining, 20 + rng.nextInt(40));
            accumulated += qty;

            String demandId = String.format("DEM%05d", dId++);
            LocalDate date = baseDate.plusDays(rng.nextInt(365));
            String hId = hospitals.get(rng.nextInt(hospitals.size()))[0];
            String urg = (qty > 35) ? "CRITICAL" : ((qty > 20) ? "URGENT" : "NORMAL");

            list.add(new String[]{demandId, date.toString(), hId, medId,
                    String.valueOf(qty), String.valueOf(qty), urg, "1.00"});
        }
    }

    private static List<String[]> generateSupplyRequests(int count, List<String[]> hospitals, List<String[]> medicines, Random rng) {
        List<String[]> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Specific REQ1001 for user workflow:
        // Hospital: H001 (City Hospital), Med: M002 (Insulin), Qty: 100, Urgency: HIGH, Description: Emergency insulin supply required urgently.
        list.add(new String[]{
                "REQ1001", "H001", "M002", "100", "CRITICAL",
                "Emergency insulin supply required urgently for critical diabetic ICU patients.",
                now.toString(), "PENDING"
        });

        String[] statuses = {"FULFILLED", "FULFILLED", "ALLOCATED", "PENDING", "PARTIALLY_ALLOCATED", "CANCELLED"};
        String[] descriptions = {
                "Routine monthly replenishment for in-patient pharmacy.",
                "Urgent trauma emergency resuscitation replenishment.",
                "Intensive care critical respiratory and ventilator support supplies.",
                "Infection control buffer stock replenishment for surgical wings.",
                "Pediatric ward high-priority antibiotic medication requirement.",
                "Dialysis unit essential saline and electrolyte fluid delivery."
        };

        for (int i = 2; i <= count; i++) {
            String reqId = String.format("REQ%04d", 1000 + i);
            String hId = hospitals.get(rng.nextInt(hospitals.size()))[0];
            String mId = medicines.get(rng.nextInt(medicines.size()))[0];
            int qty = 10 + rng.nextInt(150);
            String urg = (qty > 80) ? "CRITICAL" : ((qty > 40) ? "URGENT" : "NORMAL");
            String desc = descriptions[rng.nextInt(descriptions.length)];
            LocalDate date = now.minusDays(rng.nextInt(180));
            String status = statuses[rng.nextInt(statuses.length)];

            list.add(new String[]{reqId, hId, mId, String.valueOf(qty), urg, desc, date.toString(), status});
        }

        return list;
    }

    private static List<String[]> generateShipments(List<String[]> requests, List<String[]> warehouses,
                                                   List<String[]> routes, Random rng) {
        List<String[]> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        int reqIdx = 1;
        while (list.size() < 210) {
            String[] req = requests.get(reqIdx % requests.size());
            reqIdx++;
            String reqId = req[0];
            String mId = req[2];
            int qty = Math.max(5, Integer.parseInt(req[3]) / (1 + (list.size() % 2)));
            String status = req[7];

            String sId = String.format("SHP%04d", 1000 + list.size() + 1);
            String wId = warehouses.get(rng.nextInt(warehouses.size()))[0];
            String rId = routes.get(rng.nextInt(routes.size()))[0];
            LocalDate shipDate = now.minusDays(rng.nextInt(120));
            LocalDate arrDate = shipDate.plusDays(1 + rng.nextInt(3));
            String shipStatus = (list.size() % 4 == 0) ? "IN_TRANSIT" : "DELIVERED";

            list.add(new String[]{sId, reqId, wId, mId, String.valueOf(qty), rId,
                    shipDate.toString(), arrDate.toString(), shipStatus});
        }

        return list;
    }
}
