/*
 * ChartTB.java        15.04 22/05/07
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
 * 13/12/10 Richard Wright Set the chart background to white (it was gray prior to this point and out of step with the other charts
 * 18/12/15 Richard Wright Modified to allow filter by user and improved readability                      
 */

package RichMon;

import java.awt.Color;
import java.awt.Font;

import java.awt.Rectangle;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import javax.swing.Renderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;


public class ChartTB extends RichButton {
  
  JLabel statusBar;
  ScratchDetailPanel scratchPanel;
  int offSet = 75;  // number of bar's to allow
  ResultCache resultCache;
  
  DefaultCategoryDataset chartDS;
  boolean dynamicChart=false;
  JFreeChart myChart;
  
  boolean debug = false;
    
  public ChartTB(String buttonName, ScratchDetailPanel scratchPanel, JLabel statusBar, ResultCache resultCache) {  
    this.setText(buttonName);
    
    this.scratchPanel = scratchPanel;
    this.statusBar = statusBar;
    this.resultCache = resultCache;

  }
  
  public void actionPerformed(int iterationCounter) {
    
    if (iterationCounter == 1 || (!(myChart instanceof JFreeChart))) {
      makeChart();
    }
    else {
      updateChart();
    }
  }

  private void makeChart() {
    
    if (scratchPanel.iterateTB.isSelected()) {
      dynamicChart = true;
    }
    

    try {      
      Cursor myCursor = new Cursor("scratchChart.sql",false);
      myCursor.setSQLTxtOriginal(scratchPanel.getSQLTA());
      Parameters myPars = new Parameters();
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
      Boolean restrictRows = true;
      Boolean flip = true;
      Boolean eggTimer = true;
      Boolean showSQL = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      String[][] myResultSet = myResult.getResultSetAsStringArray();
      String executionTime = myResult.getExecutionClockTime();
      
      if (myResult.getNumCols() >= 2 && myResult.getNumCols() <= 3) {
        chartDS = new DefaultCategoryDataset();
        
        for (int i=0; i < myResult.getNumRows(); i++) {
          int value;
          if (myResult.getNumCols() ==2) {
            value = Integer.valueOf(myResultSet[i][1]).intValue();
          }
          else {
            value = Integer.valueOf(myResultSet[i][2]).intValue();
          }
          if (myResult.getNumCols() == 2) {
            if (dynamicChart) {
              chartDS.addValue(value,myResultSet[i][0],executionTime);
            }
            else {
              chartDS.addValue(value,myResultSet[i][0],myResultSet[i][0]);
            }
          }
          else {
            // 3 columns in the result set
            chartDS.addValue(value,myResultSet[i][1],myResultSet[i][0]);
          }
        }
        
        if (dynamicChart || myResult.getNumCols() == 3) {
          myChart = ChartFactory.createStackedBarChart3D(
                                      "",
                                      "",
                                      "",
                                      chartDS,
                                      PlotOrientation.VERTICAL,
                                      true,
                                      true,
                                      false
                                    );
        }
        else {
          myChart = ChartFactory.createBarChart3D(
                                      "",
                                      "",
                                      "",
                                      chartDS,
                                      PlotOrientation.VERTICAL,
                                      true,
                                      true,
                                      false
                                    );
        }
        
        CategoryPlot myPlot = myChart.getCategoryPlot();
        myPlot.setBackgroundPaint(Color.WHITE);
        
        if (dynamicChart || myResult.getNumCols() == 3) {
          StackedBarRenderer3D myRenderer = (StackedBarRenderer3D) myPlot.getRenderer();
          myRenderer.setMaximumBarWidth(0.01);
        }
        else {
          BarRenderer3D myRenderer = (BarRenderer3D) myPlot.getRenderer();
          myRenderer.setMaximumBarWidth(0.01); 
        }
        
        myChart.getLegend().setBorder(0,0,0,0); 
        myChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));
        
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
        NumberAxis yAxis = (NumberAxis)myPlot.getRangeAxis();
        yAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
        yAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
        
        ChartPanel myChartPanel = new ChartPanel(myChart);
        
        scratchPanel.displayChart(myChartPanel);
      }
    } 
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Making Chart...");  
    }
  }

  private void updateChart() {
    try {
      // get the resultset which needs converting into a chart
//      Cursor myCursor = new Cursor("scratchChart.sql",false);
//      myCursor.setSQLTxtOriginal(scratchPanel.getSQLTA());

//      QueryResult myResult = new QueryResult();
//      myResult = ExecuteDisplay.execute(myCursor,false,true,null);
//      int ms = (int)myResult.getExecutionTime();
//      int numRows = myResult.getNumRows();
//      String cursorId = "scratch.sql";
//      String executionTime = myResult.getExecutionClockTime();
//      statusBar.setText("Executed " + cursorId + " in " + ms + 
//                        " milleseconds returning " + numRows + " rows @ " + 
//                        executionTime);

      Cursor myCursor = new Cursor("scratchChart.sql",false);
      myCursor.setSQLTxtOriginal(scratchPanel.getSQLTA());
      Parameters myPars = new Parameters();
      Boolean filterByRAC = false;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "default";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
      Boolean restrictRows = true;
      Boolean flip = true;
      Boolean eggTimer = true;
      Boolean showSQL = false;
      QueryResult myResult = executeFilterStatement(myCursor, myPars, flip, eggTimer, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);

      String[][] myResultSet = myResult.getResultSetAsStringArray();
      String executionTime = myResult.getExecutionClockTime();
      
      if (myResult.getNumCols() == 2) {

        for (int i = 0; i < myResult.getNumRows(); i++) {
          int value = Integer.valueOf(myResultSet[i][1]).intValue();
          chartDS.addValue(value, myResultSet[i][0], executionTime);
        }
      }
      
      removeOldEntries();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Updating Chart...");  
    }
  }
  
  public void removeOldEntries() {
    try {
      List colList = chartDS.getColumnKeys();
      Object[] colObj = colList.toArray();
      
      for(int i=0; i < colObj.length - offSet; i++) {
        chartDS.removeColumn(colObj[i].toString());
      }        
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Removing old chart entries...");
    }
  }
  
  public void nullChart() {
    if (myChart instanceof JFreeChart) myChart = null;
  }
}
