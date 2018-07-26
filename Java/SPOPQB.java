/*
 * SPOPQB.java        17.48 22/12/10
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
 * 05/03/12 Richard Wright Modified by use awrCache
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.plot.CategoryPlot;


/**
 *
 */
public class SPOPQB extends JButton {
  JLabel statusBar;                      // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;                   // The JScrollPane on which output should be displayed 
  String[] options;                      // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;         // parent panel 

  int[] snapIdRange;                     // list of the snap_id's in the range to be charted 
  String[] snapDateRange;                // list the snapshot date/times to be charted

  /**
   * Constructor 
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPOPQB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Parallel Operations");
    this.options = options;    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  public void actionPerformed() {
    // if a list frame already exists, then remove it
    ConnectWindow.getConsoleWindow().removeLastFrame();
    
    final JList myList = new JList(options);     
    myList.setVisibleRowCount(options.length);
    JScrollPane listScroller = new JScrollPane(myList);
    final JFrame listFrame = new JFrame(this.getText());
    listFrame.add(listScroller);
    listFrame.pack();
    listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    listFrame.setLocationRelativeTo(this);
    
    myList.addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          listActionPerformed(myList.getSelectedValue(), listFrame);
          listFrame.dispose();
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
      });
    
    listFrame.setIconImage(ConnectWindow.getRichMonIcon().getImage());     
    listFrame.setVisible(true);  

    // save a reference to the listFrame so it can be removed if left behind
    ConnectWindow.getConsoleWindow().saveFrameRef(listFrame);
  }

  /**
   * Performs the user selected action 
   * 
   * @param selectedOption 
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    statspackAWRInstancePanel.setLastAction(selection);
    try {  
//      statspackPanel.clearScrollP();
      snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
      snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
      statspackAWRInstancePanel.sanityCheckRange();
      
      if (selection.equals("OPQ Operations")) { 
        ChartPanel myChartPanel = createOPQOperationsChart();

//        scrollP.getViewport().removeAll();
//        scrollP.getViewport().add(myChartPanel, null);
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

      if (selection.equals("OPQ Downgrades")) { 
        ChartPanel myChartPanel = createOPQDowngradesChart();

//        scrollP.getViewport().removeAll();
//        scrollP.getViewport().add(myChartPanel, null);
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

      if (selection.equals("OPQ Operations and Downgrades")) { 
        ChartPanel myChartPanel = createOPQOperationsAndDowngradesChart();

//        scrollP.getViewport().removeAll();
//        scrollP.getViewport().add(myChartPanel, null);
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public ChartPanel createOPQOperationsChart() throws Exception {
    String[] statistics = {"Parallel operations not downgraded"};

    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;
  }
  
  public ChartPanel createOPQDowngradesChart() throws Exception {
    String[] statistics = {"Parallel operations downgraded to serial",
                           "Parallel operations downgraded 1 to 25 pct",
                           "Parallel operations downgraded 25 to 50 pct",
                           "Parallel operations downgraded 50 to 75 pct",
                           "Parallel operations downgraded 75 to 99 pct"};

    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;    
  }
  
  public ChartPanel createOPQOperationsAndDowngradesChart() throws Exception {
    String[] statistics = {"Parallel operations not downgraded",
                           "Parallel operations downgraded to serial",
                           "Parallel operations downgraded 1 to 25 pct",
                           "Parallel operations downgraded 25 to 50 pct",
                           "Parallel operations downgraded 50 to 75 pct",
                           "Parallel operations downgraded 75 to 99 pct"};
    
    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;
  }

  public ChartPanel createChart(String[] statistics, boolean singleAxis) throws Exception {
    ChartPanel myChartPanel = statspackAWRInstancePanel.spStatisticsB.createChart(statistics, singleAxis);    

    CategoryPlot myPlot = (CategoryPlot)myChartPanel.getChart().getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,4));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    myPlot.setBackgroundPaint(Color.WHITE);
    

    // Set the range of both axis to be the same 
    if (!singleAxis) {
      NumberAxis axis1 = (NumberAxis)myPlot.getRangeAxis(0);
      NumberAxis axis2 = (NumberAxis)myPlot.getRangeAxis(1);
      double high1 = axis1.getUpperBound();
      double high2 = axis2.getUpperBound();
      double newHigh = Math.max(high1,high2);
      axis1.setRange(0,newHigh);
      axis2.setRange(0,newHigh);
      axis1.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
      axis1.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
      axis2.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
      axis2.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));  
    }
    else {
      NumberAxis axis1 = (NumberAxis)myPlot.getRangeAxis(0);
      axis1.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
      axis1.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    }
    
    return myChartPanel;
  }

}
