package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class UnpakCliTest {
    @Test
    void savePakFileToDisk(@TempDir Path tempDir) {
        File outputFile = tempDir.resolve("output.vtf").toFile();

        String filePath = getBspFile().getAbsolutePath();

        assertEquals(false, outputFile.exists());

        UnpakCli.savePakFileToDisk(filePath, "cubemapdefault.vtf", outputFile.getAbsolutePath());

        assertEquals(true, outputFile.exists());
        assertEquals(1408923060, MappakTest.getFileCrc(outputFile));
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

        UnpakCli.dumpPak(filePath, outputFile.getAbsolutePath());

        assertEquals(true, outputFile.exists());
        assertEquals(3068538124L, MappakTest.getFileCrc(outputFile));
    }

    private static File getBspFile() {
        try {
            return new File(UnpakCliTest.class.getClassLoader().getResource("test_npcclip.bsp").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get URI of resource.", e);
        }
    }
}
