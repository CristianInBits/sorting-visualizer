package app.algorithms;

/** Repeatedly walks the array, bubbling each largest value to the end. */
public class BubbleSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        for (int i = 0; i < values.length - 1; i++) {
            for (int j = 0; j < values.length - i - 1; j++) {
                trace.compared(j, j + 1);

                if (values[j] > values[j + 1]) {
                    int temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                    trace.swapped(j, j + 1);
                }
            }
        }
    }
}
