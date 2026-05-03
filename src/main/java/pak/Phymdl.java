package pak;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Phymdl {
   boolean isValid = false;
   int version;
   int id;
   int numsolids;
   int checksum;
   String physblock;
   ArrayList<String> gibmodel;

   public Phymdl() {
   }

   public void read(ByteBuffer b) throws IOException {
      this.isValid = false;
      this.version = b.getInt();
      this.id = b.getInt();
      if (this.version == 16 && this.id == 0) {
         this.numsolids = b.getInt();
         this.checksum = b.getInt();

         for(int i = 0; i < this.numsolids; ++i) {
            int ssize = b.getInt();
            b.position(b.position() + ssize);
         }

         this.physblock = this.readstr(b);
         String[] physdata = this.physblock.split("\n");
         this.gibmodel = new ArrayList<>();

         for(int i = 0; i < physdata.length; ++i) {
            if (physdata[i].startsWith("break ")) {
            }

            String line = physdata[i];
            int mindex = line.indexOf("\"model\"");
            if (mindex >= 0) {
               line = line.substring(mindex + 7).trim();
               String[] token = line.split("\"");
               this.gibmodel.add(token[1]);
            }
         }

         this.isValid = true;
      }
   }

   public String readstr(ByteBuffer b) {
      StringBuffer linebuff = new StringBuffer();

      while(true) {
         char c = (char)b.get();
         if (c == 0) {
            return linebuff.toString();
         }

         linebuff.append(c);
      }
   }
}
