/*
 * SPStatisticsB.java        13.00 23/06/05
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
 * Caution:  The value used in the chart if the begin snapshot subtracted from 
 *           end snapshot.  This is appropriate for things like physical reads, 
 *           so you know how many ocurred in the snapshot, but for a statistic
 *           like 'logons current' the result might be confusing.
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
 *                         parameter or menu item.  Made the font size of labels 
 *                         dependant on the number of snapshots selected.
 * 06/03/06 Richard Wright Clear the statspack panel before displaying new objects
 * 22/06/06 Richard Wright Put the statistic value in a long rather than int to 
 *                         avoid overflows
 * 13/07/06 Richard Wright Changed the limit on snapshot ranges from 100 to 120 
 * 10/07/06 Richard Wright Allow the user to choose whether all datasets should 
 *                         be presented against a single axis
 * 05/09/06 Richard Wright Modified the comment style and error handling
 * 15/09/06 Richard Wright Moved sanityCheckRange to the statspackPanel
 * 11/12/06 Richard Wright Fixed bug in font size following previous change
 * 11/03/07 Richard Wright Set chart background White
 * 07/12/07 Richard Wright Enhanced for RAC
 * 25/04/08 Richard Wright Prevented chart from being produced when cancelled
 * 21/05/08 Richard Wright Corrected bug that caused the first 2 datasets to display in RED.
 * 09/06/08 Richard Wright Fixed problems charts using not using a single axis
 * 10/12/08 Richard Wright Called displayChart instead of doing it manually each time
 */ 

package RichMon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
public class SPStatisticsB extends JButton {
  JLabel statusBar;         // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;      // The JScrollPane on which output should be displayed 
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel 

  private DefaultCategoryDataset oneDS;
  private DefaultCategoryDataset twoDS;
  private DefaultCategoryDataset threeDS;
  private DefaultCategoryDataset fourDS;
  private DefaultCategoryDataset fiveDS;
  private DefaultCategoryDataset sixDS;
  private DefaultCategoryDataset sevenDS;
  private DefaultCategoryDataset eightDS;

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
  
  boolean singleAxis = false;
  
  /**
   * Constructor
   * 
   * @param statspackPanel
   * @param buttonName
   * @param scrollP
   * @param statusBar
   */
  public SPStatisticsB(StatspackAWRInstancePanel statspackPanel,String buttonName, JScrollPane scrollP, JLabel statusBar) {
    super(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.statspackAWRInstancePanel = statspackPanel;
  }

  /**
   * Performs the user selected 
   * 
   */
  public void actionPerformed() {
    try {
      statspackAWRInstancePanel.sanityCheckRange();
      String[] statistics = getStatisticNames();
      
      if (statistics.length > 0) {
        ChartPanel myChartPanel = createChart(statistics,singleAxis); 
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }
    }
    catch (ReStartedDBException ee) {
      JOptionPane.showMessageDialog(scrollP,ee,"Error...",JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  public ChartPanel createChart(String[] statistics,boolean singleAxis) throws Exception {
    this.singleAxis = singleAxis;
    
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    
    oneDS = new DefaultCategoryDataset();
    if (!singleAxis) {
      twoDS = new DefaultCategoryDataset();
      threeDS = new DefaultCategoryDataset();
      fourDS = new DefaultCategoryDataset();
      fiveDS = new DefaultCategoryDataset();
      sixDS = new DefaultCategoryDataset();
      sevenDS = new DefaultCategoryDataset();
      eightDS = new DefaultCategoryDataset();
    }

    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
            
      String cursorId;
      
      if (ConsoleWindow.getDBVersion() >= 10 && Properties.isAvoid_awr() == false) {
        cursorId = "stats$StatisticsAWR.sql";        
      }
      else {
        cursorId = "stats$Statistics.sql";
      }
      
      
      QueryResult myResult;
      String[][] resultSet;
      String statisticName = "";
      long statisticValue = 0;
      String label;
      
      for (int j=0; j < statistics.length; j++) {
        
        Parameters myPars = new Parameters();
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());    
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
        myPars.addParameter("String",statistics[j]);
        
        myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),statistics[j]);
        
        resultSet = myResult.getResultSetAsStringArray();

        statisticName = resultSet[0][0];
        statisticValue = Long.valueOf(resultSet[0][1]).longValue();
        label = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
        
        if (singleAxis) {
          oneDS.addValue(statisticValue,statisticName,label);
        }
        else {
          if (j==0) oneDS.addValue(statisticValue,statisticName,label);
          if (j==1) twoDS.addValue(statisticValue,statisticName,label);
          if (j==2) threeDS.addValue(statisticValue,statisticName,label);
          if (j==3) fourDS.addValue(statisticValue,statisticName,label);
          if (j==4) fiveDS.addValue(statisticValue,statisticName,label);
          if (j==5) sixDS.addValue(statisticValue,statisticName,label);
          if (j==6) sevenDS.addValue(statisticValue,statisticName,label);
          if (j==7) eightDS.addValue(statisticValue,statisticName,label);
          
        }
      }  
      i++;
    }
    
    String rangeAxisLabel = statistics[0];
    if (singleAxis) rangeAxisLabel = "Value of Statistic";
    
    // all values now added to the datasets 
    JFreeChart myChart = ChartFactory.createLineChart(
                              "",
                              "",
                              rangeAxisLabel,
                              oneDS,
                              PlotOrientation.VERTICAL,
                              true,
                              true,
                              true
                            );
     
    myChart.setBackgroundPaint(Color.WHITE);
    myChart.getLegend().setBorder(0,0,0,0);
    myChart.getLegend().setItemFont(new Font("SansSerif",     Font.PLAIN, 10));

    // set angle of labels on the domain axis 
    CategoryPlot myPlot = (CategoryPlot)myChart.getPlot();
    CategoryAxis myDomainAxis = myPlot.getDomainAxis();
    myDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
    myPlot.setBackgroundPaint(Color.WHITE);
    
    // set the line shape 
    CategoryItemRenderer myRenderer = new LineAndShapeRenderer();
    myRenderer.setSeriesShape(0,new Rectangle(0,0));      
    myPlot.setRenderer(0,myRenderer);
    for(int j=0; j < statistics.length; j++) {
      myRenderer.setSeriesShape(j, new Rectangle(0, 0));
      myPlot.setRenderer(j, myRenderer);
      
    }
    
    // set axis color scheme 
    myRenderer.setSeriesPaint(0,Color.RED);
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    if (!singleAxis) {
      myAxis.setLabelPaint(axisPaint);
      myAxis.setTickLabelPaint(axisPaint);
      myAxis.setAxisLinePaint(axisPaint);
    }

    myAxis.setLowerBound(0.0);
    
    // add additional axis for each dataset 
    if (!singleAxis) {
      if (statistics.length > 1) setupAxis(1,myPlot,twoDS,"RIGHT",statistics[1]);
      if (statistics.length > 2) setupAxis(2,myPlot,threeDS,"LEFT",statistics[2]);
      if (statistics.length > 3) setupAxis(3,myPlot,fourDS,"RIGHT",statistics[3]);
      if (statistics.length > 4) setupAxis(4,myPlot,fiveDS,"LEFT",statistics[4]);
      if (statistics.length > 5) setupAxis(5,myPlot,sixDS,"RIGHT",statistics[5]);
      if (statistics.length > 6) setupAxis(6,myPlot,sevenDS,"LEFT",statistics[6]);
      if (statistics.length > 7) setupAxis(7,myPlot,eightDS,"RIGHT",statistics[7]);
    }
    else {
      // set the color of all datasets
      myRenderer.setSeriesPaint(0,Color.RED);
      myRenderer.setSeriesPaint(1,Color.BLUE);
      myRenderer.setSeriesPaint(2,Color.ORANGE);
      myRenderer.setSeriesPaint(3,Color.GREEN);
      myRenderer.setSeriesPaint(4,Color.MAGENTA);
      myRenderer.setSeriesPaint(5,Color.PINK);
      myRenderer.setSeriesPaint(6,Color.LIGHT_GRAY);
      myRenderer.setSeriesPaint(7,Color.BLACK);
    }
    
    Font myFont = myDomainAxis.getTickLabelFont();
    String fontName = myFont.getFontName();
    int fontStyle = myFont.getStyle();
    if (statspackAWRInstancePanel.getSnapshotCount() >= 75) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,5));        
    }
    else if (statspackAWRInstancePanel.getSnapshotCount() >= 50) {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,6));        
    }
    else {
      myDomainAxis.setTickLabelFont(new Font(fontName,fontStyle,8));        
    }

    ChartPanel myChartPanel = new ChartPanel(myChart);
    
    return myChartPanel;
  }
  
   /**
   * Capture up to 8 statistics from the user
   * @return 
   */
  private String[] getStatisticNames() {
    String cursorId;
    QueryResult myResult;
    String[] selectedStatistics = new String[8];  // statistics to chart 
    int numStats = 0;
    
    // Get a list of valid statistics 
    cursorId = "stats$getStatisticName.sql";
    
    String[] statistics = {" "};
    
    try {
      myResult = ExecuteDisplay.execute(cursorId,false,true,null);
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      statistics = new String[myResult.getNumRows() + 1];
      statistics[0] = "None";
      for (int i=1; i < statistics.length; i++) {
        statistics[i] = resultSet[i-1][0];
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    // construct the panel to allow a user to choose statistics 
    JPanel stOneP = new JPanel();
    JLabel stOneL = new JLabel("Statistic: ");
    JComboBox stOneCB = new JComboBox(statistics);
    stOneCB.setSelectedIndex(oneSelectedIndex);
    stOneP.add(stOneL);
    stOneP.add(stOneCB);
    
    JPanel stTwoP = new JPanel();
    JLabel stTwoL = new JLabel("Statistic: ");
    JComboBox stTwoCB = new JComboBox(statistics);
    stTwoCB.setSelectedIndex(twoSelectedIndex);
    stTwoP.add(stTwoL);
    stTwoP.add(stTwoCB);
    
    JPanel stThreeP = new JPanel();
    JLabel stThreeL = new JLabel("Statistic: ");
    JComboBox stThreeCB = new JComboBox(statistics);
    stThreeCB.setSelectedIndex(threeSelectedIndex);
    stThreeP.add(stThreeL);
    stThreeP.add(stThreeCB);
    
    JPanel stFourP = new JPanel();
    JLabel stFourL = new JLabel("Statistic: ");
    JComboBox stFourCB = new JComboBox(statistics);
    stFourCB.setSelectedIndex(fourSelectedIndex);
    stFourP.add(stFourL);
    stFourP.add(stFourCB);
    
    JPanel stFiveP = new JPanel();
    JLabel stFiveL = new JLabel("Statistic: ");
    JComboBox stFiveCB = new JComboBox(statistics);
    stFiveCB.setSelectedIndex(fiveSelectedIndex);
    stFiveP.add(stFiveL);
    stFiveP.add(stFiveCB);
    
    JPanel stSixP = new JPanel();
    JLabel stSixL = new JLabel("Statistic: ");
    JComboBox stSixCB = new JComboBox(statistics);
    stSixCB.setSelectedIndex(sixSelectedIndex);
    stSixP.add(stSixL);
    stSixP.add(stSixCB);
    
    JPanel stSevenP = new JPanel();
    JLabel stSevenL = new JLabel("Statistic: ");
    JComboBox stSevenCB = new JComboBox(statistics);
    stSevenCB.setSelectedIndex(sevenSelectedIndex);
    stSevenP.add(stSevenL);
    stSevenP.add(stSevenCB);
    
    JPanel stEightP = new JPanel();
    JLabel stEightL = new JLabel("Statistic: ");
    JComboBox stEightCB = new JComboBox(statistics);
    stEightCB.setSelectedIndex(eightSelectedIndex);
    stEightP.add(stEightL);
    stEightP.add(stEightCB);
   
    JPanel singleAxisP = new JPanel();
    JCheckBox singleAxisCB = new JCheckBox("Use a single range axis for all datasets");
    singleAxisCB.setSelected(false);
    singleAxisP.add(singleAxisCB);
    
    String msg = "Select up to 8 different statistics...";
    Object[] options = {msg, stOneP, stTwoP, stThreeP, stFourP, stFiveP, stSixP, stSevenP, stEightP, singleAxisCB};
    
    int result = JOptionPane.showOptionDialog(this,options,"Statistics...",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
    
    if (result == 0) {
      String statistic = String.valueOf(stOneCB.getSelectedItem());
      oneSelectedIndex = stOneCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;
   
      statistic = String.valueOf(stTwoCB.getSelectedItem());
      twoSelectedIndex = stTwoCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stThreeCB.getSelectedItem());
      threeSelectedIndex = stThreeCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stFourCB.getSelectedItem());
      fourSelectedIndex = stFourCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stFiveCB.getSelectedItem());
      fiveSelectedIndex = stFiveCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stSixCB.getSelectedItem());
      sixSelectedIndex = stSixCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stSevenCB.getSelectedItem());
      sevenSelectedIndex = stSevenCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;   
  
      statistic = String.valueOf(stEightCB.getSelectedItem());
      eightSelectedIndex = stEightCB.getSelectedIndex();
      if (!statistic.equals("None")) selectedStatistics[numStats++] = statistic;
      
      if (singleAxisCB.isSelected()) { 
        singleAxis = true;
      }
      else {
        singleAxis = false;
      }
     
      // resize the statistics array to the correct size 
      String[] tmp = new String[numStats];
      System.arraycopy(selectedStatistics,0,tmp,0,numStats);
      selectedStatistics = new String[numStats];
      System.arraycopy(tmp,0,selectedStatistics,0,numStats);
    }
    else {
      selectedStatistics = new String[0];
    }
    
    return selectedStatistics;
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
    if (i==0) myRenderer.setSeriesPaint(0,Color.RED);
    if (i==1) myRenderer.setSeriesPaint(0,Color.BLUE);
    if (i==2) myRenderer.setSeriesPaint(0,Color.ORANGE);
    if (i==3) myRenderer.setSeriesPaint(0,Color.GREEN);
    if (i==4) myRenderer.setSeriesPaint(0,Color.MAGENTA);
    if (i==5) myRenderer.setSeriesPaint(0,Color.PINK);
    if (i==6) myRenderer.setSeriesPaint(0,Color.LIGHT_GRAY);
    if (i==7) myRenderer.setSeriesPaint(0,Color.BLACK);
    myPlot.setRenderer(i,myRenderer);
    
    // set axis color scheme 
    Paint axisPaint = myRenderer.getSeriesPaint(0);
    myAxis.setLabelPaint(axisPaint);
    myAxis.setTickLabelPaint(axisPaint);
    myAxis.setAxisLinePaint(axisPaint);     
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    myAxis.setLowerBound(0.0);
  }
}