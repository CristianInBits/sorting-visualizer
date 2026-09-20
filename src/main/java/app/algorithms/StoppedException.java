package app.algorithms;

/**
 * Thrown by a {@link SortTrace} when the user has asked the run to stop.
 *
 * Algorithms never catch this: they let it propagate, which unwinds the
 * recursion and ends the sort wherever it happened to be. That replaces the
 * isStopRequested() checks that used to be sprinkled through every loop.
 */
public class StoppedException extends Exception {

    public StoppedException() {
        super("sorting run stopped");
    }
}
