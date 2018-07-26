 /*
  * OPQDowngradesChart.java        17.48 16/12/10
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
  * 22/09/11 Richard Wright Removing playback functionality
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
public class OPQDowngradesChart {

  private DefaultCategoryDataset parallelOperationsDS;

  private NumberAxis myAxis;

  private float opqEvents[];
  private int opqCounter;

  private int chartIteration = 0;
  private float valuesOfNotDowngraded;
  private float valuesOfDowngradedToSerial;
  private float valuesOfDowngraded75to99PCT;
  private float valuesOfDowngraded50to75PCT;
  private float valuesOfDowngraded25to50PCT;
  private float valuesOfDowngraded1to25PCT;
  private int offSet = 75;
  private int defaultOffSet = 75;

  private JFreeChart myChart;
    boolean debug = false;
  
  /**
   * Constructor
   */
  public OPQDowngradesChart() {
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
       ExecuteDisplay.addWindowHolder(myChartPanel, "OPQ Downgrades - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": OPQ Downgrades Chart",myChart, true);
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
    parallelOperationsDS = new DefaultCategoryDataset();

    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      parallelOperationsDS.addValue(0, "Parallel operations not downgraded", currentTime);
      parallelOperationsDS.addValue(0, "Parallel operations downgraded to serial", currentTime);
      parallelOperationsDS.addValue(0, "Parallel operations downgraded 75 to 99 pct", currentTime);
      parallelOperationsDS.addValue(0, "Parallel operations downgraded 50 to 75 pct", currentTime);
      parallelOperationsDS.addValue(0, "Parallel operations downgraded 25 to 50 pct", currentTime);
      parallelOperationsDS.addValue(0, "Parallel operations downgraded 1 to 25 pct", currentTime);
    }

    valuesOfDowngradedToSerial = 0;
    valuesOfNotDowngraded = 0;
    valuesOfDowngraded75to99PCT = 0;      
    valuesOfDowngraded50to75PCT = 0;
    valuesOfDowngraded25to50PCT = 0;      
    valuesOfDowngraded1to25PCT = 0;
    
    
    opqEvents = new float[offSet];

    chartIteration = 0;
    String chartTitle;
    if (ConsoleWindow.getDBVersion() >= 10) {
      chartTitle = "OPQ Downgrades";
    }
    else {
      chartTitle = "OPQ Downgrades";
    }


    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("",
                                                         "",
                                                         chartTitle,
                                                         parallelOperationsDS,
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
      String[][] resultSet = myResult.getResultSetAsStringArray();

      float v1 = 0;
      float v2 = 0;
      float v3 = 0;
      float v4 = 0;
      float v5 = 0;
      float v6 = 0;

      for (int i = 0; i < resultSet.length; i++) {

        /*
         * The query uses gv$ which means that the query itself shows up in the results.
         * On the local instance you will see a Parallel oparation downgraded to serial and 
         * 1 Parallel operation not downgraded for each remote instance (unless downgraded !)
         */
        
        if (resultSet[i][0].equals("Parallel operations downgraded to serial")) {
          
          // adjust down by 1 if the local instance is selected
          int adjustment = 0;
          String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
          for (int j=0; j < selectedInstances.length; j++) {
            if (selectedInstances[j][1].toLowerCase().equals(ConsoleWindow.getInstanceName().toLowerCase())) adjustment++;
          }
          
          if (chartIteration > 0) v1 = (Float.valueOf(resultSet[i][1]).floatValue()) - (valuesOfDowngradedToSerial + adjustment);
          parallelOperationsDS.addValue(v1, "Parallel operations downgraded to serial", myResult.getExecutionClockTime());
          valuesOfDowngradedToSerial = (Float.valueOf(resultSet[i][1]).floatValue());
        }          
        
        if (resultSet[i][0].equals("Parallel operations not downgraded")) {
          
          //adjust down by 1 for each remote instance selected
          int adjustment = 0;
          String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
          for (int j=0; j < selectedInstances.length; j++) {
            if (!selectedInstances[j][1].toLowerCase().equals(ConsoleWindow.getInstanceName().toLowerCase())) adjustment++;
          }
          
          if (chartIteration > 0) v2 = (Float.valueOf(resultSet[i][1]).floatValue()) - (valuesOfNotDowngraded + adjustment);
          parallelOperationsDS.addValue(v2, "Parallel operations not downgraded", myResult.getExecutionClockTime());
          valuesOfNotDowngraded = (Float.valueOf(resultSet[i][1]).floatValue());
        }

        if (resultSet[i][0].equals("Parallel operations downgraded 75 to 99 pct")) {
          if (chartIteration > 0) v3 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfDowngraded75to99PCT;
          parallelOperationsDS.addValue(v3, "Parallel operations downgraded 75 to 99 pct", myResult.getExecutionClockTime());
          valuesOfDowngraded75to99PCT = (Float.valueOf(resultSet[i][1]).floatValue());
        }          
        
        if (resultSet[i][0].equals("Parallel operations downgraded 50 to 75 pct")) {
            if (chartIteration > 0) v4 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfDowngraded50to75PCT;
            parallelOperationsDS.addValue(v4, "Parallel operations downgraded 50 to 75 pct", myResult.getExecutionClockTime());
            valuesOfDowngraded50to75PCT = (Float.valueOf(resultSet[i][1]).floatValue());
          }
        
        if (resultSet[i][0].equals("Parallel operations downgraded 25 to 50 pct")) {
            if (chartIteration > 0) v5 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfDowngraded25to50PCT;
            parallelOperationsDS.addValue(v5, "Parallel operations downgraded 25 to 50 pct", myResult.getExecutionClockTime());
            valuesOfDowngraded25to50PCT = (Float.valueOf(resultSet[i][1]).floatValue());
          }
        
        if (resultSet[i][0].equals("Parallel operations downgraded 1 to 25 pct")) {
            if (chartIteration > 0) v6 = (Float.valueOf(resultSet[i][1]).floatValue()) - valuesOfDowngraded1to25PCT;
            parallelOperationsDS.addValue(v6, "Parallel operations downgraded 1 to 25 pct", myResult.getExecutionClockTime());
            valuesOfDowngraded1to25PCT = (Float.valueOf(resultSet[i][1]).floatValue());
          }

      }

      opqEvents[opqCounter++] = Math.max(v2, v1);
      Math.max(v1, Math.max(v2, Math.max(v3, Math.max(v4, Math.max(v5, v6)))));
      if (opqCounter >= offSet - 1) opqCounter = 0;

      removeOldEntries();
      updateOPQAxis();
      chartIteration++;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  /**
   * Update the scale of the axis
   */
  private void updateOPQAxis() {
    try {
      // user calls axis
      double max = 0;
      for (int i = 0; i < offSet; i++) {
        max = Math.max(max, opqEvents[i]);
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
         List colList = parallelOperationsDS.getColumnKeys();
         Object[] colObj = colList.toArray();
         if (debug) System.out.println("Number of Phyisical IO Entries in Chart prior to removing old entries is: " + colObj.length);

         for(int i=0; i < colObj.length - offSet; i++) {
           parallelOperationsDS.removeColumn(colObj[i].toString());
         }
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this);
     }
   }   
 
  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
  }
   
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
