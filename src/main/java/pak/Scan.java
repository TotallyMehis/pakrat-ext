package pak;

import static java.util.Locale.ROOT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class Scan {
    private final Mappak m;
    private final ZipDirModel tmod;
    private JFrame sframe;
    private final JTable stable;
    private final ScanModel smodel;
    private final RandomAccessFile raf;
    private final Unpak pakrat;
    private JProgFrame prog;
    private final List<Scanfile> files;
    private final boolean auton;
    private boolean nofiles;
    private static final String[] mattexref = new String[] { "$basetexture", "$basetexture2", "$bumpmap", "$bumpmap2",
            "$envmap",
            "$normalmap", "$dudvmap", "$envmapmask", "$detail", "$parallaxmap", "$parallaxmap2", "$texture2",
            "$selfillumtexture", "$cloudalphatexture", "$gradienttexture", "$fbtexture", "$refracttexture",
            "$refracttinttexture", "$hdrbasetexture" };
    private static final String[] matmatref = new String[] { "include", "$fallbackmaterial", "$bottommaterial",
            "$crackmaterial",
            "$material", "$modelmaterial", "$translucent_material" };
    private static final String[] mdlext = new String[] { ".dx80.vtx", ".dx90.vtx", ".sw.vtx", ".phy", ".vvd" };
    private String basedir;
    private final List<String> dirset;

    public Scan(Unpak pakrat, Component parent, Mappak m, ZipDirModel tmod, String fname, String gamedir) {
        this(pakrat, parent, m, tmod, fname, gamedir, false);
    }

    public Scan(Unpak pakrat, Component parent, Mappak m, ZipDirModel tmod, String fname, String gamedir,
            boolean autoadd) {
        this.prog = null;
        this.nofiles = false;
        this.dirset = new ArrayList<>();
        this.auton = parent == null;
        this.pakrat = pakrat;
        this.m = m;
        this.tmod = tmod;
        this.raf = tmod.getbuff();
        this.files = new ArrayList<>();
        if (autoadd) {
            this.stable = null;
            this.smodel = null;
            this.basedir = gamedir;
            this.doAutoscan(parent);
        } else {
            this.sframe = new JFrame("Pakrat - Scan Files - " + fname);
            this.sframe.setLocationRelativeTo(parent);
            JPanel spanel = new JPanel(new BorderLayout());
            this.smodel = new ScanModel(this.files);
            this.stable = new JTable(this.smodel);
            Container topbox = Box.createHorizontalBox();
            topbox.add(new JLabel("Gamedir: "));
            final JComboBox<String> filebox = new JComboBox<>(this.getBaseDirs(gamedir));
            Font ffont = filebox.getFont();
            filebox.setFont(new Font(ffont.getName(), 0, ffont.getSize() - 1));
            filebox.setToolTipText("Base game directory to scan");
            filebox.setPreferredSize(new Dimension(450, filebox.getPreferredSize().height));
            topbox.add(filebox);
            filebox.setEditable(true);
            topbox.add(Box.createHorizontalGlue());
            final JButton scanb = new JButton("Scan");
            scanb.setToolTipText("Perform file scan");
            scanb.setMnemonic(83);
            topbox.add(scanb);
            Container controls = Box.createHorizontalBox();
            final JButton selall = new JButton("Select All");
            selall.setToolTipText("Select all On Disk files");
            selall.setMnemonic(69);
            controls.add(selall);
            selall.setEnabled(false);
            final JButton selnone = new JButton("Select None");
            selnone.setToolTipText("Clear selection");
            selnone.setMnemonic(78);
            controls.add(selnone);
            selnone.setEnabled(false);
            controls.add(Box.createHorizontalStrut(40));
            final JButton check = new JButton("Reference");
            check.setToolTipText("Check file reference");
            check.setMnemonic(82);
            check.setEnabled(false);
            controls.add(check);
            controls.add(Box.createHorizontalGlue());
            final JButton addsel = new JButton("Add Selected");
            addsel.setToolTipText("Add selected file(s) to the pak");
            addsel.setMnemonic(65);
            controls.add(addsel);
            addsel.setEnabled(false);
            JButton cancel = new JButton("Done");
            cancel.setToolTipText("Close scan window");
            cancel.setMnemonic(68);
            controls.add(cancel);
            this.stable.setAutoResizeMode(1);
            this.stable.setSelectionMode(0);
            this.stable.getColumn(ScanModel.header[2]).setMaxWidth(78);
            this.stable.getColumn(ScanModel.header[3]).setMaxWidth(60);
            this.stable.getColumn(ScanModel.header[4]).setMaxWidth(24);
            this.stable.getColumn(ScanModel.header[3]).setCellRenderer(new ScanTCR());
            this.stable.getColumn(ScanModel.header[4]).setCellRenderer(new ScanTCBR());
            JScrollPane stablesp = new JScrollPane(this.stable);
            stablesp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
                    BorderFactory.createEtchedBorder()));
            spanel.add(stablesp, "Center");
            spanel.add(controls, "South");
            spanel.add(topbox, "North");
            this.sframe.setDefaultCloseOperation(2);
            this.sframe.setSize(640, 400);
            this.sframe.getContentPane().add(spanel);
            this.sframe.setVisible(true);
            scanb.addActionListener(_ -> {
                Scan.this.basedir = (String) filebox.getSelectedItem();
                Scan.this.addBaseDir(Scan.this.basedir);
                Scan.this.sframe.setCursor(Cursor.getPredefinedCursor(3));
                SwingWorker worker = new SwingWorker() {
                    @Override
                    public Object construct() {
                        Scan.this.doScan();
                        return null;
                    }

                    @Override
                    public void finished() {
                        Scan.this.smodel.update();
                        Scan.this.sframe.setCursor(Cursor.getDefaultCursor());
                        selall.setEnabled(true);
                        selnone.setEnabled(true);
                        addsel.setEnabled(Scan.this.smodel.setallselected());
                    }
                };
                worker.start();
            });
            addsel.addActionListener(_ -> {
                Scan.this.addSelected();
                scanb.doClick();
            });
            selall.addActionListener(_ -> {
                addsel.setEnabled(Scan.this.smodel.setallselected());
            });
            selnone.addActionListener(_ -> {
                Scan.this.smodel.resetallselected();
                addsel.setEnabled(false);
            });
            cancel.addActionListener(_ -> {
                Scan.this.close();
            });
            check.addActionListener(_ -> {
                Scan.this.printFileReferences(Scan.this.smodel.getfile(Scan.this.stable.getSelectedRow()));
            });
            ListSelectionModel tabsel = this.stable.getSelectionModel();
            tabsel.addListSelectionListener(lse -> {
                if (!lse.getValueIsAdjusting()) {
                    if (Scan.this.stable.getSelectedRowCount() == 0) {
                        check.setEnabled(false);
                    } else {
                        check.setEnabled(true);
                    }

                    if (Scan.this.smodel.noneselected()) {
                        addsel.setEnabled(false);
                    } else {
                        addsel.setEnabled(true);
                    }

                }
            });
        }
    }

    private void printFileReferences(Scanfile s) {
        StringBuilder t = new StringBuilder("File: " + s.fullname + "\n");
        t.append("Type: " + s.type.getPrettyName() + "\n");
        t.append("Diskname: " + s.diskname + "\n");
        t.append("Location: ");
        if (s.inpak) {
            t.append("In Pak  ");
        } else if (s.inlist) {
            t.append("In Pak List  ");
        }

        if (s.ondisk) {
            t.append("On Disk");
        }

        if (!s.inpak && !s.inlist && !s.ondisk) {
            t.append("Not Found");
        }

        t.append("\n");
        printFile(s, t);
        JTextArea textarea = new JTextArea(t.toString());
        textarea.setEditable(false);
        JFrame tframe = new JFrame("Pakrat - Check File Reference - " + s.listname);
        tframe.setLocationRelativeTo(this.sframe);
        tframe.setSize(550, 250);
        tframe.getContentPane().add(new JScrollPane(textarea));
        tframe.setDefaultCloseOperation(2);
        tframe.setVisible(true);
    }

    private static void printFile(Scanfile s, StringBuilder t) {
        if (s == null || s.parent == null) {
            return;
        }

        t.append("\nReferenced from:\n");
        if (s.parent == Scanfile.ENTITY) {
            t.append("    Entity list - " + s.referent + "\n");
        } else if (s.parent == Scanfile.STATIC) {
            t.append("    Prop_static list\n");
        } else if (s.parent == Scanfile.DETAIL) {
            t.append("    Prop_detail list\n");
        } else if (s.parent == Scanfile.TEXTURE) {
            t.append("    Map material list\n");
        } else if (s.parent == Scanfile.OTHER) {
            t.append("    Extra file list\n");
        } else {
            t.append("    File: " + s.parent.fullname + " - keyword \"" + s.referent + "\"\n");
            printFile(s.parent, t);
        }

    }

    private String[] getBaseDirs(String gamedir) {
        this.dirset.clear();

        for (int i = 0; i < 6; ++i) {
            String dir = Pakpref.get("Basedir" + i, (String) null);
            if (dir != null) {
                this.dirset.add(dir);
            }
        }

        this.dirset.add(gamedir);
        String mapbase = this.pakrat.getInfile().getPath().replace(File.separatorChar, '/');
        int im = mapbase.toLowerCase(ROOT).lastIndexOf("maps/");
        if (im > 0) {
            mapbase = mapbase.substring(0, im - 1);
            this.dirset.add(mapbase);
        }

        this.compactBaseDirs();
        return (String[]) this.dirset.toArray(new String[0]);
    }

    private void addBaseDir(String dir) {
        this.dirset.add(0, dir);
        this.compactBaseDirs();
        String[] dirs = (String[]) this.dirset.toArray(new String[0]);

        for (int i = 0; i < dirs.length && i != 6; ++i) {
            Pakpref.put("Basedir" + i, dirs[i]);
        }

    }

    private void compactBaseDirs() {
        boolean[] marked = new boolean[this.dirset.size()];
        String[] dirs = (String[]) this.dirset.toArray(new String[0]);

        for (int i = 0; i < dirs.length; ++i) {
            marked[i] = false;
        }

        for (int i = 0; i < dirs.length; ++i) {
            for (int j = i + 1; j < dirs.length; ++j) {
                if (dirs[i].equals(dirs[j])) {
                    marked[j] = true;
                }
            }
        }

        for (int i = dirs.length - 1; i >= 0; --i) {
            if (marked[i]) {
                this.dirset.remove(i);
            }
        }

    }

    private void addSelected() {
        File[] filearray = new File[this.smodel.numselected()];
        int j = 0;

        try {
            for (int i = 0; i < this.smodel.getRowCount(); ++i) {
                Scanfile sf = this.smodel.getfile(i);
                if (sf.onlydisk()) {
                    File sfile = new File(sf.diskname);
                    if (sfile.exists() && sfile.canRead()) {
                        filearray[j++] = sfile;
                    } else {
                        Cons.println("Couldn't read " + sfile + " from disk");
                    }
                }
            }

            this.pakrat.addFileToPak(filearray, this.basedir, false);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void doAutoscan(final Component parent) {
        if (this.basedir.equals("")) {
            String mapbase = this.pakrat.getInfile().getPath().replace(File.separatorChar, '/');
            int im = mapbase.toLowerCase(ROOT).lastIndexOf("maps/");
            if (im > 0) {
                mapbase = mapbase.substring(0, im - 1);
                this.basedir = mapbase;
            }
        }

        if (this.auton) {
            this.doScan();
            if (this.autoAddFiles((Component) null) == 0) {
                this.nofiles = true;
            }
        } else {
            parent.setCursor(Cursor.getPredefinedCursor(3));
            SwingWorker worker = new SwingWorker() {
                @Override
                public Object construct() {
                    Scan.this.doScan();
                    return null;
                }

                @Override
                public void finished() {
                    parent.setCursor(Cursor.getDefaultCursor());
                    Scan.this.autoAddFiles(parent);
                }
            };
            worker.start();
        }

    }

    private int autoAddFiles(Component parent) {
        Iterator<Scanfile> fit = this.files.iterator();

        while (fit.hasNext()) {
            if (!((Scanfile) fit.next()).onlydisk()) {
                fit.remove();
            }
        }

        if (this.files.size() == 0) {
            if (this.auton) {
                Cons.println("Found no files to add.");
            } else {
                JOptionPane.showMessageDialog(parent, "Found no files to add.", "Auto Scan", 1);
            }

            return 0;
        } else {
            if (this.auton) {
                Cons.println("Found " + this.files.size() + " referenced file(s) on disk.");
            } else {
                int res = JOptionPane.showConfirmDialog(parent,
                        new Object[] { "Found " + this.files.size() + " file(s) on disk.", "Add them to the Pak?" },
                        "Auto Scan", 0);
                if (res == 1) {
                    return 0;
                }
            }

            File[] farray = new File[this.files.size()];

            for (int i = 0; i < this.files.size(); ++i) {
                File sfile = new File(((Scanfile) this.files.get(i)).diskname);
                if (this.auton) {
                    Cons.println(sfile);
                }

                if (sfile.exists() && sfile.canRead()) {
                    farray[i] = sfile;
                } else {
                    Cons.println("Couldn't read " + sfile + " from disk");
                }
            }

            try {
                this.pakrat.addFileToPak(farray, this.basedir, true);
            } catch (IOException e) {
                System.out.println(e);
            }

            return this.files.size();
        }
    }

    private void doScan() {
        this.files.clear();

        try {
            if (!this.auton) {
                this.prog = new JProgFrame(this.sframe, "Pakrat - Scanning for files...");
                this.prog.start("Textures...", true);
            }

            Cons.print("Scanning textures...");
            this.scanTextures();
            if (!this.auton) {
                this.prog.setString("Prop_statics...");
                this.prog.setValue(0);
            }

            Cons.print("prop_statics...");
            this.scanStatics();
            if (!this.auton) {
                this.prog.setString("Prop_details...");
                this.prog.setValue(0);
            }

            Cons.print("prop_details...");
            this.scanDetails();
            if (!this.auton) {
                this.prog.setString("Loading entities...");
                this.prog.setValue(0);
            }

            Cons.print("entities...");
            this.scanEntities();
            if (!this.auton) {
                this.prog.setString("Extra files...");
                this.prog.setValue(0);
            }

            Cons.print("extras...");
            this.scanExtras();
            if (!this.auton) {
                this.prog.end();
            }

            Cons.println(this.compactList() + " duplicates removed");
            if (!this.auton) {
                int pakfiles = this.tmod.getRowCount();
                boolean[] refd = new boolean[pakfiles];
                String mapname = this.pakrat.getInfile().getName().toLowerCase(ROOT);
                String cubemappath = "materials/maps/" + mapname.substring(0, mapname.lastIndexOf(".bsp"));

                for (int i = 0; i < pakfiles; ++i) {
                    refd[i] = this.tmod.getzipfile(i).getPath().startsWith(cubemappath);
                }

                for (int i = 0; i < this.files.size(); ++i) {
                    Scanfile s = this.files.get(i);
                    if (s.zip != null) {
                        int row = this.tmod.getrow(s.zip);
                        if (row != -1) {
                            refd[row] = true;
                        }
                    }
                }

                int rcount = 0;

                for (int i = 0; i < pakfiles; ++i) {
                    if (refd[i]) {
                        ++rcount;
                    }
                }

                if (rcount != pakfiles) {
                    Cons.println(pakfiles - rcount + " files in Pak were not referenced in scan (excluding cubemaps):");

                    for (int i = 0; i < pakfiles; ++i) {
                        if (!refd[i]) {
                            Cons.println("  " + this.tmod.getzipfile(i).toString());
                        }
                    }

                    Cons.println();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    public void close() {
        if (this.sframe != null) {
            this.sframe.dispose();
        }

        this.sframe = null;
    }

    private void scanExtras() throws IOException {
        if (!this.auton) {
            this.prog.setMaximum(7);
        }

        String mapname = this.pakrat.getInfile().getName().toLowerCase(ROOT);
        mapname = mapname.substring(0, mapname.lastIndexOf(".bsp"));
        if (!this.auton) {
            if (Pakpref.navfile) {
                Scanfile sfile = new Scanfile("maps/" + mapname + ".nav", this.tmod, this.basedir, ScanfileType.NAV,
                        Scanfile.OTHER,
                        "");
                this.prog.setValue(1);
                this.files.add(sfile);
            }

            if (Pakpref.ainfile) {
                Scanfile sfile = new Scanfile("maps/graphs/" + mapname + ".ain", this.tmod, this.basedir,
                        ScanfileType.AIN,
                        Scanfile.OTHER, "");
                this.prog.setValue(2);
                this.files.add(sfile);
            }

            if (Pakpref.soundcache) {
                Scanfile sfile = new Scanfile("maps/soundcache/" + mapname + ".cache", this.tmod, this.basedir,
                        ScanfileType.CACHE,
                        Scanfile.OTHER, "");
                this.prog.setValue(3);
                this.files.add(sfile);
            }
        }

        if (Pakpref.description) {
            Scanfile sfile = new Scanfile("maps/" + mapname + ".txt", this.tmod, this.basedir, ScanfileType.TXT,
                    Scanfile.OTHER,
                    "");
            if (!this.auton) {
                this.prog.setValue(4);
            }

            this.files.add(sfile);
        }

        if (Pakpref.overview) {
            Scanfile sfile = new Scanfile("resource/overviews/" + mapname + ".txt", this.tmod, this.basedir,
                    ScanfileType.TXT,
                    Scanfile.OTHER, "");
            if (!this.auton) {
                this.prog.setValue(5);
            }

            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

        if (Pakpref.soundscape) {
            Scanfile sfile = new Scanfile("scripts/soundscapes_" + mapname + ".txt", this.tmod, this.basedir,
                    ScanfileType.TXT,
                    Scanfile.OTHER, "");
            if (!this.auton) {
                this.prog.setValue(6);
            }

            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

        if (!this.auton) {
            this.prog.setValue(7);
        }

    }

    private void scanEntities() throws IOException {
        this.m.loadEntities(this.raf, this.prog);
        if (!this.auton) {
            this.prog.setString("Entities...");
        }

        List<String> entValueList = this.m.getEntvallist();
        if (!this.auton) {
            this.prog.setMaximum(entValueList.size());
        }

        List<String> entKeyList = this.m.getEntkeylist();

        assert entValueList.size() == entKeyList.size();
        for (int i = 0; i < entValueList.size(); ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            String name = entValueList.get(i);
            String ref = entKeyList.get(i);
            Scanfile sfile = new Scanfile(name, this.tmod, this.basedir, ScanfileType.getTypeFromFilename(name),
                    Scanfile.ENTITY, ref);
            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

    }

    private void scanStatics() throws IOException {
        List<String> staticNames = this.m.getStaticname();
        if (!this.auton) {
            this.prog.setMaximum(staticNames.size());
        }

        for (int i = 0; i < staticNames.size(); ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(staticNames.get(i), this.tmod, this.basedir, ScanfileType.MDL,
                    Scanfile.STATIC, "");
            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

    }

    private void scanDetails() throws IOException {
        List<String> detailNames = this.m.getDetailname();
        if (!this.auton) {
            this.prog.setMaximum(detailNames.size());
        }

        for (int i = 0; i < detailNames.size(); ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(detailNames.get(i), this.tmod, this.basedir, ScanfileType.MDL,
                    Scanfile.DETAIL, "");
            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

    }

    private void scanTextures() throws IOException {
        List<String> textures = this.m.getTexname();
        if (!this.auton) {
            this.prog.setMaximum(textures.size());
        }

        for (int i = 0; i < textures.size(); ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(textures.get(i), this.tmod, this.basedir, ScanfileType.VMT, Scanfile.TEXTURE,
                    "");
            this.files.add(sfile);
            this.files.addAll(this.checkSubfile(sfile));
        }

    }

    private List<Scanfile> checkSubfile(Scanfile sf) throws IOException {
        List<Scanfile> subfiles = new ArrayList<>();
        if (sf.inlist || sf.ondisk) {
            switch (sf.type) {
                case VMT -> {
                    subfiles = this.getRefFromVmt(sf);
                }
                case MDL -> {
                    subfiles = this.getRefFromMdl(sf);
                }
                case PHY -> {
                    subfiles = this.getRefFromPhy(sf);
                }
                case TXT -> {
                    if (sf.fullname.startsWith("resource/overviews/")
                            || sf.fullname.startsWith("scripts/soundscapes_")) {
                        subfiles = this.getRefFromTxt(sf);
                    }
                }
                case NUL, VTF, VTX, VVD, WAV, MP3, NAV, AIN, CACHE -> {
                }
            }
        }

        return subfiles;
    }

    private List<Scanfile> getRefFromPhy(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getFileBuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                buff.order(ByteOrder.LITTLE_ENDIAN);
                Phymdl phy = new Phymdl();
                phy.read(buff);
                if (!phy.isValid) {
                    Cons.println("Failed to read model physics file");
                    return sublist;
                } else {
                    for (int j = 0; j < phy.gibmodel.size(); ++j) {
                        Scanfile sfile = new Scanfile((String) phy.gibmodel.get(j), this.tmod, this.basedir,
                                ScanfileType.MDL,
                                s,
                                "gib model");
                        sublist.add(sfile);
                        sublist.addAll(this.checkSubfile(sfile));
                    }

                    return sublist;
                }
            }
        }
    }

    private List<Scanfile> getRefFromMdl(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        String basename = s.name;

        for (int i = 0; i < mdlext.length; ++i) {
            String name = basename + mdlext[i];
            Scanfile sfile = new Scanfile(name, this.tmod, this.basedir, ScanfileType.getTypeFromFilename(name), s,
                    "datafile");
            sublist.add(sfile);
            sublist.addAll(this.checkSubfile(sfile));
        }

        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getFileBuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                buff.order(ByteOrder.LITTLE_ENDIAN);
                Mdl model = Mdl.read(buff);
                if (!model.isValid()) {
                    Cons.println("Failed to read model");
                    return sublist;
                } else {
                    List<String> texturelist = model.getTextureList();

                    for (int j = 0; j < texturelist.size(); ++j) {
                        Scanfile sfile = new Scanfile((String) texturelist.get(j), this.tmod, this.basedir,
                                ScanfileType.VMT, s,
                                "texture");
                        sublist.add(sfile);
                        sublist.addAll(this.checkSubfile(sfile));
                    }

                    for (String incmodel : model.getIncmodels()) {
                        Scanfile sfile = new Scanfile(incmodel, this.tmod, this.basedir,
                                ScanfileType.MDL,
                                s,
                                "include model");
                        sublist.add(sfile);
                        sublist.addAll(this.checkSubfile(sfile));
                    }

                    return sublist;
                }
            }
        }
    }

    private List<Scanfile> getRefFromTxt(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getFileBuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                while ((long) buff.position() < s.length) {
                    String line = readline(buff).trim();
                    if (line.length() != 0) {
                        int ic = line.indexOf("//");
                        if (ic > -1) {
                            line = line.substring(0, ic);
                        }

                        String[] token = tokenize(line);
                        if (token.length == 2) {
                            if (token[0].equalsIgnoreCase("material")) {
                                Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, ScanfileType.VMT, s,
                                        "material");
                                sublist.add(ssfile);
                                List<Scanfile> subsublist = this.getRefFromVmt(ssfile);

                                for (int j = 0; j < subsublist.size(); ++j) {
                                    sublist.add(subsublist.get(j));
                                }
                            } else if (token[0].equalsIgnoreCase("wave")) {
                                Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, ScanfileType.WAV, s,
                                        "wave");
                                sublist.add(ssfile);
                            }
                        }
                    }
                }

                return sublist;
            }
        }
    }

    private List<Scanfile> getRefFromVmt(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getFileBuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                while ((long) buff.position() < s.length) {
                    String line = readline(buff).trim();
                    if (line.length() != 0) {
                        int ic = line.indexOf("//");
                        if (ic > -1) {
                            line = line.substring(0, ic);
                        }

                        String[] token = tokenize(line);
                        if (token.length == 2 && !token[1].startsWith("_")) {
                            for (int i = 0; i < matmatref.length; ++i) {
                                if (token[0].equalsIgnoreCase(matmatref[i])) {
                                    Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, ScanfileType.VMT,
                                            s,
                                            token[0]);
                                    sublist.add(ssfile);
                                    List<Scanfile> subsublist = this.getRefFromVmt(ssfile);

                                    for (int j = 0; j < subsublist.size(); ++j) {
                                        sublist.add(subsublist.get(j));
                                    }
                                }
                            }

                            for (int i = 0; i < mattexref.length; ++i) {
                                if (token[0].equalsIgnoreCase(mattexref[i])) {
                                    if (mattexref[i].equals("$envmap")) {
                                        if (!token[1].equalsIgnoreCase("env_cubemap")) {
                                            Scanfile vfile = new Scanfile(token[1], this.tmod, this.basedir,
                                                    ScanfileType.VTF,
                                                    s,
                                                    token[0]);
                                            sublist.add(vfile);
                                        }
                                    } else {
                                        Scanfile vfile = new Scanfile(token[1], this.tmod, this.basedir,
                                                ScanfileType.VTF, s,
                                                token[0]);
                                        sublist.add(vfile);
                                    }
                                }
                            }
                        }
                    }
                }

                return sublist;
            }
        }
    }

    private static String[] tokenize(String line) {
        ArrayList<String> token = new ArrayList<>();
        Matcher match = Pattern.compile("\\S+").matcher(line);

        while (match.find()) {
            token.add(antiquine(match.group()));
        }

        return (String[]) token.toArray(new String[0]);
    }

    private static String antiquine(String in) {
        String[] tok = in.split("\"");
        return tok.length < 2 ? in : tok[1];
    }

    private int compactList() {
        for (int i = 0; i < this.files.size(); ++i) {
            Scanfile ifile = (Scanfile) this.files.get(i);
            if (!ifile.mark) {
                for (int j = i + 1; j < this.files.size(); ++j) {
                    Scanfile jfile = (Scanfile) this.files.get(j);
                    if (ifile.name.equals(jfile.name) && ifile.type == jfile.type) {
                        jfile.mark = true;
                    }
                }
            }
        }

        int count = 0;
        Iterator<Scanfile> fit = this.files.iterator();

        while (fit.hasNext()) {
            if (((Scanfile) fit.next()).mark) {
                fit.remove();
                ++count;
            }
        }

        return count;
    }

    private static String readline(ByteBuffer b) {
        StringBuilder linebuff = new StringBuilder();

        try {
            while (true) {
                char c = (char) b.get();
                if (c == '\n' || c == '\r') {
                    return linebuff.toString();
                }

                linebuff.append(c);
            }
        } catch (BufferUnderflowException var5) {
            return linebuff.toString();
        }
    }

    private ByteBuffer getFileBuffer(Scanfile s) throws IOException {
        ByteBuffer buff;
        long bufflen;
        if (s.inlist) {
            int off = this.tmod.getoffset();
            if (s.inpak) {
                this.raf.seek((long) (off + s.zip.getDataOffset()));
                byte[] buffer = new byte[s.zip.getSize()];
                this.raf.read(buffer);
                buff = ByteBuffer.wrap(buffer);
            } else {
                buff = ByteBuffer.wrap(s.zip.getData());
            }

            bufflen = (long) s.zip.getSize();
        } else {
            File diskfile = new File(s.diskname);
            if (!diskfile.exists() || !diskfile.canRead()) {
                Cons.println("Couldn't read " + diskfile + " from disk");
                return null;
            }

            RandomAccessFile sraf = new RandomAccessFile(diskfile, "r");
            bufflen = diskfile.length();
            byte[] buffer = new byte[(int) bufflen];
            sraf.read(buffer);
            buff = ByteBuffer.wrap(buffer);
            sraf.close();
        }

        s.length = bufflen;
        return buff;
    }

    public boolean isNofiles() {
        return this.nofiles;
    }
}
