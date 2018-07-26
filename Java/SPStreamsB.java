/*
 * SPStreamsB.java        17.43 20/05/10
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
 * 05/03/12 Richard Wright Modified by use awrCache
 */


package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 *
 */
public class SPStreamsB extends RichButton {
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel 

  private DefaultCategoryDataset streamsDS;

  int[] snapIdRange;                         // list of the snap_id's in the range to be charted 
  String[] snapDateRange;                    // list the snapshot date/times to be charted 
  int oneSelectedIndex = 1;

  
  double oneDSMaxValue = 0;
  double axisOffSet = 1.0;

  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param options
   * @param scrollP
   * @param statusBar
   */
  public SPStreamsB(StatspackAWRInstancePanel statspackPanel,String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Streams");
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
      statspackAWRInstancePanel.sanityCheckRange();

      if (selection.equals("Capture")) {
        String cursorId = "stats$captureAWR.sql";
        Parameters myPars = new Parameters();
        myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
        
        QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), 0, statspackAWRInstancePanel.getInstanceName(),null);
        ExecuteDisplay.displayTable(myResult,statspackAWRInstancePanel,false,statusBar);
        
        //executeDisplayStatement("stats$captureAWR.sql",myPars,false);
      }      
      
      if (selection.equals("Capture Lag Chart")) {
       captureLagChart();
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"PGA Stats CB");
    }
  }
  
  private void captureLagChart() throws Exception {
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    
    streamsDS = new DefaultCategoryDataset();
    
    int snapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      snapId = snapIdRange[i];
      
      String cursorId = "stats$captureAWR.sql";
      Parameters myPars = new Parameters();
      myPars.addParameter("int",statspackAWRInstancePanel.getStartSnapId());
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());      
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, statspackAWRInstancePanel.getStartSnapId(), 0, statspackAWRInstancePanel.getInstanceName(),null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      String captureName;
      double lag;
      String label;
      
      for (int j=0; j < myResult.getNumRows(); j++) {
        captureName = resultSet[j][1];
        lag = Double.valueOf(resultSet[j][2]).doubleValue();

        label = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
        
        streamsDS.addValue(lag,captureName,label);
        
      }  
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              "lag (seconds)",
                              streamsDS,
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
    myPlot.setRenderer(0,myRenderer);
    
    // set axis color scheme 
    myRenderer.setSeriesPaint(0,Color.RED);
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
    statspackAWRInstancePanel.displayChart(myChartPanel);

  }
}