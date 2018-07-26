/*
 * SQLCache.java        12.15 05/01/04
 *
 * Copyright (c) 2005 Oracle Corporation UK Limited
 * 5 Hollis Wood Drive, Wrecclesham, Farnham, Surrey.  GU10 4JT
 * All rights reserved.
 *
 * RichMon is a lightweight database monitoring tool.  
 * 
 * Keep up to date with the latest developement at http://richmon.blogspot.com
 * 
 * Report bugs and request new features by email to support@richmon4oracle.com
 * 
 * Change History since 23/05/05
 * =============================
 * 23/05/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 10/07/06 Richard Wright Copies the compressCache method over from ResultCache
 *                         to resolve bug.
 * 13/07/05 Richard Wright Made the cacheSize configurable
 * 13/09/06 Richard Wright Complete re-write to use collections rather than arrays
 * 13/09/06 Richard Wright Modified the comment style and error handling
 * 14/07/11 Richard Wright Fixed obscure bug in moving backwards when the cache only has a single entry
 * 
 */
 
 package RichMon;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.LinkedList;

/**
 * A cache to store Cursor objects to avoid the overhead of reloading them from disk.
 */
public class SQLCache {

  private  int cacheSize = 50; // max number of entries in cache 

  private LinkedList cache;
  
  private int cachePointer = 0;
  boolean debug = false;

  /**
   * Constructor
   * 
   * A cache to store sql objects 
   */
  public SQLCache() {
    cache = new LinkedList();
  }


  public String getLastSQL() throws Exception {
    if (cachePointer == cache.size() -1 & cachePointer > 1) {
      if (debug) System.out.println("cachePointer: " + cachePointer + "    : cacheSize: " + cache.size());
      throw new NoMoreResultsInCacheException("Cannot proceed beyond the start of the sql cache");
    }
    else {
      cachePointer = Math.min(++cachePointer,cache.size() -1);
      if (debug ) System.out.println("cachePointer: " + cachePointer + "    : cacheSize: " + cache.size());
    }
    return (String)cache.get(cachePointer);
  }
  
  public String getNextSQL() throws Exception {
    if (cachePointer == 0) {
      if (debug) System.out.println("cachePointer: " + cachePointer + "    : cacheSize: " + cache.size());
      throw new NoMoreResultsInCacheException("Cannot proceed beyond the end of the sql cache");
    }
    else {
      --cachePointer;
      if (debug) System.out.println("cachePointer: " + cachePointer + "    : cacheSize: " + cache.size());
    }
    return (String)cache.get(cachePointer);
  }

  public void cacheSQL(String sqlTxt) {
    // add entry to the LRU and cache
    cache.addFirst(sqlTxt);

    if (cache.size() > cacheSize) {
      String key = (String)cache.getLast();
      cache.remove(key);
    }

    cachePointer = 0;
  }
  
  public String getResult(int i) {
    return cache.get(i).toString();
    
  }
}
