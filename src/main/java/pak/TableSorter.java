package pak;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableSorter extends AbstractTableModel {
   protected TableModel tableModel;
   public static final int DESCENDING = -1;
   public static final int NOT_SORTED = 0;
   public static final int ASCENDING = 1;
   private static Directive EMPTY_DIRECTIVE = new Directive(-1, 0);

   public static final Comparator<Comparable<Object>> COMPARABLE_COMAPRATOR = (o1, o2) -> {
      return o1.compareTo(o2);
   };

   public static final Comparator<Object> LEXICAL_COMPARATOR = (o1, o2) -> {
      return o1.toString().compareTo(o2.toString());
   };

   private Row[] viewToModel;
   private int[] modelToView;
   private JTableHeader tableHeader;
   private MouseListener mouseListener;
   private TableModelListener tableModelListener;
   private List<Directive> sortingColumns;

   public TableSorter() {
      this.sortingColumns = new ArrayList<>();
      this.mouseListener = new MouseHandler();
      this.tableModelListener = new TableModelHandler();
   }

   public TableSorter(TableModel tableModel) {
      this();
      this.setTableModel(tableModel);
   }

   public TableSorter(TableModel tableModel, JTableHeader tableHeader) {
      this();
      this.setTableHeader(tableHeader);
      this.setTableModel(tableModel);
   }

   private void clearSortingState() {
      this.viewToModel = null;
      this.modelToView = null;
   }

   public TableModel getTableModel() {
      return this.tableModel;
   }

   public void setTableModel(TableModel tableModel) {
      if (this.tableModel != null) {
         this.tableModel.removeTableModelListener(this.tableModelListener);
      }

      this.tableModel = tableModel;
      if (this.tableModel != null) {
         this.tableModel.addTableModelListener(this.tableModelListener);
      }

      this.clearSortingState();
      this.fireTableStructureChanged();
   }

   public JTableHeader getTableHeader() {
      return this.tableHeader;
   }

   public void setTableHeader(JTableHeader tableHeader) {
      if (this.tableHeader != null) {
         this.tableHeader.removeMouseListener(this.mouseListener);
         TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
         if (defaultRenderer instanceof SortableHeaderRenderer) {
            this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
         }
      }

      this.tableHeader = tableHeader;
      if (this.tableHeader != null) {
         this.tableHeader.addMouseListener(this.mouseListener);
         this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
      }

   }

   public boolean isSorting() {
      return this.sortingColumns.size() != 0;
   }

   private Directive getDirective(int column) {
      for (int i = 0; i < this.sortingColumns.size(); ++i) {
         Directive directive = (Directive) this.sortingColumns.get(i);
         if (directive.column == column) {
            return directive;
         }
      }

      return EMPTY_DIRECTIVE;
   }

   public int getSortingStatus(int column) {
      return this.getDirective(column).direction;
   }

   private void sortingStatusChanged() {
      this.clearSortingState();
      this.fireTableDataChanged();
      if (this.tableHeader != null) {
         this.tableHeader.repaint();
      }

   }

   public void setSortingStatus(int column, int status) {
      Directive directive = this.getDirective(column);
      if (directive != EMPTY_DIRECTIVE) {
         this.sortingColumns.remove(directive);
      }

      if (status != 0) {
         this.sortingColumns.add(new Directive(column, status));
      }

      this.sortingStatusChanged();
   }

   protected Icon getHeaderRendererIcon(int column, int size) {
      Directive directive = this.getDirective(column);
      return directive == EMPTY_DIRECTIVE ? null
            : new Arrow(directive.direction == -1, size, this.sortingColumns.indexOf(directive));
   }

   public void cancelSorting() {
      this.sortingColumns.clear();
      this.sortingStatusChanged();
   }

   protected Comparator<? extends Object> getComparator(int column) {
      Class<?> columnType = this.tableModel.getColumnClass(column);
      return Comparable.class.isAssignableFrom(columnType) ? COMPARABLE_COMAPRATOR : LEXICAL_COMPARATOR;
   }

   private Row[] getViewToModel() {
      if (this.viewToModel == null) {
         int tableModelRowCount = this.tableModel.getRowCount();
         this.viewToModel = new Row[tableModelRowCount];

         for (int row = 0; row < tableModelRowCount; ++row) {
            this.viewToModel[row] = new Row(row);
         }

         if (this.isSorting()) {
            Arrays.sort(this.viewToModel);
         }
      }

      return this.viewToModel;
   }

   public int viewIndex(int modelIndex) {
      return this.getModelToView()[modelIndex];
   }

   public int modelIndex(int viewIndex) {
      return this.getViewToModel()[viewIndex].modelIndex;
   }

   public int[] modelIndex(int[] viewIndices) {
      int[] map = new int[viewIndices.length];
      Row[] vtm = this.getViewToModel();

      for (int i = 0; i < map.length; ++i) {
         map[i] = vtm[viewIndices[i]].modelIndex;
      }

      return map;
   }

   private int[] getModelToView() {
      if (this.modelToView == null) {
         int n = this.getViewToModel().length;
         this.modelToView = new int[n];

         for (int i = 0; i < n; this.modelToView[this.modelIndex(i)] = i++) {
         }
      }

      return this.modelToView;
   }

   @Override
   public int getRowCount() {
      return this.tableModel == null ? 0 : this.tableModel.getRowCount();
   }

   @Override
   public int getColumnCount() {
      return this.tableModel == null ? 0 : this.tableModel.getColumnCount();
   }

   @Override
   public String getColumnName(int column) {
      return this.tableModel.getColumnName(column);
   }

   @Override
   public Class<?> getColumnClass(int column) {
      return this.tableModel.getColumnClass(column);
   }

   @Override
   public boolean isCellEditable(int row, int column) {
      return this.tableModel.isCellEditable(this.modelIndex(row), column);
   }

   @Override
   public Object getValueAt(int row, int column) {
      return this.tableModel.getValueAt(this.modelIndex(row), column);
   }

   @Override
   public void setValueAt(Object aValue, int row, int column) {
      this.tableModel.setValueAt(aValue, this.modelIndex(row), column);
   }

   private class Row implements Comparable<Row> {
      private int modelIndex;

      public Row(int index) {
         this.modelIndex = index;
      }

      @Override
      public int compareTo(Row o) {
         int row1 = this.modelIndex;
         int row2 = ((Row) o).modelIndex;

         for (Directive directive : TableSorter.this.sortingColumns) {
            int column = directive.column;
            Object o1 = TableSorter.this.tableModel.getValueAt(row1, column);
            Object o2 = TableSorter.this.tableModel.getValueAt(row2, column);
            int comparison = 0;
            if (o1 == null && o2 == null) {
               comparison = 0;
            } else if (o1 == null) {
               comparison = -1;
            } else if (o2 == null) {
               comparison = 1;
            } else {
               @SuppressWarnings("unchecked")
               Comparator<Object> comprator = (Comparator<Object>) TableSorter.this.getComparator(column);
               comparison = comprator.compare(o1, o2);
            }

            if (comparison != 0) {
               return directive.direction == -1 ? -comparison : comparison;
            }
         }

         return 0;
      }
   }

   private class TableModelHandler implements TableModelListener {
      private TableModelHandler() {
      }

      public void tableChanged(TableModelEvent e) {
         if (!TableSorter.this.isSorting()) {
            TableSorter.this.clearSortingState();
            TableSorter.this.fireTableChanged(e);
         } else if (e.getFirstRow() == -1) {
            TableSorter.this.cancelSorting();
            TableSorter.this.fireTableChanged(e);
         } else {
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow() && column != -1 && TableSorter.this.getSortingStatus(column) == 0
                  && TableSorter.this.modelToView != null) {
               int viewIndex = TableSorter.this.getModelToView()[e.getFirstRow()];
               TableSorter.this.fireTableChanged(
                     new TableModelEvent(TableSorter.this, viewIndex, viewIndex, column, e.getType()));
            } else {
               TableSorter.this.clearSortingState();
               TableSorter.this.fireTableDataChanged();
            }
         }
      }
   }

   private class MouseHandler extends MouseAdapter {
      private MouseHandler() {
      }

      public void mouseClicked(MouseEvent e) {
         JTableHeader h = (JTableHeader) e.getSource();
         TableColumnModel columnModel = h.getColumnModel();
         int viewColumn = columnModel.getColumnIndexAtX(e.getX());
         int column = columnModel.getColumn(viewColumn).getModelIndex();
         if (column != -1 && column != 0) {
            int status = TableSorter.this.getSortingStatus(column);
            if (!e.isControlDown()) {
               TableSorter.this.cancelSorting();
            }

            status += e.isShiftDown() ? -1 : 1;
            status = (status + 4) % 3 - 1;
            TableSorter.this.setSortingStatus(column, status);
         }

      }
   }

   private static class Arrow implements Icon {
      private boolean descending;
      private int size;
      private int priority;

      public Arrow(boolean descending, int size, int priority) {
         this.descending = descending;
         this.size = size;
         this.priority = priority;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         Color color = c == null ? Color.GRAY : c.getBackground();
         int dx = (int) ((double) this.size * 0.7 * Math.pow(0.8, (double) this.priority));
         int dy = this.descending ? dx : -dx;
         y = y + 5 * this.size / 6 + (this.descending ? -dy : 0);
         int shift = this.descending ? 1 : -1;
         g.translate(x, y);
         g.setColor(color.darker());
         g.drawLine(dx / 2, dy, 0, 0);
         g.drawLine(dx / 2, dy + shift, 0, shift);
         g.setColor(color.brighter());
         g.drawLine(dx / 2, dy, dx, 0);
         g.drawLine(dx / 2, dy + shift, dx, shift);
         if (this.descending) {
            g.setColor(color.darker().darker());
         } else {
            g.setColor(color.brighter().brighter());
         }

         g.drawLine(dx, 0, 0, 0);
         g.setColor(color);
         g.translate(-x, -y);
      }

      public int getIconWidth() {
         return this.size;
      }

      public int getIconHeight() {
         return this.size;
      }
   }

   private class SortableHeaderRenderer implements TableCellRenderer {
      private TableCellRenderer tableCellRenderer;

      public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
         this.tableCellRenderer = tableCellRenderer;
      }

      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
         Component c = this.tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
               column);
         if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setHorizontalTextPosition(2);
            int modelColumn = table.convertColumnIndexToModel(column);
            l.setIcon(TableSorter.this.getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
         }

         return c;
      }
   }

   private static class Directive {
      private int column;
      private int direction;

      public Directive(int column, int direction) {
         this.column = column;
         this.direction = direction;
      }
   }
}
