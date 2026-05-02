package pak;

class ColRGBA8888 {
   int r;
   int g;
   int b;
   int a;
   int c565;

   ColRGBA8888() {
   }

   public void from565(int col) {
      int red = col >>> 11 & 31;
      int green = col >>> 5 & 63;
      int blue = col & 31;
      this.r = red << 3 | red >> 2;
      this.g = green << 2 | green >> 4;
      this.b = blue << 3 | blue >> 2;
      this.a = -1;
   }

   public void from565(int byte0, int byte1) {
      this.c565 = (255 & byte1) * 256 + (255 & byte0);
      this.from565(this.c565);
   }

   @Override
   public String toString() {
      return "[" + Integer.toHexString(this.r) + " " + Integer.toHexString(this.g) + " " + Integer.toHexString(this.b) + "]";
   }
}
