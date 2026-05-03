package pak;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class JProgFrame extends JFrame {
   private final JProgressBar progressBar;
   private final Component frame;

   public JProgFrame(JFrame frame, String title) {
      super(title);

      this.setIconImage(frame.getIconImage());
      this.frame = frame;
      this.progressBar = new JProgressBar(0, 0);
      this.progressBar.setStringPainted(true);
      this.progressBar.setString("");
      this.getContentPane().add(this.progressBar);
      this.progressBar.setPreferredSize(new Dimension(420, 24));
      this.setResizable(false);
      this.setDefaultCloseOperation(0);
      this.pack();

      int x = frame.getX() + (frame.getWidth() - this.getWidth()) / 2;
      int y = frame.getY() + (frame.getHeight() - this.getHeight()) / 2;
      this.setLocation(x, y);

      this.setVisible(false);
   }

   public void start(String progstr, boolean hide) {
      this.progressBar.setString(progstr);
      this.setVisible(true);
      this.requestFocus();
      this.progressBar.setValue(0);
      if (hide && this.frame != null) {
         this.frame.setEnabled(false);
      }

   }

   public void end() {
      if (this.frame != null) {
         this.frame.setEnabled(true);
      }

      this.setVisible(false);
      this.dispose();
   }

   public void setString(String progstr) {
      this.progressBar.setString(progstr);
   }

   public void setMaximum(int max) {
      this.progressBar.setMaximum(max);
   }

   public void setValue(int val) {
      this.progressBar.setValue(val);
   }
}
