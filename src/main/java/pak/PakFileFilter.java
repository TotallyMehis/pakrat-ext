package pak;

import java.io.File;
import javax.swing.filechooser.FileFilter;

class PakFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = Util.getExtension(f);
            if (extension != null && (extension.equals("vmt") || extension.equals("vtf"))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "Valve material or texture file (*.vmt, *.vtf)";
    }
}
