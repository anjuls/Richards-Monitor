/*
 *  SPTablespaceFileB.java        13.22 28/06/06
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
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120 
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 16/10/08 Richard Wright Amended the maths to cope when elapsed time doesn't have any decimal places
 * 05/03/12 Richard Wright Modified to use awrCache
 * 19/03/12 Richard Wright Changed from SPIOB.... added tabelspace growth and tablespace and file io charts
 * 22/07/12 Richard Wright Added Segment Growth and Segment IO Charts
 */
 
 package RichMon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Implements a query to show a summary of each database session.
 */
public class SPTablespaceFileB extends JButton {
  JLabel statusBar;                // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;             // The JScrollPane on which output should be displayed 
  static String[] options;                // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel 

  private LogarithmicAxis IOAxis;

  int[] snapIdRange;  // list of the snap_id's in the range to be charted 
  int numSnapshots;

  int oneSelectedIndex = 1;
  int twoSelectedIndex = 0;
  int threeSelectedIndex = 0;
  int fourSelectedIndex = 0;
  int fiveSelectedIndex = 0;
  int sixSelectedIndex = 0;
  int sevenSelectedIndex = 0;
  int eightSelectedIndex = 0;
  
  private DefaultCategoryDataset oneDS;
  private DefaultCategoryDataset twoDS;
  
  boolean singleAxis = false;
  
  boolean debug = false;
  
  /** 
   * Constructor
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPTablespaceFileB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("File/Tablespace");
    SPTablespaceFileB.options = options;    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  public void actionPerformed() {
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
    statspackAWRInstancePanel.setLastAction(selection);
    try {  
      statspackAWRInstancePanel.clearScrollP();
      snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("Tablespace IO")) {
        QueryResult myResult = getTablespaceIO();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }

      if (selection.equals("File IO")) {
        QueryResult myResult = getFileIO();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
      if (selection.equals("Tablespace Growth")) {
        String[] tablespaces = getTablespaceNames();
        
        if (tablespaces.length > 0) {
          ChartPanel myChartPanel = createTablespaceGrowthChart(tablespaces); 
          statspackAWRInstancePanel.displayChart(myChartPanel);
        }
      }      
        
        if (selection.equals("Tablespace IO Chart")) {
          String[] tablespaces = getTablespaceNames();
          
          if (tablespaces.length > 0) {
            ChartPanel myChartPanel = createTablespaceIOChart(tablespaces); 
            statspackAWRInstancePanel.displayChart(myChartPanel);
          }
        }   
        
      if (selection.equals("File IO Chart")) {
        String[] files = getFileNames();
        
        if (files.length > 0) {
          ChartPanel myChartPanel = createFileIOChart(files); 
          statspackAWRInstancePanel.displayChart(myChartPanel);
        }
      }
        
      if (selection.equals("Segment IO Chart")) {
        String[] schemaAndSegmentNames = getSchemaAndObjectID();
        ChartPanel myChartPanel = createSegmentIOChart(schemaAndSegmentNames); 
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
            
      if (selection.equals("Segment Growth Chart")) {
        String[] schemaAndSegmentNames = getSchemaAndObjectID();
        ChartPanel myChartPanel = createSegmentGrowthChart(schemaAndSegmentNames); 
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getTablespaceIO() throws Exception {
    QueryResult myResult = new QueryResult();
    
    try {
      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();
      
      statspackAWRInstancePanel.sanityCheckRange();
      
      int elapsed = getElapsedTime(startSnapId,endSnapId);
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId = "stats$TablespaceIO.sql";
        }
        else {
          cursorId = "stats$TablespaceIOAWR.sql";          
        }
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 8.17) {
          cursorId = "stats$TablespaceIO.sql";        
        }
        else {
          cursorId = "stats$TablespaceIO816.sql";                  
        }
      }
      
      Parameters myPars = new Parameters();
      
      if (ConsoleWindow.getDBVersion() >= 8.17) {
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          

      }
      else {
        myPars = new Parameters();
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
      }
     
     
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Tablespace IO");
    }
    
    return myResult;
  }

  public QueryResult getFileIO() throws Exception {
    QueryResult myResult = new QueryResult();
    
    try {
      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();
      
      statspackAWRInstancePanel.sanityCheckRange();
      
      int elapsed = getElapsedTime(startSnapId,endSnapId);
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId = "stats$FileIO.sql";
        }
        else {
          cursorId = "stats$FileIOAWR.sql";          
        }
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 8.17) {
          cursorId = "stats$FileIO.sql";        
        }
        else {
          cursorId = "stats$FileIO816.sql";                  
        }
      }
      
      Parameters myPars;
      
      if (ConsoleWindow.getDBVersion() >= 8.17) {
        myPars = new Parameters();
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          

      }
      else {
        myPars = new Parameters();
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());    
      }
     
     
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"File IO");
    }
    
    return myResult;
  }




  
  private int getElapsedTime(int startSnapId, int endSnapId) throws Exception {
    // get the elapsed time in seconds 
    
    String startDate = statspackAWRInstancePanel.getSnapDate(startSnapId);
    String endDate = statspackAWRInstancePanel.getSnapDate(endSnapId);
    String cursorId = "SPDateRange.sql";

    Parameters myPars = new Parameters();
    myPars.addParameter("String",endDate);
    myPars.addParameter("String",startDate);

    if (debug) System.out.println("getElapsedTime(" + startSnapId + "," + endSnapId + ") between " + startDate + " and " + endDate);

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),"getting elapsed Time");
    String[][] resultSet = myResult.getResultSetAsStringArray();


    String elapsedTime = String.valueOf(resultSet[0][0]);
    if (debug) System.out.println("getElapsedTime(" + startSnapId + "," + endSnapId + ") elapsedTime " + elapsedTime); 
                 
    int daysInSecs = Math.abs(86400*Integer.valueOf((elapsedTime.substring(0,Math.max(elapsedTime.indexOf("."),1)))).intValue());
    String fractionOfDayInSecs = "0";
    if (elapsedTime.indexOf(".") > 0) fractionOfDayInSecs = String.valueOf(86400*Double.valueOf((elapsedTime.substring(elapsedTime.indexOf("."),Math.min(10,elapsedTime.length())))).doubleValue());
    int snapshotDurationSecs = daysInSecs + Integer.valueOf(fractionOfDayInSecs.substring(0,Math.max(fractionOfDayInSecs.indexOf("."),1))).intValue();
    
    if (debug) System.out.println("getElapsedTime(" + startSnapId + "," + endSnapId + ") returning " + snapshotDurationSecs); 
    
    return snapshotDurationSecs;
  }
  
  public static void addItem(String newOption) {
    // check to see whether this option already exists
    boolean dup = false;
    for (int i=0; i < options.length; i++) {
      if (options[i].equals(newOption)) {
        dup = true;
        break;
      }
    }
    
    // if its not a duplicate, add it to the options array
    if (!dup) {
      String[] tmp = new String[options.length];
      System.arraycopy(options,0,tmp,0,options.length);
      options = new String[options.length +1];
      System.arraycopy(tmp,0,options,0,tmp.length);
      options[options.length-1] = newOption;
    }
  }
  
  private String[] getSchemaAndObjectID() {
    
    
    // Get a list of schemas from AWR that can be used
    String cursorId = "schemasAWR.sql";
    QueryResult myResult;
    
    String[] schemas = new String[0];
    
    Parameters myPars = new Parameters();
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      schemas = new String[myResult.getNumRows()];
      for (int i=0; i < schemas.length; i++) {
        schemas[i] = resultSet[i][0];
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    
    
    // allow the user to select the required schema from the list
    String s = (String)JOptionPane.showInputDialog(
                        statspackAWRInstancePanel,
                        "Choose the Schema",
                        "Schema",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        schemas,
                        schemas[1]);

   
    // Get a list of the objects for the chosen schema
    cursorId = "objectsAWR.sql";
    
    String[] objects = new String[0];
    
    myPars = new Parameters();
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("String", s);
    
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      objects = new String[myResult.getNumRows()];
      for (int i=0; i < objects.length; i++) {
        objects[i] = resultSet[i][0];
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }


    String[] schemaAndObjectID = new String[2];
    schemaAndObjectID[0] = s;

    // allow the user to select the required object from the list
    String o = (String)JOptionPane.showInputDialog(
                        statspackAWRInstancePanel,
                        "Choose the Object",
                        "Object",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        objects,
                        objects[1]);


   // get the object# for the chosen object
   cursorId = "objectID.sql";
   myPars = new Parameters();
   myPars.addParameter("String", s);
   myPars.addParameter("String", o);
   
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      schemaAndObjectID[1] = resultSet[0][0];
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
   
   return schemaAndObjectID;
   
  }

    private String[] getTablespaceNames() {
      String cursorId;
      QueryResult myResult;
      String[] selectedTablespaces = new String[8];  // statistics to chart 
      int numStats = 0;
      
      // Get a list of valid statistics 
      cursorId = "stats$TablespaceNames.sql";
      
      String[] allTablespaces = {" "};
      
      try {
        myResult = ExecuteDisplay.execute(cursorId,false,true,null);
        String[][] resultSet = myResult.getResultSetAsStringArray();
        
        allTablespaces = new String[myResult.getNumRows() + 1];
        allTablespaces[0] = "None";
        for (int i=1; i < allTablespaces.length; i++) {
          allTablespaces[i] = resultSet[i-1][0];
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this);
      }
      
      // construct the panel to allow a user to choose statistics 
      JPanel stOneP = new JPanel();
      JLabel stOneL = new JLabel("Tablespace: ");
      JComboBox stOneCB = new JComboBox(allTablespaces);
      stOneCB.setSelectedIndex(oneSelectedIndex);
      stOneP.add(stOneL);
      stOneP.add(stOneCB);
      
      JPanel stTwoP = new JPanel();
      JLabel stTwoL = new JLabel("Tablespace: ");
      JComboBox stTwoCB = new JComboBox(allTablespaces);
      stTwoCB.setSelectedIndex(twoSelectedIndex);
      stTwoP.add(stTwoL);
      stTwoP.add(stTwoCB);
      
      JPanel stThreeP = new JPanel();
      JLabel stThreeL = new JLabel("Tablespace: ");
      JComboBox stThreeCB = new JComboBox(allTablespaces);
      stThreeCB.setSelectedIndex(threeSelectedIndex);
      stThreeP.add(stThreeL);
      stThreeP.add(stThreeCB);
      
      JPanel stFourP = new JPanel();
      JLabel stFourL = new JLabel("Tablespace: ");
      JComboBox stFourCB = new JComboBox(allTablespaces);
      stFourCB.setSelectedIndex(fourSelectedIndex);
      stFourP.add(stFourL);
      stFourP.add(stFourCB);
      
      JPanel stFiveP = new JPanel();
      JLabel stFiveL = new JLabel("Tablespace: ");
      JComboBox stFiveCB = new JComboBox(allTablespaces);
      stFiveCB.setSelectedIndex(fiveSelectedIndex);
      stFiveP.add(stFiveL);
      stFiveP.add(stFiveCB);
      
      JPanel stSixP = new JPanel();
      JLabel stSixL = new JLabel("Tablespace: ");
      JComboBox stSixCB = new JComboBox(allTablespaces);
      stSixCB.setSelectedIndex(sixSelectedIndex);
      stSixP.add(stSixL);
      stSixP.add(stSixCB);
      
      JPanel stSevenP = new JPanel();
      JLabel stSevenL = new JLabel("Tablespace: ");
      JComboBox stSevenCB = new JComboBox(allTablespaces);
      stSevenCB.setSelectedIndex(sevenSelectedIndex);
      stSevenP.add(stSevenL);
      stSevenP.add(stSevenCB);
      
      JPanel stEightP = new JPanel();
      JLabel stEightL = new JLabel("Tablespace: ");
      JComboBox stEightCB = new JComboBox(allTablespaces);
      stEightCB.setSelectedIndex(eightSelectedIndex);
      stEightP.add(stEightL);
      stEightP.add(stEightCB);

      
      String msg = "Select up to 8 different tablespaces...";
      Object[] options = {msg, stOneP, stTwoP, stThreeP, stFourP, stFiveP, stSixP, stSevenP, stEightP};
      
      int result = JOptionPane.showOptionDialog(this,options,"Tablespaces...",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
      
      if (result == 0) {
        String statistic = String.valueOf(stOneCB.getSelectedItem());
        oneSelectedIndex = stOneCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;
     
        statistic = String.valueOf(stTwoCB.getSelectedItem());
        twoSelectedIndex = stTwoCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stThreeCB.getSelectedItem());
        threeSelectedIndex = stThreeCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stFourCB.getSelectedItem());
        fourSelectedIndex = stFourCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stFiveCB.getSelectedItem());
        fiveSelectedIndex = stFiveCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stSixCB.getSelectedItem());
        sixSelectedIndex = stSixCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stSevenCB.getSelectedItem());
        sevenSelectedIndex = stSevenCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;   
    
        statistic = String.valueOf(stEightCB.getSelectedItem());
        eightSelectedIndex = stEightCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedTablespaces[numStats++] = statistic;
       
        // resize the statistics array to the correct size 
        String[] tmp = new String[numStats];
        System.arraycopy(selectedTablespaces,0,tmp,0,numStats);
        selectedTablespaces = new String[numStats];
        System.arraycopy(tmp,0,selectedTablespaces,0,numStats);
      }
      else {
        selectedTablespaces = new String[0];
      }
      
      return selectedTablespaces;
    }
    
    private String[] getFileNames() {
      String cursorId;
      QueryResult myResult;
      String[] selectedFiles = new String[8];  // statistics to chart 
      int numStats = 0;
      
      // Get a list of valid statistics 
      cursorId = "stats$FileNames.sql";
      
      String[] allFiles = {" "};
      
      try {
        myResult = ExecuteDisplay.execute(cursorId,false,true,null);
        String[][] resultSet = myResult.getResultSetAsStringArray();
        
        allFiles = new String[myResult.getNumRows() + 1];
        allFiles[0] = "None";
        for (int i=1; i < allFiles.length; i++) {
          allFiles[i] = resultSet[i-1][0];
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this);
      }
      
      // construct the panel to allow a user to choose statistics 
      JPanel stOneP = new JPanel();
      JLabel stOneL = new JLabel("File: ");
      JComboBox stOneCB = new JComboBox(allFiles);
      stOneCB.setSelectedIndex(oneSelectedIndex);
      stOneP.add(stOneL);
      stOneP.add(stOneCB);
      
      JPanel stTwoP = new JPanel();
      JLabel stTwoL = new JLabel("File: ");
      JComboBox stTwoCB = new JComboBox(allFiles);
      stTwoCB.setSelectedIndex(twoSelectedIndex);
      stTwoP.add(stTwoL);
      stTwoP.add(stTwoCB);
      
      JPanel stThreeP = new JPanel();
      JLabel stThreeL = new JLabel("File: ");
      JComboBox stThreeCB = new JComboBox(allFiles);
      stThreeCB.setSelectedIndex(threeSelectedIndex);
      stThreeP.add(stThreeL);
      stThreeP.add(stThreeCB);
      
      JPanel stFourP = new JPanel();
      JLabel stFourL = new JLabel("File: ");
      JComboBox stFourCB = new JComboBox(allFiles);
      stFourCB.setSelectedIndex(fourSelectedIndex);
      stFourP.add(stFourL);
      stFourP.add(stFourCB);
      
      JPanel stFiveP = new JPanel();
      JLabel stFiveL = new JLabel("File: ");
      JComboBox stFiveCB = new JComboBox(allFiles);
      stFiveCB.setSelectedIndex(fiveSelectedIndex);
      stFiveP.add(stFiveL);
      stFiveP.add(stFiveCB);
      
      JPanel stSixP = new JPanel();
      JLabel stSixL = new JLabel("File: ");
      JComboBox stSixCB = new JComboBox(allFiles);
      stSixCB.setSelectedIndex(sixSelectedIndex);
      stSixP.add(stSixL);
      stSixP.add(stSixCB);
      
      JPanel stSevenP = new JPanel();
      JLabel stSevenL = new JLabel("File: ");
      JComboBox stSevenCB = new JComboBox(allFiles);
      stSevenCB.setSelectedIndex(sevenSelectedIndex);
      stSevenP.add(stSevenL);
      stSevenP.add(stSevenCB);
      
      JPanel stEightP = new JPanel();
      JLabel stEightL = new JLabel("File: ");
      JComboBox stEightCB = new JComboBox(allFiles);
      stEightCB.setSelectedIndex(eightSelectedIndex);
      stEightP.add(stEightL);
      stEightP.add(stEightCB);

      
      String msg = "Select up to 8 different Files...";
      Object[] options = {msg, stOneP, stTwoP, stThreeP, stFourP, stFiveP, stSixP, stSevenP, stEightP};
      
      int result = JOptionPane.showOptionDialog(this,options,"Files...",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
      
      if (result == 0) {
        String statistic = String.valueOf(stOneCB.getSelectedItem());
        oneSelectedIndex = stOneCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;
     
        statistic = String.valueOf(stTwoCB.getSelectedItem());
        twoSelectedIndex = stTwoCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stThreeCB.getSelectedItem());
        threeSelectedIndex = stThreeCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stFourCB.getSelectedItem());
        fourSelectedIndex = stFourCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stFiveCB.getSelectedItem());
        fiveSelectedIndex = stFiveCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stSixCB.getSelectedItem());
        sixSelectedIndex = stSixCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stSevenCB.getSelectedItem());
        sevenSelectedIndex = stSevenCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;   
    
        statistic = String.valueOf(stEightCB.getSelectedItem());
        eightSelectedIndex = stEightCB.getSelectedIndex();
        if (!statistic.equals("None")) selectedFiles[numStats++] = statistic;
       
        // resize the statistics array to the correct size 
        String[] tmp = new String[numStats];
        System.arraycopy(selectedFiles,0,tmp,0,numStats);
        selectedFiles = new String[numStats];
        System.arraycopy(tmp,0,selectedFiles,0,numStats);
      }
      else {
        selectedFiles = new String[0];
      }
      
      return selectedFiles;
    }

  public ChartPanel createTablespaceGrowthChart(String[] tablespaceNames) throws Exception {
    
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    String[] snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();

    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      endSnapId = snapIdRange[i +1];
            
      String cursorId;
      
//      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        cursorId = "stats$TablespaceGrowthAWR.sql";        
//      }
//      else {
//        cursorId = "stats$Statistics.sql";
//      }
      
      
      QueryResult myResult;
      String[][] resultSet;
      String tablespaceName = "";
      long tablespaceSize = 0;
      long tablespaceUsedSize = 0;
      String label;
      
      for (int j=0; j < tablespaceNames.length; j++) {
        
        Parameters myPars = new Parameters();
        myPars.addParameter("String",tablespaceNames[j]);
        myPars.addParameter("int",endSnapId);
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, 0, endSnapId, statspackAWRInstancePanel.getInstanceName(),tablespaceNames[j]);
        
        resultSet = myResult.getResultSetAsStringArray();

        tablespaceName = resultSet[0][0];
        tablespaceSize = Long.valueOf(resultSet[0][1]).longValue();
        tablespaceUsedSize = Long.valueOf(resultSet[0][2]).longValue();
        
        label = snapDateRange[i+1];
        
        oneDS.addValue(tablespaceSize,tablespaceName+"-Size",label);  
        oneDS.addValue(tablespaceUsedSize,tablespaceName+"-Used",label);

      }  
      i++;
    }
    
    String rangeAxisLabel = tablespaceNames[0];
    rangeAxisLabel = "Size in mb";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    myRenderer.setSeriesShape(1,new Rectangle(0,0));    
    myRenderer.setSeriesStroke(1,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                 1.0f, new float[] {3.0f, 3.0f}, 0.0f));
    myRenderer.setSeriesPaint(0,Color.RED);
    myRenderer.setSeriesPaint(1,Color.RED);
    
    if (tablespaceNames.length > 1) {
      myRenderer.setSeriesShape(2,new Rectangle(0,0));
      myRenderer.setSeriesShape(3,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(3,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(2,Color.BLUE);
      myRenderer.setSeriesPaint(3,Color.BLUE);
    }
    
    if (tablespaceNames.length > 2) {
      myRenderer.setSeriesShape(4,new Rectangle(0,0));
      myRenderer.setSeriesShape(5,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(5,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(4,Color.ORANGE);
      myRenderer.setSeriesPaint(5,Color.ORANGE);
    }    
    
    if (tablespaceNames.length > 3) {
      myRenderer.setSeriesShape(6,new Rectangle(0,0));
      myRenderer.setSeriesShape(7,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(7,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(6,Color.GREEN);
      myRenderer.setSeriesPaint(7,Color.GREEN);
    }
    
    if (tablespaceNames.length > 4) {
      myRenderer.setSeriesShape(8,new Rectangle(0,0));
      myRenderer.setSeriesShape(9,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(9,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(8,Color.MAGENTA);
      myRenderer.setSeriesPaint(9,Color.MAGENTA);
    }    
    
    if (tablespaceNames.length > 5) {
      myRenderer.setSeriesShape(10,new Rectangle(0,0));
      myRenderer.setSeriesShape(11,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(11,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(10,Color.PINK);
      myRenderer.setSeriesPaint(11,Color.PINK);
    }
    
    if (tablespaceNames.length > 6) {
      myRenderer.setSeriesShape(12,new Rectangle(0,0));
      myRenderer.setSeriesShape(13,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(13,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(12,Color.LIGHT_GRAY);
      myRenderer.setSeriesPaint(13,Color.LIGHT_GRAY);
    }
    
    if (tablespaceNames.length > 7) {
      myRenderer.setSeriesShape(14,new Rectangle(0,0));
      myRenderer.setSeriesShape(15,new Rectangle(0,0));    
      myRenderer.setSeriesStroke(15,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer.setSeriesPaint(14,Color.BLACK);
      myRenderer.setSeriesPaint(15,Color.BLACK);
    }
       
    myPlot.setRenderer(0,myRenderer);

    // set axis color scheme 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));    

    myAxis.setLowerBound(0.0);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }


  public ChartPanel createSegmentGrowthChart(String[] schemaAndObjectID) throws Exception {
    
    
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    String[] snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      
      endSnapId = snapIdRange[i +1];      
      startSnapId = snapIdRange[i];
      
      
      statspackAWRInstancePanel.sanityCheckRange();
      
    
      String cursorId;
      cursorId = "segmentGrowthAWR.sql";        

      
      QueryResult myResult;
      String[][] resultSet;

      long spaceUsedTotal = 0;
      long spaceAllocatedTotal=0;
      
      String label;
      
       
      Parameters myPars = new Parameters();
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("String",schemaAndObjectID[1]);
      
      // check whether leaving the last parameter null works as expected or causes problems with the check refresh using the wrong data
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),schemaAndObjectID[1]);
      resultSet = myResult.getResultSetAsStringArray();

      if (myResult.getNumRows() > 0) {
        spaceUsedTotal = Long.valueOf(resultSet[0][0].trim()).longValue();
        spaceAllocatedTotal = Long.valueOf(resultSet[0][1].trim()).longValue();
      }
      else {
        spaceUsedTotal = 0;
        spaceAllocatedTotal=0;
      }
      
      label = snapDateRange[i+1];

      oneDS.addValue(spaceUsedTotal, "space used", label);
      oneDS.addValue(spaceAllocatedTotal, "space allocated", label);

       
      i++;
    }
    
    
    String rangeAxisLabel = "space";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
       
    // set the line shape 
    CategoryItemRenderer myRenderer1 = new LineAndShapeRenderer();
    myRenderer1.setSeriesShape(0,new Rectangle(0,0));
    myRenderer1.setSeriesShape(1,new Rectangle(0,0));
    myPlot.setRenderer(0,myRenderer1);


    // set axis color scheme 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));    

    myAxis.setLowerBound(0.0);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }

  private void setupAxis(int i,CategoryPlot myPlot,DefaultCategoryDataset axisDS,String axisPos,String axisName) {
    // add the dataset to the plot 
    myPlot.setDataset(i,axisDS);
    
    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    
    myAxis.setLabel(axisName);

    // make axis increment in integers 
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);

    myPlot.setRangeAxis(i,myAxis);
    if (axisPos.equals("RIGHT")) {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_RIGHT);
    }
    else {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_LEFT);          
    }
    myPlot.mapDatasetToRangeAxis(i,i); 
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
//    myRenderer.setSeriesPaint(0,Color.BLACK);
    myPlot.setRenderer(i,myRenderer);
    
    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
//    myAxis.setLabelPaint(axisPaint);
//    myAxis.setTickLabelPaint(axisPaint);
//    myAxis.setAxisLinePaint(axisPaint);     
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    myAxis.setLowerBound(0.0);
  }
  
  public ChartPanel createTablespaceIOChart(String[] tablespaceNames) throws Exception {
    
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    String[] snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();
    twoDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      
      endSnapId = snapIdRange[i +1];      
      startSnapId = snapIdRange[i];
      
      
      statspackAWRInstancePanel.sanityCheckRange();
      
      int elapsed = getElapsedTime(startSnapId,endSnapId);
      
      String cursorId;
      cursorId = "stats$TablespaceIOChartAWR.sql";        

      
      QueryResult myResult;
      String[][] resultSet;
      String tablespaceName = "";
      double tsReadsPerSecDouble = 0;
      double tsAverageReadTimeInMSDouble = 0;
      long tsReadsPerSecLong = 0;
      long tsAverageReadTimeInMSLong = 0;
      String label;
      
      for (int j=0; j < tablespaceNames.length; j++) {
        
        Parameters myPars = new Parameters();
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());    
        myPars.addParameter("String", tablespaceNames[j]);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        myPars.addParameter("String", tablespaceNames[j]);
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),tablespaceNames[j]);
        
        if (debug) {
          System.out.println(cursorId + " for ts " + tablespaceNames[j] + " returned " + myResult.getNumRows() + " rows for snapshots period " + startSnapId + "-" + endSnapId);
        }
        
        resultSet = myResult.getResultSetAsStringArray();
        
        if (debug) {
          System.out.println(cursorId + " for ts " + tablespaceNames[j] + " returned " + resultSet[0][0] + " : " + resultSet[0][1] + " : " + resultSet[0][2]);
        }

        boolean usingDouble = true;
        tablespaceName = resultSet[0][0];
        try {
          tsReadsPerSecDouble = Double.valueOf(resultSet[0][1].trim()).doubleValue();
          tsAverageReadTimeInMSDouble = Double.valueOf(resultSet[0][2].trim()).doubleValue();
        }
        catch (NumberFormatException nfe) {
          tsReadsPerSecLong = Long.valueOf(resultSet[0][1].trim()).longValue();
          tsAverageReadTimeInMSLong = Long.valueOf(resultSet[0][2].trim()).longValue();
          usingDouble = false;
        }
        
        label = snapDateRange[i+1];

        if (usingDouble) {
          oneDS.addValue(tsReadsPerSecDouble, tablespaceName + " -reads per sec", label);
          twoDS.addValue(tsAverageReadTimeInMSDouble, tablespaceName + " -av read (ms)", label);
        }
        else {
          oneDS.addValue(tsReadsPerSecLong, tablespaceName + " -reads per sec", label);
          twoDS.addValue(tsAverageReadTimeInMSLong, tablespaceName + " -av read (ms)", label);
        }

      }  
      i++;
    }
    
    String rangeAxisLabel = tablespaceNames[0];
    rangeAxisLabel = "reads per second";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    setupAxis(1, myPlot, twoDS, "RIGHT", "average read time (ms)");
    
    // set the line shape 
    CategoryItemRenderer myRenderer1 = new LineAndShapeRenderer();
    CategoryItemRenderer myRenderer2 = new LineAndShapeRenderer();
    myRenderer1.setSeriesShape(0,new Rectangle(0,0));
    myRenderer2.setSeriesShape(0,new Rectangle(0,0));
    myRenderer2.setSeriesStroke(0,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                 1.0f, new float[] {3.0f, 3.0f}, 0.0f));
    myRenderer1.setSeriesPaint(0,Color.RED);
    myRenderer2.setSeriesPaint(0,Color.RED);
    
    if (tablespaceNames.length > 1) {
      myRenderer1.setSeriesShape(1,new Rectangle(0,0));
      myRenderer2.setSeriesShape(1,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(1,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(1,Color.BLUE);
      myRenderer2.setSeriesPaint(1,Color.BLUE);
    }
    
    if (tablespaceNames.length > 2) {
      myRenderer1.setSeriesShape(2,new Rectangle(0,0));
      myRenderer2.setSeriesShape(2,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(2,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(2,Color.ORANGE);
      myRenderer2.setSeriesPaint(2,Color.ORANGE);
    }    
    
    if (tablespaceNames.length > 3) {
      myRenderer1.setSeriesShape(3,new Rectangle(0,0));    
      myRenderer2.setSeriesShape(3,new Rectangle(0,0));    
      myRenderer2.setSeriesStroke(3,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(3,Color.GREEN);
      myRenderer2.setSeriesPaint(3,Color.GREEN);
    }
    
    if (tablespaceNames.length > 4) {
      myRenderer1.setSeriesShape(4,new Rectangle(0,0));   
      myRenderer2.setSeriesShape(4,new Rectangle(0,0));   
      myRenderer2.setSeriesStroke(4,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(4,Color.MAGENTA);
      myRenderer2.setSeriesPaint(4,Color.MAGENTA);
    }    
    
    if (tablespaceNames.length > 5) {
      myRenderer1.setSeriesShape(5,new Rectangle(0,0));  
      myRenderer2.setSeriesShape(5,new Rectangle(0,0));  
      myRenderer2.setSeriesStroke(5,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(5,Color.CYAN);
      myRenderer2.setSeriesPaint(5,Color.CYAN);
    }
    
    if (tablespaceNames.length > 6) {
      myRenderer1.setSeriesShape(6,new Rectangle(0,0));  
      myRenderer2.setSeriesShape(6,new Rectangle(0,0));  
      myRenderer2.setSeriesStroke(6,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(6,Color.LIGHT_GRAY);
      myRenderer2.setSeriesPaint(6,Color.LIGHT_GRAY);
    }
    
    if (tablespaceNames.length > 7) {
      myRenderer1.setSeriesShape(7,new Rectangle(0,0));
      myRenderer2.setSeriesShape(7,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(7,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(7,Color.BLACK);
      myRenderer2.setSeriesPaint(7,Color.BLACK);
    }
       
    myPlot.setRenderer(0,myRenderer1);
    myPlot.setRenderer(1,myRenderer2);

    // set axis color scheme 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));    

    myAxis.setLowerBound(0.0);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }
  
  public ChartPanel createFileIOChart(String[] fileNames) throws Exception {
    
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    String[] snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();
    twoDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      
      endSnapId = snapIdRange[i +1];      
      startSnapId = snapIdRange[i];
      
      
      statspackAWRInstancePanel.sanityCheckRange();
      
      int elapsed = getElapsedTime(startSnapId,endSnapId);
      
      String cursorId;
      cursorId = "stats$FileIOChartAWR.sql";        

      
      QueryResult myResult;
      String[][] resultSet;
      String tablespaceName = "";
      double tsReadsPerSecDouble = 0;
      double tsAverageReadTimeInMSDouble = 0;
      long tsReadsPerSecLong = 0;
      long tsAverageReadTimeInMSLong = 0;
      String label;
      
      for (int j=0; j < fileNames.length; j++) {
        
        Parameters myPars = new Parameters();
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());    
        myPars.addParameter("String", fileNames[j]);
        myPars.addParameter("int",elapsed);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        myPars.addParameter("String", fileNames[j]);
        
        if (debug) System.out.println("about to run " + cursorId + " for "+ fileNames[j]);
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),fileNames[j]);
        
        if (debug) {
          System.out.println(cursorId + " for " + fileNames[j] + " returned " + myResult.getNumRows() + " rows for snapshots period " + startSnapId + "-" + endSnapId);
        }
        
        resultSet = myResult.getResultSetAsStringArray();
        
        if (debug) {
          System.out.println(cursorId + " for " + fileNames[j] + " returned " + resultSet[0][0] + " : " + resultSet[0][1] + " : " + resultSet[0][2]);
        }

        boolean usingDouble = true;
        tablespaceName = resultSet[0][0];
        try {
          tsReadsPerSecDouble = Double.valueOf(resultSet[0][1].trim()).doubleValue();
          tsAverageReadTimeInMSDouble = Double.valueOf(resultSet[0][2].trim()).doubleValue();
        }
        catch (NumberFormatException nfe) {
          tsReadsPerSecLong = Long.valueOf(resultSet[0][1].trim()).longValue();
          tsAverageReadTimeInMSLong = Long.valueOf(resultSet[0][2].trim()).longValue();
          usingDouble = false;
        }
        
        label = snapDateRange[i+1];

        if (usingDouble) {
          oneDS.addValue(tsReadsPerSecDouble, tablespaceName + " -reads per sec", label);
          twoDS.addValue(tsAverageReadTimeInMSDouble, tablespaceName + " -av read (ms)", label);
        }
        else {
          oneDS.addValue(tsReadsPerSecLong, tablespaceName + " -reads per sec", label);
          twoDS.addValue(tsAverageReadTimeInMSLong, tablespaceName + " -av read (ms)", label);
        }

      }  
      i++;
    }
    
    
    String rangeAxisLabel = fileNames[0];
    rangeAxisLabel = "reads per second";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    setupAxis(1, myPlot, twoDS, "RIGHT", "average read time (ms)");
    
    // set the line shape 
    CategoryItemRenderer myRenderer1 = new LineAndShapeRenderer();
    CategoryItemRenderer myRenderer2 = new LineAndShapeRenderer();
    myRenderer1.setSeriesShape(0,new Rectangle(0,0));
    myRenderer2.setSeriesShape(0,new Rectangle(0,0));
    myRenderer2.setSeriesStroke(0,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                 1.0f, new float[] {3.0f, 3.0f}, 0.0f));
    myRenderer1.setSeriesPaint(0,Color.RED);
    myRenderer2.setSeriesPaint(0,Color.RED);
    
    if (fileNames.length > 1) {
      myRenderer1.setSeriesShape(1,new Rectangle(0,0));
      myRenderer2.setSeriesShape(1,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(1,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(1,Color.BLUE);
      myRenderer2.setSeriesPaint(1,Color.BLUE);
    }
    
    if (fileNames.length > 2) {
      myRenderer1.setSeriesShape(2,new Rectangle(0,0));
      myRenderer2.setSeriesShape(2,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(2,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(2,Color.ORANGE);
      myRenderer2.setSeriesPaint(2,Color.ORANGE);
    }    
    
    if (fileNames.length > 3) {
      myRenderer1.setSeriesShape(3,new Rectangle(0,0));    
      myRenderer2.setSeriesShape(3,new Rectangle(0,0));    
      myRenderer2.setSeriesStroke(3,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(3,Color.GREEN);
      myRenderer2.setSeriesPaint(3,Color.GREEN);
    }
    
    if (fileNames.length > 4) {
      myRenderer1.setSeriesShape(4,new Rectangle(0,0));   
      myRenderer2.setSeriesShape(4,new Rectangle(0,0));   
      myRenderer2.setSeriesStroke(4,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                   1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(4,Color.MAGENTA);
      myRenderer2.setSeriesPaint(4,Color.MAGENTA);
    }    
    
    if (fileNames.length > 5) {
      myRenderer1.setSeriesShape(5,new Rectangle(0,0));  
      myRenderer2.setSeriesShape(5,new Rectangle(0,0));  
      myRenderer2.setSeriesStroke(5,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(5,Color.CYAN);
      myRenderer2.setSeriesPaint(5,Color.CYAN);
    }
    
    if (fileNames.length > 6) {
      myRenderer1.setSeriesShape(6,new Rectangle(0,0));  
      myRenderer2.setSeriesShape(6,new Rectangle(0,0));  
      myRenderer2.setSeriesStroke(6,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(6,Color.LIGHT_GRAY);
      myRenderer2.setSeriesPaint(6,Color.LIGHT_GRAY);
    }
    
    if (fileNames.length > 7) {
      myRenderer1.setSeriesShape(7,new Rectangle(0,0));
      myRenderer2.setSeriesShape(7,new Rectangle(0,0));
      myRenderer2.setSeriesStroke(7,new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                    1.0f, new float[] {3.0f, 3.0f}, 0.0f));
      myRenderer1.setSeriesPaint(7,Color.BLACK);
      myRenderer2.setSeriesPaint(7,Color.BLACK);
    }
       
    myPlot.setRenderer(0,myRenderer1);
    myPlot.setRenderer(1,myRenderer2);

    // set axis color scheme 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));    

    myAxis.setLowerBound(0.0);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }


  public ChartPanel createSegmentIOChart(String[] schemaAndObjectID) throws Exception {
   
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    String[] snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      
      endSnapId = snapIdRange[i +1];      
      startSnapId = snapIdRange[i];
      
      
      statspackAWRInstancePanel.sanityCheckRange();
      
    
      String cursorId;
      cursorId = "segmentIOAWR.sql";        

      
      QueryResult myResult;
      String[][] resultSet;

      long logicalReadsDelta = 0;
      long physicalReadsDelta=0;
      long physicalWritesDelta=0;
      long physicalReadsDirectDelta=0;
      long physicalWritesDirectDelta=0;
      long tableScansDelta=0;
      
      String label;
      
       
      Parameters myPars = new Parameters();
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("String",schemaAndObjectID[1]);
      
      // check whether leaving the last parameter null works as expected or causes problems with the check refresh using the wrong data
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),schemaAndObjectID[1]);
      resultSet = myResult.getResultSetAsStringArray();

      if (myResult.getNumRows() > 0) {
        logicalReadsDelta = Long.valueOf(resultSet[0][0].trim()).longValue();
        physicalReadsDelta = Long.valueOf(resultSet[0][1].trim()).longValue();
        physicalWritesDelta = Long.valueOf(resultSet[0][2].trim()).longValue();
        physicalReadsDirectDelta = Long.valueOf(resultSet[0][3].trim()).longValue();
        physicalWritesDirectDelta = Long.valueOf(resultSet[0][4].trim()).longValue();
        tableScansDelta = Long.valueOf(resultSet[0][5].trim()).longValue();
      }
      else {
        logicalReadsDelta = 0;
        physicalReadsDelta=0;
        physicalWritesDelta=0;
        physicalReadsDirectDelta=0;
        physicalWritesDirectDelta=0;
        tableScansDelta=0;
      }
      
      label = snapDateRange[i+1];

      oneDS.addValue(logicalReadsDelta, "logical reads", label);
      oneDS.addValue(physicalReadsDelta, "physical reads", label);
      oneDS.addValue(physicalWritesDelta, "physical writes", label);
      oneDS.addValue(physicalReadsDirectDelta, "physical reads direct", label);
      oneDS.addValue(physicalWritesDirectDelta, "physical writes direct", label);
      oneDS.addValue(tableScansDelta, "table scans", label);

       
      i++;
    }
    
    
    String rangeAxisLabel = "reads / writes";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
       
    // set the line shape 
    CategoryItemRenderer myRenderer1 = new LineAndShapeRenderer();
    myRenderer1.setSeriesShape(0,new Rectangle(0,0));
    myRenderer1.setSeriesShape(1,new Rectangle(0,0));
    myRenderer1.setSeriesShape(2,new Rectangle(0,0));
    myRenderer1.setSeriesShape(3,new Rectangle(0,0));
    myRenderer1.setSeriesShape(4,new Rectangle(0,0));
    myRenderer1.setSeriesShape(5,new Rectangle(0,0));
//    myRenderer1.setSeriesPaint(0,Color.RED);
//    myRenderer1.setSeriesPaint(1,Color.BLUE);
    myPlot.setRenderer(0,myRenderer1);


    // set axis color scheme 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));    

    myAxis.setLowerBound(0.0);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }

}
