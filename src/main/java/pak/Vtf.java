package pak;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Vtf {
   static final int IF_RGBA8888 = 0;
   static final int IF_ABGR8888 = 1;
   static final int IF_RGB888 = 2;
   static final int IF_BGR888 = 3;
   static final int IF_RGB565 = 4;
   static final int IF_I8 = 5;
   static final int IF_IA88 = 6;
   static final int IF_P8 = 7;
   static final int IF_A8 = 8;
   static final int IF_RGB888_BS = 9;
   static final int IF_BGR888_BS = 10;
   static final int IF_ARGB8888 = 11;
   static final int IF_BGRA8888 = 12;
   static final int IF_DXT1 = 13;
   static final int IF_DXT3 = 14;
   static final int IF_DXT5 = 15;
   static final int IF_BGRX8888 = 16;
   static final int IF_BGR565 = 17;
   static final int IF_BGRX5551 = 18;
   static final int IF_BGRA4444 = 19;
   static final int IF_DXT1_1BA = 20;
   static final int IF_BGRA5551 = 21;
   static final int IF_UV88 = 22;
   static final int IF_UVWQ8888 = 23;
   static final int IF_RGBA16161616F = 24;
   static final int IF_RGBA16161616 = 25;
   static final String[] imgfmt = new String[]{"RBGA8888", "ABGR8888", "RGB888", "BGR888", "RGB565", "I8", "IA88", "P8", "A8", "RGB888-BS", "BGR888-BS", "ARGB8888", " BGRA8888", "DXT1", "DXT3", "DXT5", "BGRX8888", "BGR565", "BGRX5551", "BGRA4444", "DXT1_1BA", "BGRA5551", "UV88", "UVWQ8888", "RGBA16161616F", "RGBA16161616"};
   static final int[] imgfmtsize = new int[]{4, 4, 3, 3, 2, 1, 2, 1, 1, 3, 3, 4, 4, 0, 0, 0, 4, 2, 2, 2, 0, 2, 2, 4, 8, 8, 4};
   static final String[] flagstr = new String[]{"POINTSAMPLE", "TRILINEAR", "CLAMP-S", "CLAMP-T", "ANISOTROPIC", "HINT-DXT5", "NOCOMPRESS", "NORMAL", "NOMIP", "NOLOD", "MINMIP", "PROC", "1BALPHA", "8BALPHA", "ENVMAP", "RENDERTARGET", "DEPTH-RT", "NODEBUGOVERRIDE", "SINGLECOPY", "1OVERMIPLEVELINALPHA", "PREMULTCOL1OML", "NORMALTODUDV", "ALPHATESTMIPGEN", "NODEPTHBUFF", "NICEFILTERED"};
   static final int TF_ENVMAP = 16384;
   int[] vers = new int[2];
   boolean isValid = false;
   int headersize;
   short width;
   short height;
   int flags;
   int imageformat;
   int numframes;
   int startframe;
   float refx;
   float refy;
   float refz;
   float bumpscale;
   int nummips;
   int lrimageformat;
   short lrwidth;
   short lrheight;
   boolean isLR;
   byte[] lrbuffer;
   byte[] buffer;
   double gamma = (double)1.0F;
   double bright = (double)1.0F;

   public Vtf() {
   }

   public void read(ByteBuffer b, long size) throws IOException {
      this.isValid = false;
      char[] type = new char[4];

      for(int i = 0; i < 4; ++i) {
         type[i] = (char)b.get();
      }

      String tstr = new String(type);
      if (tstr.equals("VTF\u0000")) {
         this.vers[0] = b.getInt();
         this.vers[1] = b.getInt();
         this.headersize = b.getInt();
         this.width = b.getShort();
         this.height = b.getShort();
         this.flags = b.getInt();
         this.numframes = b.getShort();
         this.startframe = b.getShort();
         b.getInt();
         this.refx = b.getFloat();
         this.refy = b.getFloat();
         this.refz = b.getFloat();
         b.getInt();
         this.bumpscale = b.getFloat();
         this.imageformat = b.getInt();
         this.nummips = b.get();
         this.lrimageformat = b.getInt();
         this.lrwidth = (short)b.get();
         this.lrheight = (short)b.get();
         this.isLR = this.lrimageformat != -1;
         int lrbuffsize = 0;
         if (this.isLR) {
            lrbuffsize = this.CalcSize(this.lrwidth, this.lrheight, this.lrimageformat);
         }

         int buffsize = this.CalcSize(this.width, this.height, this.nummips, this.imageformat) * this.GetFaceCount() * this.numframes;
         b.position(this.headersize);
         this.lrbuffer = new byte[lrbuffsize];
         if (this.isLR) {
            b.get(this.lrbuffer);
         }

         this.buffer = new byte[buffsize];
         b.get(this.buffer);
         this.isValid = true;
      }
   }

   public String GetFlagStr() {
      int bflags = this.flags;
      StringBuffer str = new StringBuffer();

      for(int i = 0; i < 25; ++i) {
         if ((bflags & 1) == 1) {
            str.append(flagstr[i]).append(" ");
         }

         bflags >>= 1;
      }

      return str.toString();
   }

   public int[] GetIntARGB(int frame, int face, int miplevel) {
      int[] idata = new int[this.GetWidth(miplevel) * this.GetHeight(miplevel)];
      byte[] data = this.GetRGBA(this.GetData(frame, face, miplevel), this.GetWidth(miplevel), this.GetHeight(miplevel), this.imageformat);
      int a = 0;

      for(int i = 0; i < idata.length; ++i) {
         idata[i] = (data[a + 3] & 255) << 24 | (data[a] & 255) << 16 | (data[a + 1] & 255) << 8 | data[a + 2] & 255;
         a += 4;
      }

      return idata;
   }

   public int[] GetIntCompRGBA(int frame, int face, int miplevel, int component) {
      int[] idata = new int[this.GetWidth(miplevel) * this.GetHeight(miplevel)];
      byte[] data = this.GetRGBA(this.GetData(frame, face, miplevel), this.GetWidth(miplevel), this.GetHeight(miplevel), this.imageformat);
      int a = 0;

      for(int i = 0; i < idata.length; ++i) {
         int alph = data[a + component] & 255;
         idata[i] = alph << 16 | alph << 8 | alph;
         a += 4;
      }

      return idata;
   }

   public byte[] GetRGBA(int frame, int face, int miplevel) {
      return this.GetRGBA(this.GetData(frame, face, miplevel), this.GetWidth(miplevel), this.GetHeight(miplevel), this.imageformat);
   }

   public byte[] GetRGBA(byte[] data, int mwidth, int mheight, int format) {
      int destsize = this.CalcSize(mwidth, mheight, 0);
      if (format != 13 && format != 20) {
         if (format == 15) {
            return this.DecompDXT5(data, mwidth, mheight);
         } else {
            byte[] dest = new byte[destsize];
            if (format == 2) {
               int end = mwidth * mheight * 3;
               int j = 0;

               for(int i = 0; i < end; i += 3) {
                  dest[j] = data[i];
                  dest[j + 1] = data[i + 1];
                  dest[j + 2] = data[i + 2];
                  dest[j + 3] = -1;
                  j += 4;
               }

               return dest;
            } else if (format == 3) {
               int end = mwidth * mheight * 3;
               int j = 0;

               for(int i = 0; i < end; i += 3) {
                  dest[j] = data[i + 2];
                  dest[j + 1] = data[i + 1];
                  dest[j + 2] = data[i + 0];
                  dest[j + 3] = -1;
                  j += 4;
               }

               return dest;
            } else if (format != 0 && format != 23) {
               if (format == 12) {
                  int end = mwidth * mheight * 4;

                  for(int i = 0; i < end; i += 4) {
                     dest[i + 0] = data[i + 2];
                     dest[i + 1] = data[i + 1];
                     dest[i + 2] = data[i + 0];
                     dest[i + 3] = data[i + 3];
                  }

                  return dest;
               } else if (format == 16) {
                  int end = mwidth * mheight * 4;

                  for(int i = 0; i < end; i += 4) {
                     dest[i + 0] = data[i + 2];
                     dest[i + 1] = data[i + 1];
                     dest[i + 2] = data[i + 0];
                     dest[i + 3] = -1;
                  }

                  return dest;
               } else if (format == 11) {
                  int end = mwidth * mheight * 4;

                  for(int i = 0; i < end; i += 4) {
                     dest[i + 0] = data[i + 3];
                     dest[i + 1] = data[i + 2];
                     dest[i + 2] = data[i + 1];
                     dest[i + 3] = data[i + 0];
                  }

                  return dest;
               } else if (format == 8) {
                  int end = mwidth * mheight;
                  int j = 0;

                  for(int i = 0; i < end; ++i) {
                     dest[j + 0] = -1;
                     dest[j + 1] = -1;
                     dest[j + 2] = -1;
                     dest[j + 3] = data[i];
                     j += 4;
                  }

                  return dest;
               } else if (format == 5) {
                  int end = mwidth * mheight;
                  int j = 0;

                  for(int i = 0; i < end; ++i) {
                     dest[j + 0] = data[i];
                     dest[j + 1] = data[i];
                     dest[j + 2] = data[i];
                     dest[j + 3] = -1;
                     j += 4;
                  }

                  return dest;
               } else if (format == 6) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     dest[j + 0] = data[i];
                     dest[j + 1] = data[i];
                     dest[j + 2] = data[i];
                     dest[j + 3] = data[i + 1];
                     j += 4;
                  }

                  return dest;
               } else if (format == 9) {
                  int end = mwidth * mheight * 3;
                  int j = 0;

                  for(int i = 0; i < end; i += 3) {
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
               } else if (format == 10) {
                  int end = mwidth * mheight * 3;
                  int j = 0;

                  for(int i = 0; i < end; i += 3) {
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
               } else if (format == 4) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int red = src & 31;
                     int green = src >> 5 & 63;
                     int blue = src >>> 11 & 31;
                     dest[j + 0] = (byte)(red << 3 | red >> 2);
                     dest[j + 1] = (byte)(green << 2 | green >> 4);
                     dest[j + 2] = (byte)(blue << 3 | blue >> 2);
                     dest[j + 3] = -1;
                     j += 4;
                  }

                  return dest;
               } else if (format == 17) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int blue = src & 31;
                     int green = src >> 5 & 63;
                     int red = src >>> 11 & 31;
                     dest[j + 0] = (byte)(red << 3 | red >> 2);
                     dest[j + 1] = (byte)(green << 2 | green >> 4);
                     dest[j + 2] = (byte)(blue << 3 | blue >> 2);
                     dest[j + 3] = -1;
                     j += 4;
                  }

                  return dest;
               } else if (format == 18) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int blue = src & 31;
                     int green = src >> 5 & 31;
                     int red = src >>> 10 & 31;
                     dest[j + 0] = (byte)(red << 3 | red >> 2);
                     dest[j + 1] = (byte)(green << 3 | green >> 2);
                     dest[j + 2] = (byte)(blue << 3 | blue >> 2);
                     dest[j + 3] = -1;
                     j += 4;
                  }

                  return dest;
               } else if (format == 21) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int blue = src & 31;
                     int green = src >> 5 & 31;
                     int red = src >>> 10 & 31;
                     int alpha = (int)((long)src & 32768L);
                     dest[j + 0] = (byte)(red << 3 | red >> 2);
                     dest[j + 1] = (byte)(green << 3 | green >> 2);
                     dest[j + 2] = (byte)(blue << 3 | blue >> 2);
                     if (alpha == 0) {
                        dest[j + 3] = 0;
                     } else {
                        dest[j + 3] = -1;
                     }

                     j += 4;
                  }

                  return dest;
               } else if (format == 19) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     int src = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int blue = src & 15;
                     int green = src >> 4 & 15;
                     int red = src >> 8 & 15;
                     int alpha = src >>> 12 & 15;
                     dest[j + 0] = (byte)(red << 4 | red >> 4);
                     dest[j + 1] = (byte)(green << 4 | green >> 4);
                     dest[j + 2] = (byte)(blue << 4 | blue >> 4);
                     dest[j + 3] = (byte)(alpha << 4 | alpha >> 4);
                     j += 4;
                  }

                  return dest;
               } else if (format == 22) {
                  int end = mwidth * mheight * 2;
                  int j = 0;

                  for(int i = 0; i < end; i += 2) {
                     dest[j + 0] = data[i];
                     dest[j + 1] = data[i + 1];
                     dest[j + 2] = 0;
                     dest[j + 3] = -1;
                     j += 4;
                  }

                  return dest;
               } else if (format == 25) {
                  int end = mwidth * mheight * 8;
                  int j = 0;

                  for(int i = 0; i < end; i += 8) {
                     int red = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int green = (255 & data[i + 3]) * 256 + (255 & data[i + 2]);
                     int blue = (255 & data[i + 5]) * 256 + (255 & data[i + 4]);
                     int alpha = (255 & data[i + 7]) * 256 + (255 & data[i + 6]);
                     dest[j + 0] = (byte)(red >>> 8);
                     dest[j + 1] = (byte)(green >>> 8);
                     dest[j + 2] = (byte)(blue >>> 8);
                     dest[j + 3] = (byte)(alpha >>> 8);
                     j += 4;
                  }

                  return dest;
               } else if (format != 24) {
                  System.out.println("Vtf: Unsupported format " + imgfmt[format]);
                  return dest;
               } else {
                  int end = mwidth * mheight * 8;
                  int j = 0;

                  for(int i = 0; i < end; i += 8) {
                     int red = (255 & data[i + 1]) * 256 + (255 & data[i]);
                     int green = (255 & data[i + 3]) * 256 + (255 & data[i + 2]);
                     int blue = (255 & data[i + 5]) * 256 + (255 & data[i + 4]);
                     int alpha = (255 & data[i + 7]) * 256 + (255 & data[i + 6]);
                     red = this.HDRScale(red);
                     green = this.HDRScale(green);
                     blue = this.HDRScale(blue);
                     dest[j + 0] = (byte)(red >>> 8);
                     dest[j + 1] = (byte)(green >>> 8);
                     dest[j + 2] = (byte)(blue >>> 8);
                     dest[j + 3] = (byte)(alpha >>> 8);
                     j += 4;
                  }

                  return dest;
               }
            } else {
               int end = mwidth * mheight * 4;

               for(int i = 0; i < end; ++i) {
                  dest[i] = data[i];
               }

               return dest;
            }
         }
      } else {
         return this.DecompDXT1(data, mwidth, mheight);
      }
   }

   public int HDRScale(int chan) {
      int out = (int)(Math.pow((double)((float)chan / 65535.0F), this.gamma) * (double)65535.0F * this.bright);
      if (out > 65535) {
         out = 65535;
      }

      return out;
   }

   public void setHDR(double g, double b) {
      this.gamma = g;
      this.bright = b;
   }

   public byte[] DecompDXT1(byte[] data, int mwidth, int mheight) {
      int destsize = this.CalcSize(mwidth, mheight, 0);
      byte[] dest = new byte[destsize];
      int bpp = 4;
      int bps = bpp * mwidth;
      ColRGBA8888[] colours = new ColRGBA8888[4];

      int index = 0;

      for(int y = 0; y < mheight; y += 4) {
         for(int x = 0; x < mwidth; x += 4) {
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

            for(int j = 0; j < 4; ++j) {
               for(int var19 = 0; var19 < 4; ++var19) {
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

   public byte[] DecompDXT5(byte[] data, int mwidth, int mheight) {
      int destsize = this.CalcSize(mwidth, mheight, 0);
      byte[] dest = new byte[destsize];
      int bpp = 4;
      int bps = bpp * mwidth;
      int[] alphas = new int[8];
      ColRGBA8888[] colours = new ColRGBA8888[4];

      int index = 0;

      for(int y = 0; y < mheight; y += 4) {
         for(int x = 0; x < mwidth; x += 4) {
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

            for(int j = 0; j < 4; ++j) {
               for(int var23 = 0; var23 < 4; ++var23) {
                  int select = (bitmask & 3 << k * 2) >>> k * 2;
                  ColRGBA8888 col = colours[select];
                  if (x + var23 < mwidth && y + j < mheight) {
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

            for(int var26 = 0; var26 < 2; ++var26) {
               for(int var24 = 0; var24 < 4; ++var24) {
                  if (x + var24 < mwidth && y + var26 < mheight) {
                     int offset = (y + var26) * bps + (x + var24) * bpp + 3;
                     dest[offset] = (byte)alphas[bits & 7];
                  }

                  bits >>= 3;
               }
            }

            bits = alphamask1;

            for(int var27 = 2; var27 < 4; ++var27) {
               for(int var25 = 0; var25 < 4; ++var25) {
                  if (x + var25 < mwidth && y + var27 < mheight) {
                     int offset = (y + var27) * bps + (x + var25) * bpp + 3;
                     dest[offset] = (byte)alphas[bits & 7];
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

   public byte[] GetData(int frame, int face, int miplevel) {
      int dwidth = this.GetWidth(miplevel);
      int dheight = this.GetHeight(miplevel);
      int dlength = this.CalcSize(dwidth, dheight, this.imageformat);
      int doffset = this.GetOffset(frame, face, miplevel);
      byte[] databuff = new byte[dlength];

      for(int i = 0; i < dlength; ++i) {
         databuff[i] = this.buffer[i + doffset];
      }

      return databuff;
   }

   public int GetOffset(int frame, int face, int mip) {
      int offset = 0;
      int mframecount = this.numframes;
      int mfacecount = this.GetFaceCount();
      int mmipcount = this.nummips;
      if (frame >= mframecount) {
         frame = mframecount - 1;
      }

      if (face >= mfacecount) {
         face = mfacecount - 1;
      }

      if (mip >= mmipcount) {
         mip = mmipcount - 1;
      }

      for(int i = mmipcount - 1; i > mip; --i) {
         offset += mframecount * mfacecount * this.CalcSize(this.GetWidth(i), this.GetHeight(i), this.imageformat);
      }

      int temp = this.CalcSize(this.GetWidth(mip), this.GetHeight(mip), this.imageformat);
      offset += temp * (frame * mfacecount + face);
      return offset;
   }

   public int GetWidth(int miplev) {
      if (miplev >= this.nummips) {
         return 0;
      } else {
         int mwidth = this.width;

         for(int i = 0; i < miplev; ++i) {
            mwidth >>= 1;
            if (mwidth < 1) {
               mwidth = 1;
            }
         }

         return mwidth;
      }
   }

   public int GetHeight(int miplev) {
      if (miplev >= this.nummips) {
         return 0;
      } else {
         int mheight = this.height;

         for(int i = 0; i < miplev; ++i) {
            mheight >>= 1;
            if (mheight < 1) {
               mheight = 1;
            }
         }

         return mheight;
      }
   }

   public int GetFaceCount() {
      if (this.isEnvmap()) {
         return this.startframe == -1 ? 6 : 7;
      } else {
         return 1;
      }
   }

   public boolean isEnvmap() {
      return (this.flags & 16384) != 0;
   }

   public int CalcSize(int mwidth, int mheight, int mmipmaps, int mformat) {
      int size = 0;
      if (mheight != 0 && mwidth != 0) {
         for(int i = 0; i < mmipmaps; ++i) {
            size += this.CalcSize(mwidth, mheight, mformat);
            mwidth >>= 1;
            mheight >>= 1;
            if (mwidth < 1) {
               mwidth = 1;
            }

            if (mheight < 1) {
               mheight = 1;
            }
         }

         return size;
      } else {
         return -1;
      }
   }

   public int CalcSize(int mwidth, int mheight, int mformat) {
      switch (mformat) {
         case 13:
         case 20:
            if (mwidth < 4 && mwidth > 0) {
               mwidth = 4;
            }

            if (mheight < 4 && mheight > 0) {
               mheight = 4;
            }

            return (mwidth + 3) / 4 * ((mheight + 3) / 4) * 8;
         case 14:
         case 15:
            if (mwidth < 4 && mwidth > 0) {
               mwidth = 4;
            }

            if (mheight < 4 && mheight > 0) {
               mheight = 4;
            }

            return (mwidth + 3) / 4 * ((mheight + 3) / 4) * 16;
         case 16:
         case 17:
         case 18:
         case 19:
         default:
            return mwidth * mheight * imgfmtsize[mformat];
      }
   }
}
