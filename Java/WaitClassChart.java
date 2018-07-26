/*
 * WaitClassChart.java        17.55 19/03/12
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
 * Change History since 23/05/05
 * =============================
 * 29/09/15 Richard Wright re-instating the chart break out function
 * 
 */
 
package RichMon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;
import java.util.Vector;

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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Create and display a chart showing wait events in near real time
 */
public class WaitClassChart {

  private DefaultCategoryDataset waitClassDS;
  private NumberAxis waitClassAxis;
  private int[] waitClassMaxEvents = new int[150];
  private int waitClassMaxEventsCounter = 0;
  private int chartIteration = 0;
  
  private int offSet = 64;
  private int defaultOffSet = 64;
  private String currentExecutionTime;
  private JFreeChart myChart;
  
  
  boolean debug = false;

  /**
   * Constructor
   */
  public WaitClassChart() {
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
      ExecuteDisplay.addWindowHolder(myChartPanel, "Wait Class - " + instanceName);
    } else {
      ChartFrame waitClassCF = new ChartFrame(ConsoleWindow.getInstanceName() + 
          ": Wait Class Chart", myChart, true);
      if (maxWidth) {
        // check the screen width 
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        
        // set the frame width 
        Dimension currentDimension = waitClassCF.getSize();
        int newWidth = Math.min(screenWidth,currentDimension.width*2);
        currentDimension.setSize(newWidth,currentDimension.height);
        waitClassCF.setSize(currentDimension);
        
      }  
      else {
        waitClassCF.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
      }
      waitClassCF.setVisible(true);
    }

    /*
     * remove old values 
     * this is required when the frame is restarted
     */
    removeOldWaitClassEntries();
  }
                          

   public ChartPanel createChartPanel(QueryResult myResult, boolean initialize, String instanceId) throws Exception {

     myChart = makeChart(myResult, initialize);

     // create the chart frame and display it 
     ChartPanel myChartPanel = new ChartPanel(myChart);

     /*
      * remove old values 
      * this is required when the frame is restarted
      */
     removeOldWaitClassEntries();
     
     return myChartPanel;
   }    

  private JFreeChart makeChart(QueryResult myResult, boolean initialize) {
    waitClassDS = new DefaultCategoryDataset();

    // Get the current time 
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's 
    String[] defaultEvents = Properties.getDefaultEvents();
    String event;
    
    if (initialize) {
      for (int i = 0 - offSet; i < 0; i++) {
        today = new Date();
        today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
        currentTime = dateFormatter.format(today);
                   
        waitClassDS.addValue(0,"User I/O",currentTime);
        waitClassDS.addValue(0,"Concurrency",currentTime);
        waitClassDS.addValue(0,"System I/O",currentTime);
        waitClassDS.addValue(0,"Administrative",currentTime);
        waitClassDS.addValue(0,"Other",currentTime);
        waitClassDS.addValue(0,"Scheduler",currentTime);
        waitClassDS.addValue(0,"Configuration",currentTime);
        waitClassDS.addValue(0,"Cluster",currentTime);
        waitClassDS.addValue(0,"Application",currentTime);
        waitClassDS.addValue(0,"Network",currentTime);
        waitClassDS.addValue(0,"Commit",currentTime);
        waitClassDS.addValue(0,"Cluster",currentTime);
      }
    }
   

    // create the chart 
    JFreeChart myWaitChart = ChartFactory.createStackedBarChart3D(
                                "",
                                "",
                                "# Sessions waiting on an event in CLASS",
                                waitClassDS,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false
                              );
    
//    myWaitChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myWaitChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    myWaitChart.getLegend().setBorder(0,0,0,0);   // remove border around the legend
    myWaitChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));
    
    
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
    waitClassAxis = (NumberAxis)myPlot.getRangeAxis();
    waitClassAxis.setLowerBound(0);
    waitClassAxis.setAutoRangeMinimumSize(20);
    waitClassAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    waitClassAxis.setStandardTickUnits(integerTicks);
    waitClassAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    waitClassAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    currentExecutionTime = myResult.getExecutionClockTime();

    updateChart(myResult);

    return myWaitChart;
  }


  /**
   * Add a new result set to the chart
   * 
   * @param myResult - QueryResult
   */
  public void updateChart(QueryResult myResult) {
    try {
      removeOldWaitClassEntries();
      int sumNumEvents = 0;
      
      currentExecutionTime = myResult.getExecutionClockTime();
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      for (int i=0; i < myResult.getNumRows(); i++) {
        int numEvents = Integer.valueOf(resultSet[i][1]).intValue();
        waitClassDS.addValue(numEvents , resultSet[i][0], currentExecutionTime);
        sumNumEvents = sumNumEvents + numEvents;
        //if (debug) System.out.println("WE: added entry : " + resultSet[i][0] + currentExecutionTime);
      }
      
      // if there are no wait events to report add a blank entry 
      if (myResult.getNumRows() == 0) addBlankEntry(currentExecutionTime);
          
      // calculate the maximum number of events for the wait event range axis 
      int maxNumEvents = 0;
      for (int j=0; j < offSet; j++) {
        maxNumEvents = Math.max(maxNumEvents,waitClassMaxEvents[j]);
      }
      
      maxNumEvents = Math.max(maxNumEvents,sumNumEvents);
      
      // set the wait event range axis upper boundary 
      if (waitClassAxis.getUpperBound() != Math.max(10.0,maxNumEvents +1)) waitClassAxis.setRange(0,Math.max(10.0,maxNumEvents +1));
      
      chartIteration++;
      
      // store away the numEvents to enable range axis calculations 
      waitClassMaxEvents[waitClassMaxEventsCounter++] = sumNumEvents;
      if (waitClassMaxEventsCounter >= offSet -1) waitClassMaxEventsCounter = 0;
      
//      removeOldWaitEventEntries();
    } catch (Exception e) {
        ConsoleWindow.displayError(e, this);
    }
  }


  /**
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
   public void removeOldWaitClassEntries() {
     try {
       boolean debug=false;
       List colList = waitClassDS.getColumnKeys();
       
       Object[] colObj = colList.toArray();
//       if (debug) System.out.println("Number of Wait Events Entries in Chart prior to removing old entries is: " + colObj.length);
//       if(debug) System.out.println("WE:  colObj.length - offSet     : " + (colObj.length - offSet));

       for(int i=0; i < colObj.length - offSet; i++) {
        

         waitClassDS.removeColumn(colObj[i].toString());
         
         if (debug) {
//           myChartPanel.repaint();
//           List rowList = waitEventsDS.getRowKeys();
           //search rowkeys for db file sequential read and remove that key

          try {
//            boolean ok = rowList.remove("db file sequential read");
//            waitEventsDS.removeRow("db file sequential read");
            
          } catch (Exception e) {
            // TODO: Add catch code
            
          }
         }
       }        
       
       /* 
        * Re-construct the Legend
        */
 /*      List rowList = waitEventsDS.getRowKeys();
       Object[] rowObj = rowList.toArray();
       
       final LegendItemCollection legendItemsNew = new LegendItemCollection();

       for(int i = 0; i < rowObj.length; i++) {
         if (debug) System.out.println("WE: adding new legend item: " + rowObj[i].toString());
         legendItemsNew.add(new LegendItem(rowObj[i].toString()));
       }
       
       LegendItemSource source = new LegendItemSource() {
         LegendItemCollection lic = new LegendItemCollection();
         {lic.addAll(legendItemsNew);}
           public LegendItemCollection getLegendItems() {  
           return lic;
           }
         };

       myChart.removeLegend();
       myChart.addLegend(new LegendTitle(source);
       
    */   
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this);
     }
   }  
  
  public int removeRecentEntries() {
    int numEntriesRemoved = 0;
    
    try {
        List colList = waitClassDS.getColumnKeys();
        Object[] colObj = colList.toArray();
        if (debug) System.out.println("Number of Wait Events Entries in Chart prior to removing recent entries is: " + colObj.length);

        for(int i=colObj.length -1; i > offSet; i--) {
          waitClassDS.removeColumn(colObj[i].toString());
          numEntriesRemoved++;
        }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    
    return numEntriesRemoved;
  }
  
 
  public void addBlankEntry(String executionDateTime) {
    waitClassDS.addValue(0,"Cluster",executionDateTime);
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
