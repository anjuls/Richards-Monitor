/*
 * SchemaViewerPanel.java        12.17 05/01/05
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
 * Change History since 29/03/05
 * =============================
 * 
 * 29/03/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 02/05/05 Richard Wright Modified to call the ExecuteDisplay class
 * 02/06/05 Richard Wright Stopped flipping result sets to vertical as this is 
 *                         now done in the database class
 * 09/06/05 Richard Wright Made objectsSP 300 by 525, down from 300 by 550 because 
 *                         on a laptop screen it did not display properly
 * 10/06/05 Richard Wright Modified getViewDetail() to use ExecuteDisplay
 * 16/08/05 Richard Wright Modified populateUsernames() to use ExecuteDisplay
 * 15/08/05 Richard Wright Selection of a schema now displays schema details
 * 29/09/05 Richard Wright Made objectsSP 300 by 500, down from 300 by 525 because 
 *                         on my laptop screen it did not display properly
 * 03/11/05 Richard Wright Added support for user roles and sys privs.
 * 03/11/05 Richard Wright A right click on table will open up a scratch panel 
 *                         pre populated with a select against that table
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 04/05/07 Richard Wright A change of db connection now resets this screen
 * 18/05/07 Richard Wright Stopped populating user names from jbinit()
 * 07/09/07 Richard Wright Corrected the number of rows in a flipped result set
 * 07/09/07 Richard Wright Modified displayTab to use the displayTable method 
 *                         from ExecuteDisplay rather than a version specific to
 *                         this class
 * 17/09/07 Richard Wright Set the background of the detail area to white (regression from 16.04)
 *                        
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;


public class SchemaViewerPanel extends JPanel {
  // panels 
  JPanel objectsP = new JPanel(new GridBagLayout());
  JTabbedPane detailTP = new JTabbedPane();
  
  // scrollPanes 
  static JScrollPane objectsSP = new JScrollPane();
    
  // comboBoxs 
  static JComboBox schemaNameCB = new JComboBox();
  static JComboBox objectTypeCB = new JComboBox();
  
  // labels 
  JLabel schemaNameL = new JLabel("Schema Name");
  JLabel objectTypeL = new JLabel("Object Type");
  
  // misc 
  private Dimension prefSize = new Dimension(300,25);
  
  // JTable 
  JTable objectsTAB = new JTable();

  // status bar 
  private JLabel statusBarL = new JLabel();

  // JPanels for the centreTP 
  JPanel panelOne;
  JPanel panelTwo;
  JPanel panelThree;
  JPanel panelFour;
  JPanel panelFive;
  JPanel panelSix;
  
  /**
   * Constructor
   */
  public SchemaViewerPanel() {

    try {
      jbInit();
      
//      populateUsernames();
    } 
    catch(Exception e) {
      e.printStackTrace();
    }  
  }
    
  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */ 
  private void jbInit() throws Exception {
    // layout 
    this.setLayout(new BorderLayout());
    
    // objectsP 
    schemaNameL.setPreferredSize(prefSize);
    schemaNameCB.setPreferredSize(prefSize);
    schemaNameCB.setForeground(Color.BLUE);
    schemaNameCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        schemaNameCB_actionPerformed();
      }
    });
    objectTypeL.setPreferredSize(prefSize);
    objectTypeCB.setPreferredSize(prefSize);
    objectTypeCB.setForeground(Color.BLUE);
    objectTypeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        objectTypeCB_actionPerformed();
      }
    });
    objectsSP.setPreferredSize(prefSize);
    
    GridBagConstraints objectsPCons = new GridBagConstraints();

    objectsSP.getViewport().setBackground(Color.WHITE);
    
    objectsPCons.gridx = 0;
    objectsPCons.gridy = 0;
    objectsP.add(schemaNameL, objectsPCons);
    objectsPCons.gridy = 1;
    objectsP.add(schemaNameCB, objectsPCons);
    objectsPCons.gridy = 2;
    objectsP.add(objectTypeL, objectsPCons);
    objectsPCons.gridy = 3;
    objectsP.add(objectTypeCB, objectsPCons);
    objectsPCons.gridy = 5;
    objectsPCons.weighty=10;
    objectsSP.add(objectsTAB);
    objectsSP.setPreferredSize(new Dimension(300,500));
    objectsP.add(objectsSP, objectsPCons);
    
    // statusBarL 
    statusBarL.setText(" ");
    
    objectsP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    detailTP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    this.add(objectsP, BorderLayout.WEST);
    this.add(detailTP, BorderLayout.CENTER);
    this.add(statusBarL, BorderLayout.SOUTH);
    
    
  }

  public static void runPopulateUserNames() {
    // populate the usernames
    Thread populateUsernamesThread = new Thread ( new Runnable() 
    {
      public void run() 
      {
        populateUsernames();
      }
    });
    
    populateUsernamesThread.setDaemon(false);
    populateUsernamesThread.setName("populateUsernames");
    populateUsernamesThread.start();
  }
  
  /**
   * Display all the schema names
   */
  public static void populateUsernames() {
    String cursorId = "usernames.sql";
    
    try {
      /*
       * remove action listener from schemaNameCB to prevent it firing 
       * for every row added 
       */
      ActionListener[] myActionListener = schemaNameCB.getActionListeners();
      schemaNameCB.removeActionListener(myActionListener[0]);
      schemaNameCB.removeAllItems();

      boolean restrictRows = false;
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      QueryResult myResult = ExecuteDisplay.execute(myCursor,myPars,false,false,null,restrictRows);

      int numRows = myResult.getNumRows();
      int indexOfSchema = 0;
      String targetSchema = ConnectWindow.getUsername().toUpperCase();

      // remove old entries ( in case reconnection has changed the db)
      schemaNameCB.removeAllItems();
      objectTypeCB.removeAllItems();
      objectsSP.getViewport().removeAll();
      
      // populate the usernames array with the resultSet 
      for ( int i=0; i < numRows; i++) {
        Vector resultSetRow = myResult.getResultSetRow(i);
        schemaNameCB.addItem(resultSetRow.get(0));
        if (String.valueOf(resultSetRow.get(0)).equals(targetSchema)) indexOfSchema = i;
      }
      
      // add the action listener back to schemaNameCB 
      schemaNameCB.addActionListener(myActionListener[0]);

      schemaNameCB.setSelectedIndex(indexOfSchema);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,"SchemaViewerPanel","Getting Schema Names");
    }
  }
  
  
  /**
   * Take appropriate action when selection is made in the object type combo box.
   * 
   */
  private void objectsTAB_mouseClicked(MouseEvent e) {
    // identify the selected entry 
    int row = objectsTAB.getSelectedRow();
    int col = objectsTAB.getSelectedColumn();
    
    if (e.getButton() == 1) {
      String cellValue;
      cellValue = String.valueOf(objectsTAB.getValueAt(row,col));
      
      if (objectTypeCB.getSelectedItem().equals("TABLE")) getTableDetail(cellValue);    
      if (objectTypeCB.getSelectedItem().equals("INDEX")) getIndexDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("TABLE PARTITION")) getTablePartitionDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("INDEX PARTITION")) getIndexPartitionDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("QUEUE")) getQueueDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("SEQUENCE")) getSequenceDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("SYNONYM")) getSynonymDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("PROCEDURE")) getSourceDetail(cellValue,"PROCEDURE");
      if (objectTypeCB.getSelectedItem().equals("PACKAGE")) getSourceDetail(cellValue,"PACKAGE");
      if (objectTypeCB.getSelectedItem().equals("PACKAGE BODY")) getSourceDetail(cellValue,"PACKAGE BODY");
      if (objectTypeCB.getSelectedItem().equals("FUNCTION")) getSourceDetail(cellValue,"FUNCTION");
      if (objectTypeCB.getSelectedItem().equals("JAVA SOURCE")) getSourceDetail(cellValue,"JAVA SOURCE");
      if (objectTypeCB.getSelectedItem().equals("TRIGGER")) getSourceDetail(cellValue,"TRIGGER");
      if (objectTypeCB.getSelectedItem().equals("TYPE")) getSourceDetail(cellValue,"TYPE");
      if (objectTypeCB.getSelectedItem().equals("TYPE BODY")) getSourceDetail(cellValue,"TYPE BODY");
      if (objectTypeCB.getSelectedItem().equals("VIEW")) getViewDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("CLUSTER")) getClusterDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("JAVA CLASS")) getJavaClassDetail(cellValue);
      if (objectTypeCB.getSelectedItem().equals("LIBRARY")) getLibraryDetail(cellValue);
    }    
  }
  
  /**
   * A new schema name has been selected.  Re-populate the schema objects for this new schema.
   */
  private void schemaNameCB_actionPerformed() {
    String cursorId;
    Parameters myPars = new Parameters();
    
    if (ConsoleWindow.getDBVersion() < 8.1) {
      cursorId = "objectTypes8.sql";

      myPars.addParameter("String",schemaNameCB.getSelectedItem());
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
    } 
    else {
      cursorId = "objectTypes.sql";
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
    }

    try {
      Cursor myCursor = new Cursor(cursorId,true);
      boolean restrictRows = false;
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
  
      int numRows = myResult.getNumRows();
      int indexOfTable = 0;
      
      /* 
       * remove action listener from objectTypeCB to prevent it firing 
       * for every row added 
       */
      ActionListener[] myActionListener = objectTypeCB.getActionListeners();
      objectTypeCB.removeActionListener(myActionListener[0]);
 
      // remove existing entries 
      objectTypeCB.removeAllItems();
      
      // populate the objectTypeCB with the resultSet 
      for ( int i=0; i < numRows; i++) {
        Vector resultSetRow = myResult.getResultSetRow(i);
        objectTypeCB.addItem(resultSetRow.get(0));
        if (String.valueOf(resultSetRow.get(0)).equals("TABLE")) indexOfTable = i;
      }

      // add the action listener back to objectTypeCB 
      objectTypeCB.addActionListener(myActionListener[0]);
      
      if (numRows > 0) {
        objectTypeCB.setSelectedIndex(indexOfTable);
      }
      else {
        objectsSP.getViewport().removeAll();
      }
      detailTP.removeAll();

      // display schema details for the selected schema 
      cursorId = "getUserDetails.sql";
      JScrollPane myScrollPane = new JScrollPane();
      myScrollPane.getViewport().setBackground(Color.WHITE);
      
      try {
        myPars = new Parameters();
        myPars.addParameter("String", schemaNameCB.getSelectedItem());
        
        myCursor = new Cursor(cursorId,true);
        ExecuteDisplay.executeDisplay(myCursor, myPars, myScrollPane, statusBarL, false, null, false);
      } catch (Exception e) {
        // dba_users didn't work, we may noty have enough privs, so lets try all_users instead
        cursorId = "getAllUserDetails.sql";
        myPars = new Parameters();
        myPars.addParameter("String", schemaNameCB.getSelectedItem());

        myCursor = new Cursor(cursorId,true);
        ExecuteDisplay.executeDisplay(myCursor, myPars, myScrollPane, statusBarL, false, null, false);
      }
      detailTP.add(myScrollPane,"Schema");

      // display role privs for the selected schema 
      cursorId = "rolePrivs.sql";
      JScrollPane myScrollPane2 = new JScrollPane();
      myScrollPane2.getViewport().setBackground(Color.WHITE);
      myPars = new Parameters();
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
      
      myCursor = new Cursor(cursorId,true);
      ExecuteDisplay.executeDisplay(myCursor,myPars,myScrollPane2,statusBarL,false,null,false);
      detailTP.add(myScrollPane2,"Roles");
      
      // display sys privs for the selected schema 
      cursorId = "sysPrivs.sql";
      JScrollPane myScrollPane3 = new JScrollPane();
      myScrollPane3.getViewport().setBackground(Color.WHITE);
      myPars = new Parameters();
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
            
      myCursor = new Cursor(cursorId,true);
      ExecuteDisplay.executeDisplay(myCursor,myPars,myScrollPane3,statusBarL,false,null,false);
      detailTP.add(myScrollPane3,"System Privileges");
      
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Object Types");
    }
  }
  
  /**
   * An object type selection has been made.  Populate the objects box with the relevant objects.
   */
  private void objectTypeCB_actionPerformed() {
    detailTP.removeAll();

    if (objectTypeCB.getSelectedIndex() >= 0) {
      if (objectTypeCB.getSelectedItem().equals("TABLE")) getObjectList("tableNames.sql");
      if (objectTypeCB.getSelectedItem().equals("INDEX")) getObjectList("indexeNames.sql");
      if (objectTypeCB.getSelectedItem().equals("TABLE PARTITION")) getObjectList("partitionedTableNames.sql");
      if (objectTypeCB.getSelectedItem().equals("INDEX PARTITION")) getObjectList("partitionedIndexeNames.sql");
      if (objectTypeCB.getSelectedItem().equals("VIEW")) getObjectList("viewNames.sql");
      if (objectTypeCB.getSelectedItem().equals("PACKAGE")) getObjectList("packageNames.sql");
      if (objectTypeCB.getSelectedItem().equals("PACKAGE BODY")) getObjectList("packageBodyNames.sql");
      if (objectTypeCB.getSelectedItem().equals("PROCEDURE")) getObjectList("procedureNames.sql");
      if (objectTypeCB.getSelectedItem().equals("FUNCTION")) getObjectList("functionNames.sql");
      if (objectTypeCB.getSelectedItem().equals("JAVA SOURCE")) getObjectList("javaSourceNames.sql");
      if (objectTypeCB.getSelectedItem().equals("JAVA CLASS")) getObjectList("javaClassNames.sql");
      if (objectTypeCB.getSelectedItem().equals("TRIGGER")) getObjectList("triggerNames.sql");
      if (objectTypeCB.getSelectedItem().equals("SYNONYM")) getObjectList("synonymNames.sql");    if (objectTypeCB.getSelectedItem().equals("PACKAGE BODY")) getObjectList("packageBodyNames.sql");
      if (objectTypeCB.getSelectedItem().equals("CLUSTER")) getObjectList("clusterNames.sql");
      if (objectTypeCB.getSelectedItem().equals("DATABASE LINK")) getObjectList("databaseLinkNames.sql");
      if (objectTypeCB.getSelectedItem().equals("SEQUENCE")) getObjectList("sequenceNames.sql");
      if (objectTypeCB.getSelectedItem().equals("QUEUE")) getObjectList("queueNames.sql");
      if (objectTypeCB.getSelectedItem().equals("LIBRARY")) getObjectList("libraryNames.sql");
      if (objectTypeCB.getSelectedItem().equals("SNAPSHOT")) getObjectList("snapshotNames.sql");
      if (objectTypeCB.getSelectedItem().equals("MATERIALIZED VIEW")) getObjectList("materializedViewNames.sql");
      if (objectTypeCB.getSelectedItem().equals("TYPE")) getObjectList("typeNames.sql");
      if (objectTypeCB.getSelectedItem().equals("TYPE BODY")) getObjectList("typeBodyNames.sql");
    }
  }
  
  /**
   * Get a list of objects ie. all tables for a schema
   * 
   * @param cursorId - name of the sql script to query the required objects from the dictionary
   */
  private void getObjectList(String cursorId) {
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
          
    if (cursorId.equals("partitionedTableNames.sql") || cursorId.equals("partitionedIndexeNames.sql")) {
      myPars = new Parameters();
      myPars.addParameter("String",schemaNameCB.getSelectedItem());
      myPars.addParameter("String","%");
    }
        
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      ExecuteDisplay.executeDisplay(myCursor,myPars,objectsSP,statusBarL,false,null,false);
      objectsTAB = (JTable)objectsSP.getViewport().getComponent(0);
      
      objectsTAB.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          objectsTAB_mouseClicked(e);
        }
      });
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  /**
   * Display multiple tabs showing different details about a chosen table.
   * 
   * @param tableName
   */
  private void getTableDetail(String tableName) {
    detailTP.removeAll();
    
    // get table detail 
    Parameters myPars = new Parameters();
    Parameters myPars2 = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",tableName);
    
    
    // some sql pre v9 only requires the table name and not owner 
    if (ConsoleWindow.getDBVersion() < 9) 
    {
      myPars2.addParameter("String",tableName);
    }
    
    try {
//      QueryResult myResult = executeQuery("dbaTables.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaTables.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Table Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Details");
    }
    
    // get table columns  
    try {
//      QueryResult myResult = executeQuery("dbaTabColumns.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaTabColumns.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Columns");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Tab Columns");
    }

    // get table constraints  
    try {
//      QueryResult myResult = executeQuery("dbaConstraints.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaConstraints.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Constraints");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Constraints");
    }      
    
    // get table modifications 
    if (ConsoleWindow.getDBVersion() > 8.0) {
      try {
//        QueryResult myResult = executeQuery("dbaTabModifications.sql",myPars);
        QueryResult myResult = ExecuteDisplay.execute("dbaTabModifications.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
        displayTab(myResult,"Modifications");
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Getting Table Modifications");
      }        
    }
    
    // get table partitions 
    try {
//      QueryResult myResult = executeQuery("dbaTabPartitions.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaTabPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Partitions");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Partitions");
    }     
    
    if (ConsoleWindow.getDBVersion() >= 8.1)
    {
      // get table syb partitions 
      try {
//        QueryResult myResult = executeQuery("dbaTabSubPartitions.sql",myPars);
        QueryResult myResult = ExecuteDisplay.execute("dbaTabSubPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
        displayTab(myResult,"Sub Partitions");
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Getting Table Sub Partitions");
      }    
    }
    
    // get table column statistics 
    try {
      QueryResult myResult;
      if (ConsoleWindow.getDBVersion() >= 9)
      {
//        myResult = executeQuery("dbaTabColStatistics.sql",myPars);
        myResult = ExecuteDisplay.execute("dbaTabColStatistics.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);

      }
      else
      {
//        myResult = executeQuery("dbaTabColStatisticsPre9.sql",myPars2);        
        myResult = ExecuteDisplay.execute("dbaTabColStatisticsPre9.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      }
      displayTab(myResult,"Column Statistics");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Column Statistics");
    }    
    
    // get table comments 
    try {
//      QueryResult myResult = executeQuery("dbaTabComments.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaTabComments.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Table Comments");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Comments");
    }
    
    // get column comments 
    try {
//      QueryResult myResult = executeQuery("dbaColComments.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaColComments.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Column Comments");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Column Comments");
    } 
  
    // get table indexes 
    try {
//      QueryResult myResult = executeQuery("tabIndexes.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("tabIndexes.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Indexes");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Indexes");
    }    
    
    /* get table indexe columns */
    try {
//      QueryResult myResult = executeQuery("tabIndColumns.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("tabIndColumns.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Index Columns");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Indexed Columns");
    }    
    
    /* get tab histograms */
    try {
//      QueryResult myResult = executeQuery("tabHistograms.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("tabHistograms.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Histograms");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Histograms");
    }    
    
    /* get tab histograms */
    try {
//      QueryResult myResult = executeQuery("tabTriggers.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("tabTriggers.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Triggers");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Table Triggers");
    }
  }
  
  /**
   * Display the results of a query on a new tab.
   * 
   * @param myResult
   * @param tabTitle - Title of the tab, as seen by the user
   */
  private void displayTab(QueryResult myResult,String tabTitle) {
    // convert single row tables into a vertical format 
    if (myResult.getNumRows() == 1 && myResult.getNumCols() > 1 && (!ConsoleWindow.isFlipSingleRowOutput())) flipResultSet(myResult);
    
//    JTable myTAB = createTable(myResult);
//    JScrollPane spOne = new JScrollPane();
//    spOne.getViewport().setBackground(Color.WHITE);
//    spOne.getViewport().add(myTAB);
//    detailTP.add(spOne,tabTitle);
    
    JScrollPane myScrollPane = new JScrollPane();
    myScrollPane.getViewport().setBackground(Color.WHITE);
    ExecuteDisplay.displayTable(myResult,myScrollPane,false,statusBarL);
    detailTP.add(myScrollPane,tabTitle);
  }
  
  /**
   * Display multiple tabs showing different details about a chosen table.
   * 
   * @param indexName
   */
  private void getIndexDetail(String indexName) {
    detailTP.removeAll();
    
    // get index detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",indexName);
    

    // get index details 
    try {
 //     QueryResult myResult = executeQuery("dbaIndexes.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaIndexes.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Index Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Index Details");
    }     
    
    // get index columns 
    try {
//      QueryResult myResult = executeQuery("dbaIndColumns.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaIndColumns.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Columns");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Getting Index Columns");
    }        
    
    if (ConsoleWindow.getDBVersion() >= 8.1)
    {
      // get index expressions 
      try {
//        QueryResult myResult = executeQuery("dbaIndExpressions.sql",myPars);
        QueryResult myResult = ExecuteDisplay.execute("dbaIndExpressions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
        displayTab(myResult,"Expressions");
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Getting Index Expressions");
      }       
    }
    
    // get index partitions 
    try {
//      QueryResult myResult = executeQuery("dbaIndPartitions.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaIndPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Partitions");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Index Partitions");
    }     
    
    if (ConsoleWindow.getDBVersion() >= 8.1)
    {
      // get index sub partitions 
      try {
//        QueryResult myResult = executeQuery("dbaIndSubPartitions.sql",myPars);
        QueryResult myResult = ExecuteDisplay.execute("dbaIndSubPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
        displayTab(myResult,"Sub Partitions");
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Index Sub Partitions");
      }     
    }
  }
  
  /**
   * Display details of a partitioned table.
   * 
   * @param tableName
   */
  private void getTablePartitionDetail(String tableName) {
    detailTP.removeAll();
    
    // get table partition detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",tableName);
    
    try {
//      QueryResult myResult = executeQuery("dbaTabPartitions.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaTabPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Partition Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }  
  
  /**
   * Display details of a partitioned index.
   * 
   * @param indexName
   */
  private void getIndexPartitionDetail(String indexName) {
    detailTP.removeAll();
    
    // get index partition detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",indexName);
    
    try {
//      QueryResult myResult = executeQuery("dbaIndPartitions.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaIndPartitions.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Partition Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }
  
  /**
   * Display details about a queue.
   * 
   * @param queueName
   */
  private void getQueueDetail(String queueName) {
    detailTP.removeAll();
    
    // get queue detail 
    Parameters myPars = new Parameters();
    //Object[][] parameters = new Object[2][2];
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",queueName);
    
    try {
//      QueryResult myResult = executeQuery("dbaQueues.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaQueues.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Queue Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }  
  
  /**
   * Display details of a sequence.
   * 
   * @param sequenceName - Name of a sequence
   */
  private void getSequenceDetail(String sequenceName) {
    detailTP.removeAll();
    
    // get sequence detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",sequenceName);
    try {
//      QueryResult myResult = executeQuery("dbaSequences.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaSequences.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,"Sequence Details");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }  
  
  /**
   * Display source code from dba_source.
   * 
   * @param sourceName 
   * @param sourceType 
   */
  private void getSourceDetail(String sourceName,String sourceType) {
    detailTP.removeAll();
    
    // get souce detail
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",sourceName);
    myPars.addParameter("String",sourceType);
    
    try {
//      QueryResult myResult = executeQuery("dbaSource.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaSource.sql",myPars,ConsoleWindow.isFlipSingleRowOutput(),true,null);
      displayTab(myResult,sourceType);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }  
  
  /**
   * Display details of a view.
   * 
   * @param viewName 
   */
  private void getViewDetail(String viewName) {
    detailTP.removeAll();
    
    // get view detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",viewName);
    
    try {
      QueryResult myResult = ExecuteDisplay.execute("dbaViews.sql", myPars, false, false,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      // format the view sql 
      SQLFormatter mySQLFormatter = new SQLFormatter();
      String[] formattedSQL = mySQLFormatter.formatSQL(resultSet[0][0]);
      
      // replace myResult resultSet with the formatted sql 
      int len = 0;
      Vector myResultSet = new Vector(formattedSQL.length);
      for (int i=0; i < formattedSQL.length; i++) {
        Vector row = new Vector(1);
        row.add(formattedSQL[i]);
        myResultSet.add(row);
        if (len < formattedSQL[i].length()) len = formattedSQL[i].length();
        
      }
      myResult.setResultSet(myResultSet);
      int[] myColumnWidths = new int[1];
      myColumnWidths[0] = len;
      myResult.setColumnWidths(myColumnWidths);
      
      
      displayTab(myResult,"View");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  } 
  
  /**
   * Display details of cluster.
   * 
   * @param clusterName 
   */
  private void getClusterDetail(String clusterName) {
    detailTP.removeAll();
    
    // get cluster detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",clusterName);
    
    try {
//      QueryResult myResult = executeQuery("dbaClusters.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaClusters.sql", myPars, false, false,null);
      displayTab(myResult,"Cluster");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  } 

  /**
   * Display details of a library.
   * 
   * @param libraryName 
   */
  private void getLibraryDetail(String libraryName) {
    detailTP.removeAll();
    
    // get libary detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",libraryName);
    
    try {
//      QueryResult myResult = executeQuery("dbaLibraries.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaLibraries.sql", myPars, false, false,null);
      displayTab(myResult,"Library");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  } 

  /**
   * Display details of a java class.
   * 
   * @param javaClassName 
   */
  private void getJavaClassDetail(String javaClassName) {
    detailTP.removeAll();
    
    // get view detail 
    Parameters myPars = new Parameters();
/*    Object[][] parameters = new Object[2][2];
    parameters[0][0] = "String";
    parameters[1][0] = schemaNameCB.getSelectedItem();
    parameters[0][1] = "String";
    parameters[1][1] = javaClassName; */
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",javaClassName);
    
    try {
//      QueryResult myResult = executeQuery("dbaJavaClasses.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaJavaClasses.sql", myPars, false, false,null);
      displayTab(myResult,"Java Class");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  } 
  
  /**
   * Display details of synonym.
   * 
   * @param synonymName 
   */
  private void getSynonymDetail(String synonymName) {
    detailTP.removeAll();
    
    // get view detail 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",synonymName);
    
    try {
//      QueryResult myResult = executeQuery("dbaSynonyms.sql",myPars);
      QueryResult myResult = ExecuteDisplay.execute("dbaSynonyms.sql", myPars, false, false,null);
      displayTab(myResult,"Synonym");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }     
  }
  
  /**
   * 
   * @param cursorId
   * @param parameters
   * @return
   * @throws Exception
   */
/*  private QueryResult executeQuery(String cursorId,Parameters myPars) throws Exception {
    QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars);
   
    return myResult;
  } */ 
  
  /**
   * Execute a Query.
   * 
   * @param cursorId 
   * @param parameters 
   * @return QueryResult
   * @throws Exception 
   */
/*  private static QueryResult executeQuery2(String cursorId,Parameters myPars) throws Exception {
    QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars);
   
    return myResult;
  } */
  
  /**
   * This method ought not to be required.  The whole class should make better use of ExecuteDisplay 
   * but being pragmatic, it'll continue to duplicate some of that functionality for now.
   * 
   * Takes a query result and returns a JTable.
   * @param myResult
   * @return JTable
   */
  private JTable createTable(QueryResult myResult) {
    
    // get the resultSet from the query
    Vector resultSet = myResult.getResultSetAsVectorOfVectors();
    
    // get the number of columns in the resultSet
    int numCols = myResult.getNumCols();
    
    // how wide are all the columns
    int[] columnWidths = myResult.getColumnWidths();
    
    // get the resultSet column headings
    String[] resultHeadings = myResult.getResultHeadings();
    
    // create the JTable to display
    TableModel tm = new TableModel(resultHeadings,resultSet);
    TableSorter sorter = new TableSorter(tm);
    JTable myTAB = new JTable(sorter);
    sorter.setTableHeader(objectsTAB.getTableHeader());
    myTAB.getTableHeader().setToolTipText("Click to specify sorting");

    myTAB.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    myTAB.setCellSelectionEnabled(true);
    myTAB.setGridColor(Color.white);
    myTAB.setForeground(Color.blue);
    myTAB.setFont(new Font("Monospaced", 0, 11));

    
    // set the column widths
    for (int i = 0;i < numCols;i++) {
      TableColumn column = myTAB.getColumnModel().getColumn(i);
      column.setPreferredWidth(columnWidths[i] * 9);
      column.setHeaderValue(resultHeadings[i]);
    }
  
    return myTAB;
  }
 
  /**
   * Converts a queryresult which only contains a single to vertical format.  
   * ie.  one row per attribute value pair.
   * 
   * @param myResult 
   */
  private void flipResultSet(QueryResult myResult) {
    String[][] resultSet = myResult.getResultSetAsStringArray();
    String[] resultHeadings = myResult.getResultHeadings();
    String[][] newResultSet = new String[myResult.getNumCols()][2];
    int col0Width = 0;
    int col1Width = 1;
    
    for (int i=0; i < myResult.getNumCols(); i++) {
      newResultSet[i][0] = resultHeadings[i];
      newResultSet[i][1] = resultSet[0][i];
      if (resultHeadings[i].length() > col0Width) col0Width = resultHeadings[i].length();
      if (resultSet[0][i].length() > col1Width) col1Width = resultSet[0][i].length();
    }
    
    resultHeadings = new String[2];
    resultHeadings[0] = "Attribute";
    resultHeadings[1] = "Value";
    
    int[] columnWidths = new int[2];
    columnWidths[0] = col0Width;
    columnWidths[1] = col1Width;
    
    Vector myResultSet = new Vector(newResultSet.length);
    for (int i=0; i < newResultSet.length; i++) {
      Vector row = new Vector(2);
      row.add(newResultSet[i][0]);
      row.add(newResultSet[i][1]);
      myResultSet.add(row);
    }
    
    myResult.setResultSet(myResultSet);
    myResult.setColumnWidths(columnWidths);
    //myResult.setNumRows(newResultSet.length);
    myResult.setNumCols(2);
    myResult.setResultHeadings(resultHeadings);
  }
  
  /**
   * Open a new scratch panel populated with a sql statement to select from a table right clicked
   */
  public static void openPrePopulatedScratchPanel(String tableName) {
    // get a list of all the columns for this table 
    Parameters myPars = new Parameters();
    myPars.addParameter("String",schemaNameCB.getSelectedItem());
    myPars.addParameter("String",tableName);
    
    QueryResult myResult;
    String[][] resultSet;
    StringBuffer sql = new StringBuffer();  
    
    try {
//      myResult = executeQuery2("dbaTabColumns.sql",myPars);
      myResult = ExecuteDisplay.execute("dbaTabColumns.sql", myPars, false, false,null);
      resultSet = myResult.getResultSetAsStringArray();
      
      // Build a select statement from the list of columns 
      sql.append("SELECT " + resultSet[0][2] + "\n");
      
      for (int i=1; i < myResult.getNumRows(); i++) {
        sql.append(",      " + resultSet[i][2] + "\n");
      }
      
      sql.append("FROM " + schemaNameCB.getSelectedItem().toString() + "." + tableName + "\n");
      sql.append("WHERE rownum < 50;");
      
      ConsoleWindow.addScratchPanel(sql.toString());      
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,ConnectWindow.getConsoleWindow());
    }    
  }
}