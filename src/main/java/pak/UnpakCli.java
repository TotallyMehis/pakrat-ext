package pak;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Collection;

public abstract class UnpakCli {
    public static void savePakFileToDisk(String bspFilePath, String pakFile, String outputFile) throws Exception {
        Cons.open(false);
        long starttime = System.currentTimeMillis();
        Cons.println(
                "**** Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)"
                        .formatted(Version.getFullVersion()));
        Cons.println("Saving " + pakFile + " from " + bspFilePath);

        try {
            if (!bspFilePath.endsWith(".bsp")) {
                bspFilePath = bspFilePath + ".bsp";
            }

            File bspFile = new File(bspFilePath);
            if (bspFile.exists() && bspFile.canRead()) {
                Cons.println("Reading " + bspFilePath);
                var raf = new RandomAccessFile(bspFile, "r");
                var mapPak = new Mappak(true);
                mapPak.loadMap(raf);
                Zipf match = findZipFileByName(mapPak.getZf(), pakFile);
                if (match == null) {
                    Cons.println("Can't find file " + pakFile + " in Pak.");
                } else {
                    File mfile = new File(outputFile);
                    Unpak.savePakFile(mapPak, raf, null, true, match, mfile, false);
                    raf.close();
                    long duration = System.currentTimeMillis() - starttime;
                    Cons.println("**** Pakrat file save complete in "
                            + (new DecimalFormat("0.#")).format((double) ((float) duration / 1000.0F)) + " seconds");
                }
            } else {
                Cons.println("Can't open " + bspFilePath);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static Zipf findZipFileByName(Collection<Zipf> zipFiles, String fname) {
        for (Zipf zipFile : zipFiles) {
            if (fname.equalsIgnoreCase(zipFile.getFileName())) {
                return zipFile;
            }
        }

        return null;
    }

    public static void printPakFiles(String filename) throws Exception {
        Cons.open(false);
        Cons.println(
                "Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)".formatted(Version.getFullVersion()));
        Cons.println("Listing pak files from " + filename);

        if (!filename.endsWith(".bsp")) {
            filename = filename + ".bsp";
        }

        var infile = new File(filename);
        if (!infile.exists() || !infile.canRead()) {
            Cons.println("Can't open " + filename);
            return;
        }

        try (var raf = new RandomAccessFile(infile, "r")) {
            var m = new Mappak(true);
            m.loadMap(raf);

            for (Zipf zipFile : m.getZf()) {
                Cons.println(zipFile.getFullDetails());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void dumpPak(String bspFilePath, String outputFile) throws Exception {
        Cons.open(false);
        long starttime = System.currentTimeMillis();
        Cons.println(
                "**** Pakrat %s - Original Pakrat 0.95 by Rof (rof@mellish.org.uk)"
                        .formatted(Version.getFullVersion()));
        Cons.println("Dumping pak lump from " + bspFilePath);

        if (!bspFilePath.endsWith(".bsp")) {
            bspFilePath = bspFilePath + ".bsp";
        }

        var infile = new File(bspFilePath);
        if (!infile.exists() || !infile.canRead()) {
            Cons.println("Can't open " + bspFilePath);
            return;
        }

        Cons.println("Reading " + bspFilePath);
        Pakpref.mapdir = infile.getPath();
        File outfile = new File(outputFile);
        try (var raf = new RandomAccessFile(infile, "r"); var zraf = new RandomAccessFile(outfile, "rw")) {
            var m = new Mappak(true);
            m.loadMap(raf);
            Cons.print("Writing " + outputFile + "...");
            zraf.setLength(0L);
            raf.seek(m.getOffset());
            Util.copyBlock(raf, zraf, m.getLength());
            zraf.close();
            Cons.println("done");
            long duration = System.currentTimeMillis() - starttime;
            Cons.println("**** Pakrat file dump complete in "
                    + (new DecimalFormat("0.#")).format((double) ((float) duration / 1000.0F)) + " seconds");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
