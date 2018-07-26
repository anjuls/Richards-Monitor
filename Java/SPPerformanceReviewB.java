/*
 * SPPerformanceReviewB.java        late V13
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
 * 09/08/06 Richard Wright Rounded % of time waited to 2 decimal places
 * 10/08/06 Richard Wright Modifyed calls to SPStatistics to specify whether a single axis is required
 * 10/08/06 Richard Wright Adding a progress monitor
 * 21/08/09 Richard Wright Temporary files now being deleted on exit
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 08/11/06 Richard Wright Added dba jobs, scheduler and sga target information
 * 08/11/06 Richard Wright Check the output dir exists, create it if needed
 * 11/12/06 Richard Wright Placed all data on the sorts chart against a single axis
 * 30/01/07 Richard Wright Add top sql by executions
 * 21/05/07 Richard Wright Added buffer pool advisory and modified pga advisory to work of statspack/awr
 * 12/09/07 Richard Wright Corrected some of the displayed descriptions when getting data from the database
 * 07/11/07 Richard Wright Check parameter setting before extracting dba jobs
 * 10/12/07 Richard Wright Enhanced for RAC
 * 07/12/08 Richard Wright Fixed the datatype of sqlorhashid binding for stats$awrtextv2.sql
 * 07/12/08 Richard Wright Modified the progress to know that it generates 72 objects
 * 21/05/08 Richard Wright Added event histogram
 * 22/05/08 Richard Wright Added database links
 * 17/06/08 Richard Wright Added lob details
 * 10/06/09 Richard Wright Add sqlPlanHistory for 10g and above
 * 22/09/09 Richard Wright Increased progress bar maximum from 74 to 95
 * 29/01/10 Richard Wright Allow the user to choose where perf review output is saved
 * 24/02/10 Richard Wright Output top sql by cpu time, elapsed time, version count & cluster wait time
 * 22/07/10 Richard Wright Modified cursorSummary to restrict output to the selected instance
 * 17/11/10 Richard Wright Modified getIndexesLargerThanTable() to stop it running as the output
 *                         could be misleading because it takes no account of partitioning
 * 25/11/10 Richard Wright Modified getReservedPool to flip the results
 * 28/01/11 Richard Wright Reduced font size of sql plan history output to account for extra columns that have been added
 * 12/10/11 Richard Wright Modified dbms_xplan calls to use 'ALL' instead of 'TYPICAL'
 * 06/07/12 Richard Wright Added the average wait events charts, the wait events class chart and the IOPS chart
 * 09/07/12 Richard Wright Stopped Top SQL by Elapsed Time being produced for db's less then 10
 * 17/07/12 Richard Wright Updated to ensure it works with some of the older versions of the database properly
 * 29/11/13 Richard Wright Added ability to avoid running the cpu time breakdown chart
 */

 package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ProgressMonitor;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Collect a number of different stats etc to make it easier to write a
 * performance review.
 */
public class SPPerformanceReviewB extends JButton {
  JLabel statusBar;                // The statusBar to be updated after a query is executed
  JScrollPane scrollP;             // The JScrollPane on which output should be displayed
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // Pointer to the statspack Panel

  JFrame collectionsF;
  JTabbedPane collectionsTP;

  JTextPane myPane = new JTextPane();  // fudge

  String cursorId;

  ProgressMonitor myProgressM;
  int progress;

  File saveDirectory;

  boolean debug = false;


  /**
   * Constructor
   *
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPPerformanceReviewB(StatspackAWRInstancePanel statspackPanel,String buttonName, JScrollPane scrollP, JLabel statusBar) {
      super(buttonName);

      this.scrollP = scrollP;
      this.statusBar = statusBar;
      this.statspackAWRInstancePanel = statspackPanel;

  }

  /**
   * Performs data collection
   *
   */
  public void actionPerformed() {
    try {
      statspackAWRInstancePanel.sanityCheckRange();

      String msg = "Charts and other data will be extracted and saved to disk for the snapshot period you have selected." +
                   "\nOld charts and data will be removed first." +
                   "\n\nThis can take a while for large snapshot ranges." +
                   "\n\n\nThe output will be saved to $RICHMON_BASE/Performance Review Output by default. \n" +
                   "Open the relevant template in word and run the RichMon macro to import the data.\n\n" +
                   "WARNING:  Some of the output will be specific to the instance you are connected too, so\n" +
                   "only run this when connected to the instance your extracting data for; And do not filter by username.";

      int answer = JOptionPane.showConfirmDialog(scrollP,msg,"Performance Data Extraction...",JOptionPane.CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);

      if (answer == 0) {
        JFileChooser fc = new JFileChooser("Choose an output directory");
        fc.setDialogTitle("Choose an Output Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String defaultFileName;

        if (ConnectWindow.isLinux()) {
          defaultFileName = ConnectWindow.getBaseDir() + "//Performance Review Output//" + ConsoleWindow.getInstanceName();
        }
        else {
          defaultFileName = ConnectWindow.getBaseDir() + "\\Performance Review Output\\" + ConsoleWindow.getInstanceName();
        }

        fc.setSelectedFile(new File(defaultFileName));
        int option = fc.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
          saveDirectory = fc.getSelectedFile();

          Thread collectionThread = new Thread ( new Runnable() {
            public void run() {
              collectChartsAndData();
              myProgressM.setProgress(myProgressM.getMaximum());
            }
          });

          collectionThread.setName("collectionThread");
          collectionThread.setDaemon(true);
          collectionThread.start();
        }
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
  }

  private void collectChartsAndData() {
    // Set up the progress monitor
    myProgressM = new ProgressMonitor(statspackAWRInstancePanel,"Extracting Performance Review Data...","",0,152);
    progress = 1;
    
    // Remove previously saved data
    myProgressM.setNote("Removing old data");
    myProgressM.setProgress(progress++);

    // Check the dir exists, if not create it
    if (!saveDirectory.exists()) {
      boolean ok = saveDirectory.mkdir();
    }

    // remove files that already exist in the output dir
    File[] oldFiles = saveDirectory.listFiles();
    for (int i=0; i < oldFiles.length; i++) oldFiles[i].delete();


    // Perform the data collections
    getDBVersion();
    getParallelTables();

    if (ConsoleWindow.getDBVersion() >= 9.0) {
      getResourceLimit();
    }
    else {
      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","resourceLimit.rtf");
      myProgressM.setProgress(progress++);
    }

    getFGWaitEvents();
    

    QueryResult topPhysicalSQLResult = getTopSQLByPhysicalReads();
    int numSQLRows = topPhysicalSQLResult.getNumRows();

    for (int i=0; i < 5; i++) {
      if (numSQLRows > i)  {
        try {
          getStatementAndPlan(topPhysicalSQLResult, i, "PhysicalReads");
        }
        catch (Exception e) {
          saveRTF("Problem getting statement and plan: " + e.toString(),"statementPhysicalReads" + (i +1) +  ".rtf");
          saveRTF("Please delete this section of the report","sqlPlanHistoryPhysical" + (i + 1) + ".rtf");
        }
      }
      else {
        saveRTF("Please delete this section of the report","statementPhysicalReads" + (i +1) +  ".rtf");
        myProgressM.setProgress(progress++);
      }
    }

    QueryResult topSQLBufferGets = getTopSQLByBufferGets();
    numSQLRows = topSQLBufferGets.getNumRows();

    for (int i=0; i < 5; i++) {
      if (numSQLRows > i)  {
        try {
          getStatementAndPlan(topSQLBufferGets,i,"BufferGets");
        }
        catch (Exception e) {
          saveRTF("Problem getting statement and plan: " + e.toString(),"statementBufferGets" + (i +1) +  ".rtf");
          saveRTF("Please delete this section of the report","sqlPlanHistoryBufferGets" + (i + 1) + ".rtf");
        }
      }
      else {
        saveRTF("Please delete this section of the report","statementBufferGets" + (i +1) + ".rtf");
        myProgressM.setProgress(progress++);
      }
    }

    QueryResult topSQLExecutions = getTopSQLByExecutions();
    numSQLRows = topSQLExecutions.getNumRows();

    for (int i=0; i < 5; i++) {
      if (numSQLRows > i)  {
        try {
          getStatementAndPlan(topSQLExecutions,i,"Executions");
        }
        catch (Exception e) {
          saveRTF("Problem getting statement and plan: " + e.toString(),"statementExecutions" + (i +1) +  ".rtf");
          saveRTF("Please delete this section of the report","sqlPlanHistoryExecutions" + (i + 1) + ".rtf");
        }
      }
      else {
        saveRTF("Please delete this section of the report","statementExecutions" + (i +1) + ".rtf");
        myProgressM.setProgress(progress++);
      }
    }

    if (ConsoleWindow.getDBVersion() >= 10.0) {
      QueryResult topSQLElapsedTime = getTopSQLByElapsedTime();
      numSQLRows = topSQLElapsedTime.getNumRows();

      for (int i = 0; i < 5; i++) {
        if (numSQLRows > i) {
          try {
            getStatementAndPlan(topSQLElapsedTime, i, "ElapsedTime");
          }
          catch (Exception e) {
            saveRTF("Problem getting statement and plan: " + e.toString(), "statementElapsedTime" + (i + 1) + ".rtf");
            saveRTF("Please delete this section of the report","sqlPlanHistoryElapsedTime" + (i + 1) + ".rtf");
          }
        }
        else {
          saveRTF("Please delete this section of the report", "statementElapsedTime" + (i + 1) + ".rtf");
          myProgressM.setProgress(progress++);
        }
      }
    }
    else {
      for (int i = 0; i < 5; i++) {
        saveRTF("Please delete this section of the report", "statementElapsedTime" + (i + 1) + ".rtf");
        saveRTF("Please delete this section of the report","sqlPlanHistoryElapsedTime" + (i + 1) + ".rtf");
        myProgressM.setProgress(progress++);
        saveRTF("Please delete this section of the report", "topSQLByElapsedTime.rtf");
        }
    }


    if (ConsoleWindow.getDBVersion() >= 10.0) {
      QueryResult topSQLClusterWaitTime = getTopSQLByClusterWaitTime();
      numSQLRows = topSQLClusterWaitTime.getNumRows();

      for (int i = 0; i < 5; i++) {
        if (numSQLRows > i) {
          try {
            getStatementAndPlan(topSQLClusterWaitTime, i, "ClusterWaitTime");
          }
          catch (Exception e) {
            saveRTF("Problem getting statement and plan: " + e.toString(), "statementClusterWaitTime" + (i + 1) + ".rtf");
            saveRTF("Please delete this section of the report","sqlPlanHistoryClusterWaitTime" + (i + 1) + ".rtf");
          }
        }
        else {
          saveRTF("Please delete this section of the report", "statementClusterWaitTime" + (i + 1) + ".rtf");
          myProgressM.setProgress(progress++);
        }
      }
    }
    else {
      for (int i = 0; i < 5; i++) {
        saveRTF("Please delete this section of the report", "statementClusterWaitTime" + (i + 1) + ".rtf");
        saveRTF("Please delete this section of the report","sqlPlanHistoryClusterWaitTime" + (i + 1) + ".rtf");
        myProgressM.setProgress(progress++);
        saveRTF("Please delete this section of the report", "topSQLByClusterWaitTime.rtf");

      }
    }

    if (ConsoleWindow.getDBVersion() >= 10.0) {
      QueryResult topSQLParseCalls = getTopSQLByParseCalls();
      numSQLRows = topSQLParseCalls.getNumRows();
  
      for (int i=0; i < 5; i++) {
        if (numSQLRows > i)  {
          try {
            getStatementAndPlan(topSQLParseCalls,i,"ParseCalls");
          }
          catch (Exception e) {
            saveRTF("Problem getting statement and plan: " + e.toString(),"statementParseCalls" + (i +1) +  ".rtf");
            saveRTF("Please delete this section of the report","sqlPlanHistoryParseCalls" + (i + 1) + ".rtf");
          }
        }
        else {
          saveRTF("Please delete this section of the report","statementParseCalls" + (i +1) + ".rtf");
          myProgressM.setProgress(progress++);
        }
      }
    }
    else {
      for (int i = 0; i < 5; i++) {
        saveRTF("Please delete this section of the report", "statementParseCalls" + (i + 1) + ".rtf");
        saveRTF("Please delete this section of the report","sqlPlanHistoryParseCalls" + (i + 1) + ".rtf");
        myProgressM.setProgress(progress++);
        saveRTF("Please delete this section of the report", "topSQLByParseCalls.rtf");
      }
    }

    if (ConsoleWindow.getDBVersion() >= 10.0) {
      QueryResult topSQLVersionCount = getTopSQLByVersionCount();
      numSQLRows = topSQLVersionCount.getNumRows();
  
      for (int i=0; i < 5; i++) {
        if (numSQLRows > i)  {
          try {
            getStatementAndPlan(topSQLVersionCount,i,"VersionCount");
          }
          catch (Exception e) {
            saveRTF("Problem getting statement and plan: " + e.toString(),"statementVersionCount" + (i +1) +  ".rtf");
            saveRTF("Please delete this section of the report","sqlPlanHistoryVersionCount" + (i + 1) + ".rtf");
          }
        }
        else {
          saveRTF("Please delete this section of the report","statementVersionCount" + (i +1) + ".rtf");
          myProgressM.setProgress(progress++);
        }
      }
    }
    else {
      for (int i = 0; i < 5; i++) {
        saveRTF("Please delete this section of the report", "statementVersionCount" + (i + 1) + ".rtf");
        saveRTF("Please delete this section of the report","sqlPlanHistoryVersionCount" + (i + 1) + ".rtf");
        myProgressM.setProgress(progress++);
        saveRTF("Please delete this section of the report", "topSQLByVersionCount.rtf");
      }
    }

    if (ConsoleWindow.getDBVersion() >= 10.0) {
      QueryResult topSQLCPU = getTopSQLByCPU();
      numSQLRows = topSQLCPU.getNumRows();

      for (int i = 0; i < 5; i++) {
        if (numSQLRows > i) {
          try {
            getStatementAndPlan(topSQLCPU, i, "CPU");
          }
          catch (Exception e) {
            saveRTF("Problem getting statement and plan: " + e.toString(), "statementCPU" + (i + 1) + ".rtf");
            saveRTF("Please delete this section of the report","sqlPlanHistoryCPU" + (i + 1) + ".rtf");
          }
        }
        else {
          saveRTF("Please delete this section of the report", "statementCPU" + (i + 1) + ".rtf");
          myProgressM.setProgress(progress++);
        }
      }
    }
    else {
      for (int i = 0; i < 5; i++) {
        saveRTF("Please delete this section of the report", "statementCPU" + (i + 1) + ".rtf");
        saveRTF("Please delete this section of the report","sqlPlanHistoryCPU" + (i + 1) + ".rtf");
        myProgressM.setProgress(progress++);
        saveRTF("Please delete this section of the report", "topSQLByCPU.rtf");
      }
    }

    if (ConsoleWindow.getDBVersion() >= 9.0)  {
      getPGAStat();
      getPGAAdvisory();
      getPGAWorkAreas();
      getPGAHistogram();
      getSegmentLogicalReads();
      getSegmentPhysicalReads();
      getSegmentRowLockWaits();
      getSegmentITLWaits();
      getSegmentBufferBusyWaits();
      getBufferPoolAdvisory();
    }
    else {

      ChartPanel myChartPanel = createDummyChart();

      saveChart("pgaStatChart.png",myChartPanel);
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","pgaAdvisory.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","segmentLogicalReads.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","segmentPhysicalReads.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","segmentRowLockWaits.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","segmentITLWaits.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","segmentBufferBusyWaits.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.  Please consider deleting this section of the report","bufferPoolAdvisory.rtf");
      myProgressM.setProgress(progress++);
    }



    if (ConsoleWindow.getDBVersion() >= 10.0) {
      getLogSwitchesChart();
      getOutlines10();
    }
    else {
      getLogSwitchesByHour();
      getOutlines();
    }

    if (ConsoleWindow.getDBVersion() >= 10.2) {
      getIOPSChart();
    }
    
    if (ConsoleWindow.getDBVersion() >= 10.0) {
      if (!Properties.isAvoid_awr()) getAverageWaitEventsByClassCharts();
      getOsLoadChart();
      getSchedulerJobs();
      getSGADynamicComponents();
      getSGAResizeOps();
      if (ConsoleWindow.getDBVersion() >= 10.2) {
        getSQLProfiles102();
      }
      else {
        getSQLProfiles10();
      }
      getWaitEventsClassChart();
      getIOEventsChart();
    }
    else {
      ChartPanel myChartPanel = createDummyChart();
      saveChart("osChart.png",myChartPanel);
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 10.  Please consider deleting this section of the report","schedulerJobs.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 10.  Please consider deleting this section of the report","sgaDynamicComponentsDetail.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 10.  Please consider deleting this section of the report","sgaResizeOperDetail.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 10.  Please consider deleting this section of the report","sqlProfiles.rtf");
      myProgressM.setProgress(progress++);
    }

    getUndoSummary();
    getUndoDetail();
    getLibraryCache();
    getLoadCallsChart();
    getLoadParseChart();
    getLoadPhysicalIOChart();
    getWaitEventsTxt();
    getReloadsInvalidationsChart();
    getLoadProfile();
    getStatisticsSummary();
    getNonDefaultParameters();
    getAllParameters();
    getSorts();
    getTableFetchContinuedRow();
    getRowChainingByTable();
    getSessionIdleTime();
    getCursorSummary();
    getInvalidObjectsSummary();
    getInvalidObjects();
    getNonSystemObjectsInSystem();
    getDefaultPasswords();
    getLatchSummary();
    getLatchSleeps();
    getTablespaceIO();
    getFileIO();
    getBufferCacheSummary();
    getBufferCacheChart();
    getBufferWaitStats();
    getEnqueue();
    getDatabaseLinks();
    getSequences();
    getLobs();
    getOPQDowngradesChart();
    getOPQOperationsChart();
    getOPQOperationsAndDowngradesChart();

    if (Properties.isExtractDbaJobsInPerfReview())  {
      getJobs();
    }
    else {
      saveRTF("You have chosen not to extract dba jobs data for this report","jobs.rtf");
      myProgressM.setProgress(progress++);
    };
     


    getRedoConfig();
    getSQLUsage();
    getTablesWithNoPrimaryKey();
    getSegmentsWithExcessiveExtents();
    getReservedPool();
    getOPQ();
    getIndexesLargerThanTable();

    if (ConsoleWindow.getDBVersion() >= 11 || (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr())) {
        getEventHistogram();
    }
    else {
      saveRTF("This data is available in 11g onwards or 10g using Statspack.  Please consider deleting this section of the report","eventHistogram.rtf");
      myProgressM.setProgress(progress++);
    }

    /**
     * Keep this section at the bottom of this method.  This seems to take forever
     * on 11g, and by having it last it doesn't matter so much if RichMon is killed.
     */
    if (ConsoleWindow.getDBVersion() >= 9.2) {
      if (Properties.isExtractCPUTimeBreakdownInPerfReview())  {
        getCPUTimeBreakdownTxt();
        getCPUTimeBreakdownChart();
      }
      else {
        saveRTF("You have chosen not to extract the CPU Time Breaksdown for this report","CPUTimeBreakdown.rtf");
        myProgressM.setProgress(progress++);
        saveRTF("You have chosen not to extract the CPU Time Breakdown chart for this report","CPUTimeBreakdownChart.png");
        myProgressM.setProgress(progress++);
      };
      getCPUTimeBreakdownTxt();
    }
    else {
      saveRTF("This data is not available prior to Oracle 9.2.  Please consider deleting this section of the report","CPUTimeBreakdown.rtf");
      myProgressM.setProgress(progress++);

      saveRTF("This data is not available prior to Oracle 9.2.  Please consider deleting this section of the report","CPUTimeBreakdownChart.png");
      myProgressM.setProgress(progress++);
    }
  }

  private QueryResult getTopSQLByPhysicalReads() {
    myProgressM.setNote("Getting the Top SQL by Physical Reads");
    myProgressM.setProgress(progress++);

    Parameters myPars = new Parameters();
//    Object[][] parameters;
    long benchmark = getStatistic("physical reads");

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyPhysicalReads10.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("long",benchmark);
      }
      else {
        cursorId = "topSQLbyPhysicalReadsAWR.sql";

        myPars.addParameter("long",benchmark);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("long",benchmark);
      }
    }
    else {
      if (ConsoleWindow.getDBVersion() >= 9) {
        cursorId = "topSQLbyPhysicalReads9.sql";

        myPars.addParameter("long",benchmark);
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("long",benchmark);
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 8.17) {
          cursorId = "topSQLbyPhysicalReads.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("long",benchmark);
        }
        else {
          cursorId = "topSQLbyPhysicalReads816.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        }
      }
    }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByPhysicalReads.rtf");
    }

    String topSQLByPhysicalReads = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByPhysicalReads output to a file
    saveRTF(topSQLByPhysicalReads,"topSQLByPhysicalReads.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByBufferGets() {
    myProgressM.setNote("Getting the Top SQL by Buffer Gets");
    myProgressM.setProgress(progress++);

    Parameters myPars = new Parameters();
//    Object[][] parameters;
    long benchmark = getStatistic("db block gets");
    benchmark = benchmark + getStatistic("consistent gets");

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }


    if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId = "topSQLbyBufferGets10.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

        }
        else {
          cursorId = "topSQLbyBufferGetsAWR.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        }
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 9) {
          cursorId = "topSQLbyBufferGets9.sql";

          myPars.addParameter("long",benchmark);
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        }
        else {
          if (ConsoleWindow.getDBVersion() >= 8.17) {
            cursorId = "topSQLbyBufferGets.sql";

            myPars.addParameter("long",benchmark);
            myPars.addParameter("int",startSnapId);
            myPars.addParameter("int",endSnapId);
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
          else {
            cursorId = "topSQLbyBufferGets816.sql";

            myPars.addParameter("long",benchmark);
            myPars.addParameter("int",startSnapId);
            myPars.addParameter("int",endSnapId);
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
        }
      }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByBufferGets.rtf");

    }

    String topSQLByBufferGets = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByBufferGets output to a file
    saveRTF(topSQLByBufferGets,"topSQLByBufferGets.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByExecutions() {
    myProgressM.setNote("Getting the Top SQL by Executions");
    myProgressM.setProgress(progress++);

    Parameters myPars = new Parameters();
    //Object[][] parameters;
    long benchmark = getStatistic("execute count");

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }


    if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId = "topSQLbyExecutions10.sql";

          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        }
        else {
          cursorId = "topSQLbyExecutionsAWR.sql";

          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());

        }
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 9) {
          cursorId = "topSQLbyExecutions9.sql";

          myPars.addParameter("int",startSnapId);
          myPars.addParameter("int",endSnapId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        }
        else {
          if (ConsoleWindow.getDBVersion() >= 8.17) {
            cursorId = "topSQLbyExecutions.sql";

            myPars.addParameter("int",startSnapId);
            myPars.addParameter("int",endSnapId);
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
          else {
            cursorId = "topSQLbyExecutions816.sql";

            myPars.addParameter("long",benchmark);
            myPars.addParameter("int",startSnapId);
            myPars.addParameter("int",endSnapId);
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
            myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
          }
        }

      }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByExecutions.rtf");

    }

    String topSQLByExecutions = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByExecutions output to a file
    saveRTF(topSQLByExecutions,"topSQLByExecutions.rtf");

    return myResult;
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
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();

    if (Properties.isAvoid_awr() == false) {
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
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      resultSet = myResult.getResultSetAsStringArray();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
    if (debug) System.out.println(statistic + " : " + resultSet[0][1]);

    return Long.valueOf(resultSet[0][0]).longValue();
  }

  private QueryResult getTopSQLByElapsedTime() {
    myProgressM.setNote("Getting the Top SQL by Elapsed Time");
    myProgressM.setProgress(progress++);

    long dbtime = getTimeModelStatistic("DB time");

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();

    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyElapsedTime10.sql";

      myPars.addParameter("long",dbtime);
      myPars.addParameter("long",dbtime);
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyElapsedTimeAWR.sql";

      myPars.addParameter("long",dbtime);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByElapsedTime.rtf");

    }

    String topSQLByElapsedTime = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByElapsedTime output to a file
    saveRTF(topSQLByElapsedTime,"topSQLByElapsedTime.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByVersionCount() {
    myProgressM.setNote("Getting the Top SQL by VersionCount");
    myProgressM.setProgress(progress++);

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();

    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyVersionCount10.sql";

      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyVersionCountAWR.sql";

      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByVersionCount.rtf");
    }

    String topSQLByVersionCount = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByExecutions output to a file
    saveRTF(topSQLByVersionCount,"topSQLByVersionCount.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByCPU() {
    myProgressM.setNote("Getting the Top SQL by CPU");
    myProgressM.setProgress(progress++);

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();

    long dbcpu = getTimeModelStatistic("DB CPU");

    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyCPU10.sql";

      myPars.addParameter("long", dbcpu);
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyCPUAWR.sql";

      myPars.addParameter("long", dbcpu);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByCPU.rtf");
    }

    String topSQLByCPU = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByCPU output to a file
    saveRTF(topSQLByCPU,"topSQLByCPU.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByClusterWaitTime() {
    myProgressM.setNote("Getting the Top SQL by Cluster Wait Time");
    myProgressM.setProgress(progress++);

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();

    if (Properties.isAvoid_awr()) {
      cursorId = "topSQLbyClusterWaitTime10.sql";

      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "topSQLbyClusterWaitTimeAWR.sql";

      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    }

    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByClusterWaitTime.rtf");
    }

    String topSQLByExecutions = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLByExecutions output to a file
    saveRTF(topSQLByExecutions,"topSQLByClusterWaitTime.rtf");

    return myResult;
  }

  private QueryResult getTopSQLByParseCalls() {
    myProgressM.setNote("Getting the Top SQL by Parse Calls");
    myProgressM.setProgress(progress++);
    long prse = getStatistic("parse count (total)");

    int startSnapId = 0;
    int endSnapId = 0;

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();


      if (Properties.isAvoid_awr()) {
        cursorId = "topSQLbyParseCalls10.sql";

        myPars.addParameter("long", prse);
        myPars.addParameter("int", startSnapId);
        myPars.addParameter("int", endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "topSQLbyParseCallsAWR.sql";

        myPars.addParameter("long", prse);
        myPars.addParameter("long", prse);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", startSnapId);
        myPars.addParameter("int", endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      }


    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","topSQLByParseCalls.rtf");

    }

    String topSQLbyParseCalls = ExecuteDisplay.createOutputString(myResult,true);

    // Save topSQLbyParseCalls output to a file
    saveRTF(topSQLbyParseCalls,"topSQLByParseCalls.rtf");

    return myResult;
  }

  private long getStatistic(String statistic) {
    cursorId = "stats$Statistics.sql";
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10.0 ) {
      cursorId = "stats$StatisticsAWR.sql";
    }

    int startSnapId = 0;
    int endSnapId = 0;

    String[][] resultSet = new String[1][1];

    try {
      startSnapId = statspackAWRInstancePanel.getStartSnapId();
      endSnapId = statspackAWRInstancePanel.getEndSnapId();
    }
    catch (NotEnoughSnapshotsException e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("String",statistic);


    try {
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      resultSet = myResult.getResultSetAsStringArray();

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    if (resultSet.length == 0) {
      return Long.valueOf("0").longValue();
    }
    else {
      return Long.valueOf(resultSet[0][1]).longValue();
    }
  }


  private void getStatementAndPlan(QueryResult myResult,int statementId, String suffix)  throws Exception {

    int t = statementId +1;
    myProgressM.setNote("Getting the SQL and Plan for Statement " + t +  " by " + suffix);
    myProgressM.setProgress(progress++);
    if (debug) System.out.println("Getting the SQL and Plan for Statement " + t +  " by " + suffix);

    // Get the sql_id / hash_value from the resultset
    String[][] resultSet = myResult.getResultSetAsStringArray();
    String sqlOrHashId = new String();
    String sqlText = new String();

    
    if (suffix.equals("Executions")) {
      if (ConsoleWindow.getDBVersion() >= 10.0) {
          if (Properties.isAvoid_awr()) {
              sqlOrHashId = resultSet[statementId][6];
          }
          else {
              sqlOrHashId = resultSet[statementId][6];
          }
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 8.17) {
          sqlOrHashId = resultSet[statementId][3];
        }
        else {
          sqlText = resultSet[statementId][5];
        }
      }
    }
    else {
      if (suffix.equals("VersionCount")) {
        sqlOrHashId = resultSet[statementId][2];
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 9.0) {
          sqlOrHashId = resultSet[statementId][6];
        } else {
          if (ConsoleWindow.getDBVersion() >= 8.17) {
            sqlOrHashId = resultSet[statementId][5];
          } else {
            sqlText = resultSet[statementId][5];
          }
        }
      }
    }

    cursorId = new String();
    Parameters myPars = new Parameters();
    if (ConsoleWindow.getDBVersion() >= 8.17) {
      // Get the formatted sql statement
      if (ConsoleWindow.getDBVersion() >= 10.0) {
        if (Properties.isAvoid_awr()) {
          cursorId = "stats$SqlText10.sql";
          myPars.addParameter("String",sqlOrHashId);
        }
        else {
          cursorId = "stats$SqlTextAWR.sql"; // original from 7th jan 08
          myPars.addParameter("String",sqlOrHashId);
          myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
          if (debug) System.out.println(sqlOrHashId);
          if (debug) System.out.println(statspackAWRInstancePanel.getDbId());
        }
      }
      else {
        cursorId = "stats$SqlText.sql";
        myPars.addParameter("long",sqlOrHashId);
      }
    }
    else {
      cursorId = "stats$SqlText.sql";
      myPars.addParameter("long",sqlOrHashId);
    }

    // sqlorHashId contains the sql_id or hash value at this point

    try {
      if (ConsoleWindow.getDBVersion() >= 8.17) {
        try {
          myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
        }
        catch (SQLException e) {
          if (debug) System.out.println("Exception in first sql thing: " + e.toString());
          cursorId = "stats$SqlTextAWRv2.sql";
//          myPars.addParameter("String",sqlOrHashId);
//          myPars.addParameter("long",StatspackAWRPanel.getDbId());

          myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
        }
      }
      else {
        // make a queryresult object with the sql text
        myResult = new QueryResult();
        Vector resultSetRow = new Vector();
        resultSetRow.addElement(sqlText);
        myResult.addResultRow(resultSetRow);
      }
      myResult = reFormatSQL(myResult);
      resultSet = myResult.getResultSetAsStringArray();
      QueryResult myPlan;

      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        // for 10g db's get the execution plan from the AWR repository
        String cursorId = "RichMonAWRPlanQueryFromPerfReview";
        Cursor myCursor = new Cursor(cursorId,false);
        if (ConsoleWindow.getDBVersion() >= 11) {
            myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_awr(sql_id => '" + sqlOrHashId + "',format => 'ALL'))\n");
        }
        else {
            myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_awr('" + sqlOrHashId + "'))\n");
        }
        
        myPlan = ExecuteDisplay.execute(myCursor,false,true,null);
      }
      else {
        StringBuffer sqlToExplain = new StringBuffer();
        for (int i=0; i < myResult.getNumRows(); i++) sqlToExplain.append(resultSet[i][0]);

        ExecutionPlan executionPlan = new ExecutionPlan(new JPanel());
        myPlan = executionPlan.runExplainPlan(sqlToExplain.toString());
      }

      int maxWidth = 0;

      QueryResult finalResult = new QueryResult();
      Vector row;
      for (int i=0; i < myResult.getNumRows(); i++) {
        row = new Vector();
        row = myResult.getResultSetRow(i);
        String r = (String)row.get(0);
        maxWidth = Math.max(r.length(),maxWidth);
        finalResult.addResultRow(row);
      }

      row = new Vector();
      row.add(" ");
      finalResult.addResultRow(row);
      finalResult.setNumCols(1);

      for (int i=0; i < myPlan.getNumRows(); i++) {
        row = new Vector();
        row = myPlan.getResultSetRow(i);
        String r = (String)row.get(0);
        maxWidth = Math.max(r.length(),maxWidth);
        finalResult.addResultRow(row);
      }

      String[] headings = {"SQL Statement + Plan"};
      finalResult.setResultHeadings(headings);
      int[] columnWidths = new int[1];
      columnWidths[0] = maxWidth;
      finalResult.setColumnWidths(columnWidths);

      statementId++;

      String statement = ExecuteDisplay.createOutputString(finalResult,true);

      saveRTF(statement,"statement" + suffix + statementId + ".rtf");


      // get the sql plan history for 10g and above
      if (ConsoleWindow.getDBVersion() >= 10.0) {
        String cursorId = "sqlPlanHistory10v2.sql";
        myPars = new Parameters();
        myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String",sqlOrHashId);

        try {
          myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
          String sqlPlanHistory = ExecuteDisplay.createOutputString(myResult,true);

          this.saveRTF(sqlPlanHistory,"sqlPlanHistory" + suffix + statementId + ".rtf",5);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"Error getting sql plan history for perf review");
        }
      }
      else {
        // output an empty file for the performance review template
        saveRTF(" ","sqlPlanHistory" + suffix + statementId + ".rtf");
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
  }

  private QueryResult reFormatSQL(QueryResult myResult) {
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
    newResult.setNumCols(colWidths.length);

    String[] resultHeadings = new String[1];
    resultHeadings[0] =  " SQL Statement (Re-Formatted)";

    newResult.setResultHeadings(resultHeadings);

    return newResult;
  }

  private void getResourceLimit() {
    myProgressM.setNote("Getting Resource Limits");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spResourceLimitB.createOutput();
      String resourceLimit = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(resourceLimit,"resourceLimit.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","resourceLimit.rtf");
    }
  }

  private void getCPUTimeBreakdownTxt() {
    myProgressM.setNote("Getting CPU Time Breakdown (Text) -- if this is very slow then gather fresh stats on the dictionary or kill Richmon as this is the last step");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spWaitEventsB.getCPUTimeBreakdownTxt();
      String resourceLimit = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(resourceLimit,"CPUTimeBreakdown.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","CPUTimeBreakdown.rtf");
    }
  }

  private void getDBVersion() {
    myProgressM.setNote("Getting the Database Version");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("version.sql",false,false,null);
      String dbVersion = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(dbVersion,"v$version.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","v$version.rtf");
   }
  }

  private void getJobs() {
    myProgressM.setNote("Getting Database Jobs");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("jobs.sql",false,true,null);
      String jobs = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(jobs,"jobs.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","jobs.rtf");
    }
  }

  private void getEventHistogram() {
    myProgressM.setNote("Getting Event Histogram");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spWaitEventsB.getWaitEventHistogram();
      String eventHistogram = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(eventHistogram,"eventHistogram.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","eventHistogram.rtf");
    }
  }

  private void getSQLUsage() {
    myProgressM.setNote("Getting SQL Usage");
    myProgressM.setProgress(progress++);

    try {
      Parameters myPars = new Parameters();
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

      String cursorId = "sqlUsage.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String SQLUsage = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(SQLUsage,"SQLUsage.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","SQLUsage.rtf");
    }
  }

  private void getRedoConfig() {
    myProgressM.setNote("Getting Redo Configuration");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("redoConfig.sql",false,true,null);
      String redoConfig = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(redoConfig,"redoConfig.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","redoConfig.rtf");
    }
  }

  private void getTablesWithNoPrimaryKey() {
    myProgressM.setNote("Getting Tables With No Primary Key");
    myProgressM.setProgress(progress++);

    try {
        String cursorId = "tablesWithNoPrimaryKey.sql";
        QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
        String tablesWithNoPrimaryKey = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(tablesWithNoPrimaryKey,"tablesWithNoPrimaryKey.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","tablesWithNoPrimaryKey.rtf");
    }
  }

  private void getSegmentsWithExcessiveExtents() {
    myProgressM.setNote("Getting Tables With Excessive Extents");
    myProgressM.setProgress(progress++);

    try {
        String cursorId = "segmentsWithExcessiveExtents.sql";
        QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
        String segmentsWithExcessiveExtents = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentsWithExcessiveExtents,"segmentsWithExcessiveExtents.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentsWithExcessiveExtents.rtf");
    }
  }

  private void getReservedPool() {
    myProgressM.setNote("Getting V$Shared_Pool_Reserved");
    myProgressM.setProgress(progress++);

    try {
        String cursorId = "sharedPoolReserved.sql";
        QueryResult myResult = ExecuteDisplay.execute(cursorId,true,true,null);
        String reservedPool = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(reservedPool,"reservedPool.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","reservedPool.rtf");
    }
  }

  private void getOPQ() {
    myProgressM.setNote("Getting Parallel Query");
    myProgressM.setProgress(progress++);

    try {
        QueryResult myResult = ConsoleWindow.getDatabasePanel().parallelQueryB.getPQsysstat(null);
        String opq = ExecuteDisplay.createOutputString(myResult,false);

      // Save wait events output to a file
      saveRTF(opq,"opq.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","opq.rtf");
    }
  }

  private void getSchedulerJobs() {
    myProgressM.setNote("Getting Database Scheduler Jobs");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("schedulerJobs.sql",false,true,null);
      String jobs = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(jobs,"schedulerJobs.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","schedulerJobs.rtf");
    }
  }

  private void getLatchSummary() {
    myProgressM.setNote("Getting Latch Summary");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spLatchB.getLatchSummary();
      String latchSummary = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(latchSummary,"latchSummary.rtf",5);
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","latchSummary.rtf");
    }
  }
  private void getParallelTables() {
    myProgressM.setNote("Getting Tables with parallelism defined");
    myProgressM.setProgress(progress++);

    try {
      String cursorId = "parallelTables.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String parallelTables = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(parallelTables,"parallelTables.rtf",5);
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","parallelTables.rtf");
    }
  }

  private void getLatchSleeps() {
    myProgressM.setNote("Getting Latch Sleeps");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spLatchB.getLatchMisses();
      String latchSleeps = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(latchSleeps,"latchSleeps.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","latchSleeps.rtf");
   }
  }

  private void getBufferCacheSummary() {
    myProgressM.setNote("Getting Buffer Cache Summary");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spBufferCacheB.getBufferCacheSummary();
      String bufferCache = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(bufferCache,"bufferCacheSummary.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","bufferCacheSummary.rtf");
    }
  }

  private void getBufferWaitStats() {
    myProgressM.setNote("Getting Buffer Wait Statistics");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spBufferCacheB.getBufferWaitStatistics();
      String bufferWaitStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(bufferWaitStats,"bufferWaitStats.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","bufferWaitStats.rtf");
    }
  }

  private void getIndexesLargerThanTable() {
    myProgressM.setNote("Getting List of Indexes larger than the Table");
    myProgressM.setProgress(progress++);

    /*
     * Commented out because this sql does not take account of partitioning
     * and therefore the results of potentially misleading
     *
     * 17-Nov-10
     */
/*    try {
      String cursorId = "indexesLargerThanTable.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String bufferWaitStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(bufferWaitStats,"indexesLargerThanTable.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","indexesLargerThanTable.rtf");
    }
*/
    saveRTF("This output is deliberately blank.  Remove this section of the report.","indexesLargerThanTable.rtf");
  }

  private void getSegmentLogicalReads() {
    myProgressM.setNote("Getting Segments by Logical Reads");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSegmentWaitsB.getSegmentLogicalReads();
      String segmentStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentStats,"segmentLogicalReads.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentLogicalReads.rtf");
    }
  }

  private void getSegmentRowLockWaits() {
    myProgressM.setNote("Getting Segments by Row Lock Waits Waits");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSegmentWaitsB.getSegmentRowLockWaits();
      String segmentStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentStats,"segmentRowLockWaits.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentRowLockWaits.rtf");
    }
  }

  private void getSegmentPhysicalReads() {
    myProgressM.setNote("Getting Segments by Physical Reads");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSegmentWaitsB.getSegmentPhysicalReads();
      String segmentStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentStats,"segmentPhysicalReads.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentPhysicalReads.rtf");
    }
  }

  private void getSegmentITLWaits() {
    myProgressM.setNote("Getting Segments by ITL Waits");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSegmentWaitsB.getSegmentITLWaits();
      String segmentStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentStats,"segmentITLWaits.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentITLWaits.rtf");
    }
  }

  private void getSegmentBufferBusyWaits() {
    myProgressM.setNote("Getting Segments by Buffer Busy Waits");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSegmentWaitsB.getSegmentBufferBusyWaits();
      String segmentStats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segmentStats,"segmentBufferBusyWaits.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","segmentBufferBusyWaits.rtf");
    }
  }

  private void getBufferCacheChart() {
    myProgressM.setNote("Getting the Buffer Cache Chart");
    myProgressM.setProgress(progress++);

    ChartPanel bufferCacheChart;
    try {
      bufferCacheChart = statspackAWRInstancePanel.spBufferCacheB.getBufferCacheChart();
      saveChart("bufferCacheChart.png",bufferCacheChart);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("bufferCacheChart.png",createErrorChart());
    }
  }

  private void getCPUTimeBreakdownChart() {
    myProgressM.setNote("Getting the CPU Time Breakdown Chart -- if this is very slow then gather fresh stats on the dictionary or kill Richmon as this is the last step");
    myProgressM.setProgress(progress++);

    JFreeChart cpuTimeBreakdownChart;
    try {
      cpuTimeBreakdownChart = statspackAWRInstancePanel.spWaitEventsB.createCPUTimeBreakdownChart(true);
      saveChart("CPUTimeBreakdownChart.png",cpuTimeBreakdownChart);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("CPUTimeBreakdownChart.png",createErrorChart());
    }
  }

  private void getTablespaceIO() {
    myProgressM.setNote("Getting Tablespace I/O");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spTablespaceFileB.getTablespaceIO();
      String tablespaceIO = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(tablespaceIO,"tablespaceIO.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","tablespaceIO.rtf");
    }
  }

  private void getFileIO() {
    myProgressM.setNote("Getting File I/O");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spTablespaceFileB.getFileIO();
      String fileIO = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(fileIO,"fileIO.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","fileIO.rtf");
    }
  }

  private void getEnqueue() {
    myProgressM.setNote("Getting Enqueue");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spEnqueueB.getEnqueue();
      String enqueue = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(enqueue,"enqueue.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","enqueue.rtf");
    }
  }

  private void getSessionIdleTime() {
    myProgressM.setNote("Getting Session Idle Time");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "sessionIdleTime.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String sessionIdleTime = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(sessionIdleTime,"sessionIdleTime.rtf",5);
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sessionIdleTime.rtf");
    }
  }

  private void getDefaultPasswords() {
    myProgressM.setNote("Getting Schema's with Default Passwords");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "defaultPasswords.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String defaultPasswords = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(defaultPasswords,"defaultPasswords.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","defaultPasswords.rtf");
    }
  }

  private void getCursorSummary() {
    myProgressM.setNote("Getting a Summary of Cursor's");
    myProgressM.setProgress(progress++);

    try {
      Parameters myPars = new Parameters();
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

      cursorId = "cursorSummary.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String cursorSummary = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(cursorSummary,"cursorSummary.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","cursorSummary.rtf");
    }
  }

  private void getNonSystemObjectsInSystem() {
    myProgressM.setNote("Getting Non System Segments in the System Tablespace");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "nonSystemObjectsInSystem.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String segments = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(segments,"nonSystemObjectsInSystem.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","nonSystemObjectsInSystem.rtf");
    }
  }

  private void getInvalidObjectsSummary() {
    myProgressM.setNote("Getting Invalid Objects Summary");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "invalidObjectsSummary.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String invalidObjectsSummary = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(invalidObjectsSummary,"invalidObjectsSummary.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","invalidObjectsSummary.rtf");
    }
  }

  private void getInvalidObjects() {
    myProgressM.setNote("Getting Invalid Objects");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "invalidObjects.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String invalidObjects = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(invalidObjects,"invalidObjects.rtf");
      }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","invalidObjects.rtf");
    }
  }

  private void getWaitEventsTxt() {
    myProgressM.setNote("Getting Wait Events (Txt)");
    myProgressM.setProgress(progress++);

    try {
      QueryResult finalResult = generateWaitEventsTxt();
      String waitEvents = ExecuteDisplay.createOutputString(finalResult,true);

      // Save wait events output to a file
      saveRTF(waitEvents,"waitEvents.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","waitEvents.rtf");
    }
  }

  private void getDatabaseLinks() {
    myProgressM.setNote("Getting Database Links");
    myProgressM.setProgress(progress++);

    try {
      String cursorId = "databaseLinks.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String dbLinks = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(dbLinks,"dbLinks.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","dbLinks.rtf");
    }
  }

  private void getLobs() {
    myProgressM.setNote("Getting Lob details and average lob sizes");
    myProgressM.setProgress(progress++);

    try {
      if (Properties.getLobSampleSize() > 0) {
        // generate the sql to get lob details
        cursorId = "generateGetLobDetails.sql";
//        QueryResult myResult = ExecuteDisplay.execute(cursorId);
        QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
        String[][] resultSet = myResult.getResultSetAsStringArray();
        int numberOfLobs = myResult.getNumRows();
        int[] columnWidths = { 5, 10, 11, 12, 5, 6, 10 };
        String[] columnHeadings =
        { "Owner", "Table Name", "Column Name", "Segment Name", "Cache", "In Row", "Avg Length" };
        QueryResult completeResult = new QueryResult();
        completeResult.setNumCols(7);
        String sqlrun = null;

        for (int i = 0; i < myResult.getNumRows(); i++) {
          myProgressM.setNote("Getting Lob details and average lob sizes " + i + " of " + myResult.getNumRows());

          String cursorId = "RichMonGetLobDetails";
          Cursor myCursor = new Cursor(cursorId,false);
          myCursor.setSQLTxtOriginal(resultSet[i][0] + " where rownum < " + (Properties.getLobSampleSize() + 1));
          sqlrun = resultSet[i][0] + " where rownum < " + (Properties.getLobSampleSize() + 1);
          QueryResult myLobResult = ExecuteDisplay.execute(myCursor,false,false,null);

          completeResult.addResultRow(myLobResult.getResultSetRow(0));

          int[] tmp = myLobResult.getColumnWidths();
          for (int cw = 0; cw < 7; cw++) {
            columnWidths[cw] = Math.max(columnWidths[cw], tmp[cw]);
          }

          if (i == 0) completeResult.setResultHeadings(myLobResult.getResultHeadings());
        }

        completeResult.setNumCols(7);
        completeResult.setNumRows(numberOfLobs);
        completeResult.setColumnWidths(columnWidths);
        completeResult.setResultHeadings(columnHeadings);

        String lobs = ExecuteDisplay.createOutputString(completeResult, true);

        // Save wait events output to a file
        saveRTF(lobs, "lobDetails.rtf");
      }
      else {
        saveRTF("No lob data was collected as the sample size was specified as zero in richmon.properties", "lobDetails.rtf");
      }
    }
    catch (Exception e) {
      String msg =
        "\n\nThere was a problem calculating the average lob length so \n" +
        "Lob Details will be listed without the average length instead.\n\n" +
        "You could try File=>Alter Session set current schema to get around this.\n\n";
      ConsoleWindow.displayError(e, this, msg);

      try {
        String cursorId = "getLobDetails.sql";
        QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);

        String lobs = ExecuteDisplay.createOutputString(myResult, true);

        // Save wait events output to a file
        saveRTF(lobs, "lobDetails.rtf");
      }
      catch (Exception ee) {
        saveRTF("There was a problem collecting lob details: " + ee, "lobDetails.rtf");
      }
    }
  }

  private void getSequences() {
    myProgressM.setNote("Getting Sequences");
    myProgressM.setProgress(progress++);

    try {
      String cursorId = "sequences.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String sequences = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(sequences,"sequences.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sequences.rtf");
    }
  }

  public QueryResult generateWaitEventsTxt() {
    QueryResult finalResult = new QueryResult();
    try {
      // Get total CPU first for use in calculating % if time waiting
      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        cursorId = "stats$StatisticsAWR.sql";
      }
      else {
        cursorId = "stats$Statistics.sql";
      }

      Parameters myPars = new Parameters();
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String","CPU used by this session");

      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      double tCPU;
      if (ConsoleWindow.getDBVersion() >= 9.0) {
        tCPU = Double.valueOf(resultSet[0][1]).doubleValue() * 10;
      }
      else {
        tCPU = 0;
      }


      float dbCPU = 0;
      if (ConsoleWindow.getDBVersion() >= 10) {
        dbCPU = statspackAWRInstancePanel.spWaitEventsB.getDBCPU(statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      }
      if (ConsoleWindow.getDBVersion() >= 9 && ConsoleWindow.getDBVersion() < 10) {
        dbCPU = statspackAWRInstancePanel.spWaitEventsB.getTotalCPU(statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      }
      dbCPU = dbCPU / 10000;
      
      float dbTime = 0;
      if (ConsoleWindow.getDBVersion() >= 10) {
        dbTime = statspackAWRInstancePanel.spWaitEventsB.getDBTime(statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getDbId(), statspackAWRInstancePanel.getInstanceNumber());
      }

      dbTime = dbTime / 10000;
      
      double totalCPU;
      totalCPU = Float.valueOf(dbCPU).doubleValue();
       myResult = statspackAWRInstancePanel.spWaitEventsB.createOutput(true);

      String[][] chartResult = myResult.getResultSetAsStringArray();
      /* add onto total cpu the wait time of each event */
      for (int i=0; i < myResult.getNumRows(); i++) {
        if (!chartResult[i][0].equals("CPU time")) {
          if (ConsoleWindow.getDBVersion() >= 9.0 && ConsoleWindow.getDBVersion() < 10) {
              totalCPU = totalCPU + (Double.valueOf(chartResult[i][2]).doubleValue() * 1000) ;
          }
//          else {
//              totalCPU = totalCPU + (Double.valueOf(chartResult[i][2]).doubleValue() * 100000) ;
//          }
        }
      }

      String[][] newResult = new String[myResult.getNumRows()][myResult.getNumCols() +2];

      for (int i=0; i < myResult.getNumRows(); i++) {
        newResult[i][0] = chartResult[i][0];                  // event
        if (!chartResult[i][0].equals("CPU time") && !chartResult[i][0].equals("DB CPU")) {          // # waits
          newResult[i][1] = chartResult[i][1];
        }
        else {
          newResult[i][1] = " ";
        }
        if (ConsoleWindow.getDBVersion() >= 9.0) {
            double d = Double.valueOf(chartResult[i][2]).doubleValue()*10;
            d = Math.round(d);
            newResult[i][2] = String.valueOf(d/10);
//            newResult[i][2] = String.valueOf(Math.round(Double.valueOf(chartResult[i][2]).doubleValue()*10)/10); // time waited (s)
        }
        else {
            newResult[i][2] = String.valueOf(Math.round(Double.valueOf(chartResult[i][2]).doubleValue() * 1000)/10); // time waited (s)
        }
        if (!chartResult[i][0].equals("CPU time") && !chartResult[i][0].equals("DB CPU")) {   // avg wait time (ms)
          if (ConsoleWindow.getDBVersion() >= 9.0) {
              newResult[i][3] = String.valueOf(Math.round((Double.valueOf(chartResult[i][2]).doubleValue() / Double.valueOf(chartResult[i][1]).doubleValue()) * 1000));
          }
          else {
              newResult[i][3] = String.valueOf(Math.round(((Double.valueOf(chartResult[i][2]).doubleValue() * 100 )/ Double.valueOf(chartResult[i][1]).doubleValue()) * 10));
          }
        }
        else {
          newResult[i][3] = " ";
        }

        double percent = 0;
        if (ConsoleWindow.getDBVersion() >= 9.0 && ConsoleWindow.getDBVersion() < 10) {
            percent = (Double.valueOf(chartResult[i][2]).doubleValue()/ (tCPU / 10000)) * 1000;
        }
        
        if (ConsoleWindow.getDBVersion() >= 10.0) {
            percent = ((Double.valueOf(chartResult[i][2]).doubleValue() * 100) / (dbTime / 10000)) * 1000;
        }


        percent = percent / 100000;
        percent = (Double.valueOf(Math.round(percent * 10)).doubleValue() /10);
//        percent = Math.round(percent * 10) /10;
        newResult[i][4] = String.valueOf(percent);     // % of time waited
      }


      finalResult = new QueryResult();
      finalResult.setNumCols(5);
      for (int i=0; i < myResult.getNumRows(); i++) {
        Vector row = new Vector();
        row.add(newResult[i][0]);
        row.add(newResult[i][1]);
        row.add(newResult[i][2]);
        row.add(newResult[i][3]);
        row.add(newResult[i][4]);

        finalResult.addResultRow(row);
      }

      String[] columnHeadings = {"EVENT","# WAITS","TIME WAITED (S)","AVG WAIT TIME (MS)","% TIME WAITED"};
      if (ConsoleWindow.getDBVersion() <= 9.0) {
        columnHeadings[2] = "TIME WAITED (CS)";
      }
      if (ConsoleWindow.getDBVersion() >= 10.0) {
        columnHeadings[4] = "% Total Call Time";
      }      
      if (ConsoleWindow.getDBVersion() >= 11.0) {
        columnHeadings[4] = "% DB Time";
      }

      finalResult.setResultHeadings(columnHeadings);
      int[] origColumnWidths = myResult.getColumnWidths();
      int[] newColumnWidths = new int[5];
      newColumnWidths[0] = origColumnWidths[0];
      newColumnWidths[1] = Math.max(origColumnWidths[1],7);
      if (ConsoleWindow.getDBVersion() >= 9.0) {
        newColumnWidths[2] = 15;
      }
      else {
        newColumnWidths[2] = 16;
      }
      newColumnWidths[3] = Math.max(origColumnWidths[2],18);
      newColumnWidths[4] = 13;

      finalResult.setColumnWidths(newColumnWidths);
      finalResult.setNumCols(5);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }

    return finalResult;
  }

  private void getLogSwitchesByHour() {
      myProgressM.setNote("Getting the Log Switches by Hour Chart");
      myProgressM.setProgress(progress++);

      try {
        String instanceNumber = String.valueOf(statspackAWRInstancePanel.getInstanceNumber());
        ChartPanel logSwitchesByHourChart = ConsoleWindow.getDatabasePanel().redoB.getLogSwitchesByHourChart(instanceNumber,"");
        saveChart("logSwitchesChart.png",logSwitchesByHourChart);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Report Generation still possible");
        this.saveErrorChart("logSwitchesChart.png",createErrorChart());
      }
    }
  
  private void getAverageWaitEventsByClassCharts() {
    String cursorId = "listWaitClasses.sql";
    QueryResult myResult = new QueryResult();
    try {
      myResult = ExecuteDisplay.execute(cursorId, false, true, null);
    } catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
    
    String[][] resultSet = myResult.getResultSetAsStringArray();
    
    for (int i=0; i < myResult.getNumRows(); i++) {
      myProgressM.setNote("Getting the Average Wait Events Chart" + resultSet[i][0]);
      myProgressM.setProgress(progress++);
      
      JFreeChart myChart;
      String chartNameSuffix = resultSet[i][0].replaceAll(" ","");
      if (chartNameSuffix.contains("/")) {
        chartNameSuffix = chartNameSuffix.substring(0, chartNameSuffix.indexOf("/")) + chartNameSuffix.substring(chartNameSuffix.indexOf("/")+1);
      }
        
      try {
        myChart = statspackAWRInstancePanel.spWaitEventsB.createAverageWaitEventsChart(false,resultSet[i][0]);
        saveChart("averageWaitEventsChart" + chartNameSuffix + ".png",myChart);
      }
      catch(Exception e) {
        ConsoleWindow.displayError(e,this,"Report Generation still possible");
        this.saveErrorChart("averageWaitEventsChart" + chartNameSuffix + ".png",createErrorChart());
      }
    }
  } 

  private void getFGWaitEvents() {
    myProgressM.setNote("Getting the Foreground Wait Events Chart");
    myProgressM.setProgress(progress++);

    JFreeChart waitEventsChart;
    try {
      waitEventsChart = statspackAWRInstancePanel.spWaitEventsB.createFGWaitEventsChart(false);
      saveChart("waitEventsChart.png",waitEventsChart);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("waitEventsChart.png",createErrorChart());
    }
  }  
  
  private void getWaitEventsClassChart() {
    myProgressM.setNote("Getting the Wait Events Class Chart");
    myProgressM.setProgress(progress++);

    JFreeChart waitEventsChart;
    try {
      waitEventsChart = statspackAWRInstancePanel.spWaitEventsB.createWaitClassChart(false);
      saveChart("waitEventsClassChart.png",waitEventsChart);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("waitEventsClassChart.png",createErrorChart());
    }
  }  
  
  private void getIOEventsChart() {
    myProgressM.setNote("Getting the IO Events Chart");
    myProgressM.setProgress(progress++);

    JFreeChart waitEventsChart;
    try {
      waitEventsChart = statspackAWRInstancePanel.spWaitEventsB.createIOEventChart(false);
      saveChart("ioEventsChart.png",waitEventsChart);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("ioEventsChart.png",createErrorChart());
    }
  }

  private void getPGAStat() {
    myProgressM.setNote("Getting the PGA Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spPGAStatsB.createPGAStatChart();
      saveChart("pgaStatChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("pgaStatChart.png",createErrorChart());
    }
  }

  private void getLoadCallsChart() {
    myProgressM.setNote("Getting the Calls Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spOtherChartsB.createCallsChart();
      saveChart("loadCallsChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("loadCallsChart.png",createErrorChart());
    }
  }

  private void getLoadParseChart() {
    myProgressM.setNote("Getting the Parse Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spOtherChartsB.createParseChart();
      saveChart("loadParseChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("loadParseChart.png",createErrorChart());
    }
  }

  private void getLoadPhysicalIOChart() {
    myProgressM.setNote("Getting the physical I/O Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spOtherChartsB.createPhysicalIOChart();
      saveChart("loadPhysicalIOChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("loadPhysicalIOChart.png",createErrorChart());
    }
  }  
  
  private void getIOPSChart() {
    myProgressM.setNote("Getting the IOPS Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spOtherChartsB.createIOPSChart();
      saveChart("loadIOPSChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("loadIOPSChart.png",createErrorChart());
    }
  }

  private void getLogSwitchesChart() {
    myProgressM.setNote("Getting the Log Switches Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spLogSwitchB.createLogSwitchChart();
      saveChart("logSwitchesChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("loadSwitchesChart.png",createErrorChart());
    }
  }

  private void getOsLoadChart() {
    myProgressM.setNote("Getting the OS Load Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spOsLoadB.createOSLoadChart();
      saveChart("osChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("osChart.png",createErrorChart());
    }
  }

  private void getReloadsInvalidationsChart() {
    myProgressM.setNote("Getting the Reloads and Invalidations Chart");
    myProgressM.setProgress(progress++);

    try {
      ChartPanel myChartPanel = statspackAWRInstancePanel.spReloadInvalidationsB.createChart();
      saveChart("reloadsInvalidationsChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      this.saveErrorChart("reloadsInvalidationsChart.png",createErrorChart());
    }
  }

  private void getLoadProfile() {
    myProgressM.setNote("Getting Load Profile");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spSummaryB.getloadProfile();
      String loadProfile = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(loadProfile,"loadProfile.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","loadProfile.rtf");
   }
  }

  private void getStatisticsSummary() {
    myProgressM.setNote("Getting Statistics Summary");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("statisticsSummary.sql",false,true,null);
      String stats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(stats,"statisticsSummary.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","statisticsSummary.rtf");
    }
  }

  private void getRowChainingByTable() {
    myProgressM.setNote("Getting Row Chaining by Table");
    myProgressM.setProgress(progress++);

    try {
      cursorId = "tablesWithChainedRows.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String stats = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(stats,"tablesWithChainedRows.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","tablesWithChainedRows.rtf");
    }
  }

  private void getNonDefaultParameters() {
    myProgressM.setNote("Getting Non-Default Parameters");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ConsoleWindow.getDatabasePanel().parametersB.getNonDefaultParameters(null);
      String parameters = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(parameters,"nonDefaultParameters.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","nonDefaultParameters.rtf");
    }
  }

  private void getAllParameters() {
    myProgressM.setNote("Getting All Parameters");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ConsoleWindow.getDatabasePanel().parametersB.getAllParameters(null);
      String parameters = ExecuteDisplay.createOutputString(myResult,true);

      // Save wait events output to a file
      saveRTF(parameters,"allParameters.rtf");     }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","allParameters.rtf");
    }
  }

  private void getSorts() {
    myProgressM.setNote("Getting Sorts");
    myProgressM.setProgress(progress++);

    try {
      String[] sorts = {"sorts (disk)","sorts (memory)"};
      ChartPanel myChartPanel = statspackAWRInstancePanel.spStatisticsB.createChart(sorts, true);
      saveChart("sortsChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveErrorChart("sortsChart.png",createErrorChart());
    }
  }

  private void getTableFetchContinuedRow() {
    myProgressM.setNote("Getting Table Fetch Continued Row");
    myProgressM.setProgress(progress++);

    try {
      String[] fetch = {"table fetch continued row","table fetch by rowid"};
      ChartPanel myChartPanel = statspackAWRInstancePanel.spStatisticsB.createChart(fetch, true);
      saveChart("tableFetchChart.png",myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveErrorChart("tableFetchChart.png",createErrorChart());
    }
  }

  private void getPGAAdvisory() {
    myProgressM.setNote("Getting the PGA Advisory");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spPGAStatsB.getPGAAdvisory();
      String advisory = ExecuteDisplay.createOutputString(myResult,true);

      /* Save wait events output to a file */
      saveRTF(advisory,"pgaAdvisory.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","pgaAdvisory.rtf");
    }
  }

  private void getPGAWorkAreas() {
    myProgressM.setNote("Getting the PGA Workareas");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spPGAStatsB.getPGAWorkareas();
      String advisory = ExecuteDisplay.createOutputString(myResult,true);

      /* Save wait events output to a file */
      saveRTF(advisory,"pgaWorkareas.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","pgaWorkareas.rtf");
    }
  }  
  
  private void getPGAHistogram() {
    myProgressM.setNote("Getting the PGA Histogram");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spPGAStatsB.getPGAHistogram();
      String advisory = ExecuteDisplay.createOutputString(myResult,true);

      /* Save wait events output to a file */
      saveRTF(advisory,"pgaHistogram.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","pgaHistogram.rtf");
    }
  }

  private void getBufferPoolAdvisory() {
    myProgressM.setNote("Getting the Buffer Cache Advisory");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spBufferCacheB.getBuffeCacheAdvisory();
      String advisory = ExecuteDisplay.createOutputString(myResult,true);

      /* Save wait events output to a file */
      saveRTF(advisory,"bufferPoolAdvisory.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","bufferPoolAdvisory.rtf");
    }
  }

  private void getLibraryCache() {
    myProgressM.setNote("Getting Library Cache");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spLibraryCacheB.createOutput();
      String libraryCache = ExecuteDisplay.createOutputString(myResult,true);

      /* Save libraryCache output to a file */
      saveRTF(libraryCache,"libraryCache.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","libraryCache.rtf");
    }
  }

  private void getOPQDowngradesChart() {
    myProgressM.setNote("Getting OPQ Downgrades Chart");
    myProgressM.setProgress(progress++);

    ChartPanel opqDowngradesCP;
    try {
      opqDowngradesCP = statspackAWRInstancePanel.spOPQB.createOPQDowngradesChart();
      saveChart("OPQDowngradesChart.png", opqDowngradesCP);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","libraryCache.rtf");
    }
  }  
  
  private void getOPQOperationsAndDowngradesChart() {
    myProgressM.setNote("Getting OPQ Operations and Downgrades Chart");
    myProgressM.setProgress(progress++);

    ChartPanel opqDowngradesCP;
    try {
      opqDowngradesCP = statspackAWRInstancePanel.spOPQB.createOPQOperationsAndDowngradesChart();
      saveChart("OPQOperationsAndDowngradesChart.png", opqDowngradesCP);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","libraryCache.rtf");
    }
  }

  private void getOPQOperationsChart() {
    myProgressM.setNote("Getting OPQ Operations and Downgrades Chart");
    myProgressM.setProgress(progress++);

    ChartPanel opqDowngradesCP;
    try {
      opqDowngradesCP = statspackAWRInstancePanel.spOPQB.createOPQOperationsChart();
      saveChart("OPQOperationsChart.png", opqDowngradesCP);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","libraryCache.rtf");
    }
  }
  
  private void getOutlines() {
    myProgressM.setNote("Getting Outlines");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("outlines.sql", false, true, null);
      String outlines = ExecuteDisplay.createOutputString(myResult,true);

      /* Save outlines output to a file */
      saveRTF(outlines,"outlines.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","outlines.rtf");
    }
  }

  private void getOutlines10() {
    myProgressM.setNote("Getting Outlines");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("outlines10.sql", false, true, null);
      String outlines = ExecuteDisplay.createOutputString(myResult,true);

      /* Save outlines output to a file */
      saveRTF(outlines,"outlines.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","outlines.rtf");
    }
  }

  private void getSQLProfiles10() {
    myProgressM.setNote("Getting SQL Profiles");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("sqlProfiles10.sql", false, true, null);
      String profiles = ExecuteDisplay.createOutputString(myResult,true);

      /* Save outlines output to a file */
      saveRTF(profiles,"profiles.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sqlProfiles.rtf");
    }
  }

  private void getSQLProfiles102() {
    myProgressM.setNote("Getting SQL Profiles");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ExecuteDisplay.execute("sqlProfiles102.sql", false, true, null);
      String profiles = ExecuteDisplay.createOutputString(myResult,true);

      /* Save outlines output to a file */
      saveRTF(profiles,"profiles.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sqlProfiles.rtf");
    }
  }

  private void getUndoSummary() {
    myProgressM.setNote("Getting Undo Summary");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spUndoB.getUndoSummary();
      String undoSummary = ExecuteDisplay.createOutputString(myResult,true);

      /* Save undo summary output to a file */
      saveRTF(undoSummary,"undoSummary.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","undoSummary.rtf");
    }
  }

  private void getUndoDetail() {
    myProgressM.setNote("Getting Undo Detail");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = statspackAWRInstancePanel.spUndoB.getUndoDetail();
      String undoDetail = ExecuteDisplay.createOutputString(myResult,true);

      /* Save undo detail output to a file */
      saveRTF(undoDetail,"undoDetail.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","undoDetail.rtf");
    }
  }

  private void getSGADynamicComponents() {
    myProgressM.setNote("Getting SGA Dynamic Components Detail");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ConsoleWindow.getDatabasePanel().parametersB.getSGADynamicComponents(null);
      String undoDetail = ExecuteDisplay.createOutputString(myResult,true);

      /* Save undo detail output to a file */
      saveRTF(undoDetail,"sgaDynamicComponentsDetail.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sgaDynamicComponentsDetail.rtf");
    }
  }

  private void getSGAResizeOps() {
    myProgressM.setNote("Getting SGA Reize Operation Detail");
    myProgressM.setProgress(progress++);

    try {
      QueryResult myResult = ConsoleWindow.getDatabasePanel().parametersB.getSGAResizeOps(null);
      String undoDetail = ExecuteDisplay.createOutputString(myResult,true);

      /* Save undo detail output to a file */
      saveRTF(undoDetail,"sgaResizeOperDetail.rtf");
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
      saveRTF("There was an error producing this data","sgaResizeOperDetail.rtf");
    }
  }

  private void saveRTF(String outputText,String fileName) {
    saveRTF(outputText, fileName, 6);
  }

  private void saveRTF(String outputText, String fileName, int fontSize) {
    SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
    StyleConstants.setForeground(outputAttrs, Color.BLACK);
    StyleConstants.setFontSize(outputAttrs,fontSize);
    StyleConstants.setFontFamily(outputAttrs,"courier new");
    StyleConstants.setAlignment(outputAttrs,StyleConstants.ALIGN_JUSTIFIED);

    //myPane.setLogicalStyle();
    Document myDoc = myPane.getStyledDocument();
    RTFEditorKit kit = new RTFEditorKit();

    try {
      File outputF;
      if (ConnectWindow.isLinux()) {
       outputF = new File(saveDirectory + "//" + fileName);
      }
      else {
        outputF = new File(saveDirectory + "\\" + fileName);
      }
      if (outputF.exists()) outputF.delete();

      myDoc.remove(0,myDoc.getLength());
      myDoc.insertString(myDoc.getLength(),outputText,outputAttrs);

      OutputStream outOS = new FileOutputStream(outputF);
      kit.write(outOS, myDoc, 0, myDoc.getLength());

      outOS.close();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
  }

  private ChartPanel createDummyChart() {
    DefaultCategoryDataset oneDS = new DefaultCategoryDataset();
    JFreeChart myChart = ChartFactory.createLineChart("Please delete this section of the report",
                                                      "Please delete this section of the report",
                                                      "Please delete this section of the report",
                                                      oneDS,PlotOrientation.VERTICAL,
                                                      true,
                                                      true,
                                                      true);

    ChartPanel myChartPanel = new ChartPanel(myChart);

    return myChartPanel;
  }

  private ChartPanel createErrorChart() {
    DefaultCategoryDataset oneDS = new DefaultCategoryDataset();
    JFreeChart myChart = ChartFactory.createLineChart("There was an error producing this chart",
                                                      "There was an error producing this chart",
                                                      "There was an error producing this chart",
                                                      oneDS,PlotOrientation.VERTICAL,
                                                      true,
                                                      true,
                                                      true);

    ChartPanel myChartPanel = new ChartPanel(myChart);

    return myChartPanel;
  }

  private void saveChart(String fileName,ChartPanel myChartPanel) {
    JFreeChart myChart = myChartPanel.getChart();
    saveChart(fileName,myChart);
  }

  private void saveErrorChart(String fileName,ChartPanel myChartPanel) {
    JFreeChart myChart = myChartPanel.getChart();
    saveChart(fileName,myChart);
  }

  private void saveChart(String fileName,JFreeChart myChart) {
    try {
      File outputF;
      if (ConnectWindow.isLinux()) {
       outputF = new File(saveDirectory + "//" + fileName);
      }
      else {
        outputF = new File(saveDirectory + "\\" + fileName);
      }

      ChartUtilities.saveChartAsPNG(outputF,myChart,800,600);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Report Generation still possible");
    }
  }
}