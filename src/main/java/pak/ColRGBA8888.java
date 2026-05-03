package pak;

record ColRGBA8888(int r, int g, int b, int a, int c565) {

   private static ColRGBA8888 from565(int col) {
      int red = col >>> 11 & 31;
      int green = col >>> 5 & 63;
      int blue = col & 31;
      int r = red << 3 | red >> 2;
      int g = green << 2 | green >> 4;
      int b = blue << 3 | blue >> 2;
      int a = -1;
      int c565 = col;
      return new ColRGBA8888(r, g, b, a, c565);
   }

   public static ColRGBA8888 from565(int byte0, int byte1) {
      int c565 = (255 & byte1) * 256 + (255 & byte0);
      return from565(c565);
   }

   @Override
   public String toString() {
      return "[" + Integer.toHexString(this.r) + " " + Integer.toHexString(this.g) + " " + Integer.toHexString(this.b) + "]";
   }
}
