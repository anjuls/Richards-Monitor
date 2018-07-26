/*
 * SharedPoolB.java        1.0 18/02/05
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
 * 31/10/05 Richard Wright Exchanged simple scripts for a table name
 * 01/09/06 Richard Wright Modified the comment style and error handling
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 19/10/07 Richard Wright Enhanced for RAC
 * 04/05/10 Richard Wright Extend RichButton
 * 23/07/10 Richard Wright Added 'number of subpools'
 * 10/07/12 Richard Wright Adding the exception to tell users when sysdba privs are required
 * 24/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;


/**
 * Implements queries related to the shared pool.
 */
public class SharedPoolB extends RichButton {
  boolean showSQL;
  boolean tearOff;

  /**
   * Constructor
   *
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
   public SharedPoolB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Shared Pool");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 9) {
      addItem("Number of Subpools");
      addItem("x$ksmlru");
    }
  }

  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;

    // if a list frame already exists, then remove it
    ConnectWindow.getConsoleWindow().removeLastFrame();

    final JList myList = new JList(options);
    myList.setVisibleRowCount(options.length);
    JScrollPane listScroller = new JScrollPane(myList);
    final JFrame listFrame = new JFrame(this.getText());
    listFrame.add(listScroller);
    listFrame.pack();
    listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    listFrame.setLocationRelativeTo(this);

    myList.addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          listActionPerformed(myList.getSelectedValue(), listFrame);
          listFrame.dispose();
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
      });

    listFrame.setIconImage(ConnectWindow.getRichMonIcon().getImage());
    listFrame.setVisible(true);

    // save a reference to the listFrame so it can be removed if left behind
    ConnectWindow.getConsoleWindow().saveFrameRef(listFrame);
  }

  /**
   * Performs the user selected action
   *
   * @param selectedOption
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    DatabasePanel.setLastAction(selection);

    try {

      if (selection.equals("S/P Summary")) {
        if (ConnectWindow.isSysdba()) {
          Cursor myCursor = new Cursor("sharedPoolSummary.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%");
          Boolean filterByRAC = true;
          Boolean filterByUser = false;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "k";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        } 
        else {
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }

      if (selection.equals("S/P Freelists")) {
        if (ConnectWindow.isSysdba()) {
          Cursor myCursor = new Cursor("sharedPoolFreelists.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%");
          Boolean filterByRAC = true;
          Boolean filterByUser = false;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "k";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        } 
        else {
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }

      if (selection.equals("S/P LRU Stats")) {
        if (ConnectWindow.isSysdba()) {
          Cursor myCursor = new Cursor("sharedPoolLRUStats.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%");
          Boolean filterByRAC = true;
          Boolean filterByUser = false;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "k";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        } 
        else {
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }

      if (selection.equals("R/P Summary")) {
        Cursor myCursor = new Cursor("reservedPoolSummary.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%");
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "k";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }

      if (selection.equals("x$ksmlru")) {
        if (ConnectWindow.isSysdba()) {
          Cursor myCursor = new Cursor("x$ksmlru.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%");
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
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }

      if (selection.equals("db_object_cache")) {
        Cursor myCursor = new Cursor("dummy",false);
        if (ConsoleWindow.getDBVersion() >= 10) {
          myCursor = new Cursor("dbObjectCache10.sql",true);
        }
        else {
          myCursor = new Cursor("dbObjectCache.sql",true);
        }
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%");
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "c";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }

      if (selection.equals("v$librarycache")) {
        Cursor myCursor = new Cursor("libraryCache.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%");
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "l";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }

      if (selection.equals("v$shared_pool_reserved")) {
        Cursor myCursor = new Cursor("sharedPoolReserved.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%");
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

      if (selection.equals("Number of Subpools")) {
        if (ConnectWindow.isSysdba()) {
          Cursor myCursor = new Cursor("x$kghlu.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%");
          Boolean filterByRAC = true;
          Boolean filterByUser = false;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "x";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        } 
        else {
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public void listActionPerformed(boolean showSQL, String lastAction) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;
    listActionPerformed(lastAction, new JFrame());
  };
  
  public QueryResult getSharedPoolReserved(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("sharedPoolReserved.sql",true);
    Parameters myPars = new Parameters();
    myPars.addParameter("String","%");
    Boolean filterByRAC = true;
    Boolean filterByUser = false;
    Boolean includeDecode = true;
    String includeRACPredicatePoint = "default";
    String filterByRACAlias = "s";
    String filterByUserAlias = "none";
    Boolean restrictRows = false;
    Boolean flip = true;
    Boolean eggTimer = true;
    QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
    
    return myResult;
  }
}