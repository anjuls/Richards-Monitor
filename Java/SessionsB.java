/*
 *  SessionsB.java        1.0 18/02/05
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
 * Change History since 23/05/05
 * =============================
 * 01/09/06 Richard Wright Modified the comment style and error handling
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 18/10/07 Richard Wright Enhanced for RAC
 * 30/04/10 Richard Wright Extend RichButton
 * 05/01/12 Richard Wright Added active sessions and active foreground sessions
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * Implements session related queries.
 */
public class SessionsB extends RichButton {
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
   public SessionsB(String[] options, JScrollPane scrollP, JLabel statusBar,ResultCache resultCache) {
    this.setText("Sessions");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      addItem("Sessions - Active");
      addItem("Sessions - Active Foreground");
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
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    DatabasePanel.setLastAction(selection);
   
    try {     
      if (selection.equals("Session Summary")) {
        Cursor myCursor = new Cursor("sessionSummary.sql",true);
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
      
      if (selection.equals("Sessions")) {
        Cursor myCursor = new Cursor("sessions.sql",true);
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
      
      if (selection.equals("Sessions - Active")) {
        Cursor myCursor = new Cursor("sessionsActive.sql",true);
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
      
      if (selection.equals("Sessions - Active Foreground")) {
        Cursor myCursor = new Cursor("sessionsActiveForeground.sql",true);
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
      
      if (selection.equals("Session IO")) {
        Cursor myCursor = new Cursor("sessionIOv2.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%");
        Boolean filterByRAC = true;
        Boolean filterByUser = true;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "s";
        String filterByUserAlias = "sess";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      
      if (selection.equals("Session Events")) {
        Cursor myCursor = new Cursor("sessionEventsv2.sql",true);
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
      
      if (selection.equals("Rman Sessions")) {
        Cursor myCursor = new Cursor("rmanProcesses.sql",true);
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