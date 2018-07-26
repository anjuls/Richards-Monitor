/*
 * SPLibraryCacheB.java        13.05 03/10/05
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
 * 19/10/05 Richard Wright Reduced the requirement for a range of 3 snapshots down to 2. 
 * 08/12/05 Richard Wright Error about db restarts now include the date/time of previous restarts
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 05/03/12 Richard Wight Modified to use awrCache
 * 
 */


package RichMon;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements session related queries.
 */
public class SPLibraryCacheB extends JButton {
  JLabel statusBar;         // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;      // The JScrollPane on which output should be displayed 
  StatspackAWRInstancePanel statspackAWRInstancePanel; // reference to the parent panel 

  int[] snapIdRange;        // list of the snap_id's in the range to be charted 
  String[] snapDateRange;   // list the snapshot date/times to be charted 

  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPLibraryCacheB(StatspackAWRInstancePanel statspackPanel,String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  /**
   * Performs the user selected action
   */
  public void actionPerformed() {
    try {
      QueryResult myResult = createOutput();     
      ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult createOutput() throws NotEnoughSnapshotsException, Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    statspackAWRInstancePanel.clearScrollP();
    
    String cursorId;
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
      cursorId = "stats$LibraryCacheAWR.sql";        
    }
    else {
      cursorId = "stats$LibraryCache.sql";
    }
      
    Parameters myPars = new Parameters();
    myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
    myPars.addParameter("int",statspackAWRInstancePanel.getEndSnapId());
    myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }
  

  
}