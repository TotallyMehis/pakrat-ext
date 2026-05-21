package pak;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Mappak {
    private int ident;
    private int version;
    private Lump[] lumps;
    private int maprev;
    private int offset;
    private int length;
    private long filelength;
    private int cdoffs;
    private final List<Zipf> zf = new ArrayList<>();
    private final List<String> texname = new ArrayList<String>();
    private final List<String> staticname = new ArrayList<String>();
    private final List<String> detailname = new ArrayList<String>();
    private int glumps;
    private GameLump[] gl;
    private List<String> entkeylist;
    private List<String> entvallist;
    private final boolean auton;
    private int centralCommentLength;
    private String centralComment;

    private static final List<String> entext = List.of(".vmt", ".mdl", ".spr", ".wav", ".mp3", ".txt");

    private static final int CENTRAL_DIRECTORY_SIGNATURE = 101010256; // PK56
    private static final int LOCAL_FILEHEADER_SIGNATURE = 67324752; // PK34
    private static final int FILEHEADER_SIGNATURE = 33639248; // PK12
    private static final int BSP_MAGIC_NUMBER = 1347633750; // VBSP
    private static final int LZMA_MAGIC_NUMBER = 1280986433; // LZMA

    public Mappak(boolean auton) {
        this.auton = auton;
    }

    public void saveMap(RandomAccessFile bin, RandomAccessFile bout) throws IOException {
        bin.seek(1036L);
        bout.seek(1036L);
        Util.copyBlock(bin, bout, (long) (this.offset - 1036));
        bin.seek(roundUpTo4((long) (this.offset + this.length)));
        bout.seek(roundUpTo4(bout.getFilePointer()));
        long pakdiff = bout.getFilePointer() - bin.getFilePointer();
        Util.copyBlock(bin, bout, this.filelength - bin.getFilePointer());
        long outpos = roundUpTo4(bout.getFilePointer());
        bout.seek(0L);
        byte[] buffer = new byte[1036];
        ByteBuffer b = ByteBuffer.wrap(buffer);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.position(0);
        b.putInt(this.ident);
        b.putInt(this.version);

        for (int i = 0; i < LumpIndices.NUM_LUMPS; ++i) {
            Lump l = this.lumps[i];
            if (l.ofs() > this.lumps[LumpIndices.PAKFILE].ofs()) {
                int newOfs = (int) ((long) l.ofs() + pakdiff);
                l = this.lumps[i] = new Lump(newOfs, l.len(), l.vers(), l.fourCC());
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

    public void savePak(RandomAccessFile bin, RandomAccessFile bout) throws IOException {
        int numzips = this.zf.size();
        int newoffset = (int) bout.getFilePointer();

        for (int i = 0; i < numzips; ++i) {
            Zipf z = this.zf.get(i);
            z.setRelativeOffset((int) bout.getFilePointer() - newoffset);
            bout.writeInt(Swab.I(LOCAL_FILEHEADER_SIGNATURE));
            bout.writeShort(Swab.unsignedShort(10));
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeInt(Swab.I((int) z.getCRC()));
            bout.writeInt(Swab.I(z.getSize()));
            bout.writeInt(Swab.I(z.getSize()));
            bout.writeShort(Swab.unsignedShort(z.getFullPath().length()));
            bout.writeShort(0);
            writeString(bout, z.getFullPath());
            int oldDataOffset = z.getDataOffset();
            z.setDataOffset((int) bout.getFilePointer() - newoffset);
            if (z.isInPak()) {
                bin.seek((long) (this.offset + oldDataOffset));
                Util.copyBlock(bin, bout, z.getSize());
            } else {
                z.moveToPak(bout);
            }
        }

        this.cdoffs = (int) bout.getFilePointer() - newoffset;

        for (int i = 0; i < numzips; ++i) {
            Zipf z = (Zipf) this.zf.get(i);
            bout.writeInt(Swab.I(FILEHEADER_SIGNATURE));
            bout.writeShort(Swab.unsignedShort(20));
            bout.writeShort(Swab.unsignedShort(10));
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeInt(Swab.I((int) z.getCRC()));
            bout.writeInt(Swab.I(z.getSize()));
            bout.writeInt(Swab.I(z.getSize()));
            bout.writeShort(Swab.unsignedShort(z.getFullPath().length()));
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeShort(0);
            bout.writeInt(0);
            bout.writeInt(Swab.I(z.getRelativeOffset()));
            writeString(bout, z.getFullPath());
        }

        int cdend = (int) bout.getFilePointer() - newoffset;
        bout.writeInt(Swab.I(CENTRAL_DIRECTORY_SIGNATURE));
        bout.writeShort(0);
        bout.writeShort(0);
        bout.writeShort(Swab.unsignedShort(numzips));
        bout.writeShort(Swab.unsignedShort(numzips));
        bout.writeInt(Swab.I(cdend - this.cdoffs));
        bout.writeInt(Swab.I(this.cdoffs));
        bout.writeShort(Swab.unsignedShort(this.centralCommentLength));
        writeString(bout, this.centralComment);
        long actual_cdend = bout.getFilePointer();
        alignTo2(bout);
        int newlen = (int) actual_cdend - newoffset;
        this.lumps[LumpIndices.PAKFILE] = new Lump(newoffset, newlen, this.lumps[LumpIndices.PAKFILE].vers(),
                this.lumps[LumpIndices.PAKFILE].fourCC());
        this.offset = this.lumps[LumpIndices.PAKFILE].ofs();
        this.length = this.lumps[LumpIndices.PAKFILE].len();
        bout.seek(648L);
        bout.writeInt(Swab.I(this.lumps[LumpIndices.PAKFILE].ofs()));
        bout.writeInt(Swab.I(this.lumps[LumpIndices.PAKFILE].len()));
    }

    public void loadMap(RandomAccessFile b) throws IOException {
        this.loadHeader(b);
        this.loadPak(b);
        this.loadTexString(b);
        this.loadGameLump(b);
        this.loadPropStatics(b);
        this.loadPropDetails(b);
    }

    private void loadGameLump(RandomAccessFile raf) throws IOException {
        raf.seek((long) this.lumps[LumpIndices.GAME_LUMP].ofs());
        this.glumps = Swab.I(raf.readInt());
        this.gl = new GameLump[this.glumps];

        for (int i = 0; i < this.glumps; ++i) {
            int id = Swab.I(raf.readInt());
            short flags = Swab.S(raf.readShort());
            short vers = Swab.S(raf.readShort());
            int ofs = Swab.I(raf.readInt());
            int len = Swab.I(raf.readInt());

            this.gl[i] = new GameLump(id, flags, vers, ofs, len);
        }

    }

    private void loadPropStatics(RandomAccessFile raf) throws IOException {
        int spid = -1;

        for (int i = 0; i < this.glumps; ++i) {
            if (this.gl[i].id() == 1936749168) {
                spid = i;
            }
        }

        if (spid >= 0) {
            raf.seek((long) this.gl[spid].ofs());

            if (isAtLzma(raf)) {
                Cons.println("Prop statics lump is compressed, ignoring.");
                return;
            }

            int psnames = Swab.I(raf.readInt());

            for (int i = 0; i < psnames; ++i) {
                this.staticname.add(readNullTerminatedString(raf, 128));
            }
        }
    }

    private void loadPropDetails(RandomAccessFile raf) throws IOException {
        int dpid = -1;

        for (int i = 0; i < this.glumps; ++i) {
            if (this.gl[i].id() == 1685090928) {
                dpid = i;
            }
        }

        if (dpid >= 0) {
            raf.seek((long) this.gl[dpid].ofs());

            if (isAtLzma(raf)) {
                Cons.println("Prop details lump is compressed, ignoring.");
                return;
            }

            int pdnames = Swab.I(raf.readInt());

            for (int i = 0; i < pdnames; ++i) {
                this.detailname.add(readNullTerminatedString(raf, 128));
            }
        }
    }

    public void loadTexString(RandomAccessFile raf) throws IOException {
        int sofs = this.lumps[LumpIndices.TEXDATA_STRING_DATA].ofs();
        int slen = this.lumps[LumpIndices.TEXDATA_STRING_DATA].len();
        raf.seek((long) sofs);
        if (isAtLzma(raf)) {
            Cons.println("Texdata is compressed, ignoring.");
            return;
        }
        String tdsd = readString(raf, slen);
        int ofs = this.lumps[LumpIndices.TEXDATA_STRING_TABLE].ofs();
        int len = this.lumps[LumpIndices.TEXDATA_STRING_TABLE].len();
        final int stringTableStructSize = LumpIndices.lumpStructSize(LumpIndices.TEXDATA_STRING_TABLE);
        assert len % stringTableStructSize == 0;
        int numtdst = len / stringTableStructSize;
        raf.seek((long) ofs);

        for (int i = 0; i < numtdst; ++i) {
            int ix = Swab.I(raf.readInt());

            for (int j = ix; j < sofs; ++j) {
                if (tdsd.charAt(j) == 0) {
                    this.texname.add(tdsd.substring(ix, j));
                    break;
                }
            }
        }

    }

    public void loadEntities(RandomAccessFile raf, JProgFrame prog) throws IOException {
        this.entkeylist = new ArrayList<>();
        this.entvallist = new ArrayList<>();
        ArrayList<String> keylist = new ArrayList<>();
        ArrayList<String> vallist = new ArrayList<>();
        String classname = "";
        int ofs = this.lumps[LumpIndices.ENTITIES].ofs();
        int end = ofs + this.lumps[LumpIndices.ENTITIES].len();
        raf.seek((long) ofs);
        if (!this.auton) {
            prog.setMaximum(this.lumps[LumpIndices.ENTITIES].len());
        }

        long fp;
        while ((fp = raf.getFilePointer()) < (long) end) {
            String line = raf.readLine();
            if (line == null) {
                break;
            }

            if (line.equals("{")) {
                keylist.clear();
                vallist.clear();
                classname = "";
            } else if (line.equals("}")) {
                for (int i = 0; i < keylist.size(); ++i) {
                    this.entkeylist.add(classname + " : keyword \"" + keylist.get(i) + "\"");
                    this.entvallist.add(vallist.get(i));
                }

                if (!this.auton) {
                    prog.setValue((int) fp - ofs);
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
                        for (String ext : entext) {
                            if (val.endsWith(ext)) {
                                if (ext.equals(".spr")) {
                                    val = stripExtension(val) + ".vmt";
                                }

                                keylist.add(key);
                                vallist.add(val);
                                done = true;
                                break;
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

    private static String stripExtension(String in) {
        String out = in;
        int i = in.lastIndexOf(".");
        if (i > -1) {
            out = in.substring(0, i);
        }

        return out;
    }

    private void loadHeader(RandomAccessFile bb) throws IOException {
        this.filelength = bb.length();
        this.ident = Swab.I(bb.readInt());
        this.version = Swab.I(bb.readInt());
        if (this.ident != BSP_MAGIC_NUMBER) {
            Cons.println("Not a map file: " + BSP_MAGIC_NUMBER + " != " + this.ident);
        } else {
            if (!this.auton) {
                Cons.println("Ident: " + this.ident);
                Cons.println("Version: " + this.version);
            }

            this.lumps = new Lump[LumpIndices.NUM_LUMPS];

            for (int i = 0; i < LumpIndices.NUM_LUMPS; ++i) {
                int ofs = Swab.I(bb.readInt());
                int len = Swab.I(bb.readInt());
                int vers = Swab.I(bb.readInt());
                int fourCC = Swab.I(bb.readInt());

                Lump lump = new Lump(ofs, len, vers, fourCC);

                if (lump.len() > 0 && !this.auton) {
                    int lumpStructSize = LumpIndices.lumpStructSize(i);
                    if (lumpStructSize <= 0) {
                        lumpStructSize = 1;
                    }

                    Cons.print("Lump " + i + ": ");
                    Cons.print(lump.ofs() + ", " + lump.len() + ", " + lump.vers() + ", " + lump.fourCC());
                    Cons.println(
                            " " + LumpIndices.lumpName(i) + "  " + lump.len() / lumpStructSize
                                    + (lumpStructSize == 1 ? " bytes" : ""));
                }

                this.lumps[i] = lump;
            }

            this.maprev = Swab.I(bb.readInt());
            if (!this.auton) {
                Cons.println("MapRev: " + this.maprev + "\n----------------\n");
            }

            int last = 0;

            for (int i = 0; i < LumpIndices.NUM_LUMPS; ++i) {
                if (this.lumps[i].ofs() > this.lumps[last].ofs()) {
                    last = i;
                }
            }

            this.offset = this.lumps[LumpIndices.PAKFILE].ofs();
            this.length = this.lumps[LumpIndices.PAKFILE].len();
            if (this.length == 0) {
                Cons.println("Map contains no pakfile!");
            } else {
                Cons.println("Pak file at offset " + this.offset + ", length " + this.length + " bytes");
            }
        }
    }

    private void loadPak(RandomAccessFile bb) throws IOException {
        boolean cdirf = false;

        for (int off = this.offset + this.length - 22; off >= 0; --off) {
            bb.seek((long) off);
            int csig = Swab.I(bb.readInt());
            if (csig == CENTRAL_DIRECTORY_SIGNATURE) {
                cdirf = true;
                break;
            }
        }

        if (!cdirf) {
            Cons.println("Couldn't find pak file EOCD");
        } else {
            bb.readShort(); // numberOfThisDisk
            bb.readShort(); // numberOfTheDiskWithStartOfCentralDirectory
            bb.readShort(); // nCentralDirectoryEntries_ThisDisk
            int cdes = Swab.unsignedShort(bb.readUnsignedShort()); // nCentralDirectoryEntries_Total
            bb.readInt(); // centralDirectorySize
            int cdoffs = Swab.I(bb.readInt()); // startOfCentralDirOffset
            this.centralCommentLength = Swab.unsignedShort(bb.readUnsignedShort()); // commentLength
            this.centralComment = readString(bb, this.centralCommentLength);
            Cons.println("Pak lump entries: " + cdes);
            if (cdes > 0) {
                bb.seek((long) (this.offset + cdoffs));
                int[] zfcs = new int[cdes];
                int[] zfus = new int[cdes];
                int[] zfro = new int[cdes];
                int[] zfdo = new int[cdes];
                int[] zcrc = new int[cdes];
                String[] zffn = new String[cdes];

                // Read file headers
                for (int i = 0; i < cdes; ++i) {
                    int hsig = Swab.I(bb.readInt());
                    if (hsig != FILEHEADER_SIGNATURE) {
                        Cons.println("ZipFH signature incorrect");
                        return;
                    }

                    bb.readShort(); // Version made by
                    bb.readShort(); // Version needed to extract
                    bb.readShort(); // Bit flags
                    bb.readShort(); // Compression method
                    bb.readShort(); // File last modified time
                    bb.readShort(); // File last modified date
                    zcrc[i] = Swab.I(bb.readInt());
                    int csize = zfcs[i] = Swab.I(bb.readInt()); // Compressed size
                    int usize = zfus[i] = Swab.I(bb.readInt()); // Uncompressed size
                    int fnlen = Swab.unsignedShort(bb.readUnsignedShort()); // File name length
                    int exlen = Swab.unsignedShort(bb.readUnsignedShort()); // Extra field length
                    int fclen = Swab.unsignedShort(bb.readUnsignedShort()); // File comment length
                    bb.readShort(); // Disk number start
                    bb.readShort(); // Internal file attributes
                    bb.readInt(); // External file attributes
                    zfro[i] = Swab.I(bb.readInt()); // Offset of local header
                    zffn[i] = readString(bb, fnlen); // File name
                    readString(bb, exlen + fclen); // Extr field & file comment
                    if (csize != usize) {
                        Cons.println("Zip file " + i + " is compressed! " + csize + "!=" + usize);
                        return;
                    }
                }

                // Read local file headers
                for (int i = 0; i < cdes; ++i) {
                    bb.seek((long) (this.offset + zfro[i]));
                    int lsig = Swab.I(bb.readInt());
                    if (lsig != LOCAL_FILEHEADER_SIGNATURE) {
                        Cons.println("ZipLFH signature incorrect");
                        return;
                    }

                    bb.readShort(); // Version needed to extract
                    bb.readShort(); // Bit flags
                    bb.readShort(); // Compression method
                    bb.readShort(); // File last modified time
                    bb.readShort(); // File last modified date
                    bb.readInt(); // CRC
                    bb.readInt(); // Compression size
                    bb.readInt(); // Uncompressed size
                    int fnlen = Swab.unsignedShort(bb.readUnsignedShort()); // File name length
                    int exlen = Swab.unsignedShort(bb.readUnsignedShort()); // Extra field length
                    zfdo[i] = zfro[i] + 30 + fnlen + exlen;
                }

                for (int i = 0; i < cdes; ++i) {
                    this.zf.add(Zipf.fromPak(zffn[i], zfcs[i], zfro[i], zfdo[i], zcrc[i]));
                }
            }
        }
    }

    private static void writeString(RandomAccessFile b, String str) throws IOException {
        for (int i = 0; i < str.length(); ++i) {
            b.writeByte(str.charAt(i));
        }

    }

    private static String readString(RandomAccessFile b, int len) throws IOException {
        StringBuilder linebuff = new StringBuilder(len);

        for (int i = 0; i < len; ++i) {
            char c = (char) b.readUnsignedByte();
            linebuff.append(c);
        }

        return linebuff.toString();
    }

    private static String readNullTerminatedString(RandomAccessFile r, int len) throws IOException {
        String str = readString(r, len);
        int eos = str.indexOf(0);
        if (eos > -1) {
            str = str.substring(0, eos);
        }

        return str;
    }

    private static long roundUpTo4(long value) {
        return (value + 3L) / 4L * 4L;
    }

    private static void alignTo2(RandomAccessFile fileOut) throws IOException {
        final int ALIGNMENT = 2;

        int offset = (int) (fileOut.getFilePointer() % ALIGNMENT);
        for (int i = 0; i < offset; i++) {
            fileOut.writeByte(0);
        }
    }

    private static boolean isAtLzma(RandomAccessFile file) throws IOException {
        long previousPointer = file.getFilePointer();
        try {
            int magicNumber = file.readInt();
            if (magicNumber == LZMA_MAGIC_NUMBER) {
                return true;
            }
        } finally {
            file.seek(previousPointer);
        }

        return false;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.length;
    }

    public int getCdoffs() {
        return this.cdoffs;
    }

    public List<String> getTexname() {
        return this.texname;
    }

    public List<String> getStaticname() {
        return this.staticname;
    }

    public List<String> getDetailname() {
        return this.detailname;
    }

    public List<String> getEntkeylist() {
        return this.entkeylist;
    }

    public List<String> getEntvallist() {
        return this.entvallist;
    }

    public List<Zipf> getZf() {
        return this.zf;
    }
}
