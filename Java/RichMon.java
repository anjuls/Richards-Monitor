/*
 *  RichMonitor.java        12.15 05/01/04
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
 * Change History since 19/03/05
 * =============================
 * 
 * 29/03/05 Richard Wright Swithed comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 23/03/06 Richard Wright Command line arguments are passed onto the connect window.
 *                         This allows a base directory to be specified on the command
 *                         line.  Useful for running RichMon on Linux/unix servers.
 * 08/08/07 Richard Wright The look and feel is set to metal is the second parameter is 'metal'
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 14/01/08 Richard Wright Added debug boolean
 */


package RichMon;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.UIManager;


/**
 * RichMon - a lightweight database monitoring tool.
 */
public class RichMon
{

  int horizontalPos;
  int verticalPos;
  
  boolean debug = false;
  
  static ConnectWindow connectWindow;
  
  /**
   * Start RichMon
   */
  public RichMon(String[] args)
  {    
    connectWindow = new ConnectWindow(args);    

    URL url = this.getClass().getClassLoader().getResource("RichMon.gif");
    ImageIcon RichMonIcon = new ImageIcon(url);
    connectWindow.setRichMonIcon(RichMonIcon);

    getWindowPosition();
    connectWindow.setLocation(horizontalPos,verticalPos);
    connectWindow.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          System.exit(0);
        }
      });
      
    connectWindow.setVisible(true);
  }

  /**
   * Centre the connectWindow in screen
   */
  void getWindowPosition()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = connectWindow.getSize();
    verticalPos = (screenSize.height - frameSize.height) / 2;
    horizontalPos = (screenSize.width - frameSize.width) / 2;
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
      verticalPos = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
      horizontalPos = screenSize.width;
    }
    
    if (debug) System.out.println("screenSize.height : " + screenSize.height);
    if (debug) System.out.println("screenSize.width  : " + screenSize.width);
  }

  /**
   * main method
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    // set the look and feel 
    try
    {
      if (args.length == 0) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
      }
      else {
        if (args.length >= 2) {
          String feel = args[1].toLowerCase();
          if (feel.equals("metal")) {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
          }
          else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    
    // start RichMon 
    new RichMon(args);
  }
}