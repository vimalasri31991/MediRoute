package mediroute.service;

import mediroute.algorithms.strings.ZAlgorithm;
import mediroute.algorithms.suffix.*;
import mediroute.data.DataRepository;
import mediroute.model.Medicine;

import java.util.*;

/**
 * Service for indexing and analyzing medical documents and product descriptions.
 * Integrates Suffix Array, SA-IS, Kasai LCP, Multi-Document Suffix Tree, Suffix Automaton, and Z-Function.
 */
public class DocumentAnalysisService {

    private final DataRepository repository;
    private final List<String> documents = new ArrayList<>();
    private final List<String> documentHeaders = new ArrayList<>();
    private SuffixTree suffixTree;
    private String combinedCorpus;
    private int[] suffixArray;
    private int[] saIsArray;
    private int[] lcpArray;

    public DocumentAnalysisService(DataRepository repository) {
        this.repository = repository;
        initializeCorpus();
    }

    private void initializeCorpus() {
        documents.clear();
        documentHeaders.clear();
        StringBuilder corpusBuilder = new StringBuilder();

        for (Medicine m : repository.getMedicines().values()) {
            String docText = m.getName() + ": " + m.getDescription();
            documents.add(docText);
            documentHeaders.add(m.getMedicineId() + " (" + m.getName() + ")");
            corpusBuilder.append(docText).append(". ");
        }

        this.combinedCorpus = corpusBuilder.toString();
        this.suffixTree = new SuffixTree(documents);

        // Standard Suffix Array and SA-IS
        SuffixArray saObj = new SuffixArray(combinedCorpus);
        this.suffixArray = saObj.getArray();
        this.saIsArray = SAIS.buildSuffixArray(combinedCorpus);

        // Kasai LCP
        this.lcpArray = KasaiLCP.buildLcpArray(combinedCorpus, suffixArray);
    }

    public String getCombinedCorpus() {
        return combinedCorpus;
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    public int[] getSaIsArray() {
        return saIsArray;
    }

    public int[] getLcpArray() {
        return lcpArray;
    }

    /**
     * Searches multi-document suffix tree for a substring query.
     */
    public List<String> searchSuffixTree(String query) {
        List<String> results = new ArrayList<>();
        List<SuffixTree.MatchLocation> matches = suffixTree.search(query);

        for (SuffixTree.MatchLocation loc : matches) {
            int docId = loc.getDocId();
            if (docId >= 0 && docId < documentHeaders.size()) {
                results.add(String.format("Found in %s at offset %d: \"...%s...\"",
                        documentHeaders.get(docId),
                        loc.getOffset(),
                        extractSnippet(documents.get(docId), loc.getOffset(), query.length())));
            }
        }
        return results;
    }

    private String extractSnippet(String doc, int offset, int len) {
        int start = Math.max(0, offset - 15);
        int end = Math.min(doc.length(), offset + len + 15);
        return doc.substring(start, end).replace("\n", " ");
    }

    /**
     * Finds repeated description fragments using Kasai LCP.
     */
    public List<KasaiLCP.RepeatedFragment> findRepeatedFragments(int minLength) {
        return KasaiLCP.findRepeatedFragments(combinedCorpus, suffixArray, lcpArray, minLength);
    }

    /**
     * Analyzes repeating prefix patterns using the Z-Algorithm.
     */
    public ZAlgorithm.PrefixAnalysisResult analyzeRepeatingPrefix(String text) {
        return ZAlgorithm.analyzeRepeatedPrefix(text);
    }
}
