/*
 *  SessionDetailPanel.java        13.0 05/01/04
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
 * Change History since 23/05/05
 * =============================
 *
 * 23/05/05 Richard Wright Switched comment style to match that
 *                         recommended in Sun's own coding conventions.
 * 23/05/05 Richard Wright Modified to use ExecuteDisplay class
 * 23/05/05 Richard Wright Changed remove commands on scroll panes to removeAll
 *                         rather than remove(object) to try and remove a bug
 *                         that caused null pointer exceptions
 * 03/06/05 Richard Wright Stop the egg timer showing and add more try catch blocks
 *                         to try and diagnose array index exceptions
 * 07/06/05 Richard Wright Changed remove(n) to removeAll() when trying to remove
 *                         components in iterate
 * 07/06/05 Richard Wright Put in more try-catch blocks to narrow down null pointer
 *                         exceptions in currentSQL()
 * 07/06/05 Richard Wright Renamed databaseSort2.sql to sessionSort.sql
 * 08/06/05 Richard Wright Added getSid() method
 * 10/11/05 Richard Wright If generating an execution plan on 9.2 or higher
 *                         dbms_xplan will be used
 * 01/03/06 Richard Wright Undo the set current schema before creating a plan table
 *                         to prevent errors
 * 01/03/06 Richard Wright Only obtain an execution plan is the sql has changed
 *                         since the last iteration
 * 01/03/06 Richard Wright When creating a plan table, use the appropriate version
 * 02/03/06 Richard Wright Only get the sql text if it's different to the previous iteration
 * 23/03/06 Richard Wright Replace references to 'c:' with a call to
 *                         ConnectWindow.getBaseDir() and ConnectWindow.isLinux()
 * 07/07/06 Richard Wright Modified calls to gather the execution plan to specify
 *                         timeoutExecutionPlans.
 * 18/08/09 Richard Wright Modified sessionIO to appear flipped as it's only a single row
 * 21/08/09 Richard Wright Temporary files now being deleted on exit
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 14/05/07 Richard Wright Removed unused code
 * 07/09/07 Richard Wright Converted the sql and execution plan output areas from
 *                         JTextArea to JTextPane to allow right click copy to
 *                         be implemented
 * 10/09/07 Richard Wright Implemented removeDuplicatePlan()
 * 12/09/07 Richard Wright Updated every status bar update to show only 1 row for flipped result sets
 * 18/09/07 Richard Wright Corrected numRows in the error message so it displays properly
 * 17/12/07 Richard Wright Modified the error message when no sql plan is produced
 * 10/01/08 Richard Wright Removed script name from statusbar updates
 * 28/04/08 Richard Wright Added the ASH button
 * 02/05/08 Richard Wright Added the session wait history radio button
 * 06/05/08 Richard Wright Added loads of try catch blocks to diagnose errors
 * 14/07/08 Richard Wright Added the blocking sessions panel
 * 14/10/08 Richard Wright Added SQL_ID to be displayed instead of Terminal for 10g and above
 * 14/10/08 Richard Wright Brought forward the section of code that displays the blocked sessions panel
 *                         to give the impression of better performance
 * 28/10/08 Richard Wright Improved error messages so they include the method name to make finding problems easier
 * 28/10/08 Richard Wright Added cursorId to the statusBar update
 * 03/11/08 Richard Wright Modified to use sql_id and prev_sql_id to identify the sql and plan for 10g onwards
 * 10/11/08 Richard Wright Chil Number now used as input dbms_xplan.display_cursor
 * 14/05/09 Richard Wright Making session panels aware of the instance
 * 15/07/09 Richard Wright Move the initialization of SQLResult forward 2 lines in 2 places to see if it stops garbage appeating in the sql panel on some iterations
 * 19/10/09 Richard Wright Finally fixed the null pointer exceptions and odd behaviour when displaying sql.  I have modified displaySQLTable and displayPlanTable
 *                         to make use of SwingUtilities.invokeLater() as a result.
 * 09/12/09 Richard Wright Minor changes to improve the look of the html output which is spooled
 * 09/12/09 Richard Wright Stopped prefixing cursorId's with the sid name for spooled output
 * 16/12/09 Richard Wright Changed title on ash button from 'Show ASH' to 'Active Session History'
 * 24/02/10 Richard Wright Modified name of spooled output to show the instance that the session is on rather than instance richmon is connected too
 * 17/11/10 Richard Wright Fixed bug preventing null pointer exception when sql_id is null
 * 17/11/10 Richard Wright Modified to cope with 10.1 where v$session.blocking_instance does not exist
 * 08/08/11 Richard Wright Removed iterateOnceTB and renames iterateTB to startStopTB
 * 08/08/11 Richard Wright Converting the sql id that it displayed to a button which opens the sqlId panel
 * 09/08/11 Richard Wright Renamed from SessionPanel to SessionDetailPanel
 * 12/10/11 Richard Wright Modified dbms_xplan calls to use 'ALL' instead of 'TYPICAL'
 * 12/10/11 Richard Wright Set TextField font sizes to make it look better on linux (ubuntu)
 * 12/10/11 Richard Wright Removed shared server and dispatchers from the display and added sql_id child number and execution id
 * 12/10/11 Richard Wright Merged p1 and p1 txt to save space, also for p2 and p3
 * 26/11/12 Richard Wright Fixed problem with undo only showing when connected to the correct instance
 * 16/02/14 Richard Wright Changed the font on the SQL_ID button to RED
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultEditorKit;


/**
 * A panel which provides an overview of a single sessions current activity.
 */
public class SessionDetailPanel extends JPanel  {
  //  panels
  JPanel westP = new JPanel();
  JPanel southWestP = new JPanel();
  JPanel otherRadioButtonP = new JPanel();
  JPanel sessionDetailsP = new JPanel();
  JPanel currentWaitEventP = new JPanel();
  JPanel spaceP = new JPanel();
  JPanel blockP = new JPanel();
  public JPanel centreP = new JPanel();
  JSplitPane sqlSpP = new JSplitPane();
  JScrollPane sqlSP = new JScrollPane();
  JScrollPane planSP = new JScrollPane();
  JSplitPane nestedSQLSP = new JSplitPane();
  JPanel offLoadP = new JPanel();
  JScrollPane offLoadSP = new JScrollPane();
  JPanel allButtonsP = new JPanel();
  JPanel controlButtonP = new JPanel();
  JPanel controlButton2P = new JPanel();

  // controlButton2P panel buttons
  JToggleButton startStopTB = new JToggleButton("Start",false);
  JLabel iterationsL = new JLabel("not iterating");
  SpinnerNumberModel snm = new SpinnerNumberModel(5,1,600,1);
  JSpinner iterationSleepS = new JSpinner(snm);
  JToggleButton spoolTB = new JToggleButton("Spool Output");
  JButton ashB = new JButton("Active Session History");

  // statusBar
  JLabel statusBarL = new JLabel();

  // sessionDetailsP labels & textfields
  JLabel sidL = new JLabel("Sid");
  JTextField sidTF = new JTextField();
  JLabel spidL = new JLabel("Spid");
  JTextField spidTF = new JTextField();
  JLabel serialL = new JLabel("Serial#");
  JTextField serialTF = new JTextField();
  JLabel usernameL = new JLabel("Username");
  JTextField usernameTF = new JTextField();
  JLabel logonL = new JLabel("Logon Time");
  JTextField logonTF = new JTextField();
  JLabel statusL = new JLabel("Status");
  JTextField statusTF = new JTextField();
  JLabel serverL = new JLabel("Server");
  JTextField serverTF = new JTextField();
  JLabel programL = new JLabel("Program");
  JTextField programTF = new JTextField();
  JLabel terminalL = new JLabel("Terminal");
  JTextField terminalTF = new JTextField();
  JLabel machineL = new JLabel("Machine");
  JTextField machineTF = new JTextField();
  JLabel sqlIdL = new JLabel("Sql Id");
  JButton sqlIdB = new JButton();
  JLabel instanceNameL = new JLabel("Instance");
  JTextField instanceNameTF = new JTextField();
  JLabel sqlChildNumL = new JLabel("Child#");
  JTextField sqlChildNumTF = new JTextField();
  JLabel sqlExecIdL = new JLabel("Exec Id");
  JTextField sqlExecIdTF = new JTextField();

  // currentWaitEventsP panel
  JLabel eventL = new JLabel("Event");
  JTextField eventTF = new JTextField();
  JLabel p1L = new JLabel("P1");
  JTextField p1TF = new JTextField();
  JLabel p2L = new JLabel("P2");
  JTextField p2TF = new JTextField();
  JLabel p3L = new JLabel("P3");
  JTextField p3TF = new JTextField();
  JLabel stateL = new JLabel("State");
  JTextField stateTF = new JTextField();
  JLabel waitTimeL = new JLabel("Wait Time Micro");
  JTextField waitTimeTF = new JTextField();
  JLabel secondsInWaitL = new JLabel("Secs in Wait micro");
  JTextField secondsInWaitTF = new JTextField();

  // lockRadioButtonGroup
  ButtonGroup otherRBG = new ButtonGroup();
  JRadioButton lockDetailRB = new JRadioButton("Lock Detail");
  JRadioButton lockDecodedRB = new JRadioButton("Lock Decoded");
  JRadioButton blockedSessionsRB = new JRadioButton("Other Sessions Blocked");
  JRadioButton sessionIORB = new JRadioButton("Session IO");
  JRadioButton topWaitEventsRB = new JRadioButton("Top Events");
  JRadioButton sessionWaitHistoryRB = new JRadioButton("Session Wait Hist");
  JRadioButton sqlBindCaptureRB = new JRadioButton("Bind Variables Captured");
  JRadioButton sessionSmartScanRB = new JRadioButton("Smart Scan");
  JRadioButton sessionEHCCRB = new JRadioButton("EHCC");

  // spaceP Panel
  JLabel sortKBL = new JLabel("Sort (k)");
  JTextField sortKBTF = new JTextField();
  JLabel undoKBL = new JLabel("Undo (k)");
  JTextField undoKBTF = new JTextField();
  JLabel pgaKBL = new JLabel("PGA (k)");
  JTextField pgaKBTF = new JTextField();
  JLabel ugaKBL = new JLabel("UGA (K)");
  JTextField ugaKBLTF = new JTextField();

  // blockP Panel
  JLabel blockingSidL = new JLabel("Sid");
  JTextField blockingSidTF = new JTextField();
  JLabel blockingStatusL = new JLabel("Status");
  JTextField blockingStatusTF = new JTextField();
  JLabel blockingInstanceL = new JLabel("Instance");
  JTextField blockingInstanceTF = new JTextField();


  // southWestSP scroll pane
  JScrollPane southWestSP = new JScrollPane();

  // pointers to other objects
  SQLFormatter mySQLFormatter = new SQLFormatter();

  // misc
  Dimension prefSize = new Dimension(110,25);
  Dimension prefSize3 = new Dimension(220,25);
  int sid;
  public long serial;
  int instanceNumber;
  QueryResult currentSQLResult;
  String currentSQLStatement;
  String lastSQLStatement;
  long currentSQLHashValue;
  Object currentSQLAddress = new Object();
  long prevSQLHashValue;
  Object prevSQLAddress = new Object();
  long oldCurrentSQLHashValue;
  long oldPrevSQLHashValue;
  QueryResult tmpResult; /* error message if plan cannot be generated */
  boolean sessionExists = false;
  boolean current = false;  /* is the sql statement shown the current or previous statement */
  JPopupMenu popupMenu;
  GridBagLayout westPGridBagLayout = new GridBagLayout();
  GridBagConstraints westCons = new GridBagConstraints();
  boolean undoAdded = false;
  String sqlId;
  String oldSqlId;
  String prevSqlId;
  String oldPrevSqlId;
  int sqlChildNum;
  int prevChildNum;
  QueryResult executionPlan;
  boolean execPlanExists = false;
  


  boolean debug = false;

   /* used to store the results of the last iteration
    *   0 = sessionDetails         1 = waitEventDetails
    *   2 = undo                   3 = sort
    *   4 = lockDetails            5 = lockDecoded
    *   6 = blockedSessions        7 = sessionIO
    *   8 = sessionEvents          9 = currentSQL
    *  10 = lastSQL               11 = executionPlanCurrentSQL
    *  12 = executionPlanLastSQL
    */
  public QueryResult[] lastIteration = new QueryResult[12];  // array to hold all the query results from the last iteration.

  // iteration stuff
  int numIterations = 0;    // 0=don't iterate, 1=iterate once, 2=keep iterating
  int iterationCounter = 0; // how many times have we iterated since last told to start
  int totalIterations = 0;  // how many times have we iterated - ever!

  // spooling stuff
  OutputHTML outputHTML;
  public boolean currentlySpooling = false;
  File saveFile;
  BufferedWriter save;

  // JTable stuff
  JTextPane sqlTP = new JTextPane();
  JTextPane planTP = new JTextPane();

  /**
   * Constructor.
   */
  public SessionDetailPanel(int instanceNumber,int sid) {
    this.sid = sid;
    this.instanceNumber = instanceNumber;

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    instanceNameTF.setText(ConsoleWindow.getInstanceName(instanceNumber));

    // perform a single iteration
    numIterations = 1;
    iterate();
  }

  /**
   * Defines all the components that make up the panel
   *
   * @throws Exception
   */
  void jbInit() throws Exception {
    // layout
    this.setLayout(new BorderLayout());

    // controlButton2P Panel
    controlButton2P.setLayout(new GridBagLayout());
    GridBagConstraints controlCons = new GridBagConstraints();
    controlCons.gridx=0;
    controlCons.gridy=0;
    controlCons.insets = new Insets(3,3,3,3);
    controlCons.gridx=1;
    spoolTB.setPreferredSize(prefSize);
    spoolTB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          spoolTB_actionPerformed();
        }
      });
    controlButton2P.add(spoolTB, controlCons);

    controlCons.gridx=2;
    controlCons.gridwidth=1;
    

    controlCons.gridx=3;
    startStopTB.setPreferredSize(prefSize);
    startStopTB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          startStopTB_actionPerformed();
        }
      });
    controlButton2P.add(startStopTB, controlCons);

    controlCons.gridx=4;
    iterationSleepS.setPreferredSize(prefSize);
    controlButton2P.add(iterationSleepS, controlCons);

    controlCons.gridx=5;
    iterationsL.setFont(new Font("????", 1, 13));
    iterationsL.setPreferredSize(prefSize3);
    iterationsL.setHorizontalAlignment(SwingConstants.CENTER);
    iterationsL.setForeground(Color.BLACK);
    controlButton2P.add(iterationsL, null);

    ashB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ashB_actionPerformed();
        }
      });
    if (!ConnectWindow.getDiagnosticPackLicensed()) ashB.setEnabled(false);

    // controlButtonP panel
    controlButtonP.add(controlButton2P, BorderLayout.WEST);
    if (ConsoleWindow.getDBVersion() >= 10.0) controlButtonP.add(ashB, BorderLayout.EAST);
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // westP panel
    westP.setLayout(westPGridBagLayout);

    // sessionDetailsP panel
    sessionDetailsP.setLayout(null);
    sessionDetailsP.setPreferredSize(new Dimension(0, 130));
    sessionDetailsP.setBorder(BorderFactory.createTitledBorder("Session Details"));

    sidTF.setBounds(new Rectangle(5, 35, 40, 20));
    sidTF.setForeground(Color.blue);
    sidTF.setFont(new Font("Tahoma",0,11));
    sidL.setBounds(new Rectangle(5, 15, 40, 20));
    sidL.setHorizontalAlignment(SwingConstants.CENTER);
    sidL.setHorizontalTextPosition(SwingConstants.CENTER);
    spidTF.setBounds(new Rectangle(45, 35, 40, 20));
    spidTF.setForeground(Color.blue);
    spidTF.setFont(new Font("Tahoma",0,11));
    spidL.setBounds(new Rectangle(45, 15, 40, 20));
    spidL.setHorizontalAlignment(SwingConstants.CENTER);
    spidL.setHorizontalTextPosition(SwingConstants.CENTER);
    usernameTF.setBounds(new Rectangle(125, 35, 125, 20));
    usernameTF.setForeground(Color.blue);
    usernameTF.setFont(new Font("Tahoma",0,11));
    usernameL.setBounds(new Rectangle(125, 15, 125, 20));
    usernameL.setHorizontalAlignment(SwingConstants.CENTER);
    usernameL.setHorizontalTextPosition(SwingConstants.CENTER);
    serialTF.setBounds(new Rectangle(85, 35, 40, 20));
    serialTF.setForeground(Color.blue);
    serialTF.setFont(new Font("Tahoma",0,11));
    serialL.setBounds(new Rectangle(85, 15, 40, 20));
    serialL.setHorizontalTextPosition(SwingConstants.CENTER);
    serialL.setHorizontalAlignment(SwingConstants.CENTER);
    logonTF.setBounds(new Rectangle(250, 35, 130, 20));
    logonTF.setForeground(Color.blue);
    logonTF.setFont(new Font("Tahoma",0,11));
    logonL.setBounds(new Rectangle(250, 15, 130, 20));
    logonL.setHorizontalTextPosition(SwingConstants.CENTER);
    logonL.setHorizontalAlignment(SwingConstants.CENTER);
    serverTF.setBounds(new Rectangle(50, 55, 75, 20));
    serverTF.setForeground(Color.blue);
    serverTF.setFont(new Font("Tahoma",0,11));
    serverL.setBounds(new Rectangle(5, 55, 55, 20));
    serverL.setHorizontalTextPosition(SwingConstants.CENTER);
    serverL.setHorizontalAlignment(SwingConstants.CENTER);
    statusTF.setBounds(new Rectangle(380, 35, 60, 20));
    statusTF.setForeground(Color.blue);
    statusTF.setFont(new Font("Tahoma",0,11));
    statusL.setBounds(new Rectangle(380, 15, 60, 20));
    statusL.setHorizontalTextPosition(SwingConstants.CENTER);
    statusL.setHorizontalAlignment(SwingConstants.CENTER);
    programL.setBounds(new Rectangle(5, 75, 40, 20));
    programL.setHorizontalTextPosition(SwingConstants.CENTER);
    programL.setHorizontalAlignment(SwingConstants.CENTER);
    programTF.setBounds(new Rectangle(50, 75, 390, 20));
    programTF.setForeground(Color.blue);
    programTF.setFont(new Font("Tahoma",0,11));
    if (ConsoleWindow.getDBVersion() < 10) {
      terminalL.setBounds(new Rectangle(5, 95, 50, 20));
      terminalTF.setBounds(new Rectangle(60, 95, 150, 20));
      terminalTF.setForeground(Color.blue);
      terminalTF.setFont(new Font("Tahoma",0,11));
    }
    else {

      sqlIdB.setBounds(new Rectangle(5, 95, 150, 20));
      sqlIdB.setMargin(new Insets(0,0,0,0));
      Font f = new Font ("Dialog",Font.PLAIN,11);
      sqlIdB.setFont(f);
      sqlIdB.setForeground(Color.red);
      sqlIdB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            sqlIdB_actionPerformed();
          }
        });
    }
    machineL.setBounds(new Rectangle(250, 55, 60, 20));
    machineL.setHorizontalTextPosition(SwingConstants.CENTER);
    machineL.setHorizontalAlignment(SwingConstants.CENTER);
    machineTF.setBounds(new Rectangle(310, 55, 130, 20));
    machineTF.setForeground(Color.blue);
    machineTF.setFont(new Font("Tahoma",0,11));
  
    instanceNameL.setBounds(new Rectangle(135, 55, 55, 20));
    instanceNameL.setHorizontalTextPosition(SwingConstants.CENTER);
    instanceNameL.setHorizontalAlignment(SwingConstants.CENTER);
    instanceNameTF.setBounds(new Rectangle(190, 55, 65, 20));
    instanceNameTF.setForeground(Color.blue);
    instanceNameTF.setFont(new Font("Tahoma",0,11));
    
    sqlChildNumL.setBounds(155,95,65,20);
    sqlChildNumL.setHorizontalTextPosition(SwingConstants.CENTER);
    sqlChildNumL.setHorizontalAlignment(SwingConstants.CENTER);
    sqlChildNumTF.setBounds(210,95,65,20);
    sqlChildNumTF.setForeground(Color.blue);
    sqlChildNumTF.setFont(new Font("Tahoma",0,11));
    
    sqlExecIdL.setBounds(285,95,65,20);
    sqlExecIdL.setHorizontalTextPosition(SwingConstants.CENTER);
    sqlExecIdL.setHorizontalAlignment(SwingConstants.CENTER);
    sqlExecIdTF.setBounds(350,95,95,20);
    sqlExecIdTF.setForeground(Color.blue);
    sqlExecIdTF.setFont(new Font("Tahoma",0,11));
   
    
    sessionDetailsP.add(sidL ,null);
    sessionDetailsP.add(sidTF, null);
    sessionDetailsP.add(spidL, null);
    sessionDetailsP.add(spidTF, null);
    sessionDetailsP.add(serialL, null);
    sessionDetailsP.add(serialTF, null);
    sessionDetailsP.add(usernameL, null);
    sessionDetailsP.add(usernameTF, null);
    sessionDetailsP.add(logonL, null);
    sessionDetailsP.add(logonTF, null);
    sessionDetailsP.add(statusL, null);
    sessionDetailsP.add(statusTF, null);
    sessionDetailsP.add(serverL, null);
    sessionDetailsP.add(serverTF, null);
    sessionDetailsP.add(programL, null);
    sessionDetailsP.add(programTF, null);
    if (ConsoleWindow.getDBVersion() < 10) {
      sessionDetailsP.add(terminalL, null);
      sessionDetailsP.add(terminalTF, null);
    }
    else {
      sessionDetailsP.add(sqlIdL, null);
      sessionDetailsP.add(sqlIdB, null);
      sessionDetailsP.add(sqlChildNumL, null);
      sessionDetailsP.add(sqlChildNumTF, null);
      if (ConsoleWindow.getDBVersion() > 11) {
        sessionDetailsP.add(sqlExecIdL, null);
        sessionDetailsP.add(sqlExecIdTF, null);
      }
      
    }
    sessionDetailsP.add(instanceNameL,null);
    sessionDetailsP.add(instanceNameTF,null);
    sessionDetailsP.add(machineL, null);
    sessionDetailsP.add(machineTF, null);

    // currentWaitEventP panel
    currentWaitEventP.setLayout(null);
    currentWaitEventP.setPreferredSize(new Dimension(455, 105));
    currentWaitEventP.setBorder(BorderFactory.createTitledBorder("Current Wait Event"));

    if (ConsoleWindow.getDBVersion() > 11) {
      waitTimeL.setText("Wait Time Micro");
      secondsInWaitL.setText("Time since last Wait Micro");
      eventL.setBounds(new Rectangle(5, 15, 40, 20));
      eventTF.setBounds(new Rectangle(50, 15, 410, 20));
      eventTF.setForeground(Color.blue);
      eventTF.setFont(new Font("Tahoma", 0, 11));
      eventL.setHorizontalAlignment(SwingConstants.RIGHT);
      stateL.setHorizontalAlignment(SwingConstants.RIGHT);
      waitTimeL.setHorizontalAlignment(SwingConstants.RIGHT);
      secondsInWaitL.setHorizontalAlignment(SwingConstants.RIGHT);
      p1TF.setBounds(new Rectangle(30, 35, 175, 20));
      p1TF.setForeground(Color.blue);
      p1TF.setFont(new Font("Tahoma", 0, 11));
      p1L.setBounds(new Rectangle(5, 35, 30, 20));
      p1L.setHorizontalAlignment(SwingConstants.LEFT);
      p2TF.setBounds(new Rectangle(30, 55, 175, 20));
      p2TF.setForeground(Color.blue);
      p2TF.setFont(new Font("Tahoma", 0, 11));
      p2L.setBounds(new Rectangle(5, 55, 30, 20));
      p2L.setHorizontalAlignment(SwingConstants.LEFT);
      p3TF.setBounds(new Rectangle(30, 75, 175, 20));
      p3TF.setForeground(Color.blue);
      p3TF.setFont(new Font("Tahoma", 0, 11));
      p3L.setBounds(new Rectangle(5, 75, 30, 20));
      p3L.setHorizontalAlignment(SwingConstants.LEFT);
      waitTimeTF.setBounds(new Rectangle(330, 55, 130, 20));
      waitTimeTF.setForeground(Color.blue);
      waitTimeTF.setFont(new Font("Tahoma", 0, 11));
      waitTimeL.setBounds(new Rectangle(210, 55, 110, 20));
      waitTimeL.setHorizontalAlignment(SwingConstants.RIGHT);
      secondsInWaitTF.setBounds(new Rectangle(350, 75, 110, 20));
      secondsInWaitTF.setForeground(Color.blue);
      secondsInWaitTF.setFont(new Font("Tahoma", 0, 11));
      secondsInWaitL.setBounds(new Rectangle(210, 75, 130, 20));
      secondsInWaitL.setHorizontalAlignment(SwingConstants.RIGHT);
      stateTF.setBounds(new Rectangle(320, 35, 140, 20));
      stateTF.setForeground(Color.blue);
      stateTF.setFont(new Font("Tahoma", 0, 11));
      stateL.setBounds(new Rectangle(210, 35, 100, 20));
      stateL.setHorizontalAlignment(SwingConstants.RIGHT);
      currentWaitEventP.add(eventL, null);
      currentWaitEventP.add(eventTF, null);
      currentWaitEventP.add(p1L, null);
      currentWaitEventP.add(p1TF, null);
      currentWaitEventP.add(p2L, null);
      currentWaitEventP.add(p2TF, null);
      currentWaitEventP.add(p3L, null);
      currentWaitEventP.add(p3TF, null);
      currentWaitEventP.add(stateL, null);
      currentWaitEventP.add(stateTF, null);
      currentWaitEventP.add(waitTimeL, null);
      currentWaitEventP.add(waitTimeTF, null);
      currentWaitEventP.add(secondsInWaitL, null);
      currentWaitEventP.add(secondsInWaitTF, null);
    }
    else {
      //
      // positioning of some fields and the labels are a little different in older db's
      //
      waitTimeL.setText("Wait Time");
      secondsInWaitL.setText("Secs in Wait");
      eventL.setBounds(new Rectangle(5, 15, 40, 20));
      eventTF.setBounds(new Rectangle(50, 15, 400, 20));
      eventTF.setForeground(Color.blue);
      eventTF.setFont(new Font("Tahoma",0,11));
      eventL.setHorizontalAlignment(SwingConstants.RIGHT);
      stateL.setHorizontalAlignment(SwingConstants.RIGHT);
      waitTimeL.setHorizontalAlignment(SwingConstants.RIGHT);
      secondsInWaitL.setHorizontalAlignment(SwingConstants.RIGHT);
      p1TF.setBounds(new Rectangle(50, 35, 175, 20));
      p1TF.setForeground(Color.blue); 
      p1TF.setFont(new Font("Tahoma",0,11));
      p1L.setBounds(new Rectangle(5, 35, 30, 20));
      p1L.setHorizontalAlignment(SwingConstants.RIGHT);
      p2TF.setBounds(new Rectangle(50, 55, 175, 20));
      p2TF.setForeground(Color.blue);
      p2TF.setFont(new Font("Tahoma",0,11));
      p2L.setBounds(new Rectangle(5, 55, 30, 20));
      p2L.setHorizontalAlignment(SwingConstants.RIGHT);
      p3TF.setBounds(new Rectangle(50, 75, 175, 20));
      p3TF.setForeground(Color.blue);
      p3TF.setFont(new Font("Tahoma",0,11));
      p3L.setBounds(new Rectangle(5, 75, 30, 20));
      p3L.setHorizontalAlignment(SwingConstants.RIGHT);
      waitTimeTF.setBounds(new Rectangle(300, 55, 140, 20));
      waitTimeTF.setForeground(Color.blue);
      waitTimeTF.setFont(new Font("Tahoma",0,11));
      waitTimeL.setBounds(new Rectangle(230, 55, 70, 20));
      waitTimeL.setHorizontalAlignment(SwingConstants.RIGHT);
      secondsInWaitTF.setBounds(new Rectangle(300, 75, 140, 20));
      secondsInWaitTF.setForeground(Color.blue);
      secondsInWaitTF.setFont(new Font("Tahoma",0,11));
      secondsInWaitL.setBounds(new Rectangle(230, 75, 75, 20));
      secondsInWaitL.setHorizontalAlignment(SwingConstants.CENTER);
      stateTF.setBounds(new Rectangle(300, 35, 140, 20));
      stateTF.setForeground(Color.blue);
      stateTF.setFont(new Font("Tahoma",0,11));
      stateL.setBounds(new Rectangle(230, 35, 40, 20));
      stateL.setHorizontalAlignment(SwingConstants.RIGHT);
      currentWaitEventP.add(eventL, null);
      currentWaitEventP.add(eventTF, null);
      currentWaitEventP.add(p1L, null);
      currentWaitEventP.add(p1TF, null);
      currentWaitEventP.add(p2L, null);
      currentWaitEventP.add(p2TF, null);
      currentWaitEventP.add(p3L, null);
      currentWaitEventP.add(p3TF, null);
      currentWaitEventP.add(stateL, null);
      currentWaitEventP.add(stateTF, null);
      currentWaitEventP.add(waitTimeL, null);
      currentWaitEventP.add(waitTimeTF, null);
      currentWaitEventP.add(secondsInWaitL, null);
      currentWaitEventP.add(secondsInWaitTF, null);
    }

    // sortP Panel
    spaceP.setLayout(null);
    spaceP.setPreferredSize(new Dimension(455,45));
    spaceP.setBorder(BorderFactory.createTitledBorder("Sort / Undo / PGA"));


    sortKBL.setBounds(new Rectangle(5, 15, 50, 20));
    sortKBL.setHorizontalAlignment(SwingConstants.LEFT);
    sortKBL.setHorizontalTextPosition(SwingConstants.LEFT);
    sortKBTF.setBounds(new Rectangle(60, 15, 85, 20));
    sortKBTF.setForeground(Color.blue);
    sortKBTF.setFont(new Font("Tahoma",0,11));
    undoKBL.setBounds(new Rectangle(150, 15, 60, 20));
    undoKBL.setHorizontalAlignment(SwingConstants.LEFT);
    undoKBL.setHorizontalTextPosition(SwingConstants.LEFT);
    undoKBTF.setBounds(new Rectangle(210, 15, 85, 20));
    undoKBTF.setForeground(Color.blue);
    undoKBTF.setFont(new Font("Tahoma",0,11));
    pgaKBL.setBounds(new Rectangle(300, 15, 50, 20));
    pgaKBL.setHorizontalAlignment(SwingConstants.LEFT);
    pgaKBL.setHorizontalTextPosition(SwingConstants.LEFT);
    pgaKBTF.setBounds(new Rectangle(360, 15, 85, 20));
    pgaKBTF.setForeground(Color.blue);
    pgaKBTF.setFont(new Font("Tahoma",0,11));

    spaceP.add(sortKBL, null);
    spaceP.add(sortKBTF, null);
    spaceP.add(undoKBL, null);
    spaceP.add(undoKBTF, null);
    spaceP.add(pgaKBL, null);
    spaceP.add(pgaKBTF, null);


    // blockP Panel
    blockP.setLayout(null);
    blockP.setPreferredSize(new Dimension(455,45));
    blockP.setBorder(BorderFactory.createTitledBorder("Blocking Session"));

    blockingSidL.setBounds(new Rectangle(5, 15, 30, 20));
    blockingSidL.setHorizontalTextPosition(SwingConstants.LEFT);
    blockingSidL.setHorizontalAlignment(SwingConstants.LEFT);
    blockingSidTF.setBounds(new Rectangle(30, 15, 50, 20));
    blockingSidTF.setForeground(Color.blue);
    blockingSidTF.setFont(new Font("Tahoma",0,11));
    blockingStatusL.setBounds(new Rectangle(83, 15, 85, 20));
    blockingStatusL.setHorizontalTextPosition(SwingConstants.LEFT);
    blockingStatusL.setHorizontalAlignment(SwingConstants.LEFT);
    blockingStatusTF.setBounds(new Rectangle(135, 15, 85, 20));
    blockingStatusTF.setForeground(Color.blue);
    blockingStatusTF.setFont(new Font("Tahoma",0,11));
    blockingInstanceL.setBounds(new Rectangle(225, 15, 85, 20));
    blockingInstanceL.setHorizontalTextPosition(SwingConstants.LEFT);
    blockingInstanceL.setHorizontalAlignment(SwingConstants.LEFT);
    blockingInstanceTF.setBounds(new Rectangle(290, 15, 85, 20));
    blockingInstanceTF.setForeground(Color.blue);
    blockingInstanceTF.setFont(new Font("Tahoma",0,11));

    blockP.add(blockingSidL,null);
    blockP.add(blockingSidTF,null);
    blockP.add(blockingStatusL,null);
    blockP.add(blockingStatusTF,null);
    blockP.add(blockingInstanceL,null);
    blockP.add(blockingInstanceTF,null);


    // lockRadioButtonP panel
    otherRadioButtonP.setPreferredSize(new Dimension(455,95));
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      otherRBG.add(sessionSmartScanRB);
      otherRBG.add(sessionEHCCRB);
    }
    otherRBG.add(lockDecodedRB);
    otherRBG.add(blockedSessionsRB);
    otherRBG.add(sessionIORB);
    otherRBG.add(lockDetailRB);
    otherRBG.add(topWaitEventsRB);
    otherRBG.add(sessionWaitHistoryRB);
    otherRBG.add(sqlBindCaptureRB);
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      otherRadioButtonP.add(sessionSmartScanRB,null);
      otherRadioButtonP.add(sessionEHCCRB,null);
    }
    otherRadioButtonP.add(lockDetailRB, null);
    otherRadioButtonP.add(lockDecodedRB, null);
    otherRadioButtonP.add(blockedSessionsRB, null);
    otherRadioButtonP.add(sessionIORB, null);
    otherRadioButtonP.add(topWaitEventsRB, null);
    if (ConsoleWindow.getDBVersion() >= 10) {
      otherRadioButtonP.add(sessionWaitHistoryRB,null);
      otherRadioButtonP.add(sqlBindCaptureRB,null);
    }
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      sessionSmartScanRB.setSelected(true);
    }
    else {
      lockDetailRB.setSelected(true);
    }



    // southWestSP scroll pane
    southWestSP.setPreferredSize(new Dimension(455,50));
    southWestSP.getViewport().setBackground(Color.WHITE);

    // southWestP panel
    southWestP.setLayout(new BorderLayout());
    southWestP.setBorder(BorderFactory.createTitledBorder("Other"));
    southWestP.add(otherRadioButtonP, BorderLayout.NORTH);
    southWestP.add(southWestSP, BorderLayout.CENTER);

    // eastSP split pane
    sqlSpP.setOrientation(JSplitPane.VERTICAL_SPLIT);
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      sqlSpP.setBottomComponent(nestedSQLSP);
    }
    else {
      sqlSpP.setBottomComponent(planSP);
    }
    sqlSpP.setTopComponent(sqlSP);
    sqlSpP.setDividerLocation(250);
    
    // offLoadP pane
    offLoadP.setLayout(new BorderLayout());
//    offLoadP.setBorder(BorderFactory.createTitledBorder("Misc Data related to this SQL_ID (not session)"));
    offLoadP.setBorder(BorderFactory.createTitledBorder("Misc Data from GV$SQL related to this SQL_ID (not session)"));
 
    offLoadP.add(offLoadSP,null);
    
    // nestedSP
    if (ConsoleWindow.getDBVersion() >= 11.2) {
      nestedSQLSP.setOrientation(JSplitPane.VERTICAL_SPLIT);
      nestedSQLSP.setBottomComponent(offLoadP);
      nestedSQLSP.setTopComponent(planSP);
      nestedSQLSP.setDividerLocation(300);
    }


    sqlTP.setEditable(false);
    sqlTP.setForeground(Color.BLUE);
    sqlTP.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseReleased(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    });

    popupMenu = new JPopupMenu();
    popupMenu.add( new DefaultEditorKit.CopyAction() );


    // statusBar
    statusBarL.setText(" ");

    // westP
    westCons.fill = GridBagConstraints.BOTH;
    westCons.gridx=0;
    westCons.gridy=0;
    westCons.weighty=0;
    westP.add(sessionDetailsP, westCons);
    westCons.gridy=1;
    westP.add(currentWaitEventP, westCons);
    westCons.gridy=2;
    westP.add(spaceP, westCons);
    westCons.gridy=3;

//    if (ConsoleWindow.getDBVersion() >= 10.0) {
//      westCons.gridy=4;
//      westP.add(blockP,westCons);
//      westCons.gridy=5;
//      westCons.weighty=2;
//    }
//    else {
      westCons.gridy=5;
      westCons.weighty=2;
//    }
    westP.add(southWestP, westCons);

    // sqlSP scrollpane
    sqlSP.getViewport().setBackground(Color.WHITE);

    // planSP scrollpane
    planSP.getViewport().setBackground(Color.WHITE);

    // centreP pane
    centreP.setLayout(new BorderLayout());
    centreP.add(sqlSpP, BorderLayout.CENTER);
    centreP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    // no longer add this panel in, just default the forceUserTF to the session username instead
//    if (!ConsoleWindow.menuFileEnableV$SQL_Plan.isSelected()) centreP.add(forceUserP, BorderLayout.NORTH);

    // allButtonsP panel
    allButtonsP.setLayout(new BorderLayout());
    allButtonsP.add(controlButtonP, BorderLayout.WEST);

    // construct sessionPanel
    this.add(westP, BorderLayout.WEST);
    this.add(centreP, BorderLayout.CENTER);
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(allButtonsP, BorderLayout.NORTH);

  }

  /**
   * Close this tab.  This also removes the pointer to the tab from the sessionsV Vector in ConsoleWindow.
   */
  void closeB_actionPerformed() {
    numIterations=0;
    ConsoleWindow.removeSessionPanel(this);
  }

  /**
   * Starts a new deamon thread which executes the selected options and then
   * sleeps for n seconds, as defined by the iteraction interval.
   */
  void iterate() {
    Thread sessionOverviewThread = new Thread ( new Runnable()
    {
      public void run()
      {
        while (numIterations > 0) {
          // increment iteration counters
          iterationCounter++;
          totalIterations++;

          // make this the last iteration if this is a once only
          if (numIterations == 1) numIterations =0;

          // update iterationsL
          iterationsL.setText("Iteration " + iterationCounter);

          sessionDetails();
          if (sessionExists) {
//          System.out.println("a: " + blockingSidTF.getText().trim().length() + "b: " + blockingSidTF.getText());
            if (blockingSidTF.getText().trim().length() > 0 && (!undoAdded)) {
              westCons.gridy=4;
              westCons.weighty=0;
              westP.add(blockP,westCons);
              undoAdded = true;
            }

            waitEventDetails();
            undo();
            sort();
            pga();
            if (lockDetailRB.isSelected()) lockDetails();
            if (lockDecodedRB.isSelected()) lockDecoded();
            if (blockedSessionsRB.isSelected()) blockedSessions();
            if (sessionIORB.isSelected()) sessionIO();
            if (topWaitEventsRB.isSelected()) sessionEvents();
            if (sessionWaitHistoryRB.isSelected()) sessionWaitHistory();
            if (sqlBindCaptureRB.isSelected()) sqlBindCapture();
            if (sessionSmartScanRB.isSelected()) sessionSmartScan();
            if (sessionEHCCRB.isSelected()) sessionEHCC();


            /*
             * Database verions earlier then 10g use sql_hash_value and sql address to find the current sql statement
             * whereas from 10g the sql_id and child_number are used.
             */
            if (ConsoleWindow.getDBVersion() < 10) {
              if ((!(oldCurrentSQLHashValue == currentSQLHashValue && oldPrevSQLHashValue == prevSQLHashValue)) || totalIterations == 1) {
                currentSQLResult = currentSQL();
                oldCurrentSQLHashValue = currentSQLHashValue;
                oldPrevSQLHashValue = prevSQLHashValue;              }
            }
            else {
              if ((!(oldSqlId == sqlId && oldPrevSqlId == prevSqlId)) || totalIterations == 1) {
                currentSQLResult = currentSQL10();
                oldSqlId = sqlId;
                oldPrevSqlId = prevSqlId;
              }
            }


            /*
             * Compare the sql statement with that from the previous iteration, if different then get another execution plan
             */
            currentSQLStatement = extractSQLStatement(currentSQLResult);
            if ((!currentSQLStatement.equals(lastSQLStatement))) {
              explainSQL(currentSQLResult);
              lastSQLStatement = extractSQLStatement(currentSQLResult);
            }
            else {
              try {
                if (currentlySpooling) outputHTML.saveSingleResult(executionPlan);
              }
              catch (Exception e) {
                //
              }
            }
            
            if (ConsoleWindow.getDBVersion() >= 11) cellOffLoadEfficiencyFromV$SQL();
          }
          else {
            clearWaitEventDetails();
            clearSort();
            clearUndo();

            // clear the lock panel
            if (southWestSP.getViewport().getComponentCount() > 0) southWestSP.getViewport().removeAll();
            // clear sql area
            if (sqlSP.getViewport().getComponentCount() > 0) sqlSP.getViewport().removeAll();
            // clear the plan area
            if (planSP.getViewport().getComponentCount() > 0) planSP.getViewport().removeAll();
          }

          if (numIterations > 0) {
            // if another iteration is required, sleep first
            long sleepMillis = Integer.valueOf(String.valueOf(iterationSleepS.getValue())).intValue();
            sleepMillis = sleepMillis * 1000;  // convert seconds to milli seconds

            try {
              Thread.sleep(sleepMillis);
            }
            catch (InterruptedException e) {
              ConsoleWindow.displayError(e,this,"Error whilst sleeping");
            }
          }
        }

      }
    });

    sessionOverviewThread.setDaemon(false);
    sessionOverviewThread.setName("sessionOverview");
    sessionOverviewThread.start();
  }

  /**
   * Display session details.
   */
  void sessionDetails() {
    // create a cursor and execute the sql
    try {
      String cursorId;
      if (ConsoleWindow.getDBVersion() >= 11) {
        cursorId = "sessionDetails11.sql";
      }
      else {
        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "sessionDetails10.sql";
        } else {
          if (ConsoleWindow.getDBVersion() >= 10.1) {
            cursorId = "sessionDetails101.sql";
          } else {
            cursorId = "sessionDetails.sql";
          }
        }
      }

      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      // display the result
      if (myResult.getNumRows() > 0) {
        Vector resultSet = myResult.getResultSetRow(0);
        spidTF.setText(String.valueOf(resultSet.elementAt(0)));
        sidTF.setText(String.valueOf(resultSet.elementAt(1)));
        //sidTF.setFont(new Font("Tahoma",0,11));
        usernameTF.setText(String.valueOf(resultSet.elementAt(2)));
        serialTF.setText(String.valueOf(resultSet.elementAt(3)));
        serial = Long.valueOf((String)resultSet.elementAt(3)).longValue();
        logonTF.setText(String.valueOf(resultSet.elementAt(4)));
        serverTF.setText(String.valueOf(resultSet.elementAt(5)));
    //    sharedServerTF.setText(String.valueOf(resultSet.elementAt(6)));
    //    dispatcherTF.setText(String.valueOf(resultSet.elementAt(7)));
        statusTF.setText(String.valueOf(resultSet.elementAt(8)));
        programTF.setText(String.valueOf(resultSet.elementAt(9)));
        if (ConsoleWindow.getDBVersion() < 10.1) {
          terminalTF.setText(String.valueOf(resultSet.elementAt(10)));
        }
        else {
          sqlIdB.setText("sql_id:" + String.valueOf(resultSet.elementAt(10)));
          sqlIdB.setFont(new Font("Dialog",0,11));
          sqlId = String.valueOf(resultSet.elementAt(10));
        }
        machineTF.setText(String.valueOf(resultSet.elementAt(11)));
        currentSQLHashValue = Long.valueOf(String.valueOf(resultSet.elementAt(12))).longValue();
        currentSQLAddress = resultSet.elementAt(13);
        prevSQLHashValue = Long.valueOf(String.valueOf(resultSet.elementAt(14))).longValue();
        prevSQLAddress = resultSet.elementAt(15);
        if (ConsoleWindow.getDBVersion() >= 10.1) {
          blockingSidTF.setText(String.valueOf(resultSet.elementAt(16)));
          blockingStatusTF.setText(String.valueOf(resultSet.elementAt(17)));
          blockingInstanceTF.setText(String.valueOf(resultSet.elementAt(18)));
          prevSqlId = String.valueOf(resultSet.elementAt(19));
          if (!String.valueOf(resultSet.elementAt(20)).equals("null")) {
            sqlChildNum = Integer.valueOf(String.valueOf(resultSet.elementAt(20))).intValue();
            sqlChildNumTF.setText(String.valueOf(resultSet.elementAt(20)));
          }
          if (!String.valueOf(resultSet.elementAt(21)).equals("null")) prevChildNum = Integer.valueOf(String.valueOf(resultSet.elementAt(21))).intValue();
          
          if (ConsoleWindow.getDBVersion() > 11) sqlExecIdTF.setText(String.valueOf(resultSet.elementAt(22)));
        }
      }
      else {
        spidTF.setText("");
        sidTF.setText("");
        usernameTF.setText("");
        serialTF.setText("");
        logonTF.setText("");
        serverTF.setText("");
    //    sharedServerTF.setText("");
    //    dispatcherTF.setText("");
        statusTF.setText("");
        programTF.setText("");
        terminalTF.setText("");
        machineTF.setText("");
        if (ConsoleWindow.getDBVersion() >= 10.1) {
          blockingSidTF.setText("");
          blockingStatusTF.setText("");
          blockingInstanceTF.setText("");
          sqlIdB.setText("");
          sqlChildNumTF.setText("");
          if (ConsoleWindow.getDBVersion() > 11) sqlExecIdTF.setText("");
        }
      }

      // update status bar
      updateStatusBar(cursorId, myResult);

      /*
       * Spool output
       *
       * As wait events are the first thing to be spooled for each iteration, then output a new heading for each iteration here
       */
      if (currentlySpooling) {
        outputHTML.saveIteration(iterationCounter);
        outputHTML. saveSingleResult(myResult);
      }

      // save the output in case this session is killed
      lastIteration[0] = myResult;

      if (myResult.getNumRows() > 0) {
        sessionExists = true;
      }
      else {
        sessionExists = false;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sessionDetails()");
    }
  }

  void updateStatusBar(String cursorId, QueryResult myResult)
  {

    if (cursorId.length() >= 9 && cursorId.startsWith("sid")) {
      try {
        int sixdigits = Integer.valueOf(cursorId.substring(3,8)).intValue();
        // this file is prefixed from a session panel
        cursorId = cursorId.substring(9);
      }
      catch (NumberFormatException e) {
        // do nothing
      }
    }

    int ms = (int)myResult.getExecutionTime();
    int numRows = myResult.getNumRows();
    if (myResult.isFlipped()) numRows = 1;
  	String executionTime = myResult.getExecutionClockTime();
  	statusBarL.setText("Executed " + cursorId + " in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime);
  }


  /**
   * Display wait event details.
   */
  void waitEventDetails() {
    // create a cursor and execute the sql
    try {
      String cursorId = "";
      
      if (ConsoleWindow.getDBVersion() >= 11) {
        cursorId = "sessionWaitEvent11.sql";
      }
      else {
        cursorId = "sessionWaitEvent.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      // display the result
      if (myResult.getNumRows() > 0) {
        Vector resultSet = myResult.getResultSetRow(0);
        eventTF.setText(String.valueOf(resultSet.elementAt(0)));
//        p1rawTF.setText());
        p1TF.setText(String.valueOf(resultSet.elementAt(2)) + " - " + String.valueOf(resultSet.elementAt(1)));
//        p2rawTF.setText();
        p2TF.setText(String.valueOf(resultSet.elementAt(4)) + " - " + String.valueOf(resultSet.elementAt(3)));
//        p3rawTF.setText(String.valueOf(resultSet.elementAt(5)));
        p3TF.setText(String.valueOf(resultSet.elementAt(6)) + " - " + String.valueOf(resultSet.elementAt(5)));
        waitTimeTF.setText(String.valueOf(resultSet.elementAt(7)));
        stateTF.setText(String.valueOf(resultSet.elementAt(9)));
        secondsInWaitTF.setText(String.valueOf(resultSet.elementAt(8)));
      }
      else {
        clearWaitEventDetails();
      }

      // update status bar
      updateStatusBar(cursorId, myResult);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[1] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in waitEventDetails()");
    }
  }

  /**
   * Display lock details.
   */
  void lockDetails() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionLocks.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[4] = myResult;
      myResult = null;   // hopefully this will stop the lock details from ever appearing in the sql window when iterating - rw 19/10/09
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in lockDetails()");
    }
  }

  /**
   * Decode the lock types.
   */
  void lockDecoded() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionLocksDecoded.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[5] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in lockDecoded()");
    }
  }

  /**
   * List the sessions blocked by this session.
   */
  void blockedSessions() {
    // create a cursor and execute the sql
    try {
      String cursorId = "blockedSessions.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      // spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[6] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in blockedSessions()");
    }
  }

  /**
   * Show session tablespaceFile.
   */
  void sessionIO() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionIO.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[7] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sessionIO()");
    }
  }
  
  void sessionSmartScan() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionOffLoadEfficiency.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);      
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);      
      myPars.addParameter("int",sid);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,true,false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[7] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sessionOffLoadEfficiency()");
    }
  }
  
  void sessionEHCC() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionEHCC.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("int",sid);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,true,false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[7] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sessionOffLoadEfficiency()");
    }
  }
  
  private void cellOffLoadEfficiencyFromV$SQL() {
    try {
      String cursorId = "cellOffLoadEfficiencyFromV$SQL.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("String",sqlId);
      myPars.addParameter("int",sqlChildNum);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
      ExecuteDisplay.displayTable(myResult,offLoadSP,false,statusBarL);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[7] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"error in cellOffLoadEfficiencyFromV$SQL");
    }
  }

  /**
   * Show session events.
   */
  void sessionEvents() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionEvents.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);


      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[8] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sessionEvents()");
    }
  }

  /**
   * Show session wait history.
   */
   private void sessionWaitHistory() {
     // create a cursor and execute the sql
     try {
       String cursorId = "sessionWaitHistory.sql";
       Parameters myPars = new Parameters();
       myPars.addParameter("int",sid);
       myPars.addParameter("int",instanceNumber);
       QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
       ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);


       //  spool output
       if (currentlySpooling) outputHTML. saveSingleResult(myResult);

       // save the output in case this session is killed
       lastIteration[8] = myResult;
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this,"Error in sessionWaitHistory()");
     }
   }

  private void sqlBindCapture() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sqlBindVariables.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("String",sqlId);
      myPars.addParameter("int",sqlChildNum);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,ConsoleWindow.isFlipSingleRowOutput(),false,null);
      ExecuteDisplay.displayTable(myResult,southWestSP,false,statusBarL);


      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[8] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in sqlBindCapture()");
    }
  }

  /**
   * Display session undo.
   */
  void undo() {
    // create a cursor and execute the sql
    try {
      String cursorId = "undo2.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      // display the result
      if (myResult.getNumRows() > 0) {
        // display the result
        Vector resultSet = myResult.getResultSetRow(0);
        undoKBTF.setText(String.valueOf(resultSet.elementAt(0)));

      }
      else {
        clearUndo();
      }

      // update status bar
      updateStatusBar(cursorId,myResult);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[2] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in undo()");
    }
  }

  /**
   * Display session temporary segment usage.
   */
   void sort() {
     // create a cursor and execute the sql
     try {
       String cursorId = "sessionSort.sql";
       Parameters myPars = new Parameters();
       myPars.addParameter("int",sid);
       myPars.addParameter("int",instanceNumber);
       myPars.addParameter("int",instanceNumber);
       QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars, false, false, null);

       // display the result
       if (myResult.getNumRows() > 0) {
         Vector resultSet = myResult.getResultSetRow(0);
         sortKBTF.setText(String.valueOf(resultSet.elementAt(5)));
       }
       else {
         clearSort();
       }

       // update status bar
       updateStatusBar(cursorId, myResult);

       //  spool output
       if (currentlySpooling) outputHTML. saveSingleResult(myResult);

       // save the output in case this session is killed
       lastIteration[3] = myResult;
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this,"Error in Sort()");
     }
   }  
  
  void pga() {
    // create a cursor and execute the sql
    try {
      String cursorId = "sessionPGA.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",instanceNumber);
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars, false, false, null);

      // display the result
      if (myResult.getNumRows() > 0) {
        Vector resultSet = myResult.getResultSetRow(0);
        pgaKBTF.setText(String.valueOf(resultSet.elementAt(2)));

      }
      else {
        clearPGA();
      }

      // update status bar
      updateStatusBar(cursorId, myResult);

      //  spool output
      if (currentlySpooling) outputHTML. saveSingleResult(myResult);

      // save the output in case this session is killed
      lastIteration[3] = myResult;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in Sort()");
    }
  }


  /**
   * Display the execution plan for the sessions current sql statement.
   *
   * @param SQLResult
   */
  void explainSQL(QueryResult SQLResult) {
    try {
      if (ConsoleWindow.getDBVersion() >= 8.1) {
        if ((SQLResult.getNumRows() > 0) && (ConsoleWindow.getDBVersion() < 9 || (ConsoleWindow.getDBVersion() >= 9 && ConsoleWindow.isSqlPlan() == false))) {
          runExplainPlan(SQLResult);
        }
        else {
          try {
            if (ConsoleWindow.getDBVersion() >= 10) {
              queryExecutionPlan10();
            }
            else {
              queryExecutionPlan(SQLResult);
            }
          }
          catch (Exception e) {
            if (ConsoleWindow.getDBVersion() >= 9.2) runExplainPlan(SQLResult);
          }
        }
      }
      else {
        /* the execution plan could not be explained */
        Vector errMsg = new Vector(1);
        Vector lineOne = new Vector(1);
        Vector lineTwo = new Vector(1);
        Vector lineThree = new Vector(1);

        lineOne.add("This SQL statement could not be explained!");
        lineTwo.add(" ");
        lineThree.add("It is not possible to use execute immediate prior to 8.1");

        errMsg.add(lineOne);
        errMsg.add(lineTwo);
        errMsg.add(lineThree);

        tmpResult = new QueryResult();
        tmpResult.setResultSet(errMsg);
        tmpResult.setNumRows(3);
        tmpResult.setExecutionTime(Long.valueOf("0").longValue());
        int[] columnWidths = {80};
        tmpResult.setColumnWidths(columnWidths);
        tmpResult.setNumCols(1);
        String[] resultHeadings = {"Warning"};
        tmpResult.setResultHeadings(resultHeadings);
        String[] columnTypes = {"VARCHAR"};
        tmpResult.setColumnTypes(columnTypes);
        tmpResult.setSelectableColumns(resultHeadings);

        // display the error message
        displayPlanTable(tmpResult,"explainSQL()",Color.red);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error in explainSQL()");
    }
  }


  QueryResult currentSQL() {
    QueryResult myResult = new QueryResult();
    long totalExecTime = 0;


    /*
     * use the sql_address and sql_hash_value to get the sql statement from
     * v$sqltext _woth_newlines
    */

    Parameters myPars = new Parameters();
    myPars.addParameter("long",currentSQLHashValue);
    myPars.addParameter("byte[]",currentSQLAddress);
    myPars.addParameter("int",instanceNumber);

    if (currentSQLHashValue != 0) {
      try {
        current = true;
        String cursorId = "sessionSQL.sql";
        try {
          myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"execute sessionSQL.sql");
        }

        try {
          totalExecTime = totalExecTime + myResult.getExecutionTime();
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"calculating total exec time");
        }

        try {
          if (myResult.getNumRows() > 0) {
            QueryResult SQLResult = new QueryResult();
            try {
              if (ConsoleWindow.isEnableReFormatSQL()) {
                SQLResult = reFormatSQL(myResult);
              }
              else {
                SQLResult = formatSQL(myResult);
              }
            }
            catch (Exception e) {
              ConsoleWindow.displayError(e, this, "reformatting sql");
            }

            try {
              displaySQLTable(SQLResult, totalExecTime, Color.blue);
            }
            catch (Exception e) {
              //ConsoleWindow.displayError(e, this, "displaying current sql statement in table");
              /*
               * Do nothing in this section.  Otherwise it very occasionally fails with
               * a null pointer exception
               */
            }
          }
          else {
            if (sqlSP.getViewport().getComponentCount() > 0) {
              try {
                sqlSP.getViewport().removeAll();
              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this, "removing component from sqlSP (1st)");
              }
            }
          }
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"processing result");
          ConsoleWindow.displayError(e,this,"rows=" + myResult.getNumRows());
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"session hash value");
      }
    }
    else {

      /*
       * use the sql_address and sql_hash_value to get the sql statement from
       * v$sqltext _with_newlines
       */
      myPars = new Parameters();
      myPars.addParameter("long",prevSQLHashValue);
      myPars.addParameter("byte[]",prevSQLAddress);
      myPars.addParameter("int",instanceNumber);

      if (prevSQLHashValue != 0) {
        try {
          current = false;
          String cursorId = "sessionSQL.sql";
          try {
            myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"execute sessionSQL.sql for previous statement");
          }

          try {
            totalExecTime = totalExecTime + myResult.getExecutionTime();
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"calculating total exec time for previous statement");
          }

          try {
            if (myResult.getNumRows() > 0) {
              QueryResult SQLResult = new QueryResult();
              try {
                if (ConsoleWindow.isEnableReFormatSQL()) {
                  SQLResult = reFormatSQL(myResult);
                }
                else {
                  SQLResult = formatSQL(myResult);
                }
              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this, "reformatting sql for previous statement");
              }

              try {
                displaySQLTable(SQLResult, totalExecTime, Color.gray);
              }
              catch (Exception e) {
                //ConsoleWindow.displayError(e, this, "displaying previous sql statement in table");
                /*
                 * Do nothing in this section.  Otherwise it very occasionally fails with
                 * a null pointer exception
                 */
              }
            }
            else {
              if (sqlSP.getViewport().getComponentCount() > 0) {
                try {
                  sqlSP.getViewport().removeAll();
                }
                catch (Exception e) {
                  ConsoleWindow.displayError(e, this, "removing component from sqlSP (2nd)");
                }
              }
            }
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"processing previous result");
            ConsoleWindow.displayError(e,this,"rows=" + myResult.getNumRows());
          }
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"previous session hash value");
        }
      }
      else {
        if (sqlSP.getViewport().getComponentCount() > 0) {
          try
          {
            sqlSP.getViewport().removeAll();
          }
          catch (Exception e)
          {
            String errMsg = "Error removing 2nd component from sqlSP : " + e.toString();
            JOptionPane.showMessageDialog(this,errMsg,"Error...",JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }

    /* save the output in case this session is killed */
    lastIteration[9] = myResult;

    return myResult;
  }


  /**
   * Get the sessions current sql statement.
   *
   * @return myResult
   */
  private QueryResult currentSQL10() {
    QueryResult myResult = new QueryResult();
    long totalExecTime = 0;


    /*
     * use the sql_id to get the sql statement from
     * v$sqltext
    */
    Parameters myPars = new Parameters();
    myPars.addParameter("String",sqlId);
    myPars.addParameter("int",instanceNumber);

///    if (!sqlId.equals("null")) {
    if (sqlId instanceof String) {
      try {
        current = true;
        String cursorId = "sessionSQL10.sql";
        try {
          myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"execute sessionSQL.sql");
        }

        try {
          totalExecTime = totalExecTime + myResult.getExecutionTime();
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"calculating total exec time");
        }

        try {
          QueryResult SQLResult = new QueryResult();
          if (myResult.getNumRows() > 0) {
            try {
              if (ConsoleWindow.isEnableReFormatSQL()) {
                SQLResult = reFormatSQL(myResult);
              }
              else {
                SQLResult = formatSQL(myResult);
              }
            }
            catch (Exception e) {
              ConsoleWindow.displayError(e, this, "reformatting sql");
            }

            try {
              displaySQLTable(SQLResult, totalExecTime, Color.blue);
            }
            catch (Exception e) {
              //ConsoleWindow.displayError(e, this, "displaying current sql statement in table");
              /*
               * Do nothing in this section.  Otherwise it very occasionally fails with
               * a null pointer exception
               */
            }
          }
          else {
            if (sqlSP.getViewport().getComponentCount() > 0) {
              try {
                sqlSP.getViewport().removeAll();
                sqlSP.setBackground(Color.WHITE);
              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this, "removing component from sqlSP (1st)");
              }
            }
          }
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"processing result");
          ConsoleWindow.displayError(e,this,"rows=" + myResult.getNumRows());
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"session hash value");
      }
    }
    else {

      /*
       * use the prev_sql_id
       */
      myPars = new Parameters();
      myPars.addParameter("String",prevSqlId);
      myPars.addParameter("int",instanceNumber);

//      if (!prevSqlId.equals("null")) {
      if (prevSqlId instanceof String) {
        try {
          current = false;
          String cursorId = "sessionSQL10.sql";
          try {
            myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"execute sessionSQL.sql for previous statement");
          }

          try {
            totalExecTime = totalExecTime + myResult.getExecutionTime();
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"calculating total exec time for previous statement");
          }

          try {
            QueryResult SQLResult = new QueryResult();
            if (myResult.getNumRows() > 0) {
              try {
                if (ConsoleWindow.isEnableReFormatSQL()) {
                  SQLResult = reFormatSQL(myResult);
                }
                else {
                  SQLResult = formatSQL(myResult);
                }
              }
              catch (Exception e) {
                ConsoleWindow.displayError(e, this, "reformatting sql for previous statement");
              }

              try {
                displaySQLTable(SQLResult, totalExecTime, Color.gray);
              }
              catch (Exception e) {
                //ConsoleWindow.displayError(e, this, "displaying previous sql statement in table");
                /*
                 * Do nothing in this section.  Otherwise it very occasionally fails with
                 * a null pointer exception
                 */
              }
            }
            else {
              if (sqlSP.getViewport().getComponentCount() > 0) {
                try {
                  sqlSP.getViewport().removeAll();
                  sqlSP.setBackground(Color.WHITE);
                }
                catch (Exception e) {
                  ConsoleWindow.displayError(e, this, "removing component from sqlSP (2nd)");
                }
              }
            }
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"processing previous result");
            ConsoleWindow.displayError(e,this,"rows=" + myResult.getNumRows());
          }
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"prev_sql_id");
        }
      }
      else {
        if (sqlSP.getViewport().getComponentCount() > 0) {
          try
          {
            sqlSP.getViewport().removeAll();
            sqlSP.setBackground(Color.WHITE);
          }
          catch (Exception e)
          {
            String errMsg = "Error removing 2nd component from sqlSP : " + e.toString();
            JOptionPane.showMessageDialog(this,errMsg,"Error...",JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }

    /* save the output in case this session is killed */
    lastIteration[9] = myResult;

    return myResult;
  }

  /**
   * Query v$sql_plan to obtain an execution plan for the current sql statement.
   *
   * @param SQLResult
   * @throws java.lang.Exception
   */
  void queryExecutionPlan(QueryResult SQLResult) throws Exception {

    // set the current schema
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
    }

    // get the hashvalue for the current sql statement
    if (currentSQLHashValue != 0 && SQLResult.getNumRows() > 0) {

      Parameters myPars = new Parameters();
      myPars.addParameter("long",currentSQLHashValue);
      myPars.addParameter("byte[]",currentSQLAddress);
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("long",currentSQLHashValue);
      myPars.addParameter("byte[]",currentSQLAddress);
      myPars.addParameter("int",instanceNumber);
      myPars.addParameter("long",currentSQLHashValue);

      String cursorId = "executionPlanPost9.sql";
      Cursor myCursor = new Cursor(cursorId,true);
      boolean restrictRows = false;
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
      removeDuplicatePlan(myResult);
      if (myResult.getNumRows() > 0) displayPlanTable(myResult,"Plan extracted from V$SQL_PLAN using sql_hash_value and sql_addr taken from v$session",Color.BLUE);
      executionPlan = myResult;
      execPlanExists = true;
    }
    else {
      if (prevSQLHashValue != 0 && SQLResult.getNumRows() > 0) {
        Parameters myPars = new Parameters();
        myPars.addParameter("long",prevSQLHashValue);
        myPars.addParameter("byte[]",prevSQLAddress);
        myPars.addParameter("int",instanceNumber);
        myPars.addParameter("long",prevSQLHashValue);
        myPars.addParameter("byte[]",prevSQLAddress);
        myPars.addParameter("int",instanceNumber);
        myPars.addParameter("long",prevSQLHashValue);

        String cursorId = "executionPlanPost9.sql";
        Cursor myCursor = new Cursor(cursorId,true);
        boolean restrictRows = false;
        QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
        removeDuplicatePlan(myResult);
        if (myResult.getNumRows() > 0) displayPlanTable(myResult,"Plan extracted from V$SQL_PLAN using prev_hash_value and prev_sql_addr taken from v$session",Color.GRAY);
        executionPlan = myResult;
        execPlanExists = true;
      }
      else {
        if (planSP.getViewport().getComponentCount() > 0 ) planSP.getViewport().removeAll();
        execPlanExists = false;
      }
    }

    // alter session back to the original if it was changed above
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
    }
  }

  /**
   * Obtain an execution plan for the current sql statement.
   *
   * @param SQLResult
   * @throws java.lang.Exception
   */
  void queryExecutionPlan10() throws Exception {

    // set the current schema
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
    }

    // get the sql_id for the current sql statement
    QueryResult myResult = new QueryResult();
    if (ConsoleWindow.getDBVersion() >= 10) {
        if (!sqlId.equals("null")) {
          // get the plan
          Cursor myCursor = new Cursor("RichMonSession" + sid + "QueryPlan",false);
          myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_cursor('" + sqlId + "'," + sqlChildNum + ",'ALL'))\n");          
        
          myResult = ExecuteDisplay.execute(myCursor,false,false,null);
          if (myResult.getNumRows() > 0) displayPlanTable(myResult,"Plan produced from dbms_xplan.display_cursor using the sql_id and sql_child_number taken from v$session",Color.BLUE);
          executionPlan = myResult;
          execPlanExists = true;
        }
        else {
          if (!prevSqlId.equals("null")) {
          // get the plan
          Cursor myCursor = new Cursor("RichMonSession" + sid + "QueryPlan",false);
          myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display_cursor('" + prevSqlId + "'," + prevChildNum + ",'ALL'))\n");
          myResult = ExecuteDisplay.execute(myCursor,false,false,null);
          if (myResult.getNumRows() > 0) displayPlanTable(myResult,"Plan produced from dbms_xplan.display_cursor using the prev_sql_id and prev_child_number taken from v$session",Color.GRAY);
          executionPlan = myResult;
          execPlanExists = true;
        }
        else {
          if (planSP.getViewport().getComponentCount() > 0 ) planSP.getViewport().removeAll();
          execPlanExists = false;
        }
      }
    }

    // alter session back to the original if it was changed above
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
    }
  }


  /**
   * Run 'explain plan' for the current sql statement.
   *
   * @param SQLResult
   * @throws java.lang.Exception
   */
  void runExplainPlan(QueryResult SQLResult) throws Exception {
    /*
     * if requested switch current schema
     * Note:  The value of forceUserTF is set to the current schema name by RichMon
     *        to ensure that this switch always occurs
     */
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
    }

    // get the sqlstatement to explain from the resultSet
      String sqlStatement = "";
      StringBuffer tmpSQL = new StringBuffer("");

    try {
      int numCols = SQLResult.getNumCols();
      for (int i=0; i < SQLResult.getNumRows(); i++) {
        Vector tmpSQLRow = SQLResult.getResultSetRow(i);
        for (int j=0; j < numCols; j++) tmpSQL.append(tmpSQLRow.elementAt(j));
      }
      sqlStatement = tmpSQL.toString();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error getting the sql statement to explain from the resultSet");
    }

    // remove any trailing ';' or '/' from the sql
    try {
      sqlStatement.trim();
      int semiColon = sqlStatement.lastIndexOf(";");
      if (semiColon >= 0) {
        sqlStatement = sqlStatement.substring(0,semiColon);
      }

      int slash = sqlStatement.lastIndexOf("/");
      int lastCloseBacket = sqlStatement.lastIndexOf(")");
      if ((slash >= 0) && (slash > lastCloseBacket)&& (sqlStatement.charAt(slash -1) != '*')){
        sqlStatement = sqlStatement.substring(0,slash);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"removing trailing ; or / from the sqlStatment");
    }

    // generate random number to ensure the statement_id is unique
    Random rand = new Random();
    int offSet = rand.nextInt(Integer.MAX_VALUE);

    /*
     * check for the presence of a plan table, and if missing create it
     * only applicable for db versions earlier than 10i
     */
    if (ConsoleWindow.getDBVersion() < 10.0)
    {
      String cursorId = "doesPlanTableExist.sql";
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      boolean restrictRows = false;
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);

      if (myResult.getNumRows() == 0)
      {
        // alter session back to the original if it was changed above
        if (ConsoleWindow.getCurrentSchema().length() > 0) {
          ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
        }

        // build plan table
        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "createPlanTable102.sql";
        }
        else {
          if (ConsoleWindow.getDBVersion() >= 9.2) {
            cursorId = "createPlanTable92.sql";
          }
          else {
            cursorId = "createPlanTable.sql";
          }
        }
        Cursor planTableCursor = new Cursor(cursorId,true);
        int numRows = planTableCursor.executeUpdate(true);

        // put it back again
        if (ConsoleWindow.getCurrentSchema().length() > 0) {
          ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
        }
      }
    }

    // prepare the explain plan statement
    sqlStatement = "EXPLAIN PLAN SET STATEMENT_ID = 'RichMon" + offSet + "' FOR " + sqlStatement.trim();

    // replace each single quote with double quote
    /*
     * I have commented out this section because I changed the execute immediate section of richmon in 17.38
     * I'm leaving it here in case I ever change it back.
     *
     * In essence I no longer wrap the statement being executed with a begin and end and as such it no longer seems to
     * need this double single quote business
     */
 /*   tmpSQL = new StringBuffer(sqlStatement);
    StringBuffer finalSQL = new StringBuffer();

    try {
      for (int i = 0; i < sqlStatement.length(); i++) {
          if (tmpSQL.charAt(i) == '\'') finalSQL.append("'");
          finalSQL.append(tmpSQL.charAt(i));
      }
      sqlStatement = finalSQL.toString();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"replacing each single quote with a double quote");
    } */

    // execute the explain plan
    try
    {
      ConnectWindow.getDatabase().executeImmediate(sqlStatement,false,true);

      // alter session back to the original if it was changed above
      if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
      }

      String prefix;

      // get the execution plan
       QueryResult myResult;
      if (ConsoleWindow.getDBVersion() < 9.2 || (ConsoleWindow.getDBVersion() == 9.2 && (!ConnectWindow.isCursorSharingExact()))) {
        String cursorId = "executionPlan.sql";
        Parameters myPars = new Parameters();
        myPars.addParameter("String","RichMon"+ offSet);
        myPars.addParameter("String","RichMon"+ offSet);
        myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);

        prefix = "Plan generated using explain plan";
      }
      else {
        Cursor myCursor = new Cursor("RichMon" + sid + "explainplan" + offSet,false);
        myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display('PLAN_TABLE','RichMon" + offSet + "','ALL'))\n");
        myResult = ExecuteDisplay.execute(myCursor,false,false,null);
        prefix = "Plan generated using explain plan with dbms_xplan";
      }

      // save the output in case this session is killed
      lastIteration[11] = myResult;

      /*
       * remove the execution plan from the plan table if the db version < 10i
       *from 10i onwards this is not required as the plan table is implemented
       * as a global temporary table
       */
      if (ConsoleWindow.getDBVersion() < 10.0)
      {
        try {
          String cursorId = "removeExecutionPlan.sql";
          Parameters myPars = new Parameters();
          myPars.addParameter("String","RichMon"+ offSet);

          Cursor myCursor2 = new Cursor(cursorId,true);
          int numRows = myCursor2.executeUpdate(true,myPars);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"removing execution plan from Plan Table");
        }
      }

      // display the execution plan
      displayPlanTable(myResult,prefix,Color.red);
      executionPlan = myResult;
      execPlanExists = true;
    }
    catch (Exception e)
    {
      // the execution plan could not be explained
      Vector errMsg = new Vector(1);
      Vector lineOne = new Vector(1);
      Vector lineTwo = new Vector(1);
      Vector lineThree = new Vector(1);
      Vector lineFour = new Vector(1);
      Vector lineFive = new Vector(1);
      Vector lineSix = new Vector(1);
      Vector lineSeven = new Vector(1);

      lineOne.add("This SQL statement could not be explained!  It might be because:");
      lineTwo.add(" ");
      lineThree.add("a) You do not have privileges on the relevant objects.  Try using file => Alter session set current schema");
      lineFour.add("b) Try turning off file => enable advanced features => use v$sqlplan");
      lineFive.add("c) Check your plan table matches the definition in $OH/rdbms/admin/utlxplan ");
      lineSix.add(" ");
      lineSeven.add(e.toString());
      errMsg.add(lineOne);
      errMsg.add(lineTwo);
      errMsg.add(lineThree);
      errMsg.add(lineFour);
      errMsg.add(lineFive);
      errMsg.add(lineSix);
      errMsg.add(lineSeven);



      tmpResult = new QueryResult();
      tmpResult.setResultSet(errMsg);
      tmpResult.setNumRows(7);
      tmpResult.setExecutionTime(Long.valueOf("0").longValue());
      int[] columnWidths = {80};
      tmpResult.setColumnWidths(columnWidths);
      tmpResult.setNumCols(1);
      String[] resultHeadings = {"Warning"};
      tmpResult.setResultHeadings(resultHeadings);
      String[] columnTypes = {"VARCHAR"};
      tmpResult.setColumnTypes(columnTypes);
      tmpResult.setSelectableColumns(resultHeadings);

      // display the error message
      displayPlanTable(tmpResult,"",Color.red);
      execPlanExists = false;
      // alter session back to the original if it was changed above
      if (ConsoleWindow.getCurrentSchema().length() > 0) {
        ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
      }
    }
  }


  /**
   * Turns iterating on or off depending on the state of the startStopTB JToggleButton.
   */
  void startStopTB_actionPerformed() {
    if (startStopTB.isSelected()) {
      //  set numIterations to 2 - meaning keep iterating until told to stop
      numIterations=2;
      startStopTB.setText("Stop");

      // start iterating
      iterate();
    }
    else {
      // set numIterations to 0 - meaning stop after current iteration
      numIterations=0;
      startStopTB.setText("Start");
    }
  }



  /**
   * Display the current sql statement.
   *
   * @param myResult
   * @param executionTime
   * @param myColor
   * @throws java.lang.Exception
   */
  void displaySQLTable(QueryResult myResult,long executionTime,Color myColor) throws Exception {
    try {
      if (debug) {
        System.out.println("numSQLRows: " + myResult.getNumRows());
        System.out.println("CursorId:   " + myResult.getCursorId());
        String[][] o = myResult.getResultSetAsStringArray();
        for (int i=0; i < o.length; i++) System.out.println("sql: " + o[i][0]);
      }

      ExecuteDisplay.displayTextPane(myResult, sqlTP, null, myColor);
    }
    catch (Exception e) {
      //ConsoleWindow.displayError(e,this,"displayTextPane in displaySQLTable");
      /*
       * Do nothing in this section.  Otherwise it very occasionally fails with
       * a null pointer exception
       */
    }

/*    sqlTP.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseReleased(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    });    */

    // display JTable
    try {
        SwingUtilities.invokeLater(addSQLTP);
      //sqlSP.getViewport().add(sqlTP);
    }
    catch (Exception e) {
      /*
       * Do nothing in this section.  Otherwise it very occasionally fails with
       * a null pointer exception
       */
    }

    // update status bar
    try {
      int ms = (int)myResult.getExecutionTime();
      int numRows = myResult.getNumRows();
      if (myResult.isFlipped()) numRows = 1;
      statusBarL.setText("Extracted sql Statement in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Updating statusbar in displaySQLTable");
    }

    //  spool output
    if (currentlySpooling) outputHTML. saveSingleResult(myResult);
  }

    Runnable addSQLTP = new Runnable() {
         public void run() {
             sqlSP.getViewport().add(sqlTP);
         }
     };

    Runnable addPlanTP = new Runnable() {
         public void run() {
             planSP.getViewport().add(planTP);
         }
     };

  /**
   * Display the execution plan or the current sql statement.
   *
   * @param myResult
   * @throws java.lang.Exception
   */
  void displayPlanTable(QueryResult myResult,String prefix, Color myColor) throws Exception {
    ExecuteDisplay.displayTextPane(myResult,planTP,prefix,myColor);

    planTP.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseReleased(MouseEvent e) {
          rightClickPopup( e );
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    });

    // display JTable
    try {
      SwingUtilities.invokeLater(addPlanTP);
//      planSP.getViewport().add(planTP, null);
    } catch (Exception e) {
      /*
       * Very occasionally this line causes a null pointer exception and since I cannot see what the cause is, this
       * try catch blocks works around the problem.  Hopefully, on the next iteration, the problem has gone away.  Usually.
       */
    }

    // update status bar
    int ms = 0;
    try {
      ms = (int)myResult.getExecutionTime();
    }
    catch (Exception e) {
      ms = 0;
    }

    String cursorId = myResult.getCursorId();

    /*
     * update the statusBar, but if the current resultSet does not have a cursorId,
     * then it must contain a generated error message about no plan being available,
     * in which case we do not update the statusBar
     */
    if (cursorId instanceof String) updateStatusBar(cursorId, myResult);

    /* spool output */
    if (currentlySpooling) outputHTML. saveSingleResult(myResult);
  }

  /**
   * Perform a single iteration.
   */
  void iterateOnceB_actionPerformed() {
    // set numIterations to 1 to ensure a single iteration is performanced
    numIterations = 1;

    // iterate
    iterate();
  }

  /**
   * Prompt for a file name and create an outputHTML object to spool output.
   */
  void fileSave() {
    try
    {
      // construct the default file name using the instance name + current date and time
      StringBuffer currentDT = new StringBuffer();
      try {
        currentDT = new StringBuffer(ConnectWindow.getDatabase().getSYSDate());
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(this,e.toString(),"Error...",JOptionPane.ERROR_MESSAGE);
      }

      // construct a default file name
      String defaultFileName;
      if (ConnectWindow.isLinux()) {
        defaultFileName = ConnectWindow.getBaseDir() + "/Output/RichMon " + ConsoleWindow.getInstanceName(instanceNumber) + " " + "Sid " + sid + " " + currentDT.toString() +  ".html";
      }
      else {
        defaultFileName = ConnectWindow.getBaseDir() + "\\Output\\RichMon " + ConsoleWindow.getInstanceName(instanceNumber) + " " + "Sid " + sid + " " + currentDT.toString() +  ".html";
      }
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(new File(defaultFileName));

      // prompt the user to choose a file name
      int option = fileChooser.showSaveDialog(this);
      if (option == JFileChooser.APPROVE_OPTION)
      {
        saveFile = fileChooser.getSelectedFile();

        // force the user to use a new file name if the specified filename already exists
        while (saveFile.exists())
        {
          JOptionPane.showConfirmDialog(this,"File already exists","File Already Exists",JOptionPane.ERROR_MESSAGE);
          option = fileChooser.showOpenDialog(this);
          if (option == JFileChooser.APPROVE_OPTION)
          {
            saveFile = fileChooser.getSelectedFile();
          }
        }
        save = new BufferedWriter(new FileWriter(saveFile));
        saveFile.createNewFile();

        // create a process to format the output and write the file
        try {
          outputHTML = new OutputHTML(saveFile,save,"Session " + this.sid);
          /* start spooling */
          currentlySpooling = true;
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this);
        }
      }
    }
    catch (IOException e)
    {
      ConsoleWindow.displayError(e,this);
    }
  }

  /**
   * Turn spooling on or off.
   */
  void spoolTB_actionPerformed() {
    if (spoolTB.isSelected()) {
      fileSave();
    }
    else {
      fileClose();
    }
  }

  /**
   * Close the output file being used for spooled output.
   */
  public void fileClose() {
    try {
      currentlySpooling = false;
      save.close();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,e.toString(),"Error...",JOptionPane.ERROR_MESSAGE);
    }
  }

  /*
   * Passws the sql statement thru the sql formatter
   */
  QueryResult reFormatSQL(QueryResult myResult) {
    // convert myResult into a String
    StringBuffer sqlStatement = new StringBuffer();
    Vector tmp;
    for (int i=0; i < myResult.getNumRows(); i++) {
      tmp = myResult.getResultSetRow(i);
      sqlStatement.append(tmp.firstElement());
    }

    // format the sql statement
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

    String[] colTypes = {"VARCHAR2"};
    newResult.setColumnTypes(colTypes);

    newResult.getCursorId();

    String[] resultHeadings = new String[1];
    if (current) {
      resultHeadings[0] =  "CURRENT SQL Statement (Re-Formatted)";
    }
    else {
      resultHeadings[0] =  "PREVIOUS SQL Statement (Re-Formatted)";
    }
    newResult.setResultHeadings(resultHeadings);

    return newResult;
  }

  /*
   * Splites the sql statement into lines
   */
  QueryResult formatSQL(QueryResult myResult) {
    // convert myResult into a String
    StringBuffer sqlStatement = new StringBuffer();
    Vector tmp;
    for (int i=0; i < myResult.getNumRows(); i++) {
      tmp = myResult.getResultSetRow(i);
      sqlStatement.append(tmp.firstElement());
      sqlStatement.append("\n");
    }

    // split the sqlStatement into seperate lines
    StringBuffer line = new StringBuffer();
    String[] finalSQL = new String[1000];
    int numLines = 0;

    for (int i=0; i < sqlStatement.length(); i++) {
      if (sqlStatement.charAt(i) != '\n') {
        // append to the current line
        line.append(sqlStatement.charAt(i));
      }
      else {
        // add a line of sql
        finalSQL[numLines++] = line.toString();
        line = new StringBuffer();
      }
    }

    // add the final line
    finalSQL[numLines++] = line.toString();

    // create the new QueryResult
    QueryResult newResult = new QueryResult();

    // convert the String into a vector or vectors and add it to the QueryResult
    Vector tmpV = new Vector(1);
    for (int i=0; i < numLines; i++) {
      tmpV.add(finalSQL[i]);
      newResult.addResultRow(tmpV);
      tmpV = new Vector(1);
    }

    // update the QueryResult with the number of Rows
    newResult.setNumRows(numLines);

    // update the QueryResult with the column Widths
    int maxColWidth = 40;
    for (int i=0; i < numLines; i++) {
      maxColWidth = Math.max(maxColWidth,finalSQL[i].length());
    }

    int [] colWidths = new int[1];
    colWidths[0] = maxColWidth;
    newResult.setColumnWidths(colWidths);

    String[] colTypes = {"VARCHAR2"};
    newResult.setColumnTypes(colTypes);

    newResult.setCursorId(myResult.getCursorId());

    String[] resultHeadings = new String[1];
    if (current) {
      resultHeadings[0] =  "CURRENT SQL Statement";
    }
    else {
      resultHeadings[0] =  "PREVIOUS SQL Statement";
    }
    newResult.setResultHeadings(resultHeadings);

    return newResult;
  }

  void clearWaitEventDetails() {
    eventTF.setText("");
    p1TF.setText("");
    p2TF.setText("");
    p3TF.setText("");
    waitTimeTF.setText("");
    stateTF.setText("");
    secondsInWaitTF.setText("");
  }

  void clearSort() {
    sortKBTF.setText("");
  }  
  
  void clearPGA() {
    pgaKBTF.setText("");

  }

  void clearUndo() {
    undoKBTF.setText("");
  }

  String extractSQLStatement(QueryResult myResult) {
    String[][] sql = myResult.getResultSetAsStringArray();

    StringBuffer sqlStatement = new StringBuffer();
    for (int i=0; i < myResult.getNumRows(); i++) sqlStatement.append(sql[i][0]);

    return sqlStatement.toString();
  }

  void rightClickPopup(MouseEvent e)
    {
      if (e.isPopupTrigger())
      {
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }

  void removeDuplicatePlan(QueryResult myResult) {
    String[][] myResultSet = myResult.getResultSetAsStringArray();

    int match=-1;
    for (int i=1; i < myResultSet.length; i++) {
      if (myResultSet[i][0].equals(myResultSet[0][0])) {
        match=i;
        break;
      }
    }

    if (match > 0) {
      Vector newResultSet = new Vector(myResultSet.length - match -1);

      for (int i=0; i <= match; i++) newResultSet.add(myResultSet[i][0]);

      myResult.setResultSet(newResultSet);
      myResult.setNumRows(match);
    }
  }

  void ashB_actionPerformed() {
    ConsoleWindow.addAshPanel(sid,serial,instanceNumber);
  }


  public int getInst_id() {
    return instanceNumber;
  }

  public int getSid() {
    return sid;
  }
  
  private void sqlIdB_actionPerformed() {    
    /*
     * If you specify a sql execution id then you only get a sql monitor report for that execution id.  If you specify 0 (here) then it will list all the sql monitor reports it can find
     * 
     * Because SQL Detail Panels can be called from an AWR panel they require start and end snap id's and a fake string array for the date range.  These are ignored when not relevant.
     */
    String[] snapDateRange = new String[0];
    ConsoleWindow.addSQLDetailPanel(sqlIdB.getText().substring(sqlIdB.getText().indexOf(":") +1),SQLIdPanel.sqlIdTP,ConsoleWindow.getDatabaseId(),instanceNumber,Long.parseLong(sqlExecIdTF.getText()),0,0,snapDateRange);
    
    //ConsoleWindow.addSQLDetailPanel(sqlIdB.getText().substring(sqlIdB.getText().indexOf(":") +1),SQLIdPanel.sqlIdTP,ConsoleWindow.getDatabaseId(),instanceNumber,0);
  }
  

}