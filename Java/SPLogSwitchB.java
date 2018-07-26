/*
 * SPLogSwitchCB.java        13.18 20/04/06
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
 * 15/05/06 Richard Wright Corrected a bug that stopped the final snapshot being shown
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 11/03/07 Richard Wright Set chart background White
 * 01/05/08 Richard Wright Set series paint to allow usage of JFreeChart 1.0.9
 * 27/01/09 Richard Wright Called StatsPackAWRPanel displayChart to avoid text headings being left behind
 * 05/03/12 Richard Wright Modified by use awrCache
 */

package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 * 
 */
public class SPLogSwitchB extends JButton {
  JLabel statusBar;        // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;     // The JScrollPane on which output should be displayed
  StatspackAWRInstancePanel statspackAWRInstancePanel; // parent panel 

  private DefaultCategoryDataset numSwitchesDS;


  int[] snapIdRange;          // list of the snap_id's in the range to be charted 
  String[] snapDateRange;     // list the snapshot date/times to be charted 
  int oneSelectedIndex = 1;

  double oneDSMaxValue = 0;
  double axisOffSet = 1.0;

  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPLogSwitchB(StatspackAWRInstancePanel statspackPanel, String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);

    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  /**
   * Performs the user selected action 
   * 
   */
  public void actionPerformed() {
    try {
//      statspackPanel.clearScrollP();
      ChartPanel myChartPanel = createLogSwitchChart();

//      scrollP.getViewport().removeAll();
//      scrollP.getViewport().add(myChartPanel, null);
      statspackAWRInstancePanel.displayChart(myChartPanel);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public ChartPanel createLogSwitchChart() throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();

    numSwitchesDS = new DefaultCategoryDataset();

    int startSnapId;
    int endSnapId;
    int i = 0;

    while (i < snapIdRange.length - 1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i+1];
      
      String cursorId;

      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$LogSwitchAWR.sql";
      }
      else {
        cursorId = "stats$LogSwitch.sql";
      }


      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      String[][] resultSet = myResult.getResultSetAsStringArray();

      String label = snapDateRange[i] + " - " + snapDateRange[i + 1].substring(9);
      numSwitchesDS.addValue(Integer.valueOf(resultSet[0][1]).intValue(), "Log Switches (derived)", label);
      oneDSMaxValue = Math.max(oneDSMaxValue, Integer.valueOf(resultSet[0][1]).intValue());

      i++;
    }

    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart("", 
                                                      "", 
                                                      "# Log Switches between Snapshots", 
                                                      numSwitchesDS,
                                                      PlotOrientation.VERTICAL, 
                                                      true, 
                                                      true, 
                                                      true);

    myChart.setBackgroundPaint(Color.WHITE);
    ChartPanel myChartPanel = new ChartPanel(myChart);

    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 5));
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 6));
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName, fontStyle, 8));
    }

    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0, new Rectangle(0, 0));
    myPlot.setRenderer(0, myRenderer);

    // set axis color scheme 
    myRenderer.setSeriesPaint(0,Color.RED);
//    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
//    myAxis.setLabelPaint(axisPaint);
//    myAxis.setTickLabelPaint(axisPaint);
//    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    TickUnitSource integerTicks = NumberAxis.createIntegerTickUnits();
    myAxis.setStandardTickUnits(integerTicks);
    
    return myChartPanel;
  }

}
