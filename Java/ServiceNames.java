/*
 * ServiceNames.java        17.43 29/04/10
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
 * Change History since 29/04/10
 * =============================
 * 
 * 29/04/10 Richard Wright Created this class and moved the functionality from ConnectWindow.  Gave the code a sprint clean.
 * 29/07/11 Richard Wright The service name, host number nad port number are now trimmed to remove trailing spaces before saving
 * 29/07/11 Richard Wright The lastServiceName file now saves correctly on Linux
 * 11/08/11 Richard Wright Modification so that username is saved and restored along with servicename, hostname nad portnumber.
 * 14/02/12 Richard Wright Changed format of servicenames files to include indicator of servicename or sid
 * 27/11/12 Richard Wright Use Sets to remove duplicate and then sort servicenames
 */

package RichMon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.util.SortedSet;

import java.util.TreeSet;

import javax.swing.JOptionPane;


public class ServiceNames {
  
  static File serviceNamesFile;
  static File lastServiceNameFile;
  static BufferedReader serviceNamesReader;
  static BufferedWriter serviceNamesWriter;
  static BufferedReader lastServiceNameReader;
  static BufferedWriter lastServiceNameWriter;
  
  private static String[] serviceNamesArray;
  private static String  importedServiceName;
  private static boolean preSetServiceName = false;
  private static boolean serviceNamesFileExists = false;
  private static boolean debug = false;
  
  /**
   * Populates the service names combo box with services names from the 
   * file 'C:\RichMon\ServiceNames\RichMonServiceNames.txt', always sorting entries in alphabetical 
   * order first.
   * 
   * This needs a re-write to use treeSet.
   * 
   */
  public static void getServiceNamesFromDisk() throws Exception {
    if (ConnectWindow.isLinux()) {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/RichMonServiceNames.txt");
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/LastServiceName.txt");   

    } else {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\RichMonServiceNames.txt");        
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\LastServiceName.txt");        
    }
    serviceNamesReader = new BufferedReader(new FileReader(serviceNamesFile));     
    
    /* read in service names from file */
    String[] tmpNamesArray = new String[1000];
    String inputLine;
    int numServiceNames = 0;
    for (; numServiceNames < tmpNamesArray.length; numServiceNames++) {
      inputLine = serviceNamesReader.readLine();
      if (inputLine instanceof String) {
        tmpNamesArray[numServiceNames] = inputLine;
      }
      else {
        // eof
        break;
      }  
    }
          
    // add imported service name from command line
    if (preSetServiceName) tmpNamesArray[numServiceNames++] = importedServiceName;
    
    /* sort the serviceNames */
    serviceNamesArray = new String[numServiceNames];
    System.arraycopy(tmpNamesArray,0,serviceNamesArray,0,numServiceNames);
    Arrays.sort(serviceNamesArray);
    
    serviceNamesReader.close();
    serviceNamesFileExists = true;
    String serviceNameOrSID = "";
    
    if (lastServiceNameFile.exists()) {
      boolean endOfRow = false;
      lastServiceNameReader = new BufferedReader(new FileReader(lastServiceNameFile));
      String lastServiceName = lastServiceNameReader.readLine();
      if (lastServiceName.indexOf(":") > 0) {
        if (!lastServiceName.substring(0,lastServiceName.indexOf(":")).equals("null")) {
          serviceNameOrSID = lastServiceName.substring(0,lastServiceName.indexOf(":"));
        }
        String t = lastServiceName.substring(lastServiceName.indexOf(":") +1);
        lastServiceName = lastServiceName.substring(0,lastServiceName.indexOf(":"));   
        if (!t.substring(0,t.indexOf(":")).equals("null")) {
          ConnectWindow.setHostName(t.substring(0,t.indexOf(":")));
        }
        t = t.substring(t.indexOf(":") +1);
        if (t.indexOf(":") >= 0) {
          if (!t.substring(0, t.indexOf(":")).equals("null")) {
            ConnectWindow.setPortNumber(t.substring(0, t.indexOf(":")));
          }
        }
        else {
          if (!t.equals("null")) {
            ConnectWindow.setPortNumber(t);
            endOfRow = true;
          }
        }
        if (!endOfRow) {
          if (t.indexOf(":") >= 0) {
            t = t.substring(t.indexOf(":") + 1);
            ConnectWindow.setUsername(t.substring(0,t.indexOf(":")));
            
            t = t.substring(t.indexOf(":") + 1);
            
            if (t.equals("sid")) ConnectWindow.setSID(serviceNameOrSID);
            if (t.toLowerCase().equals("servicename")) ConnectWindow.setServiceName(serviceNameOrSID);


          }
        }
      }
      else {
        ConnectWindow.setServiceName(lastServiceName);
      }
      
      /* set the service name */
      if (preSetServiceName) {
        ConnectWindow.setServiceName(importedServiceName);
      }
    }
  }
  
  /**
   * Saves all the services names in the combo box to a file called 
   * '$RICHMON_BASE\ServiceNames\RichMonServiceNames.txt' and including the newly 
   * specified entry.  The existing version of the file is overwritten not 
   * appended too.  
   * 
   * To improve performance this is done in a new thread.
   * 
   * This needs a re-write to use the java collections which will simplify the 
   * code and improve it's performance
   * 
   */
  public static void save(final String serviceName
                               ,final ConnectWindow connectWindow) {
    Thread saveServiceNamesThread = new Thread ( new Runnable() {
      public void run() {
        // Use Set's to remove duplicate and then sort servicenames
        
        Set serviceNamesSet = new HashSet(); 
        
        // Add each servicename to the set - duplicates fail 
        if (serviceNamesFileExists) {
          for (int i = 0; i < serviceNamesArray.length; i++) {
            if (serviceNamesArray[i] instanceof String) {
              serviceNamesSet.add(serviceNamesArray[i].toLowerCase());
            }
          }
        }
          
        // Add the new servicename to the set
        
        if (ConnectWindow.serviceNameCB.isSelected()) {
          serviceNamesSet.add(serviceName.toLowerCase() + ":" +
                              ConnectWindow.getHostName().toLowerCase() + ":" +
                              ConnectWindow.getPortNumber() + ":" + ConnectWindow.getUsername() + ":serviceName");

        }
        else {
          serviceNamesSet.add(serviceName.toLowerCase() + ":" +
                                ConnectWindow.getHostName().toLowerCase() + ":" +
                                ConnectWindow.getPortNumber() + ":" + ConnectWindow.getUsername() + ":sid");
        }
        
        if (debug) {
          // list out all unique but unsorted servicenames
          Iterator debugIterator = serviceNamesSet.iterator();
          while (debugIterator.hasNext()) {
            System.out.println("unique service name: " + debugIterator.next().toString());       
          }
        }
        
        // serviceNamesSet contains a unique list, now it needs to be sorted
        SortedSet sortedNames = new TreeSet();
        Iterator it = serviceNamesSet.iterator();
        while (it.hasNext()) {
          sortedNames.add(it.next().toString());        
        }
        
        if (debug) {
          // list out all unique but unsorted servicenames
          Iterator debugIterator2 = sortedNames.iterator();
          while (debugIterator2.hasNext()) {
            System.out.println("sorted service name: " + debugIterator2.next().toString());       
          }
        }
        
        
        // write out the new service names file
        try {
          serviceNamesFile.createNewFile();
          serviceNamesWriter = new BufferedWriter(new FileWriter(serviceNamesFile));
          
          Iterator itt = sortedNames.iterator();
          while (itt.hasNext()) {
            serviceNamesWriter.write(itt.next().toString());
            serviceNamesWriter.newLine();             
          }
          
          serviceNamesWriter.close();
          
        }
        catch (Exception e){
          String errorMsg = "Saving Service Names...\n\n" + e.toString();
          JOptionPane.showMessageDialog(connectWindow,errorMsg,"Error...",JOptionPane.ERROR_MESSAGE);
        }
          
        try {
          // save the last service name 
          if (lastServiceNameFile.exists()) lastServiceNameFile.delete();
          lastServiceNameFile.createNewFile();
          
          lastServiceNameWriter = new BufferedWriter(new FileWriter(lastServiceNameFile));
          
          StringBuffer lastServiceName = new StringBuffer(serviceName);

          if (ConnectWindow.serviceNameCB.isSelected()) {
              lastServiceName.append(":" + ConnectWindow.getHostName().toLowerCase().trim() + ":" + ConnectWindow.getPortNumber().trim() + ":" + ConnectWindow.getUsername().trim() + ":serviceName");
          }
          else {
            lastServiceName.append(":" + ConnectWindow.getHostName().toLowerCase().trim() + ":" + ConnectWindow.getPortNumber().trim() + ":" + ConnectWindow.getUsername().trim() + ":sid");
          }


          if (debug) System.out.println("Writing Last serviceName out: " + lastServiceName.toString());
          if (debug) System.out.println("is LastServiceName.txt writeable: " + lastServiceNameFile.canWrite());
                
          lastServiceNameWriter.write(lastServiceName.toString());
          lastServiceNameWriter.close();
          
        }
        catch (IOException e) {
          String errorMsg = "Saving Last Service Name...\n\n" + e.toString();
          JOptionPane.showMessageDialog(connectWindow,errorMsg,"Error...",JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    
    saveServiceNamesThread.setName("saveServiceNames");
    saveServiceNamesThread.setDaemon(true);

    saveServiceNamesThread.start();
  }


  public static void setImportedServiceName(String importedServiceName) {
    ServiceNames.importedServiceName = importedServiceName;
  }

  public static void setServiceNamesFileExists(boolean serviceNamesFileExists) {
    ServiceNames.serviceNamesFileExists = serviceNamesFileExists;
  }

  public static void setPreSetServiceName(boolean preSetServiceName) {
    ServiceNames.preSetServiceName = preSetServiceName;
  }

  public static String[] getServiceNamesArray() {
    return serviceNamesArray;
  }
  
  public static boolean isPreSetServiceName() {
    return preSetServiceName;
  }
  
  /**
   * If the servicenames files is in the format used prior to 1752 it needs updating
   */
  public static void upgradeTo1752Format() throws Exception {
    if (ConnectWindow.isLinux()) {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/RichMonServiceNames.txt");
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/LastServiceName.txt");   

    } else {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\RichMonServiceNames.txt");        
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\LastServiceName.txt");        
    }
    
    /*
     * upgrade RichMonServiceNames.txt
     */

    
    serviceNamesReader = new BufferedReader(new FileReader(serviceNamesFile));     
    
    /* read in service names from file */
    String[] tmpNamesArray = new String[1000];
    String inputLine;

    int numServiceNames = 0;
    for (; numServiceNames < tmpNamesArray.length; numServiceNames++) {
      inputLine = serviceNamesReader.readLine();
      if (inputLine instanceof String) {
        String servicename;
        String hostname = "";
        String portnumber = "";
        String username = "";
        
        String t= inputLine;
        if (t.indexOf(":") > -1) {
          servicename = t.substring(0,t.indexOf(":"));
          t = t.substring(t.indexOf(":") +1);
          if (t.indexOf(":") > -1) {
            hostname = t.substring(0, t.indexOf(":"));
            t = t.substring(t.indexOf(":") +1);
            portnumber = t;
            username = "system";
          }
        }
        else {
          servicename = t;
          username = "system";
        }
        tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username;
      }
      else {
        // eof
        break;
      }  
    }
    
    serviceNamesFile.createNewFile();
    serviceNamesWriter = new BufferedWriter(new FileWriter(serviceNamesFile));
          
    // add imported service name from command line
    if (preSetServiceName) tmpNamesArray[numServiceNames++] = importedServiceName;
    
    /* sort the serviceNames */
    serviceNamesArray = new String[numServiceNames];
    System.arraycopy(tmpNamesArray,0,serviceNamesArray,0,numServiceNames);
    Arrays.sort(serviceNamesArray);
    
    serviceNamesReader.close();
    serviceNamesFileExists = true;
    
    // write existing service names 
    if (serviceNamesArray instanceof String[]) {
      for (int i=0; i < serviceNamesArray.length; i++) {
        if (serviceNamesArray[i] instanceof String) {
          serviceNamesWriter.write(serviceNamesArray[i].toLowerCase());
          serviceNamesWriter.newLine();  
        }
        else {
          break;
        }
      }
    }
    
    serviceNamesWriter.close();
    
    /*
     * upgrade LastServiceName.txt
     */

    
    serviceNamesReader = new BufferedReader(new FileReader(lastServiceNameFile));     
    
    /* read in service names from file */
    tmpNamesArray = new String[1000];
    String inputLine2;

    numServiceNames = 0;
    for (; numServiceNames < tmpNamesArray.length; numServiceNames++) {
      inputLine2 = serviceNamesReader.readLine();
      if (inputLine2 instanceof String) {
        String servicename;
        String hostname = "";
        String portnumber = "";
        String username = "";
        
        String t= inputLine2;
        if (t.indexOf(":") > -1) {
          servicename = t.substring(0,t.indexOf(":"));
          t = t.substring(t.indexOf(":") +1);
          if (t.indexOf(":") > -1) {
            hostname = t.substring(0, t.indexOf(":"));
            t = t.substring(t.indexOf(":") +1);
            portnumber = t;
            username = "system";
          }
        }
        else {
          servicename = t;
          username = "system";
        }
        tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username;
      }
      else {
        // eof
        break;
      }  
    }
    
    serviceNamesFile.createNewFile();
    serviceNamesWriter = new BufferedWriter(new FileWriter(serviceNamesFile));
          
    // add imported service name from command line
    if (preSetServiceName) tmpNamesArray[numServiceNames++] = importedServiceName;
    
    /* sort the serviceNames */
    serviceNamesArray = new String[numServiceNames];
    System.arraycopy(tmpNamesArray,0,serviceNamesArray,0,numServiceNames);
    Arrays.sort(serviceNamesArray);
    
    serviceNamesReader.close();
    serviceNamesFileExists = true;
    
    // write existing service names 
    if (serviceNamesArray instanceof String[]) {
      for (int i=0; i < serviceNamesArray.length; i++) {
        if (serviceNamesArray[i] instanceof String) {
          serviceNamesWriter.write(serviceNamesArray[i].toLowerCase());
          serviceNamesWriter.newLine();  
        }
        else {
          break;
        }
      }
    }
    
    serviceNamesWriter.close();
  }
  
  
  /**
   * If the servicenames files is in the format used prior to 1754 it needs updating
   */
  public static void upgradeTo1754Format() throws Exception {
    if (ConnectWindow.isLinux()) {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/RichMonServiceNames.txt");
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "/ServiceNames/LastServiceName.txt");   

    } else {
      serviceNamesFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\RichMonServiceNames.txt");        
      lastServiceNameFile = new File(ConnectWindow.getBaseDir() + "\\ServiceNames\\LastServiceName.txt");        
    }
    
    /*
     * upgrade RichMonServiceNames.txt
     */

    
    serviceNamesReader = new BufferedReader(new FileReader(serviceNamesFile));     
    
    /* read in service names from file */
    String[] tmpNamesArray = new String[1000];
    String inputLine;

    int numServiceNames = 0;
    for (; numServiceNames < tmpNamesArray.length; numServiceNames++) {
      inputLine = serviceNamesReader.readLine();
      if (inputLine instanceof String) {
      if (debug) System.out.println("upgrading:" + inputLine);
        String servicename;
        String hostname = "";
        String portnumber = "";
        String username = "";
        
        String t= inputLine;
        if (t.indexOf(":") > -1) {
          servicename = t.substring(0,t.indexOf(":"));
          t = t.substring(t.indexOf(":") +1);
          if (t.indexOf(":") > -1) {
            hostname = t.substring(0, t.indexOf(":"));
            t = t.substring(t.indexOf(":") +1);
            if (t.indexOf(":") == -1) {
              portnumber = t;
              username = "system";
            }
            else {
              portnumber = t.substring(0, t.indexOf(":"));
              if (t.indexOf(":") >= 0) {
                t = t.substring(t.indexOf(":") +1);
                username = t;
              }
              else {
                username = "system";
              }
            }
            
          }
        }
        else {
          servicename = t;
          username = "system";
        }
        
        if (hostname.length() > 1) {
          tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username + ":sid";
        }
        else {
          tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username + ":serviceName";
        }
      }
      else {
        // eof
        break;
      }  
    }
    
    serviceNamesFile.createNewFile();
    serviceNamesWriter = new BufferedWriter(new FileWriter(serviceNamesFile));
          
    // add imported service name from command line
    if (preSetServiceName) tmpNamesArray[numServiceNames++] = importedServiceName;
    
    /* sort the serviceNames */
    serviceNamesArray = new String[numServiceNames];
    System.arraycopy(tmpNamesArray,0,serviceNamesArray,0,numServiceNames);
    Arrays.sort(serviceNamesArray);
    
    serviceNamesReader.close();
    serviceNamesFileExists = true;
    
    // write existing service names 
    if (serviceNamesArray instanceof String[]) {
      for (int i=0; i < serviceNamesArray.length; i++) {
        if (serviceNamesArray[i] instanceof String) {
          serviceNamesWriter.write(serviceNamesArray[i].toLowerCase());
          serviceNamesWriter.newLine();  
        }
        else {
          break;
        }
      }
    }
    
    serviceNamesWriter.close();
    
    /*
     * upgrade LastServiceName.txt
     */

    
    serviceNamesReader = new BufferedReader(new FileReader(lastServiceNameFile));     
    
    /* read in service names from file */
    tmpNamesArray = new String[1000];
    String inputLine2;

    numServiceNames = 0;
    for (; numServiceNames < tmpNamesArray.length; numServiceNames++) {
      inputLine2 = serviceNamesReader.readLine();
      if (inputLine2 instanceof String) {
        String servicename;
        String hostname = "";
        String portnumber = "";
        String username = "";
        
        String t= inputLine2;
        if (t.indexOf(":") > -1) {
          servicename = t.substring(0,t.indexOf(":"));
          t = t.substring(t.indexOf(":") +1);
          if (t.indexOf(":") > -1) {
            hostname = t.substring(0, t.indexOf(":"));
            t = t.substring(t.indexOf(":") +1);
            portnumber = t.substring(0, t.indexOf(":"));
            if (t.indexOf(":") >= 0) {
              t = t.substring(t.indexOf(":") +1);
              username = t;
            }
            else {
              username = "system";
            }
          }
        }
        else {
          servicename = t;
          username = "system";
        }
        
        if (hostname.length() > 1) {
          tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username + ":sid";
        }
        else {
          tmpNamesArray[numServiceNames] = servicename + ":" + hostname + ":" + portnumber + ":" + username + ":serviceName";
        }
      }
      else {
        // eof
        break;
      }  
    }
    
    serviceNamesFile.createNewFile();
    serviceNamesWriter = new BufferedWriter(new FileWriter(lastServiceNameFile));
          
    // add imported service name from command line
    if (preSetServiceName) tmpNamesArray[numServiceNames++] = importedServiceName;
    
    /* sort the serviceNames */
    serviceNamesArray = new String[numServiceNames];
    System.arraycopy(tmpNamesArray,0,serviceNamesArray,0,numServiceNames);
    Arrays.sort(serviceNamesArray);
    
    serviceNamesReader.close();
    serviceNamesFileExists = true;
    
    // write existing service names 
    if (serviceNamesArray instanceof String[]) {
      for (int i=0; i < serviceNamesArray.length; i++) {
        if (serviceNamesArray[i] instanceof String) {
          serviceNamesWriter.write(serviceNamesArray[i].toLowerCase());
          serviceNamesWriter.newLine();  
        }
        else {
          break;
        }
      }
    }
    
    serviceNamesWriter.close();
  }
}
