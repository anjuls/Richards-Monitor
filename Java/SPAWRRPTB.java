/*
 * SPAWARRPTB.java        ? ?/?/?
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
 * Change History since 04/05/07
 * =============================
 * 
 * 15/07/08 Richard Wright Improved the error message to say that dba privs are required
 * 01/06/08 Richard Wright Give users the option of producing a report for every snapshot in the range
 * 09/12/09 Richard Wright Allow AWR reports to be extracted in HTML form
 */
 
 package RichMon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;

/**
 * Implements a query to show all current sort operations.
 */
public class SPAWRRPTB extends JButton {
  JLabel statusBar; // The statusBar to be updated after a query is executed 
  JScrollPane scrollP; // The JScrollPane on which output should be displayed 

  StatspackAWRInstancePanel statspackAWRInstancePanel;
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
  public SPAWRRPTB(StatspackAWRInstancePanel statspackPanel,String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);

    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  /**
   * Performs the user selected action 
   */
  public void actionPerformed() {
    
    Object[] options = {"Single AWR Report",
                        "All AWR Reports",
                        "AWR Diff Report",
//                        "AWR Global Report",
                        "Cancel"};
    int n = JOptionPane.showOptionDialog(statspackAWRInstancePanel,
        "All reports will be in HTML format and saved to '$RICHMON_BASE/AWR Reports' directory.\n\n" + 
        "RichMon does not currently allow you to run diff reports cross instance, please use the command line for that.", 
        "AWR Report",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);
    
    if (debug) System.out.println("AWR Option:" + n);
    
    
    /* Single AWR Report in HTML Format */
    if (n == 0) {
      try {
        String cursorId = "RichMonAWRREPORT";
        Cursor myCursor = new Cursor(cursorId,false);
        myCursor.setSQLTxtOriginal("select output as \"AWR Report\" from table(dbms_workload_repository.awr_report_html( " + statspackAWRInstancePanel.getDbId() + "," + statspackAWRInstancePanel.getInstanceNumber() + "," + 
                            String.valueOf(statspackAWRInstancePanel.getStartSnapId()) + "," + 
                            statspackAWRInstancePanel.getEndSnapId() + "));");
        Cursor InstNameCursor = new Cursor("InstNameCursor",false);
        InstNameCursor.setSQLTxtOriginal("select instance_name from gv$instance where inst_id = " + statspackAWRInstancePanel.getInstanceNumber());
        QueryResult InstNameResult = ExecuteDisplay.execute(InstNameCursor,false,true,null);
        instanceName = InstNameResult.getResultSetAsStringArray()[0][0];
        QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
        String resultString =  ExecuteDisplay.createOutputString(myResult, false);

        File awrFile = new File(ConnectWindow.getBaseDir() + "\\AWR Reports\\AWR_" + instanceName + "_" + statspackAWRInstancePanel.getStartSnapId() + "_" + statspackAWRInstancePanel.getEndSnapId() + ".html");
        if (ConnectWindow.isLinux()) awrFile = new File(ConnectWindow.getBaseDir() + "//AWR Reports//AWR_" + instanceName + "_" + statspackAWRInstancePanel.getStartSnapId() + "_" + statspackAWRInstancePanel.getEndSnapId() + ".html");
        
        BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
        awrFileWriter.write(resultString);
        awrFileWriter.close();
        
        String msg4 = "You AWR Report has been saved to the '$RICHMON_BASE/AWR Reports' directory.";
        JOptionPane.showMessageDialog(statspackAWRInstancePanel,msg4,"ADDM Report Saved...",JOptionPane.OK_OPTION);

      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this, "DBA privilege is required for this.");
      }
    }
    
    /* All AWR Report in HTML Format */
    if (n == 1) {
      try {
        instanceName = ConsoleWindow.getInstanceName(statspackAWRInstancePanel.getInstanceNumber());
        snapshotIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
        
        myProgressM = new ProgressMonitor(statspackAWRInstancePanel,null,"",0,snapshotIdRange.length-1);
        
        Thread awrThread = new Thread ( new Runnable() {
          public void run() {
            getAWRReportsHTML();
            myProgressM.setProgress(myProgressM.getMaximum());
            
            String msg3 = "You AWR reports have been saved to the '$RICHMON_BASE/AWR Reports' directory.";
            JOptionPane.showMessageDialog(statspackAWRInstancePanel,msg3,"ADDM Report Saved...",JOptionPane.OK_OPTION);

          }
        });
        
        awrThread.setName("awrThread");
        awrThread.setDaemon(true);
        awrThread.start();
        
      } catch (Exception e) {
        ConsoleWindow.displayError(e, this, "DBA privilege is required for this.");        
      }
    }
    
    /* AWR Diff Report */
    if (n==2) {
      // Get a list of all snaphots
      
      String[] allSnapshots = new String[statspackAWRInstancePanel.startSnapshotCB.getItemCount()];
      for (int i = 0; i < statspackAWRInstancePanel.startSnapshotCB.getItemCount(); i++) {
        allSnapshots[i] = statspackAWRInstancePanel.startSnapshotCB.getItemAt(i).toString();
      }
      
      
      /* Use the start and end snap id's from the AWR panel for the start values so we ask the user for the other snap ids to compare against */
      JPanel startSnap1P = new JPanel();
      JLabel startSnap1L = new JLabel("1st Start Snapshot: ");
      JComboBox startSnap1CB = new JComboBox();
      for (int i = 0; i < allSnapshots.length; i++) {
        startSnap1CB.addItem(allSnapshots[i]);
      }
      startSnap1CB.setSelectedIndex(statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex());
      startSnap1P.add(startSnap1L);
      startSnap1P.add(startSnap1CB);
      
      JPanel endSnap1P = new JPanel();
      JLabel endSnap1L = new JLabel("1st End Snapshot: ");
      JComboBox endSnap1CB = new JComboBox();   
      for (int i = 0; i < allSnapshots.length; i++) {
        endSnap1CB.addItem(allSnapshots[i]);
      }
      endSnap1CB.setSelectedIndex(statspackAWRInstancePanel.endSnapshotCB.getSelectedIndex());
      endSnap1P.add(endSnap1L);
      endSnap1P.add(endSnap1CB);      
      JPanel startSnap2P = new JPanel();
      JLabel startSnap2L = new JLabel("2nd Start Snapshot: ");
      JComboBox startSnap2CB = new JComboBox();   
      for (int i = 0; i < allSnapshots.length; i++) {
        startSnap2CB.addItem(allSnapshots[i]);
      }
      startSnap2CB.setSelectedIndex(statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex());
      startSnap2P.add(startSnap2L);
      startSnap2P.add(startSnap2CB);
      
      JPanel endSnap2P = new JPanel();
      JLabel endSnap2L = new JLabel("2nd End Snapshot: ");
      JComboBox endSnap2CB = new JComboBox();   
      for (int i = 0; i < allSnapshots.length; i++) {
        endSnap2CB.addItem(allSnapshots[i]);
      }
      endSnap2CB.setSelectedIndex(statspackAWRInstancePanel.endSnapshotCB.getSelectedIndex());
      endSnap2P.add(endSnap2L);
      endSnap2P.add(endSnap2CB);
      

      String msg = "Select the start and end snapshots for the difference report...\n\n";
      Object[] options2 = {msg, startSnap1P, endSnap1P, startSnap2P, endSnap2P};

      int result = JOptionPane.showOptionDialog(statspackAWRInstancePanel,options2,"AWR DIFF Report",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);

      if (result == 0) {
        // The first 25 chars represent the date and some spaces before the snap Id 
        int startSnapId1 = Integer.valueOf(startSnap1CB.getSelectedItem().toString().substring(25)).intValue();
        int endSnapId1 = Integer.valueOf(endSnap1CB.getSelectedItem().toString().substring(25)).intValue();
        int startSnapId2 = Integer.valueOf(startSnap2CB.getSelectedItem().toString().substring(25)).intValue();
        int endSnapId2 = Integer.valueOf(endSnap2CB.getSelectedItem().toString().substring(25)).intValue();
       
        try {
          String cursorId = "RichMonAWRREPORT";
          Cursor myCursor = new Cursor(cursorId,false);
          myCursor.setSQLTxtOriginal("select output as \"AWR Report\" from table(dbms_workload_repository.awr_diff_report_html( " + 
                                     statspackAWRInstancePanel.getDbId() + "," + 
                                     statspackAWRInstancePanel.getInstanceNumber() + "," + 
                                     startSnapId1 + "," + 
                                     endSnapId1 + "," +
                                     statspackAWRInstancePanel.getDbId() + "," + 
                                     statspackAWRInstancePanel.getInstanceNumber() + "," + 
                                     startSnapId2 + "," +                 
                                     endSnapId2 + "));");
          
          Cursor InstNameCursor = new Cursor("InstNameCursor",false);
          InstNameCursor.setSQLTxtOriginal("select instance_name from gv$instance where inst_id = " + statspackAWRInstancePanel.getInstanceNumber());
          QueryResult InstNameResult = ExecuteDisplay.execute(InstNameCursor,false,true,null);
          instanceName = InstNameResult.getResultSetAsStringArray()[0][0];
          
          QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
          String resultString =  ExecuteDisplay.createOutputString(myResult, false);

          File awrFile = new File(ConnectWindow.getBaseDir() + "\\AWR Reports\\AWR_DIFF_" + instanceName + "_" + startSnapId1 + "_" + startSnapId1 + "_vs_" + startSnapId2 + "_" + startSnapId2 +".html");
          if (ConnectWindow.isLinux()) awrFile = new File(ConnectWindow.getBaseDir() + "//AWR Reports//AWR_DIFF_" + instanceName + "_" + startSnapId1 + "_" + startSnapId1 + "_vs_" + startSnapId2 + "_" + startSnapId2 +".html");
          
          BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
          awrFileWriter.write(resultString);
          awrFileWriter.close();
          
          String msg2 = "You AWR Diff Report has been saved to the '$RICHMON_BASE/AWR Reports' directory.";
          JOptionPane.showMessageDialog(statspackAWRInstancePanel,msg2,"ADDM Report Saved...",JOptionPane.OK_OPTION);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e, this, "DBA privilege is required for this.");
        } 
      }
    }

  }


  /* Not used since 17.79 (05/02/16) when I changed all WR reports to HTML format and saved them to disk rather than display them */
  void getAWRReports() {
    try {
      String cursorId = "RichMonAWRREPORT";
      Cursor myCursor = new Cursor(cursorId, false);
      
      
      for (int i = 0; i < snapshotIdRange.length - 1; i++) {
        myProgressM.setProgress(i);
        myProgressM.setNote("Running AWR report " + i + " of " + 
                            (snapshotIdRange.length - 1));
        
        myCursor.setSQLTxtOriginal("select output from table(dbms_workload_repository.awr_report_text( " + statspackAWRInstancePanel.getDbId() + "," + statspackAWRInstancePanel.getInstanceNumber() + 
                                   "," + snapshotIdRange[i] + "," + 
                                   snapshotIdRange[i + 1] + "));");
        QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
        String resultString =  ExecuteDisplay.createOutputString(myResult, false);

        File awrFile = new File(ConnectWindow.getBaseDir() + "\\AWR Reports\\AWR_" + instanceName + "_" + snapshotIdRange[i] + "_" + snapshotIdRange[i + 1] + ".txt");
        if (ConnectWindow.isLinux()) awrFile = new File(ConnectWindow.getBaseDir() + "//AWR Reports//AWR_" + instanceName + "_" + snapshotIdRange[i] + "_" + snapshotIdRange[i + 1] + ".txt");
      
        BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
        awrFileWriter.write(resultString);
        awrFileWriter.close();

        if (debug)
          System.out.println(resultString);
      }
    } catch (IOException e) {
      ConsoleWindow.displayError(e,this);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this); 
    }
  }
  
  void getAWRReportsHTML() {
    try {
      String cursorId = "RichMonAWRREPORT";
      Cursor myCursor = new Cursor(cursorId, false);
      
      
      for (int i = 0; i < snapshotIdRange.length - 1; i++) {
        myProgressM.setProgress(i);
        myProgressM.setNote("Running AWR report " + i + " of " + 
                            (snapshotIdRange.length - 1));
        
        myCursor.setSQLTxtOriginal("select output from table(dbms_workload_repository.awr_report_HTML( " + statspackAWRInstancePanel.getDbId() + "," + statspackAWRInstancePanel.getInstanceNumber() + 
                                   "," + snapshotIdRange[i] + "," + 
                                   snapshotIdRange[i + 1] + "));");
        QueryResult myResult = ExecuteDisplay.execute(myCursor,false,true,null);
        String resultString =  ExecuteDisplay.createOutputString(myResult, false);

        File awrFile = new File(ConnectWindow.getBaseDir() + "\\AWR Reports\\AWR_" + instanceName + "_" + snapshotIdRange[i] + "_" + snapshotIdRange[i + 1] + ".html");
        if (ConnectWindow.isLinux()) awrFile = new File(ConnectWindow.getBaseDir() + "//AWR Reports//AWR_" + instanceName + "_" + snapshotIdRange[i] + "_" + snapshotIdRange[i + 1] + ".html");
        
        BufferedWriter awrFileWriter = new BufferedWriter(new FileWriter(awrFile));
        awrFileWriter.write(resultString);
        awrFileWriter.close();

        if (debug)
          System.out.println(resultString);
      }
    } catch (IOException e) {
      ConsoleWindow.displayError(e,this);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this); 
    }
  }
}
