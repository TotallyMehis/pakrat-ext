package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pak.TestUtil.getFileCrc;
import static pak.TestUtil.getResourceAsFile;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class UnpakCliTest {
    @Test
    void savePakFileToDisk(@TempDir Path tempDir) {
        File outputFile = tempDir.resolve("output.vtf").toFile();

        String filePath = getBspFile().getAbsolutePath();

        assertEquals(false, outputFile.exists());

        UnpakCli.extractPakFile(filePath, "cubemapdefault.vtf", outputFile.getAbsolutePath());

        assertEquals(true, outputFile.exists());
        assertEquals(1408923060, getFileCrc(outputFile));
    }

    @Test
    void printPakFiles() {
        String filePath = getBspFile().getAbsolutePath();

        assertDoesNotThrow(() -> UnpakCli.printPakFiles(filePath));
    }

    @Test
    void dumpPak(@TempDir Path tempDir) {
        File outputFile = tempDir.resolve("output.zip").toFile();

        String filePath = getBspFile().getAbsolutePath();

        assertEquals(false, outputFile.exists());

        UnpakCli.extractPakFileAsZip(filePath, outputFile.getAbsolutePath());

        assertEquals(true, outputFile.exists());
        assertEquals(3068538124L, getFileCrc(outputFile));
    }

    private static File getBspFile() {
        return getResourceAsFile("test_npcclip.bsp");
    }
}
