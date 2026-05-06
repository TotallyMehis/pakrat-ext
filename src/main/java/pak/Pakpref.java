package pak;

import java.util.prefs.Preferences;

public class Pakpref {
    private static Preferences prefs;
    static String gamedir;
    static String mapdir;
    static String adddir;
    static int fixup;
    static boolean navfile;
    static boolean ainfile;
    static boolean soundcache;
    static boolean description;
    static boolean overview;
    static boolean soundscape;

    public Pakpref() {
    }

    public static void getInit() {
        String currentdir = System.getProperty("user.dir");
        prefs = Preferences.userRoot().node("Pakrat");
        gamedir = prefs.get("Gamedir", "");
        fixup = prefs.getInt("Fixup", 1);
        mapdir = prefs.get("Mapdir", currentdir);
        adddir = prefs.get("Adddir", currentdir);
        navfile = prefs.getBoolean("AutoNavfile", true);
        ainfile = prefs.getBoolean("AutoAinfile", true);
        soundcache = prefs.getBoolean("AutoSoundcache", true);
        description = prefs.getBoolean("AutoDescript", true);
        overview = prefs.getBoolean("AutoOverview", true);
        soundscape = prefs.getBoolean("AutoSoundscape", true);
    }

    public static void setInit() {
        prefs.put("Gamedir", gamedir);
        prefs.putInt("Fixup", fixup);
        prefs.putBoolean("AutoNavfile", navfile);
        prefs.putBoolean("AutoAinfile", ainfile);
        prefs.putBoolean("AutoSoundcace", soundcache);
        prefs.putBoolean("AutoDescript", description);
        prefs.putBoolean("AutoOverview", overview);
        prefs.putBoolean("AutoSoundscape", soundscape);
    }

    public static void put(String key, String val) {
        prefs.put(key, val);
    }

    public static String get(String key, String def) {
        return prefs.get(key, def);
    }
}
