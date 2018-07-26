/*
 * GetSnapshotsB.java        13.0 13/06/05
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
 * 08/12/05 Richard Wright Automatically select a range of 100 snapshots
 * 13/07/06 Richard Wright Call SelectSnapshotsLast24HoursB instead of calculating the last 100 here
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 22/08/06 Richard Wright Modified the comment style and error handling
 * 22/08/06 Richard Wright Simplified the method calls and set eggTimer automatically
 * 05/12/07 Richard Wright Enhanced for RAC
 * 17/12/07 Richard Wright No longer select host name when listing awr/statpack datasets
 * 19/12/07 Richard Wright Fixed bug in choosing a dataset
 * 15/07/09 Richard Wright Display the dialog for choosing a dataset over the top left of the screen for ease of use
 * 15/07/09 Richard Wright Display the dialog for choosing a dataset in order of instance number
 * 28/10/09 Richard Wright Removed unused references to the statusbar
 * 17/11/10 Richard Wright Added support for a pre_set_awr_instance
 */


package RichMon;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * Implements a query to show a summary of each database session.
 */
public class GetSnapshotsB extends JButton {
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // The Panel which will be updated
  JPanel parent;               // The dataset selection dialog box will appear over this panel

  boolean eggTimer;

  boolean debug = false;

  /**
   * Constructor
   *
   * @param buttonName
   * @param statspackAWRP
   * @param statusBar
   */
  public GetSnapshotsB(String buttonName, StatspackAWRInstancePanel statspackAWRP, JPanel parent) {
    super(buttonName);

    this.statspackAWRInstancePanel = statspackAWRP;
    this.parent = parent;
  }


  /**
   * Get a list of snapshots from the database and display these on the statspack panel
   *
   * @param startupPhase
   */
  public void actionPerformed(boolean startupPhase) {
    try {
      statspackAWRInstancePanel.startSnapshotCB.removeAllItems();
      statspackAWRInstancePanel.endSnapshotCB.removeAllItems();

 /*
      // check how many instances are storing there data in this AWR/Perfstat repository

      String cursorId;
      if (startupPhase) {
        eggTimer = false;
      }
      else {
        eggTimer = true;
      }

      QueryResult myResult = new QueryResult();
      String[][] resultSet = new String[0][0];
      
      if (Properties.getEnableAWRInstanceSelection()) {
        if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
          cursorId = "getInstanceNamesAWR.sql";
        } else {
          cursorId = "getInstanceNames.sql";
        }

        
        

        myResult = ExecuteDisplay.execute(cursorId, false, eggTimer, null);
        resultSet = myResult.getResultSetAsStringArray();
         

        if (myResult.getNumRows() == 0)
          throw new NoSnapshotsException("No Snapshots Exist");
        
          if (myResult.getNumRows() == 1) {
            long dbId = Long.valueOf(resultSet[0][0]).longValue();
            int instanceNumber = Integer.valueOf(resultSet[0][1]).intValue();
            statspackAWRInstancePanel.setDbId(dbId);
            statspackAWRInstancePanel.setInstanceNumber(instanceNumber);
            statspackAWRInstancePanel.clearSelectedInstanceName();
          }

       else {

            // more than one dataset exists in this AWR or perstat repository

            Object[] possibilities = new Object[myResult.getNumRows()];

            for (int i = 0; i < resultSet.length; i++) {
              possibilities[i] =
                  resultSet[i][0] + "          " + resultSet[i][1] + "               " + resultSet[i][2] +
                  "                 " + resultSet[i][3];
            }

            String chosenOption =
              (String)JOptionPane.showInputDialog(parent, "Select the data set your interested in:\n\n" +
                " DB ID                    Instance    Instance Name      Last Snapshot", "Choose a dataset",
                JOptionPane.PLAIN_MESSAGE, null, possibilities, null);

            if (debug)
              System.out.println(chosenOption.substring(0, chosenOption.indexOf(" ")) + ":" + chosenOption);

            long inst_id = Long.valueOf(chosenOption.substring(0, chosenOption.indexOf(" "))).longValue();
            // stringbuffer contains the chose option without the database id at the front
            StringBuffer tb = new StringBuffer();
            tb = new StringBuffer(chosenOption.substring(chosenOption.indexOf(" ") + 2));
            // cycle thru the string buffer till we find the first non space char, which will be the instance_id (might be more than 1 char long)
            String inst_num = new String();
            for (int j = 0; j < tb.length(); j++) {
              String first = tb.substring(j, j + 1);
              if (!(first.equals(" "))) {
                inst_num = first;
                String second = tb.substring(j + 1, j + 2);
                if (!second.equals(" "))
                  inst_num = inst_num + second;
                break;
              }
            }

            statspackAWRInstancePanel.setDbId(inst_id);
            statspackAWRInstancePanel.setInstanceNumber(Integer.valueOf(inst_num).intValue());
            // figure out the instance name
            for (int i = 0; i < resultSet.length; i++) {
              if (debug)
                System.out.println(resultSet[i][1]);
              if (debug)
                System.out.println(inst_num);
              if (resultSet[i][1].equals(inst_num)) {
                statspackAWRInstancePanel.setSelectedInstanceName(resultSet[i][2]);
                break;
              }
            }
          }  
        
      } */

  
      // get the snapshots
      Parameters myPars = new Parameters();
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());

      String cursorId;

      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        cursorId = "snapshotsAWR.sql";
      }
      else {
        cursorId = "snapshots.sql";
      }

      if (startupPhase) {
        eggTimer = false;
      }
      else {
        eggTimer = true;
      }

      if (debug) JOptionPane.showMessageDialog(this, "about to get snapshot info for the selected instance from the database");
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, eggTimer, null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      String date;
      String snapId;

      if (myResult.getNumRows() > 0) {
        for (int i = 0; i < myResult.getNumRows(); i++) {
          date = resultSet[i][0];
          snapId = resultSet[i][1];

          while (date.length() < 25) {
            date = date + " ";
          }

          statspackAWRInstancePanel.startSnapshotCB.addItem(date + snapId);
          statspackAWRInstancePanel.endSnapshotCB.addItem(date + snapId);
        }

        statspackAWRInstancePanel.endSnapshotCB.setSelectedIndex(myResult.getNumRows() - 1);

        // set the start snapshot n snapshots prior to the end snapshot
        statspackAWRInstancePanel.selectSnapshotsLast24.actionPerformed();
      }
      else {
        if (!startupPhase)
          throw new NoSnapshotsException("No Snapshots Exist");
      }
    }
    catch (Exception NoSnapshotsException) {
      ConsoleWindow.displayError(NoSnapshotsException, this);
    }
    //    catch (Exception e) {

    //      ConsoleWindow.displayError(e,this,"Check the perfstat schema exists!");

    //    }

  }
}