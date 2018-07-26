/*
 *  ParallelQueryB.java        1.0 18/02/05
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
 * 22/08/06 Richard Wright Modified the comment style and error handling
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 30/10/07 Richard Wright Enhanced for RAC
 * 04/05/10 Richard Wright Extend RichButton
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
 * Implements all queries detailing parallel queries running in the database.
 */
public class ParallelQueryB extends RichButton {
  boolean showSQL;
    
  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP
   * @param statusBar 
   * @param resultCache
   */
  public ParallelQueryB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Parallel Query");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
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
      if (selection.equals("OPQ Summary")) {
        Cursor myCursor;
        if (ConsoleWindow.getDBVersion() >= 10) {
          myCursor = new Cursor("parallelQuerySummary10.sql",true);
        }
        else {
          
          myCursor = new Cursor("parallelQuerySummary.sql",true);
        }
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = true;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "p";
        String filterByUserAlias = "sess";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      
      if (selection.equals("OPQ Detail")) { 
        Cursor myCursor;
        if (ConsoleWindow.getDBVersion() >= 10) {
          myCursor = new Cursor("parallelQueryDetail10.sql",true);
        }
        else {
          
          myCursor = new Cursor("parallelQueryDetail.sql",true);
        }
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = true;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "p";
        String filterByUserAlias = "sess";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }

      if (selection.equals("v$pq_sysstat")) {
        Cursor myCursor = new Cursor("pqSysstat.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "p";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      
      if (selection.equals("v$pq_slave")) { 
        Cursor myCursor = new Cursor("pqSlave.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "p";
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
  
  public void listActionPerformed(boolean showSQL, String lastAction) {
    this.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  };  
  
  public QueryResult getPQsysstat(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("pqSysstat.sql",true);
    Parameters myPars = new Parameters();
    Boolean filterByRAC = true;
    Boolean filterByUser = false;
    Boolean includeDecode = true;
    String includeRACPredicatePoint = "default";
    String filterByRACAlias = "p";
    String filterByUserAlias = "none";
    Boolean restrictRows = true;
    Boolean flip = true;
    Boolean eggTimer = true;
    QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

    return myResult;
  }
}