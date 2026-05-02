package pak;

import java.io.File;
import javax.swing.filechooser.FileFilter;

class MdlFileFilter extends FileFilter {
   MdlFileFilter() {
   }

   @Override
   public boolean accept(File f) {
      if (f != null) {
         if (f.isDirectory()) {
            return true;
         }

         String extension = this.getExtension(f);
         if (extension != null && (extension.equals("mdl") || extension.equals("vtx") || extension.equals("ani") || extension.equals("phy") || extension.equals("vvd"))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String getDescription() {
      return "Valve model file (*.mdl, *.vtx, *.vvd, *.phy, *.ani)";
   }

   public String getExtension(File f) {
      if (f != null) {
         String filename = f.getName();
         int i = filename.lastIndexOf(46);
         if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
         }
      }

      return null;
   }
}
