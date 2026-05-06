package pak;

public abstract class Swab {
    public static final int I(int v) {
        return v >>> 24 | v << 24 | v << 8 & 16711680 | v >> 8 & '\uff00';
    }

    public static final short S(int v) {
        return (short) (('\uff00' & v) >>> 8 | v << 8 & '\uff00');
    }
}
