package app.algorithms;

/**
 * A {@link SortTrace} for tests.
 *
 * Besides counting events, it replays every reported step onto its own copy
 * of the original array. If an algorithm and its narration agree, that mirror
 * ends up identical to the array the algorithm produced -- which is exactly
 * the assumption the on-screen animation depends on.
 */
public class RecordingTrace implements SortTrace {

    private final int[] mirror;
    private final boolean[] marked;

    private int comparisons;
    private int moves;
    private int steps;
    private int stopAfter = Integer.MAX_VALUE;

    public RecordingTrace(int[] original) {
        this.mirror = original.clone();
        this.marked = new boolean[original.length];
    }

    /** Makes this trace abort the run once that many steps have been reported. */
    public RecordingTrace stoppingAfter(int step) {
        this.stopAfter = step;
        return this;
    }

    @Override
    public void compared(int i, int j) throws StoppedException {
        checkRange(i);
        checkRange(j);
        comparisons++;
        step();
    }

    @Override
    public void swapped(int i, int j) throws StoppedException {
        checkRange(i);
        checkRange(j);

        int temp = mirror[i];
        mirror[i] = mirror[j];
        mirror[j] = temp;

        if (mirror[i] != mirror[j]) {
            moves += 2;
        }
        step();
    }

    @Override
    public void wrote(int index, int value) throws StoppedException {
        checkRange(index);

        if (mirror[index] != value) {
            moves++;
        }
        mirror[index] = value;
        step();
    }

    @Override
    public void mark(int index) {
        checkRange(index);
        marked[index] = true;
    }

    @Override
    public void unmark(int index) {
        checkRange(index);
        marked[index] = false;
    }

    private void step() throws StoppedException {
        steps++;
        if (steps >= stopAfter) {
            throw new StoppedException();
        }
    }

    private void checkRange(int index) {
        if (index < 0 || index >= mirror.length) {
            throw new AssertionError("reported index " + index + " outside 0.." + (mirror.length - 1));
        }
    }

    /** The array rebuilt purely from the reported steps. */
    public int[] mirror() {
        return mirror.clone();
    }

    public int comparisons() {
        return comparisons;
    }

    public int moves() {
        return moves;
    }

    public int steps() {
        return steps;
    }

    /** Positions still flagged when the run ended; should be none. */
    public int stillMarked() {
        int n = 0;
        for (boolean m : marked) {
            if (m) {
                n++;
            }
        }
        return n;
    }
}
