/*
 *  ASHPanel.java
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
 * 16/12/09 Richard Wright Modified to allow an ASH report to be generated as well as output from v$active_session_history
 * 24/02/10 Richard Wright Do not display the ash report button prior to 11g
 * 20/11/14 Richard Wright fix bug so the html ash report is saved to disk in the correct location
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
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultEditorKit;

/**
 * A panel which provides an overview of a single sessions current activity.
 */
public class SessionASHPanel extends JPanel {

  //  panels 

  private JPanel controlButtonP = new JPanel();
  private JPanel controlButton2P = new JPanel();
  private JPanel controlButton3P = new JPanel();

  // controlButtonP panel objects 
  private JButton getAshDataB = new JButton("v$active_session_history");
  private JButton getAshReportB = new JButton("ASH Report");
  private SpinnerNumberModel snm = new SpinnerNumberModel(15, 1, 6000, 1);
  private JSpinner minutesS = new JSpinner(snm);
  private JLabel minutesL = new JLabel("ASH reports cover n minutes back from sysdate: ");

  // statusBar 
  private JLabel statusBarL = new JLabel();

  JScrollPane myScrollP = new JScrollPane();
  
  // misc 
  private Dimension prefSize = new Dimension(110, 25);
  private Dimension prefSize3 = new Dimension(175, 25);
  private Dimension prefSize2 = new Dimension(55, 25);
  public int sid; // sid for this ash data
  public long serial; // serial# for this ash data
  int instanceNumber;
  
  boolean debug = false;


  /**
   * Constructor.
   */
  public SessionASHPanel(int sid, long serial, int instanceNumber) {
    this.sid = sid;
    this.serial = serial;
    this.instanceNumber = instanceNumber;

    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception {
    // layout 
    this.setLayout(new BorderLayout());

    // controlButtonP Panel 
    controlButtonP.setLayout(new BorderLayout());
    controlButton2P.setLayout(new FlowLayout());
    controlButton3P.setLayout(new BorderLayout());

    getAshDataB.setPreferredSize(prefSize3);
    getAshDataB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            getAshDataB_actionPerformed();
          }
        });
    getAshReportB.setPreferredSize(prefSize);
    getAshReportB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            getAshReportB_actionPerformed();
          }
        });

    minutesS.setPreferredSize(prefSize2);
    minutesL.setForeground(Color.BLUE);

    controlButton2P.add(minutesL);
    controlButton2P.add(minutesS);
    controlButton2P.add(getAshDataB);
    if (ConsoleWindow.getDBVersion() >= 11) controlButton2P.add(getAshReportB);
    

    controlButton2P.add(controlButton3P);

    controlButtonP.add(controlButton2P,BorderLayout.WEST);
    
    // main panel
    
    myScrollP.getViewport().setBackground(Color.WHITE);
    
    // statusBar 
    statusBarL.setText(" ");
    
    
    this.add(controlButtonP, BorderLayout.NORTH);
    this.add(myScrollP, BorderLayout.CENTER);
    this.add(statusBarL, BorderLayout.SOUTH);
  }

  /**
   * Close this tab.  This also removes the pointer to the tab from the sessionsV Vector in ConsoleWindow.
   */
  private void closeB_actionPerformed() {
    ConsoleWindow.removeAshPanel(this);
  }



  private void updateStatusBar(String cursorId, QueryResult myResult) {
    int ms = (int)myResult.getExecutionTime();
    int numRows = myResult.getNumRows();
    if (myResult.isFlipped())
      numRows = 1;
    String executionTime = myResult.getExecutionClockTime();
    statusBarL.setText("Executed in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime);
  }

  private void getAshDataB_actionPerformed() {

    try {
      String cursorId = "ash.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int", instanceNumber);
      myPars.addParameter("int",serial);
      myPars.addParameter("int",minutesS.getValue());

      Cursor myCursor = new Cursor(cursorId,true);
      ExecuteDisplay.executeDisplay(myCursor,myPars,myScrollP,statusBarL,false,null,false);

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error getting ASH data for session");
    }
  }
  
  private void getAshReportB_actionPerformed() {
    Object[] options = {"ASH Report",
                        "ASH Report in HTML format"};
    int n = JOptionPane.showOptionDialog(this,
        "An ASH report will be created on this session.\n\n" +
        "You can choose to have the report in text format displayed on this panel, or in HTML format saved to disk.\n\n" +
        "The html report will be saved to '$RICHMON_BASE/ASH Reports' directory. On windows '....\\My Documents\\ASH Reports'",
        "ASH Report",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);
    
    // Get the dbid for the instance
    long dbId = 0;
    String instanceName = null;
    String sysdate = null;
    try {
      String cursorId = "dbid.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int", instanceNumber);

      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, true, null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      dbId = Long.valueOf(resultSet[0][0]).longValue();
      instanceName = resultSet[0][1];
      sysdate = resultSet[0][2];
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this, "Exception getting the dbId and instance name from the ash panel.");
    }

    if (n==0) {
      try {
        String cursorId = "RichMonASHREPORT";
        Cursor myCursor = new Cursor(cursorId,false);
        myCursor.setSQLTxtOriginal("select output as \"Ash Report\" from table(dbms_workload_repository.ash_report_text( l_dbid=>" + dbId + ",l_inst_num=>" + instanceNumber +  
                                   ",l_btime=> sysdate-" + minutesS.getValue() + "/1440,l_etime=>sysdate,l_sid=>" + sid + "));");
        
        if (debug) System.out.println("create ash report : " + myCursor.getSQL());
        
        Parameters myPars = new Parameters();
        ExecuteDisplay.executeDisplay(myCursor,myPars,myScrollP,statusBarL,false,null,false);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this, "Exception creating an ASH report in text format from the ash panel.");
      }
    }
    
    if (n==1) {
      try {
        
        String cursorId = "RichMonASHREPORT";
        Cursor myCursor = new Cursor(cursorId,false);
        myCursor.setSQLTxtOriginal("select output from table(dbms_workload_repository.ash_report_html( l_dbid=>" + dbId + ",l_inst_num=>" + instanceNumber +  
                                   ",l_btime=> sysdate-" + minutesS.getValue() + "/1440,l_etime=>sysdate,l_sid=>" + sid + "));");
        
        if (debug) System.out.println("create ash report : " + myCursor.getSQL());
        
        QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
        String resultString =  ExecuteDisplay.createOutputString(myResult, false);

        File awrFile;
        if (ConnectWindow.isLinux()) {
          awrFile = new File(ConnectWindow.getBaseDir() + "/ASH Reports/ASH_" + 
                                          instanceName + "_" + minutesS.getValue() + "minutes_"+ sysdate + ".html");        
        }
        else {
          awrFile = new File(ConnectWindow.getBaseDir() + "\\ASH Reports\\ASH_" + 
                                          instanceName + "_" + minutesS.getValue() + "minutes_"+ sysdate + ".html");
        }
       
        BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
        awrFileWriter.write(resultString);
        awrFileWriter.close();
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this, "Exception creating an ASH report in html format from the ash panel.");
      }
    }
  }
}
