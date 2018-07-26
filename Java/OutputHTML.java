/*
 * OutputHTML.java        12.15 14/02/05
 *
 * Copyright (c) 2003 - 2010 Richard Wright
 * 5 Hollis Wood Drive, Wrecclesham, Farnham, Surrey.  GU10 4JT
 * All rights reserved.
 *
 * RichMon is a lightweight database monitoring tool.  
 * 
 * Keep up to date with the latest developement at http://richmon.blogspot.com
 * 
 * Report bugs and request new features by email to support@richmon4oracle.com Change History
 * ==============
 * 
 * 28/02/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 19/08/05 Richard Wright Corrected a spelling mistake in the header
 * 22/08/06 Richard Wright Modified the comment style and error handling
 * 15/05/06 Richard Wright Updated the comment that appears in output
 * 02/03/10 Richard Wright Made html output fixed width and added the append method to fix special characters
 * 30/03/10 Richard Wright Save the SQL statement as well as the result for a scratch panel
 * 21/02/11 Richard Wright Modified to SQL SQL_ID data to the output directory
 * 11/12/15 Richard Wright Adding the RichMon version and username to the heading, adding filtering details to the output
 */


package RichMon;

import java.io.BufferedWriter;
import java.io.File;

import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * To create and update output files for saving or spooling output.
 */
public class OutputHTML {
  private File outputFile;
  private BufferedWriter fileWriter;
  private ResultCache resultCache;
  private String panelName;
  private boolean spooling;   // true = spooling false = saving a panel's resultCache 


  /**
   * Constructor
   * 
   * @param outputFile
   * @param fileWriter
   * @param panelName
   * @param resultCache
   * @throws Exception
   */
  public OutputHTML(File outputFile,BufferedWriter fileWriter,String panelName,ResultCache resultCache) throws Exception {
    this.outputFile = outputFile;
    this.fileWriter = fileWriter;
    this.resultCache = resultCache;
    spooling = false;
    
    saveHeading();
    savePanel(panelName,resultCache,null);
  }
  
  /**
   * Constructor used when spooling the output of a panel (overviewPanel & sessionPanel).
   * 
   * @param outputFile - name of the output file to be created
   * @param fileWriter - pointer to the BufferedWriter to be used
   * @param panelName - name of the panel being saved
   * @throws java.lang.Exception
   */
   public OutputHTML(File outputFile,BufferedWriter fileWriter,String panelName) throws Exception {
     this.outputFile = outputFile;
     this.fileWriter = fileWriter;
     spooling = true;
     
     saveHeading();
     savePanel(panelName);
   }  
  
  public OutputHTML(File outputFile,BufferedWriter fileWriter,QueryResult[] results) throws Exception {
    this.outputFile = outputFile;
    this.fileWriter = fileWriter;
    spooling = false;
    
    saveSQLIDHeading();

    for (int i=0; i < results.length; i++) {
      this.saveSingleResult(results[i]);
    }
  }

  /**
   * Constructor used when saving a kill log.
   * 
   * @param outputFile - name of the output file to be created
   * @param sessionPanel
   * @param fileWriter - pointer to the BufferedWriter to be used
   * @throws java.lang.Exception
   */
  public OutputHTML(File outputFile,BufferedWriter fileWriter,SessionDetailPanel sessionPanel) throws Exception {
    this.outputFile = outputFile;
    this.fileWriter = fileWriter;
    spooling = false;
    
    // sessionDetails 
    saveSingleResult(sessionPanel.lastIteration[0]);
    // waitEventDetails 
    saveSingleResult(sessionPanel.lastIteration[1]);
    // undo 
    saveSingleResult(sessionPanel.lastIteration[2]);
    // sort 
    saveSingleResult(sessionPanel.lastIteration[3]);
    // lockDetails 
    if (sessionPanel.lastIteration[4] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[4]);
    // lockDecoded 
    if (sessionPanel.lastIteration[5] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[5]);
    // blockedSessions 
    if (sessionPanel.lastIteration[6] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[6]);
    // sessionIO 
    if (sessionPanel.lastIteration[7] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[7]);
    // sessionEvents 
    if (sessionPanel.lastIteration[8] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[8]);
    // currentSQL 
    if (sessionPanel.lastIteration[9] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[9]);
    // lastSQL 
    if (sessionPanel.lastIteration[10] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[10]);
    // executionPlan 
    if (sessionPanel.lastIteration[11] instanceof QueryResult) saveSingleResult(sessionPanel.lastIteration[11]);
  }

  /**
   * Write the name of a panel to the output file.
   * 
   * @param panelName
   * @throws java.lang.Exception
   */
  public void savePanel(String panelName) throws Exception {
    // write out the panel name 
    fileWriter.write("<p>");
    fileWriter.write("<h2>" + panelName + "</h2>\n");
    fileWriter.write("<p>Output from the " + panelName + " tab appears below this point.</p>");
    fileWriter.write("</p>");

//    fileWriter.write("<hr>");
  }
  
  /**
   * Write to the output file, all the contents of the resultCache.
   * 
   * @param panelName
   * @param resultCache
   * @throws java.lang.Exception
   */
  public void savePanel(String panelName,ResultCache resultCache,SQLCache sqlCache) throws Exception {
    this.resultCache = resultCache;
    this.panelName = panelName;
    
    int numResults = resultCache.getNumCacheEntries();
    
    // write out the panel name 

    fileWriter.write("<h2>" + panelName + "</h2>\n");
    fileWriter.write("<p>Output from the " + panelName + " tab appears below this point.</p>\n\n");

    
    // make it possible to collapse the content of this panel
    fileWriter.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    fileWriter.write("var shown" + panelName + " = \"Y\";\n");
    fileWriter.write("var bodyHTML" + panelName + ";\n");
    fileWriter.write("function showhide" + panelName + "()\n");
    fileWriter.write("{\n");
    fileWriter.write("  if (shown" + panelName + "==\"Y\")\n");
    fileWriter.write("  {\n");
    fileWriter.write("    shown" + panelName + " = \"N\";\n");
    fileWriter.write("    bodyHTML" + panelName + " = document.getElementById(\"" + panelName + "\").innerHTML;\n");
    fileWriter.write("    document.getElementById(\"" + panelName + "a\").innerHTML = '[<a href=\"javascript:showhide" + panelName + "();\" title=\"expand tab\">+</a>]';\n");
    fileWriter.write("    document.getElementById(\"" + panelName + "\").innerHTML = '&nbsp;';\n");
    fileWriter.write("  }\n");
    fileWriter.write("  else\n");
    fileWriter.write("  {\n");
    fileWriter.write("    shown" + panelName + " = \"Y\";\n");
    fileWriter.write("    document.getElementById(\"" + panelName + "a\").innerHTML = '[<a href=\"javascript:showhide" + panelName + "();\" title=\"collapse tab\">-</a>]';\n");
    fileWriter.write("    document.getElementById(\"" + panelName + "\").innerHTML = bodyHTML" + panelName + ";\n");
    fileWriter.write("  }\n");
    fileWriter.write("}\n");
    fileWriter.write("</script>\n\n\n");

    // collapse hide element
    fileWriter.write("\n\n<span id=\"" + panelName + "a\">[<a href=\"javascript:showhide" + panelName + "();\" title=\"collapse tab\">-</a>]</span>\n");
    fileWriter.write("<span id=\"" + panelName + "\">\n\n");
    
    // get each query result from the cache and write it out 
    for (int i=0; i < numResults; i++) {
      if (sqlCache instanceof SQLCache) saveSQLStatement(sqlCache.getResult(i));
      saveSingleResult(resultCache.getResult(i));
    }
    

    // close the remaining tags at the bottom of the file 
    fileWriter.write("</body>\n");
    fileWriter.write("</span>\n");

  }
  
  /**
   * Write a heading to the output file.
   * 
   * @throws java.lang.Exception
   */
   private void saveHeading() throws Exception {
     fileWriter.write("<html><head>\n");
     fileWriter.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
     fileWriter.write("</script>");
     fileWriter.write("<style type=\"text/css\">\n");
     fileWriter.write("a:active {color:#ff6600}\n");
     fileWriter.write("a:link {color:#663300}\n");
     fileWriter.write("a:visited {color:#996633}\n");
     fileWriter.write("body {font-family:courier new,monospace,arial,helvetica;font-size:9pt;background-color:#ffffff}\n");
     fileWriter.write("h1 {font-size:16pt;color:rgb(153,0,0);font-weight:bold}\n");
     fileWriter.write("h2 {font-size:14pt;color:rgb(153,0,0);font-weight:bold}\n");
     fileWriter.write("h3 {font-size:12pt;color:rgb(153,0,0);font-weight:bold}\n");
     fileWriter.write("h4 {font-size:10pt;color:rgb(153,0,0);font-weight:bold}\n");
     fileWriter.write("li {font-size:10pt;color:rgb(153,0,0)}\n");
     fileWriter.write("table {font-size:8pt;text-align:left}\n");
     fileWriter.write("th {background-color:#cccc99;color:rgb(153,0,0);vertical-align:bottom;padding-left:3pt;padding-right:3pt;padding-top:1pt;padding-bottom:1pt}\n");
     fileWriter.write("th.verticaltext {writing-mode:tb-rl;filter:flipV flipH;text-align:left;padding-left:1pt;padding-right:1pt;padding-top:3pt;padding-bottom:3pt}\n");
     fileWriter.write("td {background-color:#f7f7e7;color:#000000;vertical-align:top;padding-left:3pt;padding-right:3pt;padding-top:1pt;padding-bottom:1pt}\n");
     fileWriter.write("td.left {text-align:left}\n");
     fileWriter.write("td.right {text-align:right}\n");
     fileWriter.write("td.rightred {text-align:right;color:#ff0000}\n");
     fileWriter.write("td.rightredred {text-align:right;background-color:#ff0000}\n");
     fileWriter.write("td.title {font-weight:bold;text-align:right;background-color:#cccc99;color:rgb(153,0,0)}\n");
     fileWriter.write("td.lefttitle {font-weight:bold;text-align:left;background-color:#cccc99;color:rgb(153,0,0)}\n");
     fileWriter.write("td.white {background-color:#ffffff}\n");
     fileWriter.write("td.leftwhite {font-size:10pt;text-align:left;background-color:#ffffff}\n");
     fileWriter.write("td.verticaltext {writing-mode:tb-rl;filter:flipV flipH;text-align:left;padding-left:1pt;padding-right:1pt;padding-top:3pt;padding-bottom:3pt}\n");
     fileWriter.write("font.tablenote {font-size:8pt;font-style:italic;color:rgb(153,0,0)}\n");
     fileWriter.write("font.footer {font-style:italic;color:#999999}\n");
     fileWriter.write("</style>\n");
     fileWriter.write("</head>\n\n");
     
     fileWriter.write("<head>\n");
     fileWriter.write("<h1>\n");
     fileWriter.write("RichMon - Richards Monitoring Tool for Oracle\n");
     fileWriter.write("</h1>\n");
     fileWriter.write("</head>\n");
     fileWriter.write("<body>\n");
     fileWriter.write("<p>RichMon caches the last 100 results for each tab which is what you see in this file unless the output is from the session or overview tab in " +
                      "which case you see all the output generated, since spooling was enabled.  " +
                      "To change the number of results output for each tab, adjust the value of 'cache_size' in the RichMon.properties file.");
     fileWriter.write("</p>\n");
     fileWriter.write("<p>This output was generated from RichMon Version " + ConsoleWindow.getRichMonVersion() + " by " + System.getProperty("user.name") + "</p>\n");    
     fileWriter.write("</p>\n");
     fileWriter.write("<p>See http://richmon.blogspot.com for the latest news and updates.</p>\n");
     fileWriter.write("<hr>\n\n");
   }  
  

  private void saveSQLIDHeading() throws Exception {
    fileWriter.write("<html><head>\n");
    fileWriter.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    fileWriter.write("</script>");
    fileWriter.write("<style type=\"text/css\">\n");
    fileWriter.write("a:active {color:#ff6600}\n");
    fileWriter.write("a:link {color:#663300}\n");
    fileWriter.write("a:visited {color:#996633}\n");
    fileWriter.write("body {font-family:courier new,monospace,arial,helvetica;font-size:9pt;background-color:#ffffff}\n");
    fileWriter.write("h1 {font-size:16pt;color:rgb(153,0,0);font-weight:bold}\n");
    fileWriter.write("h2 {font-size:14pt;color:rgb(153,0,0);font-weight:bold}\n");
    fileWriter.write("h3 {font-size:12pt;color:rgb(153,0,0);font-weight:bold}\n");
    fileWriter.write("h4 {font-size:10pt;color:rgb(153,0,0);font-weight:bold}\n");
    fileWriter.write("li {font-size:10pt;color:rgb(153,0,0)}\n");
    fileWriter.write("table {font-size:8pt;text-align:left}\n");
    fileWriter.write("th {background-color:#cccc99;color:rgb(153,0,0);vertical-align:bottom;padding-left:3pt;padding-right:3pt;padding-top:1pt;padding-bottom:1pt}\n");
    fileWriter.write("th.verticaltext {writing-mode:tb-rl;filter:flipV flipH;text-align:left;padding-left:1pt;padding-right:1pt;padding-top:3pt;padding-bottom:3pt}\n");
    fileWriter.write("td {background-color:#f7f7e7;color:#000000;vertical-align:top;padding-left:3pt;padding-right:3pt;padding-top:1pt;padding-bottom:1pt}\n");
    fileWriter.write("td.left {text-align:left}\n");
    fileWriter.write("td.right {text-align:right}\n");
    fileWriter.write("td.rightred {text-align:right;color:#ff0000}\n");
    fileWriter.write("td.rightredred {text-align:right;background-color:#ff0000}\n");
    fileWriter.write("td.title {font-weight:bold;text-align:right;background-color:#cccc99;color:rgb(153,0,0)}\n");
    fileWriter.write("td.lefttitle {font-weight:bold;text-align:left;background-color:#cccc99;color:rgb(153,0,0)}\n");
    fileWriter.write("td.white {background-color:#ffffff}\n");
    fileWriter.write("td.leftwhite {font-size:10pt;text-align:left;background-color:#ffffff}\n");
    fileWriter.write("td.verticaltext {writing-mode:tb-rl;filter:flipV flipH;text-align:left;padding-left:1pt;padding-right:1pt;padding-top:3pt;padding-bottom:3pt}\n");
    fileWriter.write("font.tablenote {font-size:8pt;font-style:italic;color:rgb(153,0,0)}\n");
    fileWriter.write("font.footer {font-style:italic;color:#999999}\n");
    fileWriter.write("</style>\n");
    fileWriter.write("</head>\n\n");
    
    fileWriter.write("<head>\n");
    fileWriter.write("<h1>\n");
    fileWriter.write("RichMon - Richards Monitoring Tool for Oracle\n");
    fileWriter.write("</h1>\n");
    fileWriter.write("</head>\n");
    fileWriter.write("<body>\n");
    fileWriter.write("<p>RichMon automatically saves any data about a specific SQL_ID that you access in RichMon.  This allows you to refer back to it at a later date.");
    fileWriter.write("</p>\n");
    fileWriter.write("<p>This output was generated from RichMon Version " + ConsoleWindow.getRichMonVersion() + " by " + System.getProperty("user.name") + "</p>\n");    
    fileWriter.write("</p>\n");
    fileWriter.write("<p>See http://richmon.blogspot.com for the latest news and updates.</p>\n");
    fileWriter.write("<hr>\n\n");
  }  


  public void saveIteration(int iteration) throws Exception {   
    fileWriter.write("</span>");           // end any previous iteration that might exist
    fileWriter.write("\n<h3>Iteration " + iteration + "</h3>\n\n");
    
    // make it possible to collapse the content of this iteration
    fileWriter.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    fileWriter.write("var shown" + "Iteration" + iteration + " = \"Y\";\n");
    fileWriter.write("var bodyHTML" + "Iteration" + iteration + ";\n");
    fileWriter.write("function showhide" + "Iteration" + iteration + "()\n");
    fileWriter.write("{\n");
    fileWriter.write("  if (shown" + "Iteration" + iteration + "==\"Y\")\n");
    fileWriter.write("  {\n");
    fileWriter.write("    shown" + "Iteration" + iteration + " = \"N\";\n");
    fileWriter.write("    bodyHTML" + "Iteration" + iteration + " = document.getElementById(\"" + "Iteration" + iteration + "\").innerHTML;\n");
    fileWriter.write("    document.getElementById(\"" + "Iteration" + iteration + "a\").innerHTML = '[<a href=\"javascript:showhide" + "Iteration" + iteration + "();\" title=\"expand iteration\">+</a>]';\n");
    fileWriter.write("    document.getElementById(\"" + "Iteration" + iteration + "\").innerHTML = '&nbsp;';\n");
    fileWriter.write("  }\n");
    fileWriter.write("  else\n");
    fileWriter.write("  {\n");
    fileWriter.write("    shown" + "Iteration" + iteration + " = \"Y\";\n");
    fileWriter.write("    document.getElementById(\"" + "Iteration" + iteration + "a\").innerHTML = '[<a href=\"javascript:showhide" + "Iteration" + iteration + "();\" title=\"collapse iteration\">-</a>]';\n");
    fileWriter.write("    document.getElementById(\"" + "Iteration" + iteration + "\").innerHTML = bodyHTML" + "Iteration" + iteration + ";\n");
    fileWriter.write("  }\n");
    fileWriter.write("}\n");
    fileWriter.write("</script>\n\n\n");

    // collapse hide element
    fileWriter.write("\n\n<span id=\"" + "Iteration" + iteration + "a\">[<a href=\"javascript:showhide" + "Iteration" + iteration + "();\" title=\"collapse iteration\">-</a>]</span>\n");
    fileWriter.write("<span id=\"" + "Iteration" + iteration + "\">\n\n");

  }
  
  /**
   * Write a single QueryResult to the output file.
   * 
   * @param myResult - a QueryResult object
   * @throws java.lang.Exception
   */
   public void saveSingleResult(QueryResult myResult) throws Exception {
     // get caption details 
     int ms = (int)myResult.getExecutionTime();
     int numRows = myResult.getNumRows();
     String cursorId = myResult.getCursorId();
     String executionTime = myResult.getExecutionClockTime();
     String caption = "";
     if (cursorId.equals("scratch.sql")) {
       if (numRows < Properties.getMaxResultSetSize()) {
         caption = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
       }
       else {
         caption = "Executed in " + ms + " milleseconds returning " + myResult.getResultSetSize() + " rows @ " + executionTime + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties  ";        
       } 
     }
     else {
         if (numRows < Properties.getMaxResultSetSize()) {
         caption = "Executed SQL above in " + ms + " milleseconds returning " + numRows + " rows @ " + executionTime;
         if (myResult.getUsernameFilter() instanceof String) caption = caption + " filtered by the user " + myResult.getUsernameFilter();
         if (myResult.isFilterByRAC() && !myResult.isOnlyLocalInstanceSelected()) {
           caption = caption + " and filtered by instance ";
           String[][] selectedInstances = ConsoleWindow.getSelectedInstances();
             for (int i = 0; i < selectedInstances.length; i++) {
             caption = caption + selectedInstances[i][0];
             if (i < selectedInstances.length -1) caption = caption + ",";
           }
         }
       }
       else {
         caption = "Executed in " + ms + " milleseconds returning " + myResult.getResultSetSize() + " rows @ " + executionTime + "    ONLY " + numRows + " have been displayed.  This output has been truncated because max_resultset_size is specified in RichMon.properties";        
       } 
     }
     
     fileWriter.write("<p>" + caption + "</p>\n");
       
     // write out the table
    fileWriter.write("<kbd><table border=0 cellspacing=1 cellpadding=1>\n");

       
     // construct the table header 
     int numCols = myResult.getNumCols();
     String[] columnHeadings = myResult.getResultHeadings();
     String[] columnTypes = myResult.getColumnTypes();
     StringBuffer th = new StringBuffer("<tr>\n");
       
      for (int x=0; x < numCols; x++) {
        th.append("<th>" + columnHeadings[x] + "</th>\n");
      } 
      th.append("</tr>\n");
      
      // write the table header 
      fileWriter.write(th.toString());
       
     // write table data (each row) 
     for (int k=0; k < numRows; k++) {
       Vector resultSetRow = myResult.getResultSetRow(k);
       StringBuffer tr = new StringBuffer("<tr>");

       // output each row 
       for (int j=0; j < numCols; j++) {
         String row = "";
         if (!(resultSetRow.elementAt(j) == null)) row = resultSetRow.elementAt(j).toString();

           if (columnTypes[j].equals("NUMBER") | columnTypes[j].equals("INTEGER")) {
             tr.append("<td class=\"right\">");
             //tr.append(resultSetRow.elementAt(j) + "</td>");
             append(tr,row);
             tr.append("</td>");
           }
           else {
             tr.append("<td>"); 
             append(tr,row);
             tr.append("</td>");
           }
       }
       tr.append("</tr>\n");
        
       fileWriter.write(tr.toString());
     }
       
     // end the table tab 
     fileWriter.write("</table></kbd>");
  }  
  
  /**
   * Write a single entry to the output file.
   * 
   * @param myResult - a String object
   * @throws java.lang.Exception
   */
   public void saveSQLStatement(String myResult) throws Exception {
     // write out the table
     fileWriter.write("<p></p>\n");
     fileWriter.write("<kbd><table border=0 cellspacing=1 cellpadding=1>\n");
//    fileWriter.write("<tr><th>SQL Statement</th></tr>");
    
     // Check for ascii 13 (carriege returns) and use a new line otherwise the sql will be displayed as a single line and formatting is lost
     StringBuffer tmp = new StringBuffer();
     for (int i=0; i < myResult.length(); i++) {
       if (myResult.charAt(i) == 13) {
         fileWriter.write("<tr><td>" + tmp.toString() + "</td></tr>\n");
         tmp = new StringBuffer();
       }
       else {
         tmp.append(myResult.charAt(i));
       }
     }
     fileWriter.write("<tr><td>" + tmp.toString() + "</td></tr>\n");
     fileWriter.write("</table></kbd>\n");
  }
  
  private void append (StringBuffer sb,String inputLine) {
    if (inputLine instanceof String) {
      for (int i = 0; i < inputLine.length(); i++) {
        if (inputLine.charAt(i) == ' ') {
          sb.append("&nbsp;");
        }
        else {
          if (inputLine.charAt(i) == '<') {
            sb.append("&lt;");
          }
          else {
            if (inputLine.charAt(i) == '>') {
              sb.append("&gt;");
            }
            else {
              if (inputLine.charAt(i) == '&') {
                sb.append("&amp;");
              }
              else {
                sb.append(inputLine.charAt(i));
              }
            }
          }
        }
      }
    }
  }
}

