/*
 *  ExecuteB.java        1.0 10/05/05
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
 * Change History
 * ==============
 * 
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 07/09/07 Richard Wright Changed sqlTA to sqlTP (JTextArea to JTextPane)
 */


package RichMon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;


/**
 * Re-Formats sql entered onto the scratch panel
 */
public class ReformatSQLB extends JButton {
  ExecutionPlan executionPlan;
  JTextPane sqlTA;      // The source for SQL to be formated 
  SQLFormatter mySQLFormatter;


  /**
   * Constructor 
   * 
   * @param buttonName
   * @param sqlTA
   * @param sqlFormatter
   */
  public ReformatSQLB(String buttonName, JTextPane sqlTA, SQLFormatter sqlFormatter) {
    super(buttonName);
    
    this.sqlTA = sqlTA;
    this.mySQLFormatter = sqlFormatter;
  }

  /**
   * Formats sql
   * 
   */
  public void actionPerformed() {
       if (sqlTA.getText().length() > 0) {
      String[] formattedSQL = mySQLFormatter.formatSQL(sqlTA.getText());
      
      // put the formattedSQL into a single String 
      StringBuffer displaySQL = new StringBuffer();
      
      for (int i=0; i < formattedSQL.length; i++) {
        displaySQL.append(formattedSQL[i]);
        displaySQL.append("\n");
      }
      
      // display the formatted sql 
      sqlTA.setText(displaySQL.toString());
    }
  }
}