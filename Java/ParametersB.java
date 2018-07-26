/*
 * ParametersB.java        1.0 18/02/05
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
 * 21/06/06 Richard Wright Added non default parameters
 * 05/07/06 Richard Wright Fixed methods that forgot to update the resultCache
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 07/11/06 Richard Wright Added support for SGA_TARGET
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 18/10/07 Richard Wright Enhanced for RAC
 * 30/04/10 Richard Wright Extend RichButton
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;


/**
 * Implements queries about database and instance parameters.
 */
public class ParametersB extends RichButton {
  static boolean showSQL;  
  
  /**
   * Constructor.
   *
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
   public ParametersB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Parameters");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      addItem("SGA Dynamic Components");
      addItem("SGA Resize Ops");
    }
  }

  /**
   * @param showSQL
   */
  public void actionPerformed(boolean showSQL) {
    ParametersB.showSQL = showSQL;
    
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
   * Performs the user selected action.
   * @param selectedOption
   * @param listFrame
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    DatabasePanel.setLastAction(selection);   
    try {
      if (selection.equals("Parameters")) {
        // ask the user to enter the parameter of interest 
        Object myPar = JOptionPane.showInputDialog(
                  scrollP,
                  "Which Parameter are you interested in\n\n This equates to the " +
                  "show parameter function in sqlplus",
                  "show Parameter",
                  JOptionPane.QUESTION_MESSAGE,null,null,"%");

        Cursor myCursor = new Cursor("databaseParameter.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%" + myPar + "%");  // prefix and suffix parameter name with %
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
      
      if (selection.equals("_Parameters")) {
        if (ConnectWindow.isSysdba()) {          
          Cursor myCursor = new Cursor("underscoreParameters.sql",true);
          Parameters myPars = new Parameters();
          Boolean filterByRAC = true;
          Boolean filterByUser = false;
          Boolean includeDecode = true;
          String includeRACPredicatePoint = "beforeOrderBy";
          String filterByRACAlias = "x";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        } else {
          throw new InsufficientPrivilegesException("SYSDBA access is required");
        }
      }
      
      if (selection.equals("SP Parameters")) {
        // ask the user to enter the parameter of interest 
        Object myPar = JOptionPane.showInputDialog(
                  scrollP,
                  "Which Parameter are you interested in\n\n ",
                  "show Parameter",
                  JOptionPane.QUESTION_MESSAGE,null,null,"%");   
        
        Cursor myCursor = new Cursor("spParameter.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%" + myPar + "%");  // prefix and suffix parameter name with %
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
      
      if (selection.equals("Non-Default Parameters")) {
        QueryResult myResultSet = getNonDefaultParameters(resultCache);
        ExecuteDisplay.displayTable(myResultSet,scrollP,showSQL,statusBar);
      }      
      
      if (selection.equals("SGA Dynamic Components")) {        
        QueryResult myResultSet = getSGADynamicComponents(resultCache);
        ExecuteDisplay.displayTable(myResultSet,scrollP,showSQL,statusBar);
      }      
      
      if (selection.equals("SGA Resize Ops")) {      
        QueryResult myResultSet = this.getSGAResizeOps(resultCache);
        ExecuteDisplay.displayTable(myResultSet,scrollP,showSQL,statusBar);
      }      
      
      if (selection.equals("Inconsistent Parameters in RAC db")) {
        QueryResult myResultSet = getInconsistentParameters(resultCache);
        ExecuteDisplay.displayTable(myResultSet,scrollP,showSQL,statusBar);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  /**
   * @param showSQL
   * @param lastAction
   */
  public void listActionPerformed(boolean showSQL, String lastAction) {
    ParametersB.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  };

  /**
   * @return
   * @throws Exception
   */
  public QueryResult getSGADynamicComponents(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("sgaDynamicComponents.sql",true);
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
    QueryResult myResultSet = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

    return myResultSet;
  }

  /**
   * @return
   * @throws Exception
   */
  public QueryResult getSGAResizeOps(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("sgaResizeOps.sql",true);
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
    QueryResult myResultSet = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

    return myResultSet;
  }

  /**
   * @return
   * @throws Exception
   */
  public QueryResult getNonDefaultParameters(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("nonDefaultParameters.sql",true);
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
    QueryResult myResultSet = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

    return myResultSet;
  }

  /**
   * @return
   * @throws Exception
   */
  public QueryResult getAllParameters(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("databaseParameter.sql",true);
    Parameters myPars = new Parameters();
    myPars.addParameter("String","%"); 
    Boolean filterByRAC = true;
    Boolean filterByUser = false;
    Boolean includeDecode = true;
    String includeRACPredicatePoint = "default";
    String filterByRACAlias = "p";
    String filterByUserAlias = "none";
    Boolean restrictRows = true;
    Boolean flip = true;
    Boolean eggTimer = true;
    QueryResult myResultSet = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
    
    return myResultSet;
  }  
  
  /**
   * @return
   * @throws Exception
   */
  public QueryResult getInconsistentParameters(ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor("inconsistentParameters.sql",true);
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
    QueryResult myResultSet = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

    return myResultSet;
    
  }
}