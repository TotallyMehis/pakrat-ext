package pak;

import java.io.File;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

class MdlFileFilter extends FileFilter {
    private static final Set<String> extensions = Set.of("mdl", "vtx", "ani", "phy", "vvd");

    @Override
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = Util.getExtension(f);
            if (extension != null && extensions.contains(extension)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "Valve model file (*.mdl, *.vtx, *.vvd, *.phy, *.ani)";
    }
}
