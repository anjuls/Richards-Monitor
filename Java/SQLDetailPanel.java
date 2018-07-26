/*
 * SQLDetailPane.java        17.39 29/12/09
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
 * (Prior to 29/12/09 this funtionality was performed from within the ExecuteDisplay class)
 * 
 * Change History
 * ==============
 * 02/12/10 Richard Wright When running from the performance panel, removed dependancy on the awr panel snapshot range
 * 21/02/11 Richard Wright Save SQL_ID output to the output directory after displaying on screen
 * 08/08/11 Richard Wright The ash panel is only used when the db is 11g and above rather than 10g above
 * 10/10/11 Richard Wright Added SQLReportMonitor when 11gr1 and above
 * 29/05/15 Richard Wright Added the execution history chart and a progress monitor
 * 16/06/15 Richard Wright Fixed bug where not all axis were displayed properly and wrong results were possible
 * 07/12/14 Richard Wright Added 'sql_execution_history_chart' - Brian says it slows RichMon down too much
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Rectangle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.sql.SQLOutput;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import javax.swing.ProgressMonitor;

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
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;


public class SQLDetailPanel extends JPanel {
  
  JTabbedPane myTabbedPane = new JTabbedPane();
  JPanel mySQLPanel = new JPanel(new BorderLayout());
  JScrollPane mySQLSP = new JScrollPane();
  JTextPane sqlTP = new JTextPane();
  
  JPanel myPlanPanel = new JPanel(new BorderLayout());
  JScrollPane myPlanSP = new JScrollPane();
  
  JPanel myHistoryPanel = new JPanel(new BorderLayout());
  JScrollPane myHistorySP = new JScrollPane();
  
  JPanel myBindCapturePanel = new JPanel(new BorderLayout());
  JScrollPane myBindCaptureSP = new JScrollPane();
  
  JPanel myReportSQLMonitorPanel = new JPanel (new BorderLayout());
  JScrollPane myReportSQLMonitorSP = new JScrollPane();
  
  JPanel myExecutionHistoryChartPanel = new JPanel(new BorderLayout());
  JScrollPane executionHistoryChartSP = new JScrollPane();

  JPanel ASHPanel = new JPanel(new BorderLayout());
  JPanel ASHTopPanel = new JPanel();
  JPanel ASHBottomPanel = new JPanel(new BorderLayout());
  JScrollPane ashScrollP = new JScrollPane();
  
  JTextField ashStartTime = new JTextField();
  JTextField ashEndTime = new JTextField();
  JLabel ashStartL = new JLabel("Enter times in format dd-mon-yyyy hh24:mi - Start Time");
  JLabel ashEndL = new JLabel("End Time");      
  JButton ashButton = new JButton("Get Active Session History");

  //String sqlId;
  
  Vector sqlMonitorReportsV = new Vector();
  
  ProgressMonitor myProgressM;
  int progress;

  String sqlId;
  long dbId;
  int instanceNumber;
  long sqlExecId;
  int startSnapId;
  int endSnapId;
  String[] snapDateRange;
  
  boolean debug = false;

      
  public SQLDetailPanel(String sqlId, long dbId, int instanceNumber,long sqlExecId,int startSnapId,int endSnapId, String[] snapDateRange) {
    this.sqlId = sqlId;
    this.dbId = dbId;
    this.instanceNumber = instanceNumber;
    this.sqlExecId = sqlExecId;
    this.startSnapId = startSnapId;
    this.endSnapId = endSnapId;
    this.snapDateRange = snapDateRange;
    
    jb_init();
    
    
    
    Thread collectionThread = new Thread ( new Runnable() {
      public void run() {
        captureAndDisplaySQLDetail();
        myProgressM.setProgress(myProgressM.getMaximum());
        myTabbedPane.setVisible(true);
      }
    });

    collectionThread.setName("SQLDetailCollectionThread");
    collectionThread.setDaemon(true);
    collectionThread.start();
    
    
    
  }

  private void captureAndDisplaySQLDetail() {
    
    myProgressM = new ProgressMonitor(myTabbedPane,"Collecting SQL Execution History Detail","",0,10);
    myProgressM.setMillisToPopup(0);
    progress = 1;
    
    myProgressM.setNote("Capture the SQL Text from AWR");
    myProgressM.setProgress(progress++);
    
    /*
     * Get the Query
     */
    String cursorId = "stats$SqlTextAWR.sql";
    
    Parameters myPars = new Parameters();     
    myPars.addParameter("String",sqlId);
    myPars.addParameter("long", dbId);
      
    QueryResult mySQLText = new QueryResult();
    QueryResult myPlan = new QueryResult();
    QueryResult myPlanHistory = new QueryResult();
    QueryResult myBindCapture = new QueryResult();
    QueryResult myReportSQLMonitor = new QueryResult();
    QueryResult myExecutionHistoryChartData = new QueryResult();
    
    try {
        mySQLText = ExecuteDisplay.execute(cursorId,myPars,false,true,null);

      if (Properties.isReFormatSQLInSQLPanels()) mySQLText = ExecuteDisplay.reFormatSQL(mySQLText, sqlId);
      String[] columnTypes = {"VARCHAR"};
      mySQLText.setColumnTypes(columnTypes);
      mySQLText.setCursorId(cursorId);


      myProgressM.setNote("Getting the execution plan/s");
      myProgressM.setProgress(progress++);

      /*
       * Get the execution plan
       */
      
      cursorId = "RichMonAWRPlanQuery";
      Cursor myCursor = new Cursor(cursorId,false);
      if (ConsoleWindow.getDBVersion() >= 11.0 ) {
        myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_awr(sql_id => '" + sqlId + "', format => 'ALL'))\n");
        
        // ps.  Using 'ALLSTATS LAST' results in less information, not more :-(
      }
      else {
        myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_awr('" + sqlId + "',null,null,'ALL'))\n");
     }
      myPlan = ExecuteDisplay.execute(myCursor,false ,false,null);


      myProgressM.setNote("Extracting the Execution History from AWR for the TEXT display");
      myProgressM.setProgress(progress++);
      
      /*
       * Get the plan changes over time
       */

      
      myPars = new Parameters();
      cursorId = "sqlPlanHistory10.sql";

      myPars.addParameter("String",sqlId);
      myPars.addParameter("long", dbId);
      
      myPlanHistory = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      
      myProgressM.setNote("Extracting the Execution History from AWR for the Execution Chart");
      myProgressM.setProgress(progress++);
      
      /*
       * Get the data for the execution history chart
       * 
       * If the original call did not come from the AWR panel, it will have zero for the start and end snapId's.
       * In this case we set the endSnapId very very high to ensure we return all snapshots.  Maybe later, add a parameter 
       * to control the size of the snap range in this circumstance.
       */
      

      
      if (endSnapId == 0) endSnapId = 9999999;
      
      myPars = new Parameters();
      cursorId = "sqlPlanHistory10v3.sql";

      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", dbId);
      myPars.addParameter("String",sqlId);
      
      myExecutionHistoryChartData = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      
      myProgressM.setNote("Getting the captured bind variables from AWR");
      myProgressM.setProgress(progress++);
      
      /* 
       * Get the sql statements bind values
       */
      
      
      myPars = new Parameters();
      cursorId = "sqlBindVariablesAWR.sql";

      myPars.addParameter("long", dbId);
      myPars.addParameter("int", instanceNumber);
      myPars.addParameter("String",sqlId);
      
      myBindCapture = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
     

      myProgressM.setNote("Getting the SQL Monitor Report/s");
      myProgressM.setProgress(progress++);      
      
      /*
       * Get the SQL Monitor Report
       */
      
      
      boolean noOutput = false;
      try {
        if (ConsoleWindow.getDBVersion() == 11.1 || sqlExecId > 0) {
          cursorId = "RichMonSQLMonitorReportQuery";
          myCursor = new Cursor(cursorId, false);
          if (sqlExecId > 0) {
            myCursor.setSQLTxtOriginal("select dbms_sqltune.report_sql_monitor( sql_id=> '" + sqlId +
                                       "', type => 'TEXT', report_level => 'All', sql_exec_id => " + sqlExecId +
                                       ") AS report from dual\n");
            
            myReportSQLMonitor = ExecuteDisplay.execute(myCursor, false, false, null);
            addSQLMonitorReport(null,sqlExecId,myReportSQLMonitor);
          }
          else {
            myCursor.setSQLTxtOriginal("select dbms_sqltune.report_sql_monitor( sql_id=> '" + sqlId +
                                       "', type => 'TEXT', report_level => 'All') AS report from dual\n");

            myReportSQLMonitor = ExecuteDisplay.execute(myCursor, false, false, null);
            addSQLMonitorReport(null,0,myReportSQLMonitor);
          }
        }
          
        // if the database is 11.1 then just get the report for the last execution (you cannot specify the plan hash value in 11.1)
        if (ConsoleWindow.getDBVersion() >= 11.2) {
          ;
         // Get a list of all plan hash values for this SQL
          cursorId = "planHashValues.sql";
          myCursor = new Cursor(cursorId, false);
          myPars = new Parameters();
          myPars.addParameter("String", sqlId);

          QueryResult planHashValues = ExecuteDisplay.execute(cursorId, myPars, false, true, null);
          String[][] resultSet = planHashValues.getResultSetAsStringArray();
          int n = -1;

          if (planHashValues.getNumRows() > 1) {
            // ask the user to specify whether to report on all plan hash values or a specfic one
            Object[] options = { "All Plans", "Last Executed Plan", "None" };

            String msg =
              "Obtaining SQL Monitor reports can take a few minutes on some systems.\n\nThis SQL has " +
              planHashValues.getNumRows() +
              " plan/s.\n\nA blank SQL Monitor Report tab shows that the data is not available.\n\n  Choose carefully. Which plan do you wish to report on?";

            n = JOptionPane.showOptionDialog(this, msg, "Pick a Plan", JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
          }
          else {
            if (planHashValues.getNumRows() == 1) n = 1;  // if only 1 phv then get the last sql monitor report regardless
          } 
          
          
          if (n == 1) {
            myCursor.setSQLTxtOriginal("select dbms_sqltune.report_sql_monitor( sql_id=> '" + sqlId +
                                       "', type => 'TEXT', report_level => 'All') AS report from dual\n");

            myReportSQLMonitor = ExecuteDisplay.execute(myCursor, false, false, null);
            addSQLMonitorReport(null,0,myReportSQLMonitor);
          }

          if (n == 0) {
            for (int i = 0; i < planHashValues.getNumRows(); i++) {
              myCursor.setSQLTxtOriginal("select dbms_sqltune.report_sql_monitor( sql_id=> '" + sqlId +
                                         "', type => 'TEXT', report_level => 'All', sql_plan_hash_value => '" +
                                         resultSet[i][0] + "') AS report from dual\n");

              myReportSQLMonitor = ExecuteDisplay.execute(myCursor, false, false, null);
              addSQLMonitorReport(resultSet[i][0],0,myReportSQLMonitor);
            }
          }
        }
        
        myProgressM.setNote("Creating the Execution History Chart");
        myProgressM.setProgress(progress++);
        
        /*
         * Create the Execution History Chart
         */
        

        JFreeChart myChart =
          createExecutionHistoryChart(myExecutionHistoryChartData, startSnapId, endSnapId, snapDateRange);
        ChartPanel myChartPanel = new ChartPanel(myChart);
        executionHistoryChartSP.getViewport().add(myChartPanel);

        
      } catch (Exception e) {
        // Do Nothing .... use might not have enough privilegs to get this information
        ConsoleWindow.displayError(e, this);
      }
      
      myProgressM.setNote("Displaying the results of all the previous steps");
      myProgressM.setProgress(progress++);

      /*
       * Display the results
       */
      ExecuteDisplay.displayTextPane(mySQLText,sqlTP,null,Color.BLUE);
      JTable historyTable = ExecuteDisplay.createTable(myPlanHistory);   
      myHistorySP.getViewport().add(historyTable);
      JTable bindCaptureTable = ExecuteDisplay.createTable(myBindCapture);      
      myBindCaptureSP.getViewport().add(bindCaptureTable);
      JTable planTable = ExecuteDisplay.createTable(myPlan);      
      myPlanSP.getViewport().add(planTable);

      myProgressM.setNote("Saving the output to disk");
      myProgressM.setProgress(progress++);
      
      /*
       * Save the results to the output directory
       */
    
      
      if (Properties.isAutosave_sql_detail()) {
        File SQLOutput;
        if (ConnectWindow.isLinux()) {
          SQLOutput = new File(ConnectWindow.getBaseDir() + "/Output/RichMon SQLID " + ConsoleWindow.getDatabaseName() + " " + sqlId + " " + ConnectWindow.getDatabase().getSYSDate() + ".html");
        } else {
          SQLOutput = new File(ConnectWindow.getBaseDir() + "\\Output\\RichMon SQLID " + ConsoleWindow.getDatabaseName() + " " + sqlId + " " + ConnectWindow.getDatabase().getSYSDate() + ".html");
        }
        BufferedWriter SQLOutputWriter = new BufferedWriter(new FileWriter(SQLOutput));

        if (SQLOutput.exists()) {
          SQLOutput.delete();
          SQLOutputWriter = new BufferedWriter(new FileWriter(SQLOutput));
        }
        

        if (ConsoleWindow.getDBVersion() > 11 && noOutput==false) { 
          QueryResult[] results = { myPlanHistory,mySQLText,myPlan,myBindCapture,myReportSQLMonitor};
          OutputHTML outputHTML = new OutputHTML(SQLOutput, SQLOutputWriter, results);
        }
        else {
          QueryResult[] results = { myPlanHistory,mySQLText,myPlan,myBindCapture};
          OutputHTML outputHTML = new OutputHTML(SQLOutput, SQLOutputWriter, results);
        }
        

        SQLOutputWriter.close();
      }
    }
    catch (Exception ee) {
      JOptionPane.showMessageDialog(ConnectWindow.getConsoleWindow(),"Error fetching sql detail from AWR: " + ee.toString());
    }
  }
  
  /*
   * Called with the formatted sql after a 'hash value' or 'top sql hash value' has been clicked on
   */
  public SQLDetailPanel(QueryResult resultSet, String sqlHashValue) {
    
    jb_init();
    
    try {
      ExecuteDisplay.displayTextPane(resultSet,sqlTP,null,Color.BLUE);
      myTabbedPane.setVisible(true);
    }
    catch (Exception e) {
      
    }

    
  }
  private void jb_init() {
    
    //this.getContentPane().setLayout(new BorderLayout());
    
    mySQLSP.getViewport().add(sqlTP);
    mySQLPanel.add(mySQLSP,BorderLayout.CENTER);
    //      if (numRowsInSQLStatement > 0) myTabbedPane.addTab("SQL",mySQLPanel);
    myTabbedPane.addTab("SQL",mySQLPanel);
    
    // check to see if this SQL_ID already has a tab and if so delete it 
//    int tabAt = -1;
//    for (int i = 0; i < sqlIdTP.getTabCount(); i++) {
//      if (sqlIdTP.getTitleAt(i).equalsIgnoreCase(sqlId)) {
//        tabAt = i;
//        break;
//      }
//    }
    
//    if (tabAt >=0) sqlIdTP.remove(tabAt);
    
    // add a new tab for this SQLId
//    sqlIdTP.add(sqlId, myTabbedPane);
//    sqlIdTP.setSelectedComponent(myTabbedPane);
//    JTabbedPane consoleTP = ConsoleWindow.getConsoleTP();
//    consoleTP.setSelectedIndex(5);

    if (ConsoleWindow.getDBVersion() >= 10.0) {
      myPlanSP.getViewport().setBackground(Color.WHITE); 
      myPlanPanel.add(myPlanSP);
      myTabbedPane.addTab("Plan/s",myPlanPanel);
    }
    
    if (ConsoleWindow.getDBVersion() >= 10.0) {
      myHistoryPanel.add(myHistorySP);
      myTabbedPane.addTab("History",myHistoryPanel);      
    }
    
    if (ConsoleWindow.getDBVersion() >= 10.0) {
      myBindCaptureSP.getViewport().setBackground(Color.WHITE); 
      myBindCapturePanel.add(myBindCaptureSP);
      myTabbedPane.addTab("Bind Variables Captured",myBindCapturePanel);
    }
    
//    if (ConsoleWindow.getDBVersion() > 11.0) {
//      myReportSQLMonitorSP.getViewport().setBackground(Color.WHITE);
//      myReportSQLMonitorPanel.add(myReportSQLMonitorSP);
//      myTabbedPane.addTab("SQL Monitor Report", myReportSQLMonitorPanel);
//    }
    
    if (ConsoleWindow.getDBVersion() >=10 && Properties.isSqlExecutionHistoryChart()) {
      executionHistoryChartSP.getViewport().setBackground(Color.WHITE);
      myExecutionHistoryChartPanel.add(executionHistoryChartSP);
      myTabbedPane.addTab("Execution Chart",myExecutionHistoryChartPanel);
    }
    
    ashScrollP.getViewport().setBackground(Color.WHITE);
    
    ASHPanel.add(ASHTopPanel,BorderLayout.NORTH);
    ASHPanel.add(ASHBottomPanel,BorderLayout.CENTER);
    ASHBottomPanel.add(ashScrollP,BorderLayout.CENTER);

    ashStartTime.setPreferredSize(new Dimension(110, 25));
    ashEndTime.setPreferredSize(new Dimension(110, 25));
    ASHTopPanel.add(ashStartL);
    ASHTopPanel.add(ashStartTime);
    ASHTopPanel.add(ashEndL);
    ASHTopPanel.add(ashEndTime);
    ASHTopPanel.add(ashButton);
    if (ConsoleWindow.getDBVersion() >= 11) myTabbedPane.addTab("ASH Report",ASHPanel);
    
    ashButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ashButton_actionPerformed();
          }
        });
    
    this.setLayout(new BorderLayout());;
    myTabbedPane.setVisible(false);
    this.add(myTabbedPane, BorderLayout.CENTER);
    
  }
  
  private void ashButton_actionPerformed() {
    String startTime = ashStartTime.getText();
    String endTime = ashEndTime.getText();
    
    int instanceNumber = ConsoleWindow.getThisInstanceNumber();
    long dbId;
    
    try {
      Cursor myCursor = new Cursor("getDBID.sql",true);
      QueryResult myResult = ExecuteDisplay.execute(myCursor, false, true, null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      dbId = Long.valueOf(resultSet[0][0]).longValue();

      String cursorId = "RichMonASHREPORT";
      myCursor = new Cursor(cursorId,false);
      myCursor.setSQLTxtOriginal("select output as \"Ash Report\" from table(dbms_workload_repository.ash_report_text( l_dbid=>" + dbId + ",l_inst_num=>" + instanceNumber +  
                                 ",l_btime=> to_date('" + startTime + "','dd-mon-yyyy hh24:mi'),l_etime=> to_date('" + endTime + "','dd-mon-yyyy hh24:mi'),l_sql_id=>'" + sqlId + "'));");
      
      Parameters myPars = new Parameters();
      ExecuteDisplay.executeDisplay(myCursor,myPars,ashScrollP,null,false,null,false);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this, "Exception creating an ASH report in text format from the SQL Detail Panel.");
    }
  }
  
  private void addSQLMonitorReport(String planHashValue, long sqlExecId, QueryResult myResult) {

    try {
      JPanel myPanel = new JPanel(new BorderLayout());
      JScrollPane mySP = new JScrollPane();

      mySP.getViewport().setBackground(Color.WHITE);
      myPanel.add(mySP);

      JTextPane myTP = new JTextPane();
      ExecuteDisplay.displayTextPane(myResult, myTP, " ", Color.BLUE);
      mySP.getViewport().add(myTP);


      if (planHashValue != null) {
        myTabbedPane.addTab("SQL Monitor Report" + " - PHV " + planHashValue, myPanel);
      } else {
        if (sqlExecId > 0) {
          myTabbedPane.addTab("SQL Monitor Report" + " - Execution " + sqlExecId, myPanel);
        } else {
          myTabbedPane.addTab("SQL Monitor Report - last execution", myPanel);
        }
      }
    } catch (Exception e) {
      // TODO: Add catch code
      e.printStackTrace();
    }
  }

  private JFreeChart createExecutionHistoryChart(QueryResult executionHistoryChartData,int startSnapId, int endSnapId, String[] snapDateRange) {
   
    String[][] myResultSet = executionHistoryChartData.getResultSetAsStringArray();
    
    DefaultCategoryDataset executionsDS = new DefaultCategoryDataset();
    DefaultCategoryDataset timeDS = new DefaultCategoryDataset();
    DefaultCategoryDataset getsReadsFetchesDS = new DefaultCategoryDataset();
    
    int maxExecutions = 0;
      
    int i = 0;
    while (i < myResultSet.length -1) {

      int executions = Integer.valueOf(myResultSet[i][2]).intValue();
      maxExecutions = Math.max(executions +5, maxExecutions);   // track largest value of executions - used for setting the upper range of the axis
      
      executionsDS.addValue(executions,"Executions",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      
      if (debug) System.out.println("executions: " + executions);
      
      double avgExecTime = Double.valueOf(myResultSet[i][5]).doubleValue();
      double avgCPUTime = Double.valueOf(myResultSet[i][6]).doubleValue();
      double avgClusterWaitTime = Double.valueOf(myResultSet[i][7]).doubleValue();
      double fetches = Double.valueOf(myResultSet[i][9]).doubleValue();
      double reads = Double.valueOf(myResultSet[i][12]).doubleValue();
      double gets = Double.valueOf(myResultSet[i][13]).doubleValue();
      
      timeDS.addValue(avgExecTime,"Average Execution Time",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      timeDS.addValue(avgCPUTime,"Average CPU Time",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      timeDS.addValue(avgClusterWaitTime,"Average Cluster Wait Time",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      getsReadsFetchesDS.addValue(fetches,"Fetches",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      getsReadsFetchesDS.addValue(reads,"Disk Reads",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      getsReadsFetchesDS.addValue(gets,"Buffer Gets",myResultSet[i][1] + " - " + myResultSet[i+1][1].substring(12));
      
      i++;
    }
    
    String chartTitle = "";
    if (startSnapId > 0) chartTitle = "Execution History Snapshot " + startSnapId + " to " + endSnapId;
    
    JFreeChart myChart = ChartFactory.createStackedBarChart3D(
                            chartTitle,
                            "",
                            "Executions",
                            executionsDS,
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
//    myDomainAxis.setRange(0,maxExecutions);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (snapDateRange.length >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (snapDateRange.length >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    
    myPlot.setDataset(1,timeDS);
    myPlot.setDataset(2,getsReadsFetchesDS);
    myPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    
    NumberAxis timeAxis = new NumberAxis("Time in Secs");
    timeAxis.setLabelFont(new Font("SansSerif",     Font.PLAIN, Properties.getAxisLabelFontSize()));
    timeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
        
    myPlot.setRangeAxis(1,timeAxis);
    myPlot.setRangeAxisLocation(1,AxisLocation.BOTTOM_OR_RIGHT);
    myPlot.mapDatasetToRangeAxis(1,1);        
    CategoryItemRenderer  sessionRenderer = new LineAndShapeRenderer();
    sessionRenderer.setSeriesShape(0,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(0,Color.GRAY);
    sessionRenderer.setSeriesShape(1,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(1,Color.YELLOW);
    sessionRenderer.setSeriesShape(2,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(2,Color.BLUE);
    myPlot.setRenderer(1,sessionRenderer);

    
    NumberAxis getsAxis = new NumberAxis("Gets/Reads/Fetches");
    getsAxis.setLabelFont(new Font("SansSerif",     Font.PLAIN, Properties.getAxisLabelFontSize()));
    getsAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
        
    myPlot.setRangeAxis(2,getsAxis);
    myPlot.setRangeAxisLocation(2,AxisLocation.BOTTOM_OR_RIGHT);
    myPlot.mapDatasetToRangeAxis(2,2);   
    CategoryItemRenderer  sessionRenderer2 = new LineAndShapeRenderer();
    sessionRenderer2.setSeriesShape(0,new Rectangle(0,0));
    sessionRenderer2.setSeriesPaint(0,Color.RED);
    sessionRenderer2.setSeriesShape(1,new Rectangle(0,0));
    sessionRenderer2.setSeriesPaint(1,Color.GREEN);
    sessionRenderer2.setSeriesShape(2,new Rectangle(0,0));
    sessionRenderer2.setSeriesPaint(2,Color.BLACK);
    myPlot.setRenderer(2,sessionRenderer2);

//    CategoryPlot timePlot = myChart.getCategoryPlot();
//    CategoryPlot getsPlot = myChart.getCategoryPlot();
    
    // set the line shape 
//    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
//    myRenderer.setSeriesShape(0,new Rectangle(0,0));
//    myRenderer.setSeriesShape(1,new Rectangle(0,0));   
//    myRenderer.setSeriesShape(2,new Rectangle(0,0));
//    myRenderer.setSeriesShape(3,new Rectangle(0,0));
//    myRenderer.setSeriesShape(4,new Rectangle(0,0));
//    myPlot.setRenderer(0,myRenderer);
    
//    setupAxis(1,timePlot,timeDS,"Right","Time in secs");
//    setupAxis(2,getsPlot,getsReadsFetchesDS,"Right","Gets / Reads / Fetches");
    
    return myChart;
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
    if (axisPos.toUpperCase().equals("RIGHT")) {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_RIGHT);
    }
    else {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_LEFT);          
    }
    myPlot.mapDatasetToRangeAxis(i,i); 
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    if (i==0) myRenderer.setSeriesPaint(0,Color.RED);
    if (i==1) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
    if (i==2) myRenderer.setSeriesPaint(0,Color.ORANGE);
    if (i==3) myRenderer.setSeriesPaint(0,Color.CYAN);
    if (i==4) myRenderer.setSeriesPaint(0,Color.MAGENTA);
    if (i==5) myRenderer.setSeriesPaint(0,Color.BLUE);
//    if (i==6) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
    myPlot.setRenderer(i,myRenderer);

    
    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);     
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    myAxis.setLowerBound(0.0);
    
//    if (i == 5) myAxis.setUpperBound(Math.max(Math.max(threeDSMaxValue,fourDSMaxValue) * 1.1,1));
//    if (i == 2) myAxis.setUpperBound(Math.max(Math.max(threeDSMaxValue,fourDSMaxValue) * 1.1,1));
//    if (i == 3) myAxis.setUpperBound(Math.max(fiveDSMaxValue * 2,1));
//    if (i == 4) myAxis.setUpperBound(Math.max(sixDSMaxValue * 2.25,1));

//    if (axisName.equals("cache hit percentage")) myAxis.setRange(0,100);
  }
}
