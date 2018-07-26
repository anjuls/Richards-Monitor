/*
 *  LockB.java        1.0 18/02/05
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
 * 22/08/06 Richard Wright Modified the comment style and error handling
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 31/10/07 Richard Wright Enhanced for RAC
 * 15/05/09 Richard Wright Removed the RAC stuff as the sql will now always use gv$ and show the instance name.
 * 04/05/10 Richard Wright Extend RichButton
 * 26/11/12 Richard Wright Improved Lockblockers and holders so it is not dependant on what instance is selected
 * 25/08/15 Richard Wright Modified to allow filter by user and improved readability
 * 08/01/18 Richard Wright Improved version of lock holders and waiter for 9.2 and above
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;


/**
 * Implements all lock / enqueue queries.
 */
public class LockB extends RichButton {
  boolean showSQL;

  /**
   * Constructor
   * 
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public LockB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Locks");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    
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
    DatabasePanel.setLastAction(selection);
     
    try {  
      if (selection.equals("Lock Holder+Waiter")) {
        String cursorId;
        
        if (ConsoleWindow.getDBVersion() >= 9.2) {
          cursorId = "gv$LockHoldersWithWaiters92.sql";
        }
        else {
          cursorId = "gv$LockHoldersWithWaiters.sql";
        }
        
        Cursor myCursor = new Cursor(cursorId,true);
        
        Parameters myPars = new Parameters();
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = false;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "none";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = false;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
      
      if (selection.equals("Lock Waiters")) {
        Cursor myCursor = new Cursor("gv$LockWaiters.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "l";
        String filterByUserAlias = "l";
        Boolean restrictRows = true;
        Boolean flip = false;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
            
      if (selection.equals("All Locks (Decoded)")) {
        Cursor myCursor = new Cursor("gv$AllLocksDecoded.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "l";
        String filterByUserAlias = "s";
        Boolean restrictRows = true;
        Boolean flip = false;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  public void listActionPerformed(boolean showSQL,String lastAction) {
    this.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  }
}