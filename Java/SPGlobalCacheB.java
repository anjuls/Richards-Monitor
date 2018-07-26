/*
 * SPGlobalCacheB.java        17.40 05/02/10
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Implements a query to show a summary of each database session.
 */
public class SPGlobalCacheB extends JButton {
  JLabel statusBar;     // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;  // The JScrollPane on which output should be displayed 
  String[] options;     // The options listed for this button
  StatspackAWRInstancePanel statspackAWRInstancePanel;   // parent panel

  private DefaultCategoryDataset globalCacheEfficiencyDS;
  private DefaultCategoryDataset globalCacheLoadProfileDS;
  private DefaultCategoryDataset globalCacheEnqueueServicesWorkloadCharacteristicsDS;
  private DefaultCategoryDataset globalCacheEnqueueServicesMessagingStatisticsDS;

  int[] snapIdRange;                         // list of the snap_id's in the range to be charted 
  String[] snapDateRange;                    // list of snap dates in the range to be charted 

  boolean debug = false;
  
  /**
   * Constructor
   * 
   * @param options 
   * @param scrollP 
   * @parem statusBar
   */
  public SPGlobalCacheB(StatspackAWRInstancePanel statspackPanel,String[] options, JScrollPane scrollP, JLabel statusBar) {
    this.setText("Global Cache");
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
          
      if (selection.equals("Global Cache Efficiency")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 2) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createGlobalCacheEfficiencyChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }      

      if (selection.equals("Global Cache Load Profile")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 2) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createGlobalCacheLoadProfileChart();
        ChartPanel myChartPanel = new ChartPanel(myChart);
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }       
      
      if (selection.equals("Global Cache and Enqueue Services - Workload Characteristics")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 2) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createGCESWorkloadCharacteristics();
        ChartPanel myChartPanel = new ChartPanel(myChart);
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      }      
      
      if (selection.equals("Global Cache and Enqueue Services - Messaging Statistics")) {
        if (statspackAWRInstancePanel.getSnapshotCount() < 2) throw new NoSnapshotsException("Not enough snapshots!");
        JFreeChart myChart = createGCESMessagingStatistics();
        ChartPanel myChartPanel = new ChartPanel(myChart);
        
        statspackAWRInstancePanel.displayChart(myChartPanel);
      } 
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }

  
  public JFreeChart createGlobalCacheEfficiencyChart() throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    globalCacheEfficiencyDS = new DefaultCategoryDataset();
    
    
    int startSnapId;
    int endSnapId;
    int i = 0;

    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];

      float physicalReads = getSysstatAWR(startSnapId, endSnapId, "physical reads cache");
      float gets = getSysstatAWR(startSnapId, endSnapId, "consistent gets from cache");
      gets = gets + getSysstatAWR(startSnapId, endSnapId, "db block gets from cache");
      float gccrrv = getSysstatAWR(startSnapId, endSnapId, "gc cr blocks received");
      float gccurv = getSysstatAWR(startSnapId, endSnapId, "gc current blocks received");
      
      double localCache = 100*(1-(physicalReads + gccrrv + gccurv)/gets);
      double remoteCache = 100*(gccurv + gccrrv)/gets;
      double disk = 100*physicalReads/gets;
      
      String snapshotPeriod = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
      globalCacheEfficiencyDS.addValue(localCache,"Buffer Access - local cache %",snapshotPeriod);
      globalCacheEfficiencyDS.addValue(remoteCache,"Buffer Access - remote cache %",snapshotPeriod);
      globalCacheEfficiencyDS.addValue(disk,"Buffer Access - disk %",snapshotPeriod);
    
      // add events that did not occur in this snapshot but did in previous 
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;
  
    myChart = ChartFactory.createLineChart(
                            "",
                            "",
                            "",
                            globalCacheEfficiencyDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }  
  public JFreeChart createGlobalCacheLoadProfileChart() throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    globalCacheLoadProfileDS = new DefaultCategoryDataset();
    
    
    int startSnapId;
    int endSnapId;
    int i = 0;

    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];

      float gccrrv = getSysstatAWR(startSnapId, endSnapId, "gc cr blocks received");
      float gccurv = getSysstatAWR(startSnapId, endSnapId, "gc current blocks received");
      float gccusv = getSysstatAWR(startSnapId, endSnapId, "gc current blocks served");
      float gccrsv = getSysstatAWR(startSnapId, endSnapId,"gc cr blocks served");
      float dbfr = getSysstatAWR(startSnapId, endSnapId,"DBWR fusion writes");
      float pmrv = getDLMAWR(startSnapId, endSnapId, "gcs msgs received");
      float npmrv = getDLMAWR(startSnapId, endSnapId, "ges msgs received");
      float dpms = getDLMAWR(startSnapId, endSnapId, "gcs messages sent");
      float dnpms = getDLMAWR(startSnapId, endSnapId, "ges messages sent");
      int elapsed = getSnapshotRunTime(startSnapId,endSnapId);
      int blockSize = getParameterAWR(startSnapId, "db_block_size");
      
      double globalCacheBlocksReceived = (gccurv + gccrrv) / elapsed;
      double globalCacheBlocksServed = (gccusv + gccrsv) / elapsed;
      double gcsgesMessagesReceived = (pmrv + npmrv) / elapsed;
      double gcsgesMessagesSent = (dpms + dnpms) / elapsed;
      double dbwrFusionWrites = dbfr / elapsed;
      double estInterconnectTraffic = (((gccrrv + gccurv + gccrsv + gccusv) * blockSize) + ((dpms + dnpms + pmrv + npmrv) * 200)) /1024 / elapsed;
      
      String snapshotPeriod = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
      globalCacheLoadProfileDS.addValue(globalCacheBlocksReceived,"Global Cache blocks received",snapshotPeriod);
      globalCacheLoadProfileDS.addValue(globalCacheBlocksServed,"Global Cache blocks served",snapshotPeriod);
      globalCacheLoadProfileDS.addValue(gcsgesMessagesReceived,"GCS/GES messages received",snapshotPeriod);
      globalCacheLoadProfileDS.addValue(gcsgesMessagesSent,"GCS/GES messages Sent",snapshotPeriod);
      globalCacheLoadProfileDS.addValue(dbwrFusionWrites,"DBWR Fusion writes",snapshotPeriod);
      globalCacheLoadProfileDS.addValue(estInterconnectTraffic,"Est Interconnect traffic (KB)",snapshotPeriod);
    
      // add events that did not occur in this snapshot but did in previous 
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    myChart = ChartFactory.createLineChart(
                            "",
                            "",
                            "per second",
                            globalCacheLoadProfileDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }  
  
  public JFreeChart createGCESWorkloadCharacteristics() throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    globalCacheEnqueueServicesWorkloadCharacteristicsDS = new DefaultCategoryDataset();
    
    
    int startSnapId;
    int endSnapId;
    int i = 0;

    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];

      float glag = getSysstatAWR(startSnapId, endSnapId, "global enqueue gets async");
      float glsg = getSysstatAWR(startSnapId, endSnapId, "global enqueue gets sync");
      float glgt = getSysstatAWR(startSnapId, endSnapId, "global enqueue get time");
      float gccrrv = getSysstatAWR(startSnapId, endSnapId, "gc cr blocks received");
      float gccrrt = getSysstatAWR(startSnapId, endSnapId, "gc cr block receive time");
      float gccrsv = getSysstatAWR(startSnapId, endSnapId,"gc cr blocks served");
      float gccurv = getSysstatAWR(startSnapId, endSnapId, "gc current blocks received");
      float gccurt = getSysstatAWR(startSnapId, endSnapId, "gc current block receive time");
      float gccrbt = getSysstatAWR(startSnapId, endSnapId, "gc cr block build time");
      float gccrst = getSysstatAWR(startSnapId, endSnapId, "gc cr block send time");
      float gccrft = getSysstatAWR(startSnapId, endSnapId, "gc cr block flush time");
      float gccrfl = getCRFlushesAWR(startSnapId, endSnapId);
      float gccufl = getCurrentFlushesAWR(startSnapId, endSnapId);
      float gccusv = getSysstatAWR(startSnapId, endSnapId, "gc current blocks served");
      float gccupt = getSysstatAWR(startSnapId, endSnapId, "gc current block pin time");
      float gccust = getSysstatAWR(startSnapId, endSnapId, "gc current block send time");
      float gccuft = getSysstatAWR(startSnapId, endSnapId, "gc current block flush time");
      
      double avgGlobalEnqueueGetTime;
      if (glag + glsg == 0) {
        avgGlobalEnqueueGetTime = 0;
      }
      else {
        avgGlobalEnqueueGetTime = (glgt / (glag + glsg)) * 10;
      }
  
      double avgGlobalCacheCRBlockReceiveTime;
      if (gccrrv == 0) {
        avgGlobalCacheCRBlockReceiveTime = 0;
      }
      else {
        avgGlobalCacheCRBlockReceiveTime = 10 * gccrrt / gccrrv;
      }
      
      double avgGlobalCacheCurrentBlockReceiveTime;
      if (gccurv == 0) {
        avgGlobalCacheCurrentBlockReceiveTime = 0;
      }
      else
      {
        avgGlobalCacheCurrentBlockReceiveTime = 10 * gccurt / gccurv;
      }

      double avgGlobalCacheCRBlockBuildTime;
      if (gccrsv == 0) {
        avgGlobalCacheCRBlockBuildTime = 0;
      }
      else {
        avgGlobalCacheCRBlockBuildTime = 10 * gccrbt / gccrsv;
      }
      
      double avgGlobalCacheCRBlockSendTime;
      if (gccrsv == 0) {
        avgGlobalCacheCRBlockSendTime = 0;
      }
      else {
        avgGlobalCacheCRBlockSendTime = 10 * gccrst / gccrsv;
      }
      
      double avgGlobalCacheCRBlockFlushTime;
      if (gccrfl == 0) {
        avgGlobalCacheCRBlockFlushTime = 0;
      }
      else {
        avgGlobalCacheCRBlockFlushTime = 10 * gccrft / gccrfl;
      }
      
      double globalCacheLogFlushesForCRBlocksServedPCT;
      if (gccrsv == 0) {
        globalCacheLogFlushesForCRBlocksServedPCT = 0;
      }
      else {
        globalCacheLogFlushesForCRBlocksServedPCT = 100 * (gccrfl / gccrsv);
      }
      
      double avgGlobalCacheCurrentBlockPinTime;
      if (gccusv == 0) {
        avgGlobalCacheCurrentBlockPinTime = 0;
      }
      else {
        avgGlobalCacheCurrentBlockPinTime = 10 * gccupt / gccusv;
      }

      double avgGlobalCacheCurrentBlockSendTime;
      if (gccusv == 0) {
        avgGlobalCacheCurrentBlockSendTime = 0;
      }
      else {
        avgGlobalCacheCurrentBlockSendTime = 10 * gccust / gccusv;
      }
      
      double avgGlobalCacheCurrentBlockFlushTime;
      if (gccufl == 0) {
        avgGlobalCacheCurrentBlockFlushTime = 0;
      }
      else {
        avgGlobalCacheCurrentBlockFlushTime = 10 * gccuft / gccufl;
      }
      
      double globalCacheLogFlushesForCurrentBlocksServed;
      if (gccusv == 0) {
        globalCacheLogFlushesForCurrentBlocksServed = 0;
      }
      else {
        globalCacheLogFlushesForCurrentBlocksServed = 100 * (gccufl / gccusv);
      }
      


      
      String snapshotPeriod = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalEnqueueGetTime,"Avg global enqueue get time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCRBlockReceiveTime,"Avg global cache cr block receive time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCurrentBlockReceiveTime,"Avg global cache current block receive time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCRBlockBuildTime,"Avg global cache cr block built time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCRBlockSendTime,"Avg global cache cr block send time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCRBlockFlushTime,"Avg global cache cr block flush time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(globalCacheLogFlushesForCRBlocksServedPCT,"Global cache log flushes for cr blocks served %",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCurrentBlockPinTime,"Avg global cache current block pin time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCurrentBlockSendTime,"Avg global cache current block send time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(avgGlobalCacheCurrentBlockFlushTime,"Avg global cache current block flush time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesWorkloadCharacteristicsDS.addValue(globalCacheLogFlushesForCurrentBlocksServed,"Global cache log flushes for current blocks server %",snapshotPeriod);
    
      // add events that did not occur in this snapshot but did in previous 
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    myChart = ChartFactory.createLineChart(
                            "",
                            "",
                            "",
                            globalCacheEnqueueServicesWorkloadCharacteristicsDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }  
  
  public JFreeChart createGCESMessagingStatistics() throws Exception {
    statspackAWRInstancePanel.clearScrollP();
    snapIdRange = statspackAWRInstancePanel.getSelectedSnapIdRange();
    snapDateRange = statspackAWRInstancePanel.getSelectedSnapDateRange();
    statspackAWRInstancePanel.sanityCheckRange();
    globalCacheEnqueueServicesMessagingStatisticsDS = new DefaultCategoryDataset();
    
    int startSnapId;
    int endSnapId;
    int i = 0;
    
    while (i < snapIdRange.length -1) {
      startSnapId = snapIdRange[i];
      endSnapId = snapIdRange[i +1];
      
      
      float msgsq = getDLMAWR(startSnapId, endSnapId, "msgs sent queued");
      float msgsqt = getDLMAWR(startSnapId, endSnapId, "msgs sent queue time (ms)");
      float msgsqk = getDLMAWR(startSnapId, endSnapId, "msgs sent queued on ksxp");
      float msgsqtk = getDLMAWR(startSnapId, endSnapId, "msgs sent queue time on ksxp (ms)");
      float msgrq = getDLMAWR(startSnapId, endSnapId, "msgs received queued");
      float msgrqt = getDLMAWR(startSnapId, endSnapId, "msgs received queue time (ms)");
      float pmrv = getDLMAWR(startSnapId, endSnapId, "gcs msgs received");
      float npmrv = getDLMAWR(startSnapId, endSnapId, "ges msgs received");
      float pmpt = getDLMAWR(startSnapId, endSnapId, "gcs msgs process time(ms)");
      float npmpt = getDLMAWR(startSnapId, endSnapId, "ges msgs process time(ms)");
      float dmsd = getDLMAWR(startSnapId, endSnapId, "messages sent directly");
      float dmsi = getDLMAWR(startSnapId, endSnapId, "messages sent indirectly");
      float dmfc = getDLMAWR(startSnapId, endSnapId, "messages flow controlled");
           
                                                                                  
      double avgMessageSentQueueTime;
      if (msgsq == 0) {
        avgMessageSentQueueTime = 0;
      }
      else {
        avgMessageSentQueueTime = msgsqt / msgsq;
      }
                                                                                        
      double avgMessageQueueTimeOnKSXP;
      if (msgsqk == 0) {
        avgMessageQueueTimeOnKSXP = 0;
      }
      else {
        avgMessageQueueTimeOnKSXP = msgsqtk / msgsqk;
      }
                                                                                           
      double avgMessageReceivedQueueTime;
      if (msgrq == 0) {
        avgMessageReceivedQueueTime = 0;
      }
      else {
        avgMessageReceivedQueueTime = msgrqt / msgrq;
      }
                                                                                  
      double avgGCSMessageProcessTime;
      if (pmrv == 0) {
        avgGCSMessageProcessTime = 0;
      }
      else {
        avgGCSMessageProcessTime = pmpt / pmrv;
      }
                                                                                  
      double avgGESMessageProcessTime;
      if (npmrv == 0) {
        avgGESMessageProcessTime = 0;
      }
      else {
        avgGESMessageProcessTime = npmpt / npmrv;
      }
                                                                    
      double pctOfDirectSentMessages;
      if (dmsd + dmsi + dmfc == 0) {
        pctOfDirectSentMessages = 0;
      }
      else {
        pctOfDirectSentMessages = (100 * dmsd) / (dmsd + dmsi + dmfc);
      }
                                                                  
      double pctOfIndirectSentMessages;
      if (dmsd + dmsi + dmfc == 0) {
        pctOfIndirectSentMessages = 0;
      }
      else {
        pctOfIndirectSentMessages = (100 * dmsi) / (dmsd + dmsi + dmfc);
      }
                                                                     
      double pctFlowControlledMessages;
      if (dmsd + dmsi + dmfc ==0 ) {
        pctFlowControlledMessages = 0;
      }
      else {
        pctFlowControlledMessages = 100 * dmfc / (dmsd + dmsi + dmfc);
      }
      
      String snapshotPeriod = snapDateRange[i] + " - " + snapDateRange[i+1].substring(9);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(avgMessageSentQueueTime,"Avg message sent queue time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(avgMessageQueueTimeOnKSXP,"Avg message queue time on ksfp (ms)",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(avgMessageReceivedQueueTime,"Avg message received queue time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(avgGCSMessageProcessTime,"Avg GCS message process time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(avgGESMessageProcessTime,"Avg GES message process time (ms)",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(pctOfDirectSentMessages,"% of direct sent messages",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(pctOfIndirectSentMessages,"% of indirect sent messages",snapshotPeriod);
      globalCacheEnqueueServicesMessagingStatisticsDS.addValue(pctFlowControlledMessages,"% of flow controlled messages",snapshotPeriod);
    
      // add events that did not occur in this snapshot but did in previous 
      i++;
    }
    
    // all values now added to the datasets 
    JFreeChart myChart;

    myChart = ChartFactory.createLineChart(
                            "",
                            "",
                            "",
                            globalCacheEnqueueServicesMessagingStatisticsDS,
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
    
    // configure the events axis 
    NumberAxis myAxis = (NumberAxis)myPlot.getRangeAxis();
    myAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelFontSize()));
    myAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, Properties.getAxisLabelTickFontSize()));
    
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
    
    return myChart;
  }  
  
  
  
  

  public float getSysstatAWR(int startSnapId, int endSnapId, String statisticName) {    
    float stat = 0;
    
    try {
      String cursorId;
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$sysstatAWR.sql";
      }
      else {
        cursorId = "stats$sysstat.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String",statisticName);  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),statisticName);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
  
      stat = Float.valueOf(resultSet[1][0]).floatValue() - Float.valueOf(resultSet[0][0]).floatValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return stat;
  }
  public int getParameterAWR(int startSnapId, String parameterName) {    
    int stat = 0;
    
    try {
      String cursorId;
      
//      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$parameterAWR.sql";
//      }
//      else {
//        cursorId = "stats$sysstat.sql";
//      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String",parameterName);  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, 0, statspackAWRInstancePanel.getInstanceName(),parameterName);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
       
      stat = Integer.valueOf(resultSet[0][0]).intValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return stat;
  }

  public float getDLMAWR(int startSnapId, int endSnapId, String statisticName) {    
    float stat = 0;
    
    try {
      String cursorId;
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$DlmMiscAWR.sql";
      }
      else {
        cursorId = "stats$DlmMisc.sql";
      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("String",statisticName);  
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),statisticName);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
  
      if (myResult.getNumRows() > 0) {
        stat = Float.valueOf(resultSet[1][0]).floatValue() - Float.valueOf(resultSet[0][0]).floatValue();
      }
      else {
        stat = 0;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return stat;
  }
  
  public float getCRFlushesAWR(int startSnapId, int endSnapId) {    
    float stat = 0;
    
    try {
      String cursorId;
      
  //      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$CRFlushesAWR.sql";
  //      }
  //      else {
  //        cursorId = "stats$DlmMisc.sql";
  //      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
       
      if (myResult.getNumRows() > 0) {
        stat = Float.valueOf(resultSet[0][0]).floatValue();
      }
      else {
        stat = 0;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return stat;
  }
  
  public float getCurrentFlushesAWR(int startSnapId, int endSnapId) {    
    float stat = 0;
    
    try {
      String cursorId;
      
  //      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$CurrentFlushesAWR.sql";
  //      }
  //      else {
  //        cursorId = "stats$DlmMisc.sql";
  //      }
      
      Parameters myPars = new Parameters();
      myPars.addParameter("int",startSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      myPars.addParameter("int",endSnapId);
      myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
      myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
      
//      QueryResult myResult = ExecuteDisplay.execute(cursorId, myPars, false, true, null);
      /*
       * Check whether the result has already been cached before querying the database
       */
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
  
      if (myResult.getNumRows() > 0) {
        stat = Float.valueOf(resultSet[0][0]).floatValue();
      }
      else {
        stat = 0;
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }

    return stat;
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
  
  /**
   * Returns the number of seconds between 2 snapshots (rounded to the nearest second)
   * 
   * @param startSnapId 
   * @param endSnapId 
   * @return int
   */
  private int getSnapshotRunTime(int startSnapId, int endSnapId) {
    int runTime = 0; 
    
    try {
      String cursorId;
      Parameters myPars = new Parameters();
      
      if (Properties.isAvoid_awr() == false && ConsoleWindow.getDBVersion() >= 10) {
        cursorId = "stats$SnapShotRunTimeAWR.sql";
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      else {
        cursorId = "stats$SnapShotRunTime.sql";
        myPars.addParameter("int",startSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId()); 
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());  
        myPars.addParameter("int",endSnapId);
        myPars.addParameter("long", statspackAWRInstancePanel.getDbId());     
        myPars.addParameter("int", statspackAWRInstancePanel.getInstanceNumber());
      }
      

  
        
      QueryResult myResult = ExecuteDisplay.executeAWR(cursorId, myPars, false, startSnapId, endSnapId, statspackAWRInstancePanel.getInstanceName(),null);
      
      String[][] resultSet = myResult.getResultSetAsStringArray();
      
      runTime = Integer.valueOf(resultSet[0][0]).intValue();
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
    
    return runTime;
  }  
}