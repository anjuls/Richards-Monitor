/*
 *  ResultCache.java        12.15 05/01/04
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
 * Change History since 19/03/05
 * =============================
 * 
 * 29/03/05 Richard Wright Swithed comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 13/07/05 Richard Wright Made the cacheSize configurable
 * 16/08/06 Richard Wright Complete re-write to stop using arrays and use collections 
 *                         instead. Much more elegant.
 * 23/08/06 Richard Wright Modified the comment style and error handling
 * 15/10/09 Richard Wright Implemented the cache threshold
 * 15/12/09 Richard Wright Modified so that QueryResults with more than cache_theshold rows are serialized
 * 24/02/10 Richard Wright Fixed bug that caused the tempory Queryresult that is cache when a result is serialized from being after the forward button is used
 * 14/07/11 Richard Wright Fixed obscure bug in moving backwards when the cache only has a single entry
 */


package RichMon;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;


/**
 * A cache of the last n QueryResults.
 */
public class ResultCache {

  int cacheSize = 100;    // number of results to cache 
  
  LinkedList cache;
  int cachePointer = 0;
  
  boolean debug = false;
   
  /**
   * Constructor.
   */
  public ResultCache() {
    cacheSize = Properties.getCacheSize();
    cache = new LinkedList();
  }
  
  /**
   * Add a query result to the cache.
   * 
   * @param myResult - QueryResult object
   */
  public void cacheResult(QueryResult myResult)  throws Exception {
    
    if (myResult.getNumRows() > Properties.getCacheThreshold()) {
  
      if (Properties.isCacheResultsWithNoRows() || myResult.getNumRows() > 0) {
        // This QueryResult is too large and will be stored on disk instead.
        Random rand = new Random();
        int suffix = rand.nextInt(Integer.MAX_VALUE);
        String resultCacheFileName = "RichMonResultCache" + suffix;
        myResult.setResultCacheFileName(resultCacheFileName);
        
        File myFile;
        myFile = File.createTempFile(resultCacheFileName,".tmp");
        myFile.deleteOnExit();
        
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        
        fos = new FileOutputStream(myFile);
        out = new ObjectOutputStream(fos);
        out.writeObject(myResult);
        out.close();
        
        if (debug) System.out.println("Saved resultcache entry: " + myFile.getAbsolutePath());
        
        // create a temporary object for the resultcache in place of the real result
        Vector errMsg = new Vector();
        Vector lineOne = new Vector();
        Vector lineTwo = new Vector();
        Vector lineThree = new Vector();
        Vector lineFour = new Vector();
   
        
        lineOne.add("This result has not been added to the result cache!");
        lineTwo.add("");
        lineThree.add("This is because this result returned " + myResult.getNumRows() + " rows which is more than the setting of cache_threshold (" + Properties.getCacheThreshold() + " rows).  This result was cached to disk instead.");
        lineFour.add("This parameter is set in your richmon.properties.  You should never see this message, if you do its a fault...");
        
        errMsg.add(lineOne);
        errMsg.add(lineTwo);
        errMsg.add(lineThree);
        errMsg.add(lineFour);
        
        QueryResult newResult = new QueryResult();
        newResult.setResultSet(errMsg);
        newResult.setNumRows(4);
        newResult.setNumCols(1);
        int[] columnWidths = {120};
        newResult.setColumnWidths(columnWidths);
        String[] resultHeadings = {"Information..."};
        newResult.setResultHeadings(resultHeadings);
        String[] columnTypes = {"VARCHAR"};
        newResult.setColumnTypes(columnTypes);
        newResult.setExecutionTime(myResult.getExecutionTime());
        newResult.setResultCacheFileName(myFile.getAbsolutePath());
      
        cache.addFirst(newResult);
        cachePointer = 0;
      }
    }
    else {
      if (Properties.isCacheResultsWithNoRows() || myResult.getNumRows() > 0) {
        cache.addFirst(myResult);
        cachePointer = 0;
      }
    }
    
    if (debug) {
      // dump content of resultCache
      for (int i=0; i < cache.size(); i++) {
        System.out.println("resultCache Dump of entry " + i + " " + cache.get(i).toString());
      }
      System.out.println("\ncachePointer= " + cachePointer);
    }
    
    // remove older entries if the cache is nearing capacity 
   if (cache.size() > cacheSize) cache.removeLast();
  }
    
  /**
   * Get the last query result.
   * 
   * @return QueryResult
   * @throws java.lang.Exception
   */
  public QueryResult getLastResult() throws Exception {   
    QueryResult cachedResult = new QueryResult();
    
    
    try {
      cachePointer = Math.min(++cachePointer, cache.size() -1);
      cachedResult = (QueryResult)cache.get(cachePointer);
      
      if (cachedResult.getResultCacheFileName() instanceof String) {
        // this result is actually stored on disk
        
        String resultCacheFilename = cachedResult.getResultCacheFileName();
        
        QueryResult myResult = new QueryResult();
        FileInputStream fis;
        ObjectInputStream ins;
        try {
          fis = new FileInputStream(resultCacheFilename);
          ins = new ObjectInputStream(fis);
          myResult = (QueryResult)ins.readObject();
          ins.close();
          
          if (debug) System.out.println("Read in resultcache entry: " + resultCacheFilename);
        }
        catch (IOException e) {
          ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
        }
        catch (ClassNotFoundException e) {
          ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
        }
        
        cachedResult = myResult;
      }
    }
    catch (Exception e) {
      throw new NoMoreResultsInCacheException("Cannot proceed beyound cache start point");      
    }
    
    
    return cachedResult;
  }
  
  /**
   * Get the next query result.
   * 
   * @return QueryResult
   * @throws java.lang.Exception
   */
  public QueryResult getNextResult() throws Exception {
    QueryResult cachedResult = new QueryResult();
    

    try {
      cachedResult = (QueryResult)cache.get(--cachePointer);
      
      if (cachedResult.getResultCacheFileName() instanceof String) {
        // this result is actually stored on disk
        
        String resultCacheFilename = cachedResult.getResultCacheFileName();
        
        QueryResult myResult = new QueryResult();
        FileInputStream fis;
        ObjectInputStream ins;
        try {
          fis = new FileInputStream(resultCacheFilename);
          ins = new ObjectInputStream(fis);
          myResult = (QueryResult)ins.readObject();
          ins.close();
          
          if (debug) System.out.println("Read in resultcache entry: " + resultCacheFilename);
        }
        catch (IOException e) {
          ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
        }
        catch (ClassNotFoundException e) {
          ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
        }
        
        cachedResult = myResult;
      }
    }
    catch (Exception e) {
      throw new NoMoreResultsInCacheException("Cannot proceed beyond cache end point");
    }
    
    return cachedResult;
  }
  
  public QueryResult getResult(int i) {
    QueryResult cachedResult = (QueryResult)cache.get(i);
    
    if (cachedResult.getResultCacheFileName() instanceof String) {
      // this result is actually stored on disk
      
      String resultCacheFilename = cachedResult.getResultCacheFileName();
      
      QueryResult myResult = new QueryResult();
      FileInputStream fis;
      ObjectInputStream ins;
      try {
        fis = new FileInputStream(resultCacheFilename);
        ins = new ObjectInputStream(fis);
        myResult = (QueryResult)ins.readObject();
        ins.close();
      }
      catch (IOException e) {
        ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
      }
      catch (ClassNotFoundException e) {
        ConsoleWindow.displayError(e, this, "Error trying to read in a saved resultcache entry : " + resultCacheFilename);
      }
      
      cachedResult = myResult;
    }
    
    return cachedResult;
  }
  
  public int getNumCacheEntries() {
    return cache.size();
  }
  
}