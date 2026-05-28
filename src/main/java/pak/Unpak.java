package pak;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class Unpak {
    private Mappak m;
    private File infile;
    private RandomAccessFile raf;
    private boolean treeview = false;
    private String gamedir;
    private JFrame frame;
    private JScrollPane mainsp;
    private JTable table;
    private ZipDirModel zmodel;
    private TableSorter tmodel;
    private JTree tree;
    private DefaultTreeModel treemodel;
    private DefaultMutableTreeNode root;
    private Scan scan;
    private boolean dirty = false;
    private boolean auton = false;

    private void exec(String basename, String filename) throws Exception {
        this.auton = true;
        Cons.open(false);
        long starttime = System.currentTimeMillis();
        Cons.println(
                "**** Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)"
                        .formatted(Version.getFullVersion()));
        Cons.println("Game base directory " + basename);
        Cons.println("Perfoming autoscan of " + filename);

        try {
            this.gamedir = basename;
            Pakpref.fixup = 2;
            if (!filename.endsWith(".bsp")) {
                filename = filename + ".bsp";
            }

            this.infile = new File(filename);
            if (this.infile.exists() && this.infile.canRead()) {
                Cons.println("Reading " + filename);
                Pakpref.mapdir = this.infile.getPath();
                this.raf = new RandomAccessFile(this.infile, "r");
                this.m = new Mappak(true);
                this.m.loadMap(this.raf);
                this.zmodel = new ZipDirModel(this.m.getZf());
                this.zmodel.setfileparams(this.raf, this.m.getOffset());
                Cons.println("Scanning for referenced files...");
                this.scan = new Scan(this, null, this.m, this.zmodel, filename, this.gamedir, true);
                if (this.scan.isNofiles()) {
                    long duration = System.currentTimeMillis() - starttime;
                    Cons.println("**** Pakrat autoscan complete in "
                            + (new DecimalFormat("0.#")).format((double) ((float) duration / 1000.0F)) + " seconds");
                } else {
                    File sfile = new File(this.infile.getAbsolutePath());
                    long ilength = this.infile.length();
                    File renfile = new File(this.infile.getAbsolutePath() + ".bak");
                    Cons.print("Copying current map file to " + renfile.getAbsolutePath() + "...");
                    RandomAccessFile copyraf = new RandomAccessFile(renfile, "rw");
                    copyraf.setLength(0L);
                    this.raf.seek(0L);
                    Util.copyBlock(this.raf, copyraf, ilength);
                    copyraf.close();
                    Cons.println("Done");
                    this.infile = renfile;
                    if (!this.infile.exists()) {
                        Cons.println("Cannot find renamed file - map write aborted!");
                    } else {
                        this.raf.close();
                        this.raf = new RandomAccessFile(this.infile, "r");
                        this.zmodel.setfileparams(this.raf, this.m.getOffset());
                        Cons.print("Writing " + filename + "...");
                        this.raf.seek(0L);
                        RandomAccessFile outraf = new RandomAccessFile(sfile, "rw");
                        outraf.setLength(0L);
                        Cons.print("BSP data...");
                        this.m.saveMap(this.raf, outraf);
                        Cons.print("Pak data...");
                        this.m.savePak(this.raf, outraf);
                        outraf.close();
                        Cons.println("Done");
                        this.raf.close();
                        this.infile = sfile;
                        this.raf = new RandomAccessFile(this.infile, "rw");
                        this.zmodel.setfileparams(this.raf, this.m.getOffset());
                        this.checkNav();
                        this.raf.close();
                        long duration = System.currentTimeMillis() - starttime;
                        Cons.println("**** Pakrat autoscan complete in "
                                + (new DecimalFormat("0.#")).format((double) ((float) duration / 1000.0F))
                                + " seconds");
                    }
                }
            } else {
                Cons.println("Can't open " + filename);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void exec(String filename) throws Exception {
        try {
            Cons.open(true);
            Pakpref.getInit();
            this.gamedir = Pakpref.gamedir;
            Cons.settitle("Pakrat - console");
            Cons.println(
                    "Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)".formatted(Version.getFullVersion()));
            if (filename == null) {
                JFileChooser chooser = new JFileChooser(Pakpref.mapdir);
                chooser.setDialogTitle("Open a map file");
                chooser.setFileFilter(new BspFileFilter());
                int result = chooser.showOpenDialog(Cons.getConsole());
                if (result == 1) {
                    System.exit(0);
                }

                this.infile = chooser.getSelectedFile();
                filename = this.infile.getName();
            } else {
                if (!filename.endsWith(".bsp")) {
                    filename = filename + ".bsp";
                }

                this.infile = new File(filename);
            }

            if (this.infile.exists() && this.infile.canRead()) {
                Cons.println("Reading " + filename);
                Pakpref.mapdir = this.infile.getPath();
                Pakpref.put("Mapdir", Pakpref.mapdir);
                this.raf = new RandomAccessFile(this.infile, "r");
                this.m = new Mappak(false);
                this.m.loadMap(this.raf);
                this.frame = new JFrame("Pakrat %s - %s".formatted(Version.getVersion(), filename));
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                JMenu filemenu = new JMenu("File");
                JMenuItem mload = filemenu.add("Load BSP");
                JMenuItem msave = filemenu.add("Save BSP");
                mload.setToolTipText("Load an new map file");
                msave.setToolTipText("Save the current map file");
                mload.setAccelerator(KeyStroke.getKeyStroke(76, 2));
                msave.setAccelerator(KeyStroke.getKeyStroke(83, 2));
                filemenu.addSeparator();
                JMenuItem mpref = filemenu.add("Preferences");
                mpref.setToolTipText("Set preferences");
                mpref.setAccelerator(KeyStroke.getKeyStroke(80, 2));
                filemenu.addSeparator();
                JMenuItem mquit = filemenu.add("Quit");
                mquit.setToolTipText("Quit Pakrat");
                mquit.setAccelerator(KeyStroke.getKeyStroke(81, 2));
                JMenu viewmenu = new JMenu("View");
                final JCheckBoxMenuItem mtree = new JCheckBoxMenuItem("As Tree");
                mtree.setToolTipText("Display files as a directory tree");
                mtree.setAccelerator(KeyStroke.getKeyStroke(84, 2));
                viewmenu.add(mtree);
                viewmenu.addSeparator();
                final JMenu sortmenu = new JMenu("Sort");
                sortmenu.setToolTipText("Sort file list by column");
                JMenuItem snone = new JMenuItem("None");
                sortmenu.add(snone);
                JMenuItem sname = new JMenuItem("Name");
                sortmenu.add(sname);
                JMenuItem spath = new JMenuItem("Path");
                sortmenu.add(spath);
                JMenuItem ssize = new JMenuItem("Size");
                sortmenu.add(ssize);
                JMenuItem stype = new JMenuItem("Type");
                sortmenu.add(stype);
                viewmenu.add(sortmenu);
                JMenu helpmenu = new JMenu("Help");
                JMenuItem mcons = helpmenu.add("Console");
                mcons.setToolTipText("Show the console window");
                helpmenu.addSeparator();
                JMenuItem mhelp = helpmenu.add("About Pakrat");
                JMenuBar menubar = new JMenuBar();
                menubar.add(filemenu);
                menubar.add(viewmenu);
                menubar.add(helpmenu);
                this.frame.setJMenuBar(menubar);
                this.zmodel = new ZipDirModel(this.m.getZf());
                this.zmodel.setfileparams(this.raf, this.m.getOffset());
                this.tmodel = new TableSorter(this.zmodel);
                this.table = new JTable(this.tmodel);
                this.tmodel.setTableHeader(this.table.getTableHeader());
                this.table.setAutoResizeMode(1);
                this.table.getColumn("Size").setMaxWidth(80);
                this.table.getColumn("Size").setWidth(50);
                this.table.getColumn("Type").setMaxWidth(50);
                this.table.getColumn("In").setMaxWidth(20);
                this.table.getColumn(ZipDirModel.header[1]).setCellRenderer(new ZipTableCR());
                this.table.setSelectionMode(2);
                TransferHandler fileth = new FileTransferHandler(files -> {
                    try {
                        addFileToPak(files, this.gamedir, false);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                });
                this.table.setTransferHandler(fileth);
                this.mainsp = new JScrollPane(this.table);
                this.mainsp.setTransferHandler(fileth);
                panel.add(this.mainsp, "Center");
                this.root = this.zmodel.getTree(filename);
                this.treemodel = new DefaultTreeModel(this.root);
                this.tree = new JTree(this.treemodel);
                this.tree.setCellRenderer(new ZipTreeCR());
                this.tree.setTransferHandler(fileth);
                Container controls = Box.createHorizontalBox();
                final JButton view = new JButton("View");
                view.setToolTipText("View the selected file(s) contents");
                view.setMnemonic(86);
                controls.add(view);
                final JButton editfile = new JButton("Edit");
                editfile.setToolTipText("Edit the selected file's path and filename");
                editfile.setMnemonic(69);
                controls.add(editfile);
                controls.add(Box.createHorizontalStrut(30));
                JButton addfile = new JButton("Add");
                addfile.setToolTipText("Add file(s) to the pak");
                addfile.setMnemonic(65);
                controls.add(addfile);
                final JButton delfile = new JButton("Delete");
                delfile.setToolTipText("Delete the selected file(s) from the pak");
                delfile.setMnemonic(127);
                controls.add(delfile);
                final JButton savefile = new JButton("Save");
                savefile.setToolTipText("Save the selected file to disk");
                savefile.setMnemonic(83);
                controls.add(savefile);
                controls.add(Box.createHorizontalStrut(30));
                JButton ascan = new JButton("Scan");
                ascan.setToolTipText("Scans files referenced in map");
                ascan.setMnemonic(67);
                controls.add(ascan);
                JButton auto = new JButton("Auto");
                auto.setToolTipText("Scans and adds all files referenced in map");
                auto.setMnemonic(85);
                controls.add(auto);
                panel.add(controls, "South");
                view.setEnabled(false);
                editfile.setEnabled(false);
                delfile.setEnabled(false);
                savefile.setEnabled(false);
                ListSelectionModel rowsel = this.table.getSelectionModel();
                rowsel.addListSelectionListener(lse -> {
                    if (!lse.getValueIsAdjusting()) {
                        ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
                        if (lsm.isSelectionEmpty()) {
                            view.setEnabled(false);
                            editfile.setEnabled(false);
                            delfile.setEnabled(false);
                            savefile.setEnabled(false);
                        } else {
                            view.setEnabled(true);
                            delfile.setEnabled(true);
                            savefile.setEnabled(true);
                            if (Unpak.this.table.getSelectedRowCount() == 1) {
                                editfile.setEnabled(true);
                            } else {
                                editfile.setEnabled(false);
                            }
                        }

                    }
                });
                this.tree.addTreeSelectionListener(_ -> {
                    TreePath[] paths = Unpak.this.tree.getSelectionPaths();
                    if (paths != null) {
                        for (int i = 0; i < paths.length; ++i) {
                            Object sel = ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
                            if (sel.getClass() != Zipf.class) {
                                Unpak.this.tree.removeSelectionPath(paths[i]);
                            }
                        }
                    }

                    int numselected = Unpak.this.tree.getSelectionCount();
                    if (numselected == 0) {
                        view.setEnabled(false);
                        editfile.setEnabled(false);
                        delfile.setEnabled(false);
                        savefile.setEnabled(false);
                    } else {
                        view.setEnabled(true);
                        delfile.setEnabled(true);
                        savefile.setEnabled(true);
                        if (numselected == 1) {
                            editfile.setEnabled(true);
                        } else {
                            editfile.setEnabled(false);
                        }
                    }

                });
                msave.addActionListener(_ -> {
                    try {
                        File sfile = new File(Unpak.this.infile.getAbsolutePath());
                        JFileChooser schooser = new JFileChooser(sfile);
                        schooser.setDialogTitle("Save map file - " + sfile.getName());
                        schooser.setFileFilter(new BspFileFilter());
                        schooser.setSelectedFile(sfile);
                        int result = schooser.showSaveDialog(Unpak.this.frame);
                        if (result == 1) {
                            return;
                        }

                        sfile = schooser.getSelectedFile();
                        String sfilename = sfile.getName();
                        if (sfile.exists()) {
                            result = JOptionPane.showConfirmDialog(Unpak.this.frame,
                                    "Map file \"" + sfile + "\" exists. \nAre you sure you want to overwrite?",
                                    "Save BSP file", 0);
                            if (result == 1) {
                                return;
                            }

                            Unpak.this.frame.setCursor(Cursor.getPredefinedCursor(3));
                            if (sfile.getCanonicalPath().equals(Unpak.this.infile.getCanonicalPath())) {
                                long ilength = Unpak.this.infile.length();
                                File renfile = new File(Unpak.this.infile.getAbsolutePath() + ".bak");
                                Cons.print("Copying current map file to " + renfile.getAbsolutePath() + "...");
                                RandomAccessFile copyraf = new RandomAccessFile(renfile, "rw");
                                copyraf.setLength(0L);
                                Unpak.this.raf.seek(0L);
                                Util.copyBlock(Unpak.this.raf, copyraf, ilength);
                                copyraf.close();
                                Cons.println("Done");
                                Unpak.this.infile = renfile;
                                if (!Unpak.this.infile.exists()) {
                                    Cons.println("Cannot find renamed file - map save aborted");
                                    Unpak.this.frame.setCursor(Cursor.getDefaultCursor());
                                    return;
                                }

                                Unpak.this.raf.close();
                                Unpak.this.raf = new RandomAccessFile(Unpak.this.infile, "r");
                                Unpak.this.zmodel.setfileparams(Unpak.this.raf, Unpak.this.m.getOffset());
                            }
                        }

                        Cons.print("Writing " + sfilename + "...");
                        Unpak.this.closeScan();
                        Unpak.this.raf.seek(0L);
                        RandomAccessFile outraf = new RandomAccessFile(sfile, "rw");
                        outraf.setLength(0L);
                        Cons.print("BSP data...");
                        Unpak.this.m.saveMap(Unpak.this.raf, outraf);
                        Cons.print("Pak data...");
                        Unpak.this.m.savePak(Unpak.this.raf, outraf);
                        outraf.close();
                        Cons.println("Done");
                        Unpak.this.raf.close();
                        Unpak.this.frame.setCursor(Cursor.getDefaultCursor());
                        Unpak.this.infile = sfile;
                        Unpak.this.raf = new RandomAccessFile(Unpak.this.infile, "rw");
                        Unpak.this.checkNav();
                        Unpak.this.raf.close();
                        Unpak.this.raf = new RandomAccessFile(Unpak.this.infile, "r");
                        Unpak.this.zmodel.setfileparams(Unpak.this.raf, Unpak.this.m.getOffset());
                        Unpak.this.tmodel.fireTableDataChanged();
                        Unpak.this.dirty = false;
                        Unpak.this.frame.setTitle("Pakrat - " + sfile.getName());
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }

                });
                savefile.addActionListener(_ -> {
                    int[] rows = Unpak.this.getSelection();
                    if (rows.length != 0) {
                        if (rows.length == 1) {
                            Zipf z = Unpak.this.zmodel.getzipfile(rows[0]);
                            File sfile = new File(z.getFullPath());
                            JFileChooser schooser = new JFileChooser(Pakpref.adddir);
                            schooser.setDialogTitle("Save selected file - " + z.getFullPath());
                            schooser.setSelectedFile(sfile);
                            int result = schooser.showSaveDialog(Unpak.this.frame);
                            if (result == 1) {
                                return;
                            }

                            sfile = schooser.getSelectedFile();
                            Unpak.this.savePakFile(z, sfile, false);
                        } else {
                            JFileChooser sc = new JFileChooser(Pakpref.adddir);
                            sc.setDialogTitle("Select location to save " + rows.length + " files");
                            sc.setFileSelectionMode(1);
                            int result = sc.showSaveDialog(Unpak.this.frame);
                            if (result == 1) {
                                return;
                            }

                            File path = sc.getSelectedFile();

                            for (int r = 0; r < rows.length; ++r) {
                                Zipf z = Unpak.this.zmodel.getzipfile(rows[r]);
                                File sfile = new File(path, z.getFileName());
                                if (!Unpak.this.savePakFile(z, sfile, true)) {
                                    break;
                                }
                            }
                        }

                    }
                });
                addfile.addActionListener(_ -> {
                    try {
                        JFileChooser fchooser = new JFileChooser(Pakpref.adddir);
                        fchooser.setDialogTitle("Select file(s) to add to pak");
                        fchooser.setApproveButtonToolTipText("Open the selected files(s)");
                        fchooser.setFileFilter(new MdlFileFilter());
                        fchooser.setFileFilter(new PakFileFilter());
                        fchooser.setFileFilter(new AllFileFilter());
                        fchooser.setMultiSelectionEnabled(true);
                        fchooser.setFileSelectionMode(2);
                        int result = fchooser.showOpenDialog(Unpak.this.frame);
                        if (result == 1) {
                            return;
                        }

                        File[] tfile = fchooser.getSelectedFiles();
                        Pakpref.adddir = fchooser.getCurrentDirectory().getPath();
                        Pakpref.put("Adddir", Pakpref.adddir);
                        Unpak.this.addFileToPak(tfile, Unpak.this.gamedir, false);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }

                });
                delfile.addActionListener(_ -> {
                    int[] rows = Unpak.this.getSelection();
                    if (rows.length != 0) {
                        Unpak.this.deletePakFiles(rows);
                    }
                });
                editfile.addActionListener(_ -> {
                    int[] rows = Unpak.this.getSelection();
                    if (rows.length != 0) {
                        Zipf z = Unpak.this.zmodel.getzipfile(rows[0]);
                        JTextField filetext = new JTextField(z.getFileName());
                        JTextField pathtext = new JTextField(z.getPath());
                        Container cbox = Box.createHorizontalBox();
                        cbox.add(new JLabel(
                                "Size: " + z.getSize() + "  CRC32: " + Integer.toHexString((int) z.getCRC())));
                        Container fbox = Box.createHorizontalBox();
                        fbox.add(new JLabel("Filename : "));
                        fbox.add(filetext);
                        Container pbox = Box.createHorizontalBox();
                        pbox.add(new JLabel("Path : "));
                        pbox.add(pathtext);
                        int result = JOptionPane.showOptionDialog(Unpak.this.frame,
                                new Object[] { z.getFullPath(), cbox, fbox, pbox }, "Edit file parameters", 2, -1,
                                (Icon) null, (Object[]) null, (Object) null);
                        if (result != 2) {
                            z.setFullPath(pathtext.getText() + "/" + filetext.getText());
                            Unpak.this.tmodel.fireTableDataChanged();
                            Unpak.this.dirty = true;
                            if (Unpak.this.treeview) {
                                Unpak.this.updateTree();
                            }

                        }
                    }
                });
                mpref.addActionListener(_ -> {
                    Unpak.this.doPreferences();
                });
                mload.addActionListener(_ -> {
                    try {
                        JFileChooser rchooser = new JFileChooser(Pakpref.mapdir);
                        rchooser.setDialogTitle("Open a map file");
                        rchooser.setFileFilter(new BspFileFilter());
                        int result = rchooser.showOpenDialog(Unpak.this.frame);
                        if (result == 1) {
                            return;
                        }

                        Unpak.this.closeScan();
                        Unpak.this.infile = rchooser.getSelectedFile();
                        String filename_ = Unpak.this.infile.getName();
                        if (!Unpak.this.infile.exists() || !Unpak.this.infile.canRead()) {
                            Cons.println("Can't open " + filename_);
                            return;
                        }

                        Cons.println("Reading " + filename_);
                        Pakpref.mapdir = Unpak.this.infile.getPath();
                        Pakpref.put("Mapdir", Pakpref.mapdir);
                        Unpak.this.raf = new RandomAccessFile(Unpak.this.infile, "r");
                        Unpak.this.m = new Mappak(false);
                        Unpak.this.frame.setCursor(Cursor.getPredefinedCursor(3));
                        Unpak.this.m.loadMap(Unpak.this.raf);
                        Unpak.this.frame.setCursor(Cursor.getDefaultCursor());
                        Unpak.this.frame.setTitle("Pakrat - " + filename_);
                        Unpak.this.zmodel = new ZipDirModel(Unpak.this.m.getZf());
                        Unpak.this.zmodel.setfileparams(Unpak.this.raf, Unpak.this.m.getOffset());
                        Unpak.this.tmodel = new TableSorter(Unpak.this.zmodel);
                        Unpak.this.table.setModel(Unpak.this.tmodel);
                        Unpak.this.tmodel.setTableHeader(Unpak.this.table.getTableHeader());
                        Unpak.this.table.setAutoResizeMode(1);
                        Unpak.this.table.getColumn("Size").setMaxWidth(50);
                        Unpak.this.table.getColumn("Type").setMaxWidth(50);
                        Unpak.this.table.getColumn("In").setMaxWidth(20);
                        Unpak.this.table.getColumn(ZipDirModel.header[1]).setCellRenderer(new ZipTableCR());
                        Unpak.this.dirty = false;
                        if (Unpak.this.treeview) {
                            Unpak.this.updateTree();
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }

                });
                mquit.addActionListener(_ -> {
                    int result = JOptionPane.showConfirmDialog(Unpak.this.frame,
                            "Quit Pakrat?" + (Unpak.this.dirty ? "\n(Changes have not been saved)" : ""), "Pakrat", 0);
                    if (result == 0) {
                        System.exit(0);
                    }

                });
                this.frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        if (!Unpak.this.dirty) {
                            System.exit(0);
                        }

                        int result = JOptionPane.showConfirmDialog(Unpak.this.frame,
                                "Quit Pakrat?\n(Changes have not been saved)", "Pakrat", 0);
                        if (result == 0) {
                            System.exit(0);
                        }

                    }
                });
                mtree.addActionListener(_ -> {
                    Unpak.this.treeview = mtree.getState();
                    if (Unpak.this.treeview) {
                        Unpak.this.updateTree();
                        Unpak.this.zmodel.setTreeSelection(Unpak.this.tree, Unpak.this.table);
                        sortmenu.setEnabled(false);
                        Unpak.this.mainsp.getViewport().setView(Unpak.this.tree);
                    } else {
                        Unpak.this.zmodel.setTableSelection(Unpak.this.tree, Unpak.this.table);
                        sortmenu.setEnabled(true);
                        Unpak.this.mainsp.getViewport().setView(Unpak.this.table);
                    }

                });
                snone.addActionListener(_ -> {
                    Unpak.this.tmodel.cancelSorting();
                });
                sname.addActionListener(_ -> {
                    int status = Unpak.this.tmodel.getSortingStatus(1);
                    status = status < 1 ? 1 : -1;
                    Unpak.this.tmodel.cancelSorting();
                    Unpak.this.tmodel.setSortingStatus(1, status);
                });
                spath.addActionListener(_ -> {
                    int status = Unpak.this.tmodel.getSortingStatus(2);
                    status = status < 1 ? 1 : -1;
                    Unpak.this.tmodel.cancelSorting();
                    Unpak.this.tmodel.setSortingStatus(2, status);
                });
                ssize.addActionListener(_ -> {
                    int status = Unpak.this.tmodel.getSortingStatus(3);
                    status = status < 1 ? 1 : -1;
                    Unpak.this.tmodel.cancelSorting();
                    Unpak.this.tmodel.setSortingStatus(3, status);
                });
                stype.addActionListener(_ -> {
                    int status = Unpak.this.tmodel.getSortingStatus(4);
                    status = status < 1 ? 1 : -1;
                    Unpak.this.tmodel.cancelSorting();
                    Unpak.this.tmodel.setSortingStatus(4, status);
                });
                view.addActionListener(_ -> {
                    int[] rows = Unpak.this.getSelection();
                    if (rows.length != 0) {
                        for (int i = 0; i < rows.length; ++i) {
                            Unpak.this.viewFile(Unpak.this.zmodel.getzipfile(rows[i]));
                        }

                    }
                });
                ascan.addActionListener(_ -> {
                    Unpak.this.closeScan();
                    Unpak.this.scan = new Scan(Unpak.this, Unpak.this.frame, Unpak.this.m, Unpak.this.zmodel,
                            Unpak.this.infile.getName(), Unpak.this.gamedir);
                });
                auto.addActionListener(_ -> {
                    Unpak.this.closeScan();
                    Unpak.this.scan = new Scan(Unpak.this, Unpak.this.frame, Unpak.this.m, Unpak.this.zmodel,
                            Unpak.this.infile.getName(), Unpak.this.gamedir, true);
                });
                mcons.addActionListener(_ -> {
                    Cons.show();
                });
                mhelp.addActionListener(_ -> {
                    String help = """
                             Pakrat %s
                             Original Pakrat 0.95 by Rof (rof@mellish.org.uk)
                             Edited by Mehis

                             A program for managing Half-Life 2 BSP PAK archives

                             File menu:
                              Load BSP     - load a new BSP file
                              Save BSP     - save the current BSP file, writing any changes to pak
                              Preferences  - set the game base directory, path-fixup, and autoscan options
                              Quit         - quit Pakrat

                             View menu:
                              As Tree      - view pak list as a directory tree
                              Sort...      - sort the pak list via columns

                             Help menu:
                              Console      - show console window
                              About Pakrat - this information

                             Button controls:
                              View         - view the selected pak entry
                              Edit         - edit the selected entry's file and path name
                              Add          - add a file or files to the pak
                              Delete       - delete the selected entry from the pak
                              Save         - save the selected entry to disk
                              Scan         - scan for all files used in map
                              Auto         - automatically scan and add all files used in map to the pak

                             About Pakrat
                              Pakrat is a graphical replacement for the command-line bspzip program.
                              HL2 map (.bsp) files contain a general file storage area, known as the
                              pak. Usually this area contains special material (.vmt) and texture
                              (.vtf) files which store the environment reflection maps from
                              env_cubemap entities generated when the console command buildcubemaps
                              is run. These files will be visible in the pak list of opened maps.

                              Pakrat allows you to add files to the pak, such as texture, material,
                              sound and model files. If these files are used in the map, they will
                              be preferentially loaded from the map's pak, allowing you to make
                              maps with custom textures, etc., embedded into the map .bsp file.
                              These maps therefore do not need to be distributed with extra files
                              to include custom components.

                             Path fixup
                              The Source engine looks for files in the pak with a certain relative
                              paths. For example, material and texture files should have a path
                              starting with the "materials" folder. If set to do so, Pakrat can
                              attempt to change the path of any file added to the pak such that it
                              is correct. The best way to do this is set the Game Root directory
                              under the Preferences menu item. This should be, for example:
                              "C:\\Games\\Steam\\SteamApps\\common\\Half-Life 2\\hl2"
                              for a typical HL2 installation. If mapping for CS:S or HL2DM, change
                              the Game Root appropriately. If the Game Root is not set, Pakrat
                              can attempt to guess the correct path from the file name and location.
                              You may also edit each pak entry's filename and path directly, using
                              the Edit button.

                             The View button shows the contents of the selected file(s). For material
                             (.vmt) files, the file is displayed as text. For textures (.vtf), a
                             summary of the texture properties is printed, and the texture bitmap
                             is displayed below. Unrecognised file types are displayed as ASCII
                             text or as a hex dump depending on which tab is selected.

                             The Scan button opens a new window which allows all files referenced
                             in the map geometry and entities to be scanned for. Files that can be
                             found on disk can be added to the pack by using the Add Selected button.

                             The Auto button performs a scan of used files and automatically adds any
                             file found on disk to the pak.


                            """.formatted(Version.getFullVersion());
                    Unpak.this.TextBox("About Pakrat", help);
                });
                this.frame.setDefaultCloseOperation(0);
                this.frame.setSize(640, 480);
                this.frame.getContentPane().add(panel);
                this.frame.setVisible(true);
            } else {
                Cons.println("Can't open " + filename);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void dump(String filename) throws Exception {
        this.auton = true;
        Cons.open(false);
        long starttime = System.currentTimeMillis();
        Cons.println(
                "**** Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)"
                        .formatted(Version.getFullVersion()));
        Cons.println("Dumping pak lump from " + filename);

        try {
            if (!filename.endsWith(".bsp")) {
                filename = filename + ".bsp";
            }

            this.infile = new File(filename);
            if (this.infile.exists() && this.infile.canRead()) {
                Cons.println("Reading " + filename);
                Pakpref.mapdir = this.infile.getPath();
                this.raf = new RandomAccessFile(this.infile, "r");
                this.m = new Mappak(true);
                this.m.loadMap(this.raf);
                String zipname = filename + ".zip";
                File outfile = new File(zipname);
                Cons.print("Writing " + zipname + "...");
                RandomAccessFile zraf = new RandomAccessFile(outfile, "rw");
                zraf.setLength(0L);
                this.raf.seek((long) this.m.getOffset());
                Util.copyBlock(this.raf, zraf, (long) this.m.getLength());
                zraf.close();
                Cons.println("done");
                this.raf.close();
                long duration = System.currentTimeMillis() - starttime;
                Cons.println("**** Pakrat file dump complete in "
                        + (new DecimalFormat("0.#")).format((double) ((float) duration / 1000.0F)) + " seconds");
            } else {
                Cons.println("Can't open " + filename);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void printList(String filename) throws Exception {
        this.auton = true;
        Cons.open(false);
        Cons.println(
                "Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)".formatted(Version.getFullVersion()));
        Cons.println("Listing pak files from " + filename);

        try {
            if (!filename.endsWith(".bsp")) {
                filename = filename + ".bsp";
            }

            this.infile = new File(filename);
            if (this.infile.exists() && this.infile.canRead()) {
                Pakpref.mapdir = this.infile.getPath();
                this.raf = new RandomAccessFile(this.infile, "r");
                this.m = new Mappak(true);
                this.m.loadMap(this.raf);
                this.zmodel = new ZipDirModel(this.m.getZf());
                this.zmodel.setfileparams(this.raf, this.m.getOffset());

                for (int i = 0; i < this.zmodel.getRowCount(); ++i) {
                    Zipf z = this.zmodel.getzipfile(i);
                    Cons.println(z.getFullDetails());
                }

                this.raf.close();
            } else {
                Cons.println("Can't open " + filename);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int addFileToPak(File[] tfile, String base, boolean yta) throws IOException {
        boolean all = yta;

        for (int i = 0; i < tfile.length; ++i) {
            if (tfile[i] != null) {
                if (tfile[i].isDirectory()) {
                    int res = this.addFileToPak(tfile[i].listFiles(), base, all);
                    if (res == -1) {
                        return -1;
                    }

                    if (res == 1) {
                        all = true;
                    }
                } else {
                    String tfilename = tfile[i].getName();
                    if (tfile[i].exists() && tfile[i].canRead()) {
                        String fullPath = Util.normalizePath(tfile[i].getAbsolutePath());
                        String relativePath = Util.getRelativePath(fullPath, base);
                        boolean fixupPath = false;
                        if (Pakpref.fixup != 0) {
                            if (relativePath != null) {
                                if (Pakpref.fixup == 1 && !all) {
                                    int result = JOptionPane.showOptionDialog(this.frame,
                                            fullPath + "\nFix-up path to: \"" + relativePath + "\" ?",
                                            "Add file " + (i + 1) + " of " + tfile.length, 0, 3, (Icon) null,
                                            new String[] { "Yes", "Yes to All", "No", "Skip", "Cancel" },
                                            (Object) null);
                                    if (result == 0 || result == 1) {
                                        fixupPath = true;
                                    }

                                    if (result == 1) {
                                        all = true;
                                    }

                                    if (result == 3) {
                                        continue;
                                    }

                                    if (result == 4) {
                                        return -1;
                                    }
                                } else {
                                    fixupPath = true;
                                }
                            }
                        }

                        Cons.println("Reading " + tfilename);
                        this.zmodel.addfile(Zipf.fromFile(tfile[i], fixupPath, base));
                        if (!this.auton) {
                            this.table.scrollRectToVisible(
                                    this.table.getCellRect(this.tmodel.getRowCount() - 1, 0, true));
                        }

                        this.dirty = true;
                        if (this.treeview) {
                            this.updateTree();
                        }
                    } else {
                        Cons.println("Can't open " + tfilename);
                    }
                }
            }
        }

        if (all) {
            return 1;
        } else {
            return 0;
        }
    }

    private void checkNav() {
        List<Zipf> fileList = this.m.getZf();
        for (int i = 0; i < fileList.size(); ++i) {
            Zipf z = fileList.get(i);
            if (z.getFileName().toLowerCase().endsWith(".nav")) {
                try {
                    this.raf.seek((long) (this.m.getOffset() + z.getDataOffset()));
                    byte[] buffer = new byte[z.getSize()];
                    this.raf.read(buffer);
                    ByteBuffer zb = ByteBuffer.wrap(buffer);
                    zb.order(ByteOrder.LITTLE_ENDIAN);
                    long magic = (long) zb.getInt() & -1L;
                    if (magic != -17958194L) {
                        Cons.println("Nav file " + z.getFullPath() + " is invalid.");
                    } else {
                        zb.getInt();
                        long nlen = (long) zb.getInt() & -1L;
                        long blen = this.raf.length();
                        if (nlen != blen) {
                            if (!this.auton) {
                                int result = JOptionPane
                                        .showConfirmDialog(
                                                this.frame, "Nav file \"" + z.getFullPath()
                                                        + "\" version does not match this bsp file.\n"
                                                        + "Do you want to update it?",
                                                "Check NAV file", 0);
                                if (result == 1) {
                                    break;
                                }
                            }

                            Cons.print("Updating " + z.getFullPath() + "...");
                            zb.position(8);
                            zb.putInt((int) blen);
                            CRC32 crc = new CRC32();
                            crc.update(buffer);
                            z.setCRC(crc.getValue());
                            this.raf.seek((long) (this.m.getOffset() + z.getDataOffset() + 8));
                            this.raf.writeInt(Swab.I((int) blen));
                            this.raf.seek((long) (this.m.getOffset() + z.getRelativeOffset() + 14));
                            this.raf.writeInt(Swab.I((int) z.getCRC()));
                            long cdpos = (long) (this.m.getOffset() + this.m.getCdoffs());

                            for (int j = 0; j < i; ++j) {
                                Zipf zj = fileList.get(j);
                                cdpos += (long) (46 + zj.getFullPath().length());
                            }

                            cdpos += 16L;
                            this.raf.seek(cdpos);
                            this.raf.writeInt(Swab.I((int) z.getCRC()));
                            Cons.println("Done");
                        } else {
                            Cons.println("Nav file " + z.getFullPath() + " matches BSP.");
                        }
                    }
                } catch (IOException ex) {
                    Cons.println(ex);
                }
            }
        }

    }

    private void closeScan() {
        if (this.scan != null) {
            this.scan.close();
        }

        this.scan = null;
    }

    private void TextBox(String title, String text) {
        JTextArea textarea = new JTextArea(text);
        textarea.setFont(new Font("Monospaced", 0, 14));
        textarea.setTabSize(4);
        textarea.setEditable(false);
        JFrame tframe = new JFrame(title);
        tframe.setLocationRelativeTo(this.frame);
        tframe.setSize(600, 250);
        tframe.getContentPane().add(new JScrollPane(textarea));
        tframe.setDefaultCloseOperation(2);
        tframe.setVisible(true);
    }

    private int[] getSelection() {
        if (this.treeview) {
            TreePath[] paths = this.tree.getSelectionPaths();
            if (paths == null) {
                return new int[0];
            } else {
                int[] rows = new int[paths.length];

                for (int i = 0; i < paths.length; ++i) {
                    Zipf sel = (Zipf) ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
                    int row = this.zmodel.getrow(sel);
                    if (row == -1) {
                        Cons.println("GetSelection: Couldn't find a match for " + sel);
                    } else {
                        rows[i] = row;
                    }
                }

                return rows;
            }
        } else {
            return this.tmodel.modelIndex(this.table.getSelectedRows());
        }
    }

    private void deletePakFiles(int[] rows) {
        Object[] options = new Object[] { "Yes", "Yes to All", "No", "Cancel" };
        boolean all = false;
        Arrays.sort(rows);

        for (int i = rows.length - 1; i >= 0; --i) {
            Zipf z = this.zmodel.getzipfile(rows[i]);
            if (!all) {
                int result = JOptionPane.showOptionDialog(this.frame,
                        "Remove file " + z.getFileName() + " from the pak?",
                        "Delete file " + (rows.length - i) + " of " + rows.length, 1, 3, (Icon) null, options,
                        options[0]);
                if (result == 2) {
                    continue;
                }

                if (result == 3 || result == -1) {
                    return;
                }

                if (result == 1) {
                    all = true;
                }
            }

            this.zmodel.deletefile(rows[i]);
        }

        this.dirty = true;
        if (this.treeview) {
            this.updateTree();
        }

    }

    private void updateTree() {
        this.root = this.zmodel.getTree(this.infile.getName());
        this.treemodel.nodeStructureChanged(this.root);
        this.treemodel = new DefaultTreeModel(this.root);
        this.tree.setModel(this.treemodel);
    }

    private boolean savePakFile(Zipf z, File sfile, boolean multi) {
        return savePakFile(this.m, this.raf, this.frame, this.auton, z, sfile, multi);
    }

    public static boolean savePakFile(Mappak mapPak, RandomAccessFile raf, JFrame frame, boolean auton, Zipf z,
            File sfile,
            boolean multi) {
        int off = mapPak.getOffset();

        try {
            ByteBuffer zb;
            if (z.isInPak()) {
                raf.seek((long) (off + z.getDataOffset()));
                byte[] buffer = new byte[z.getSize()];
                raf.read(buffer);
                zb = ByteBuffer.wrap(buffer);
            } else {
                zb = ByteBuffer.wrap(z.getData());
            }

            zb.order(ByteOrder.LITTLE_ENDIAN);
            String sfilename = sfile.getName();
            if (sfile.exists()) {
                if (auton) {
                    Cons.println("File exists, overwriting...");
                } else {
                    int options = 0;
                    if (multi) {
                        options = 1;
                    }

                    int result = JOptionPane.showConfirmDialog(frame, "File \"" + sfile + "\" exists, overwrite?",
                            "Save selected file" + (multi ? "s" : ""), options);
                    if (result == 1) {
                        return true;
                    }

                    if (result == 2) {
                        return false;
                    }
                }
            }

            Cons.print("Writing " + sfilename + " ...");
            FileOutputStream fos = new FileOutputStream(sfile);

            for (int i = 0; i < z.getSize(); ++i) {
                fos.write(zb.get());
            }

            fos.close();
            Cons.println("Done");
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }
    }

    private void doPreferences() {
        final JTextField gdirtext = new JTextField(this.gamedir);
        gdirtext.setPreferredSize(new Dimension(200, -1));
        gdirtext.setToolTipText("The directory to strip off path of files added to the pak");
        JButton gdfind = new JButton("...");
        gdfind.setToolTipText("Browse to set the game root directory");
        Box gbox = Box.createHorizontalBox();
        gbox.add(new JLabel("Game root directory :   "));
        gbox.add(gdirtext);
        gbox.add(gdfind);
        JComboBox<String> fcombo = new JComboBox<>(new String[] { "Never", "Ask", "Always" });
        fcombo.setSelectedIndex(Pakpref.fixup);
        fcombo.setToolTipText("Sets whether to strip off the root directory on adding a file");
        Box fbox = Box.createHorizontalBox();
        fbox.add(new JLabel("Path fixup on add file :   "));
        fbox.add(fcombo);
        gdfind.addActionListener(_ -> {
            JFileChooser dc = new JFileChooser(gdirtext.getText());
            dc.setDialogTitle("Set game root directory");
            dc.setFileSelectionMode(1);
            int dr = dc.showOpenDialog(Unpak.this.frame);
            if (dr != 1) {
                gdirtext.setText(dc.getSelectedFile().getPath());
            }

        });
        Box abox = Box.createVerticalBox();
        abox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Autoscan extra files:"));
        JCheckBox ab0 = new JCheckBox("NAV file (maps/<mapname>.nav)", Pakpref.navfile);
        ab0.setToolTipText("Scan for bot navigation file");
        abox.add(ab0);
        JCheckBox ab1 = new JCheckBox("AIN file (maps/graphs/<mapname>.ain)", Pakpref.ainfile);
        ab1.setToolTipText("Scan for NPC AI node file");
        abox.add(ab1);
        JCheckBox ab2 = new JCheckBox("Map soundcache (maps/soundcache/<mapname>.cache)", Pakpref.soundcache);
        ab2.setToolTipText("Scan for map-specific soundcache file");
        abox.add(ab2);
        JCheckBox ab3 = new JCheckBox("Map description (maps/<mapname>.txt)", Pakpref.description);
        ab3.setToolTipText("Scan for map description text file");
        abox.add(ab3);
        JCheckBox ab4 = new JCheckBox("Level overview (resource/overviews/<mapname>.txt)", Pakpref.overview);
        ab4.setToolTipText("Scan for level overview map file (and associated material/texture)");
        abox.add(ab4);
        JCheckBox ab5 = new JCheckBox("Soundscape (scripts/soundscapes_<mapname>.txt)", Pakpref.soundscape);
        ab5.setToolTipText("Scan for custom soundscape file (and associated sound files)");
        abox.add(ab5);
        abox.setToolTipText("Optional files to search for during Scan and Autoscan operations");
        int result = JOptionPane.showOptionDialog(this.frame, new Object[] { gbox, fbox, abox }, "Preferences", 2, -1,
                (Icon) null, (Object[]) null, (Object) null);
        if (result != 2) {
            this.gamedir = gdirtext.getText();
            this.gamedir = this.gamedir.replace(File.separatorChar, '/');
            if (this.gamedir.endsWith("/")) {
                this.gamedir = this.gamedir.substring(0, this.gamedir.length() - 1);
            }

            Pakpref.gamedir = this.gamedir;
            Pakpref.fixup = fcombo.getSelectedIndex();
            Pakpref.navfile = ab0.isSelected();
            Pakpref.ainfile = ab1.isSelected();
            Pakpref.soundcache = ab2.isSelected();
            Pakpref.description = ab3.isSelected();
            Pakpref.overview = ab4.isSelected();
            Pakpref.soundscape = ab5.isSelected();
            Pakpref.setInit();
        }
    }

    private static String readString(ByteBuffer b, int len) {
        StringBuilder linebuff = new StringBuilder();

        for (int i = 0; i < len; ++i) {
            char c = (char) b.get();
            linebuff.append(c);
        }

        return linebuff.toString();
    }

    private void viewFile(Zipf z) {
        try {
            ByteBuffer zb;
            if (z.isInPak()) {
                this.raf.seek((long) (this.m.getOffset() + z.getDataOffset()));
                byte[] buffer = new byte[z.getSize()];
                this.raf.read(buffer);
                zb = ByteBuffer.wrap(buffer);
            } else {
                zb = ByteBuffer.wrap(z.getData());
            }

            zb.order(ByteOrder.LITTLE_ENDIAN);
            switch (z.getType()) {
                case FileType.OTHER:
                case FileType.SOUND:
                    this.hexList(readString(zb, z.getSize()), z.getFullPath());
                    break;
                case FileType.MATERIAL:
                case FileType.TEXT:
                    String text = readString(zb, z.getSize());
                    this.TextBox("Pakrat - " + z.getFullPath(), text);
                    break;
                case FileType.TEXTURE:
                    this.vtfInfo(zb, z.getFullPath(), z.getSize());
                    break;
                case FileType.MODEL:
                    this.mdlInfo(zb, z.getFullPath(), z.getSize());
                    break;
                case FileType.MODEL_DAT:
                    if (z.getFullPath().toLowerCase().endsWith(".phy")) {
                        this.phyInfo(zb, z.getFullPath(), z.getSize());
                    } else {
                        this.hexList(readString(zb, z.getSize()), z.getFullPath());
                    }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    private void vtfInfo(ByteBuffer b, String filename, int size) {
        StringBuilder t = new StringBuilder();
        Vtf vtf = new Vtf();

        try {
            vtf.read(b, (long) size);
        } catch (Exception ex) {
            Cons.println(ex);
            t.append(ex.toString());
        }

        if (!vtf.isValid) {
            t.append("Invalid VTF file\n");
        } else {
            t.append("VTF file " + size + " bytes\n");
            t.append("Version " + vtf.vers[0] + "." + vtf.vers[1] + "\n");
            t.append("WxH: " + vtf.width + " x " + vtf.height + "\n");
            t.append("Flags: " + vtf.GetFlagStr() + "\n");
            t.append(
                    "Start frame " + vtf.startframe + " of " + vtf.numframes + " with " + vtf.GetFaceCount()
                            + " face(s)\n");
            t.append("Reflectivity: ");
            DecimalFormat df = new DecimalFormat("0.00");
            t.append(df.format((double) vtf.refx) + ", " + df.format((double) vtf.refy) + ", "
                    + df.format((double) vtf.refz));
            t.append("  Bumpscale: " + df.format((double) vtf.bumpscale) + "\n");
            if (vtf.imageformat < Vtf.imgfmt.length) {
                t.append("Image format: " + Vtf.imgfmt[vtf.imageformat]);
            } else {
                t.append("Unknown image format: " + vtf.imageformat);
            }

            t.append(" with " + vtf.nummips + " mip levels\n");
            if (vtf.isLR) {
                if (vtf.lrimageformat < Vtf.imgfmt.length) {
                    t.append("Low-res format: " + Vtf.imgfmt[vtf.lrimageformat]);
                } else {
                    t.append("Unknown lrif: " + vtf.lrimageformat);
                }

                t.append(" WxH: " + vtf.lrwidth + " x " + vtf.lrheight + "\n");
            } else {
                t.append("No low-res image\n");
            }
        }

        JFrame vframe = new JFrame("Pakrat - " + filename);
        vframe.setLocationRelativeTo(this.frame);
        JPanel panel = new JPanel();
        JTextArea textarea = new JTextArea(t.toString());
        textarea.setFont(new Font("Dialog", 0, 12));
        textarea.setTabSize(4);
        textarea.setEditable(false);
        panel.setLayout(new BorderLayout());
        panel.add(textarea, "North");
        if (vtf.isValid) {
            VImage vim = new VImage(vtf);
            panel.add(vim, "Center");
        }

        vframe.setSize(600, 480);
        vframe.getContentPane().add(new JScrollPane(panel));
        vframe.setDefaultCloseOperation(2);
        vframe.setVisible(true);
    }

    private void phyInfo(ByteBuffer b, String filename, int size) {
        StringBuilder t = new StringBuilder();
        Phymdl phy = new Phymdl();

        try {
            phy.read(b);
        } catch (IOException ex) {
            Cons.println(ex);
            t.append(ex.toString());
        }

        if (!phy.isValid) {
            t.append("Invalid PHY file : ID=" + Integer.toHexString(phy.id) + " version=" + phy.version + "\n");
        } else {
            t.append("Model collision file version " + phy.version + "\n");
            t.append("Solids: " + phy.numsolids + "\n");
            t.append("Checksum : " + Integer.toHexString(phy.checksum) + "\n\n");
            t.append("Gib models: ");
            if (phy.gibmodel.size() == 0) {
                t.append(" none\n");
            } else {
                t.append("\n");
            }

            for (int i = 0; i < phy.gibmodel.size(); ++i) {
                String gib = (String) phy.gibmodel.get(i);
                t.append("    " + gib + ".mdl");
                if (this.isInPak("models/" + gib + ".mdl")) {
                    t.append(" - in pak\n");
                } else {
                    t.append(" - not in pak\n");
                }
            }

            t.append("\nPhysics data:\n");
            t.append(phy.physblock);
        }

        this.TextBox("Pakrat - " + filename, t.toString());
    }

    private void mdlInfo(ByteBuffer b, String filename, int size) {
        StringBuilder t = new StringBuilder();
        Mdl model = new Mdl();

        try {
            model.read(b);
        } catch (IOException ex) {
            Cons.println(ex);
            t.append(ex.toString());
        }

        if (!model.isValid) {
            t.append("Invalid MDL file : ID=" + Integer.toHexString(model.id) + " version=" + model.version + "\n");
        } else {
            t.append("Model file version " + model.version + "  length " + model.length + " bytes\n");
            t.append("Internal name: " + model.name + "\n");
            t.append("Checksum : " + Integer.toHexString(model.checksum) + "\n");
            t.append(model.numtexpaths + " texture path(s)\n");

            for (int i = 0; i < model.numtexpaths; ++i) {
                t.append("    [materials/]" + model.texpaths[i] + "\n");
            }

            t.append(model.numtextures + " texture(s)\n");

            for (int i = 0; i < model.numtextures; ++i) {
                t.append("    " + model.textures[i]);
                if (this.isInPak("materials/" + model.texpaths[0] + model.textures[i] + ".vmt")) {
                    t.append(" - in pak\n");
                } else {
                    t.append(" - not in pak\n");
                }
            }

            t.append(model.numincmodels + " include model(s)\n");

            for (int i = 0; i < model.numincmodels; ++i) {
                t.append("    " + model.incmodelfile[i]);
                if (this.isInPak(model.incmodelfile[i])) {
                    t.append(" - in pak\n");
                } else {
                    t.append(" - not in pak\n");
                }
            }
        }

        String[] modelexts = new String[] { ".phy", ".sw.vtx", ".dx80.vtx", ".dx90.vtx", ".vvd" };
        t.append("Associated files\n");

        for (int i = 0; i < modelexts.length; ++i) {
            String amfname = strsubext(filename, modelexts[i]);
            t.append("    " + amfname);
            if (this.isInPak(amfname)) {
                t.append(" - in pak\n");
            } else {
                t.append(" - not in pak\n");
            }
        }

        this.TextBox("Pakrat - " + filename, t.toString());
    }

    private void hexList(String input, String filename) {
        StringBuilder text = new StringBuilder();
        StringBuilder hext = new StringBuilder();

        for (int i = 0; i < input.length(); ++i) {
            if (i % 32 == 0) {
                hext.append("\n");
            }

            if (i % 64 == 0) {
                text.append("\n");
            }

            int c = input.charAt(i) & 255;
            if (c < 16) {
                hext.append("0");
            }

            hext.append(Integer.toHexString(c));
            if (i % 4 == 3) {
                hext.append(" ");
            }

            if (c > 31) {
                text.append((char) c);
            } else {
                text.append('\u0000');
            }
        }

        text.deleteCharAt(0);
        hext.deleteCharAt(0);
        JTextArea textarea = new JTextArea(text.toString());
        textarea.setFont(new Font("Monospaced", 0, 14));
        textarea.setTabSize(4);
        textarea.setEditable(false);
        JTextArea hextarea = new JTextArea(hext.toString());
        hextarea.setFont(new Font("Monospaced", 0, 14));
        hextarea.setTabSize(4);
        hextarea.setEditable(false);
        JTabbedPane tabbox = new JTabbedPane();
        tabbox.addTab("ASCII", new JScrollPane(textarea));
        tabbox.addTab("Hex", new JScrollPane(hextarea));
        JFrame tframe = new JFrame("Pakrat - " + filename);
        tframe.setLocationRelativeTo(this.frame);
        tframe.setSize(620, 350);
        tframe.getContentPane().setLayout(new BorderLayout());
        tframe.getContentPane().add(tabbox, "Center");
        tframe.setDefaultCloseOperation(2);
        tframe.setVisible(true);
    }

    private static String strsubext(String file, String ext) {
        file = file.replace(File.separatorChar, '/');
        int idot = file.lastIndexOf(".");
        int isep = file.lastIndexOf("/");
        return idot >= 0 && idot >= isep ? file.substring(0, idot) + ext : file + ext;
    }

    public boolean isInPak(String filename) {
        filename = filename.replace(File.separatorChar, '/');
        Zipf f = this.zmodel.getbyname(filename);
        return f != null;
    }

    public File getInfile() {
        return this.infile;
    }

    public static void main(String[] args) throws Exception {
        Unpak inst = new Unpak();
        String fn;
        if (args.length < 1) {
            fn = null;
        } else {
            if (args[0].equalsIgnoreCase("-save")) {
                fn = args[1];
                String pakFile = args[2];
                UnpakCli.savePakFileToDisk(fn, pakFile, new File(pakFile).getName());
                return;
            }

            if (args[0].equalsIgnoreCase("-list")) {
                fn = args[1];
                inst.printList(fn);
                return;
            }

            if (args[0].equalsIgnoreCase("-dump")) {
                fn = args[1];
                inst.dump(fn);
                return;
            }

            if (args[0].equalsIgnoreCase("-auto")) {
                String bn = args[1];
                fn = args[2];
                inst.exec(bn, fn);
                return;
            }

            if (args.length != 1) {
                System.out.println(
                        """
                                Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)
                                Usage:
                                  pakrat [<filename.bsp>]
                                  pakrat -auto <base directory> <filename.bsp>
                                  pakrat -list <filename.bsp>
                                  pakrat -save <filename.bsp> <pakfile>
                                  pakrat -dump <filename.bsp>
                                """.formatted(Version.getFullVersion()));
                return;
            }

            fn = args[0];
        }

        inst.exec(fn);
    }
}
