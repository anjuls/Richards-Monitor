 /*
  * PGAChart.java        17.60 29/11/12
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
import java.awt.Paint;
import java.awt.Rectangle;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * Create and display a chart showing pga characteristics of the database
 */
public class PGAChart {

  private DefaultCategoryDataset pgaSizeDS;
  private DefaultCategoryDataset overAllocationCountDS;

  private NumberAxis pgaSizeAxis;
  private NumberAxis overAllocationCountAxis;

  private int chartIteration = 0;
  private float valuesOfPGATarget;
  private float valuesOfPGAAutoTarget;
  private float valuesOfPGAInUse;
  private float valuesOfPGAAllocated;
  private float valuesOfOverAllocationCount;
  private int offSet = 75;
  private int defaultOffSet = 75;

  private JFreeChart myChart;
  
  boolean debug = false;
  
  /**
   * Constructor
   */
  public PGAChart() {
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
       ExecuteDisplay.addWindowHolder(myChartPanel, "PGA - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": PGA Chart",myChart, true);
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
      
    pgaSizeDS = new DefaultCategoryDataset();
    overAllocationCountDS = new DefaultCategoryDataset();
    
    valuesOfPGATarget = 0;
    valuesOfPGAAutoTarget = 0;
    valuesOfPGAInUse = 0;
    valuesOfPGAAllocated = 0;
    valuesOfOverAllocationCount = 0;
    
    // configure all other axis 
    pgaSizeAxis = new NumberAxis();
    overAllocationCountAxis = new NumberAxis();


    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      pgaSizeDS.addValue(0, "aggregate PGA auto target", currentTime);
      pgaSizeDS.addValue(0, "aggregate PGA target parameter", currentTime);
      pgaSizeDS.addValue(0, "total PGA allocated", currentTime);
      pgaSizeDS.addValue(0, "total PGA inuse", currentTime);
      overAllocationCountDS.addValue(0, "over allocation count", currentTime);
    }
    
    chartIteration = 0;
    String chartTitle = "PGA Size/Allocated/Target (bytes)";


    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("",
                                                         "",
                                                         chartTitle,
                                                         pgaSizeDS,
                                                         PlotOrientation.VERTICAL,
                                                         true, true, false);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    setupAxis(1, myPlot, overAllocationCountDS, "RIGHT", "Over Allocation Count",1);
  

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
    pgaSizeAxis = (NumberAxis)myPlot.getRangeAxis();
    pgaSizeAxis.setLowerBound(0);
    pgaSizeAxis.setAutoRangeMinimumSize(20);
    pgaSizeAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    pgaSizeAxis.setStandardTickUnits(integerTicks);
    pgaSizeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    pgaSizeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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

      for (int i = 0; i < resultSet.length; i++) {

        if (resultSet[i][0].equals("aggregate PGA auto target")) {
          v1 = Float.valueOf(resultSet[i][1]).floatValue();
          if (debug)
            System.out.println("aggregate PGA auto target: " + v1);
          pgaSizeDS.addValue(v1, resultSet[i][0], myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("aggregate PGA target parameter")) {
          v2 = Float.valueOf(resultSet[i][1]).floatValue();
          if (debug)
            System.out.println("aggregate PGA target parameter: " + v2);
          pgaSizeDS.addValue(v2, resultSet[i][0], myResult.getExecutionClockTime());

        }

        if (resultSet[i][0].equals("over allocation count")) {
          v3 = Float.valueOf(resultSet[i][1]).floatValue() - valuesOfOverAllocationCount;
          if (debug)
            System.out.println("over allocation count: " + v3);
          if (chartIteration > 0)
            overAllocationCountDS.addValue(v3, resultSet[i][0], myResult.getExecutionClockTime());
          valuesOfOverAllocationCount = Float.valueOf(resultSet[i][1]).floatValue();
        }

        if (resultSet[i][0].equals("total PGA allocated")) {
          v4 = Float.valueOf(resultSet[i][1]).floatValue();
          if (debug)
            System.out.println("total PGA allocated: " + v4);
          pgaSizeDS.addValue(v4, resultSet[i][0], myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("total PGA inuse")) {
          v5 = Float.valueOf(resultSet[i][1]).floatValue();
          if (debug)
            System.out.println("total PGA inuse: " + v5);
          pgaSizeDS.addValue(v5, resultSet[i][0], myResult.getExecutionClockTime());
        }

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
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
  public void removeOldEntries() {
    try {
      if (debug) System.out.println("columnCount | rowCount " + pgaSizeDS.getColumnCount() + " " + pgaSizeDS.getRowCount());
      List colList = pgaSizeDS.getColumnKeys();
      Object[] colObj = colList.toArray();
      
      

      for(int i=0; i < colObj.length - offSet; i++) {
        if (debug) System.out.println("removing readWriteDS entry " + colObj[i].toString());
        if (debug) System.out.println("removing reads " + pgaSizeDS.getValue(0, 0) + " writes " + pgaSizeDS.getValue(1,0));
        pgaSizeDS.removeColumn(colObj[i].toString());
        overAllocationCountDS.removeColumn(colObj[i].toString());

      }
    }
    catch (Exception e) {
      // ConsoleWindow.displayError(e,this);
    }
  }
     

  private void updateAxis() {
    List colList = pgaSizeDS.getColumnKeys();
    Object[] colObj = colList.toArray();

    int maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(pgaSizeDS.getValue(0,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(pgaSizeDS.getValue(1,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(pgaSizeDS.getValue(2,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(pgaSizeDS.getValue(3,i).intValue()));
    }    
//    if (debug) System.out.println("readWriteDS maxValue=" + maxValue);
      pgaSizeAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
    
    maxValue = 0;
    
    for(int i=0; i < colObj.length; i++) {
      try {
        maxValue = Math.max(maxValue, Integer.valueOf(overAllocationCountDS.getValue(0, i).intValue()));
      } catch (Exception e) {
        // workaround to unexplained null pointer exception because overAllocationCountDS keeps getting smaller
      }
    }    
//    if (debug) System.out.println("readWriteTimeDS maxValue=" + maxValue);
    overAllocationCountAxis.setRange(0, Math.max(10.0, maxValue * 1.3));

  }

  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
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
  
  private void setupAxis(int i, CategoryPlot myPlot, 
                         DefaultCategoryDataset axisDS, String axisPos, 
                         String axisName, int chartNum) {
    // add the dataset to the plot 
    myPlot.setDataset(i, axisDS);

    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    
    if (chartNum == 1 && i == 1) myAxis = pgaSizeAxis;
    if (chartNum == 1 && i == 2) myAxis = overAllocationCountAxis;

    myAxis.setLabel(axisName);

    // make axis increment in integers 
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);

    myPlot.setRangeAxis(i, myAxis);
    if (axisPos.equals("RIGHT")) {
      myPlot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_RIGHT);
    }
    else {
      myPlot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_LEFT);
    }
    
    myPlot.mapDatasetToRangeAxis(i, i);

    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0, new Rectangle(0, 0));
    myRenderer.setSeriesShape(1, new Rectangle(0, 0));
    if (i == 1) myRenderer.setSeriesPaint(0, Color.GREEN);
    if (i == 1) myRenderer.setSeriesPaint(1, Color.ORANGE);
    if (i == 2) myRenderer.setSeriesPaint(0, Color.CYAN);
    if (i == 2) myRenderer.setSeriesPaint(1, Color.GRAY);
    
  //  myRenderer.setSeriesPaint(0, Color.BLACK);
    myPlot.setRenderer(i, myRenderer);

    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
  //  myAxis.setLabelPaint(axisPaint);
  //  myAxis.setTickLabelPaint(axisPaint);
  //  myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    myAxis.setRange(0, 10);
  }

}
