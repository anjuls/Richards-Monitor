/*
 * FlashFRA.java        17.38 06/10/09
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
 * 04/05/10 Richard Wright Extend RichButton
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * Implements undo related queries.
 */
public class FlashFRAB extends RichButton {
  boolean showSQL;

  /**
   * Constructor
   *
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public FlashFRAB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("Flash / FRA");
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

        public void mousePressed(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
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
      if (selection.equals("Flashback Log")) {
        
        Cursor myCursor = new Cursor("flashbackLog.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "fl";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);    

      }

      if (selection.equals("Flashback Stat")) {
        
        Cursor myCursor = new Cursor("flashbackStat.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "fl";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);    

      }

      if (selection.equals("v$flash_recovery_area_usage")) {
        
        Cursor myCursor = new Cursor("v$flash_recovery_area_usage",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "v$flash_recovery_area_usage";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);    

      }
      
      if (selection.equals("v$recovery_file_dest")) {
        
        Cursor myCursor = new Cursor("v$recovery_file_dest",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "v$recovery_file_dest";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);    
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  } 
  
  public void listActionPerformed(boolean showSQL,
                                  String lastAction) {
    this.showSQL = showSQL;
    listActionPerformed(lastAction, new JFrame());
  }
}
