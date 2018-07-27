/*
 * ConsoleWindow_AboutBoxPanel1.java        13.00 05/01/04
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
 * Change History since 21/02/05
 * =============================
 * 
 * 21/02/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 06/06/05 Richard Wright Changed version to 12.16
 * 09/06/05 Richard Wright Changed version to 12.17
 * 10/06/05 Richard Wright Changed version to 13.00
 * 17/06/05 Richard Wright Changed version to 13.02
 * 18/06/05 Richard Wright Changed version to 13.03
 * 19/06/05 Richard Wright Changed version to 13.04
 * 04/10/05 Richard Wright Changed version to 13.05
 * 10/10/05 Richard Wright Changed version to 13.06
 * 26/10/05 Richard Wright Changed version to 13.07
 * 01/11/05 Richard Wright Changed version to 13.08
 * 02/11/05 Richard Wright Changed version to 13.09
 * 07/11/05 Richard Wright Changed version to 13.09
 * 08/12/05 Richard Wright Changed version to 13.12
 * 04/01/06 Richard Wright Changed version to 13.13
 * 20/01/06 Richard Wright Changed version to 13.14
 * 10/03/06 Richard Wright Changed version to 13.15
 * 12/03/06 Richard Wright Changed version to 13.16
 * 23/03/06 Richard Wright The RichMon version is no longer hard coded in this 
 *                         class but read in from ConsoleWindow.
 * 13/12/07 Richard Wright Enhanced the colour,title, website address etc
 */


package RichMon;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Shows the authors name, emailid and the version of RichMon etc
 */
public class ConsoleWindow_AboutBoxPanel1 extends JPanel  {
  private Border border = BorderFactory.createEtchedBorder();
  private GridBagLayout layoutMain = new GridBagLayout();
  private JLabel labelCompany = new JLabel();
  private JLabel labelCopyright = new JLabel();
  private JLabel labelAuthor = new JLabel();
  private JLabel labelAuthor2 = new JLabel();
  private JLabel labelTitle = new JLabel();
  private JLabel labelBlog = new JLabel();
  private JLabel labelJFreeChart = new JLabel();
  private JLabel labelBlank = new JLabel();
  private JLabel labelBlank2 = new JLabel();
  private JLabel labelBlank3 = new JLabel();
  
  /**
   * Constructor
   */
  public ConsoleWindow_AboutBoxPanel1() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Defines all the components that make up the window
   * 
   * @throws Exception
   */  private void jbInit() throws Exception {
    this.setLayout(layoutMain);
    this.setBorder(border);
    labelBlank.setText(" ");
    labelBlank2.setText(" ");
    labelBlank3.setText(" ");
    labelTitle.setText("RichMon: Richard's Monitoring Tool for Oracle                                                                 Version: " + ConsoleWindow.getRichMonVersion());
    labelAuthor.setText("Website: www.RichMon4Oracle.com");
    labelAuthor2.setText("Email: support@richmon4oracle.com");
    labelCopyright.setText("Copyright 2003 - 2018 Richard Wright                                                    ");
    labelCompany.setText("RichMon is still in development, please report bugs, comments and enhancement requests by email.");
    labelBlog.setText("Checkout \"http://www.richmon4oracle.com\" and \"http://richmon.blogspot.com\" for the latest news and updates");
    labelJFreeChart.setText("All charts are produced using JFreeChart, available from \"http://www.jfree.org/jfreechart\" under the GNU Lesser General Public Licence.");
    this.add(labelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 15), 0, 0));
    this.add(labelBlank, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));    
    this.add(labelAuthor, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelAuthor2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelBlank2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));    
    this.add(labelCopyright, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelBlank3, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));    
    this.add(labelCompany, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
    this.add(labelBlog, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
    this.add(labelJFreeChart, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
  }
}