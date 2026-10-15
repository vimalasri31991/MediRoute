package mediroute.algorithms.suffix;

import java.util.*;

/**
 * Implementation of a Multi-Document Suffix Tree.
 * Correctly isolates concatenated documents using unique end-of-document boundary tokens
 * so substring matches never erroneously cross between different medical records.
 */
public class SuffixTree {

    public static class MatchLocation {
        private final int docId;
        private final int offset;

        public MatchLocation(int docId, int offset) {
            this.docId = docId;
            this.offset = offset;
        }

        public int getDocId() { return docId; }
        public int getOffset() { return offset; }

        @Override
        public String toString() {
            return String.format("Doc#%d at offset %d", docId, offset);
        }
    }

    private static class Node {
        final Map<Character, Edge> edges = new TreeMap<>();
        final List<MatchLocation> leaves = new ArrayList<>();
    }

    private static class Edge {
        final int start;
        int end;
        Node next;

        Edge(int start, int end, Node next) {
            this.start = start;
            this.end = end;
            this.next = next;
        }
    }

    private final String fullText;
    private final Node root;
    private final List<Integer> docStartOffsets = new ArrayList<>();
    private final List<Integer> docLengths = new ArrayList<>();

    public SuffixTree() {
        this.fullText = "";
        this.root = new Node();
    }

    /**
     * Builds a generalized suffix tree from a list of document strings.
     * Inserts unique non-printable delimiter tokens ('\u0001', '\u0002', ...) to prevent cross-document matches.
     */
    public SuffixTree(List<String> documents) {
        this.root = new Node();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            String doc = documents.get(i);
            docStartOffsets.add(sb.length());
            docLengths.add(doc.length());
            sb.append(doc);
            // Append document boundary delimiter
            sb.append((char) ('\uE000' + i));
        }

        this.fullText = sb.toString();
        buildTree();
    }

    /**
     * Constructor for a single document.
     */
    public SuffixTree(String singleDocument) {
        this(Collections.singletonList(singleDocument));
    }

    private void buildTree() {
        int n = fullText.length();
        for (int i = 0; i < n; i++) {
            // Determine which document this index belongs to
            int docId = getDocIdForIndex(i);
            int localOffset = i - docStartOffsets.get(docId);
            insertSuffix(i, new MatchLocation(docId, localOffset));
        }
    }

    private int getDocIdForIndex(int globalIdx) {
        for (int d = docStartOffsets.size() - 1; d >= 0; d--) {
            if (globalIdx >= docStartOffsets.get(d)) {
                return d;
            }
        }
        return 0;
    }

    private void insertSuffix(int suffixStart, MatchLocation location) {
        Node curr = root;
        int i = suffixStart;
        int n = fullText.length();

        while (i < n) {
            char ch = fullText.charAt(i);
            Edge edge = curr.edges.get(ch);

            if (edge == null) {
                // New branch
                Node leaf = new Node();
                leaf.leaves.add(location);
                curr.edges.put(ch, new Edge(i, n, leaf));
                return;
            }

            int edgeStart = edge.start;
            int edgeEnd = edge.end;
            int k = edgeStart;

            while (k < edgeEnd && i < n && fullText.charAt(k) == fullText.charAt(i)) {
                k++;
                i++;
            }

            if (k == edgeEnd) {
                // Fully matched this edge, proceed to next node
                curr = edge.next;
                curr.leaves.add(location);
            } else {
                // Split edge
                Node splitNode = new Node();
                splitNode.leaves.addAll(edge.next.leaves);
                splitNode.leaves.add(location);

                Edge splitEdge = new Edge(k, edgeEnd, edge.next);
                splitNode.edges.put(fullText.charAt(k), splitEdge);

                edge.end = k;
                edge.next = splitNode;

                if (i < n) {
                    Node leafNode = new Node();
                    leafNode.leaves.add(location);
                    splitNode.edges.put(fullText.charAt(i), new Edge(i, n, leafNode));
                }
                return;
            }
        }
    }

    /**
     * Checks if a substring exists in any document without crossing boundary markers.
     */
    public boolean containsSubstring(String pattern) {
        return !search(pattern).isEmpty();
    }

    /**
     * Searches for pattern and returns all valid document match locations.
     */
    public List<MatchLocation> search(String pattern) {
        List<MatchLocation> results = new ArrayList<>();
        if (pattern == null || pattern.isEmpty() || fullText.isEmpty()) {
            return results;
        }

        // Substring cannot contain boundary sentinels
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) >= '\uE000') {
                return results;
            }
        }

        Node curr = root;
        int i = 0;
        int m = pattern.length();

        while (i < m) {
            char ch = pattern.charAt(i);
            Edge edge = curr.edges.get(ch);
            if (edge == null) {
                return results; // Mismatch
            }

            int k = edge.start;
            while (k < edge.end && i < m) {
                if (fullText.charAt(k) != pattern.charAt(i)) {
                    return results; // Mismatch inside edge
                }
                k++;
                i++;
            }

            curr = edge.next;
        }

        // Collect leaves from matched subtree, verifying that match does not cross boundaries
        Set<String> seen = new HashSet<>();
        for (MatchLocation loc : curr.leaves) {
            int docId = loc.getDocId();
            int offset = loc.getOffset();
            if (offset + pattern.length() <= docLengths.get(docId)) {
                String key = docId + ":" + offset;
                if (!seen.contains(key)) {
                    seen.add(key);
                    results.add(loc);
                }
            }
        }

        return results;
    }
}
