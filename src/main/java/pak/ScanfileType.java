package pak;

import static java.util.Locale.ROOT;

public enum ScanfileType {

    NUL(""), VMT(".vmt"), VTF(".vtf"), MDL(".mdl"), VTX(".vtx"), PHY(".phy"), VVD(".vvd"), WAV(".wav"), MP3(
            ".mp3"), NAV(".nav"), AIN(".ain"), TXT(".txt"), CACHE(".cache");

    private final String extension;

    private ScanfileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

    public static ScanfileType getTypeFromFilename(String name) {
        name = name.toLowerCase(ROOT);

        for (ScanfileType scanfileType : values()) {
            if (scanfileType == NUL) {
                continue;
            }

            if (name.endsWith(scanfileType.getExtension())) {
                return scanfileType;
            }
        }

        return NUL;
    }

    public String getPrettyName() {
        return switch (this) {
            case NUL -> "Unknown";
            case VMT -> "Material";
            case VTF -> "Texture";
            case MDL, VTX, PHY, VVD -> "Model";
            case WAV, MP3 -> "Sound";
            case NAV -> "Navigation";
            case AIN -> "AI Node";
            case TXT -> "Text";
            case CACHE -> "Soundcache";
        };
    }
}
