package pak;

import java.io.File;
import javax.swing.filechooser.FileFilter;

class BspFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = Util.getExtension(f);
            if (extension != null && extension.equals("bsp")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "HL2 map file (*.bsp)";
    }
}
