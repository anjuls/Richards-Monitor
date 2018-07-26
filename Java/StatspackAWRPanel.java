/*
 *  StatspackAWRPanel.java        17.52 09/08/11
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

public class StatspackAWRPanel extends JPanel {
    static JTabbedPane statspackAWRTP = new JTabbedPane();
    double dbVersion;
    
 
        
    static boolean debug = false;
      
    /**
     * Constructor.
     */
    public StatspackAWRPanel() {   
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
      
      this.setLayout(new BorderLayout());
      this.add(statspackAWRTP, BorderLayout.CENTER);
    }
    
    public static void addInstancePanel(int instanceNumber,String instanceName,long dbId, boolean imported, String prefix) {
      if (prefix != null) instanceName = prefix + "-" + instanceName;
      StatspackAWRInstancePanel instancePanel = new StatspackAWRInstancePanel(instanceNumber,instanceName,dbId,imported);
      statspackAWRTP.add(instancePanel,instanceName);
      
      if (debug) System.out.println("Added AWR Panel: " + instanceName);
    }
    
    public static void removeAllInstancePanels() {
      statspackAWRTP.removeAll();
    }    
    
    public static void removeImportedInstancePanels() {
      for (int i=statspackAWRTP.getComponentCount() -1; i >= 0; i--) {
        StatspackAWRInstancePanel statspackAWRInstancePanel = (StatspackAWRInstancePanel)statspackAWRTP.getComponentAt(i);
        if (statspackAWRInstancePanel.isImported()) {
          statspackAWRTP.remove(i);
        }
      }
    }
    
  public static void addPerformanceReviewButton() {
    for (int i=0; i < statspackAWRTP.getComponentCount(); i++) {
      StatspackAWRInstancePanel statspackAWRInstancePanel = (StatspackAWRInstancePanel)statspackAWRTP.getComponentAt(i);
      if (!statspackAWRInstancePanel.isImported()) {
        statspackAWRInstancePanel.addPerformanceReviewButton();
      }
    }
  }
  
  public static void removePerformanceReviewButton() {
    for (int i=0; i < statspackAWRTP.getComponentCount(); i++) {
      StatspackAWRInstancePanel statspackAWRInstancePanel = (StatspackAWRInstancePanel)statspackAWRTP.getComponentAt(i);
      if (!statspackAWRInstancePanel.isImported()) {
        statspackAWRInstancePanel.removePerformanceReviewButton();
      }
    }
  }

}