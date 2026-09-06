package pak;

public interface ZipFileCollector {
    public void addZipFile(Zipf file);

    public Zipf getZipFileByIndex(int index);

    public void removeZipFileByIndex(int index);

    public int getZipFileCount();

    public Zipf getZipFileByPath(String filePath);

    public int getZipFileIndex(Zipf file);
}
