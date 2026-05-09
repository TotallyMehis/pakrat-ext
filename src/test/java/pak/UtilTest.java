package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

final class UtilTest {
    @CsvSource("""
            filename.txt,txt
            filename.mdl,mdl
            /path/filename.mdl,mdl
            FILENAME.MDL,mdl
            /PATH/TO/FILENAME.TXT,txt
            """)
    @ParameterizedTest
    void getExtension(String path, String expected) {
        assertEquals(expected, Util.getExtension(new File(path)));
    }

    @CsvSource("""
            /path/to/materials/example.vmt,materials/example.vmt
            /path/to/models/sub/example.mdl,models/sub/example.mdl
            /path/to/sound/sub/example.mp3,sound/sub/example.mp3
            /path/to/materials/models/example.vmt,materials/models/example.vmt
            /path/to/maps/example.txt,maps/example.txt
            /path/to/scripts/soundscripts_example.txt,scripts/soundscripts_example.txt
            """)
    @ParameterizedTest
    void getRelativePathNoRoot(String path, String expected) {
        assertEquals(expected, Util.getRelativePath(path, null));
    }

    @ValueSource(strings = { "/unknown/path/example.vmt", "materials.vmt" })
    @ParameterizedTest
    void getRelativePathNone(String path) {
        assertEquals(null, Util.getRelativePath(path, null));
    }

    @CsvSource("""
            /path/to,/path/to/materials/example.vmt,materials/example.vmt
            /path/to,/path/to/asdf/example.vmt,asdf/example.vmt
            /path/to/,/path/to/asdf/example.vmt,asdf/example.vmt
            /path/to,/path/to/,
            /path/to/,/path/to/,
            /path/to,/path/to,
            /path/to/,/path/to,
            """)
    @ParameterizedTest
    void getRelativePathRoot(String rootDir, String fullPath, String expected) {
        assertEquals(expected, Util.getRelativePath(fullPath, rootDir));
    }

    @CsvSource("""
            200,199
            2000,2000
            4096,4096
            2048,1024
            """)
    @ParameterizedTest
    void copyBlock(int inSize, int numBytesToCopy, @TempDir Path parentDir) throws Exception {
        Path tempFile = Files.writeString(parentDir.resolve("temp.txt"), "1".repeat(inSize));

        try (
                RandomAccessFile in = new RandomAccessFile(tempFile.toFile(), "r");
                RandomAccessFile out = new RandomAccessFile(parentDir.resolve("out.txt").toFile(), "rw")) {
            Util.copyBlock(in, out, numBytesToCopy);

            out.seek(0);

            byte[] outBuffer = new byte[8192];
            int bytesRead = out.read(outBuffer);
            assertEquals(numBytesToCopy, bytesRead);
        }
    }

    @CsvSource("""
            199,200
            199,201
            1024,1025
            1024,2048
            """)
    @ParameterizedTest
    void copyBlockNotBigEnough(int inSize, int numBytesToCopy, @TempDir Path parentDir) throws Exception {
        Path tempFile = Files.writeString(parentDir.resolve("temp.txt"), "1".repeat(inSize));

        try (
                RandomAccessFile in = new RandomAccessFile(tempFile.toFile(), "r");
                RandomAccessFile out = new RandomAccessFile(parentDir.resolve("out.txt").toFile(), "rw")) {
            assertThrows(RuntimeException.class, () -> Util.copyBlock(in, out, numBytesToCopy));
        }
    }
}
