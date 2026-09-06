package pak;

import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ZipDirModel extends AbstractTableModel {
    private final ZipFiles zipFiles;
    private static final String[] header = new String[] { "In", "Filename", "Path", "Size", "Type" };
    private static final Object[] cols = new Object[] { true, "", "", "", "" };

    public ZipDirModel(ZipFiles zipFiles) {
        this.zipFiles = Objects.requireNonNull(zipFiles);
    }

    @Override
    public int getRowCount() {
        return this.zipFiles.getZipFileCount();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return cols[col].getClass();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return switch (col) {
            case 0 -> this.zipFiles.getZipFileByIndex(row).isInPak();
            case 1 -> this.zipFiles.getZipFileByIndex(row).getFileName();
            case 2 -> this.zipFiles.getZipFileByIndex(row).getPath();
            case 3 -> this.zipFiles.getZipFileByIndex(row).getSize();
            case 4 -> this.zipFiles.getZipFileByIndex(row).getType().getName();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 1 -> {
                this.zipFiles.getZipFileByIndex(row).setFileName((String) value);
            }
            case 2 -> {
                this.zipFiles.getZipFileByIndex(row).setPath((String) value);
            }
        }

        this.fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
        return header[col];
    }

    public DefaultMutableTreeNode getTree(String file) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file);

        for (Zipf z : this.zipFiles.getZipFiles()) {
            DefaultMutableTreeNode znode = new DefaultMutableTreeNode(z);
            if (z.getPath().equals("")) {
                root.add(znode);
            } else {
                String[] dirs = z.getPath().split("/");
                DefaultMutableTreeNode top = root;

                for (String s : dirs) {
                    DefaultMutableTreeNode next = this.findMatchingChildNode(top, s);
                    if (next == null) {
                        next = new DefaultMutableTreeNode(s);
                    }

                    top.add(next);
                    top = next;
                }

                top.add(znode);
            }
        }

        return root;
    }

    private DefaultMutableTreeNode findMatchingChildNode(DefaultMutableTreeNode top, String s) {
        Enumeration<TreeNode> children = top.children();

        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getUserObject().getClass() == String.class && s.equalsIgnoreCase(child.toString())) {
                return child;
            }
        }

        return null;
    }

    public void setTreeSelection(JTree tree, JTable table) {
        TableSorter sorter = (TableSorter) table.getModel();
        tree.clearSelection();
        int[] rows = sorter.modelIndex(table.getSelectedRows());
        if (rows.length != 0) {
            for (int i = 0; i < rows.length; ++i) {
                Zipf z = this.zipFiles.getZipFileByIndex(rows[i]);
                this.selectTreeNode(tree, z);
            }

        }
    }

    public void selectTreeNode(JTree tree, Zipf target) {
        DefaultMutableTreeNode currentnode = (DefaultMutableTreeNode) tree.getModel().getRoot();

        while (currentnode.getUserObject() != target) {
            currentnode = currentnode.getNextNode();
            if (currentnode == null) {
                Cons.println("SelectTreeNode: Couldn't find a match for " + target);
                return;
            }
        }

        TreePath tp = new TreePath(currentnode.getPath());
        tree.addSelectionPath(tp);
        tree.makeVisible(tp);
    }

    public void setTableSelection(JTree tree, JTable table) {
        TableSorter sorter = (TableSorter) table.getModel();
        table.clearSelection();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; ++i) {
                Object sel = ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
                if (sel.getClass() == Zipf.class) {
                    int row = this.zipFiles.getZipFileIndex((Zipf) sel);
                    if (row == -1) {
                        Cons.println("SetTableSelection: Couldn't find a match for " + (Zipf) sel);
                    } else {
                        row = sorter.viewIndex(row);
                        table.addRowSelectionInterval(row, row);
                    }
                } else {
                    Cons.println("SetTableSelection: Selection wasn't a file");
                }
            }

        }
    }
}
