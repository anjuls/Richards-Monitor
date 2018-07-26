/*
 *  CacheBufferChainsB.java        12.16 21/02/05
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
 * 06/06/05 Richard Wright Switched to using ExecuteDisplay and not slipping the 
 *                         result set
 * 08/06/05 Richard Wright Added LogWriter Output
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 07/11/06 Richard Wright Allowed usage when isDefaultUserNameToOSUser()
 * 08/11/07 Richard Wright Enhanced for RAC
 * 03/12/08 Richard Wright Renamed from CacheBufferChains to CacheBuffersChains
 * 03/12/08 Richard Wright Fixed a problem with the sql refering to 'library cache' latch instead of 'cache buffers chains'
 * 04/05/10 Richard Wright Extend RichButton
 * 08/09/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * Implements queries to show the most hot blocks.
 */
public class CacheBuffersChainsB extends RichButton {

  /**
   * Constructor 
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public CacheBuffersChainsB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action 
   * 
   * @param showSQL 
   */
  public void actionPerformed(boolean showSQL) {
    try {
      if (showSQL) throw new CannotShowSQLException("Cannot Show this SQL as it's multi step.  See Note 163424.1 in Metalink");
      
      if (ConnectWindow.isSysdba() || Properties.isDefaultUserNameToOSUser()) {
        if (!ConsoleWindow.isOnlyLocalInstanceSelected()) {
          String msg = "You have selected multiple instances, however this function will only operate on this instance.\n\n" +
                       "Start RichMon against each instance to perform the same steps on other instances.";
          JOptionPane.showConfirmDialog(scrollP,msg,"Warning...",JOptionPane.WARNING_MESSAGE);
        }
        
        Parameters myPars = new Parameters();     
        ResultCache noResultCache = null;
        // step 1 - find the latch with the most sleeps
        try {
          Cursor myCursor = new Cursor("cacheBufferChainsPart1.sql",true);
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, noResultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
          
          Vector resultSetRow = myResult.getResultSetRow(0);
          myPars = new Parameters();
          myPars.addParameter("String",resultSetRow.elementAt(0).toString());
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"Step 1");
        }
      
        // step 2
        try {
          Cursor myCursor = new Cursor("cacheBufferChainsPart2.sql",true);
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, noResultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          ExecuteDisplay.displayTable(myResult, scrollP, false, statusBar);
          
          Vector resultSetRow = myResult.getResultSetRow(0);
          myPars = new Parameters();
          myPars.addParameter("String",resultSetRow.elementAt(0).toString());
          myPars.addParameter("String",resultSetRow.elementAt(1).toString());
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"Step 2 - getting to top 10 blocks");
        }
        
        // offer the block details
        String wrn = "This output shows the top 10 busiest blocks.  Do you wish to decode the most busy into a segment name";
        int opt = JOptionPane.showConfirmDialog(scrollP,wrn,"Cache Buffer Chains",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if (opt == 0) {
          try {
            Cursor myCursor = new Cursor("blockDetails.sql",true);
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
            ConsoleWindow.displayError(e,this,"Step 3 - getting block details");
          }
        }
      }
      else {
        throw new InsufficientPrivilegesException("SYSDBA access is required");
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}