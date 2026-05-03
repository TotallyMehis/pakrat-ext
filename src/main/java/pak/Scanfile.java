package pak;

import java.io.File;

public class Scanfile {
   String name;
   String fullname;
   String diskname;
   byte type;
   boolean inlist = false;
   boolean inpak = false;
   boolean ondisk = false;
   Zipf zip;
   long length;
   String listname;
   String pathname;
   boolean mark = false;
   Scanfile parent;
   String referent;
   static final String[] ext = new String[] { "null", ".vmt", ".vtf", ".mdl", ".vtx", ".phy", ".vvd", ".wav", ".mp3",
         ".nav", ".ain", ".txt", ".cache" };
   static final byte NUL = 0;
   static final byte VMT = 1;
   static final byte VTF = 2;
   static final byte MDL = 3;
   static final byte VTX = 4;
   static final byte PHY = 5;
   static final byte VVD = 6;
   static final byte WAV = 7;
   static final byte MP3 = 8;
   static final byte NAV = 9;
   static final byte AIN = 10;
   static final byte TXT = 11;
   static final byte CACHE = 12;
   static final Scanfile ENTITY = new Scanfile();
   static final Scanfile STATIC = new Scanfile();
   static final Scanfile DETAIL = new Scanfile();
   static final Scanfile TEXTURE = new Scanfile();
   static final Scanfile OTHER = new Scanfile();

   public Scanfile() {
   }

   public Scanfile(String name, ZipDirModel tm, String basedir, byte type, Scanfile parent, String referent) {
      this.parent = parent;
      this.referent = referent;
      name = name.replace(File.separatorChar, '/');
      this.name = this.trimprefext(name, type);
      this.type = type;
      this.buildname(basedir);
      this.zip = tm.getbyname(this.fullname);
      if (this.zip != null) {
         this.inlist = true;
         if (this.zip.inpak) {
            this.inpak = true;
         }
      }

      if ((new File(this.diskname)).exists()) {
         this.ondisk = true;
      }

   }

   public void buildname(String basedir) {
      String base = basedir + (basedir.endsWith("/") ? "" : "/");
      this.fullname = this.getprefix(this.type) + this.name + this.getext(this.type);
      this.diskname = base + this.fullname;
      int is = this.fullname.lastIndexOf("/");
      if (is >= 0 && is < this.fullname.length() - 1) {
         this.listname = this.fullname.substring(is + 1);
         this.pathname = this.fullname.substring(0, is);
      } else {
         this.listname = this.fullname;
         this.pathname = "";
      }

   }

   public String getprefix(byte type) {
      switch (type) {
         case 0:
         case 9:
         case 10:
         case 11:
         case 12:
         default:
            return "";
         case 1:
         case 2:
            return "materials/";
         case 3:
         case 4:
         case 5:
         case 6:
            return "models/";
         case 7:
         case 8:
            return "sound/";
      }
   }

   public String getext(byte type) {
      return type >= 0 && type < ext.length ? ext[type] : null;
   }

   public static byte gettype(String name) {
      name = name.toLowerCase();

      for (byte i = 1; i < ext.length; ++i) {
         if (name.endsWith(ext[i])) {
            return i;
         }
      }

      return -1;
   }

   public boolean onlydisk() {
      return this.ondisk && !this.inpak & !this.inlist;
   }

   public String trimprefext(String name, byte t) {
      String ret = name;
      int i = name.toLowerCase().lastIndexOf(this.getext(t));
      if (i > 0) {
         ret = name.substring(0, i);
      }

      if (ret.toLowerCase().startsWith(this.getprefix(t))) {
         ret = ret.substring(this.getprefix(t).length());
      }

      return ret;
   }

   @Override
   public String toString() {
      return this.name + " (" + this.getext(this.type) + ") : " + (this.inlist ? "L" : " ") + (this.inpak ? "P" : " ")
            + (this.ondisk ? "D" : " ");
   }
}
