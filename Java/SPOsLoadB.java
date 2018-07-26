package RichMon;

/*
 *  SPOsLoadB.java        13.14 28/02/06
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
 * 06/03/06 Richard Wright Clear the statspack panel before displaying new objects
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 11/03/07 Richard Wright Set chart background White
 * 01/05/08 Richard Wright Set series paint to allow usage of JFreeChart 1.0.9
 * 09/06/08 Richard Wright Converted to use just a single axis to aid clarity
 * 27/01/09 Richard Wright Called StatsPackAWRPanel displayChart to avoid text headings being left behind
 * 16/12/01 Richard Wright Amended OSLOAD chart to work on 10.1
 * 05/03/12 Richard Wright Modified by use awrCache
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
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
import org.jfree.data.category.DefaultCategoryDataset;


/**
 *
 */
public class SPOsLoadB extends JButton {
  JLabel statusBar;       // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;    // The JScrollPane on which output should be displayed 
  StatspackAWRInstancePanel statspackAWRInstancePanel; // parent panel 

  private DefaultCategoryDataset osLoadDS;

  int[] snapIdRange;      // list of the snap_id's in the range to be charted 
  String[] snapDateRange; // list the snapshot date/times to be charted 
  int oneSelectedIndex = 1;
  int twoSelectedIndex = 0;
  int threeSelectedIndex = 0;
  int fourSelectedIndex = 0;
  int fiveSelectedIndex = 0;
  int sixSelectedIndex = 0;
  int sevenSelectedIndex = 0;
  int eightSelectedIndex = 0;

  double maxValue = 0;

  double axisOffSet = 1.0;

  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPOsLoadB(StatspackAWRInstancePanel statspackPanel, String buttonName, JScrollPane scrollP, JLabel statusBar) {
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
      ChartPanel myChartPanel = createOSLoadChart();

//      scrollP.getViewport().removeAll();;
//      scrollP.getViewport().add(myChartPanel, null);
      statspackAWRInstancePanel.displayChart(myChartPanel);
    }
    catch (NoSnapshotsException eee) {
      JOptionPane.showMessageDialog(scrollP, eee, "Error...", JOptionPane.ERROR_MESSAGE);
    }
    catch (ReStartedDBException ee) {
      JOptionPane.showMessageDialog(scrollP, ee, "Error...", JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public ChartPanel createOSLoadChart() throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();

    osLoadDS = new DefaultCategoryDataset();

    int startSnapId;
    int endSnapId;
    int i = 0;

    while (i < snapIdRange.length - 1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i+1];
      
      String cursorId;

      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "stats$osloadAWR.sql";
        }
        else {
          // 10.1
          cursorId = "stats$osloadAWR101.sql";
        }
      }
      else {
        cursorId = "stats$osload.sql";
      }

      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());

      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      String statisticName = "";
      double statisticValue = 0;
      String label;


      for (int j = 0; j < myResult.getNumRows(); j++) {
        statisticName = resultSet[j][0];
        statisticValue = Double.valueOf(resultSet[j][1]).doubleValue();

        label = snapDateRange[i] + " - " + snapDateRange[i + 1].substring(9);

        if (statisticName.toLowerCase().equals("avg_user_time")) {
          osLoadDS.addValue(statisticValue, "avg cpu user time (cs)", label);
        }

        if (statisticName.toLowerCase().equals("avg_sys_time")) {
          osLoadDS.addValue(statisticValue, "avg cpu system time (cs)", label);
        }

        if (statisticName.toLowerCase().equals("avg_busy_time")) {
          osLoadDS.addValue(statisticValue, "avg cpu busy time (cs)", label);
        }

        if (statisticName.toLowerCase().equals("avg_idle_time")) {
          osLoadDS.addValue(statisticValue, "avg cpu idle time (cs)", label);
        }        
        
        if (statisticName.equals("USER_TIME")) {
          osLoadDS.addValue(statisticValue, "USER_TIME (cs)", label);
        }

        if (statisticName.equals("SYS_TIME")) {
          osLoadDS.addValue(statisticValue, "SYS_TIME (cs)", label);
        }

        if (statisticName.equals("BUSY_TIME")) {
          osLoadDS.addValue(statisticValue, "BUSY_TIME (cs)", label);
        }

        if (statisticName.equals("IOWAIT_TIME")) {
          osLoadDS.addValue(statisticValue, "IOWAIT_TIME (cs)", label);
        }        
        
        if (statisticName.equals("IDLE_TIME")) {
          osLoadDS.addValue(statisticValue, "IDLE_TIME (cs)", label);
        }
        
        maxValue = Math.max(maxValue, statisticValue);
      }
      i++;
    }

    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                               "",
                               "OS Load",
                               "cpu time",
                               osLoadDS,
                               PlotOrientation.VERTICAL,
                               true,
                               true,
                               true
                             );

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
    myPlot.setRenderer(0, myRenderer);

    // set axis color scheme 
//    myRenderer.setSeriesPaint(0,Color.RED);
//    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
//    myAxis.setLabelPaint(axisPaint);
//    myAxis.setTickLabelPaint(axisPaint);
//    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setUpperBound(Math.max(maxValue * 1.25, 1));
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    
    // set the color for other datasets

    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    myRenderer.setSeriesShape(1,new Rectangle(0,0));
    myRenderer.setSeriesShape(2,new Rectangle(0,0));
    myRenderer.setSeriesShape(3,new Rectangle(0,0));
    myRenderer.setSeriesShape(4,new Rectangle(0,0));
    myRenderer.setSeriesPaint(0,Color.RED);
    myRenderer.setSeriesPaint(1,Color.BLUE);
    myRenderer.setSeriesPaint(2,Color.GREEN);
    myRenderer.setSeriesPaint(3,Color.ORANGE);
    myRenderer.setSeriesPaint(4,Color.GRAY);

    return myChartPanel;
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
                         String axisName) {
    // add the dataset to the plot 
    myPlot.setDataset(i, axisDS);

    // create a new axis 
    NumberAxis myAxis = new NumberAxis();

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
    if (i==0) myRenderer.setSeriesPaint(0,Color.RED);
    if (i==1) myRenderer.setSeriesPaint(0,Color.BLUE);
    if (i==2) myRenderer.setSeriesPaint(0,Color.ORANGE);
    if (i==3) myRenderer.setSeriesPaint(0,Color.GREEN);
    if (i==4) myRenderer.setSeriesPaint(0,Color.MAGENTA);
    if (i==5) myRenderer.setSeriesPaint(0,Color.PINK);
    if (i==6) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
    myPlot.setRenderer(i, myRenderer);

    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    
    myAxis.setLowerBound(0.0);

    myAxis.setUpperBound(Math.max(maxValue * 1.25, 1));
  }
}
