package pak;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import javax.swing.JComponent;

class Jimage extends JComponent {
   private BufferedImage image;
   private BufferedImage viewimage;
   private int owidth;
   private int oheight;
   private int width;
   private int height;

   public Jimage(BufferedImage jimage, int jwidth, int jheight) {
      this.image = jimage;
      this.width = jwidth;
      this.owidth = jwidth;
      this.height = jheight;
      this.oheight = jheight;
      this.setSize(new Dimension(this.width, this.height));
      this.viewimage = this.image;
      this.repaint();
   }

   public void update(BufferedImage jimage, int jwidth, int jheight) {
      this.image = jimage;
      this.owidth = jwidth;
      this.oheight = jheight;
   }

   public void settrans(float scale) {
      BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance((double)scale, (double)scale), (RenderingHints)null);
      this.viewimage = op.filter(this.image, (BufferedImage)null);
      this.width = (int)((float)this.owidth * scale);
      this.height = (int)((float)this.oheight * scale);
      this.revalidate();
      this.repaint();
   }

   @Override
   public void paint(Graphics g) {
      g.drawImage(this.viewimage, 0, 0, this);
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(this.width, this.height);
   }
}
