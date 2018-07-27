/*
 * ConnectWindow.java        13.0 05/01/04
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
 * 21/02/05 Richard Wright Swithed comment style to match that
 *                         recommended in Sun's own coding conventions.
 * 07/06/05 Richard Wright Added LogWriter output
 * 08/06/05 Richard Wright Added support for the properties file
 * 09/06/05 Richard Wright Deprecated exitConnectWindow() and improved logging
 * 10/06/05 Richard Wright Made the sysdba boolean public and static so that it
 *                         can be referenced by other classes
 * 13/06/05 Richard Wright added support for a properties file entry of
 *                         'defaultStatspackSchema'
 * 17/08/05 Richard Wright Re-ordered elements of ConnectB_actionPerformed() to
 *                         reduce perceived startup time and ensure the config
 *                         file is read earlier in the process
 * 18/08/05 Richard Wright Added a new parameter to the config file 'avoid_awr'
 *                         for use when perfstat is preferred over awr.
 * 16/09/05 Richard Wright Added a new parameter to the config file to allow
 *                         statspack wait event charts to be adjusted by snapshot
 *                         length.
 * 03/10/05 Richard Wright Scratch panels are now only added if no previous
 *                         connection existed
 * 10/10/05 Richard Wright Removed the config parameter allowing statspack/awt
 *                         results to be adjusted by snapshot length
 * 25/10/05 Richard Wright Added config property allowing the disablement of the
 *                         prompt to save output on exit - requested by Sally Cowan
 * 01/11/05 Richard Wright Remove all support for dbarep database connections & queries
 * 04/11/05 Richard Wright Added the currentVersion file and brought
 *                         createPropertiesFile() up to date
 * 10/11/05 Richard Wright Added query to find if cursor_sharing is set to force
 *                         on a 9.2 db because if so, execution plans should not
 *                         be generated using dbms_xplan.
 * 28/11/05 Richard Wright Added functionality to ensure the service name
 *                         displayed is the last one used.
 * 09/12/05 Richard Wright Added config property to allow unlimited numbers of
 *                         snapshots to be selected in the statspack/AWR panels
 *                         and allowing snapshots to span instance startups
 * 02/02/06 Richard Wright Added creation of the 'Performance Review Output'
 *                         directory.
 * 08/02/06 Richard Wright Added the config parameter 'vlts_testing' to enable
 *                         specific options for monitoring vlts testing
 * 08/02/06 Richard Wright Added the config parameter 'place_windows_into_a_single_panel'.
 * 08/02/06 Richard Wright Made all the boolean for configuration parameters private
 *                         and created the required accessor methods.
 * 23/03/06 Richard Wright Replace references to 'c:' with a call to
 *                         ConnectWindow.getBaseDir() and isLinux()
 * 22/06/06 Richard Wright Added the config parameter 'enable_performance_review_options'
 * 05/07/06 Richard Wright Added the config parameters 'timeout_operations' &
 *                         'timeout_execution_plans'
 * 12/07/06 Richard Wright Added the config parameters 'additonal_window_width'
 *                         and 'additional_window_height'
 * 13/07/06 Richard Wright Increased the size of the array for holding service names from 100 to 1000
 * 13/07/06 Richard Wright Added the config parameter 'cache_size'
 * 03/07/06 Richard Wright Added the config parameter 'dynamic_chart_domain_labels'
 *                         and 'dynamic_chart_domain_label_font_size'
 * 08/08/06 Richard Wright Set the size of the consoleWindow explicity otherwise
 *                         it does not display on hp-ux.
 * 08/08/06 Richard Wright Service names are not stored as 'service:hostname:port'
 *                         where known so that users do not have to remember so much
 *                         connection information.
 * 10/08/06 Richard Wright Changed maximizeConsoleWindow to ensure it is properly sized on all platforms
 * 17/08/06 Richard Wright Added the config parameter stack_trace
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 14/09/06 Richard Wright Removed vlts_testing parameter
 * 06/12/06 Richard Wright Added support for the config parameter defaultUserNameToOSUser
 * 07/12/06 Richard Wright Added support for SGA_TARGET
 * 14/12/06 Richard Wright Added support for sox checking
 * 15/12/06 Richard Wright Convert config file parameters and values to lowercase before checking
 * 15/12/06 Richard Wright Fixed bug stopping ServiceNames directory from being created
 * 15/12/06 Richard Wright Allow #'s in the config file as comments lines
 * 16/03/07 Richard Wright Allow a dummy instance name to be set
 * 14/05/07 Richard Wright Modify cursorCache invocation following change to the constructor in that class
 * 15/05/07 Richard Wright Remove the saveServerNames method and it's call
 * 18/05/07 Richard Wright Added support for the parameters: language
 *                                                           locale
 *                                                           date
 *                                                           dateTimestamp
 *                                                           allow_alter_session
 *                                                           allow_alter_system
 *                                                           allow_alter_database
 * 19/10/07 Richard Wright No longer set username to sys when selecting sysdba connection
 * 07/11/07 Richard Wright Added support for the parameter extract_Dba_Jobs_In_Perf_Review
 * 19/11/07 Richard Wright Closed properties file after creation
 * 20/11/07 Richard Wright Modified so that currentVersion.txt is only updated for with greater version numbers
 * 10/12/07 Richard Wright Removed db info pages links
 * 10/12/07 Richard Wright added the ssh_with_putty option
 * 10/12/07 Richard Wright Added support for ssh_with_putty parameter
 * 15/01/08 Richard Wright Removed the parameter 'merge_idrive'
 * 15/01/08 Richard Wright Removed the parameter 'maximize_width_of_the_wait_event_summary_chart'
 * 15/01/08 Richard Wright Set default to false for parameter 'allow_alter_session'
 * 31/01/08 Richard Wright Added the RichMon Icon to the frame titlebar
 * 31/01/08 Richard Wright Set the default to false for 'placeOverviewChartsIntoASingleFrame'
 * 04/02/08 Richard Wright Removed the ssh with putty option
 * 23/06/08 Richard Wright Add lob sample size parameter and removed option to turn on and off
 *                         session overlay on wait events chart
 * 14/11/08 Richard Wright The connection is disconnected before a new one is established
 * 18/11/08 Richard Wright Instance name for the connection is queried rather than taken from the service name used on the connect window
 * 30/05/09 Richard Wright Added support for the defaultEvents parameter
 * 15/07/09 Richard Wright Better traverse service names that use thick driver
 * 09/12/09 Richard Wright Question the user to confirm they are licensed for DIAGNOSTIC and TUNING packs
 * 08/03/10 Richard Wright Addded the standardButtonDefaultSize parameter - changed the startup order to allow for this
 * 29/03/10 Richard Wright Added parameters to allow AWR/Statspack buttons to be modified
 *   /03/10 Richard Wright Allow the use to specify RICHMON_BASE in windows
 * 09/04/10 Richard Wright Moved baseDir and linux over from ConsoleWindow
 * 29/04/10 Richard Wright Moved some fucntionality over to Properties
 * 29/04/10 Richard Wright Moved some fucntionality over to ServiceNames
 * 02/09/10 Richard Wright Correctly set isLinux to true on linux systems
 * 02/12/10 Richard Wright Added cursor warning methods
 * 14/02/12 Richard Wright Added service name or sid radio group
 * 18/07/12 Richard Wright Ensure statspackAWRPanel not visible in 8i db's
 * 15/02/14 Richard Wright After connect is clicked it is now disabled for a short time to prevent multiple clicks
 * 11/12/15 Richard Wright Setting jdbc cache size to 100 and not doing any calculation
 * 24/10/16 Richard Wright No longer attempts to hide the RichMonBase.txt file
 * 28/11/17 Richard Wright Stopping setting locale when parameter file contains set_locale=false  (because it stopped connections at Thomas Cook)
 * 28/11/17 Richard Wright No longer disable AWR and SQL panels when avoid_awr is specified because these panels will use statspack instead
 * 14/02/18 Richard Wright Get the container name when the connection is established
 */ 


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;


/**
 * A Connect window allowing the user to enter the details required to
 * create a JDBC database connection.  Optionally a thin jdbc connection
 * can be selected which allows the hostname and port number to be specified.
 *
 * @author Richard Wright
 */
public class ConnectWindow extends JFrame  {
  // layouts
  private FlowLayout flowLayout1 = new FlowLayout();

  // panels
  private JPanel centerP = new JPanel();
  private JPanel titleP = new JPanel();
  private JPanel tagLineP = new JPanel();
  private JPanel connectDetailsP = new JPanel();
  JScrollPane serviceNamesSP = new JScrollPane();
  JSplitPane splitPane = new JSplitPane();

  // centerP panel will contain all of the following panels

  // titleP panel will contain
  private JLabel titleL = new JLabel();

  // username P panel will contain
  private JLabel usernameL = new JLabel();
  private static JTextField usernameT = new JTextField();

  // passwordP panel will contain
  private JLabel passwordL = new JLabel();
  private static JPasswordField passwordF = new JPasswordField();

  // serviceNameP panel will contain
  ButtonGroup otherRBG = new ButtonGroup();
  private static JTextField serviceNameTF = new JTextField();
  private static JTextField sidTF = new JTextField();
  public static JCheckBox serviceNameCB = new JCheckBox();
  public static JCheckBox sidCB = new JCheckBox();

  // hostNameP panel
  private JLabel hostNameL = new JLabel("Host");
  private static JTextField hostNameTF = new JTextField();

  // portNumberP panel
  private JLabel portNumberL = new JLabel("Port ");
  static private JTextField portNumberTF = new JTextField();

  // sysdbaP panel will contain
  static JCheckBox sysdbaCB = new JCheckBox();
  static JLabel sysdbaL = new JLabel();

  // connectP panel will contain
  private JButton connectB = new JButton();
  private JButton cancelB = new JButton();

  // tagLine panel
  private JLabel tagLineL = new JLabel("    www.richmon4oracle.com           richmon.blogspot.com    ");

  // other objects
  private static Database database;
  private static ConsoleWindow consoleWindow;
  public static Properties properties;

  static File configFile;
  static File currentVersionFile;
  static File licenseFile;

  // misc
  static private boolean sysdba = false;
  private boolean previousConnection = false;

  private static boolean thickDriver = true;
  private static  boolean bloggedAlready=true;
  private static boolean cursorSharingExact=true;
  private static boolean cursorWarningAlreadyGiven=false;
  boolean exampleDB = false; // swap the real instance name for "exampleDB"
  private static ImageIcon richMonIcon;
  boolean connected = false;
  boolean debug = false;
  JButton historyB = new JButton("History");
  final JFrame listFrame = new JFrame("Service Names");
  private static boolean diagnosticPackLicensed = false;
  static String baseDir;
  static boolean linux = false;
  private static String serviceNameOrSID = "";
  private static boolean connectInProgress = false;
  private static String containerName;



  /**
   * Constructor
   *
   * @param args
   */
  public ConnectWindow(String[] args) {

    try {
      jbInit();

      /*
       * Arguments to Richmon
       *
       * 1) The base directory (which defaults to 'c:\RichMon')
       * 2) Looks and Feel ie 'Metal' or 'Default'
       * 3) Service Name
       *
       * It is not possible to specify 2 & 3 without the preceding arguments.
       *
       * Hint:  The second parameter is set in RichMon.java
       */



      checkOS();
      properties = new Properties();
      if (args.length > 0) {
        if (debug) System.out.println("Setting base dir in console window when more than 0 args");
        setBaseDir(args[0]);
        if (debug) System.out.println("Done setting base dir in console window when more than 0 args");
      }
      else {
        boolean replaceHiddenFile = false;
        createBaseDir(replaceHiddenFile);
      }

      checkDirAndConfigFiles();
      readCurrentVersionFile();
      try {
        ServiceNames.getServiceNamesFromDisk();
        DisplayServiceNames();
        
      }
      catch (FileNotFoundException e) {
        ServiceNames.setServiceNamesFileExists(false);
      }
      catch (Exception e) {
        String errorMsg = "Populating Service Names...\n\n" + e.toString();
        JOptionPane.showMessageDialog(this,errorMsg,"Error...",JOptionPane.ERROR_MESSAGE);
      }

      Properties.readPropertiesFile(configFile);
//      consoleWindow = new ConsoleWindow(this);


      if (args.length == 3) {
        ServiceNames.setPreSetServiceName(true);
        ServiceNames.setImportedServiceName(args[2].toLowerCase());
      }

      

      // create other required objects
      database = new Database();
      database.setMaxResultSetSize(Properties.getMaxResultSetSize());

//      consoleWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//      consoleWindow.addWindowListener(new WindowAdapter()
//        {
//          public void windowClosing(WindowEvent e)
//          {
//              try {
//                tidyClose();
//              }
//              catch (Exception f) {
//                 ConsoleWindow.displayError(f,this);
//              }
//            }
//        });

//      maximizeConsoleWindow();

//      licenseCheck();
      purgeAWRCache();
    }
    catch (Exception e) {
      // depending on where we exception occurred the console window might not yet exist
      if (consoleWindow instanceof ConsoleWindow) {
        ConsoleWindow.displayError(e,this);
      }
      else {
         displayError(e);
      }
    }
  }

  public static void populateChosenServiceName(String serviceName,String hostName,String portNumber,String username,Boolean sid) {
    if (sid) setSID(serviceName);
    if (!sid) setServiceName(serviceName);
    
    hostNameTF.setText(hostName);
    portNumberTF.setText(portNumber);
    usernameT.setText(username);

  }
  
  public void displayError(Exception e) {
    // this is the code from consoleWindow.displayError(....), so remember to keep it in sync
    StringBuffer errMsg = new StringBuffer("Error in " + this.getClass().getName() + " : " + e.toString() + "\n");

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

    JOptionPane.showMessageDialog(ConsoleWindow.getConsoleTP(), errMsg, "Error...", JOptionPane.ERROR_MESSAGE);
  }

  public static void displayError(Exception e, String method) {
    // this is the code from consoleWindow.displayError(....), so remember to keep it in sync
    StringBuffer errMsg = new StringBuffer("Error in ConnectWindow : " + method + " : " + e.toString() + "\n");

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

    JOptionPane.showMessageDialog(ConsoleWindow.getConsoleTP(), errMsg, "Error...", JOptionPane.ERROR_MESSAGE);
  }

  private void setOptionsInConsoleWindow()
  {
    consoleWindow.setReFormatSQL(Properties.isReFormatSQLInSessionPanels());
    consoleWindow.setFlipSingleRowOutput(Properties.isFlipResultSet());
    consoleWindow.setEnableKillSession(Properties.isEnableKillSession());
    consoleWindow.setUnlimitedSnapshots(Properties.isUnlimitedSnapshots());
    consoleWindow.setAllowSnapshotsToSpanRestarts(Properties.isAllowSnapshotsToSpanRestarts());
//    consoleWindow.setPlaceOverviewChartsIntoASingleFrame(Properties.isPlaceOverviewChartsIntoASingleFrame());
    consoleWindow.setEnablePerformanceReviewOptions(Properties.isEnablePerformanceReviewOptions());
  }


  /**
   * Prompt the user to save RichMon output, close all database connections, stop spooling and exit.
   */
  public static void tidyClose() throws Exception {
    if (!Properties.isAvoid_prompt_to_save_output_on_exit()) {
      int option = 5;
        String msg = "Do you want to save your RichMon output ? ";
        option = JOptionPane.showConfirmDialog(consoleWindow, msg, "Save...", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
          consoleWindow.fileSave();
        }
        if (option != JOptionPane.CANCEL_OPTION) {
          database.closeConnection();
          consoleWindow.stopSpooling();
          consoleWindow.dispose();
          System.exit(0);
        }
    }
    else {
        database.closeConnection();
        consoleWindow.stopSpooling();
        consoleWindow.dispose();
        System.exit(0);
    }
  }

  /**
   * Close the connection to the database
   */
  public static void closeDatabase() {
    try {
      database.closeConnection();
    }
    catch (Exception e) {
      displayError(e,"closeDatabase");
    }
  }

  /**
   * Defines all the components that make up the window
   */
   private void jbInit() throws Exception {
     this.getContentPane().setLayout(new BorderLayout());
     this.setTitle("Connect ...");
     this.setSize(new Dimension(1300, 460));
     this.getRootPane().setDefaultButton(connectB);

     // titleP panel
     titleL.setText("RichMon - Richards Monitoring Tool For Oracle");
     titleL.setForeground(new Color(153,0,0));
//     titleL.setForeground(Color.white);
     titleL.setFont(new Font("????", 1, 16));

     titleP.add(titleL);
     //titleP.setBackground(new Color(153,0,0));
     titleP.setBackground(Color.lightGray);
     titleP.setLayout(flowLayout1);

     // usernameP panel
     usernameL.setText("UserName");
     usernameT.setPreferredSize(new Dimension(110, 27));
     usernameL.setPreferredSize(new Dimension(110, 27));
     usernameT.setText("system");
     usernameL.setHorizontalAlignment(SwingConstants.RIGHT);

     // passwordP panel
     passwordL.setText("Password");
     passwordL.setPreferredSize(new Dimension(110, 27));
     passwordL.setHorizontalAlignment(SwingConstants.RIGHT);
     passwordF.setPreferredSize(new Dimension(110, 27));

     // serviceNameP panel
     otherRBG.add(serviceNameCB);
     otherRBG.add(sidCB);
     serviceNameCB.setSelected(true);
     
     serviceNameCB.setText("Service Name");
     serviceNameCB.setHorizontalTextPosition(SwingConstants.LEFT);
     serviceNameCB.setHorizontalAlignment(SwingConstants.RIGHT);
     serviceNameCB.setPreferredSize(new Dimension(130, 27));
     serviceNameCB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           serviceNameCB_actionPerformed();
         }
       });

     sidCB.setText("SID");
     sidCB.setHorizontalTextPosition(SwingConstants.LEFT);
     sidCB.setHorizontalAlignment(SwingConstants.RIGHT);
     sidCB.setPreferredSize(new Dimension(50, 27));
     sidCB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           sidCB_actionPerformed();
         }
       });
     

     // sysdbaP panel
     sysdbaCB.setText("sysdba");
     sysdbaCB.setHorizontalTextPosition(SwingConstants.LEFT);
     sysdbaCB.setHorizontalAlignment(SwingConstants.RIGHT);
     sysdbaCB.setPreferredSize(new Dimension(110, 27));
     sysdbaCB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           sysdbaCB_actionPerformed();
         }
       });

     // hostNameP panel
     hostNameTF.setPreferredSize(new Dimension(110,27));
     hostNameL.setPreferredSize(new Dimension(110,27));
    // hostNameL.setPreferredSize(new Dimension(50,21));
     hostNameL.setHorizontalAlignment(SwingConstants.RIGHT);

     // portNumberP panel
     portNumberL.setPreferredSize(new Dimension(110,27));
     portNumberL.setHorizontalAlignment(SwingConstants.RIGHT);
     portNumberTF.setPreferredSize(new Dimension(110,27));


     // tagLineP panel
     //tagLineP.setBackground(new Color(153,0,0));
     //tagLineL.setForeground(Color.white);
     tagLineL.setForeground(new Color(153,0,0));
     tagLineP.setBackground(Color.lightGray);
     tagLineP.add(tagLineL);

     // connectP panel
     connectB.setText("Connect");
     connectB.setForeground(Color.BLUE);
     connectB.setPreferredSize(new Dimension(110, 21));
     connectB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           connectB_actionPerformed();
         }
       });

     cancelB.setText("Cancel");
     cancelB.setForeground(Color.BLUE);
     cancelB.setPreferredSize(new Dimension(110, 21));
     cancelB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           cancelB_actionPerformed();
         }
       });

 //    connectP.add(connectB);
 //    connectP.add(cancelB);
 //    connectP.setLayout(flowLayout1);

     // center panel
/*     centerP.add(titleP);
     centerP.add(usernameP);
     centerP.add(passwordP);
     centerP.add(serviceNameP);
     centerP.add(sysdbaP);
     centerP.add(hostNameP);
     centerP.add(portNumberP);
     centerP.add(connectP);
     centerP.add(tagLineP); */
     
     
     
     connectDetailsP.setLayout(new GridBagLayout());
     GridBagConstraints connectCons = new GridBagConstraints();
     
     connectCons.gridx=0;
     connectCons.gridy=0;
     connectCons.insets = new Insets(3,3,3,3);
     connectCons.fill = GridBagConstraints.HORIZONTAL;
     connectDetailsP.add(usernameL, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(usernameT, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=1;
     connectDetailsP.add(passwordL, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(passwordF, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=2;
     //connectCons.anchor = GridBagConstraints.EAST;
     connectDetailsP.add(serviceNameCB, connectCons);
     connectCons.gridx=1;
     //  connectCons.anchor = GridBagConstraints.CENTER;
     connectDetailsP.add(serviceNameTF, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=3;
    // connectCons.anchor = GridBagConstraints.EAST;
     connectDetailsP.add(sidCB, connectCons);
     connectCons.gridx=1;
    //   connectCons.anchor = GridBagConstraints.CENTER;
     connectDetailsP.add(sidTF, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=4;
    // connectCons.gridwidth=2;
     connectDetailsP.add(sysdbaCB, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(sysdbaL,connectCons);
     connectCons.gridx=0;
     connectCons.gridy=6;
     connectCons.gridwidth=1;
     connectDetailsP.add(hostNameL, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(hostNameTF, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=7;
     connectDetailsP.add(portNumberL, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(portNumberTF, connectCons);
     connectCons.gridx=0;
     connectCons.gridy=9;
     connectDetailsP.add(connectB, connectCons);
     connectCons.gridx=1;
     connectDetailsP.add(cancelB, connectCons);
     
     centerP.add(connectDetailsP);
     splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,centerP, serviceNamesSP);
     splitPane.setDividerLocation(400);
     this.getContentPane().add(titleP, BorderLayout.NORTH);
     this.getContentPane().add(splitPane, BorderLayout.CENTER);
     this.getContentPane().add(tagLineP, BorderLayout.SOUTH); 
     
   }

  private void DisplayServiceNames() {
    String[] serviceNames = ServiceNames.getServiceNamesArray();
    
    // Construct the list of service names into a queryresult object so that it can be used to create an output table
    QueryResult myResult = new QueryResult();
    myResult.setNumCols(4);
    String[] colTypes = {"String","String","String","String","String"};
    myResult.setColumnTypes(colTypes);
    String[] resultSetHeadings = {"Service Name","Host Name","Port Number","Username","serviceName or SID"};
    myResult.setResultHeadings(resultSetHeadings);
    myResult.setIsOnlyLocalInstanceSelected(true);
    myResult.setCursorId("scratch.sql");
    int maxServiceNameLength=12;
    int maxHostNameLength=0;
    int maxPortNumberLength=12;
    int maxUsernameLength=12;
    int maxServiceNameorSIDLength=15;
    
    Vector row;
    for (int i=0; i < serviceNames.length; i++) {
      row = new Vector();
      boolean endOfRow = false;
    
      String t = serviceNames[i];
      row.add(t.substring(0, t.indexOf(":")));
      maxServiceNameLength = Math.max(maxServiceNameLength, t.substring(0, t.indexOf(":")).length());
      t = t.substring(t.indexOf(":") +1);
      
      row.add(t.substring(0, t.indexOf(":")));
      maxHostNameLength = Math.max(maxHostNameLength, t.substring(0, t.indexOf(":")).length());
      t = t.substring(t.indexOf(":") +1);
      
      row.add(t.substring(0, t.indexOf(":")));
      maxPortNumberLength = Math.max(maxPortNumberLength, t.length());
      t = t.substring(t.indexOf(":") +1);
      
      if (t.length() > 0 && !endOfRow) {
        row.add(t.substring(0, t.indexOf(":")));
      }
      else {
        row.add("");
      }
    
      maxUsernameLength = Math.max(maxUsernameLength, t.length());
      t = t.substring(t.indexOf(":") +1);
      
      row.add(t);
      
      
      myResult.addResultRow(row);
    }
    
    int[] columnwidths = {maxServiceNameLength,maxHostNameLength,maxPortNumberLength,maxUsernameLength,maxServiceNameorSIDLength};
    myResult.setColumnWidths(columnwidths);
    
    ExecuteDisplay.displayTable(myResult, serviceNamesSP, false, null);
  }


  /**
   * Closes the database connection.  Called when the application is closing.
   */
  public static void exitConnectWindow() {
    try {
      database.closeConnection();
    }
    catch (Exception e) {
      displayError(e,"exitConnectWindow");
    }
    System.exit(0);
  }

  /**
   * Maximize the size of the consoleWindow
   */
  private void maximizeConsoleWindow() {
    consoleWindow.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
    consoleWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);

    double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();

    if (consoleWindow.getWidth() < width) {
      consoleWindow.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
  }

  /**
   * This method is called after the components on the dialog have been realized,
   * but before it is shown.  It ensures that when the connectWindow is first
   * displayed the cursor will be in the password field
   */
  public void addNotify() {
    super.addNotify();
    passwordF.requestFocus();
  }

  /**
   * Initiates a new jdbc database connection to the database using the username,
   * password, service name specified by the user.
   *
   */
  private void connectB_actionPerformed() {
    
    if (this.isEnabled()) {
      this.setEnabled(false);   // prevents multiple connect clicks
      
      listFrame.setVisible(false);


      if (serviceNameCB.isSelected())
        serviceNameOrSID = serviceNameTF.getText();
      if (sidCB.isSelected())
        serviceNameOrSID = sidTF.getText();

      if (sysdbaCB.isSelected()) {
        sysdba = true;
      } else {
        sysdba = false;
      }

      /* open database connection */
      try {

        if (connected) {
          try {
            database.closeConnection();
          } catch (Exception e) {
            // do nothing
          }
          connected = false;
        }

        if (hostNameTF.getText().length() > 0 && portNumberTF.getText().length() > 0) {
          thickDriver = false;
        } else {
          thickDriver = true;
        }

        if (thickDriver) {
          connected =
              database.openConnection(serviceNameOrSID, usernameT.getText(), new String(passwordF.getPassword()),
                                      sysdba);
        } else {
          connected =
              database.openConnection(serviceNameOrSID, usernameT.getText(), new String(passwordF.getPassword()),
                                      sysdba, hostNameTF.getText(), portNumberTF.getText(),
                                      serviceNameCB.isSelected());
        }

        if (connected) {
          Double dbVersion = retrieveDBVersionFromDB();

          if (dbVersion > 12.0) containerName = getContainerNameFromDB();
          consoleWindow = new ConsoleWindow(this, dbVersion);
          consoleWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
          consoleWindow.addWindowListener(new WindowAdapter()
            {
              public void windowClosing(WindowEvent e)
              {
                  try {
                    tidyClose();
                  }
                  catch (Exception f) {
                     ConsoleWindow.displayError(f,this);
                  }
                }
            });
          
          if (dbVersion > 12.0) if (containerName.equals("CDB$ROOT")) consoleWindow.setCDB(true);
          
          maximizeConsoleWindow();
          licenseCheck();
          consoleWindow.setVisible(true);
          
          if (Properties.isSetLocale()) database.setLocale(Properties.getMyLocale(), Properties.getLanguage(), Properties.getDate(), Properties.getDateTimestamp());
          ServiceNames.save(serviceNameOrSID, this);
          if (exampleDB) serviceNameOrSID = "exampleDB";
          consoleWindow.setInstanceName();
          
          if (dbVersion > 12.0) {
            consoleWindow.setTitle("Container: " + containerName + "  Instance: " + ConsoleWindow.getInstanceName() + "  Host: " + consoleWindow.getHostName() + " :  RichMon - Richards Monitoring Tool For Oracle");
          }
          else {
            consoleWindow.setTitle("Instance: " + ConsoleWindow.getInstanceName() + "  Host: " + consoleWindow.getHostName() + " :  RichMon - Richards Monitoring Tool For Oracle");
          }
          consoleWindow.setModule();

          this.setVisible(false);

          if (!previousConnection) {
            setOptionsInConsoleWindow();
            if (Properties.isEnableSchemaViewer()) consoleWindow.addSchemaViewer();
            ConsoleWindow.addScratchPanel();
            previousConnection = true;
          }
          
          
          SchemaViewerPanel.runPopulateUserNames();



          if (ConsoleWindow.getDBVersion() == 9.2) getCursorSharing();
          if (ConsoleWindow.getDBVersion() < 9) ConsoleWindow.removeStatspackAWRPanel();
          consoleWindow.removeAllSessionPanels();
          consoleWindow.removeAllSQLDetailPanels();

          if (ConsoleWindow.getDBVersion() >= 10.0) consoleWindow.setStatspackPanelNameAWR();
          StatspackAWRPanel.removeAllInstancePanels();

          consoleWindow.checkRAC();
          getCursorCacheSize();

        } else {
          previousConnection = false;
        }

      } catch (Exception ee) {
        this.setEnabled(true);
        this.setVisible(true);
        displayError(ee);
      }
      
      this.setEnabled(true);
//      this.setVisible(false);
    }
    
  }

  /**
   * Cancels creation of a new jdbc database connection.  If no other connection
   * exists, RichMon is terminated, otherwise control is returned to the
   * console using the existing jdbc connection.
   */
  private void cancelB_actionPerformed() {
  	this.setVisible(false);
    if (!previousConnection) System.exit(0);
  }



  private void createBaseDir(boolean replaceHiddenFile) {
    // If a RichMon directory exists under myDocuments, then use that
    File oldBaseDir;
    File hiddenFile;

    if (!isLinux()) {
      oldBaseDir = new File(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "\\RichMon");
      hiddenFile = new File(FileSystemView.getFileSystemView().getDefaultDirectory() + "\\RICHMON_BASE.txt");
      if (debug) System.out.println("oldBaseDir: " + oldBaseDir);
      if (debug) System.out.println("hiddenFile: " + hiddenFile);
    }
    else {
      oldBaseDir = new File(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "//RichMon");
      hiddenFile = new File(FileSystemView.getFileSystemView().getDefaultDirectory() + "//.RICHMON_BASE.txt");
      if (debug) System.out.println("oldBaseDir: " + oldBaseDir);
      if (debug) System.out.println("hiddenFile: " + hiddenFile);
    }

    if (replaceHiddenFile && hiddenFile.exists()) {
      hiddenFile.delete();
    }


    /*
     * Check to see if RICHMON_BASE has been specified before in the hidden file, if so use the RICHMON_BASE specified
     * otherwise check for the existance of a richmon directory under the users default directory, if it exists then use that dir
     * otherwise prompt the user for a new location
     *
     * This means that new users will be prompted to specify an location for RICHMON_BASE, existing users will not be affected.
     *
     * None of this affects LINUX users who start RichMon from the commmand line and specify RICHMON_BASE as the first argument.
     */

    if (debug) {
      System.out.println("oldBaseDir exists: " + oldBaseDir.exists());
      System.out.println("hiddenFile exists: " + hiddenFile.exists());
    }

    if (!oldBaseDir.exists()) {
      if (!hiddenFile.exists() || replaceHiddenFile) {
        try {
          boolean ok = hiddenFile.createNewFile();
    
  //        if (!isLinux()) {
  //          try {
  //            Process p = Runtime.getRuntime().exec("attrib +h " + hiddenFile.getPath());
  //            p.waitFor();
  //          } catch (Exception e) {
  //            /* deliberately left blank so that windows users without admin rights still get the hidden file
  //             * just not hidden.  This traps the error and prevents users seeing it
  //             */
  //          }
  //        };

          // prompt the user to specify a location for RICHMON_BASE
          String msg = "RichMon needs a directory to store your RichMon.properties file and other saved data.\n" +
                       "The default location is under myDocuments on Windows and $HOME on Linux.";

          JOptionPane.showMessageDialog(this,msg,"Choose a location for $RICHMON_BASE",JOptionPane.INFORMATION_MESSAGE);


          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setSelectedFile(oldBaseDir);
          fileChooser.setDialogTitle("Choose a location in which the RichMon directory will be created...");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

          File newBaseDir = new File(oldBaseDir.toString());
          int option = fileChooser.showSaveDialog(this);
          if (option == JFileChooser.APPROVE_OPTION) {
            newBaseDir = fileChooser.getSelectedFile();

            System.out.println("Writing newBaseDir to hidden file: " + newBaseDir.toString());
            BufferedWriter myWriter = new BufferedWriter(new FileWriter(hiddenFile));
            myWriter.write(newBaseDir.toString());
            myWriter.close();

            File newDir;
            if (!isLinux()) {
              newDir = new File(newBaseDir + "\\RichMon");
            }
            else {
              newDir = new File(newBaseDir.toString());
            }
            if (!newDir.exists()) {
              newDir.mkdir();
            }
          }



          if (!isLinux()) {
            setBaseDir(newBaseDir.toString() + "\\RichMon");
          }
          else {
            setBaseDir(newBaseDir.toString());
          }
        }
        catch (Exception e) {
          displayError(e,"Error creating new RICHMON_BASE directory or hidden file");
        }
      }
      else {
        // hidden file exists, so use that to specify RICHMON_BASE
        try {
          BufferedReader myReader = new BufferedReader(new FileReader(hiddenFile));
          if (!isLinux()) {
            setBaseDir(myReader.readLine().toString() + "\\RichMon");
          }
          else {
            setBaseDir(myReader.readLine().toString() + "//RichMon");
          }
        }
        catch (Exception ioe) {
          displayError(ioe, "Cannot read a valid location from RICHMON_BASE.txt");
        }
      }
    }
  }

   /**
   * Checks that the RichMon directory exists and if not creates the following directories:
   *
   *    $RichMon_Base\RichMon
   *    $RichMon_Base\ServiceNames
   *    $RichMon_Base\KillLog
   *    $RichMon_Base\Output
   *    $RichMon_Base\Scripts
   *    $RichMon_Base\Software
   *    $RichMon_Base\Config
   *
   *  On windows, RichMon_Base is the users 'my documents' directory, on linux it is specifed at startup
   *
   *  If a RichMon directory if found under c:\ then a warning is displayed as this is deprecated since 17.38
   */
  private void checkDirAndConfigFiles() throws IOException
  {
    if (debug) System.out.println("getBaseDir from checkDirAndConfigFiles: " + ConnectWindow.getBaseDir());
    File richMonDir = new File(ConnectWindow.getBaseDir());

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
      if (debug) System.out.println("RICHMON_BASE exists: " + ok);

      if (!ok) {
        boolean replaceHiddenFile = true;
        createBaseDir(replaceHiddenFile);
      }
    }

    System.out.println("checkDirAndConfigFiles isLinux " + isLinux());

    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/ServiceNames");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\ServiceNames");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }

    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/KillLog");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\KillLog");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }


    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Output");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Output");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Scripts");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Scripts");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Software");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Software");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Log");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Log");
    }

    if (richMonDir.exists()) {
      boolean ok = richMonDir.delete();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Performance Review Output");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Performance Review Output");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Config");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Config");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/AWR Reports");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\AWR Reports");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/ASH Reports");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\ASH Reports");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/ADDM Reports");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\ADDM Reports");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }


    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Recording");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Recording");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }    
    
    if (isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Cache");
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Cache");
    }

    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }



    if (isLinux()) {
      configFile = new File(ConnectWindow.getBaseDir() + "/Config/RichMon.properties");
    }
    else {
      configFile = new File(ConnectWindow.getBaseDir() +  "\\Config\\RichMon.properties");
    }

    if (!configFile.exists()) Properties.createPropertiesFile(configFile);



    if (isLinux()) {
      currentVersionFile = new File(ConnectWindow.getBaseDir() + "/Software/CurrentVersion.txt");
    }
    else {
      currentVersionFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\CurrentVersion.txt");
    }

    if (!currentVersionFile.exists()) createCurrentVersionFile();

    // Check whether an old richmon dir on c: needs migrating
/*    if (isLinux()) {
      richMonDir = new File("c://RichMon");
    }
    else {
      richMonDir = new File("c:\\RichMon");
    }

    if (richMonDir.exists()) {
      String msg = "On Windows, the default location for the RichMon directory is now under ''My Documents'' (as of RichMon V17.36).\n\n" +
                   "You may wish to remove ''c:\\RichMon'' as it will no longer be used. This means that your ''RichMon.properties'' file has been\n" +
                   "refreshed so review this file and re-make any customizations you had previously set.\n\n" +
                   "You may wish to consider moving the moving ''c:\\RichMon\\serviceNames'' to ''My Documents\\Richmon\\serviceNames'' to retain your history\n\n" +
                   "This message will appear each time RichMon starts if the ''c:\\RichMon'' directory exists.";

      JOptionPane.showMessageDialog(this,msg,"RichMon has moved...",JOptionPane.INFORMATION_MESSAGE);
    }

*/
  }

   private void licenseCheck() {

     if (isLinux()) {
       licenseFile = new File(ConnectWindow.getBaseDir() + "/Software/license.txt");
     }
     else {
       licenseFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\license.txt");
     }

     try {
       if (!licenseFile.exists()) {
         promptLicenseQuestion();
       }
       else {
         readLicenseInfo();
       }
     }
     catch (Exception e) {
       displayError(e, "Error with LicenseFile");
     }
   }

  private void promptLicenseQuestion() throws Exception {
    String msg = "Some functionality in RichMon requires that you have a license for the DIAGNOSTIC PACK.\n\n" +
                 "Please confirm whether you have a license for DIAGNOSTIC PACK.  If not, then the relevant functionality\n" +
                 "in RichMon will be disabled.  In particular the AWR tab will be disabled, but remember that you can still\n" +
                 "use statspack in your database and force RichMon to use it instead of AWR by changing the 'avoid_awr' entry\n" +
                 "in your 'RichMon.properties' file to true.\n\n" +
                 "After this question you will be asked whether you have a license for the Tuning Pack.\n\n" +
                 "These questions will be repeated each time you use a new version of RichMon.";

    Object[] options = {"YES, I Have a license for DIAGNOSTIC PACK",
                        "NO, I do not have a license for DIAGNOSTIC PACK"};

    int diagnosticResponse = JOptionPane.showOptionDialog(this,
                                         msg,
                                         "Diagnostic Pack",
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options,
                                         options[0]);


    String msg2 = "Some functionality in RichMon requires that you have a license for the TUNING PACK.\n\n" +
                 "Please confirm whether you have a license for TUNING PACK.  If not, then the relevant functionality\n" +
                 "in RichMon will be disabled.\n\n" +
                 "This question will be repeated each time you use a new version of RichMon.";

    Object[] options2 = {"YES, I Have a license for TUNING PACK",
                        "NO, I do not have a license for TUNING PACK"};

    int tuningResponse = JOptionPane.showOptionDialog(this,
                                         msg2,
                                         "Tuning Pack",
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options2,
                                         options2[0]);

    // create a license.txt file to store this information between restarts
    File licenseFile;
    if (isLinux()) {
      licenseFile = new File(ConnectWindow.getBaseDir() + "/Software/license.txt");
    }
    else {
      licenseFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\license.txt");
    }

    if (licenseFile.exists()) licenseFile.delete();
    boolean ok = licenseFile.createNewFile();
    BufferedWriter licenseFileWriter = new BufferedWriter(new FileWriter(licenseFile));

    if (diagnosticResponse==0) {
      // DIAGNOSTIC PACK Licensed
      licenseFileWriter.write("Y\n");
    }
    else {
      // DIAGNOSTIC PACK NOT Licensed
      licenseFileWriter.write("N\n");
    }

    if (tuningResponse==0) {
      // TUNING PACK licensed
      licenseFileWriter.write("Y\n");
    }
    else {
      // TUNING PACK NOT Licensed
      licenseFileWriter.write("N\n");
    }

    licenseFileWriter.close();


    // Disable functionality when DIAGNOSTIC PACK is not licensed.
    //readLicenseInfo();
  }

  public static void createCursorWarningFile() throws Exception {
    File cursorWarningFile;
    if (isLinux()) {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() + "/Software/cursorWarning.txt");
    }
    else {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\CursorWarning.txt");
    }
    
    if (cursorWarningFile.exists()) cursorWarningFile.delete();
    boolean ok = cursorWarningFile.createNewFile();
    BufferedWriter cursorWarningFileWriter = new BufferedWriter(new FileWriter(cursorWarningFile));

    cursorWarningFileWriter.write("The user have been warned about the Cursor: pin S wait on X button usage in 10g");
    cursorWarningFileWriter.close();

    readCursorWarningFile();
  }
  
  private void removeCursorWarningFile() {
    File cursorWarningFile;
    if (isLinux()) {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() + "/Software/cursorWarning.txt");
    }
    else {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\CursorWarning.txt");
    }
    
    if (cursorWarningFile.exists()) cursorWarningFile.delete();
  }
  
  private static void readCursorWarningFile() {
    File cursorWarningFile;
    if (isLinux()) {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() + "/Software/cursorWarning.txt");
    }
    else {
      cursorWarningFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\CursorWarning.txt");
    }
    
    if (cursorWarningFile.exists()) cursorWarningAlreadyGiven = true;
  }
  
  public static boolean isCursorWarningAlreadyGiven() {
    return cursorWarningAlreadyGiven;
  }
  
  private void readLicenseInfo() {

    try {
      if (isLinux()) {
        licenseFile = new File(ConnectWindow.getBaseDir() + "/Software/license.txt");
      }
      else {
        licenseFile = new File(ConnectWindow.getBaseDir() +  "\\Software\\license.txt");
      }
      
      BufferedReader licenseFileReader = new BufferedReader(new FileReader(licenseFile));

      String diagnosticLicense = licenseFileReader.readLine();
      String tuningLicense = licenseFileReader.readLine();

      if (diagnosticLicense.equalsIgnoreCase("Y")) {
        consoleWindow.disableAWRPanel(true);
        consoleWindow.disableSQLIdPanel(true);
        ConsoleWindow.getDatabasePanel().disableAlerts(true);
        diagnosticPackLicensed = true;
      }
      else {
//        consoleWindow.disableAWRPanel(false);
        ConsoleWindow.getDatabasePanel().disableAlerts(false);
//        consoleWindow.disableSQLIdPanel(false);
      }

      if (tuningLicense.equalsIgnoreCase("Y")) {
        ConsoleWindow.getPerformancePanel().disableSQLProfile(true);
      }
      else {
        ConsoleWindow.getPerformancePanel().disableSQLProfile(false);
      }
    }
    catch (FileNotFoundException fnfe) {
      displayError(fnfe, "Error Reading License Info");
    }
    catch (IOException ioe) {
      displayError(ioe, "Error Reading License Info");
    }
  }





  public static String getUsername() {
    return usernameT.getText();
  }






  /**
   * Creates a file to store the current version number of RichMon.  This is used
   * at startup to decide whether the blog needs to be displayed.
   *
   * @throws IOException
   */
  private void createCurrentVersionFile() throws IOException {
    boolean ok = currentVersionFile.createNewFile();

    // Write a default config set to the new config file
    BufferedWriter currentVersionFileWriter = new BufferedWriter(new FileWriter(currentVersionFile));

    currentVersionFileWriter.write(String.valueOf(ConsoleWindow.getRichMonVersion()));

    currentVersionFileWriter.close();

    // open up the blog
    if (Properties.isOpen_blog_on_first_use_of_a_release()) {
      ConsoleWindow.openBlog();
      bloggedAlready = true;
    }
  }

  private void readCurrentVersionFile() {
    try {
      BufferedReader currentVersionFileReader = new BufferedReader(new FileReader(currentVersionFile));

      String current = currentVersionFileReader.readLine();

      current = current.trim();
      double currentVersion = Double.valueOf(current).doubleValue();
      if (currentVersion < Double.valueOf(ConsoleWindow.getRichMonVersion()).doubleValue()) {
        if (Properties.isOpen_blog_on_first_use_of_a_release() && bloggedAlready == false) ConsoleWindow.openBlog();
        // re-create the currentVersion File with this version of RichMon
        boolean ok = currentVersionFile.delete();
//        if (currentVersion <= 17.51 &&  Double.valueOf(ConsoleWindow.getRichMonVersion()).doubleValue() >= 17.53  )  {
        if (currentVersion <= 17.51  )  {
          // upgrate the serviceNamesFile to the 17.52 format
          ServiceNames.upgradeTo1752Format();
        }
        if (currentVersion <= 17.53  )  {
          // upgrate the serviceNamesFile to the 17.52 format
          ServiceNames.upgradeTo1754Format();
        }
        createCurrentVersionFile();
        /*
         * As this is a new version of RichMon being used for the first time the following actions are required
         */
        promptLicenseQuestion();    
        removeCursorWarningFile();
      }
    }
    catch (Exception e) {
      displayError(e);
    }
  }

  /**
   * Called when the database is 9.2, to find out whether cursor sharing is set
   * to force.  If set to force dbms_xplan cannot be used to obtain execution
   * plans because of a bug that only affects 9.2.
   */
  private void getCursorSharing() {
    try {
      String cursorId = "databaseParameter.sql";

      /* array to hold parameters */
      Parameters myPars = new Parameters();
      myPars.addParameter("String","cursor_sharing");

      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      if (!resultSet[0][1].toLowerCase().equals("exact")) cursorSharingExact = false;
    }
    catch (Exception e) {
      displayError(e);
    }
  }



  public static boolean isSysdba() {
    return sysdba;
  }

  public static void setUserName() {
    usernameT.setText(System.getProperty("user.name"));
    Properties.setDefaultUserNameToOSUser(true);
  }

  public static void setUsername(String username) {
    usernameT.setText(username);
  }

  public void setRichMonIcon(ImageIcon myIcon) {
    richMonIcon = myIcon;
    this.setIconImage(richMonIcon.getImage());
  }

  public static ImageIcon getRichMonIcon() {
    return richMonIcon;
  }


  public void getCursorCacheSize() {
    Thread getCacheSizeThread = new Thread ( new Runnable() {
      public void run() {

      try {

//        String cursorId = "databaseParameter.sql";

//        Parameters myPars = new Parameters();
//        myPars.addParameter("String","open_cursors");

//        QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
//        String[][] resultSet = myResult.getResultSetAsStringArray();

        // set the cacheSize to the value of open_cursors minus 20, but not less than 5 and not more than 100
//        int openCursors = Integer.valueOf(resultSet[0][1]).intValue();
//        int cacheSize = Math.min(Math.max(5,openCursors - 20),100);
        
      // for test purposes setting cachesize to 100
        int cacheSize = 100;
        database.setStatementCacheSize(cacheSize);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Error finding out what open_cursors is set too.");
      }
    }
    });

    getCacheSizeThread.setName("getCacheSize");
    getCacheSizeThread.setDaemon(true);
    getCacheSizeThread.start();
  }



  static public String getServiceName() {
    if (serviceNameTF.getText().indexOf(":") > -1 ) {
      return serviceNameTF.getText().substring(0,serviceNameTF.getText().indexOf(":"));
    }
    else {
      return serviceNameTF.getText();
    }
  }

  static public String getPassword() {
    return new String(passwordF.getPassword());
  }

  static public boolean getSYSDBA() {
    return sysdba;
  }

  static public boolean isThickDriver() {
    return thickDriver;
  }

  static public String getHostName() {
    return hostNameTF.getText();
  }

  static public String getPortNumber() {
    return portNumberTF.getText();
  }

/*
  private void showHistory() {

    if (listFrame.isVisible()) {
        listFrame.setVisible(false);
    }
    else {
//      listFrame.removeAll();
      final JList myList = new JList(ServiceNames.getServiceNamesArray());
      myList.setVisibleRowCount(20);
      JScrollPane listScroller = new JScrollPane(myList);
      listFrame.add(listScroller);
      listFrame.pack();
      listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      Point myPoint = this.getLocationOnScreen();
      myPoint.setLocation(myPoint.getX() + this.getWidth(),myPoint.getY());
      listFrame.setLocation(myPoint);


      myList.addMouseListener(new MouseListener() {
          public void mouseClicked(MouseEvent e) {
            historyActionPerformed(myList.getSelectedValue().toString(), listFrame);
            listFrame.dispose();
          }
          public void mousePressed(MouseEvent e) {}
          public void mouseEntered(MouseEvent e) {}
          public void mouseReleased(MouseEvent e) {}
          public void mouseExited(MouseEvent e) {}
        });

      listFrame.setIconImage(ConnectWindow.getRichMonIcon().getImage());
      listFrame.setVisible(true);
    }
  }

  private void historyActionPerformed(String selectedServiceName,JFrame listFrame) {
    if (selectedServiceName.indexOf(":") < 0) {
      serviceNameTF.setText(selectedServiceName);
      hostNameTF.setText("");
      portNumberTF.setText("");
    }
    else {
      serviceNameTF.setText(selectedServiceName.substring(0,selectedServiceName.indexOf(":")));
      String t = selectedServiceName.substring(selectedServiceName.indexOf(":") +1);
      hostNameTF.setText(t.substring(0,t.indexOf(":")));
      String t2 = selectedServiceName.substring(selectedServiceName.indexOf(":"));
      portNumberTF.setText(t.substring(t.indexOf(":")+1));
    }

    listFrame.dispose();
  }
*/




  public static boolean getDiagnosticPackLicensed() {
    return diagnosticPackLicensed;
  }



  public static void setBaseDir(String newBaseDir) {
    System.out.println("Setting base directory to " + newBaseDir);
    baseDir = newBaseDir;
  }

  public static void checkOS() {
    System.out.println("O/S is " + System.getProperty("os.name"));

    if (System.getProperty("os.name").indexOf("Windows") == -1) {
      setLinux(true);
      System.out.println("Configuring for Linux/Unix");
    }
    else {
      setLinux(false);
      System.out.println("Configuring for Windows");
    }
  }

  public static String getBaseDir() {
    // if the baseDir has not be specified explicitly from the command line then set it to the users home directory in windows
    if (!(baseDir instanceof String) && System.getProperty("os.name").indexOf("Windows") > -1) {
      //baseDir = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "\\RichMon";
      setBaseDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "\\RichMon");
    }

    // if the baseDir has not been specified explicitly from the command line then set it to the users home directory in linux
    if (!(baseDir instanceof String) && System.getProperty("os.name").indexOf("Windows") == -1) {
      //baseDir = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "//RichMon";
      setBaseDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "//RichMon");
    }

    return baseDir;
  }

  public static boolean isLinux() {
    return linux;
  }

  public static void setLinux(boolean isLinux) {
    linux = isLinux;
  }

  public static Database getDatabase() {
    return database;
  }

  public static ConsoleWindow getConsoleWindow() {
    return consoleWindow;
  }

  public static boolean isCursorSharingExact() {
    return cursorSharingExact;
  }

  public static void setServiceName(String serviceName) {
    ConnectWindow.serviceNameTF.setText(serviceName);
    serviceNameCB_actionPerformed();
  }

  public static void setSID(String serviceName) {
    ConnectWindow.sidTF.setText(serviceName);
    sidCB_actionPerformed();
  }

  public static void setHostName(String hostName) {
    ConnectWindow.hostNameTF.setText(hostName);
  }
  
  public static void setUserName(String userName) {
    ConnectWindow.usernameT.setText(userName);
  }

  public static void setPortNumber(String portNumber) {
    ConnectWindow.portNumberTF.setText(portNumber);
  }
  
  public static File getConfigFile() {
    return configFile;
  }
  
  private static void sidCB_actionPerformed() {
    if (sidTF.getText().length() == 0 && serviceNameTF.getText().length() > 0) sidTF.setText(serviceNameTF.getText());   
    serviceNameTF.setText("");
    serviceNameTF.setEnabled(false);
    sidTF.setEnabled(true);
    sidCB.setSelected(true);
  }  
  
  private static void serviceNameCB_actionPerformed() {
    if (serviceNameTF.getText().length() == 0 && sidTF.getText().length() > 0) serviceNameTF.setText(sidTF.getText());   
    sidTF.setText("");
    sidTF.setEnabled(false);
    serviceNameTF.setEnabled(true);
    serviceNameCB.setSelected(true);
  }  
  
  private static void sysdbaCB_actionPerformed() {
    if (sysdbaCB.isSelected()) {
      hostNameTF.setText("");
      hostNameTF.setEnabled(false);
      portNumberTF.setText("");
      portNumberTF.setEnabled(false);
      sysdbaL.setText("using tnsnames.ora");
    }
    else {
      hostNameTF.setEnabled(true);
      portNumberTF.setEnabled(true);
      sysdbaL.setText("");
    }
  }
  
  public static String getServiceNameOrSID() {
    return serviceNameOrSID;
  }
  
  private static void purgeAWRCache() {
    Thread purgeAWRCacheThread = new Thread ( new Runnable() {
      public void run() {
        
      Long oneDay = 86400000L;
      Long purgeThreshold = oneDay * Properties.getAWRCacheRetention();
      Date myDate = new Date();

      try {
        File cacheFile;
        if (ConnectWindow.isLinux()) {
          cacheFile = new File(ConnectWindow.getBaseDir() + "/Cache");
        }
        else {
          cacheFile = new File(ConnectWindow.getBaseDir() + "\\Cache");
        }
        
        if (cacheFile.exists()) {
          // each file in this directory should be a directory named after a database
          File[] databaseName = cacheFile.listFiles();
          for (int i =0; i < databaseName.length; i++) {
            if (databaseName[i].isDirectory()) {
              // each file in the database directory should be named after an instance
              File[] instanceName = databaseName[i].listFiles();
              
              for (int j=0; j < instanceName.length; j++) {
                if (instanceName[j].isDirectory()) {
                  // each file in the instance directory should be checked to see if its old enough to purge
                  File[] awrResultFile = instanceName[j].listFiles();
                  for (int k=0; k < awrResultFile.length; k++) {
                    long fileDate = awrResultFile[k].lastModified();
                    if (fileDate < (myDate.getTime() - purgeThreshold)) awrResultFile[k].delete();
                  }
                }
                
                // if the instance directory is empty, then delete it
                if (instanceName[j].isDirectory()) {
                  File[] dirContents = instanceName[j].listFiles();
                  if (dirContents.length == 0) instanceName[j].delete();
                }
              }
            }
            
            // if the database directory is empty, then delete it
            if (databaseName[i].isDirectory()) {
              File[] dirContents = databaseName[i].listFiles();
              if (dirContents.length == 0) databaseName[i].delete();
            }
          }
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Error purging the AWR cache.");
      }
    }
    });

    purgeAWRCacheThread.setName("purgeAWRCache");
    purgeAWRCacheThread.setDaemon(true);
    purgeAWRCacheThread.start();
  }
  
  public double retrieveDBVersionFromDB() {
    double dbVersion = 0.0;
    try {
      String cursorId = "dbVersion.sql";
//      Cursor myCursor = new Cursor(cursorId,true,this);
      Cursor myCursor = new Cursor(cursorId,true);

      QueryResult myResult = ExecuteDisplay.execute(myCursor,false,false,null);
      Vector resultSetRow = myResult.getResultSetRow(0);

      String wholeVer = resultSetRow.elementAt(0).toString();
      dbVersion = Double.valueOf(wholeVer).doubleValue();
    }
    catch (Exception e) {
      // do something here
    }
    
    return dbVersion;
  }
  
  private String getContainerNameFromDB() {
    String containerName = "not Found";
    try {
      String cursorId = "getCurrentContainerName.sql";
      Cursor myCursor = new Cursor(cursorId, true);

      boolean flip = false;
      boolean eggTimer = false;
      QueryResult myResult = ExecuteDisplay.execute(myCursor, flip, eggTimer, null);
//      String resultSetRow = myResult.getResultSetRow(0).firstElement().toString();
      String resultSetRow = myResult.getResultSetRow(0).elementAt(0).toString();
      containerName = resultSetRow;

    } catch (Exception e) {
      // do something here
    }
    
    return containerName;
  }
  
  public static String[][] getContainers() {
    String[][] containers = new String[1][1];
      
    try {
      String cursorId = "getContainerNames.sql";
      Cursor myCursor = new Cursor(cursorId, true);

      boolean flip = false;
      boolean eggTimer = false;
      QueryResult myResult = ExecuteDisplay.execute(myCursor, flip, eggTimer, null);
      containers = myResult.getResultSetAsStringArray();
      
      for (int i=0; i < containers.length; i++) {
        if (containers[i][1].equals(containerName)) containers[i][2] = "selected";
      }

    } catch (Exception e) {
      // do something here
    }
    
    return containers;
  }
  
  public static String getContainerName() {
    return containerName;
  }

}
