 /*
  * PhysicalIOChart.java        13.24 26/07/06
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
  * Change History
  * ==============
  * 28/07/06 Richard Wright Added time labels to the catagory axis
  * 02/08/06 Richard Wright Made the offset default to 75 rather than 50
  * 03/08/06 Richard Wright Implemented config parameters for domain labels
  * 23/08/06 Richard Wright Modified the comment style and error handling
  * 11/03/07 Richard Wright Set chart background White
  * 11/03/07 Richard Wright Fixed error causing too many values to be removed
  *                         from the chart
  * 20/03/07 Richard Wright Modifyed offSet from 75 to 70
  * 20/03/07 Richard Wright Set the range axis to be black not red
  * 10/04/07 Richard Wright Modified for the new chart panel
  * 04/05/07 Richard Wright Correct Frame size when broken out
  * 14/05/07 Richard Wright Removed unused code
  * 30/11/07 Richard Wright Enhanced for RAC
  * 06/11/09 Richard Wright makeChart now calls updateChart
  * 22/09/11 Richard Wright Remove Playback functionality
  * 01/03/12 Richard Wright Stopped false spikes that occurred on changes to the offset
  * 17/05/12 Richard Wright Applied correct chart title to 10.1 db's
  * 29/09/15 Richard Wright re-instating the chart break out function
  */

  package RichMon;

import java.awt.Color;
import java.awt.Font;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Create and display a chart showing some other characteristics of a database
 */
public class PhysicalIOChart {

  private DefaultCategoryDataset physicalIODS;

  private NumberAxis myAxis;

  private float[] physicalIOEvents;
  private int physicalIOCounter;

  private int chartIteration = 0;
  private float valuesOfLastPhysicalRead;
  private float valuesOfLastPhysicalWrite;
  private int offSet = 75;
  private int defaultOffSet = 75;

  private JFreeChart myChart;
  
  boolean debug = false;
  int debugLevel=0;
  
  /**
   * Constructor
   */
  public PhysicalIOChart() {
  }

  /**
   * Create the chart and display it
   *
   * @param myResult - the QueryResult use to populate first iteration of the chart
   * @throws Exception
   */
   public void createChart(QueryResult myResult, String instanceName) throws Exception {
     myChart = makeChart(myResult);

     // create the chart frame and display it
     if (Properties.isBreakOutChartsTabsFrame()) {
       ChartPanel myChartPanel = new ChartPanel(myChart);
       ExecuteDisplay.addWindowHolder(myChartPanel, "Physical IO - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": Physical IO Chart",myChart, true);
       loadCF.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
       loadCF.pack();
       loadCF.setVisible(true);
     }
   }

    /**
     * Create a ChartPanel containing a chart
     * 
     * @param myResult
     * @return
     * @throws Exception
     */
    public ChartPanel createChartPanel(QueryResult myResult) throws Exception {
    myChart = makeChart(myResult);
    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }

    /**
     * Create the chart.
     * 
     * @param myResult
     * @return
     */
    private JFreeChart makeChart(QueryResult myResult) {
    physicalIODS = new DefaultCategoryDataset();
    valuesOfLastPhysicalRead = 0;
    valuesOfLastPhysicalWrite = 0;

    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      physicalIODS.addValue(0, "physical reads", currentTime);
      physicalIODS.addValue(0, "physical writes", currentTime);
    }
    
    physicalIOEvents = new float[offSet];

    chartIteration = 0;
    String axisLabel;
    if (ConsoleWindow.getDBVersion() >= 10.2) {
      axisLabel = "Physical I/O in mb";
    }
    else {
      axisLabel = "Number of Physical Reads/Writes";
    }

    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("",
                                                         "",
                                                         axisLabel,
                                                         physicalIODS,
                                                         PlotOrientation.VERTICAL,
                                                         true, true, false);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    

    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // do not display the domain axis (bottom axis)
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,fontSize));

    // configure the execute axis
    myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLowerBound(0);
    myAxis.setAutoRangeMinimumSize(20);
    myAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    updateChart(myResult);
    
    return myChart;
  }


  /**
   * Add a new result set to the chart
   *
   * @param myResult - QueryResult
   */
  public void updateChart(QueryResult myResult) {
    try {
      if (debug) System.out.println("chartIteration: " + chartIteration);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();

      float v1 = 0;
      float v2 = 0;

      for (int i = 0; i < resultSet.length; i++) {

        if (ConsoleWindow.getDBVersion() >= 10) {
          if (resultSet[i][0].equals("physical read total bytes")) {
            
            if (chartIteration > 0) v1 = (Float.valueOf(resultSet[i][1]).floatValue() / 1024 / 1204) - valuesOfLastPhysicalRead;
            
            if (debug) {
              System.out.println("adding " + myResult.getExecutionClockTime() + " new: " + Float.valueOf(resultSet[i][1]).floatValue() / 1024 / 1204 + " old: " + valuesOfLastPhysicalRead);
            }
            
            physicalIODS.addValue(v1, "physical reads", myResult.getExecutionClockTime());
            valuesOfLastPhysicalRead = (Float.valueOf(resultSet[i][1]).floatValue() / 1024 / 1204);
          }

          if (resultSet[i][0].equals("physical write total bytes")) {
            if (chartIteration > 0) v2 = (Float.valueOf(resultSet[i][1]).floatValue() / 1024 / 1024) - valuesOfLastPhysicalWrite;
            physicalIODS.addValue(v2, "physical writes", myResult.getExecutionClockTime());
            valuesOfLastPhysicalWrite = (Float.valueOf(resultSet[i][1]).floatValue() / 1024 / 1024);
          }
        }
        else {
          if (resultSet[i][0].equals("physical reads")) {
            if (chartIteration > 0) v1 = Float.valueOf(resultSet[i][1]).floatValue() - valuesOfLastPhysicalRead;

            physicalIODS.addValue(v1, "physical reads", myResult.getExecutionClockTime());
            valuesOfLastPhysicalRead = Float.valueOf(resultSet[i][1]).floatValue();
          }

          if (resultSet[i][0].equals("physical writes")) {
            if (chartIteration > 0) v2 = Float.valueOf(resultSet[i][1]).floatValue() - valuesOfLastPhysicalWrite;
            physicalIODS.addValue(v2, "physical writes", myResult.getExecutionClockTime());
            valuesOfLastPhysicalWrite = Float.valueOf(resultSet[i][1]).floatValue();
          }          
        }
      }

      physicalIOEvents[physicalIOCounter++] = Math.max(v2, v1);
      if (physicalIOCounter >= offSet - 1) physicalIOCounter = 0;

      removeOldEntries();
      updateAxis();
      chartIteration++;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  /**
   * Update the scale of the axis
   */
  private void updateAxis() {
    try {
      double max = 0;
      for (int i = 0; i < offSet; i++) {
        max = Math.max(max, physicalIOEvents[i]);
      }
      if (myAxis.getUpperBound() != Math.max(10.0, max + 1))
        myAxis.setRange(0, Math.max(10.0, max * 1.2));
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  /**
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
   public void removeOldEntries() {
     try {
         List colList = physicalIODS.getColumnKeys();
         Object[] colObj = colList.toArray();
         if (debug) System.out.println("Number of Phyisical IO Entries in Chart prior to removing old entries is: " + colObj.length);

         for(int i=0; i < colObj.length - offSet; i++) {
           if (debug) System.out.println("removing: " + colObj[i].toString());
           physicalIODS.removeColumn(colObj[i].toString());
         }
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this);
     }
   }   

  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
    debugLevel=1;
  }
 
  /**
   */
  public void resetOffSet() {  
    if (this.offSet != defaultOffSet) setOffSet(defaultOffSet);
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
