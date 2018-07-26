/*
 *  SesionPanel.java        17.52 09/08/11
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;


public class SessionPanel extends JPanel {
  // panels 
  static JPanel buttonsP = new JPanel();  
  public static JTabbedPane sessionTP = new JTabbedPane();
  
  static JLabel instanceIdL = new JLabel("Enter an Instance Number:");
  JLabel sessionIdL = new JLabel("Enter a SID:");
  JLabel processIdL = new JLabel("alternatively Enter an OS PID:");
  static JTextField instanceIdTF = new JTextField();
  JTextField sessionIdTF = new JTextField();
  JTextField processIdTF = new JTextField();
  JButton getSessionB = new JButton("Get Session");
       
    
  /**
   * Constructor.
   */
  public SessionPanel() {   
    
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
        
    getSessionB.setPreferredSize(standardButtonSize);
    getSessionB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getSessionB_actionPerformed();
        }
      });
    
    //addInstanceFields();
    
    buttonsP.add(sessionIdL);
    
    sessionIdTF.setPreferredSize(new Dimension(80,20));
    sessionIdTF.setForeground(Color.blue);
    buttonsP.add(sessionIdTF);
    
    buttonsP.add(processIdL);
    
    processIdTF.setPreferredSize(new Dimension(80,20));
    processIdTF.setForeground(Color.blue);
    buttonsP.add(processIdTF);
    
    buttonsP.add(getSessionB);
    buttonsP.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    // .getRootPane().setDefaultButton(getSessionB);
        
    this.add(buttonsP, BorderLayout.NORTH);
    this.add(sessionTP, BorderLayout.CENTER);

  }

  public static void addInstanceFields() {
    buttonsP.add(instanceIdL,0);

    instanceIdTF.setPreferredSize(new Dimension(30, 20));
    instanceIdTF.setForeground(Color.blue);
    buttonsP.add(instanceIdTF,1);
    
  }
  
  private void getSessionB_actionPerformed()  {
    if (sessionIdTF.getText().length() > 0) {
      getSessionFromSid();
    }
    else {
      if (processIdTF.getText().length() > 0) getSessionFromPid();
    }
  }

  private void getSessionFromSid() {
    String t = sessionIdTF.getText();
    try {
      if (t instanceof String) {
        int sid;
        int inst_id;
        /* It is possible to enter a sid with the instance number first seperated by a | as in '1|234'
       * which would suggest instance 1 sid 234
       */
        if (t.indexOf("|") >= 1) {
          // an instance number was specified
          inst_id = Integer.valueOf(t.substring(0, t.indexOf("|"))).intValue();
          sid = Integer.valueOf(t.substring(t.indexOf("|") + 1)).intValue();
        } else {
          // no instance number was specified, so use this connections instance number
          if (!ConsoleWindow.isDbRac()) {
            inst_id = ConsoleWindow.getThisInstanceNumber();
            sid = Integer.valueOf(t).intValue();
          } else {
            String i = instanceIdTF.getText();
            if (i.length() > 0) {
              inst_id = Integer.valueOf(i).intValue();
            }
            else {
              inst_id = ConsoleWindow.getThisInstanceNumber();
            }
            sid = Integer.valueOf(t).intValue();
          }
        }


        // check the sid exists before adding a sessionPanel
        int serial = getSid(inst_id, sid);
        if (serial != 0) {
          ConsoleWindow.addSessionPanel(inst_id, sid);
        } else {
          throw new NoSuchSessionExistsException("Sid: " + sid);
        }
      }
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e,"SessionPanel.getSid()");
    }
  }
  
  public static JTabbedPane getSQL_IdPane() {
    return sessionTP;
  }
  
  private void getSessionFromPid() {
    String t = processIdTF.getText();
    try {
      if (t instanceof String) {
        int pid;
        int inst_id;
        /* It is possible to enter a sid with the instance number first seperated by a | as in '1|234'
         * which would suggest instance 1 sid 234
         */
        if (t.indexOf("|") >= 1) {
          // an instance number was specified
          inst_id = Integer.valueOf(t.substring(0,t.indexOf("|"))).intValue();
          pid = Integer.valueOf(t.substring(t.indexOf("|")+1)).intValue();
        }
        else {
          // no instance number was specified, so use this connections instance number
          if (!ConsoleWindow.isDbRac()) {
            inst_id = ConsoleWindow.getThisInstanceNumber();
            pid = Integer.valueOf(t).intValue();
          }
          else {
            String i = instanceIdTF.getText();
            if (i.length() > 0) {
              inst_id = Integer.valueOf(i).intValue();
            }
            else {
              inst_id = ConsoleWindow.getThisInstanceNumber();
            }
            pid = Integer.valueOf(t).intValue();
          }
        }

        // check the sid exists before adding a sessionPanel
        pid = getPid(inst_id,pid);

        if (pid != 0) {
          ConsoleWindow.addSessionPanel(inst_id,pid);
        }
        else {
          throw new NoSuchSessionExistsException("Pid: " + pid);
        }
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  /**
   * Check that the pid <process id> exists in v$session
   *
   * @param pid
   * @return sid <session id> of the session, or return 0 if no such session exists
   */
  private int getPid(int inst_id,int pid) {
    int returnSid = 0;
    try {
      // create a cursor and execute the sql
      String cursorId = "getPid.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",pid);
      myPars.addParameter("int",inst_id);

      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      // add the sessionPanel if the sid exists
      if (myResult.getNumRows() == 1) {
        Vector resultSetRow = myResult.getResultSetRow(0);
        returnSid = Integer.valueOf(resultSetRow.elementAt(0).toString()).intValue();
      }
      else {
        returnSid = 0;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    return returnSid;
  }

  
  /**
   * Check to see that the given sid <session id> exists in v$session
   *
   * @param sid
   * @return int - the sessions serial# from v$session, or return 0 if the session doesn't exist
   */
  public static int getSid(int inst_id,int sid) {
    int serial = 0;
    try {
      // create a cursor and execute the sql
      String cursorId = "getSid.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",sid);
      myPars.addParameter("int",inst_id);

      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);

      // add the sessionPanel if the sid exists
      if (myResult.getNumRows() == 1) {
        Vector resultSetRow = myResult.getResultSetRow(0);
        serial = Integer.valueOf(resultSetRow.elementAt(1).toString()).intValue();
      }
      else {
        serial = 0;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,"SessionPanel.getSid()");
    }

    return serial;
  }
}