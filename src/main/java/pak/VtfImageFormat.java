package pak;

import java.util.Optional;

public enum VtfImageFormat {
    RGBA8888(0), ABGR8888(1), RGB888(2), BGR888(3), RGB565(4), I8(5), IA88(6), P8(7), A8(8), RGB888_BLUESCREEN(
            9), BGR888_BLUESCREEN(10), ARGB8888(11), BGRA8888(12), DXT1(13), DXT3(14), DXT5(15), BGRX8888(16), BGR565(
                    17), BGRX5551(18), BGRA4444(19), DXT1_ONEBITALPHA(20), BGRA5551(21), UV88(22), UVWQ8888(
                            23), RGBA16161616F(
                                    24), RGBA16161616(25), UVLX8888(26), R32F(27), RGB323232F(28), RGBA32323232F(29);

    private static final int[] FORMAT_SIZE_IN_BYTES = new int[] {
            4, // RGBA8888
            4, // ABGR8888
            3, // RGB888
            3, // BGR888
            2, // RGB565
            1, // I8
            2, // IA88
            1, // P8
            1, // A8
            3, // RGB888_BLUESCREEN
            3, // BGR888_BLUESCREEN
            4, // ARGB8888
            4, // BGRA8888
            0, // DXT1
            0, // DXT3
            0, // DXT5
            4, // BGRX8888
            2, // BGR565
            2, // BGRX5551
            2, // BGRA4444
            0, // DXT1_ONEBITALPHA
            2, // BGRA5551
            2, // UV88
            4, // UVWQ8888
            8, // RGBA16161616F
            8, // RGBA16161616
            4, // UVLX8888
            4, // R32F
            12, // RGB323232F
            16, // RGBA32323232F
    };

    private final int value;

    private VtfImageFormat(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Optional<VtfImageFormat> from(int value) {
        for (VtfImageFormat format : values()) {
            if (format.getValue() == value) {
                return Optional.of(format);
            }
        }

        return Optional.empty();
    }

    public int getPixelSizeInBytes() {
        assert this.value >= 0 && this.value < FORMAT_SIZE_IN_BYTES.length;
        return FORMAT_SIZE_IN_BYTES[this.value];
    }

    public String getName() {
        return this.toString();
    }
}
