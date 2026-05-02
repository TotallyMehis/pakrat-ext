package pak;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class ScanTCR extends DefaultTableCellRenderer {
   static final Color Color_lightred = new Color(255, 196, 196);
   static final Color Color_lightgreen = new Color(196, 255, 196);

   ScanTCR() {
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
      if (value instanceof String && col == 3 && !isSelected) {
         String vstr = (String)value;

         int i;
         for(i = 0; i < 4 && !vstr.equals(ScanModel.locstr[i]); ++i) {
         }

         Color bcolor = cell.getBackground();
         Color fcolor = cell.getForeground();
         switch (i) {
            case 0:
               fcolor = Color.gray;
               bcolor = Color.white;
               break;
            case 1:
               fcolor = Color.black;
               bcolor = Color_lightgreen;
               break;
            case 2:
               fcolor = Color.blue;
               bcolor = Color.white;
               break;
            case 3:
               bcolor = Color_lightred;
               fcolor = Color.black;
         }

         cell.setBackground(bcolor);
         cell.setForeground(fcolor);
      }

      return cell;
   }
}
