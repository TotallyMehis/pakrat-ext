package pak;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

class ScanTCBR extends JCheckBox implements TableCellRenderer {
   private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

   public ScanTCBR() {
      this.setHorizontalAlignment(0);
      this.setBorderPainted(true);
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      boolean enabled = false;
      if (table != null) {
         enabled = ((ScanModel)table.getModel()).getfile(row).onlydisk();
         this.setEnabled(enabled);
      }

      if (isSelected) {
         if (enabled) {
            this.setForeground(table.getSelectionForeground());
         } else {
            this.setForeground(Color.lightGray);
         }

         super.setBackground(table.getSelectionBackground());
      } else if (enabled) {
         this.setForeground(table.getForeground());
         this.setBackground(ScanTCR.Color_lightred);
      } else {
         this.setForeground(Color.lightGray);
         this.setBackground(table.getBackground());
      }

      this.setSelected(value != null && (Boolean)value);
      if (hasFocus) {
         this.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      } else {
         this.setBorder(noFocusBorder);
      }

      return this;
   }
}
