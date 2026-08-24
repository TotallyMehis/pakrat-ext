package pak;

public abstract class PakratUI {
    private static final String PAKRAT_TITLE = "Pakrat %s - Original Pakrat 0.95 by Rof";

    public static String getPakratTitle() {
        return PAKRAT_TITLE.formatted(Version.getFullVersion());
    }
}
