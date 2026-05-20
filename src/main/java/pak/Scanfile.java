package pak;

import java.io.File;
import java.util.Objects;

public class Scanfile {
    final String name;
    final String fullname;
    final String diskname;
    final ScanfileType type;
    boolean inlist = false;
    boolean inpak = false;
    boolean ondisk = false;
    final Zipf zip;
    long length;
    final String listname;
    final String pathname;
    boolean mark = false;
    final Scanfile parent;
    final String referent;

    public static final Scanfile ENTITY = new Scanfile();
    public static final Scanfile STATIC = new Scanfile();
    public static final Scanfile DETAIL = new Scanfile();
    public static final Scanfile TEXTURE = new Scanfile();
    public static final Scanfile OTHER = new Scanfile();

    private Scanfile() {
        this.name = "";
        this.fullname = "";
        this.diskname = "";
        this.type = ScanfileType.NUL;
        this.zip = null;
        this.listname = "";
        this.pathname = "";
        this.parent = null;
        this.referent = "";
    }

    public Scanfile(String name, ZipDirModel tm, String basedir, ScanfileType type, Scanfile parent, String referent) {
        this.parent = Objects.requireNonNull(parent);
        this.referent = referent;
        name = name.replace(File.separatorChar, '/');
        this.name = trimName(name, type);
        this.type = type;

        String base = basedir + (basedir.endsWith("/") ? "" : "/");
        this.fullname = getPathPrefix(this.type) + this.name + this.type.getExtension();
        this.diskname = base + this.fullname;
        int is = this.fullname.lastIndexOf("/");
        if (is >= 0 && is < this.fullname.length() - 1) {
            this.listname = this.fullname.substring(is + 1);
            this.pathname = this.fullname.substring(0, is);
        } else {
            this.listname = this.fullname;
            this.pathname = "";
        }

        this.zip = tm.getbyname(this.fullname);
        if (this.zip != null) {
            this.inlist = true;
            if (this.zip.isInPak()) {
                this.inpak = true;
            }
        }

        if ((new File(this.diskname)).exists()) {
            this.ondisk = true;
        }

    }

    public static String getPathPrefix(ScanfileType type) {
        return switch (type) {
            case NUL, NAV, AIN, TXT, CACHE -> "";
            case VMT, VTF -> "materials/";
            case MDL, VTX, PHY, VVD -> "models/";
            case WAV, MP3 -> "sound/";
        };
    }

    public boolean onlydisk() {
        return this.ondisk && !this.inpak & !this.inlist;
    }

    private static String trimName(String name, ScanfileType t) {
        String ret = name;
        int i = name.toLowerCase().lastIndexOf(t.getExtension());
        if (i > 0) {
            ret = name.substring(0, i);
        }

        if (ret.toLowerCase().startsWith(getPathPrefix(t))) {
            ret = ret.substring(getPathPrefix(t).length());
        }

        return ret;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.type.getExtension() + ") : " + (this.inlist ? "L" : " ")
                + (this.inpak ? "P" : " ")
                + (this.ondisk ? "D" : " ");
    }
}
