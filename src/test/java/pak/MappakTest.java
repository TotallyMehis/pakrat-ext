package pak;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class MappakTest {
    @Test
    void loadMap() throws Exception {
        Mappak mappak = new Mappak(true);

        RandomAccessFile file = openResourceFileForRead("test_npcclip.bsp");

        mappak.loadMap(file);

        assertEquals(105679, mappak.getLength());

        String[] textureNames = mappak.getTexname();
        assertEquals(5, textureNames.length);
        assertListEquals(List.of("DEV/DEV_MEASUREGENERIC01B", "TOOLS/TOOLSNODRAW", "TOOLS/TOOLSSKYBOX",
                "DEV/DEV_MEASUREWALL01A", "TOOLS/TOOLSNPCCLIP"), textureNames);

        List<Zipf> pakFiles = mappak.getZf();
        assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.vtf");
        assertContainsFile(pakFiles, "materials/maps/test_npcclip/cubemapdefault.hdr.vtf");
    }

    @CsvSource("""
            test_npcclip.bsp
            """)
    @ParameterizedTest
    void saveMapCrc(String testMap, @TempDir Path tempDir) throws Exception {
        Mappak mappak = new Mappak(true);

        File outputFile = tempDir.resolve(testMap).toFile();

        try (RandomAccessFile in = openResourceFileForRead(testMap)) {
            mappak.loadMap(in);

            assertDoesNotThrow(() -> {
                try (var out = new RandomAccessFile(outputFile, "rw")) {
                    mappak.saveMap(in, out);
                    mappak.savePak(in, out);
                }
            });
        }

        long inCrc = getFileCrc(new File(MappakTest.class.getClassLoader().getResource(testMap).toURI()));
        long outCrc = getFileCrc(outputFile);
        assertEquals(inCrc, outCrc);
    }

    private static RandomAccessFile openResourceFileForRead(String fileName) {
        URL fileUrl = MappakTest.class.getClassLoader().getResource(fileName);

        try {
            return new RandomAccessFile(new File(fileUrl.toURI()), "r");
        } catch (FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException("Failed to open file for reading: " + fileName, e);
        }
    }

    private <T> void assertListEquals(List<T> expected, T[] actual) {
        assertTrue(expected.size() == actual.length);

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

    private static long getFileCrc(File file) {
        byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate CRC of a file " + file.getAbsolutePath(), e);
        }

        Checksum checksum = new CRC32();
        checksum.update(data);
        return checksum.getValue();
    }
}
