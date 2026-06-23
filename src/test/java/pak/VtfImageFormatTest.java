package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

final class VtfImageFormatTest {
    @Test
    void from() {
        Set<VtfImageFormat> imageFormats = EnumSet.allOf(VtfImageFormat.class);
        for (int i = 0; i <= 29; i++) {
            VtfImageFormat format = VtfImageFormat.from(i).orElseThrow();
            assertEquals(true, imageFormats.contains(format));
            assertDoesNotThrow(() -> format.getPixelSizeInBytes());
        }
    }

    @Test
    void name() {
        assertEquals("RGBA8888", VtfImageFormat.from(0).orElseThrow().getName());
    }
}
