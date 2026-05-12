package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class ScanfileTypeTest {
    @CsvSource("""
            filename.unknown,NUL
            filename.vmt,VMT
            filename.vtf,VTF
            filename.mdl,MDL
            filename.vtx,VTX
            filename.phy,PHY
            filename.vvd,VVD
            filename.wav,WAV
            filename.mp3,MP3
            filename.nav,NAV
            filename.ain,AIN
            filename.txt,TXT
            filename.cache,CACHE
            FILENAME.TXT,TXT
            """)
    @ParameterizedTest
    void filenameToType(String fileName, ScanfileType expected) {
        assertEquals(expected, ScanfileType.getTypeFromFilename(fileName));
    }
}
