package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pak.TestUtil.getResourceAsFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ScanfileTest {

    static class SingleFileTestCollector implements ZipFileCollector {

        private final Zipf file;

        public SingleFileTestCollector() {
            this(null);
        }

        public SingleFileTestCollector(Zipf file) {
            this.file = file;
        }

        @Override
        public Zipf getZipFileByPath(String filePath) {
            return this.file;
        }

        @Override
        public Zipf getZipFileByIndex(int index) {
            assert index == 0;
            return this.file;
        }

        @Override
        public int getZipFileCount() {
            return this.file != null ? 1 : 0;
        }

        @Override
        public int getZipFileIndex(Zipf file) {
            return this.file != null && file == this.file ? 0 : -1;
        }

        @Override
        public void addZipFile(Zipf file) {
            throw new UnsupportedOperationException("Unimplemented method 'addZipFile'");
        }

        @Override
        public void removeZipFileByIndex(int index) {
            throw new UnsupportedOperationException("Unimplemented method 'removeZipFileByIndex'");
        }

        @Override
        public void addFilesToPak(File[] files, String baseDirectory, boolean force) throws IOException {
            throw new UnsupportedOperationException("Unimplemented method 'addFilesToPak'");
        }

        @Override
        public List<Zipf> getZipFiles() {
            throw new UnsupportedOperationException("Unimplemented method 'getZipFiles'");
        }
    }

    @Test
    void scanFileOnDisk(@TempDir Path tempDir) throws Exception {

        Path filePath = tempDir.resolve("materials/subfolder/flat_normal.vtf");
        tempDir.resolve("materials/subfolder").toFile().mkdirs();
        Files.copy(getResourceAsFile("flat_normal.vtf").toPath(), Files.newOutputStream(filePath));

        String baseDirectory = tempDir.toFile().getAbsolutePath();
        var collector = new SingleFileTestCollector();
        var scanFile = new Scanfile("subfolder/flat_normal.vtf", collector, baseDirectory, ScanfileType.VTF,
                Scanfile.TEXTURE,
                "referent");

        assertEquals(true, scanFile.ondisk);
        assertEquals(false, scanFile.inpak);
        assertEquals(false, scanFile.inlist);
        assertEquals(Scanfile.TEXTURE, scanFile.parent);
        assertEquals(true, scanFile.onlydisk());
        assertEquals(filePath.toFile().getAbsolutePath(), scanFile.diskname);
        assertEquals("materials/subfolder/flat_normal.vtf", scanFile.fullname);
        assertEquals("flat_normal.vtf", scanFile.listname);
        assertEquals("subfolder/flat_normal", scanFile.name);
        assertEquals("materials/subfolder", scanFile.pathname);
        assertEquals("referent", scanFile.referent);
    }

    @Test
    void scanFileInList(@TempDir Path tempDir) throws Exception {
        Path filePath = tempDir.resolve("materials/subfolder/flat_normal.vtf");
        tempDir.resolve("materials/subfolder").toFile().mkdirs();
        Files.copy(getResourceAsFile("flat_normal.vtf").toPath(), Files.newOutputStream(filePath));

        String baseDirectory = tempDir.toFile().getAbsolutePath();
        var collector = new SingleFileTestCollector(Zipf.fromFile(filePath.toFile(), false, null));
        var scanFile = new Scanfile("subfolder/flat_normal.vtf", collector, baseDirectory, ScanfileType.VTF,
                Scanfile.TEXTURE,
                "referent");

        assertEquals(true, scanFile.ondisk);
        assertEquals(false, scanFile.inpak);
        assertEquals(true, scanFile.inlist);
        assertEquals(Scanfile.TEXTURE, scanFile.parent);
        assertEquals(false, scanFile.onlydisk());
        assertEquals(filePath.toFile().getAbsolutePath(), scanFile.diskname);
        assertEquals("materials/subfolder/flat_normal.vtf", scanFile.fullname);
        assertEquals("flat_normal.vtf", scanFile.listname);
        assertEquals("subfolder/flat_normal", scanFile.name);
        assertEquals("materials/subfolder", scanFile.pathname);
        assertEquals("referent", scanFile.referent);
    }

    @Test
    void getPathPrefix() {
        assertEquals("materials/", Scanfile.getPathPrefix(ScanfileType.VTF));
    }
}
