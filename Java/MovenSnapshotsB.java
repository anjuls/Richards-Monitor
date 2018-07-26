/*
 * MovenSnapshotsB.java        14.08 19/03/07
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
 * 10/09/07 Richard Wright Moved the refresh call into the StatspackAWRPanel
 */
 
 package RichMon;

import javax.swing.JButton;

public class MovenSnapshotsB extends JButton {
  StatspackAWRInstancePanel statspackAWRInstancePanel;

  int numSnaps = 0;

  public MovenSnapshotsB(String buttonName, StatspackAWRInstancePanel statspackP, 
                         int numSnaps) {
    super(buttonName);

    this.statspackAWRInstancePanel = statspackP;
    this.numSnaps = numSnaps;
  }

  public void actionPerformed() {
    int newStartSnap;
    int newEndSnap;
    
//    System.out.println("# starts: " + statspackP.startSnapshotCB.getItemCount());
//    System.out.println("# ends: " + statspackP.endSnapshotCB.getItemCount());
//    System.out.println("# snaps: " + numSnaps);
 
    int oldStartSnap = statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex();
    int oldEndSnap = statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex();
   
    if (numSnaps < 0) {
      if (statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex() > 0) {
        newStartSnap = statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex() - Math.abs(numSnaps);
        newEndSnap = statspackAWRInstancePanel.endSnapshotCB.getSelectedIndex() - Math.abs(numSnaps);
        int lag = oldEndSnap - oldStartSnap;
        
        // set startSnapshotCB
        if (newStartSnap < 0) {
          statspackAWRInstancePanel.startSnapshotCB.setSelectedIndex(0);
        }
        else {
          statspackAWRInstancePanel.startSnapshotCB.setSelectedIndex(newStartSnap);      
        }
        
        // set endSnapshotCB
        if (newEndSnap > (statspackAWRInstancePanel.endSnapshotCB.getItemCount() -1)) {
          statspackAWRInstancePanel.endSnapshotCB.setSelectedIndex(statspackAWRInstancePanel.endSnapshotCB.getItemCount() -1);
        }
        else {
          statspackAWRInstancePanel.endSnapshotCB.setSelectedIndex(newEndSnap);
        }    
      }
    }
    else {
      if (statspackAWRInstancePanel.endSnapshotCB.getSelectedIndex() < statspackAWRInstancePanel.endSnapshotCB.getItemCount() -1) {
        newStartSnap = statspackAWRInstancePanel.startSnapshotCB.getSelectedIndex() + numSnaps;
        newEndSnap = statspackAWRInstancePanel.endSnapshotCB.getSelectedIndex() + numSnaps;
        int lag = oldEndSnap - oldStartSnap;
  
        // set endSnapshotCB
        if (newEndSnap > (statspackAWRInstancePanel.endSnapshotCB.getItemCount() -1)) {
          statspackAWRInstancePanel.endSnapshotCB.setSelectedIndex(statspackAWRInstancePanel.endSnapshotCB.getItemCount() -1);
        }
        else {
          statspackAWRInstancePanel.endSnapshotCB.setSelectedIndex(newEndSnap);
        } 
        
        // set startSnapshotCB
        if (newStartSnap < statspackAWRInstancePanel.startSnapshotCB.getItemCount() -1 -lag) {
          statspackAWRInstancePanel.startSnapshotCB.setSelectedIndex(newStartSnap);
        }
        else {
          newStartSnap = Math.min(statspackAWRInstancePanel.startSnapshotCB.getItemCount() -1 -lag,newStartSnap);
          statspackAWRInstancePanel.startSnapshotCB.setSelectedIndex(newStartSnap);      
        }
      }
    }
    
    statspackAWRInstancePanel.setKeepEventColours(true);
    statspackAWRInstancePanel.refresh();
  }


}
