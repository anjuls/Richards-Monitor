/*
 * SPUndo.java        13.18 05/04/06
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
 * Change History
 * ==============
 * 16/06/06 Richard Wright Modified the calls so that the output is not displayed twice
 * 30/06/06 Richard Wright Included V8 support
 * 12/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackAWRInstancePanel
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 05/03/12 Richard Wright Modified by use awrCache
 */
 
 package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * 
 */
public class SPUndoB extends RichButton {
  JLabel statusBar;              // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;           // The JScrollPane on which output should be displayed 
  String[] options;              // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel; // reference to the parent panel 

  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @parem statusBar 
   */
  public SPUndoB(StatspackAWRInstancePanel statspackAWRInstancePanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Undo");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackAWRInstancePanel;
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
      if (selection.equals("Undo Summary"))  {
        myResult = getUndoSummary();
      }
      
      if (selection.equals("Undo Detail")) { 
        myResult = getUndoDetail();
      }
      
      ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
    }
    catch (Exception e) {
      
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getUndoSummary() throws Exception {
    QueryResult myResult;
    
    
    String[] snapshotDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();
                           

    String cursorId = "";

    if (ConsoleWindow.getDBVersion() < 10) {
      if (ConsoleWindow.getDBVersion() >= 9.0) {
        cursorId = "spUndoSummary9.sql";
      }
      else {
        cursorId = "spUndoSummary8.sql";      
      }
    }
    else {
      if (Properties.isAvoid_awr()) {
        cursorId = "spUndoSummary.sql";
      }
      else {
        cursorId = "spUndoSummaryAWR.sql";            
      }
    }  
   
    Parameters myPars = new Parameters();
    if (ConsoleWindow.getDBVersion() >= 9.0) {
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String",snapshotDateRange[0]);
      myPars.addParameter("String",snapshotDateRange[snapshotDateRange.length -1]);
    }
    else {
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, true, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    
    return myResult;
  }
  
  public QueryResult getUndoDetail() throws Exception {
    QueryResult myResult;

    String[] snapshotDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    int startSnapId = statspackAWRInstancePanel.getStartSnapId();
    int endSnapId = statspackAWRInstancePanel.getEndSnapId();
                           

    String cursorId = "";
    
    if (ConsoleWindow.getDBVersion() < 10) {
      if (ConsoleWindow.getDBVersion() >= 9.0) {
        cursorId = "spUndoStats9.sql";
      }
      else {
        cursorId = "spUndoStats8.sql";
        
      }
    }
    else {
      if (Properties.isAvoid_awr()) {
        cursorId = "spUndoStats.sql";
      }
      else {
        cursorId = "spUndoStatsAWR.sql";            
      }
    }
    
    Parameters myPars = new Parameters();
    if (ConsoleWindow.getDBVersion() >= 9.0) {     
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("String",snapshotDateRange[0]);
      myPars.addParameter("String",snapshotDateRange[snapshotDateRange.length -1]);
    }
    else {
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
    }

    myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);

    return myResult;
  }


}
