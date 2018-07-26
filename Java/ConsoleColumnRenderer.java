/*
 *  ConsoleColumnRenderer.java        12.15 05/01/04
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
 * 21/02/05 Richard Wright Swithed comment style to match that 
 *                         recommended in Sun's own coding conventions.
 */


package RichMon;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ConsoleColumnRenderer extends DefaultTableCellRenderer {
  
  Color foregroundColor;

  public ConsoleColumnRenderer(Color foregroundColor) {
    super();
    this.foregroundColor = foregroundColor;
    

  }
  
  public Component getTableCellRenderComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    cell.setForeground(foregroundColor);

    return cell;
  }
}