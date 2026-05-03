package pak;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

class FileTransferHandler extends TransferHandler {
   private DataFlavor fileFlavor;
   private Unpak pakrat;

   public FileTransferHandler(Unpak pakrat) {
      this.pakrat = pakrat;
      this.fileFlavor = DataFlavor.javaFileListFlavor;
   }

   @Override
   public boolean importData(JComponent c, Transferable t) {
      if (!this.canImport(c, t.getTransferDataFlavors())) {
         return false;
      } else {
         try {
            @SuppressWarnings("unchecked")
            List<File> files = (List<File>) t.getTransferData(this.fileFlavor);
            File[] filearray = (File[]) files.toArray(new File[0]);
            this.pakrat.addfiletopak(filearray, this.pakrat.gamedir, false);
            return true;
         } catch (UnsupportedFlavorException var5) {
            Cons.println("importData: unsupported data flavor");
         } catch (IOException var6) {
            Cons.println("importData: I/O exception");
         }

         return false;
      }
   }

   @Override
   public int getSourceActions(JComponent c) {
      return 1;
   }

   @Override
   public boolean canImport(JComponent c, DataFlavor[] flavors) {
      return this.hasFileFlavor(flavors);
   }

   private boolean hasFileFlavor(DataFlavor[] flavors) {
      for (int i = 0; i < flavors.length; ++i) {
         if (this.fileFlavor.equals(flavors[i])) {
            return true;
         }
      }

      return false;
   }
}
