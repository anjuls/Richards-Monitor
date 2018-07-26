/*
 *  SQLMonitorB.java        1.0 10/10/11
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
 * 05/03/12 Richard Wright Modified to account for different view definitions in 11gR1 and 11gR2
 * 08/09/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Show long operations
 */
public class SQLMonitorB extends RichButton {
  boolean showSQL = false;

  /**
   * Constructor 
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public SQLMonitorB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action 
   * 
   * @param showSQL
   * @param tearOff 
   */
  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    
    try {   
      String cursorId;
      if (ConsoleWindow.getDBVersion() >= 11.2) {
        cursorId = "sqlMonitor112.sql";
      }
      else {
        cursorId = "sqlMonitor11.sql";
      }
      
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "s";
      String filterByUserAlias = "s2";
      Boolean restrictRows = true;
      Boolean flip = true;
      Boolean eggTimer = true;
      executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}