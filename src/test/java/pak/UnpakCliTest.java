package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class UnpakCliTest {
    @Test
    void savePakFileToDisk(@TempDir Path tempDir) throws Exception {
        File outputFile = tempDir.resolve("output.vtf").toFile();

        String filePath = new File(UnpakCliTest.class.getClassLoader().getResource("test_npcclip.bsp").toURI())
                .getAbsolutePath();

        assertEquals(false, outputFile.exists());

        UnpakCli.savePakFileToDisk(filePath, "cubemapdefault.vtf", outputFile.getAbsolutePath());

        assertEquals(true, outputFile.exists());
        assertEquals(1408923060, MappakTest.getFileCrc(outputFile));
    }

    @Test
    void printPakFiles() throws Exception {
        String filePath = new File(UnpakCliTest.class.getClassLoader().getResource("test_npcclip.bsp").toURI())
                .getAbsolutePath();

        assertDoesNotThrow(() -> UnpakCli.printPakFiles(filePath));
    }
}
