package mediroute.algorithms.strings;

import java.util.*;

/**
 * Implementation of the Aho-Corasick Multi-Pattern Automaton.
 * Constructs a trie with failure transitions (suffix links) and dictionary output links.
 * Scans text in linear time O(N + sum(matches)) across all dictionary keywords simultaneously.
 */
public class AhoCorasickMatcher {

    public static class Match {
        private final String keyword;
        private final int startIndex;
        private final int endIndex;

        public Match(String keyword, int startIndex, int endIndex) {
            this.keyword = keyword;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getKeyword() { return keyword; }
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }

        @Override
        public String toString() {
            return String.format("'%s' [%d..%d]", keyword, startIndex, endIndex);
        }
    }

    private static class Node {
        final Map<Character, Node> children = new HashMap<>();
        Node fail = null;
        final List<String> outputs = new ArrayList<>();
    }

    private final Node root;
    private boolean built = false;
    private final List<String> keywords = new ArrayList<>();

    public AhoCorasickMatcher() {
        this.root = new Node();
    }

    public AhoCorasickMatcher(Collection<String> patterns) {
        this();
        for (String p : patterns) {
            addPattern(p);
        }
        build();
    }

    /**
     * Adds a keyword pattern to the automaton trie.
     */
    public synchronized void addPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) return;
        String p = pattern.trim();
        keywords.add(p);
        Node current = root;
        for (char ch : p.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new Node());
        }
        if (!current.outputs.contains(p)) {
            current.outputs.add(p);
        }
        built = false;
    }

    /**
     * Builds failure and dictionary links using BFS.
     */
    public synchronized void build() {
        if (built) return;

        Queue<Node> queue = new ArrayDeque<>();

        // Level 1 children fail to root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                // Find failure state for child
                Node f = current.fail;
                while (f != null && !f.children.containsKey(ch)) {
                    f = f.fail;
                }

                if (f != null) {
                    child.fail = f.children.get(ch);
                } else {
                    child.fail = root;
                }

                // Inherit output matches from failure state
                if (child.fail != null) {
                    child.outputs.addAll(child.fail.outputs);
                }

                queue.add(child);
            }
        }

        built = true;
    }

    /**
     * Scans text for all registered keywords.
     *
     * @param text Text to search (case-insensitive search)
     * @return List of Match objects indicating detected keywords and spans
     */
    public List<Match> search(String text) {
        if (!built) {
            build();
        }

        List<Match> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        String lowerText = text.toLowerCase();
        Node current = root;

        for (int i = 0; i < lowerText.length(); i++) {
            char ch = lowerText.charAt(i);

            // Follow failure transitions if character does not match
            while (current != null && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            if (current == null) {
                current = root;
                continue;
            }

            current = current.children.get(ch);

            // Record all matching keywords ending at position i
            for (String kw : current.outputs) {
                int start = i - kw.length() + 1;
                results.add(new Match(kw, start, i));
            }
        }

        return results;
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }
}
