package pak;

import java.io.RandomAccessFile;

public class BspFileReaderImpl implements BspFileReader {
    private long pakOffset;
    private RandomAccessFile randomAccessFile;

    public BspFileReaderImpl() {
    }

    @Override
    public long getPakOffset() {
        return this.pakOffset;
    }

    @Override
    public RandomAccessFile getRandomAccessFile() {
        return this.randomAccessFile;
    }

    public void update(RandomAccessFile randomAccessFile, long pakOffset) {
        this.randomAccessFile = randomAccessFile;
        this.pakOffset = pakOffset;
    }
}
