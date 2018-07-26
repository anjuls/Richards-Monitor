/*
 *  ScratchPanel.java        17.52 09/08/11
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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class ScratchPanel extends JPanel {
  static JTabbedPane scratchTP = new JTabbedPane();
      
    
  /**
   * Constructor.
   */
  public ScratchPanel() {   
    
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
        
    // layout 
    this.setLayout(new BorderLayout());
    
    this.add(scratchTP, BorderLayout.CENTER);
    
    
  }
  

  
  public static JTabbedPane getScratchTP() {
    return scratchTP;
  }
  
}