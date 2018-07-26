/*
 * CannotShowSQLException.java        14.01 22/08/06
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

public class CannotShowSQLException extends Exception {
  public CannotShowSQLException() {
    super();
  }

  public CannotShowSQLException(String msg) {
    super(msg);
  }
}
