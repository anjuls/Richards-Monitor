/*
 * Database.java        12.15 05/01/04
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
 * Change History since 21/02/05
 * =============================
 *
 * 21/02/05 Richard Wright Switched comment style to match that
 *                         recommended in Sun's own coding conventions.
 * 02/06/05 Richard Wright Switch single row result sets to a vertical format
 * 07/11/05 Richard Wright Removed all methods related to statistics gathering and jobs
 * 05/07/06 Richard Wright Added/Amended methods to take account of 'timeout_operations' & 'timeout_execution_plans';
 * 18/08/06 Richard Wright Modified the comment style and error handling
 * 29/01/07 Richard Wright Added float to the list of datatypes supported for parameters in
 * 18/05/07 Richard Wright Added support for Date datatypes being returned
 * 06/06/07 Richard Wright Modified to use OracleDriver rather than Driver
 * 06/09/07 Richard Wright For db's 10 or greater use dbms_monitor rathar than dbms_system to trace sessions
 * 07/09/07 Richard Wright Corrected error showing the wrong number of rows in a flipped resultSet
 * 10/09/07 Richard Wright Only flip result sets where there is more than 1 column
 * 12/09/07 Richard Wright Regressed change from the 7/9/07 as causes changes in output elsewhere.
 * 15/07/08 Richard Wright Now displays timestamp datatypes, all through they appear in US format unfortunately
 * 07/10/08 Richard Wright Modifed createTuningTask to take additional parameters
 * 24/10/08 Richard Wright Modifed so that cursors are not closed after every execution
 * 29/10/08 Richard Wright Moved the processing of the oracleResultSet out to the QueryResult class
 * 30/01/09 Richard Wright Regressed the last 2 changes and implemented the jdbc statement cache instead
 * 11/08/09 Richard Wright Modified to make the class more easily reusable in other applications
 * 11/09/09 Richard Wright set the default row prefetch to 50, to reduce round trips to the database.
 * 14/09/09 Richard Wright Modifed the handling of timestamptz datatypes with a try catch to cover occasions when it fails
 * 02/10/09 Richard Wright When removing timeout operations introduced bug when call to execute immediate called itself - fixed
 * 17/11/10 Richard Wright Implement maxResultSetSize in executeQuery to prevent hanging RichMon on very very very large result sets
 * 06/12/10 Richard Wright Changed executeQuery to execute so that it is possible to run dml from the scratch panel
 * 26/02/14 Richard Wright added restrictRows boolean to allow row restriction to be turned off
 * 28/02/14 Richard Wright Fixed issue with timestamps being converted to a string (wrongly) then put into an object
 * 09/12/15 Richard Wright Fixed display of timestamps with tine zones
 */ 


package RichMon;

import java.sql.DriverManager;
import java.sql.SQLException;

import java.text.DateFormat;

import java.util.Locale;
import java.util.Properties;
import java.util.Vector;


import oracle.jdbc.*;
import oracle.sql.*; 



/**
 * Facilitates all interaction thru JDBC to a database.
 */
public class Database
{
  public OracleConnection conn = null;
  private boolean debug = false;
  private DateFormat dateFormatter;
  int maxResultSetSize = 0;

  /*
   * Settings from the parameter file that control date datatype formats
   */

  Locale myLocale;
  String myDate = "default";
  String myDateTimestamp = "default";
  String country;
  String language;

  TIMESTAMPTZ myTimestampTZ;
  TIMESTAMP myTimestamp;
  TIMESTAMP myTimestampLTZ;

  /**
   * Constructor.
   */
   public Database() {
     jbInit();
   }

   private void jbInit() {

     country = "GB";
     language = "en";
     myDate = "default";
     myLocale = new Locale(language,country);
     myDateTimestamp = "default";

     // if both are set, then unset myDate
     if (!myDate.equals("null") && !myDateTimestamp.equals("null")) myDate="null";
   }

   public void setLocale(String country,String language,String date,String dateTimestamp) {
     this.country = country.toUpperCase();
     this.language = language;
     myLocale = new Locale(language,country);
     this.myDate = date;
     this.myDateTimestamp = dateTimestamp;

     // if both are set, then unset myDate
     if (!myDate.equals("null") && !myDateTimestamp.equals("null")) myDate="null";
   }

  /**
   * Open a jdbc database connection using the oci driver.  This is a thick
   * connection which thus requires an oracle home
   *
   * @param netServiceName
   * @param username
   * @param userPassword
   * @param connectAsSYSDBA
   * @return true if a connection request was successfull, false otherwise
   * @throws Exception
   */
  public boolean openConnection(String netServiceName
                                ,String username
                                ,String userPassword
                                ,boolean connectAsSYSDBA) throws Exception {
    String driver_class = "oracle.jdbc.OracleDriver";
    String URL = "jdbc:oracle:oci:@" + netServiceName;

    Properties props = new Properties();
    props.put("user",username);
    props.put("password",userPassword);
    props.put("internal_logon","sysdba");
    props.put("SetBigStringTryClob", "true");
    props.put("connection-timeout",30);

    // Load JDBC driver
    Class.forName(driver_class);

    // Connect to the database
    if (connectAsSYSDBA) {
      conn=(OracleConnection) DriverManager.getConnection(URL,props);
    }
    else {
      conn = (OracleConnection) DriverManager.getConnection(URL,username,userPassword);
    }

    conn.setAutoCommit( false );
    conn.setDefaultRowPrefetch(50);

    // set module and client_info in v$session

    if (debug) System.out.println("New DB Connection");

    // successfull connection
    return true;
  }

  /**
   * Open a jdbc database connection using the thin driver.  This dose not
   * require an oracle home
   *
   * @param netServiceName - database name
   * @param username
   * @param userPassword
   * @param connectAsSYSDBA - not used since sysdba has no meaning with the thin driver
   * @param hostName
   * @param portNumber
   * @return true if a connection request was successfull, false otherwise
   * @throws Exception
   */
  public boolean openConnection(String netServiceName
                                ,String username
                                ,String userPassword
                                ,boolean connectAsSYSDBA
                                ,String hostName
                                ,String portNumber
                                ,boolean useServiceName) throws Exception {
    String driver_class = "oracle.jdbc.OracleDriver";
    String URL;
    
    if (useServiceName) {
      URL = "jdbc:oracle:thin:@//" + hostName + ":" + portNumber + "/" + netServiceName;
      //URL = "jdbc:oracle:thin:@" + hostName + ":" + portNumber + "/" + netServiceName;
    }
    else {
      // using a sid rather than a servicename
      URL = "jdbc:oracle:thin:@" + hostName + ":" + portNumber + ":" + netServiceName;
    }

    // Load JDBC driver
    Class.forName(driver_class);

    // Connect to the database
    if (!ConnectWindow.properties.isSetLocale()) Locale.setDefault(Locale.ENGLISH);
    conn = (OracleConnection) DriverManager.getConnection(URL,username,userPassword);

    conn.setAutoCommit( false );
    conn.setDefaultRowPrefetch(50);

    if (debug) System.out.println("New DB Connection");

    // successfull connection
    return true;
  }

  /**
   * Close a jdbc database connection.
   */
  public void closeConnection() throws Exception {
    conn.close();
  }



  public void setStatementCacheSize(int statementCacheSize) throws Exception {
    conn.setStatementCacheSize(statementCacheSize);
    conn.setImplicitCachingEnabled(true);

    if (debug) System.out.println("Set statementCacheSize to " + statementCacheSize);

  }


  public synchronized QueryResult execute(String sqlTxtToExecute, Parameters myPars, boolean flippable, boolean restrictRows) throws Exception {                 

    if (debug) System.out.println("executeQuery (preparedStatement was passed in");

    // open cursor
    OraclePreparedStatement statement = (OraclePreparedStatement)conn.prepareStatement(sqlTxtToExecute);

    // bind parameters into the query
    for (int i = 0; i < myPars.getNumParameters(); i++) {
      Object[] singleParameter = myPars.getParameter(i);
      int colIndex = i + 1;
      if (debug) System.out.println("Binding parameter: "+ singleParameter[1] + " as a " + singleParameter[0] + " for column indei " + colIndex);
      
      // Only the following data types are supported currently, but more can be added should the need arise.
      if (singleParameter[0].equals("String")) statement.setString(colIndex, String.valueOf(singleParameter[1]));
      if (singleParameter[0].equals("int")) statement.setInt(colIndex, Integer.valueOf(String.valueOf(singleParameter[1])).intValue());
      if (singleParameter[0].equals("long")) statement.setLong(colIndex, Long.valueOf(String.valueOf(singleParameter[1])).longValue());
      if (singleParameter[0].equals("float")) statement.setFloat(colIndex, Float.valueOf(String.valueOf(singleParameter[1])).floatValue());
      if (singleParameter[0].equals("byte[]")) {
        byte[] myByte = myPars.getByteParameter(i);
        statement.setBytes(colIndex, myByte);
      }
    }

    // record query start time
    long msStart = System.currentTimeMillis();     // query start time in milliseconds
    QueryResult myResult = new QueryResult();
    
    boolean query = true;

    try {
      // execute the statement
      if (ConnectWindow.properties.isAllowDML()) {

          
        query = statement.execute();
      }
      else {
        statement.executeQuery();
      }
        
      // record query execution time
      long msExecutionTime = System.currentTimeMillis() - msStart;

      
      if (query) {
        OracleResultSet oracleResultSet = (OracleResultSet)statement.getResultSet();
        populateQueryResultWithJDBCResultSet(flippable, myResult, oracleResultSet, msExecutionTime, restrictRows);
        oracleResultSet.close();
      }
      else {
        myResult.setRowsProcessed(statement.getUpdateCount());
        
        Vector resultSetRow = new Vector(1);
        String row = myResult.getRowsProcessed() + " rows processed";
        resultSetRow.addElement(row);
        myResult.addResultRow(resultSetRow);
        
        String[] resultSetHeadings = { "Result" };
        myResult.setResultHeadings(resultSetHeadings);        
        int[] colWidths = { 80 };
        myResult.setColumnWidths(colWidths);
        myResult.setNumCols(1);
        String[] colTypes = { "VARCHAR" };
        myResult.setColumnTypes(colTypes);
        
      }

      // close prepared statement and resultset
      statement.close();
      }
      catch (Exception e) {
        // close prepared statement
        statement.close();
        throw e;
      }
      finally {
        statement.close();
      }

      // return QueryResult
      return myResult;
  }   
  
  public synchronized QueryResult executeBatch(String sqlTxtToExecute
                                 ,Parameters myPars
                                 ,boolean flippable) throws Exception {                 

     if (debug) System.out.println("executeQuery (preparedStatement was passed in");

     // open cursor
     OraclePreparedStatement statement = (OraclePreparedStatement)conn.prepareStatement(sqlTxtToExecute);

     // bind parameters into the query
     for (int i = 0; i < myPars.getNumParameters(); i++) {
       Object[] singleParameter = myPars.getParameter(i);
       int colIndex = i + 1;
       if (debug) System.out.println("Binding parameter: "+ singleParameter[1] + " as a " + singleParameter[0] + " for column indei " + colIndex);
       
       // Only the following data types are supported currently, but more can be added should the need arise.
       if (singleParameter[0].equals("String")) statement.setString(colIndex, String.valueOf(singleParameter[1]));
       if (singleParameter[0].equals("int")) statement.setInt(colIndex, Integer.valueOf(String.valueOf(singleParameter[1])).intValue());
       if (singleParameter[0].equals("long")) statement.setLong(colIndex, Long.valueOf(String.valueOf(singleParameter[1])).longValue());
       if (singleParameter[0].equals("float")) statement.setFloat(colIndex, Float.valueOf(String.valueOf(singleParameter[1])).floatValue());
       if (singleParameter[0].equals("byte[]")) {
         byte[] myByte = myPars.getByteParameter(i);
         statement.setBytes(colIndex, myByte);
       }
     }

     // record query start time
     long msStart = System.currentTimeMillis();     // query start time in milliseconds
     QueryResult myResult = new QueryResult();


     try {
       // execute the statement
       statement.executeBatch();
  
       // record query execution time
       long msExecutionTime = System.currentTimeMillis() - msStart;

       
       
         myResult.setRowsProcessed(statement.getUpdateCount());
         
         Vector resultSetRow = new Vector(1);
         String row = myResult.getRowsProcessed() + " rows processed";
         resultSetRow.addElement(row);
         myResult.addResultRow(resultSetRow);
         
         String[] resultSetHeadings = { "Result" };
         myResult.setResultHeadings(resultSetHeadings);        
         int[] colWidths = { 80 };
         myResult.setColumnWidths(colWidths);
         myResult.setNumCols(1);
         String[] colTypes = { "VARCHAR" };
         myResult.setColumnTypes(colTypes);
         
       

       // close prepared statement
       statement.close();
       }
       catch (Exception e) {
         // close prepared statement
         statement.close();

         throw e;
       }

       // return QueryResult
       return myResult;
   }

  private void populateQueryResultWithJDBCResultSet(boolean flippable, QueryResult myResult, OracleResultSet oracleResultSet, long msExecutionTime, boolean restrictRows) throws SQLException {
    int[] columnWidths;
    String[] columnHeadings;
    String[] columnTypes;
    int numCols;
    
    // get the query result meta data
    OracleResultSetMetaData meta = (OracleResultSetMetaData)oracleResultSet.getMetaData();
    
    // create the QueryResult
    numCols = meta.getColumnCount();
    
    myResult.setNumCols(numCols);
    myResult.setExecutionTime(msExecutionTime);

    // create arrays to hold column headings and widths
    columnWidths = new int[numCols];
    columnHeadings = new String[numCols];
    columnTypes = new String[numCols];

    // store column headings and set initial column widths
    for (int i=0; i < numCols; i++) {
      columnHeadings[i] = meta.getColumnLabel(i+1).toUpperCase();
      columnWidths[i] = meta.getColumnLabel(i+1).length();
      columnTypes[i] = meta.getColumnTypeName(i+1);
      if (debug) System.out.println("Column Type returned by Query: " + meta.getColumnTypeName(i+1));
    }


    // update the QueryResult with column Headings
    myResult.setResultHeadings(columnHeadings);

    Object answer = new Object();      // individual column value
    String answerString = new String();
    String answerType;

    Vector resultSetRow;   // one row of query output

    // populate the resultSet vector with the query result
    while (oracleResultSet.next() && (myResult.getNumRows() < maxResultSetSize || restrictRows== false)) {
      // initialize resultSetRow
      resultSetRow = new Vector();
      // process result row
      for (int x=0; x < numCols; x++) {

        if (columnTypes[x].equals("CLOB")) {
          answerString = oracleResultSet.getString(x+1);
          answerType = "String";
        }
        else {
          if (columnTypes[x].equals("DATE")) {
            if (myDateTimestamp.equals("default")) {
              dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,myLocale);
            }
            else {
              if (myDateTimestamp.equals("long")) {
                dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG,myLocale);
              }
              else {
                if (myDateTimestamp.equals("medium")) {
                  dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,myLocale);
                }
                else {
                  if (myDateTimestamp.equals("short")) {
                    dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,myLocale);
                  }
                  else {
                    if (myDate.equals("default")) {
                      dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,myLocale);
                    }
                    else {
                      if (myDate.equals("short")) {
                        dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT,myLocale);
                      }
                      else {
                        if (myDate.equals("long")) {
                          dateFormatter = DateFormat.getDateInstance(DateFormat.LONG,myLocale);
                        }
                      }
                    }
                  }
                }
              }
            }

            if (oracleResultSet.getObject(x+1) instanceof Object) {
              answer = dateFormatter.format(oracleResultSet.getTimestamp(x + 1));
            }
            else {
              answer = null;
            }
            answerType = "Date";
          }
          else {
            if (columnTypes[x].equals("INTERVALDS")) {
            /*
             * This is not supported in the oracle9 version of Ojdbc14.jar, only in oracle10
             */
              answer = oracleResultSet.getINTERVALDS(x+1);
              answerType = "INTERVALDS";
    //                 if (debug) System.out.println("Found INTERVALSD");
                answer = oracleResultSet.getObject(x+1);
                answerType = "Object";
            }
            else {
              if (columnTypes[x].equals("TIMESTAMP")) {
                answerType = "TIMESTAMP";
                answer = oracleResultSet.getTIMESTAMP(x+1);
//                answer = oracleResultSet.getTIMESTAMP(x+1).stringValue();
    //                   if (debug) System.out.println("timestamp:" + oracleResultSet.getTIMESTAMP(x+1).stringValue());
              }
              else {
                if (columnTypes[x].equals("TIMESTAMPTZ") || columnTypes[x].equals("TIMESTAMP WITH TIME ZONE")) {
                  /*
                   * This timestamptz datatype does not seem to convert into anything easily readable in 10.2 :-(
                   * 
                   */
                  try {
                    answer = oracleResultSet.getTIMESTAMPTZ(x + 1).stringValue();
                  } catch (Exception e) {
                   answer = oracleResultSet.getString(x+1);
                  }
    //                     if (debug) System.out.println("Found TIMESTAMPTZ");
                   answerType = "TIMESTAMPTZ";
                 }
                 else {
                   answer = oracleResultSet.getObject(x+1);
                   answerType = "Object";
                }
              }
            }
          }
        }

        try {
          if (answerType.equals("Object") || answerType.equals("Date") || answerType.equals("INTERVALDS") ||
              answerType.equals("TIMESTAMP") || answerType.equals("TIMESTAMPTZ")) {
            resultSetRow.add(answer);

            // find the longest entry in the column for output formatting
            columnWidths[x] = Math.max(columnWidths[x],answer.toString().length());
            
            if (debug) System.out.println("Column: " + x + " answerType:" + answerType + " answer: " + answer);
          }
          else {
            resultSetRow.add(answerString);

            // find the longest entry in the column for output formatting
            columnWidths[x] = Math.max(columnWidths[x],answerString.length());
          }

          // find the longest entry in the column for output formatting
          columnWidths[x] = Math.max(columnWidths[x],answer.toString().length());
          
          if (debug) System.out.println("Column: " + x + " answerType:" + answerType + " answer: " + answerString);
        }
        catch (NullPointerException npe) {
          // the answer was null so we can ignore that
        }
        catch(Exception e) {
          /*
           * Comment out this line if outside of RichMon or change the reference to ConsoleWindow to some other frame.
           * Doing so may mean you don't get an error when you deal with a new datatype not handled explicitly above.
           */
          ConsoleWindow.displayError(e,this);
        }
      }
      // put resultRow into QueryResult
      myResult.addResultRow(resultSetRow);
        if (debug) System.out.println("num rows in result set " + myResult.getNumRows() + " row= " + answer);
    }

    /*
     * Put here some code to count the total number of rows in the result set
     * so that when the message is displayed it tells you how many rows are missing
     */
    
    int rowCounter = myResult.getNumRows();
    
    while (oracleResultSet.next()) {
      rowCounter++;
    }
    
    rowCounter++;
    myResult.setResultSetSize(rowCounter);
    
    // set the QueryResult column widths
    myResult.setColumnWidths(columnWidths);

    // set the columnTypes
    myResult.setColumnTypes(columnTypes);

    // Flip the result to a vertical format for a single row result set
    // where there is more than one column
    if (flippable && myResult.getNumRows() == 1 && myResult.getNumCols() > 1) myResult.flipResultSet();
  }


  /**
   * Executes any 'update' statement that does not require parameters.
   *
   * @param sqlStatement - sql statement to be executed
   * @param commit - boolean which causes a commit to occur after execution if true
   * @return int - the number of rows processed
   */
  public int executeUpdate(String sqlStatement, boolean commit) throws Exception {
    int numRows = -1;
    OraclePreparedStatement update = (OraclePreparedStatement)conn.prepareStatement(sqlStatement);

    try {

      // execute the query
      numRows = update.executeUpdate();

      // commit the update
      if (commit) conn.commit();

      // close the prepared statement
      update.close();

    }
    catch (Exception e) {
      // close the prepared statement
      update.close();

      throw e;
    }

    return numRows;
  }

  /**
   * Executes any 'update' statement that requires parameters.
   *
   * @param sqlStatement - sql statement to be executed
   * @param commit - boolean which causes a commit to occur after execution if true
   * @return int - the number of rows processed
   */
  public int executeUpdate(String sqlStatement, boolean commit,Parameters myPars) throws Exception {
    int numRows = -1;
    OraclePreparedStatement update = (OraclePreparedStatement)conn.prepareStatement(sqlStatement);

    // fudge to find out how many rows are in the parameters array

    // bind parameters into the query
    for (int x=0; x < myPars.getNumParameters(); x++) {
      Object[] singleParameter = myPars.getParameter(x);
      if (singleParameter[0].equals("String")) update.setString(x+1,String.valueOf(singleParameter[1]));
      if (singleParameter[0].equals("int")) update.setInt(x+1,Integer.valueOf(String.valueOf(singleParameter[1])).intValue());
    }

    try {
      // execute the update
      numRows = update.executeUpdate();

      // commit the result
      if (commit) conn.commit();

      // close the prepared statement
      update.close();
    }
    catch (Exception e) {
      // close the prepared statement
      update.close();

      throw e;
    }

    return numRows;
  }

  /**
   * @param statement
   * @param showSQL
   * @param commit
   * @throws Exception
   */
   public void executeImmediate(String statement, boolean showSQL, boolean commit) throws Exception {
   //    if (!showSQL) {
       OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
         /*
          * Modified 15/10/09 to remove the words execute immediate.  This hasn't broken anything (so far) and allows pl/sql to be run.
          */
   //        "begin execute immediate '" + statement + "'; end;");
   //        "begin " + statement + "; end;");
     statement);

       try {
   //        proc.setQueryTimeout(timeoutOperations);

         // execute the procedure
         proc.execute();

         // commit the result
         if (commit) conn.commit();
       }
       catch (Exception e) {
         // close the CallableStatment
         proc.close();

         throw e;
       }

       // close the CallableStatment
       proc.close();
   //    }
   //    else {
   //      JOptionPane.showMessageDialog(ConnectWindow.getConsoleWindow(),statement,"Show SQL",JOptionPane.INFORMATION_MESSAGE);
   //    }
   }

  public void executeImmediate2(String statement, boolean showSQL, boolean commit) throws Exception {
  //    if (!showSQL) {
      OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
        /*
         * Modified 15/10/09 to remove the words execute immediate.  This hasn't broken anything (so far) and allows pl/sql to be run.
         */
  //        "begin execute immediate '" + statement + "'; end;");
  //        "begin " + statement + "; end;");
    statement);

      try {
  //        proc.setQueryTimeout(timeoutOperations);

        // execute the procedure
        proc.execute();

        // commit the result
        if (commit) conn.commit();
      }
      catch (Exception e) {
        // close the CallableStatment
        proc.close();

        throw e;
      }

      // close the CallableStatment
      proc.close();
  //    }
  //    else {
  //      JOptionPane.showMessageDialog(ConnectWindow.getConsoleWindow(),statement,"Show SQL",JOptionPane.INFORMATION_MESSAGE);
  //    }
  }

  /**
   * Calls the database package dbms_system.set_sql_trace_in_session to enable
   * session tracing.
   *
   * @param sid
   * @param serialNum
   * @throws Exception
   */
  public void startSessionTrace(int sid,int serialNum,double dbVersion) throws Exception {
    if (dbVersion < 10) {
      OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
                "{call sys.dbms_system.set_sql_trace_in_session(?,?,TRUE)}");

      // pass in parameters
      proc.setInt(1,sid);
      proc.setInt(2,serialNum);

      // execute the call
      proc.execute();

      // close the CallableStatement
      proc.close();
    }
    else {
      OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
                "{call sys.dbms_monitor.session_trace_enable(?,?,TRUE,TRUE)}");

      // pass in parameters
      proc.setInt(1,sid);
      proc.setInt(2,serialNum);

      // execute the call
      proc.execute();

      // close the CallableStatement
      proc.close();
    }
  }

  /**
   * Calls the database package dbms_system.set_sql_trace_in_session to disable
   * session tracing.
   *
   * @param sid
   * @param serialNum
   * @throws Exception
   */
  public void stopSessionTrace(int sid,int serialNum,double dbVersion) throws Exception {
    
    try {
      if (ConnectWindow.isSysdba()) {
        if (dbVersion < 10) {
          OracleCallableStatement proc =
            (OracleCallableStatement)conn.prepareCall("{call sys.dbms_system.set_sql_trace_in_session(?,?,FALSE)}");

          // pass in parameters
          proc.setInt(1, sid);
          proc.setInt(2, serialNum);

          // execute the call
          proc.execute();

          // close the CallableStatement
          proc.close();
        } else {
          OracleCallableStatement proc =
            (OracleCallableStatement)conn.prepareCall("{call sys.dbms_monitor.session_trace_disable(?,?)}");

          // pass in parameters
          proc.setInt(1, sid);
          proc.setInt(2, serialNum);

          // execute the call
          proc.execute();

          // close the CallableStatement
          proc.close();
        }
      } else {
        throw new InsufficientPrivilegesException("SYSDBA privilege required");
      }
    } catch (SQLException sqle) {
      ConsoleWindow.displayError(sqle, this);
    }
  }

  /**
   * Obtains the current date and time from the database.  Used in file names
   * when saving output
   *
   * @return current data and time
   * @throws java.lang.Exception
   */
  public String getSYSDate() throws Exception {
    String cursorId = "currentDateTime.sql";

    QueryResult myResult = ExecuteDisplay.execute(cursorId,false,false,null);
    String[][] resultSet = myResult.getResultSetAsStringArray();

    return resultSet[0][0];
/*    String currentDT = "";
    OraclePreparedStatement select = (OraclePreparedStatement)conn.prepareStatement(
            "select to_char(sysdate,'dd-mon-yyyy hh24 mi ss')" +
            "from   sys.dual ");

    // execute the query
    OracleResultSet result = (OracleResultSet)select.executeQuery();

    // get the query result
    while (result.next())
    {
      currentDT = result.getString(1);
    }

    // close the OraclePreparedStatement
    select.close();

    return currentDT;
    */
  }

  /**
   * Create a sql tuning set
   *
   * @return task id
   * @throws java.lang.Exception
   */
  public String createSQLTuningTask(String sqlId, String taskName, String taskDescription) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{ ? = call dbms_sqltune.create_tuning_task(?,?,?,?,?,?)}");

    // output parameters
    proc.registerOutParameter(1,OracleTypes.VARCHAR);

    // input parameters
    proc.setString(1, sqlId);
    proc.setNull(2,OracleTypes.NUMBER);
    proc.setNull(3,OracleTypes.VARCHAR);
    proc.setNull(4,OracleTypes.NUMBER);
    proc.setString(5, taskName);
    proc.setString(6, taskDescription);

    // execute the call
    proc.execute();


    String taskId = proc.getString(1);
    // close the CallableStatement
    proc.close();

    return taskId;
  }

  /**
   * Accept a sql tuning task recommendations
   *
   * @return task id
   * @throws java.lang.Exception
   */
  public String acceptSQLProfile(String taskName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{ ? = call dbms_sqltune.accept_sql_profile(?)}");
//    "{ call dbms_sqltune.accept_sql_profile(?)}");

    // output parameters
    proc.registerOutParameter(1,OracleTypes.VARCHAR);

    // input parameters
    proc.setString(2,taskName);

    // execute the call
    proc.execute();


    String result = proc.getString(1);
    // close the CallableStatement
    proc.close();

    return result;
  }


  /**
   * Drop a sql profile
   *
   * @param profileName
   * @throws java.lang.Exception
   * @return task id
   */
  public void dropSQLProfile(String profileName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{call dbms_sqltune.drop_sql_profile(?)}");


    // input parameters
    proc.setString(1,profileName);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }

  /**
   * Drop a sql profile
   *
   * @return task id
   * @throws java.lang.Exception
   */
  public void dropTuningTask(String taskName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{call dbms_sqltune.drop_tuning_task(?)}");


    // input parameters
    proc.setString(1,taskName);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }

  /**
   * Alter a sql profile
   *
   * @return task id
   * @throws java.lang.Exception
   */
  public void alterSQLProfile(String profileName,String status) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{call dbms_sqltune.alter_sql_profile(?,?,?)}");


    // input parameters
    proc.setString(1,profileName);
    proc.setString(2,"STATUS");
    proc.setString(3,status);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }


  /**
   * Execute a tuning task
   *

   * @throws java.lang.Exception
   */
  public void executeTuningTask(String taskName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{call dbms_sqltune.execute_tuning_task(?)}");


    // input parameters
    proc.setString(1,taskName);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();

  }


  /**
   * Create a sql tuning task
   *
   * @return task id
   * @throws java.lang.Exception
   */
  public void createSQLTuningSet(String setName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{ call dbms_sqltune.create_sqlset(?,?,?)}");

    // pass out parameters
    proc.registerOutParameter(1,OracleTypes.VARCHAR);

    // pass in parameters
    proc.setString(1,setName);
    proc.setString(2,"SQL Tuning Set Created by RichMon");
    proc.setString(3,ConnectWindow.getUsername().toUpperCase());

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
    conn.commit();
  }

  /**
   * Delete a SQL Tuning Set
   *
   * @param setName -
   *
   * @throws java.lang.Exception
   */
  public void dropSQLTuningSet(String setName) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    "{ call dbms_sqltune.drop_sqlset(?)}");

    // pass in parameters
    proc.setString(1,setName);

    // execute the call
    proc.execute();


    // close the CallableStatement
    conn.commit();
    proc.close();
  }

  /**
   * Writes a msg to the alert log.  This requires that the connection has 'sysdba' role.
   *
   * @param msg
   * @throws java.lang.Exception
   */
  public void writeMSGToAlert(String msg) throws Exception {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    	        "{call sys.dbms_system.ksdwrt(2,?)}");

    // pass in parameters
    proc.setString(1,msg);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }

  /**
   * Switch current schema.
   *
   * @param schema
   * @throws java.lang.Exception
   */
   public void setCurrentSchema(String schema) throws Exception {
     String statement = "alter session set current_schema = " + schema;

     OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
               "begin execute immediate '" + statement + "'; end;");

   //    if (debug) JOptionPane.showConfirmDialog(consoleWindow,"Set Schema to " + schema,"Set Schema",JOptionPane.INFORMATION_MESSAGE);

     // execute the call
     proc.execute();

     // commit the change
     conn.commit();

     // close the CallableStatement
     proc.close();
   }

  /**
   * @throws Exception
   */
  public void hanganalyze() throws Exception {
    String statement = "alter session set events ''immediate trace name hanganalyze level 3''";

    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall("begin execute immediate '" + statement + "'; end;");

    // execute the call
    proc.execute();

    // commit the change
    conn.commit();

    // close the CallableStatement
    proc.close();
  }


  /**
   * Put an entry into the 'client_info' column of v$session which shows the
   * users username and the vesion of RichMon they are running.
   *
   * @throws Exception
   */
  public void setClientInfo(String version) throws Exception
  {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall(
    	        "{call sys.dbms_application_info.set_client_info(?)}");

    String richMonVersion = version;
    String userName = System.getProperty("user.name");
    String module = "RichMonV" + richMonVersion + " executed by " + userName;

    // pass in parameters
    proc.setString(1,module);

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }

  /**
   * Put an entry in the 'module' column of v$session containing the string 'RichMonVxx.xx'
   * where xx.xx is the version of RichMon.  This can then been seen by users who query v$session.
   *
   * @throws Exception
   */
  public void setModule(String version) throws Exception
  {
    OracleCallableStatement proc = (OracleCallableStatement)conn.prepareCall("{call sys.dbms_application_info.set_module(?,?)}");

    String richMonVersion = version;
    String module = "RichMonV" + richMonVersion;

    // pass in parameters
    proc.setString(1,module);
    proc.setString(2," ");

    // execute the call
    proc.execute();

    // close the CallableStatement
    proc.close();
  }

  public OracleConnection getConnection() {
    return conn;
  }

  public void setMaxResultSetSize(int maxResultSetSize) {
    this.maxResultSetSize = maxResultSetSize;
  }
}