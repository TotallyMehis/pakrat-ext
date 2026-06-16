package pak;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Mdl {
    private final boolean valid;
    private final int id;
    private final int version;
    private final int checksum;
    private final String name;
    private final int length;
    private final String[] textures;
    private final String[] texpaths;
    private final String[] incmodels;

    public static final int MDL_ID = 1414743113; // IDST
    public static final int MDL_VERSION = 44;

    private Mdl(boolean valid, int id, int version, int checksum, String[] incmodels, int length, String name,
            String[] texpaths,
            String[] textures) {
        this.valid = valid;
        this.id = id;
        this.version = version;
        this.checksum = checksum;
        this.name = name;
        this.length = length;
        this.textures = textures;
        this.texpaths = texpaths;
        this.incmodels = incmodels;
    }

    public static Mdl read(ByteBuffer b) throws IOException {
        int start = b.position();
        int id = b.getInt();
        int version = b.getInt();
        if (id != MDL_ID || version != MDL_VERSION) {
            return new Mdl(false, id, version, 0, new String[0], id, "", new String[0], new String[0]);
        }

        int checksum = b.getInt();
        String name = readstr(b);
        b.position(start + 76);
        int length = b.getInt();
        b.position(start + 204);
        int numtextures = b.getInt();
        int textureindex = b.getInt();
        int numtexpaths = b.getInt();
        int texpathindex = b.getInt();
        String[] texpaths = new String[numtexpaths];
        String[] textures = new String[numtextures];
        b.position(start + 336);
        int numincmodels = b.getInt();
        int incmodelindex = b.getInt();
        String[] incmodels = new String[numincmodels];

        for (int i = 0; i < numtexpaths; ++i) {
            b.position(start + texpathindex + i * 4);
            int pindex = b.getInt();
            b.position(start + pindex);
            texpaths[i] = readstr(b);
        }

        for (int i = 0; i < numtextures; ++i) {
            b.position(start + textureindex + 64 * i);
            int tindex = b.getInt();
            b.position(start + textureindex + 64 * i + tindex);
            textures[i] = readstr(b);
        }

        for (int i = 0; i < numincmodels; ++i) {
            b.position(start + incmodelindex + 8 * i + 4);
            int imfindex = b.getInt();
            b.position(start + incmodelindex + 8 * i + imfindex);
            incmodels[i] = readstr(b);
        }

        return new Mdl(true, id, version, checksum, incmodels, length, name, texpaths, textures);
    }

    public List<String> getTextureList() {
        ArrayList<String> texlist = new ArrayList<>();
        if (!this.valid) {
            return texlist;
        } else {
            for (String texpath : this.texpaths) {
                for (String texture : this.textures) {
                    String tex = texpath + texture;
                    texlist.add(tex);
                }
            }

            return texlist;
        }
    }

    private static String readstr(ByteBuffer b) {
        StringBuilder linebuff = new StringBuilder();

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
        return !this.valid ? null : this.name + " " + Integer.toHexString(this.checksum);
    }

    public boolean isValid() {
        return valid;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public int getChecksum() {
        return checksum;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public String[] getTextures() {
        return textures;
    }

    public String[] getTexturePaths() {
        return texpaths;
    }

    public String[] getIncmodels() {
        return incmodels;
    }
}
