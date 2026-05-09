package pak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class Zipf {
    int size;
    int relofs;
    int datofs;
    private String fullPath;
    private String fileName;
    private String path;
    private FileType type;
    boolean inpak;
    byte[] data;
    long CRC;

    private Zipf() {
    }

    public static Zipf fromPak(String filePath, int size, int relativeOffset, int datOffset, long crc) {
        Zipf z = new Zipf();
        z.size = size;
        z.relofs = relativeOffset;
        z.datofs = datOffset;
        z.CRC = crc;
        z.setFullPath(filePath);
        z.inpak = true;
        z.data = null;

        return z;
    }

    public static Zipf fromFile(File file, boolean fixupPath, String rootDir) throws IOException {
        assert file.isFile();
        assert file.canRead();

        Zipf z = new Zipf();
        z.size = (int) file.length();
        z.datofs = z.relofs = 0;
        z.inpak = false;
        z.data = new byte[z.size];
        FileInputStream fis = new FileInputStream(file);
        fis.read(z.data, 0, z.size);
        fis.close();
        CRC32 crc = new CRC32();
        crc.update(z.data);
        z.CRC = crc.getValue();

        String normalizedPath = Util.normalizePath(file.getAbsolutePath());

        String relativePath = Util.getRelativePath(normalizedPath, rootDir);
        if (fixupPath && relativePath != null) {
            z.setFullPath(relativePath);
        } else {
            z.setFullPath(normalizedPath);
        }

        return z;
    }

    @Override
    public String toString() {
        return this.fullPath;
    }

    public String getDetails() {
        return this.fileName + "   (" + this.getType().getName() + ",  " + this.size + " bytes)";
    }

    public String getFullDetails() {
        return this.fullPath + "   (" + this.getType().getName() + ",  " + this.size + " bytes)";
    }

    public void setFullPath(String path) {
        if (path.startsWith("/")) {
            this.fullPath = path.substring(1, path.length());
        } else {
            this.fullPath = path;
        }

        int is = this.fullPath.lastIndexOf("/");
        if (is >= 0 && is < this.fullPath.length() - 1) {
            this.fileName = this.fullPath.substring(is + 1);
            this.path = this.fullPath.substring(0, is);
        } else {
            this.fileName = this.fullPath;
            this.path = "";
        }

        this.type = FileType.from(this.fileName);
    }

    public void setFileName(String fileName) {
        this.setFullPath(this.path + "/" + fileName);
    }

    public void setPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        this.setFullPath(path + "/" + this.fileName);
    }

    public FileType getType() {
        return this.type;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public String getPath() {
        return this.path;
    }

    public String getFileName() {
        return this.fileName;
    }
}
