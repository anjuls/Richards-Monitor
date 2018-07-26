 /*
  * ParseChart.java        13.24 14/07/06
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
  * 03/08/06 Richard Wright Added time labels to the catagory axis
  * 03/08/06 Richard Wright Made the offset default to 75 rather than 50
  * 03/08/06 Richard Wright Implemented config parameters for domain labels
  * 23/08/06 Richard Wright Modified the comment style and error handling
  * 11/03/07 Richard Wright Set chart background White
  * 20/03/07 Richard Wright Modifed removeOldEntries to use offSet not 50
  * 20/03/07 Richard Wright Set the range axis to be black not red
  * 10/04/07 Richard Wright Modified for the new chart panel
  * 15/05/07 Richard Wright Removed unused code
  * 03/12/07 Richard Wright Enhanced for RAC
  * 06/11/09 Richard Wright makeChart now calls updateChart
  * 22/09/11 Richard Wright Removed Playback functionality
  * 06/03/12 Richard Wright Fixing false spikes in activity and improving the code
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
public class ParseChart {

  private DefaultCategoryDataset parseDS;

  private NumberAxis myAxis;

  private float[] parseEvents;
  private int parseCounter;

  private int chartIteration = 0;
  private float valueOfLastParseCountHard = 0;
  private float valueOfLastParseCountTotal = 0;
  private float valueOfLastParseCountFailure = 0;
  private int offSet = 75;          // number of columns displayed on chart
  private int defaultOffSet = 75;

  JFreeChart myChart;

  boolean debug = false;


  /**
   * Constructor
   */
  public ParseChart() {
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
       ExecuteDisplay.addWindowHolder(myChartPanel, "Parse - " + instanceName);
     }
     else {
       ChartFrame loadCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": Parse Chart", myChart, true);
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
    parseDS = new DefaultCategoryDataset();

    // Get the current time
    Date today;
    DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    String currentTime;

    // initialize the dataset's
    for (int i = 0 - offSet; i < 0; i++) {
      today = new Date();
      today.setTime(System.currentTimeMillis() - (offSet * 100 * TextViewPanel.getIterationSleepS()) + (1000 * i * TextViewPanel.getIterationSleepS()));
      currentTime = dateFormatter.format(today);

      parseDS.addValue(0, "parse count (hard)", currentTime);
      parseDS.addValue(0, "parse count (total)", currentTime);
      parseDS.addValue(0, "parse count (failures)", currentTime);
    }

    parseEvents = new float[offSet];

    chartIteration = 0;

    String[][] resultSet = myResult.getResultSetAsStringArray();

    // create the chart
    JFreeChart myChart = ChartFactory.createLineChart("", "","# parses", parseDS ,PlotOrientation.VERTICAL, true, true, false);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);

    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // do not display the domain axis (bottom axis)
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(false);
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,fontSize));

    myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLowerBound(0);
    myAxis.setAutoRangeMinimumSize(20);
    myAxis.setLowerBound(0);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);
    myAxis.setLabelFont(new Font("SansSerif",     Font.PLAIN, Properties.getAxisLabelFontSize()));
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

      for (int i = 0; i < resultSet.length; i++) {

        if (resultSet[i][0].equals("parse count (hard)")) {
          if (chartIteration > 0) v1 = Float.valueOf(resultSet[i][1]).floatValue() - valueOfLastParseCountHard;

          parseDS.addValue(v1, "parse count (hard)", myResult.getExecutionClockTime());
          valueOfLastParseCountHard = Float.valueOf(resultSet[i][1]).floatValue();
        }

        if (resultSet[i][0].equals("parse count (total)")) {
          if (chartIteration > 0) v2 = Float.valueOf(resultSet[i][1]).floatValue() - valueOfLastParseCountTotal;

          parseDS.addValue(v2, "parse count (total)", myResult.getExecutionClockTime());
          valueOfLastParseCountTotal = Float.valueOf(resultSet[i][1]).floatValue();
        }

        if (resultSet[i][0].equals("parse count (failures)")) {
          if (chartIteration > 0) v3 = Float.valueOf(resultSet[i][1]).floatValue() - valueOfLastParseCountFailure;

          parseDS.addValue(v3, "parse count (failures)", myResult.getExecutionClockTime());
          valueOfLastParseCountFailure = Float.valueOf(resultSet[i][1]).floatValue();
        }

      }

      parseEvents[parseCounter++] = Math.max(v1,Math.max(v2,v3));
      if (parseCounter == offSet - 1) parseCounter = 0;

      removeOldEntries();
      updateLoadAxis();
      chartIteration++;
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
  
  /**
   * Update the scale of the axis
   */
  private void updateLoadAxis() {
    try {
      // user calls axis
      double max = 0;
      for (int i = 0; i < offSet; i++) {
        max = Math.max(max, parseEvents[i]);
      }
      if (myAxis.getUpperBound() != Math.max(10.0, max + 1)) myAxis.setRange(0, Math.max(10.0, max * 1.2));

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
      List colList = parseDS.getColumnKeys();
      Object[] colObj = colList.toArray();

      for(int i=0; i < colObj.length - offSet; i++) {
        parseDS.removeColumn(colObj[i].toString());
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

