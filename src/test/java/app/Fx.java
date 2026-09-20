package app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javafx.application.Platform;

/**
 * Runs test code on the JavaFX thread.
 *
 * The toolkit is started in headless mode through Monocle, configured by the
 * system properties surefire passes, so these tests need no display and run
 * on a CI runner unchanged.
 */
public final class Fx {

    private static final long TIMEOUT_SECONDS = 15;

    private Fx() {
    }

    /** Safe to call from every test class: the second call onwards is a no-op. */
    public static void startToolkit() {
        requireMonocle();

        CountDownLatch up = new CountDownLatch(1);
        try {
            Platform.startup(up::countDown);
        } catch (IllegalStateException alreadyRunning) {
            return;
        }
        await(up, "the JavaFX toolkit never started");
    }

    /**
     * Fails early and clearly when the headless toolkit cannot be loaded.
     *
     * Worth the few lines: without Monocle, Platform.startup() does not throw,
     * it blocks inside native code where no JUnit timeout can reach it, and
     * the suite hangs for as long as CI will let it. A build once burned
     * seven minutes before anyone could see that the only problem was a
     * Monocle built for a newer Java than the one running it.
     */
    private static void requireMonocle() {
        try {
            Class.forName("com.sun.glass.ui.monocle.MonoclePlatformFactory");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Monocle is missing from the test classpath", e);
        } catch (UnsupportedClassVersionError e) {
            throw new AssertionError("Monocle was built for a newer Java than this one."
                    + " Running on " + System.getProperty("java.version")
                    + "; either match the monocle.version in the pom to it, or build on a newer JDK.", e);
        }
    }

    /** Runs the work on the JavaFX thread and waits for it, rethrowing failures. */
    public static void onFx(Runnable work) {
        if (Platform.isFxApplicationThread()) {
            work.run();
            return;
        }

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                work.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        await(done, "the work never ran on the JavaFX thread");

        if (failure.get() != null) {
            throw new AssertionError("failed on the JavaFX thread", failure.get());
        }
    }

    /** As {@link #onFx(Runnable)}, for work that produces a value. */
    public static <T> T get(Supplier<T> work) {
        AtomicReference<T> result = new AtomicReference<>();
        onFx(() -> result.set(work.get()));
        return result.get();
    }

    /** Lets every update already queued on the JavaFX thread finish. */
    public static void settle() {
        onFx(() -> {
        });
    }

    private static void await(CountDownLatch latch, String message) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(message, e);
        }
    }
}
