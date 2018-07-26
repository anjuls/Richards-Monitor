/*
 * Cursor.java        12.15 05/01/04
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
 * Change History since 21/02/05
 * =============================
 * 
 * 21/02/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 02/06/05 Richard Wright Add ability to stop single row result sets being flipped
 * 31/10/05 Richard Wright Check a cursorId has a '.sql' before attempting to load it from disk.
 *                         Otherwise, assume it is a table name a build a select * from t instead.
 * 01/11/05 Richard Wright Removed references to setting the database because dbarep is no longer accessed directly
 * 17/08/06 Richard Wright Modified the comment style and error handling
 * 18/08/06 Richard Wright Removed old deprecated methods
 * 30/01/07 Richard Wright Added methods to support timeouts when executing sql
 * 15/10/07 Richard Wright Enhanced to support RAC
 * 14/01/08 Richard Wright Added debug boolean
 * 27/10/08 Richard Wright Added modifiedForRac boolean
 * 03/11/08 Richard Wright Cursors can now be pinned in the cursorCache
 * 29/01/09 Richard Wright Regressed previous change.
 * 11/08/09 Richard Wright Removed timeout operations
 * 11/08/09 Richard Wright Add myFrame and setMyFrame to make this class reusable
 * 23/10/09 Richard Wright Modified to allow recording of data and charts for later playback.
 * 30/03/10 Richard Wright Set the default value for eggTimer to true
 * 29/02/12 Richard Wright Allow results to be cached
 * 26/02/14 Richard Wright added restrictRows boolean to allow row restriction to be turned off
 * 14/08/15 Richard Wright Modified to allow filter by user
 * 15/12/15 Richard Wright Modified to add filterByRAC and usernameFilter
 */


package RichMon;

import java.awt.Frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

import java.text.DateFormat;

import java.util.Date;


/**
 * A Cursor is essentially the sql text to be executed.
 */
public class Cursor {
  String cursorId;         // name of the cursor 
  String sqlTxtOriginal;   // sql 
  private String sqlTxtToExecute;  // cursor text after first format 
  boolean eggTimer = true;        // use egg timer 
  boolean flippable = true;// single row result sets can be flipped 
  boolean debug = false;
  boolean decodeModifiedForRAC = false;
  boolean predicateModifedForRAC = false;
  boolean predicateBeforeOrderByModifiedForRAC = false;
  boolean cache = false;  // used when recording a queryresult for later playback
  boolean filterByRAC;
  private String cacheDatabaseName;
  private String cacheInstanceName;
  private String cacheStartSnapshot;      
  private String cacheEndSnapshot;
  private String cacheFileName;
  private boolean cacheDirectoryExits = false;
  private String usernameFilter;
  
//  static Frame myFrame;   // a pointer to the consoleWindow in RichMon, implemented this way so this call can be reused
                          // The only place in richmon that this is set is ConsoleWindow.retrieveDBVersionFromDB()
  
  Date today;         
  DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
  String currentTime;
  
  Database database = ConnectWindow.getDatabase();       // pointer to the database object 
  
  /**
   * Constructor
   * 
   * @param cursorId 
   * @param withSQL  Set to true if specifying a .sql file or tablename otherwise with if you want to add sql to later
   * @throws NoSuchSQLException 
   */
  public Cursor(String cursorId,boolean withSQL) throws NoSuchSQLException {
    // A value of false for withSQL means the SQL is coming from the scratch panel
    // If the cursorID does not contain a '.sql' it is converted into a select statement, see loadCursor method
    this.cursorId = cursorId;

    if (withSQL) {
      /* load the sql text in from file */
      loadCursor();
      
      /* format sql ready to execute */
      simpleFormat();
    }
  }
  
  /**
   * Alternative Constructor
   * 
   */
//  public Cursor(String cursorId,boolean withSQL,Frame myFrame) throws NoSuchSQLException {
//    // A value of false for withSQL means the SQL is coming from the scratch panel
//    // If the cursorID does not contain a '.sql' it is converted into a select statement, see loadCursor in Cursor.java
//    Cursor.myFrame = myFrame;
    
//    this.cursorId = cursorId;

//    if (withSQL) {
      /* load the sql text in from file */
//      loadCursor();
      
      /* format sql ready to execute */
//      simpleFormat();
//    }
//  }
  
  /**
   * Sets sqlTxtOriginal.  Called after a new cursor has been created rather 
   * than loaded from the jar file, to set the cursors sql.
   * 
   * @param sql - sql text
   */
  public void setSQLTxtOriginal(String sql) throws Exception {
    this.sqlTxtOriginal = sql;
    
    /* format the sql ready to execute */
    simpleFormat();
    

  }
  
  
  /**
   * Load the sql text for the required cursor
   * 
   * @throws NoSuchSQLException
   */
  private void loadCursor() throws NoSuchSQLException {
    String localCursorId = cursorId;
    
    // If the cursorId does not contain a '.sql' then it must be a table name not a sql script. 
    if (cursorId.indexOf(".sql") >= 0) {
      try {
        // is the filename prefixed to keep cursors unique for a session panel
        // such filenames are in the format sid999999name.sql
        if (cursorId.length() >= 9 && cursorId.startsWith("sid")) {
          try {
            int sixdigits = Integer.valueOf(cursorId.substring(3,8)).intValue();
            // this file is prefixed from a session panel
            localCursorId = cursorId.substring(9);
          }
          catch (NumberFormatException e) {
            // do nothing
          }
          
        }
        
        // open file 
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(ConsoleWindow.class.getResourceAsStream("/" + localCursorId)));
  
        // read in each line in the sql file 
        StringBuffer tmpSQL = new StringBuffer();
        for (String line; (line = fileReader.readLine()) != null;) {
          tmpSQL.append(line);
          tmpSQL.append("\n");
        }
        sqlTxtOriginal = tmpSQL.toString();
      
        // close file 
        fileReader.close();
      }
      catch (Exception NullPointerException) {
        throw new NoSuchSQLException("The SQL " + cursorId + " was not found in jar file");
      }
    }
    else {
      createCursorFromTableName();
    }
  }
  
  private void createCursorFromTableName() {
    sqlTxtOriginal = "select * from " + cursorId;
  }
  
  /**
   * Get the sql statment
   * 
   * @return String sql
   */
  public String getSQL() {
    return sqlTxtOriginal;
  }

  /**
   * Get the sql statement after simple formatting
   * 
   * @return 
   */
  public String getSQLToExecute() {
    return sqlTxtToExecute;
  }
    
  
  /**
   * Executes any sql query which requires parameters
   * 
   * @param parameters 
   * @return QueryResult 
   * @throws java.lang.Exception
   */
  public QueryResult executeQuery(Parameters myPars, boolean restrictRows) throws Exception {
    try {
      boolean flip = checkWhetherToFlip();
      if (eggTimer) {
//        myFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
//        ConsoleWindow.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
      }
      getTime();
      QueryResult myResult = database.execute(sqlTxtToExecute, myPars, flip, restrictRows);
      if (eggTimer) {
//        myFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
      }
      myResult.setCursorId(cursorId);
      myResult.setExecutionClockTime(currentTime);
      if (debug) System.out.println("filterByRac inside cursor is " + filterByRAC);
      if (filterByRAC) myResult.setFilterByRAC(true);
      if (usernameFilter instanceof String) myResult.setUsernameFilter(usernameFilter);
      
      if (cache) {
        myResult.setRecordInstanceName(cacheInstanceName);
        myResult.setRecordStartSnapshot(cacheStartSnapshot);
        myResult.setRecordEndSnapshot(cacheEndSnapshot);
        myResult.setRecordDatabaseName(cacheDatabaseName);
        cacheQueryResult(myResult);
      }
      return myResult;
    } 
    catch (Exception e) {
      ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
      throw e;
    }
  }    
  
  public QueryResult executeQuery(Parameters myPars, boolean restrictRows, int iteration) throws Exception {
    try {
      boolean flip = checkWhetherToFlip();
      if (eggTimer) {
//        myFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
//        ConsoleWindow.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
      }
      getTime();
      QueryResult myResult = database.execute(sqlTxtToExecute, myPars, flip, restrictRows);
      if (eggTimer) {
//        myFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
      }
      myResult.setCursorId(cursorId);
      myResult.setExecutionClockTime(currentTime);
      myResult.setIteration(iteration);
      if (filterByRAC) myResult.setFilterByRAC(true);
      if (usernameFilter instanceof String) myResult.setUsernameFilter(usernameFilter);

      if (cache) {
        myResult.setRecordInstanceName(cacheInstanceName);
        myResult.setRecordStartSnapshot(cacheStartSnapshot);
        myResult.setRecordEndSnapshot(cacheEndSnapshot);
        myResult.setRecordDatabaseName(cacheDatabaseName);
        cacheQueryResult(myResult);
      }
      return myResult;
    } 
    catch (Exception e) {
      ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
      throw e;
    }
  }  
  
  private boolean checkWhetherToFlip() {
    if (flippable && ConsoleWindow.isFlipSingleRowOutput()) {
      return true;
    }
    else {
      return false;
    }
  }
 
  /**
   * Executes an update statement, passing in parameters
   * 
   * @param commit - if true a commit is performed after the update
   * @param parameters 
   * @return int - the number of rows updated
   * @throws java.lang.Exception
   */
  public int executeUpdate(boolean commit,Parameters myPars) throws Exception {
    if (eggTimer) ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
    int numRows = database.executeUpdate(sqlTxtToExecute,commit,myPars);
    if (eggTimer) ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    return numRows;
  }  
  
  /**
   * Executes an update statement
   * 
   * @param commit - if true a commit is performed after the update
   * @return int - the number of rows updated
   * @throws java.lang.Exception
   */
  public int executeUpdate(boolean commit) throws Exception {
    if (eggTimer) ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
    int numRows = database.executeUpdate(sqlTxtToExecute,commit);
    if (eggTimer) ConnectWindow.getConsoleWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    return numRows;
  }
  
  /**
   * Re-formats sql to remove leading and trailing spaces, 
   *                   remove carriage returns and multiple spaces
   *                   remove trailing slash or semi colon
   */
  public void simpleFormat()
  {
    sqlTxtToExecute = sqlTxtOriginal;
    sqlTxtToExecute = sqlTxtToExecute.replace('\n',' ');
    sqlTxtToExecute = sqlTxtToExecute.replaceAll("  "," ");
    sqlTxtToExecute = sqlTxtToExecute.trim();
    
    // remove and trailing slash or semi colons from sqlStatement 
    if (sqlTxtToExecute.substring(sqlTxtToExecute.length()-1).equals("/") ||
        sqlTxtToExecute.substring(sqlTxtToExecute.length()-1).equals(";")) {
      sqlTxtToExecute = sqlTxtToExecute.substring(0,sqlTxtToExecute.length()-1);
        }
  }
  
  /**
   * Get the current time
   */
  private void getTime() {
    today = new Date();
    currentTime = dateFormatter.format(today);
  }
  
  public void setEggTimer(boolean eggTimer)
  {
    this.eggTimer = eggTimer;
  }


  public void setFlippable(boolean flippable) {
    this.flippable = flippable;
  }


  public boolean isFlippable() {
    return flippable;
  }
  
  public void setDatabase(Database newDatabase) {
    this.database = newDatabase;
  }
  
  /**
   * Insert a decode statement after the 'SELECT' to convert inst_id into instance_name
   */
  public void includeDecode(String alias) {
    if (!decodeModifiedForRAC) {
      String[][] instances = ConsoleWindow.getSelectedInstances();
      
      StringBuffer decode = new StringBuffer("decode(" + alias + ".inst_id,");
      for (int i = 0; i < instances.length; i++)  {
        decode.append(instances[i][0] + ",'" + instances[i][1] + "'");
        if ((instances.length -i) > 1) {
          decode.append(",");
        }
        else {
          decode.append(",'should never see this')");
        }
      }  
      
      boolean distinct = false;
      if (sqlTxtToExecute.toLowerCase().indexOf("select distinct") >= 0) {
        distinct = true;
        String tempSQL = "select " + sqlTxtToExecute.substring(15);
        sqlTxtToExecute = tempSQL;
      }
  
  
      int commentEndPos = sqlTxtToExecute.indexOf("*/");
      int selectPos = sqlTxtToExecute.toLowerCase().indexOf("select");
  
      if (commentEndPos > -1) {
        // this statement includes a comment
        String tempSQL = sqlTxtToExecute.substring(0,commentEndPos +2) + " " + decode + " " + "\"Instance Name\", " + sqlTxtToExecute.substring(commentEndPos +2);
        sqlTxtToExecute = tempSQL;
      }
      else {
        if (selectPos > -1) {
          String tempSQL;
          if (distinct) {
            tempSQL = sqlTxtToExecute.substring(0,selectPos +6) + " distinct " + decode + " " + "\"Instance Name\", " + sqlTxtToExecute.substring(selectPos +6);
          }
          else {
            tempSQL = sqlTxtToExecute.substring(0,selectPos +6) + " " + decode + " " + "\"Instance Name\", " + sqlTxtToExecute.substring(selectPos +6);
          }
          sqlTxtToExecute = tempSQL;
        }
      }
      
      int groupByClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("group by");
      int whereClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("where");
  
      if (groupByClausePos > -1 && (groupByClausePos > whereClausePos)) {
        String tempSQL = sqlTxtToExecute.substring(0,groupByClausePos +8) + " " + decode + ", " + sqlTxtToExecute.substring(groupByClausePos +8);
        sqlTxtToExecute = tempSQL;
      }
    }

    decodeModifiedForRAC = true;
  }
  
  /**
   * Insert a predicate to restrict by inst_id.  This assumes it is inserting into the outer most where clause only.
   */
  public void includePredicate(String alias) {
    if (!predicateModifedForRAC) {
      String[][] instances = ConsoleWindow.getSelectedInstances();
  
      StringBuffer predicate = new StringBuffer(alias + ".inst_id in (");
      for (int i = 0; i < instances.length; i++) {
        predicate.append(instances[i][0]);
        if ((instances.length - i) > 1) predicate.append(",");
      }
      predicate.append(")");
  
      int whereClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("where");
      int fromClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("from");
      int orderByClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("order by");
      int groupByClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("group by");
      int andPos = sqlTxtToExecute.toLowerCase().lastIndexOf("and ");
      
      int insertPos = 0;
      
      if (groupByClausePos > 0 && groupByClausePos > whereClausePos) {
        insertPos = groupByClausePos;
        if (whereClausePos == -1) predicate.insert(0,"where ");
        if (whereClausePos > -1) predicate.insert(0,"and ");
      }
      
      if ((orderByClausePos > 0 && orderByClausePos > whereClausePos) && !(groupByClausePos > 0 && groupByClausePos > whereClausePos)) {
        insertPos = orderByClausePos;
        if (whereClausePos == -1) predicate.insert(0,"where ");
        if (whereClausePos > -1) predicate.insert(0,"and ");
      }
      
      if (insertPos == 0) {
        insertPos = sqlTxtToExecute.length();
        if (whereClausePos == -1 || (whereClausePos < fromClausePos && andPos < fromClausePos)) predicate.insert(0,"where ");
        if (whereClausePos > -1 && (whereClausePos > fromClausePos || andPos > fromClausePos)) predicate.insert(0,"and ");
      }
      
      String tempSQL = sqlTxtToExecute.substring(0,insertPos) + " " + predicate + " " + sqlTxtToExecute.substring(insertPos);
      sqlTxtToExecute = tempSQL;
    }
    
    predicateModifedForRAC = true;
    if (debug) System.out.println(sqlTxtToExecute);
  }
  
  
  /**
   * Insert a predicate to restrict by user.  This assumes it is inserting into the outer most where clause only.
   */
  public void includeUserFilter(String alias) {  
    if (ConsoleWindow.isFilteringByUsername()) {
  
    StringBuffer predicate = new StringBuffer(alias + ".username = '" + ConsoleWindow.getUsernameFilter() + "'");

    int whereClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("where");
    int fromClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("from");
    int orderByClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("order by");
    int groupByClausePos = sqlTxtToExecute.toLowerCase().lastIndexOf("group by");
    int andPos = sqlTxtToExecute.toLowerCase().lastIndexOf("and ");
    
    int insertPos = 0;
    
    if (groupByClausePos > 0 && groupByClausePos > whereClausePos) {
      insertPos = groupByClausePos;
      if (whereClausePos == -1) predicate.insert(0,"where ");
      if (whereClausePos > -1) predicate.insert(0,"and ");
    }
    
    if ((orderByClausePos > 0 && orderByClausePos > whereClausePos) && !(groupByClausePos > 0 && groupByClausePos > whereClausePos)) {
      insertPos = orderByClausePos;
      if (whereClausePos == -1) predicate.insert(0,"where ");
      if (whereClausePos > -1) predicate.insert(0,"and ");
    }
    
    if (insertPos == 0) {
      insertPos = sqlTxtToExecute.length();
      if (whereClausePos == -1 || (whereClausePos < fromClausePos && andPos < fromClausePos)) predicate.insert(0,"where ");
      if (whereClausePos > -1 && (whereClausePos > fromClausePos || andPos > fromClausePos)) predicate.insert(0,"and ");
    }
    
    String tempSQL = sqlTxtToExecute.substring(0,insertPos) + " " + predicate + " " + sqlTxtToExecute.substring(insertPos);
    sqlTxtToExecute = tempSQL;
    }
    
    
    if (debug) System.out.println(sqlTxtToExecute);
  }
  
  
  /**
   * Insert a predicate to restrict by inst_id.  This will be inserted immediately prior to the last order by clause
   */
  public void includePredicateBeforeOrderBy(String alias,boolean includeWhereClause) {
    if (!predicateBeforeOrderByModifiedForRAC) {
      String[][] instances = ConsoleWindow.getSelectedInstances();
  
      StringBuffer predicate = new StringBuffer(alias + ".inst_id in (");
      for (int i = 0; i < instances.length; i++) {
        predicate.append(instances[i][0]);
        if ((instances.length - i) > 1) predicate.append(",");
      }
      predicate.append(")");
      
      int insertPos = sqlTxtToExecute.toLowerCase().lastIndexOf("order by");;
      
      if (includeWhereClause) { 
        predicate.insert(0,"where ");
      }
      else {
        predicate.insert(0,"and ");
      }
      
      String tempSQL = sqlTxtToExecute.substring(0,insertPos) + " " + predicate + " " + sqlTxtToExecute.substring(insertPos);
      sqlTxtToExecute = tempSQL;
    }
    
    predicateBeforeOrderByModifiedForRAC = true;
   if (debug) System.out.println(sqlTxtToExecute);
  }
  
 
  public boolean isModifiedForRAC() {
    if (decodeModifiedForRAC || predicateModifedForRAC || predicateBeforeOrderByModifiedForRAC) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public void unsetModifiedForRac() {
    decodeModifiedForRAC = false;
    predicateModifedForRAC = false;
    predicateBeforeOrderByModifiedForRAC = false;
  }
  
  public String getCursorID() {
    return cursorId;
  }
  
  /*
   * The only place in richmon that this is set is ConsoleWindow.retrieveDBVersionFromDB()
   * 
   * because myFrame is set to static it is set to all version of this object at the same time.
   * it was done this way in the process of trying to make cursor, database and queryresult classes reusable in other apps more easily.
   */
//  public void setFrame(Frame myFrame) {
//    Cursor.myFrame = myFrame;
//  }
  
  public void setCachingOn(String instanceName,int startSnapshot,int endSnapshot,String fileName) {
    this.cache = true;
    this.cacheInstanceName = instanceName;
    this.cacheStartSnapshot = String.valueOf(startSnapshot).toString();
    this.cacheEndSnapshot = String.valueOf(endSnapshot).toString();
    this.cacheFileName = fileName;
    this.cacheDatabaseName = ConsoleWindow.getDatabaseName();
  }

  
  private void cacheQueryResult(QueryResult myResult) throws Exception {
    myResult.setRecordInstanceName(cacheInstanceName);
    myResult.setRecordStartSnapshot(cacheStartSnapshot);
    myResult.setRecordEndSnapshot(cacheEndSnapshot);
    myResult.setRecordDatabaseName(cacheDatabaseName);
    
    if (Properties.isAWRCacheEnabled()) {
      if (!cacheDirectoryExits) CreateDirForThisRecordingSession();

      FileOutputStream fos = new FileOutputStream(cacheFileName);
      ObjectOutputStream out = new ObjectOutputStream(fos);
      out.writeObject(myResult);
      out.close();
    }
  }

  private void CreateDirForThisRecordingSession() {
    File richMonDir = new File(ConnectWindow.getBaseDir()); 
    if (ConnectWindow.isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Cache/" + cacheDatabaseName);     
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Cache\\" + cacheDatabaseName);      
    }
    
    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
    }
   
    if (ConnectWindow.isLinux()) {
      richMonDir = new File(ConnectWindow.getBaseDir() + "/Cache/" + cacheDatabaseName + "/" + cacheInstanceName + "/");     
    }
    else {
      richMonDir = new File(ConnectWindow.getBaseDir() +  "\\Cache\\" + cacheDatabaseName + "\\" + cacheInstanceName + "\\");      
    }
    
    if (!richMonDir.exists()) {
      boolean ok = richMonDir.mkdir();
      cacheDirectoryExits = true;
    }
  }
  
  public void setFilterByRAC(boolean filter) {
    filterByRAC = filter;
//    System.out.println("setting filterByRAC inside Cursor to " + filterByRAC);
  }
  
  public boolean isFilterByRAC() {
    return filterByRAC;
  }
  
  public String getUsernameFilter() {
    return usernameFilter;
  }
  
  public void setUsernameFilter(String username) {
    usernameFilter = username;
  }
}