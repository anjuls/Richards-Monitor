 /*
  * IOPSChart.java        17.56 18/05/12
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
public class IOPSChart {

  private DefaultCategoryDataset iopsDS;

  private NumberAxis myAxis;

  private double[] iopsEvents;
  private int iopsCounter;

  private int chartIteration = 0;
  private float valuesOfLastRead;
  private float valuesOfLastWrite;
  private int offSet = 75;
  private int defaultOffSet = 75;
  private long lastExecutionTimeInMS = 0;

  private JFreeChart myChart;
  
  boolean debug = false;
  int debugLevel=0;
  
  /**
   * Constructor
   */
  public IOPSChart() {
  }

  /**
   * Create the chart and display it
   *
   * @param myResult - the QueryResult use to populate first iteration of the chart
   * @throws Exception
   */
   public void createChart(QueryResult myResult, String instanceName, long executionStartTime) throws Exception {
     
     myChart = makeChart(myResult,executionStartTime);

     // create the chart frame and display it
     if (Properties.isBreakOutChartsTabsFrame()) {
       ChartPanel myChartPanel = new ChartPanel(myChart);
       ExecuteDisplay.addWindowHolder(myChartPanel, "IOPS - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": IOPS Chart",myChart, true);
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
    public ChartPanel createChartPanel(QueryResult myResult,long executionStartTime) throws Exception {
    myChart = makeChart(myResult,executionStartTime);
    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }

    /**
     * Create the chart.
     * 
     * @param myResult
     * @return
     */
    private JFreeChart makeChart(QueryResult myResult, long executionStartTime) {
    iopsDS = new DefaultCategoryDataset();
    valuesOfLastRead = 0;
    valuesOfLastWrite = 0;

    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      iopsDS.addValue(0, "read IOPS", currentTime);
      iopsDS.addValue(0, "write IOPS", currentTime);
      iopsDS.addValue(0, "total IOPS", currentTime);
    }
    
    iopsEvents = new double[offSet];

    chartIteration = 0;
    String axisLabel= "I/O's per Second";
   

    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("",
                                                         "",
                                                         axisLabel,
                                                         iopsDS,
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
    
    updateChart(myResult, executionStartTime);
    
    return myChart;
  }


  /**
   * Add a new result set to the chart
   *
   * @param myResult - QueryResult
   */
  public void updateChart(QueryResult myResult, long executionStartTime) {
    try {
      if (debug) System.out.println("chartIteration: " + chartIteration);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();

      double v1 = 0;
      double v2 = 0;
      double v3 = 0;

      for (int i = 0; i < resultSet.length; i++) {

        if (resultSet[i][0].equals("physical read total IO requests")) {  
          if (chartIteration > 0) {
            v1 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfLastRead;
            if (debug) {
              System.out.println("V1 prior " + v1);
              System.out.println("executionStartTime " + executionStartTime + " lastExecutionTimeInMS " + lastExecutionTimeInMS );
              System.out.println("divisor is " + (executionStartTime - lastExecutionTimeInMS)/1000);
            }            
            v1 = v1 / ((executionStartTime - lastExecutionTimeInMS)/1000);
            if (debug) System.out.println("v1 " + v1);

          }
          iopsDS.addValue(v1, "read IOPS", myResult.getExecutionClockTime());
          valuesOfLastRead = (Float.valueOf(resultSet[i][1]).floatValue());
         
        }
  
        if (resultSet[i][0].equals("physical write total IO requests")) {
          if (chartIteration > 0) {
            v2 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfLastWrite;
            if (debug) {
              System.out.println("V2 prior " + v2);
              System.out.println("executionStartTime " + executionStartTime + " lastExecutionTimeInMS " + lastExecutionTimeInMS );
              System.out.println("divisor is " + (executionStartTime - lastExecutionTimeInMS)/1000);
            }
            v2 = v2 / ((executionStartTime - lastExecutionTimeInMS)/1000);
            if (debug) System.out.println("v2 " + v2);

          }
          iopsDS.addValue(v2, "write IOPS", myResult.getExecutionClockTime());
          valuesOfLastWrite = (Float.valueOf(resultSet[i][1]).floatValue());
        }
      }
      
      lastExecutionTimeInMS = executionStartTime;

      if (chartIteration > 0) {
        v3 = v1 + v2;
        v3 = v3 / Math.max((executionStartTime - lastExecutionTimeInMS),1);
        if (debug) System.out.println("v3 " + v3 + " " + Math.max((executionStartTime - lastExecutionTimeInMS),1));

      }
      iopsDS.addValue(v3, "total IOPS", myResult.getExecutionClockTime());
      iopsEvents[iopsCounter++] = v3;
      if (iopsCounter >= offSet - 1) {
        iopsCounter = 0;
        if (debug) System.out.println("reseting iopsCounter");
      }
      
      

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
        max = Math.max(max, iopsEvents[i]);
      }
      if (myAxis.getUpperBound() != Math.max(1, max + 1))
        myAxis.setRange(0, Math.max(1, max * 1.2));
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
         List colList = iopsDS.getColumnKeys();
         Object[] colObj = colList.toArray();

         for(int i=0; i < colObj.length - offSet; i++) {
           iopsDS.removeColumn(colObj[i].toString());
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
