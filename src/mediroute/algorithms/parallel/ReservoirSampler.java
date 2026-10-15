package mediroute.algorithms.parallel;

import java.util.*;

/**
 * Streaming Reservoir Sampling Algorithm (Algorithm R by Alan G. Waterman / Jeffrey Vitter).
 * Samples k uniform items from an unbounded or single-pass data stream without storing the full stream.
 *
 * Time Complexity: O(N) where N is the stream length.
 * Space Complexity: O(k) for the reservoir buffer.
 */
public class ReservoirSampler {

    private final Random rng;

    public ReservoirSampler() {
        this.rng = new Random();
    }

    public ReservoirSampler(long seed) {
        this.rng = new Random(seed);
    }

    /**
     * Samples at most k items uniformly from an iterator stream.
     */
    public <T> List<T> sample(Iterator<T> stream, int k) {
        if (k <= 0) {
            return Collections.emptyList();
        }

        List<T> reservoir = new ArrayList<>(k);
        int count = 0;

        while (stream.hasNext()) {
            T item = stream.next();
            count++;

            if (reservoir.size() < k) {
                reservoir.add(item);
            } else {
                // Pick a random integer in [0, count - 1]
                int r = rng.nextInt(count);
                if (r < k) {
                    reservoir.set(r, item);
                }
            }
        }

        return reservoir;
    }

    /**
     * Convenience method for sampling from an Iterable/Collection.
     */
    public <T> List<T> sample(Iterable<T> iterable, int k) {
        return sample(iterable.iterator(), k);
    }
}
