 /*
  * TextViewPanel.java        13.05 03/10/05
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
  * Change History
  * ==============
  * 
  * 28/02/05 Richard Wright Switched comment style to match that 
  *                         recommended in Sun's own coding conventions.
  * 14/03/05 Richard Wright Seperated out the code for the wait event chart, 
  *                         load chart and shared pool chart into seperate 
  *                         classes.
  * 02/06/05 Richard Wright Stopped any result sets from flipping if single row
  * 02/06/05 Richard Wright Set the chart objects to null if no longer selected 
  *                         to save memory
  * 02/06/05 Richard Wright Added OPQ Summary
  * 10/06/05 Richard Wright Modified the query chosen to run for the sharedPoolChart 
  *                         so that if connected as sysdba the free space in the 
  *                         reserved pool is given
  * 17/06/05 Richard Wright Modified the script names used in getting wait event to call
  *                         the new scripts which eliminate idle events from the same
  *                         list as statspack.  Moved code adding blank lines to the 
  *                         wait events chart out to the WaitEventsChart class.
  * 14/09/05 Richard Wright Corrected bug that made long ops data appear in the 
  *                         wait events scroll pane
  * 03/10/05 Richard Wright Added new scripts to allow all the idle events in 10.2 to be 
  *                         removed from wait event displays
  * 24/10/05 Richard Wright Added the os load chart for 10g db's and above
  * 28/10/05 Richard Wright Made foreground text blue on the control buttons
  * 28/10/05 Richard Wright Reduced the width of the iteration spinner to make 
  *                         more room for checkboxes on the right of the screen
  * 28/02/06 Richard Wright Add the OSLoadChart to the display
  * 15/03/06 Richard Wright Add the CircuitsChart to the display
  * 23/03/06 Richard Wright Replace references to 'c:' with a call to 
  *                         ConnectWindow.getBaseDir() and ConnectWindow.isLinux()
  * 30/04/06 Richard Wright Introduced the vertical flow panels to improve the 
  *                         checkbox layout
  * 14/07/06 Richard Wright Adds Calls Chart
  * 14/07/06 Richard Wright Add Parse Chart
  * 27/07/06 Richard Wright Added Physical IO Chart and removed load chart
  * 22/08/06 Richard Wright Modified the comment style and error handling
  * 14/09/06 Richard Wright Removed the vltsSoakTestingChart
  * 08/01/07 Richard Wright Made the options panel bigger to display properly on a laptop
  * 14/05/07 Richard Wright Removed unused code
  * 20/11/07 Richard Wright Enhanced for RAC
  * 12/05/09 Richard Wright Blockers and Waiters that use blocking_session on v$session only do so after 11g
  * 15/05/09 Richard Wright The blockers and waiters scripts no longer need changing for rac.
  * 23/10/09 Richard Wright Modified to allow recording of data for later playback.
  * 23/10/09 Richard Wright Set the background to white so that it matches other panels before any data has been displayed
  * 08/03/10 Richard Wright Use configurable standard button size
  * 28/07/11 Richard Wright Changed name to textViewPanel and iterate buttons to start and stop
  * 22/09/11 Richard Wright Remove the 'start recording' button
  * 01/03/12 Richard Wright Reduce the screen estate taken up by control buttons to allow more room for the checkboxes
  *                         Because one user reports not being able to see all the checkboxes
  * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
  * 16/09/15 Richard Wright Fixed display of execution time for shared server output
  * 02/02/15 Richard Wright Code Tidyup
  */


 package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DateFormat;

import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;

import oracle.jdeveloper.layout.VerticalFlowLayout;


/**
 * A panel which iterates at user defined intervals to display selected results
 * which give an overview of current database performance.
 */
public class TextViewPanel extends RichPanel {
  // panels 
  JPanel optionsLP = new JPanel();
  JPanel optionsL1P = new JPanel();
  JPanel optionsL2P = new JPanel();
  JPanel optionsL3P = new JPanel();
  JPanel optionsL4P = new JPanel();
  JPanel optionsL5P = new JPanel();
  JPanel optionsL6P = new JPanel();

  private JPanel allButtonsP = new JPanel();
  private JPanel controlButtonP = new JPanel();
  private JPanel controlButton2P = new JPanel();
  private JPanel overviewP = new JPanel();
  private BoxLayout boxLayout1 = new BoxLayout(overviewP, BoxLayout.Y_AXIS);

  // controlButton2P panel buttons 
//  private JButton iterateOnceB = new JButton("Iterate Once");
  private JToggleButton startStopTB = new JToggleButton("Start", false);
  private JLabel iterationsL = new JLabel("not iterating");
  private SpinnerNumberModel snm = new SpinnerNumberModel(5, 1, 600, 1);
  static JSpinner iterationSleepS;
  private JToggleButton spoolTB = new JToggleButton("Spool Output");
//  private JToggleButton recordTB = new JToggleButton("Start Recording");

  // checkboxes for optionsP panel 
  private JCheckBox waitEventsCB = new JCheckBox("Wait Events", true);
  private JCheckBox poolFreeSpaceCB = new JCheckBox("Pool Free Space");
  private JCheckBox longOperationsCB = new JCheckBox("Long Operations");
  private JCheckBox undoCB = new JCheckBox("Undo");
  private JCheckBox sortCB = new JCheckBox("Sorts");
  private JCheckBox runningJobsCB = new JCheckBox("Running Jobs");
  private JCheckBox lockWaitersCB = new JCheckBox("Lock Waiters");
  private JCheckBox lockBlockersCB = new JCheckBox("Lock Blockers");
  private JCheckBox dispatchersCB = new JCheckBox("Dispatchers");
  private JCheckBox sharedServersCB = new JCheckBox("Shared Servers");
  private JCheckBox parallelQueryCB = new JCheckBox("OPQ Summary");
  private JCheckBox sqlMonitorCB = new JCheckBox("SQL Monitor");

  // scrollPanes for each element to be displayed in overiewP 
  private JScrollPane waitEventsSP = new JScrollPane();
  private JScrollPane poolFreeSpaceSP = new JScrollPane();
  private JScrollPane longOperationsSP = new JScrollPane();
  private JScrollPane undoSP = new JScrollPane();
  private JScrollPane sortSP = new JScrollPane();
  private JScrollPane runningJobsSP = new JScrollPane();
  private JScrollPane lockWaitersSP = new JScrollPane();
  private JScrollPane lockBlockersSP = new JScrollPane();
  private JScrollPane loadSP = new JScrollPane();
  private JScrollPane dispatchersSP = new JScrollPane();
  private JScrollPane sharedServersSP = new JScrollPane();
  private JScrollPane parallelQuerySP = new JScrollPane();
  private JScrollPane sqlMonitorSP = new JScrollPane();

  // is a component already displayed 
  boolean waitEventsSummaryDisplayed = false;
  boolean loadChartDisplayed = false;
  boolean sharedPoolDisplayed = false;
  boolean osLoadDisplayed = false;
  boolean waitEventsDisplayed = false;
  boolean poolFreeSpaceDisplayed = false;
  boolean longOperationsDisplayed = false;
  boolean undoDisplayed = false;
  boolean sortDisplayed = false;
  boolean runningJobsDisplayed = false;
  boolean lockWaitersDisplayed = false;
  boolean lockBlockersDisplayed = false;
  boolean loadDisplayed = false;
  boolean dispatchersDisplayed = false;
  boolean sharedServersDisplayed = false;
  boolean parallelQueryDisplayed = false;
  boolean sqlMonitorDisplayed = false;

  // table & column models 
  ConsoleTableModel waitEventsCTM;
  ConsoleColumnModel waitEventsCCM;
  ConsoleTableModel poolFreeSpaceCTM;
  ConsoleColumnModel poolFreeSpaceCCM;
  ConsoleTableModel longOperationsCTM;
  ConsoleColumnModel longOperationsCCM;
  ConsoleTableModel undoCTM;
  ConsoleColumnModel undoCCM;
  ConsoleTableModel sortCTM;
  ConsoleColumnModel sortCCM;
  ConsoleTableModel runningJobsCTM;
  ConsoleColumnModel runningJobsCCM;
  ConsoleTableModel lockWaitersCTM;
  ConsoleColumnModel lockWaitersCCM;
  ConsoleTableModel lockBlockersCTM;
  ConsoleColumnModel lockBlockersCCM;
  ConsoleTableModel loadCTM;
  ConsoleColumnModel loadCCM;
  ConsoleTableModel dispatchersCTM;
  ConsoleColumnModel dispatchersCCM;
  ConsoleTableModel sharedServersCTM;
  ConsoleColumnModel sharedServersCCM;
  ConsoleTableModel parallelQueryCTM;
  ConsoleColumnModel parallelQueryCCM;
  ConsoleTableModel sqlMonitorCTM;
  ConsoleColumnModel sqlMonitorCCM;

  // statusBar 
  private JLabel statusBarL = new JLabel();

  // misc 
  private Dimension standardButtonSize;
//  private Dimension buttonSize2 = new Dimension(55, 25);
  private String[][] dispatcherValues;
  private int dispatchersIdleTime;
  private int dispatchersBusyTime;
  private String[][] sharedServerValues;
  private int sharedServerRequests;
  private int sharedServerIdleTime;
  private int sharedServerBusyTime;
  private int numSharedServers;

  // spooling stuff 
  private OutputHTML outputHTML;
   boolean currentlySpooling = false;
  private File saveFile;
  private BufferedWriter save;

  // iteration stuff 
  private int numIterations = 0; // 0=don't iterate, 1=iterate once, 2=keep iterating 
  private int iterationCounter = 0; // how many times have we iterated since last told to start 
  private int totalIterations = 0; // how many times have we iterated - ever! 

  private boolean waitEventsOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean undoOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean lockWaitersOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean lockBlockersOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean dispatchersOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean longOperationsOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean poolFreeSpaceOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean sortOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean sharedServersOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean parallelQueryOnlyLocalInstance = true; // true if only the local instance was selected last iteration
  private boolean sqlMonitorOnlyLocalInstance = true; // true if only the local instance was selected last iteration

  
  boolean record = false;
  String recordExecutionDate;
  String recordFirstIterationStartTime;

  /**
   * Constructor
   */
  public TextViewPanel() {
    try {
      iterationSleepS = new JSpinner(snm);
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Defines all the components that make up the frame.
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception {
    standardButtonSize = Properties.getStandardButtonDefaultSize();
    
    // layout 
    this.setLayout(new BorderLayout());

    // controlButton2P Panel 
    controlButton2P.setLayout(new GridBagLayout());
    GridBagConstraints controlCons = new GridBagConstraints();
    controlCons.gridx = 0;
    controlCons.gridy = 0;
    controlCons.insets = new Insets(3, 3, 3, 3);
    controlCons.gridy = 0;
    iterationsL.setFont(new Font("????", 1, 13));
    iterationsL.setForeground(Color.BLACK);
    controlButton2P.add(iterationsL, controlCons);
    controlCons.gridy = 1;
    spoolTB.setPreferredSize(standardButtonSize);
    spoolTB.setForeground(Color.BLUE);
    spoolTB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            spoolTB_actionPerformed();
          }
        });
/*    recordTB.setPreferredSize(standardButtonSize);
    recordTB.setForeground(Color.BLUE);
    recordTB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            recordTB_actionPerformed();
          }
        });
*/
    controlButton2P.add(spoolTB, controlCons); 
//    controlButton2P.add(recordTB, controlCons);
    controlCons.gridx = 0;
    controlCons.gridy = 1;
/*    iterateOnceB.setPreferredSize(standardButtonSize);
    iterateOnceB.setForeground(Color.BLUE);
    iterateOnceB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            iterateOnceB_actionPerformed();
          }
        });
    controlButton2P.add(iterateOnceB, controlCons);
*/
    controlCons.gridx = 1;
    controlCons.gridy = 0;
    startStopTB.setPreferredSize(standardButtonSize);
    startStopTB.setForeground(Color.BLUE);
    startStopTB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            startStopTB_actionPerformed();
          }
        });
    controlButton2P.add(startStopTB, controlCons);
    controlCons.gridx = 1;
    controlCons.gridy = 1;
//    iterationSleepS.setPreferredSize(buttonSize2);
    iterationSleepS.setPreferredSize(standardButtonSize);
    iterationSleepS.setForeground(Color.BLUE);
    controlButton2P.add(iterationSleepS, controlCons);


    // controlButtonP panel 
    controlButtonP.add(controlButton2P, null);
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // statusBar 
    statusBarL.setText(" ");

    // optionsP panel 
    optionsLP = new JPanel() {
          public Insets getInsets() {
            return new Insets(-1, -1, -1, -1);
          }
        };

    optionsLP.setLayout(new FlowLayout());
    optionsL1P.setLayout(new VerticalFlowLayout());
    optionsL2P.setLayout(new VerticalFlowLayout());
    optionsL3P.setLayout(new VerticalFlowLayout());
    optionsL4P.setLayout(new VerticalFlowLayout());
    optionsL5P.setLayout(new VerticalFlowLayout());
    optionsL6P.setLayout(new VerticalFlowLayout());
    optionsLP.add(optionsL1P);
    optionsLP.add(optionsL2P);
    optionsLP.add(optionsL3P);
    optionsLP.add(optionsL4P);
    optionsLP.add(optionsL5P);
    optionsLP.add(optionsL6P);

    optionsL1P.add(waitEventsCB);
    optionsL1P.add(poolFreeSpaceCB);
    optionsL5P.add(longOperationsCB);
    optionsL2P.add(undoCB);
    optionsL2P.add(sortCB);
    optionsL5P.add(runningJobsCB);
    optionsL3P.add(lockWaitersCB);
    optionsL3P.add(lockBlockersCB);
    optionsL6P.add(parallelQueryCB);
    optionsL4P.add(dispatchersCB);
    optionsL4P.add(sharedServersCB);
    

    optionsLP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // allButtonsP panel 
    allButtonsP.setLayout(new BorderLayout());
    allButtonsP.add(controlButtonP, BorderLayout.WEST);
    allButtonsP.add(optionsLP, BorderLayout.CENTER);

    // construct sessionPanel 
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(overviewP, BorderLayout.CENTER);
    this.add(allButtonsP, BorderLayout.NORTH);

    // overviewP panel 
    overviewP.setLayout(boxLayout1);
    waitEventsSP.getViewport().setBackground(Color.WHITE);
    poolFreeSpaceSP.getViewport().setBackground(Color.WHITE);
    longOperationsSP.getViewport().setBackground(Color.WHITE);
    undoSP.getViewport().setBackground(Color.WHITE);
    sortSP.getViewport().setBackground(Color.WHITE);
    runningJobsSP.getViewport().setBackground(Color.WHITE);
    lockWaitersSP.getViewport().setBackground(Color.WHITE);
    lockBlockersSP.getViewport().setBackground(Color.WHITE);
    loadSP.getViewport().setBackground(Color.WHITE);
    dispatchersSP.getViewport().setBackground(Color.WHITE);
    sharedServersSP.getViewport().setBackground(Color.WHITE);
    parallelQuerySP.getViewport().setBackground(Color.WHITE);
    sqlMonitorSP.getViewport().setBackground(Color.WHITE);

    waitEventsSP.setBorder(BorderFactory.createTitledBorder("Wait Events"));
    poolFreeSpaceSP.setBorder(BorderFactory.createTitledBorder("Pool Free Space"));
    longOperationsSP.setBorder(BorderFactory.createTitledBorder("Long Operations"));
    undoSP.setBorder(BorderFactory.createTitledBorder("Undo"));
    sortSP.setBorder(BorderFactory.createTitledBorder("Sort"));
    runningJobsSP.setBorder(BorderFactory.createTitledBorder("Running Jobs"));
    lockBlockersSP.setBorder(BorderFactory.createTitledBorder("Lock Blockers"));
    lockWaitersSP.setBorder(BorderFactory.createTitledBorder("Lock Waiters"));
    dispatchersSP.setBorder(BorderFactory.createTitledBorder("Dispatchers"));
    sharedServersSP.setBorder(BorderFactory.createTitledBorder("Shared Servers"));
    parallelQuerySP.setBorder(BorderFactory.createTitledBorder("Parallel Query Summary"));
    sqlMonitorSP.setBorder(BorderFactory.createTitledBorder("SQL Monitor"));
    
    overviewP.setBackground(Color.white);
    
    
    if (ConsoleWindow.getDBVersion() >= 11.0) optionsL6P.add(sqlMonitorCB);

  }

  /**
   * Starts a new deamon thread which executes the selected options and then 
   * sleeps for n seconds, as defined by the iteraction interval.
   */
  private void iterate() {
    Thread sessionOverviewThread = new Thread(new Runnable() {
          public void run() {
            while (numIterations > 0) {
              // increment iteration counters 
              iterationCounter++;
              totalIterations++;

              // make this the last iteration if this is a once only 
              if (numIterations == 1)
                numIterations = 0;

              // update iterations label seen on the screen 
              iterationsL.setText("Iteration " + iterationCounter);


              try {
                
                if (currentlySpooling) {
                  outputHTML.saveIteration(iterationCounter);
                }
                
                if (dispatchersCB.isSelected()) {
                  dispatchers();
                }
                else {
                  if (dispatchersDisplayed) {
                    overviewP.remove(dispatchersSP);
                    dispatchersDisplayed = false;
                  }
                }

                if (sharedServersCB.isSelected()) {
                  sharedServers();
                }
                else {
                  if (sharedServersDisplayed) {
                    overviewP.remove(sharedServersSP);
                    sharedServersDisplayed = false;
                  }
                }

                if (waitEventsCB.isSelected()) {
                  waitEvents();
                }
                else {
                  if (waitEventsDisplayed) {
                    overviewP.remove(waitEventsSP);
                    waitEventsDisplayed = false;
                  }
                }

                if (poolFreeSpaceCB.isSelected()) {
                  poolFreeSpace();
                }
                else {
                  if (poolFreeSpaceDisplayed) {
                    overviewP.remove(poolFreeSpaceSP);
                    poolFreeSpaceDisplayed = false;
                  }
                }

                if (longOperationsCB.isSelected()) {
                  longOperations();
                }
                else {
                  if (longOperationsDisplayed) {
                    overviewP.remove(longOperationsSP);
                    longOperationsDisplayed = false;
                  }
                }

                if (undoCB.isSelected()) {
                  undo();
                }
                else {
                  if (undoDisplayed) {
                    overviewP.remove(undoSP);
                    undoDisplayed = false;
                  }
                }

                if (sortCB.isSelected()) {
                  sort();
                }
                else {
                  if (sortDisplayed) {
                    overviewP.remove(sortSP);
                    sortDisplayed = false;
                  }
                }

                if (runningJobsCB.isSelected()) {
                  runningJobs();
                }
                else {
                  if (runningJobsDisplayed) {
                    overviewP.remove(runningJobsSP);
                    runningJobsDisplayed = false;
                  }
                }

                if (lockBlockersCB.isSelected()) {
                  lockBlockers();
                }
                else {
                  if (lockBlockersDisplayed) {
                    overviewP.remove(lockBlockersSP);
                    lockBlockersDisplayed = false;
                  }
                }
                
                if (lockWaitersCB.isSelected()) {
                  lockWaiters();
                }
                else {
                  if (lockWaitersDisplayed) {
                    overviewP.remove(lockWaitersSP);
                    lockWaitersDisplayed = false;
                  }
                }

                if (parallelQueryCB.isSelected()) {
                  parallelQuery();
                }
                else {
                  if (parallelQueryDisplayed) {
                    overviewP.remove(parallelQuerySP);
                    parallelQueryDisplayed = false;
                  }
                }                
                
                if (sqlMonitorCB.isSelected()) {
                  sqlMonitor();
                }
                else {
                  if (sqlMonitorDisplayed) {
                    overviewP.remove(sqlMonitorSP);
                    sqlMonitorDisplayed = false;
                  }
                }
              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this);
                if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().equals("java.sql.SQLRecoverableException: IO Error: Connection reset by peer: socket write error")) numIterations = 0;
                if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().equals("java.sql.SQLRecoverableException: Closed Connection")) numIterations = 0;
                startStopTB.setSelected(false);
                startStopTB.setText("Start");
              }

              if (numIterations > 0) {
                // if another iteration is required, sleep first 
                long sleepMillis = Integer.valueOf(String.valueOf(iterationSleepS.getValue())).intValue();
                sleepMillis = sleepMillis * 1000; // convert seconds to milli seconds 

                try {
                  Thread.sleep(sleepMillis);
                }
                catch (InterruptedException e) {
                  ConsoleWindow.displayError(e, this);
                }
              }
            }

          }
        });

    sessionOverviewThread.setDaemon(false);
    sessionOverviewThread.setName("Database Overview");
    sessionOverviewThread.start();
  }

  /**
   * Signal iterating to either start or stop.
   */
  private void startStopTB_actionPerformed() {
    if (startStopTB.isSelected()) {
      // set numIterations to 2 - meaning keep iterating until told to stop 
      numIterations = 2;

      // start iterating 
      iterate();
      startStopTB.setText("Stop");
    }
    else {
      // set numIterations to 0 - meaning stop after current iteration 
      numIterations = 0;
      startStopTB.setText("Start");
    }
  }

  /**
   * Perform a single iteration.
   */
  private void iterateOnceB_actionPerformed() {
    // set numIterations to 1 to ensure a single iteration is performanced 
    numIterations = 1;

    // iterate 
    iterate();
  }


  /**
   * Show wait events
   */
  private void waitEvents() {
    String cursorId = "waitEvents.sql";
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      cursorId = "waitEvents10.sql";
    }
    else {
      cursorId = "waitEvents.sql";
    }
        
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "s";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();


      if (waitEventsOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(waitEventsSP);
        waitEventsDisplayed = false;
      }

      if (waitEventsDisplayed == false) {
        waitEventsCTM = new ConsoleTableModel(resultHeadings, resultSet);
        waitEventsCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, waitEventsSP, waitEventsCTM, waitEventsCCM, statusBarL);
        overviewP.add(waitEventsSP);
        waitEventsDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, waitEventsCTM, statusBarL);
      }

      setPaneSize(waitEventsSP);

      waitEventsSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML.saveSingleResult(myResult);

      waitEventsOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Set the height of a scrollPane so that they are all proportional to the 
   * number of rows displayed
   * 
   * @param sP 
   */
  private void setPaneSize(JScrollPane sP) {
    if (sP.getViewport().getComponentCount() > 0) {
      JTable myTab = (JTable)sP.getViewport().getComponent(0);
      int height = myTab.getRowHeight();
      int numRows = myTab.getRowCount();
      sP.setPreferredSize(new Dimension(500, height * numRows + (height * 4)));
    }
  }


  /**
   * Show free space in the shared pool, large pool & java pool
   */
  private void poolFreeSpace() {
    String cursorId = "poolFreeSpace.sql";
    
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "none";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "beforeOrderBy";
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);


      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (poolFreeSpaceOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(poolFreeSpaceSP);
        poolFreeSpaceDisplayed = false;
      }

      if (poolFreeSpaceDisplayed == false) {
        poolFreeSpaceCTM = new ConsoleTableModel(resultHeadings, resultSet);
        poolFreeSpaceCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, poolFreeSpaceSP, poolFreeSpaceCTM, poolFreeSpaceCCM, statusBarL);
        overviewP.add(poolFreeSpaceSP);
        poolFreeSpaceDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, poolFreeSpaceCTM, statusBarL);
      }

      setPaneSize(poolFreeSpaceSP);

      poolFreeSpaceSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML.saveSingleResult(myResult);

      poolFreeSpaceOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Query v$session_longops
   */
  private void longOperations() {
    String cursorId = "";
    if (ConsoleWindow.getDBVersion() < 8.1) {
      cursorId = "longOpsPre8i.sql";
    }
    else {
      if (ConsoleWindow.getDBVersion() < 9) {
        cursorId = "longOps.sql";
      }
      else {
        if (ConsoleWindow.getDBVersion() < 10) {
          cursorId = "longOps9.sql";
        }
        else {
          cursorId = "longOps10.sql";
        }
      }
    }
    
    try {
      QueryResult myResult;
      
      if (ConsoleWindow.getDBVersion() >= 9) {       
        Cursor myCursor = new Cursor(cursorId,true);
        Parameters myPars = new Parameters();
        Boolean flip = false;
        boolean eggTimer = true;
        ResultCache resultCache = null;
        boolean restrictRows = true;
        String filterByRACAlias = "l";
        String filterByUserAlias = "s";
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        Boolean filterByRAC = true;
        Boolean filterByUser = true;
        myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      else {      
        Cursor myCursor = new Cursor(cursorId,true);
        Parameters myPars = new Parameters();
        Boolean flip = false;
        boolean eggTimer = true;
        ResultCache resultCache = null;
        boolean restrictRows = true;
        String filterByRACAlias = "l";
        String filterByUserAlias = "none";
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
     

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (longOperationsOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(longOperationsSP);
        longOperationsDisplayed = false;
      }

      if (longOperationsDisplayed == false) {
        longOperationsCTM = new ConsoleTableModel(resultHeadings, resultSet);
        longOperationsCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, longOperationsSP, longOperationsCTM, longOperationsCCM, statusBarL);
        overviewP.add(longOperationsSP);
        longOperationsDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, longOperationsCTM, statusBarL);
      }

      setPaneSize(longOperationsSP);

      longOperationsSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML.saveSingleResult(myResult);

      longOperationsOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }
  
  
  /**
   * SQL Monitor
   */
  private void sqlMonitor() {
    String cursorId;    
    
    if (ConsoleWindow.getDBVersion() <= 11.2) {
      cursorId = "sqlMonitor112.sql";
    }
    else {
      cursorId = "sqlMonitor11.sql";
    }
       
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "s2";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (sqlMonitorOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(sqlMonitorSP);
        sqlMonitorDisplayed = false;
      }

      if (sqlMonitorDisplayed == false) {
        sqlMonitorCTM = new ConsoleTableModel(resultHeadings, resultSet);
        sqlMonitorCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, sqlMonitorSP, sqlMonitorCTM, sqlMonitorCCM, statusBarL);
        overviewP.add(sqlMonitorSP);
        sqlMonitorDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, sqlMonitorCTM, statusBarL);
      }

      setPaneSize(sqlMonitorSP);

      sqlMonitorSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML.saveSingleResult(myResult);

      longOperationsOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);             
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show current undo usage by session
   */
  private void undo() {
    try {
      Cursor myCursor = new Cursor("undo4.sql",true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "s";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (undoOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(undoSP);
        undoDisplayed = false;
      }

      if (undoDisplayed == false) {
        undoCTM = new ConsoleTableModel(resultHeadings, resultSet);
        undoCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, undoSP, undoCTM, undoCCM, statusBarL);
        overviewP.add(undoSP);
        undoDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, undoCTM, statusBarL);

      }

      setPaneSize(undoSP);

      undoSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML.saveSingleResult(myResult);

      undoOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show current temporary space usage
   */
  private void sort() {
    try {
      Cursor myCursor = new Cursor("databaseSort2.sql",true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "s";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (sortOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(sortSP);
        sortDisplayed = false;
      }

      if (sortDisplayed == false) {
        sortCTM = new ConsoleTableModel(resultHeadings, resultSet);
        sortCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, sortSP, sortCTM, sortCCM, statusBarL);
        overviewP.add(sortSP);
        overviewP.updateUI();
        sortDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, sortCTM, statusBarL);
      }

      setPaneSize(sortSP);

      sortSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      sortOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show jobs that are currently running
   */
  private void runningJobs() {
    try {
      Cursor myCursor = new Cursor("jobsRunning.sql",true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (runningJobsDisplayed == false) {
        runningJobsCTM = new ConsoleTableModel(resultHeadings, resultSet);
        runningJobsCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, runningJobsSP, runningJobsCTM, runningJobsCCM, statusBarL);
        overviewP.add(runningJobsSP);
        overviewP.updateUI();
        runningJobsDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, runningJobsCTM, statusBarL);
      }

      setPaneSize(runningJobsSP);

      //  spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show sessions waiting on a lock (request > 0 in v$lock)
   */
  private void lockWaiters() {
    String cursorId;
    /*
     * 10g Suffers from Bug 5481650, consequently I'm using the 10 version of 
     * this sql prior to Oracle 11
     * 
     * I know that on rac the inst_id column is output twice but it is important 
     * to ensure it is output even when only 1 instance is selected on rac
     */
    String filterByRACAlias = "none";
    String filterByUserAlias = "none";
    if (ConsoleWindow.getDBVersion() < 11) {
      cursorId = "lockWaiters.sql";
      filterByRACAlias = "l";
      filterByUserAlias = "l";
    }
    else {
      cursorId = "lockWaiters10.sql";
      filterByRACAlias = "i";
      filterByUserAlias = "w";
    }

    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (lockWaitersOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(lockWaitersSP);
        lockWaitersDisplayed = false;
      }

      if (lockWaitersDisplayed == false) {
        lockWaitersCTM = new ConsoleTableModel(resultHeadings, resultSet);
        lockWaitersCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, lockWaitersSP, lockWaitersCTM, lockWaitersCCM, statusBarL);
        overviewP.add(lockWaitersSP);
        lockWaitersDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, lockWaitersCTM, statusBarL);
      }

      setPaneSize(lockWaitersSP);

      lockWaitersSP.getViewport().updateUI();
      overviewP.updateUI();

      // spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      lockWaitersOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show sessions blocking others (block > 0 in v$lock)
   */
  private void lockBlockers() {
    String cursorId;
    /*
     * 10g Suffers from Bug 5481650, consequently I'm using the 10 version of 
     * this sql prior to Oracle 11
     * 
     * I know that on rac the inst_id column is output twice but it is important 
     * to ensure it is output even when only 1 instance is selected on rac
     */
    String filterByRACAlias = "none";
    String filterByUserAlias = "none";
    if (ConsoleWindow.getDBVersion() < 11) {
      cursorId = "lockBlockers.sql";
      filterByRACAlias = "i";
      filterByUserAlias = "l";
    }
    else {
      cursorId = "lockBlockers10.sql";
      filterByRACAlias = "i";
      filterByUserAlias = "s";
    }

    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = false;
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

     
      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (lockBlockersOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(lockBlockersSP);
        lockBlockersDisplayed = false;
      }

      if (lockBlockersDisplayed == false) {
        lockBlockersCTM = new ConsoleTableModel(resultHeadings, resultSet);
        lockBlockersCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, lockBlockersSP, lockBlockersCTM, lockBlockersCCM, statusBarL);
        overviewP.add(lockBlockersSP);
        overviewP.updateUI();
        lockBlockersDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, lockBlockersCTM, statusBarL);
      }

      setPaneSize(lockBlockersSP);

      lockBlockersSP.getViewport().updateUI();
      overviewP.updateUI();

      // spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      lockBlockersOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show a Summary Parallel Queries currently running
   */
  private void parallelQuery() {
    String cursorId = "parallelQuerySummary.sql";
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "sess";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (parallelQueryOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(parallelQuerySP);
        parallelQueryDisplayed = false;
      }

      if (parallelQueryDisplayed == false) {
        parallelQueryCTM = new ConsoleTableModel(resultHeadings, resultSet);
        parallelQueryCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, parallelQuerySP, parallelQueryCTM, parallelQueryCCM, statusBarL);
        overviewP.add(parallelQuerySP);
        parallelQueryDisplayed = true;
      }
      else {
        ExecuteDisplay.updateDisplayedTable(myResult, parallelQueryCTM, statusBarL);
      }

      setPaneSize(parallelQuerySP);

      parallelQuerySP.getViewport().updateUI();
      overviewP.updateUI();

      // spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      parallelQueryOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Show dispatchers and how busy they are in percentage terms.
   */
  private void dispatchers() {
    String cursorId = "dispatchers2.sql";
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "d";
      String filterByUserAlias = "none";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();
      String[][] tmpResultSet = myResult.getResultSetAsStringArray();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (dispatchersOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(dispatchersSP);
        dispatchersDisplayed = false;
      }

      if (dispatchersDisplayed == false) {
        // store away this result set 
        dispatcherValues = new String[3][myResult.getNumRows()];
        for (int i = 0; i < myResult.getNumRows(); i++) {
          if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
            dispatcherValues[0][i] = tmpResultSet[i][0]; // name
            dispatcherValues[1][i] = tmpResultSet[i][4]; // protocol
            dispatcherValues[2][i] = tmpResultSet[i][5]; // busy
          }
          else {
            dispatcherValues[0][i] = tmpResultSet[i][1]; // name
            dispatcherValues[1][i] = tmpResultSet[i][5]; // protocol
            dispatcherValues[2][i] = tmpResultSet[i][6]; // busy             
          }
        }

        dispatchersCTM = new ConsoleTableModel(resultHeadings, resultSet);
        dispatchersCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, dispatchersSP, dispatchersCTM, dispatchersCCM, statusBarL);
        overviewP.add(dispatchersSP);
        dispatchersDisplayed = true;
      }
      else {
        // update Stored Values 
        for (int i = 0; i < myResult.getNumRows(); i++) {
          if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
            dispatchersIdleTime = 
                Integer.valueOf(tmpResultSet[i][4]).intValue() - Integer.valueOf(dispatcherValues[1][i]).intValue(); // idle
            dispatchersBusyTime = 
                Integer.valueOf(tmpResultSet[i][5]).intValue() - Integer.valueOf(dispatcherValues[2][i]).intValue(); // busy
            dispatcherValues[1][i] = tmpResultSet[i][4]; // idle
            dispatcherValues[2][i] = tmpResultSet[i][5]; // busy
          }
          else {
            dispatchersIdleTime = 
                Integer.valueOf(tmpResultSet[i][5]).intValue() - Integer.valueOf(dispatcherValues[1][i]).intValue(); // idle
            dispatchersBusyTime = 
                Integer.valueOf(tmpResultSet[i][6]).intValue() - Integer.valueOf(dispatcherValues[2][i]).intValue(); // busy
            dispatcherValues[1][i] = tmpResultSet[i][5]; // idle
            dispatcherValues[2][i] = tmpResultSet[i][6]; // busy             
          }
        }

        QueryResult newResult = new QueryResult();
        newResult.setNumCols(myResult.getNumCols());
        newResult.setCursorId(myResult.getCursorId());
        newResult.setExecutionClockTime(myResult.getExecutionClockTime());
        newResult.setExecutionTime(myResult.getExecutionTime());
        newResult.setColumnWidths(myResult.getColumnWidths());
        newResult.setNumRows(myResult.getNumRows());
        for (int i = 0; i < myResult.getNumRows(); i++) {
          if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
            Vector row = new Vector(6);
            row.add(tmpResultSet[i][0]);
            row.add(tmpResultSet[i][1]);
            row.add(tmpResultSet[i][2]);
            row.add(tmpResultSet[i][3]);
            row.add(String.valueOf(dispatchersIdleTime));
            row.add(String.valueOf(dispatchersBusyTime));
            newResult.addResultRow(row);
          }
          else {
            Vector row = new Vector(7);
            if (isInListOfSelectedInstances(tmpResultSet[i][0])) {
              row.add(tmpResultSet[i][0]);
              row.add(tmpResultSet[i][1]);
              row.add(tmpResultSet[i][2]);
              row.add(tmpResultSet[i][3]);
              row.add(tmpResultSet[i][4]);
              row.add(String.valueOf(dispatchersIdleTime));
              row.add(String.valueOf(dispatchersBusyTime));
              newResult.addResultRow(row);
            }
          }
        }

        ExecuteDisplay.updateDisplayedTable(newResult, dispatchersCTM, statusBarL);
      }

      setPaneSize(dispatchersSP);

      dispatchersSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      dispatchersOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this, "Dispatchers()");
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  private boolean isInListOfSelectedInstances(String instanceName) {
    String[][] selectedInstances = ConsoleWindow.getSelectedInstances();

    for (int i = 0; i < selectedInstances.length; i++) {
      if (selectedInstances[i][1].equals(instanceName) && selectedInstances[i][2].equals("selected"))
        return true;
    }

    return false;
  }

  /**
   * Show shared servers and how busy they are in percentage terms
   */
  private void sharedServers() {
    String cursorId = "sharedServers2.sql";
    try {
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      boolean eggTimer = true;
      ResultCache resultCache = null;
      boolean restrictRows = true;
      String filterByRACAlias = "s";
      String filterByUserAlias = "none";
      Boolean includeDecode = true;
      String includeRACPredicatePoint = "default";
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      // get the resultSet from the query 
      Vector resultSet = myResult.getResultSetAsVectorOfVectors();
      String[][] tmpResultSet = myResult.getResultSetAsStringArray();

      // get the resultSet column headings 
      String[] resultHeadings = myResult.getResultHeadings();

      if (sharedServersOnlyLocalInstance != ConsoleWindow.isOnlyLocalInstanceSelected()) {
        overviewP.remove(sharedServersSP);
        sharedServersDisplayed = false;
      }

      if (sharedServersDisplayed == false) {
        // store away this result set 
        sharedServerValues = new String[myResult.getNumRows()][myResult.getNumCols()];
        System.arraycopy(tmpResultSet,0,sharedServerValues,0,tmpResultSet.length);
        
/*        for (int i = 0; i < myResult.getNumRows(); i++) {
          if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
            sharedServerValues[0][0] = tmpResultSet[i][0]; // name 
            sharedServerValues[1][1] = tmpResultSet[i][2]; // requests 
            sharedServerValues[2][2] = tmpResultSet[i][3]; // busy 
            sharedServerValues[3][3] = tmpResultSet[i][4]; // idle 
          }
          else {
            sharedServerValues[0][i] = tmpResultSet[i][1]; // name 
            sharedServerValues[1][i] = tmpResultSet[i][3]; // requests 
            sharedServerValues[2][i] = tmpResultSet[i][4]; // busy 
            sharedServerValues[3][i] = tmpResultSet[i][5]; // idle              
          }
        }*/

        sharedServersCTM = new ConsoleTableModel(resultHeadings, resultSet);
        sharedServersCCM = new ConsoleColumnModel();
        ExecuteDisplay.displayTable(myResult, sharedServersSP, sharedServersCTM, sharedServersCCM, statusBarL);
        overviewP.add(sharedServersSP);
        sharedServersDisplayed = true;
        numSharedServers = myResult.getNumRows();
      }
      else {
        // update Stored Values 
        QueryResult newResult = new QueryResult();
        newResult.setNumCols(myResult.getNumCols());;
        newResult.setColumnWidths(myResult.getColumnWidths());
        newResult.setNumRows(myResult.getNumRows());
        newResult.setResultHeadings(myResult.getResultHeadings());
        newResult.setExecutionClockTime(myResult.getExecutionClockTime());
        newResult.setExecutionTime(myResult.getExecutionTime());

        for (int i = 0; i < myResult.getNumRows(); i++) {
          boolean foundMatch = false;
          for (int j = 0; j < numSharedServers; j++) {
            
            if ((ConsoleWindow.isOnlyLocalInstanceSelected() && sharedServerValues[j][0].equals(tmpResultSet[i][0])) || 
                (!ConsoleWindow.isOnlyLocalInstanceSelected() && sharedServerValues[j][1].equals(tmpResultSet[i][1]) && sharedServerValues[j][0].equals(tmpResultSet[i][0]))) {
              
              foundMatch = true;
              
              if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
                sharedServerRequests = Integer.valueOf(tmpResultSet[i][2]).intValue() - Integer.valueOf(sharedServerValues[i][2]).intValue(); // requests
                sharedServerBusyTime = Integer.valueOf(tmpResultSet[i][3]).intValue() - Integer.valueOf(sharedServerValues[i][3]).intValue(); // busy
                sharedServerIdleTime = Integer.valueOf(tmpResultSet[i][4]).intValue() - Integer.valueOf(sharedServerValues[i][4]).intValue(); // idle
              }
              else {
                sharedServerRequests = Integer.valueOf(tmpResultSet[i][3]).intValue() - Integer.valueOf(sharedServerValues[i][3]).intValue(); // requests
                sharedServerBusyTime = Integer.valueOf(tmpResultSet[i][4]).intValue() - Integer.valueOf(sharedServerValues[i][4]).intValue(); // busy
                sharedServerIdleTime = Integer.valueOf(tmpResultSet[i][5]).intValue() - Integer.valueOf(sharedServerValues[i][5]).intValue(); // idle             
              }
  
              if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
                Vector row = new Vector(6);
                row.add(tmpResultSet[i][0]);
                row.add(tmpResultSet[i][1]);
                row.add(String.valueOf(sharedServerRequests));
                row.add(String.valueOf(sharedServerBusyTime));
                row.add(String.valueOf(sharedServerIdleTime));
                row.add(tmpResultSet[i][5]);
                newResult.addResultRow(row);
              }
              else {
                Vector row = new Vector(7);
                if (isInListOfSelectedInstances(tmpResultSet[i][0])) {
                  row.add(tmpResultSet[i][0]);
                  row.add(tmpResultSet[i][1]);
                  row.add(tmpResultSet[i][2]);
                  row.add(String.valueOf(sharedServerRequests));
                  row.add(String.valueOf(sharedServerBusyTime));
                  row.add(String.valueOf(sharedServerIdleTime));
                  row.add(tmpResultSet[i][6]);
                  newResult.addResultRow(row);
                }
              }
            }
          }
          
          if (foundMatch == false) {
            if (ConsoleWindow.isOnlyLocalInstanceSelected()) {
              Vector row = new Vector(6);
              row.add(tmpResultSet[i][0]);
              row.add(tmpResultSet[i][1]);
              row.add(tmpResultSet[i][2]);
              row.add(tmpResultSet[i][3]);
              row.add(tmpResultSet[i][4]);
              row.add(tmpResultSet[i][5]);
              newResult.addResultRow(row);
            }
            else {
              Vector row = new Vector(7);
              if (isInListOfSelectedInstances(tmpResultSet[i][0])) {
                row.add(tmpResultSet[i][0]);
                row.add(tmpResultSet[i][1]);
                row.add(tmpResultSet[i][2]);
                row.add(tmpResultSet[i][3]);
                row.add(tmpResultSet[i][4]);
                row.add(tmpResultSet[i][5]);
                row.add(tmpResultSet[i][6]);
                newResult.addResultRow(row);
              }
            }
            

          }
        }
        numSharedServers = myResult.getNumRows();
        
        // store away this result set 
        sharedServerValues = new String[myResult.getNumRows()][myResult.getNumCols()];
        System.arraycopy(tmpResultSet,0,sharedServerValues,0,tmpResultSet.length);
        
        ExecuteDisplay.updateDisplayedTable(newResult, sharedServersCTM, statusBarL);
      }

      setPaneSize(sharedServersSP);

      sharedServersSP.getViewport().updateUI();
      overviewP.updateUI();

      //  spool output 
      if (currentlySpooling)
        outputHTML. saveSingleResult(myResult);

      sharedServersOnlyLocalInstance = ConsoleWindow.isOnlyLocalInstanceSelected();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }

  /**
   * Prompt for a file/directory to spool output too.  Create an OutputHTML 
   * object to write out the output.
   */
  private void fileSave() {
    try {
      // construct the default file name using the instance name + current date and time 
      StringBuffer currentDT = new StringBuffer();
      try {
        currentDT = new StringBuffer(ConnectWindow.getDatabase().getSYSDate());
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
      }
      // construct a default file name 
      String defaultFileName;
      if (ConnectWindow.isLinux()) {
        defaultFileName = 
            ConnectWindow.getBaseDir() + "/Output/RichMon " + ConsoleWindow.getInstanceName() + " Overview " + 
            currentDT.toString() + ".html";
      }
      else {
        defaultFileName = 
            ConnectWindow.getBaseDir() + "\\Output\\RichMon " + ConsoleWindow.getInstanceName() + " Overview " + 
            currentDT.toString() + ".html";
      }
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(new File(defaultFileName));

      // prompt the user to choose a file name 
      int option = fileChooser.showSaveDialog(this);
      if (option == JFileChooser.APPROVE_OPTION) {
        saveFile = fileChooser.getSelectedFile();

        // force the user to use a new file name if the specified filename already exists 
        while (saveFile.exists()) {
          JOptionPane.showConfirmDialog(this, "File already exists", "File Already Exists", JOptionPane.ERROR_MESSAGE);
          option = fileChooser.showOpenDialog(this);
          if (option == JFileChooser.APPROVE_OPTION) {
            saveFile = fileChooser.getSelectedFile();
          }
        }
        save = new BufferedWriter(new FileWriter(saveFile));
        saveFile.createNewFile();

        // create a process to format the output and write the file 
        try {
          outputHTML = new OutputHTML(saveFile, save, "Overview");
          // start spooling 
          currentlySpooling = true;
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e, this);
        }
      }
    }
    catch (IOException e) {
      ConsoleWindow.displayError(e, this);
    }
  }

  /**
   * Check the status of the spoolTB JToggleButton and start or stop spooling accordingly.
   */
  private void spoolTB_actionPerformed() {
    if (spoolTB.isSelected()) {
      fileSave();
    }
    else {
      fileClose();
    }
  }

  /**
   * Stop spooling output and close the output file.
   */
  public void fileClose() {
    try {
      currentlySpooling = false;
      if (save instanceof BufferedWriter)
        save.close();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
    }
  }


  public static int getIterationSleepS() {
    int s = Integer.valueOf(String.valueOf(iterationSleepS.getValue())).intValue();
    return s;
  }


  private String getTodaysDate() {
    Date today;
    String dateOut;
    DateFormat dateFormatter;

    dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);
    today = new Date();
    dateOut = dateFormatter.format(today);
  
    return dateOut;
  }
  
  private String getTime() {
    Date today;         
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String tmpTime;
    
    today = new Date();
    tmpTime = dateFormatter.format(today);
    
    //remove the coluns from tmpTime
    StringBuffer newTime = new StringBuffer(6);
    for (int i=0; i < tmpTime.length(); i++) {
      if (tmpTime.charAt(i) != ':') newTime.append(tmpTime.charAt(i));
    }
    
    return newTime.toString();
  }

}