package pak;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ZipFiles implements ZipFileCollector {
    private final List<Zipf> zips;
    private final Unpak unpak;

    public ZipFiles(Unpak unpak, Mappak m) {
        this.unpak = unpak;
        this.zips = m.getZf();
    }

    @Override
    public void addFilesToPak(File[] files, String baseDirectory, boolean force) throws IOException {
        this.unpak.addFileToPak(files, baseDirectory, force);
    }

    @Override
    public List<Zipf> getZipFiles() {
        return List.copyOf(this.zips);
    }

    @Override
    public Zipf getZipFileByIndex(int index) {
        assert index >= 0 && index < this.zips.size();
        return this.zips.get(index);
    }

    @Override
    public int getZipFileCount() {
        return this.zips.size();
    }

    @Override
    public Zipf getZipFileByPath(String filePath) {
        return this.zips.stream().filter(file -> filePath.equalsIgnoreCase(file.getFullPath())).findAny().orElse(null);
    }

    @Override
    public int getZipFileIndex(Zipf file) {
        assert file != null;
        return this.zips.indexOf(file);
    }

    @Override
    public void addZipFile(Zipf file) {
        this.zips.add(file);
    }

    @Override
    public void removeZipFileByIndex(int index) {
        assert index >= 0 && index < this.zips.size();
        this.zips.remove(index);
    }
}
