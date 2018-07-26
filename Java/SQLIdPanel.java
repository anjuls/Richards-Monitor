/*
 *  SQLIdPanel.java        17.52 08/08/11
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

 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;


public class SQLIdPanel extends JPanel {
  // status bar 
  JLabel statusBarL = new JLabel();
  
  // panels 
  JPanel buttonsP = new JPanel();  
  static JTabbedPane sqlIdTP = new JTabbedPane();
  
  Label sqlIdL = new Label("Enter a SQL ID:");
  TextField sqlIdTF = new TextField();
  JButton sqlIdB = new JButton("Get SQL Details");
       
    
  /**
   * Constructor.
   */
  public SQLIdPanel() {   
    
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */  
  private void jbInit() throws Exception {
    
    Dimension standardButtonSize = Properties.getStandardButtonDefaultSize();
    
    // layout 
    this.setLayout(new BorderLayout());

        
    sqlIdB.setPreferredSize(standardButtonSize);
    sqlIdB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sqlIdB_actionPerformed();
        }
      });
    
    buttonsP.add(sqlIdL);
    //sqlIdTF.setBounds(new Rectangle(50, 95, 85, 20));
    sqlIdTF.setPreferredSize(new Dimension(160,20));
    sqlIdTF.setForeground(Color.blue);
    buttonsP.add(sqlIdTF);
    buttonsP.add(sqlIdB);
    buttonsP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    
    // statusBar 
    statusBarL.setText(" ");
        
    // construct performancePanel 
    this.add(statusBarL, BorderLayout.SOUTH);
    this.add(buttonsP, BorderLayout.NORTH);
    this.add(sqlIdTP, BorderLayout.CENTER);
  }
  
  private void sqlIdB_actionPerformed() {
//    ConsoleWindow.addSQLDetailPanel(sqlIdTF.getText().trim(), false, sqlIdTP);
    String[] snapDateRange = new String[0];
    ConsoleWindow.addSQLDetailPanel(sqlIdTF.getText().trim(),sqlIdTP,ConsoleWindow.getDatabaseId(),ConsoleWindow.getThisInstanceNumber(),0,0,0, snapDateRange);
  }
  
  public static JTabbedPane getSQL_IdPane() {
    return sqlIdTP;
  }
  
}