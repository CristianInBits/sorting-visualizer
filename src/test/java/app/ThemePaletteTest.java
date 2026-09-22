package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the stylesheets agree with one another.
 *
 * base.css holds no colours, only names it looks up; light.css and dark.css
 * each have to supply every one of them. A name missing from a palette raises
 * no error anywhere -- JavaFX logs a warning and draws that part of the window
 * without the colour -- so nothing else would catch it. Plain file reading, no
 * toolkit.
 */
class ThemePaletteTest {

    private static final Pattern LOOKUP = Pattern.compile("-sv-[a-z0-9-]+");
    private static final Pattern DEFINITION = Pattern.compile("(-sv-[a-z0-9-]+)\\s*:");
    private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    @Test
    @DisplayName("every colour base.css looks up is defined by both palettes")
    void palettesCoverEverythingBaseUses() throws IOException {
        Set<String> used = matches(LOOKUP, read("/base.css"), 0);
        assertFalse(used.isEmpty(), "base.css should be looking its colours up");

        for (String palette : List.of("/light.css", "/dark.css")) {
            Set<String> missing = new TreeSet<>(used);
            missing.removeAll(matches(DEFINITION, read(palette), 1));
            assertTrue(missing.isEmpty(), palette + " does not define " + missing);
        }
    }

    @Test
    @DisplayName("the two palettes define exactly the same colours")
    void palettesMatch() throws IOException {
        assertEquals(matches(DEFINITION, read("/light.css"), 1), matches(DEFINITION, read("/dark.css"), 1),
                "a colour one palette defines and the other does not");
    }

    private static String read(String resource) throws IOException {
        try (InputStream in = ThemePaletteTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource + " is not on the classpath");
            String css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return COMMENT.matcher(css).replaceAll("");
        }
    }

    private static Set<String> matches(Pattern pattern, String text, int group) {
        Set<String> found = new TreeSet<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group(group));
        }
        return found;
    }
}
