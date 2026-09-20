package app;

import org.junit.jupiter.api.Test;

/** Throwaway: which glass platform is actually in use? */
class PlatformProbeTest {

    @Test
    void reportPlatform() {
        System.out.println("PROBE os=" + System.getProperty("os.name")
                + " display=" + System.getenv("DISPLAY"));
        try {
            Fx.startToolkit();
            System.out.println("PROBE startToolkit OK");
        } catch (Throwable t) {
            System.out.println("PROBE startToolkit FAILED: " + t);
            t.printStackTrace(System.out);
        }
        System.out.println("PROBE glass.platform=" + System.getProperty("glass.platform"));
        System.out.println("PROBE monocle.platform=" + System.getProperty("monocle.platform"));
        try {
            Class<?> app = Class.forName("com.sun.glass.ui.Application");
            Object current = app.getMethod("GetApplication").invoke(null);
            System.out.println("PROBE toolkit=" + (current == null ? "null" : current.getClass().getName()));
        } catch (Throwable t) {
            System.out.println("PROBE toolkit lookup failed: " + t);
        }
        try {
            Class.forName("com.sun.glass.ui.monocle.MonocleApplication");
            System.out.println("PROBE monocle classes present");
        } catch (Throwable t) {
            System.out.println("PROBE monocle classes NOT on classpath: " + t.getClass().getSimpleName());
        }
    }
}
