/*
 * SortChart.java        15.02 03/05/07
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
 * Change History since
 * ====================
 * 21/09/07 Richard Wright Removed shadow from chart introduced by JFreeChart 1.0.11
 */
 
package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Create and display a chart showing some other characteristics of a database
 */
public class SortChart {

  private DefaultCategoryDataset sortDS;
  private NumberAxis sortSizeAxis;
  private JFreeChart myChart;
  private int offSet = 60; // maximum number of undo bars to display
  private int defaultOffSet = 60; // maximum number of undo bars to display


  /**
   * Constructor
   */
  public SortChart() {
  }

  public void createChart(QueryResult myResult,String instanceName) throws Exception {

    myChart = makeChart(myResult);

    // create the chart frame and display it 
    ChartFrame undoCF = new ChartFrame(ConsoleWindow.getInstanceName() + ": Sort Chart", myChart, true);
    undoCF.setSize(Properties.getAdditionalWindowWidth(), Properties.getAdditionalWindowHeight());
    undoCF.setVisible(true);



  }

  /**
   * Create the chart and display it
   * 
   * @param myResult - the QueryResult use to populate first iteration of the chart
   * @throws Exception
   */
  public ChartPanel createChartPanel(QueryResult myResult) throws Exception {

    myChart = makeChart(myResult);

    // create the chart frame and display it 
    ChartPanel myChartPanel = new ChartPanel(myChart);

    return myChartPanel;
  }

  private JFreeChart makeChart(QueryResult myResult) {
    sortDS = new DefaultCategoryDataset();

    // create the chart 
    myChart = ChartFactory.createBarChart("", "Sid", "Sort in Kb", sortDS, PlotOrientation.VERTICAL, 
          true, true, false);
    updateChart(myResult);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myChart.removeLegend();

    // setup the domain axis (bottom axis) 
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    

    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    int fontSize = Properties.getDynamicChartDomainLabelFontSize();
    myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, fontSize));
//    myChart.setTitle("Sort (top " + offSet + ")");
    myChart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));

    // configure the events axis 
    sortSizeAxis = (NumberAxis)myPlot.getRangeAxis();
    sortSizeAxis.setLowerBound(0);
    sortSizeAxis.setAutoRange(true);
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    sortSizeAxis.setStandardTickUnits(integerTicks);
    sortSizeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    sortSizeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    // set up gradient paints for series...
    GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.GREEN, 0.0f, 0.0f, Color.lightGray);

    BarRenderer renderer = (BarRenderer)myPlot.getRenderer();
    //   renderer.setDrawBarOutline(false);
    renderer.setMaximumBarWidth(0.01);
    renderer.setSeriesPaint(0, gp0);
    renderer.setShadowVisible(false);


    return myChart;
  }


  /**
   * Add a new result set to the chart
   * 
   * @param myResult - QueryResult
   */
  public void updateChart(QueryResult myResult) {
    sortDS = new DefaultCategoryDataset();

    String[][] resultSet = myResult.getResultSetAsStringArray();
    int max = Math.min(offSet, myResult.getNumRows());

    for (int i = 0; i < max; i++) {
      sortDS.addValue(Double.valueOf(resultSet[i][1]).doubleValue(), 
          "sort in kb", resultSet[i][0]);
    }

    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setDataset(sortDS);
  }


  public void setOffSet(int offSet) {
    if (this.offSet != offSet) this.offSet = offSet;
//    myChart.setTitle("Sort (top " + offSet + ")");
  }

  public void resetOffSet() {
    if (this.offSet != defaultOffSet) offSet = defaultOffSet;
//    myChart.setTitle("Sort (top " + offSet + ")");
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
