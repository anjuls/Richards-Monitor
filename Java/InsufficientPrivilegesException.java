/*
 *  InsufficientPrivilegesException.java        14.01 18/08/06
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
 */


package RichMon;

/**
 * An attempt has been made to read past the end or beginning of the ResultsCache.
 * or one of the following is not set to true in the RichMon.properties file:
 * 
 *    allow_alter_session
 *    allow_alter_system
 *    allow_alter_database
 *    
 */
public class InsufficientPrivilegesException extends Exception {
  public InsufficientPrivilegesException() {
    super();
  }

  public InsufficientPrivilegesException(String msg) {
    super(msg);
  }
}
