package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class ColRGBA8888Test {
    @CsvSource("""
            255,255,255,255,255,-1,65535
            128,128,132,16,0,-1,32896
            200,5,0,186,66,-1,1480
            32,0,0,4,0,-1,32
            """)
    @ParameterizedTest
    void color(int byte0, int byte1, int red, int green, int blue, int alpha, int c565) {
        ColRGBA8888 color = ColRGBA8888.from565(byte0, byte1);

        assertEquals(red, color.r());
        assertEquals(green, color.g());
        assertEquals(blue, color.b());
        assertEquals(alpha, color.a());
        assertEquals(c565, color.c565());
    }
}
