package RichMon;

/*
 *  SPResourceLimitB.java        13.14 28/02/06
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
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 */
 
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements session related queries.
 */
public class SPResourceLimitB extends JButton {
  JLabel statusBar;       // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;    // The JScrollPane on which output should be displayed 

  StatspackAWRInstancePanel statspackPanel; // reference to the parent panel 

  int[] snapIdRange;      // list of the snap_id's in the range to be charted 

  String[] snapDateRange; // list the snapshot date/times to be charted 

  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPResourceLimitB(StatspackAWRInstancePanel statspackPanel, String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);

    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackPanel = statspackPanel;
  }

  /**
   * Perform the user selected action
   * 
   */
  public void actionPerformed() {
    try {
      QueryResult myResult = createOutput();

      //ExecuteDisplay.displayTable(myResult,scrollP,false,statusBar);
      ExecuteDisplay.displayTable(myResult,statspackPanel,false,statusBar);
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult createOutput() throws NotEnoughSnapshotsException, Exception {
    statspackPanel.clearScrollP();
    snapIdRange = statspackPanel.getSelectedSnapIdRange();
    snapDateRange = statspackPanel.getSelectedSnapDateRange();
    statspackPanel.sanityCheckRange();

    String cursorId;
    Parameters myPars = new Parameters();
    if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
      cursorId = "stats$ResourceLimitAWR.sql";
      
      myPars.addParameter("int",statspackPanel.getStartSnapId());
      myPars.addParameter("int",statspackPanel.getEndSnapId());
      myPars.addParameter("long",statspackPanel.getDbId());
      myPars.addParameter("int",statspackPanel.getInstanceNumber());
      
    } else {
      cursorId = "stats$ResourceLimit.sql";
      
      myPars.addParameter("int",statspackPanel.getStartSnapId());
      myPars.addParameter("int",statspackPanel.getEndSnapId());
      myPars.addParameter("long",statspackPanel.getDbId());
    }
    
    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackPanel.getStartSnapId(), statspackPanel.getEndSnapId(), statspackPanel.getInstanceName(),null);
    
    return myResult;
  }
}
