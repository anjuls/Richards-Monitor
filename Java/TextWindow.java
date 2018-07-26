/*
 *  TextWindow.java        12.15 05/01/04
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
 * 14/09/06 Richard Wright Modified the comment style and error handling 
 * 31/01/08 Richard Wright Added the RichMon Icon to the frame title bar
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * A Frame to display html output.
 */
public class TextWindow extends JFrame  {
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JEditorPane editorPane = new JEditorPane();

  public TextWindow(String text,String title) {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    editorPane.setText(text);
    this.setTitle(title);
  }

  /**
   * Defines all the components that make up the panel
   * 
   * @throws Exception
   */ 
  private void jbInit() throws Exception {
    editorPane.setEditable(false);
    editorPane.setContentType("text/html");
    jScrollPane1.getViewport().add(editorPane, null);
    this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    this.setSize(new Dimension(800, 600));
    
    // add the richmon icon to the frame title bar
    this.setIconImage( new ImageIcon( ClassLoader.getSystemResource("RichMon.gif")).getImage());
  }
}