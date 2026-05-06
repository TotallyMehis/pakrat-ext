package pak;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Mdl {
    boolean isValid = false;
    int id;
    int version;
    int checksum;
    String name;
    int length;
    int numtextures;
    int textureindex;
    int numtexpaths;
    int texpathindex;
    String[] textures;
    String[] texpaths;
    int numincmodels;
    int incmodelindex;
    String[] incmodelfile;

    public Mdl() {
    }

    public void read(ByteBuffer b) throws IOException {
        this.isValid = false;
        int start = b.position();
        this.id = b.getInt();
        this.version = b.getInt();
        if (this.id == 1414743113 && this.version == 44) {
            this.checksum = b.getInt();
            this.name = this.readstr(b);
            b.position(start + 76);
            this.length = b.getInt();
            b.position(start + 204);
            this.numtextures = b.getInt();
            this.textureindex = b.getInt();
            this.numtexpaths = b.getInt();
            this.texpathindex = b.getInt();
            this.texpaths = new String[this.numtexpaths];
            this.textures = new String[this.numtextures];
            b.position(start + 336);
            this.numincmodels = b.getInt();
            this.incmodelindex = b.getInt();
            this.incmodelfile = new String[this.numincmodels];

            for (int i = 0; i < this.numtexpaths; ++i) {
                b.position(start + this.texpathindex + i * 4);
                int pindex = b.getInt();
                b.position(start + pindex);
                this.texpaths[i] = this.readstr(b);
            }

            for (int i = 0; i < this.numtextures; ++i) {
                b.position(start + this.textureindex + 64 * i);
                int tindex = b.getInt();
                b.position(start + this.textureindex + 64 * i + tindex);
                this.textures[i] = this.readstr(b);
            }

            for (int i = 0; i < this.numincmodels; ++i) {
                b.position(start + this.incmodelindex + 8 * i + 4);
                int imfindex = b.getInt();
                b.position(start + this.incmodelindex + 8 * i + imfindex);
                this.incmodelfile[i] = this.readstr(b);
            }

            this.isValid = true;
        }
    }

    public ArrayList<String> gettexturelist() {
        ArrayList<String> texlist = new ArrayList<>();
        if (!this.isValid) {
            return texlist;
        } else {
            for (int i = 0; i < this.numtexpaths; ++i) {
                for (int j = 0; j < this.numtextures; ++j) {
                    String tex = this.texpaths[i] + this.textures[j];
                    texlist.add(tex);
                }
            }

            return texlist;
        }
    }

    public String readstr(ByteBuffer b) {
        StringBuffer linebuff = new StringBuffer();

        while (true) {
            char c = (char) b.get();
            if (c == 0) {
                return linebuff.toString();
            }

            linebuff.append(c);
        }
    }

    @Override
    public String toString() {
        return !this.isValid ? null : this.name + " " + Integer.toHexString(this.checksum);
    }
}
