/*
 * FileIOChart.java        15.03 08/05/07
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
 * 03/12/07 Richard Wright Enhanced for RAC
 * 27/05/08 Richard Wright Removed the path from file names on windows
 * 21/09/07 Richard Wright Removed shadow from chart introduced by JFreeChart 1.0.11
 */
 
 package RichMon;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Create and display a chart showing some other characteristics of a database
 */
public class FileIOChart {

  private DefaultCategoryDataset fileIODS;
  private String[][] fileIOValues;
  private String[][] newIOValues;
  private int blockSize;
  private NumberAxis ioAxis;
  private int offSet = 20; // number of ts to display ie top 'n' rows
  
  JFreeChart myChart;

  /**
   * Constructor
   */
  public FileIOChart() {

    // get the database blocksize
    String cursorId = "parameter.sql";

    Parameters myPars = new Parameters();
    myPars.addParameter("String","db_block_size");

    try {
      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      blockSize = Integer.valueOf(resultSet[0][0]).intValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
  }

  public void createChart(QueryResult myResult, String instanceName) throws Exception {

    myChart = makeChart(myResult);

    // create the chart frame and display it 
//    if (Properties.isPlaceOverviewChartsIntoASingleFrame()) {
//      ChartPanel myChartPanel = new ChartPanel(myChart);
//      ExecuteDisplay.addWindowHolder(myChartPanel, "File IO - " + instanceName);
//    }
//    else {
      ChartFrame tsIOCF = new ChartFrame(ConsoleWindow.getInstanceName() + 
          ": File IO Chart", myChart, true);

      tsIOCF.setSize(Properties.getAdditionalWindowWidth(), Properties.getAdditionalWindowHeight());
      tsIOCF.setVisible(true);
//    }
  }

  /*   public JScrollPane createChartScrollPane(QueryResult myResult) throws Exception {

    JFreeChart myChart = makeChart(myResult);

    // create the chart frame and display it
    ChartPanel myChartPanel = new ChartPanel(myChart);
    JScrollPane myScrollPane = new JScrollPane();
    myScrollPane.getViewport().add(myChartPanel);
    int h = myChartPanel.getHeight();
    int w = myChartPanel.getWidth();

    return myScrollPane;
  }*/

  public ChartPanel createChartPanel(QueryResult myResult) throws Exception {

    myChart = makeChart(myResult);

    // create the chart frame and display it 
    ChartPanel myChartPanel = new ChartPanel(myChart);

    return myChartPanel;
  }

  private JFreeChart makeChart(QueryResult myResult) {
    fileIODS = new DefaultCategoryDataset();

    // Store values from first iteration
    fileIOValues = myResult.getResultSetAsStringArray();
    
    // remove the path from the file name - unix
    for (int i=0; i < fileIOValues.length; i++) {
      fileIOValues[i][0] = fileIOValues[i][0].substring(fileIOValues[i][0].lastIndexOf("/") +1);
    }    
    
    // remove the path from the file name - pc
    for (int i=0; i < fileIOValues.length; i++) {
      fileIOValues[i][0] = fileIOValues[i][0].substring(fileIOValues[i][0].lastIndexOf("\\") +1);
    }

    // create the chart 
    JFreeChart myChart = ChartFactory.createBarChart("", "", "Read / Writes in Mb (Top 20 most active)", fileIODS,PlotOrientation.HORIZONTAL, true, true, false);

    myChart.setBackgroundPaint(Color.WHITE);
    CategoryPlot myPlot = myChart.getCategoryPlot();
    myPlot.setBackgroundPaint(Color.WHITE);
    
    myChart.getLegend().setBorder(0, 0, 0, 0);
    myChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));

    // setup the domain axis (bottom axis) 
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setVisible(Properties.getDynamicChartDomainLabels());
    myDomainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getDynamicChartDomainLabelFontSize()));

    // configure the events axis 
    ioAxis = (NumberAxis)myPlot.getRangeAxis();
    ioAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    ioAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    BarRenderer renderer = (BarRenderer)myPlot.getRenderer();
    renderer.setShadowVisible(false);
    
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
      
      // remove the path from the file name - unix
      for (int i=0; i < resultSet.length; i++) {
        resultSet[i][0] = resultSet[i][0].substring(resultSet[i][0].lastIndexOf("/") +1);
      }
      
      // remove the path from the file name -pc
      for (int i=0; i < resultSet.length; i++) {
        resultSet[i][0] = resultSet[i][0].substring(resultSet[i][0].lastIndexOf("\\") +1);
      }
      
      newIOValues = new String[myResult.getNumRows()][4];

      float maxAxisRange = 0;

      // work out how much of change has taken place
      for (int i = 0; i < resultSet.length; i++) {
        for (int j = 0; j < fileIOValues.length; j++) {
          if (resultSet[i][0].equals(fileIOValues[j][0])) {
            float curValWrites = Float.valueOf(resultSet[i][2]).floatValue();
            float oldValWrites = Float.valueOf(fileIOValues[j][2]).floatValue();
            long diffValWrites = 
              (long)((curValWrites - oldValWrites) * blockSize) / 1024 / 1024;
            float curValReads = Float.valueOf(resultSet[i][1]).floatValue();
            float oldValReads = Float.valueOf(fileIOValues[j][1]).floatValue();
            long diffValReads = 
              (long)((curValReads - oldValReads) * blockSize) / 1024 / 1024;

            //            tsIODS.addValue(diffValReads,"Physical Reads",resultSet[i][0]);
            //            tsIODS.addValue(diffValWrites,"Physical Writes",resultSet[i][0]);

            maxAxisRange = Math.max(maxAxisRange, Math.max(diffValReads, 
                                                           diffValWrites));

            // store away the # of blocks reas/written for next time
            fileIOValues[j][1] = resultSet[i][1];
            fileIOValues[j][2] = resultSet[i][2];
            newIOValues[i][0] = resultSet[i][0];
            newIOValues[i][1] = String.valueOf(diffValReads);
            newIOValues[i][2] = String.valueOf(diffValWrites);
            newIOValues[i][3] = String.valueOf(diffValReads + diffValWrites);
          }
        }
      }

      /*
       * newIOValues contains the change for every TS on this iteration.
       * Identify the top 'n' by column 3 so they can be displayed
       */

      int[] topn = new int[offSet];

      for (int i = 0; i < offSet; i++) {
        long maxValue = -1;
        int element = -1;
        for (int j = 0; j < newIOValues.length; j++) {
          long currVal = Long.valueOf(newIOValues[j][3]).longValue();
          if (currVal >= maxValue) {
            maxValue = currVal;
            topn[i] = j;
            element = j;
          }
        }
        newIOValues[element][3] = "-1";
      }

      // remove old dataset entries
      for (int i = 0; i < fileIODS.getColumnCount(); i++) {
        fileIODS.removeColumn(i);
      }

      // add topn values to the dataset
      for (int i = 0; i < offSet; i++) {
        fileIODS.addValue(Long.valueOf(newIOValues[topn[i]][1]).longValue(), 
            "Physical Reads", newIOValues[topn[i]][0]);
        fileIODS.addValue(Long.valueOf(newIOValues[topn[i]][2]).longValue(), 
            "Physical Writes", newIOValues[topn[i]][0]);
      }


      if (maxAxisRange < 1) {
        ioAxis.setRange(0, 1);
      }
      else {
        ioAxis.setRange(0, maxAxisRange * 1.1);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e, this);
    }
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
