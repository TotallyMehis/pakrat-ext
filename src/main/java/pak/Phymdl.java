package pak;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
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

   public void load(String filename) throws IOException {
      File infile = new File(filename);
      if (infile.exists() && infile.canRead()) {
         System.out.println("Reading " + filename);
         try (RandomAccessFile raf = new RandomAccessFile(infile, "r")) {
            FileChannel rafch = raf.getChannel();
            MappedByteBuffer mbuffer = rafch.map(MapMode.READ_ONLY, 0L, rafch.size());
            ByteBuffer b = mbuffer.order(ByteOrder.LITTLE_ENDIAN);
            this.read(b);
         }
      } else {
         System.out.println("Can't open " + filename);
      }
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
