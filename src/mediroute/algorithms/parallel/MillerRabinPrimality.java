package mediroute.algorithms.parallel;

import java.math.BigInteger;
import java.util.Random;

/**
 * Miller-Rabin Probabilistic Primality Testing Algorithm with Modular Exponentiation.
 * Used for synthetic batch code verification and cryptographic authenticity validation.
 *
 * Time Complexity: O(k log^3 n) where k is the number of testing rounds.
 */
public class MillerRabinPrimality {

    private static final Random RNG = new Random();

    // Deterministic bases sufficient for all 64-bit integers
    private static final long[] DETERMINISTIC_BASES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};

    /**
     * Modular Exponentiation: (base^exp) % mod using repeated squaring.
     */
    public static long modPow(long base, long exp, long mod) {
        if (mod <= 0) throw new IllegalArgumentException("Modulus must be positive");
        BigInteger b = BigInteger.valueOf(base);
        BigInteger e = BigInteger.valueOf(exp);
        BigInteger m = BigInteger.valueOf(mod);
        return b.modPow(e, m).longValue();
    }

    /**
     * Tests if n is prime using the Miller-Rabin algorithm.
     *
     * @param n Number to test
     * @param rounds Number of random testing bases (or uses deterministic bases if small)
     * @return true if probably prime, false if definitely composite
     */
    public static boolean isPrime(long n, int rounds) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0) return false;

        // Write n - 1 as d * 2^s where d is odd
        long d = n - 1;
        int s = 0;
        while (d % 2 == 0) {
            d /= 2;
            s++;
        }

        // Test using bases
        if (rounds <= 0) rounds = DETERMINISTIC_BASES.length;

        for (int i = 0; i < rounds; i++) {
            long a;
            if (i < DETERMINISTIC_BASES.length && DETERMINISTIC_BASES[i] < n) {
                a = DETERMINISTIC_BASES[i];
            } else {
                a = 2 + (Math.abs(RNG.nextLong()) % (n - 3));
            }

            if (!millerRabinRound(a, d, s, n)) {
                return false; // Definitely composite
            }
        }

        return true; // Probably prime with error probability <= 4^(-rounds)
    }

    private static boolean millerRabinRound(long a, long d, int s, long n) {
        long x = modPow(a, d, n);
        if (x == 1 || x == n - 1) {
            return true;
        }

        for (int r = 1; r < s; r++) {
            x = modPow(x, 2, n);
            if (x == n - 1) {
                return true;
            }
        }

        return false;
    }
}
