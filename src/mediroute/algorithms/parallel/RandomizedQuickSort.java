package mediroute.algorithms.parallel;

import java.util.*;

/**
 * In-Place Randomized QuickSort Algorithm.
 * Selects pivots uniformly at random to guarantee expected O(N log N) runtime
 * regardless of initial input ordering.
 */
public class RandomizedQuickSort {

    private static final Random RNG = new Random();

    /**
     * Sorts a list in place using Randomized QuickSort and a custom Comparator.
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        if (list == null || list.size() <= 1) return;
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSort(List<T> list, int low, int high, Comparator<? super T> comparator) {
        if (low < high) {
            int pIndex = randomizedPartition(list, low, high, comparator);
            quickSort(list, low, pIndex - 1, comparator);
            quickSort(list, pIndex + 1, high, comparator);
        }
    }

    private static <T> int randomizedPartition(List<T> list, int low, int high, Comparator<? super T> comparator) {
        int randomPivot = low + RNG.nextInt(high - low + 1);
        swap(list, randomPivot, high);
        return partition(list, low, high, comparator);
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<? super T> comparator) {
        T pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
