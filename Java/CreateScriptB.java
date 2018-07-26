/*
 *  CreateScriptB.java        12.17 18/02/05
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
 * 09/06/05 Richard Wright Added LogWriter output
 * 18/08/06 Richard Wright Modified the comment style and error handling
 * 04/05/10 Richard Wright Extend RichButton
 */


package RichMon;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Implements a call to dbms_meta to produce a create script for any object 
 * directly from the dictionary.  Does not always work on partitioned tables 
 * if the output is very long.
 */
public class CreateScriptB extends RichButton {
  // misc
  Dimension prefSize = new Dimension(120,25);


  /**
   * Constructor
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public CreateScriptB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText(buttonName);
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action from the sharedServerB JComboBox
   * 
   * @param showSQL - controls whether the sql should be displayed(true) or executed(false)
   */
  public void actionPerformed(boolean showSQL) {
    try {
      JPanel schemaP = new JPanel();
      JLabel schemaNameL = new JLabel("Schema Name",JLabel.RIGHT);
      schemaNameL.setPreferredSize(prefSize);
      JTextField schemaNameTF = new JTextField();
      schemaNameTF.setPreferredSize(prefSize);
      schemaP.add(schemaNameL);
      schemaP.add(schemaNameTF);
      
      JPanel objectTypeP = new JPanel();
      JLabel objectTypeL = new JLabel("Object Type",JLabel.RIGHT);
      objectTypeL.setPreferredSize(prefSize);
      JTextField objectTypeTF = new JTextField();
      objectTypeTF.setPreferredSize(prefSize);
      objectTypeP.add(objectTypeL);
      objectTypeP.add(objectTypeTF);
  
      JPanel objectNameP = new JPanel();
      JLabel objectNameL = new JLabel("Object Name",JLabel.RIGHT);
      objectNameL.setPreferredSize(prefSize);
      JTextField objectNameTF = new JTextField();
      objectNameTF.setPreferredSize(prefSize);
      objectNameP.add(objectNameL);
      objectNameP.add(objectNameTF);
      
      Object[] options = {schemaP, objectTypeP, objectNameP};
         
      int result = JOptionPane.showOptionDialog(ConnectWindow.getConsoleWindow(),options,"Generate Create Script...",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
 
      if (result == JOptionPane.OK_OPTION) {
        Cursor myCursor = new Cursor("desc.sql",true);
        Parameters myPars = new Parameters();
        myPars.addParameter("String",objectTypeTF.getText().toUpperCase());
        myPars.addParameter("String",objectNameTF.getText().toUpperCase());
        myPars.addParameter("String",schemaNameTF.getText().toUpperCase());
        Boolean filterByRAC = false;
        Boolean filterByUser = false;
        Boolean includeDecode = false;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "none";
        String filterByUserAlias = "none";
        Boolean restrictRows = false;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}