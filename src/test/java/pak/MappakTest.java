package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pak.TestUtil.getFileCrc;
import static pak.TestUtil.getResourceAsFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

final class MappakTest {
    @ParameterizedTest
    @ValueSource(strings = { "test_npcclip.bsp" })
    void loadMap(String mapName) throws Exception {
        Mappak mappak = new Mappak(true);

        try (RandomAccessFile file = openResourceFileForRead(mapName)) {
            assertDoesNotThrow(() -> mappak.loadMap(file));
        }
    }

    @Test
    void loadMapInfo() throws Exception {
        Mappak mappak = new Mappak(true);

        try (RandomAccessFile file = openResourceFileForRead("test_npcclip.bsp")) {
            mappak.loadMap(file);

            assertEquals(105679, mappak.getLength());

            List<String> textureNames = mappak.getTexname();
            assertEquals(5, textureNames.size());
            assertListEquals(List.of("DEV/DEV_MEASUREGENERIC01B", "TOOLS/TOOLSNODRAW", "TOOLS/TOOLSSKYBOX",
                    "DEV/DEV_MEASUREWALL01A", "TOOLS/TOOLSNPCCLIP"), textureNames);

            List<Zipf> pakFiles = mappak.getZf();
            assertEquals(2, pakFiles.size());
            assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.vtf");
            assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.hdr.vtf");
        }
    }

    @CsvSource("""
            test_npcclip.bsp
            """)
    @ParameterizedTest
    void saveMapCrc(String testMap, @TempDir Path tempDir) throws Exception {
        Mappak mappak = new Mappak(true);

        File outputFile = tempDir.resolve(new File(testMap).getName()).toFile();

        try (RandomAccessFile in = openResourceFileForRead(testMap)) {
            mappak.loadMap(in);

            assertDoesNotThrow(() -> {
                try (var out = new RandomAccessFile(outputFile, "rw")) {
                    mappak.saveMap(in, out);
                    mappak.savePak(in, out);
                }
            });
        }

        long inCrc = getFileCrc(getResourceAsFile(testMap));
        long outCrc = getFileCrc(outputFile);
        assertEquals(inCrc, outCrc);
    }

    @Test
    void addFile(@TempDir Path tempDir) throws Exception {
        File outputFile = tempDir.resolve("output.bsp").toFile();

        try (RandomAccessFile in = openResourceFileForRead("test_npcclip.bsp")) {
            Mappak mappak = new Mappak(true);
            File testFile = getResourceAsFile("test.txt");

            mappak.loadMap(in);

            assertEquals(2, mappak.getZf().size());

            Zipf zipFile = Zipf.fromFile(testFile, false, null);
            zipFile.setPath("maps");
            mappak.getZf().add(zipFile);

            try (var out = new RandomAccessFile(outputFile, "rw")) {
                mappak.saveMap(in, out);
                mappak.savePak(in, out);
            }
        }

        try (RandomAccessFile in = new RandomAccessFile(outputFile, "r")) {
            Mappak mappak = new Mappak(true);
            mappak.loadMap(in);

            List<Zipf> pakFiles = mappak.getZf();
            assertEquals(3, pakFiles.size());
            assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.vtf");
            assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.hdr.vtf");
            assertContainsFile(pakFiles, "maps/test.txt");
        }
    }

    private static RandomAccessFile openResourceFileForRead(String fileName) {
        try {
            return new RandomAccessFile(getResourceAsFile(fileName), "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open file for reading: " + fileName, e);
        }
    }

    private <T> void assertListEquals(List<T> expected, List<T> actual) {
        assertTrue(expected.size() == actual.size());

        for (T t : actual) {
            if (!expected.contains(t)) {
                fail("Did not contain %s".formatted(t));
            }
        }
    }

    private void assertContainsFile(List<Zipf> files, String fileName) {
        assertTrue(files.stream().anyMatch(zipf -> fileName.equals(zipf.getFullPath())),
                () -> "Did not contain file %s".formatted(fileName));
    }
}
