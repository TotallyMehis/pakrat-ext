package pak;

public abstract class PakratUI {
    public static String getPakratTitle() {
        return "Pakrat %s - Original Pakrat 0.95 by Rof".formatted(Version.getFullVersion());
    }
}
