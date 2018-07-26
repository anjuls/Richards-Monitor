/*
 *  ForwardB.java        1.0 18/02/05
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
 * 14/09/06 Richard Wright Changed the order of events in actionPerformed
 * 07/09/07 Richard Wright Changed sqlTA to sqlTP (JTextArea to JTextPane)
 * 14/10/14 Richard Wright Integrated iterationsL for the scratchPanel
 */


package RichMon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 * Retrieves from the resultCache the next resultset in chronological order
 */
public class ForwardB extends JButton {
  JLabel statusBar;     // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;  // The JScrollPane on which output should be displayed 
  ResultCache resultCache;
  SQLCache sqlCache;
  JTextPane sqlTA;

  JLabel iterationsL;
  boolean changeIterationsLabel = false;
  
  /**
   * Constructor
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public ForwardB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }


  /**
   * Constructor
   * 
   * @param buttonName
   * @param scrollP
   * @param sqlTA
   * @param statusBar
   * @param resultCache
   * @param sqlCache
   */
  public ForwardB(String buttonName, JScrollPane scrollP, JTextPane sqlTA, JLabel statusBar, ResultCache resultCache, SQLCache sqlCache) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    this.sqlCache = sqlCache;
    this.sqlTA = sqlTA;
  }  
  
  public ForwardB(String buttonName, JScrollPane scrollP, JTextPane sqlTA, JLabel statusBar, ResultCache resultCache, SQLCache sqlCache, JLabel iterationsL) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    this.sqlCache = sqlCache;
    this.sqlTA = sqlTA;
    
    if (iterationsL instanceof JLabel) {
      changeIterationsLabel=true;
      this.iterationsL = iterationsL;
    }
  }


  /**
   * Perform the use selected action
   * 
   * @param tearOff
   */
  public void actionPerformed() {
    QueryResult myResult;
    try {
      if (sqlCache instanceof SQLCache) sqlTA.setText(sqlCache.getNextSQL());

      myResult = resultCache.getNextResult();
      ExecuteDisplay.displayTable(myResult, scrollP, false, statusBar);
      
      if (changeIterationsLabel) {
        int i = myResult.getIteration();
        iterationsL.setText("Iteration " + Integer.toString(i));
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}