package pak;

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
    Mappak m;
    ZipDirModel tmod;
    JFrame sframe;
    JTable stable;
    ScanModel smodel;
    RandomAccessFile raf;
    Unpak pakrat;
    JProgFrame prog;
    ArrayList<Scanfile> files;
    boolean auton;
    boolean nofiles;
    static final String[] mattexref = new String[] { "$basetexture", "$basetexture2", "$bumpmap", "$bumpmap2",
            "$envmap",
            "$normalmap", "$dudvmap", "$envmapmask", "$detail", "$parallaxmap", "$parallaxmap2", "$texture2",
            "$selfillumtexture", "$cloudalphatexture", "$gradienttexture", "$fbtexture", "$refracttexture",
            "$refracttinttexture", "$hdrbasetexture" };
    static final String[] matmatref = new String[] { "include", "$fallbackmaterial", "$bottommaterial",
            "$crackmaterial",
            "$material", "$modelmaterial", "$translucent_material" };
    static final String[] mdlext = new String[] { ".dx80.vtx", ".dx90.vtx", ".sw.vtx", ".phy", ".vvd" };
    String basedir;
    ArrayList<String> dirset;

    public Scan(Unpak pakrat, Component parent, Mappak m, ZipDirModel tmod, String fname, String gamedir) {
        this(pakrat, parent, m, tmod, fname, gamedir, false);
    }

    public Scan(Unpak pakrat, Component parent, Mappak m, ZipDirModel tmod, String fname, String gamedir,
            boolean autoadd) {
        this.prog = null;
        this.auton = false;
        this.nofiles = false;
        this.dirset = new ArrayList<>();
        this.auton = parent == null;
        this.pakrat = pakrat;
        this.m = m;
        this.tmod = tmod;
        this.raf = tmod.getbuff();
        this.files = new ArrayList<>();
        if (autoadd) {
            this.basedir = gamedir;
            this.doautoscan(parent);
        } else {
            this.sframe = new JFrame("Pakrat - Scan Files - " + fname);
            this.sframe.setLocationRelativeTo(parent);
            JPanel spanel = new JPanel(new BorderLayout());
            this.smodel = new ScanModel(this.files);
            this.stable = new JTable(this.smodel);
            Container topbox = Box.createHorizontalBox();
            topbox.add(new JLabel("Gamedir: "));
            final JComboBox<String> filebox = new JComboBox<>(this.getbasedirs(gamedir));
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
                Scan.this.addbasedir(Scan.this.basedir);
                Scan.this.sframe.setCursor(Cursor.getPredefinedCursor(3));
                SwingWorker worker = new SwingWorker() {
                    @Override
                    public Object construct() {
                        Scan.this.doscan();
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
                Scan.this.addselected();
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
                Scan.this.docheck(Scan.this.smodel.getfile(Scan.this.stable.getSelectedRow()));
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

    public void docheck(Scanfile s) {
        StringBuilder t = new StringBuilder("File: " + s.fullname + "\n");
        t.append("Type: " + ScanModel.tstr[s.type] + "\n");
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
        this.checkparent(s, t);
        JTextArea textarea = new JTextArea(t.toString());
        textarea.setEditable(false);
        JFrame tframe = new JFrame("Pakrat - Check File Reference - " + s.listname);
        tframe.setLocationRelativeTo(this.sframe);
        tframe.setSize(550, 250);
        tframe.getContentPane().add(new JScrollPane(textarea));
        tframe.setDefaultCloseOperation(2);
        tframe.setVisible(true);
    }

    public void checkparent(Scanfile s, StringBuilder t) {
        if (s != null) {
            if (s.parent != null) {
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
                    this.checkparent(s.parent, t);
                }
            }
        }
    }

    public String[] getbasedirs(String gamedir) {
        this.dirset.clear();

        for (int i = 0; i < 6; ++i) {
            String dir = Pakpref.get("Basedir" + i, (String) null);
            if (dir != null) {
                this.dirset.add(dir);
            }
        }

        this.dirset.add(gamedir);
        String mapbase = this.pakrat.infile.getPath().replace(File.separatorChar, '/');
        int im = mapbase.toLowerCase().lastIndexOf("maps/");
        if (im > 0) {
            mapbase = mapbase.substring(0, im - 1);
            this.dirset.add(mapbase);
        }

        this.compactbasedirs();
        return (String[]) this.dirset.toArray(new String[0]);
    }

    public void addbasedir(String dir) {
        this.dirset.add(0, dir);
        this.compactbasedirs();
        String[] dirs = (String[]) this.dirset.toArray(new String[0]);

        for (int i = 0; i < dirs.length && i != 6; ++i) {
            Pakpref.put("Basedir" + i, dirs[i]);
        }

    }

    public void compactbasedirs() {
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

    public void addselected() {
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

    public void doautoscan(final Component parent) {
        if (this.basedir.equals("")) {
            String mapbase = this.pakrat.infile.getPath().replace(File.separatorChar, '/');
            int im = mapbase.toLowerCase().lastIndexOf("maps/");
            if (im > 0) {
                mapbase = mapbase.substring(0, im - 1);
                this.basedir = mapbase;
            }
        }

        if (this.auton) {
            this.doscan();
            if (this.autoaddfiles((Component) null) == 0) {
                this.nofiles = true;
            }
        } else {
            parent.setCursor(Cursor.getPredefinedCursor(3));
            SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    Scan.this.doscan();
                    return null;
                }

                public void finished() {
                    parent.setCursor(Cursor.getDefaultCursor());
                    Scan.this.autoaddfiles(parent);
                }
            };
            worker.start();
        }

    }

    public int autoaddfiles(Component parent) {
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

    public void doscan() {
        this.files.clear();

        try {
            if (!this.auton) {
                this.prog = new JProgFrame(this.sframe, "Pakrat - Scanning for files...");
                this.prog.start("Textures...", true);
            }

            Cons.print("Scanning textures...");
            this.scantextures();
            if (!this.auton) {
                this.prog.setString("Prop_statics...");
                this.prog.setValue(0);
            }

            Cons.print("prop_statics...");
            this.scanstatics();
            if (!this.auton) {
                this.prog.setString("Prop_details...");
                this.prog.setValue(0);
            }

            Cons.print("prop_details...");
            this.scandetails();
            if (!this.auton) {
                this.prog.setString("Loading entities...");
                this.prog.setValue(0);
            }

            Cons.print("entities...");
            this.scanentities();
            if (!this.auton) {
                this.prog.setString("Extra files...");
                this.prog.setValue(0);
            }

            Cons.print("extras...");
            this.scanextras();
            if (!this.auton) {
                this.prog.end();
            }

            Cons.println(this.compactlist() + " duplicates removed");
            if (!this.auton) {
                int pakfiles = this.tmod.getRowCount();
                boolean[] refd = new boolean[pakfiles];
                String mapname = this.pakrat.infile.getName().toLowerCase();
                String cubemappath = "materials/maps/" + mapname.substring(0, mapname.lastIndexOf(".bsp"));

                for (int i = 0; i < pakfiles; ++i) {
                    refd[i] = this.tmod.getzipfile(i).getPathname().startsWith(cubemappath);
                }

                for (int i = 0; i < this.files.size(); ++i) {
                    Scanfile s = (Scanfile) this.files.get(i);
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

    public void scanextras() throws IOException {
        if (!this.auton) {
            this.prog.setMaximum(7);
        }

        String mapname = this.pakrat.infile.getName().toLowerCase();
        mapname = mapname.substring(0, mapname.lastIndexOf(".bsp"));
        if (!this.auton) {
            if (Pakpref.navfile) {
                Scanfile sfile = new Scanfile("maps/" + mapname + ".nav", this.tmod, this.basedir, (byte) 9,
                        Scanfile.OTHER,
                        "");
                this.prog.setValue(1);
                this.files.add(sfile);
            }

            if (Pakpref.ainfile) {
                Scanfile sfile = new Scanfile("maps/graphs/" + mapname + ".ain", this.tmod, this.basedir, (byte) 10,
                        Scanfile.OTHER, "");
                this.prog.setValue(2);
                this.files.add(sfile);
            }

            if (Pakpref.soundcache) {
                Scanfile sfile = new Scanfile("maps/soundcache/" + mapname + ".cache", this.tmod, this.basedir,
                        (byte) 12,
                        Scanfile.OTHER, "");
                this.prog.setValue(3);
                this.files.add(sfile);
            }
        }

        if (Pakpref.description) {
            Scanfile sfile = new Scanfile("maps/" + mapname + ".txt", this.tmod, this.basedir, (byte) 11,
                    Scanfile.OTHER,
                    "");
            if (!this.auton) {
                this.prog.setValue(4);
            }

            this.files.add(sfile);
        }

        if (Pakpref.overview) {
            Scanfile sfile = new Scanfile("resource/overviews/" + mapname + ".txt", this.tmod, this.basedir, (byte) 11,
                    Scanfile.OTHER, "");
            if (!this.auton) {
                this.prog.setValue(5);
            }

            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

        if (Pakpref.soundscape) {
            Scanfile sfile = new Scanfile("scripts/soundscapes_" + mapname + ".txt", this.tmod, this.basedir, (byte) 11,
                    Scanfile.OTHER, "");
            if (!this.auton) {
                this.prog.setValue(6);
            }

            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

        if (!this.auton) {
            this.prog.setValue(7);
        }

    }

    public void scanentities() throws IOException {
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
            Scanfile sfile = new Scanfile(name, this.tmod, this.basedir, Scanfile.gettype(name), Scanfile.ENTITY, ref);
            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

    }

    public void scanstatics() throws IOException {
        String[] staticNames = this.m.getStaticname();
        if (!this.auton) {
            this.prog.setMaximum(staticNames.length);
        }

        for (int i = 0; i < staticNames.length; ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(staticNames[i], this.tmod, this.basedir, (byte) 3, Scanfile.STATIC, "");
            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

    }

    public void scandetails() throws IOException {
        String[] detailNames = this.m.getDetailname();
        if (!this.auton) {
            this.prog.setMaximum(detailNames.length);
        }

        for (int i = 0; i < detailNames.length; ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(detailNames[i], this.tmod, this.basedir, (byte) 3, Scanfile.DETAIL, "");
            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

    }

    public void scantextures() throws IOException {
        String[] textures = this.m.getTexname();
        if (!this.auton) {
            this.prog.setMaximum(textures.length);
        }

        for (int i = 0; i < textures.length; ++i) {
            if (!this.auton) {
                this.prog.setValue(i);
            }

            Scanfile sfile = new Scanfile(textures[i], this.tmod, this.basedir, (byte) 1, Scanfile.TEXTURE, "");
            this.files.add(sfile);
            this.files.addAll(this.checksubfile(sfile));
        }

    }

    public ArrayList<Scanfile> checksubfile(Scanfile sf) throws IOException {
        ArrayList<Scanfile> subfiles = new ArrayList<>();
        if (sf.inlist || sf.ondisk) {
            switch (sf.type) {
                case 1:
                    subfiles = this.getreffromvmt(sf);
                case 2:
                case 4:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                default:
                    break;
                case 3:
                    subfiles = this.getreffrommdl(sf);
                    break;
                case 5:
                    subfiles = this.getreffromphy(sf);
                    break;
                case 11:
                    if (sf.fullname.startsWith("resource/overviews/")
                            || sf.fullname.startsWith("scripts/soundscapes_")) {
                        subfiles = this.getreffromtxt(sf);
                    }
            }
        }

        return subfiles;
    }

    public ArrayList<Scanfile> getreffromphy(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getfilebuffer(s);
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
                        Scanfile sfile = new Scanfile((String) phy.gibmodel.get(j), this.tmod, this.basedir, (byte) 3,
                                s,
                                "gib model");
                        sublist.add(sfile);
                        sublist.addAll(this.checksubfile(sfile));
                    }

                    return sublist;
                }
            }
        }
    }

    public ArrayList<Scanfile> getreffrommdl(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        String basename = s.name;

        for (int i = 0; i < mdlext.length; ++i) {
            String name = basename + mdlext[i];
            Scanfile sfile = new Scanfile(name, this.tmod, this.basedir, Scanfile.gettype(name), s, "datafile");
            sublist.add(sfile);
            sublist.addAll(this.checksubfile(sfile));
        }

        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getfilebuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                buff.order(ByteOrder.LITTLE_ENDIAN);
                Mdl model = new Mdl();
                model.read(buff);
                if (!model.isValid) {
                    Cons.println("Failed to read model");
                    return sublist;
                } else {
                    ArrayList<String> texturelist = model.gettexturelist();

                    for (int j = 0; j < texturelist.size(); ++j) {
                        Scanfile sfile = new Scanfile((String) texturelist.get(j), this.tmod, this.basedir, (byte) 1, s,
                                "texture");
                        sublist.add(sfile);
                        sublist.addAll(this.checksubfile(sfile));
                    }

                    for (int j = 0; j < model.numincmodels; ++j) {
                        Scanfile sfile = new Scanfile(model.incmodelfile[j], this.tmod, this.basedir, (byte) 3, s,
                                "include model");
                        sublist.add(sfile);
                        sublist.addAll(this.checksubfile(sfile));
                    }

                    return sublist;
                }
            }
        }
    }

    public ArrayList<Scanfile> getreffromtxt(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getfilebuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                while ((long) buff.position() < s.length) {
                    String line = this.readline(buff).trim();
                    if (line.length() != 0) {
                        int ic = line.indexOf("//");
                        if (ic > -1) {
                            line = line.substring(0, ic);
                        }

                        String[] token = this.tokenize(line);
                        if (token.length == 2) {
                            if (token[0].equalsIgnoreCase("material")) {
                                Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, (byte) 1, s,
                                        "material");
                                sublist.add(ssfile);
                                ArrayList<Scanfile> subsublist = this.getreffromvmt(ssfile);

                                for (int j = 0; j < subsublist.size(); ++j) {
                                    sublist.add(subsublist.get(j));
                                }
                            } else if (token[0].equalsIgnoreCase("wave")) {
                                Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, (byte) 7, s, "wave");
                                sublist.add(ssfile);
                            }
                        }
                    }
                }

                return sublist;
            }
        }
    }

    public ArrayList<Scanfile> getreffromvmt(Scanfile s) throws IOException {
        ArrayList<Scanfile> sublist = new ArrayList<>();
        if (!s.inlist && !s.ondisk) {
            return sublist;
        } else {
            ByteBuffer buff = this.getfilebuffer(s);
            if (buff == null) {
                return sublist;
            } else {
                while ((long) buff.position() < s.length) {
                    String line = this.readline(buff).trim();
                    if (line.length() != 0) {
                        int ic = line.indexOf("//");
                        if (ic > -1) {
                            line = line.substring(0, ic);
                        }

                        String[] token = this.tokenize(line);
                        if (token.length == 2 && !token[1].startsWith("_")) {
                            for (int i = 0; i < matmatref.length; ++i) {
                                if (token[0].equalsIgnoreCase(matmatref[i])) {
                                    Scanfile ssfile = new Scanfile(token[1], this.tmod, this.basedir, (byte) 1, s,
                                            token[0]);
                                    sublist.add(ssfile);
                                    ArrayList<Scanfile> subsublist = this.getreffromvmt(ssfile);

                                    for (int j = 0; j < subsublist.size(); ++j) {
                                        sublist.add(subsublist.get(j));
                                    }
                                }
                            }

                            for (int i = 0; i < mattexref.length; ++i) {
                                if (token[0].equalsIgnoreCase(mattexref[i])) {
                                    if (mattexref[i].equals("$envmap")) {
                                        if (!token[1].equalsIgnoreCase("env_cubemap")) {
                                            Scanfile vfile = new Scanfile(token[1], this.tmod, this.basedir, (byte) 2,
                                                    s,
                                                    token[0]);
                                            sublist.add(vfile);
                                        }
                                    } else {
                                        Scanfile vfile = new Scanfile(token[1], this.tmod, this.basedir, (byte) 2, s,
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

    public String[] tokenize(String line) {
        ArrayList<String> token = new ArrayList<>();
        Matcher match = Pattern.compile("\\S+").matcher(line);

        while (match.find()) {
            token.add(this.antiquine(match.group()));
        }

        return (String[]) token.toArray(new String[0]);
    }

    public String antiquine(String in) {
        String[] tok = in.split("\"");
        return tok.length < 2 ? in : tok[1];
    }

    public int compactlist() {
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

    public String readline(ByteBuffer b) {
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

    public String readstr(ByteBuffer b) {
        StringBuilder linebuff = new StringBuilder();

        while (true) {
            char c = (char) b.get();
            if (c == 0) {
                return linebuff.toString();
            }

            linebuff.append(c);
        }
    }

    public ByteBuffer getfilebuffer(Scanfile s) throws IOException {
        ByteBuffer buff;
        long bufflen;
        if (s.inlist) {
            int off = this.tmod.getoffset();
            if (s.inpak) {
                this.raf.seek((long) (off + s.zip.datofs));
                byte[] buffer = new byte[s.zip.size];
                this.raf.read(buffer);
                buff = ByteBuffer.wrap(buffer);
            } else {
                buff = ByteBuffer.wrap(s.zip.data);
            }

            bufflen = (long) s.zip.size;
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
}
