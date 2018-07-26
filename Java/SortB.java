/*
 * SortB.java        1.0 18/02/05
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
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 30/04/10 Richard Wright Extend RichButton
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements a query to show all current sort operations.
 */
public class SortB extends RichButton {


  /**
   * Constuctor
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public SortB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
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
    try {  
      Cursor myCursor = new Cursor("databaseSort.sql",true);
      Parameters myPars = new Parameters();
      myPars.addParameter("String","%");
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "u";
      String filterByUserAlias = "s";
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