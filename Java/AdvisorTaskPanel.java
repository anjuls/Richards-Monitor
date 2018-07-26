/*
 * AdvisorTaskPanel.java        17.52 25/07/11
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
 * (Prior to 29/12/09 this funtionality was performed from within the ExecuteDisplay class)
 * 
 * Change History
 * ==============
 *
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;


public class AdvisorTaskPanel extends JFrame {
  
  JTabbedPane myTabbedPane = new JTabbedPane();
  JPanel myAdvisorFindingsPanel = new JPanel(new BorderLayout());
  JScrollPane myAdvisorFindingsSP = new JScrollPane();
  JTextPane myAdvisorFindingsTP = new JTextPane();
  
  JPanel myAdvisorRecommendationsPanel = new JPanel(new BorderLayout());
  JScrollPane myAdvisorRecommendationsSP = new JScrollPane();
  
  JPanel myAdvisorActionsPanel = new JPanel(new BorderLayout());
  JScrollPane myAdvisorActionsSP = new JScrollPane();
  
  JPanel myBindCapturePanel = new JPanel(new BorderLayout());
  JScrollPane myBindCaptureSP = new JScrollPane();

  
  String taskId;
  boolean restrictSnapshotRange=false;

  public AdvisorTaskPanel(String taskId) {
    this.taskId = taskId;
    
    jb_init();
    
    this.setTitle("DBA Advisor Task: " + taskId);

    
    Parameters myPars = new Parameters();     
    myPars.addParameter("int",Integer.valueOf(taskId).intValue());
      
    QueryResult myAdvisorFindings = new QueryResult();
    QueryResult myAdvisorRecommendations = new QueryResult();
    QueryResult myAdvisorActions = new QueryResult();
    
    try {
      myAdvisorFindings = ExecuteDisplay.execute("dbaAdvisorFindings.sql",myPars,false,true,null);
      myAdvisorRecommendations = ExecuteDisplay.execute("dbaAdvisorRecommendations.sql",myPars,false,true,null);
      myAdvisorActions = ExecuteDisplay.execute("dbaAdvisorActions.sql",myPars,false,true,null);

      JTable myAdvisorFindingsTable = ExecuteDisplay.createTable(myAdvisorFindings);   
      myAdvisorFindingsSP.getViewport().add(myAdvisorFindingsTable);

      JTable myAdvisorRecommendationsTable = ExecuteDisplay.createTable(myAdvisorRecommendations);   
      myAdvisorRecommendationsSP.getViewport().add(myAdvisorRecommendationsTable);

      JTable myAdvisorActionsTable = ExecuteDisplay.createTable(myAdvisorActions);   
      myAdvisorActionsSP.getViewport().add(myAdvisorActionsTable);
      
    }
    catch (Exception ee) {
      JOptionPane.showMessageDialog(this,"Error fetching advisor task details: " + ee.toString());
    }
    
    // add the richmon icon to the frame title bar
    this.setIconImage(ConnectWindow.getRichMonIcon().getImage());

    this.setVisible(true);
  }
  
  private void jb_init() {
    
    int width = Properties.getAdditionalWindowWidth();
    int height = Properties.getAdditionalWindowHeight();
    this.setSize(new Dimension(width, height));
    this.getContentPane().setLayout(new BorderLayout());
     
    myAdvisorFindingsSP.getViewport().add(myAdvisorFindingsTP);
    myAdvisorFindingsPanel.add(myAdvisorFindingsSP,BorderLayout.CENTER);

    myTabbedPane.addTab("Findings",myAdvisorFindingsPanel);
    this.getContentPane().add(myTabbedPane,BorderLayout.CENTER);
    

    myAdvisorRecommendationsSP.getViewport().setBackground(Color.WHITE); 
    myAdvisorRecommendationsPanel.add(myAdvisorRecommendationsSP);
    myTabbedPane.addTab("Recommendations",myAdvisorRecommendationsPanel);
    
    
    myAdvisorActionsPanel.add(myAdvisorActionsSP);
    myTabbedPane.addTab("Actions",myAdvisorActionsPanel);      
 
  }
}
