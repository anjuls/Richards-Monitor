package RichMon;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RichPanel extends JPanel {
  
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
    
    // add a predicate to the where clause for each instance
    if (filterByRAC) {
      if (ConsoleWindow.isDbRac()) {
        if (!ConsoleWindow.isOnlyLocalInstanceSelected() && includeDecode)
          myCursor.includeDecode(filterByRACAlias);
        if (includeRACPredicatePoint.equals("beforeOrderBy")) {
          myCursor.includePredicateBeforeOrderBy(filterByRACAlias, true);
        } else {
          myCursor.includePredicate(filterByRACAlias);
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
