 /*
  * ChartViewPanel.java        15.01 10/04/07
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
  * 14/05/07 Richard Wright Removed parameters from method calls where they are no longer used
  * 23/11/07 Richard Wright Enhanced for RAC
  * 24/09/09 Richard Wright Modified the condition causing the chart to have the instance name displayed (bug fix)
  * 24/09/09 Richard Wright Fixed the null pointer exception when reducing the number of charts displayed and connected to the rac instance
  * 23/10/09 Richard Wright Modified to allow recording of charts for later playback.
  * 14/10/10 Richard Wright Added OPQ Chart
  * 16/12/10 Richard Wright Modified OSLOADto work on 10.1 as well and 10.2 and above
  * 16/12/10 Richard Wright Added OPQ Downgrades Chart
  * 23/12/10 Richard Wright Modified so that correct (older) query is run against 10.1 for the physical io chart
  * 28/07/11 Richard Wright Changed name to ChartViewPanel and changed iterate buttons to start and stop
  * 22/09/11 Richard Wright Remove the 'start recording' button
  * 06/03/12 Richard Wright Removed the tablespace IO / File IO / Sort and UNDO charts by commenting them out of jbinit()
  *                         These 4 charts off little value and nobody uses them
  * 19/03/12 Richard Wright Uses an updated sql for 10g databases to better eliminate idle events
  * 20/03/12 Richard Wright Modified setOffSet to include the asmIOStat chart
  * 18/05/12 Richard Wright Modified to add the IOPS chart
  * 08/04/14 Richard Wright Removed all all functionality that breaks charts out into seperate windows
  * 08/04/14 Richard Wright Modified to allow all checkboxes to appear on a small screen even after removing the breakout checkbox
  */

package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DateFormat;

import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;

import org.jfree.chart.ChartPanel;


/**
 * A panel which iterates at user defined intervals to display selected results
 * which give an overview of current database performance.
 */
public class ChartViewPanel extends RichPanel {
  // panels 
  public JPanel optionsRP = new JPanel();
  private JPanel allButtonsP = new JPanel();
  private JPanel controlButtonP = new JPanel();
  private JPanel controlButton2P = new JPanel();
  
  // chart panel
  private JPanel chartP = new JPanel();
  GridLayout chartPLayout = new GridLayout(1,1);

  // controlButton2P panel buttons 
  private JToggleButton startStopTB = new JToggleButton("Start", false);
  private JLabel iterationsL = new JLabel("not iterating");
  private SpinnerNumberModel snm = new SpinnerNumberModel(5, 1, 600, 1);
  private static JSpinner iterationSleepS;
//  private static JCheckBox breakOutCB = new JCheckBox("Break Out",false);
//  private JToggleButton recordTB = new JToggleButton("Start Recording", false);

  // checkboxes for optionsP panel 
  private JCheckBox waitEventsSummaryCB = new JCheckBox("Wait Events",true);
  private JCheckBox waitClassCB = new JCheckBox("Wait Class");
  private JCheckBox sharedPoolCB = new JCheckBox("Shared Pool");
  public JCheckBox osLoadCB = new JCheckBox("OS Load");
  private JCheckBox callsChartCB = new JCheckBox("Calls");
  private JCheckBox parseChartCB = new JCheckBox("Parse");
  private JCheckBox physicalIOChartCB = new JCheckBox("Physical IO");
  private JCheckBox iopsChartCB = new JCheckBox("IOPS");
  private JCheckBox tsIOChartCB = new JCheckBox("Tablespace IO");
  private JCheckBox fileIOChartCB = new JCheckBox("File IO");
  private JCheckBox undoChartCB = new JCheckBox("Undo");
  private JCheckBox sortChartCB = new JCheckBox("Sort");
  private JCheckBox opqChartCB = new JCheckBox("OPQ Processes");
  private JCheckBox opqDowngradesChartCB = new JCheckBox("OPQ Downgrades");
  private JCheckBox asmIOStatChartCB = new JCheckBox("ASM IOSTAT");
  private JCheckBox pgaChartCB = new JCheckBox("PGA");

  // Chart Panels
  ChartPanel[] waitEventsCP;
  ChartPanel[] waitClassCP;
  JPanel[] sharedPoolCP;
  ChartPanel[] physicalIOCP;
  ChartPanel[] iopsCP;
  ChartPanel[] parseCP;
  ChartPanel[] osLoadCP;
  ChartPanel[] callsCP;
  ChartPanel[] undoCP;
  ChartPanel[] sortCP;
  ChartPanel[] tsIOCP;
  ChartPanel[] fileIOCP;
  ChartPanel[] opqCP;
  ChartPanel[] opqDowngradesCP;
  ChartPanel[] asmIOStatCP;
  ChartPanel[] pgaCP;

  // is a component already displayed 
  boolean waitEventsSummaryDisplayed = false;
  boolean waitClassSummaryDisplayed = false;
  boolean sharedPoolChartDisplayed = false;
  boolean osLoadChartDisplayed = false;
  boolean callsChartDisplayed = false;
  boolean parseChartDisplayed = false;
  boolean physicalIOChartDisplayed = false;
  boolean iopsChartDisplayed = false;
  boolean tsIOChartDisplayed = false;
  boolean fileIOChartDisplayed = false;
  boolean undoChartDisplayed = false;
  boolean sortChartDisplayed = false;
  boolean opqChartDisplayed = false;
  boolean opqDowngradesChartDisplayed = false;
  boolean asmIOStatChartDisplayed = false;
  boolean pgaChartDisplayed = false;
  
  boolean showSQL = false;

  // statusBar 
  private JLabel statusBarL = new JLabel();

  // pointers to other objects 

  // misc 
  private Dimension buttonSize = new Dimension(110, 25);
  private Dimension buttonSize2 = new Dimension(75, 25);
  private int numCharts = 0;

  // iteration stuff 
  private int numIterations =  0; // 0=don't iterate, 1=iterate once, 2=keep iterating 
  private int iterationCounter =  0; // how many times have we iterated since last told to start 
  private int totalIterations = 0; // how many times have we iterated - ever! 

  // Chart Stuff 
  private WaitEventsChart[] waitEventsChart; 
  private WaitClassChart[] waitClassChart; 
  private PhysicalIOChart[] physicalIOChart; 
  private IOPSChart[] iopsChart; 
  private SharedPoolChart[] sharedPoolChart; 
  private CallsChart[] callsChart;
  private ParseChart[] parseChart;
  private TablespaceIOChart[] tablespaceIOChart;
  private FileIOChart[] fileIOChart;
  private UndoChart[] undoChart;
  private SortChart[] sortChart; 
  private OsLoadChart[] osLoadChart;
  private OPQChart[] opqChart;
  private OPQDowngradesChart[] opqDowngradesChart;
  private ASMIOStatChart[] asmIOStatChart;
  private PGAChart[] pgaChart;
  
  // misc
  String[][] allInstances;
  boolean debug = false;
  boolean record = false;
  String recordExecutionDate;
  String recordFirstIterationStartTime;

  /**
   * Constructor
   */
  public ChartViewPanel() {
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
    // layout 
    this.setLayout(new BorderLayout());

    // controlButton2P Panel 
    controlButton2P.setLayout(new GridBagLayout());
    GridBagConstraints controlCons = new GridBagConstraints();
    controlCons.gridheight = 2;
    controlCons.gridx = 0;
    controlCons.gridy = 0;
//    controlCons.insets = new Insets(3, 3, 3, 3);
    controlCons.insets = new Insets(12, 3, 12, 3);
    controlCons.gridy = 0;
    iterationsL.setFont(new Font("????", 1, 13));
    iterationsL.setForeground(Color.BLACK);
    controlButton2P.add(iterationsL, controlCons);
    controlCons.gridx = 1;
    controlCons.gridy = 0;
    startStopTB.setPreferredSize(buttonSize2);
    startStopTB.setForeground(Color.BLUE);
    startStopTB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            startStopTB_actionPerformed();
          }
        });

    controlButton2P.add(startStopTB, controlCons);
    controlCons.gridx = 1;
    controlCons.gridy = 1;

    controlCons.gridx = 2;
    controlCons.gridy = 0;
    iterationSleepS.setPreferredSize(buttonSize2);
    iterationSleepS.setForeground(Color.BLUE);
    controlButton2P.add(iterationSleepS, controlCons);
    controlCons.gridwidth=1;
    controlCons.gridx=2;
    controlCons.gridy=1;



    // controlButtonP panel 
    controlButtonP.add(controlButton2P, null);
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // statusBar 
    statusBarL.setText(" ");

    // optionsP panel 
    optionsRP.setLayout(new FlowLayout());

    optionsRP.add(waitEventsSummaryCB);
    optionsRP.add(physicalIOChartCB);
    optionsRP.add(sharedPoolCB);
    optionsRP.add(callsChartCB);
    optionsRP.add(parseChartCB);
//    optionsRP.add(tsIOChartCB);
//    optionsRP.add(fileIOChartCB);
//    optionsRP.add(undoChartCB);
//    optionsRP.add(sortChartCB);
    optionsRP.add(opqChartCB);
    optionsRP.add(opqDowngradesChartCB);
    optionsRP.add(pgaChartCB);
    //optionsRP.add(asmIOStatChartCB);
    optionsRP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // allButtonsP panel 
    allButtonsP.setLayout(new BorderLayout());
    allButtonsP.add(controlButtonP, BorderLayout.WEST);
    allButtonsP.add(optionsRP, BorderLayout.CENTER);

    // construct sessionPanel 
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(chartP, BorderLayout.CENTER);
    this.add(allButtonsP, BorderLayout.NORTH);

    // chartP panel 
    chartP.setLayout(new GridLayout(1,2));
    chartP.setBackground(Color.WHITE);

//    if (Properties.isPlaceOverviewChartsIntoASingleFrame()) breakOutCB.setSelected(true);
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      optionsRP.add(osLoadCB); 
      optionsRP.add(waitClassCB);
    }
    
    if (ConsoleWindow.getDBVersion() >= 10.2) optionsRP.add(iopsChartCB,2);
    
    if (ConsoleWindow.getDBVersion() >= 11.2) optionsRP.add(asmIOStatChartCB); 


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
                // obtain a list of instances to chart against this iteration
                allInstances = ConsoleWindow.getAllInstances();

                if (debug) System.out.println("NumCharts: " + numCharts);

                if (waitEventsSummaryCB.isSelected()) {
                  waitEventsChart();
//                  if (!isBreakOut()) setOffSet("waitEvents");
                }
                else {
                  if (waitEventsSummaryDisplayed) {
                    waitEventsSummaryDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < waitEventsCP.length; i++)  {
                      removeChart(waitEventsCP[i]);
                      waitEventsCP[i] = null;
                      waitEventsChart[i] = null;
                    }
                  }
                }                
                

                if (physicalIOChartCB.isSelected()) {
                  physicalIOChart();
//                  if (!isBreakOut()) setOffSet("physicalIO");
                }
                else {
                  if (physicalIOChartDisplayed) {
                    physicalIOChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < physicalIOCP.length; i++)  {
                      removeChart(physicalIOCP[i]);
                      physicalIOCP[i] = null;
                      physicalIOChart[i] = null;
                    }
                  }
                } 
                

                if (iopsChartCB.isSelected()) {
                  iopsChart();
//                  if (!isBreakOut()) setOffSet("IOPS");
                }
                else {
                  if (iopsChartDisplayed) {
                    iopsChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < iopsCP.length; i++)  {
                      removeChart(iopsCP[i]);
                      iopsCP[i] = null;
                      iopsChart[i] = null;
                    }
                  }
                }                
                
                if (opqChartCB.isSelected()) {
                  opqChart();
//                  if (!isBreakOut()) setOffSet("opq");
                }
                else {
                  if (opqChartDisplayed) {
                    opqChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < opqCP.length; i++)  {
                      removeChart(opqCP[i]);
                      opqCP[i] = null;
                      opqChart[i] = null;
                    }
                  }
                }                
                
                if (opqDowngradesChartCB.isSelected()) {
                  opqDowngradesChart();
//                  if (!isBreakOut()) setOffSet("opq downgrades");
                }
                else {
                  if (opqDowngradesChartDisplayed) {
                    opqDowngradesChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < opqDowngradesCP.length; i++)  {
                      removeChart(opqDowngradesCP[i]);
                      opqDowngradesCP[i] = null;
                      opqDowngradesChart[i] = null;
                    }
                  }
                }

                if (osLoadCB.isSelected()) {
                  osLoadChart();
//                  if (!isBreakOut()) setOffSet("osLoad");
                }
                else {
                  if (osLoadChartDisplayed) {
                    osLoadChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < osLoadCP.length; i++)  {
                      removeChart(osLoadCP[i]);
                      osLoadCP[i] = null;
                      osLoadChart[i] = null;
                    }
                  }
                }

                if (sharedPoolCB.isSelected()) {
                  sharedPoolChart();
//                  if (!isBreakOut()) setOffSet("sharedPool");
                }
                else {
                  if (sharedPoolChartDisplayed) {
                    sharedPoolChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < sharedPoolCP.length; i++)  {
                      removeJPanelChart(sharedPoolCP[i]);
                      sharedPoolCP[i] = null;
                      sharedPoolChart[i] = null;
                    }
                  }
                }

                if (callsChartCB.isSelected()) {
                  callsChart();
//                  if (!isBreakOut()) setOffSet("calls");
                }
                else {
                  if (callsChartDisplayed) {
                    callsChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  callsCP.length; i++)  {
                      if (debug) System.out.println("length of callCP: " + callsCP.length);
                      removeChart(callsCP[i]);
                      callsCP[i] = null;
                      callsChart[i] = null;
                    }
                  }
                }

                if (parseChartCB.isSelected()) {
                  parseChart();
//                  if (!isBreakOut()) setOffSet("parse");
                }
                else {
                  if (parseChartDisplayed) {
                    parseChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  parseCP.length; i++)  {
                      removeChart(parseCP[i]);
                      parseCP[i] = null;
                      parseChart[i] = null;
                    }
                  }
                }                        
                
                if (undoChartCB.isSelected()) {
                  undoChart();
//                  if (!isBreakOut()) setOffSet("undo");
                }
                else {
                  if (undoChartDisplayed) {
                    undoChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  undoCP.length; i++)  {
                      removeChart(undoCP[i]);
                      undoCP[i] = null;
                      undoChart[i] = null;
                    }
                  }
                }                          
                
                if (sortChartCB.isSelected()) {
                  sortChart();
//                  if (!isBreakOut()) setOffSet("sort");
                }
                else {
                  if (sortChartDisplayed) {
                    sortChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  sortCP.length; i++)  {
                      removeChart(sortCP[i]);
                      sortCP[i] = null;
                      sortChart[i] = null;
                    }
                  }
                }                
                
                if (tsIOChartCB.isSelected()) {
                  tsIOChart();
                }
                else {
                  if (tsIOChartDisplayed) {
                    tsIOChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  tsIOCP.length; i++)  {
                      removeChart(tsIOCP[i]);
                      tsIOCP[i] = null;
                      tablespaceIOChart[i] = null;
                    }
                  }
                }                
                
                if (fileIOChartCB.isSelected()) {
                  fileIOChart();
                }
                else {
                  if (fileIOChartDisplayed) {
                    fileIOChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  fileIOCP.length; i++)  {
                      removeChart(fileIOCP[i]);
                      fileIOCP[i] = null;
                      fileIOChart[i] = null;
                    }
                  }
                }                
                
                if (asmIOStatChartCB.isSelected()) {
                  asmIOStatChart();
//                  if (!isBreakOut()) setOffSet("asmIOStat");
                }
                else {
                  if (asmIOStatChartDisplayed) {
                    asmIOStatChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  asmIOStatCP.length; i++)  {
                      removeChart(asmIOStatCP[i]);
                      asmIOStatCP[i] = null;
                      asmIOStatChart[i] = null;
                    }
                  }
                }        
                
                if (pgaChartCB.isSelected()) {
                  pgaChart();
//                  if (!isBreakOut()) setOffSet("pga");
                }
                else {
                  if (pgaChartDisplayed) {
                    pgaChartDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i <  pgaCP.length; i++)  {
                      removeChart(pgaCP[i]);
                      pgaCP[i] = null;
                      pgaChart[i] = null;
                    }
                  }
                }

                if (waitClassCB.isSelected()) {
                  waitClassChart();
//                  if (!isBreakOut()) setOffSet("waitClass");
                }
                else {
                  if (waitClassSummaryDisplayed) {
                    waitClassSummaryDisplayed = false;
                    //decrementNumCharts(numberOfSelectedInstances());
                    for (int i = 0; i < waitClassCP.length; i++)  {
                      removeChart(waitClassCP[i]);
                      waitClassCP[i] = null;
                      waitClassChart[i] = null;
                    }
                  }
                }   

              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this);
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


//  private boolean isBreakOut() {
//    return breakOutCB.isSelected();
//  }
  
  /**
   * Create a chart (in a seperate frame) to summarize wait events.
   */
   private void waitEventsChart() {

     if (waitEventsSummaryDisplayed == false) {
       waitEventsChart = new WaitEventsChart[allInstances.length]; // array of charts
       waitEventsCP = new ChartPanel[allInstances.length]; // array of chart panels
     }
     
     try {
       String cursorId;
       
       if (ConsoleWindow.getDBVersion() >= 10) {
         cursorId = "waitEventsSummary2-10.sql";
       }
       else {
         cursorId = "waitEventsSummary2.sql";
       }

       for (int i=0; i < allInstances.length; i++) {
   
         if (!(waitEventsChart[i] instanceof WaitEventsChart )) {    
           // a chart does not already exist for this instance
           if (allInstances[i][2].equals("selected")) {
             Parameters myPars = new Parameters();
             myPars.addParameter("int",allInstances[i][0]);
             
             Cursor myCursor = new Cursor(cursorId,true);
             Boolean flip = false;
             boolean eggTimer = false;
             ResultCache resultCache = null;
             boolean restrictRows = false;
             String filterByRACAlias = "none";
             String filterByUserAlias = "none";
             Boolean includeDecode = false;
             String includeRACPredicatePoint = "default";
             Boolean filterByRAC = false;
             Boolean filterByUser = false;
             QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

             if (Properties.isBreakOutChartsTabsFrame()) {
               if (!(waitEventsChart[i] instanceof WaitEventsChart)) waitEventsChart[i] = new WaitEventsChart();
               waitEventsChart[i].createChart(myResult, true, false, allInstances[i][0],allInstances[i][1]);
             } 
             else {
               if (!(waitEventsChart[i] instanceof WaitEventsChart)) waitEventsChart[i] = new WaitEventsChart();
               waitEventsCP[i] =  waitEventsChart[i].createChartPanel(myResult, true, allInstances[i][0]);
               if (!ConsoleWindow.isOnlyLocalInstanceSelected()) waitEventsChart[i].setChartTitle(allInstances[i][1]);
               addChart(waitEventsCP[i]);
             }
             
   //            if (consoleWindow.menuFileWaitSummarySessions.isSelected()) {
   //              waitEventsChart[i].updateSessionLine(allInstances[i][0]); // why - it was done when chart created !!!!!
   //            }
             
             waitEventsSummaryDisplayed = true;
           }
         }     
         else {
           Parameters myPars = new Parameters();
           myPars.addParameter("int",allInstances[i][0]);
           
           Cursor myCursor = new Cursor(cursorId,true);
           Boolean flip = false;
           boolean eggTimer = false;
           ResultCache resultCache = null;
           boolean restrictRows = false;
           String filterByRACAlias = "none";
           String filterByUserAlias = "none";
           Boolean includeDecode = false;
           String includeRACPredicatePoint = "default";
           Boolean filterByRAC = false;
           Boolean filterByUser = false;
           QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

           // a chart already exists for this instance
           if (allInstances[i][2].equals("selected")) {

             // update the chart because it already exists 
             waitEventsChart[i].updateChart(myResult);
             
             // set the chart title
             if (numberOfSelectedInstances() > 1 && waitEventsChart[i].isChartTitleSet() == false) waitEventsChart[i].setChartTitle(allInstances[i][1]);
             
             // display the number of sessions against a seperate axis 
   //           if (consoleWindow.waitSummarySessions.isSelected()) {
               waitEventsChart[i].updateSessionLine(allInstances[i][0]);
   //           }
           }
           else {
             // this instance if not selected, check to see if it is a chart and if so, remove it
             removeChart(waitEventsCP[i]);
             waitEventsChart[i] = null;
             waitEventsCP[i] = null;
           }
         }
       }
     }
     catch (Exception e) {
       JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
       if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
         numIterations = 0;
         startStopTB.setSelected(false);
         startStopTB.setText("Start");
       }
     }
   }
   
  private void waitClassChart() {

    if (waitClassSummaryDisplayed == false) {
      waitClassChart = new WaitClassChart[allInstances.length]; // array of charts
      waitClassCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "waitClassSummary.sql";

      for (int i=0; i < allInstances.length; i++) {
  
        if (!(waitClassChart[i] instanceof WaitClassChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
             if (!(waitClassChart[i] instanceof WaitClassChart)) waitClassChart[i] = new WaitClassChart();
              waitClassChart[i].createChart(myResult, true, false, allInstances[i][0],allInstances[i][1]);
            } 
            else {
              if (!(waitClassChart[i] instanceof WaitClassChart)) waitClassChart[i] = new WaitClassChart();
              waitClassCP[i] =  waitClassChart[i].createChartPanel(myResult, true, allInstances[i][0]);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) waitClassChart[i].setChartTitle(allInstances[i][1]);
              addChart(waitClassCP[i]);
            }
                        
            waitClassSummaryDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            waitClassChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && waitClassChart[i].isChartTitleSet() == false) waitClassChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(waitClassCP[i]);
            waitClassChart[i] = null;
            waitClassCP[i] = null;
          }
        }
      }
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }
  
  
  private void opqChart() {

    if (opqChartDisplayed == false) {
      opqChart = new OPQChart[allInstances.length]; // array of charts
      opqCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "opq.sql";

      for (int i=0; i < allInstances.length; i++) {
  
        if (!(opqChart[i] instanceof OPQChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(opqChart[i] instanceof OPQChart)) opqChart[i] = new OPQChart();
              opqChart[i].createChart(myResult, true, false, allInstances[i][0],allInstances[i][1]);
            } 
            else {
              if (!(opqChart[i] instanceof OPQChart)) opqChart[i] = new OPQChart();
              opqCP[i] =  opqChart[i].createChartPanel(myResult, true, allInstances[i][0]);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) opqChart[i].setChartTitle(allInstances[i][1]);
              addChart(opqCP[i]);
            }
 
            opqChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            opqChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && opqChart[i].isChartTitleSet() == false) opqChart[i].setChartTitle(allInstances[i][1]);
            
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(opqCP[i]);
            opqChart[i] = null;
            opqCP[i] = null;
          }
        }
      }
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }
  private void opqDowngradesChart() {

    if (opqDowngradesChartDisplayed == false) {
      opqDowngradesChart = new OPQDowngradesChart[allInstances.length]; // array of charts
      opqDowngradesCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "parallelOperationDowngrades.sql";

      for (int i=0; i < allInstances.length; i++) {
  
        if (!(opqDowngradesChart[i] instanceof OPQDowngradesChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(opqDowngradesChart[i] instanceof OPQDowngradesChart)) opqDowngradesChart[i] = new OPQDowngradesChart();
              opqDowngradesChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(opqDowngradesChart[i] instanceof OPQDowngradesChart)) opqDowngradesChart[i] = new OPQDowngradesChart();
              opqDowngradesCP[i] =  opqDowngradesChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) opqDowngradesChart[i].setChartTitle(allInstances[i][1]);
              addChart(opqDowngradesCP[i]);
            }
                        
            opqDowngradesChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            opqDowngradesChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && opqDowngradesChart[i].isChartTitleSet() == false) opqDowngradesChart[i].setChartTitle(allInstances[i][1]);
            
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(opqDowngradesCP[i]);
            opqDowngradesChart[i] = null;
            opqDowngradesCP[i] = null;
          }
        }
      }
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error...", JOptionPane.ERROR_MESSAGE);
      if ((Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("socket write error") || (Properties.isAutomaticStopIteratingOnLostConnection()) && e.toString().endsWith("Closed Connection")) {
        numIterations = 0;
        startStopTB.setSelected(false);
        startStopTB.setText("Start");
      }
    }
  }


  private void removeChart(ChartPanel myChartPanel) {
      if (debug) System.out.println("removeChart()");
      if (myChartPanel instanceof ChartPanel) {
        chartP.remove(myChartPanel);
        decrementNumCharts(1);
        setChartPLayout();
        chartP.updateUI();
      }
  }  
  
  private void removeJPanelChart(JPanel myPanel) {
      if (debug) System.out.println("removeJPanelChart()");
      if (myPanel instanceof JPanel) {
        chartP.remove(myPanel);
        decrementNumCharts(1);
        setChartPLayout();
        chartP.updateUI();
      }
  }
  
  private int numberOfSelectedInstances() {
    int numSelected = 0;
    for (int i = 0; i < allInstances.length; i++)  {
      if (allInstances[i][2].equals("selected")) numSelected++;
    }
    
    return numSelected;
  }
  
  /**
   * Create a chart (in a seperate frame) to show other data.
   */
  private void osLoadChart() {
    
    if (osLoadChartDisplayed == false) {
      osLoadChart = new OsLoadChart[allInstances.length]; // array of charts
      osLoadCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId;
      if (ConsoleWindow.getDBVersion() >= 10.2) {
        cursorId = "osLoad.sql";
      }
      else {
        cursorId = "osLoad101.sql";
      }

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(osLoadChart[i] instanceof OsLoadChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(osLoadChart[i] instanceof OsLoadChart)) osLoadChart[i] = new OsLoadChart();
              osLoadChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(osLoadChart[i] instanceof OsLoadChart)) osLoadChart[i] = new OsLoadChart();
              osLoadCP[i] =  osLoadChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) osLoadChart[i].setChartTitle(allInstances[i][1]);
              addChart(osLoadCP[i]);
            }
            
            osLoadChartDisplayed = true;
          }
        }     
        else {
          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
            
            // update the chart because it already exists 
            osLoadChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && osLoadChart[i].isChartTitleSet() == false) osLoadChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(osLoadCP[i]);
            osLoadChart[i] = null;
            osLoadCP[i] = null;
          }
        }
      }
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
   * Create a chart (in a seperate frame) to show calls other data.
   */
  private void callsChart() {

    if (callsChartDisplayed == false) {
      callsChart = new CallsChart[allInstances.length]; // array of charts
      callsCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "sysstat.sql";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(callsChart[i] instanceof CallsChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("String","user calls");
            myPars.addParameter("String","recursive calls");
            myPars.addParameter("String","ignore me");
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(callsChart[i] instanceof CallsChart)) callsChart[i] = new CallsChart();
              callsChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(callsChart[i] instanceof CallsChart)) callsChart[i] = new CallsChart();
              callsCP[i] =  callsChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) callsChart[i].setChartTitle(allInstances[i][1]);
              addChart(callsCP[i]);
            }
            
            callsChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("String","user calls");
          myPars.addParameter("String","recursive calls");
          myPars.addParameter("String","ignore me");
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            callsChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && callsChart[i].isChartTitleSet() == false) callsChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(callsCP[i]);
            callsChart[i] = null;
            callsCP[i] = null;
          }
        }
      }
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
   * Create a chart (in a seperate frame) to show physical tablespaceFile other data.
   */
   private void physicalIOChart() {
     if (physicalIOChartDisplayed == false) {
       physicalIOChart = new PhysicalIOChart[allInstances.length]; // array of charts
       physicalIOCP = new ChartPanel[allInstances.length]; // array of chart panels
     }
     
     try {
       String cursorId = "sysstat.sql";
       
       for (int i=0; i < allInstances.length; i++) {
     
         if (!(physicalIOChart[i] instanceof PhysicalIOChart )) {    
           // a chart does not already exist for this instance
           if (allInstances[i][2].equals("selected")) {
             Parameters myPars = new Parameters();
             if (ConsoleWindow.getDBVersion() >= 10.2) {
               myPars.addParameter("String", "physical read total bytes");
               myPars.addParameter("String", "physical write total bytes");
             }
             else {
               myPars.addParameter("String", "physical reads");
               myPars.addParameter("String", "physical writes");              
             }
             myPars.addParameter("String","ignore me");
             myPars.addParameter("int",allInstances[i][0]);

             Cursor myCursor = new Cursor(cursorId,true);
             Boolean flip = false;
             boolean eggTimer = false;
             ResultCache resultCache = null;
             boolean restrictRows = false;
             String filterByRACAlias = "none";
             String filterByUserAlias = "none";
             Boolean includeDecode = false;
             String includeRACPredicatePoint = "default";
             Boolean filterByRAC = false;
             Boolean filterByUser = false;
             QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

             if (Properties.isBreakOutChartsTabsFrame()) {
               if (!(physicalIOChart[i] instanceof PhysicalIOChart)) physicalIOChart[i] = new PhysicalIOChart();
               physicalIOChart[i].createChart(myResult, allInstances[i][1]);
             } 
             else {
               if (!(physicalIOChart[i] instanceof PhysicalIOChart)) physicalIOChart[i] = new PhysicalIOChart();
               physicalIOCP[i] =  physicalIOChart[i].createChartPanel(myResult);
               if (!ConsoleWindow.isOnlyLocalInstanceSelected()) physicalIOChart[i].setChartTitle(allInstances[i][1]);
               
               addChart(physicalIOCP[i]);
             }
                         
             physicalIOChartDisplayed = true;
           }
         }     
         else {
           Parameters myPars = new Parameters();
           if (ConsoleWindow.getDBVersion() >= 10.2) {
             myPars.addParameter("String", "physical read total bytes");
             myPars.addParameter("String", "physical write total bytes");
           }
           else {
             myPars.addParameter("String", "physical reads");
             myPars.addParameter("String", "physical writes");              
           }
           myPars.addParameter("String","ignore me");
           myPars.addParameter("int",allInstances[i][0]);
           
           Cursor myCursor = new Cursor(cursorId,true);
           Boolean flip = false;
           boolean eggTimer = false;
           ResultCache resultCache = null;
           boolean restrictRows = false;
           String filterByRACAlias = "none";
           String filterByUserAlias = "none";
           Boolean includeDecode = false;
           String includeRACPredicatePoint = "default";
           Boolean filterByRAC = false;
           Boolean filterByUser = false;
           QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

           // a chart already exists for this instance
           if (allInstances[i][2].equals("selected")) {

             // update the chart because it already exists 
             physicalIOChart[i].updateChart(myResult);
             
             // set the chart title
             if (numberOfSelectedInstances() > 1 && physicalIOChart[i].isChartTitleSet() == false) physicalIOChart[i].setChartTitle(allInstances[i][1]);
           }
           else {
             // this instance if not selected, check to see if it is a chart and if so, remove it
             removeChart(physicalIOCP[i]);
             physicalIOChart[i] = null;
             physicalIOCP[i] = null;
           }
         }
       }
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
  
  
  private void iopsChart() {
    if (iopsChartDisplayed == false) {
      iopsChart = new IOPSChart[allInstances.length]; // array of charts
      iopsCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "sysstat.sql";
      
      for (int i=0; i < allInstances.length; i++) {
    
        if (!(iopsChart[i] instanceof IOPSChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("String", "physical read total IO requests");
            myPars.addParameter("String", "physical write total IO requests");
            myPars.addParameter("String","ignore me");
            myPars.addParameter("int",allInstances[i][0]);

            long queryStartTimeInMS = System.currentTimeMillis();
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(iopsChart[i] instanceof IOPSChart)) iopsChart[i] = new IOPSChart();
              iopsChart[i].createChart(myResult, allInstances[i][1],queryStartTimeInMS);
            } 
            else {
              if (!(iopsChart[i] instanceof IOPSChart)) iopsChart[i] = new IOPSChart();
              iopsCP[i] =  iopsChart[i].createChartPanel(myResult, queryStartTimeInMS);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) iopsChart[i].setChartTitle(allInstances[i][1]);
              
              addChart(iopsCP[i]);
            }
                        
            iopsChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("String", "physical read total IO requests");
          myPars.addParameter("String", "physical write total IO requests");
          myPars.addParameter("String","ignore me");
          myPars.addParameter("int",allInstances[i][0]);
          
          long queryStartTimeInMS = System.currentTimeMillis();
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            iopsChart[i].updateChart(myResult,queryStartTimeInMS);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && iopsChart[i].isChartTitleSet() == false) iopsChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(iopsCP[i]);
            iopsChart[i] = null;
            iopsCP[i] = null;
          }
        }
      }
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
   * Create a chart (in a seperate frame) to show parse other data.
   */
  private void parseChart() {

    if (parseChartDisplayed == false) {
      parseChart = new ParseChart[allInstances.length]; // array of charts
      parseCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "sysstat.sql";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(parseChart[i] instanceof ParseChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("String","parse count (total)");
            myPars.addParameter("String","parse count (hard)");
            myPars.addParameter("String","parse count (failures)");
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(parseChart[i] instanceof ParseChart)) parseChart[i] = new ParseChart();
              parseChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(parseChart[i] instanceof ParseChart)) parseChart[i] = new ParseChart();
              parseCP[i] =  parseChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) parseChart[i].setChartTitle(allInstances[i][1]);
              addChart(parseCP[i]);
            }
            
            parseChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("String","parse count (total)");
          myPars.addParameter("String","parse count (hard)");
          myPars.addParameter("String","parse count (failures)");
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            parseChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && parseChart[i].isChartTitleSet() == false) parseChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(parseCP[i]);
            parseChart[i] = null;
            parseCP[i] = null;
          }
        }
      }
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


  private void undoChart() {
    
    if (undoChartDisplayed == false) {
      undoChart = new UndoChart[allInstances.length]; // array of charts
      undoCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "undo3.sql";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(undoChart[i] instanceof UndoChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            
            Cursor myCursor = new Cursor(cursorId,true);
            Parameters myPars = new Parameters();
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

//            if (isBreakOut()) {
//              if (!(undoChart[i] instanceof UndoChart)) undoChart[i] = new UndoChart();
//              undoChart[i].createChart(myResult, allInstances[i][1]);
//            } 
//            else {
              if (!(undoChart[i] instanceof UndoChart)) undoChart[i] = new UndoChart();
              undoCP[i] =  undoChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) undoChart[i].setChartTitle(allInstances[i][1]);
              addChart(undoCP[i]);
//            }
            
            undoChartDisplayed = true;
          }
        }     
        else {
          
          Cursor myCursor = new Cursor(cursorId,true);
          Parameters myPars = new Parameters();
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            undoChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && undoChart[i].isChartTitleSet() == false) undoChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(undoCP[i]);
            undoChart[i] = null;
            undoCP[i] = null;
          }
        }
      }
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
  
  private void sortChart() {


    if (sortChartDisplayed == false) {
      sortChart = new SortChart[allInstances.length]; // array of charts
      sortCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "sort2.sql";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(sortChart[i] instanceof SortChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            
            Cursor myCursor = new Cursor(cursorId,true);
            Parameters myPars = new Parameters();
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

//            if (isBreakOut()) {
//              if (!(sortChart[i] instanceof SortChart)) sortChart[i] = new SortChart();
//              sortChart[i].createChart(myResult, allInstances[i][1]);
//            } 
//            else {
              if (!(sortChart[i] instanceof SortChart)) sortChart[i] = new SortChart();
              sortCP[i] =  sortChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) sortChart[i].setChartTitle(allInstances[i][1]);
              addChart(sortCP[i]);
//            }
            
            sortChartDisplayed = true;
          }
        }     
        else {
          
          Cursor myCursor = new Cursor(cursorId,true);
          Parameters myPars = new Parameters();
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);


          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            sortChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && sortChart[i].isChartTitleSet() == false) sortChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(sortCP[i]);
            sortChart[i] = null;
            sortCP[i] = null;
          }
        }
      }
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
   * Create a chart (in a seperate frame) to show shared pool utilization
   */
/*  private void sharedPoolChart() {
    QueryResult myResult;
    try {
      String cursorId = "sharedPool.sql";
      myResult = ExecuteDisplay.execute(cursorId, false, false);

      if (sharedPoolChartDisplayed == false) {
        //sharedPoolChart = new SharedPoolChart();
        sharedPoolChart = new SharedPoolChartEnhanced();
        if (isBreakOut()) {
          sharedPoolChart.createChart(myResult, iterationCounter);
        }
        else {
          sharedPoolCP = sharedPoolChart.createChartPanel(myResult, iterationCounter);
          addChart(sharedPoolCP);
        }
        sharedPoolChartDisplayed = true;
        numCharts++;
      }
      else {
        // remove oldest values 
        sharedPoolChart.removeOldEntries();
        sharedPoolChart.updateChart(myResult);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }*/
  
  
  /**
   * Create a chart (in a seperate frame) to show shared pool utilization
   */
  private void sharedPoolChart() {
    
    if (sharedPoolChartDisplayed == false) {
      sharedPoolChart = new SharedPoolChart[allInstances.length]; // array of charts
      sharedPoolCP = new JPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "sharedPool.sql";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(sharedPoolChart[i] instanceof SharedPoolChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(sharedPoolChart[i] instanceof SharedPoolChart)) sharedPoolChart[i] = new SharedPoolChart();
              sharedPoolChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(sharedPoolChart[i] instanceof SharedPoolChart)) sharedPoolChart[i] = new SharedPoolChart();
              sharedPoolCP[i] =  sharedPoolChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) sharedPoolChart[i].setChartTitle(allInstances[i][1]);
              addChart(sharedPoolCP[i]);
            }
            
            sharedPoolChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            sharedPoolChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && sharedPoolChart[i].isChartTitleSet() == false) sharedPoolChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeJPanelChart(sharedPoolCP[i]);
            sharedPoolChart[i] = null;
            sharedPoolCP[i] = null;
          }
        }
      }
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
   * 
   */
   private void tsIOChart() {
     
     if (tsIOChartDisplayed == false) {
       tablespaceIOChart = new TablespaceIOChart[allInstances.length]; // array of charts
       tsIOCP = new ChartPanel[allInstances.length]; // array of chart panels
     }
     
     try {
       String cursorId = "tablespaceIO.sql";
       
       for (int i=0; i < allInstances.length; i++) {
     
         if (!(tablespaceIOChart[i] instanceof TablespaceIOChart )) {    
           // a chart does not already exist for this instance
           if (allInstances[i][2].equals("selected")) {
             
             Cursor myCursor = new Cursor(cursorId,true);
             Parameters myPars = new Parameters();
             Boolean flip = false;
             boolean eggTimer = false;
             ResultCache resultCache = null;
             boolean restrictRows = false;
             String filterByRACAlias = "none";
             String filterByUserAlias = "none";
             Boolean includeDecode = false;
             String includeRACPredicatePoint = "default";
             Boolean filterByRAC = false;
             Boolean filterByUser = false;
             QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

//             if (isBreakOut()) {
//               if (!(tablespaceIOChart[i] instanceof TablespaceIOChart)) tablespaceIOChart[i] = new TablespaceIOChart();
//               tablespaceIOChart[i].createChart(myResult, allInstances[i][1]);
//             } 
//             else {
               if (!(tablespaceIOChart[i] instanceof TablespaceIOChart)) tablespaceIOChart[i] = new TablespaceIOChart();
               tsIOCP[i] =  tablespaceIOChart[i].createChartPanel(myResult);
               if (!ConsoleWindow.isOnlyLocalInstanceSelected()) tablespaceIOChart[i].setChartTitle(allInstances[i][1]);
               addChart(tsIOCP[i]);
//             }
             
             tsIOChartDisplayed = true;
           }
         }     
         else {
           
           Cursor myCursor = new Cursor(cursorId,true);
           Parameters myPars = new Parameters();
           Boolean flip = false;
           boolean eggTimer = false;
           ResultCache resultCache = null;
           boolean restrictRows = false;
           String filterByRACAlias = "none";
           String filterByUserAlias = "none";
           Boolean includeDecode = false;
           String includeRACPredicatePoint = "default";
           Boolean filterByRAC = false;
           Boolean filterByUser = false;
           QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);


           // a chart already exists for this instance
           if (allInstances[i][2].equals("selected")) {

             // update the chart because it already exists 
             tablespaceIOChart[i].updateChart(myResult);
             
             // set the chart title
             if (numberOfSelectedInstances() > 1 && tablespaceIOChart[i].isChartTitleSet() == false) tablespaceIOChart[i].setChartTitle(allInstances[i][1]);
           }
           else {
             // this instance if not selected, check to see if it is a chart and if so, remove it
             removeChart(tsIOCP[i]);
             tablespaceIOChart[i] = null;
             tsIOCP[i] = null;
           }
         }
       }
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
   
   
  private void fileIOChart() {
    
    if (fileIOChartDisplayed == false) {
      fileIOChart = new FileIOChart[allInstances.length]; // array of charts
      fileIOCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
//      Cursor myCursor = new Cursor("fileIO.sql",true);
      
      Cursor myCursor = new Cursor("fileIO.sql",true);
      Parameters myPars = new Parameters();
      Boolean flip = false;
      Boolean eggTimer = true;
      ResultCache resultCache = null;
      Boolean restrictRows = true;
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";

      for (int i=0; i < allInstances.length; i++) {
    
        if (!(fileIOChart[i] instanceof FileIOChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
//            QueryResult myResult = ExecuteDisplay.execute(myCursor,false,false,null);

//            if (isBreakOut()) {
//              if (!(fileIOChart[i] instanceof FileIOChart)) fileIOChart[i] = new FileIOChart();
//              fileIOChart[i].createChart(myResult, allInstances[i][1]);
//            } 
//            else {
              if (!(fileIOChart[i] instanceof FileIOChart)) fileIOChart[i] = new FileIOChart();
              fileIOCP[i] =  fileIOChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) fileIOChart[i].setChartTitle(allInstances[i][1]);
              addChart(fileIOCP[i]);
//            }
            
            fileIOChartDisplayed = true;
          }
        }     
        else {
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
//          QueryResult myResult = ExecuteDisplay.execute(myCursor,false,false,null);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            fileIOChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && fileIOChart[i].isChartTitleSet() == false) fileIOChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(fileIOCP[i]);
            fileIOChart[i] = null;
            fileIOCP[i] = null;
          }
        }
      }
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

   
  private void asmIOStatChart() {
    
    if (asmIOStatChartDisplayed == false) {
      asmIOStatChart = new ASMIOStatChart[allInstances.length]; // array of charts
      asmIOStatCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "asmDiskIOStat2.sql";
      
      for (int i=0; i < allInstances.length; i++) {
    
        if (!(asmIOStatChart[i] instanceof ASMIOStatChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(asmIOStatChart[i] instanceof ASMIOStatChart)) asmIOStatChart[i] = new ASMIOStatChart();
              asmIOStatChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(asmIOStatChart[i] instanceof ASMIOStatChart)) asmIOStatChart[i] = new ASMIOStatChart();
              asmIOStatCP[i] =  asmIOStatChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) asmIOStatChart[i].setChartTitle(allInstances[i][1]);
              addChart(asmIOStatCP[i]);
            }
            
            asmIOStatChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            asmIOStatChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && asmIOStatChart[i].isChartTitleSet() == false) asmIOStatChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(asmIOStatCP[i]);
            asmIOStatChart[i] = null;
            asmIOStatCP[i] = null;
          }
        }
      }
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
  
  private void pgaChart() {
    
    if (pgaChartDisplayed == false) {
      pgaChart = new PGAChart[allInstances.length]; // array of charts
      pgaCP = new ChartPanel[allInstances.length]; // array of chart panels
    }
    
    try {
      String cursorId = "pgastat.sql";
      
      for (int i=0; i < allInstances.length; i++) {
    
        if (!(pgaChart[i] instanceof PGAChart )) {    
          // a chart does not already exist for this instance
          if (allInstances[i][2].equals("selected")) {
            Parameters myPars = new Parameters();
            myPars.addParameter("int",allInstances[i][0]);
            
            Cursor myCursor = new Cursor(cursorId,true);
            Boolean flip = false;
            boolean eggTimer = false;
            ResultCache resultCache = null;
            boolean restrictRows = false;
            String filterByRACAlias = "none";
            String filterByUserAlias = "none";
            Boolean includeDecode = false;
            String includeRACPredicatePoint = "default";
            Boolean filterByRAC = false;
            Boolean filterByUser = false;
            QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

            if (Properties.isBreakOutChartsTabsFrame()) {
              if (!(pgaChart[i] instanceof PGAChart)) pgaChart[i] = new PGAChart();
              pgaChart[i].createChart(myResult, allInstances[i][1]);
            } 
            else {
              if (!(pgaChart[i] instanceof PGAChart)) pgaChart[i] = new PGAChart();
              pgaCP[i] =  pgaChart[i].createChartPanel(myResult);
              if (!ConsoleWindow.isOnlyLocalInstanceSelected()) pgaChart[i].setChartTitle(allInstances[i][1]);
              addChart(pgaCP[i]);
            }
            
            pgaChartDisplayed = true;
          }
        }     
        else {
          Parameters myPars = new Parameters();
          myPars.addParameter("int",allInstances[i][0]);
          
          Cursor myCursor = new Cursor(cursorId,true);
          Boolean flip = false;
          boolean eggTimer = false;
          ResultCache resultCache = null;
          boolean restrictRows = false;
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

          // a chart already exists for this instance
          if (allInstances[i][2].equals("selected")) {

            // update the chart because it already exists 
            pgaChart[i].updateChart(myResult);
            
            // set the chart title
            if (numberOfSelectedInstances() > 1 && pgaChart[i].isChartTitleSet() == false) pgaChart[i].setChartTitle(allInstances[i][1]);
          }
          else {
            // this instance if not selected, check to see if it is a chart and if so, remove it
            removeChart(pgaCP[i]);
            pgaChart[i] = null;
            pgaCP[i] = null;
          }
        }
      }
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


  public static int getIterationSleepS() {
    int s = Integer.valueOf(String.valueOf(iterationSleepS.getValue())).intValue();
    return s;
  }
 
  private void addChart(org.jfree.chart.ChartPanel myChartPanel) {
//    LineBorder myBorder = new LineBorder(Color.GRAY);
//    myChartPanel.setBorder(myBorder);
    chartP.add(myChartPanel);
    incrementNumCharts(1);
    setChartPLayout();
    chartP.updateUI();
   }  
   
  private void addChart(JPanel myPanel) {
  //    LineBorder myBorder = new LineBorder(Color.WHITE);
  //    myPanel.setBorder(myBorder);
    chartP.add(myPanel);
    incrementNumCharts(1);
    setChartPLayout();    
    chartP.updateUI();
  }  
  
/*  private void addChart(JScrollPane myScrollPane) {
//    LineBorder myBorder = new LineBorder(Color.WHITE);
//    myPanel.setBorder(myBorder);
    chartP.add(myScrollPane);
    incrementNumCharts(1);
    setChartPLayout();
    chartP.updateUI();
  } */
  
  private boolean isCharted(ChartPanel myChartPanel) {
    Component[] myComps = chartP.getComponents();
    boolean exists = false;
    for (int i=0; i < myComps.length; i++) {
      if (myComps[i] == myChartPanel) {
        exists = true;
        break;
      }
    }
    
    return exists;
  }  
  
  private boolean isCharted(JPanel myPanel) {
    Component[] myComps = chartP.getComponents();
    boolean exists = false;
    for (int i=0; i < myComps.length; i++) {
      if (myComps[i] == myPanel) {
        exists = true;
        break;
      }
    }
    
    return exists;
  }
  
  private void setOffSet(String source) {

    if (debug) System.out.println("set offset numCharts: " + numCharts);
    
//    if (!isBreakOut()) {
      if (source.equals("waitEvents")) {
        for (int i=0; i < waitEventsChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) waitEventsChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) waitEventsChart[i].setOffSet(49);
            if (numCharts > 4) waitEventsChart[i].setOffSet(31);
          }
        }
      }      
      
      if (source.equals("sharedPool")) {
        for (int i=0; i < sharedPoolChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) sharedPoolChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) sharedPoolChart[i].setOffSet(43);
            if (numCharts > 4) sharedPoolChart[i].setOffSet(23);
          }
        }
      }      
      
      if (source.equals("physicalIO")) {
        for (int i=0; i < physicalIOChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) physicalIOChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) physicalIOChart[i].setOffSet(55);
            if (numCharts > 4) physicalIOChart[i].setOffSet(35);
          }
        }
      } 
      
      if (source.equals("IOPS")) {
        for (int i=0; i < iopsChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) iopsChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) iopsChart[i].setOffSet(55);
            if (numCharts > 4) iopsChart[i].setOffSet(35);
          }
        }
      }      
      
      if (source.equals("asmIOStat")) {
        for (int i=0; i < asmIOStatChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) asmIOStatChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) asmIOStatChart[i].setOffSet(55);
            if (numCharts > 4) asmIOStatChart[i].setOffSet(35);
          }
        }
      }      
      
      if (source.equals("calls")) {
        for (int i=0; i < callsChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
          if (numCharts <= 2) callsChart[i].resetOffSet();
          if (numCharts > 2 && numCharts <= 4) callsChart[i].setOffSet(55);
          if (numCharts > 4) callsChart[i].setOffSet(35);
          }
        }
      }      
      
      if (source.equals("pga")) {
        for (int i=0; i < pgaChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
          if (numCharts <= 2) pgaChart[i].resetOffSet();
          if (numCharts > 2 && numCharts <= 4) pgaChart[i].setOffSet(55);
          if (numCharts > 4) pgaChart[i].setOffSet(35);
          }
        }
      }
      
      if (source.equals("parse")) {
        for (int i=0; i < parseChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) parseChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) parseChart[i].setOffSet(55);
            if (numCharts > 4) parseChart[i].setOffSet(35);
          }
        }
      }      
      
      if (source.equals("osLoad")) {
        for (int i=0; i < osLoadChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {        
            if (numCharts <= 2) osLoadChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) osLoadChart[i].setOffSet(45);
            if (numCharts > 4) osLoadChart[i].setOffSet(25);
          }
        }
      }            
      
      if (source.equals("undo")) {
        for (int i=0; i < undoChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 4) undoChart[i].resetOffSet();
            if (numCharts > 4) undoChart[i].setOffSet(40);
          }
        }
      }       
      
      if (source.equals("sort")) {
        for (int i=0; i < sortChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 4) sortChart[i].resetOffSet();
            if (numCharts > 4) sortChart[i].setOffSet(40);
          }
        }
      }      
//    }
    
//    if (!isBreakOut()) {
      if (source.equals("opq")) {
        for (int i=0; i < opqChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) opqChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) opqChart[i].setOffSet(49);
            if (numCharts > 4) opqChart[i].setOffSet(31);
          }
        }
      }    
//    }    
    
//    if (!isBreakOut()) {
      if (source.equals("opq downgrades")) {
        for (int i=0; i < opqDowngradesChart.length; i++) {
          if (allInstances[i][2].equals("selected")) {
            if (numCharts <= 2) opqDowngradesChart[i].resetOffSet();
            if (numCharts > 2 && numCharts <= 4) opqDowngradesChart[i].setOffSet(49);
            if (numCharts > 4) opqDowngradesChart[i].setOffSet(31);
          }
        }
      }    
//    }    
    
    if (source.equals("waitClass")) {
      for (int i=0; i < waitClassChart.length; i++) {
        if (allInstances[i][2].equals("selected")) {
          if (numCharts <= 2) waitClassChart[i].resetOffSet();
          if (numCharts > 2 && numCharts <= 4) waitClassChart[i].setOffSet(49);
          if (numCharts > 4) waitClassChart[i].setOffSet(31);
        }
      }
    }  

    //resizeChartDatasets();
  }

    
  private void decrementNumCharts(int jump) {
    if (debug) System.out.println("decrementnumCharts() by : " + jump);
    numCharts = numCharts - jump;
  }  
  
  private void incrementNumCharts(int jump) {
    if (debug) System.out.println("incrementNumbCharts()by: " + jump);
    numCharts = numCharts + jump;
  }
  
  private void setChartPLayout() {
   int numColumns = chartPLayout.getColumns();
   int numRows = chartPLayout.getRows();
   
    if (numCharts == 1) chartP.setLayout(new GridLayout(1,1));  
    if (numCharts == 2) chartP.setLayout(new GridLayout(2,1));
    if (numCharts == 3) chartP.setLayout(new GridLayout(2,2));
    if (numCharts == 4) chartP.setLayout(new GridLayout(2,2));
    if (numCharts == 5) chartP.setLayout(new GridLayout(2,3));
    if (numCharts == 6) chartP.setLayout(new GridLayout(2,3));    
    if (numCharts == 7) chartP.setLayout(new GridLayout(2,4));    
    if (numCharts == 8) chartP.setLayout(new GridLayout(2,4));
    if (numCharts == 9) chartP.setLayout(new GridLayout(3,4));   
    if (numCharts == 10) chartP.setLayout(new GridLayout(3,4));   
    if (numCharts == 11) chartP.setLayout(new GridLayout(3,4));    
    if (numCharts == 12) chartP.setLayout(new GridLayout(3,4));
  }
  
/*  private void resizeChartDatasets() {
    if (waitEventsSummaryDisplayed) {
      for (int i=0; i < waitEventsChart.length; i++) {
        if (waitEventsChart[i] instanceof WaitEventsChart) {
          waitEventsChart[i].removeOldWaitEventEntries();
          waitEventsChart[i].removeOldSessionEntries();
        }
      }
    }
      
    if (sharedPoolChartDisplayed) sharedPoolChart.removeOldEntries();
    if (osLoadChartDisplayed) osLoadChart.removeOldEntries();
    if (callsChartDisplayed) callsChart.removeOldEntries();
    if (parseChartDisplayed) parseChart.removeOldEntries();
    if (physicalIOChartDisplayed) physicalIOChart.removeOldEntries();
  } */
  
//  public static void setBreakOutBox(boolean value) {
//    breakOutCB.setSelected(value);
//  }

/*  
  private void recordTB_actionPerformed() {
    if (recordTB.isSelected()) {
      record = true;
      
      // check whether a directory exists for today under $RICHMON_BASE\Recording   
      recordExecutionDate = getTodaysDate();
      if (!(recordFirstIterationStartTime instanceof String)) {
        recordFirstIterationStartTime = getTime();
      }
      File richMonDir = new File(ConnectWindow.getBaseDir());
      if (ConnectWindow.isLinux()) {
        richMonDir = new File(ConnectWindow.getBaseDir() + "/Recording/" + ConsoleWindow.getDatabaseName());     
      }
      else {
        richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Recording\\" + ConsoleWindow.getDatabaseName());      
      }
      
      if (!richMonDir.exists()) {
        boolean ok = richMonDir.mkdir();
      }
      
      if (ConnectWindow.isLinux()) {
        richMonDir = new File(ConnectWindow.getBaseDir() + "/Recording/" + ConsoleWindow.getDatabaseName() + "/" + recordExecutionDate);     
      }
      else {
        richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Recording\\" + ConsoleWindow.getDatabaseName() + "\\" + recordExecutionDate);      
      }
      
      if (!richMonDir.exists()) {
        boolean ok = richMonDir.mkdir();
      }
      
      recordTB.setText("Stop Recording");
    }
    else {
      record = false;
      recordTB.setText("Start Recording");
    }  
  }

*/
  
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


