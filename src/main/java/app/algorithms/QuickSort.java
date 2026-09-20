package app.algorithms;

/** Lomuto partitioning around the last element of each range. */
public class QuickSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        quickSort(values, trace, 0, values.length - 1);
    }

    private void quickSort(int[] values, SortTrace trace, int low, int high) throws StoppedException {
        if (low < high) {
            int pivotIndex = partition(values, trace, low, high);
            quickSort(values, trace, low, pivotIndex - 1);
            quickSort(values, trace, pivotIndex + 1, high);
        }
    }

    private int partition(int[] values, SortTrace trace, int low, int high) throws StoppedException {
        int pivot = values[high];
        trace.mark(high);

        int i = low - 1;
        for (int j = low; j < high; j++) {
            trace.compared(j, high);

            if (values[j] < pivot) {
                i++;
                swap(values, i, j);
                trace.swapped(i, j);
            }
        }

        swap(values, i + 1, high);
        trace.unmark(high);
        trace.swapped(i + 1, high);

        return i + 1;
    }

    private void swap(int[] values, int i, int j) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;
    }
}
