package pak;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import javax.swing.JComponent;

class JImage extends JComponent {
   private BufferedImage image;
   private BufferedImage viewImage;
   private int owidth;
   private int oheight;
   private int width;
   private int height;

   public JImage(BufferedImage image, int width, int height) {
      this.image = image;
      this.width = width;
      this.owidth = width;
      this.height = height;
      this.oheight = height;
      this.setSize(new Dimension(this.width, this.height));
      this.viewImage = this.image;
      this.repaint();
   }

   public void update(BufferedImage image, int width, int height) {
      this.image = image;
      this.owidth = width;
      this.oheight = height;
   }

   public void setScale(float scale) {
      BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance((double) scale, (double) scale),
            (RenderingHints) null);
      this.viewImage = op.filter(this.image, (BufferedImage) null);
      this.width = (int) ((float) this.owidth * scale);
      this.height = (int) ((float) this.oheight * scale);
      this.revalidate();
      this.repaint();
   }

   @Override
   public void paint(Graphics g) {
      g.drawImage(this.viewImage, 0, 0, this);
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(this.width, this.height);
   }
}
