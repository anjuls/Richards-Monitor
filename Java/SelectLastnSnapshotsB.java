/*
 * SelectLastnSnapshotsB.java        13.24 13/07/06
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
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 08/01/07 Richard Wright Modified so that it returns 24 snapshots not 24 hours worth etc
 * 19/03/07 Richard Wright Renamed this class from SelectSnapshotsLastnHoursB to SelectLastnSnapshotsB
 * 20/10/09 Richard Wright Undone the change from 08/01/07 so that I could modify the code to support a new button for a single snapshot
 */
 
 package RichMon;

import javax.swing.JButton;
import javax.swing.JOptionPane;


public class SelectLastnSnapshotsB extends JButton {
  StatspackAWRInstancePanel statspackP;

  int numSnaps = 0;

  public SelectLastnSnapshotsB(String buttonName, StatspackAWRInstancePanel statspackP, int numSnaps) {
    super(buttonName);

    this.statspackP = statspackP;
    this.numSnaps = numSnaps;
  }

  public void actionPerformed() {
    int numSnapshots = statspackP.endSnapshotCB.getItemCount();
    if (numSnapshots == 0) { 
      statspackP.getSnapshotsB.actionPerformed(false);
//      statspackP.getDBDetails();
      numSnapshots = statspackP.endSnapshotCB.getItemCount();
    }
    else {
      if (numSnapshots > numSnaps ) { 
        int t = statspackP.endSnapshotCB.getSelectedIndex() - numSnaps;
        statspackP.startSnapshotCB.setSelectedIndex(Math.max(t,0));
      }
      else {
        statspackP.startSnapshotCB.setSelectedIndex(0);
      }
    }
  }
}
