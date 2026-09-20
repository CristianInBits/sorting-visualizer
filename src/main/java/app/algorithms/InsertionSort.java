package app.algorithms;

/**
 * Grows a sorted prefix, walking each new value left into its place.
 *
 * Written with exchanges rather than the shift-and-drop variant. Shifting
 * lifts the value out into a temporary and leaves a hole behind, so for the
 * length of the inner loop the array holds a duplicate and is missing an
 * element. Exchanging keeps every intermediate state a genuine permutation
 * of the input, which is what the animation draws and what the tests assert
 * after stopping a run at an arbitrary step.
 */
public class InsertionSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        for (int i = 1; i < values.length; i++) {
            int j = i;
            trace.mark(j);

            while (j > 0) {
                trace.compared(j - 1, j);

                if (values[j - 1] <= values[j]) {
                    break;
                }

                int temp = values[j];
                values[j] = values[j - 1];
                values[j - 1] = temp;
                trace.swapped(j, j - 1);

                trace.unmark(j);
                j--;
                trace.mark(j);
            }

            trace.unmark(j);
        }
    }
}
