/*
 * RichButton.java        17.43 30/04/10
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
 * Change History
 * =============
 * 12/10/11 Richard Wright Removed the construct, which was never used anyway
 * 14/08/15 Richard Wright Modified to allow filter by user and improved readability
 * 16/12/15 Richard Wright Removed old methods for filtered statements and code cleanup
 */

package RichMon;

import java.awt.Font;
import java.awt.event.MouseEvent;

import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class RichButton extends JButton {
  
  JLabel statusBar;         // The statusBar to be updated after a query is executed 
  JScrollPane scrollP;      // The JScrollPane on which output should be displayed 
  ResultCache resultCache;  // The resultCache to hold the QueryResult
  String[] options;         // The options listed for this button

  
  public void addItem(String newOption) {
    // check to see whether this option already exists
    boolean dup = false;
    for (int i=0; i < options.length; i++) {
      if (options[i].equals(newOption)) {
        dup = true;
        break;
      }
    }
    
    // if its not a duplicate, add it to the options array
    if (!dup) {
      String[] tmp = new String[options.length];
      System.arraycopy(options,0,tmp,0,options.length);
      options = new String[options.length +1];
      System.arraycopy(tmp,0,options,0,tmp.length);
      options[options.length-1] = newOption;
    }
  }
  
  
  public void executeDisplayFilterStatement(Cursor myCursor
                                            ,Parameters myPars
                                            ,boolean flip
                                            ,boolean eggTimer
                                            ,JScrollPane scrollP
                                            ,JLabel statusBar
                                            ,boolean showSQL
                                            ,ResultCache resultCache
                                            ,boolean restrictRows
                                            ,String filterByRACAlias
                                            ,String filterByUserAlias
                                            ,boolean includeDecode
                                            ,String includePoint
                                            ,Boolean filterByRAC
                                            ,Boolean filterByUser) throws Exception {   
    
    String filterByContainerAlias = "c";
    boolean includeContainerName = false;
    boolean filterByContainer = false;
    
    executeDisplayFilterStatement(myCursor
                                  ,myPars
                                  ,flip
                                  ,eggTimer
                                  ,scrollP
                                  ,statusBar
                                  ,showSQL
                                  ,resultCache
                                  ,restrictRows
                                  ,filterByRACAlias
                                  ,filterByUserAlias
                                  ,filterByContainerAlias
                                  ,includeDecode
                                  ,includeContainerName
                                  ,includePoint
                                  ,filterByRAC
                                  ,filterByUser
                                  ,filterByContainer);
    
  }
  
  public void executeDisplayFilterStatement(Cursor myCursor
                                            ,Parameters myPars
                                            ,boolean flip
                                            ,boolean eggTimer
                                            ,JScrollPane scrollP
                                            ,JLabel statusBar
                                            ,boolean showSQL
                                            ,ResultCache resultCache
                                            ,boolean restrictRows
                                            ,String filterByRACAlias
                                            ,String filterByUserAlias
                                            ,String filterByContainerAlias
                                            ,boolean includeDecode
                                            ,boolean includeContainerName
                                            ,String includePoint
                                            ,boolean filterByRAC
                                            ,boolean filterByUser
                                            ,boolean filterByContainer) throws Exception {   
    
    // add a predicate to the where clause for each instance
    if (filterByRAC) {
      if (ConsoleWindow.isDbRac()) {
        if (!ConsoleWindow.isOnlyLocalInstanceSelected() && includeDecode)
          myCursor.includeRACDecode(filterByRACAlias);
        if (includePoint.equals("beforeOrderBy")) {
          myCursor.includeRACPredicateBeforeOrderBy(filterByRACAlias, true);
        } else {
          myCursor.includeRACPredicate(filterByRACAlias);
        }
      }
    }

    if (filterByRAC && ConsoleWindow.isDbRac() && !ConsoleWindow.isOnlyLocalInstanceSelected()) myCursor.setFilterByRAC(true);
    
    if (filterByUser) {
      myCursor.setUsernameFilter(ConsoleWindow.getUsernameFilter());
      myCursor.includeUserFilter(filterByUserAlias);
    }

    // add a predicate to the where clause for each container
    if ((ConsoleWindow.getDBVersion() >= 12.0)) {
      if (filterByContainer) {
        if (ConsoleWindow.getNumContainers() > 1) {
          boolean onlyLocalContainerSelected = ConsoleWindow.isOnlyLocalContainerSelected();
          if (!onlyLocalContainerSelected) {
            if (includeContainerName) {
            myCursor.includeContainerDecode(filterByContainerAlias);
            }
          }
          if (includePoint.equals("beforeOrderBy")) {
            myCursor.includeContainerPredicateBeforeOrderBy(filterByContainerAlias, true);
          } else {
            myCursor.includeContainerPredicate(filterByContainerAlias);
          }
        }
      }

      if (filterByContainer && ConsoleWindow.getNumContainers() > 1 && ConsoleWindow.getNumSelectedContainers() > 0)
        myCursor.setFilterByContainer(true);
    }

    myCursor.setFlippable(flip);
    myCursor.setEggTimer(eggTimer);
                                                      
//    ExecuteDisplay.executeDisplay(myCursor, myPars, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByUser, filterByRAC);
    ExecuteDisplay.executeDisplay(myCursor, myPars, scrollP, statusBar, showSQL, resultCache, restrictRows);
  }    
  
  public QueryResult executeFilterStatement(Cursor myCursor
                                           ,Parameters myPars
                                           ,boolean flip
                                           ,boolean eggTimer
                                           ,ResultCache resultCache
                                           ,boolean restrictRows
                                           ,String filterByRACAlias
                                           ,String filterByUserAlias
                                           ,boolean includeDecode
                                           ,String includeRACPredicatePoint
                                           ,Boolean filterByRAC
                                           ,Boolean filterByUser) throws Exception {
                                              
    String filterByContainerAlias = "c";
    boolean includeContainerName = false;
    boolean filterByContainer = false;
    
    QueryResult myResultSet = executeFilterStatement( myCursor
                                                     , myPars
                                                     , flip
                                                     , eggTimer
                                                     , resultCache
                                                     , restrictRows
                                                     , filterByRACAlias
                                                     , filterByUserAlias
                                                     , filterByContainerAlias
                                                     , includeDecode
                                                     , includeContainerName
                                                     , includeRACPredicatePoint
                                                     , filterByRAC
                                                     , filterByUser
                                                     , filterByContainer);
    
    return myResultSet;                                                
  }
  
  
  public QueryResult executeFilterStatement(Cursor myCursor
                                           ,Parameters myPars
                                           ,boolean flip
                                           ,boolean eggTimer
                                           ,ResultCache resultCache
                                           ,boolean restrictRows
                                           ,String filterByRACAlias
                                           ,String filterByUserAlias
                                           ,String filterByContainerAlias
                                           ,boolean includeDecode
                                           ,boolean includeContainerName
                                           ,String includePoint
                                           ,Boolean filterByRAC
                                           ,Boolean filterByUser
                                           ,Boolean filterByContainer) throws Exception {   
    
    // add a predicate to the where clause for each instance
    if (filterByRAC) {
      if (ConsoleWindow.isDbRac()) {
        if (!ConsoleWindow.isOnlyLocalInstanceSelected() && includeDecode)
          myCursor.includeRACDecode(filterByRACAlias);
        if (includePoint.equals("beforeOrderBy")) {
          myCursor.includeRACPredicateBeforeOrderBy(filterByRACAlias, true);
        } else {
          myCursor.includeRACPredicate(filterByRACAlias);
        }
      }
    }    
    
    // add a predicate to the where clause for each container
    if ((ConsoleWindow.getDBVersion() >= 12.0)) {
      if (filterByContainer) {
        if (ConsoleWindow.getNumContainers() > 1) {
          if (ConsoleWindow.isOnlyLocalContainerSelected() && includeContainerName)
            myCursor.includeContainerDecode(filterByContainerAlias);
          if (includePoint.equals("beforeOrderBy")) {
            myCursor.includeContainerPredicateBeforeOrderBy(filterByContainerAlias, true);
          } else {
            myCursor.includeContainerPredicate(filterByContainerAlias);
          }
        }
      }
    }

    if (filterByUser) myCursor.includeUserFilter(filterByUserAlias);
    myCursor.setFlippable(flip);
    myCursor.setEggTimer(eggTimer);
                                                      
    QueryResult myResultSet = ExecuteDisplay.execute(myCursor, myPars, flip, eggTimer, resultCache, restrictRows);
    
    return myResultSet;
  }  
}

