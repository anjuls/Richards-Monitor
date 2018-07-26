/*
 *  WhatsThisB.java        1.0 18/02/05
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
 * Change History since 23/05/05
 * =============================
 * 14/09/06 Richard Wright Modified the comment style and error handling
 * 04/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 04/05/10 Richard Wright Extend RichButton
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;


/**
 * Implements a query against dba_objects to find details about any object/segment.  Queries details of any synonym.
 */
public class WhatsThisB extends RichButton {
  boolean showSQL;
  boolean tearOff;  
  String whatsThat; // last entered value for whatsThat 
  String synonymName; // last entered synonym

  /**
   * Constructor
   * 
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public WhatsThisB(String[] options, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText("What's This");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;
    
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
   * Performs the user selected action from the sharedServerB JComboBox
   * 
   * @param selectedOption * @param tearOff - controls whether the result should be displayed in a seperate window(true) or not (false)
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    DatabasePanel.setLastAction(selection);
    String cursorId = "";    
    try {  
      if (selection.equals("What's This")) {         
        // ask the user to enter the segment name of interest 
        whatsThat = (String)JOptionPane.showInputDialog(
                    scrollP,
                    "What object are you interested in\n\n ",
                    "Whats This...",
                    JOptionPane.QUESTION_MESSAGE,null,null,whatsThat);
        
        if (whatsThat instanceof String) {
          // prefix and suffix parameter name with % 
          whatsThat = whatsThat.trim();
          
          Cursor myCursor = new Cursor("whatsThis.sql",true);
          Parameters myPars = new Parameters();
          myPars.addParameter("String","%" + whatsThat + "%");
          Boolean filterByRAC = false;
          Boolean filterByUser = false;
          Boolean includeDecode = false;
          String includeRACPredicatePoint = "default";
          String filterByRACAlias = "none";
          String filterByUserAlias = "none";
          Boolean restrictRows = true;
          Boolean flip = true;
          Boolean eggTimer = true;
          executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
        }
        else {
          whatsThat = "%";
        }
      }
      
      if (selection.equals("Synonym")) {
        // ask the user to enter the segment name of interest 
        synonymName = (String)JOptionPane.showInputDialog(
                    this,
                    "Specify the SYNONYM you are interested in\n\n ",
                    "Whats This...",
                    JOptionPane.QUESTION_MESSAGE,null,null,whatsThat);
                    
          
        // prefix and suffix parameter name with % 
        synonymName = synonymName.trim();
        
        Cursor myCursor = new Cursor("synonyms.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String","%" + synonymName + "%");
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = false;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "none";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  public void listActionPerformed(boolean showSQL, String lastAction) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;
    listActionPerformed(lastAction, new JFrame());
  };  
}