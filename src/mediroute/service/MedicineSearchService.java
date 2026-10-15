package mediroute.service;

import mediroute.algorithms.dp.EditDistance;
import mediroute.algorithms.strings.AhoCorasickMatcher;
import mediroute.algorithms.strings.KMPMatcher;
import mediroute.algorithms.strings.RabinKarpMatcher;
import mediroute.algorithms.suffix.SuffixAutomaton;
import mediroute.data.DataRepository;
import mediroute.model.Medicine;

import java.util.*;

/**
 * Service for searching medicines and medical equipment.
 * Integrates KMP, Rabin-Karp, Aho-Corasick, Edit Distance (Levenshtein/Damerau), and Suffix Automaton.
 */
public class MedicineSearchService {

    private final DataRepository repository;
    private final Map<String, SuffixAutomaton> descriptionAutomata = new HashMap<>();

    public MedicineSearchService(DataRepository repository) {
        this.repository = repository;
        buildAutomata();
    }

    private void buildAutomata() {
        for (Medicine m : repository.getMedicines().values()) {
            descriptionAutomata.put(m.getMedicineId(), new SuffixAutomaton(m.getDescription().toLowerCase()));
        }
    }

    public static class SearchResult {
        private final Medicine medicine;
        private final String matchType;
        private final String details;

        public SearchResult(Medicine medicine, String matchType, String details) {
            this.medicine = medicine;
            this.matchType = matchType;
            this.details = details;
        }

        public Medicine getMedicine() { return medicine; }
        public String getMatchType() { return matchType; }
        public String getDetails() { return details; }
    }

    /**
     * Search specifically by medicine name using KMP and exact match.
     */
    public List<SearchResult> searchByName(String query) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return results;
        String q = query.trim();

        for (Medicine m : repository.getMedicines().values()) {
            if (m.getName().equalsIgnoreCase(q)) {
                results.add(new SearchResult(m, "Exact Name Match", "Exact match for name '" + m.getName() + "'"));
                return results; // Immediate exact match
            }
            List<Integer> kmpMatches = KMPMatcher.search(m.getName(), q, true);
            if (!kmpMatches.isEmpty()) {
                results.add(new SearchResult(m, "KMP Name Match", "Matched name at index " + kmpMatches.get(0)));
            }
        }
        return results;
    }

    /**
     * Search specifically by product code using Rabin-Karp rolling hash.
     */
    public List<SearchResult> searchByProductCode(String code) {
        List<SearchResult> results = new ArrayList<>();
        if (code == null || code.trim().isEmpty()) return results;
        String c = code.trim();

        for (Medicine m : repository.getMedicines().values()) {
            List<Integer> rk = RabinKarpMatcher.search(m.getProductCode(), c, true);
            if (!rk.isEmpty()) {
                results.add(new SearchResult(m, "Rabin-Karp Code Match",
                        "Matched code '" + m.getProductCode() + "' at index " + rk.get(0)));
            }
        }
        return results;
    }

    /**
     * Search by description keyword using KMP and Suffix Automaton.
     */
    public List<SearchResult> searchByDescription(String keyword) {
        List<SearchResult> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;
        String kwLower = keyword.trim().toLowerCase();

        for (Medicine m : repository.getMedicines().values()) {
            SuffixAutomaton sam = descriptionAutomata.get(m.getMedicineId());
            if (sam != null && sam.contains(kwLower)) {
                int idx = sam.indexOf(kwLower);
                results.add(new SearchResult(m, "Suffix Automaton Description Match",
                        "Matched description at offset " + idx));
            } else {
                List<Integer> kmp = KMPMatcher.search(m.getDescription(), keyword.trim(), true);
                if (!kmp.isEmpty()) {
                    results.add(new SearchResult(m, "KMP Description Match",
                            "Matched description at index " + kmp.get(0)));
                }
            }
        }
        return results;
    }

    /**
     * Search using multiple keywords simultaneously via Aho-Corasick.
     */
    public List<SearchResult> searchByMultipleKeywords(List<String> keywords) {
        List<SearchResult> results = new ArrayList<>();
        if (keywords == null || keywords.isEmpty()) return results;

        AhoCorasickMatcher ac = new AhoCorasickMatcher(keywords);
        for (Medicine m : repository.getMedicines().values()) {
            String fullText = m.getName() + " " + m.getDescription() + " " + m.getCategory();
            List<AhoCorasickMatcher.Match> matches = ac.search(fullText);
            if (!matches.isEmpty()) {
                results.add(new SearchResult(m, "Aho-Corasick Multi-Keyword Match",
                        "Matched keywords: " + matches));
            }
        }
        return results;
    }

    /**
     * General search by name, code, category, or description.
     */
    public List<SearchResult> search(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        String q = query.trim();

        // 1. Exact ID check
        Medicine exact = repository.getMedicine(q);
        if (exact != null) {
            return Collections.singletonList(new SearchResult(exact, "Exact ID Match", "Matched medicine ID " + exact.getMedicineId()));
        }

        List<SearchResult> byName = searchByName(q);
        if (!byName.isEmpty()) return byName;

        List<SearchResult> byCode = searchByProductCode(q);
        if (!byCode.isEmpty()) return byCode;

        return searchByDescription(q);
    }

    /**
     * Suggests closest medicine names using Levenshtein / Damerau-Levenshtein distance.
     */
    public List<EditDistance.Suggestion> suggestSpellingCorrections(String query, int topK) {
        List<String> names = new ArrayList<>();
        for (Medicine m : repository.getMedicines().values()) {
            names.add(m.getName());
        }
        return EditDistance.findSuggestions(query, names, topK, null);
    }
}
