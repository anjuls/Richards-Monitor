/*
 * SPASH.java        13.02 15/08/05
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
 * 05/06/15 Richard Wright fixed so that all cached results have a unique name
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import javax.swing.ProgressMonitor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Analyze ASH Data
 */
public class SPASH extends JButton {
  JLabel statusBar;         // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;      // The JScrollPane on which output should be displayed 
  static String[] options;         // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel; // reference to the parent panel 
  int[] snapIdRange;                          // list of the snap_id's in the range to be charted 
  String[] snapDateRange; 
  DefaultCategoryDataset chartDS = new DefaultCategoryDataset();
  String[] distinctSQLIds = new String[1000]; // record of the distinst sqlId's encountered 
  int numDistinctSQLIds = 0;                  // number of distinct sqlId's found 
  long benchmark;            // used to calculate percentage values 
  
  boolean debug = false;      // debug mode
  
  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @parem statusBar 
   */
   public SPASH(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("ASH");
    SPASH.options = options;
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
      statspackAWRInstancePanel.sanityCheckRange();
         
      if (selection.equals("Detail for a single SQL_ID")) {
        ashSQLDetailSingleSQLID();
      }
           
      if (selection.equals("Summary for a single SQL_ID")) {
        ashSQLSummarySingleSQLID();
      }  
      

      
      if (selection.equals("Summary Chart for a single SQL_ID")) {
        JFreeChart myChart = ashSQLSummaryChartSQL_IDAWR();
        ChartPanel myChartPanel = new ChartPanel(myChart);

        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
           
      if (selection.equals("Detail for a single Event")) {
        ashSQLDetailSingleEvent();
      } 
      
      if (selection.equals("Summary for a single Event")) {
        ashSQLSummarySingleEvent();
      } 
      
      if (selection.equals("Summary Chart for a single Event")) {
        JFreeChart myChart = ashSQLSummaryChartEventAWR();
        ChartPanel myChartPanel = new ChartPanel(myChart);

        statspackAWRInstancePanel.displayChart(myChartPanel);
      }     
     
      if (selection.equals("ASH report for a SQL_ID")) {
        
        ASHReportForSQLID();
      }

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  private void ashSQLDetailSingleSQLID() throws Exception, NotEnoughSnapshotsException {
    // Ask for the SQL_ID
    String sqlId = JOptionPane.showInputDialog(this,"Enter a SQL_ID.","Enter a SQL_ID",JOptionPane.QUESTION_MESSAGE);
    
    if (sqlId instanceof String) {
      sqlId = sqlId.trim();

      String cursorId = "";
      Parameters myPars = new Parameters();

      if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
        cursorId = "ashSQLDetailSingleSQL_IDAWR10.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", sqlId);
      }

      if (ConsoleWindow.getDBVersion() >= 10.2 && ConsoleWindow.getDBVersion() < 11.2) {
        cursorId = "ashSQLDetailSingleSQL_IDAWR10-2.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", sqlId);
      }

      if (ConsoleWindow.getDBVersion() >= 11.2) {
        cursorId = "ashSQLDetailSingleSQL_IDAWR11-2.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", sqlId);
      }


      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();

      QueryResult myResult =
        ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                  statspackAWRInstancePanel.getInstanceName(), sqlId);

      ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
    }
  }


  private void ashSQLSummarySingleEvent() throws Exception, NotEnoughSnapshotsException {
    // Ask for the SQL_ID
    String event = JOptionPane.showInputDialog(this,"Enter an Event. (case sensitive)","Enter an Event",JOptionPane.QUESTION_MESSAGE);
    
    if (event instanceof String) {
      event = event.trim();

      String cursorId = "";
      Parameters myPars = new Parameters();

      if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
        cursorId = "ashSQLSummarySingleEventAWR-10-1.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", event);
      }

      if (ConsoleWindow.getDBVersion() >= 10.2) {
        cursorId = "ashSQLSummarySingleEventAWR.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", event);
      }

      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();

      QueryResult myResult =
        ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                  statspackAWRInstancePanel.getInstanceName(), event);

      ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
    }
  }
  
  private void ashSQLDetailSingleEvent() throws Exception, NotEnoughSnapshotsException {
    // Ask for the SQL_ID
    String event = JOptionPane.showInputDialog(this,"Enter an Event. (case sensitive)","Enter an Event",JOptionPane.QUESTION_MESSAGE);
    
    
    String cursorId = "";
    Parameters myPars = new Parameters();

    if (event instanceof String) {
      event = event.trim();
      
     if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
        cursorId = "ashSQLDetailSingleEventAWR10.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", event);
      }

      if (ConsoleWindow.getDBVersion() >= 10.2 && ConsoleWindow.getDBVersion() < 11.2) {
        cursorId = "ashSQLDetailSingleEventAWR10-2.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", event);
      }

      if (ConsoleWindow.getDBVersion() >= 11.2) {
        cursorId = "ashSQLDetailSingleEventAWR11-2.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", event);
      }


      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();

      QueryResult myResult =
        ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                  statspackAWRInstancePanel.getInstanceName(), event);

      ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
    }
  }

  private void ashSQLSummarySingleSQLID() throws Exception, NotEnoughSnapshotsException {
    // Ask for the SQL_ID
    String sqlId = JOptionPane.showInputDialog(this,"Enter a SQL_ID.","Enter a SQL_ID",JOptionPane.QUESTION_MESSAGE);

    if (sqlId instanceof String) {
      sqlId = sqlId.trim();

      String cursorId = "";
      Parameters myPars = new Parameters();

      if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
        cursorId = "ashSQLSummarySingleSQL_IDAWR-10-1.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", sqlId);
      }

      if (ConsoleWindow.getDBVersion() >= 10.2) {
        cursorId = "ashSQLSummarySingleSQL_IDAWR.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String", sqlId);
      }

      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();

      QueryResult myResult =
        ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                  statspackAWRInstancePanel.getInstanceName(), sqlId);

      ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
    }
  }


  private JFreeChart ashSQLSummaryChartSQL_IDAWR() throws Exception, NotEnoughSnapshotsException {   
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    String[] sqlIdsThisSnapshot = new String[25];
    chartDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    String cursorId = "";
    Parameters myPars = new Parameters();
    double timeWaited;
    String event;
    
    // Ask for the SQL_ID
    String sqlId = JOptionPane.showInputDialog(this,"Enter a SQL_ID.","Enter a SQL_ID",JOptionPane.QUESTION_MESSAGE);
    
      sqlId = sqlId.trim();
      while (i < snapIdRange.length - 1) {
        startSnapId = snapIdRange[i];
        endSnapId = snapIdRange[i + 1];

        if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
          cursorId = "ashSQLSummarySingleSQL_IDAWR-10-1.sql";

          myPars = new Parameters();
          myPars.addParameter("int", startSnapId);
          myPars.addParameter("int", endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("String", sqlId);
        }

        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "ashSQLSummarySingleSQL_IDAWR.sql";

          myPars = new Parameters();
          myPars.addParameter("int", startSnapId);
          myPars.addParameter("int", endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("String", sqlId);
        }

        QueryResult myResult =
          ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                    statspackAWRInstancePanel.getInstanceName(), sqlId);

        String[][] resultSet = myResult.getResultSetAsStringArray();

        int j = 0;
        while (j == 0 || j < myResult.getNumRows()) {
          if (myResult.getNumRows() > j) {
            event = resultSet[j][0];
            timeWaited = Double.valueOf(resultSet[j][1]).doubleValue();

            addResultToDS(event, timeWaited, i + 1, j, "time waited");
            sqlIdsThisSnapshot[j] = event;
          } else {
            if (j == 0) {
              // there are no rows in this result set, so we will add a dummy row anyway.
              // This ensures that the chart has an empty bar for this snapshot
              event = "db file sequential read";
              timeWaited = 0;
              addResultToDS(event, timeWaited, i + 1, j, "time waited");
              sqlIdsThisSnapshot[j] = event;
            }
          }

          j++;
        }

        i++;
      }

      String chartTitle = "Time Waited (S)";

      JFreeChart myChart = ChartFactory.createStackedBarChart3D("", "", chartTitle, chartDS, PlotOrientation.VERTICAL, true, true, true);

      myChart.setBackgroundPaint(Color.WHITE);
      myChart.getLegend().setBorder(0, 0, 0, 0);
      myChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));

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
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 5));
      } else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 6));
      } else {
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 8));
      }
        
    return myChart;
  }

  private JFreeChart ashSQLSummaryChartEventAWR() throws Exception, NotEnoughSnapshotsException {   
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    String[] sqlIdsThisSnapshot = new String[25];
    chartDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    String cursorId = "";
    Parameters myPars = new Parameters();
    double timeWaited;
    
    // Ask for the SQL_ID
    String event = JOptionPane.showInputDialog(this,"Enter an Event.","Enter a Event",JOptionPane.QUESTION_MESSAGE);

      event = event.trim();

      while (i < snapIdRange.length - 1) {
        startSnapId = snapIdRange[i];
        endSnapId = snapIdRange[i + 1];

        if (ConsoleWindow.getDBVersion() >= 10 && ConsoleWindow.getDBVersion() < 10.2) {
          cursorId = "ashSQLSummarySingleEventAWR-10-1.sql";

          myPars = new Parameters();
          myPars.addParameter("int", startSnapId);
          myPars.addParameter("int", endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("String", event);
        }

        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "ashSQLSummarySingleEventAWR.sql";

          myPars = new Parameters();
          myPars.addParameter("int", startSnapId);
          myPars.addParameter("int", endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("String", event);
        }

        QueryResult myResult =
          ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId,
                                    statspackAWRInstancePanel.getInstanceName(), event);

        String[][] resultSet = myResult.getResultSetAsStringArray();
        String sqlId;

        int j = 0;
        while (j == 0 || j < myResult.getNumRows()) {
          if (myResult.getNumRows() > j) {
            sqlId = resultSet[j][0];
            timeWaited = Double.valueOf(resultSet[j][1]).doubleValue();

            addResultToDS(sqlId, timeWaited, i + 1, j, "time waited");
            sqlIdsThisSnapshot[j] = sqlId;
          } else {
            if (j == 0) {
              // there are no rows in this result set, so we will add a dummy row anyway.
              // This ensures that the chart has an empty bar for this snapshot
              sqlId = "dummy";
              timeWaited = 0;
              addResultToDS(sqlId, timeWaited, i + 1, j, "time waited");
              sqlIdsThisSnapshot[j] = sqlId;
            }
          }

          j++;
        }

        i++;
      }

      // all values now added to the datasets
      


      String chartTitle = "Time Waited (S)";
     
      JFreeChart myChart = ChartFactory.createStackedBarChart3D("", "", chartTitle, chartDS, PlotOrientation.VERTICAL, true, true, true);

      myChart.setBackgroundPaint(Color.WHITE);
      myChart.getLegend().setBorder(0, 0, 0, 0);
      myChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));

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
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 5));
      } else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 6));
      } else {
        myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 8));
      }
    
    
    return myChart;
  }


  private long getTimeModelStatistic(String statistic) {
    String cursorId = "stats$TimeModelStatistic.sql";
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10.0 ) {
      cursorId = "stats$TimeModelStatisticAWR.sql";            
    }
      
    int startSnapId = 0;
    int endSnapId = 0;
    
    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this);
    }
    
    Parameters myPars = new Parameters();

    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
      myPars.addParameter("int", startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String", statistic);
      myPars.addParameter("int", endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String", statistic);
    }
    else {
      myPars.addParameter("int", startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String", statistic);
      myPars.addParameter("int", endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
    }
    
    String[][] resultSet = new String[1][1];
    
    try {
      /*
       * Check whether the result has already been cached before querying the database
       */
  
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),statistic);
      
      resultSet = myResult.getResultSetAsStringArray();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    if (debug) System.out.println(statistic + " : " + resultSet[0][1]);
    
    return Long.valueOf(resultSet[0][0]).longValue();
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
  
  private void addResultToDS(String sqlId, double measure, int indexOfSnap, int rowInResultSet,String chartType) {  
    if (debug) System.out.println("addResultToDS sqlid " + sqlId + "  measure " + measure + " for snap " + indexOfSnap);
    
    /* 
    * add all previously seen distinct events to the result set
    * so that if moving the window back and forth the colours don't change
    */ 
    if (indexOfSnap == 0  && indexOfSnap == 0 && rowInResultSet == 0) {
      if (debug) System.out.println("has it been seen before");
      for (int i=0; i < numDistinctSQLIds; i++) {
        chartDS.addValue(0.0,distinctSQLIds[i],snapDateRange[0]);
//        if (debug) System.out.println("adding distinct SQLID: " + distinctSQLIds[i] + " indexOfSnap" + indexOfSnap + " i " + i + " for date range " + snapDateRange[0] + " - " + snapDateRange[1].substring(9) + " where numDistinctSQLIds " + numDistinctSQLIds);
        if (debug) System.out.println("adding distinct SQLID: " + distinctSQLIds[i] + " indexOfSnap" + indexOfSnap + " i " + i + " for date range " + snapDateRange[0] + " where numDistinctSQLIds " + numDistinctSQLIds);
      }
    }

    // is this the first time we've seen this event 
    boolean found = false;
    for (int i=0; i < this.numDistinctSQLIds; i++) {
      if (debug) System.out.println("is the first time");
      if (distinctSQLIds[i].equals(sqlId)) {
        found = true;
        break;
      }
    }
    
    if (!found) {
     // this event has not been seen before 
      if (debug) System.out.println("add it once");
     distinctSQLIds[numDistinctSQLIds++] = sqlId;
     
     // add it to all other previous series 
     double t = 0.0;
     for (int i = 1; i < indexOfSnap; i++) {
       if (debug) System.out.println("add it again");
       chartDS.addValue(t,sqlId,snapDateRange[i]);
     }
    }
    

    
    // add the new value 
  //    if (indexOfSnap > 0) {
    if (debug) System.out.println("and again");
    chartDS.addValue(measure,sqlId,snapDateRange[indexOfSnap]);
  //    }
  //    else {
  //      if (found) {
  //        chartDS.setValue(measure,sqlId,snapDateRange[0] + " - " + snapDateRange[1].substring(9));
  //        System.out.println("first snapshot set a value");
  //      }
  //      else {
  //        chartDS.addValue(measure,sqlId,snapDateRange[indexOfSnap] + " - " + snapDateRange[indexOfSnap+1].substring(9));
  //        System.out.println("first snapshot added a value");
  //      }
  //    }
  }


private void ASHReportForSQLID() {
    String message = "Enter a SQL_ID\n";
    final String SQL_ID = JOptionPane.showInputDialog(this,message,"ASH Report",JOptionPane.QUESTION_MESSAGE);

    Thread ashReportThread = new Thread ( new Runnable() {
      public void run() {

      // Set up the progress monitor
//      JProgressBar myProgressBar = new JProgressBar();
//      myProgressBar.setIndeterminate(true);
//      myProgressBar.setString("Generating ASH Report");
//      myProgressBar.setVisible(true);
        ProgressMonitor myProgressMonitor = new ProgressMonitor(statspackAWRInstancePanel,"Generating ASH Report...","",0,2);
        myProgressMonitor.setProgress(1);

      try {
        String cursorId = "RichMonASHREPORT";
        Cursor myCursor = new Cursor(cursorId,false);
        myCursor.setSQLTxtOriginal("select output as \"ASH Report\" from table(dbms_workload_repository.ash_report_html( l_dbid =>" + statspackAWRInstancePanel.getDbId() + ",l_inst_num=>" + 
                                   statspackAWRInstancePanel.getInstanceNumber() + ",l_btime=>to_date('" + 
                                   String.valueOf(statspackAWRInstancePanel.getStartDate()) + "','dd/mm/yy hh24:mi:ss'),l_etime=>to_date('" + 
                                   statspackAWRInstancePanel.getEndDate() + "','dd/mm/yy hh24:mi:ss')" + 
                                   ",l_sql_id=>'" + SQL_ID + "'));");
     
        Cursor InstNameCursor = new Cursor("InstNameCursor",false);
        InstNameCursor.setSQLTxtOriginal("select instance_name from gv$instance where inst_id = " + statspackAWRInstancePanel.getInstanceNumber());
        QueryResult InstNameResult = ExecuteDisplay.execute(InstNameCursor,false,true,null);
        String instanceName = InstNameResult.getResultSetAsStringArray()[0][0];
        QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
        String resultString =  ExecuteDisplay.createOutputString(myResult, false);
    
        //myProgressBar.setString("Saving ASH Report");
        myProgressMonitor.setProgress(2);

        File awrFile = new File(ConnectWindow.getBaseDir() + "\\ASH Reports\\ASH_" + instanceName + "_" + statspackAWRInstancePanel.getStartSnapId() + "_" + statspackAWRInstancePanel.getEndSnapId() + ".html");
        if (ConnectWindow.isLinux()) awrFile = new File(ConnectWindow.getBaseDir() + "//ASH Reports//ASH_" + instanceName + "_" + statspackAWRInstancePanel.getStartSnapId() + "_" + statspackAWRInstancePanel.getEndSnapId() + "_" + SQL_ID + ".html");
        
        BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
        awrFileWriter.write(resultString);
        awrFileWriter.close();
        
        String msg4 = "You ASH Report has been saved to the '$RICHMON_BASE/ASH Reports' directory.";
        JOptionPane.showMessageDialog(statspackAWRInstancePanel,msg4,"ADDM Report Saved...",JOptionPane.OK_OPTION);
    
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this, "DBA privilege is required for this.");
      }
    
    }
    });

    ashReportThread.setName("ashReportThread");
    ashReportThread.setDaemon(true);
    ashReportThread.start();
  }
}