package app.algorithms;

/**
 * Builds a max heap over the array, then repeatedly moves its root to the end.
 *
 * The array doubles as the heap: the children of the node at {@code i} sit at
 * {@code 2i + 1} and {@code 2i + 2}. Everything is done with exchanges, so
 * every intermediate state is a permutation of the input.
 */
public class HeapSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        int length = values.length;

        // Build the heap bottom up. Nodes past length / 2 are leaves, which
        // are already heaps of one element.
        for (int root = length / 2 - 1; root >= 0; root--) {
            siftDown(values, trace, root, length);
        }

        // The root is now the largest value. Swap it to the end, shrink the
        // heap by one, and sift the new root back down.
        for (int end = length - 1; end > 0; end--) {
            swap(values, 0, end);
            trace.swapped(0, end);

            siftDown(values, trace, 0, end);
        }
    }

    /** Walks the value at {@code root} down until the subtree is a heap again. */
    private void siftDown(int[] values, SortTrace trace, int root, int end) throws StoppedException {
        trace.mark(root);

        while (2 * root + 1 < end) {
            int child = 2 * root + 1;
            int right = child + 1;

            if (right < end) {
                trace.compared(child, right);
                if (values[child] < values[right]) {
                    child = right;
                }
            }

            trace.compared(root, child);
            if (values[root] >= values[child]) {
                break;
            }

            swap(values, root, child);
            trace.swapped(root, child);

            trace.unmark(root);
            root = child;
            trace.mark(root);
        }

        trace.unmark(root);
    }

    private void swap(int[] values, int i, int j) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;
    }
}
