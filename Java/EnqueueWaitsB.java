/*
 *  EnqueueWaitsB.java        1.0 21/02/05
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
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 08/11/07 Richard Wright Enhanced for RAC
 * 04/05/10 Richard Wright Extend RichButton
 */


package RichMon;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements a query to show a summary of each database session.
 */
public class EnqueueWaitsB extends RichButton {
  boolean showSQL = false;


  /**
   * Constructor 
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public EnqueueWaitsB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action from the sharedServerB JComboBox
   * 
   * @param showSQL
   * @param tearOff
   */
  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    
    try {
      Cursor myCursor = new Cursor("enqueueWaits.sql",true);
      Parameters myPars = new Parameters();
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "sw";
      String filterByUserAlias = "none";
      Boolean restrictRows = false;
      Boolean flip = true;
      Boolean eggTimer = true;
      executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}