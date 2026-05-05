package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class FileTypeTest {
    @CsvSource("""
            file.txt,TEXT
            file.vmt,MATERIAL
            file.vtf,TEXTURE
            file.mdl,MODEL
            file.vvd,MODEL_DAT
            file.ani,MODEL_DAT
            file.vtx,MODEL_DAT
            file.mp3,SOUND
            file.wav,SOUND
            file.asdf,OTHER
            """)
    @ParameterizedTest
    void fromFileName(String fileName, FileType expected) {
        assertEquals(expected, FileType.from(fileName));
    }
}
