/*
 * OsLoadChart.java        13.06 14/10/05
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
 * 28/02/06 Richard Wright Made all the axis except load have a maximum value of
 *                         100 times the iteration time
 * 10/03/06 Richard Wright Modified so that the chart can appear in another frame
 * 22/08/06 Richard Wright Modified the comment style and error handling 
 * 11/03/07 Richard Wright Set chart background White
 * 20/03/07 Richard Wright Setup the domain axis to show timestamps
 * 20/03/07 Richard Wright Modified the font size of axis labels
 * 05/04/07 Richard Wright Load axis not showing an appropriate range
 * 10/04/07 Richard Wright Modified for the new chart panel
 * 04/05/07 Richard Wright Correct Frame size when broken out
 * 14/05/07 Richard Wright Removed unused code
 * 03/12/07 Richard Wright Enhanced for RAC
 * 24/04/08 Richard Wright Set series paint to allow upgrade of jFreeChart
 * 10/06/08 Richard Wright Modified colors to ensure each dataset is unique
 * 07/12/09 Richard Wright Correctly set the font size of the time axis labels
 * 05/01/11 Richard Wright Enhanced to work with 10.1
 * 22/09/11 Richard Wright Removed Playback functionality
 * 04/01/12 Richard Wright Re-wrote some code to ensure the axis update properly
 * 29/09/15 Richard Wright re-instating the chart break out function
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
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
public class OsLoadChart {

  private DefaultCategoryDataset timeDS;
  private DefaultCategoryDataset loadDS;
  private NumberAxis timeAxis;
  private NumberAxis loadAxis;
  private float userTimeEvents;
  private float systemTimeEvents;
  private float busyTimeEvents;
  private float idleTimeEvents;
  private float waitIOEvents;
  private float timeEvents;
  private int timeCounter = 0;
  private float loadEvents;
  private int loadCounter;
  private int chartIteration = 0;
  private int offSet = 64;
  private int defaultOffSet = 64;
  
  JFreeChart myChart;
  
  boolean debug = false;

  /**
   * Constructor
   */
  public OsLoadChart() {
  }

  /**
   * Create the chart and display it
   *
   * @param myResult 
   * @throws Exception
   */
   public void createChart(QueryResult myResult, String instanceName) throws Exception {
     myChart = makeChart(myResult);

     // create the chart frame and display it 
      if (Properties.isBreakOutChartsTabsFrame()) {
        ChartPanel myChartPanel = new ChartPanel(myChart);
        ExecuteDisplay.addWindowHolder(myChartPanel,"OS Load Chart - " + instanceName);
      }
      else {
        ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": OS Load Chart",myChart,true);
        loadCF.setSize(Properties.getAdditionalWindowWidth(),Properties.getAdditionalWindowHeight());
        loadCF.pack();
        loadCF.setVisible(true);
      }
   }  
   
   public ChartPanel createChartPanel(QueryResult myResult) throws Exception {
    myChart = makeChart(myResult);
    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }

  private JFreeChart makeChart(QueryResult myResult) {
    timeDS = new DefaultCategoryDataset();
    loadDS = new DefaultCategoryDataset();

    // Get the current time 
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's 
    for (int i=0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + 
          (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      if (ConsoleWindow.getDBVersion() >= 10.2) {
        timeDS.addValue(0, "cpu user time (cs)", currentTime);
        timeDS.addValue(0, "cpu system time (cs)", currentTime);
        timeDS.addValue(0, "cpu busy time (cs)", currentTime);
        timeDS.addValue(0, "cpu idle time (cs)", currentTime);
        timeDS.addValue(0, "cpu wait on io time (cs)", currentTime);
        loadDS.addValue(0, "load", currentTime);
      }
      else {
        timeDS.addValue(0, "user_ticks", currentTime);
        timeDS.addValue(0, "sys_ticks", currentTime);
        timeDS.addValue(0, "idle_ticks", currentTime);
        timeDS.addValue(0, "io_wait_ticks", currentTime);
        timeDS.addValue(0, "busy_ticks", currentTime);
      }
    }
    
    userTimeEvents = 0;
    systemTimeEvents = 0;
    busyTimeEvents = 0;
    idleTimeEvents = 0;
    timeEvents = 0;
    loadEvents = 0;
    waitIOEvents = 0;

    // create the chart 
    JFreeChart osLoadChart = ChartFactory.createLineChart(
                                "",
                                "",
                                "time (centi seconds)",
                                timeDS,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false
                              );

    osLoadChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = osLoadChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    osLoadChart.getLegend().setBorder(0,0,0,0);
    
    // setup the domain axis (bottom axis) 
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, fontSize));

    // configure the time axis 
    timeAxis = (NumberAxis)myPlot.getRangeAxis();
    CategoryItemRenderer myRenderer = myPlot.getRenderer(0);
    timeAxis.setAutoRangeMinimumSize(20);
    timeAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    timeAxis.setStandardTickUnits(integerTicks);
    timeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    timeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    

    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    myRenderer.setSeriesShape(1,new Rectangle(0,0));
    myRenderer.setSeriesShape(2,new Rectangle(0,0));
    myRenderer.setSeriesShape(3,new Rectangle(0,0));
    myRenderer.setSeriesPaint(0,Color.RED);
    myRenderer.setSeriesPaint(1,Color.BLUE);
    myRenderer.setSeriesPaint(2,Color.GREEN);
    myRenderer.setSeriesPaint(3,Color.PINK);
    myRenderer.setSeriesPaint(4,Color.MAGENTA);
    

    
    // configure all other axis 
    loadAxis = new NumberAxis();
    setupAxis(1,myPlot,loadDS,"RIGHT","load");
    
    updateChart(myResult);    
    
    return osLoadChart;
  }

  /**
   * Add new axis to the chart and set the axis scale and colour
   * @param i - index of the axis
   * @param myPlot - plot
   * @param axisDS - dataset for the axis
   * @param axisPos - position of the axis
   * @param axisName - name of the axis
   */
  private void setupAxis(int i,CategoryPlot myPlot,DefaultCategoryDataset axisDS,String axisPos,String axisName) {
    // add the dataset to the plot 
    myPlot.setDataset(i,axisDS);

    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    myAxis = loadAxis;


    myAxis.setLabel(axisName);

    // Make the axis increment in integers 
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);

    myPlot.setRangeAxis(i,myAxis);
    if (axisPos.equals("RIGHT")) {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_RIGHT);
    }
    else {
      myPlot.setRangeAxisLocation(i,AxisLocation.BOTTOM_OR_LEFT);
    }
    myPlot.mapDatasetToRangeAxis(i,i);

    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    if (i==1) {
      myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
      myAxis.setLabelPaint(Color.LIGHT_GRAY);
      myAxis.setTickLabelPaint(Color.LIGHT_GRAY);
      myAxis.setAxisLinePaint(Color.LIGHT_GRAY);
    }

    
    myPlot.setRenderer(i,myRenderer);

    myAxis.setAutoRange(true);
    
    // set axis color scheme 
    myAxis.setLabelFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set the axis boundaries 
    myAxis.setRange(0,1);
  }

  /**
   * Add a new result set to the chart
   *
   * @param myResult 
   */
  public void updateChart(QueryResult myResult) {
    try {
      String[][] resultSet = myResult.getResultSetAsStringArray();

      boolean time = false;
      boolean load = false;
      
      for (int i = 0; i < resultSet.length; i++) {

        if (resultSet[i][0].equals("user_time") || resultSet[i][0].equals("user_ticks")) {
          float v;
          if (chartIteration > 1) {
            v = Float.valueOf(resultSet[i][1]).floatValue() - userTimeEvents;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu user time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"user_ticks",myResult.getExecutionClockTime());

            }
          }
          else {
            v = 0;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu user time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"user_ticks",myResult.getExecutionClockTime());

            }       
          }
          
          userTimeEvents = Float.valueOf(resultSet[i][1]).floatValue();
          timeEvents = Math.max(v,timeEvents);
          time = true;
        }

        if (resultSet[i][0].equals("system_time") || resultSet[i][0].equals("sys_time") || resultSet[i][0].equals("sys_ticks")) {
          float v; 
          if (chartIteration > 1) {
            v = Float.valueOf(resultSet[i][1]).floatValue() - systemTimeEvents;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v, "cpu system time (cs)", myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v, "sys_ticks", myResult.getExecutionClockTime());
            }
          }
          else {
            v = 0;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v, "cpu system time (cs)", myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v, "sys_ticks", myResult.getExecutionClockTime());
            }           
          }
          
          systemTimeEvents = Float.valueOf(resultSet[i][1]).floatValue();
          timeEvents = Math.max(v,timeEvents);
          time = true;
        }

        if (resultSet[i][0].equals("idle_time") || resultSet[i][0].equals("idle_ticks")) {
          float v;
          if (chartIteration > 0) {
            v = Float.valueOf(resultSet[i][1]).floatValue() - idleTimeEvents;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu idle time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"idle_ticks",myResult.getExecutionClockTime());
            }
          }
          else {
            v = 0;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu idle time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"idle_ticks",myResult.getExecutionClockTime());
            }          
          }
          
          idleTimeEvents = Float.valueOf(resultSet[i][1]).floatValue();
          timeEvents = Math.max(v,timeEvents);
          time = true;
        }

        if (resultSet[i][0].equals("iowait_time") || resultSet[i][0].equals("io_wait_ticks")) {
          float v; 
          if (chartIteration > 0) {
            v = Float.valueOf(resultSet[i][1]).floatValue() - waitIOEvents;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu wait on io time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"io_wait_ticks",myResult.getExecutionClockTime());
            }
          }
          else {
            v = 0;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu wait on io time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"io_wait_ticks",myResult.getExecutionClockTime());
            }       
          }
          
          waitIOEvents = Float.valueOf(resultSet[i][1]).floatValue();
          timeEvents = Math.max(v,timeEvents);
          time = true;
        }

        if (resultSet[i][0].equals("busy_time") || resultSet[i][0].equals("busy_ticks")) {
          float v; 
          if (chartIteration > 0) {
            v = Float.valueOf(resultSet[i][1]).floatValue() - busyTimeEvents;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu busy time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"busy_ticks",myResult.getExecutionClockTime());
            }
          }
          else {
            v = 0;
            if (ConsoleWindow.getDBVersion() >= 10.2) {
              timeDS.addValue(v,"cpu busy time (cs)",myResult.getExecutionClockTime());
            }
            else {
              timeDS.addValue(v,"busy_ticks",myResult.getExecutionClockTime());
            }
          }
          
          busyTimeEvents = Float.valueOf(resultSet[i][1]).floatValue();
          timeEvents = Math.max(v,timeEvents);
          time = true;
        }

        if (resultSet[i][0].equals("load")) {
          float v = Float.valueOf(resultSet[i][1]).floatValue();
          if (chartIteration > 0) {
            loadDS.addValue(v,"load",myResult.getExecutionClockTime());
          }
          else {
            loadDS.addValue(0,"load",myResult.getExecutionClockTime());
          }
          
          loadEvents = Float.valueOf(resultSet[i][1]).floatValue();
          if (loadCounter >= offSet -1) loadCounter = 0;
          loadEvents = v;
          load = true;
        }
      }
     
      if (!load && ConsoleWindow.getDBVersion() >= 10.2) {
        loadDS.addValue(0,"load",myResult.getExecutionClockTime());
        loadEvents = 0;
        loadEvents = 0;
        if (loadCounter == offSet -1) loadCounter = 0;
      }
      
      time=false;
      load=false;
      
      if (timeCounter >= offSet -1) timeCounter = 0;
      timeCounter++;
      
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
      List colList = timeDS.getColumnKeys();
      Object[] colObj = colList.toArray();

      int maxValue = 0;
        
      if (debug) System.out.println("colList.length " + colObj.length + " and offSet is " + offSet);
      
      for (int i=0; i < colObj.length -1; i++) {
        if (debug) System.out.println("timeDS 0 " + i + " " + timeDS.getValue(0,i));
        if (debug) System.out.println("timeDS 1 " + i + " " + timeDS.getValue(0,i));
        if (debug) System.out.println("timeDS 2 " + i + " " + timeDS.getValue(0,i));
        if (debug) System.out.println("timeDS 3 " + i + " " + timeDS.getValue(0,i));
        if (debug) System.out.println("timeDS 4 " + i + " " + timeDS.getValue(0,i));
        
        /*
         * This try catch clause works around an unexplained bug, for some reason the timeDS is 
         * getting smaller with each iteration causing null pointer exceptions
         */
        try {
          maxValue = Math.max(maxValue, Integer.valueOf(timeDS.getValue(0, i).intValue()));
          maxValue = Math.max(maxValue, Integer.valueOf(timeDS.getValue(1, i).intValue()));
          maxValue = Math.max(maxValue, Integer.valueOf(timeDS.getValue(2, i).intValue()));
          maxValue = Math.max(maxValue, Integer.valueOf(timeDS.getValue(3, i).intValue()));
          maxValue = Math.max(maxValue, Integer.valueOf(timeDS.getValue(4, i).intValue()));
        } catch (Exception e) {
        }
      }    
      if (debug) System.out.println("timeDS maxValue=" + maxValue);
      timeAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
        

      
      maxValue = 0;
      
      for(int i=0; i < colObj.length; i++) {
        maxValue= Math.max(maxValue,Integer.valueOf(loadDS.getValue(0,i).intValue()));
      }    
      if (debug) System.out.println("loadDS maxValue=" + maxValue);
      loadAxis.setRange(0, Math.max(10.0, maxValue * 1.3));
      
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
      List colList = timeDS.getColumnKeys();
      Object[] colObj = colList.toArray();
      if (debug) System.out.println("Number of OS Load Entries in Chart prior to removing old entries is: " + colObj.length + " and offSet is " + offSet);

      for (int i = 0; i < colObj.length -offSet; i++) {
        timeDS.removeColumn(colObj[i].toString());
      }      
      
      colList = loadDS.getColumnKeys();
      colObj = colList.toArray();

      for (int i = 0; i < colObj.length -offSet; i++) {
        loadDS.removeColumn(colObj[i].toString());
      }
      
      if (debug) {
        colList = timeDS.getColumnKeys();
        colObj = colList.toArray();
        System.out.println("Number of OS Load Entries in Chart after removing old entries is: " + colObj.length + " and offSet is " + offSet);
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