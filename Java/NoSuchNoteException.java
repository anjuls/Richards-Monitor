/*
 *  NoSuchNoteException.java        12.15 06/09/04
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
 * A request has been made to read a note from the jar file which does not exist.
 */
public class NoSuchNoteException extends Exception  {
  public NoSuchNoteException() {
    super();
  }
  
  public NoSuchNoteException(String msg){
    super(msg);
  }
}