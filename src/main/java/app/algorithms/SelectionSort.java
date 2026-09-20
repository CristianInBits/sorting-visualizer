package app.algorithms;

/** Selects the smallest remaining value and swaps it into place. */
public class SelectionSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        for (int i = 0; i < values.length - 1; i++) {
            int min = i;
            trace.mark(min);

            for (int j = i + 1; j < values.length; j++) {
                trace.compared(j, min);

                if (values[j] < values[min]) {
                    trace.unmark(min);
                    min = j;
                    trace.mark(min);
                }
            }

            if (min != i) {
                int temp = values[i];
                values[i] = values[min];
                values[min] = temp;
                trace.swapped(i, min);
            }

            trace.unmark(min);
        }
    }
}
