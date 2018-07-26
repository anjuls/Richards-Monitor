/*
 * SPLatchB.java        13.22 28/06/06
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
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
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
 *
 */
public class SPLatchB extends JButton {
  JLabel statusBar;              // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;           // The JScrollPane on which output should be displayed 
  String[] options;              // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel; // reference to the parent panel 


  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPLatchB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Latches");
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
      QueryResult myResult = new QueryResult();
      if (selection.equals("Latch Summary")) {
        myResult = getLatchSummary();
      }

      if (selection.equals("Latch Sleeps")) {
        myResult = getLatchSleeps();
      }

      if (selection.equals("Latch Misses")) {
        myResult = getLatchMisses();
      }

      ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getLatchSummary() throws Exception {
    QueryResult myResult;

    statspackAWRInstancePanel.sanityCheckRange();

    String cursorId = "";
    Parameters myPars = new Parameters();

    if (ConsoleWindow.getDBVersion() < 10) {
      if (ConsoleWindow.getDBVersion() < 9) {
        cursorId = "stats$LatchSummary8.sql";
      }
      else {
        cursorId = "stats$LatchSummary.sql";        
      }
    }
    else {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$LatchSummary.sql";
      }
      else {
        cursorId = "stats$LatchSummaryAWR.sql";
      }
    }
    
    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }  
  
  public QueryResult getLatchMisses() throws Exception {
    QueryResult myResult;

    statspackAWRInstancePanel.sanityCheckRange();


    String cursorId = "";
    Parameters myPars = new Parameters();
//    Object[][] parameters = new Object[2][6];

    if (ConsoleWindow.getDBVersion() == 8.16) {
      cursorId = "stats$LatchMissesSummary816.sql";
    }
    else {
      if (ConsoleWindow.getDBVersion() < 10) {
        cursorId = "stats$LatchMissesSummary.sql";
      }
      else {
        if (Properties.isAvoid_awr()) {
          cursorId = "stats$LatchMissesSummary.sql";
        }
        else {
          cursorId = "stats$LatchMissesSummaryAWR.sql";
        }
      }
    }
    

    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    
    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }

  public QueryResult getLatchSleeps() throws Exception {
    QueryResult myResult;

    statspackAWRInstancePanel.sanityCheckRange();

    String cursorId = "";
    Parameters myPars = new Parameters();
//    Object[][] parameters = new Object[2][6];

    if (ConsoleWindow.getDBVersion() < 10) {
      cursorId = "stats$LatchSleeps.sql";
    }
    else {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$LatchSleeps.sql";
      }
      else {
        cursorId = "stats$LatchSleepsAWR.sql";
      }
    }

    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }


}
