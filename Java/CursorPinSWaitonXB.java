/*
 *  CursorPinSWaitonXB.java        17.44 24/11/10
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
 * 09/09/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * Show long operations
 */
public class CursorPinSWaitonXB extends RichButton {
  boolean showSQL = false;


  /**
   * Constructor 
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public CursorPinSWaitonXB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
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
      if (ConsoleWindow.getDBVersion() >= 11.0) {
        Cursor myCursor = new Cursor("cursorPinSWaitonX11g.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "s";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 10.0) {
          String message = "This button is only relevant to databases prior to 11g.  From 11g the blocking session can be identified directly from v$session.\n\n" +
            "*** The output of this button is relevant to this instance you connected too, not to any instances you specified by the instance menu ****";

          if (!ConnectWindow.isCursorWarningAlreadyGiven())
            JOptionPane.showMessageDialog(scrollP, message, "Information", JOptionPane.INFORMATION_MESSAGE);

          Cursor myCursor = new Cursor("cursorPinSWaitonX.sql",true);
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

          ConnectWindow.createCursorWarningFile();
        }
        else {
          String message = "This button is only relevant to databases prior to 11g.  From 11g the blocking session can be identified directly from v$session.\n\n" +
            "*** The output of this button is relevant to this instance you connected too, not to any instances you specified by the instance menu ****";

          if (!ConnectWindow.isCursorWarningAlreadyGiven())
            JOptionPane.showMessageDialog(scrollP, message, "Information", JOptionPane.INFORMATION_MESSAGE);

          Cursor myCursor = new Cursor("cursorPinSWaitonX9.sql",true);
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

          ConnectWindow.createCursorWarningFile();
        }
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}