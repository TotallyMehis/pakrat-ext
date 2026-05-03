package pak;

import java.util.List;

public enum FileType {
    OTHER(0, "Other"),
    MATERIAL(1, "Material"),
    TEXTURE(2, "Texture"),
    MODEL(3, "Model"),
    MODEL_DAT(4, "Model"),
    TEXT(5, "Text"),
    SOUND(6, "Sound");

    private final int value;
    private final String name;

    private FileType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public static FileType from(String fileName) {
        String fname = fileName.toLowerCase();

        if (fname.endsWith(".vmt")) {
            return FileType.MATERIAL;
        }

        if (fname.endsWith(".vtf")) {
            return FileType.TEXTURE;
        }

        if (fname.endsWith(".mdl")) {
            return FileType.MODEL;
        }

        for (String modelDataExt : List.of(".phy", ".ani", ".vtx", ".vvd")) {
            if (fname.endsWith(modelDataExt)) {
                return FileType.MODEL_DAT;
            }
        }

        if (fname.endsWith(".txt")) {
            return FileType.TEXT;
        }

        for (String soundExt : List.of(".wav", ".mp3", ".ogg")) {
            if (fname.endsWith(soundExt)) {
                return FileType.SOUND;
            }
        }

        return FileType.OTHER;
    }
}
