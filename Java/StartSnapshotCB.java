/*
 *  StartSnapshotsCB.java        13.0 13/06/05
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
 * Change History since 23/05/05
 * =============================
 * 14/09/06 Richard Wright Modified the comment style and error handling
 */


package RichMon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements dispatcher and shared server queries.
 */
public class StartSnapshotCB extends JComboBox {
  JLabel statusBar;         // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;      // The JScrollPane on which output should be displayed 
  ResultCache resultCache;

  /**
   * Constructor 
   * 
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public StartSnapshotCB(Object[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    super(options);
    
    this.setSize(300,25);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action 
   * 
   * @param showSQL  
   */
  public void actionPerformed(boolean showSQL) {
    String selection = (String)this.getSelectedItem();

    try {
      String cursorId = "";
      
      if (selection.equals("MTS Dispatchers")) { 
        cursorId = "dispatchers.sql";
      }
      
      if (selection.equals("MTS Shared Servers")) {
        cursorId = "sharedServers.sql";
      }

      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      ExecuteDisplay.executeDisplay(myCursor,myPars, scrollP, statusBar, showSQL, resultCache, true);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}