package mediroute.algorithms.suffix;

import java.util.*;

/**
 * Implementation of Suffix Automaton (SAM / Directed Acyclic Word Graph).
 * A minimal DFA accepting all substrings of a text string.
 *
 * Time Complexity: O(N) linear time construction, O(M) substring queries.
 * Space Complexity: O(N * Sigma) <= 2N states and 3N transitions.
 */
public class SuffixAutomaton {

    public static class State {
        int len;
        int link;
        int firstEndPos;
        final Map<Character, Integer> next = new HashMap<>();

        State(int len, int link, int firstEndPos) {
            this.len = len;
            this.link = link;
            this.firstEndPos = firstEndPos;
        }
    }

    private final List<State> states = new ArrayList<>();
    private int last;
    private final String text;

    public SuffixAutomaton(String text) {
        this.text = text == null ? "" : text;
        // Initial root state (state 0)
        states.add(new State(0, -1, -1));
        this.last = 0;

        for (int i = 0; i < this.text.length(); i++) {
            extend(this.text.charAt(i), i);
        }
    }

    private void extend(char c, int index) {
        int cur = states.size();
        states.add(new State(states.get(last).len + 1, -1, index));

        int p = last;
        while (p != -1 && !states.get(p).next.containsKey(c)) {
            states.get(p).next.put(c, cur);
            p = states.get(p).link;
        }

        if (p == -1) {
            states.get(cur).link = 0;
        } else {
            int q = states.get(p).next.get(c);
            if (states.get(p).len + 1 == states.get(q).len) {
                states.get(cur).link = q;
            } else {
                int clone = states.size();
                State cloneState = new State(states.get(p).len + 1, states.get(q).link, states.get(q).firstEndPos);
                cloneState.next.putAll(states.get(q).next);
                states.add(cloneState);

                while (p != -1 && states.get(p).next.get(c) == q) {
                    states.get(p).next.put(c, clone);
                    p = states.get(p).link;
                }

                states.get(q).link = clone;
                states.get(cur).link = clone;
            }
        }

        last = cur;
    }

    /**
     * Checks if pattern exists as a substring in O(|pattern|) time.
     */
    public boolean contains(String pattern) {
        if (pattern == null || pattern.isEmpty()) return true;
        int curr = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!states.get(curr).next.containsKey(c)) {
                return false;
            }
            curr = states.get(curr).next.get(c);
        }
        return true;
    }

    /**
     * Returns the 0-based starting index of the first occurrence of pattern, or -1 if not found.
     */
    public int indexOf(String pattern) {
        if (pattern == null || pattern.isEmpty()) return 0;
        int curr = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!states.get(curr).next.containsKey(c)) {
                return -1;
            }
            curr = states.get(curr).next.get(c);
        }
        return states.get(curr).firstEndPos - pattern.length() + 1;
    }

    public int getStateCount() {
        return states.size();
    }
}
