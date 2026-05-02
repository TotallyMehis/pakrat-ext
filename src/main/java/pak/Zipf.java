package pak;

import java.io.File;

public class Zipf {
   int size;
   int relofs;
   int datofs;
   String fullname;
   String filename;
   String pathname;
   int type;
   boolean inpak;
   byte[] data;
   long CRC;
   static final int OTHER = 0;
   static final int MATERIAL = 1;
   static final int TEXTURE = 2;
   static final int MODEL = 3;
   static final int MODEL_DAT = 4;
   static final int TEXT = 5;
   static final int SOUND = 6;
   static String[] tstr = new String[]{"Other", "Material", "Texture", "Model", "Model", "Text", "Sound"};

   public Zipf() {
   }

   @Override
   public String toString() {
      return this.fullname;
   }

   public String getDetails() {
      return this.filename + "   (" + this.getTypeStr() + ",  " + this.size + " bytes)";
   }

   public String getFullDetails() {
      return this.fullname + "   (" + this.getTypeStr() + ",  " + this.size + " bytes)";
   }

   public String getTypeStr() {
      return tstr[this.type];
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

   }

   public void setcfull(String fn) {
      fn = fn.replace(File.separatorChar, '/');
      this.setfull(fn);
   }

   public String getrelfull(String rootdir) {
      String full = this.fullname;
      if (!rootdir.equals("") && full.startsWith(rootdir)) {
         int index = rootdir.length() + 1;
         if (index < full.length()) {
            return full.substring(index);
         }
      }

      int index = full.indexOf("/materials");
      if (index != -1) {
         return full.substring(index + 1);
      } else {
         index = full.indexOf("/models");
         if (index != -1) {
            return full.substring(index + 1);
         } else {
            index = full.indexOf("/sound");
            return index != -1 ? full.substring(index + 1) : null;
         }
      }
   }

   public void setfile(String fn) {
      this.setfull(this.pathname + "/" + fn);
   }

   public void setpath(String pn) {
      this.setfull(pn + "/" + this.filename);
   }

   public void settype() {
      this.type = 0;
      if (this.filename.toLowerCase().endsWith(".vmt")) {
         this.type = 1;
      }

      if (this.filename.toLowerCase().endsWith(".vtf")) {
         this.type = 2;
      }

      if (this.filename.toLowerCase().endsWith(".mdl")) {
         this.type = 3;
      }

      if (this.filename.toLowerCase().endsWith(".phy")) {
         this.type = 4;
      }

      if (this.filename.toLowerCase().endsWith(".ani")) {
         this.type = 4;
      }

      if (this.filename.toLowerCase().endsWith(".vtx")) {
         this.type = 4;
      }

      if (this.filename.toLowerCase().endsWith(".vvd")) {
         this.type = 4;
      }

      if (this.filename.toLowerCase().endsWith(".txt")) {
         this.type = 5;
      }

      if (this.filename.toLowerCase().endsWith(".wav")) {
         this.type = 6;
      }

      if (this.filename.toLowerCase().endsWith(".mp3")) {
         this.type = 6;
      }

   }
}
