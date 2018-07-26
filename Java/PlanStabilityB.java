/*
 *  PlanStabilityB.java        Unknown Creation Date
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * 
 */
public class PlanStabilityB extends RichButton {
  boolean showSQL;

  /**
   * Constructor
   * 
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public PlanStabilityB(String[] options, JScrollPane scrollP, JLabel statusBar, 
                       ResultCache resultCache) {
    this.setText("Plan Stability");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 10) addItem("SQL Profiles");
    
    if (ConsoleWindow.getDBVersion() >= 11) {
      addItem("Plan Baseline Summary");
      addItem("All Plan Baselines");
      addItem("Plan Baselines Never Used");
      addItem("Show Plan Base Line in Detail");
    }
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

          public void mousePressed(MouseEvent e) {
          }

          public void mouseEntered(MouseEvent e) {
          }

          public void mouseReleased(MouseEvent e) {
          }

          public void mouseExited(MouseEvent e) {
          }
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
      if (selection.equals("Outlines")) {
        if (ConsoleWindow.getDBVersion() >= 10.0) {
          Cursor myCursor = new Cursor("outlines10.sql",true);
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
        else {
          Cursor myCursor = new Cursor("outlines.sql",true);
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

      if (selection.equals("SQL Profiles")) {
        Cursor myCursor = new Cursor("sqlProfiles.sql",true);
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
      
      if (selection.equals("Plan Baseline Summary")) {
        Cursor myCursor = new Cursor("sqlPlanBaselineSummary.sql",true);
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
      
      if (selection.equals("All Plan Baselines")) {
        Cursor myCursor = new Cursor("dba_sql_plan_baselines",true);
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
      
      if (selection.equals("Plan Baselines Never Used")) { 
        Cursor myCursor = new Cursor("sqlPlanBaselinesNeverUsed.sql",true);
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

      if (selection.equals("Show Plan Base Line in Detail")) { 
        showPlanBaseLine();
      } 
      
    } catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }

  public void listActionPerformed(boolean showSQL, String lastAction) {
    this.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  }
  
  public void showPlanBaseLine() {


    try {

      String planName = (String)JOptionPane.showInputDialog(
                  scrollP,
                  "Enter the Plan Name. (NOT A SQL HANDLE)",
                  "Plan Name",
                  JOptionPane.QUESTION_MESSAGE,null,null,null);

      String cursorId = "RichMonShowPlanBaseline";
      Cursor myCursor = new Cursor(cursorId, false);

      if (planName.trim().length() > 0) {
        myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_sql_plan_baseline(plan_name => '" +
                                   planName + "', format => 'ALL'))\n");
                
        Parameters myPars = new Parameters();
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = false;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "none";
        String filterByUserAlias = "none";
        Boolean restrictRows = false;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
    } catch (Exception e) {
      ConsoleWindow.displayError(e, this,"Error showing sql plan baseline");
    }

  }
}
