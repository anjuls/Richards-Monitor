/*
 * SPPGAStatsB.java        13.00 30/06/05
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
 * 
 * 19/10/05 Richard Wright Reduced the requirement for a range of 3 snapshots 
 *                         down to 2.
 * 08/12/05 Richard Wright Error about db restarts now include the date/time of 
 *                         previous restarts
 * 07/02/06 Richard Wright No longer allows charts to span db restarts if the 
 *                         parameter or menu specifies.  No longer allows a range
 *                         of more than 100 snapshots unless specified by 
 *                         parameter or menu item.
 * 06/03/06 Richard Wright Clear the statspack panel before displaying new objects
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackAWRInstancePanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 11/03/07 Richard Wright Set chart background White
 * 21/05/07 Richard Wright Converted to a JComboBox to accomodate pga advisory
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 17/09/07 Richard Wright Corrected button name error regressed in 16.04
 * 01/05/08 Richard Wright Set series paint to allow usage of JFreeChart 1.0.9
 * 10/12/08 Richard Wright Called displayChart instead of doing it manually each time
 * 20/10/09 Richard Wright Reset the axis scales each time a new chart is generated
 * 01/03/12 Richard Wright Modified to use the awr Cache
 * 12/02/13 Richard Wright Removed 'PGA memory freed back to OS' from the chart
 *                         and made over allocation count show the difference between each snap rather than being cumulative
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;

import java.io.FileInputStream;

import java.io.ObjectInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
public class SPPGAStatsB extends JButton {
  JLabel statusBar;     // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;  // The JScrollPane on which output should be displayed 
  String[] options;     // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel 

  private DefaultCategoryDataset oneDS;
  private DefaultCategoryDataset twoDS;
  private DefaultCategoryDataset threeDS;
  private DefaultCategoryDataset fourDS;
  private DefaultCategoryDataset fiveDS;
  private DefaultCategoryDataset sixDS;


  int[] snapIdRange;                         // list of the snap_id's in the range to be charted 
  String[] snapDateRange;                    // list the snapshot date/times to be charted 
  int oneSelectedIndex = 1;
  int twoSelectedIndex = 0;
  int threeSelectedIndex = 0;
  int fourSelectedIndex = 0;
  int fiveSelectedIndex = 0;
  int sixSelectedIndex = 0;
  int sevenSelectedIndex = 0;
  int eightSelectedIndex = 0;
  
  double oneDSMaxValue = 0;
  double twoDSMaxValue = 0;
  double threeDSMaxValue = 0;
  double fourDSMaxValue = 0;
  double fiveDSMaxValue = 0;
  double sixDSMaxValue = 0;

  double axisOffSet = 1.0;
  
  boolean debug = false;

  /**
   * Constructor
   * 
   * @param statspackAWRInstancePanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPPGAStatsB(StatspackAWRInstancePanel statspackAWRInstancePanel,String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("PGA Summary");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackAWRInstancePanel;
  }

  public void actionPerformed() {
    // if a list frame already exists, then remove it
    ConnectWindow.getConsoleWindow().removeLastFrame();
    
    final JList myList = new JList(options);     
    myList.setVisibleRowCount(options.length);
    JScrollPane listScroller = new JScrollPane(myList);
    final JFrame listFrame = new JFrame(this.getText());
    listFrame.add(listScroller);
    listFrame.pack();
    listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    listFrame.setLocationRelativeTo(this);
    
    myList.addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          listActionPerformed(myList.getSelectedValue(), listFrame);
          listFrame.dispose();
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
      });
    
    listFrame.setIconImage(ConnectWindow.getRichMonIcon().getImage());  
    listFrame.setVisible(true);  

    // save a reference to the listFrame so it can be removed if left behind
    ConnectWindow.getConsoleWindow().saveFrameRef(listFrame);
  }

  /**
   * Performs the user selected action 
   * 
   * @param selectedOption 
   */
  public void listActionPerformed(Object selectedOption, JFrame listFrame) {
    String selection = selectedOption.toString();
    listFrame.setVisible(false);
    statspackAWRInstancePanel.setLastAction(selection);
    try {  
//      statspackAWRInstancePanel.clearScrollP();
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("PGA Stats Chart")) {
        ChartPanel myChartPanel = createPGAStatChart();
        
//        scrollP.getViewport().removeAll();
//        scrollP.getViewport().add(myChartPanel,null);
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
      
        if (selection.equals("PGA Advisory")) {
          QueryResult myResult = getPGAAdvisory();
          ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
        }
        
        if (selection.equals("PGA Workareas")) {
          QueryResult myResult = getPGAWorkareas();
          ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
        }
        
        if (selection.equals("PGA Histogram")) {
          QueryResult myResult = getPGAHistogram();
          ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
        }
    
    
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"PGA Stats CB");
    }
  }

  public ChartPanel createPGAStatChart() throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    
    oneDS = new DefaultCategoryDataset();
    twoDS = new DefaultCategoryDataset();
    threeDS = new DefaultCategoryDataset();
    fourDS = new DefaultCategoryDataset();
    fiveDS = new DefaultCategoryDataset();
    sixDS = new DefaultCategoryDataset();

    oneDSMaxValue = 0;
    twoDSMaxValue = 0;
    threeDSMaxValue = 0;
    fourDSMaxValue = 0;
    fiveDSMaxValue = 0;
    sixDSMaxValue = 0;
    
    Double overAllocationCountLastValue = 0.0;
    
    int snapId;
    int i = 1;
    
    while (i < snapIdRange.length) {
      snapId = snapIdRange[i];
            
      String cursorId;
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$pgastatAWR.sql";        
      }
      else {
        cursorId = "stats$pgastat.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",snapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",snapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long", statspackAWRInstancePanel.getInstanceNumber());
      
  
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, snapId, snapId, statspackAWRInstancePanel.getInstanceName(),null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      String statisticName = "";
      double statisticValue = 0;
      String label;
      
      
      for (int j=0; j < myResult.getNumRows(); j++) {
        statisticName = resultSet[j][0];
        statisticValue = Double.valueOf(resultSet[j][1]).doubleValue();

        label = snapDateRange[i-1] + " - " + snapDateRange[i].substring(9);
        
        if (statisticName.equals("maximum PGA allocated")) { 
          oneDS.addValue(statisticValue,statisticName,label);
          oneDSMaxValue = Math.max(oneDSMaxValue,statisticValue);
        }
        
        if (statisticName.equals("aggregate PGA auto target")) { 
          oneDS.addValue(statisticValue,statisticName,label);
          oneDSMaxValue = Math.max(oneDSMaxValue,statisticValue);
        }
        
        if (statisticName.equals("total PGA allocated")) {
          oneDS.addValue(statisticValue,statisticName,label);
          oneDSMaxValue = Math.max(oneDSMaxValue,statisticValue);
        }
        
        if (statisticName.equals("total PGA inuse")) {
          oneDS.addValue(statisticValue,statisticName,label);
          oneDSMaxValue = Math.max(oneDSMaxValue,statisticValue);
        }
        
        if (statisticName.equals("aggregate PGA target parameter")) {
          oneDS.addValue(statisticValue,statisticName,label);
          oneDSMaxValue = Math.max(oneDSMaxValue,statisticValue);
        }
        
        if (statisticName.equals("over allocation count")) {
          Double overAllocationCount = statisticValue - overAllocationCountLastValue;
          overAllocationCountLastValue = statisticValue;
          if (i > 1) fiveDS.addValue(overAllocationCount,statisticName,label);
          fiveDSMaxValue = Math.max(fiveDSMaxValue,overAllocationCount);
          if (debug) System.out.println(label + " : " + overAllocationCount + " " + statisticName + " :maxValue " + fiveDSMaxValue);
        }
        
        if (statisticName.equals("cache hit percentage")) {
          sixDS.addValue(statisticValue,statisticName,label);
          sixDSMaxValue = Math.max(sixDSMaxValue,statisticValue);
        }
      }  
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              "PGA (mb)   -   (max allocated / in use / total allocated & targets)",
                              oneDS,
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
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,4));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));
    myRenderer.setSeriesShape(1,new Rectangle(0,0));   
    myRenderer.setSeriesShape(2,new Rectangle(0,0));
    myRenderer.setSeriesShape(3,new Rectangle(0,0));
    myRenderer.setSeriesShape(4,new Rectangle(0,0));
    myPlot.setRenderer(0,myRenderer);
    
    // set axis color scheme 
    myRenderer.setSeriesPaint(0,Color.BLACK);
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    // add additional axis for each dataset 
    //setupAxis(1,myPlot,twoDS,"Left","aggregate PGA Auto target (mb)");
    //setupAxis(2,myPlot,fourDS,"RIGHT","total PGA in use (mb)");
    setupAxis(3,myPlot,fiveDS,"LEFT","over allocation count");
    setupAxis(4,myPlot,sixDS,"RIGHT","cache hit percentage");
    //setupAxis(5,myPlot,threeDS,"RIGHT","total PGA allocated (mb)");
    
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
  private void setupAxis(int i,CategoryPlot myPlot,DefaultCategoryDataset axisDS,String axisPos,String axisName) {
    // add the dataset to the plot 
    myPlot.setDataset(i,axisDS);
    
    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    
    myAxis.setLabel(axisName);

    // make axis increment in integers 
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
//    if (i==0) myRenderer.setSeriesPaint(0,Color.RED);
    if (i==1) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
//    if (i==2) myRenderer.setSeriesPaint(0,Color.ORANGE);
    if (i==3) myRenderer.setSeriesPaint(0,Color.CYAN);
    if (i==4) myRenderer.setSeriesPaint(0,Color.MAGENTA);
    if (i==5) myRenderer.setSeriesPaint(0,Color.BLUE);
//    if (i==6) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
    myPlot.setRenderer(i,myRenderer);
    
    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);     
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    myAxis.setLowerBound(0.0);
    
    if (i == 5) myAxis.setUpperBound(Math.max(Math.max(threeDSMaxValue,fourDSMaxValue) * 1.1,1));
    if (i == 2) myAxis.setUpperBound(Math.max(Math.max(threeDSMaxValue,fourDSMaxValue) * 1.1,1));
    if (i == 3) myAxis.setUpperBound(Math.max(fiveDSMaxValue * 2,1));
    if (i == 4) myAxis.setUpperBound(Math.max(sixDSMaxValue * 2.25,1));

    if (axisName.equals("cache hit percentage")) myAxis.setRange(0,100);
  }
  
  
    public QueryResult getPGAAdvisory() {
      String cursorId;     
      QueryResult myResult = new QueryResult();
      
      try {
        int endSnapId = statspackAWRInstancePanel.getEndSnapId();
        
        if (ConsoleWindow.getDBVersion() >= 10) {
          if (Properties.isAvoid_awr()) {
            cursorId= "stats$PGAAdvisory.sql";
          }
          else {
            cursorId= "stats$PGAAdvisoryAWR.sql";          
          }
        }
        else {
          cursorId= "stats$PGAAdvisory.sql";        
        }

        Parameters myPars = new Parameters();
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, 0, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this);
      }
      
      return myResult;
    }
    
    public QueryResult getPGAWorkareas() {
      String cursorId = "stats$PGAWorkareasAWR.sql";
      QueryResult myResult = new QueryResult();
      
      try {

        Parameters myPars = new Parameters();
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());          
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this);
      }
      
      return myResult;
    }
  
  public QueryResult getPGAHistogram() {
    String cursorId = "stats$PGAHistogramAWR.sql";
    QueryResult myResult = new QueryResult();
    
    try {

      Parameters myPars = new Parameters();        
      myPars.addParameter("int", statspackAWRInstancePanel.getEndSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
      myPars.addParameter("int", statspackAWRInstancePanel.getStartSnapId());
  
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), statspackAWRInstancePanel.getEndSnapId(), statspackAWRInstancePanel.getInstanceName(),null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return myResult;
  }

  public void addItem(String newOption) {
    // check to see whether this option already exists
    boolean dup = false;
    for (int i=0; i < options.length; i++) {
      if (options[i].equals(newOption)) {
        dup = true;
        break;
      }
    }
    
    // if its not a duplicate, add it to the options array
    if (!dup) {
      String[] tmp = new String[options.length];
      System.arraycopy(options,0,tmp,0,options.length);
      options = new String[options.length +1];
      System.arraycopy(tmp,0,options,0,tmp.length);
      options[options.length-1] = newOption;
    }
  }
}