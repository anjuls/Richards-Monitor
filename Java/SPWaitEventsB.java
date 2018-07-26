
/*
 * SPWaitEventsB.java        13.00 15/06/05
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
 * 16/09/05 Richard Wright Determines whether results should be adjusted by the 
 *                         snapshot length by checking the menu item on the console 
 *                         window.
 * 10/10/05 Richard Wright Converted from an extention of JButton to an JComboBox,
 *                         allowing choice of adjusted results from the screen rather 
 *                         than having to make a config parameter change
 * 20/10/05 Richard Wright Made the colors of some more common events consistent
 * 24/10/05 Richard Wright Removed CPU Time from V8 output as is might not be accurate
 * 25/10/05 Richard Wright Do not allow selection of more than 50 snapshots otherwise the 
 *                         domain labels overlap
 * 31/10/05 Richard Wright Allowed the selection of up to 100 snapshots by decreasing 
 *                         the font size of the snapshot label as more snapshots are used.
 * 08/12/05 Richard Wright Error about db restarts now include the date/time of 
 *                         previous restarts
 * 08/12/05 Richard Wright Incorporate support for more than 100 snapshots and 
 *                         snapshots over instance restarts
 * 02/02/06 Richard Wright Moved the chart creation into a seperate method
 * 27/02/06 Richard Wright When creating test wait event output, a call is made to the 
 *                         relevant methods in the performance review button class
 * 06/03/06 Richard Wright Clear the statspack panel before displaying new objects
 * 13/06/06 Richard Wright Put cputime into a long rather than int to avoid overflow errors
 * 14/06/06 Richard Wright Get the dbId and instanceNumber before calculating CPU usage
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 12/09/06 Richard Wright Modified the comment style and error handling
 * 14/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 17/01/07 Richard Wright Hold totalCPU in a float rather than long as the long overflowed
 * 11/03/07 Richard Wright Set chart background White
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 03/10/07 Richard Wright Added the CPU Time 
 * 08/11/07 Richard Wright Removed option to make events chart an area chart
 * 06/12/07 Richard Wright Enhanced for RAC
 * 18/11/08 Richard Wright Modified way of displaying chart
 * 10/12/08 Richard Wright Called displayChart instead of doing it manually each time
 * 29/02/12 Richard Wright Allow query results to be cached
 * 29/02/12 Richard Wright Only gather snapshot runtime when running the time adjusted chart
 * 29/02/12 Richard Wright Modified so that all query results are cached
 * 21/05/12 Richard Wright Added the average wait events chart
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;

import java.io.FileInputStream;

import java.io.ObjectInputStream;

import java.util.Date;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;

import javax.swing.plaf.ProgressBarUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Implements a query to show a summary of each database session.
 */
public class SPWaitEventsB extends JButton {
  JLabel statusBar;     // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;  // The JScrollPane on which output should be displayed 
  String[] options;     // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel

  private DefaultCategoryDataset waitEventsDS;
  private DefaultCategoryDataset cpuTimeDS;
  
  double cpuTimeDSMaxValue = 0;

  String[] distinctEvents = new String[1000]; // record of the distinst events encountered 
  int numDistinctEvents = 0;                  // number of distinct events found 
  int[] snapIdRange;                          // list of the snap_id's in the range to be charted 
  String[] snapDateRange;                     // list of snap dates in the range to be charted 
  boolean timeCorrect = false;                // should be snapshot results be corrected over time 
  String selectedClass = "null";
  
  boolean debug = false;
  boolean debugTime = false;
  Long debugStartTime;
  Long debugLastTime;
  
  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @parem statusBar
   */
  public SPWaitEventsB(StatspackAWRInstancePanel statspackPanel,String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Wait Events");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
    
    debugStartTime = new Date().getTime();
    debugLastTime = new Date ().getTime();
    
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
          listActionPerformed(myList.getSelectedValue(), listFrame, false);
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
  public void listActionPerformed(Object selectedOption, JFrame listFrame, boolean refresh) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    statspackAWRInstancePanel.setLastAction(selection);
    
    if (!statspackAWRInstancePanel.isKeepEventColours()) {
      distinctEvents = new String[1000];
      numDistinctEvents = 0; 
    }
    statspackAWRInstancePanel.setKeepEventColours(false);
    
    try { 
          
      if (selection.equals("F/G Adj Wait Events")) {
        timeCorrect = true;
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createFGWaitEventsChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);

        statspackAWRInstancePanel.displayChart(myChartPanel);
        timeCorrect = false;
      }      
      
      if (selection.equals("F/G Wait Events")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createFGWaitEventsChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
   
      if (selection.equals("F/G Wait Events (Text)")) {
        scrollP.getViewport().removeAll();
        QueryResult myResult = statspackAWRInstancePanel.performanceReviewB.generateWaitEventsTxt();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
    
      if (selection.equals("CPU Time Breakdown (Text)")) {
        QueryResult myResult = getCPUTimeBreakdownTxt();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
      if (selection.equals("Event Histogram")) {
        QueryResult myResult = getWaitEventHistogram();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
      if (selection.equals("CPU Time Breakdown")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createCPUTimeBreakdownChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);

        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("Adj Wait Class")) {
        timeCorrect = true;
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createWaitClassChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
        timeCorrect = false;
      }      
      
      if (selection.equals("Adj IO Events")) {
        timeCorrect = true;
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createIOEventChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
        timeCorrect = false;
      }      
      
      if (selection.equals("Wait Class")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createWaitClassChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }      
      
      if (selection.equals("IO Events")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createIOEventChart(true);
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("F/G Average Wait Events Time")) {
        if (!refresh) selectedClass = "null";
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        
        
        // Get a list of the different wait classes that exist and allow the user to restrict output to just one class
        String cursorId = "listWaitClasses.sql"; 
        QueryResult myResult = ExecuteDisplay.execute(cursorId, false, true, null);
        String[][] resultSet = myResult.getResultSetAsStringArray();
        String[] waitClasses = new String[resultSet.length +1];
        waitClasses[0] = "All Classes";
        for (int i=1; i < resultSet.length; i++) waitClasses[i] = resultSet[i][0];
        
        String message = "This chart will select the top 10 events by total wait time for each bar\n"  +
                         "just like the other wait events charts.  However the value displayed for each\n" +
                         "event is the average wait time.\n";
        if (selectedClass.equals("null")) {
          selectedClass = (String)JOptionPane.showInputDialog(statspackAWRInstancePanel, 
                            message, 
                            "Choose a wait class", 
                            JOptionPane.QUESTION_MESSAGE,
                            null, 
                            waitClasses, 
                            "Wait Class");
        }
        
        JFreeChart myChart = createAverageWaitEventsChart(true,selectedClass);
        ChartPanel myChartPanel = new ChartPanel(myChart);       
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("io done")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createSpecialEventChart(true,"io done");
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("RMAN backup & recovery I/O")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createSpecialEventChart(true,"RMAN backup & recovery I/O");
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("control file parallel write")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createSpecialEventChart(true,"control file parallel write");
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("control file sequential write")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 3) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createSpecialEventChart(true,"control file sequential read");
        ChartPanel myChartPanel = new ChartPanel(myChart);
                
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

      
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult createOutput(boolean txt) throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    statspackAWRInstancePanel.sanityCheckRange();
    
    String cursorId = "";
    if (ConsoleWindow.getDBVersion() < 9.0) {
      if (txt) {
        cursorId = "stats$TopWaitEvents817-2.sql";
      }
      else {
        cursorId = "stats$TopWaitEvents817.sql";        
      }
    }
    
    
    if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false  && ConsoleWindow.getDBVersion() <= 11) {
      if (txt) {
        cursorId = "stats$TopWaitEventsAWR-2-10.sql";
      }
      else {
        cursorId = "stats$TopWaitEventsAWR-10.sql";  
      }
    }
      
    if (ConsoleWindow.getDBVersion() >= 11 && Properties.isAvoid_awr() == false) {
      if (txt) {
        cursorId = "stats$TopWaitEventsAWR-2-11.sql";
      }
      else {
        cursorId = "stats$TopWaitEventsAWR-11.sql";  
      }
    }
      
    if (ConsoleWindow.getDBVersion() >= 9.0 && ConsoleWindow.getDBVersion() <= 10.0) {
      if (txt) {
        cursorId = "stats$TopWaitEvents-2.sql";             
      }
      else {
        cursorId = "stats$TopWaitEvents.sql";                   
      }
    }
    
    int startSnapId = snapIdRange[0];
    int endSnapId = snapIdRange[snapIdRange.length -1];
    
    float dbCPU = 0;
    if (ConsoleWindow.getDBVersion() >= 10) {
      dbCPU = getDBCPU(startSnapId, endSnapId, statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
    }
    if (ConsoleWindow.getDBVersion() >= 9 && ConsoleWindow.getDBVersion() < 10) {
      dbCPU = getTotalCPU(startSnapId, endSnapId, statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
    }
    dbCPU = dbCPU / 10000;
      
    Parameters myPars;
    
    if (ConsoleWindow.getDBVersion() >= 9) {        
      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());        
      myPars.addParameter("float",dbCPU);  
      myPars.addParameter("float",dbCPU);  
    }
    else {
      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    }
    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }
  
  public JFreeChart createFGWaitEventsChart(boolean addTitle) throws Exception {
    
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    waitEventsDS = new DefaultCategoryDataset();
       
    int startSnapId;
    int endSnapId;
    int i = 0;
    int snapshotRunTime;
    
    if (debugTime) {
      debugStartTime = new Date().getTime();
    }
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      String[] eventsThisSnapshot = new String[10];
      snapshotRunTime = -1;
      
      if (timeCorrect) snapshotRunTime = getSnapshotRunTime(startSnapId, endSnapId);
      
      float dbCPU = 0;
      if (ConsoleWindow.getDBVersion() >= 10)  dbCPU = getDBCPU(startSnapId, endSnapId, statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      if (ConsoleWindow.getDBVersion() < 10) dbCPU = getTotalCPU(startSnapId, endSnapId, statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      
      String cursorId = "";
      if (ConsoleWindow.getDBVersion() < 9.0) {
        cursorId = "stats$TopWaitEvents817.sql";
      }

      if (ConsoleWindow.getDBVersion() >= 11 && Properties.isAvoid_awr() == false) {
        cursorId = "stats$TopWaitEventsAWR-11.sql";
      }


      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() <= 11.0) {
        cursorId = "stats$TopWaitEventsAWR-10.sql";
      }
      
      if ((ConsoleWindow.getDBVersion() >= 9.0 && ConsoleWindow.getDBVersion() <= 10.0) || Properties.isAvoid_awr() == true) {
        cursorId = "stats$TopWaitEvents.sql";          
      }
 
      
      Parameters myPars = new Parameters();
      
      if (ConsoleWindow.getDBVersion() >= 9 && ConsoleWindow.getDBVersion() <= 10) {        
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("String",dbCPU);
        myPars.addParameter("String",dbCPU);
      }
      
      if (ConsoleWindow.getDBVersion() >= 10) {        
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("String",dbCPU/10000);
        myPars.addParameter("String",dbCPU/10000);
      }
      
      if (ConsoleWindow.getDBVersion() <= 9) {
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      }
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String event;
      double time;
      for (int j=0; j < myResult.getNumRows(); j++) {
        event = resultSet[j][0];
        time = Double.valueOf(resultSet[j][1]).doubleValue();
        
        if (timeCorrect) time = time / snapshotRunTime;
        
        addResultToDS(event,time,i,j,"WaitEvent");
        eventsThisSnapshot[j] = event;
      }
    
      // add events that did not occur in this snapshot but did in previous
      for (int j = 0; j < numDistinctEvents; j++) {
        // for each event that occured in the past did it occur this time
        boolean found = false;
        for (int k = 0; k < eventsThisSnapshot.length; k++) {
          if (distinctEvents[j].equals(eventsThisSnapshot[k])) {
            found = true;
            break;
          }
        }

        if (!found) {
          double t = 0.0;
          String e = distinctEvents[j];
          addResultToDS(distinctEvents[j], t, i, 9999, "WaitEvent");
        }
      }

      i++;
      
      
      if (debugTime) {
        System.out.println("Wait Events Data Gathering for " + startSnapId + "-" + endSnapId + " took " + debugTime());
      }
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    
    String chartTitle;
    if (timeCorrect) {
      chartTitle = "Time Waited per second of snapshot";
    }
    else 
    {
      chartTitle = "Time Waited (s)";
    }
  
    myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            waitEventsDS,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            true
                          ); 
    if (addTitle) {
      if (timeCorrect) {
        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));      
      }
      else {
  //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
      }
    }

    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));
    
    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }
  public JFreeChart createAverageWaitEventsChart(boolean addTitle,String selectedClass) throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    waitEventsDS = new DefaultCategoryDataset();
  
  
    if (selectedClass.equals("All Classes")) selectedClass = "%";
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    int snapshotRunTime;
    
    if (debugTime) {
      debugStartTime = new Date().getTime();
    }
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      String[] eventsThisSnapshot = new String[10];
      snapshotRunTime = -1;
     
      String cursorId;
      if (ConsoleWindow.getDBVersion() < 11) {
        cursorId = "stats$TopAverageWaitEventsAWR-10.sql";
      }
      else {
        cursorId = "stats$TopAverageWaitEventsAWR-11.sql";
      }
      
      Parameters myPars;
      
      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("String",selectedClass);
  
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),selectedClass);
       
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String event;
      double time;
      for (int j=0; j < myResult.getNumRows(); j++) {
        event = resultSet[j][0];
        time = Double.valueOf(resultSet[j][2]).doubleValue() / Double.valueOf(resultSet[j][1]).doubleValue();
        
        addResultToDS(event,time,i,j,"AverageWaitEvent");
        eventsThisSnapshot[j] = event;
      }
    
      // add events that did not occur in this snapshot but did in previous

      for (int j = 0; j < numDistinctEvents; j++) {
        // for each event that occured in the past did it occur this time
        boolean found = false;
        for (int k = 0; k < eventsThisSnapshot.length; k++) {
          if (distinctEvents[j].equals(eventsThisSnapshot[k])) {
            found = true;
            break;
          }
        }

        if (!found) {
          double t = 0.0;
          String e = distinctEvents[j];
          addResultToDS(distinctEvents[j], t, i, 9999, "AverageWaitEvent");
        }
      }

      i++;

    }
        
    String chartTitle = "Average Time Waited (s)";
  
    JFreeChart myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            waitEventsDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }

  
  public JFreeChart createCPUTimeBreakdownChart(boolean addTitle) throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    
    cpuTimeDS = new DefaultCategoryDataset();
    
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      int startSnapId = snapIdRange[i];
      int endSnapId = snapIdRange[i +1];
            
      String cursorId;
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$CPUTimeBreakDownAWR.sql";        
      }
      else {
        cursorId = "stats$CPUTimeBreakDown.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      String statisticName = "";
      double statisticValue = 0;
      String label;
      
      
      for (int j=0; j < myResult.getNumRows(); j++) {
        statisticName = resultSet[j][0];
        statisticValue = Double.valueOf(resultSet[j][1]).doubleValue();

        label = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
        
        
        cpuTimeDS.addValue(statisticValue,statisticName,label);
        cpuTimeDSMaxValue = Math.max(cpuTimeDSMaxValue,statisticValue);
        
      }  
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              "CPU (s)",
                              cpuTimeDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    ChartPanel myChartPanel = new ChartPanel(myChart);

    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,4));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));      
    myRenderer.setSeriesShape(1,new Rectangle(0,0));      
    myRenderer.setSeriesShape(2,new Rectangle(0,0));      
    myRenderer.setSeriesShape(3,new Rectangle(0,0));      
    myRenderer.setSeriesPaint(0,Color.RED);
    myRenderer.setSeriesPaint(1,Color.BLUE);
    myRenderer.setSeriesPaint(2,Color.YELLOW);
    myRenderer.setSeriesPaint(3,Color.GREEN);
    myPlot.setRenderer(0,myRenderer);
    
    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    myChart.setBackgroundPaint(Color.WHITE);
    
    return myChart;
  }
  
  private void addResultToDS(String event, double time, int indexOfSnap, int rowInResultSet,String chartType) {  
    /* 
    * add all previously seen distinct events to the result set
    * so that if moving the window back and forth the colours don't change
    */ 
    if (indexOfSnap == 0  && indexOfSnap == 0 && rowInResultSet == 0) {
      for (int i=0; i < numDistinctEvents; i++) {
        waitEventsDS.addValue(0.0,distinctEvents[i],snapDateRange[0] + " - " + snapDateRange[1].substring(9));
        if (debug) System.out.println("adding distinct event: " + distinctEvents[i]);
      }
    }
    
    if (indexOfSnap == 0  && numDistinctEvents == 0 && chartType.equals("WaitEvent")) {
         // initialize the dataset with some common entries to ensure chart color's are more consistent
         waitEventsDS.addValue(0.0,"db file sequential read",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "db file sequential read";
         waitEventsDS.addValue(0.0,"db file scattered read",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "db file scattered read";
         waitEventsDS.addValue(0.0,"latch free",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "latch free";
         waitEventsDS.addValue(0.0,"buffer busy waits",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "buffer busy waits";
         waitEventsDS.addValue(0.0,"enqueue",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "enqueue";
         waitEventsDS.addValue(0.0,"log file sync",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "log file sync";
         waitEventsDS.addValue(0.0,"log file parallel write",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "log file parallel write";
         waitEventsDS.addValue(0.0,"db file parallel write",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
         distinctEvents[numDistinctEvents++] = "db file parallel write";
         if (ConsoleWindow.getDBVersion() > 11) {
           waitEventsDS.addValue(0.0,"DB CPU",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
           distinctEvents[numDistinctEvents++] = "DB CPU";      }
         else {
    
           waitEventsDS.addValue(0.0,"CPU time",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
           distinctEvents[numDistinctEvents++] = "CPU time";
    
         }
       }

    if (indexOfSnap == 0  && numDistinctEvents == 0 && chartType.equals("WaitClass")) {
      // initialize the dataset with some common entries to ensure chart color's are more consistent 
      waitEventsDS.addValue(0.0,"System I/O",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "System I/O";
      waitEventsDS.addValue(0.0,"User I/O",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "User I/O";
      waitEventsDS.addValue(0.0,"Administrative",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Administrative";
      waitEventsDS.addValue(0.0,"Application",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Application";
      waitEventsDS.addValue(0.0,"Cluster",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Cluster";
      waitEventsDS.addValue(0.0,"Commit",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Commit";
      waitEventsDS.addValue(0.0,"Concurrency",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Concurrency";
      waitEventsDS.addValue(0.0,"Configuration",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Configuration";
      waitEventsDS.addValue(0.0,"CPU time",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "CPU time";
      waitEventsDS.addValue(0.0,"Network",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Network";
      waitEventsDS.addValue(0.0,"Other",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Other";
      waitEventsDS.addValue(0.0,"Queueing",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Queueing";      
      waitEventsDS.addValue(0.0,"Scheduler",snapDateRange[0] + " - " + snapDateRange[1].substring(9));
      distinctEvents[numDistinctEvents++] = "Scheduler";
    }

    
    // is this the first time we've seen this event 
    boolean found = false;
    for (int i=0; i < this.numDistinctEvents; i++) {
      if (distinctEvents[i].equals(event)) {
        found = true;
        break;
      }
    }
    
    if (!found) {
     // this event has not been seen before 
     distinctEvents[numDistinctEvents++] = event;
     
     // add it to all other previous series 
     double t = 0.0;
     for (int i = 1; i < indexOfSnap; i++) {
       waitEventsDS.addValue(t,event,snapDateRange[i] + " - " + snapDateRange[i+1].substring(9));
     }
    }
    

    
    // add the new value 
    if (indexOfSnap > 0) {
      waitEventsDS.addValue(time,event,snapDateRange[indexOfSnap] + " - " + snapDateRange[indexOfSnap+1].substring(9));
    }
    else {
      if (found) {
        waitEventsDS.setValue(time,event,snapDateRange[0] + " - " + snapDateRange[1].substring(9));
//        System.out.println("first snapshot set a value");
      }
      else {
        waitEventsDS.addValue(time,event,snapDateRange[indexOfSnap] + " - " + snapDateRange[indexOfSnap+1].substring(9));
//        System.out.println("first snapshot added a value");
      }
    }
  }
  

  public float getTotalCPU(int startSnapId, int endSnapId, long dbId, int instanceNumber) {    
    float totalCPU = 0;
  
    try {
      String cursorId;
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$sysstatAWR.sql";
      }
      else {
        cursorId = "stats$sysstat.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String","CPU used by this session");  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", dbId);     
      myPars.addParameter("int", instanceNumber);  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),"CPU used by this session");
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
  
      float tCPU = Float.valueOf(resultSet[1][0]).floatValue() - Float.valueOf(resultSet[0][0]).floatValue();
  //      long tCPU = Long.valueOf(resultSet[1][0]).longValue() - Long.valueOf(resultSet[0][0]).longValue();
      totalCPU = tCPU;    
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return totalCPU;
  }
  
  /**
   * Return the total amount of cpu accumulated between 2 snapshots
   * 
   * @param startSnapId 
   * @param endSnapId 
   * @param dbId 
   * @param instanceNumber 
   * @return int
   */
  public float getDBCPU(int startSnapId, int endSnapId, long dbId, int instanceNumber) {    
    float dbCPU = 0;
  
    try {
      String cursorId;
      if (ConsoleWindow.getDBVersion() < 10 || Properties.isAvoid_awr()) {
        cursorId = "stats$SysTimeModel.sql";
      }
      else {
        cursorId = "stats$SysTimeModelAWR.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String","DB CPU");  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", dbId);     
      myPars.addParameter("int", instanceNumber);  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),"DB CPU");
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      dbCPU = Float.valueOf(resultSet[0][0]).floatValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return dbCPU;
  }
  
  public float getDBTime(int startSnapId, int endSnapId, long dbId, int instanceNumber) {    
    float dbTime = 0;
  
    try {
      String cursorId;
      if (ConsoleWindow.getDBVersion() < 10 || Properties.isAvoid_awr()) {
        cursorId = "stats$SysTimeModel.sql";
      }
      else {
        cursorId = "stats$SysTimeModelAWR.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String","DB time");  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", dbId);     
      myPars.addParameter("int", instanceNumber);  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),"DB time");
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      dbTime = Float.valueOf(resultSet[0][0]).floatValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return dbTime;
  }
  
  /**
   * Returns the number of seconds between 2 snapshots (rounded to the nearest second)
   * 
   * @param startSnapId 
   * @param endSnapId 
   * @return int
   */
  private int getSnapshotRunTime(int startSnapId, int endSnapId) {
    int runTime = 0;
    
    try {
      String cursorId;
      Parameters myPars = new Parameters();
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$SnapShotRunTimeAWR.sql";
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "stats$SnapShotRunTime.sql";
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      
      
//      myPars.addParameter("int",startSnapId);
//      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
//      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
//      myPars.addParameter("int",endSnapId);
//      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
//      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      runTime = Integer.valueOf(resultSet[0][0]).intValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return runTime;
  }
  
  public QueryResult getCPUTimeBreakdownTxt() throws Exception {
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;
    
    if (ConsoleWindow.getDBVersion() >= 10 && !Properties.isAvoid_awr()) {
      cursorId = "stats$CPUTimeBreakDownAWR.sql";
    }
    else {
      cursorId = "stats$CPUTimeBreakDown.sql";
    }
    
    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
     
    
    /*
     * Check whether the result has already been cached before querying the database
     */
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
    return myResult;
  }  
  
  
  public QueryResult getWaitEventHistogram() throws Exception {
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;
    Parameters myPars = new Parameters();
    
    if (ConsoleWindow.getDBVersion() >= 10 && !Properties.isAvoid_awr()) {
      cursorId = "stats$EventHistogramAWR.sql";
      
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
      }
    else {
      cursorId = "stats$EventHistogram.sql";
      
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
    }
       
    /*
     * Check whether the result has already been cached before querying the database
     */
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }
  
  public void addItem(String newOption) {
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
  
  private long debugTime() {
    Long duration = new Date().getTime() - debugLastTime;
    debugLastTime = new Date().getTime();
    
    return duration;
  }
  
  public JFreeChart createWaitClassChart(boolean addTitle) throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    waitEventsDS = new DefaultCategoryDataset();
    
    distinctEvents = new String[1000];
    numDistinctEvents = 0;
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    int snapshotRunTime;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      String[] eventsThisSnapshot = new String[1000];
      snapshotRunTime = -1;
      if (timeCorrect) snapshotRunTime = getSnapshotRunTime(startSnapId, endSnapId);    
      
      float dbCPU = getDBCPU(startSnapId, endSnapId, statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      
      String cursorId = "stats$WaitClassAWR.sql";
    
      Parameters myPars;
             
      myPars = new Parameters();
      myPars.addParameter("String",dbCPU/10000);
      myPars.addParameter("String",dbCPU/10000);
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  

      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String waitClass;
      double time;
      for (int j=0; j < myResult.getNumRows(); j++) {
        waitClass = resultSet[j][0];
        time = Double.valueOf(resultSet[j][1]).doubleValue();
        
        if (timeCorrect) time = time / snapshotRunTime;
        
        addResultToDS(waitClass,time,i,j,"WaitClass");
        eventsThisSnapshot[j] = waitClass;
      }
    
      // add events that did not occur in this snapshot but did in previous 
      for (int j=0; j < numDistinctEvents; j++) {
        // for each event that occured in the past did it occur this time 
        boolean found = false;
        for (int k=0; k < eventsThisSnapshot.length; k++) {
          if (distinctEvents[j].equals(eventsThisSnapshot[k])) {
            found = true;
            break;
          }
        }
        
        if (!found) {
          double t = 0.0;
          String e = distinctEvents[j];
          addResultToDS(distinctEvents[j],t,i,9999,"WaitClass");
        }
      }
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    
    String chartTitle;
    if (timeCorrect) {
      chartTitle = "Time Waited per second of snapshot";
    }
    else 
    {
      chartTitle = "Time Waited (s)";
    }
  
    myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            waitEventsDS,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            true
                          ); 
    if (addTitle) {
      if (timeCorrect) {
        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));      
      }
      else {
  //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
      }
    }

    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));
    
    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }
  
  public JFreeChart createIOEventChart(boolean addTitle) throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    waitEventsDS = new DefaultCategoryDataset();
  
    distinctEvents = new String[1000];
    numDistinctEvents = 0;
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    int snapshotRunTime;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      String[] eventsThisSnapshot = new String[1000];
      snapshotRunTime = -1;
      if (timeCorrect) snapshotRunTime = getSnapshotRunTime(startSnapId, endSnapId);    
      
      String cursorId = "stats$IOEventAWR.sql";
    
      Parameters myPars;
             
      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
    
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String waitClass;
      double time;
      for (int j=0; j < myResult.getNumRows(); j++) {
        waitClass = resultSet[j][0];
        time = Double.valueOf(resultSet[j][1]).doubleValue();
        
        if (timeCorrect) time = time / snapshotRunTime;
        
        addResultToDS(waitClass,time,i,j,"IOEvent");
        eventsThisSnapshot[j] = waitClass;
      }
    
      // add events that did not occur in this snapshot but did in previous 
      for (int j=0; j < numDistinctEvents; j++) {
        // for each event that occured in the past did it occur this time 
        boolean found = false;
        for (int k=0; k < eventsThisSnapshot.length; k++) {
          if (distinctEvents[j].equals(eventsThisSnapshot[k])) {
            found = true;
            break;
          }
        }
        
        if (!found) {
          double t = 0.0;
          String e = distinctEvents[j];
          addResultToDS(distinctEvents[j],t,i,9999,"IOEvent");
        }
      }
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    
    String chartTitle;
    if (timeCorrect) {
      chartTitle = "Time Waited per second of snapshot";
    }
    else 
    {
      chartTitle = "Time Waited (s)";
    }
  
    myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            waitEventsDS,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            true
                          ); 
    if (addTitle) {
      if (timeCorrect) {
        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));      
      }
      else {
  //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
      }
    }

    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));
    
    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }
  
  // called when pete=true etc
  public JFreeChart createSpecialEventChart(boolean addTitle,String eventIn) throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    waitEventsDS = new DefaultCategoryDataset();
  
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    int snapshotRunTime;
    
    if (debugTime) {
      debugStartTime = new Date().getTime();
    }
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      String[] eventsThisSnapshot = new String[10];
      snapshotRunTime = -1;
     
      String cursorId = "stats$SpecialEventAWR.sql";
      
      Parameters myPars;
      
      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("String", eventIn);
  
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),eventIn);
       
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String event;
      double time;
      for (int j=0; j < myResult.getNumRows(); j++) {
        event = resultSet[j][0];
        time = Double.valueOf(resultSet[j][2]).doubleValue() / Double.valueOf(resultSet[j][1]).doubleValue();
        
        addResultToDS(event,time,i,j,"AverageWaitEvent");
        eventsThisSnapshot[j] = event;
      }
    
      // add events that did not occur in this snapshot but did in previous

      for (int j = 0; j < numDistinctEvents; j++) {
        // for each event that occured in the past did it occur this time
        boolean found = false;
        for (int k = 0; k < eventsThisSnapshot.length; k++) {
          if (distinctEvents[j].equals(eventsThisSnapshot[k])) {
            found = true;
            break;
          }
        }

        if (!found) {
          double t = 0.0;
          String e = distinctEvents[j];
          addResultToDS(distinctEvents[j], t, i, 9999, "AverageWaitEvent");
        }
      }

      i++;

    }
        
    String chartTitle = "Average Time Waited (s)";
  
    JFreeChart myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            waitEventsDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }

}