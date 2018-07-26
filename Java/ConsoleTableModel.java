/*
 *  ConsoleTableModel.java        12.15 05/01/04
 *
 * Copyright (c) 2003 - 2010 Richard Wright
 * 5 Hollis Wood Drive, Wrecclesham, Farnham, Surrey.  GU10 4JT
 * All rights reserved.
 *
 * RichMon is a lightweight database monitoring tool.  
 * 
 * Keep up to date with the latest developement at http://richmon.blogspot.com
 * 
 * Report bugs and request new features by email to support@richmon4oracle.com 
 * 
 * Change History since 21/02/05
 * =============================
 * 
 * 21/02/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 */


package RichMon;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class ConsoleTableModel extends AbstractTableModel {

    String[] columnNames;
    Vector data;

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
        return resultSetRow.elementAt(col);
    }

/*    public Class getColumnClass(int c) {
 *        return getValueAt(0, c).getClass();
 *    }
 */

  public ConsoleTableModel(String[] columnNames,Vector data) {
    this.columnNames = columnNames;
    this.data = data;
  }

  public void updateTable(String[] columnNames,Vector data) {
    this.columnNames = columnNames;
    this.data = data;

    fireTableStructureChanged();
  }

  public void replaceAllColumns(String[] columnNames) {
    this.columnNames = columnNames;
  }
}