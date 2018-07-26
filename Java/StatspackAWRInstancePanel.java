/*
 * StatspackAWRPanel.java        13.00 13/06/05
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
 * Change History since 21/02/05
 * =============================
 * 
 * 31/01/06 Richard Wright Added the performance review button
 * 14/06/06 Richard Wright Removed all references 'String parallel;'
 * 22/06/06 Richard Wright Added a method to remove the performance review button
 * 27/06/06 Richard Wright Added IO, Latch and Undo CB's recently
 * 27/06/06 Richard Wright Added BufferPoolCB
 * 13/07/06 Richard Wright Added the snapshot selection period buttons
 * 13/07/06 Richard Wright The height of the databaseButtonP now increasing on 
 *                         laptop screens to help ensure all buttons display propery
 * 14/09/06 Richard Wright Modified the comment style and error handling
 * 08/01/07 richard Wright 'Select n' buttons now select n snapshots, not n hours
 * 24/01/07 Richard Wright Added top sql by executions
 * 31/01/07 Richard Wright Added awrrpt button for 10g AWR
 * 19/03/07 Richard Wright Added the buttons to move the snapshot window & refresh chart
 * 21/05/07 Richard Wright Converted SPPGAStatsCB to a JComboBox to accomodate pga advisory
 * 22/05/07 Richard Wright Added chartB
 * 10/09/07 Richard Wright Fixed bug setting to start snapshot to negative numbers
 * 10/09/07 Richard Wright Implemented a refresh method and storing last action
 * 17/09/07 Richard Wright Fixed problem with the perf review button not removing porperly via menu
 * 05/12/07 Richard Wright Enhanced for RAC
 * 01/05/07 Richard Wright Added event histogram as part of the wait events rather than a seperate button
 * 02/07/08 Ricgard Wright Added a name to the statspackSP and methods to ensure table headers are not displayed with charts
 * 10/12/08 Richard Wright Added the displayChart method
 * 22/09/09 Richard Wright Increased the snapshot comboboxs to display 25 rows each
 * 29/10/09 Richard Wright All buttons etc are now disabled until after snapshots have been collected
 * 08/03/10 Richard Wright Use configurable standard button size
 * 29/03/10 Richard Wright The lastn buttons and Moven buttons can now be modified
 * 20/05/10 Richard Wright Fixed the size of databaseButtonP so it does not change according to screen size
 * 20/05/10 Richard Wright Added the streams button
 * 20/03/12 Richard Wright Clear the status bar when a chart is displayed
 * 22/05/12 Richard Wright Added keepEventColours to improve control over when the colours in the 
 *                         wait events chart are kept or refreshed.
 * 06/07/12 Richard Wright Added the IOPS Chart, renamed Load button to Other Charts
 * 10/07/12 Richard Wright Add the performance review button if enabled in config file
 * 22/07/12 Richard Wright Added segment growth and segment IO
 * 29/08/13 Richard Wright Added specific system IO charts when parameter 'pete=true' is specified
 * 02/04/15 Richard Wright Modified the order of Top SQL options to appear more logical and include charts
 * 29/05/15 Richard Wright Added the findFTS and ASH buttons with all their options
 * 28/11/17 Richard Wright Ensure ASH and Find FTS buttons only appear when using AWR not statspack
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import oracle.jdeveloper.layout.VerticalFlowLayout;

import org.jfree.chart.ChartPanel;

public class StatspackAWRInstancePanel extends JPanel  {
  // status bar 
  public JLabel statusBarL = new JLabel();
  
  // panels 
  private JPanel allButtonsP = new JPanel();
  public JPanel databaseButtonP = new JPanel();
  private JPanel controlButtonP = new JPanel();
  private JPanel controlButtonLP = new JPanel();
  private JPanel controlButtonLLP = new JPanel();
  private JPanel controlButtonLRP = new JPanel();
  private JPanel controlButtonRP = new JPanel();
  
  // scrollPanes 
  private JScrollPane statspackSP = new JScrollPane();
  
  // controlButtonP 
  GetSnapshotsB getSnapshotsB = new GetSnapshotsB("Get Snapshots",this,controlButtonP);
  JComboBox startSnapshotCB = new JComboBox();
  JComboBox endSnapshotCB = new JComboBox();
  public SelectLastnSnapshotsB selectSnapshotsLast1;
  public SelectLastnSnapshotsB selectSnapshotsLast72;
  public SelectLastnSnapshotsB selectSnapshotsLast24;
  public SelectLastnSnapshotsB selectSnapshotsLast120;
  public MovenSnapshotsB moveBackMinus12Snapshots;
  public MovenSnapshotsB moveBackMinus24Snapshots;
  public MovenSnapshotsB moveBackPlus12Snapshots;
  public MovenSnapshotsB moveBackPlus24Snapshots;
  
  // databaseButtonP 
  String[] waitEvents = {"F/G Wait Events", "F/G Adj Wait Events","F/G Wait Events (Text)","CPU Time Breakdown"};
  public SPWaitEventsB spWaitEventsB = new SPWaitEventsB(this, waitEvents, statspackSP, statusBarL);
  String[] globalCache = {"Global Cache Efficiency", "Global Cache Load Profile","Global Cache and Enqueue Services - Workload Characteristics","Global Cache and Enqueue Services - Messaging Statistics"};
  public SPGlobalCacheB spGlobalCacheB = new SPGlobalCacheB(this, globalCache, statspackSP, statusBarL);
  SPStatisticsB spStatisticsB = new SPStatisticsB(this,"Custom Statistics",statspackSP,statusBarL);
  String[] pga = {"PGA Stats Chart"};
  public SPPGAStatsB spPGAStatsB = new SPPGAStatsB(this,pga,statspackSP,statusBarL);
  public SPAWRRPTB spAWRRPTB = new SPAWRRPTB(this,"AWR RPT",statspackSP,statusBarL);
  public SPADDMB spADDMB = new SPADDMB(this,"ADDM RPT",statspackSP,statusBarL);
  public SPFindFTSB spFindFTSB = new SPFindFTSB(this,"Find FTS", statspackSP, statusBarL);
  public String[] topSQL = {"Top SQL by Buffer Gets", "Top SQL by Physical Reads","Top SQL by Executions","Top SQL by Parse Calls","Top SQL by Version Count"};
  SPTopSQLB spTopSQLB = new SPTopSQLB(this,topSQL,statspackSP,statusBarL);
  public String[] ash = {"Detail for a single SQL_ID", "Summary for a single SQL_ID","Summary Chart for a single SQL_ID","Detail for a single Event","Summary for a single Event","Summary Chart for a single Event"};
  SPASH spASHB = new SPASH(this,ash,statspackSP,statusBarL);
  SPLibraryCacheB spLibraryCacheB = new SPLibraryCacheB(this,"Library Cache", statspackSP, statusBarL);
  public SPPerformanceReviewB performanceReviewB = new SPPerformanceReviewB(this,"Perf Review", statspackSP, statusBarL);
  String[] other = {"Calls","Parse","Physical I/O","Commits / Rollbacks"};
  SPOtherChartsB spOtherChartsB = new SPOtherChartsB(this,other,statspackSP,statusBarL);
  SPResourceLimitB spResourceLimitB = new SPResourceLimitB(this,"Resource Limit", statspackSP, statusBarL);
  SPOsLoadB spOsLoadB = new SPOsLoadB(this,"OS Load", statspackSP, statusBarL);
  SPEnqueueB spEnqueueB = new SPEnqueueB(this,"Enqueue", statspackSP, statusBarL);
  SPReloadInvalidationsB spReloadInvalidationsB = new SPReloadInvalidationsB(this,"Reload/Invalidations", statspackSP, statusBarL);
  String[] undo = {"Undo Summary", "Undo Detail"};
  SPUndoB spUndoB = new SPUndoB(this, undo, statspackSP, statusBarL);
  String[] tablespaceFile = {"Tablespace IO", "File IO"};
  public SPTablespaceFileB spTablespaceFileB = new SPTablespaceFileB(this, tablespaceFile, statspackSP, statusBarL);
  String[] latch = {"Latch Summary", "Latch Sleeps","Latch Misses"};
  public SPLatchB spLatchB = new SPLatchB(this, latch, statspackSP, statusBarL);
  String[] summary = {"Summary","Load Profile","Time Model Statistics"};
  SPSummaryB spSummaryB = new SPSummaryB(this,summary,statspackSP,statusBarL);
  SPLogSwitchB spLogSwitchB = new SPLogSwitchB(this,"Log Switches", statspackSP, statusBarL);
  String[] bufferCache = {"Buffer Cache Summary","Buffer Cache Chart","Buffer Wait Statistics"};
  public SPBufferCacheB spBufferCacheB = new SPBufferCacheB(this,bufferCache, statspackSP, statusBarL);
  String[] segmentWaits = {"Segment by Logical Reads","Segment by Physical Reads","Segment by Row Lock Waits","Segment by ITL Waits","Segment by Buffer Busy Waits"};
  public SPSegmentWaitsB spSegmentWaitsB = new SPSegmentWaitsB(this,segmentWaits, statspackSP, statusBarL);
  String[] streams = {"Capture","Capture Lag Chart"};
  private SPStreamsB spStreamsB = new SPStreamsB(this,streams, statspackSP, statusBarL);
  String[] opqOperations = {"OPQ Operations and Downgrades","OPQ Operations","OPQ Downgrades"};
  public SPOPQB spOPQB = new SPOPQB(this,opqOperations, statspackSP, statusBarL);

  Dimension standardButtonSize;
//  Dimension prefSize2 = new Dimension(90,25);    
  Dimension prefSize3 = new Dimension(165,25);    
  private long dbId;
  private int instanceNumber;
  private String instanceName;
  String lastButton;
  String lastAction;
  boolean debug = false;
  boolean imported = false;
  
  static boolean chartDisplayed = false;
  JPanel displayedChartP;
  
  boolean keepEventColours = false;
  static JLabel selectedInstanceNameL = new JLabel();

  public StatspackAWRInstancePanel(int instanceNumber,String instanceName,long dbId, boolean imported) {
    this.instanceNumber = instanceNumber;
    this.instanceName = instanceName;
    this.dbId = dbId;
    this.imported = imported;
    
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
   
    // controlButtonsP 
    startSnapshotCB.setForeground(Color.BLUE);
    startSnapshotCB.setMaximumRowCount(25);
    endSnapshotCB.setForeground(Color.BLUE);
    endSnapshotCB.setMaximumRowCount(25);
    startSnapshotCB.setSize(prefSize3);
    endSnapshotCB.setSize(prefSize3);

    
    controlButtonP.setLayout(new BorderLayout());
    controlButtonLP.setLayout(new BorderLayout());
    controlButtonLLP.setLayout(new VerticalFlowLayout());
    controlButtonLRP.setLayout(new VerticalFlowLayout());
    controlButtonRP.setLayout(new VerticalFlowLayout());
    controlButtonLP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    controlButtonRP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    controlButtonLLP.add(getSnapshotsB);
    controlButtonLLP.add(startSnapshotCB);
    controlButtonLLP.add(endSnapshotCB);
    controlButtonLLP.add(selectedInstanceNameL);

    String moveN = Properties.getAWRMovenButtonValues();  
    moveBackPlus24Snapshots = new MovenSnapshotsB(">>" + moveN.substring(2, moveN.indexOf(",")),this,Integer.valueOf(moveN.substring(2, moveN.indexOf(","))).intValue());
    moveN = moveN.substring(Integer.valueOf(moveN.indexOf(",") +1));
    moveBackPlus12Snapshots = new MovenSnapshotsB(">>" + moveN.substring(2, moveN.indexOf(",")),this,Integer.valueOf(moveN.substring(2, moveN.indexOf(","))).intValue());
    moveN = moveN.substring(Integer.valueOf(moveN.indexOf(",") +1));
    moveBackMinus24Snapshots = new MovenSnapshotsB("<<" + moveN.substring(2, moveN.indexOf(",")),this,Integer.valueOf(moveN.substring(2, moveN.indexOf(","))).intValue() - (2*Integer.valueOf(moveN.substring(2, moveN.indexOf(","))).intValue()));
    moveN = moveN.substring(Integer.valueOf(moveN.indexOf(",") +1));
    moveBackMinus12Snapshots = new MovenSnapshotsB("<<" + moveN.substring(2),this,Integer.valueOf(moveN.substring(2)).intValue() - (2*Integer.valueOf(moveN.substring(2)).intValue()));
    
    controlButtonLRP.add(moveBackMinus12Snapshots);
    controlButtonLRP.add(moveBackMinus24Snapshots);
    controlButtonLRP.add(moveBackPlus12Snapshots);
    controlButtonLRP.add(moveBackPlus24Snapshots);
    
    controlButtonLP.add(controlButtonLLP, BorderLayout.WEST);
    controlButtonLP.add(controlButtonLRP, BorderLayout.EAST);
    
    String lastN = Properties.getAWRLastnButtonValues();
    selectSnapshotsLast1 = new SelectLastnSnapshotsB("Select Last " + lastN.substring(0, lastN.indexOf(",")),this,Integer.valueOf(lastN.substring(0, lastN.indexOf(","))).intValue());
    lastN = lastN.substring(lastN.indexOf(",") +1);
    selectSnapshotsLast24 = new SelectLastnSnapshotsB("Select Last " + lastN.substring(0, lastN.indexOf(",")),this,Integer.valueOf(lastN.substring(0, lastN.indexOf(","))).intValue());
    lastN = lastN.substring(lastN.indexOf(",") +1);
    selectSnapshotsLast72 = new SelectLastnSnapshotsB("Select Last " + lastN.substring(0, lastN.indexOf(",")),this,Integer.valueOf(lastN.substring(0, lastN.indexOf(","))).intValue());
    lastN = lastN.substring(lastN.indexOf(",") +1);
    selectSnapshotsLast120 = new SelectLastnSnapshotsB("Select Last " + lastN.substring(0),this,Integer.valueOf(lastN.substring(0)).intValue());
    
    controlButtonRP.add(selectSnapshotsLast120);
    controlButtonRP.add(selectSnapshotsLast72);
    controlButtonRP.add(selectSnapshotsLast24);
    controlButtonRP.add(selectSnapshotsLast1);
    
    controlButtonP.add(controlButtonLP, BorderLayout.WEST);
    controlButtonP.add(controlButtonRP, BorderLayout.EAST);
    
    selectSnapshotsLast1.setForeground(Color.BLUE);
    selectSnapshotsLast72.setForeground(Color.BLUE);
    selectSnapshotsLast24.setForeground(Color.BLUE);
    selectSnapshotsLast120.setForeground(Color.BLUE);
    moveBackMinus12Snapshots.setForeground(Color.BLUE);
    moveBackMinus24Snapshots.setForeground(Color.BLUE);
    moveBackPlus12Snapshots.setForeground(Color.BLUE);
    moveBackPlus24Snapshots.setForeground(Color.BLUE);
    
    // databaseButtonP 
    databaseButtonP.setLayout(new FlowLayout(0));
    databaseButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    databaseButtonP.add(spSummaryB);
    databaseButtonP.add(spWaitEventsB);
    databaseButtonP.add(spStatisticsB);
    databaseButtonP.add(spTopSQLB);
    databaseButtonP.add(spLibraryCacheB);
    databaseButtonP.add(spOtherChartsB);
    databaseButtonP.add(spReloadInvalidationsB);
    databaseButtonP.add(spLatchB);
    databaseButtonP.add(spTablespaceFileB);
    databaseButtonP.add(spBufferCacheB);
    databaseButtonP.add(spUndoB);
    databaseButtonP.add(spEnqueueB);

    if (ConsoleWindow.getDBVersion() > 9) {
      addPGAStatsB();
      addResourceLimitB();
      addSegmentWaitCB();
      spBufferCacheB.addItem("Buffer Cache Advisory");
      spPGAStatsB.addItem("PGA Advisory");
    }

    if (ConsoleWindow.getDBVersion() > 10) {
      addLogSwitchB();
      SPTopSQLB.addItem("Top SQL by CPU");
      SPTopSQLB.addItem("Top SQL by Elapsed Time");
      SPTopSQLB.addItem("Top SQL by Average PQ Slaves");
      if (ConsoleWindow.isDbRac()) SPTopSQLB.addItem("Top SQL by Cluster Wait Time");   
      if (ConsoleWindow.getDBVersion() >= 11.2 && ConsoleWindow.isDbRac()) SPTopSQLB.addItem("Top SQL IO Saved % (Smart Scan)");
      SPTopSQLB.addItem("Top SQL by Buffer Gets Chart");
      SPTopSQLB.addItem("Top SQL by Physical Reads Chart");
      SPTopSQLB.addItem("Top SQL by Executions Chart");
      SPTopSQLB.addItem("Top SQL by Parse Calls Chart");
      SPTopSQLB.addItem("Top SQL by Version Count Chart");      
      SPTopSQLB.addItem("Top SQL by CPU Chart");
      SPTopSQLB.addItem("Top SQL by Elapsed Time Chart");
      SPTopSQLB.addItem("Top SQL by Average PQ Slaves Chart");
      if (ConsoleWindow.isDbRac()) SPTopSQLB.addItem("Top SQL by Cluster Wait Time Chart");
      if (ConsoleWindow.getDBVersion() >= 11.2 && ConsoleWindow.isDbRac()) SPTopSQLB.addItem("Top SQL IO Saved % (Smart Scan) Chart");
      
    }    
    
    if (ConsoleWindow.getDBVersion() >= 10.2) {
      spOtherChartsB.addItem("IOPS");
      SPASH.addItem("ASH report for a SQL_ID");
    }
    
    if(ConsoleWindow.getDBVersion() >=10 && Properties.isAvoid_awr()) {
      spWaitEventsB.addItem("Event Histogram");
    }

    
    
    if(ConsoleWindow.getDBVersion() >=10 && !Properties.isAvoid_awr()) {
      addASH();
      addFindFTSB();
      addAWRRPTB();
      if (!imported) addADDMB();
      addStreamsB();
      addRACB();
      addOPQB();
      addOsLoadB();
      SPTablespaceFileB.addItem("Tablespace Growth");
      SPTablespaceFileB.addItem("Tablespace IO Chart");
      SPTablespaceFileB.addItem("File IO Chart");
      SPTablespaceFileB.addItem("Segment Growth Chart");
      SPTablespaceFileB.addItem("Segment IO Chart");
      spWaitEventsB.addItem("Wait Class");
      spWaitEventsB.addItem("IO Events");
      spWaitEventsB.addItem("Adj Wait Class");
      spWaitEventsB.addItem("Adj IO Events");    
      spWaitEventsB.addItem("F/G Average Wait Events Time");
      spPGAStatsB.addItem("PGA Workareas");
      spPGAStatsB.addItem("PGA Histogram");          


    }

    if (ConsoleWindow.getDBVersion() >= 9.2) {
      spWaitEventsB.addItem("CPU Time Breakdown (Text)");
      spWaitEventsB.addItem("CPU Time Breakdown");
    }
    
    if (ConsoleWindow.getDBVersion() >= 11.0) {
      spWaitEventsB.addItem("Event Histogram");
    }
    
    if(ConsoleWindow.getDBVersion() >=10 && !Properties.isAvoid_awr() && Properties.getPete()) {
      spWaitEventsB.addItem("io done");
      spWaitEventsB.addItem("RMAN backup & recovery I/O");
      spWaitEventsB.addItem("control file parallel write");
      spWaitEventsB.addItem("control file sequential write");
    }
    
    if (Properties.isEnablePerformanceReviewOptions()) {
      addPerformanceReviewButton();
    }
    
    setAllControlsEnabled(false);
    
    allButtonsP.setLayout(new BorderLayout());
    allButtonsP.add(controlButtonP,BorderLayout.WEST);
    allButtonsP.add(databaseButtonP,BorderLayout.CENTER);
    
    this.setLayout(new BorderLayout());
    this.add(allButtonsP,BorderLayout.NORTH);
    this.add(statspackSP,BorderLayout.CENTER);
    this.add(statusBarL,BorderLayout.SOUTH);
    
    databaseButtonP.setPreferredSize(new Dimension(200,127));       
    
    // set scrollpane background to white 
    statspackSP.getViewport().setBackground(Color.white);
    statspackSP.getViewport().setName("statspack");
    
    getSnapshotsB.setPreferredSize(prefSize3);
    getSnapshotsB.setForeground(Color.BLUE);
    getSnapshotsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getSnapshotsB.actionPerformed(false);
          setAllControlsEnabled(true);
        }
      });      
      
    selectedInstanceNameL.setPreferredSize(prefSize3);
    selectedInstanceNameL.setForeground(Color.BLUE);
    
    selectSnapshotsLast72.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectSnapshotsLast72.actionPerformed();
      }
    });      
    
    selectSnapshotsLast1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectSnapshotsLast1.actionPerformed();
      }
    });      

    selectSnapshotsLast24.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectSnapshotsLast24.actionPerformed();
      }
    });      
    
    selectSnapshotsLast120.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectSnapshotsLast120.actionPerformed();
      }
    });    

    moveBackMinus12Snapshots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveBackMinus12Snapshots.actionPerformed();
      }
      });
      
    moveBackMinus24Snapshots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveBackMinus24Snapshots.actionPerformed();
      }
      });
      
    moveBackPlus12Snapshots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveBackPlus12Snapshots.actionPerformed();
      }
      });
      
    moveBackPlus24Snapshots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveBackPlus24Snapshots.actionPerformed();
      }
      });
      
    spWaitEventsB.setPreferredSize(standardButtonSize);
    spWaitEventsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spWaitEventsCB";
          spWaitEventsB.actionPerformed();
        }
      });       
    
    spGlobalCacheB.setPreferredSize(standardButtonSize);
    spGlobalCacheB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spRACCB";
          spGlobalCacheB.actionPerformed();
        }
      });        
      
    spSummaryB.setPreferredSize(standardButtonSize);
    spSummaryB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spSummaryCB";
          spSummaryB.actionPerformed();
        }
      });    
      
    spUndoB.setPreferredSize(standardButtonSize);
    spUndoB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spUndoCB";
          spUndoB.actionPerformed();
        }
      });  
      
    spTablespaceFileB.setPreferredSize(standardButtonSize);
    spTablespaceFileB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spIOCB";
          spTablespaceFileB.actionPerformed();
        }
      });    
         
    spStatisticsB.setPreferredSize(standardButtonSize);
    spStatisticsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spStatisticsB";
          spStatisticsB.actionPerformed();
        }
      }); 
      
    spEnqueueB.setPreferredSize(standardButtonSize);
    spEnqueueB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spEnqueueB";
          spEnqueueB.actionPerformed();
        }
      });   
     
    spOtherChartsB.setPreferredSize(standardButtonSize); 
    spOtherChartsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spLoadCB";
          spOtherChartsB.actionPerformed();
        }
      });    
      
    spLatchB.setPreferredSize(standardButtonSize); 
    spLatchB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spLatchCB";
          spLatchB.actionPerformed();
        }
      });     
      
    spResourceLimitB.setPreferredSize(standardButtonSize); 
    spResourceLimitB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spResourceLimitB";
          spResourceLimitB.actionPerformed();
        }
      });    
      
    spPGAStatsB.setPreferredSize(standardButtonSize);
    spPGAStatsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spPGAStatsCB";
          spPGAStatsB.actionPerformed();
        }
      });    
      
    spTopSQLB.setPreferredSize(standardButtonSize);
    spTopSQLB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "topSQLCB";
          spTopSQLB.actionPerformed();
        }
      });
      
    spLibraryCacheB.setPreferredSize(standardButtonSize);
    spLibraryCacheB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            lastButton = "spLibraryCacheB";
            spLibraryCacheB.actionPerformed();
          }
        });
        
    performanceReviewB.setPreferredSize(standardButtonSize);
    performanceReviewB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "performanceReviewB";
          performanceReviewB.actionPerformed();
        }
      });  
      
    spOsLoadB.setPreferredSize(standardButtonSize);
    spOsLoadB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spOsLoadB";
          spOsLoadB.actionPerformed();
        }
      }); 
      
    spBufferCacheB.setPreferredSize(standardButtonSize);
    spBufferCacheB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spBufferCacheCB";
          spBufferCacheB.actionPerformed();
        }
      }); 
      
    spSegmentWaitsB.setPreferredSize(standardButtonSize);
    spSegmentWaitsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spSegmentWaitsCB";
          spSegmentWaitsB.actionPerformed();
        }
      }); 
      
    spLogSwitchB.setPreferredSize(standardButtonSize);
    spLogSwitchB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spLogSwitchB";
          spLogSwitchB.actionPerformed();
        }
      }); 
      
    spReloadInvalidationsB.setPreferredSize(standardButtonSize);
    spReloadInvalidationsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spReloadInvalidationsB";
          spReloadInvalidationsB.actionPerformed();
        }
      });  
      
    spAWRRPTB.setPreferredSize(standardButtonSize);
    spAWRRPTB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spAWRRPTB";
          spAWRRPTB.actionPerformed();
        }
      });  

    spADDMB.setPreferredSize(standardButtonSize);
    spADDMB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spADDMB";
          spADDMB.actionPerformed();
        }
      });     
    
    spStreamsB.setPreferredSize(standardButtonSize);
    spStreamsB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spStreamsB";
          spStreamsB.actionPerformed();
        }
      });  
    
    spOPQB.setPreferredSize(standardButtonSize);
    spOPQB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spOPQB";
          spOPQB.actionPerformed();
        }
      });  
    
    spASHB.setPreferredSize(standardButtonSize);
    spASHB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spASHB";
          spASHB.actionPerformed();
        }
      });  
    
    spFindFTSB.setPreferredSize(standardButtonSize);
    spFindFTSB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lastButton = "spFindFTSB";
          spFindFTSB.actionPerformed();
        }
      }); 
  }
  
/*  public void getDBDetails() {
    try {
      // get the db_id and instance_number 
      String cursorId;
      if (ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "getStatspackDBid10.sql";
      }
      else {
        cursorId = "getStatspackDBid.sql";
      }
      
      QueryResult myResult = ExecuteDisplay.execute(cursorId, false, true,null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      dbId = Long.valueOf(resultSet[0][0]).longValue();
      instanceNumber = Integer.valueOf(resultSet[0][1]).intValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  } */
  
  public int getStartSnapId() throws NotEnoughSnapshotsException {
    if (startSnapshotCB.getItemCount() < 2) throw new NotEnoughSnapshotsException("At least 2 snapshots are required");
    
    String selectedItem = String.valueOf(startSnapshotCB.getSelectedItem());
    
    // The first 25 chars represent the date and some spaces before the snap Id 
    int snapId = Integer.valueOf(selectedItem.substring(25)).intValue();
      
    return snapId;
  }  
  
  public int getEndSnapId()  throws NotEnoughSnapshotsException {
    if (startSnapshotCB.getItemCount() < 2) throw new NotEnoughSnapshotsException("At least 2 snapshots are required");

    String selectedItem = String.valueOf(endSnapshotCB.getSelectedItem());
    
    // The first 25 chars represent the date and some spaces before the snap Id 
    int snapId = Integer.valueOf(selectedItem.substring(25)).intValue();
        
    return snapId;
  }
  
  public String getStartDate() throws NotEnoughSnapshotsException {
    if (startSnapshotCB.getItemCount() < 2) throw new NotEnoughSnapshotsException("At least 2 snapshots are required");
    
    String selectedItem = String.valueOf(startSnapshotCB.getSelectedItem());
    
    // The first 25 chars represent the date and some spaces before the snap Id 
    String startDate =  selectedItem.substring(0,17);
      
    return startDate;
  }  
  
  public String getEndDate()  throws NotEnoughSnapshotsException {
    if (startSnapshotCB.getItemCount() < 2) throw new NotEnoughSnapshotsException("At least 2 snapshots are required");

    String selectedItem = String.valueOf(endSnapshotCB.getSelectedItem());
    
    // The first 25 chars represent the date and some spaces before the snap Id 
    String endDate = selectedItem.substring(0,17);
        
    return endDate;
  }
  
  public int[] getSelectedSnapIdRange() {
    int selectedStartIndex = startSnapshotCB.getSelectedIndex();
    int selectedEndIndex = endSnapshotCB.getSelectedIndex();
    
    int range = selectedEndIndex - selectedStartIndex + 1;
    int[] snapIdRange = new int[range];
    int j = 0;
    
    for (int i = selectedStartIndex; i <= selectedEndIndex; i++) {
      String snapId = String.valueOf(startSnapshotCB.getItemAt(i)).substring(25);
      snapIdRange[j++] = Integer.valueOf(snapId).intValue();
    }
    
    return snapIdRange;
  }
  
  public String[] getSelectedSnapDateRange() {
    int selectedStartIndex = startSnapshotCB.getSelectedIndex();
    int selectedEndIndex = endSnapshotCB.getSelectedIndex();
    
    int range = selectedEndIndex - selectedStartIndex + 1;
    String[] snapIdRange = new String[range];
    int j = 0;
    
    for (int i = selectedStartIndex; i <= selectedEndIndex; i++) {
      snapIdRange[j++] = String.valueOf(startSnapshotCB.getItemAt(i)).substring(0,17);
    }
    
    return snapIdRange;
  }
  
  public String getSnapDate(int snap) {
    String snapDate = "";
    for (int i=0; i < startSnapshotCB.getItemCount(); i++) {
      int lastSpace = String.valueOf(startSnapshotCB.getItemAt(i)).lastIndexOf(" ");
      
      boolean debug = false;
      if (debug) System.out.println("checking " + snap + " against " + String.valueOf(startSnapshotCB.getItemAt(i)).substring(lastSpace+1));
      
      if (String.valueOf(startSnapshotCB.getItemAt(i)).substring(lastSpace+1).equals(String.valueOf(snap))) {
        snapDate =String.valueOf(startSnapshotCB.getItemAt(i)).substring(0,17);
      }
    }
    
    return snapDate;
  }  
  

  
  
  public void clearScrollP() {
    statspackSP.getViewport().removeAll();
  }
  
  public void addPGAStatsB() {
    databaseButtonP.add(spPGAStatsB);
  }
  
  public void addUndoCB() {
    databaseButtonP.add(spUndoB);
  }  
  
  public void addSegmentWaitCB() {
    databaseButtonP.add(spSegmentWaitsB);
  }
  
  public void addResourceLimitB() {
    databaseButtonP.add(spResourceLimitB);
  }

  public void addOPQB() {
    databaseButtonP.add(spOPQB);
  }
  
  public void getSnapshots() { 
    Thread getSnapshotsThread = new Thread ( new Runnable() {
      public void run() {      
        getSnapshotsB.actionPerformed(true);
      }
    });
    
    getSnapshotsThread.setDaemon(false);
    getSnapshotsThread.setName("getSnapshots");
    getSnapshotsThread.start();
  }
  
  public int getSnapshotCount() {
//    return startSnapshotCB.getItemCount();
    return endSnapshotCB.getSelectedIndex() - startSnapshotCB.getSelectedIndex();
  }
  
  public void addPerformanceReviewButton() {
    databaseButtonP.add(performanceReviewB);
  }  
  
  public void removePerformanceReviewButton() {
    databaseButtonP.remove(performanceReviewB);
    databaseButtonP.updateUI();
  }
  
  public void addOsLoadB() {
    databaseButtonP.add(spOsLoadB);
  }   
  
  public void addStreamsB() {
    databaseButtonP.add(spStreamsB);
  }
  
  public void addAWRRPTB() {
    databaseButtonP.add(spAWRRPTB);
  }  

  public void addADDMB() {
    databaseButtonP.add(spADDMB);
  }  
  
  public void addLogSwitchB() {
    databaseButtonP.add(spLogSwitchB);
  }
  
  public void addRACB() {
    databaseButtonP.add(spGlobalCacheB);
  }
  
  public void addASH() {
    databaseButtonP.add(spASHB);
  }
  
  public void addFindFTSB() {
    databaseButtonP.add(spFindFTSB);
  }
  
  /**
   * Check the database was not restarted between the selected snapshot range and 
   * at least 2 snapshots exist in the range.
   * 
   * @throws Exception 
   */
  public void sanityCheckRange() throws Exception {
    String cursorId;
    
    if (debug) {
      System.out.println("dbid: " + dbId);
      System.out.println("instanceNumber: "+ instanceNumber);
    }
    
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
      cursorId = "stats$StartupTimesAWR.sql";
    }
    else {
      cursorId = "stats$StartupTimes.sql";
    }
    
    Parameters myPars = new Parameters();
    myPars.addParameter("int",getStartSnapId());
    myPars.addParameter("int",getEndSnapId());
    myPars.addParameter("long",getDbId());
    myPars.addParameter("int",getInstanceNumber());
    
    QueryResult myResult;
    int numStarts = 0;
    int numSnapshots = getSelectedSnapDateRange().length;
    
    try {
      myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      numStarts = Integer.valueOf(resultSet[0][0]).intValue();
      if (debug) System.out.println("# start dates in range: " + numStarts);
    }
    catch (Exception e) {
      throw new InsaneSnapshotRangeException("Failed Sanity Check on the selected Snapshot Range");
    }
    
    if (numStarts > 1 && (!ConsoleWindow.isAllowSnapshotsToSpanRestarts())) {
      // Get a list of startup times 
      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        cursorId = "stats$StartupTimesListAWR.sql";
      }
      else {
        cursorId = "stats$StartupTimesList.sql";          
      }
       
      QueryResult myResult1 = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      String[][] myResultSet = myResult1.getResultSetAsStringArray();
      
      String startupList = "\n\nThese are the startup times (no more than 10 will be listed):\n\n";
      
      for (int i=0; i < myResult1.getNumRows(); i++) {
        startupList = startupList + myResultSet[i][0] + "\n";
      }
      
      throw new ReStartedDBException("The DB was re-started during the snapshot range specified" + startupList);
    }
    
    if (numSnapshots < 2) {
      throw new NotEnoughSnapshotsException("Not enough Snapshots in the selected range");
    }    
    
    if (numSnapshots > 121 && (!ConsoleWindow.isUnlimitedSnapshots())) {
      throw new ToManySnapshotsException("To Many Snapshots in the selected range.  More than 120 not allowed");
    }
  }
  
  public String getLastButton () {
    return lastButton;
  }
  
  /**
   * setLastOption - Stores the last option selected from the list of options for a button
   * 
   * @param action
   */
  public void setLastAction(String action) {
    lastAction = action;
  }
  
  public void refresh() {
    if (lastButton == "spWaitEventsCB") spWaitEventsB.listActionPerformed(lastAction, new JFrame(),true);
    if (lastButton == "spRACCB") spGlobalCacheB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spSummaryCB") spSummaryB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spUndoCB") spUndoB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spIOCB") spTablespaceFileB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spStatisticsB") spStatisticsB.actionPerformed();
    if (lastButton == "spEnqueueB") spEnqueueB.actionPerformed();
    if (lastButton == "spLoadCB") spOtherChartsB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spLatchCB") spLatchB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spResourceLimitB") spResourceLimitB.actionPerformed();
    if (lastButton == "spPGAStatsCB") spPGAStatsB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "topSQLCB") spTopSQLB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spLibraryCacheB") spLibraryCacheB.actionPerformed();
    if (lastButton == "spOsLoadB") spOsLoadB.actionPerformed();
    if (lastButton == "spBufferCacheCB") spBufferCacheB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spSegmentWaitsCB") spSegmentWaitsB.listActionPerformed(lastAction, new JFrame());
    if (lastButton == "spLogSwitchB") spLogSwitchB.actionPerformed();
    if (lastButton == "spReloadInvalidationsB") spReloadInvalidationsB.actionPerformed();
    if (lastButton == "spOPQB") spOPQB.actionPerformed();    
    if (lastButton == "spASHB") spASHB.actionPerformed();
    if (lastButton == "spFindFTS") spFindFTSB.actionPerformed();
  }
  
  public void setDbId(long id) {
    dbId = id;
  }
  
  public long getDbId() {
    return dbId;
  }  
  
  public void setInstanceNumber(int id) {
    instanceNumber = id;
  }
  
  public int getInstanceNumber() {
    return instanceNumber;
  }
  
  public JScrollPane addScrollP() {
    if (displayedChartP instanceof ChartPanel) {
      this.remove(displayedChartP);
      displayedChartP = null;
      chartDisplayed = false;
      this.add(statspackSP,BorderLayout.CENTER);
      this.updateUI();
    }
    
    return statspackSP;
  }
  
  public static boolean isChartDisplayed() {
    return chartDisplayed;
  }
  
  public void displayChart(ChartPanel myChartPanel) {
    if (displayedChartP instanceof ChartPanel) {
      this.remove(displayedChartP);
    }
    else {
      statspackSP.getViewport().removeAll();
      this.remove(statspackSP);      
    }
      
    displayedChartP = myChartPanel;
    this.add(displayedChartP,BorderLayout.CENTER);
    chartDisplayed = true;
    this.updateUI();
    
    statusBarL.setText("");

  }
  
  public void clearSelectedInstanceName() {
    selectedInstanceNameL.setText("");
    instanceName=null;
  }
  
  public void setSelectedInstanceName(String newInstanceName) {
    selectedInstanceNameL.setText("Selected instance: " + newInstanceName);
    instanceName=newInstanceName;
  }
  
  public String getSelectedInstanceName() {
    if (instanceName instanceof String) {
      return instanceName;
    }
    else {
      return "abc123def456ghi789jkl";              // if the preSetAWRInstance has not been set then we must return nonsense
    }
    
  }
  
  public String getInstanceName() {
    return instanceName;
  }
  
  private void setAllControlsEnabled(boolean enabled) {
    selectSnapshotsLast1.setEnabled(enabled);
    selectSnapshotsLast72.setEnabled(enabled);
    selectSnapshotsLast24.setEnabled(enabled);
    selectSnapshotsLast120.setEnabled(enabled);
    moveBackMinus12Snapshots.setEnabled(enabled);
    moveBackMinus24Snapshots.setEnabled(enabled);
    moveBackPlus12Snapshots.setEnabled(enabled);
    moveBackPlus24Snapshots.setEnabled(enabled);
    
    // databaseButtonP 
    spWaitEventsB.setEnabled(enabled);
    spStatisticsB.setEnabled(enabled);
    spPGAStatsB.setEnabled(enabled);
    spAWRRPTB.setEnabled(enabled);
    spADDMB.setEnabled(enabled);
    spTopSQLB.setEnabled(enabled);    
    spLibraryCacheB.setEnabled(enabled);
    performanceReviewB.setEnabled(enabled);
    spOtherChartsB.setEnabled(enabled);
    spResourceLimitB.setEnabled(enabled);
    spOsLoadB.setEnabled(enabled);
    spEnqueueB.setEnabled(enabled);
    spReloadInvalidationsB.setEnabled(enabled);
    spUndoB.setEnabled(enabled);
    spTablespaceFileB.setEnabled(enabled);
    spLatchB.setEnabled(enabled);
    spSummaryB.setEnabled(enabled);
    spLogSwitchB.setEnabled(enabled);
    spBufferCacheB.setEnabled(enabled);
    spSegmentWaitsB.setEnabled(enabled);
    spGlobalCacheB.setEnabled(enabled);
    spStreamsB.setEnabled(enabled);
    spOPQB.setEnabled(enabled);
    spASHB.setEnabled(enabled);
    spFindFTSB.setEnabled(enabled);

  }

  private void addSpecificTablespaceIO() {

  }
  
  public boolean isKeepEventColours() {
    return keepEventColours;
  }
  
  public void setKeepEventColours(boolean value) {
    keepEventColours = value;
  }
  
  public boolean isImported() {
    return imported;
  }
}



