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
 * 21/08/06 Richard Wright Modified the comment style and error handling
 * 07/09/07 Richard Wright Changed sqlTA to sqlTP (JTextArea to JTextPane)
 */


package RichMon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;

/**
 * Retrives the execution plan for sql entered onto the scratch panel
 */
public class ExecutionPlanB extends JButton {
  JLabel statusBar;     // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;  // The JScrollPane on which output should be displayed 
  ExecutionPlan executionPlan;
  JTextPane sqlTA;      // The source for SQL to be executed 
  boolean showSQL;


  /**
   * Constructor
   * 
   * @param buttonName
   * @param sqlTA
   * @param scrollP
   * @param statusBar
   * @param executionPlan
   * @param tearOffTB
   */
  public ExecutionPlanB(String buttonName, JTextPane sqlTA, JScrollPane scrollP, JLabel statusBar, ExecutionPlan executionPlan) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.executionPlan = executionPlan;
    this.sqlTA = sqlTA;
  }


  /**
   * Perform the user selected action
   * 
   * @param forceUser
   */
  public void actionPerformed(String forceUser) {
    try {
      if (sqlTA.getText().length() > 0) {
        QueryResult myPlan = executionPlan.runExplainPlan(sqlTA.getText());      

        ExecuteDisplay.displayTable(myPlan, scrollP, showSQL,statusBar);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}