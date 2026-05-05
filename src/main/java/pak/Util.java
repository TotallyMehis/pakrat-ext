package pak;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public abstract class Util {
    public static String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }

        return null;
    }

    public static String getRelativePath(String fullPath, String rootDir) {
        String full = fullPath;

        if (rootDir != null && !"".equals(rootDir) && full.startsWith(rootDir)) {
            int index = rootDir.length() + 1;
            if (index < full.length()) {
                return full.substring(index);
            }
        }

        int lowestIndex = -1;
        for (String folder : List.of("/materials", "/models", "/sound", "/maps", "/scripts")) {
            int index = full.indexOf(folder);
            if (index != -1 && (lowestIndex == -1 || lowestIndex > index)) {
                lowestIndex = index + 1;
            }
        }

        if (lowestIndex != -1) {
            return full.substring(lowestIndex);
        }

        return null;
    }

    public static void copyBlock(RandomAccessFile in, RandomAccessFile out, long length) throws IOException {
        assert length >= 0;
        assert length <= Integer.MAX_VALUE;
        assert in != null;
        assert out != null;

        if (in.getFilePointer() + length > in.length()) {
            throw new RuntimeException("Failed to copy " + length + " bytes, input file is not long enough.");
        }

        int size = (int) length;

        final int BUFFER_SIZE = 1024;

        int bytesRead;
        for (byte[] buffer = new byte[BUFFER_SIZE]; size > 0; size -= bytesRead) {
            int toRead = Math.min(size, BUFFER_SIZE);
            bytesRead = in.read(buffer, 0, toRead);
            out.write(buffer, 0, bytesRead);
        }
    }
}
