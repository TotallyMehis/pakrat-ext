package pak;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class TestUtil {
    public static File getResourceAsFile(String fileName) {
        assert fileName != null;

        try {
            return new File(TestUtil.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert resource to URI of file: " + fileName, e);
        }
    }

    public static byte[] readResourceFile(String fileName) {
        assert fileName != null;

        try {
            return Files.readAllBytes(getResourceAsFile(fileName).toPath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read all bytes of file " + fileName, e);
        }
    }

    public static long getFileCrc(File file) {
        assert file != null;

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
