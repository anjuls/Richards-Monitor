/*
 * SPADDMB.java        17.38 14/10/09
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
 * 
 */

package RichMon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;

/**
 * Implements a query to show all current sort operations.
 */
public class SPADDMB extends JButton {
  JLabel statusBar; // The statusBar to be updated after a query is executed
  JScrollPane scrollP; // The JScrollPane on which output should be displayed

  StatspackAWRInstancePanel statspackPanel;
  ProgressMonitor myProgressM;
  String instanceName;
  int[] snapshotIdRange;

  boolean debug = false;

  /**
   * Constuctor
   *
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPADDMB(StatspackAWRInstancePanel statspackPanel, String buttonName,
                 JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);

    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackPanel = statspackPanel;
  }

  /**
   * Performs the user selected action
   */
  public void actionPerformed() {

    // run a single report and display that on the screen       
    try {
      String taskName = "ADDM " + statspackPanel.getStartSnapId() + "-" + statspackPanel.getEndSnapId();
      String taskDesc = "ADDM report initiated from RichMon by " + System.getProperty("user.name");
      
      // Check to see whether the tuning task already exists first
      String cursorId = "dbaAdvisorTasks.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("String",taskName);
      
      QueryResult myResult = ExecuteDisplay.execute(cursorId,myPars,false,true,null);
      if (myResult.getNumRows() == 0) {
        // create the task
        String statement = "begin DBMS_ADVISOR.create_task(advisor_name => 'ADDM',task_name => '" + taskName + "',task_desc => '" + taskDesc + "'); end;";
        if (debug) System.out.println("create task command: " + statement);
        ConnectWindow.getDatabase().executeImmediate(statement,false,true);
        
        // set the start parameter for the task
        String startPar = "begin DBMS_ADVISOR.set_task_parameter(task_name => '" + taskName + "',parameter => 'START_SNAPSHOT',value => " + statspackPanel.getStartSnapId() + "); end;";
        ConnectWindow.getDatabase().executeImmediate(startPar,false,true);

        // set the end parameter for the task
        String endPar = "begin DBMS_ADVISOR.set_task_parameter(task_name => '" + taskName + "',parameter => 'END_SNAPSHOT',value => " + statspackPanel.getEndSnapId() + "); end;";
        ConnectWindow.getDatabase().executeImmediate(endPar,false,true);

        // run the task
        String runTask = "begin DBMS_ADVISOR.execute_task(task_name => '" + taskName + "'); end;";
        ConnectWindow.getDatabase().executeImmediate(runTask,false,true);
      }

      // get the ADDM result 
      cursorId = "RichMonADDMReport";
      Cursor addmCursor = new Cursor(cursorId,false);
      addmCursor.setSQLTxtOriginal("SELECT DBMS_ADVISOR.get_task_report('" + taskName + "','TEXT', 'TYPICAL', 'ALL') AS report FROM dual");
      myResult = ExecuteDisplay.execute(addmCursor,false,true,null);
      
      String resultString = ExecuteDisplay.createOutputString(myResult, false);
      String instanceName;
      if (statspackPanel.getSelectedInstanceName() instanceof String) {
        instanceName = statspackPanel.getSelectedInstanceName();
      }
      else {
        instanceName = ConsoleWindow.getInstanceName();
      }
      
      File addmFile = new File(ConnectWindow.getBaseDir() + "\\ADDM Reports\\ADDM_" + instanceName + "_" + statspackPanel.getStartSnapId() + "_" + statspackPanel.getEndSnapId() + ".txt");
      if (ConnectWindow.isLinux()) addmFile = new File(ConnectWindow.getBaseDir() + "//ADDM Reports//ADDM_" + instanceName + "_" + statspackPanel.getStartSnapId() + "_" + statspackPanel.getEndSnapId() + ".txt");
      BufferedWriter addmFileWriter = new BufferedWriter(new FileWriter(addmFile));
      addmFileWriter.write(resultString);
      addmFileWriter.close();
      
      String msg = "You ADDM report has been saved to the '$RICHMON_BASE/ADDM Reports' directory. On windows '....\\\\My Documents\\\\ADDM Reports'" +
                   "\n\nADDM reports do not display well on screen so it is better to save them to disk.";
      JOptionPane.showMessageDialog(statspackPanel,msg,"ADDM Report Saved...",JOptionPane.OK_OPTION);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }
}
