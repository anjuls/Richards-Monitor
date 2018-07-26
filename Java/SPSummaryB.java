/*
 * SPSummaryB.java        13.18 11/04/06
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
 * 21/06/06 Richard Wright Corrected problem caused when elapsed time was not 10 digits long
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 12/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 06/12/07 Richard Wright Enhanced for RAC
 * 16/10/08 Richard Wright Amended the maths to cope when elapsed time doesn't have any decimal places
 * 04/05/10 Richard Wright Extend RichButton
 * 05/03/12 Richard Wright Modified to use awrCache
 */ 


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;


/**
 * Implements a query to show a summary of each database session.
 */
public class SPSummaryB extends RichButton {
  StatspackAWRInstancePanel statspackAWRInstancePanel; // parent panel 

  int[] snapIdRange;      // list of the snap_id's in the range to be charted 
  String[] snapDateRange; // list of snap dates in the range to be charted 
  int numSnapshots;
  

  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @parem statusBar
   */
  public SPSummaryB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Db Summary");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;

  }

  public void actionPerformed() {
    // if a list frame already exists, then remove it
    ConnectWindow.getConsoleWindow().removeLastFrame();
    
    final JList myList = new JList(options);     
    myList.setVisibleRowCount(options.length);
    JScrollPane listScroller = new JScrollPane(myList);
    final JFrame listFrame = new JFrame(this.getText());
    listFrame.add(listScroller);
    listFrame.pack();
    listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    listFrame.setLocationRelativeTo(this);
    
    myList.addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          listActionPerformed(myList.getSelectedValue(), listFrame);
          listFrame.dispose();
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
      });
    
    listFrame.setIconImage(ConnectWindow.getRichMonIcon().getImage());   
    listFrame.setVisible(true);  

    // save a reference to the listFrame so it can be removed if left behind
    ConnectWindow.getConsoleWindow().saveFrameRef(listFrame);
  }

  /**
   * Performs the user selected action 
   * 
   * @param selectedOption 
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    statspackAWRInstancePanel.setLastAction(selection);
    try {  
      statspackAWRInstancePanel.clearScrollP();
      snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("Summary")) {
        QueryResult myResult = getSummary();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
      if (selection.equals("Load Profile")) {
        QueryResult myResult = getloadProfile();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }    
      
      if (selection.equals("Time Model Statistics")) {
        QueryResult myResult = getTimeModelStatistics();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }      

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getSummary() throws Exception {
    String cursorId;

    Parameters myPars;
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();
    
    if (ConsoleWindow.getDBVersion() < 10 || Properties.isAvoid_awr()) {
      cursorId = "SPSummary.sql";

      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);      
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);      
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    else {
      cursorId = "SPSummaryAWR.sql";

      myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",endSnapId);      
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",endSnapId);      
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",endSnapId);      
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }
    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }
  

  public QueryResult getloadProfile() throws Exception {
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;
    if (ConsoleWindow.getDBVersion() >= 10) {
      cursorId = "SPLoadProfileStatsCollectionAWR.sql";
    }
    else {
      cursorId = "SPLoadProfileStatsCollection.sql";
    }
    
    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    
    long redoSize = 0;
    long sessionLogicalReads = 0;
    long dbBlockChanges = 0;
    long physicalReads = 0;
    long physicalWrites = 0;
    long userCalls = 0;
    long parseCountHard = 0;
    long parseCountTotal = 0;
    long sortsRows = 0;
    long sortsMemory = 0;
    long sortsDisk = 0;
    long logonsCumulative = 0;
    long executeCount = 0;
    long userCommits = 0;
    long recursiveCalls = 0;
    long transactionRollbacks = 0;
    long userRollbacks = 0;
    
    String[][] resultSet = myResult.getResultSetAsStringArray();
    
    for (int i=0; i < myResult.getNumRows(); i++) {
      if (resultSet[i][0].equals("redo size")) redoSize = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("db block changes")) dbBlockChanges = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("session logical reads")) sessionLogicalReads = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("physical reads")) physicalReads = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("physical writes")) physicalWrites = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("user calls")) userCalls = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("parse count (hard)")) parseCountHard = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("parse count (total)")) parseCountTotal = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("sorts (rows)")) sortsRows = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("sorts (memory)")) sortsMemory = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("sorts (disk)")) sortsDisk = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("logons cumulative")) logonsCumulative = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("execute count")) executeCount = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("user commits")) userCommits = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("recursive calls")) recursiveCalls = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("transaction rollbacks")) transactionRollbacks = Long.valueOf(resultSet[i][1]).longValue();
      if (resultSet[i][0].equals("user rollbacks")) userRollbacks = Long.valueOf(resultSet[i][1]).longValue();
    }
    
    // get the elapsed time in seconds 
    String[] dateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    cursorId = "SPDateRange.sql";
    
    myPars = new Parameters();
    myPars.addParameter("String",dateRange[dateRange.length -1]);
    myPars.addParameter("String",dateRange[0]);
    
    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    resultSet = myResult.getResultSetAsStringArray();
    
    
    String elapsedTime = String.valueOf(resultSet[0][0]);

    int daysInSecs = Math.abs(86400*Integer.valueOf((elapsedTime.substring(0,Math.max(elapsedTime.indexOf("."),1)))).intValue());
    String fractionOfDayInSecs = "0";
    if (elapsedTime.indexOf(".") > 0) fractionOfDayInSecs = String.valueOf(86400*Double.valueOf((elapsedTime.substring(elapsedTime.indexOf("."),Math.min(10,elapsedTime.length())))).doubleValue());
    int snapshotDurationSecs = daysInSecs + Integer.valueOf(fractionOfDayInSecs.substring(0,Math.max(fractionOfDayInSecs.indexOf("."),1))).intValue();
    
    if (userCommits == 0) userCommits++;
    
    // get the load profile 
    cursorId = "SPLoadProfile.sql";
    
    myPars = new Parameters();

    myPars.addParameter("long",redoSize);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",redoSize);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",sessionLogicalReads);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",sessionLogicalReads);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",dbBlockChanges);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",dbBlockChanges);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",physicalReads);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",physicalReads);
    myPars.addParameter("long",userCommits);
    
    myPars.addParameter("long",physicalWrites);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",physicalWrites);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",userCalls);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",userCalls);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",parseCountTotal);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",parseCountTotal);
    myPars.addParameter("long",userCommits);
    
    myPars.addParameter("long",parseCountHard);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",parseCountHard);
    myPars.addParameter("long",userCommits);
  
    myPars.addParameter("long",sortsDisk);
    myPars.addParameter("long",sortsMemory);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",sortsDisk);
    myPars.addParameter("long",sortsMemory);
    myPars.addParameter("long",userCommits);
    
    myPars.addParameter("long",logonsCumulative);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",logonsCumulative);
    myPars.addParameter("long",userCommits);

    myPars.addParameter("long",executeCount);
    myPars.addParameter("int",snapshotDurationSecs);
    myPars.addParameter("long",executeCount);
    myPars.addParameter("long",userCommits);
    
    myPars.addParameter("long",userCommits + userRollbacks);
    myPars.addParameter("int",snapshotDurationSecs);

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }
  
  public QueryResult getTimeModelStatistics() {
      
    QueryResult myResult = new QueryResult();
    
    try {
      float dbTime = getDBTime(statspackAWRInstancePanel.getStartSnapId(),statspackAWRInstancePanel.getEndSnapId(),statspackAWRInstancePanel.getDbId(),statspackAWRInstancePanel.getInstanceNumber());

      String cursorId = "stats$SysTimeModel-2.sql";
      
      Parameters myPars = new Parameters();
      myPars.addParameter("float", dbTime/1000000); 
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());     
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("float", dbTime/1000000);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
//      myPars.addParameter("float", dbTime/1000000);
      
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),"Time Model Statstics");
      
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return myResult;
  }
  
  public float getDBTime(int startSnapId, int endSnapId, long dbId, int instanceNumber) {    
    float dbTime = 0;
  
    try {
      String cursorId = "stats$SysTimeModel.sql";
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String","DB time");  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", dbId);     
      myPars.addParameter("int", instanceNumber);  
      
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),"DB time");
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      dbTime = Float.valueOf(resultSet[0][0]).floatValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return dbTime;
  }
}
