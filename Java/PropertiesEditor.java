/*
 *  PropertiesEditor.java        17.45 26/11/10
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

/**
 * A Frame to display query results.
 */
public class PropertiesEditor extends JFrame 
{

  JPanel saveP = new JPanel();
  JButton saveB = new JButton("Save");
  JPanel editP = new JPanel();
  JScrollPane editSP = new JScrollPane();
  JTextPane editTP = new JTextPane();
  JLabel restartMessageL = new JLabel("RichMon must be restarted for changes made here to take effect");
  
 
  /**
   * Constructor.
   * 
   */
  public PropertiesEditor()
  {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */ 
  private void jbInit() throws Exception
  {
    int width = Properties.getAdditionalWindowWidth();
    int height = Properties.getAdditionalWindowHeight();
    editTP.setPreferredSize(new Dimension(width, height -100));
   
    this.setSize(new Dimension(width, height));
    
    // add the richmon icon to the frame title bar
    this.setIconImage(ConnectWindow.getRichMonIcon().getImage());
    
    this.setLayout(new BorderLayout());
    this.setTitle("Properties Editor");
    this.setLocationRelativeTo(null);
    saveP.add(saveB);
    saveP.add(restartMessageL);
    editP.add(editSP);
    editSP.getViewport().add(editTP);
    editSP.getViewport().setBackground(Color.WHITE);
    this.getContentPane().add(saveP,BorderLayout.NORTH);
    this.getContentPane().add(editP,BorderLayout.CENTER);

    saveB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           savePropertiesFile();
         }
       });
    
    readPropertiesFile();
    
    this.setVisible(true);
  }
  
  private void readPropertiesFile() {
    try {
      File configFile = ConnectWindow.getConfigFile();
      BufferedReader propertiesFileReader = new BufferedReader(new FileReader(configFile));
      String inputLine = propertiesFileReader.readLine();
      StringBuffer entireMessage = new StringBuffer(10000);

      while (inputLine instanceof String) {
        entireMessage.append(inputLine);
        entireMessage.append("\n");
        inputLine = propertiesFileReader.readLine();
      }
      
      editTP.setText(entireMessage.toString());
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e, this, "Error reading properties file into the properties editor");
    }
  }
  
  private void savePropertiesFile() {
    try {
      File configFile = ConnectWindow.getConfigFile();

      if (configFile.exists())
        configFile.delete();

      boolean ok = configFile.createNewFile();

      // Write a default config set to the new config file
      BufferedWriter configFileWriter = new BufferedWriter(new FileWriter(configFile));
      configFileWriter.write(editTP.getText());
      
      configFileWriter.close();
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e, this, "Error saving the properties file");
    }

    this.setVisible(false);
  }
}