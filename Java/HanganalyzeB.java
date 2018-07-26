/*
 *  HanganalyzeB.java        17.39 09/12/09
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
 */


package RichMon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * Show long operations
 */
public class HanganalyzeB extends JButton {

  /**
   * Constructor 
   * 
   */
  public HanganalyzeB() {
    super("HangAnalyze");
  }

  /**
   * Performs the user selected action 
   * 
   */
  public void actionPerformed() {
    String msg = "Hanganalyze will create a trace file in your udump directory using:\n\n" +
                 "alter session set events 'immediate trace name hanganalyze level 3';";
    
    String[] options = {"Yes","No"};
    
    int n = JOptionPane.showOptionDialog(ConsoleWindow.getPerformancePanel(), 
                                         msg, 
                                         "Hanganalyze", 
                                         JOptionPane.YES_NO_OPTION, 
                                         JOptionPane.QUESTION_MESSAGE, 
                                         null,
                                         options,
                                         options[0]);
    
    if (n==0) {
      try {
        ConnectWindow.getDatabase().hanganalyze();
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this, "DBA Privileges are required for running HANGANALYZE");
      }
    }
  }
}