/*
 * PerformancePanel.java        12.15 05/01/04
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
 * 21/02/05 Richard Wright Reworked entire class further into line with Sun's own
 *                         coding conventions.  Created new classes that extent 
 *                         JButton and JComboBox and move execution into those 
 *                         classes.  Swithed comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 27/10/05 Richard Wright Made all the control buttons have a blue foreground.
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 01/02/07 Richard Wright Added the sql profile combo box
 * 03/01/08 Richard Wright Renamed 'cache buffer chains' to 'cache buffers chains'
 * 09/12/09 Richard Wright Added HANGANALYZE
 * 08/03/10 Richard Wright Use configurable standard button size
 * 10/05/10 Richard Wright Added dfs lock handle
 * 28/04/16 Richard Wright Added sqlTuningTasksB back in as it go removed in 17.79 somehow
 * 06/12/17 Richard Wright Added sqlPlanDirectivesB
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import oracle.jdeveloper.layout.VerticalFlowLayout;

/**
 * A panel to bring database performance related queries together in one place
 */
public class PerformancePanel extends JPanel {
  // status bar 
  private JLabel statusBarL = new JLabel();
  
  // panels 
  private JPanel allButtonsP = new JPanel();
  private JPanel performanceButtonP = new JPanel();
  private JPanel controlButtonP = new JPanel();
  private JPanel verticalBoxP = new JPanel();
  
  // performanceSP (scroll Pane) 
  private JScrollPane performanceSP = new JScrollPane();
  
  // create a new cache for holding query results 
  ResultCache resultCache = new ResultCache();

  // controlButtonP panel buttons 
  private BackB backB = new BackB("Back", performanceSP, statusBarL, resultCache);
  private ForwardB forwardB = new ForwardB("Forward", performanceSP, statusBarL, resultCache);
  private JToggleButton showSQLTB = new JToggleButton();
  private JButton refreshB = new JButton("Refresh");
  
  // databaseButtonP panel buttons 
  private EnqueueWaitsB enqueueWaitsB = new EnqueueWaitsB("Enqueue Wait", performanceSP, statusBarL, resultCache);
  private BufferBusyWaitsB bufferBusyWaitsB = new BufferBusyWaitsB("Buffer Busy Wait", performanceSP, statusBarL, resultCache);
  private LongOperationsB longOperationsB = new LongOperationsB("Long Operations", performanceSP, statusBarL, resultCache);
  private EnqCFContentionB enqCFContentionB = new EnqCFContentionB("enq: CF - contention", performanceSP, statusBarL, resultCache);
  private CursorPinSWaitonXB cursorPinSWaitonXB = new CursorPinSWaitonXB("Cursor: Pin S wait on X", performanceSP, statusBarL, resultCache);
  private LibraryCachePinB libraryCachePinB = new LibraryCachePinB("Library Cache Pin Wait", performanceSP, statusBarL, resultCache);
  private CacheBuffersChainsB cacheBufferChainsB = new CacheBuffersChainsB("Cache Buffers Chains", performanceSP, statusBarL, resultCache);
  private DFSLockHandleB dfsLockHandleB = new DFSLockHandleB("DFS Lock Handle", performanceSP, statusBarL, resultCache);
  private SQLMonitorB sqlMonitorB = new SQLMonitorB("SQL Monitor", performanceSP, statusBarL, resultCache);
  private HanganalyzeB hanganalyzeB = new HanganalyzeB();
  private SQLPlanDirectivesB sqlPlanDirectivesB = new SQLPlanDirectivesB("SQL Plan Directives", performanceSP, statusBarL, resultCache);
      
      
  
  // performanceButtonP combo boxs 
  String[] waitEvents = {"Wait Events","Wait Events Summary"};
  public WaitEventsB waitEventsB = new WaitEventsB(waitEvents, performanceSP, statusBarL, resultCache);  
  String[] latchs = {"Latch Contention","Latch Contention Detail","Latch Misses"};
  private LatchB latchB = new LatchB(latchs, performanceSP, statusBarL, resultCache);
//  String[] tasks = {"Create Tuning Task","Exec Tuning Task","Task Status","Task Progress","List Tuning Tasks","Drop Tuning Task","Show Task Result","Show Implementation Script","Accept SQL Profile","Drop SQL Profile","Enable SQL Profile","Disable SQL Profile"};
  String[] tasks = {"Create Tuning Task (from Cursor)","Create Tuning Task (from AWR)","Execute Tuning Task","Show Task Status","Show Status of Tasks Generated by RichMon","Show Task Progress","Show Progress of Tasks Generated by RichMon","List Tuning Tasks","List Tuning Tasks Generated by RichMon","Drop Tuning Task","Show Task Result","Show Implementation Script"};
  public SQLTuningTasksB sqlTuningTasksB = new SQLTuningTasksB(tasks, performanceSP, statusBarL, resultCache);
  String[] taskSets = {"Create Tuning Set","Load Tuning Set","Show Tuning Set Content","Delete Tuning Set",
                    "List Tuning Sets","Exec Tuning Set","Task Status",
                    "Task Progress","Show Set Result"};
//  private SQLProfileCB sqlTaskSetsCB = new SQLProfileCB(taskSets, performanceSP, statusBarL, resultCache);
  String[] planStability = {"Outlines"};
  public PlanStabilityB planStabilityB = new PlanStabilityB(planStability, performanceSP, statusBarL, resultCache);
  String[] topSessions = {"Top Sessions by CPU","Top Sessions by CPU (for sessions active in the last 30 minutes)","Top Sessions by Physical IO","Top Sessions by Physical IO (for sessions active in the last 30 minutes)","Top Sessions by Redo","Top Sessions by Redo (for sessions active in the last 30 minutes)"};
  private TopSessionsB topSessionsB = new TopSessionsB(topSessions, performanceSP, statusBarL, resultCache);
  String[] dfsLockHandle = {"Waiters","Blockers"};
  
  // misc 
  
  Dimension standardButtonSize;
  Dimension prefSize2 = new Dimension(100,85);
  Dimension prefSize3 = new Dimension(120,50);
  String lastButton = "";
  String lastAction = "";             // keeps track of the last action from the list of options for a button
  
  /**
   * Constructor.
   */
  public PerformancePanel() {   
    
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
    
    standardButtonSize = Properties.getStandardButtonDefaultSize();
    
    // layout 
    this.setLayout(new BorderLayout());

    // verticalBoxP Panel 
    refreshB.setPreferredSize(prefSize2);
    refreshB.setForeground((Color.BLUE));
    refreshB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          refreshB();
        }
      });
    verticalBoxP.setLayout(new VerticalFlowLayout());
    verticalBoxP.add(showSQLTB, null);
    verticalBoxP.add(refreshB, null);
   
    // controlButtonP panel 
    backB.setText("Back");
    backB.setForeground(Color.BLUE);
    backB.setPreferredSize(prefSize2);
    backB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          backB.actionPerformed();
        }
      });
      
    forwardB.setText("Forward");
    forwardB.setForeground(Color.BLUE);
    forwardB.setPreferredSize(prefSize2);
    forwardB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          forwardB.actionPerformed();
        }
      });
      
    
    showSQLTB.setText("Show SQL");
    showSQLTB.setForeground(Color.BLUE);
    showSQLTB.setPreferredSize(standardButtonSize);
    refreshB.setPreferredSize(standardButtonSize);
    controlButtonP.add(backB, null);
    controlButtonP.add(forwardB, null);
    controlButtonP.add(verticalBoxP, null);
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // performanceButtonP Panel 
    topSessionsB.setPreferredSize(standardButtonSize);
    topSessionsB.setMargin(new Insets(2, 2, 2, 2));
    topSessionsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "topSessionsB";
          topSessionsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    waitEventsB.setPreferredSize(standardButtonSize);
    waitEventsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "waitEventsB";
          waitEventsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    planStabilityB.setPreferredSize(standardButtonSize);
    planStabilityB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "planStabilityB";
          planStabilityB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    latchB.setPreferredSize(standardButtonSize);
    latchB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "latchB";
          latchB.actionPerformed(showSQLTB.isSelected());
        }
      });
    
    sqlTuningTasksB.setPreferredSize(standardButtonSize);
    sqlTuningTasksB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sqlTasksCB";
          sqlTuningTasksB.actionPerformed(showSQLTB.isSelected());
        }
      });    
      
/*    sqlTaskSetsCB.setPreferredSize(standardButtonSize);
    sqlTaskSetsCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sqlTaskSetsCB";
          sqlTaskSetsCB.actionPerformed(showSQLTB.isSelected());
        }
      }); 
*/
      
    enqueueWaitsB.setPreferredSize(standardButtonSize);
    enqueueWaitsB.setMargin(new Insets(2, 2, 2, 2));
    enqueueWaitsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "enqueueWaitsB";
          enqueueWaitsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    bufferBusyWaitsB.setPreferredSize(standardButtonSize);
    bufferBusyWaitsB.setMargin(new Insets(2, 2, 2, 2));
    bufferBusyWaitsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "bufferBusyWaitsB";
          bufferBusyWaitsB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    longOperationsB.setPreferredSize(standardButtonSize);
    longOperationsB.setMargin(new Insets(2, 2, 2, 2));
    longOperationsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "longOperationsB";
          longOperationsB.actionPerformed(showSQLTB.isSelected());
        }
      });    
    
    sqlMonitorB.setPreferredSize(standardButtonSize);
    sqlMonitorB.setMargin(new Insets(2, 2, 2, 2));
    sqlMonitorB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sqlMonitorB";
          sqlMonitorB.actionPerformed(showSQLTB.isSelected());
        }
      });
    
    enqCFContentionB.setPreferredSize(standardButtonSize);
    enqCFContentionB.setMargin(new Insets(2, 2, 2, 2));
    enqCFContentionB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "enqCFContentionB";
          enqCFContentionB.actionPerformed(showSQLTB.isSelected());
        }
      });
    
    cursorPinSWaitonXB.setPreferredSize(standardButtonSize);
    cursorPinSWaitonXB.setMargin(new Insets(2, 2, 2, 2));
    cursorPinSWaitonXB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "cursorPinSWaitonXB";
          cursorPinSWaitonXB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    libraryCachePinB.setPreferredSize(standardButtonSize);
    libraryCachePinB.setMargin(new Insets(2, 2, 2, 2));
    libraryCachePinB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "libraryCachePinB";
          libraryCachePinB.actionPerformed(showSQLTB.isSelected());
        }
      });
      
    cacheBufferChainsB.setPreferredSize(standardButtonSize);
    cacheBufferChainsB.setMargin(new Insets(2, 2, 2, 2));
    cacheBufferChainsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "cacheBufferChainsB";
          cacheBufferChainsB.actionPerformed(showSQLTB.isSelected());
        }
      });    
    
    hanganalyzeB.setPreferredSize(standardButtonSize);
    hanganalyzeB.setMargin(new Insets(2, 2, 2, 2));
    hanganalyzeB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "hanganalyzeB";
          hanganalyzeB.actionPerformed();
        }
      });    
    
    dfsLockHandleB.setPreferredSize(standardButtonSize);
    dfsLockHandleB.setMargin(new Insets(2, 2, 2, 2));
    dfsLockHandleB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "dfsLockHandleB";
          dfsLockHandleB.actionPerformed(showSQLTB.isSelected());
        }
      });    
    
    sqlPlanDirectivesB.setPreferredSize(standardButtonSize);
    sqlPlanDirectivesB.setMargin(new Insets(2, 2, 2, 2));
    sqlPlanDirectivesB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "sqlPlanDirectivesB";
          sqlPlanDirectivesB.actionPerformed(showSQLTB.isSelected());
        }
      });

    performanceButtonP.setLayout(new FlowLayout(0));
    performanceButtonP.add(topSessionsB);
    performanceButtonP.add(waitEventsB);
    performanceButtonP.add(enqueueWaitsB);
    performanceButtonP.add(bufferBusyWaitsB);
    performanceButtonP.add(libraryCachePinB);   
    performanceButtonP.add(cacheBufferChainsB);
    performanceButtonP.add(longOperationsB);
    performanceButtonP.add(latchB);
    performanceButtonP.add(planStabilityB);
    performanceButtonP.add(hanganalyzeB);
    performanceButtonP.add(dfsLockHandleB);
    performanceButtonP.add(cursorPinSWaitonXB);
    
//    performanceButtonP.add(enqCFContentionB);
    //performanceButtonP.add(sqlTasksCB);
    performanceButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    
    // statusBar 
    statusBarL.setText(" ");
        
    // construct performancePanel 
    allButtonsP.setLayout(new BorderLayout());
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(allButtonsP, BorderLayout.NORTH);
    this.add(performanceSP, BorderLayout.CENTER);
    allButtonsP.add(performanceButtonP, BorderLayout.CENTER);
    allButtonsP.add(controlButtonP, BorderLayout.WEST);  
    
    // set scrollpane background to white 
    performanceSP.getViewport().setBackground(Color.white);
    
    if (ConsoleWindow.getDBVersion() >= 10) { 
      performanceButtonP.add(enqCFContentionB);
      performanceButtonP.add(sqlTuningTasksB); 
    }    
    
    if (ConsoleWindow.getDBVersion() >= 11) { 
      performanceButtonP.add(sqlMonitorB);
    }
  
    if (ConsoleWindow.getDBVersion() >= 11) { 
      performanceButtonP.add(sqlPlanDirectivesB);
    }

  }

  /**
   * Get a pointer to the ResultCache object.
   * 
   * @return resultCache - pointer to the ResultCache object
   */
  public ResultCache getResultCache() {
    return resultCache;
  }
    
  /**
   * Re-run the last selected operation.
   */
  private void refreshB() {
    if (lastButton.equals("bufferBusyWaitsB")) bufferBusyWaitsB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("cacheBufferChainsB")) cacheBufferChainsB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("enqueueWaitsB")) enqueueWaitsB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("libraryCachePinB")) libraryCachePinB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("longOperationsB")) longOperationsB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("cursorPinSWaitonXB")) cursorPinSWaitonXB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("enqCFContentionB")) enqCFContentionB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("topSessionsB")) topSessionsB.listActionPerformed(showSQLTB.isSelected(),lastAction);
    if (lastButton.equals("waitEventsB")) waitEventsB.listActionPerformed(showSQLTB.isSelected(),lastAction);
    if (lastButton.equals("sqlTasksCB")) sqlTuningTasksB.listActionPerformed(showSQLTB.isSelected(),lastAction);
//    if (lastButton.equals("sqlTaskSetsCB")) sqlTaskSetsCB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("latchB")) latchB.listActionPerformed(showSQLTB.isSelected(),lastAction);
    if (lastButton.equals("planStabilityB")) planStabilityB.listActionPerformed(showSQLTB.isSelected(),lastAction);
    if (lastButton.equals("dfsLockHandleB")) dfsLockHandleB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("hanganalyzeB")) hanganalyzeB.actionPerformed();
    if (lastButton.equals("sqlMonitorB")) sqlMonitorB.actionPerformed(showSQLTB.isSelected());
    if (lastButton.equals("sqlPlanDirectivesB")) sqlPlanDirectivesB.actionPerformed(showSQLTB.isSelected());
  }
  
  public void addSQLProfileCB() {
  /* I'm not happy about the reliability of these yet, so commenting out so 14.07 can be deployed without it. */
    
    performanceButtonP.add(sqlTuningTasksB);
//    performanceButtonP.add(sqlTaskSetsCB);
  }
  
  /**
   * setLastOption - Stores the last option selected from the list of options for a button
   * 
   * @param action
   */
  public void setLastAction(String action) {
    lastAction = action;
  }
  
  public void disableSQLProfile(boolean enabled) {
    sqlTuningTasksB.setEnabled(enabled);
  }
}