
package RichMon;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Implements a query sessions waiting on the library cache pin wait event
 */
public class SQLPlanDirectivesB extends RichButton { 
  boolean showSQL = false;

  /**
   * Constructor
   * 
   * @param buttonName
   * @param scrollP
   * @param statusBar
   * @param resultCache
   */
  public SQLPlanDirectivesB(String buttonName, JScrollPane scrollP, JLabel statusBar, ResultCache resultCache) {
    this.setText(buttonName);
    
    this.scrollP = scrollP;
    this.statusBar = statusBar;
    this.resultCache = resultCache;
  }

  /**
   * Performs the user selected action from the sharedServerB JComboBox
   * 
   * @param showSQL
   * @param tearOff
   */
  public void actionPerformed(boolean showSQL) {
    this.showSQL = showSQL;

    try {

      Cursor myCursor = new Cursor("sqlPlanDirectives.sql",true);
      Parameters myPars = new Parameters();
      Boolean filterByRAC = true;
      Boolean filterByUser = false;
      Boolean includeDecode = false;
      String includeRACPredicatePoint = "none";
      String filterByRACAlias = "none";
      String filterByUserAlias = "none";
      Boolean restrictRows = true;
      Boolean flip = true;
      Boolean eggTimer = true;
      executeDisplayFilterStatement(myCursor, myPars, flip, eggTimer, scrollP, statusBar, showSQL, resultCache, restrictRows, filterByRACAlias, filterByUserAlias, includeDecode, includeRACPredicatePoint, filterByRAC, filterByUser);
    }
    catch (Exception e) {
      ConsoleWindow.displayError(e,this);
    }
  }
}