/*
 *  SPBufferCacheB.java        13.22 29/06/06
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
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120
 * 09/08/06 Richard Wright Fixed problem causing only flat lines to appear on the chart
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 14/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 11/03/07 Richard Wright Set chart background White
 * 15/05/07 Richard Wright Added Buffer Pool Advisory
 * 06/09/07 Richard Wright Converted to use JList & JButton and renamed class file
 * 07/09/07 Richard Wright Renamed buffer pool to buffer cache
 * 01/10/07 Richard Wright Removed the target range from the chart to enhance read ability
 * 01/05/08 Richard Wright Set series paint to allow usage of JFreeChart 1.0.9
 * 09/06/08 Richard Wright Fixed problems with dataset colours
 * 10/12/08 Richard Wright Called displayChart instead of doing it manually each time
 * 05/03/12 Richard Wright Modified by use awrCache
 */
 
 package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
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
 * Implements a query to show all current sort operations.
 */
public class SPBufferCacheB extends JButton {
  JLabel statusBar;       // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;    // The JScrollPane on which output should be displayed 
  String[] options;     // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;
  
  int[] snapIdRange;                         //  list of the snap_id's in the range to be charted 
  String[] snapDateRange;                    // list the snapshot date/times to be charted 
  int maxValue = 0;
  int numStarts = 0;

  private DefaultCategoryDataset poolHitRatioDS;
  private DefaultCategoryDataset waitDS;


  /**
   * Constructor 
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPBufferCacheB( StatspackAWRInstancePanel statspackPanel, String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Buffer Cache");
    this.options = options;
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
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
      statspackAWRInstancePanel.clearScrollP();
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("Buffer Cache Summary")) {
        QueryResult myResult = getBufferCacheSummary();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }
      
      if (selection.equals("Buffer Wait Statistics")) {
        QueryResult myResult = getBufferWaitStatistics();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }      
        
      if (selection.equals("Buffer Cache Advisory")) {
        QueryResult myResult = getBuffeCacheAdvisory();
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
      }

      if (selection.equals("Buffer Cache Chart")) {
        ChartPanel myChartPanel = getBufferCacheChart();
        
//        scrollP.getViewport().removeAll();
//        scrollP.getViewport().add(myChartPanel, null);
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public QueryResult getBufferCacheSummary() {
    QueryResult myResult = new QueryResult();
    
    try {
      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();
            
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId= "stats$BufferPool.sql";
        }
        else {
          cursorId= "stats$BufferPoolAWR.sql";          
        }
      }
      else {
        cursorId= "stats$BufferPool.sql";        
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());

      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return myResult;
  }
  
  public QueryResult getBufferWaitStatistics() {
    QueryResult myResult = new QueryResult();
    
    try {
      int startSnapId = statspackAWRInstancePanel.getStartSnapId();
      int endSnapId = statspackAWRInstancePanel.getEndSnapId();
            
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId= "stats$BufferWaitStats.sql";
        }
        else {
          cursorId= "stats$BufferWaitStatsAWR.sql";          
        }
      }
      else {
        cursorId= "stats$BufferWaitStats.sql";        
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());

      
      myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return myResult;
  }
  
    public QueryResult getBuffeCacheAdvisory() {
      // get the database blocksize
      String cursorId = "parameter.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("String","db_block_size");
  
      int blockSize = 8192;
      
      try {
        QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, false, null);
        String[][] resultSet = myResult.getResultSetAsStringArray();
  
        blockSize = Integer.valueOf(resultSet[0][0]).intValue();
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e, this);
      }
        
        
      QueryResult myResult = new QueryResult();
      
      try {
        int endSnapId = statspackAWRInstancePanel.getEndSnapId();
        
        if (ConsoleWindow.getDBVersion() >= 10) {
          if (Properties.isAvoid_awr()) {
            cursorId= "stats$BufferPoolAdvisory10.sql";
          }
          else {
            cursorId= "stats$BufferPoolAdvisoryAWR.sql";          
          }
        }
        else {
          cursorId= "stats$BufferPoolAdvisory.sql";        
        }

        myPars = new Parameters();
        myPars.addParameter("int",blockSize);
        myPars.addParameter("int",blockSize);
        myPars.addParameter("int",blockSize);
        myPars.addParameter("int",blockSize);
        myPars.addParameter("int",blockSize);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
        
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, 0, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this);
      }
      
      return myResult;
    }
  
  public ChartPanel getBufferCacheChart() throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();

    poolHitRatioDS = new DefaultCategoryDataset();
    waitDS = new DefaultCategoryDataset();


    int i = 0;
    int startSnapId;
    int endSnapId;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];            
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10) {
        if (Properties.isAvoid_awr()) {
          cursorId= "stats$BufferPool.sql";
        }
        else {
          cursorId= "stats$BufferPoolAWR.sql";          
        }
      }
      else {
        cursorId= "stats$BufferPool.sql";        
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("long",statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
      myPars.addParameter("int",statspackAWRInstancePanel.getInstanceNumber());
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      double poolHit = Double.valueOf(resultSet[0][2]).doubleValue();
      int freeBufferWaits = Integer.valueOf(resultSet[0][6]).intValue();
      int bufferBusyWaits = Integer.valueOf(resultSet[0][8]).intValue();
      int writeCompleteWaits = Integer.valueOf(resultSet[0][7]).intValue();
      
      String label = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
      
        
      poolHitRatioDS.addValue(poolHit,"Pool Hit %",label);

      waitDS.addValue(freeBufferWaits,"Free Buffer Waits",label);
      maxValue = Math.max(maxValue,freeBufferWaits);
    
      waitDS.addValue(bufferBusyWaits,"Buffer Busy Waits",label);
      maxValue = Math.max(maxValue,bufferBusyWaits);

      waitDS.addValue(writeCompleteWaits,"Write Complete Waits",label);
      maxValue = Math.max(maxValue,writeCompleteWaits);
      
      i++;
    }  
    
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              "Pool Hit %",
                              poolHitRatioDS,
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
    
    // add an interval marker to the plot
/*    IntervalMarker target = new IntervalMarker(90, 100);
    target.setLabel("Hit Ratio Target Range");
    target.setLabelPaint(Color.RED);
    target.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
    target.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
    target.setPaint(new Color(150, 245, 219, 128));
    myPlot.addRangeMarker(target, Layer.BACKGROUND);
*/
    
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
    myPlot.setRenderer(0,myRenderer);
    
    // set axis color scheme 
    myRenderer.setSeriesPaint(0,Color.RED);
//    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
//    myAxis.setLabelPaint(axisPaint);
//    myAxis.setTickLabelPaint(axisPaint);
//    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setRange(0,100);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    
    // add additional axis for each dataset 
    setupAxis(1,myPlot,waitDS,"RIGHT","# Waits");
    
    // set the color for other datasets
    CategoryItemRenderer myRenderer1 = new LineAndShapeRenderer();
    myRenderer1.setSeriesShape(0,new Rectangle(0,0));
    myRenderer1.setSeriesShape(1,new Rectangle(0,0));
    myRenderer1.setSeriesShape(2,new Rectangle(0,0));
    myRenderer1.setSeriesPaint(0,Color.BLUE);
    myRenderer1.setSeriesPaint(1,Color.GREEN);
    myRenderer1.setSeriesPaint(2,Color.ORANGE);
    myPlot.setRenderer(1,myRenderer1);

    
    return myChartPanel;   
  }
  
    private void setupAxis(int i,CategoryPlot myPlot,DefaultCategoryDataset axisDS,String axisPos,String axisName) {
    // add the dataset to the plot 
    myPlot.setDataset(i,axisDS);
    
    // create a new axis 
    NumberAxis myAxis = new NumberAxis();
    
    myAxis.setLabel(axisName);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));

    
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
/*    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    if (i==1) {
      myRenderer.setSeriesShape(0,new Rectangle(0,0));
      myRenderer.setSeriesPaint(0,Color.BLUE);
      myAxis.setLabelPaint(Color.BLUE);
      myAxis.setTickLabelPaint(Color.BLUE);
      myAxis.setAxisLinePaint(Color.BLUE);  
    }
    if (i==2) {
      myRenderer.setSeriesShape(0,new Rectangle(0,0));
      myRenderer.setSeriesPaint(0,Color.GREEN);
      myAxis.setLabelPaint(Color.GREEN);
      myAxis.setTickLabelPaint(Color.GREEN);
      myAxis.setAxisLinePaint(Color.GREEN);  
    }
    if (i==3) {
      myRenderer.setSeriesShape(0,new Rectangle(0,0)); 
      myRenderer.setSeriesPaint(0,Color.ORANGE);
      myAxis.setLabelPaint(Color.ORANGE);
      myAxis.setTickLabelPaint(Color.ORANGE);
      myAxis.setAxisLinePaint(Color.ORANGE);  
    } 
    
    myPlot.setRenderer(i,myRenderer);  */
    myAxis.setLowerBound(0.0);

    if (i == 1) myAxis.setUpperBound(Math.max(maxValue * 1.25,1));
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
