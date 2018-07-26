/*
 *  EndCFContentionB.java        17.47 08/12/10
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
 */


package RichMon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Show long operations
 */
public class EnqCFContentionB extends RichButton {
  boolean showSQL = false;

  /**
   * Constructor 
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public EnqCFContentionB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
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
      Cursor myCursor = new Cursor("enqCFContention.sql",true);
      Parameters myPars = new Parameters();
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
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