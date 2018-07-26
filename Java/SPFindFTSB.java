/*
 * SPFindFTSB.java        17.71 27/04/15
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
public class SPFindFTSB extends JButton {
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
  public SPFindFTSB(StatspackAWRInstancePanel statspackPanel, String buttonName,
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

    try {
      String cursorId = "findFTSAWR.sql";
      
      int startSnapId = statspackPanel.getStartSnapId();
      int endSnapId = statspackPanel.getEndSnapId();
      
      Parameters myPars = new Parameters();
      myPars.addParameter("long", statspackPanel.getDbId());
      myPars.addParameter("long", statspackPanel.getDbId());
      myPars.addParameter("int", startSnapId);
      myPars.addParameter("int", endSnapId);
      myPars.addParameter("long", statspackPanel.getInstanceNumber());
      
      boolean showSQL = false;
      boolean restrictRows = true;

      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackPanel.getInstanceName(),null);
      ExecuteDisplay.displayTable(myResult, statspackPanel, false, statusBar);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}
