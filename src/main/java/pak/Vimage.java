package pak;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class Vimage extends JPanel {
   Vtf v;
   Jimage ji;
   int frame = 0;
   int face = 0;
   int mip = 0;
   int zoom = 2;
   int chan = 0;
   double vgamma = (double)1.5F;
   double vbrite = (double)5.0F;
   String[] zoomstr = new String[]{"25%", "50%", "100%", "200%", "400%", "800%"};
   String[] chanstr = new String[]{"RGB", "RGBA", "Red", "Green", "Blue", "Alpha"};

   public Vimage(Vtf vtf) {
      this.v = vtf;
      this.v.setHDR(this.vgamma, this.vbrite);
      int[] data = this.v.GetIntARGB(0, 0, 0);
      int vwidth = this.v.GetWidth(0);
      int vheight = this.v.GetHeight(0);
      BufferedImage image = new BufferedImage(vwidth, vheight, 1);
      image.setRGB(0, 0, vwidth, vheight, data, 0, vwidth);
      this.ji = new Jimage(image, vwidth, vheight);
      JPanel jp = new JPanel();
      ((FlowLayout)jp.getLayout()).setAlignment(0);
      jp.add(this.ji);
      jp.setBorder(BorderFactory.createEtchedBorder());
      this.setLayout(new BorderLayout());
      this.add(jp, "Center");
      JPanel cp = new JPanel();
      JPanel cpanel = new JPanel();
      cp.add(cpanel);
      this.add(cp, "West");
      final SpinnerNumberModel facemod = new SpinnerNumberModel(0, 0, this.v.GetFaceCount() - 1, 1);
      final SpinnerNumberModel framemod = new SpinnerNumberModel(0, 0, this.v.numframes - 1, 1);
      final SpinnerNumberModel mipmod = new SpinnerNumberModel(0, 0, this.v.nummips - 1, 1);
      final SpinnerNumberModel gammod = new SpinnerNumberModel(this.vgamma, 0.1, (double)3.0F, 0.1);
      final SpinnerNumberModel brimod = new SpinnerNumberModel(this.vbrite, 0.1, (double)40.0F, 0.1);
      JSpinner facespin = new JSpinner(facemod);
      JSpinner framespin = new JSpinner(framemod);
      JSpinner mipspin = new JSpinner(mipmod);
      JSpinner gamspin = new JSpinner(gammod);
      JSpinner brispin = new JSpinner(brimod);
      if (this.v.GetFaceCount() == 1) {
         facespin.setEnabled(false);
      }

      if (this.v.numframes == 1) {
         framespin.setEnabled(false);
      }

      if (this.v.nummips == 1) {
         mipspin.setEnabled(false);
      }

      if (this.v.imageformat != 24) {
         gamspin.setEnabled(false);
         brispin.setEnabled(false);
      }

      final JComboBox<String> zoomcombo = new JComboBox<>(this.zoomstr);
      this.zoom = this.getnicesize(Math.max(vheight, vwidth));
      this.setzoom();
      zoomcombo.setSelectedIndex(this.zoom);
      final JComboBox<String> chancombo = new JComboBox<>(this.chanstr);
      facemod.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent ce) {
            Vimage.this.face = facemod.getNumber().intValue();
            Vimage.this.setimage();
         }
      });
      framemod.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent ce) {
            Vimage.this.frame = framemod.getNumber().intValue();
            Vimage.this.setimage();
         }
      });
      mipmod.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent ce) {
            Vimage.this.mip = mipmod.getNumber().intValue();
            Vimage.this.setimage();
         }
      });
      gammod.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent ce) {
            Vimage.this.vgamma = gammod.getNumber().doubleValue();
            Vimage.this.setimage();
         }
      });
      brimod.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent ce) {
            Vimage.this.vbrite = brimod.getNumber().doubleValue();
            Vimage.this.setimage();
         }
      });
      zoomcombo.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            Vimage.this.zoom = zoomcombo.getSelectedIndex();
            Vimage.this.setzoom();
         }
      });
      chancombo.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            Vimage.this.chan = chancombo.getSelectedIndex();
            Vimage.this.setimage();
         }
      });
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

   public int getnicesize(int height) {
      float[] zfactor = new float[]{0.25F, 0.5F, 1.0F, 2.0F, 4.0F, 8.0F};
      if (height <= 32) {
         return 5;
      } else if (height >= 1024) {
         return 0;
      } else {
         for(int i = 0; i < 5; ++i) {
            if ((float)height * zfactor[i] > 192.0F) {
               return i;
            }
         }

         return 2;
      }
   }

   public void setimage() {
      this.v.setHDR(this.vgamma, this.vbrite);
      int[] data;
      switch (this.chan) {
         case 0:
         case 1:
         default:
            data = this.v.GetIntARGB(this.frame, this.face, this.mip);
            break;
         case 2:
         case 3:
         case 4:
         case 5:
            data = this.v.GetIntCompRGBA(this.frame, this.face, this.mip, this.chan - 2);
      }

      int vwidth = this.v.GetWidth(this.mip);
      int vheight = this.v.GetHeight(this.mip);
      BufferedImage image = new BufferedImage(vwidth, vheight, this.chan == 1 ? 2 : 1);
      image.setRGB(0, 0, vwidth, vheight, data, 0, vwidth);
      this.ji.update(image, vwidth, vheight);
      this.setzoom();
      this.revalidate();
   }

   public void setzoom() {
      float s = 1.0F;
      switch (this.zoom) {
         case 0:
            s = 0.25F;
            break;
         case 1:
            s = 0.5F;
            break;
         case 2:
         default:
            s = 1.0F;
            break;
         case 3:
            s = 2.0F;
            break;
         case 4:
            s = 4.0F;
            break;
         case 5:
            s = 8.0F;
      }

      this.ji.settrans(s);
      this.revalidate();
   }
}
