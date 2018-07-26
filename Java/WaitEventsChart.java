/*
 * WaitEventsChart.java        13.0 05/01/04
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
 * 
 * 23/05/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 09/06/05 Richard Wright Upgraded to JFreeChart 1.0.0 
 * 09/06/05 Richard Wright Added LogWriter Output
 * 17/06/05 Richard Wright Modified updateChart to add a blank entry is no wait 
 *                         event was found in this iteration and take the current 
 *                         iteration into account when calculating the axis range.  
 *                         Also Re-Wrote the code which populates the chart 
 *                         dataset to stop it displaying incorrectly.
 * 10/03/06 Richard Wright Modified so that the chart can appear in another frame
 * 08/06/06 Richard Wright Removed reference to waitEventsRenderer.setMaxBarWidth 
 *                         as this is not supported in JFreeChart 1.0.1
 * 28/07/06 Richard Wright Added time labels to the catagory axis
 * 02/08/06 Richard Wright Made the offset default to 75 rather than 50
 * 03/08/06 Richard Wright Implemented config parameters for domain labels
 * 17/08/09 Richard Wright Made offSet 60 and fixed bug in removing old entries
 * 14/09/06 Richard Wright Modified the comment style and error handling
 * 11/03/07 Richard Wright Set chart background white
 * 13/03/07 Richard Wright Re-written from a copy of the physical io chart so that 
 *                         it works error free from JFreeChart 1.0.4
 * 16/03/07 Richard Wright Set DatasetRenderingOrder to forward else session lines 
 *                         are behind the bar chart
 * 19/03/07 Richard Wright Made the offset default to 65 rather than 75
 * 20/03/07 Richard Wright Set the range axis to be black not red
 * 05/04/07 Richard Wright Chart did not appear in correctly sized from when not in the tabbed frame 
 * 10/04/07 Richard Wright Modified for the new chart panel
 * 23/11/07 Richard Wright Enhanced for RAC
 * 29/11/07 Richard Wright I now remove old entries then add new ones rather
 *                         rather than the other way around.  This seems to 
 *                         reduce the frequency of concurrent modification errors
 * 23/06/07 Richard Wright Session overlay is now permanent and cannot be turned off
 * 20/01/09 Richard Wright Now uses 'read by other session' rather than 'buffer busy waits' on 10g and above
 * 15/05/09 Richard Wright Now uses 'enq: TM Contention' rather then 'enqueue' on 10g and above
 * 30/05/09 Richard Wright Changed to take account of the user specified wait events in initialization
 * 03/12/09 Richard Wright Fixed null pointer exception
 * 10/09/15 Richard Wright Minor code tidy up
 * 29/09/15 Richard Wright re-instating the chart break out function
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
public class WaitEventsChart {

  private DefaultCategoryDataset waitEventsDS;
  private NumberAxis waitEventsAxis;
  private int[] waitEventsMaxEvents = new int[150];
  private int waitEventsMaxEventsCounter = 0;
  private int chartIteration = 0;
  
  private DefaultCategoryDataset sessionsDS;
  private LogarithmicAxis sessionsAxis;
  private int maxSessions = 0;
  private CategoryItemRenderer sessionRenderer;
  private int offSet = 64;
  private int defaultOffSet = 64;
  private String currentExecutionTime;
  private JFreeChart myChart;
  
  
  boolean debug = false;

  /**
   * Constructor
   */
  public WaitEventsChart() {
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
      ExecuteDisplay.addWindowHolder(myChartPanel, "Wait Events - " + instanceName);
    } else {
      ChartFrame waitEventsCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": Wait Events Chart", myChart, true);
      if (maxWidth) {
        // check the screen width
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        // set the frame width
        Dimension currentDimension = waitEventsCF.getSize();
        int newWidth = Math.min(screenWidth, currentDimension.width * 2);
        currentDimension.setSize(newWidth, currentDimension.height);
        waitEventsCF.setSize(currentDimension);

      } else {
        waitEventsCF.setSize(Properties.getAdditionalWindowWidth(), Properties.getAdditionalWindowHeight());
      }
      waitEventsCF.setVisible(true);
    }

    // display the number of sessions against a seperate axis 
//    if (ConsoleWindow.waitSummarySessions.isSelected()) {
        createSessionLine(myChart, instanceId);
//    }

    /*
     * remove old values 
     * this is required when the frame is restarted
     */
    removeOldWaitEventEntries();
    removeOldSessionEntries();
  }
                          
  /**
   * Create the chart and display it
   * 
   * @param myResult - the QueryResult use to populate first iteration of the chart
   * @param iterationCounter - number of iterations
   * @throws Exception
   */
   public ChartPanel createChartPanel(QueryResult myResult, boolean initialize, String instanceId) throws Exception {

     myChart = makeChart(myResult, initialize);

     // create the chart frame and display it 
     ChartPanel myChartPanel = new ChartPanel(myChart);
     
     // display the number of sessions against a seperate axis 
     if (instanceId instanceof String) createSessionLine(myChart, instanceId);

     /*
      * remove old values 
      * this is required when the frame is restarted
      */
     removeOldWaitEventEntries();
     if (instanceId instanceof String) removeOldSessionEntries();
     
     return myChartPanel;
   }    

  private JFreeChart makeChart(QueryResult myResult, boolean initialize) {
    waitEventsDS = new DefaultCategoryDataset();

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
  
        for (int j = 0; j < defaultEvents.length; j++) {
          event = defaultEvents[j];
          
          if (ConsoleWindow.getDBVersion() < 10) { 
            if (event.equals("read by other session")) event = "buffer busy waits";
            if (event.equals("enq: TM - contention")) event = "enqueue";                                         
          }
          else {
            if (event.equals("buffer busy waits")) event = "read by other session";
            if (event.equals("enqueue")) event = "enq: TM - contention";
          }
          
          waitEventsDS.addValue(0,event,currentTime);
        }
        
        if (debug) System.out.println("WE: init dataset i + space + currentTime: " + i + "  " + currentTime);
      }
    }
    
    if (debug) {
      System.out.println("WE: post dataset init waitEventsDS.getColumnCount() : " + waitEventsDS.getColumnCount());
    }

    // create the chart 
    JFreeChart myWaitChart = ChartFactory.createStackedBarChart3D(
                                "",
                                "",
                                "# Sessions waiting on EVENT",
                                waitEventsDS,
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
    waitEventsAxis = (NumberAxis)myPlot.getRangeAxis();
    waitEventsAxis.setLowerBound(0);
    waitEventsAxis.setAutoRangeMinimumSize(20);
    waitEventsAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    waitEventsAxis.setStandardTickUnits(integerTicks);
    waitEventsAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    waitEventsAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
      removeOldWaitEventEntries();
      int sumNumEvents = 0;
      
      currentExecutionTime = myResult.getExecutionClockTime();
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      for (int i=0; i < myResult.getNumRows(); i++) {
        int numEvents = Integer.valueOf(resultSet[i][1]).intValue();
        waitEventsDS.addValue(numEvents , resultSet[i][0], currentExecutionTime);
        sumNumEvents = sumNumEvents + numEvents;
        //if (debug) System.out.println("WE: added entry : " + resultSet[i][0] + currentExecutionTime);
      }
      
      // if there are no wait events to report add a blank entry 
      if (myResult.getNumRows() == 0) addBlankEntry(currentExecutionTime);
      
      if (debug) System.out.println("WE: number of entry in dataset : " + waitEventsDS.getColumnCount());
      
      // calculate the maximum number of events for the wait event range axis 
      int maxNumEvents = 0;
      for (int j=0; j < offSet; j++) {
        maxNumEvents = Math.max(maxNumEvents,waitEventsMaxEvents[j]);
      }
      
      maxNumEvents = Math.max(maxNumEvents,sumNumEvents);
      
      // set the wait event range axis upper boundary 
      if (waitEventsAxis.getUpperBound() != Math.max(10.0,maxNumEvents +1)) waitEventsAxis.setRange(0,Math.max(10.0,maxNumEvents +1));
      
      chartIteration++;
      
      // store away the numEvents to enable range axis calculations 
      waitEventsMaxEvents[waitEventsMaxEventsCounter++] = sumNumEvents;
      if (waitEventsMaxEventsCounter >= offSet -1) waitEventsMaxEventsCounter = 0;
      
//      removeOldWaitEventEntries();
    } catch (Exception e) {
        ConsoleWindow.displayError(e, this);
    }
  }

  public void createSessionLine(JFreeChart waitEventsChart, String instanceId) throws Exception {
    CategoryPlot myPlot = waitEventsChart.getCategoryPlot();
    sessionsDS = new DefaultCategoryDataset();
    myPlot.setDataset(1,sessionsDS);
    myPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

    // create the second axis 
    sessionsAxis = new LogarithmicAxis("# Sessions");
    sessionsAxis.setLabelFont(new Font("SansSerif",     Font.PLAIN, Properties.getAxisLabelFontSize()));
    sessionsAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    myPlot.setRangeAxis(1,sessionsAxis);
    myPlot.setRangeAxisLocation(1,AxisLocation.BOTTOM_OR_RIGHT);
    myPlot.mapDatasetToRangeAxis(1,1);        
    sessionRenderer = new LineAndShapeRenderer();
    sessionRenderer.setSeriesShape(0,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(0,Color.GRAY);
    sessionRenderer.setSeriesShape(1,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(1,Color.YELLOW);
    sessionRenderer.setSeriesShape(2,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(2,Color.BLUE);
    sessionRenderer.setSeriesShape(3,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(3,Color.RED);
    sessionRenderer.setSeriesShape(4,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(4,Color.GREEN);
    sessionRenderer.setSeriesShape(5,new Rectangle(0,0));
    sessionRenderer.setSeriesPaint(5,Color.BLACK);
    myPlot.setRenderer(1,sessionRenderer);

    // get the number of sessions 
    updateSessionLine(instanceId);
  }

  public void updateSessionLine(String instanceId) throws Exception {
    try {
      removeOldSessionEntries();

      // get the number of sessions 
      Parameters myPars = new Parameters();
      myPars.addParameter("int",instanceId);
      myPars.addParameter("int",instanceId);
      
      QueryResult sessionResult = ExecuteDisplay.execute("sessionSummary2.sql", myPars, false, false, null);

      /* initialize dataset */ 
      if (chartIteration == 1) {
        List eventsColList = waitEventsDS.getColumnKeys();
        Object[] eventsColObj = eventsColList.toArray();   
       
       /*
        * by putting in a fake data series, and ensuring it not set visible, it 
        * ensures the other session lines appear from the right, just like the bars do
        */
        for (int j=0; j < eventsColObj.length -1; j++) {
          sessionsDS.addValue(1,"Fake",eventsColObj[j].toString());
          if (debug) System.out.println("SE: init dataset : " + eventsColObj[j].toString());
        }
        sessionRenderer.setSeriesVisible(0,new Boolean("FALSE"));
      } 
      if (chartIteration == 1 && debug) System.out.println("SE: number of entry in dataset after init: " + sessionsDS.getColumnCount() + " : " + sessionsDS.getRowCount());
      
      // retrieve data row at a time and put it in the dataset 
      int numSessions = 0;
      for (int r=0; r < sessionResult.getNumRows(); r++) {
        Vector resultSetRow = sessionResult.getResultSetRow(r);
      
        String sessionType = "";
        numSessions = 0;
        for (int c=0; c < sessionResult.getNumCols(); c++) {
          Object result = resultSetRow.elementAt(c);
        
          if (c==0) sessionType = String.valueOf(result);
          if (c==1) numSessions = Integer.valueOf(String.valueOf(result)).intValue();
          
          if (sessionType.equals("ACTIVE")) sessionType = "Active Sessions";
          if (sessionType.equals("INACTIVE")) sessionType = "Inactive Sessions";
        }
                  
        // add to the dataset 
        if (!sessionType.equals("RMan Processes")) sessionsDS.addValue(numSessions,sessionType,currentExecutionTime);
        if (sessionType.equals("RMan Processes") && numSessions > 0) sessionsDS.addValue(numSessions,sessionType,currentExecutionTime);
       // if (debug) System.out.println("SE: adding entry : " + currentExecutionTime);

        // set session axis upper bound 
        maxSessions = Math.max(maxSessions,numSessions);
      }
      if (debug & sessionResult.getNumRows() > 0) System.out.println("SE: added entry to dataset : " + currentExecutionTime);
      if (debug) System.out.println("SE: number of entry in dataset : " + sessionsDS.getColumnCount());
      
      sessionsAxis.setLowerBound(0);
      sessionsAxis.setTickLabelsVisible(true);
      sessionsAxis.setLog10TickLabelsFlag(false);
      sessionsAxis.setRange(0,Math.max(10,maxSessions + Math.max(10,maxSessions * 10))); 
      
      //removeOldSessionEntries();
    }
    catch (Exception e) {
     throw e;
    }
  }

  /**
   * Remove old entries from the chart dataset.  The chart only displays the last n iterations of data.
   */
   public void removeOldWaitEventEntries() {
     try {
       boolean debug=false;
       List colList = waitEventsDS.getColumnKeys();
       
       Object[] colObj = colList.toArray();

       for(int i=0; i < colObj.length - offSet; i++) {
         if (debug) {
           System.out.println("WE: removing colObj[i].toString() : " + colObj[i].toString());
           System.out.println("WE: removing ");
         }

         waitEventsDS.removeColumn(colObj[i].toString());
         
       }         
     }
     catch (Exception e) {
       ConsoleWindow.displayError(e,this);
     }
   }  
  
  public int removeRecentEntries() {
    int numEntriesRemoved = 0;
    
    try {
        List colList = waitEventsDS.getColumnKeys();
        Object[] colObj = colList.toArray();
        if (debug) System.out.println("Number of Wait Events Entries in Chart prior to removing recent entries is: " + colObj.length);

        for(int i=colObj.length -1; i > offSet; i--) {
          waitEventsDS.removeColumn(colObj[i].toString());
          numEntriesRemoved++;
        }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    
    return numEntriesRemoved;
  }
  
  public void removeOldSessionEntries() {
    try {
      
//      if (ConsoleWindow.waitSummarySessions.isSelected()) {
        List colList = sessionsDS.getColumnKeys();
        Object[] colObj = colList.toArray();
          
        if (debug) System.out.println("SE:  colObj.length - offSet     : " + (colObj.length - offSet));

        for(int i=0; i < colObj.length - offSet; i++) {
          sessionsDS.removeColumn(colObj[i].toString());
          if (debug) System.out.println("SE:  removed colObj[i].toString() : " + colObj[i].toString());
        }
//      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  public void addBlankEntry(String executionDateTime) {
    waitEventsDS.addValue(0,"db file parallel write",executionDateTime);
    if (debug) System.out.println("WE: added blank entry: " + executionDateTime);
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
