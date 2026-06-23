package pak;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Vtf {
    private static final int TEXTUREFLAGS_ENVMAP = 16384;

    private static final String VTF_SIGNATURE = "VTF\u0000";

    private final boolean valid;
    private final int[] version;
    private final short width;
    private final short height;
    private final int flags;
    private final VtfImageFormat imageFormat;
    private final int numberOfFrames;
    private final int startFrame;
    private final float reflectivityX;
    private final float reflectivityY;
    private final float reflectivityZ;
    private final float bumpScale;
    private final int numberOfMipMaps;
    private final VtfImageFormat lowResImageFormat;
    private final short lowResWidth;
    private final short lowResHeight;
    private final byte[] lowResBuffer;
    private final byte[] buffer;

    private Vtf(boolean valid, int[] version, int flags, VtfImageFormat imageFormat, short width, short height,
            byte[] buffer,
            VtfImageFormat lowResImageFormat, short lowResWidth, short lowResHeight, byte[] lowResBuffer,
            float bumpScale, int numberOfFrames, int numberOfMipMaps, float reflectivityX, float reflectivityY,
            float reflectivityZ, int startFrame) {
        this.valid = valid;
        this.version = version;
        this.width = width;
        this.height = height;
        this.flags = flags;
        this.imageFormat = imageFormat;
        this.numberOfFrames = numberOfFrames;
        this.startFrame = startFrame;
        this.reflectivityX = reflectivityX;
        this.reflectivityY = reflectivityY;
        this.reflectivityZ = reflectivityZ;
        this.bumpScale = bumpScale;
        this.numberOfMipMaps = numberOfMipMaps;
        this.lowResImageFormat = lowResImageFormat;
        this.lowResWidth = lowResWidth;
        this.lowResHeight = lowResHeight;
        this.lowResBuffer = lowResBuffer;
        this.buffer = buffer;
    }

    public static Vtf read(ByteBuffer b) throws IOException {
        char[] type = new char[4];

        for (int i = 0; i < 4; ++i) {
            type[i] = (char) b.get();
        }

        String signature = new String(type);
        if (!signature.equals(VTF_SIGNATURE)) {
            return new Vtf(false, new int[2], 0, null, (short) 0, (short) 0, null, null, (short) 0, (short) 0, null,
                    1.0f,
                    0, 0, 0.0f, 0.0f, 0.0f, 0);
        }

        int[] version = new int[2];
        version[0] = b.getInt();
        version[1] = b.getInt();
        int headerSize = b.getInt();
        short width = b.getShort();
        short height = b.getShort();
        int flags = b.getInt();
        int numberOfFrames = b.getShort();
        int startFrame = b.getShort();
        b.getInt();
        float reflectivityX = b.getFloat();
        float reflectivityY = b.getFloat();
        float reflectivityZ = b.getFloat();
        b.getInt();
        float bumpScale = b.getFloat();
        int imageFormatValue = b.getInt();
        VtfImageFormat imageFormat = VtfImageFormat.from(imageFormatValue)
                .orElseThrow(() -> new RuntimeException("Unknown image format value: " + imageFormatValue));
        int numberOfMipMaps = b.get();
        int lowResImageFormatValue = b.getInt();
        VtfImageFormat lowResImageFormat = VtfImageFormat.from(lowResImageFormatValue).orElse(null);
        short lowResWidth = (short) b.get();
        short lowResHeight = (short) b.get();
        int lowResBufferSize = 0;
        if (lowResImageFormat != null) {
            lowResBufferSize = calcSize(lowResWidth, lowResHeight, lowResImageFormat);
        }

        int bufferSize = calcSize(width, height, numberOfMipMaps, imageFormat)
                * calculateFaceCount(startFrame, flags)
                * numberOfFrames;
        b.position(headerSize);
        byte[] lowResBuffer = new byte[lowResBufferSize];
        if (lowResImageFormat != null) {
            b.get(lowResBuffer);
        }

        byte[] buffer = new byte[bufferSize];
        b.get(buffer);

        return new Vtf(true, version, flags, imageFormat, width, height, buffer, lowResImageFormat, lowResWidth,
                lowResHeight, lowResBuffer, bumpScale, numberOfFrames, numberOfMipMaps, reflectivityX,
                reflectivityY, reflectivityZ, startFrame);
    }

    public int[] getIntARGB(int frame, int face, int miplevel, double gamma, double brightness) {
        int[] idata = new int[this.getWidth(miplevel) * this.getHeight(miplevel)];
        byte[] data = getRGBA(this.getData(frame, face, miplevel), this.getWidth(miplevel),
                this.getHeight(miplevel),
                this.imageFormat, gamma, brightness);
        int a = 0;

        for (int i = 0; i < idata.length; ++i) {
            idata[i] = (data[a + 3] & 255) << 24 | (data[a] & 255) << 16 | (data[a + 1] & 255) << 8 | data[a + 2] & 255;
            a += 4;
        }

        return idata;
    }

    public int[] getIntCompRGBA(int frame, int face, int miplevel, int component, double gamma, double brightness) {
        int[] idata = new int[this.getWidth(miplevel) * this.getHeight(miplevel)];
        byte[] data = getRGBA(this.getData(frame, face, miplevel), this.getWidth(miplevel),
                this.getHeight(miplevel),
                this.imageFormat, gamma, brightness);
        int a = 0;

        for (int i = 0; i < idata.length; ++i) {
            int alph = data[a + component] & 255;
            idata[i] = alph << 16 | alph << 8 | alph;
            a += 4;
        }

        return idata;
    }

    private static byte[] getRGBA(byte[] data, int mwidth, int mheight, VtfImageFormat format, double gamma,
            double brightness) {
        int destsize = calcSize(mwidth, mheight, VtfImageFormat.RGBA8888);
        if (format != VtfImageFormat.DXT1 && format != VtfImageFormat.DXT1_ONEBITALPHA) {
            if (format == VtfImageFormat.DXT5) {
                return decompDXT5(data, mwidth, mheight);
            } else {
                byte[] dest = new byte[destsize];
                if (format == VtfImageFormat.RGB888) {
                    int end = mwidth * mheight * 3;
                    int j = 0;

                    for (int i = 0; i < end; i += 3) {
                        dest[j] = data[i];
                        dest[j + 1] = data[i + 1];
                        dest[j + 2] = data[i + 2];
                        dest[j + 3] = -1;
                        j += 4;
                    }

                    return dest;
                } else if (format == VtfImageFormat.BGR888) {
                    int end = mwidth * mheight * 3;
                    int j = 0;

                    for (int i = 0; i < end; i += 3) {
                        dest[j] = data[i + 2];
                        dest[j + 1] = data[i + 1];
                        dest[j + 2] = data[i + 0];
                        dest[j + 3] = -1;
                        j += 4;
                    }

                    return dest;
                } else if (format != VtfImageFormat.RGBA8888 && format != VtfImageFormat.UVWQ8888) {
                    if (format == VtfImageFormat.BGRA8888) {
                        int end = mwidth * mheight * 4;

                        for (int i = 0; i < end; i += 4) {
                            dest[i + 0] = data[i + 2];
                            dest[i + 1] = data[i + 1];
                            dest[i + 2] = data[i + 0];
                            dest[i + 3] = data[i + 3];
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGRX8888) {
                        int end = mwidth * mheight * 4;

                        for (int i = 0; i < end; i += 4) {
                            dest[i + 0] = data[i + 2];
                            dest[i + 1] = data[i + 1];
                            dest[i + 2] = data[i + 0];
                            dest[i + 3] = -1;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.ARGB8888) {
                        int end = mwidth * mheight * 4;

                        for (int i = 0; i < end; i += 4) {
                            dest[i + 0] = data[i + 3];
                            dest[i + 1] = data[i + 2];
                            dest[i + 2] = data[i + 1];
                            dest[i + 3] = data[i + 0];
                        }

                        return dest;
                    } else if (format == VtfImageFormat.A8) {
                        int end = mwidth * mheight;
                        int j = 0;

                        for (int i = 0; i < end; ++i) {
                            dest[j + 0] = -1;
                            dest[j + 1] = -1;
                            dest[j + 2] = -1;
                            dest[j + 3] = data[i];
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.I8) {
                        int end = mwidth * mheight;
                        int j = 0;

                        for (int i = 0; i < end; ++i) {
                            dest[j + 0] = data[i];
                            dest[j + 1] = data[i];
                            dest[j + 2] = data[i];
                            dest[j + 3] = -1;
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.IA88) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            dest[j + 0] = data[i];
                            dest[j + 1] = data[i];
                            dest[j + 2] = data[i];
                            dest[j + 3] = data[i + 1];
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.RGB888_BLUESCREEN) {
                        int end = mwidth * mheight * 3;
                        int j = 0;

                        for (int i = 0; i < end; i += 3) {
                            if (data[i] == 0 && data[i + 1] == 0 && data[i + 2] == 255) {
                                dest[j] = 0;
                                dest[j + 1] = 0;
                                dest[j + 2] = 0;
                                dest[j + 3] = 0;
                            } else {
                                dest[j] = data[i];
                                dest[j + 1] = data[i + 1];
                                dest[j + 2] = data[i + 2];
                                dest[j + 3] = -1;
                            }

                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGR888_BLUESCREEN) {
                        int end = mwidth * mheight * 3;
                        int j = 0;

                        for (int i = 0; i < end; i += 3) {
                            if (data[i] == 255 && data[i + 1] == 0 && data[i + 2] == 0) {
                                dest[j] = 0;
                                dest[j + 1] = 0;
                                dest[j + 2] = 0;
                                dest[j + 3] = 0;
                            } else {
                                dest[j] = data[i + 2];
                                dest[j + 1] = data[i + 1];
                                dest[j + 2] = data[i + 0];
                                dest[j + 3] = -1;
                            }

                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.RGB565) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int red = src & 31;
                            int green = src >> 5 & 63;
                            int blue = src >>> 11 & 31;
                            dest[j + 0] = (byte) (red << 3 | red >> 2);
                            dest[j + 1] = (byte) (green << 2 | green >> 4);
                            dest[j + 2] = (byte) (blue << 3 | blue >> 2);
                            dest[j + 3] = -1;
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGR565) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int blue = src & 31;
                            int green = src >> 5 & 63;
                            int red = src >>> 11 & 31;
                            dest[j + 0] = (byte) (red << 3 | red >> 2);
                            dest[j + 1] = (byte) (green << 2 | green >> 4);
                            dest[j + 2] = (byte) (blue << 3 | blue >> 2);
                            dest[j + 3] = -1;
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGRX5551) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int blue = src & 31;
                            int green = src >> 5 & 31;
                            int red = src >>> 10 & 31;
                            dest[j + 0] = (byte) (red << 3 | red >> 2);
                            dest[j + 1] = (byte) (green << 3 | green >> 2);
                            dest[j + 2] = (byte) (blue << 3 | blue >> 2);
                            dest[j + 3] = -1;
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGRA5551) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int blue = src & 31;
                            int green = src >> 5 & 31;
                            int red = src >>> 10 & 31;
                            int alpha = (int) ((long) src & 32768L);
                            dest[j + 0] = (byte) (red << 3 | red >> 2);
                            dest[j + 1] = (byte) (green << 3 | green >> 2);
                            dest[j + 2] = (byte) (blue << 3 | blue >> 2);
                            if (alpha == 0) {
                                dest[j + 3] = 0;
                            } else {
                                dest[j + 3] = -1;
                            }

                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.BGRA4444) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int blue = src & 15;
                            int green = src >> 4 & 15;
                            int red = src >> 8 & 15;
                            int alpha = src >>> 12 & 15;
                            dest[j + 0] = (byte) (red << 4 | red >> 4);
                            dest[j + 1] = (byte) (green << 4 | green >> 4);
                            dest[j + 2] = (byte) (blue << 4 | blue >> 4);
                            dest[j + 3] = (byte) (alpha << 4 | alpha >> 4);
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.UV88) {
                        int end = mwidth * mheight * 2;
                        int j = 0;

                        for (int i = 0; i < end; i += 2) {
                            dest[j + 0] = data[i];
                            dest[j + 1] = data[i + 1];
                            dest[j + 2] = 0;
                            dest[j + 3] = -1;
                            j += 4;
                        }

                        return dest;
                    } else if (format == VtfImageFormat.RGBA16161616) {
                        int end = mwidth * mheight * 8;
                        int j = 0;

                        for (int i = 0; i < end; i += 8) {
                            int red = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int green = (255 & data[i + 3]) * 256 + (255 & data[i + 2]);
                            int blue = (255 & data[i + 5]) * 256 + (255 & data[i + 4]);
                            int alpha = (255 & data[i + 7]) * 256 + (255 & data[i + 6]);
                            dest[j + 0] = (byte) (red >>> 8);
                            dest[j + 1] = (byte) (green >>> 8);
                            dest[j + 2] = (byte) (blue >>> 8);
                            dest[j + 3] = (byte) (alpha >>> 8);
                            j += 4;
                        }

                        return dest;
                    } else if (format != VtfImageFormat.RGBA16161616F) {
                        System.out.println("Vtf: Unsupported format " + format.getName());
                        return dest;
                    } else {
                        int end = mwidth * mheight * 8;
                        int j = 0;

                        for (int i = 0; i < end; i += 8) {
                            int red = (255 & data[i + 1]) * 256 + (255 & data[i]);
                            int green = (255 & data[i + 3]) * 256 + (255 & data[i + 2]);
                            int blue = (255 & data[i + 5]) * 256 + (255 & data[i + 4]);
                            int alpha = (255 & data[i + 7]) * 256 + (255 & data[i + 6]);
                            red = calculateHDRScale(red, gamma, brightness);
                            green = calculateHDRScale(green, gamma, brightness);
                            blue = calculateHDRScale(blue, gamma, brightness);
                            dest[j + 0] = (byte) (red >>> 8);
                            dest[j + 1] = (byte) (green >>> 8);
                            dest[j + 2] = (byte) (blue >>> 8);
                            dest[j + 3] = (byte) (alpha >>> 8);
                            j += 4;
                        }

                        return dest;
                    }
                } else {
                    int end = mwidth * mheight * 4;

                    for (int i = 0; i < end; ++i) {
                        dest[i] = data[i];
                    }

                    return dest;
                }
            }
        } else {
            return decompDXT1(data, mwidth, mheight);
        }
    }

    private static int calculateHDRScale(int chan, double gamma, double brightness) {
        int out = (int) (Math.pow((double) ((float) chan / 65535.0F), gamma) * (double) 65535.0F * brightness);
        if (out > 65535) {
            out = 65535;
        }

        return out;
    }

    private static byte[] decompDXT1(byte[] data, int mwidth, int mheight) {
        int destsize = calcSize(mwidth, mheight, VtfImageFormat.RGBA8888);
        byte[] dest = new byte[destsize];
        int bpp = 4;
        int bps = bpp * mwidth;
        ColRGBA8888[] colours = new ColRGBA8888[4];

        int index = 0;

        for (int y = 0; y < mheight; y += 4) {
            for (int x = 0; x < mwidth; x += 4) {
                colours[0] = ColRGBA8888.from565(data[index], data[index + 1]);
                colours[1] = ColRGBA8888.from565(data[index + 2], data[index + 3]);
                int bitmask = toInt(data, index + 4);
                index += 8;
                if (colours[0].c565() > colours[1].c565()) {
                    {
                        int b = (2 * colours[0].b() + colours[1].b() + 1) / 3;
                        int g = (2 * colours[0].g() + colours[1].g() + 1) / 3;
                        int r = (2 * colours[0].r() + colours[1].r() + 1) / 3;
                        int a = -1;
                        colours[2] = new ColRGBA8888(r, g, b, a, 0);
                    }

                    {
                        int b = (colours[0].b() + 2 * colours[1].b() + 1) / 3;
                        int g = (colours[0].g() + 2 * colours[1].g() + 1) / 3;
                        int r = (colours[0].r() + 2 * colours[1].r() + 1) / 3;
                        int a = -1;
                        colours[3] = new ColRGBA8888(r, g, b, a, 0);
                    }

                } else {
                    {
                        int b = (colours[0].b() + colours[1].b()) / 2;
                        int g = (colours[0].g() + colours[1].g()) / 2;
                        int r = (colours[0].r() + colours[1].r()) / 2;
                        int a = -1;
                        colours[2] = new ColRGBA8888(r, g, b, a, 0);
                    }

                    {
                        int b = (colours[0].b() + 2 * colours[1].b() + 1) / 3;
                        int g = (colours[0].g() + 2 * colours[1].g() + 1) / 3;
                        int r = (colours[0].r() + 2 * colours[1].r() + 1) / 3;
                        int a = 0;
                        colours[3] = new ColRGBA8888(r, g, b, a, 0);
                    }
                }

                int k = 0;

                for (int j = 0; j < 4; ++j) {
                    for (int var19 = 0; var19 < 4; ++var19) {
                        int select = (bitmask & 3 << k * 2) >>> k * 2;
                        ColRGBA8888 col = colours[select];
                        if (x + var19 < mwidth && y + j < mheight) {
                            int offset = (y + j) * bps + (x + var19) * bpp;
                            dest[offset + 0] = toByte(col.r());
                            dest[offset + 1] = toByte(col.g());
                            dest[offset + 2] = toByte(col.b());
                            dest[offset + 3] = toByte(col.a());
                        }

                        ++k;
                    }
                }
            }
        }

        return dest;
    }

    private static byte[] decompDXT5(byte[] data, int width, int height) {
        int destsize = calcSize(width, height, VtfImageFormat.RGBA8888);
        byte[] dest = new byte[destsize];
        int bpp = 4;
        int bps = bpp * width;
        int[] alphas = new int[8];
        ColRGBA8888[] colours = new ColRGBA8888[4];

        int index = 0;

        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                alphas[0] = data[index] & 255;
                alphas[1] = data[index + 1] & 255;
                int alphamask0 = toInt3(data, index + 2);
                int alphamask1 = toInt3(data, index + 5);
                index += 8;
                colours[0] = ColRGBA8888.from565(data[index], data[index + 1]);
                colours[1] = ColRGBA8888.from565(data[index + 2], data[index + 3]);
                int bitmask = toInt(data, index + 4);
                index += 8;

                {
                    int b = (2 * colours[0].b() + colours[1].b() + 1) / 3;
                    int g = (2 * colours[0].g() + colours[1].g() + 1) / 3;
                    int r = (2 * colours[0].r() + colours[1].r() + 1) / 3;
                    int a = -1;
                    colours[2] = new ColRGBA8888(r, g, b, a, 0);
                }

                {
                    int b = (colours[0].b() + 2 * colours[1].b() + 1) / 3;
                    int g = (colours[0].g() + 2 * colours[1].g() + 1) / 3;
                    int r = (colours[0].r() + 2 * colours[1].r() + 1) / 3;
                    int a = -1;
                    colours[3] = new ColRGBA8888(r, g, b, a, 0);
                }

                int k = 0;

                for (int j = 0; j < 4; ++j) {
                    for (int var23 = 0; var23 < 4; ++var23) {
                        int select = (bitmask & 3 << k * 2) >>> k * 2;
                        ColRGBA8888 col = colours[select];
                        if (x + var23 < width && y + j < height) {
                            int offset = (y + j) * bps + (x + var23) * bpp;
                            dest[offset + 0] = toByte(col.r());
                            dest[offset + 1] = toByte(col.g());
                            dest[offset + 2] = toByte(col.b());
                        }

                        ++k;
                    }
                }

                if (alphas[0] > alphas[1]) {
                    alphas[2] = (6 * alphas[0] + 1 * alphas[1] + 3) / 7;
                    alphas[3] = (5 * alphas[0] + 2 * alphas[1] + 3) / 7;
                    alphas[4] = (4 * alphas[0] + 3 * alphas[1] + 3) / 7;
                    alphas[5] = (3 * alphas[0] + 4 * alphas[1] + 3) / 7;
                    alphas[6] = (2 * alphas[0] + 5 * alphas[1] + 3) / 7;
                    alphas[7] = (1 * alphas[0] + 6 * alphas[1] + 3) / 7;
                } else {
                    alphas[2] = (4 * alphas[0] + 1 * alphas[1] + 2) / 5;
                    alphas[3] = (3 * alphas[0] + 2 * alphas[1] + 2) / 5;
                    alphas[4] = (2 * alphas[0] + 3 * alphas[1] + 2) / 5;
                    alphas[5] = (1 * alphas[0] + 4 * alphas[1] + 2) / 5;
                    alphas[6] = 0;
                    alphas[7] = 255;
                }

                int bits = alphamask0;

                for (int var26 = 0; var26 < 2; ++var26) {
                    for (int var24 = 0; var24 < 4; ++var24) {
                        if (x + var24 < width && y + var26 < height) {
                            int offset = (y + var26) * bps + (x + var24) * bpp + 3;
                            dest[offset] = (byte) alphas[bits & 7];
                        }

                        bits >>= 3;
                    }
                }

                bits = alphamask1;

                for (int var27 = 2; var27 < 4; ++var27) {
                    for (int var25 = 0; var25 < 4; ++var25) {
                        if (x + var25 < width && y + var27 < height) {
                            int offset = (y + var27) * bps + (x + var25) * bpp + 3;
                            dest[offset] = (byte) alphas[bits & 7];
                        }

                        bits >>= 3;
                    }
                }
            }
        }

        return dest;
    }

    private static int toInt(byte[] data, int index) {
        int ret = (255 & data[index + 3]) << 24;
        ret |= (255 & data[index + 2]) << 16;
        ret |= (255 & data[index + 1]) << 8;
        ret |= 255 & data[index + 0];
        return ret;
    }

    private static int toInt3(byte[] data, int index) {
        int ret = (255 & data[index + 2]) << 16;
        ret |= (255 & data[index + 1]) << 8;
        ret |= 255 & data[index + 0];
        return ret;
    }

    private static byte toByte(int in) {
        return (byte) (in & 255);
    }

    private byte[] getData(int frame, int face, int mipLevel) {
        int dwidth = this.getWidth(mipLevel);
        int dheight = this.getHeight(mipLevel);
        int dlength = calcSize(dwidth, dheight, this.imageFormat);
        int doffset = this.getOffset(frame, face, mipLevel);
        byte[] databuff = new byte[dlength];

        for (int i = 0; i < dlength; ++i) {
            databuff[i] = this.buffer[i + doffset];
        }

        return databuff;
    }

    private int getOffset(int frame, int face, int mipLevel) {
        int offset = 0;
        int frameCount = this.numberOfFrames;
        int faceCount = this.getFaceCount();
        int mipCount = this.numberOfMipMaps;
        if (frame >= frameCount) {
            frame = frameCount - 1;
        }

        if (face >= faceCount) {
            face = faceCount - 1;
        }

        if (mipLevel >= mipCount) {
            mipLevel = mipCount - 1;
        }

        for (int i = mipCount - 1; i > mipLevel; --i) {
            offset += frameCount * faceCount * calcSize(this.getWidth(i), this.getHeight(i), this.imageFormat);
        }

        int temp = calcSize(this.getWidth(mipLevel), this.getHeight(mipLevel), this.imageFormat);
        offset += temp * (frame * faceCount + face);
        return offset;
    }

    public int getWidth(int mipLevel) {
        if (mipLevel >= this.numberOfMipMaps) {
            return 0;
        } else {
            int mwidth = this.width;

            for (int i = 0; i < mipLevel; ++i) {
                mwidth >>= 1;
                if (mwidth < 1) {
                    mwidth = 1;
                }
            }

            return mwidth;
        }
    }

    public int getHeight(int mipLevel) {
        if (mipLevel >= this.numberOfMipMaps) {
            return 0;
        } else {
            int mheight = this.height;

            for (int i = 0; i < mipLevel; ++i) {
                mheight >>= 1;
                if (mheight < 1) {
                    mheight = 1;
                }
            }

            return mheight;
        }
    }

    public int getFaceCount() {
        return calculateFaceCount(this.startFrame, this.flags);
    }

    private static int calculateFaceCount(int startFrame, int flags) {
        if (isEnvmap(flags)) {
            return startFrame == -1 ? 6 : 7;
        } else {
            return 1;
        }
    }

    private static boolean isEnvmap(int flags) {
        return (flags & TEXTUREFLAGS_ENVMAP) != 0;
    }

    private static int calcSize(int width, int height, int numberOfMipMaps, VtfImageFormat format) {
        int size = 0;
        if (height != 0 && width != 0) {
            for (int i = 0; i < numberOfMipMaps; ++i) {
                size += calcSize(width, height, format);
                width >>= 1;
                height >>= 1;
                if (width < 1) {
                    width = 1;
                }

                if (height < 1) {
                    height = 1;
                }
            }

            return size;
        } else {
            return -1;
        }
    }

    private static int calcSize(int width, int height, VtfImageFormat format) {
        switch (format) {
            case DXT1, DXT1_ONEBITALPHA -> {
                if (width < 4 && width > 0) {
                    width = 4;
                }

                if (height < 4 && height > 0) {
                    height = 4;
                }

                return (width + 3) / 4 * ((height + 3) / 4) * 8;
            }
            case DXT3, DXT5 -> {
                if (width < 4 && width > 0) {
                    width = 4;
                }

                if (height < 4 && height > 0) {
                    height = 4;
                }

                return (width + 3) / 4 * ((height + 3) / 4) * 16;
            }
            default -> {
                return width * height * format.getPixelSizeInBytes();
            }
        }
    }

    public int[] getVersion() {
        return version;
    }

    public boolean isValid() {
        return valid;
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public int getFlags() {
        return flags;
    }

    public VtfImageFormat getImageFormat() {
        return imageFormat;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public float getReflectivityX() {
        return reflectivityX;
    }

    public float getReflectivityY() {
        return reflectivityY;
    }

    public float getReflectivityZ() {
        return reflectivityZ;
    }

    public float getBumpScale() {
        return bumpScale;
    }

    public int getNumberOfMipMaps() {
        return numberOfMipMaps;
    }

    public VtfImageFormat getLowResImageFormat() {
        return lowResImageFormat;
    }

    public short getLowResWidth() {
        return lowResWidth;
    }

    public short getLowResHeight() {
        return lowResHeight;
    }

    public byte[] getLowResBuffer() {
        return lowResBuffer;
    }

    public byte[] getBuffer() {
        return buffer;
    }
}
