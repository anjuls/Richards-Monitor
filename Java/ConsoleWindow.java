/*
 * ConsoleWindow.java        13.00 05/01/04
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
 * 21/02/05 Richard Wright Swithed comment style to match that
 *                         recommended in Sun's own coding conventions.
 * 03/06/05 Richard Wright Comments out the job and statistics menu
 * 02/06/05 Richard Wright Added menu option to flip a single row result set
 * 06/06/05 Richard Wright Added further options to the dbarep combo box
 * 06/06/05 Richard Wright changed thisVersion to 12.16
 * 06/06/05 Richard Wright Stopped flipping the resultset in launchPutty
 * 07/06/05 Richard Wright Modify getSid, getSidDetails, getPid, getDbVersion to
 *                         use ExecuteDisplay and not flip the result set
 * 08/06/05 Richard Wright Add LogWriter output
 * 09/06/05 Richard Wright Changed thisVersion to 12.17
 * 10/06/05 Richard Wright Changed thisVersion to 13.00
 * 13/06/05 Richard Wright add setStatspackSchema()
 * 15/08/05 Richard Wright Changed thisVersion to 13.02
 * 17/08/05 Richard Wright Moved creation of the scratchPanel from the constructor
 *                         to addScratchPanel() which is called from connectWindow
 *                         after the config file has been read
 * 18/08/05 Richard Wright Changed thisVersion to 13.03
 * 19/08/05 Richard Wright Changed thisVersion to 13.04
 * 19/08/05 Richard Wright Corrected the database panel name in fileSave()
 * 14/09/05 Richard Wright Corrected setEnableKillSession to enable the session
 *                         menu item as well as the advanced menu.
 * 16/09/05 Richard Wright Added a menu item to allow a statspack wait event chart
 *                         to be adjusted by snapshot length.
 * 29/09/05 Richard Wright Added support for flashback, database properties and
 *                         resumable statements
 * 29/09/05 Richard Wright Enabled v$sql_plan for 10g as well as 9i.  Needs checking
 *                         to see whether this is appropriate.
 * 04/10/05 Richard Wright Changed thisVersion to 13.05
 * 10/10/05 Richard Wright Changed thisVersion to 13.06
 * 26/10/05 Richard Wright Changed thisVersion to 13.07
 * 27/10/05 Richard Wright Added support for numbering scratch panels
 * 01/11/05 Richard Wright Changed thisVersion to 13.08
 *                         Removed all reference to job & statistics menu's
 *                         Remvoed all reference to dbarep database connections
 * 02/11/05 Richard Wright Simplified launchPutty so that it calls putty direct
 *                         rather than creating and running a bat file first.
 * 02/11/05 Richard Wright Changed thisVersion to 13.09
 * 04/11/05 Richard Wright Added openBlog()
 * 07/11/05 Richard Wright Changed thisVersion to 13.11
 * 11/11/05 Richard Wright added removeScratchPanel() and improved removeSessionPanel
 *                         to run removeAll() on the panel as a way ot trying to
 *                         release as much memory as possible after the closure
 *                         of a session panel as I don't think the sessionPanel
 *                         is really being killed.
 * 08/12/05 Richard Wright Changed thisVersion to 13.12
 * 08/12/05 Richard Wright Added menu items to allow selection of more then 100
 *                         snapshots and snapshots that span instance restarts.
 * 04/01/06 Richard Wright Changed thisVersion to 13.13
 * 20/01/06 Richard Wright Changed thisVersion to 13.14
 * 31/01/06 Richard Wright Added menu item for colletion performance review data
 * 27/02/06 Richard Wright Add the resource limit button to the statspack panel
 * 28/02/06 Richard Wright Make the OSLoadChart appear on the overview panel when
 *                         the db is version 10 and above
 * 01/03/06 Richard Wright Made dbVersion private and added an accessor.  Modified
 *                         every call thru out the package to use the accessor.
 * 01/03/06 Richard Wright Removed the LogWriter class and all references to it..
 * 10/03/06 Richard Wright Changed thisVersion to 13.15.
 * 10/03/06 Richard Wright Made instanceName a private variable and generated accessor.
 * 10/03/06 Richard Wright Added a menu item to allow overview charts to appear
 *                         in a single frame
 * 12/03/06 Richard Wright Changed thisVersion to 13.16.
 * 23/03/06 Richard Wright Added isLinux, getBaseDir and setBaseDir to allow
 *                         RichMon to be run on a linux/unix server.
 * 23/03/06 Richard Wright Changed thisVersion to 13.17.
 * 31/03/06 Richard Wright RichMon detects which OS is being used and set the
 *                         boolean linux appropriately by using System.getProperty().
 * 31/03/06 Richard Wright Changed thisVersion to 13.18.
 * 04/04/06 Richard Wright Added in the redo log switch by hour code.
 * 15/04/06 Richard Wright Changed thisVersion to 13.19.
 * 16/04/06 Richard Wright Changed thisVersion to 13.20.
 * 16/04/06 Richard Wright Changed thisVersion to 13.21.
 * 08/06/06 Richard Wright Do not display the statspack panel in 8.0 db's.
 * 14/06/06 Richard Wright Changed the format of the dbVersion to include 2 decimal places
 * 22/06/06 Richard Wright Made the add and remove methods for scratch panels public
 *                         so they can be called from the ScratchPanel class and
 *                         made any new scratch panel the point of focus
 * 22/06/06 Richard Wright Made database properties appear from 9i instead of 10g
 * 22/06/06 Richard Wright Fixed bug that allowed the addition of a  performance
 *                         review button to the statspack panel when it already existed
 * 28/06/06 Richard Wright Changed thisVersion to 13.22.
 * 30/06/06 Richard Wright Removed SPUndoCB references
 * 06/07/06 Richard Wright Added menu options to allow setting both types of  timeout operations
 * 06/07/06 Richard Wright Removed menu options to create / remove scratch panels
 * 10/07/06 Richard Wright Changed thisVersion to 13.23.
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 13/07/06 Richard Wright Changed thisVersion to 13.24.
 * 13/07/06 Richard Wright Changed thisVersion to 13.25.
 * 28/07/06 Richard Wright Changed thisVersion to 13.26.
 * 10/08/06 Richard Wright Changed thisVersion to 13.27.
 * 15/08/06 Richard Wright Changed thisVersion to 13.28.
 * 15/08/06 Richard Wright Changed thisVersion to 14.01.
 * 18/08/06 Richard Wright Modified the comment style and error handling
 * 18/08/06 Richard Wright Prevent the last panel from being closeable
 * 06/10/06 Richard Wright Stopped get ServerName flipping results
 * 06/11/06 Richard Wright Changed thisVersion to 14.03
 * 07/11/06 Richard Wright Add support for scheduler jobs in jobsCB
 * 07/11/06 Richard Wright Add support for sga_target in parametersCB
 * 14/11/06 Richard Wright Added checkWhetherSoxSystem()
 * 04/01/07 Richard Wright Changed thisVersion to 14.04
 * 04/01/07 Richard Wright Enabled v$sql_plan by default
 * 08/01/07 Richard Wright Changed thisVersion to 14.05
 * 17/01/07 Richard Wright Modified checkForUpgrade so that no error is produced
 *                         when the i drive is missing (useful at non vf sites)
 * 29/01/07 Richard Wright Changed thisVersion to 14.06
 * 31/01/07 Richard Wright Added support for awrrpt button on statspack panel for 10g awr
 * 01/02/07 Richard Wright Changed thisVersion to 14.07
 * 04/04/07 Richard Wright Changed thisVersion to 14.08
 * 10/04/07 Richard Wright Changed thisVersion to 15.01
 * 27/04/07 Richard Wright Changed thisVersion to 15.02
 * 14/05/07 Richard Wright Changed thisVersion to 15.04
 * 29/05/07 Richard Wright Changed thisVersion to 15.05
 * 12/06/07 Richard Wright Changed thisVersion to 16.01
 * 31/07/07 Richard Wright Changed thisVersion to 16.02
 * 06/08/07 Richard Wright Added 'alter session set current schema' functionality
 * 17/08/07 Richard Wright Changed thisVersion to 16.03
 * 05/09/07 Richard Wright Added OptionsList
 * 08/09/07 Richard Wright Changed thisVersion to 16.04
 * 08/09/07 Richard Wright Changed thisVersion to 16.05
 * 17/09/07 Richard Wright Changed thisVersion to 16.06
 * 17/09/07 Richard Wright Improved setCurrentSchema to allow it to be unset easier.
 * 17/09/07 Richard Wright Changed thisVersion to 16.07
 * 01/10/07 Richard Wright Changed thisVersion to 16.08
 * 12/10/07 Richard Wright Changed thisVersion to 17.00
 * 15/10/07 Richard Wright Added support for RAC & debug boolean
 * 31/10/07 Richard Wright Enhanced for RAC
 * 31/10/07 Richard Wright Made v$shared_server_monitor available in 9 (from 10)
 * 08/11/07 Richard Wright Removed menu option to make a charts area rather than stacked
 * 10/12/07 Richard Wright Removed the db info pages links
 * 10/12/07 Richard Wright Modified launchPutty to allow -ssh on the cmd line
 * 10/12/07 Richard Wright No longer audit killed sessions to the alert log
 * 12/12/07 Richard Wright Changed thisVersion to 17.01
 * 17/12/07 Richard Wright Changed thisVersion to 17.02
 * 19/12/07 Richard Wright Changed thisVersion to 17.03
 * 21/12/07 Richard Wright Changed thisVersion to 17.04
 * 21/12/07 Richard Wright Changed thisVersion to 17.05
 * 03/01/08 Richard Wright Changed thisVersion to 17.06
 * 08/01/08 Richard Wright Changed thisVersion to 17.07
 * 10/01/08 Richard Wright Changed thisVersion to 17.08
 * 15/01/08 Richard Wright Removed all references to maximizing width of the event chart
 * 15/01/08 Richard Wright Updated the description of menuFileOverviewChartsSingleFrame
 * 31/01/08 Richard Wright Changed thisVersion to 17.09
 * 31/01/08 Richard Wright Added the RichMon Icon to frame title bar
 * 04/02/08 Richard Wright Changed thisVersion to 17.11 (missing out 17.10)
 * 31/03/08 Richard Wright Changed thisVersion to 17.12
 * 28/04/08 Richard Wright Added support for the ashV
 * 02/05/08 Richard Wright Changed thisVersion to 17.13
 * 21/05/08 Richard Wright Changed thisVersion to 17.14
 * 21/05/08 Richard Wright Modified the messages so that it no longer says that
 *                         killing a session is audited to the alert log
 * 27/05/08 Richard Wright Changed thisVersion to 17.15
 * 27/05/08 Richard Wright Only add the launch menu if running on windows
 * 09/06/08 Richard Wright Changed thisVersion to 17.16
 * 10/06/08 Richard Wright Changed thisVersion to 17.17
 * 17/06/08 Richard Wright Changed thisVersion to 17.18
 * 23/06/08 Richard Wright Changed thisVersion to 17.19
 * 02/07/08 Richard Wright Changed thisVersion to 17.20
 * 10/07/08 Richard Wright Changed thisVersion to 17.21
 * 03/10/08 Richard Wright Changed thisVersion to 17.23
 * 10/10/08 Richard Wright Changed thisVersion to 17.24
 * 24/11/08 Richard Wright Changed thisVersion to 17.25
 * 10/12/08 Richard Wright Changed thisVersion to 17.26
 * 08/01/09 Richard Wright Changed thisVersion to 17.27
 * 29/01/09 Richard Wright Changed thisVersion to 17.28
 * 16/02/09 Richard Wright Changed thisVersion to 17.29
 * 09/05/09 Richard Wright Changed thisVersion to 17.31
 * 12/05/09 Richard Wright Changed thisVersion to 17.32
 * 14/05/09 Richard Wright Changed thisVersion to 17.33
 * 15/05/09 Richard Wright Expanded functionality to support session panels that have instance affinity
 * 18/05/09 Richard Wright Changed thisVersion to 17.34
 * 10/06/09 Richard Wright Changed thisVersion to 17.35
 * 15/07/09 Richard Wright numScratchPanel is no longer decremented to avoid the posibility of duplicate panel numbers
 * 11/08/09 Richard Wright Changed thisVersion to 17.36
 * 11/08/09 Richard Wright Modified retriveDBVersionFromDB to set the myFrame in Cursor to make CURSOR reusable
 * 02/10/09 Richard Wright Changed thisVersion to 17.37
 * 06/10/09 Richard Wright Changed thisVersion to 17.38
 * 20/10/09 Richard Wright Changed thisVersion to 17.39
 * 20/10/09 Richard Wright Corrected the base directory used for saving out from panels
 * 23/10/09 Richard Wright Added the playback menu and associated functionality
 *   /01/10 Richard Wright Changed thisVersion to 17.40
 * 25/02/10 Richard Wright Changed thisVersion to 17.41
 * 30/03/10 Richard Wright File=>Save output will now save the output from all scratch panels not just the first
 * 09/04/10 Richard Wright Moved baseDir and linux to ConnectWindow
 * 19/04/10 Richard Wright Changed thisVersion to 17.42
 * 04/05/10 Richard Wright Remove all SOX functionality as this was specific to Vodafone
 * 10/05/10 Richard Wright Changed thisVersion to 17.43
 * 02/09/10 Richard Wright Modified FileSave to account for linux file names
 * 17/11/10 Richard Wright Changed thisVersion to 17.44 (should have done it ages ago)
 * 02/12/10 Richard Wright Changed thisVersion to 17.46 
 * 03/12/10 Richard Wright Changed thisVersion to 17.47
 * 10/12/10 Richard Wright Changed thisVersion to 17.48
 * 24/12/10 Richard Wright Changed thisVersion to 17.49
 * 20/05/11 Richard Wright Changed thisVersion to 17.51
 * 14/07/11 Richard Wright Changed thisVersion to 17.52
 * 08/09/11 Richard Wright Not selects the hostname from v$instance as well as instance name and uses this for the titlebar
 * 22/09/11 Richard Wright Remove the 'Playback' menu and functionality
 * 07/10/11 Richard Wright Changed thisVersion to 17.53
 * 28/02/12 Richard Wright Changed thisVersion to 17.55
 * 05/04/12 Richard Wright Changed thisVersion to 17.56
 * 18/05/12 Richard Wright Changed thisVersion to 17.57
 * 21/05/12 Richard Wright Changed thisVersion to 17.58
 * 05/07/12 Richard Wright Changed thisVersion to 17.59
 * 18/07/12 Richard Wright Ensure statspackAWRPanel not visible in 8i db's
 * 16/08/12 Richard Wright Changed thisVersion to 17.60
 * 05/12/12 Richard Wright add support for openning an AWR panel on imported AWR data
 * 24/01/13 Richard Wright Changed thisVersion to 17.62  (17.61 was used for looking at javafx)
 * 24/01/13 Richard Wright fixed bug when accessing imported AWR data - int needed to be long for holding the dbid
 * 22/07/13 Richard Wright Changed thisVersion to 17.63
 * 30/01/14 Richard Wright Changed thisVersion to 17.64
 * 15/02/14 Richard Wrighg Changed thisVersion to 17.65
 * 06/03/14 Richard Wright changed thisVersion to 17.66
 * 12/03/14 Richard Wright throw insuffientPrivileges errors when trying to trace a session without sysdba privs
 * 12/03/14 Richard Wright give a visual indication that sysdba or dba role requried to kill a session
 * 18/03/14 Richard Wright changed thisVersion to 17.67
 * 09/04/14 Richard Wright changed thisVersion to 17.68
 * 18/06/14 Richard Wright changed thisVersion to 17.69
 * 14/10/14 Richard Wright changed thisVersion to 17.70
 * 06/01/15 Richard Wright changed thisVersion to 17.71
 * 22/05/15 Richard Wright changed thisVersion to 17.72
 * 05/06/15 Richard Wright changed thisVersion to 17.73
 * 16/06/15 Richard Wright changed thisVersion to 17.74
 * 29/09/15 Richard Wright changed thisVersion to 17.75
 * 29/09/15 Richard Wright changed thisVersion to 17.76
 * 07/12/15 Richard Wright changed thisVersion to 17.77
 * 09/12/15 Richard Wright changed thisVersion to 17.78
 * 09/12/15 Richard Wright removed retrieveDBVersionFromDB and put it into ConnectWindow and all relevant panels and buttons instead
 * 05/??/16 Richard Wright changed thisVersion to 17.79
 * 28/04/16 Richard Wright changed thisVersion to 17.80
 * 24/10/16 Richard Wright changed thisVersion to 17.81
 * 25/10/16 Richard Wright added exadata boolean and associated functionality
 * 08/12/17 Richard Wright changed thisVersion to 17.82
 * 09/02/18 Richard Wright changed thisVersion to 18.00 - Modify to make better use of 12c CDBs etc
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.URL;

import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;


/**
 * This frame contains all the elements of RichMon which are displayed except
 * the ConnectWindow & tearOff JFrames and some charts.
 */
public class ConsoleWindow extends JFrame  {
  boolean debug = false;

  // menu's
  private JMenuItem helpAbout = new JMenuItem("About RichMon");
  private JMenuItem openBlog = new JMenuItem("New Features");
  private JMenu help = new JMenu("Help");
  private JMenuItem save = new JMenuItem("Save Output");
  private JMenuItem changeConnection = new JMenuItem("Change Connection");
  private JMenuItem setCurrentSchema = new JMenuItem("Alter session set current schema");
  private JMenu customize = new JMenu("Customize");
  private static JCheckBoxMenuItem enableKillSession = new JCheckBoxMenuItem("Enable Kill Session");
  private static JCheckBoxMenuItem enableV$SQL_Plan = new JCheckBoxMenuItem("Use V$SQL_PLAN in preference to explain plan where possible");
  private static JCheckBoxMenuItem enableReFormatSQL = new JCheckBoxMenuItem("ReFormat SQL in Session Panels");
  private static JCheckBoxMenuItem flipSingleRowOutput = new JCheckBoxMenuItem("Flip Single Row Output");
  private static JCheckBoxMenuItem unlimitedSnapshots = new JCheckBoxMenuItem("Allow Selection of more than 120 Snapshots");
  private static JCheckBoxMenuItem allowSnapshotsToSpanRestarts = new JCheckBoxMenuItem("Allow Snapshop selection to span an instance restart");
  private static JCheckBoxMenuItem enablePerformanceReview = new JCheckBoxMenuItem("Enable Performance Review Option");
  private static JCheckBoxMenuItem breakOutChartsTabsFrame = new JCheckBoxMenuItem("Break Out Charts into a Tabbed Frame");
  private static JCheckBoxMenuItem openAWROnImportedData = new JCheckBoxMenuItem("Open AWR On Imported AWR Data");
  private static JCheckBoxMenuItem cacheResultsWithNoRows = new JCheckBoxMenuItem("Cache Results with No Rows");
  private static JMenu menuAWRStatspack = new JMenu("AWR | Statspack");
  private static JMenuItem lobSampleSize = new JMenuItem("Set Lob Sample Size");
  private JMenuItem exit = new JMenuItem("Exit");
  private JMenu menuFile = new JMenu("File");
  private JMenuItem sessionOverviewSid = new JMenuItem("Start a Session Overview from a SID");
  private JMenuItem sessionOverviewPid = new JMenuItem("Start a Session Overview from a PID");
  private JMenuItem sessionStartTrace = new JMenuItem("Start 10046 Tracing");
  private JMenuItem sessionStopTrace = new JMenuItem("Stop 10046 Tracing");
  private JMenuItem sessionKill = new JMenuItem("Kill Session");
  private JMenu menuSession = new JMenu("Session");
  private JMenuItem launchPuttyTelnet = new JMenuItem("Putty (Telnet)");
  private JMenuItem launchPuttySSH = new JMenuItem("Putty (SSH)");
  private static JMenu menuLaunch = new JMenu("Launch");
  private static JMenu filterMenu = new JMenu("Filter");
  private static JMenuItem selectAllInstancesMI = new JMenuItem("Select All Instances");
  private static JMenuItem deselectAllInstancesMI = new JMenuItem("De-Select All Other Instances");
  private static JMenuItem filterByUsername = new JMenuItem("Filter by Username");
  private JMenuBar menuBar = new JMenuBar();
  private JMenuItem editProperties = new JMenuItem("Properties");

  // tabbed Pane
  private static JTabbedPane consoleTP = new JTabbedPane();
  private BorderLayout borderLayout1 = new BorderLayout();


  // pointers to other objects
  private static ConnectWindow connectWindow;

  // initial Panels
  private static DatabasePanel databasePanel;
  private static PerformancePanel performancePanel;
//  private static TextViewPanel textViewPanel;
  private static TextViewPanel textViewPanel;
  private static ChartViewPanel chartViewPanel;
  private static ScratchDetailPanel scratchDetailPanel;  
  private static ScratchPanel scratchPanel;
  private static SchemaViewerPanel schemaViewerPanel;
  private static StatspackAWRPanel statspackAWRPanel;
  private static SQLIdPanel sqlIdPanel;
  private static SessionPanel sessionPanel;

  // misc
  private static String instanceName;           // the name of the instance we connected too
  private static String hostName;
  private static double dbVersion;              // the version of this instance <from v$instance>
  Dimension prefSizeF = new Dimension(120,25);  // used by fields
  Dimension prefSizeL = new Dimension(70,25);   // used by labels
  Dimension prefSizeL2 = new Dimension(100,25); // used by labels
  private static boolean sqlPlan = true;        // should execution plan come from v$sql_plan
  private static String thisVersion = "18.00";  // the version of RichMon
  static int numScratchPanelsAdded = 0;              // the number of scratch panels that have been added
  static int numScratchPanelsRemoved = 0;
  static int numScratchLabels=0;
  static int numSessionPanelsAdded = 0;              // the number of scratch panels that have been added
  static int numSessionPanelsRemoved = 0;
  static int numSQLIdPanelsAdded = 0;
  static int numSQLIdPanelsRemoved = 0;  
  private static Vector sessionV = new Vector(); // a pointer to each sessionPanel displayed in the consoleWindow
  private static Vector ashV = new Vector();     // a pointer to each ashPanel displayed in the consoleWindow
  private static String currentSchema = "";      // used in alter session set current schema on session & scratch panels
  private JFrame listFrame;
  private static boolean racDb = false;
  private static boolean onlyLocalInstanceSelected = true;  
  private static boolean onlyLocalContainerSelected = true;
  private static String[][] allInstances;
  private static String[][] selectedInstances;    // stores the inst_id of selected instances in the instances menu
  private static String[][] allContainers;
  private static String[][] selectedContainers;    // stores the con_id of selected instances in the instances menu
  private static int mySid=0;                     // the sid of this database session (only populated for 10g and above by the session panels)
  private static String databaseName;
  private static long databaseId;                 // inst_id of the instance initially connected too
  private static JTabbedPane sqlIdPaneTP;
  private static String usernameFilter;           // username to filter by when applicable
  private static Boolean filteringByUsername = false;
  private static boolean exadata = false;
  private static boolean isCDB = false;
  
  

  /**
   * Constructor
   */
  public ConsoleWindow(ConnectWindow connectWindow, Double dbVersion) {
    this.connectWindow = connectWindow;
    this.dbVersion = dbVersion;

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    // check whether an upgrade to RichMon is available.
    checkForUpgrade();
   
/*     Commented out 8th April 2014 to remove functionlity for breaking charts out into seperate windows
       surely no one does that these days

     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     if (screenSize.width < 1025) {
//      ChartViewPanel.setBreakOutBox(true);
       overviewChartsSingleFrame.setSelected(true);
     }
*/
  }

  /**
   * Defines all the components that make up the frame.
   *
   * @throws Exception
   */
   private void jbInit() throws Exception {
    this.setVisible(false);

    // set borderLayout
    this.getContentPane().setLayout(borderLayout1);

    // menu
    createMenu();

    // create the tabs which appear in the consoleWindow tabbed pane
    databasePanel = new DatabasePanel();
    performancePanel = new PerformancePanel();
    textViewPanel = new TextViewPanel();
    chartViewPanel = new ChartViewPanel();
    statspackAWRPanel = new StatspackAWRPanel();
    sqlIdPanel = new SQLIdPanel();
    sessionPanel = new SessionPanel();
    scratchPanel = new ScratchPanel();

    // add the tabs to the consoleWindow
    consoleTP.add(databasePanel, "Database");
    consoleTP.add(performancePanel, "Performance");
    consoleTP.add(textViewPanel, "Text");
    consoleTP.add(chartViewPanel, "Chart");
    consoleTP.add(statspackAWRPanel,"Statspack");
    consoleTP.add(sqlIdPanel,"SQL");
    consoleTP.add(sessionPanel,"Session");
    consoleTP.add(scratchPanel,"Scratch");
    
    if (dbVersion < 9) customize.add(enableV$SQL_Plan);

    // add components to the frame
    this.getContentPane().add(consoleTP, BorderLayout.CENTER);

    // add the richmon icon to the frame title bar
   // this.setIconImage( new ImageIcon( ClassLoader.getSystemResource("RichMon.gif")).getImage());
    URL url = this.getClass().getClassLoader().getResource("RichMon.gif");
    this.setIconImage(new ImageIcon(url).getImage());
  }

  private void createMenu()
  {
    this.setJMenuBar(menuBar);
    changeConnection.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          fileReConnect();
        }
      });

//    waitSummarySessions.setSelected(true);
    save.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          fileSave();
        }
      });

    setCurrentSchema.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            setCurrentSchema();
          }
        });

    exit.addActionListener(new ActionListener() {
  	    public void actionPerformed(ActionEvent ae) {
  	      fileExit();
  	    }
  	  });

    helpAbout.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          helpAbout();
        }
      });

    openBlog.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          openBlog();
        }
      });

    editProperties.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          ConnectWindow.properties.edit();
        }
      });

    menuAWRStatspack.add(enablePerformanceReview);
    menuAWRStatspack.add(allowSnapshotsToSpanRestarts);
    menuAWRStatspack.add(unlimitedSnapshots);
    menuAWRStatspack.add(lobSampleSize);
    menuAWRStatspack.add(openAWROnImportedData);
    customize.add(cacheResultsWithNoRows);

    customize.add(menuAWRStatspack);
    customize.add(editProperties);
    customize.add(enableReFormatSQL);
    customize.add(flipSingleRowOutput);
    customize.add(breakOutChartsTabsFrame);
    customize.add(cacheResultsWithNoRows);


    enablePerformanceReview.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                enablePerformanceReviewOption();
              }
            });
    breakOutChartsTabsFrame.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                breakOutChartsTabsFrame();
              }
            });

    lobSampleSize.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                setLobSampleSize();
              }
            });

    customize.add(enableKillSession);
    enableKillSession.addActionListener(new ActionListener() {
  	    public void actionPerformed(ActionEvent ae) {
  	      enableKillSession_ActionPerformed();
  	    }
  	  });

    menuFile.add(customize);
    menuFile.add(save);
    menuFile.add(setCurrentSchema);
    menuFile.add(changeConnection);
    menuFile.add(exit);
    menuBar.add(menuFile);

    sessionKill.addActionListener(new ActionListener() {
  	    public void actionPerformed(ActionEvent ae) {
  	      sessionKill();
  	    }
  	  });

    cacheResultsWithNoRows.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          setCacheResultsWithNoRows();
        }
      });
    
    sessionStartTrace.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sessionStartTrace();
        }
      });

    sessionStopTrace.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sessionStopTrace();
        }
      });

    enableV$SQL_Plan.setSelected(true);
    enableV$SQL_Plan.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sessionV$SQL_Plan_ActionPerformed();
        }
      });

    menuSession.add(sessionStartTrace);
    menuSession.add(sessionStopTrace);
    menuSession.addSeparator();
    menuSession.add(sessionKill);
    sessionKill.setEnabled(false);
    cacheResultsWithNoRows.setSelected(Properties.isCacheResultsWithNoRows());
    
    launchPuttyTelnet.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        launchPutty("TELNET");
      }
    });

    launchPuttySSH.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        launchPutty("SSH");
      }
    });

    menuLaunch.add(launchPuttyTelnet);
    menuLaunch.add(launchPuttySSH);
    if (System.getProperty("os.name").indexOf("Windows") > -1) menuBar.add(menuLaunch);
    menuBar.add(menuSession);
    
    openAWROnImportedData.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (openAWROnImportedData.isSelected()) {
          additionalAWRPanel();
          openAWROnImportedData.setSelected(false);
        }
      }
    });
    
    
    help.add(helpAbout);
    help.add(openBlog);
    menuBar.add(help);
  }


  /**
   * Add a window listener to the consoleWindow to ensure that when the window
   * is closed, the ConnectWindow.getDatabase() connection is also closed, and any spooling is
   * also stoppped
   */
  private void addConsoleWindowListener() {
    this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          ConnectWindow.exitConnectWindow();
        }
      });
  }

  /**
   * Checks whether the hardcoded version is greater than that held in the
   * file "I:\\support_admin\\scripts\\richmon\\latestRelease.txt".  If so the
   * user is presented with a message dialog suggesting an upgrade.
   */
   private void checkForUpgrade() {
     try {

       File latestReleaseFile;

       if (Double.valueOf(thisVersion) >= 17) {
         latestReleaseFile = new File("I:\\support_admin\\scripts\\richmon\\upgradeToV17.txt");
       }
       else {
         latestReleaseFile = new File("I:\\support_admin\\scripts\\richmon\\upgradeToV15.txt");
       }

       BufferedReader latestRelease = new BufferedReader(new FileReader(latestReleaseFile));

       String latest = latestRelease.readLine();
       latest = latest.trim();
       double latestVersion = Double.valueOf(latest).doubleValue();
       String version = latest.substring(0,latest.indexOf(".")) + latest.substring(latest.indexOf("."));


       if (latestVersion > Double.valueOf(thisVersion).doubleValue()) {
         // prompt the user suggesting an upgrade
         String msg = "An upgrade to RichMon is available\n\nDo you want to Upgrade to RichMonV" + latest +
                      " now ?";

         int upgrade = JOptionPane.showConfirmDialog(this,msg,"Upgrade",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);

         // copy new jar file to c:\richmon\software\ and name if richmon.jar
         if (upgrade == 0) {
           try {
             String sourceF = "I:\\support_admin\\scripts\\richmon\\RichMonV" + version + ".jar";
             String destF;
             if (Double.valueOf(thisVersion) >= 17) {
               destF = ConnectWindow.getBaseDir() + "\\Software\\RichMon.jar";
             }
             else {
               destF = ConnectWindow.getBaseDir() + "\\Software\\RichMonV15.jar";
             }
             Process p = Runtime.getRuntime().exec("cmd /c copy " + sourceF + " " + destF);

             msg = "Upgrade complete.\n\nBe sure to run RichMon from a shortcut on your taskbar,\n" +
                   "which points to ....\\My Documents\\RichMon\\Software\\RichMon.jar. (for Version 17 and above)\n" +
                   "or ....\\MyDocuments\\RichMon\\Software\\RichMonV15.jar (for Version 15)\n\n" +
                   "Please re-start RichMon";

             JOptionPane.showMessageDialog(this,msg,"Upgrade Complete",JOptionPane.INFORMATION_MESSAGE);
             System.exit(0);
           }
           catch (IOException ee) {
             displayError(ee,this);
           }
         }
       }
     }
     catch (Exception e) {
       // Do not display an error here, as non vf sites will not have an i drive with this directory
      // displayError(e,this);
     }
   }

  /**
   * Menu File ReConnect actions
   */
  private static void fileReConnect() {
    // display the ConnectWindow
    connectWindow.setVisible(true);

    // set focus on the databasePanel
    databasePanel.requestFocus();
  }

  /**
   * Menu File Exit actions

   */
  private void fileExit() {
    try {
      ConnectWindow.tidyClose();
    }
    catch (Exception e) {
      displayError(e,this);
    }
  }

  /**
   * Menu Help About actions
   */
  private void helpAbout() {
    JOptionPane.showMessageDialog(this, new ConsoleWindow_AboutBoxPanel1(), "About", JOptionPane.PLAIN_MESSAGE);
  }


  /**
   * Query the db to obtain session details for a given sid <session id>
   *
   * @param sid
   * @return Vector
   */
  private Vector getSidDetails(int sid) {
    Vector resultSetRow = new Vector();
    try {
      // create a cursor and execute the sql
      String cursorId = "sessionDetails.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      if (myResult.getNumRows() == 1) {
        resultSetRow = myResult.getResultSetRow(0);
      }
    }
    catch (Exception e) {
      displayError(e,this);
    }

    return resultSetRow;
  }


  /**
   * Prompt for a sid <session id> and initiate 10046 tracing on that session
   */
  private void sessionStartTrace() {
    
    try {
      if (ConnectWindow.isSysdba()) {
        // prompt for the sid to trace
        String msg =
          "A 10046 trace will enabled on the specified session (not including wait events).\n\n" +
          "The chosen sid must be from THIS instance.\n\nEnter the SID:";
        String tmp = JOptionPane.showInputDialog(this, msg, "Session Trace", JOptionPane.QUESTION_MESSAGE);
        int sid = Integer.valueOf(tmp).intValue();

        int instanceNumber = getThisInstanceNumber();

        // check the sid exists
        int serial = SessionPanel.getSid(instanceNumber, sid);
        try {
          if (serial != 0) {
            // start trace
            msg = "Starting a 10046 trace on Sid " + sid + ", serial# " + serial;
            JOptionPane.showMessageDialog(this, msg, "Session Trace", JOptionPane.INFORMATION_MESSAGE);

            try {
              ConnectWindow.getDatabase().startSessionTrace(sid, serial, dbVersion);
            } catch (Exception e) {
              displayError(e, this);
            }
          } else {
            throw new NoSuchSessionExistsException("Sid: " + sid);
          }
        } catch (Exception e) {
          displayError(e, this);
        }
      } else {
        throw new InsufficientPrivilegesException("SYSDBA privilege required");
      }
    } catch (Exception ee) {
      ConsoleWindow.displayError(ee, this);
    }
  }

  /**
   * Prompt for a sid <session id> and stop 10046 tracing on that session
   */
  private void sessionStopTrace() {
    
    try {
      if (ConnectWindow.isSysdba()) {
        // prompt for the sid to trace
        String msg = "A 10046 trace on a session will be disabled.\n\nEnter the SID:";
        String tmp = JOptionPane.showInputDialog(this, msg, "Session Trace", JOptionPane.QUESTION_MESSAGE);
        int sid = Integer.valueOf(tmp).intValue();

        // check the sid exists
        int serial = SessionPanel.getSid(1, sid);
        try {
          if (serial != 0) {
            // stop trace
            msg = "Stopping a 10046 trace on Sid " + sid + ", serial# " + serial;
            JOptionPane.showMessageDialog(this, msg, "Session Trace", JOptionPane.INFORMATION_MESSAGE);

            try {
              ConnectWindow.getDatabase().stopSessionTrace(sid, serial, dbVersion);
            } catch (Exception e) {
              displayError(e, this);
            }
          } else {
            throw new NoSuchSessionExistsException("Sid: " + sid);
          }
        } catch (Exception e) {
          displayError(e, this);
        }
      }
      else {
        throw new InsufficientPrivilegesException("SYSDBA privilege required");
      }
    } catch (Exception ee) {
      ConsoleWindow.displayError(ee, this);
    }
  }

  /**
   * Prompt for a sid <session id> and kill that session in the db using 'alter system kill session'.
   * If connected as sysdba and the db is version 9 or greater then an audit entry will be written to the
   * alert log.  An audit of the kill is always written to 'c:\RichMon\KillLog' in html.
   */
  private void sessionKill() {
    // prompt for the sid to trace
//    String msg = "If connected as SYSDBA then an audit entry will be written to the alert log.\n\n" +
//                 "Enter the SID of the session you wish to kill:";
    String msg = "SYSDBA or DBA privileges are required...\n\nEnter the SID of the session you wish to kill:";
    String tmp = JOptionPane.showInputDialog(this,msg,"Kill Session",JOptionPane.QUESTION_MESSAGE);
    if (tmp instanceof String) {
      int sid = Integer.valueOf(tmp).intValue();

      // check the sid exists
      Vector resultSetRow = getSidDetails(sid);
      String serial = resultSetRow.elementAt(3).toString();
      try {
        if (!resultSetRow.elementAt(0).equals("-1")) {
          // get confirmation
          msg = "Killing Session " + sid + ", serial# " + serial;
          int opt = JOptionPane.showConfirmDialog(this,msg,"Kill Session",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);

          // kill session
          if (opt == 0) {
            String sql = "alter system kill session '" + sid + "," + serial + "'";
            try {
              // perform the kill
              int rows = ConnectWindow.getDatabase().executeUpdate(sql,false);

              // write a kill log to the RichMon log diectory
              boolean sessionTabExists = false;
              File killLog;
              if (ConnectWindow.isLinux()) {
                killLog = new File(ConnectWindow.getBaseDir() + "/KillLog/RichMon Killed Session " + sid + " on Instance " + instanceName + ".html");
              }
              else {
                killLog = new File(ConnectWindow.getBaseDir() + "\\KillLog\\RichMon Killed Session " + sid + " on Instance " + instanceName + ".html");
              }
              BufferedWriter killLogWriter = new BufferedWriter(new FileWriter(killLog));

              // get the sessionPanel for this sid
              int numSessionPanels = sessionV.size();
              SessionDetailPanel mySessionPanel;
              for (int i=0; i < numSessionPanels; i++) {
                mySessionPanel = (SessionDetailPanel) sessionV.get(i);
                if (mySessionPanel.sid == sid) {
                  // create an outputHTML process to write the kill log containing all QueryResult's from the last iteration
                  OutputHTML killLogOutputHTML = new OutputHTML(killLog, killLogWriter,mySessionPanel);
                  sessionTabExists = true;
                  break;
                }
              }

              if (!sessionTabExists) {
                killLogWriter.write("<html>\n");
                killLogWriter.write("<body>\n");
                killLogWriter.write("<table border cellspacing=0 cellpadding=1>");
                killLogWriter.write("<tr><td>Sid</td><td>" + resultSetRow.elementAt(1) + "</td></tr>\n");
                killLogWriter.write("<tr><td>SPid</td><td>" + resultSetRow.elementAt(0) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Username</td><td>" + resultSetRow.elementAt(2) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Serial#</td><td>" + resultSetRow.elementAt(3) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Logon Time</td><td>" + resultSetRow.elementAt(4) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Server</td><td>" + resultSetRow.elementAt(5) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Shared Server</td><td>" + resultSetRow.elementAt(6) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Dispatcher</td><td>" + resultSetRow.elementAt(7) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Status</td><td>" + resultSetRow.elementAt(8) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Program</td><td>" + resultSetRow.elementAt(9) + "</td></tr>\n");
                killLogWriter.write("<tr><td>Machine</td><td>" + resultSetRow.elementAt(10) + "</td></tr>\n");
                killLogWriter.write("</body>\n");
                killLogWriter.write("</html>\n");
              }

              // close the kill log
              killLogWriter.close();

              // inform user the kill succeeded
//              msg = "Session " + sid + " Killed\n\nA log of the session killed has been written to your RichMon log directory" +
//                    "\n\nIf you have sysdba privilege this has also been logged to the alert log.";
              msg = "Session " + sid + " Killed\n\nA log of the session killed has been written to your RichMon log directory.";

              JOptionPane.showMessageDialog(this,msg,"Kill Session",JOptionPane.INFORMATION_MESSAGE);

              if (ConnectWindow.isSysdba()) {
                try
                {
                  msg = "RichMon: Session Killed: Sid: " + sid + "\n" +
                        "                     Serial#: " + serial + "\n" +
                        "                    Username: " + resultSetRow.elementAt(2) + "\n" +
                        "                        SPid: " + resultSetRow.elementAt(0) + "\n" +
                        "                  Logon Time: " + resultSetRow.elementAt(4) + "\n" +
                        "                      Server: " + resultSetRow.elementAt(5) + "\n" +
                        "               Shared Server: " + resultSetRow.elementAt(6) + "\n" +
                        "                  Dispatcher: " + resultSetRow.elementAt(7) + "\n" +
                        "                      Status: " + resultSetRow.elementAt(8) + "\n" +
                        "                     Program: " + resultSetRow.elementAt(9) + "\n" +
                        "                     Machine: " + resultSetRow.elementAt(10);
 //                 ConnectWindow.getDatabase().writeMSGToAlert(msg);
                }
                catch (Exception eee) {
                  displayError(eee,this,"Writing audit message to the alert log");
                }
              }
            }
            catch (Exception ee) {
              displayError(ee,this);
            }
          }
        }
        else {
          throw new NoSuchSessionExistsException("Sid : " + sid);
        }
      }
      catch (Exception e) {
        displayError(e,this);
      }
    }
  }

  /**
   * Turns on or off the use of v$sql_plan for getting execution plans rather than
   * using explain plan according to the menu setting.
   */
  private void sessionV$SQL_Plan_ActionPerformed() {
    // check whether the menu item is selected or not
    if (enableV$SQL_Plan.isSelected()) {
      String msg = "This option causes execution plans to be extracted from v$sql_plan,\n" +
                   "rather than generated by 'explain plan ...'.\n\n" +
                   "However, if 2 different sessions have different plans, this option\n" +
                   "might not return the correct plan!";
      JOptionPane.showMessageDialog(this,msg,"Warning...",JOptionPane.WARNING_MESSAGE);
      sqlPlan = true;

      /*
       * need a loop here to go thru each sessionPanel in sessionV
       * and for each one ; centreP.remove(forceUserP);
       */
//      for (int i=0; i < sessionV.size(); i++) {
//        SessionPanel tmpSP = (SessionPanel)sessionV.elementAt(i);
//        tmpSP.setForceUser(false);
//      }
    }
    else {
      sqlPlan = false;
      /*
       * need a loop here to go thru each sessionPanel in sessionV
       * and for each one ; centreP.add(forceUserP, BorderLayout.North);
       */
//      for (int i=0; i < sessionV.size(); i++) {
//        SessionPanel tmpSP = (SessionPanel)sessionV.elementAt(i);
//        tmpSP.setForceUser(true);
//      }
    }
  }

  public static double getDBVersion() {
    return dbVersion;
  }

  /**
   * Query the db to find out what version the database is.
   *
   * Some features of RichMon are enabled/disabled depending on the database version.
   *
   * This is the only place in RichMon where this particular constructor for Cursor is used.  This sets the console window pointer
   * in all cursor objects because it is defined as static in that class.
   */
//  public void retrieveDBVersionFromDB() {
//    try {
//      String cursorId = "dbVersion.sql";
//      Cursor myCursor = new Cursor(cursorId,true,this);

//      QueryResult myResult = ExecuteDisplay.execute(myCursor,false,false,null);
//      Vector resultSetRow = myResult.getResultSetRow(0);

//      String wholeVer = resultSetRow.elementAt(0).toString();
//      dbVersion = Double.valueOf(wholeVer).doubleValue();

//      if (dbVersion < 8.1) consoleTP.remove(3);   // remove statspack panel

//      if (dbVersion >= 8.1) databasePanel.redoB.addItem("Log Switches by Hour");

//      if (dbVersion >= 9) {
//        databasePanel.databaseButtonP.add(databasePanel.createScriptB, null);
//        databasePanel.databaseButtonP.add(databasePanel.advisoryB, null);
//        databasePanel.undoB.addItem("Resumable Statements");
//        databasePanel.undoB.addItem("v$undostat");
//        databasePanel.undoB.addItem("v$fast_start_transactions");
//        databasePanel.undoB.addItem("v$fast_start_servers");
//        databasePanel.databaseSummaryB.addItem("Database Properties");
//        databasePanel.databaseSummaryB.addItem("DBA Registry");
//        databasePanel.sharedServerB.addItem("Shared Server Monitor");
//        databasePanel.sharedPoolB.addItem("Number of Subpools");
//        databasePanel.sharedPoolB.addItem("x$ksmlru");
        
//        customize.add(enableV$SQL_Plan);
//      }

//      if (dbVersion >= 10) {
//        databasePanel.databaseButtonP.add(databasePanel.alertsB, null);
//        databasePanel.addFlashFRAB();
//        databasePanel.addAsmB();
//        databasePanel.addStreamsB();
//        databasePanel.databaseSummaryB.addItem("Database Properties");
//        databasePanel.databaseSummaryB.addItem("High Water Mark Stats");
//        databasePanel.databaseSummaryB.addItem("Dba Feature Usage Stats");
//        databasePanel.databaseSummaryB.addItem("DBA Registry");
//        databasePanel.advisoryB.addItem("Java Pool Advice");
//        databasePanel.advisoryB.addItem("SGA Target Advice");
//        databasePanel.advisoryB.addItem("Streams Pool Advice");
//        databasePanel.advisoryB.addItem("dba_advisor_log");
//        databasePanel.statisticsB.addItem("System Statistics History");
//        databasePanel.statisticsB.addItem("Table Statistics History");
//        databasePanel.statisticsB.addItem("Index Statistics History");
//        databasePanel.statisticsB.addItem("Statistics Operations History");
//        databasePanel.statisticsB.addItem("sys.dba_tab_modifications");
//        databasePanel.spaceB.addItem("SYSAUX Space Usage");
//        chartViewPanel.optionsRP.add(chartViewPanel.osLoadCB); 
//        chartViewPanel.addWaitClassCB();
//        databasePanel.jobsSchedulerB.addItem("Scheduler Jobs");
//        databasePanel.jobsSchedulerB.addItem("Running Scheduler Jobs");
//        databasePanel.parametersB.addItem("SGA Dynamic Components");
//        databasePanel.parametersB.addItem("SGA Resize Ops");
//        performancePanel.addSQLProfileCB();
//        performancePanel.waitEventsB.addItem("Event Histogram");
//        performancePanel.addEnqCFContention();
//        databasePanel.undoB.addItem("Flashback Log");
//        databasePanel.undoB.addItem("Flashback Stat");
//        databasePanel.undoB.addItem("Resumable Statements");
//        databasePanel.undoB.addItem("Flash Recovery Area Usage");
//        databasePanel.sessionsB.addItem("Sessions - Active");
//        databasePanel.sessionsB.addItem("Sessions - Active Foreground");
//        performancePanel.planStabilityB.addItem("SQL Profiles");
//      }

//      if (dbVersion >= 10.2) {
//        databasePanel.statisticsB.addItem("Tables with Stale Stats");
//        databasePanel.databaseSummaryB.addItem("registry$history");
//        chartViewPanel.addIOPSCB();
//      }

//      if (dbVersion >= 11.0) {
//        databasePanel.jobsSchedulerB.addItem("Scheduler Job History");
//        databasePanel.jobsSchedulerB.addItem("Scheduler Windows");
//        databasePanel.asmB.addItem("v$asm_alias");
//        databasePanel.asmB.addItem("v$asm_attribute");
//        databasePanel.asmB.addItem("v$asm_client");
//        databasePanel.asmB.addItem("v$asm_disk_iostat");
//        databasePanel.asmB.addItem("v$asm_attribute");
//        databasePanel.asmB.addItem("v$asm_file");
//        databasePanel.asmB.addItem("v$asm_operation");
//        databasePanel.asmB.addItem("v$asm_template");
//        performancePanel.addSQLMonitorB();
//        textViewPanel.addSQLMonitorB();
//        performancePanel.planStabilityB.addItem("Plan Baseline Summary");
//        performancePanel.planStabilityB.addItem("All Plan Baselines");
//        performancePanel.planStabilityB.addItem("Plan Baselines Never Used");
//        performancePanel.planStabilityB.addItem("Show Plan Base Line in Detail");
//        performancePanel.sqlTuningTasksB.addItem("Show Automatic SQL Tuning Results");
//      }
      
//      if (dbVersion >= 11.2) {
//        chartViewPanel.addASMIOSTATCB(); 
//      }
      
//      if (dbVersion >= 12.0) {
//        databasePanel.databaseSummaryB.addItem("Containers");
//      }
      
//      schemaViewerPanel.runPopulateUserNames();
//    }
//    catch (Exception e) {
//      displayError(e,this);
//    }
//  }

  /**
   * If 'c:\program files\putty\putty.exe' exists, create a windows batch file 'c:putty.bat'
   * and executes it to startup putty against the host running the db
   */
  private void launchPutty(String mode) {
    boolean foundPutty = false;
    String hostName = "";

    try {
      // get the hostname
      String cursorId = "hostName.sql";
      QueryResult myResult = ExecuteDisplay.execute(cursorId, false, false,null);
      Vector resultSetRow = myResult.getResultSetRow(0);
      hostName = resultSetRow.elementAt(0).toString();

      // modify this hostName so that it works at Vodafone
      if (hostName.equals("maggie")) hostName="maggie-vcs";

      String puttyExe = "c:\\program files\\putty\\putty.exe";
      File puttyExeF = new File(puttyExe);
      if (!puttyExeF.exists()) throw new PuttyNotFoundException("Putty executable not cound in c:\\program file\\putty");

      String cmd = "c:\\program files\\putty\\putty.exe ";
      if (mode.equals("SSH")) cmd = cmd + "-ssh ";
      Process p = Runtime.getRuntime().exec(cmd + hostName);
    }
    catch (PuttyNotFoundException p){
      JOptionPane.showMessageDialog(this,p.toString(),"Error...",JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception e) {
      displayError(e,this);
    }
  }

  /**
   * Set instanceName
   */
  public void setInstanceName() {
    try {
      QueryResult myResult = ExecuteDisplay.execute("instanceName.sql", false, false,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      instanceName = resultSet[0][0];
      hostName = resultSet[0][1];
    } catch (Exception e) {
      displayError(e,this,"setInstanceName()");
    }
  }

  /**
   * Creates a new sessionPanel and adds a pointer to it, to the sessionV Vector.
   * Switches focus to the new sessionPanel.
   *
   * @param sid - session id
   */
   public static void addSessionPanel(int inst_id,int sid) {
     String combinedInstSid = String.valueOf(inst_id) + '|' + String.valueOf(sid);

     // search the sessionPanel vector to see if this panel already exists
     boolean exists = false;
     SessionDetailPanel tmpSessP = null;
     for (int i=0; i < sessionV.size(); i++) {
       tmpSessP = (SessionDetailPanel)sessionV.elementAt(i);
       String tmpCombinedInstSid = String.valueOf(tmpSessP.getInst_id()) + '|' + String.valueOf(tmpSessP.getSid());
   //      System.out.println("iteration thru sessionV : " + i + ": tmpCombinedInstSid :" + tmpCombinedInstSid + ":   combinedInstSid: " + combinedInstSid);
       if (tmpCombinedInstSid.equals(combinedInstSid))  {
         exists = true;
         break;
       }
     }

     if (exists) {
       // switch focus
       consoleTP.setSelectedIndex(6);
       SessionPanel.sessionTP.setSelectedComponent(tmpSessP);
     }
     else {
       JTabbedPane sessionTP = SessionPanel.sessionTP;
       // create the sessionPanel
       SessionDetailPanel sessionDetailPanel = new SessionDetailPanel(inst_id,sid);
       
       // add the sessionPanel to the consoleTP
       String label = inst_id + " | " + sid;
   //    try {
         sessionTP.add(sessionDetailPanel, label);
   //     } catch (Exception e) {
       
   //     }

       // add sessionPanel to sessionV Vector
       boolean ok = sessionV.add(sessionDetailPanel);

       // make this sessionPanel the focused tab
       consoleTP.setSelectedIndex(6);
       sessionTP.setSelectedComponent(sessionDetailPanel);
       try {
         sessionTP.setTabComponentAt(sessionTP.getSelectedIndex(),new ButtonTabComponent(SessionPanel.sessionTP));
       }
       catch (Exception e) {
         
       }
       numSessionPanelsAdded++;
     }
   }

/*  public static void addSQLDetailPanel(String sqlId,boolean userSelectedAWRInstance, JTabbedPane sqlIdPaneTP) {
    sqlIdPaneTP = sqlIdPaneTP;
    SQLDetailPanel sqlDetailPanel = new SQLDetailPanel(sqlId,userSelectedAWRInstance);
    
    try {
      sqlIdPaneTP.add(sqlDetailPanel, sqlId,numSQLIdPanelsAdded - numSQLIdPanelsRemoved);
    } catch (Exception e) {
      // ignore any errors here
    }


    // make this sessionPanel the focused tab
    consoleTP.setSelectedIndex(5);
    sqlIdPaneTP.setSelectedComponent(sqlDetailPanel);
    try {
      sqlIdPaneTP.setTabComponentAt(sqlIdPaneTP.getSelectedIndex(),new ButtonTabComponent(sqlIdPaneTP));
    }
    catch (Exception e) {
      
    }
    numSQLIdPanelsAdded++;
  }
*/
  public static void addSQLDetailPanel(String sqlId, JTabbedPane sqlIdPaneTP,long dbId, int instanceNumber,long sqlExecId,int startSnapId,int endSnapId, String[] snapDateRange) {

    SQLDetailPanel sqlDetailPanel = new SQLDetailPanel(sqlId,dbId,instanceNumber,sqlExecId,startSnapId,endSnapId,snapDateRange);
    
    try {
      sqlIdPaneTP.add(sqlDetailPanel, sqlId,numSQLIdPanelsAdded - numSQLIdPanelsRemoved);
    } catch (Exception e) {
      // ignore any errors here
    }


    // make this sessionPanel the focused tab
    consoleTP.setSelectedIndex(5);
    sqlIdPaneTP.setSelectedComponent(sqlDetailPanel);
    try {
      sqlIdPaneTP.setTabComponentAt(sqlIdPaneTP.getSelectedIndex(),new ButtonTabComponent(sqlIdPaneTP));
    }
    catch (Exception e) {
      
    }
    numSQLIdPanelsAdded++;
  }
  
  public static void addSQLDetailPanel(String hashValue, QueryResult resultSet, JTabbedPane sqlIdPaneTP) {
    SQLDetailPanel sqlDetailPanel = new SQLDetailPanel(resultSet,hashValue);
    
    try {
      sqlIdPaneTP.add(sqlDetailPanel, hashValue,numSQLIdPanelsAdded - numSQLIdPanelsRemoved);
    } catch (Exception e) {
      // ignore any errors here
    }


    // make this sessionPanel the focused tab
    consoleTP.setSelectedIndex(5);
    sqlIdPaneTP.setSelectedComponent(sqlDetailPanel);
    try {
      sqlIdPaneTP.setTabComponentAt(sqlIdPaneTP.getSelectedIndex(),new ButtonTabComponent(sqlIdPaneTP));
    }
    catch (Exception e) {
      
    }
    numSQLIdPanelsAdded++;
  }
  
  public static void removeSessionPanel(SessionDetailPanel sessionDetailPanel) {
    // remove entry from sessionV Vector
    boolean ok = sessionV.remove(sessionDetailPanel);

    // remove sessionPanel from consoleTP
    SessionPanel.sessionTP.remove(sessionDetailPanel);
    sessionDetailPanel.removeAll();

    // destroy the sessionPanel object
    sessionDetailPanel = null;
    
    numSessionPanelsRemoved++;
  }

  /**
   * Remove all sessionPanel's from the consoleWindow
   */
  public void removeAllSessionPanels() {
    Thread removeAllSessionPanelsThread = new Thread ( new Runnable() {
      public void run() {
        while (sessionV.size() > 0)
        {
          removeSessionPanel((SessionDetailPanel) sessionV.get(0));
        }
      }
    });

    removeAllSessionPanelsThread.setDaemon(false);
    removeAllSessionPanelsThread.setName("removeAllSessionPanels");
    removeAllSessionPanelsThread.start();
  }

  public void removeAllSQLDetailPanels() {
    SQLIdPanel.getSQL_IdPane().removeAll();  
  }
  
  
  /**
   * Creates a new ashPanel and adds a pointer to it, to the ashV Vector.
   * Switches focus to the new ashPanel.
   *
   * @param sid - session id
   */
  public static void addAshPanel(int sid,long serial, int instanceNumber) {
    // search the ashPanel vector to see if this panel already exists
    boolean exists = false;
    SessionASHPanel tmpASHP = null;
    for (int i=0; i < ashV.size(); i++) {
      tmpASHP = (SessionASHPanel)ashV.elementAt(i);
      if (tmpASHP.sid == sid)  {
        exists = true;
        break;
      }
    }

    if (exists) {
      // switch focus
      SessionPanel.sessionTP.setSelectedComponent(tmpASHP);
    }
    else {
      JTabbedPane sessionTP = SessionPanel.sessionTP;
      
      // create the ashPanel
      SessionASHPanel ashPanel = new SessionASHPanel(sid,serial,instanceNumber);
      
      // add the ashPanel to the consoleTP
      String label = "ASH " + instanceNumber + " | " + sid;
      try {
        sessionTP.add(ashPanel, label);
      } catch (Exception e) {
        
      }

      // add ashPanel to ashV Vector
      boolean ok = ashV.add(ashPanel);

      // make this ashPanel the focused tab
      sessionTP.setSelectedComponent(ashPanel);
      try {
        sessionTP.setTabComponentAt(numSessionPanelsAdded - numSessionPanelsRemoved,new ButtonTabComponent(sessionTP));
      } catch (Exception e) {
        
      }
      numSessionPanelsAdded++;
    }
  }

  public static void removeAshPanel(SessionASHPanel ashPanel) {
    // remove entry from ashV Vector
    boolean ok = ashV.remove(ashPanel);

    // remove ashPanel from consoleTP
    SessionPanel.sessionTP.remove(ashPanel);
    ashPanel.removeAll();

    // destroy the ashPanel object
    ashPanel = null;
    
    numSessionPanelsRemoved++;
  }

  /**
   * Remove all sessionPanel's from the consoleWindow
   */
  public void removeAllAshPanels() {
    Thread removeAllASHPanelsThread = new Thread ( new Runnable() {
      public void run() {
        while (sessionV.size() > 0)
        {
          removeAshPanel((SessionASHPanel) ashV.get(0));
          numSessionPanelsRemoved++;
        }
      }
    });

    removeAllASHPanelsThread.setDaemon(false);
    removeAllASHPanelsThread.setName("removeAllASHPanels");
    removeAllASHPanelsThread.start();
  }

  /**
   * Check the jar file for further information about a wait event.  The file
   * will end in '.html' and all spaces, asterisks and underline will have been
   * removed.
   *
   * @param eventName
   * @return String[] containing the note text
   * @throws java.lang.Exception
   * @Deprecated
   */
  public static String[] getNote(String eventName) throws Exception {
    // generate note name from event name
    StringBuffer tmp = new StringBuffer();
    for (int i=0; i < eventName.length(); i++) {
      char c = eventName.charAt(i);
      if ((c != ' ') & (c != '*')) tmp.append(c);
    }
    String noteName = tmp.toString().toLowerCase() + ".html";
    String noteText;

    try {
      // open file
      BufferedReader fileReader = new BufferedReader(new InputStreamReader(ConsoleWindow.class.getResourceAsStream("/" + noteName)));

      // read in each line in the sql file
      StringBuffer tmpSQL = new StringBuffer();
      for (String line; (line = fileReader.readLine()) != null;) {
        tmpSQL.append(line);
        tmpSQL.append("\n");
      }
      noteText = tmpSQL.toString();

      // close file
      fileReader.close();
    }
    catch (Exception NullPointerException) {
      throw new NoSuchNoteException("The note " + noteName + " was not found in jar file");
    }

    String [] note = new String[2];
    note[0] = noteName;
    note[1] = noteText;

    return note;
  }

  /**
   * Save to a file the output from each panel in the consoleWindow.  For each panel
   * cached results will be output in html format.
   *
   * The file name and directory can be specified by the user.
   */
  public void fileSave()
  {
    try
    {
      // construct the default file name using the instance name + current date and time
      StringBuffer currentDT = new StringBuffer();
      try {
        currentDT = new StringBuffer(ConnectWindow.getDatabase().getSYSDate());
      }
      catch (Exception e) {
        displayError(e,this)       ;
      }

      // construct a default file name
      String defaultFileName;
      if (ConnectWindow.isLinux()) {
        defaultFileName = ConnectWindow.getBaseDir() + "/Output/RichMon " + instanceName + " " + currentDT.toString() +  ".html";
      }
      else {
        defaultFileName = ConnectWindow.getBaseDir() + "\\Output\\RichMon " + instanceName + " " + currentDT.toString() +  ".html";
      }
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(new File(defaultFileName));
      File saveFile;
      BufferedWriter save;

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
          OutputHTML outputHTML = new OutputHTML(saveFile,save,"Database",databasePanel.getResultCache());
          outputHTML.savePanel("Performance",performancePanel.getResultCache(),null);
//          outputHTML.savePanel("Scratch",scratchPanel.getResultCache(),scratchPanel.getSQLCache());

          JTabbedPane scratchTP = ScratchPanel.getScratchTP();
          
          for (int i=0; i < scratchTP.getComponentCount(); i++) {
            try {
              System.out.println("i=" + i + "    class: " + scratchTP.getComponentAt(i).getClass());
              if (scratchTP.getComponentAt(i) instanceof ScratchDetailPanel) {
                ScratchDetailPanel t = (ScratchDetailPanel)scratchTP.getComponentAt(i);
                System.out.println("Saving: " + scratchTP.getTitleAt(i));
                outputHTML.savePanel(scratchTP.getTitleAt(i), t.getResultCache(), t.getSQLCache());
              }
            } catch (Exception e) {
              // do nothing 
            }
          }
        }
        catch (Exception e) {
          displayError(e,this);
        }

        // close the file
        save.close();
      }
    }
    catch (IOException e)
    {
      displayError(e,this);
    }
  }

  /**
   * Stop spooling in each sessionPanel pointed to in the vector sessionV.
   */
  public void stopSpooling() {
    int numSessionPanels = sessionV.size();

    // for each sessionPanel stop spooling
    for (int i=0; i < numSessionPanels; i++) {
      // obtain a pointer to the sesionPanel
      SessionDetailPanel myPanel = (SessionDetailPanel) sessionV.get(i);
      // instruct the panel to stop spooling
      if (myPanel.currentlySpooling) myPanel.fileClose();
    }

    // stop the overview panel spooling
    textViewPanel.fileClose();
  }

  /**
   * Set the allowSnapshotsToSpanRestarts menu item
   */
  public void setAllowSnapshotsToSpanRestarts(boolean AllowSnapshotsToSpanRestarts) {
    allowSnapshotsToSpanRestarts.setSelected(AllowSnapshotsToSpanRestarts);
  }

  /**
   * Set the unlimitedSnapshots menu item
   */
  public void setUnlimitedSnapshots(boolean unlimitedSnapshots) {
    ConsoleWindow.unlimitedSnapshots.setSelected(unlimitedSnapshots);
  }

  /**
   * Add or Remove the kill session option from the session menu depending on the menu setting
   */
  private void enableKillSession_ActionPerformed() {
    if (enableKillSession.isSelected()) {
      // add kill option to the session menu
      sessionKill.setEnabled(true);
    }
    else {
      // remove kill option from the session menu
      sessionKill.setEnabled(false);
    }
  }

  public void addSchemaViewer() {
    // remove an existing schema browser panel if one exists
    if (schemaViewerPanel instanceof SchemaViewerPanel) consoleTP.remove(schemaViewerPanel);
    schemaViewerPanel = new SchemaViewerPanel();
    consoleTP.add(schemaViewerPanel, "Schema Viewer",7);
  }

  public void setEnableKillSession(boolean enableKillSession) {
    ConsoleWindow.enableKillSession.setSelected(enableKillSession);
    sessionKill.setEnabled(enableKillSession);
  }

  public void setFlipSingleRowOutput(boolean flipSingleRowOutput) {
    ConsoleWindow.flipSingleRowOutput.setSelected(flipSingleRowOutput);
  }

  public boolean getFlipSingleRowOutput() {
    return ConsoleWindow.isFlipSingleRowOutput();
  }

  public void setReFormatSQL(boolean reFormatSQL) {
    enableReFormatSQL.setSelected(reFormatSQL);
  }

  /**
   * Add a new scratch panel
   */
  public static void addScratchPanel() {
    scratchDetailPanel = new ScratchDetailPanel();
    JTabbedPane scratchTP = ScratchPanel.getScratchTP();
    numScratchLabels++;
    
    boolean debug = false;
    if (debug) { 
      System.out.println("prior numScratchPanels " + numScratchPanelsAdded);
      System.out.println("prior num Components " + scratchTP.getComponentCount());
      System.out.println("numScratchPanelsRemoved " + numScratchPanelsRemoved);
    }


    try {
      scratchTP.add(scratchDetailPanel, "Scratch " + numScratchLabels,numScratchPanelsAdded - numScratchPanelsRemoved);
    } catch (Exception e) {
      // ignore any errors here
    }
    
    if (debug) System.out.println("panel added");
    
    if (numScratchPanelsAdded >= 1) scratchTP.setSelectedComponent(scratchDetailPanel);
    try {
      scratchTP.setTabComponentAt(numScratchPanelsAdded - numScratchPanelsRemoved, new ButtonTabComponent(scratchTP));
    } catch (Exception e) {
      //ignore any errors here
    }
    numScratchPanelsAdded++;
    
    
    if (debug) { 
      System.out.println("post numScratchPanels " + numScratchPanelsAdded);
      System.out.println("post num Components " + scratchTP.getComponentCount());
    }

  }

//  private static void addButtonToScratchPanel(int scratchPanelIndex) {
//    JTabbedPane scratchTP = ScratchPanel.getScratchTP();
//    scratchTP.setTabComponentAt(scratchPanelIndex, new TabButton("string"));
//  }

  /**
   * Add a new scratch panel and populate the sql area with sql
   *
   * @param sql
   */
  public static void addScratchPanel(String sql) {
    scratchDetailPanel = new ScratchDetailPanel();
    JTabbedPane scratchTP = ScratchPanel.getScratchTP();
     numScratchLabels++;
     
     boolean debug = false;
     if (debug) { 
       System.out.println("prior numScratchPanels " + numScratchPanelsAdded);
       System.out.println("prior num Components " + scratchTP.getComponentCount());
       System.out.println("numScratchPanelsRemoved " + numScratchPanelsRemoved);
     }
  
  
     try {
       scratchTP.add(scratchDetailPanel, "Scratch " + numScratchLabels,numScratchPanelsAdded - numScratchPanelsRemoved);
     } catch (Exception e) {
       // ignore any errors here
     }
     
     if (debug) System.out.println("panel added");
     
     if (numScratchPanelsAdded > 1) scratchTP.setSelectedComponent(scratchDetailPanel);
     try {
       scratchTP.setTabComponentAt(scratchTP.getSelectedIndex(), new ButtonTabComponent(scratchTP));
     } catch (Exception e) {
       //ignore any errors here
     }
     numScratchPanelsAdded++;
     
     
     if (debug) { 
       System.out.println("post numScratchPanels " + numScratchPanelsAdded);
       System.out.println("post num Components " + scratchTP.getComponentCount());
     }
    scratchDetailPanel.setSQLTA(sql);
 }

  public void setStatspackPanelNameAWR() {
    if (dbVersion >= 10 && Properties.isAvoid_awr() == false)
      consoleTP.setTitleAt(4,"AWR");
  }


  public static void openBlog() {
    String richMonURL = "http://richmon.blogspot.com";
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop myDesktop = Desktop.getDesktop();
        URI uri = new URI(richMonURL);
        myDesktop.browse(uri);
      }
      else {
        Process p = Runtime.getRuntime().exec("c:\\program files\\internet explorer\\iexplore " + richMonURL);
      }
    }
    catch (Exception e) {
      displayError(e,ConnectWindow.getConsoleWindow());
    }
  }

  
  public static void removeScratchPanel(int i) {
    boolean debug = false;
    
    JTabbedPane scratchTP = ScratchPanel.getScratchTP();
    Component scratchC = scratchTP.getComponentAt(i);

    try {
      if (numScratchPanelsAdded - numScratchPanelsRemoved > 1) {
        if (scratchC instanceof ScratchDetailPanel) {  
          ScratchDetailPanel myScratchPanel = (ScratchDetailPanel)scratchC;
          scratchTP.remove(myScratchPanel);
    //          numScratchPanels--;

          //  destroy the scratchPanel object
          myScratchPanel.removeAll();
          myScratchPanel = null;
        }
        numScratchPanelsRemoved++;
        if (debug) System.out.println("numScratchPanelsRemoved " + numScratchPanelsRemoved);
      }
      else {
        throw new CannotRemoveLastScratchPanelException("Cannot remove this panel");
      }
    }
    catch (CannotRemoveLastScratchPanelException e) {
      displayError(e,ConnectWindow.getConsoleWindow());
    }
  }

  public static void removeSQLDetailPanel(SQLDetailPanel sqlDetailPanel, JTabbedPane sqlIdPaneTP) {
    sqlIdPaneTP.remove(sqlDetailPanel);
    numSQLIdPanelsRemoved++;
  }

  //
  private void enablePerformanceReviewOption() {
    if (enablePerformanceReview.isSelected()) {
      StatspackAWRPanel.addPerformanceReviewButton();
    }
    else {
      StatspackAWRPanel.removePerformanceReviewButton();
    }
  }

  public void setEnablePerformanceReviewOptions(boolean enablePerformanceReviewOptions) {
      enablePerformanceReview.setSelected(enablePerformanceReviewOptions);
  }

  public static String getInstanceName() {
    return instanceName;
  }

  private void breakOutChartsTabsFrame() {
    Properties.setPlaceOverviewChartsIntoASingleFrame(breakOutChartsTabsFrame.isSelected());
  }

//  public void setPlaceOverviewChartsIntoASingleFrame(boolean set) {
//    overviewChartsSingleFrame.setSelected(set);
//  }





  public static String getRichMonVersion() {
    return thisVersion;
  }

  public static void displayError(Exception e, Object o) {
    StringBuffer errMsg = new StringBuffer("Error in " + o.getClass().getName() + " : " + e.toString() + "\n");

    String stackTrace = Properties.getStackTrace();
    if (!stackTrace.equals("none")) {
      StackTraceElement[] myStack = e.getStackTrace();
      for (int i = 0; i < myStack.length; i++)  {
        if (myStack[i].toString().startsWith("RichMon") || Properties.getStackTrace().equals("full")) {
          errMsg.append("\n" + myStack[i].toString());
        }
        else {
          break;
        }
      }
    }

    JOptionPane.showMessageDialog(consoleTP, errMsg, "Error...", JOptionPane.ERROR_MESSAGE);
  }

  public static void displayError(Exception e, Object o, String message) {
    StringBuffer errMsg = new StringBuffer("Error in " + o.getClass().getName() +  " : " + message + " : " + e.toString() + "\n");

    String stackTrace = Properties.getStackTrace();
    if (!stackTrace.equals("none")) {
      StackTraceElement[] myStack = e.getStackTrace();
      for (int i = 0; i < myStack.length; i++)  {
        if (myStack[i].toString().startsWith("RichMon") || Properties.getStackTrace().equals("full")) {
          errMsg.append("\n" + myStack[i].toString());
        }
        else {
          break;
        }
      }
    }

    JOptionPane.showMessageDialog(consoleTP, errMsg, "Error...", JOptionPane.ERROR_MESSAGE);
  }

  public static QueryResult getServerName() {
    QueryResult myResult = new QueryResult();
    try {
      String cursorId = "databaseSummary.sql";
      myResult = ExecuteDisplay.execute(cursorId,false,false,null);
    }
    catch (Exception e) {
      displayError(e,ConnectWindow.getConsoleWindow());
    }

    return myResult;
  }

//  public void resetStatspackPanel() {
//    statspackAWRPanel.startSnapshotCB.removeAllItems();
//    statspackAWRPanel.endSnapshotCB.removeAllItems();
//  }

  public void setCurrentSchema() {
    String msg = "Enter a schema name here, and it will be used in the 'alter session set current schema' \ncommand prior " +
        "to an execution plan being produced in a session or scratch panel.";

    if (currentSchema.length() >0) {
      msg = msg +  "\n\nCurrent Schema is currently set to " + currentSchema;

      msg = msg +  "\n\nDo you wish to change this, unset this or leave it as is?";

      String[] options = {"Change","Unset","Cancel"};

      int n = JOptionPane.showOptionDialog(this,msg,"Set Current Schema...",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);

      if (n == 1) currentSchema = new String();
      if (n == 0) {
        String value = JOptionPane.showInputDialog(this,msg,"Schema Name...",JOptionPane.QUESTION_MESSAGE);
        if (currentSchema.length() > 0) currentSchema = value;
      }
    }
    else {
      String value = JOptionPane.showInputDialog(this,msg,"Schema Name...",JOptionPane.QUESTION_MESSAGE);

      if (value instanceof String) currentSchema = value;
    }
  }

  public void setLobSampleSize() {
    String msg = "Enter a lob sample size here.  This is the numebr of rows (at the beginning of the table) that will " +
      "be scanned to determine the average length of a lob.  Zero will disable this function.";


    msg = msg +  "\n\nLob Sample Size is currently set to " + Properties.getLobSampleSize();

    msg = msg +  "\n\nDo you wish to change this, reset to the default (1000) or leave it as is?";

    String[] options = {"Change","Unset","Leave"};

    int n = JOptionPane.showOptionDialog(this,msg,"Set Lob Sample Size...",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);

    /*
     * n=0    is the change option
     * n=1    is the reset to default option
     * n=2    is the leave it option
     */
    if (n == 1) Properties.setLobSampleSize("1000");
    if (n == 0) {
      msg = "Set the Lob Sample Size to: ";
      String value = JOptionPane.showInputDialog(this,msg,"Set Lob Sample Size...",JOptionPane.QUESTION_MESSAGE);
      if (value instanceof String) Properties.setLobSampleSize(value);
    }
  }

  public static String getCurrentSchema() {
    return currentSchema;
  }

  public void saveFrameRef(JFrame listFrame) {
    this.listFrame = listFrame;
  }

  public void removeLastFrame() {
    if (listFrame instanceof JFrame) listFrame.dispose();
  }

  public void checkRAC() {
    Thread checkRACThread = new Thread ( new Runnable() {
      public void run() {

      try {
        setDatabaseNameAndId();
        
        // remove existing entries from the instances menu
        filterMenu.removeAll();

        String cursorId = "listInstances.sql";
        QueryResult myResult = ExecuteDisplay.execute(cursorId,false,false,null);

        String[][] resultSet = myResult.getResultSetAsStringArray();

        allInstances = new String[myResult.getNumRows()][3];
        for (int i = 0; i < myResult.getNumRows(); i++)  {
          allInstances[i][0] = resultSet[i][0];
          allInstances[i][1] = resultSet[i][1];
          allInstances[i][2] = "not selected";
        }

        if (myResult.getNumRows() > 1) {
          racDb = true;
          SessionPanel.addInstanceFields();
          
        }

        // add AWRInstance Panels
        for (int i=0; i < allInstances.length; i++) {
          StatspackAWRPanel.addInstancePanel(Integer.valueOf(allInstances[i][0]).intValue(),allInstances[i][1],databaseId,false,null);
        }
        
        if (debug) {
          for (int i = 0; i < myResult.getNumRows(); i++)  {
            System.out.println("Instance Id: " + allInstances[i][0] + " Instance Name: " + allInstances[i][1]);
          }
        }

        filterMenu.add(filterByUsername);
        filterByUsername.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent ae) {
                    askForUsernameFilter();
                  }
                });
        
        filterMenu.addSeparator();
        
        filterMenu.add(selectAllInstancesMI);
        selectAllInstancesMI.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent ae) {
                    selectAllInstancesOnMenu(filterMenu);
                  }
                });

        filterMenu.add(deselectAllInstancesMI);
        deselectAllInstancesMI.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent ae) {
                    deSelectAllInstancesOnMenu(filterMenu);
                  }
                });


        for (int i = 0; i < myResult.getNumRows(); i++)  {
          final JCheckBoxMenuItem myMenuItem = new JCheckBoxMenuItem(allInstances[i][1]);
          if (allInstances[i][1].toLowerCase().equals(instanceName.toLowerCase())) {
            myMenuItem.setSelected(true);
            allInstances[i][2] = "selected";
          }

          myMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                      instanceMenu_ActionPerformed(ae, myMenuItem.getText());
                    }
                  });
          if (myResult.getNumRows() ==1) myMenuItem.setEnabled(false);
          filterMenu.add(myMenuItem);
        }
        
        filterMenu.addSeparator();
        
        // add containers to the menu
        allContainers = ConnectWindow.getContainers();
        
        for (int i=0; i < allContainers.length; i++) {
          final JCheckBoxMenuItem myMenuItem = new JCheckBoxMenuItem(allContainers[i][1]);
          if (allContainers[i][1].equals(ConnectWindow.getContainerName())) {
            myMenuItem.setSelected(true);
          }
          
          myMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                      pdbMenu_ActionPerformed(ae, myMenuItem.getText());
                    }
                  });

          if (allContainers.length == 1) myMenuItem.setEnabled(false);
          filterMenu.add(myMenuItem);
          
 
        }
        menuBar.add(filterMenu,3);
        

        findSelectedContainers();
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Error obtaining a list of instances for this db.");
      }
    }
    });

    checkRACThread.setName("checkRAC");
    checkRACThread.setDaemon(true);
    checkRACThread.start();
  }

  public static long getDatabaseId() {
    return databaseId;
  }
  
  private void instanceMenu_ActionPerformed(ActionEvent ae, String instanceName) {
    // invert the selected status of this instance name in the instances array
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][1].equals(instanceName)) {
        if (allInstances[i][2].equals("selected")) {
          allInstances[i][2] = "not selected";
        }
        else {
          allInstances[i][2] = "selected";
        }
      }
    }

    findSelectedInstances();

    // reset any cursors in the crusor cache that have been modified for rac

    if (debug) {
      System.out.println("\nList of the instances array: ");
      for (int i = 0; i < allInstances.length; i++) {
        System.out.println(allInstances[i][0] + "  " + allInstances[i][1] + "   " + allInstances[i][2]);
      }
    }
  }
  private void pdbMenu_ActionPerformed(ActionEvent ae, String containerName) {
    // invert the selected status of this instance name in the instances array
    for (int i = 0; i < allContainers.length; i++) {
      if (allContainers[i][1].equals(containerName)) {
        if (allContainers[i][2].equals("selected")) {
          allContainers[i][2] = "not selected";
        }
        else {
          allContainers[i][2] = "selected";
        }
      }
    }

    findSelectedContainers();

  }

  public static boolean isDbRac() {
    return racDb;
  }

  public static void findSelectedInstances() {
    // find number of selected instances
    int selected = 0;
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][2].equals("selected")) selected++;
    }

    // create an array of just the selected instances
    selectedInstances = new String[selected][3];
    int j = 0;
    onlyLocalInstanceSelected = true;
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][2].equals("selected")) {
        selectedInstances[j][0] = allInstances[i][0];
        selectedInstances[j][1] = allInstances[i][1];
        selectedInstances[j][2] = allInstances[i][2];
        j++;
        if (!allInstances[i][1].toLowerCase().equals(getInstanceName().toLowerCase())) onlyLocalInstanceSelected = false;
      }
    }
  }

  public static String[][] getAllInstances() {
    return allInstances;
  }

  public static String[][] getSelectedInstances() {
    return selectedInstances;
  }

  public static boolean isOnlyLocalInstanceSelected() {
    return onlyLocalInstanceSelected;
  } 
  
  public static boolean isOnlyLocalContainerSelected() {
    return onlyLocalContainerSelected;
  }

  private void selectAllInstancesOnMenu(JMenu instanceMenu) {
    for (int i = 2; i < instanceMenu.getItemCount(); i++) {
      instanceMenu.getItem(i).setSelected(true);
    }

    for (int i = 0; i < allInstances.length; i++) {
      allInstances[i][2] = "selected";
    }

    findSelectedInstances();
  }

  private void deSelectAllInstancesOnMenu(JMenu instanceMenu) {
    for (int i = 2; i < instanceMenu.getItemCount(); i++) {
      if (debug) System.out.println("deselecting all instance: " + instanceMenu.getItem(i).getText());
      if (!instanceMenu.getItem(i).getText().toLowerCase().equals(instanceName.toLowerCase())) instanceMenu.getItem(i).setSelected(false);
    }

    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][1].toLowerCase().equals(instanceName.toLowerCase())) {
        allInstances[i][2] = "selected";
      }
      else {
        allInstances[i][2] = "not selected";
      }
    }

    findSelectedInstances();
  }

  public static int getMySid() {
    return mySid;
  }

  public static void setMySid(int sid) {
    mySid = sid;
  }

  public static int getInstanceNumber(String instanceName) {
    int inst_id = 0;
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][1].equalsIgnoreCase(instanceName)) {
        inst_id = Integer.valueOf(allInstances[i][0]).intValue();
        break;
      }
    }

    return inst_id;
  }

  public static String getInstanceName(int instanceNumber) {
    String instanceName = "not found";
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][0].equals(String.valueOf(instanceNumber))) {
        instanceName = String.valueOf(allInstances[i][1]);
        break;
      }
    }

    return instanceName;
  }

  public static int getThisInstanceNumber() {
    int inst_id = 0;
    for (int i = 0; i < allInstances.length; i++) {
      if (allInstances[i][1].equalsIgnoreCase(instanceName)) {
        inst_id = Integer.valueOf(allInstances[i][0]).intValue();
        break;
      }
    }

    return inst_id;
  }

  public void setModule() {
    try {
      ConnectWindow.getDatabase().setModule(thisVersion);
      ConnectWindow.getDatabase().setClientInfo(thisVersion);
    } catch (Exception e) {
      displayError(e,this,"Cannot set module or client info");
    }
  }

  private void setDatabaseNameAndId() {
    try {
      Cursor myCursor = new Cursor("databaseName.sql", true);
      QueryResult myResult = ExecuteDisplay.execute(myCursor, false, false, null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      databaseName = resultSet[0][0];
      databaseId = Long.valueOf(resultSet[0][1]).longValue();
    }
    catch (Exception e) {
      displayError(e,this,"setDatabaseName");
    }
  }

  public static String getDatabaseName() {
    return databaseName;
  }

  public static boolean isAllowSnapshotsToSpanRestarts() {
    return allowSnapshotsToSpanRestarts.isSelected();
  }

  public static boolean isUnlimitedSnapshots() {
    return unlimitedSnapshots.isSelected();
  }

  public void disableAWRPanel(boolean enabled) {
    consoleTP.setEnabledAt(4,enabled);
  }  
  
  public void disableSQLIdPanel(boolean enabled) {
    consoleTP.setEnabledAt(5,enabled);
  }
  
  public static boolean isSQLIDPanelEnabled() {
    return consoleTP.isEnabledAt(5);
  }


  public static DatabasePanel getDatabasePanel() {
    return databasePanel;
  }

  public static PerformancePanel getPerformancePanel() {
    return performancePanel;
  }

  public static boolean isFlipSingleRowOutput() {
    return flipSingleRowOutput.isSelected();
  }

  public static boolean isEnableReFormatSQL() {
    return enableReFormatSQL.isSelected();
  }

  public static JTabbedPane getConsoleTP() {
    return consoleTP;
  }

  public static boolean isSqlPlan() {
    return sqlPlan;
  }
  
  public static SQLIdPanel getSQLIdPanel() {
    return sqlIdPanel;
  }
  
  public String getHostName() {
    return hostName;
  }
  
  public static  int getNumberOfInstances() {
    return allInstances.length;
  }
  
  public static void removeStatspackAWRPanel() {
    consoleTP.remove(statspackAWRPanel);
  }
  
  private void additionalAWRPanel() {
    
    // get a list of all the imported AWR dbid's
    try {
      Cursor myCursor = new Cursor("stats$importedAWRdbid.sql", true);
      QueryResult myResult = ExecuteDisplay.execute(myCursor, false, false, null);
      if (myResult.getNumRows() > 0) {
        String[][] resultSet = myResult.getResultSetAsStringArray();

        Object[] dbids = new Object[resultSet.length];
        for (int i = 0; i < resultSet.length; i++) {
          dbids[i] = resultSet[i][0];
        }

        String s = (String)JOptionPane.showInputDialog(this, "Complete the sentence:\n" +
            "Enter the dbid for the AWR data you wish a view", "Customized Dialog", JOptionPane.PLAIN_MESSAGE, null, dbids, dbids[0]);
        
        String p = JOptionPane.showInputDialog(this,"Enter a prefix which will appear on the new tab to help you identify the dataset.","Enter a prefix",JOptionPane.QUESTION_MESSAGE);
        String prefix = "";
        if (p != null && p.length() > 0) prefix = p;

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
          long selectedDBID = Long.valueOf(s).longValue();
          
          // add AWRInstance Panels

          Cursor myCursor2 = new Cursor("stats$importedAWR.sql", true);
          QueryResult myResult2 = ExecuteDisplay.execute(myCursor2, false, false, null);
          String[][] myResultSet2 = myResult2.getResultSetAsStringArray();
          
          for (int i=0; i < myResultSet2.length; i++) {
            long dbid = Long.valueOf(myResultSet2[i][0]).longValue();
            int inum = Integer.valueOf(myResultSet2[i][2]).intValue();
            String iname = myResultSet2[i][1].toString();
            if (selectedDBID == dbid) StatspackAWRPanel.addInstancePanel(inum,iname,dbid,true,prefix);
          }
        }
      }
      else {
        JOptionPane.showMessageDialog(this, "No imported AWR snapshots found!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      
    } 
    catch (Exception e) {
      displayError(e,this,"getting imported AWR dbid");
    }
  }
  
  private void setCacheResultsWithNoRows() {
    if (cacheResultsWithNoRows.isSelected()) {
      Properties.setCacheResultsWithNoRows(true);
    }
    else {
      Properties.setCacheResultsWithNoRows(false);
    }
  }
  
  private void askForUsernameFilter() {
    
    String message = "Enter a username.\n\nThis will be applied as a filter to restrict rows on the Database, Performance and Text tab's where ever it is applicable.\n";
    String username = "";
    
    if (!filterByUsername.isSelected()) {
      username = JOptionPane.showInputDialog(this,message,"Username Filter",JOptionPane.QUESTION_MESSAGE);
    }
    
    if (username.trim() instanceof String && username.trim().length() > 0) {
      usernameFilter = username.trim().toUpperCase();
      filterByUsername.setText("Filtering by " + usernameFilter);
      filterByUsername.setSelected(true);
      filteringByUsername = true;
    }
    else {
      usernameFilter = "";
      filterByUsername.setText("Filter by Username");
      filterByUsername.setSelected(false);
      filteringByUsername = false;
    }
  }
  
  public static String getUsernameFilter() {
    return usernameFilter;
  }
  
  public static Boolean isFilteringByUsername() {
    return filteringByUsername;
  }

  public static void setCDB(Boolean cdb) {
    isCDB = cdb;
  }
  
  public static boolean isCDB() {
    return isCDB;
  }
  
  public static void findSelectedContainers() {
    // find number of selected instances
    int selected = 0;
    for (int i = 0; i < allContainers.length; i++) {
      if (allContainers[i][2].equals("selected")) selected++;
    }

    // create an array of just the selected instances
    selectedContainers = new String[selected][3];
    int j = 0;
    onlyLocalContainerSelected = true;
    for (int i = 0; i < allContainers.length; i++) {
      if (allContainers[i][2].equals("selected")) {
        selectedContainers[j][0] = allContainers[i][0];
        selectedContainers[j][1] = allContainers[i][1];
        selectedContainers[j][2] = allContainers[i][2];
        j++;
        if (!allContainers[i][1].toLowerCase().equals(ConnectWindow.getContainerName().toLowerCase())) onlyLocalContainerSelected = false;
      }
    }
  }
  
  public static String[][] getSelectedContainers() {
    return selectedContainers;
  }
  
  public static int getNumSelectedContainers() {
    return selectedContainers.length;
  }
  
  public static int getNumContainers() {
    return allContainers.length;
  }

}
