package pak;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ScanModel extends AbstractTableModel {
    List<Scanfile> fl;
    static String[] tstr = new String[] { "Unknown", "Material", "Texture", "Model", "Model", "Model", "Model", "Sound",
            "Sound", "Navigation", "AI Node", "Text", "Soundcache" };
    static String[] header = new String[] { "Filename", "Path", "Type", "Location", "Add" };
    static Object[] cols;
    static String[] locstr;

    public ScanModel(List<Scanfile> filelist) {
        this.fl = filelist;
    }

    public void setfilelist(List<Scanfile> filelist) {
        this.fl = filelist;
        this.update();
    }

    public Scanfile getfile(int row) {
        return this.fl != null ? (Scanfile) this.fl.get(row) : null;
    }

    public int getRowCount() {
        return this.fl != null ? this.fl.size() : 0;
    }

    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return cols[col].getClass();
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return this.getfile(row).listname;
            case 1:
                return this.getfile(row).pathname;
            case 2:
                return tstr[this.getfile(row).type];
            case 3:
                return this.getloc(row);
            case 4:
                return this.getfile(row).mark;
            default:
                return null;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 4) {
            this.getfile(row).mark = (Boolean) value;
        }

        this.fireTableDataChanged();
    }

    public String getloc(int row) {
        Scanfile sf = this.getfile(row);
        if (sf.inpak) {
            return locstr[1];
        } else if (sf.inlist) {
            return locstr[2];
        } else {
            return sf.ondisk ? locstr[3] : locstr[0];
        }
    }

    public String getColumnName(int col) {
        return header[col];
    }

    public boolean isCellEditable(int row, int col) {
        if (col != 4) {
            return false;
        } else {
            Scanfile sf = this.getfile(row);
            return sf.onlydisk();
        }
    }

    public int numselected() {
        int count = 0;

        for (int i = 0; i < this.getRowCount(); ++i) {
            if (this.getfile(i).mark) {
                ++count;
            }
        }

        return count;
    }

    public boolean noneselected() {
        for (int i = 0; i < this.getRowCount(); ++i) {
            if (this.getfile(i).mark) {
                return false;
            }
        }

        return true;
    }

    public boolean setallselected() {
        for (int i = 0; i < this.getRowCount(); ++i) {
            Scanfile sf = this.getfile(i);
            if (sf.onlydisk()) {
                sf.mark = true;
            }
        }

        this.fireTableDataChanged();
        return !this.noneselected();
    }

    public void resetallselected() {
        for (int i = 0; i < this.getRowCount(); ++i) {
            Scanfile sf = this.getfile(i);
            if (sf.onlydisk()) {
                sf.mark = false;
            }
        }

        this.fireTableDataChanged();
    }

    public void update() {
        this.fireTableDataChanged();
    }

    static {
        cols = new Object[] { "", "", "", "", Boolean.TRUE };
        locstr = new String[] { "Not found", "In Pak", "In List", "On Disk" };
    }
}
