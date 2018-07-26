/*
 *  SPEnqueueB.java        13.22 30/06/06
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
 * 14/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 05/03/12 Richard Wright Modified by use awrCache
 */
 
 package RichMon;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;


/**
 *
 */
public class SPEnqueueB extends JButton {
  JLabel statusBar;              // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;           // The JScrollPane on which output should be displayed 
  StatspackAWRInstancePanel statspackAWRInstancePanel; // parent panel 


  /**
   * Constructor 
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPEnqueueB(StatspackAWRInstancePanel statspackPanel, String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);

    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  /**
   * Performs the user selected action
   * 
   */
  public void actionPerformed() {
    QueryResult myResult;
    try {
      myResult = getEnqueue();
      
      ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getEnqueue() throws Exception {
    statspackAWRInstancePanel.sanityCheckRange();
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();
                           
    String cursorId;
    
    if (ConsoleWindow.getDBVersion() < 10) {
      if (ConsoleWindow.getDBVersion() < 9) {
        cursorId = "stats$Enqueue8.sql";
      }
      else {
        cursorId = "stats$Enqueue.sql";        
      }
    }
    else {
      if (Properties.isAvoid_awr()) {
        cursorId = "stats$Enqueue10.sql";
      }
      else {
        cursorId = "stats$EnqueueAWR.sql";
      }
    }
    
    Parameters myPars = new Parameters();
    myPars.addParameter("int",startSnapId);
    myPars.addParameter("int",endSnapId);
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

    QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }

}
