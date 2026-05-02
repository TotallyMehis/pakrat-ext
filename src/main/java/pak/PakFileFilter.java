package pak;

import java.io.File;
import javax.swing.filechooser.FileFilter;

class PakFileFilter extends FileFilter {
   PakFileFilter() {
   }

   @Override
   public boolean accept(File f) {
      if (f != null) {
         if (f.isDirectory()) {
            return true;
         }

         String extension = this.getExtension(f);
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
