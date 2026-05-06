package pak;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

class FileTransferHandler extends TransferHandler {
    private static final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;
    private final Consumer<File[]> consumer;

    public FileTransferHandler(Consumer<File[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {
        if (!this.canImport(c, t.getTransferDataFlavors())) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            List<File> files = (List<File>) t.getTransferData(FILE_FLAVOR);
            File[] filearray = files.toArray(new File[0]);
            consumer.accept(filearray);
            return true;
        } catch (UnsupportedFlavorException _) {
            Cons.println("importData: unsupported data flavor");
        } catch (IOException _) {
            Cons.println("importData: I/O exception");
        }

        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return hasFileFlavor(flavors);
    }

    private static boolean hasFileFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (FILE_FLAVOR.equals(flavor)) {
                return true;
            }
        }

        return false;
    }
}
