 /*
  * ASMIOStatChart.java        17.53 02/12/11
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
  * 29/11/12 Richard Wright Stopped data points being added more than once
  * 29/09/15 Richard Wright re-instating the chart break out function
  * 
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
 * Create and display a chart showing some other characteristics of a database
 */
public class ASMIOStatChart {

  private DefaultCategoryDataset readWriteDS;
  private DefaultCategoryDataset readWriteTimeDS;
  private DefaultCategoryDataset readWriteBytesDS;

  private NumberAxis readWriteAxis;
  private NumberAxis readWriteTimeAxis;
  private NumberAxis readWriteBytesAxis;

  private int chartIteration = 0;
  private float valuesOfReads;
  private float valuesOfWrites;
  private float valuesOfReadTime;
  private float valuesOfWriteTime;
  private float valuesOfBytesRead;
  private float valuesOfBytesWritten; 
  private int offSet = 75;
  private int defaultOffSet = 75;

  private JFreeChart myChart;
  
  boolean debug = false;
  
  /**
   * Constructor
   */
  public ASMIOStatChart() {
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
       ExecuteDisplay.addWindowHolder(myChartPanel, "ASM IOSTAT - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": ASM IOSTAT Chart",myChart, true);
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
    readWriteDS = new DefaultCategoryDataset();
    readWriteTimeDS = new DefaultCategoryDataset();
    readWriteBytesDS = new DefaultCategoryDataset();
    
    valuesOfReads = 0;
    valuesOfWrites = 0;
    valuesOfReadTime = 0;
    valuesOfWriteTime = 0;
    valuesOfBytesRead = 0;
    valuesOfBytesWritten = 0;
    
    // configure all other axis 
    readWriteTimeAxis = new NumberAxis();
    readWriteBytesAxis = new NumberAxis();
    
  

    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      readWriteDS.addValue(0, "reads", currentTime);
      readWriteDS.addValue(0, "writes", currentTime);
      readWriteTimeDS.addValue(0, "read time", currentTime);
      readWriteTimeDS.addValue(0, "write time", currentTime);
      readWriteBytesDS.addValue(0, "bytes read", currentTime);
      readWriteBytesDS.addValue(0, "bytes written", currentTime);
    }

/*    for (int i=0; i < offSet; i++) {
      valuesOfReads[i] = 0;
      valuesOfWrites[i] = 0;      
      valuesOfReadTime[i] = 0;
      valuesOfWriteTime[i] = 0;      
      valuesOfBytesRead[i] = 0;
      valuesOfBytesWritten[i] = 0;      
    } */
    
    chartIteration = 0;
    String chartTitle = "# reads / # writes";


    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("",
                                                         "",
                                                         chartTitle,
                                                         readWriteDS,
                                                         PlotOrientation.VERTICAL,
                                                         true, true, false);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    setupAxis(1, myPlot, readWriteTimeDS, "LEFT", "read / write TIME",1);
    setupAxis(2, myPlot, readWriteBytesDS, "RIGHT", "read / write BYTES",1);
  

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
    readWriteAxis = (NumberAxis)myPlot.getRangeAxis();
    readWriteAxis.setLowerBound(0);
    readWriteAxis.setAutoRangeMinimumSize(20);
    readWriteAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    readWriteAxis.setStandardTickUnits(integerTicks);
    readWriteAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    readWriteAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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


      v1 = Float.valueOf(resultSet[0][0]).floatValue() - valuesOfReads;

      if (debug)
        System.out.println("adding reads " + v1);
      if (chartIteration > 0)
        readWriteDS.addValue(v1, "reads", myResult.getExecutionClockTime());
      valuesOfReads = Float.valueOf(resultSet[0][0]).floatValue();

      
      v2 = Float.valueOf(resultSet[0][1]).floatValue() - valuesOfWrites;

      if (debug) System.out.println("adding writes " + v2);
      if (chartIteration > 0) readWriteDS.addValue(v2, "writes", myResult.getExecutionClockTime());
      valuesOfWrites = Float.valueOf(resultSet[0][1]).floatValue();
    
      v3 = Float.valueOf(resultSet[0][2]).floatValue() - valuesOfReadTime;

      if (chartIteration > 0) readWriteTimeDS.addValue(v3, "read time", myResult.getExecutionClockTime());
      valuesOfReadTime = Float.valueOf(resultSet[0][2]).floatValue();
      
      v4 = Float.valueOf(resultSet[0][3]).floatValue() - valuesOfWriteTime;

      if (chartIteration > 0) readWriteTimeDS.addValue(v4, "write time", myResult.getExecutionClockTime());
      valuesOfWriteTime = Float.valueOf(resultSet[0][3]).floatValue();
     
      v5 = Float.valueOf(resultSet[0][4]).floatValue() - valuesOfBytesRead;

      if (chartIteration > 0) readWriteBytesDS.addValue(v5, "bytes read", myResult.getExecutionClockTime());
      valuesOfBytesRead = Float.valueOf(resultSet[0][4]).floatValue();
      
      v6 = Float.valueOf(resultSet[0][5]).floatValue() - valuesOfBytesWritten;

      if (chartIteration > 0) readWriteBytesDS.addValue(v6, "bytes written", myResult.getExecutionClockTime());
      valuesOfBytesWritten = Float.valueOf(resultSet[0][5]).floatValue();



      removeOldEntries();
      updateAxis();
      chartIteration++;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
/*
  private void updateAxis() {
    try {
      // readWrightAxis 
      double maxValue = 0;
      for (int i = 0; i < offSet; i++) {
        maxValue = Math.max(maxValue, valuesOfReads[i]);
        maxValue = Math.max(maxValue, valuesOfWrites[i]);
      }
      if (readWriteAxis.getUpperBound() != Math.max(10.0, maxValue * 1.3))
        readWriteAxis.setRange(0, Math.max(10.0, maxValue * 1.3));

      // readWriteTimeAxis 
      maxValue = 0;
      for (int i = 0; i < offSet; i++) {
        maxValue = Math.max(maxValue, valuesOfReadTime[i]);
        maxValue = Math.max(maxValue, valuesOfWriteTime[i]);
      }
      if (readWriteTimeAxis.getUpperBound() != Math.max(10.0, maxValue * 1.6))
        readWriteTimeAxis.setRange(0, Math.max(10.0, maxValue * 1.6));

      // readWriteBytesAxis 
      maxValue = 0;
      for (int i = 0; i < offSet; i++) {
        maxValue = Math.max(maxValue, valuesOfBytesRead[i]);
        maxValue = Math.max(maxValue, valuesOfBytesWritten[i]);
      }
      if (readWriteBytesAxis.getUpperBound() != Math.max(10.0, maxValue * 1.9))
        readWriteBytesAxis.setRange(0, Math.max(10.0, maxValue * 1.9));

    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }
/*

  /**
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
  public void removeOldEntries() {
    try {
      if (debug) System.out.println("columnCount | rowCount " + readWriteDS.getColumnCount() + " " + readWriteDS.getRowCount());
      List colList = readWriteDS.getColumnKeys();
      Object[] colObj = colList.toArray();
      
      

      for(int i=0; i < colObj.length - offSet; i++) {
        if (debug) System.out.println("removing readWriteDS entry " + colObj[i].toString());
        if (debug) System.out.println("removing reads " + readWriteDS.getValue(0, 0) + " writes " + readWriteDS.getValue(1,0));
        readWriteDS.removeColumn(colObj[i].toString());
        readWriteTimeDS.removeColumn(colObj[i].toString());
        readWriteBytesDS.removeColumn(colObj[i].toString());
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
     

  private void updateAxis() {
    List colList = readWriteDS.getColumnKeys();
    Object[] colObj = colList.toArray();

    int maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(readWriteDS.getValue(0,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(readWriteDS.getValue(1,i).intValue()));
    }    
    if (debug) System.out.println("readWriteDS maxValue=" + maxValue);
    readWriteAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
    
    maxValue = 0;
    
    for(int i=0; i < colObj.length; i++) {
      maxValue= Math.max(maxValue,Integer.valueOf(readWriteTimeDS.getValue(0,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(readWriteTimeDS.getValue(1,i).intValue()));
    }    
    if (debug) System.out.println("readWriteTimeDS maxValue=" + maxValue);
    readWriteTimeAxis.setRange(0, Math.max(10.0, maxValue * 1.4));
    
    maxValue=0;
    
    for(int i=0; i < colObj.length; i++) {
      maxValue= Math.max(maxValue,Integer.valueOf(readWriteBytesDS.getValue(0,i).intValue()));
      maxValue = Math.max(maxValue,Integer.valueOf(readWriteBytesDS.getValue(1,i).intValue()));
    }    
    if (debug) System.out.println("readWriteBytesDS maxValue=" + maxValue);
    readWriteBytesAxis.setRange(0, Math.max(10.0, maxValue * 1.5));

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
    
    if (chartNum == 1 && i == 1) myAxis = readWriteTimeAxis;
    if (chartNum == 1 && i == 2) myAxis = readWriteBytesAxis;

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
