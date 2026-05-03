package pak;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Cons {
   private static JFrame console;
   private static JTextArea text;
   private static boolean window;

   public Cons() {
   }
   
   static void open(boolean iswindowed) {
      window = iswindowed;
      if (window) {
         console = new JFrame();
         text = new JTextArea();
         text.setEditable(false);
         text.setRows(20);
         text.setColumns(72);
         console.getContentPane().add(new JScrollPane(text), "Center");
         console.setDefaultCloseOperation(1);
         text.setFont(new Font("Dialog", 0, 11));
         console.pack();
         console.setVisible(true);
      }

   }

   static void show() {
      console.setVisible(true);
   }

   static void hide() {
      console.setVisible(false);
   }

   static void focus() {
      console.toFront();
   }

   static void close() {
      if (window) {
         console.setVisible(false);
      }

      console = null;
      text = null;
   }

   static void settitle(String title) {
      if (window) {
         console.setTitle(title);
      }

   }

   static void print(String str) {
      if (!window) {
         System.out.print(str);
      } else {
         text.append(str);
         text.setCaretPosition(text.getDocument().getLength());
         int idealSize = 16384;
         int maxExcess = 512;
         int excess = text.getDocument().getLength() - idealSize;
         if (excess >= maxExcess) {
            text.replaceRange("", 0, excess);
         }

      }
   }

   static void println(String str) {
      print(str + System.lineSeparator());
   }

   static void println() {
      print(System.lineSeparator());
   }

   static void print(Object obj) {
      print(String.valueOf(obj));
   }

   static void println(Object obj) {
      print(obj);
      println();
   }

   static JFrame getConsole() {
      return console;
   }
}
