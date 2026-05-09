package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class ZipfTest {
    @Test
    void fromPak() throws Exception {
        Zipf zipFile = Zipf.fromPak("materials/subfolder/test.vmt", 100, 101, 102, 0x100);

        assertEquals(true, zipFile.inpak);
        assertEquals(null, zipFile.data);
        assertEquals(100, zipFile.size);
        assertEquals(101, zipFile.relofs);
        assertEquals(102, zipFile.datofs);
        assertEquals("materials/subfolder/test.vmt", zipFile.getFullPath());
        assertEquals("materials/subfolder", zipFile.getPath());
        assertEquals("test.vmt", zipFile.getFileName());
        assertEquals(FileType.MATERIAL, zipFile.getType());
        assertEquals(0x100, zipFile.CRC);
    }

    @Test
    void fromFile() throws Exception {
        File file = new File(MappakTest.class.getClassLoader().getResource("test_npcclip.bsp").toURI());
        Zipf zipFile = Zipf.fromFile(file, false, null);

        assertFalse(zipFile.inpak);
        assertNotNull(zipFile.data);
        assertEquals(131604, zipFile.data.length);
        assertEquals(535977290, zipFile.CRC);
        assertEquals("test_npcclip.bsp", zipFile.getFileName());
        assertEquals(FileType.OTHER, zipFile.getType());
    }

    @CsvSource("""
            maps
            materials
            models
            scripts
            sound
            """)
    @ParameterizedTest
    void fromFileFixup(String fixupFolder, @TempDir Path tempDir) throws Exception {
        Path filePath = tempDir.resolve("%s/subfolder/test.txt".formatted(fixupFolder));
        tempDir.resolve("%s/subfolder".formatted(fixupFolder)).toFile().mkdirs();
        Files.copy(Path.of(MappakTest.class.getClassLoader().getResource("test.txt").toURI()),
                Files.newOutputStream(filePath));

        Zipf zipFile = Zipf.fromFile(filePath.toFile(), true, null);

        assertFalse(zipFile.inpak);
        assertNotNull(zipFile.data);
        assertEquals("%s/subfolder/test.txt".formatted(fixupFolder), zipFile.getFullPath());
        assertEquals("%s/subfolder".formatted(fixupFolder), zipFile.getPath());
        assertEquals("test.txt", zipFile.getFileName());
        assertEquals(FileType.TEXT, zipFile.getType());
    }

    @Test
    void fromFileFixupRoot(@TempDir Path tempDir) throws Exception {
        Path filePath = tempDir.resolve("something/subfolder/test.txt");
        File rootDir = tempDir.resolve("something").toFile();
        tempDir.resolve("something/subfolder").toFile().mkdirs();

        Files.copy(Path.of(MappakTest.class.getClassLoader().getResource("test.txt").toURI()),
                Files.newOutputStream(filePath));

        Zipf zipFile = Zipf.fromFile(filePath.toFile(), true, Util.normalizePath(rootDir.getAbsolutePath()));

        assertFalse(zipFile.inpak);
        assertNotNull(zipFile.data);
        assertEquals("subfolder/test.txt", zipFile.getFullPath());
        assertEquals("subfolder", zipFile.getPath());
        assertEquals("test.txt", zipFile.getFileName());
        assertEquals(FileType.TEXT, zipFile.getType());
    }
}
