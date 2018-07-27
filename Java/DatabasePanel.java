/*
 * DatabasePanel.java        13.05 05/01/04
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
 * Change History since 21/02/05
 * =============================
 * 
 * 21/02/05 Richard Wright Reworked entire class moving out into other classes 
 *                         the functionality for each button or combobox.                         
 * 22/02/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 03/06/05 Richard Wright Correct a spelling mistake in the definition of the 
 *                         options in the dbarep combobox
 * 03/06/05 Richard Wright Renamed sallyB button to sallySessionsB
 * 20/09/05 Richard Wright Added support for server generated alerts in 10g by 
 *                         incorporating the alertsCB
 * 20/09/05 Richard Wright Removed the 'list kept packages' button and associated 
 *                         package.  Otherwise there would be too many objects to 
 *                         display on a 15inch laptop screen.  This can be done in the 
 *                         scratch panel instead.
 * 20/09/05 Richard Wright Moved resource limit into a combobox with database summary.
 * 29/09/05 Richard Wright Added support for flashback.
 * 27/10/05 Richard Wright Changes the control buttons in size and color to make 
 *                         more room on the screen
 * 03/07/06 Richard Wright Make the jobCB public - called from SPPerformanceReviewB
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 02/07/08 Richard Wright Added a name to the database SP
 * 08/03/10 Richard Wright Use configurable standard button size
 * 20/05/10 Richard Wright Added the streams button
 * 16/12/15 Richard Wright remove sallySessiopns in its entirety
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;


/**
 * A JPanel to collect together all database (non-performance) related queries.
 */
public class DatabasePanel extends JPanel {
  // layouts 
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private FlowLayout flowLayout1 = new FlowLayout(0);

  // status bar 
  public JLabel statusBarL = new JLabel();
  
  // panels 
  private JPanel allButtonsP = new JPanel();
  public JPanel databaseButtonP = new JPanel();
  private JPanel controlButtonP = new JPanel();
  
  // create a new cache for holding query results 
  ResultCache resultCache = new ResultCache();
  
  // databaseSP (scroll Pane) 
  private JScrollPane databaseSP = new JScrollPane();

  // controlButtonP panel buttons 
  private BackB backB = new BackB("Back", databaseSP, statusBarL, resultCache);
  private ForwardB forwardB = new ForwardB("Forward", databaseSP, statusBarL, resultCache);
  public JToggleButton showSQLTB = new JToggleButton("Show SQL");
  private JButton refreshB = new JButton("Refresh");
  
  // databaseButtonP panel buttons 
  private SortB sortB = new SortB("Sort", databaseSP,  statusBarL, resultCache);
  public CreateScriptB createScriptB = new CreateScriptB("Create Script", databaseSP,  statusBarL, resultCache);
  
  // databaseButtonP combo boxs 
  String[] databaseSummary = {"Database Summary"};
  public DatabaseSummaryB databaseSummaryB = new DatabaseSummaryB(databaseSummary, databaseSP, statusBarL, resultCache);
  String[] streams = {"Apply Errors","Capture Status","Propagation Status"};
  private StreamsB streamsB = new StreamsB(streams, databaseSP, statusBarL, resultCache);
  String[] sharedPool = {"S/P Summary","S/P Freelists","S/P LRU Stats","R/P Summary","db_object_cache","v$librarycache","v$shared_pool_reserved"};
  public SharedPoolB sharedPoolB = new SharedPoolB(sharedPool, databaseSP,  statusBarL, resultCache);
  String[] parallelQuery = {"OPQ Summary","OPQ Detail","v$pq_sysstat","v$pq_slave"};
  public ParallelQueryB parallelQueryB = new ParallelQueryB(parallelQuery, databaseSP, statusBarL, resultCache);
  String[] redo = {"Redo Config","Redo History"};
  public RedoB redoB = new RedoB(redo, databaseSP, statusBarL, resultCache);
  String[] sharedServer = {"Dispatchers","Shared Servers"};
  public SharedServerB sharedServerB = new SharedServerB(sharedServer, databaseSP, statusBarL, resultCache);
  String[] lock = {"Lock Holder+Waiter","Lock Waiters","All Locks (Decoded)"};
  private LockB lockB = new LockB(lock, databaseSP, statusBarL, resultCache);
  String[] jobs = {"All Dba Jobs","Running Dba Jobs"};
  public JobsSchedulerB jobsSchedulerB = new JobsSchedulerB(jobs, databaseSP, statusBarL, resultCache);
  String[] sessions = {"Session Summary","Sessions","Session IO","Session Events","Rman Sessions"};
  public SessionsB sessionsB = new SessionsB(sessions, databaseSP, statusBarL, resultCache);
  String[] parallelServer = {"OPS Blocked Sessions","OPS Cache CR Perf","OPS Cache Lock Perf","OPS DLM Traffic","OPS Top Read Ping"
                 ,"OPS Top Write Ping","OPS V$BH"};
  String[] parameters = {"Non-Default Parameters","Parameters","_Parameters","SP Parameters","Inconsistent Parameters in RAC db"};
  public ParametersB parametersB = new ParametersB(parameters, databaseSP, statusBarL, resultCache);
  String[] distributedTrans = {"db links","dba_2pc_pending","dba_2pc_neighbors"};
  private DistributedTransB distributedTransB = new DistributedTransB(distributedTrans, databaseSP, statusBarL, resultCache);
  String[] space = {"Tablespace Freespace","Datafile Freespace","Temp File Freespace","Tablespace Definition","Datafile Definition","Temp File Definition"};
  public SpaceB spaceB = new SpaceB(space, databaseSP, statusBarL, resultCache);
  String[] whatsThis = {"What's This","Synonym"};
  private WhatsThisB whatsThisB = new WhatsThisB(whatsThis, databaseSP, statusBarL, resultCache);
  String[] statistics = {"Statistics Summary","Table Statistics","Partitioned Table Statistics","Index Statistics","Partitioned Index Statistics","System Statistics"};
  public StatisticsB statisticsB = new StatisticsB(statistics, databaseSP, statusBarL, resultCache);
  String[] advisory = {"DB Cache Advice","PGA Aggregate Target Advice","Shared Pool Advice"};
  public AdvisoryB advisoryB = new AdvisoryB(advisory, databaseSP, statusBarL, resultCache);
  String[] undo = {"Current Undo","Undo Segments"};
  public UndoB undoB = new UndoB(undo, databaseSP, statusBarL, resultCache);
  String[] flashFRA = {"Flashback Log","Flashback Stat","v$flash_recovery_area_usage","v$recovery_file_dest"};
  public FlashFRAB flashFRAB = new FlashFRAB(flashFRA, databaseSP, statusBarL, resultCache);
  String[] asm = {"v$asm_disk_stat","v$asm_diskgroup_stat","v$asm_client"};
  public ASMB asmB = new ASMB(asm, databaseSP, statusBarL, resultCache);
  String[] alerts = {"Outstanding Alerts","Alert History","Thresholds"};
  public AlertsB alertsB = new AlertsB(alerts, databaseSP, statusBarL, resultCache);

  // misc 
  Dimension standardButtonSize;     // the size of most buttons & comboboxes 
  Dimension controlButtonSize = new Dimension(65,25);    // the size of the forward & back buttons 
  Dimension controlButtonSize2 = new Dimension(65,50);    // the size of the forward & back buttons 
  Insets controlButtonInsets = new Insets(1,1,1,1);
  String lastButton = "";             // keeps track of the last selected button or combobox 
  static String lastAction = "";             // keeps track of the last action from the list of options for a button
  int databaseButtonPHeight = 100;    // height of the databaseButtonP Panel 
  String dbarepServiceName = "";      // service name from dbarep 
  String whatsThat = "%";             // used to record the entry used in 'whats this' 
  JPanel displayedChartP;
  static boolean chartDisplayed;
  
   
  /**
   * Constructor.
   */
  public DatabasePanel() {   
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */  
   private void jbInit() throws Exception {
    /*
     * check screen size and set the height of the databaseButtonP panel accordingly 
     * to ensure that all buttons fit into the available space 
     */
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    if (screenSize.getHeight() < 800) {
      databaseButtonPHeight = 159; 
    }
    else {
      databaseButtonPHeight = 127;
    }
    
    standardButtonSize = Properties.getStandardButtonDefaultSize();
    
    // set layout 
    this.setLayout(borderLayout1);

    // verticalBoxP Panel 
    refreshB.setPreferredSize(controlButtonSize);
    refreshB.setForeground(Color.BLUE);
    refreshB.setMargin(controlButtonInsets);
    refreshB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          refreshB();
        }
      });
         
    // controlButtonP panel 
    backB.setPreferredSize(controlButtonSize2);
    backB.setForeground(Color.BLUE);
    backB.setMargin(controlButtonInsets);
    backB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          backB.actionPerformed();
        }
      });
      
    forwardB.setPreferredSize(controlButtonSize2);
    forwardB.setForeground(Color.BLUE);
    forwardB.setMargin(controlButtonInsets);
    forwardB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          forwardB.actionPerformed();
        }
      });
      
    showSQLTB.setPreferredSize(controlButtonSize);
    showSQLTB.setForeground(Color.BLUE);
    showSQLTB.setMargin(controlButtonInsets);
    
    controlButtonP.setLayout(new GridBagLayout());
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    GridBagConstraints controlCons = new GridBagConstraints();
        
    controlCons.gridx=0;
    controlCons.gridy=0;
    controlCons.gridheight=3;
    controlCons.insets = new Insets(1,1,1,1);
    controlButtonP.add(backB, controlCons);
    controlCons.gridy=3;
    controlButtonP.add(forwardB, controlCons);
    controlCons.gridx=1;
    controlCons.gridy=0;
    controlCons.gridheight=2;
    controlCons.gridy=2;
    controlButtonP.add(showSQLTB, controlCons);
    controlCons.gridy=4;
    controlButtonP.add(refreshB, controlCons);
    

    // databaseButtonP Panel 
    databaseSummaryB.setPreferredSize(standardButtonSize);
    databaseSummaryB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "databaseSummaryB";
          databaseSummaryB.actionPerformed(showSQLTB.isSelected());
        }
      });
            
    parametersB.setPreferredSize(standardButtonSize);
    parametersB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "parametersB";
          parametersB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    distributedTransB.setPreferredSize(standardButtonSize);
    distributedTransB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "distributedTransB";
          distributedTransB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    sessionsB.setPreferredSize(standardButtonSize);
    sessionsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sessionsB";
          sessionsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    undoB.setPreferredSize(standardButtonSize);
    undoB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "undoB";
          undoB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    sortB.setPreferredSize(standardButtonSize);
    sortB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sortB";
          sortB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    spaceB.setPreferredSize(standardButtonSize);
    spaceB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spaceB";
          spaceB.actionPerformed(showSQLTB.isSelected());
        }
      });     
      
    whatsThisB.setPreferredSize(standardButtonSize);
    whatsThisB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "whatsThisB";
          whatsThisB.actionPerformed(showSQLTB.isSelected());
        }
      });    
      
    sharedPoolB.setPreferredSize(standardButtonSize);
    sharedPoolB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sharedPoolB";
          sharedPoolB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    parallelQueryB.setPreferredSize(standardButtonSize);
    parallelQueryB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "parallelQueryB";
          parallelQueryB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    redoB.setPreferredSize(standardButtonSize);
    redoB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "redoB";
          redoB.actionPerformed(showSQLTB.isSelected());
        }
      });    
      
    sharedServerB.setPreferredSize(standardButtonSize);
    sharedServerB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sharedServerB";
          sharedServerB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    lockB.setPreferredSize(standardButtonSize);
    lockB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "lockB";
          lockB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    jobsSchedulerB.setPreferredSize(standardButtonSize);
    jobsSchedulerB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "jobsB";
          jobsSchedulerB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    createScriptB.setPreferredSize(standardButtonSize);
    createScriptB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "createScriptB";
          createScriptB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    statisticsB.setPreferredSize(standardButtonSize);
    statisticsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "statisticsB";
          statisticsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    advisoryB.setPreferredSize(standardButtonSize);
    advisoryB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "advisoryB";
          advisoryB.actionPerformed(showSQLTB.isSelected());
        }
      });    
      
    alertsB.setPreferredSize(standardButtonSize);
    alertsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "alertsB";
          alertsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    flashFRAB.setPreferredSize(standardButtonSize);
    flashFRAB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "flashFRAB";
          flashFRAB.actionPerformed(showSQLTB.isSelected());
        }
      });  
    
    asmB.setPreferredSize(standardButtonSize);
    asmB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "asmB";
          asmB.actionPerformed(showSQLTB.isSelected());
        }
      });

    streamsB.setPreferredSize(standardButtonSize);
    streamsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "streamsB";
          streamsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    databaseButtonP.setPreferredSize(new Dimension(200,databaseButtonPHeight));
    databaseButtonP.setLayout(flowLayout1);
    databaseButtonP.add(databaseSummaryB, null);
    databaseButtonP.add(sessionsB, null);
    databaseButtonP.add(parametersB, null);
    databaseButtonP.add(undoB, null);
    databaseButtonP.add(sortB, null);
    databaseButtonP.add(whatsThisB, null);
    databaseButtonP.add(sharedPoolB, null);
    databaseButtonP.add(parallelQueryB, null);
    databaseButtonP.add(redoB, null);
    databaseButtonP.add(sharedServerB, null);
    databaseButtonP.add(lockB, null);
    databaseButtonP.add(jobsSchedulerB, null);
    databaseButtonP.add(distributedTransB, null);
    databaseButtonP.add(spaceB, null);
    databaseButtonP.add(statisticsB, null);
    databaseButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    
    // statusBar 
    statusBarL.setText(" ");
        
    
    
    // construct databasePanel 
    allButtonsP.setLayout(borderLayout2);
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(allButtonsP, BorderLayout.NORTH);
    this.add(databaseSP, BorderLayout.CENTER);
    allButtonsP.add(databaseButtonP, BorderLayout.CENTER);
    allButtonsP.add(controlButtonP, BorderLayout.WEST);  
    
    // set scrollpane background to white 
    databaseSP.getViewport().setBackground(Color.white);
    databaseSP.getViewport().setName("database");
    
    if (ConsoleWindow.getDBVersion() >= 9) {
      databaseButtonP.add(createScriptB, null);
      databaseButtonP.add(advisoryB, null);
    }
    
    if (ConsoleWindow.getDBVersion() >= 10) {
      databaseButtonP.add(alertsB, null);
      databaseButtonP.add(flashFRAB,null);
      databaseButtonP.add(asmB,null);
      databaseButtonP.add(streamsB,null);
    }
  }
    
  /**
   * Get the resultCache.
   * 
   * @return resultCache
   */
  public ResultCache getResultCache() {
    return resultCache;
  }
  
  /**
   * Re-run the last JButton or JComboBox selected.
   */
  private void refreshB() {
    if (lastButton.equals("distributedTransB")) distributedTransB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("jobsB")) jobsSchedulerB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("lockB")) lockB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("sharedServerB")) sharedServerB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("parallelQueryB")) parallelQueryB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("parametersB")) parametersB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("undoB")) undoB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("redoB")) redoB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("sessionsB")) sessionsB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("sharedPoolB")) sharedPoolB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("sortB")) sortB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("spaceB")) spaceB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("databaseSummaryB")) databaseSummaryB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("whatsThisB")) whatsThisB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("createScriptB")) createScriptB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("statisticsB")) statisticsB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("alertsB")) alertsB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("advisoryB")) advisoryB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("flashFRAB")) flashFRAB.listActionPerformed(showSQLTB.isSelected(), lastAction);
    if (lastButton.equals("streamsB")) streamsB.listActionPerformed(showSQLTB.isSelected(), lastAction);    
  }
  
  /**
   * setLastOption - Stores the last option selected from the list of options for a button
   * 
   * @param action
   */
  public static void setLastAction(String action) {
    lastAction = action;
  }
  
  public void displayChartPanel(JPanel chartP) {
    if (!chartDisplayed) this.remove(databaseSP);
    if (chartDisplayed) this.remove(displayedChartP);
    this.add(chartP,BorderLayout.CENTER);
    displayedChartP = chartP;
    chartDisplayed = true;
    this.updateUI();
  }
  
  public void addScrollP() {
    if (chartDisplayed) {
      this.remove(displayedChartP);
      displayedChartP = null;
      chartDisplayed = false;
      this.add(databaseSP,BorderLayout.CENTER);
      this.updateUI();
    }
  }
  
  public static boolean isChartDisplayed() {
    return chartDisplayed;
  }
  

  
  public void disableAlerts(boolean enabled) {
    alertsB.setEnabled(enabled);

  }
}
