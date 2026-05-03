package pak;

import java.io.File;
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
}
