/*
 * SharedPoolChartEnhanced.java        14.08 02/04/07
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
 * Change History since 23/05/05
 * =============================
 * 
 * 23/05/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 09/06/05 Richard Wright Upgraded to JFreeChart 1.0.0
 * 10/06/05 Richard Wright Modified so that if connected as sysdba the free space 
 *                         in the reserved pool is given
 * 10/06/05 Richard Wright Modified the axis ranges to make it slightly clearer.  
 *                         The sql has also been modified to round rather than 
 *                         truncate, and include a single decimal place for greater 
 *                         clarity
 * 10/06/05 Richard Wright In updateAxis(), maxValue is reset between calculation 
 *                         to avoid possible mis-calculations
 * 10/06/05 Richard Wright Added LogWriter Output
 * 10/10/05 Richard Wright Removed all references to avg cursors per session and 
 *                         now always show free space in the reserved pool instead, 
 *                         regardless of whether a sysdba connection is used
 * 10/03/06 Richard Wright Modified so that the chart can appear in another frame
 * 03/08/06 Richard Wright Added time labels to the catagory axis
 * 03/08/06 Richard Wright Made the offset default to 75 rather than 50
 * 03/08/06 Richard Wright Implemented config parameters for domain labels  
 * 01/09/06 Richard Wright Modified the comment style and error handling
 * 11/03/07 Richard Wright Set chart background white
 * 19/03/07 Richard Wright Modified removeOldEntries to use offSet rather than 50
 * 20/03/07 Richard Wright Modified offSet to be 40 rather than 75
 * 20/03/07 Richard Wright Modified font size on the axis
 * 02/04/07 Richard Wright Created from sharedPoolChart and modified to show 2 
 *                         charts on the same panel
 * 10/04/07 Richard Wright Modified for the new chart panel
 * 14/05/07 Richard Wright Remove unused code
 * 24/04/08 Richard Wright Set series paint to allow upgrade of jFreeChart
 * 22/09/11 Richard Wright Removed Playback functionality
 * 04/01/12 Richard Wright Re-wrote some code to ensure the axis update properly
 * 01/03/12 Richard Wright Adjusted so that lines are not shown exactly on top of each other
 * 05/03/12 Richard Wright Fixed index out of bounds exception when updating axis
 * 29/09/15 Richard Wright re-instating the chart break out function
 */
 
package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;

import javax.swing.JPanel;

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
 */
public class SharedPoolChart {

  private DefaultCategoryDataset libraryCacheDS;
  private DefaultCategoryDataset miscellaneousDS;
  private DefaultCategoryDataset sqlAreaDS;
  private DefaultCategoryDataset freeMemoryDS;
  private DefaultCategoryDataset freeMemoryReservedPoolDS;
  private DefaultCategoryDataset openCursorsDS;
  private DefaultCategoryDataset sessionsDS;
  private DefaultCategoryDataset reloadsDS;
  private DefaultCategoryDataset invalidationsDS;

  private NumberAxis miscellaneousAxis;
  private NumberAxis sqlAreaAxis;
  private NumberAxis freeMemoryAxis;
  private NumberAxis freeMemoryReservedPoolAxis;
  private NumberAxis sessionsAxis;
  private NumberAxis invalidationsAxis;
  private NumberAxis reloadsAxis;
  private NumberAxis openCursorsAxis;
  private NumberAxis libraryCacheAxis;

  private double reloadEvents;
  private double reloadEventValues;
  private double invalidationEvents;
  private double invalidationEventValues;
  private int iteration = 0;

  private int defaultOffSet = 50;
  private int offSet = 50;
  
  private boolean debug = false;
 
  private JFreeChart[] myCharts;
  
  public SharedPoolChart() {
  }

  private JFreeChart[] makeChart(QueryResult myResult) throws Exception {
    libraryCacheDS = new DefaultCategoryDataset();
    openCursorsDS = new DefaultCategoryDataset();
    sessionsDS = new DefaultCategoryDataset();
    reloadsDS = new DefaultCategoryDataset();
    invalidationsDS = new DefaultCategoryDataset();
    sqlAreaDS = new DefaultCategoryDataset();
    miscellaneousDS = new DefaultCategoryDataset();
    freeMemoryDS = new DefaultCategoryDataset();
    freeMemoryReservedPoolDS = new DefaultCategoryDataset();

    // Get the current time 
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize
    reloadEvents = 0;
    reloadEventValues = 0;
    invalidationEvents = 0;
    invalidationEventValues = 0;


    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + 
          (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      libraryCacheDS.addValue(0, "library cache", currentTime);
      miscellaneousDS.addValue(0, "miscellaneous", currentTime);
      sqlAreaDS.addValue(0, "sql area", currentTime);
      freeMemoryDS.addValue(0, "free memory", currentTime);
      freeMemoryReservedPoolDS.addValue(0, "free memory (reserved pool)", currentTime);
      openCursorsDS.addValue(0, "open cursors", currentTime);
      sessionsDS.addValue(0, "sessions", currentTime);
      reloadsDS.addValue(0, "reloads", currentTime);
      invalidationsDS.addValue(0, "invalidations", currentTime);
    }

    
    iteration = 0;

    // create the chart 
    JFreeChart myChart = ChartFactory.createLineChart("", "", "Library Cache (mb)", libraryCacheDS,PlotOrientation.VERTICAL, true, true, false);
    JFreeChart myChart2 = ChartFactory.createLineChart("", "", "# Open Cursors", openCursorsDS,PlotOrientation.VERTICAL, true, true, false);

    CategoryPlot myPlot = configureChart(myChart,1);
    CategoryPlot myPlot2 = configureChart(myChart2,2);
    myPlot.setBackgroundPaint(Color.WHITE);
    myPlot2.setBackgroundPaint(Color.WHITE);

    libraryCacheAxis = (NumberAxis)myPlot.getRangeAxis();   
    openCursorsAxis = (NumberAxis)myPlot2.getRangeAxis();

    // configure all other axis 
    miscellaneousAxis = new NumberAxis();
    sqlAreaAxis = new NumberAxis();
    freeMemoryAxis = new NumberAxis();
    freeMemoryReservedPoolAxis = new NumberAxis();
    sessionsAxis = new NumberAxis();
    reloadsAxis = new NumberAxis();
    invalidationsAxis = new NumberAxis();

    if (ConsoleWindow.getDBVersion() < 10) {
      setupAxis(1, myPlot, miscellaneousDS, "LEFT", "Miscellanous Pool (mb)",1);
      setupAxis(2, myPlot, sqlAreaDS, "RIGHT", "Sql Area (mb)",1);
      setupAxis(3, myPlot, freeMemoryDS, "RIGHT", "Free Memory (mb) - Shared Pool",1);
      setupAxis(4, myPlot, freeMemoryReservedPoolDS, "RIGHT", "Free Memory (mb) - Reserved Pool",1);
    }
    else {
      setupAxis(1, myPlot, sqlAreaDS, "LEFT", "Sql Area (mb)",1);
      setupAxis(2, myPlot, freeMemoryDS, "RIGHT", "Free Memory (mb) - Shared Pool",1);
      setupAxis(3, myPlot, freeMemoryReservedPoolDS, "RIGHT", "Free Memory (mb) - Reserved Pool",1);      
    }

    setupAxis(1, myPlot2, sessionsDS, "LEFT", "# Sessions",2);
    setupAxis(2, myPlot2, reloadsDS, "RIGHT", "# Reloads",2);
    setupAxis(3, myPlot2, invalidationsDS, "RIGHT", "# Invalidations",2);
    
    JFreeChart[] myCharts = new JFreeChart[2];
    myCharts[0] = myChart;
    myCharts[1] = myChart2;
    
    updateChart(myResult);
    
    return myCharts;
  }
  /**
   * Create and Display a Chart depicting selected attributes about the shared pool
   * 
   * @param myResult
   * @throws Exception
   */
  public JPanel createChart(QueryResult myResult, String instanceName) throws Exception {

    myCharts = makeChart(myResult);
    JFreeChart myChart = myCharts[0];
    JFreeChart myChart2 = myCharts[1];

    JPanel myPanel = new JPanel();
    
    // create the chart frame and display it 
    if (Properties.isBreakOutChartsTabsFrame()) {
      myPanel = new JPanel(new GridLayout(2,1));
      ChartPanel myChartPanel1 = new ChartPanel(myChart);
      ChartPanel myChartPanel2 = new ChartPanel(myChart2);
      myPanel.add(myChartPanel1);
      myPanel.add(myChartPanel2);
      
      ExecuteDisplay.addWindowHolder(myPanel, "Shared Pool - " + instanceName);
    }
    else {
      ChartFrame sharedPoolCF1 = new ChartFrame(ConsoleWindow.getInstanceName() + ": Shared Pool Chart 1",myChart,true);
      ChartFrame sharedPoolCF2 = new ChartFrame(ConsoleWindow.getInstanceName() + ": Shared Pool Chart 2",myChart2,true);
      sharedPoolCF1.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
      sharedPoolCF2.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
      sharedPoolCF1.pack();
      sharedPoolCF2.pack();
      sharedPoolCF1.setVisible(true);
      sharedPoolCF2.setVisible(true);
      
      myPanel.add(sharedPoolCF1);
      myPanel.add(sharedPoolCF2);
    }

    return myPanel;
  }

  public JPanel createChartPanel(QueryResult myResult) throws Exception {

    myCharts = makeChart(myResult);
    JFreeChart myChart = myCharts[0];
    JFreeChart myChart2 = myCharts[1];

    // create the chart frame and display it 
    JPanel myPanel = new JPanel(new GridLayout(2,1));
    ChartPanel myChartPanel1 = new ChartPanel(myChart);
    ChartPanel myChartPanel2 = new ChartPanel(myChart2);
    myPanel.add(myChartPanel1);
    myPanel.add(myChartPanel2);
      
    return myPanel;
  }

  private CategoryPlot configureChart(JFreeChart myChart, int chartNum) {
    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();

    myChart.getLegend().setBorder(0, 0, 0, 0);
    myChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));

    // configure the domain axis (bottom axis) 
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, fontSize));

    // configure the range axis
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    CategoryItemRenderer myRenderer = myPlot.getRenderer(0);
    myRenderer.setSeriesPaint(0, Color.RED);
    if (chartNum == 2) myRenderer.setSeriesPaint(2, Color.LIGHT_GRAY);
    
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setAutoRange(true);
    myAxis.setAutoRangeMinimumSize(10);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);
    
    return myPlot;
  }

  /**
   * Create Axis with names and ranges
   * 
   * @param i
   * @param myPlot
   * @param axisDS
   * @param axisPos
   * @param axisName
   */
  private void setupAxis(int i, CategoryPlot myPlot, 
                         DefaultCategoryDataset axisDS, String axisPos, 
                         String axisName, int chartNum) {
    // add the dataset to the plot 
    myPlot.setDataset(i, axisDS);

    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    if (ConsoleWindow.getDBVersion() < 10) {
      if (chartNum == 1 && i == 1) myAxis = miscellaneousAxis;
      if (chartNum == 1 && i == 2) myAxis = sqlAreaAxis;
      if (chartNum == 1 && i == 3) myAxis = freeMemoryAxis;
      if (chartNum == 1 && i == 4) myAxis = freeMemoryReservedPoolAxis;
    }
    else {
      if (chartNum == 1 && i == 1) myAxis = sqlAreaAxis;
      if (chartNum == 1 && i == 2) myAxis = freeMemoryAxis;
      if (chartNum == 1 && i == 3) myAxis = freeMemoryReservedPoolAxis;      
    }
    if (chartNum == 2 && i == 1) myAxis = sessionsAxis;
    if (chartNum == 2 && i == 2) myAxis = reloadsAxis;
    if (chartNum == 2 && i == 3) myAxis = invalidationsAxis;

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
    if (i == 1) myRenderer.setSeriesPaint(0, Color.BLUE);
    if (i == 2) myRenderer.setSeriesPaint(0, Color.magenta);
    if (i == 3) myRenderer.setSeriesPaint(0, Color.GREEN);
    if (i == 4) myRenderer.setSeriesPaint(0, Color.MAGENTA);

    myPlot.setRenderer(i, myRenderer);

    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    myAxis.setRange(0, 10);
  }

  /**
   * Remove old entries from the result behind the chart
   */
  public void removeOldEntries() {
    try {
      List colList = libraryCacheDS.getColumnKeys();
      Object[] colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        libraryCacheDS.removeColumn(colObj[i].toString());
      }      
      
      colList = miscellaneousDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        miscellaneousDS.removeColumn(colObj[i].toString());
      }
      
      colList = sqlAreaDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        sqlAreaDS.removeColumn(colObj[i].toString());
      }
      
      colList = freeMemoryDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        freeMemoryDS.removeColumn(colObj[i].toString());
      }

    
      colList = freeMemoryReservedPoolDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        freeMemoryReservedPoolDS.removeColumn(colObj[i].toString());
      }

      colList = sessionsDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        sessionsDS.removeColumn(colObj[i].toString());
      }

      colList = openCursorsDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        openCursorsDS.removeColumn(colObj[i].toString());
      }

      colList = reloadsDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        reloadsDS.removeColumn(colObj[i].toString());
      }      
      
      colList = invalidationsDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length - offSet; i++) {
        invalidationsDS.removeColumn(colObj[i].toString());
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }


  /**
   * Update the resultset behind the chart with new values
   * 
   * @param myResult
   */
  public void updateChart(QueryResult myResult) {
    try {
      String[][] resultSet = myResult.getResultSetAsStringArray();

      for (int i = 0; i < resultSet.length; i++) {

        if (resultSet[i][0].equals("library cache")) {
          libraryCacheDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "library cache", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("miscellaneous")) {
          miscellaneousDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "miscellaneous", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("sql area")) {
          sqlAreaDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "sql area", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("free memory")) {
          freeMemoryDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "free memory", myResult.getExecutionClockTime());
        }
        
        if (resultSet[i][0].equals("free memory (reserved pool)")) {
          freeMemoryReservedPoolDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "free memory (reserved pool)", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("open cursors")) {
          openCursorsDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "open cursors", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("sessions")) {
          sessionsDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), "sessions", myResult.getExecutionClockTime());
        }

        if (resultSet[i][0].equals("reloads")) {
          double diff = Double.valueOf(resultSet[i][1]).doubleValue() - reloadEvents;
          if (iteration > 0) {
            reloadsDS.addValue(diff, resultSet[i][0], myResult.getExecutionClockTime());
          }
          else {
            reloadsDS.addValue(0, resultSet[i][0], myResult.getExecutionClockTime());
          }
          
          if (iteration > 0) {
            reloadEvents = Double.valueOf(resultSet[i][1]).doubleValue();
          }
          else {
            reloadEvents = Double.valueOf(resultSet[i][1]).doubleValue();
          }
        }

        if (resultSet[i][0].equals("invalidations")) {
          double diff = Double.valueOf(resultSet[i][1]).doubleValue() - invalidationEvents;
          if (iteration > 0) {
            invalidationsDS.addValue(diff, resultSet[i][0], myResult.getExecutionClockTime());
          }
          else {
            invalidationsDS.addValue(0, resultSet[i][0], myResult.getExecutionClockTime());            
          }
          
          if (iteration > 0) {
            invalidationEvents = Double.valueOf(resultSet[i][1]).doubleValue();
          }
          else {
            invalidationEvents = Double.valueOf(resultSet[i][1]).doubleValue();
          }
        }

      }
      try {
        removeOldEntries();
        updateAxis();
      }
      catch (Exception e) {
        System.out.println("error updating shared pool chart: " + e);
        e.printStackTrace();
      }
      iteration++;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }

  /**
   * Update the axis range to reflect the dataset
   */
  private void updateAxis() {
    List colList = libraryCacheDS.getColumnKeys();
    Object[] colObj = colList.toArray();

    int maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(libraryCacheDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("libraryCacheDS maxValue=" + maxValue);
    libraryCacheAxis.setRange(0, Math.max(10.0, maxValue * 1.35));
    
    colList = miscellaneousDS.getColumnKeys();
    colObj = colList.toArray();

    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(miscellaneousDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("miscellaneousDS maxValue=" + maxValue);
    miscellaneousAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
    
    colList = sqlAreaDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(sqlAreaDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("sqlAreaDS maxValue=" + maxValue);
    sqlAreaAxis.setRange(0, Math.max(10.0, maxValue * 1.15));
    
    colList = freeMemoryDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(freeMemoryDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("freeMemoryDS maxValue=" + maxValue);
    freeMemoryAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
     
    colList = freeMemoryReservedPoolDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(freeMemoryReservedPoolDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("freeMemoryReservedPoolDS maxValue=" + maxValue);
    freeMemoryReservedPoolAxis.setRange(0, Math.max(10.0, maxValue * 1.2));
     
    colList = sessionsDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(sessionsDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("sessionsDS maxValue=" + maxValue);
    sessionsAxis.setRange(0, Math.max(10.0, maxValue * 1.2));
     
    colList = reloadsDS.getColumnKeys();
    colObj = colList.toArray();
       
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(reloadsDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("reloadsDS maxValue=" + maxValue);
    reloadsAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
     
    colList = invalidationsDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(invalidationsDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("invalidationsDS maxValue=" + maxValue);
    invalidationsAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
     
    colList = openCursorsDS.getColumnKeys();
    colObj = colList.toArray();
    
    maxValue = 0;
    
    for (int i=0; i < colObj.length; i++) {
      maxValue = Math.max(maxValue,Integer.valueOf(openCursorsDS.getValue(0,i).intValue()));
    }    
    if (debug) System.out.println("openCursorsDS maxValue=" + maxValue);
    openCursorsAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
    
  }
  
  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
  }

  public void resetOffSet() {
    if (this.offSet != defaultOffSet) setOffSet(defaultOffSet);
  }
  
  public void setChartTitle(String title) {
    myCharts[0].setTitle(new TextTitle(title));
  }
  
  public boolean isChartTitleSet() {
    if (myCharts[0].getTitle().getText() instanceof String & myCharts[0].getTitle().getText().length() > 0) {
      return true;
    }
    else {
      return false;
    }
  }
  
}
