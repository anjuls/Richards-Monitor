/*
 *  RedoB.java        13.18 04/04/06
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
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 23/03/07 Richard Wright Added getRedoConfig()
 * 04/05/07 Richard Wright Added Gradient Paint to the bar chart
 * 05/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 01/10/07 Richard Wright Removed the target marker to simplify the display
 * 30/10/07 Richard Wright Enhanced for RAC
 * 21/09/07 Richard Wright Removed shadow from chart introduced by JFreeChart 1.0.11
 * 30/04/10 Richard Wright Extend RichButton
 * 25/08/15 Richard Wright Modified to allow filter by user and improved readability
 */


package RichMon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import java.awt.GradientPaint;

import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 * Implements queries into on-line redo configuration and usage.
 */
public class RedoB extends RichButton {
  boolean showSQL;
  boolean tearOff;
  
  /**
   * Constructor
   * 
   * @param options
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public RedoB(String[] options, JScrollPane scrollP,JLabel statusBar, ResultCache resultCache) {
    this.setText("Redo");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
    
    if (ConsoleWindow.getDBVersion() >= 8.1) addItem("Log Switches by Hour");
  }

  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;
    
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
    DatabasePanel.setLastAction(selection);
     
    try { 
      if (selection.equals("Redo Config")) {
        Cursor myCursor = new Cursor("redoConfig.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "default";
        String filterByRACAlias = "l";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      }
      
      if (selection.equals("Redo History")) {
        Cursor myCursor = new Cursor("redoHistory.sql",true);
        Parameters myPars = new Parameters();
        Boolean filterByRAC = true;
        Boolean filterByUser = false;
        Boolean includeDecode = true;
        String includeRACPredicatePoint = "beforeOrderBy";
        String filterByRACAlias = "l";
        String filterByUserAlias = "none";
        Boolean restrictRows = true;
        Boolean flip = true;
        Boolean eggTimer = true;
        executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      }

      if (selection.equals("Log Switches by Hour")) {
        scrollP.getViewport().removeAll();
        JPanel chartP = new JPanel();
        
        
        String[][] instances = ConsoleWindow.getSelectedInstances();
        
        int numSelectedInstances = 0;
        for (int i = 0; i < instances.length; i++) {
          if (instances[i][2].equals("selected")) numSelectedInstances++;
        }
        
        if (numSelectedInstances == 1) chartP.setLayout(new GridLayout(1,1));
        if (numSelectedInstances == 2) chartP.setLayout(new GridLayout(2,1));
        if (numSelectedInstances == 3) chartP.setLayout(new GridLayout(2,2));
        if (numSelectedInstances == 4) chartP.setLayout(new GridLayout(2,2));
        if (numSelectedInstances == 5) chartP.setLayout(new GridLayout(2,3));
        if (numSelectedInstances == 6) chartP.setLayout(new GridLayout(2,3));
        if (numSelectedInstances == 7) chartP.setLayout(new GridLayout(3,3));
        if (numSelectedInstances == 8) chartP.setLayout(new GridLayout(3,3));
        if (numSelectedInstances == 9) chartP.setLayout(new GridLayout(3,3));
        
        for (int i = 0; i < instances.length; i++) {
          if (instances[i][2].equals("selected")) {
            ChartPanel myLogChartPanel = getLogSwitchesByHourChart(instances[i][0],instances[i][1]);
            chartP.add(myLogChartPanel);      
            
            statusBar.setText("");
          }
        }
        
        /*
         * Do not display the charts on the scrollP as it will not look any good,
         * so display on the parent component instead
         * 
         * NOTE:- This comment needs updating so that it makes more sense - 04/05/10
         */
//        JPanel myPanel = (JPanel)scrollP.getParent();
//        myPanel.add(chartP,BorderLayout.CENTER);
        ConsoleWindow.getDatabasePanel().displayChartPanel(chartP);
//        scrollP.getViewport().add(chartP); 

      }
 
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
    
  public void listActionPerformed(boolean showSQL, String lastAction) {
    this.showSQL = showSQL;
    this.tearOff = tearOff;
    listActionPerformed(lastAction, new JFrame());
  };  

  public ChartPanel getLogSwitchesByHourChart(String inst_id,String instanceName) {
    DefaultCategoryDataset logSwitchesDS = new DefaultCategoryDataset();
    NumberAxis myAxis;
    String[][] resultSet;

    try {
      Cursor myCursor = new Cursor("logSwitchesByHour.sql",true);
      Parameters myPars = new Parameters();
      myPars.addParameter("int",inst_id);
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
      Boolean restrictRows = false;
      Boolean flip = false;
      Boolean eggTimer = true;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);            
      resultSet = myResult.getResultSetAsStringArray();
      
      for (int i=0; i < resultSet.length; i++) {
        logSwitchesDS.addValue(Integer.valueOf(resultSet[i][1]).intValue(),"# Log Switches",resultSet[i][0]);
      }    
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(scrollP,e.toString(),"Error...",JOptionPane.ERROR_MESSAGE);
    }

    // create the chart 
    JFreeChart myChart = ChartFactory.createBarChart(
                                "",
                                "Log Switches by Hour - " + instanceName,
                                "# Log Switches",
                                logSwitchesDS,
                                PlotOrientation.VERTICAL,
                                false,
                                true,
                                false
                              ); 
   
    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // make the range axis increment in whole numbers only 
    myAxis = (NumberAxis)myPlot.getRangeAxis();
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    // set the domain axis font size 
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));      
     
    // set up gradient paints for series...
    GradientPaint gp0 = new GradientPaint(
        0.0f, 0.0f, Color.BLUE, 
        0.0f, 0.0f, Color.BLUE
    );

    BarRenderer renderer = (BarRenderer) myPlot.getRenderer();
    renderer.setMaximumBarWidth(0.01);
    renderer.setSeriesPaint(0, gp0);
    renderer.setShadowVisible(false);
    
    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }
}