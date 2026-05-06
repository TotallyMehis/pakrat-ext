package pak;

import java.io.File;

public class Zipf {
    int size;
    int relofs;
    int datofs;
    private String fullname;
    private String filename;
    private String pathname;
    private FileType type;
    boolean inpak;
    byte[] data;
    long CRC;

    public Zipf() {
    }

    @Override
    public String toString() {
        return this.fullname;
    }

    public String getDetails() {
        return this.filename + "   (" + this.getType().getName() + ",  " + this.size + " bytes)";
    }

    public String getFullDetails() {
        return this.fullname + "   (" + this.getType().getName() + ",  " + this.size + " bytes)";
    }

    public void setfull(String fn) {
        if (fn.startsWith("/")) {
            this.fullname = fn.substring(1, fn.length());
        } else {
            this.fullname = fn;
        }

        int is = this.fullname.lastIndexOf("/");
        if (is >= 0 && is < this.fullname.length() - 1) {
            this.filename = this.fullname.substring(is + 1);
            this.pathname = this.fullname.substring(0, is);
        } else {
            this.filename = this.fullname;
            this.pathname = "";
        }

        this.type = FileType.from(this.filename);
    }

    public void setcfull(String fn) {
        fn = fn.replace(File.separatorChar, '/');
        this.setfull(fn);
    }

    public String getrelfull(String rootdir) {
        return Util.getRelativePath(this.fullname, rootdir);
    }

    public void setfile(String fn) {
        this.setfull(this.pathname + "/" + fn);
    }

    public void setpath(String pn) {
        this.setfull(pn + "/" + this.filename);
    }

    public FileType getType() {
        return this.type;
    }

    public String getFullname() {
        return this.fullname;
    }

    public String getPathname() {
        return this.pathname;
    }

    public String getFilename() {
        return this.filename;
    }
}
