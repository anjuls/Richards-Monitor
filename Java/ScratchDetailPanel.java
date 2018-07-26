/*
 * ScratchDetailPanel.java        12.15 05/01/04
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
 * Change History since 05/05/05
 * =============================
 * 
 * 05/05/05 Richard Wright Swithed comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 23/05/05 Richard Wright Moved out much of the fucntionality associated with 
 *                         button actions in seperate classes
 * 23/05/05 Richard Wright Modified to use Exe
 * eDisplay class
 * 16/08/05 Richard Wright Move the populateCustomScripts into a seperate thread
 * 18/08/05 Richard Wright Modified clearB_actionPerformed() to only clear 
 *                         column headings if some exist and moved 
 *                         viewsCB.populateViewsCB() to inside a seperate thread
 * 27/10/05 Richard Wright Removed the list of dba and v$ views - no one uses them
 *                         and there population slows down startup
 * 27/10/05 Richard Wright Introduced a split pane so that the sql text area can 
 *                         be expanded
 * 27/10/05 Richard Wright Made button foreground blue
 * 11/11/05 Richard Wright Added getThisPointer().
 * 23/03/06 Richard Wright Replace references to 'c:' with a call to 
 *                         ConnectWindow.getBaseDir() and ConnectWindow.isLinux()
 * 31/03/06 Richard Wright Moved the code to add a listener to the custom scripts
 *                         combo box into an earlier part of the code, thus avoiding 
 *                         a delay before it was added and user confusion.
 * 22/06/06 Richard Wright Include add and remove buttons to add more scratch 
 *                         panels or remove this scratch panel
 * 07/07/06 Richard Wright Fixed problem with saving sql causing the sql on display to be replaced
 * 07/07/06 Richard Wright Modifed to allow scratch sql to iterate
 * 16/08/06 Richard Wright Amended populateCustomScripts to use collections rather than faff about with arrays.
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 22/05/07 Richard Wright Modified to allow for charts 
 * 07/09/07 Richard Wright Added a pop up menu to the sql area to allow right 
 *                         click cut|copy|paste actions
 * 01/10/07 Richard Wright Converted the JComboBox of scripts to a load button
 * 01/10/07 Richard Wright Changed iterationL to Black as yellow was not visible on XP too well.
 * 10/12/07 Richard Wright Change the right click so it displays as 'cut','copy','paste'
 * 29/01/10 Richard Wright Eggtimer now set when running sql (I did before but somehow stopped in the last couple of releases)
 * 08/03/10 Richard Wright Use configurable standard button size
 * 06/12/10 Richard Wright Allow DML
 * 09/08/11 Richard Wright Renamed to ScratchDetailPanel from ScratchPanel
 * 09/08/11 Richard Wright Scratch Panels are now tabbed
 * 30/11/12 Richard Wright Implemented a simple and rough desc command
 * 18/12/15 Richard Wright Implemented chart boolean
 */


package RichMon;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.DefaultEditorKit;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;

import javax.swing.text.StyleConstants;

import org.jfree.chart.ChartPanel;


/**
 * A panel to allow bespoke sql to be entered and run against the database.  
 * SQL can be saved, and previously saved sql called back.   If available, 
 * scripts in 'I:\support_admin\scripts\richmon\scripts' & 'c:\richmon\scripts'
 * will be available.
 */
public class ScratchDetailPanel extends JPanel  {  
  // scratchSP scrollPane 
  JScrollPane scratchSP = new JScrollPane();

  // Panels 
  private JPanel controlButtonP = new JPanel();
  private JPanel controlButtonPContainer = new JPanel();
  private JPanel topP = new JPanel();
  private JSplitPane splitP = new JSplitPane(JSplitPane.VERTICAL_SPLIT,topP,scratchSP);
    
  // sql text field 
  JScrollPane sqlSP = new JScrollPane();
  private JTextPane sqlTP = new JTextPane();
  
  // status bar 
  private JLabel statusBarL = new JLabel();
  
  // pointers to other objects 
  private ResultCache resultCache = new ResultCache();
  private SQLCache sqlCache = new SQLCache();
  private SQLFormatter mySQLFormatter = new SQLFormatter();
  private ExecutionPlan executionPlan = new ExecutionPlan(this);
  
  // controlButtonP panel buttons 
  private JLabel iterationsL = new JLabel("not iterating");
  private BackB backB = new BackB("Back", scratchSP, sqlTP, statusBarL, resultCache, sqlCache, iterationsL);
  private ForwardB forwardB = new ForwardB("Forward", scratchSP, sqlTP, statusBarL, resultCache, sqlCache, iterationsL);
  private JButton executeB = new JButton("Execute");
  private ExecutionPlanB executionPlanB = new ExecutionPlanB("Execution Plan", sqlTP, scratchSP, statusBarL, executionPlan);
  private JButton clearB = new JButton("Clear");
//  private JComboBox customScriptsCB = new JComboBox();
  private JButton saveB = new JButton("Save");
  private JButton loadB = new JButton("Load");
  private ReformatSQLB formatSQLB = new ReformatSQLB("Format SQL", sqlTP, mySQLFormatter);
  private JButton openScratchPanelB = new JButton("Open Scratch");
//  private JButton closeScratchPanelB = new JButton("Close Scratch");
  public  JToggleButton iterateTB = new JToggleButton("Keep Iterating",false);

  private SpinnerNumberModel snm = new SpinnerNumberModel(5,1,600,1);
  private JSpinner iterationSleepS = new JSpinner(snm);
  private ChartTB chartTB = new ChartTB("Chart",this,statusBarL,resultCache);

  // misc 
  Dimension standardButtonSize = Properties.getStandardButtonDefaultSize();
  Dimension standardButtonSize2 = new Dimension(Integer.valueOf(standardButtonSize.width).intValue(),(Integer.valueOf(standardButtonSize.height).intValue()*3)+13);

  String lastSqlExecuted = " ";
  JPopupMenu popupMenu;
  
  boolean debug=true;
  boolean chart = false;
  
  // script locations 
  File defaultScriptDir;
  File localScriptDir;
  
  // iteration stuff 
  private int numIterations = 0;    // 0=don't iterate, 1=iterate once, 2=keep iterating
  private int iterationCounter = 0; // how many times have we iterated since last told to start
  
  /**
   * Constructor.
   */
  public ScratchDetailPanel() {
    if (ConnectWindow.isLinux()) {
      defaultScriptDir = new File("i:/support_admin/scripts/richmon/scripts");
      localScriptDir = new File(ConnectWindow.getBaseDir() + "/Scripts"); 
      if (debug) {
        System.out.println("local: " + localScriptDir.toString());
        System.out.println("default: " + defaultScriptDir.toString());
      }
    }
    else {
      defaultScriptDir = new File("i:\\support_admin\\scripts\\richmon\\scripts");
      localScriptDir = new File(ConnectWindow.getBaseDir() + "\\Scripts");
    }
    
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
    
    this.setLayout(new BorderLayout());
    
    // allButtonP panel 
    backB.setPreferredSize(standardButtonSize2);
    backB.setForeground(Color.BLUE);
    backB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          backB.actionPerformed();
        }
      });
      
    forwardB.setPreferredSize(standardButtonSize2);
    forwardB.setForeground(Color.BLUE);
    forwardB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          forwardB.actionPerformed();
        }
      });
          
    executeB.setPreferredSize(standardButtonSize);
    executeB.setForeground(Color.BLUE);
    executeB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          iterateOnceB();
        }
      });
      
    clearB.setPreferredSize(standardButtonSize);
    clearB.setForeground(Color.BLUE);
    clearB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clearB_actionPerformed();
        }
      });
      
    openScratchPanelB.setPreferredSize(standardButtonSize);
    openScratchPanelB.setForeground(Color.BLUE);
    openScratchPanelB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          openScratchPanel_actionPerformed();
        }
      });
   
    saveB.setPreferredSize(standardButtonSize);
    saveB.setForeground(Color.BLUE);
    saveB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveB_actionPerformed();
        }
      }); 
    
    loadB.setPreferredSize(standardButtonSize);
    loadB.setForeground(Color.BLUE);
    loadB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loadB_actionPerformed();
        }
      });    
      
    formatSQLB.setPreferredSize(standardButtonSize);
    formatSQLB.setForeground(Color.BLUE);
    formatSQLB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          formatSQLB.actionPerformed();
        }
      });
      
    executionPlanB.setPreferredSize(standardButtonSize);
    executionPlanB.setForeground(Color.BLUE);
    executionPlanB.setMargin(new Insets(0,0,0,0));
    executionPlanB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          executionPlanB.actionPerformed("");
        }
      });

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
    sqlTP.setEditorKit(new NoWrapEditorKit());
    
    SimpleAttributeSet outputAttrs = new SimpleAttributeSet();
    StyleConstants.setForeground(outputAttrs, Color.blue);
    StyleConstants.setFontSize(outputAttrs,11);
    StyleConstants.setFontFamily(outputAttrs,"monospaced");    

    sqlTP.setCharacterAttributes(outputAttrs,true);
    
    popupMenu = new JPopupMenu();
    JMenuItem cutItem = new JMenuItem( new DefaultEditorKit.CutAction() );
    popupMenu.add( cutItem );    
    cutItem.setText("Cut");
    JMenuItem copyItem = new JMenuItem( new DefaultEditorKit.CopyAction() );
    copyItem.setMnemonic('C');
    popupMenu.add( copyItem );    
    copyItem.setText("Copy");
    JMenuItem pasteItem = new JMenuItem( new DefaultEditorKit.PasteAction() );
    pasteItem.setMnemonic('P');
    pasteItem.setText("Paste");
    popupMenu.add( pasteItem );
    
    iterateTB.setPreferredSize(standardButtonSize);
    iterateTB.setForeground(Color.BLUE);    
    iterateTB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          iterateTB();
        }
      });
      
    iterationsL.setFont(new Font("????", 1, 13));
    iterationsL.setForeground(Color.BLACK);

    chartTB.setPreferredSize(standardButtonSize);
    chartTB.setForeground(Color.BLUE);    
    chartTB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (chart) {
            chart = false;
            chartTB.setSelected(false);
          }
          else {
            chart = true;
            chartTB.setSelected(true);
          }
          if (debug) {
            System.out.println("ChartTB is " + chartTB.isSelected());
            System.out.println(("chart boolean is " + chart));
          }
        }
      });
    
    // sqlSP scrollpane 
    sqlSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    sqlSP.getViewport().add(sqlTP, null);
    
    // scratchSP scrollPane 
    scratchSP.getViewport().setBackground(Color.WHITE);

    // controlP panel 
    controlButtonP.setLayout(new GridBagLayout());
    controlButtonP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    GridBagConstraints controlCons = new GridBagConstraints();
    
//    customScriptsCB.setMaximumRowCount(25);
     
    controlCons.gridx=0;
    controlCons.gridy=0;
    controlCons.insets = new Insets(3,3,3,3);
    controlCons.gridheight=3;
    controlButtonP.add(backB, controlCons);
    controlCons.gridx=1;
    controlButtonP.add(forwardB, controlCons);
    controlCons.gridx=2;
    controlCons.gridheight=1;
    controlButtonP.add(executeB, controlCons);
    controlCons.gridy=1;
    controlButtonP.add(clearB, controlCons);
    controlCons.gridy=2;
    controlCons.gridx=1;
    controlCons.gridy=4;
    controlButtonP.add(saveB, controlCons);
    controlCons.gridx=0;
    controlCons.gridy=3;
    controlButtonP.add(formatSQLB, controlCons);
    controlCons.gridx=1;
    controlCons.gridy=3;
    controlButtonP.add(executionPlanB, controlCons);
    controlCons.gridx=0;
    controlCons.gridy=4;
//    controlCons.gridwidth=4;
    controlButtonP.add(loadB, controlCons);
    controlCons.gridx=0;
    controlCons.gridy=5;
    controlCons.gridwidth=1;
    controlButtonP.add(openScratchPanelB, controlCons);
    controlCons.gridx=1;
    controlCons.gridy=5;
//    controlButtonP.add(closeScratchPanelB, controlCons);
    controlCons.gridx=0;
    controlCons.gridy=6;
    controlCons.gridwidth=1;
    controlButtonP.add(iterateTB, controlCons);
    controlCons.gridx=1;
    controlCons.gridy=6;
    controlButtonP.add(iterationSleepS, controlCons);
    controlCons.gridx=2;
    controlCons.gridy=6;
    controlCons.gridwidth=1;
    controlButtonP.add(iterationsL, controlCons);
    controlCons.gridx=2;
    controlCons.gridy=5;
    controlButtonP.add(chartTB, controlCons);

    // controlButtonPContainer panel 
     controlButtonPContainer.setLayout(new BorderLayout());
     controlButtonPContainer.add(controlButtonP, BorderLayout.NORTH);
     
    // topP panel 
    topP.setLayout(new BorderLayout());
    topP.add(controlButtonPContainer, BorderLayout.WEST);
    topP.add(sqlSP, BorderLayout.CENTER);
    
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(splitP, BorderLayout.CENTER);
    
    // customer script stuff 
//    customScriptsCB.setPreferredSize(prefSize5);
//    populateCustomScripts();
    sqlTP.setText("");
    
  }
  


  /**
   * Clear the sql area and results
   */
  private void clearB_actionPerformed() {
    sqlTP.setText("");
    scratchSP.getViewport().removeAll();  
    if (scratchSP.getColumnHeader() instanceof JViewport) scratchSP.getColumnHeader().removeAll();
    
    // The above 2 actions don't change anything on screen until this 
    scratchSP.getViewport().updateUI();
    
    // Reset the iterations on screen 
    iterationsL.setText("0");
    iterationCounter = 0;
  }
  


  /**
   * Get a list of scripts to display in customScriptsCB JComboBox.  This will 
   * be a combination of those in 'c:\RichMon\Scripts' and 
   * 'I:\support_admin\scripts\Richmon\scripts'.  The later being Vodafone 
   * specific.
   */
/*  private void populateCustomScripts() {
    Thread populateCustomScriptsThread = new Thread ( new Runnable() {
      public void run() {          
        
        Set unSortedScripts = new HashSet();
        
        // get the script names from the i drive 
        if (defaultScriptDir.exists() && ConnectWindow.isMergeIDrive()) {
          String[] iDrive = defaultScriptDir.list();
          for (int i=0; i < iDrive.length; i++) unSortedScripts.add(iDrive[i]);
        }

        // get the load script names 
        if (localScriptDir.exists()) {
          String[] cDrive = localScriptDir.list();
          for (int i=0; i < cDrive.length; i++) unSortedScripts.add(cDrive[i]);
        }
        
        // Sort the scripts 
        Set sortedScripts = new TreeSet(unSortedScripts);
        
        // populate the combo box 
        Iterator myIterator = sortedScripts.iterator();
        while (myIterator.hasNext()) {
          customScriptsCB.addItem((String)myIterator.next());
        }
        
        addCustomScriptsActionListener();
      }
    });
    
    populateCustomScriptsThread.setDaemon(false);
    populateCustomScriptsThread.setName("populateCustomScripts");
    populateCustomScriptsThread.start();
  } */

  /**
   * Load and execute the script selected in the customScriptCB JComboBox.
   */
/*  private void customScriptsCB_actionPerformed() {
    // get the name of the script to load 
    String scriptName = String.valueOf(customScriptsCB.getSelectedItem());
    String fileName = defaultScriptDir + "\\" + scriptName;

    // check to see if the script is on the I drive or C drive 
    File tmpF = new File(fileName);
    if (!tmpF.exists()) 
    {
      fileName = localScriptDir + "\\" + scriptName;
    }

    String sql = "";
    
    // load script 
    try {
      File sourceF = new File(fileName);
      BufferedReader sourceBR = new BufferedReader(new FileReader(sourceF));
  
      StringBuffer tmp = new StringBuffer();
      String line;
      while ((line = sourceBR.readLine()) != null) {
        tmp.append(line);
        tmp.append("\n");
      }
      
      sql = tmp.toString();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);     
    }
    
    // put the script content into the display 

    sqlTP.setText(sql);
      
    // Reset the iterations on screen 
    iterationsL.setText("0");
    iterationCounter = 0;
  } */
  
  /**
   * Get the resultCache.
   * 
   * @return ResultCache - pointer to the ResultCache object
   */
  public ResultCache getResultCache() {
    return resultCache;
  }
  
  /**
   * Prompt for a file name and save the entered sql to that file.
   */
  private void saveB_actionPerformed() {
    // prompt user for the file name to save the sql under 
    try
    {
      File defaultFile = new File(Properties.getDefaultLoadDir());
      if (defaultFile.isDirectory()) {
        if (ConnectWindow.isLinux()) {
          defaultFile = new File(Properties.getDefaultLoadDir() + "/script.sql");
        }
        else {
          defaultFile = new File(Properties.getDefaultLoadDir() + "\\script.sql");
        }
      }
      else {
        if (ConnectWindow.isLinux()) {
          defaultFile = new File(ConnectWindow.getBaseDir() + "/Scripts/script.sql");
        }
        else {
          defaultFile = new File(ConnectWindow.getBaseDir() + "\\Scripts\\script.sql");        
        }
      }
      
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(defaultFile);
      File saveFile;
      BufferedWriter save;
      
      // prompt the user to choose a file name 
      int option = fileChooser.showSaveDialog(this);
      if (option == JFileChooser.APPROVE_OPTION)
      {
        saveFile = fileChooser.getSelectedFile();
      
        // force the user to use a new file name if the specified filename already exists 
        while (saveFile.exists()) {
          int conf = JOptionPane.showConfirmDialog(this,"File already exists, do you want to overwrite it ?","File Already Exists",JOptionPane.ERROR_MESSAGE);
          
          if (conf == 0) {
            saveFile.delete();
          }
          else {
            option = fileChooser.showOpenDialog(this);
            saveFile = fileChooser.getSelectedFile();
          }
        }
        
        save = new BufferedWriter(new FileWriter(saveFile));
        saveFile.createNewFile();
        
        save.write(sqlTP.getText());
   
        save.close();
      }
    }
    catch (Exception e)
    {
      ConsoleWindow.displayError(e,this);
    }
  }  
  
  /**
   * Prompt for a file name and save the entered sql to that file.
   */
  private void loadB_actionPerformed() {
    // prompt user for the file name to load 
    try
    {
      File defaultFile = new File(Properties.getDefaultLoadDir());
      if (defaultFile.isDirectory()) {
        if (ConnectWindow.isLinux()) {
          defaultFile = new File(Properties.getDefaultLoadDir() + "/script.sql");
        }
        else {
          defaultFile = new File(Properties.getDefaultLoadDir() + "\\script.sql");
        }
      }
      else {
        if (ConnectWindow.isLinux()) {
          defaultFile = new File(ConnectWindow.getBaseDir() + "/Scripts/script.sql");
        }
        else {
          defaultFile = new File(ConnectWindow.getBaseDir() + "\\Scripts\\script.sql");        
        }
      }
      
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(defaultFile);
      File loadFile;
      
      // prompt the user to choose a file name 
      int option = fileChooser.showOpenDialog(this);
      if (option == JFileChooser.APPROVE_OPTION)
      {
        loadFile = fileChooser.getSelectedFile();
        String sql = new String();
        
        try {
          
          BufferedReader sourceBR = new BufferedReader(new FileReader(loadFile));
        
          StringBuffer tmp = new StringBuffer();
          String line;
          while ((line = sourceBR.readLine()) != null) {
            tmp.append(line);
            tmp.append("\n");
          }
          
          sql = tmp.toString();
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this);     
        }
        
        // put the script content into the display 

        sqlTP.setText(sql);
          
        // Reset the iterations on screen 
        iterationsL.setText("0");
        iterationCounter = 0;     
        
      }
    }
    catch (Exception e)
    {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  
//  public void addCustomScriptsActionListener() {
//    customScriptsCB.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        customScriptsCB_actionPerformed();
//      }
//    });
//  }
  
  public void setSQLTA(String sql) {
    sqlTP.setText(sql);
  }
  
  /**
   * Returns a pointer to this object, so that it can be removed from the console tabbed pane.
   * @return
   */
  public ScratchDetailPanel getThisPointer() {
    return this;
  }
  
  private void openScratchPanel_actionPerformed() {
    ConsoleWindow.addScratchPanel();
  }
  
//  public void closeScratchPanel_actionPerformed() {
//    ConsoleWindow.removeScratchPanel();
//  }
  
  /**
   * Starts a new deamon thread which executes the selected options and then 
   * sleeps for n seconds, as defined by the iteraction interval.
   */
  private void iterate() {
    Thread scratchPanelThread = new Thread ( new Runnable() 
    {
      public void run() 
      {
        // reset the iterations on screen if the sql statement is not the same as the last 
        if (!(sqlTP.getText().equals(lastSqlExecuted))) {
          iterationsL.setText("0");
          iterationCounter = 0;          
        }
        
        while (numIterations > 0) {
          // increment iteration counters 
          iterationCounter++;
          
          // make this the last iteration if this is a once only 
          if (numIterations == 1) numIterations =0;
          
          // update iterationsL 
          iterationsL.setText("Iteration " + iterationCounter);

          try {   
            boolean alterSession = sqlTP.getText().trim().toLowerCase().startsWith("alter session");
            boolean alterSystem = sqlTP.getText().trim().toLowerCase().startsWith("alter system");
            boolean alterDatabase = sqlTP.getText().trim().toLowerCase().startsWith("alter database");
            boolean begin = sqlTP.getText().toLowerCase().trim().startsWith("begin");
            boolean declare = sqlTP.getText().toLowerCase().trim().startsWith("declare");
            boolean describe = false;
            if (sqlTP.getText().trim().toLowerCase().startsWith("describe") || sqlTP.getText().toLowerCase().startsWith("desc")) describe=true;
            
            if (begin || declare) {
              
              /*
               * This dose not currently work.
               * 
               * I suspect (from reading online) that to run pl/sql would require creating a pl/sql procedure, 
               * running that and then dropping it.  Clearly that it not nice and therefore not implemented
               * 
               * RW 06/12/10
               */
              if (ConsoleWindow.getDBVersion() >= 8.1) {   // not execute immediate prior to 8i
                Parameters myPars = new Parameters();
                QueryResult myResult = ConnectWindow.getDatabase().executeBatch(sqlTP.getText(),myPars,false);
                ExecuteDisplay.displayTable(myResult,scratchSP,false,statusBarL);
              }
            }
            
            if (alterSession) {
              if (Properties.isAllow_alter_session()) {
                String sql = removeTrailing(sqlTP.getText());
                Parameters myPars = new Parameters();
                QueryResult myResult = ConnectWindow.getDatabase().execute(sql, myPars, false, false);
                JOptionPane.showMessageDialog(scratchSP,"Completed : " + sql,"alter session...",JOptionPane.INFORMATION_MESSAGE);                
              }
              else {
                throw new InsufficientPrivilegesException("Alter Session not enabled in RichMon config file");
              }
            }
              
            if (alterSystem) {
              if (Properties.isAllow_alter_system()) {
                String sql = removeTrailing(sqlTP.getText());
                Parameters myPars = new Parameters();
                QueryResult myResult = ConnectWindow.getDatabase().execute(sql, myPars, false, false);
                JOptionPane.showMessageDialog(scratchSP,"Completed : " + sql,"alter system...",JOptionPane.INFORMATION_MESSAGE);                                  
              }
              else {
                throw new InsufficientPrivilegesException("Alter System not enabled in RichMon config file");                
              }
            }
                
            if (alterDatabase) {
              if (Properties.isAllow_alter_database()) {
                String sql = removeTrailing(sqlTP.getText());
                Parameters myPars = new Parameters();
                QueryResult myResult = ConnectWindow.getDatabase().execute(sql, myPars, false, false);
                JOptionPane.showMessageDialog(scratchSP,"Completed : " + sql,"alter database...",JOptionPane.INFORMATION_MESSAGE);                                                      
              }
              else {
                throw new InsufficientPrivilegesException("Alter Database not enabled in RichMon config file");
              }
            }
               
            if (describe)    {
              /*
               * Remove the cort describe and find the tablename or schema.tablename
               */
              String entireCommand = sqlTP.getText().trim();
              String tableOrSchemaTable = entireCommand.substring(entireCommand.indexOf(" ")+1);
              String schema = new String();
              String tableName = new String();
              
              // is there a schema in front of the table name
              if (tableOrSchemaTable.indexOf(".") > 0) {
                // there is a schema in front of the table name
                schema = tableOrSchemaTable.substring(0, tableOrSchemaTable.indexOf("."));
                tableName = tableOrSchemaTable.substring(tableOrSchemaTable.indexOf(".")+1);
              }
              else {
                
                schema = ConnectWindow.getUsername();
                tableName = tableOrSchemaTable;
              }
              
              String cursorId = "dbaTabColumns.sql";
              Parameters myPars = new Parameters();
              myPars.addParameter("String", schema.toUpperCase());
              myPars.addParameter("String",tableName.toUpperCase());
              
              Cursor myCursor = new Cursor(cursorId,true);
              ExecuteDisplay.executeDisplay(myCursor, myPars, scratchSP, statusBarL, false, resultCache, true); 
              
              // cache the sql
              sqlCache.cacheSQL(sqlTP.getText());
              lastSqlExecuted = sqlTP.getText();
            }
               
            if (!begin && !declare && !alterSession && !alterSystem && !alterDatabase && !describe && !chart) {
              /*
               * This is not a pl/sql block and not an alter statement
               */
              String cursorId = "scratch.sql";
              Cursor myCursor = new Cursor(cursorId,false);
              myCursor.setSQLTxtOriginal(sqlTP.getText());
          //    myCursor.setEggTimer(true); 
              
              boolean flip = true;
              boolean eggTimer = true;
              boolean restrictRows = true;

              QueryResult myResult = ExecuteDisplay.execute(myCursor,flip,eggTimer,resultCache,restrictRows,iterationCounter);
              ExecuteDisplay.displayTable(myResult, scratchSP, false, statusBarL);

              // cache the sql
              if (Properties.isCacheResultsWithNoRows() || myResult.getNumRows() > 0) {
                sqlCache.cacheSQL(sqlTP.getText());
                
              }
              lastSqlExecuted = sqlTP.getText();
            }
            
            if (chart) {
              chartTB.actionPerformed(iterationCounter);
            }
            else {
              chartTB.nullChart();
            }
          }
          catch (Exception e) {
            ConsoleWindow.displayError(e,this,"Iterating on the scratch panel");
          }
          
          if (numIterations > 0) {
            // if another iteration is required, sleep first 
            long sleepMillis = Integer.valueOf(String.valueOf(iterationSleepS.getValue())).intValue();
            sleepMillis = sleepMillis * 1000;  // convert seconds to milli seconds

            try {
              Thread.sleep(sleepMillis);
            }
            catch (InterruptedException e) {
              ConsoleWindow.displayError(e,this);
            }
          }
        }

      }
    });
    
    scratchPanelThread.setDaemon(false);
    scratchPanelThread.setName("scratchPanel");
    scratchPanelThread.start();
  }
  
  /**
   * Perform a single iteration.
   */
  private void iterateOnceB() {
    // set numIterations to 1 to ensure a single iteration is performanced 
    numIterations = 1;
    
    // iterate 
    iterate();
  }
  
  /**
   * Signal iterating to either start or stop.
   */
  private void iterateTB() {
    if (iterateTB.isSelected()) {
      // set numIterations to 2 - meaning keep iterating until told to stop 
      numIterations=2;
      
      // start iterating 
      iterate();
    }
    else {
      // set numIterations to 0 - meaning stop after current iteration 
      numIterations=0;
    }
  }
  
  private String removeTrailing(String sql) {
    if (sql.substring(sql.length()-1).equals("/") || sql.substring(sql.length()-1).equals(";")) {
      sql = sql.substring(0,sql.length()-1);
    }
    
    return sql;
  }
  
  public String getSQLTA() {
    return sqlTP.getText();
  }
  
  public void displayChart(ChartPanel myChartPanel) {
    scratchSP.getViewport().removeAll();
    scratchSP.getViewport().add(myChartPanel);
  }
  
  private void rightClickPopup(MouseEvent e)
    {
      if (e.isPopupTrigger())
      {
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  
  public SQLCache getSQLCache() {
    return sqlCache;
  }


  
  
}