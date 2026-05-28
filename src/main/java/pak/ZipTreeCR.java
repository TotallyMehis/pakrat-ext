// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package pak;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class ZipTreeCR extends DefaultTreeCellRenderer {
    ZipTreeCR() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        Component cell = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (isFile(value) && !inPak(value) && !sel) {
            cell.setForeground(Color.blue);
        }

        if (isFile(value)) {
            ((JLabel) cell).setText(getText(value));
        }

        return cell;
    }

    private static boolean isFile(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return node.getUserObject().getClass() == Zipf.class;
    }

    private static boolean inPak(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return isFile(node) ? ((Zipf) node.getUserObject()).isInPak() : false;
    }

    private static String getText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (isFile(node)) {
            Zipf z = (Zipf) node.getUserObject();
            return z.getDetails();
        } else {
            return null;
        }
    }
}
