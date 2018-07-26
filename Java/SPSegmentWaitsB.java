/*
 *  SPSegmentWaitsB.java        13.22 28/06/06
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
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 07/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 05/03/12 Richard Wright Modified by use awrCache
 */
 
 package RichMon;


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;


/**
 * Get Segment Wait Statistics
 */
public class SPSegmentWaitsB extends JButton {
  JLabel statusBar;      // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;   // The JScrollPane on which output should be displayed 
  String[] options;      // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;

  /**
   * Constructor 
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPSegmentWaitsB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Segment Waits");
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
      int[] snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
//      String[] snapDateRange = statspackPanel.getSelectedSnapDateRange();
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("Segment by Logical Reads")) {
        QueryResult myResult = getSegmentLogicalReads();
//        ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }

      if (selection.equals("Segment by Physical Reads")) {
        QueryResult myResult = getSegmentPhysicalReads();
//        ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
         ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);      
      }      
      
      if (selection.equals("Segment by Row Lock Waits")) {
        QueryResult myResult = getSegmentRowLockWaits();
//        ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
         ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);      
      }      

      if (selection.equals("Segment by ITL Waits")) {
        QueryResult myResult = getSegmentITLWaits();
//        ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
      ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);      
      }
      
      if (selection.equals("Segment by Buffer Busy Waits")) {
        QueryResult myResult = getSegmentBufferBusyWaits();
//        ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);      

      }

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getSegmentLogicalReads() throws Exception {
    QueryResult myResult = new QueryResult();

    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$SegmentLogicalReads.sql";
      }
      else {
        cursorId = "stats$SegmentLogicalReadsAWR.sql";
      }
    }
    else {
      cursorId = "stats$SegmentLogicalReads.sql";
    }

    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }  
  
  public QueryResult getSegmentPhysicalReads() throws Exception {
    QueryResult myResult = new QueryResult();

    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$SegmentPhysicalReads.sql";
      }
      else {
        cursorId = "stats$SegmentPhysicalReadsAWR.sql";
      }
    }
    else {
      cursorId = "stats$SegmentPhysicalReads.sql";
    }

    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());


    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }  
  
  public QueryResult getSegmentITLWaits() throws Exception {
    QueryResult myResult = new QueryResult();

    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$SegmentITLWaits.sql";
      }
      else {
        cursorId = "stats$SegmentITLWaitsAWR.sql";
      }
    }
    else {
      cursorId = "stats$SegmentITLWaits.sql";
    }

    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }  
  
  public QueryResult getSegmentBufferBusyWaits() throws Exception {
    QueryResult myResult = new QueryResult();

    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$SegmentBufferBusyWaits.sql";
      }
      else {
        cursorId = "stats$SegmentBufferBusyWaitsAWR.sql";
      }
    }
    else {
      cursorId = "stats$SegmentBufferBusyWaits.sql";
    }
    
    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }

  public QueryResult getSegmentRowLockWaits() throws Exception {
    QueryResult myResult = new QueryResult();

    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();

    String cursorId;

    if (ConsoleWindow.getDBVersion() >= 10) {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$SegmentRowLocks.sql";
      }
      else {
        cursorId = "stats$SegmentRowLocksAWR.sql";
      }
    }
    else {
      cursorId = "stats$SegmentRowLocks.sql";
    }

    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }


}
