/*
 * TopSQLCB.java        13.02 15/08/05
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
 * 19/10/05 Richard Wright Reduced the requirement for a range of 3 snapshots 
 *                         down to 2.
 * 08/12/05 Richard Wright Error about db restarts now include the date/time of 
 *                         previous restarts
 * 01/02/06 Richard Wright Modified so that it is possible to span restarts if 
 *                         the config file parameter is set.
 * 17/05/06 Richard Wright Set the resultSet flipping to false
 * 12/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 24/01/07 Richard Wright Added top sql by executions
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 07/12/07 Richard Wright Enhanced for RAC
 * 20/02/10 Richard Wright Added extra options for cpu time, elapsed time, version count & cluster wait time
 * 01/03/12 Richard Wright Modifed to use the awrCache
 * 01/04/15 Richard Wright Added Chart versions of all the top sql options
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

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
 * Using statspack/awr data allows queries for top sql by buffer gets/physical reads queries.
 */
public class SPTopSQLB extends JButton {
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
   public SPTopSQLB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Top SQL");
    SPTopSQLB.options = options;
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
         
      if (selection.equals("Top SQL by Physical Reads")) {
        topSQLbyPhysicalReads();
      }
           
      if (selection.equals("Top SQL by Buffer Gets")) {
        topSQLbyBufferGets();
      }  
      
      if (selection.equals("Top SQL by Executions")) {
        topSQLbyExecutions();
      } 
           
      if (selection.equals("Top SQL by Elapsed Time")) {
        topSQLbyElapsedTime();
      } 
      
      if (selection.equals("Top SQL by Cluster Wait Time")) {
        topSQLbyClusterWaitTime();
      } 
      
      if (selection.equals("Top SQL by Parse Calls")) {
        topSQLbyParseCalls();
      }  
      
      if (selection.equals("Top SQL by Version Count")) {
        topSQLbyVersionCount();
      }      
      
      if (selection.equals("Top SQL by CPU")) {
        topSQLbyCPU();
      }
        
      if (selection.equals("Top SQL IO Saved % (Smart Scan)")) {
        topSQLbySmartScan();
      }
      
      if (selection.equals("Top SQL by Average PQ Slaves")) {
        topSQLbyPQSlaves();
        
      }
      
      if (selection.equals("Top SQL by Physical Reads Chart")) {
        JFreeChart myChart = topSQLbyPhysicalReadsChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("Top SQL by Executions Chart")) {
        JFreeChart myChart = topSQLbyExecutionsChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
           
      if (selection.equals("Top SQL by Elapsed Time Chart")) {
        JFreeChart myChart = topSQLbyElapsedTimeChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
      
      if (selection.equals("Top SQL by Cluster Wait Time Chart")) {
        JFreeChart myChart = topSQLbyClusterWaitTimeChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
      
      if (selection.equals("Top SQL by Parse Calls Chart")) {
        JFreeChart myChart = topSQLbyParseCallsChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }  
      
      if (selection.equals("Top SQL by Version Count Chart")) {
        JFreeChart myChart = topSQLbyVersionCountChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }      
      
      if (selection.equals("Top SQL by CPU Chart")) {
        JFreeChart myChart = topSQLbyCPUChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
        
      if (selection.equals("Top SQL IO Saved % (Smart Scan) Chart")) {
        JFreeChart myChart = topSQLbySmartScanChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("Top SQL by Average PQ Slaves Chart")) {
        JFreeChart myChart = topSQLbyPQSlavesChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("Top SQL by Buffer Gets Chart")) {   
        JFreeChart myChart = topSQLbyBufferGetsChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
                  
        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  private void topSQLbyExecutions() throws Exception, NotEnoughSnapshotsException {
    String cursorId;
    Parameters myPars = new Parameters();
    benchmark = getStatistic("execute count");
    
      if (ConsoleWindow.getDBVersion() >= 10) {
          if (Properties.isAvoid_awr()) {
            cursorId = "topSQLbyExecutions10.sql";
            
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
          else {
            cursorId = "topSQLbyExecutionsAWR.sql";    
            
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          }

        }
        else {
          if (ConsoleWindow.getDBVersion() >= 9) {
            cursorId = "topSQLbyExecutions9.sql";
           
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());            
          }
          else {
            if (ConsoleWindow.getDBVersion() >= 8.17) {
              cursorId = "topSQLbyExecutions.sql";
        
              myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
              myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
            }
            else {
              cursorId = "topSQLbyExecutions816.sql";
              
              myPars.addParameter("int",benchmark);
              myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
              myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
            }
          }
        }
      
    // ExecuteDisplay.executeDisplay(cursorId,myPars,scrollP,statusBar,false,null);
    // QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();
    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyBufferGets() throws Exception, NotEnoughSnapshotsException {
    String cursorId;
    Parameters myPars = new Parameters();
    benchmark = getStatistic("db block gets");
    benchmark = benchmark + getStatistic("consistent gets");
    
      if (ConsoleWindow.getDBVersion() >= 10) {
          if (Properties.isAvoid_awr()) {
            cursorId = "topSQLbyBufferGets10.sql";

            myPars.addParameter("long",benchmark);
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
          else {
            cursorId = "topSQLbyBufferGetsAWR.sql";    
          
            myPars.addParameter("long",benchmark);
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            if (debug) System.out.println(statspackAWRInstancePanel.getDbId());
          }     
        }
        else {
          if (ConsoleWindow.getDBVersion() >= 9) {
            cursorId = "topSQLbyBufferGets9.sql";
      
            myPars.addParameter("long",benchmark);
            myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
            myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
          else {
            if (ConsoleWindow.getDBVersion() >= 8.17) {
              cursorId = "topSQLbyBufferGets.sql";
        
              myPars.addParameter("long",benchmark);
              myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
              myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            }
            else {
              cursorId = "topSQLbyBufferGets816.sql";
              
              myPars.addParameter("long",benchmark);
              myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
              myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
              myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            }
          }
        }
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();
  
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }

  private void topSQLbyPhysicalReads() throws Exception, NotEnoughSnapshotsException {
    String cursorId;
    Parameters myPars = new Parameters();
    benchmark = getStatistic("physical reads");

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyPhysicalReads10.sql";
        

        myPars.addParameter("long",benchmark);
        myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("long",benchmark);
      }
      else {
        cursorId = "topSQLbyPhysicalReadsAWR.sql";    

        myPars.addParameter("long",benchmark);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long",benchmark);
      } 
    }
    else {
      if (ConsoleWindow.getDBVersion() >= 9) {
        cursorId = "topSQLbyPhysicalReads9.sql";
        
        myPars.addParameter("long",benchmark);
        myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("long",benchmark);
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 8.17) {
          cursorId = "topSQLbyPhysicalReads.sql";
          
          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
          myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("long",benchmark);

        }
        else {
          cursorId = "topSQLbyPhysicalReads816.sql";
          
          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
          myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          
        }
      }
    }

    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbySmartScan() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "topSQLbySmartScanSavedPercentAWR.sql";
    Parameters myPars = new Parameters();
    benchmark = getStatistic("physical reads");

    if (ConsoleWindow.getDBVersion() >= 11.2) {

      cursorId = "topSQLbySmartScanSavedPercentAWR.sql";    

      myPars.addParameter("long",benchmark);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    }
    
    
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyPQSlaves() throws Exception, NotEnoughSnapshotsException {
    String cursorId;
    
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      cursorId = "topSQLbyPQSlavesAWR112.sql";
    }
    else {
      cursorId = "topSQLbyPQSlavesAWR.sql";
    }
  
    Parameters myPars = new Parameters();

    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    
    
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyElapsedTime() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "";
    Parameters myPars = new Parameters();
    long dbtime = getTimeModelStatistic("DB time");


    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyElapsedTime10.sql";
      
      myPars.addParameter("long",dbtime);
      myPars.addParameter("long",dbtime);
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyElapsedTimeAWR.sql";    

      myPars.addParameter("long",dbtime);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    } 
      
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyParseCalls() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "";
    Parameters myPars = new Parameters();
    long prse = getStatistic("parse count (total)");


    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyParseCalls10.sql";

        myPars.addParameter("long", prse);
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "topSQLbyParseCallsAWR.sql";

        myPars.addParameter("long", prse);
        myPars.addParameter("long", prse);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }
    } 
    else {
      cursorId = "topSQLbyParseCalls9.sql";

      myPars.addParameter("long", prse);
      myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber()); 
    }
  
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
    
  private void topSQLbyVersionCount() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "";
    Parameters myPars = new Parameters();

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyVersionCount10.sql";

        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "topSQLbyVersionCountAWR.sql";


        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }
    }
    else {
      cursorId = "topSQLbyVersionCount9.sql";

      myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }

    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyCPU() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "";
    Parameters myPars = new Parameters();
      
    long dbcpu = getTimeModelStatistic("DB CPU");

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyCPU10.sql";

        myPars.addParameter("long", dbcpu);
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "topSQLbyCPUAWR.sql";

        myPars.addParameter("long", dbcpu);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }
    }
    else {
      cursorId = "topSQLbyCPU9.sql";

      myPars.addParameter("long", dbcpu);
      myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private void topSQLbyClusterWaitTime() throws Exception, NotEnoughSnapshotsException {
    String cursorId = "";
    Parameters myPars = new Parameters();

   
    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyClusterWaitTime10.sql";
      
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyClusterWaitTimeAWR.sql";    

      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    } 
    
    /*
     * Check whether the result has already been cached before querying the database
     */
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  }
  
  private JFreeChart topSQLbyExecutionsChart() throws Exception, NotEnoughSnapshotsException {   
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    String[] sqlIdsThisSnapshot = new String[10];
    chartDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    String cursorId;
    Parameters myPars;
    String sqlId;
    int executions;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      
      cursorId = "topSQLbyExecutionsAWR.sql";    
      
      myPars = new Parameters();
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      
      if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
        sqlId = resultSet[j][6];
        executions = Integer.valueOf(resultSet[j][0]).intValue();
        
  //        if (timeCorrect) time = time / snapshotRunTime;
        
        if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " executions " + executions + " i " + i + " j " + j);
         
        addResultToDS(sqlId,executions,i,j,"Executions");
        sqlIdsThisSnapshot[j] = sqlId;
      }
      
      // add sqlId's that did not occur in this snapshot but did in previous
      for (int j = 0; j < numDistinctSQLIds; j++) {
        // for each event that occured in the past did it occur this time
        boolean found = false;
        for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
          if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
            found = true;
            break;
          }
        }

        if (!found) {
          int t = 0;
          String notFoundSQLId = distinctSQLIds[j];
          if (debug) System.out.println("not found " + notFoundSQLId);
          addResultToDS(notFoundSQLId, t, i, 9999, "Executions");
        }
      }
      
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    
    String chartTitle;
  //    if (timeCorrect) {
  //      chartTitle = "Time Waited per second of snapshot";
  //    }
  //else
  //    {
      chartTitle = "Top SQL by Executions";
  //    }
    
    myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            chartDS,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            true
                          ); 
  //    if (addTitle) {
  //      if (timeCorrect) {
  //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
  //      }
  //      else {
    //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
  //      }
  //    }

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
  
  private JFreeChart topSQLbyBufferGetsChart() throws Exception, NotEnoughSnapshotsException {  
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    String[] sqlIdsThisSnapshot = new String[10];
    chartDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    String cursorId;
    Parameters myPars;
    String sqlId;
    int bufferGets;
    
    while (i < snapIdRange.length -1) {
    
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];   
      
      benchmark = getStatistic("db block gets",startSnapId,endSnapId);
      benchmark = benchmark + getStatistic("consistent gets",startSnapId,endSnapId);
  
      cursorId = "topSQLbyBufferGetsAWR.sql";    
      
      myPars = new Parameters();
      myPars.addParameter("long",benchmark);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      
      if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
        sqlId = resultSet[j][6];
        bufferGets = Integer.valueOf(resultSet[j][0]).intValue();
        
    //        if (timeCorrect) time = time / snapshotRunTime;
        
        if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " buffer gets " + bufferGets + " i " + i + " j " + j);
         
        addResultToDS(sqlId,bufferGets,i,j,"Buffer Gets");
        sqlIdsThisSnapshot[j] = sqlId;
      }
      
      // add sqlId's that did not occur in this snapshot but did in previous
      for (int j = 0; j < numDistinctSQLIds; j++) {
        // for each event that occured in the past did it occur this time
        boolean found = false;
        for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
          if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
            found = true;
            break;
          }
        }
  
        if (!found) {
          int t = 0;
          String notFoundSQLId = distinctSQLIds[j];
          if (debug) System.out.println("not found " + notFoundSQLId);
          addResultToDS(notFoundSQLId, t, i, 9999, "Buffer Gets");
        }
      }
      
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;
  
    
    String chartTitle;
    //    if (timeCorrect) {
    //      chartTitle = "Time Waited per second of snapshot";
    //    }
    //else
    //    {
      chartTitle = "Top SQL by Buffer Gets";
    //    }
    
    myChart = ChartFactory.createStackedBarChart3D(
                            "",
                            "",
                            chartTitle,
                            chartDS,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            true
                          ); 
  //    if (addTitle) {
  //      if (timeCorrect) {
  //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
  //      }
  //      else {
  //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
  //      }
  //    }

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

  private JFreeChart topSQLbyPhysicalReadsChart() throws Exception, NotEnoughSnapshotsException {
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int physicalReads;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     long benchmark = getStatistic("physical reads",startSnapId,endSnapId);
     
     cursorId = "topSQLbyPhysicalReadsAWR.sql";    

     myPars = new Parameters();
     myPars.addParameter("long",benchmark);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int",startSnapId);
     myPars.addParameter("int",endSnapId);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("long",benchmark);
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][6];
       physicalReads = Integer.valueOf(resultSet[j][0]).intValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " executions " + physicalReads + " i " + i + " j " + j);
        
       addResultToDS(sqlId,physicalReads,i,j,"Physical Reads");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "Physical Reads");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Physical Reads";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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
  
  private JFreeChart topSQLbySmartScanChart() throws Exception, NotEnoughSnapshotsException {
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int smartScanSaved;
   
   while (i < snapIdRange.length -1) {
   
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     cursorId = "topSQLbySmartScanSavedPercentAWR.sql";    
     
     myPars = new Parameters();
     myPars.addParameter("long",benchmark);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
     myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][7];
       smartScanSaved = Integer.valueOf(resultSet[j][2]).intValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " Smart Scan Saved " + smartScanSaved + " i " + i + " j " + j);
        
       addResultToDS(sqlId,smartScanSaved,i,j,"Smart Scan Saved");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }
  
       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "Smart Scan Saved");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;
  
   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Smart Scan Saved";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }
  
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
  
  private JFreeChart topSQLbyPQSlavesChart() throws Exception, NotEnoughSnapshotsException {
  /*    String cursorId;
    
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      cursorId = "topSQLbyPQSlavesAWR112.sql";
    }
    else {
      cursorId = "topSQLbyPQSlavesAWR.sql";
    }
  
    Parameters myPars = new Parameters();

    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    
    
    /*
     * Check whether the result has already been cached before querying the database
     */
  /*    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  */
    
       statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int pqSlaves;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     if (ConsoleWindow.getDBVersion() >= 11.2) {
       cursorId = "topSQLbyPQSlavesAWR112.sql";
     }
     else {
       cursorId = "topSQLbyPQSlavesAWR.sql";
     }
     
     myPars = new Parameters();
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
     myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][6];
       pqSlaves = Integer.valueOf(resultSet[j][2]).intValue();
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " PQ Slaves " + pqSlaves + " i " + i + " j " + j);
        
       addResultToDS(sqlId,pqSlaves,i,j,"PQ Slaves");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "PQ Slaves");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Physical Reads";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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
  
  private JFreeChart topSQLbyElapsedTimeChart() throws Exception, NotEnoughSnapshotsException {
  /*    String cursorId = "";
    Parameters myPars = new Parameters();
    long dbtime = getTimeModelStatistic("DB time");


    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyElapsedTime10.sql";
      
      myPars.addParameter("long",dbtime);
      myPars.addParameter("long",dbtime);
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyElapsedTimeAWR.sql";    

      myPars.addParameter("long",dbtime);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    } 
      
    /*
     * Check whether the result has already been cached before querying the database
     */
  /*    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  */
    
     statspackAWRInstancePanel.clearScrollP();
     snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
     snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
     statspackAWRInstancePanel.sanityCheckRange();
     String[] sqlIdsThisSnapshot = new String[10];
     chartDS = new DefaultCategoryDataset();
     
     int startSnapId;
     int endSnapId;
     int i = 0;
     
     String cursorId;
     Parameters myPars;
     String sqlId;
     int elapsedTime;
     
     while (i < snapIdRange.length -1) {
    
       startSnapId = snapIdRange[i];
       endSnapId = snapIdRange[i +1];   
       
       long dbtime = getTimeModelStatistic("DB time",startSnapId,endSnapId);
       
       cursorId = "topSQLbyElapsedTimeAWR.sql";    
       
       myPars = new Parameters();
       myPars.addParameter("long",dbtime);
       myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
       myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
       myPars.addParameter("int",startSnapId);
       myPars.addParameter("int",endSnapId);
       myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
       
       if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
       
       QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
       
       String[][] resultSet = myResult.getResultSetAsStringArray();
       
       for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
         sqlId = resultSet[j][6];
         elapsedTime = Integer.valueOf(resultSet[j][0]).intValue();
         
     //        if (timeCorrect) time = time / snapshotRunTime;
         
         if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " elapsed time " + elapsedTime + " i " + i + " j " + j);
          
         addResultToDS(sqlId,elapsedTime,i,j,"Elapsed Time");
         sqlIdsThisSnapshot[j] = sqlId;
       }
       
       // add sqlId's that did not occur in this snapshot but did in previous
       for (int j = 0; j < numDistinctSQLIds; j++) {
         // for each event that occured in the past did it occur this time
         boolean found = false;
         for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
           if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
             found = true;
             break;
           }
         }
  
         if (!found) {
           int t = 0;
           String notFoundSQLId = distinctSQLIds[j];
           if (debug) System.out.println("not found " + notFoundSQLId);
           addResultToDS(notFoundSQLId, t, i, 9999, "Elapsed Time");
         }
       }
       
       i++;
     }
     
     // all values now added to the datasets 
     JFreeChart myChart;
  
     
     String chartTitle;
     //    if (timeCorrect) {
     //      chartTitle = "Time Waited per second of snapshot";
     //    }
     //else
     //    {
       chartTitle = "Top SQL by Elapsed Time";
     //    }
     
     myChart = ChartFactory.createStackedBarChart3D(
                             "",
                             "",
                             chartTitle,
                             chartDS,
                             PlotOrientation.VERTICAL,
                             true,
                             true,
                             true
                           ); 
     //    if (addTitle) {
     //      if (timeCorrect) {
     //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
     //      }
     //      else {
     //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
     //      }
     //    }
  
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
  
  private JFreeChart topSQLbyParseCallsChart() throws Exception, NotEnoughSnapshotsException {   
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int parseCalls;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     long prse = getStatistic("parse count (total)",startSnapId,endSnapId);
     
     cursorId = "topSQLbyParseCallsAWR.sql";

     myPars = new Parameters();
     myPars.addParameter("long", prse);
     myPars.addParameter("long", prse);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int", startSnapId);
     myPars.addParameter("int", endSnapId);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][3];
       parseCalls = Integer.valueOf(resultSet[j][0]).intValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " parse calls " + parseCalls + " i " + i + " j " + j);
        
       addResultToDS(sqlId,parseCalls,i,j,"Parse Calls");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "Parse Calls");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Parse Calls";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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
    
  private JFreeChart topSQLbyVersionCountChart() throws Exception, NotEnoughSnapshotsException {   
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int versionCount;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     cursorId = "topSQLbyVersionCountAWR.sql";

     myPars = new Parameters();
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int", startSnapId);
     myPars.addParameter("int", endSnapId);
     myPars.addParameter("int", endSnapId);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][2];
       versionCount = Integer.valueOf(resultSet[j][0]).intValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " version count " + versionCount + " i " + i + " j " + j);
        
       addResultToDS(sqlId,versionCount,i,j,"Version Count");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "Version Count");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Version Count";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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
  
  private JFreeChart topSQLbyCPUChart() throws Exception, NotEnoughSnapshotsException {
  /*    String cursorId = "";
    Parameters myPars = new Parameters();
      
    long dbcpu = getTimeModelStatistic("DB CPU");

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyCPU10.sql";

        myPars.addParameter("long", dbcpu);
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "topSQLbyCPUAWR.sql";

        myPars.addParameter("long", dbcpu);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }
    }
    else {
      cursorId = "topSQLbyCPU9.sql";

      myPars.addParameter("long", dbcpu);
      myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    
    /*
     * Check whether the result has already been cached before querying the database
     */
  /*    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    ExecuteDisplay.displayTable(myResult, statspackAWRInstancePanel, false, statusBar);
  */
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   double cpuTime;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     long dbcpu = getTimeModelStatistic("DB CPU",startSnapId,endSnapId);  

     cursorId = "topSQLbyCPUAWR.sql";

     myPars = new Parameters();
     myPars.addParameter("long", dbcpu);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int", startSnapId);
     myPars.addParameter("int", endSnapId);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][6];
       cpuTime = Double.valueOf(resultSet[j][0]).doubleValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " cpu time " + cpuTime + " i " + i + " j " + j);
        
       addResultToDS(sqlId,cpuTime,i,j,"CPU Time");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "CPU Time");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by CPU Time";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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
  
  private JFreeChart topSQLbyClusterWaitTimeChart() throws Exception, NotEnoughSnapshotsException {
   statspackAWRInstancePanel.clearScrollP();
   snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
   snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
   statspackAWRInstancePanel.sanityCheckRange();
   String[] sqlIdsThisSnapshot = new String[10];
   chartDS = new DefaultCategoryDataset();
   
   int startSnapId;
   int endSnapId;
   int i = 0;
   
   String cursorId;
   Parameters myPars;
   String sqlId;
   int clusterWaitTime;
   
   while (i < snapIdRange.length -1) {
  
     startSnapId = snapIdRange[i];
     endSnapId = snapIdRange[i +1];   
     
     cursorId = "topSQLbyClusterWaitTimeAWR.sql";    

     myPars = new Parameters();
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
     myPars.addParameter("int",startSnapId);
     myPars.addParameter("int",endSnapId);
     myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
     
     if (debug) System.out.println("executing " + cursorId + " for " + startSnapId + " and " + endSnapId);
     
     QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
     
     String[][] resultSet = myResult.getResultSetAsStringArray();
     
     for (int j=0; j < Math.min(myResult.getNumRows(),10); j++) {
       sqlId = resultSet[j][6];
       clusterWaitTime = Integer.valueOf(resultSet[j][0]).intValue();
       
   //        if (timeCorrect) time = time / snapshotRunTime;
       
       if (debug) System.out.println("calling addResultToDS with sqlId " + sqlId + " cluster wait time " + clusterWaitTime + " i " + i + " j " + j);
        
       addResultToDS(sqlId,clusterWaitTime,i,j,"Cluster Wait Time");
       sqlIdsThisSnapshot[j] = sqlId;
     }
     
     // add sqlId's that did not occur in this snapshot but did in previous
     for (int j = 0; j < numDistinctSQLIds; j++) {
       // for each event that occured in the past did it occur this time
       boolean found = false;
       for (int k = 0; k < sqlIdsThisSnapshot.length; k++) {
         if (distinctSQLIds[j].equals(sqlIdsThisSnapshot[k])) {
           found = true;
           break;
         }
       }

       if (!found) {
         int t = 0;
         String notFoundSQLId = distinctSQLIds[j];
         if (debug) System.out.println("not found " + notFoundSQLId);
         addResultToDS(notFoundSQLId, t, i, 9999, "Cluster Wait Time");
       }
     }
     
     i++;
   }
   
   // all values now added to the datasets 
   JFreeChart myChart;

   
   String chartTitle;
   //    if (timeCorrect) {
   //      chartTitle = "Time Waited per second of snapshot";
   //    }
   //else
   //    {
     chartTitle = "Top SQL by Cluster Wait Time";
   //    }
   
   myChart = ChartFactory.createStackedBarChart3D(
                           "",
                           "",
                           chartTitle,
                           chartDS,
                           PlotOrientation.VERTICAL,
                           true,
                           true,
                           true
                         ); 
   //    if (addTitle) {
   //      if (timeCorrect) {
   //        myChart.addSubtitle(new TextTitle("Since not all snapshots cover the same amount of time, time waited has been divided by snapshot length to aid comparison"));
   //      }
   //      else {
   //        myChart.addSubtitle(new TextTitle("Beware of snapshots that have a different elapsed time"));
   //      }
   //    }

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

  
  private long getStatistic(String statistic) {
    String cursorId = "stats$Statistics.sql";
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10.0 ) {
      cursorId = "stats$StatisticsAWR.sql";            
    }
    else {
      cursorId = "stats$Statistics.sql"; 
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

    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("String",statistic);
    
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
    return Long.valueOf(resultSet[0][1]).longValue();
  }
  
  private long getStatistic(String statistic, int startSnapId, int endSnapId) {
    String cursorId = "stats$Statistics.sql";
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10.0 ) {
      cursorId = "stats$StatisticsAWR.sql";            
    }
    else {
      cursorId = "stats$Statistics.sql"; 
    }
      

    
    Parameters myPars = new Parameters();

    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("String",statistic);
    
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
    return Long.valueOf(resultSet[0][1]).longValue();
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
  
  private long getTimeModelStatistic(String statistic, int startSnapId, int endSnapId) {
    String cursorId = "stats$TimeModelStatistic.sql";
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10.0 ) {
      cursorId = "stats$TimeModelStatisticAWR.sql";            
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
    if (debug) System.out.println(statistic + " : " + resultSet[0][0]);
    
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
      for (int i=0; i < numDistinctSQLIds; i++) {
        chartDS.addValue(0.0,distinctSQLIds[i],snapDateRange[0] + " - " + snapDateRange[1].substring(9));
  //        if (debug) System.out.println("adding distinct SQLID: " + distinctSQLIds[i] + " indexOfSnap" + indexOfSnap + " i " + i + " for date range " + snapDateRange[0] + " - " + snapDateRange[1].substring(9) + " where numDistinctSQLIds " + numDistinctSQLIds);
      }
    }

    // is this the first time we've seen this event 
    boolean found = false;
    for (int i=0; i < this.numDistinctSQLIds; i++) {
      if (distinctSQLIds[i].equals(sqlId)) {
        found = true;
        break;
      }
    }
    
    if (!found) {
     // this event has not been seen before 
     distinctSQLIds[numDistinctSQLIds++] = sqlId;
     
     // add it to all other previous series 
     double t = 0.0;
     for (int i = 1; i < indexOfSnap; i++) {
       chartDS.addValue(t,sqlId,snapDateRange[i] + " - " + snapDateRange[i+1].substring(9));
     }
    }
    

    
    // add the new value 
  //    if (indexOfSnap > 0) {
      chartDS.addValue(measure,sqlId,snapDateRange[indexOfSnap] + " - " + snapDateRange[indexOfSnap+1].substring(9));
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
}