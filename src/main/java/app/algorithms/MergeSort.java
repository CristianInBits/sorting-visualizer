package app.algorithms;

/** Splits the range in half, sorts each half, then merges through a buffer. */
public class MergeSort implements SortAlgorithm {

    @Override
    public void sort(int[] values, SortTrace trace) throws StoppedException {
        mergeSort(values, trace, 0, values.length - 1);
    }

    private void mergeSort(int[] values, SortTrace trace, int left, int right) throws StoppedException {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(values, trace, left, mid);
            mergeSort(values, trace, mid + 1, right);
            merge(values, trace, left, mid, right);
        }
    }

    private void merge(int[] values, SortTrace trace, int left, int mid, int right) throws StoppedException {
        int[] buffer = new int[right - left + 1];
        int i = left;
        int j = mid + 1;
        int k = 0;

        while (i <= mid && j <= right) {
            trace.compared(i, j);

            if (values[i] <= values[j]) {
                buffer[k++] = values[i++];
            } else {
                buffer[k++] = values[j++];
            }
        }

        // Whichever half still has elements is already sorted, so it is
        // copied across without further comparisons.
        while (i <= mid) {
            buffer[k++] = values[i++];
        }
        while (j <= right) {
            buffer[k++] = values[j++];
        }

        // The copy-back must not be abandoned half way. The buffer is a
        // rearrangement of this very range, so writing only part of it back
        // duplicates some values and loses others. Finish the range first,
        // then report the stop.
        StoppedException stopped = null;
        for (int m = 0; m < buffer.length; m++) {
            values[left + m] = buffer[m];

            if (stopped == null) {
                try {
                    trace.wrote(left + m, buffer[m]);
                } catch (StoppedException e) {
                    stopped = e;
                }
            }
        }

        if (stopped != null) {
            throw stopped;
        }
    }
}
