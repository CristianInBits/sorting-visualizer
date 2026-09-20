package app.algorithms;

/**
 * Receives the steps a sorting algorithm takes, one at a time.
 *
 * Algorithms own the array and mutate it themselves; they report here what
 * they just did. That keeps the algorithms plain Java -- no JavaFX, no
 * threads, no timing -- so the same code can drive an animation, a unit
 * test or a benchmark depending on which implementation is passed in.
 *
 * The reporting methods may throw {@link StoppedException} to abort the run.
 * Implementations that animate also block here for the frame delay and for
 * step-by-step mode, so an algorithm's pacing comes entirely from its trace.
 */
public interface SortTrace {

    /** Reports that the values at the two positions were compared. */
    void compared(int i, int j) throws StoppedException;

    /** Reports that the values at the two positions were just exchanged. */
    void swapped(int i, int j) throws StoppedException;

    /** Reports that the given value was just written at the given position. */
    void wrote(int index, int value) throws StoppedException;

    /**
     * Flags a position as algorithmically special -- a pivot, or the current
     * minimum -- until {@link #unmark(int)}. Reporting only, never pauses.
     */
    void mark(int index);

    /** Releases a flag set by {@link #mark(int)}. */
    void unmark(int index);
}
