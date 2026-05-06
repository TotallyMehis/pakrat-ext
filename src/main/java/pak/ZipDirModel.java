package pak;

import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ZipDirModel extends AbstractTableModel {
    RandomAccessFile braf;
    int offset;
    List<Zipf> zfl;
    public Unpak pakrat;
    static String[] header = new String[] { "In", "Filename", "Path", "Size", "Type" };
    static Object[] cols;

    public ZipDirModel(List<Zipf> zipfilelist, Unpak rat) {
        this.zfl = zipfilelist;
        this.pakrat = rat;
    }

    public void setziplist(List<Zipf> zipfilelist) {
        this.zfl = zipfilelist;
    }

    public Zipf getzipfile(int row) {
        return this.zfl != null ? (Zipf) this.zfl.get(row) : null;
    }

    @Override
    public int getRowCount() {
        return this.zfl != null ? this.zfl.size() : 0;
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
        switch (col) {
            case 0:
                return this.getzipfile(row).inpak;
            case 1:
                return this.getzipfile(row).getFilename();
            case 2:
                return this.getzipfile(row).getPathname();
            case 3:
                return this.getzipfile(row).size;
            case 4:
                return this.getzipfile(row).getType().getName();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 1:
                this.getzipfile(row).setfile((String) value);
                break;
            case 2:
                this.getzipfile(row).setpath((String) value);
        }

        this.fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
        return header[col];
    }

    public void setfileparams(RandomAccessFile r, int o) {
        this.braf = r;
        this.offset = o;
    }

    public RandomAccessFile getbuff() {
        return this.braf;
    }

    public int getoffset() {
        return this.offset;
    }

    public void deletefile(int row) {
        if (row >= 0 && row < this.zfl.size()) {
            this.zfl.remove(row);
        }

        this.fireTableDataChanged();
    }

    public void addfile(Zipf zip) {
        this.zfl.add(zip);
        this.fireTableDataChanged();
    }

    public Zipf getbyname(String fname) {
        for (int i = 0; i < this.getRowCount(); ++i) {
            if (fname.equalsIgnoreCase(this.getzipfile(i).getFullname())) {
                return this.getzipfile(i);
            }
        }

        return null;
    }

    public Zipf getbyfilename(String fname) {
        for (int i = 0; i < this.getRowCount(); ++i) {
            if (fname.equalsIgnoreCase(this.getzipfile(i).getFilename())) {
                return this.getzipfile(i);
            }
        }

        return null;
    }

    public int getrow(Zipf f) {
        return this.zfl.indexOf(f);
    }

    public DefaultMutableTreeNode getTree(String file) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file);

        for (Zipf z : this.zfl) {
            DefaultMutableTreeNode znode = new DefaultMutableTreeNode(z);
            if (z.getPathname().equals("")) {
                root.add(znode);
            } else {
                String[] dirs = z.getPathname().split("/");
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

    public DefaultMutableTreeNode findMatchingChildNode(DefaultMutableTreeNode top, String s) {
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
                Zipf z = this.getzipfile(rows[i]);
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
                    int row = this.getrow((Zipf) sel);
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

    public void printTree(DefaultMutableTreeNode root) {
        for (int i = 0; i < root.getLevel(); ++i) {
            System.out.print(">");
        }

        System.out.println(root);
        Enumeration<TreeNode> e = root.children();

        while (e.hasMoreElements()) {
            this.printTree((DefaultMutableTreeNode) e.nextElement());
        }

    }

    static {
        cols = new Object[] { Boolean.TRUE, "", "", "", "" };
    }
}
