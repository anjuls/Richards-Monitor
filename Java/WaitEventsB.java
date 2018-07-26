/*
 * WaitEventsB.java        13.0 21/02/05
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
 * 
 * 17/06/05 Richard Wright Modified so that wait event scripts are version specific
 * 14/09/06 Richard Wright Modified the comment style and error handling
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 08/11/07 Richard Wright Enhanced for RAC
 * 01/05/08 Richard Wright Added event histogram
 * 04/05/10 Richard Wright Extend RichButton
 * 08/09/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * 
 */
public class WaitEventsB extends RichButton {
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
  public WaitEventsB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Wait Events");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() > 10) addItem("Event Histogram");
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
    ConsoleWindow.getPerformancePanel().setLastAction(selection);
     
    try {
      if (selection.equals("Wait Events")) {
        if (ConsoleWindow.getDBVersion() >= 10.0) {
          Cursor myCursor = new Cursor("waitEvents10.sql",true);
          Parameters myPars = new Parameters();
          Boolean filterByRAC = true;
          Boolean filterByUser = true;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "s";
          String filterByUserAlias = "s";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        }
        else {
          Cursor myCursor = new Cursor("waitEvents.sql",true);
          Parameters myPars = new Parameters();
          Boolean filterByRAC = true;
          Boolean filterByUser = true;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "sw";
          String filterByUserAlias = "s";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        }
      }
      
      if (selection.equals("Wait Events Summary")) {
        Cursor myCursor = new Cursor("waitEventsSummary.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = true;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "sw";
        String filterByUserAlias = "s";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }      
      
      if (selection.equals("Event Histogram")) {
        Cursor myCursor = new Cursor("eventHistogram.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "eh";
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
    this.tearOff = tearOff;
    listActionPerformed(lastAction, new JFrame());
  };  
}