package RichMon;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

    class TableModel extends AbstractTableModel {
        private String[] columnNames;
        private Vector data;

        public TableModel(String[] columnNames,Vector resultSet) {
          this.columnNames = columnNames;
          this.data = resultSet;
        }
        
        public void updateTable(String[] columnNames,Vector resultSet) {
          this.columnNames = columnNames;
          this.data = resultSet;          
        }
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            Vector resultSetRow = (Vector)data.elementAt(row);
            
            if (resultSetRow.elementAt(col) instanceof Object) {
              return resultSetRow.elementAt(col);
            }
            else {
              return "";
            }
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 2) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
/*        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);

        }*/
    }