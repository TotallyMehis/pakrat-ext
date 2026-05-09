package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class SwabTest {
    @CsvSource("""
            0,0
            1,256
            2,512
            4,1024
            8,2048
            16,4096
            32,8192
            64,16384
            128,32768
            256,1
            512,2
            1024,4
            2048,8
            4096,16
            8192,32
            16384,64
            32768,128
            """)
    @ParameterizedTest
    void unsignedShort(int in, int expected) {
        assertEquals(expected, Swab.unsignedShort(in));
    }
}
