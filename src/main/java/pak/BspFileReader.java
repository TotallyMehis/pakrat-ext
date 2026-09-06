package pak;

import java.io.RandomAccessFile;

public interface BspFileReader {
    public long getPakOffset();

    public RandomAccessFile getRandomAccessFile();
}
