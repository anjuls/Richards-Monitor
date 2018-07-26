/*
 * SPLoadB.java        13.14 07/02/06
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
 * 06/03/06 Richard Wright Clear the statspack panel before displaying new objects
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 10/08/06 Richard Wright Modifyed calls to SPStatistics to specify whether a single axis is rquired
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 10/12/08 Richard Wright Called displayChart instead of doing it manually each time
 * 05/03/12 Richard Wright Modified to use awrCache
 * 30/01/14 Richard Wright Added 'transaction rollbacks' to the rollbacks chart
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;


/**
 *
 */
public class SPOtherChartsB extends JButton {
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
  public SPOtherChartsB(StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Other Charts");
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
      snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
      snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
      statspackAWRInstancePanel.sanityCheckRange();
      

      if (selection.equals("Calls")) { 
        ChartPanel myChartPanel = createCallsChart();

        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

      if (selection.equals("Parse")) { 
        ChartPanel myChartPanel = createParseChart();

        statspackAWRInstancePanel.displayChart(myChartPanel);
      }

      if (selection.equals("Physical I/O")) { 
        ChartPanel myChartPanel = createPhysicalIOChart();
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
      if (selection.equals("Commits / Rollbacks")) { 
        ChartPanel myChartPanel = createCommitsRollbacksChart();
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }      
      
      if (selection.equals("IOPS")) { 
        ChartPanel myChartPanel = createIOPSChart();
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public ChartPanel createAllCharts() throws Exception {
    String[] statistics = {"recursive calls","parse count (total)","user calls","parse count (hard)","physical reads","redo blocks written","physical writes"};

    ChartPanel myChartPanel = createChart(statistics, false);
    return myChartPanel;
  }
  
  public ChartPanel createCallsChart() throws Exception {
    String[] statistics = {"recursive calls","user calls"};

    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;    
  }

  public ChartPanel createCommitsRollbacksChart() throws Exception {
    String[] statistics = {"user commits","user rollbacks","transaction rollbacks"};

    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;    
  }
  
  public ChartPanel createParseChart() throws Exception {
    String[] statistics = {"parse count (total)","parse count (hard)"};
    
    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;
  }
  
  public ChartPanel createPhysicalIOChart() throws Exception {
    String[] statistics = {"physical reads","physical writes"};
    
    ChartPanel myChartPanel = createChart(statistics, true);
    return myChartPanel;
  }  
  
  public ChartPanel createIOPSChart() throws Exception {
    String[] statistics = {"physical read total IO requests","physical write total IO requests"};
    
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
  
  public void addItem(String newOption) {
    // check to see whether this option already exists
    boolean dup = false;
    for (int i=0; i < options.length; i++) {
      if (options[i].equals(newOption)) {
        dup = true;
        break;
      }
    }
    
    // if its not a duplicate, add it to the options array
    if (!dup) {
      String[] tmp = new String[options.length];
      System.arraycopy(options,0,tmp,0,options.length);
      options = new String[options.length +1];
      System.arraycopy(tmp,0,options,0,tmp.length);
      options[options.length-1] = newOption;
    }
  }
}
