/*
 * OPQ_Chart.java        17.48 10/12/10
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
 * 29/09/15 Richard Wright re-instating the chart break out function
 */
 
package RichMon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;

import java.awt.Rectangle;
import java.awt.Toolkit;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;

import java.util.Vector;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.border.Border;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Create and display a chart 
 */
public class OPQChart {

  private DefaultCategoryDataset opqDS;
  private NumberAxis opqAxis;
  private int[] opqMaxEvents = new int[150];
  private int opqMaxEventsCounter = 0;
  private int chartIteration = 0;
  
 
  private int offSet = 64;
  private int defaultOffSet = 64;
  private String currentExecutionTime;
  private JFreeChart myChart;
  
  boolean debug = false;

  /**
   * Constructor
   */
  public OPQChart() {
  }

  public void createChart(QueryResult myResult, 
                          boolean initialize,
                          boolean maxWidth,
                          String instanceId,
                          String instanceName) throws Exception {
    
    myChart = makeChart(myResult, initialize);

    // create the chart frame and display it 
   if (Properties.isBreakOutChartsTabsFrame()) {
      ChartPanel myChartPanel = new ChartPanel(myChart);
      ExecuteDisplay.addWindowHolder(myChartPanel, "OPQ - " + instanceName);
    } else {
      ChartFrame opqCF = new ChartFrame(ConsoleWindow.getInstanceName() + 
          ": OPQ Chart", myChart, true);
      if (maxWidth) {
        // check the screen width 
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        
        // set the frame width 
        Dimension currentDimension = opqCF.getSize();
        int newWidth = Math.min(screenWidth,currentDimension.width*2);
        currentDimension.setSize(newWidth,currentDimension.height);
        opqCF.setSize(currentDimension);
        
      }  
      else {
        opqCF.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
      }
      opqCF.setVisible(true);
    }
    


    /*
     * remove old values 
     * this is required when the frame is restarted
     */
    removeOldOPQEntries();
  }
                          
  /**
   * Create the chart and display it
   * 
   */
   public ChartPanel createChartPanel(QueryResult myResult, boolean initialize, String instanceId) throws Exception {

     myChart = makeChart(myResult, initialize);

     // create the chart frame and display it 
     ChartPanel myChartPanel = new ChartPanel(myChart);
     
     /*
      * remove old values 
      * this is required when the frame is restarted
      */
     removeOldOPQEntries();
     
     return myChartPanel;
   }    

  private JFreeChart makeChart(QueryResult myResult, boolean initialize) {
    opqDS = new DefaultCategoryDataset();

    // Get the current time 
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      opqDS.addValue(0, "PQ QC Process", currentTime);
      String[][] allInstances = ConsoleWindow.getAllInstances();
      for (int j=0; j < allInstances.length; j++) {
        opqDS.addValue(0, "PQ Slave Process from inst " + allInstances[j][0], currentTime);
      }
    }
    
    
    // create the chart 
    JFreeChart myOPQChart = ChartFactory.createStackedBarChart3D(
                                "",
                                "",
                                "# Processes",
                                opqDS,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false
                              );
    
    CategoryPlot myPlot = myOPQChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    myOPQChart.getLegend().setBorder(0,0,0,0);   // remove border around the legend
    myOPQChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));
    
    // setup the domain axis (bottom axis) 
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, fontSize));

    // configure the events axis 
    opqAxis = (NumberAxis)myPlot.getRangeAxis();
    opqAxis.setLowerBound(0);
    opqAxis.setAutoRangeMinimumSize(20);
    opqAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    opqAxis.setStandardTickUnits(integerTicks);
    opqAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    opqAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    currentExecutionTime = myResult.getExecutionClockTime();
//    if (myResult.getNumRows() > 0) {
      updateChart(myResult);
//    }
//    else {
//      chartIteration++;
//    }
    return myOPQChart;
  }


  /**
   * Add a new result set to the chart
   * 
   * @param myResult - QueryResult
   */
  public void updateChart(QueryResult myResult) {
    try {
      removeOldOPQEntries();
      int sumNumEvents = 0;
      
      currentExecutionTime = myResult.getExecutionClockTime();
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      for (int i=0; i < myResult.getNumRows(); i++) {
        int numEvents = Integer.valueOf(resultSet[i][1]).intValue();
        opqDS.addValue(numEvents , resultSet[i][0], currentExecutionTime);
        sumNumEvents = sumNumEvents + numEvents;
        //if (debug) System.out.println("WE: added entry : " + resultSet[i][0] + currentExecutionTime);
      }
      
      if (myResult.getNumRows() == 0) addBlankEntry(currentExecutionTime);
      
      if (debug) System.out.println("WE: number of entry in dataset : " + opqDS.getColumnCount());
      
      // calculate the maximum number of events for the wait event range axis 
      int maxNumEvents = 0;
      for (int j=0; j < offSet; j++) {
        maxNumEvents = Math.max(maxNumEvents,opqMaxEvents[j]);
      }
      
      maxNumEvents = Math.max(maxNumEvents,sumNumEvents);
      
      // set the wait event range axis upper boundary 
      if (opqAxis.getUpperBound() != Math.max(10.0,maxNumEvents +1)) opqAxis.setRange(0,Math.max(10.0,maxNumEvents +1));
      
      chartIteration++;
      
      // store away the numEvents to enable range axis calculations 
      opqMaxEvents[opqMaxEventsCounter++] = sumNumEvents;
      if (opqMaxEventsCounter >= offSet -1) opqMaxEventsCounter = 0;
      
//      removeOldWaitEventEntries();
    } catch (Exception e) {
        ConsoleWindow.displayError(e, this);
    }
  }


  /**
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
   public void removeOldOPQEntries() {
     try {
       
       List colList = opqDS.getColumnKeys();
       Object[] colObj = colList.toArray();

       for(int i=0; i < colObj.length - offSet; i++) {
         opqDS.removeColumn(colObj[i].toString());
       }        
       
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this);
     }
   }  
  
  public int removeRecentEntries() {
    int numEntriesRemoved = 0;
    
    try {
        List colList = opqDS.getColumnKeys();
        Object[] colObj = colList.toArray();
        if (debug) System.out.println("Number of Wait Events Entries in Chart prior to removing recent entries is: " + colObj.length);

        for(int i=colObj.length -1; i > offSet; i--) {
          opqDS.removeColumn(colObj[i].toString());
          numEntriesRemoved++;
        }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return numEntriesRemoved;
  }
    
  public void addBlankEntry(String executionDateTime) {
    int lastInstanceNum = ConsoleWindow.getNumberOfInstances();
    opqDS.addValue(0, "PQ Slave Process from inst " + lastInstanceNum, executionDateTime);
  }
  
  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
  }
  
  
  public void resetOffSet() {
    if (this.offSet != defaultOffSet) offSet = defaultOffSet;
  }
  
  public void setChartTitle(String title) {
    myChart.setTitle(new TextTitle(title));
  }
  
  public boolean isChartTitleSet() {
    if (myChart.getTitle().getText() instanceof String & myChart.getTitle().getText().length() > 0) {
      return true;
    }
    else {
      return false;
    }
  }
}
