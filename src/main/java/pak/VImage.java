package pak;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class VImage extends JPanel {
   private final Vtf vtf;
   private final JImage jImage;
   private int frame = 0;
   private int face = 0;
   private int mip = 0;
   private int zoom = 2;
   private int chan = 0;
   private double gamma = (double)1.5F;
   private double brightness = (double)5.0F;

   private static final List<Float> zooms = List.of(0.25F, 0.5F, 1.0F, 2.0F, 4.0F, 8.0F);
   private static final String[] zoomstr = new String[]{"25%", "50%", "100%", "200%", "400%", "800%"};

   private static final String[] chanstr = new String[]{"RGB", "RGBA", "Red", "Green", "Blue", "Alpha"};

   public VImage(Vtf vtf) {
      this.vtf = vtf;
      this.vtf.setHDR(this.gamma, this.brightness);
      int[] data = this.vtf.GetIntARGB(0, 0, 0);
      int vwidth = this.vtf.GetWidth(0);
      int vheight = this.vtf.GetHeight(0);
      BufferedImage image = new BufferedImage(vwidth, vheight, 1);
      image.setRGB(0, 0, vwidth, vheight, data, 0, vwidth);
      this.jImage = new JImage(image, vwidth, vheight);
      JPanel jp = new JPanel();
      ((FlowLayout)jp.getLayout()).setAlignment(0);
      jp.add(this.jImage);
      jp.setBorder(BorderFactory.createEtchedBorder());
      this.setLayout(new BorderLayout());
      this.add(jp, "Center");
      JPanel cp = new JPanel();
      JPanel cpanel = new JPanel();
      cp.add(cpanel);
      this.add(cp, "West");
      final SpinnerNumberModel facemod = new SpinnerNumberModel(0, 0, this.vtf.GetFaceCount() - 1, 1);
      final SpinnerNumberModel framemod = new SpinnerNumberModel(0, 0, this.vtf.numframes - 1, 1);
      final SpinnerNumberModel mipmod = new SpinnerNumberModel(0, 0, this.vtf.nummips - 1, 1);
      final SpinnerNumberModel gammod = new SpinnerNumberModel(this.gamma, 0.1, (double)3.0F, 0.1);
      final SpinnerNumberModel brimod = new SpinnerNumberModel(this.brightness, 0.1, (double)40.0F, 0.1);
      JSpinner facespin = new JSpinner(facemod);
      JSpinner framespin = new JSpinner(framemod);
      JSpinner mipspin = new JSpinner(mipmod);
      JSpinner gamspin = new JSpinner(gammod);
      JSpinner brispin = new JSpinner(brimod);
      if (this.vtf.GetFaceCount() == 1) {
         facespin.setEnabled(false);
      }

      if (this.vtf.numframes == 1) {
         framespin.setEnabled(false);
      }

      if (this.vtf.nummips == 1) {
         mipspin.setEnabled(false);
      }

      if (this.vtf.imageformat != 24) {
         gamspin.setEnabled(false);
         brispin.setEnabled(false);
      }

      final JComboBox<String> zoomcombo = new JComboBox<>(zoomstr);
      this.zoom = getNiceSize(Math.max(vheight, vwidth));
      this.updateZoom();
      zoomcombo.setSelectedIndex(this.zoom);
      final JComboBox<String> chancombo = new JComboBox<>(chanstr);
      facemod.addChangeListener(ce -> {
            VImage.this.face = facemod.getNumber().intValue();
            VImage.this.setImage();
         }
      );
      framemod.addChangeListener(ce -> {
            VImage.this.frame = framemod.getNumber().intValue();
            VImage.this.setImage();
         }
      );
      mipmod.addChangeListener(ce -> {
            VImage.this.mip = mipmod.getNumber().intValue();
            VImage.this.setImage();
         }
      );
      gammod.addChangeListener(ce -> {
            VImage.this.gamma = gammod.getNumber().doubleValue();
            VImage.this.setImage();
         }
      );
      brimod.addChangeListener(ce -> {
            VImage.this.brightness = brimod.getNumber().doubleValue();
            VImage.this.setImage();
         }
      );
      zoomcombo.addActionListener(ae -> {
            VImage.this.zoom = zoomcombo.getSelectedIndex();
            VImage.this.updateZoom();
         }
      );
      chancombo.addActionListener(ae -> {
            VImage.this.chan = chancombo.getSelectedIndex();
            VImage.this.setImage();
         }
      );
      cpanel.setLayout(new GridLayout(0, 2));
      cpanel.add(new JLabel("Face "));
      cpanel.add(facespin);
      cpanel.add(new JLabel("Frame "));
      cpanel.add(framespin);
      cpanel.add(new JLabel("Mip "));
      cpanel.add(mipspin);
      cpanel.add(new JLabel("Gamma "));
      cpanel.add(gamspin);
      cpanel.add(new JLabel("Brightness "));
      cpanel.add(brispin);
      cpanel.add(new JLabel("Zoom"));
      cpanel.add(zoomcombo);
      cpanel.add(new JLabel("Channel"));
      cpanel.add(chancombo);
      this.repaint();
   }

   private static int getNiceSize(int height) {
      if (height <= 32) {
         return 5;
      } else if (height >= 1024) {
         return 0;
      } else {
         for(int i = 0; i < zooms.size(); ++i) {
            if ((float)height * zooms.get(i) > 192.0F) {
               return i;
            }
         }

         return 2;
      }
   }

   private void setImage() {
      this.vtf.setHDR(this.gamma, this.brightness);
      int[] data;
      switch (this.chan) {
         case 0:
         case 1:
         default:
            data = this.vtf.GetIntARGB(this.frame, this.face, this.mip);
            break;
         case 2:
         case 3:
         case 4:
         case 5:
            data = this.vtf.GetIntCompRGBA(this.frame, this.face, this.mip, this.chan - 2);
      }

      int vwidth = this.vtf.GetWidth(this.mip);
      int vheight = this.vtf.GetHeight(this.mip);
      BufferedImage image = new BufferedImage(vwidth, vheight, this.chan == 1 ? 2 : 1);
      image.setRGB(0, 0, vwidth, vheight, data, 0, vwidth);
      this.jImage.update(image, vwidth, vheight);
      this.updateZoom();
      this.revalidate();
   }

   private void updateZoom() {
      float s = 1.0F;
      
      if (this.zoom >= 0 && this.zoom < zooms.size()) {
         s = zooms.get(this.zoom);
      }
      

      this.jImage.setScale(s);
      this.revalidate();
   }
}
