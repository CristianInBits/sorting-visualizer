package app.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Runs every algorithm headlessly. No JavaFX toolkit is started, which is the
 * whole point of keeping the algorithms free of the user interface.
 */
class SortAlgorithmTest {

    static Stream<Arguments> algorithms() {
        return Stream.of(
                arguments(named("Bubble Sort", new BubbleSort())),
                arguments(named("Selection Sort", new SelectionSort())),
                arguments(named("Quick Sort", new QuickSort())),
                arguments(named("Merge Sort", new MergeSort())));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void sortsRandomArrays(SortAlgorithm algorithm) throws StoppedException {
        Random random = new Random(20250920);

        for (int run = 0; run < 200; run++) {
            int[] original = randomArray(random, random.nextInt(60) + 1);
            int[] values = original.clone();
            RecordingTrace trace = new RecordingTrace(original);

            algorithm.sort(values, trace);

            assertSortedPermutationOf(original, values);
            assertArrayEquals(values, trace.mirror(),
                    "the reported steps do not rebuild the array the algorithm produced");
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void handlesEdgeCases(SortAlgorithm algorithm) throws StoppedException {
        int[][] cases = {
                {},
                {1},
                {2, 1},
                {1, 2},
                {7, 7, 7, 7, 7},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {8, 7, 6, 5, 4, 3, 2, 1},
                {5, 1, 5, 1, 5, 1, 5, 1},
                {Integer.MIN_VALUE, 0, Integer.MAX_VALUE, -1, 1},
        };

        for (int[] original : cases) {
            int[] values = original.clone();
            RecordingTrace trace = new RecordingTrace(original);

            algorithm.sort(values, trace);

            assertSortedPermutationOf(original, values);
            assertArrayEquals(values, trace.mirror(),
                    "steps do not rebuild " + Arrays.toString(original));
            assertEquals(0, trace.stillMarked(),
                    "run finished leaving a position marked in " + Arrays.toString(original));
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void instancesAreReusable(SortAlgorithm algorithm) throws StoppedException {
        int[] first = {5, 3, 9, 1};
        int[] second = {4, 4, 2, 8, 0};

        algorithm.sort(first, new RecordingTrace(first.clone()));
        algorithm.sort(second, new RecordingTrace(second.clone()));

        assertArrayEquals(new int[] {1, 3, 5, 9}, first);
        assertArrayEquals(new int[] {0, 2, 4, 4, 8}, second);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void stoppingAbortsTheRunAtEveryStep(SortAlgorithm algorithm) throws StoppedException {
        int[] original = randomArray(new Random(7), 24);

        // How many steps a full run takes.
        RecordingTrace full = new RecordingTrace(original);
        algorithm.sort(original.clone(), full);

        for (int stopAt = 1; stopAt <= full.steps(); stopAt++) {
            int[] values = original.clone();
            RecordingTrace trace = new RecordingTrace(original).stoppingAfter(stopAt);

            assertThrows(StoppedException.class, () -> algorithm.sort(values, trace));

            // A stopped run leaves the array half-done, but never corrupts it.
            assertArrayEquals(sorted(original), sorted(values),
                    "values were lost or invented after stopping at step " + stopAt);
        }
    }

    private static void assertSortedPermutationOf(int[] original, int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i - 1] > values[i]) {
                throw new AssertionError("not sorted at index " + i + ": " + Arrays.toString(values));
            }
        }
        assertArrayEquals(sorted(original), values, "result is not a permutation of the input");
    }

    private static int[] sorted(int[] values) {
        int[] copy = values.clone();
        Arrays.sort(copy);
        return copy;
    }

    private static int[] randomArray(Random random, int length) {
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = random.nextInt(300) + 10;
        }
        return values;
    }
}
