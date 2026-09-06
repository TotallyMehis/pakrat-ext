package pak;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ZipFileCollector {
    public void addFilesToPak(File[] files, String baseDirectory, boolean force) throws IOException;

    public void addZipFile(Zipf file);

    public List<Zipf> getZipFiles();

    public Zipf getZipFileByIndex(int index);

    public void removeZipFileByIndex(int index);

    public int getZipFileCount();

    public Zipf getZipFileByPath(String filePath);

    public int getZipFileIndex(Zipf file);
}
