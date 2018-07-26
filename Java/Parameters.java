/*
 * Parameters.java        12.15 05/11/08
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
 * Change History since 05/05/05
 * =============================
 * 
 *                         
 */


package RichMon;

import java.util.HashMap;
import java.util.Vector;
import oracle.sql.RAW;

public class Parameters {
  
  private boolean debug = false;
  private Vector parameters = new Vector();
  private int numParameters = 0;
  
  HashMap myHash = new HashMap();


  public Parameters() {
  }
  
  public void addParameter(String type,Object value) {
    Object[] singleParameter = new String[2];
    
    singleParameter[0] = type;
    if (singleParameter[0].equals("byte[]")) {
      myHash.put(numParameters,value);
    }
    else {
      singleParameter[1] = String.valueOf(value);
    }
 
    parameters.add(singleParameter);
    numParameters++;
  }
  
  public int getNumParameters() {
    return numParameters;
  }
  
  public Object[] getParameter(int i) {
    Object[] singleParameter = (Object[])parameters.elementAt(i);
    
    return singleParameter;
  }
  
  public byte[] getByteParameter(int i) {
    return (byte[])myHash.get(i);
  }
}
