/*
 *  QueryResult.java        12.15 05/01/04
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
 * Change History since 19/03/05
 * =============================
 * 
 * 18/03/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 02/06/05 Richard Wright Made 'Attribute' columns appear in Black
 * 01/02/06 Richard Wright Made 'TOP SQL SQL_ID' a selectable column
 * 01/03/06 Richard Wright Made all variables private and created the
 *                         nessecary accessor methods.
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 14/12/06 Richard Wright Renamed method getResultSetStringArray to getResultSetAsStringArray
 * 22/05/07 Richard Wright Renamed getResultSet to getResultSetAsVectorOfVectors
 * 29/10/08 Richard Wright Removed resultSetUnderlines
 * 29/10/08 Richard Wright Moved much of the resultset processing from the database class to here
 * 04/11/08 Richard Wright Modified Constructor so the number of columns is not required
 * 30/01/08 Richard Wright Moved the resultset processing back to the database class
 * 14/09/09 Richard Wright Removed 'EVENT' from the list of selectable columns
 * 29/01/10 Richard Wright Added serialVersionUID to ensure that serialized class can always be read back
 * 28/07/11 Richard Wright Added task_id and service name to the selectable columns
 * 14/10/14 Richard Wright Added iteration for use by the scratch panel
 * 15/12/15 Richard Wright Modified to add filterByRAC and usernameFilter
 */


package RichMon;

import java.io.Serializable;

import java.sql.SQLException;

import java.text.DateFormat;

import java.util.Locale;
import java.util.Vector;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleResultSetMetaData;

import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPTZ;


/**
 * The result of a sql query, including meta data.
 */
public class QueryResult implements Serializable {
  private long ms = 0L;                     // milliseconds (query runtime)
  private int numCols = 0;                  // number of columns in resultSet 
  private int numRows = 0;                  // number of rows in resultSet 
  private int[] columnWidths;               // array to hold the width of each column 
  private String[] columnTypes;             // column types 
  private int[] selectableColumns;          // array to hold index of columns which are selectable 
  private boolean attributeColumn = false;  // is the first column called 'attribute' 
  private String[] resultSetHeadings;       // array to hold the resultSet column headings 
  private int rowsProcessed;                // number of rows affected by insert/update/delete statements
  private int resultSetSize;                // number of rows in the resultset returned by the db, null unless the result was truncated
  private int iteration;                    // the iteration that this result refers too on the scratch panel

  private Vector resultSet;                 // array to hold the resultSet 
  private String[][] resultSetStringArray;  // array to hold the resultSet 
  private boolean flipped;                  // single row resultSet has been flipped vertical 
  private String cursorId;
  private String executionTime;             // when the query ran
  private String usernameFilter;            // the username filter acive at time of execution
  private String[][] selectedInstances;     // instances being filtered on at time of execution
  private boolean isOnlyLocalInstanceSelected;  // at time of execution
  private boolean filterByRAC;              
  
  // used when recording a queryresult for later playback
  private String recordDatabaseName;
  private String recordInstanceName;
  private String recordStartSnapshot;      
  private String recordEndSnapshot;
//  private String recordDataSource;         // i.e. "waitEventsAWR"

  
  private String resultCacheFileName;      // if this queryresult has been cached on disk, this is the file name used
  
  boolean debug=false;
  
  /*
   * The purpose of the following line is to ensure that saved results can be read back in by any version of richmon which have been compiled since the
   * result was recorded.  
   * 
   * see http://forums.sun.com/thread.jspa?threadID=459268
   */
  static final long serialVersionUID = -3611946473283033478L;
  
  /**
   * Constructor.
   * 
   * @param numCols - the number of columns in the result set
   */
  public QueryResult() {
    // set up resultSet 
    resultSet = new Vector();
  }
  
  /**
   * Add a new row to the resultSet.
   * 
   * @param resultSetRow - Vector contains a single row of output from a query
   */
  public void addResultRow(Vector resultSetRow) {   
    if (!(resultSet instanceof Vector)) resultSet = new Vector();
    resultSet.add(resultSetRow);

    // increment counters 
    numRows++;
  }
  
  /**
   * Get a specific row from the resultSet
   * 
   * @param row - int pointer 
   * @return Vector - continaining the specified row
   */
  public Vector getResultSetRow(int row) {
//    if (!(resultSet instanceof Vector)) processOracleResultSetIntoResultSet();
    return (Vector)resultSet.elementAt(row);
  }
  
  /**
   * Record the query execution set
   * 
   * @param ms - milliseconds
   */
	public void setExecutionTime(long ms) {
		this.ms = ms;
	}
  
  /**
   * Get the query execution time
   * 
   * @return ms - milliseconds
   */
  public long getExecutionTime() {
      return ms;
  }
  
  /**
   * Get the resultSet
   * 
   * @return Vector - a Vector of Vectors which makes up the resultSet
   */
    public Vector getResultSetAsVectorOfVectors() {
//      if (!(resultSet instanceof Vector)) processOracleResultSetIntoResultSet();
    
    return resultSet;
	}
  
  
	/**
	 * Set the column headings for the result set
   * 
	 * @param resultSetHeadings
	 */
  public void setResultHeadings(String[] resultSetHeadings) {
    this.resultSetHeadings = resultSetHeadings;
    
    setSelectableColumns(resultSetHeadings);
    setAttributeColumn(resultSetHeadings);
  }

  /**
   * Check resultSetHeadings to see which columns should be selectable
   * and therefore display in a different color
   * 
   * @param resultSetHeadings
   */
  public void setSelectableColumns(String[] resultSetHeadings) {
    int numSCols = 0;
    selectableColumns = new int[numCols];
    for (int i = 0;i < numCols;i++) {
      if (resultSetHeadings[i].equals("SID") || 
//          resultSetHeadings[i].equals("EVENT") ||
          resultSetHeadings[i].equals("PARENT SID") ||
          resultSetHeadings[i].equals("CHILD SID") ||
          resultSetHeadings[i].equals("BLOCKING SID") ||
          resultSetHeadings[i].equals("TOP SQL HASH VALUE") ||
          resultSetHeadings[i].equals("TOP SQL SQL_ID") ||
          resultSetHeadings[i].equals("SQL_ID") ||
          resultSetHeadings[i].equals("BLOCKING SQL_ID") ||
          resultSetHeadings[i].equals("TASK_ID") ||
          resultSetHeadings[i].equals("Service Name")) {
        selectableColumns[numSCols++] = i;
      }
    }
    
    // resize the selectableColumns array to the exact capacity required 
    int[] tmpSelectableColumns = new int[numSCols];
    System.arraycopy(selectableColumns,0,tmpSelectableColumns,0,numSCols);
    selectableColumns = new int[numSCols];
    System.arraycopy(tmpSelectableColumns,0,selectableColumns,0,numSCols);
  }
  
  /**
   * If the first column in the result set is 'ATTRIBUTE', it needs to be black output
   * 
   * @param resultSetHeadings 
   */
  private void setAttributeColumn(String[] resultSetHeadings) {
    if (resultSetHeadings[0] == "ATTRIBUTE") attributeColumn = true;
  }

  /**
   * Get a list of the columns which are user selectable.  These will be displayed in red.
   * 
   * @return selectableColumns - String[]
   */
  public int[] getSelectableColumns() {
    return selectableColumns;
  }

  /**
   * Get the resultSet column headings.
   * 
   * @return resultSetHeadings - String[] containing the column headings
   */
  public String[] getResultHeadings() {
    return resultSetHeadings;
  }

	/**
	 * Get the number of rows in the result set
	 * 
	 * @return int
	 */
	public int getNumRows() {
//	  if (!(resultSet instanceof Vector)) processOracleResultSetIntoResultSet();
		return numRows;
	}
  
  /**
   * Set the column widths.
   * 
   * @param columnWidths - int[]
   */
  public void setColumnWidths(int[] columnWidths) {
    this.columnWidths = columnWidths;
  }
  
  /**
   * Get the column widths.
   * 
   * @return columnWidths - int[]
   */
  public int[] getColumnWidths() {
    return columnWidths;
  }
  
  /**
   * Get the number of columns in the resultSet.
   * 
   * @return numCols - int
   */
  public int getNumCols() {
//    if (!(resultSet instanceof Vector)) processOracleResultSetIntoResultSet();
    return numCols;
  }
  
  /**
   * Set the name of the cursor
   * 
   * @param cursorId - name of the sql script
   */
  public void setCursorId(String cursorId) {
    this.cursorId = cursorId;
  }
  
  /**
   * Get the name of the sql script
   * @return 
   */
  public String getCursorId() {
    return cursorId;
  }
  
  /**
   * Set the query execution time in milliseconds.
   * 
   * @param executionTime - milliseconds
   */
  public void setExecutionClockTime(String executionTime) {
    this.executionTime = executionTime;
  }
  
  /**
   * Get the query execution time.
   * 
   * @return executionTime
   */
  public String getExecutionClockTime() {
    return executionTime;
  }
  
  /**
   * Set the resultSet.  Used when a dummy resultSet is required.
   * 
   * @param resultSet - Vector of Vector's
   */
  public void setResultSet(Vector resultSet) {
    this.resultSet = resultSet;
  }
  
  /**
   * Set the number of rows in the resultSet.
   * 
   * @param numRows - int
   */
  public void setNumRows(int numRows) {
    this.numRows = numRows;
  }
  
  public void setNumCols(int numCols) {
    this.numCols = numCols;
  }
  
  /**
   * Set the Column Types array.
   * 
   * @param columnTypes - String[]
   */
  public void setColumnTypes(String[] columnTypes) {
    this.columnTypes = columnTypes;
  }
  
  /**
   * Get the column Types array.
   * 
   * @return columnTypes - String[]
   */
  public String[] getColumnTypes() {
    return columnTypes;
  }
  
  /**
   * Returns the result set as a string array
   * 
   * @return String[][] - result set 
   */
  public String[][] getResultSetAsStringArray() {
//    if (!(resultSet instanceof Vector)) processOracleResultSetIntoResultSet();
    if (!(resultSetStringArray instanceof String[][])) convertResultSet();
    
    return resultSetStringArray;
  }
  
  /**
   * Converts the result set from a vector to a string array.
   * 
   */
  private void convertResultSet() {
    resultSetStringArray = new String[numRows][numCols];
    
    for (int r=0; r < numRows; r++) {
      // for each row  
      Vector row = getResultSetRow(r);
      for (int c=0; c < numCols; c++) {
        resultSetStringArray[r][c] = String.valueOf(row.elementAt(c));
        if (resultSetStringArray[r][c].equals("null")) resultSetStringArray[r][c] = " ";
      }
    }
  }

  public void setFlipped(boolean flipped) {
    this.flipped = flipped;
  }

  public boolean isFlipped() {
    return flipped;
  }
  
  public boolean isAttributeColumn() {
    return this.attributeColumn;
  }
  
//  public void setOracleResultSet(OracleResultSet oracleResultSet,long msExecutionTime,boolean flippable) {
//    this.oracleResultSet = oracleResultSet;
//    setExecutionTime(msExecutionTime);
//    shouldBeFlipped = flippable;
//}

  
  /**
  * Converts a queryresult which only contains a single column to vertical format.
  * ie.  one row per attribute value pair.
  *
  */
  public void flipResultSet() {
  String[][] resultSet = getResultSetAsStringArray();
  String[][] newResultSet = new String[getNumCols()][2];
  int col0Width = 0;
  int col1Width = 1;
  
  for (int i=0; i < getNumCols(); i++) {
    newResultSet[i][0] = resultSetHeadings[i];
    newResultSet[i][1] = resultSet[0][i];
    if (resultSetHeadings[i].length() > col0Width) col0Width = resultSetHeadings[i].length();
    if (resultSet[0][i].length() > col1Width) col1Width = resultSet[0][i].length();
  }
  
  String []newResultSetHeadings = new String[2];
  newResultSetHeadings[0] = "ATTRIBUTE";
  newResultSetHeadings[1] = "VALUE";
  
  int[] columnWidths = new int[2];
  columnWidths[0] = col0Width;
  columnWidths[1] = col1Width;
  
  Vector myResultSet = new Vector(newResultSet.length);
  for (int i=0; i < newResultSet.length; i++) {
    Vector row = new Vector(2);
    row.add(newResultSet[i][0]);
    row.add(newResultSet[i][1]);
    myResultSet.add(row);
  }
  
  setResultSet(myResultSet);
  setColumnWidths(columnWidths);
  setNumRows(newResultSet.length);
  setNumCols(2);
  setResultHeadings(newResultSetHeadings);
  
  // mark the result set as flipped 
  setFlipped(true);
  
  resultSetStringArray = null;
  }

  public void setRecordInstanceName(String recordInstanceName) {
    this.recordInstanceName = recordInstanceName;
  }

  public String getRecordInstanceName() {
    return recordInstanceName;
  }

/*  public void setRecordDataSource(String recordDataSoource) {
    this.recordDataSource = recordDataSoource;
  }

  public String getRecordDataSource() {
    return recordDataSource;
  }
*/
  public void setRecordDatabaseName(String recordDatabaseName) {
    this.recordDatabaseName = recordDatabaseName;
  }

  public String getRecordDatabaseName() {
    return recordDatabaseName;
  }  
  
  public void setRecordStartSnapshot(String recordDatabaseName) {
    this.recordStartSnapshot = recordDatabaseName;
  }

  public String getRecordStartSnapshot() {
    return recordStartSnapshot;
  }  
  
  public void setRecordEndSnapshot(String recordDatabaseName) {
    this.recordEndSnapshot = recordDatabaseName;
  }

  public String getRecordEndSnapshot() {
    return recordEndSnapshot;
  }

  public void setResultCacheFileName(String resultCacheFileName) {
    this.resultCacheFileName = resultCacheFileName;
  }

  public String getResultCacheFileName() {
    return resultCacheFileName;
  }

  public void setRowsProcessed(int rowsProcessed) {
    this.rowsProcessed = rowsProcessed;
  }

  public int getRowsProcessed() {
    return rowsProcessed;
  }
  
  public void setResultSetSize(int resultSetSize) {
    this.resultSetSize = resultSetSize;
  }
  
  public int getResultSetSize() {
    return resultSetSize;
  }
  
  public void setIteration(int iteration) {
    this.iteration = iteration;
  }
  
  public int getIteration() {
    return iteration;
  }
  
  public void setUsernameFilter(String username) {
    usernameFilter = username;
  }
  
  public void setSelectedInstances(String[][] instances) {
    selectedInstances = instances;
  }
  
  public String getUsernameFilter() {
    return usernameFilter;
  }
  
  public String[][] getSelectedInstances() {
    return selectedInstances;
  }
  
  public void setIsOnlyLocalInstanceSelected(boolean isOnlyLocalInstanceSelected) {
    this.isOnlyLocalInstanceSelected = isOnlyLocalInstanceSelected;
  }
  
  public boolean isOnlyLocalInstanceSelected() {
    return isOnlyLocalInstanceSelected;
  }
  
  public void setFilterByRAC(boolean filter) {
    filterByRAC = filter;
  }
  
  public boolean isFilterByRAC() {
    return filterByRAC;
  }
}
