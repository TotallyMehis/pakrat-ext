package pak;

public class VtfFlags {
    private static final String[] FLAG_NAMES = new String[] { "POINTSAMPLE", "TRILINEAR", "CLAMP-S", "CLAMP-T",
            "ANISOTROPIC",
            "HINT-DXT5", "NOCOMPRESS", "NORMAL", "NOMIP", "NOLOD", "MINMIP", "PROC", "1BALPHA", "8BALPHA", "ENVMAP",
            "RENDERTARGET", "DEPTH-RT", "NODEBUGOVERRIDE", "SINGLECOPY", "1OVERMIPLEVELINALPHA", "PREMULTCOL1OML",
            "NORMALTODUDV", "ALPHATESTMIPGEN", "NODEPTHBUFF", "NICEFILTERED" };

    public static String getFlagString(int flags) {
        int bflags = flags;
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < FLAG_NAMES.length; ++i) {
            if ((bflags & 1) == 1) {
                str.append(FLAG_NAMES[i]).append(" ");
            }

            bflags >>= 1;
        }

        return str.toString();
    }
}
