/*
 * DatabaseSummaryB.java        1.0 18/02/05
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
 * Change History since 20/09/05
 * =============================
 * 
 * 20/09/05 Richard Wright Converted to extent JComboBox rather than JButton so
 *                         that resource limit can be included.  To gain space 
 *                         on the database panel as it's becoming a bit crowded.
 * 29/09/05 Richard Wright Added support for database properties.
 * 31/10/05 Richard Wright Exchanged simple scripts for a table name * 
 * 02/11/05 Richard Wright Added support for the DB info Pages
 * 12/06/06 Richard Wright Added v$version so that it can be extracted for a
 *                         performance report.
 * 05/07/06 Richard Wright Fixed methods that forgot to update the resultCache
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 22/08/06 Richard Wright Did not consistently showSQL
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 15/10/07 Richard Wright Enhanced to support RAC
 * 10/12/07 Richard Wright Removed db info pages links
 * 11/01/08 Richard Wright Added high water mark stats
 * 30/04/10 Richard Wright Extend RichButton
 * 24/11/11 Richard Wright Added registry$history
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 * 09/12/15 Richard Wright Added support for containers
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;


/**
 * Implements a queries which provides a summary of the database.
 */
public class DatabaseSummaryB extends RichButton {
  boolean showSQL;
  
  boolean debug = false;
  
  
  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @param statusBar 
   * @param resultCache
   */
  public DatabaseSummaryB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Db Summary");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 9) {
      addItem("Database Properties");
      addItem("DBA Registry");
    }
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      addItem("Database Properties");
      addItem("High Water Mark Stats");
      addItem("Dba Feature Usage Stats");
      addItem("DBA Registry");
    }
      
    if (ConsoleWindow.getDBVersion() >= 10.2) {
      addItem("registry$history");
    }
    
    if (ConsoleWindow.getDBVersion() >= 12.0) addItem("Containers");
    
  }

  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    
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
      if (selection.equals("Database Summary")) {
        Cursor myCursor = new Cursor("databaseSummary.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "i";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      
      if (selection.equals("Database Properties")) {
        Cursor myCursor = new Cursor("database_properties",true);
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
      
      if (selection.equals("Resource Limit")) { 
        Cursor myCursor = new Cursor("resourceLimit.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "r";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }        
      
      if (selection.equals("High Water Mark Stats")) { 
        Cursor myCursor = new Cursor("highWaterMarkStats.sql",true);
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
      
      if (selection.equals("DBA Registry")) {
        Cursor myCursor = new Cursor("dba_registry",true);
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

   
      if (selection.equals("registry$history")) {
        Cursor myCursor = new Cursor("registry$history",true);
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
                     
 
      if (selection.equals("Dba Feature Usage Stats")) {
        Cursor myCursor = new Cursor("dbaFeatureUsage.sql",true);
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

        if (selection.equals("Containers")) {
        Cursor myCursor = new Cursor("v$containers", true);
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
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public void containers() {

    try {

    } catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public void listActionPerformed(boolean showSQL,String lastAction) {
    this.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  };   
}