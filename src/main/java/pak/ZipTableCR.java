package pak;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class ZipTableCR extends DefaultTableCellRenderer {
    ZipTableCR() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int col) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        if (value instanceof String && col == 1 && !isSelected) {
            if ((Boolean) table.getValueAt(row, 0)) {
                cell.setForeground(table.getForeground());
            } else {
                cell.setForeground(Color.blue);
            }
        }

        return cell;
    }
}
