package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

final class UtilTest {
    @CsvSource(
        """
        filename.txt,txt
        filename.mdl,mdl
        /path/filename.mdl,mdl
        FILENAME.MDL,mdl
        /PATH/TO/FILENAME.TXT,txt
        """
    )
    @ParameterizedTest
    void getExtension(String path, String expected) {
        assertEquals(expected, Util.getExtension(new File(path)));
    }

    @CsvSource(
        """
        /path/to/materials/example.vmt,materials/example.vmt
        /path/to/models/sub/example.mdl,models/sub/example.mdl
        /path/to/sound/sub/example.mp3,sound/sub/example.mp3
        /path/to/materials/models/example.vmt,materials/models/example.vmt
        /path/to/maps/example.txt,maps/example.txt
        /path/to/scripts/soundscripts_example.txt,scripts/soundscripts_example.txt
        """
    )
    @ParameterizedTest
    void getRelativePathNoRoot(String path, String expected) {
        assertEquals(expected, Util.getRelativePath(path, null));
    }

    @ValueSource(strings = {"/unknown/path/example.vmt", "materials.vmt"})
    @ParameterizedTest
    void getRelativePathNone(String path) {
        assertEquals(null, Util.getRelativePath(path, null));
    }
}
