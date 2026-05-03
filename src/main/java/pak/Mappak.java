package pak;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Mappak {
   int ident;
   int version;
   Lump[] lump;
   int maprev;
   int offset;
   int length;
   long filelength;
   int cdoffs;
   ArrayList<Zipf> zf;
   String tdsd;
   int[] tdst;
   String[] texname;
   String[] staticname;
   String[] detailname;
   int glumps;
   GameLump[] gl;
   ArrayList<String> entkeylist;
   ArrayList<String> entvallist;
   public boolean auton = false;
   static final String[] entext = new String[]{".vmt", ".mdl", ".spr", ".wav", ".mp3", ".txt"};

   public Mappak() {
   }

   public void copyblock(RandomAccessFile in, RandomAccessFile out, long length) throws IOException {
      int size = (int)length;

      int bytesread;
      for(byte[] buffer = new byte[1024]; size > 0; size -= bytesread) {
         if (size > 1024) {
            bytesread = in.read(buffer, 0, 1024);
            out.write(buffer, 0, bytesread);
         } else {
            bytesread = in.read(buffer, 0, size);
            out.write(buffer, 0, bytesread);
         }
      }

   }

   public void savemap(RandomAccessFile bin, RandomAccessFile bout) throws IOException {
      bin.seek(1036L);
      bout.seek(1036L);
      this.copyblock(bin, bout, (long)(this.offset - 1036));
      bin.seek(this.roundupto4((long)(this.offset + this.length)));
      bout.seek(this.roundupto4(bout.getFilePointer()));
      long pakdiff = bout.getFilePointer() - bin.getFilePointer();
      this.copyblock(bin, bout, this.filelength - bin.getFilePointer());
      long outpos = this.roundupto4(bout.getFilePointer());
      bout.seek(0L);
      byte[] buffer = new byte[1036];
      ByteBuffer b = ByteBuffer.wrap(buffer);
      b.order(ByteOrder.LITTLE_ENDIAN);
      b.position(0);
      b.putInt(this.ident);
      b.putInt(this.version);

      for(int i = 0; i < 64; ++i) {
         Lump l = this.lump[i];
         if (l.ofs() > this.lump[40].ofs()) {
            int newOfs = (int)((long)l.ofs() + pakdiff);
            l = this.lump[i] = new Lump(newOfs, l.len(), l.vers(), l.fourCC());
         }

         b.putInt(l.ofs());
         b.putInt(l.len());
         b.putInt(l.vers());
         b.putInt(l.fourCC());
      }

      b.putInt(this.maprev);
      bout.write(buffer);
      bout.seek(outpos);
   }

   public void savepak(RandomAccessFile bin, RandomAccessFile bout) throws IOException {
      int numzips = this.zf.size();
      int newoffset = (int)bout.getFilePointer();

      for(int i = 0; i < numzips; ++i) {
         Zipf z = (Zipf)this.zf.get(i);
         z.relofs = (int)bout.getFilePointer() - newoffset;
         bout.writeInt(Swab.I(67324752));
         bout.writeShort(Swab.S(10));
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeInt(Swab.I((int)z.CRC));
         bout.writeInt(Swab.I(z.size));
         bout.writeInt(Swab.I(z.size));
         bout.writeShort(Swab.S(z.getFullname().length()));
         bout.writeShort(0);
         this.writestr(bout, z.getFullname());
         z.datofs = (int)bout.getFilePointer() - newoffset;
         if (z.inpak) {
            bin.seek((long)(this.offset + z.datofs));
            this.copyblock(bin, bout, (long)z.size);
         } else {
            bout.write(z.data);
            z.data = null;
            z.inpak = true;
         }
      }

      this.cdoffs = (int)bout.getFilePointer() - newoffset;

      for(int i = 0; i < numzips; ++i) {
         Zipf z = (Zipf)this.zf.get(i);
         bout.writeInt(Swab.I(33639248));
         bout.writeShort(Swab.S(20));
         bout.writeShort(Swab.S(10));
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeInt(Swab.I((int)z.CRC));
         bout.writeInt(Swab.I(z.size));
         bout.writeInt(Swab.I(z.size));
         bout.writeShort(Swab.S(z.getFullname().length()));
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeShort(0);
         bout.writeInt(0);
         bout.writeInt(Swab.I(z.relofs));
         this.writestr(bout, z.getFullname());
      }

      int cdend = (int)bout.getFilePointer() - newoffset;
      bout.writeInt(Swab.I(101010256));
      bout.writeShort(0);
      bout.writeShort(0);
      bout.writeShort(Swab.S(numzips));
      bout.writeShort(Swab.S(numzips));
      bout.writeInt(Swab.I(cdend - this.cdoffs));
      bout.writeInt(Swab.I(this.cdoffs));
      bout.writeShort(0);
      int newlen = (int)bout.getFilePointer() - newoffset;
      this.lump[40] = new Lump(newoffset, newlen, this.lump[40].vers(), this.lump[40].fourCC());
      this.offset = this.lump[40].ofs();
      this.length = this.lump[40].len();
      bout.seek(648L);
      bout.writeInt(Swab.I(this.lump[40].ofs()));
      bout.writeInt(Swab.I(this.lump[40].len()));
   }

   public void loadmap(RandomAccessFile b) throws IOException {
      this.loadheader(b);
      this.loadpak(b);
      this.loadtexstring(b);
      this.loadgamelump(b);
      this.loadpropstatics(b);
      this.loadpropdetails(b);
   }

   public void loadgamelump(RandomAccessFile raf) throws IOException {
      raf.seek((long)this.lump[35].ofs());
      this.glumps = Swab.I(raf.readInt());
      this.gl = new GameLump[this.glumps];

      for(int i = 0; i < this.glumps; ++i) {
         int id = Swab.I(raf.readInt());
         short flags = Swab.S(raf.readShort());
         short vers = Swab.S(raf.readShort());
         int ofs = Swab.I(raf.readInt());
         int len = Swab.I(raf.readInt());

         this.gl[i] = new GameLump(id, flags, vers, ofs, len);
      }

   }

   public void loadpropstatics(RandomAccessFile raf) throws IOException {
      int spid = -1;

      for(int i = 0; i < this.glumps; ++i) {
         if (this.gl[i].id() == 1936749168) {
            spid = i;
         }
      }

      if (spid >= 0) {
         raf.seek((long)this.gl[spid].ofs());
         int psnames = Swab.I(raf.readInt());
         this.staticname = new String[psnames];

         for(int i = 0; i < psnames; ++i) {
            this.staticname[i] = this.readntstr(raf, 128);
         }

      }
   }

   public void loadpropdetails(RandomAccessFile raf) throws IOException {
      int dpid = -1;

      for(int i = 0; i < this.glumps; ++i) {
         if (this.gl[i].id() == 1685090928) {
            dpid = i;
         }
      }

      if (dpid >= 0) {
         raf.seek((long)this.gl[dpid].ofs());
         int pdnames = Swab.I(raf.readInt());
         this.detailname = new String[pdnames];

         for(int i = 0; i < pdnames; ++i) {
            this.detailname[i] = this.readntstr(raf, 128);
         }

      }
   }

   public void loadtexstring(RandomAccessFile raf) throws IOException {
      int sofs = this.lump[43].ofs();
      int slen = this.lump[43].len();
      raf.seek((long)sofs);
      this.tdsd = this.readstr(raf, slen);
      int ofs = this.lump[44].ofs();
      int len = this.lump[44].len();
      int numtdst = len / Lump.size(44);
      this.tdst = new int[numtdst];
      this.texname = new String[numtdst];
      raf.seek((long)ofs);

      for(int i = 0; i < numtdst; ++i) {
         int ix = this.tdst[i] = Swab.I(raf.readInt());

         for(int j = ix; j < sofs; ++j) {
            if (this.tdsd.charAt(j) == 0) {
               this.texname[i] = this.tdsd.substring(ix, j);
               break;
            }
         }
      }

   }

   public void loadentities(RandomAccessFile raf, JProgFrame prog) throws IOException {
      this.entkeylist = new ArrayList<>();
      this.entvallist = new ArrayList<>();
      ArrayList<String> keylist = new ArrayList<>();
      ArrayList<String> vallist = new ArrayList<>();
      String classname = "";
      int ofs = this.lump[0].ofs();
      int end = ofs + this.lump[0].len();
      raf.seek((long)ofs);
      if (!this.auton) {
         prog.setMaximum(this.lump[0].len());
      }

      long fp;
      while((fp = raf.getFilePointer()) < (long)end) {
         String line = raf.readLine();
         if (line == null) {
            break;
         }

         if (line.equals("{")) {
            keylist.clear();
            vallist.clear();
            classname = "";
         } else if (line.equals("}")) {
            for(int i = 0; i < keylist.size(); ++i) {
               this.entkeylist.add(classname + " : keyword \"" + (String)keylist.get(i) + "\"");
               this.entvallist.add(vallist.get(i));
            }

            if (!this.auton) {
               prog.setValue((int)fp - ofs);
            }
         } else {
            String[] token = line.split("\"");
            if (token.length > 3) {
               String key = token[1].toLowerCase();
               String val = token[3].toLowerCase();
               boolean done = false;
               if (key.equals("classname")) {
                  classname = val;
               } else {
                  for(int i = 0; i < entext.length; ++i) {
                     if (val.endsWith(entext[i])) {
                        if (entext[i].equals(".spr")) {
                           val = this.stripext(val) + ".vmt";
                        }

                        keylist.add(key);
                        vallist.add(val);
                        done = true;
                     }
                  }

                  if (!done) {
                     if (key.equals("ropematerial")) {
                        keylist.add(key);
                        vallist.add(val + ".vmt");
                     } else if (key.equals("texture")) {
                        keylist.add(key);
                        vallist.add(val + ".vmt");
                     } else if (key.startsWith("point_hud_icon_")) {
                        keylist.add(key);
                        vallist.add(val + ".vmt");
                     } else if (key.equals("skyname")) {
                        keylist.add(key);
                        vallist.add("skybox/" + val + "up.vmt");
                        keylist.add(key);
                        vallist.add("skybox/" + val + "dn.vmt");
                        keylist.add(key);
                        vallist.add("skybox/" + val + "ft.vmt");
                        keylist.add(key);
                        vallist.add("skybox/" + val + "bk.vmt");
                        keylist.add(key);
                        vallist.add("skybox/" + val + "lf.vmt");
                        keylist.add(key);
                        vallist.add("skybox/" + val + "rt.vmt");
                     }
                  }
               }
            }
         }
      }

   }

   public String stripext(String in) {
      String out = in;
      int i = in.lastIndexOf(".");
      if (i > -1) {
         out = in.substring(0, i);
      }

      return out;
   }

   public void loadheader(RandomAccessFile bb) throws IOException {
      this.filelength = bb.length();
      this.ident = Swab.I(bb.readInt());
      this.version = Swab.I(bb.readInt());
      int vbsp = 1347633750;
      if (this.ident != vbsp) {
         Cons.println("Not a map file: " + vbsp + " != " + this.ident);
      } else {
         if (!this.auton) {
            Cons.println("Ident: " + this.ident);
            Cons.println("Version: " + this.version);
         }

         this.lump = new Lump[64];

         for(int i = 0; i < 64; ++i) {
            int ofs = Swab.I(bb.readInt());
            int len = Swab.I(bb.readInt());
            int vers = Swab.I(bb.readInt());
            int fourCC = Swab.I(bb.readInt());

            Lump lump = new Lump(ofs, len, vers, fourCC);


            if (lump.len() > 0 && !this.auton) {
               Cons.print("Lump " + i + ": ");
               Cons.print(lump.ofs() + ", " + lump.len() + ", " + lump.vers() + ", " + lump.fourCC());
               Cons.println(" " + Lump.name(i) + "  " + lump.len() / Lump.size(i) + (Lump.size(i) == 1 ? " bytes" : ""));
            }

            this.lump[i] = lump;
         }

         this.maprev = Swab.I(bb.readInt());
         if (!this.auton) {
            Cons.println("MapRev: " + this.maprev + "\n----------------\n");
         }

         int last = 0;

         for(int i = 0; i < 64; ++i) {
            if (this.lump[i].ofs() > this.lump[last].ofs()) {
               last = i;
            }
         }

         this.offset = this.lump[40].ofs();
         this.length = this.lump[40].len();
         if (this.length == 0) {
            Cons.println("Map contains no pakfile!");
         } else {
            Cons.println("Pak file at offset " + this.offset + ", length " + this.length + " bytes");
         }
      }
   }

   public void loadpak(RandomAccessFile bb) throws IOException {
      boolean cdirf = false;

      for(int off = this.offset + this.length - 22; off >= 0; --off) {
         bb.seek((long)off);
         int csig = Swab.I(bb.readInt());
         if (csig == 101010256) {
            cdirf = true;
            break;
         }
      }

      if (!cdirf) {
         Cons.println("Couldn't find pak file EOCD");
      } else {
         bb.readShort();
         bb.readShort();
         bb.readShort();
         short cdes = Swab.S(bb.readShort());
         Swab.I(bb.readInt());
         int cdoffs = Swab.I(bb.readInt());
         bb.readShort();
         Cons.println("Pak lump entries: " + cdes);
         if (cdes > 0) {
            bb.seek((long)(this.offset + cdoffs));
            int[] zfcs = new int[cdes];
            int[] zfus = new int[cdes];
            int[] zfro = new int[cdes];
            int[] zfdo = new int[cdes];
            int[] zcrc = new int[cdes];
            String[] zffn = new String[cdes];

            for(int i = 0; i < cdes; ++i) {
               int hsig = Swab.I(bb.readInt());
               if (hsig != 33639248) {
                  Cons.println("ZipFH signature incorrect");
                  return;
               }

               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readShort();
               zcrc[i] = Swab.I(bb.readInt());
               int csize = zfcs[i] = Swab.I(bb.readInt());
               int usize = zfus[i] = Swab.I(bb.readInt());
               short fnlen = Swab.S(bb.readShort());
               short exlen = Swab.S(bb.readShort());
               short fclen = Swab.S(bb.readShort());
               bb.readShort();
               bb.readShort();
               bb.readInt();
               zfro[i] = Swab.I(bb.readInt());
               zffn[i] = this.readstr(bb, fnlen);
               this.readstr(bb, exlen + fclen);
               if (csize != usize) {
                  Cons.println("Zip file " + i + " is compressed! " + csize + "!=" + usize);
                  return;
               }
            }

            for(int i = 0; i < cdes; ++i) {
               bb.seek((long)(this.offset + zfro[i]));
               int lsig = Swab.I(bb.readInt());
               if (lsig != 67324752) {
                  Cons.println("ZipLFH signature incorrect");
                  return;
               }

               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readShort();
               bb.readInt();
               bb.readInt();
               bb.readInt();
               short fnlen = Swab.S(bb.readShort());
               short exlen = Swab.S(bb.readShort());
               zfdo[i] = zfro[i] + 30 + fnlen + exlen;
            }

            this.zf = new ArrayList<>();

            for(int i = 0; i < cdes; ++i) {
               Zipf z = new Zipf();
               z.size = zfcs[i];
               z.relofs = zfro[i];
               z.datofs = zfdo[i];
               z.CRC = (long)zcrc[i];
               z.setfull(zffn[i]);
               z.inpak = true;
               z.data = null;
               this.zf.add(z);
            }

         }
      }
   }

   public void writestr(RandomAccessFile b, String str) throws IOException {
      for(int i = 0; i < str.length(); ++i) {
         b.writeByte(str.charAt(i));
      }

   }

   public String readstr(RandomAccessFile b, int len) throws IOException {
      StringBuffer linebuff = new StringBuffer();

      for(int i = 0; i < len; ++i) {
         char c = (char)b.readUnsignedByte();
         linebuff.append(c);
      }

      return linebuff.toString();
   }

   public String readntstr(RandomAccessFile r, int len) throws IOException {
      String str = this.readstr(r, len);
      int eos = str.indexOf(0);
      if (eos > -1) {
         str = str.substring(0, eos);
      }

      return str;
   }

   public long roundupto4(long value) {
      return (value + 3L) / 4L * 4L;
   }
}
