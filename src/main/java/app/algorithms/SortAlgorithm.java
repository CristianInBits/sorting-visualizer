package app.algorithms;

/**
 * A sorting algorithm, free of any user interface.
 *
 * Implementations are stateless and reusable: everything they need arrives
 * as arguments, so one instance can serve every run.
 */
public interface SortAlgorithm {

    /**
     * Sorts {@code values} in place, ascending, reporting every step to
     * {@code trace}.
     *
     * @throws StoppedException if the trace aborts the run
     */
    void sort(int[] values, SortTrace trace) throws StoppedException;
}
