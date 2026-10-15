package mediroute.service;

import mediroute.algorithms.dp.EditDistance;
import mediroute.algorithms.strings.AhoCorasickMatcher;
import mediroute.data.DataRepository;
import mediroute.model.*;

import java.util.*;

/**
 * Service for managing hospital medical supply requests.
 * Uses Aho-Corasick for emergency keyword detection, and Damerau-Levenshtein for identifier typo detection.
 */
public class RequestService {

    private final DataRepository repository;
    private final AhoCorasickMatcher ahoCorasick;
    private int requestCounter = 1000;

    public RequestService(DataRepository repository) {
        this.repository = repository;
        this.ahoCorasick = new AhoCorasickMatcher(Arrays.asList(
                "oxygen", "insulin", "emergency", "urgent", "ventilator",
                "critical supply", "icu", "trauma", "cardiac", "sepsis", "resuscitation"
        ));
    }

    public static class RequestValidationResult {
        private final boolean valid;
        private final String message;
        private final Hospital hospital;
        private final Medicine medicine;
        private final List<AhoCorasickMatcher.Match> detectedKeywords;
        private final UrgencyLevel recommendedUrgency;
        private final List<EditDistance.Suggestion> hospitalSuggestions;
        private final List<EditDistance.Suggestion> medicineSuggestions;

        public RequestValidationResult(boolean valid, String message, Hospital hospital, Medicine medicine,
                                       List<AhoCorasickMatcher.Match> detectedKeywords, UrgencyLevel recommendedUrgency,
                                       List<EditDistance.Suggestion> hospitalSuggestions,
                                       List<EditDistance.Suggestion> medicineSuggestions) {
            this.valid = valid;
            this.message = message;
            this.hospital = hospital;
            this.medicine = medicine;
            this.detectedKeywords = detectedKeywords == null ? Collections.emptyList() : detectedKeywords;
            this.recommendedUrgency = recommendedUrgency;
            this.hospitalSuggestions = hospitalSuggestions == null ? Collections.emptyList() : hospitalSuggestions;
            this.medicineSuggestions = medicineSuggestions == null ? Collections.emptyList() : medicineSuggestions;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Hospital getHospital() { return hospital; }
        public Medicine getMedicine() { return medicine; }
        public List<AhoCorasickMatcher.Match> getDetectedKeywords() { return detectedKeywords; }
        public UrgencyLevel getRecommendedUrgency() { return recommendedUrgency; }
        public List<EditDistance.Suggestion> getHospitalSuggestions() { return hospitalSuggestions; }
        public List<EditDistance.Suggestion> getMedicineSuggestions() { return medicineSuggestions; }
    }

    /**
     * Validates and analyzes request parameters before submission.
     */
    public RequestValidationResult validateRequest(String hospitalInput, String medicineInput, int quantity,
                                                    UrgencyLevel statedUrgency, String notes) {
        if (hospitalInput == null || hospitalInput.trim().isEmpty()) {
            return new RequestValidationResult(false, "Hospital identifier cannot be empty.", null, null, null, statedUrgency, null, null);
        }
        if (medicineInput == null || medicineInput.trim().isEmpty()) {
            return new RequestValidationResult(false, "Medicine identifier cannot be empty.", null, null, null, statedUrgency, null, null);
        }
        if (quantity <= 0) {
            return new RequestValidationResult(false, "Requested quantity must be positive.", null, null, null, statedUrgency, null, null);
        }

        // 1. Hospital lookup
        Hospital hospital = repository.getHospital(hospitalInput.trim());
        List<EditDistance.Suggestion> hospSuggestions = Collections.emptyList();
        if (hospital == null) {
            // Check by name
            for (Hospital h : repository.getHospitals().values()) {
                if (h.getName().equalsIgnoreCase(hospitalInput.trim())) {
                    hospital = h;
                    break;
                }
            }
        }
        if (hospital == null) {
            List<String> hNames = new ArrayList<>();
            for (Hospital h : repository.getHospitals().values()) {
                hNames.add(h.getHospitalId() + ": " + h.getName());
            }
            hospSuggestions = EditDistance.findSuggestions(hospitalInput.trim(), hNames, 3, EditDistance.EditWeights.standard());
            return new RequestValidationResult(false, "Hospital not found. See spelling suggestions.", null, null, null, statedUrgency, hospSuggestions, null);
        }

        // 2. Medicine lookup
        Medicine medicine = repository.getMedicine(medicineInput.trim());
        List<EditDistance.Suggestion> medSuggestions = Collections.emptyList();
        if (medicine == null) {
            for (Medicine m : repository.getMedicines().values()) {
                if (m.getName().equalsIgnoreCase(medicineInput.trim()) || m.getProductCode().equalsIgnoreCase(medicineInput.trim())) {
                    medicine = m;
                    break;
                }
            }
        }
        if (medicine == null) {
            List<String> mNames = new ArrayList<>();
            for (Medicine m : repository.getMedicines().values()) {
                mNames.add(m.getMedicineId() + ": " + m.getName());
            }
            medSuggestions = EditDistance.findSuggestions(medicineInput.trim(), mNames, 3, EditDistance.EditWeights.standard());
            return new RequestValidationResult(false, "Medicine not found. See spelling suggestions.", hospital, null, null, statedUrgency, null, medSuggestions);
        }

        // 3. Expiry check
        if (medicine.isExpired()) {
            return new RequestValidationResult(false, "Medicine " + medicine.getName() + " has EXPIRED (" + medicine.getExpiryDate() + "). Allocation rejected.", hospital, medicine, null, statedUrgency, null, null);
        }

        // 4. Aho-Corasick multi-pattern scanning on notes & clinical description
        List<AhoCorasickMatcher.Match> detectedKeywords = ahoCorasick.search(notes);
        UrgencyLevel recommendedUrgency = statedUrgency;

        if (!detectedKeywords.isEmpty()) {
            boolean hasCritical = detectedKeywords.stream().anyMatch(m ->
                    m.getKeyword().equalsIgnoreCase("emergency") ||
                    m.getKeyword().equalsIgnoreCase("critical supply") ||
                    m.getKeyword().equalsIgnoreCase("resuscitation") ||
                    m.getKeyword().equalsIgnoreCase("trauma")
            );
            if (hasCritical) {
                recommendedUrgency = UrgencyLevel.CRITICAL;
            } else if (statedUrgency == UrgencyLevel.NORMAL) {
                recommendedUrgency = UrgencyLevel.URGENT;
            }
        }

        return new RequestValidationResult(true, "Validation passed successfully.", hospital, medicine, detectedKeywords, recommendedUrgency, null, null);
    }

    /**
     * Creates and records a supply request.
     */
    public SupplyRequest createRequest(String hospitalId, String medicineId, int quantity, UrgencyLevel urgency, String notes) {
        String reqId = "REQ-" + (++requestCounter);
        SupplyRequest request = new SupplyRequest(reqId, hospitalId, medicineId, quantity, urgency, notes);
        repository.addSupplyRequest(request);
        return request;
    }
}
