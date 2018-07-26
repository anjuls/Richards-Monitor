/*
 * ExecutionPlan.java        12.15 05/01/04
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
 * Change History
 * ==============
 *                         
 * 23/03/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 10/05/05 Richard Wright Resolved bug which prevented correct fetching of 
 *                         sql ready to be explain planned.
 * 10/11/05 Richard Wright If generating an execution plan on 9.2 or higher 
 *                         dbms_xplan will be used.  If 9.2 then cursor_sharing 
 *                         must be exact.
 * 21/08/09 Richard Wright Temporary files now being deleted on exit
 * 21/08/06 Richard Wright Modified the comment style and error handling
 * 08/08/07 Richard Wright Added support for alter session set current schema by user 
 * 17/12/07 Richard Wright Modified the error message when no sql plan is produced
 * 21/12/07 Richard Wright Correctly added all lines or the error message 
 *                         produced when a plan cannot be extracted.
 * 04/11/08 Richard Wright Modified so that no temporary files are created
 * 12/10/11 Richard Wright Modified dbms_xplan calls to use 'ALL' instead of 'TYPICAL'
 */


package RichMon;

import java.awt.Color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Class to produce Execution plans for any given piece of sql (not pl/sql)
 */
public class ExecutionPlan {
  
  // pointer to other objects 
  JPanel sourcePanel;
    
  boolean debug = false;
  
  /**
   * Constructor
   * 
   * @param sourcePanel - The instantiating object
   */
  public ExecutionPlan(JPanel sourcePanel) {
    this.sourcePanel = sourcePanel;
  }
  
  /**
   * Retrieves the execution plan for a sql statement and returns the plan.
   * 
   * @param sql 
   * @return 
   * @throws Exception 
   */
  public QueryResult runExplainPlan(String sql) throws Exception {
    QueryResult SQLResult = createQueryResultFromString(sql);
    
    // if requested switch current schema 
    if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
    }
    
    // get the sqlstatement to explain from the resultSet 
      String sqlStatement = "";
      StringBuffer tmpSQL = new StringBuffer("");
      
    try {
      int numCols = SQLResult.getNumCols();
      for (int i=0; i < SQLResult.getNumRows(); i++) {
        Vector tmpSQLRow = SQLResult.getResultSetRow(i);
        for (int j=0; j < numCols; j++) tmpSQL.append(tmpSQLRow.elementAt(j));
      }
      sqlStatement = tmpSQL.toString();
    }     
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"Error getting the sql statement to explain from the resultSet");
    }
    
    // remove any trailing ';' or '/' from the sql 
    try {
      sqlStatement.trim();
      int semiColon = sqlStatement.lastIndexOf(";");
      if (semiColon >= 0) {
        sqlStatement = sqlStatement.substring(0,semiColon);
      }      
      
      int slash = sqlStatement.lastIndexOf("/");
      int lastCloseBacket = sqlStatement.lastIndexOf(")");
      if ((slash >= 0) && (slash > lastCloseBacket)&& (sqlStatement.charAt(slash -1) != '*')){
        sqlStatement = sqlStatement.substring(0,slash);
      }
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"removing trailing ; or / from the sqlStatment");
    }

    // generate random number to ensure the statement_id is unique 
    Random rand = new Random();
    int offSet = rand.nextInt(Integer.MAX_VALUE);
    
    /*
     * check for the presence of a plan table, and if missing create it
     * only applicable for db versions earlier than 10i
     */
    if (ConsoleWindow.getDBVersion() < 10.0) 
    {
      String cursorId = "doesPlanTableExist.sql";
      Cursor myCursor = new Cursor(cursorId,true);
      Parameters myPars = new Parameters();
      boolean restrictRows = false;
      QueryResult myResult = myCursor.executeQuery(myPars,restrictRows);
      
      if (myResult.getNumRows() == 0) 
      {
        // alter session back to the original if it was changed above 
        if (ConsoleWindow.getCurrentSchema().length() > 0) {
          ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
        }
        
        // build plan table 
        if (ConsoleWindow.getDBVersion() >= 10.2) {
          cursorId = "createPlanTable102.sql";
        }
        else {
          if (ConsoleWindow.getDBVersion() >= 9.2) {
            cursorId = "createPlanTable92.sql";
          }
          else {
            cursorId = "createPlanTable.sql";            
          }
        }
        Cursor planTableCursor = new Cursor(cursorId,true);
        int numRows = planTableCursor.executeUpdate(true);
        
        // put it back again 
        if (ConsoleWindow.getCurrentSchema().length() > 0) {
          ConnectWindow.getDatabase().setCurrentSchema(ConsoleWindow.getCurrentSchema());
        }
      }
    }
    
    // prepare the explain plan statement 
    sqlStatement = "EXPLAIN PLAN SET STATEMENT_ID = 'RichMon" + offSet + "' FOR " + sqlStatement.trim();

    // replace each single quote with double quote 
    /*
     * I have commented out this section because I changed the execute immediate section of richmon in 17.38
     * I'm leaving it here in case I ever change it back.
     * 
     * In essence I no longer wrap the statement being executed with a begin and end and as such it no longer seems to 
     * need this double single quote business
     */
/*    tmpSQL = new StringBuffer(sqlStatement);
    StringBuffer finalSQL = new StringBuffer();
    
    try {
      for (int i = 0; i < sqlStatement.length(); i++) {
          if (tmpSQL.charAt(i) == '\'') finalSQL.append("'");
          finalSQL.append(tmpSQL.charAt(i));
      }
      sqlStatement = finalSQL.toString(); 
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this,"replacing each single quote with a double quote");
    }  */
    
    // execute the explain plan 
    try
    {
      ConnectWindow.getDatabase().executeImmediate(sqlStatement,false,true);
      
      // alter session back to the original if it was changed above 
      if (ConsoleWindow.getCurrentSchema().length() > 0) {
      ConnectWindow.getDatabase().setCurrentSchema(ConnectWindow.getUsername());
      }
      
      String prefix;
      
      // get the execution plan 
       QueryResult myResult;
      if (ConsoleWindow.getDBVersion() < 9.2 || (ConsoleWindow.getDBVersion() == 9.2 && (!ConnectWindow.isCursorSharingExact()))) {
        String cursorId = "executionPlan.sql";
        Parameters myPars = new Parameters();
        myPars.addParameter("String","RichMon"+ offSet);
        myPars.addParameter("String","RichMon"+ offSet);
        myResult = ExecuteDisplay.execute(cursorId,myPars,false,false,null);
        
        prefix = "Plan generated using explain plan";
      }
      else {
        Cursor myCursor = new Cursor("RichMonExplainPlan" + offSet,false);
        myCursor.setSQLTxtOriginal("select * from table (dbms_xplan.display('PLAN_TABLE','RichMon" + offSet + "','ALL'))\n");
        myResult = ExecuteDisplay.execute(myCursor,false,false,null);
        prefix = "Plan generated using explain plan with dbms_xplan";
      }
          
      /*
       * remove the execution plan from the plan table if the db version < 10i
       *from 10i onwards this is not required as the plan table is implemented
       * as a global temporary table
       */
      if (ConsoleWindow.getDBVersion() < 10.0)
      {
        try {
          String cursorId = "removeExecutionPlan.sql";
          Parameters myPars = new Parameters();
          myPars.addParameter("String","RichMon"+ offSet);

          Cursor myCursor2 = new Cursor(cursorId,true);
          int numRows = myCursor2.executeUpdate(true,myPars);
        }
        catch (Exception e) {
          ConsoleWindow.displayError(e,this,"removing execution plan from Plan Table");
        }
      }
      
      return myResult;
    }
    catch (Exception e)
    {
      // the execution plan could not be explained 
      Vector errMsg = new Vector(1);
      Vector lineOne = new Vector(1);
      Vector lineTwo = new Vector(1);
      Vector lineThree = new Vector(1);
      Vector lineFour = new Vector(1);
      Vector lineFive = new Vector(1);
      Vector lineSix = new Vector(1);
      Vector lineSeven = new Vector(1);

      lineOne.add("This SQL statement could not be explained!  It might be because:");
      lineTwo.add(" ");
      lineThree.add("a) You do not have privileges on the relevant objects.  Try using file => Alter session set current schema");
      lineFour.add("b) Try turning off file => enable advanced features => use v$sqlplan");
      lineFive.add("c) Check your plan table matches the definition in $OH/rdbms/admin/utlxplan ");
      lineSix.add(" ");
      lineSeven.add(e.toString());
      errMsg.add(lineOne);
      errMsg.add(lineTwo);
      errMsg.add(lineThree);
      errMsg.add(lineFour);
      errMsg.add(lineFive);
      errMsg.add(lineSix);
      errMsg.add(lineSeven);
      
      QueryResult tmpResult = new QueryResult();
      tmpResult.setResultSet(errMsg);
      tmpResult.setNumRows(7);
      tmpResult.setExecutionTime(Long.valueOf("0").longValue());
      int[] columnWidths = {80};
      tmpResult.setColumnWidths(columnWidths);
      tmpResult.setNumCols(1);
      String[] resultHeadings = {"Warning"};
      tmpResult.setResultHeadings(resultHeadings);
      String[] columnTypes = {"VARCHAR"};
      tmpResult.setColumnTypes(columnTypes);
      tmpResult.setSelectableColumns(resultHeadings);
      
      // display the error message 
      return tmpResult;
    }
  }
  
  /**
   * Create a QueryResult object containing a result set which is made up of the
   * sqlStatement passed in.
   * 
   * @param sqlStatement 
   * @return 
   */
  private QueryResult createQueryResultFromString(String sqlStatement) {
    // create the new QueryResult 
    QueryResult newResult = new QueryResult();
    
    // split the string into lines 
    String[] lines = new String[10000];
    StringBuffer line = new StringBuffer();
    char myChar;
    int numLines = 0;
    
    for (int i=0; i < sqlStatement.length(); i++) {
      myChar = sqlStatement.charAt(i);
      if (sqlStatement.charAt(i) != '\n') {
        line.append(myChar);
      }
      else {
        lines[numLines++] = line.toString();
        line = new StringBuffer();
      }
    }
    
    lines[numLines++] = line.toString();
    
    // convert the String into a vector of vectors and add it to the QueryResult 
    Vector tmpV = new Vector(1);
    for (int i=0; i < numLines; i++) {
      tmpV.add(lines[i]); 
      newResult.addResultRow(tmpV);
      tmpV = new Vector(1);
    }

    // update the QueryResult with the number of Rows 
    newResult.setNumRows(numLines);
    
    // update the QueryResult with the column Widths 
    int maxColWidth = 40;
    for (int i=0; i < numLines; i++) {
      maxColWidth = Math.max(maxColWidth,lines[i].length());
    }
    
    int [] colWidths = new int[1];
    colWidths[0] = maxColWidth;
    newResult.setColumnWidths(colWidths);
    newResult.setNumCols(colWidths.length);
    
    String[] resultHeadings = new String[1];
    resultHeadings[0] = "Execution Plan";
    
    newResult.setResultHeadings(resultHeadings);
    
    return newResult;
  }
}