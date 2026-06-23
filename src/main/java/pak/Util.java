package pak;

import static java.util.Locale.ROOT;

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
                return filename.substring(i + 1).toLowerCase(ROOT);
            }
        }

        return null;
    }

    public static String getRelativePath(String fullPath, String rootDir) {
        if (rootDir != null && !rootDir.isEmpty() && fullPath.startsWith(rootDir)) {
            int offset = rootDir.endsWith("/") ? 0 : 1;
            int index = rootDir.length() + offset;
            if (index < fullPath.length()) {
                return fullPath.substring(index);
            }
        }

        int lowestIndex = -1;
        for (String folder : List.of("/materials", "/models", "/sound", "/maps", "/scripts")) {
            int index = fullPath.indexOf(folder);
            if (index != -1 && (lowestIndex == -1 || lowestIndex > index)) {
                lowestIndex = index + 1;
            }
        }

        if (lowestIndex != -1) {
            return fullPath.substring(lowestIndex);
        }

        return null;
    }

    public static void copyBlock(RandomAccessFile in, RandomAccessFile out, long length) throws IOException {
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

    public static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}
