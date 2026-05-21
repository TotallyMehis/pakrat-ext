package pak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

public class Zipf {
    private final int size;
    private int localHeaderOffset;
    private int dataOffset;
    private String fullPath;
    private String fileName;
    private String path;
    private FileType type;
    private boolean inpak;
    private byte[] data;
    private long CRC;

    private Zipf(int size) {
        this.size = size;
    }

    public static Zipf fromPak(String filePath, int size, int localHeaderOffset, int dataOffset, long crc) {
        Zipf z = new Zipf(size);
        z.localHeaderOffset = localHeaderOffset;
        z.dataOffset = dataOffset;
        z.CRC = crc;
        z.setFullPath(filePath);
        z.inpak = true;
        z.data = null;

        return z;
    }

    public static Zipf fromFile(File file, boolean fixupPath, String rootDir) throws IOException {
        assert file.isFile();
        assert file.canRead();

        Zipf z = new Zipf((int) file.length());
        z.dataOffset = z.localHeaderOffset = 0;
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
        this.fullPath = path;

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
        path = Util.normalizePath(path);

        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        while (path.endsWith("/")) {
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

    public boolean isInPak() {
        return this.inpak;
    }

    public int getSize() {
        return this.size;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getRelativeOffset() {
        return this.localHeaderOffset;
    }

    public void setRelativeOffset(int relofs) {
        this.localHeaderOffset = relofs;
    }

    public int getDataOffset() {
        return this.dataOffset;
    }

    public void setDataOffset(int datofs) {
        this.dataOffset = datofs;
    }

    public long getCRC() {
        return this.CRC;
    }

    public void setCRC(long crc) {
        this.CRC = crc;
    }

    public void moveToPak(RandomAccessFile out) throws IOException {
        assert !this.inpak;

        out.write(this.data);
        this.inpak = true;
        this.data = null;
    }
}
