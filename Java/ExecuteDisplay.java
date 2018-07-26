/*
 * ExecuteDisplay.java        13.06 17/03/05
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
 * 14/03/05 Richard Wright Overloaded execute methods so that result set flipping 
 *                         can be specified
 * 02/06/05 Richard Wright Single Row result sets which have been flipped now 
 *                         have the attribute column displayed in black
 * 21/10/05 Richard Wright clicking on sql hash value now opens up a tearOff and 
 *                         displays the formatted sql statement
 * 01/11/05 Richard Wright Removed references to dbarep - no longer used
 * 10/10/05 Richard Wright Added a new execute method to allow a File to specified 
 *                         rather than a cursorId.  The file content will be read 
 *                         from disk and used to created the cursor.
 * 31/01/06 Richard Wright Increased the minimum width of column so that it displays better on XP.
 * 02/01/06 Richard Wright Added methods etc to allow printing of output to a JTextPane
 * 12/07/06 Richard Wright Added support for the config parameters 'additonal_window_width' 
 *                         and 'additional_window_height' 
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 12/01/06 Richard Wright Added createSqlDetailPanel10g
 * 30/01/07 Richard Wright Added support for timeouts executing sql
 * 22/01/07 Richard Wright Overloaded createOutputString so that heading need not be output
 * 10/09/07 Richard Wright Modified createOutputString to only includes headings if numRows > 0
 * 12/09/07 Richard Wright Updated every status bar update to show only 1 row for flipped result sets
 * 18/09/07 Richard Wright Modified createOutputString not to pad out a column if only 1 exists
 * 18/09/07 Richard Wright Modified displayTextPane to use NoWrapEditorKit
 * 02/10/07 Richard Wright Modified createSqlDetailPanel10g to use JTextPane for displaying SQL to better show pl/sql
 * 18/10/07 Richard Wright Modified showSQLOnScreen to allow for sql text as well as sql scripts
 * 07/12/07 Richard Wright Enhanced createSqlDetailPanel10g for RAC
 * 10/01/08 Richard Wright Removed script names from status bar updates
 * 02/07/08 Richard Wright Enhanced to take account of the scrollP name to determine
 *                         which scrollP to remove etc
 * 04/11/08 Richard Wright Modified so that no temporary files are created
 * 18/05/09 Richard Wright Fixed problem showSQLOnScreen
 * 14/09/09 Richard Wright Fixed problem showSQLOnScreen
 * 14/09/09 Richard Wright 'EVENT' no longer can be clicked on
 * 22/10/09 Richard Wright Removed all methods that are not called, and consolidated others to reduce the
 *                         number of overloaded methods in this class
 * 29/12/09 Richard Wright Moved createSQLDetailPanel10g into its own class SQLDetailPanel
 * 17/11/10 Richard Wright Updated statusBar updates to warn when a result set has been truncated to maxResultSetSize
 * 28/07/11 Richard Wright in displayTable statusBar is now checked to ensure it exists before updating it
 * 28/07/11 Richard Wright Clicking on service name will populate the connect window
 * 26/02/14 Richard Wright added restrictRows boolean to allow row restriction to be turned off
 * 10/09/15 Richard Wright modified displayTable and updateDisplayedTable to account ofr filtering in the message
 * 12/10/16 Richard Wright Fixed issue with instance filtering message missing out an instance
 * 16/12/15 Richard Wright Removed a few overloaded execute and executeDisplay methods
 */


package RichMon;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.io.FileInputStream;

import java.io.ObjectInputStream;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableColumn;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jfree.chart.ChartPanel;


/**
 * Collection of methods to control the execution of a cursor and the displaying of the result.
 */
public class ExecuteDisplay 
{

  static SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
  
  // panel for containing all the charts in a single window 
  static JFrame windowHolderF;
  static JTabbedPane windowHolderTP;
  
  static boolean debug = false;
  


  /**
   * Constructor
   */
  public ExecuteDisplay()
  {
  }
         
  
  public static void executeDisplay(Cursor myCursor, Parameters myPars, JScrollPane scrollP, JLabel statusBar, boolean showSQL, ResultCache resultCache, boolean restrictRows) throws Exception {
    if (showSQL == false) { 
  //      CursorCache.cacheEntry(cursorId, myCursor);
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
  
      // cache the resultSet 
      if (resultCache instanceof ResultCache) resultCache.cacheResult(myResult);
      
      // display the query result 
      displayTable(myResult, scrollP, showSQL, statusBar);
    }
    else {
      /* get the sql to display */
      showSQLOnScreen(myCursor);
    }
  }    
  
  public static void executeDisplay(Cursor myCursor, Parameters myPars, JScrollPane scrollP, JLabel statusBar, boolean showSQL, ResultCache resultCache, boolean restrictRows, boolean filterByUser, boolean filterByRAC) throws Exception {
    if (showSQL == false) { 
  //      CursorCache.cacheEntry(cursorId, myCursor);
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
      
//      if (filterByUser) myResult.setUsernameFilter(ConsoleWindow.getUsernameFilter());
//      if (filterByRAC) {
//        myResult.setSelectedInstances(ConsoleWindow.getSelectedInstances());
//        myResult.setIsOnlyLocalInstanceSelected(ConsoleWindow.isOnlyLocalInstanceSelected());
//      }
  
      // cache the resultSet 
      if (resultCache instanceof ResultCache) resultCache.cacheResult(myResult);
      
      // display the query result 
      displayTable(myResult, scrollP, showSQL, statusBar);
    }
    else {
      /* get the sql to display */
      showSQLOnScreen(myCursor);
    }
  }         
  
  /**
   * Loads a cursor from the jar file and executes the loaded sql
   *
   * @param cursorId - name of the sql script to other from the jar file
   * @param flip - whether a single row result set should be flipped vertical
   * @param eggTimer - should the egg timer be display during execution
   * @throws java.lang.Exception
   */
  public static QueryResult execute(String cursorId,boolean flip,boolean eggTimer, ResultCache resultCache) throws Exception {
      // create a cursor and execute the sql 
      Cursor myCursor = new Cursor(cursorId,true);
    
      return execute(myCursor,flip,eggTimer,resultCache);
  }      
  
  public static QueryResult execute(String cursorId, Parameters myPars, boolean flip,boolean eggTimer,ResultCache resultCache) throws Exception {
    Cursor myCursor = new Cursor(cursorId,true);
    boolean restrictRows = false;
    
    return execute(myCursor,myPars,flip,eggTimer,resultCache,restrictRows);
  }     
   
  public static QueryResult execute(Cursor myCursor, Parameters myPars, boolean flip,boolean eggTimer,ResultCache resultCache, boolean restrictRows) throws Exception {
      myCursor.setFlippable(flip);
      myCursor.setEggTimer(eggTimer);
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);

      // cache the resultSet 
      if (resultCache instanceof ResultCache) resultCache.cacheResult(myResult);
    
      return myResult;
  }  
  
  public static QueryResult execute(Cursor myCursor, Parameters myPars, boolean flip,boolean eggTimer,ResultCache resultCache, boolean restrictRows, int iteration) throws Exception {
      myCursor.setFlippable(flip);
      myCursor.setEggTimer(eggTimer);
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows,iteration);

      // cache the resultSet 
      if (resultCache instanceof ResultCache) resultCache.cacheResult(myResult);
    
      return myResult;
  }    
  
  private static QueryResult readCachedResult(String fileName) throws Exception {
    if (debug) System.out.println("Reading in from cache: " + fileName);

    FileInputStream fis;
    ObjectInputStream ins;
    QueryResult myResult = new QueryResult();
    
    fis = new FileInputStream(fileName);
    ins = new ObjectInputStream(fis);
    myResult = (QueryResult)ins.readObject();
    ins.close();

    return myResult;
  }
  
  public static QueryResult executeAWR(String cursorId, Parameters myPars, boolean flip,int startSnapId, int endSnapId, String instanceName, String fileSuffix) throws Exception {
    Cursor myCursor = new Cursor(cursorId,true);
    myCursor.setFlippable(flip);
    myCursor.setEggTimer(true);
    QueryResult myResult = new QueryResult();

    // modify the suffix to remove '%' and spaces
    if (fileSuffix instanceof String) {
      fileSuffix = fileSuffix.replaceAll("%", "percent");
      fileSuffix = fileSuffix.replaceAll(" ", "-");
      fileSuffix = fileSuffix.replaceAll("/", "_");
      fileSuffix = fileSuffix.replaceAll("\\\\", "_");
      fileSuffix = fileSuffix.replaceAll(":", "");
      fileSuffix = fileSuffix.replaceAll("\\*", "-");
    }

    // construct a path and file name

    String fileName;
    if (ConnectWindow.isLinux()) {
      fileName = ConnectWindow.getBaseDir() + "/Cache/" + ConsoleWindow.getDatabaseName() + "/" + instanceName + "/" + startSnapId + "-" + endSnapId + "-";     
    }
    else {
      fileName = ConnectWindow.getBaseDir() +  "\\Cache\\" + ConsoleWindow.getDatabaseName() + "\\" + instanceName + "\\" + startSnapId + "-" + endSnapId + "-";
    }
    
    fileName = fileName + myCursor.getCursorID().substring(0,myCursor.getCursorID().indexOf(".sql"));
    if (fileSuffix instanceof String) fileName = fileName + "-" + fileSuffix;
    
    
    File cachedResultFile = new File(fileName);
      
    if (cachedResultFile.exists()) {
      myResult = readCachedResult(fileName);
      if (debug) System.out.println("Querying database (result found in cache and read in): " + fileName);
    }
    else {
      // result it not already cached
      if (debug) System.out.println("Querying database (not found in cache): " + fileName);
      myCursor.setCachingOn(instanceName, startSnapId, endSnapId, fileName);
      myResult = ExecuteDisplay.execute(myCursor, myPars, flip, true, null,false);
    }
    
      return myResult;
  } 
  
  
  public static QueryResult execute(Cursor myCursor, boolean flip,boolean eggTimer,ResultCache resultCache) throws Exception {
    Parameters myPars = new Parameters();
    boolean restrictRows = false;
    
    return execute(myCursor,myPars,flip,eggTimer,resultCache,restrictRows);
  }       
   
  public static QueryResult execute(Cursor myCursor, boolean flip,boolean eggTimer,ResultCache resultCache, boolean restrictRows, int iteration) throws Exception {
    Parameters myPars = new Parameters();
    
    return execute(myCursor,myPars,flip,eggTimer,resultCache,restrictRows, iteration);
  }       
  
  public static void displayTextPane(QueryResult myResult, JTextPane myTextPane, String prefix, Color myColor) throws Exception {
    SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
    StyleConstants.setForeground(outputAttrs, myColor);
    StyleConstants.setFontSize(outputAttrs,11);
    StyleConstants.setFontFamily(outputAttrs,"monospaced");  

    myTextPane.setEditorKit(new NoWrapEditorKit());
    myTextPane.setCharacterAttributes(outputAttrs,true);
    
    myTextPane.removeAll();
    
    String outputLine;
    
    if (prefix instanceof String) {
      outputLine = createOutputString(myResult, prefix);
    }
    else {
      outputLine = createOutputString(myResult, true);
    }
    
    myTextPane.setText(outputLine);
  }
  

  
  public static String createOutputString(QueryResult myResult, boolean includeHeader) {
    String[][] resultSet = myResult.getResultSetAsStringArray();
    String[] resultHeadings = myResult.getResultHeadings();
    
    int[] columnWidths = myResult.getColumnWidths();
    int numCols = myResult.getNumCols();
    int numRows = myResult.getNumRows();
    
    StringBuffer outputLine = new StringBuffer();
    // Print the Heading 
    if (includeHeader && numRows > 0) {    
      for (int i=0; i < numCols; i++) {
        outputLine.append(resultHeadings[i]);
        
        for (int j=resultHeadings[i].length(); j < columnWidths[i]; j++) outputLine.append(" ");
        outputLine.append("   ");  // column break 
      }
      
      outputLine.append("\n\n");   // blank line between header and data 
    }
    
    // Print the data 
    for (int row=0; row < numRows; row++) {
      for (int i=0; i < numCols; i++) {
        outputLine.append(resultSet[row][i]);
        
        if (numCols > 1) {
          for (int j=resultSet[row][i].length(); j < columnWidths[i]; j++) outputLine.append(" ");
          outputLine.append("   ");  // column break 
        }
      }
      outputLine.append("\n");     // line break 
    }
    return outputLine.toString();
  }  
  
  public static String createOutputString(QueryResult myResult, String prefix) {
    String[][] resultSet = myResult.getResultSetAsStringArray();
    String[] resultHeadings = myResult.getResultHeadings();
    
    int[] columnWidths = myResult.getColumnWidths();
    int numCols = myResult.getNumCols();
    int numRows = myResult.getNumRows();
    
    StringBuffer outputLine = new StringBuffer();
    
    // Print the prefix
    outputLine.append(prefix);
    outputLine.append("\n\n");   // blank line between prefix and data 
    
    
    // Print the data 
    for (int row=0; row < numRows; row++) {
      for (int i=0; i < numCols; i++) {
        outputLine.append(resultSet[row][i]);
        
        if (numCols > 1) {
          for (int j=resultSet[row][i].length(); j < columnWidths[i]; j++) outputLine.append(" ");
          outputLine.append("   ");  // column break 
        }
      }
      outputLine.append("\n");     // line break 
    }
    return outputLine.toString();
  }  
  
  public static String createOutputString(String sql) {
    
    StringBuffer outputLine = new StringBuffer();
    
    
    
    // Print the data 
    outputLine.append(sql);
    outputLine.append("\n");     // line break 
    
    return outputLine.toString();
  }
  
  /**
   * Create a seperate frame and display the cursor's sql
   *
   * @param cursorId - name of the sql script to other from the jar file
   * @param scrollP - the JScrollPane on to which output will be displayed
   * @throws Exception
   */
   public static void showSQLOnScreen(String sql) {
     try {
       String sqlToDisplay = sql;
       
       if (sql.toUpperCase().indexOf(".SQL") < 0) sqlToDisplay = "Select *\nFrom " + sqlToDisplay;
       // display the sql 
       //JOptionPane.showMessageDialog(scrollP,sqlToDisplay,"Show SQL",JOptionPane.INFORMATION_MESSAGE);
       JFrame sqlF = new JFrame("Show SQL...");
       JTextPane sqlTP = new JTextPane();
       JScrollPane scrollP = new JScrollPane();
       scrollP.getViewport().add(sqlTP);
      // scrollP.getViewport().setSize(200,100);
       sqlF.setPreferredSize(new Dimension(200,100));
      // sqlTP.setPreferredSize(new Dimension(200,100));
       sqlF.add(scrollP);
       sqlF.setLocationRelativeTo(null);
       sqlF.setIconImage(ConnectWindow.getRichMonIcon().getImage());
       sqlF.pack();
       
       
       int verticalPos;
       int horizontalPos;
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize = sqlF.getSize();
         verticalPos = (screenSize.height - frameSize.height) / 2;
         horizontalPos = (screenSize.width - frameSize.width) / 2;
         if (frameSize.height > screenSize.height)
         {
           frameSize.height = screenSize.height;
           verticalPos = screenSize.height;
         }
         if (frameSize.width > screenSize.width)
         {
           frameSize.width = screenSize.width;
           horizontalPos = screenSize.width;
         }
         
      sqlF.setLocation(horizontalPos,verticalPos);   
       
       SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
       StyleConstants.setForeground(outputAttrs, Color.blue);
       StyleConstants.setFontSize(outputAttrs,11);
       StyleConstants.setFontFamily(outputAttrs,"monospaced");  

       sqlTP.setEditorKit(new NoWrapEditorKit());
       sqlTP.setCharacterAttributes(outputAttrs,true);
       
       String outputLine = createOutputString(sql);
       sqlTP.setText(outputLine);
       sqlF.setVisible(true);

     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,ConnectWindow.getConsoleWindow());
     }
   }  
  
  public static void showSQLOnScreen(Cursor myCursor) {
    try {
      String sqlToDisplay;
      
      sqlToDisplay = myCursor.getSQL();
      
      // display the sql 
     // JOptionPane.showMessageDialog(scrollP,sqlToDisplay,"Show SQL",JOptionPane.INFORMATION_MESSAGE);
      
      JFrame sqlF = new JFrame("Show SQL...");
      JTextPane sqlTP = new JTextPane();
      JScrollPane scrollP = new JScrollPane();
      scrollP.getViewport().add(sqlTP);
      //scrollP.getViewport().setSize(200,100);
      sqlF.setPreferredSize(new Dimension(400,600));
      //sqlTP.setPreferredSize(new Dimension(200,100));
      sqlF.add(scrollP);
      sqlF.setIconImage(ConnectWindow.getRichMonIcon().getImage());
      sqlF.pack();
      
      int verticalPos;
      int horizontalPos;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = sqlF.getSize();
        verticalPos = (screenSize.height - frameSize.height) / 2;
        horizontalPos = (screenSize.width - frameSize.width) / 2;
        if (frameSize.height > screenSize.height)
        {
          frameSize.height = screenSize.height;
          verticalPos = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
          frameSize.width = screenSize.width;
          horizontalPos = screenSize.width;
        }
        
        sqlF.setLocation(horizontalPos,verticalPos);      
      
      SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
      StyleConstants.setForeground(outputAttrs, Color.blue);
      StyleConstants.setFontSize(outputAttrs,11);
      StyleConstants.setFontFamily(outputAttrs,"monospaced");  

      sqlTP.setEditorKit(new NoWrapEditorKit());
      sqlTP.setCharacterAttributes(outputAttrs,true);
      
      String outputLine = createOutputString(myCursor.getSQL());
      sqlTP.setText(outputLine);
      sqlF.setVisible(true);
      
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,ConnectWindow.getConsoleWindow());
    }
  }

  /**
   * Take the resultSet from the QueryResult and displays it in a JTable
   * 
   * @param myResult - QueryResult object
   * @param scrollP - the JScrollPane on to which output will be displayed
   */
   public static void displayTable(QueryResult myResult, final JScrollPane scrollP, Boolean showSQL, JLabel statusBar) {
     if (!showSQL) {
       // get the resultSet from the query 
       Vector resultSet = myResult.getResultSetAsVectorOfVectors();
       
       // get the number of columns in the resultSet 
       int numCols = myResult.getNumCols();
       
       // how wide are all the columns 
       int[] columnWidths = myResult.getColumnWidths();
       
       // get the resultSet column headings 
       String[] resultHeadings = myResult.getResultHeadings();
       
       // get the index of columns which should be selectable 
       int[] selectableColumns = myResult.getSelectableColumns();
   
       // create the JTable to display 
       TableModel tm = new TableModel(resultHeadings,resultSet);
       TableSorter sorter = new TableSorter(tm);
       final JTable outputTable = new JTable(sorter);
       sorter.setTableHeader(outputTable.getTableHeader());
       
       outputTable.getTableHeader().setToolTipText("Click to specify sorting");
       outputTable.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
           outputTable_mouseClicked(outputTable, scrollP,e);
         }
       });
       
       outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       outputTable.setCellSelectionEnabled(true);
       outputTable.setGridColor(Color.white);
       outputTable.setForeground(Color.blue);
       outputTable.setFont(new Font("Monospaced", 0, 11));
       
        // set the column widths 
       for (int i = 0;i < numCols;i++) {
         TableColumn column = outputTable.getColumnModel().getColumn(i);
     //        column.setPreferredWidth(columnWidths[i] * 20);
           column.setPreferredWidth(Math.max(columnWidths[i] * 9,40));
         column.setHeaderValue(resultHeadings[i]);
       }
       
       // set JTable column colors 
       ConsoleColumnRenderer ccr = new ConsoleColumnRenderer(Color.red);
       for (int i = 0;i < selectableColumns.length;i++) {
         TableColumn column = outputTable.getColumnModel().getColumn(selectableColumns[i]);
         column.setCellRenderer(ccr);
         ccr.getTableCellRenderComponent(outputTable, column, false, false, 0, 0);
       }
       
       if (myResult.isAttributeColumn()) {
         ConsoleColumnRenderer ccrBlack = new ConsoleColumnRenderer(Color.black);
         TableColumn column = outputTable.getColumnModel().getColumn(0);
         column.setCellRenderer(ccrBlack);
         ccrBlack.getTableCellRenderComponent(outputTable, column, false,false, 0, 0);
       }
       
       // check scrollP is available 
       if (scrollP.getViewport().getName() instanceof String) {
         if (scrollP.getViewport().getName().equals("database")) {
           if (DatabasePanel.isChartDisplayed()) ConsoleWindow.getDatabasePanel().addScrollP();
         }
         else {
           if (scrollP.getViewport().getName().equals("statspack")) {
             StatspackAWRInstancePanel statspackAWRInstancePanel = (StatspackAWRInstancePanel)scrollP.getParent();
             statspackAWRInstancePanel.addScrollP();
   //           if (statspackAWRInstancePanelisChartDisplayed()) statspackAWRInstancePanel().addScrollP();
           }
         }
       }
       
   
       // remove all old results 
       scrollP.getViewport().removeAll();
   
       // display JTable 
       scrollP.getViewport().add(outputTable, null);
       
       // update status bar 
       int ms = (int)myResult.getExecutionTime();
       int numRows = myResult.getNumRows();
       String cursorId = myResult.getCursorId();
       if (myResult.isFlipped()) numRows = 1;
       String executionTime = myResult.getExecutionClockTime();
       if (statusBar instanceof JLabel) {
         String message;
         if (cursorId instanceof String && cursorId.equals("scratch.sql")) {
           message = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime; 
         }
         else {
           message = "Executed in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
           if (myResult.getUsernameFilter() instanceof String) message = message + " filtered by the user " + myResult.getUsernameFilter();
           if (myResult.isFilterByRAC() && !myResult.isOnlyLocalInstanceSelected()) {
             message = message + " and filtered by instance ";
             String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
               for (int i = 0; i < selectedInstances.length; i++) {
               message = message + selectedInstances[i][0];
               if (i < selectedInstances.length -1) message = message + ",";
             }
           }
         }
         if (numRows > Properties.getMaxResultSetSize()) message = message + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties  ";        
         statusBar.setText(message);
       }
     }
     else {
       // showSQL == true
       if (myResult.getCursorId().toUpperCase().indexOf(".SQL") < 0) {
         showSQLOnScreen(myResult.getCursorId());
       }
       else {
         try {
           Cursor myCursor = new Cursor(myResult.getCursorId(), true);
           showSQLOnScreen(myCursor);
         } catch (Exception e) {
           ConsoleWindow.displayError(e,"creating a temp cursor");
         }
       }
     }
   }  
 

  public static void displayTable(QueryResult myResult, StatspackAWRInstancePanel statspackPanel, Boolean showSQL, JLabel statusBar) {
    final JScrollPane scrollP = statspackPanel.addScrollP();
    final StatspackAWRInstancePanel statspackAWRInstancePanel = statspackPanel;
  
    // get the resultSet from the query 
    Vector resultSet = myResult.getResultSetAsVectorOfVectors();
    
    // get the number of columns in the resultSet 
    int numCols = myResult.getNumCols();
    
    // how wide are all the columns 
    int[] columnWidths = myResult.getColumnWidths();
    
    // get the resultSet column headings 
    String[] resultHeadings = myResult.getResultHeadings();
    
    // get the index of columns which should be selectable 
    int[] selectableColumns = myResult.getSelectableColumns();

    // create the JTable to display 
    TableModel tm = new TableModel(resultHeadings,resultSet);
    TableSorter sorter = new TableSorter(tm);
    final JTable outputTable = new JTable(sorter);
    sorter.setTableHeader(outputTable.getTableHeader());
    
    outputTable.getTableHeader().setToolTipText("Click to specify sorting");
    outputTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        try {
          outputTable_mouseClicked(outputTable, scrollP, e, statspackAWRInstancePanel.getDbId(),
                                   statspackAWRInstancePanel.getInstanceNumber(),
                                   statspackAWRInstancePanel.getStartSnapId(),
                                   statspackAWRInstancePanel.getEndSnapId(),
                                   statspackAWRInstancePanel.getSelectedSnapDateRange());
        } catch (NotEnoughSnapshotsException nese) {
          ConsoleWindow.displayError(nese, this);
        }
      }
    });
    
    outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    outputTable.setCellSelectionEnabled(true);
    outputTable.setGridColor(Color.white);
    outputTable.setForeground(Color.blue);
    outputTable.setFont(new Font("Monospaced", 0, 11));
    
     // set the column widths 
    for (int i = 0;i < numCols;i++) {
      TableColumn column = outputTable.getColumnModel().getColumn(i);
  //        column.setPreferredWidth(columnWidths[i] * 20);
        column.setPreferredWidth(Math.max(columnWidths[i] * 9,40));
      column.setHeaderValue(resultHeadings[i]);
    }
    
    // set JTable column colors 
    ConsoleColumnRenderer ccr = new ConsoleColumnRenderer(Color.red);
    for (int i = 0;i < selectableColumns.length;i++) {
      TableColumn column = outputTable.getColumnModel().getColumn(selectableColumns[i]);
      column.setCellRenderer(ccr);
      ccr.getTableCellRenderComponent(outputTable, column, false, false, 0, 0);
    }
    
    if (myResult.isAttributeColumn()) {
      ConsoleColumnRenderer ccrBlack = new ConsoleColumnRenderer(Color.black);
      TableColumn column = outputTable.getColumnModel().getColumn(0);
      column.setCellRenderer(ccrBlack);
      ccrBlack.getTableCellRenderComponent(outputTable, column, false,false, 0, 0);
    }
    
    
    

    

    // remove all old results 
    scrollP.getViewport().removeAll();

    // display JTable 
    scrollP.getViewport().add(outputTable, null);
    
    // update status bar 
    int ms = (int)myResult.getExecutionTime();
    int numRows = myResult.getNumRows();
    String cursorId = myResult.getCursorId();
    if (myResult.isFlipped()) numRows = 1;
    String executionTime = myResult.getExecutionClockTime();
    if (statusBar instanceof JLabel) {
      String message;
      if (cursorId instanceof String && cursorId.equals("scratch.sql")) {
        message = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime; 
      }
      else {
        message = "Executed in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
        if (myResult.getUsernameFilter() instanceof String) message = message + " filtered by the user " + myResult.getUsernameFilter();
        if (myResult.isFilterByRAC() && !myResult.isOnlyLocalInstanceSelected()) {
          message = message + " and filtered by instance ";
          String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
            for (int i = 0; i < selectedInstances.length; i++) {
            message = message + selectedInstances[i][0];
            if (i < selectedInstances.length -1) message = message + ",";
          }
        }
      }
      if (numRows > Properties.getMaxResultSetSize()) message = message + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties  ";        
      statusBar.setText(message);
    }
  }  

  
  /**
   * Take the resultSet from the QueryResult and displays it in a JTable on the OverviewPanel
   * 
   * @param myResult - QueryResult object
   * @param scrollP - the JScrollPane on to which output will be displayed
   * @param CTM - a ConsoleTableModel
   * @param CCM - a ConsoleColumnModel
   * @param statusBar - the status bar which needs updating after execution 
   */
  public static void displayTable(QueryResult myResult, final JScrollPane scrollP, ConsoleTableModel CTM, ConsoleColumnModel CCM, JLabel statusBar) {
    // get the resultSet from the query */
//    Vector resultSet = myResult.getResultSet();   commented out 22/08/06
    
    // get the number of columns in the resultSet 
    int numCols = myResult.getNumCols();
    
    // how wide are all the columns 
    int[] columnWidths = myResult.getColumnWidths();
    
    // get the resultSet column headings 
    String[] resultHeadings = myResult.getResultHeadings();
    
    /* get the index of columns which should be selectable */
    int[] selectableColumns = myResult.getSelectableColumns();
    
    // create the JTable to display 
    final JTable outputTable = new JTable(CTM, CCM);
    outputTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        outputTable_mouseClicked(outputTable, scrollP,e);
      }
    });
    outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    outputTable.setCellSelectionEnabled(true);
    outputTable.setGridColor(Color.white);
    outputTable.setForeground(Color.blue);
    outputTable.setFont(new Font("Monospaced", 0, 11));
    
    // add columns to the JTable and set the column widths 
    if (CCM.getColumnCount() == 0) {
      for (int i = 0;i < numCols;i++) {
        TableColumn column = new TableColumn(i);
        column.setPreferredWidth(Math.max(columnWidths[i] * 9,40));
        column.setHeaderValue(resultHeadings[i]);
        CCM.addColumn(column);
      }
    }
    
    // set JTable column colors 
    ConsoleColumnRenderer ccrRed = new ConsoleColumnRenderer(Color.red);
    for (int i = 0;i < selectableColumns.length;i++) {
      TableColumn column = outputTable.getColumnModel().getColumn(selectableColumns[i]);
      column.setCellRenderer(ccrRed);
      ccrRed.getTableCellRenderComponent(outputTable, column, false, false, 0, 0);
    }
    
    // check scrollP is available on database panel
    if (DatabasePanel.isChartDisplayed()) ConsoleWindow.getDatabasePanel().addScrollP();
  
    // remove all old results  
    scrollP.getViewport().removeAll();

    // display JTable 
    scrollP.getViewport().add(outputTable, null);
    
    // update status bar 
    int ms = (int)myResult.getExecutionTime();
    int numRows = myResult.getNumRows();
    String cursorId = myResult.getCursorId();
    if (myResult.isFlipped()) numRows = 1;
    String executionTime = myResult.getExecutionClockTime();
    if (statusBar instanceof JLabel) {
      String message;
      if (cursorId instanceof String && cursorId.equals("scratch.sql")) {
        message = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime; 
      }
      else {
        message = "Executed in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
        if (myResult.getUsernameFilter() instanceof String) message = message + " filtered by the user " + myResult.getUsernameFilter();
        if (myResult.isFilterByRAC() && !myResult.isOnlyLocalInstanceSelected()) {
          message = message + " and filtered by instance ";
          String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
            for (int i = 0; i < selectedInstances.length; i++) {
            message = message + selectedInstances[i][0];
            if (i < selectedInstances.length -1) message = message + ",";
          }
        }
      }
      if (numRows > Properties.getMaxResultSetSize()) message = message + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties  ";        
      statusBar.setText(message);
    }
  }

 
  /**
   * The mouse can be used to select a cell in a JTable which contains either a 
   * sid <session id> or a wait event.
   * 
   * If the cell contains a sid <session id> then a new sessionPanel will be 
   * created for the selected session.
   * 
   * @param outputTable - the Jtable where this event came from
   * @param scrollP - the JScrollPane the Jtable is located on
   */
  private static void outputTable_mouseClicked(JTable outputTable, JScrollPane scrollP,MouseEvent e) {
    boolean debug = false;
         
    // identify the selected cell 
    int row = outputTable.getSelectedRow();
    int col = outputTable.getSelectedColumn();
    
    // get the cell value 
    String cellValStr;
    
    if (row >= 0 && col >= 0) {
      cellValStr = String.valueOf(outputTable.getValueAt(row,col)).trim();

      String sourceColName = outputTable.getColumnName(col);
      int instanceNum = 0;
      
      // launch a sessionPanel if the selected value is a sid 
      if (sourceColName.equals("SID") || 
          sourceColName.equals("CHILD SID") ||
          sourceColName.equals("PARENT SID") ||
          sourceColName.equals("PX_QCSID")) {
        int cellValInt = Integer.valueOf(cellValStr).intValue();
        
        /*
         * Find the instance id
         * This might be in the previous column if more than one instance has been selected, 
         * or we might have to work it out from the instances menu
         */
        
        // search thru the previous columns for a instance name or instance number
        int instanceNumColumn = 0;
        boolean found = false;
        
        // count number of candidate columns that could identify the target instance
        int numCandidateCols = 0;
        for (int i=0; i < outputTable.getColumnCount(); i++) {
          if ((sourceColName.equals("SID") && (outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST_ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST ID"))) ||
              (sourceColName.equals("PARENT SID") && (outputTable.getColumnName(i).equalsIgnoreCase("PARENT INST_ID"))) ||
              (sourceColName.equals("PX_QCSID") && (outputTable.getColumnName(i).equalsIgnoreCase("PX_QCINST_ID")))) {
                numCandidateCols++;
              }
        }  
        if (debug) System.out.println("Number of Candidate columns is " + numCandidateCols);
        
        int startingColumn = 0;
        if (numCandidateCols > 1) startingColumn = 1;
        for (int i=startingColumn; i<outputTable.getColumnCount(); i++) {
          if (debug) System.out.println(i + "comparing with column: " + outputTable.getColumnName(i));
          if ((sourceColName.equals("SID") && (outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST_ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST ID"))) ||
              (sourceColName.equals("PARENT SID") && (outputTable.getColumnName(i).equalsIgnoreCase("PARENT INST_ID"))) ||
              (sourceColName.equals("PX_QCSID") && (outputTable.getColumnName(i).equalsIgnoreCase("PX_QCINST_ID")))) {
                instanceNumColumn = i;
                found = true;
                break;
              }
        }        
        

        if (found) {
          if (outputTable.getColumnName(instanceNumColumn).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(instanceNumColumn).equalsIgnoreCase("INSTANCE_NAME")) {
            instanceNum = ConsoleWindow.getInstanceNumber(String.valueOf(outputTable.getValueAt(row,instanceNumColumn)));
          }
          else {
            instanceNum = Integer.valueOf(String.valueOf(outputTable.getValueAt(row,instanceNumColumn))).intValue();
          }
        }
        else {
          instanceNum = ConsoleWindow.getThisInstanceNumber();
        }
     
        ConsoleWindow.addSessionPanel(instanceNum,cellValInt);
      }
      
        // launch a teaOff showing the selected sql statement in full (formatted) - 8.0 to 9.2
        if (sourceColName.equals("TOP SQL HASH VALUE")) {
          String cursorId;
          if (ConsoleWindow.getDBVersion() < 10) {
            cursorId = "stats$SqlText.sql";
          }
          else {
            cursorId = "stats$SqlText10.sql";
          }
          
          Parameters myPars = new Parameters();      
          if (ConsoleWindow.getDBVersion() >= 10) {
            myPars.addParameter("String",cellValStr);
          }
          else {
            myPars.addParameter("long",cellValStr);
          }
          
          try {
            QueryResult myResult = execute(cursorId,myPars,false,true,null);
            myResult = reFormatSQL(myResult, cellValStr);
            
            ConsoleWindow.addSQLDetailPanel(cellValStr,myResult,SQLIdPanel.getSQL_IdPane());
          }
          catch (Exception ee) {
            JOptionPane.showMessageDialog(scrollP,"Error fetching sql from perfstat.stats$sqltext/10: " + ee.toString());
          }
        }
        
        // launch a teaOff showing the selected sql statement in full (formatted) - 10 onwards
        if (sourceColName.equals("TOP SQL SQL_ID") || sourceColName.equals("SQL_ID")) {
          
          // check to see if a sql_exec_id exists
          long sqlExecId = 0;
          int startingColumn = 0;
          for (int i=startingColumn; i<outputTable.getColumnCount(); i++) {
          if (outputTable.getColumnName(i).equalsIgnoreCase("SQL_EXEC_ID")) {
                sqlExecId = Long.valueOf(String.valueOf(outputTable.getValueAt(row,i))).longValue();
                break;
              }
          }  
          
          String[] snapDateRange = new String[0];
          createSqlDetailPanel10g(cellValStr,ConsoleWindow.getDatabaseId(),ConsoleWindow.getThisInstanceNumber(),sqlExecId, 0, 0, snapDateRange); 
        }
        
        // create a scratchpanel pre populated with a select statement 
        if (e.getButton() == 3 && sourceColName.equals("TABLE_NAME")) {
          SchemaViewerPanel.openPrePopulatedScratchPanel(cellValStr);
        }
        
        // launch advisor task output panel 
        if (sourceColName.equals("TASK_ID")) {
          createAdvisorTaskPanel(cellValStr);
        }        
        
        // launch advisor task output panel 
        if (sourceColName.equals("Service Name")) {
          String serviceName = cellValStr;
          String hostName = "";
          String portNumber = "";
          String username = "";
          Boolean sid = false;
          if (outputTable.getValueAt(row,col+1) instanceof String & !outputTable.getValueAt(row,col+1).equals("null")) hostName = cellValStr = String.valueOf(outputTable.getValueAt(row,col+1));
          if (outputTable.getValueAt(row,col+2) instanceof String & !outputTable.getValueAt(row,col+2).equals("null")) portNumber = cellValStr = String.valueOf(outputTable.getValueAt(row,col+2));
          if (outputTable.getValueAt(row,col+3) instanceof String & !outputTable.getValueAt(row,col+3).equals("null")) username = cellValStr = String.valueOf(outputTable.getValueAt(row,col+3));
          if (outputTable.getValueAt(row,col+4) instanceof String & outputTable.getValueAt(row,col+4).equals("sid")) sid = true;
          ConnectWindow.populateChosenServiceName(serviceName,hostName,portNumber,username,sid);
        }
        
      }
      else {
        String msg = "Use a left mouse click to select an entry before using right click";
        JOptionPane.showMessageDialog(ConnectWindow.getConsoleWindow(),msg,"Error...",JOptionPane.ERROR_MESSAGE);
      }
    

    }
  
  
  /**
   * The mouse can be used to select a cell in a JTable which contains either a 
   * sid <session id> or a wait event.
   * 
   * If the cell contains a sid <session id> then a new sessionPanel will be 
   * created for the selected session.
   * 
   * @param outputTable - the Jtable where this event came from
   * @param scrollP - the JScrollPane the Jtable is located on
   */
  private static void outputTable_mouseClicked(JTable outputTable, JScrollPane scrollP,MouseEvent e, long dbId,int instanceNumber, int startSnapId, int endSnapId, String[] snapDateRange) {
    boolean debug = false;
                       
    // identify the selected cell 
    int row = outputTable.getSelectedRow();
    int col = outputTable.getSelectedColumn();
    
    // get the cell value 
    String cellValStr;
    
    if (row >= 0 && col >= 0) {
      cellValStr = String.valueOf(outputTable.getValueAt(row,col)).trim();

      String colName = outputTable.getColumnName(col);
      int instanceNum = 0;
      
      // launch a sessionPanel if the selected value is a sid 
      if (colName.equals("SID") || 
          colName.equals("CHILD SID") ||
          colName.equals("PARENT SID")) {
        int cellValInt = Integer.valueOf(cellValStr).intValue();
        
        /*
         * Find the instance id
         * This might be in the previous column if more than one instance has been selected, 
         * or we might have to work it out from the instances menu
         */
        
        // search thru the previous columns for a instance name or instance number
        int instanceNumColumn = 0;
        boolean found = false;
        
        // count number of candidate columns that could identify the target instance
        int numCandidateCols = 0;
        for (int i=0; i < outputTable.getColumnCount(); i++) {
          if (outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST_ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("Parent INST_ID")) {
                numCandidateCols++;
              }
        }  
        if (debug) System.out.println("Number of Candidate columns is " + numCandidateCols);
        
        int startingColumn = 0;
        if (numCandidateCols > 1) startingColumn = 1;
        for (int i=startingColumn; i<outputTable.getColumnCount(); i++) {
          if (debug) System.out.println(i + "comparing with column: " + outputTable.getColumnName(i));
          if (outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NAME") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INSTANCE_NUMBER") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST_ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("INST ID") ||
              outputTable.getColumnName(i).equalsIgnoreCase("PARENT INST_ID")) {
                instanceNumColumn = i;
                found = true;
                break;
              }
        }        
        

        if (found) {
          if (outputTable.getColumnName(instanceNumColumn).equalsIgnoreCase("INSTANCE NAME") ||
              outputTable.getColumnName(instanceNumColumn).equalsIgnoreCase("INSTANCE_NAME")) {
            instanceNum = ConsoleWindow.getInstanceNumber(String.valueOf(outputTable.getValueAt(row,instanceNumColumn)));
          }
          else {
            instanceNum = Integer.valueOf(String.valueOf(outputTable.getValueAt(row,instanceNumColumn))).intValue();
          }
        }
        else {
          instanceNum = instanceNumber;
        }
     
        ConsoleWindow.addSessionPanel(instanceNum,cellValInt);
      }
      
        // launch a tearOff showing the selected sql statement in full (formatted) - 8.0 to 9.2
        if (colName.equals("TOP SQL HASH VALUE")) {
          String cursorId;
          if (ConsoleWindow.getDBVersion() < 10) {
            cursorId = "stats$SqlText.sql";
          }
          else {
            cursorId = "stats$SqlText10.sql";
          }
          
          Parameters myPars = new Parameters();      
          if (ConsoleWindow.getDBVersion() >= 10) {
            myPars.addParameter("String",cellValStr);
          }
          else {
            myPars.addParameter("long",cellValStr);
          }
          
          try {
  //            QueryResult myResult = execute(cursorId,myPars);
            QueryResult myResult = execute(cursorId,myPars,false,true,null);
            myResult = reFormatSQL(myResult, cellValStr);
            
//            JTable outTable = createTable(myResult);            
//            String instanceName = ConsoleWindow.getInstanceName();    
            
            ConsoleWindow.addSQLDetailPanel(cellValStr,myResult,SQLIdPanel.getSQL_IdPane());
          }
          catch (Exception ee) {
            JOptionPane.showMessageDialog(scrollP,"Error fetching sql from perfstat.stats$sqltext/10: " + ee.toString());
          }
        }
        
        // launch a teaOff showing the selected sql statement in full (formatted) - 10 onwards
        if (colName.equals("TOP SQL SQL_ID") || colName.equals("SQL_ID")) {
          
          // check to see if a sql_exec_id exists
          long sqlExecId = 0;
          int startingColumn = 0;
          for (int i=startingColumn; i<outputTable.getColumnCount(); i++) {
          if (outputTable.getColumnName(i).equalsIgnoreCase("SQL_EXEC_ID")) {
                sqlExecId = Long.valueOf(String.valueOf(outputTable.getValueAt(row,i))).longValue();
                break;
              }
          }  
          
          createSqlDetailPanel10g(cellValStr,ConsoleWindow.getDatabaseId(),ConsoleWindow.getThisInstanceNumber(),sqlExecId,startSnapId,endSnapId,snapDateRange); 
        }
        
        // create a scratchpanel pre populated with a select statement 
        if (e.getButton() == 3 && colName.equals("TABLE_NAME")) {
          SchemaViewerPanel.openPrePopulatedScratchPanel(cellValStr);
        }
        
        // launch advisor task output panel 
        if (colName.equals("TASK_ID")) {
          createAdvisorTaskPanel(cellValStr);
        }        
        
        // launch advisor task output panel 
        if (colName.equals("Service Name")) {
          String serviceName = cellValStr;
          String hostName = "";
          String portNumber = "";
          String username = "";
          Boolean sid = false;
          if (outputTable.getValueAt(row,col+1) instanceof String & !outputTable.getValueAt(row,col+1).equals("null")) hostName = cellValStr = String.valueOf(outputTable.getValueAt(row,col+1));
          if (outputTable.getValueAt(row,col+2) instanceof String & !outputTable.getValueAt(row,col+2).equals("null")) portNumber = cellValStr = String.valueOf(outputTable.getValueAt(row,col+2));
          if (outputTable.getValueAt(row,col+3) instanceof String & !outputTable.getValueAt(row,col+3).equals("null")) username = cellValStr = String.valueOf(outputTable.getValueAt(row,col+3));
          if (outputTable.getValueAt(row,col+4) instanceof String & outputTable.getValueAt(row,col+4).equals("sid")) sid = true;
          ConnectWindow.populateChosenServiceName(serviceName,hostName,portNumber,username,sid);
        }
      }
      else {
        String msg = "Use a left mouse click to select an entry before using right click";
        JOptionPane.showMessageDialog(ConnectWindow.getConsoleWindow(),msg,"Error...",JOptionPane.ERROR_MESSAGE);
      }
     
    }

  public static void createSqlDetailPanel10g(String sqlId,long dbId, int instanceNumber,long sqlExecId, int startSnapId, int endSnapId, String[] snapDateRange) {
    ConsoleWindow.addSQLDetailPanel(sqlId, SQLIdPanel.getSQL_IdPane(),dbId,instanceNumber,sqlExecId, startSnapId, endSnapId, snapDateRange);
  }
  
  public static void createAdvisorTaskPanel(String taskId) {  
    AdvisorTaskPanel myAdvisorTaskPanel = new AdvisorTaskPanel(taskId);
  }
  
  /**
   * Update an existing JTable with a new QueryResult.
   * 
   * @param myResult - QueryResult object
   * @param CTM - ConsoleTableModel object
   * @param statusBarL - pointer to the status bar which should be updated with execution details
   */
  public static void updateDisplayedTable(QueryResult myResult,ConsoleTableModel CTM, JLabel statusBarL) {
    // get the resultSet 
    Vector resultSet = myResult.getResultSetAsVectorOfVectors();
    
    // get the resultSet column headings 
    String[] resultSetHeadings = myResult.getResultHeadings();
    
    CTM.updateTable(resultSetHeadings,resultSet);
    
    // update status bar 
    int ms = (int)myResult.getExecutionTime();
    int numRows = myResult.getNumRows();
    String cursorId = myResult.getCursorId();
    if (myResult.isFlipped()) numRows = 1;
    String executionTime = myResult.getExecutionClockTime();
    if (statusBarL instanceof JLabel) {
      String message;
      if (cursorId instanceof String && cursorId.equals("scratch.sql")) {
        message = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime; 
      }
      else {
        message = "Executed in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
        if (myResult.getUsernameFilter() instanceof String) message = message + " filtered by the user " + myResult.getUsernameFilter();
        if (myResult.isFilterByRAC() && !myResult.isOnlyLocalInstanceSelected()) {
          message = message + " and filtered by instance ";
          String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
            for (int i = 0; i < selectedInstances.length; i++) {
            message = message + selectedInstances[i][0];
            if (i < selectedInstances.length -1) message = message + ",";
          }
        }
      }
      if (numRows > Properties.getMaxResultSetSize()) message = message + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties  ";        
      statusBarL.setText(message);
    }    
  }
  
  public static JTable createTable(QueryResult myResult) {
    // get the resultSet from the query 
    Vector resultSet = myResult.getResultSetAsVectorOfVectors();
    
    // get the number of columns in the resultSet 
    int numCols = myResult.getNumCols();
    
    // how wide are all the columns 
    int[] columnWidths = myResult.getColumnWidths();
    
    // get the resultSet column headings 
    String[] resultHeadings = myResult.getResultHeadings();
    
    // get the index of columns which should be selectable 
    int[] selectableColumns = myResult.getSelectableColumns();

    // create the JTable to display 
    TableModel tm = new TableModel(resultHeadings,resultSet);
    TableSorter sorter = new TableSorter(tm);
    final JTable outputTable = new JTable(sorter);
    sorter.setTableHeader(outputTable.getTableHeader());
    
    outputTable.getTableHeader().setToolTipText("Click to specify sorting");
    
    outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    outputTable.setCellSelectionEnabled(true);
    outputTable.setGridColor(Color.white);
    outputTable.setForeground(Color.blue);
    outputTable.setFont(new Font("Monospaced", 0, 11));
    
     /* set the column widths */
    for (int i = 0;i < numCols;i++) {
      TableColumn column = outputTable.getColumnModel().getColumn(i);
      column.setPreferredWidth(columnWidths[i] * 9);
      column.setHeaderValue(resultHeadings[i]);
    }
    
    // set JTable column colors 
    ConsoleColumnRenderer ccr = new ConsoleColumnRenderer(Color.red);
    for (int i = 0;i < selectableColumns.length;i++) {
      TableColumn column = outputTable.getColumnModel().getColumn(selectableColumns[i]);
      column.setCellRenderer(ccr);
      ccr.getTableCellRenderComponent(outputTable, column, false, false, 0, 0);
    }
    
    if (myResult.isAttributeColumn()) {
      ConsoleColumnRenderer ccrBlack = new ConsoleColumnRenderer(Color.black);
      TableColumn column = outputTable.getColumnModel().getColumn(0);
      column.setCellRenderer(ccrBlack);
      ccrBlack.getTableCellRenderComponent(outputTable, column, false,false, 0, 0);
    }
    
    return outputTable;
  }
  
  public static QueryResult reFormatSQL(QueryResult myResult, String cellValStr) {
    // convert myResult into a String 
    StringBuffer sqlStatement = new StringBuffer();
    Vector tmp;
    for (int i=0; i < myResult.getNumRows(); i++) {
      tmp = myResult.getResultSetRow(i);
      sqlStatement.append(tmp.firstElement());
    }
    
    // format the sql statement 
    SQLFormatter mySQLFormatter = new SQLFormatter();
    String[] formattedSQL = mySQLFormatter.formatSQL(sqlStatement.toString());

    QueryResult newResult = new QueryResult();
    
    // convert the String into a vector or vectors and add it to the QueryResult 
    Vector tmpV = new Vector(1);
    for (int i=0; i < formattedSQL.length; i++) {
      tmpV.add(formattedSQL[i]); 
      newResult.addResultRow(tmpV);
      tmpV = new Vector(1);
    }

    // update the QueryResult with the number of Rows 
    newResult.setNumRows(formattedSQL.length);
    
    // update the QueryResult with the column Widths 
    int maxColWidth = 40;
    for (int i=0; i < formattedSQL.length; i++) {
      maxColWidth = Math.max(maxColWidth,formattedSQL[i].length());
    }
    
    int [] colWidths = new int[1];
    colWidths[0] = maxColWidth;
    newResult.setColumnWidths(colWidths);
    newResult.setNumCols(1);
    
    String[] resultHeadings = new String[1];
    resultHeadings[0] =  " SQL Statement (Re-Formatted) - Hash Value / SQL_ID " + cellValStr;

    newResult.setResultHeadings(resultHeadings);
    
    return newResult;
  }
  
  
  public static void addWindowHolder(ChartPanel myChartPanel,String title) {
    if (windowHolderF instanceof JFrame && (windowHolderF != null)) {
      windowHolderTP.add(myChartPanel,title);
   }
    else {
      windowHolderF = new JFrame(ConsoleWindow.getInstanceName() + " : Charts");
      windowHolderF.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e)
          {
            ExecuteDisplay.removeWindowHolder();
          }
        });
        
      windowHolderTP = new JTabbedPane();
      addWindowHolder(myChartPanel,title);
      
      windowHolderF.getContentPane().add(windowHolderTP);

      int width = Properties.getAdditionalWindowWidth();
      int height = Properties.getAdditionalWindowHeight();

      // add the richmon icon to the frame title bar
      windowHolderF.setIconImage(ConnectWindow.getRichMonIcon().getImage());
      
      windowHolderF.setSize(new Dimension(width, height));
      windowHolderF.setVisible(true);
    }
  }  
  
  public static void addWindowHolder(JPanel myPanel,String title) {
      if (windowHolderF instanceof JFrame && (windowHolderF != null)) {
      windowHolderTP.add(myPanel,title);
   }
    else {
      windowHolderF = new JFrame(ConsoleWindow.getInstanceName() + " : Charts");
      windowHolderF.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e)
          {
            ExecuteDisplay.removeWindowHolder();
          }
        });
        
      windowHolderTP = new JTabbedPane();
      addWindowHolder(myPanel,title);
      
      windowHolderF.getContentPane().add(windowHolderTP);
        
      // add the richmon icon to the frame title bar
      windowHolderF.setIconImage(ConnectWindow.getRichMonIcon().getImage());

      int width = Properties.getAdditionalWindowWidth();
      int height = Properties.getAdditionalWindowHeight();

      windowHolderF.setSize(new Dimension(width, height));
      windowHolderF.setVisible(true);
    }
  }
  
  public static void removeWindowHolder() {
    windowHolderF = null;
  }
}