/*
 * Properties.java        17.43 29/04/10
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
 * 16/11/10 Richard Wright Added the property pre_set_awr_instance
 * 17/11/10 Richard Wright Added the property max_resultset_size
 * 26/11/10 Richard Wright Removed referencecs to the i drive
 * 26/11/10 Richard Wright Added the ability to edit and save the content of the config file
 * 06/12/10 Richard Wright Added 'allow_dml'
 * 08/08/11 Richard Wright Removed sally sessions
 * 30/08/11 Richard Wright Added 'reFormatSQLInSQLPanels' renamed reformatSQL to reformatSQLInSessionPanes
 * 22/09/11 Richard Wright Added the 'auto_record' property
 * 02/10/11 Richard Wright Added the 'enable_awr_instance_selection property
 * 29/02/12 Richard Wright Added the 'cache_awr_data' property
 * 29/02/12 Richard Wright Added the 'cache_awr_retention' property
 * 21/05/12 Richard Wright Added the 'pete' property
 * 08/04/14 Richard Wright Removed all all functionality that breaks charts out into seperate windows
 * 07/12/14 Richard Wright Added 'sql_execution_history_chart' - Brian says it slows RichMon down too much
 * 28/11/18 Richard Wright Added the 'set_locale' property
 */


package RichMon;


import java.awt.Dimension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Vector;


public class Properties {

  private static boolean overlayEventChart = false;
  private static boolean reFormatSQLInSessionPanels = true;
  private static boolean reFormatSQLInSQLPanels = true;
  private static boolean flipResultSet = false;
  private static boolean enableKillSession = false;
  private static boolean enableSchemaViewer = false;
  private static boolean avoid_awr = false;
  private static boolean avoid_prompt_to_save_output_on_exit = false;
  private static boolean open_blog_on_first_use_of_a_release = false;
  private static boolean unlimitedSnapshots = false;
  private static boolean allowSnapshotsToSpanRestarts = false;
  private static boolean breakOutChartsTabsFrame = false;
  private static boolean enablePerformanceReviewOptions = false;
  private static int additionalWindowHeight = 600;
  private static int additionalWindowWidth = 1024;
  private static int cacheSize = 100;
  private static boolean dynamicChartDomainLabels = true;
  private static int dynamicChartDomainLabelFontSize = 7;
  private static String stackTrace="none";     // none | partial | full
  private static boolean defaultUserNameToOSUser = false;
  private static int axisLabelFontSize = 10;
  private static int axisLabelTickFontSize = 8;
  private static String language="en";
  private static String locale="GB";
  private static String date="null";
  private static String dateTimestamp="default";
  private static boolean allow_alter_session = true;
  private static boolean allow_alter_system = false;
  private static boolean allow_alter_database = false;
  private static boolean extractDbaJobsInPerfReview = true;
  private static boolean extractCPUTimeBreakdownInPerfReview = true;
private static boolean enableAWRInstanceSelection = false;
  private static String defaultLoadDir = "$RICHMON_BASE";
  private static String defaultSaveDir = "$RICHMON_BASE";
  private static int lobSampleSize = 1000;
  private static int cacheThreshold = 5000;
  private static String standardButtonDefaultSize = "120,25";
  private static String AWRLastnButtonValues = "1,24,72,120";
  private static String AWRMovenButtonValues = ">>24,>>12,<<24,<<12";
  static String[] defaultEvents = {"db file sequential read","db file scattered read",
                                   "latch free","read by other session","enq: TM - contention",
                                   "log file sync","log file parallel write",
                                   "db file parallel write","db file sequential read"};
  private static int maxResultSetSize = 999999999;
  private static boolean allow_dml = false;
  private static boolean autosave_sql_detail = true;
  private static boolean cache_awr_data = true;
  private static int cache_awr_retention = 7;
  private static boolean pete = false;
  private static boolean automaticStopIteratingOnLostConnection = true;
  private static boolean cacheResultsWithNoRows = true;
  private static boolean sqlExecutionHistoryChart = true;
  private static boolean setLocale = true;


  static boolean debug = false;

  public void Properties() {
    
  }
  
  /**
   * Read the RichMon.properties file and process the options
   *
   * @param configFile
   */
  public static void readPropertiesFile(File configFile) {
    try {
      BufferedReader propertiesFileReader = new BufferedReader(new FileReader(configFile));

      String inputLine = propertiesFileReader.readLine();

      while (inputLine instanceof String) {
        if (inputLine.length() > 0) {
          if (!inputLine.substring(0,1).equals("#")) {
            String property = inputLine.substring(0,inputLine.indexOf("=")).toLowerCase();
            String value;
            if (!property.equals("default_events")) {
              value = inputLine.substring(inputLine.indexOf("=")+1).toLowerCase();
            }
            else {
              value = inputLine.substring(inputLine.indexOf("=")+1);
            }

            if(debug) System.out.println("Processing properties file: " + property);

            if (property.equals("reformat_sql_in_session_panels") && value.equals("false")) setReFormatSQLInSessionPanels(false);
            if (property.equals("reformat_sql_in_sql_panels") && value.equals("false")) setReFormatSQLInSQLPanels(false);
            if (property.equals("flip_single_row_output") && value.equals("true")) flipResultSet = true;
            if (property.equals("enable_kill_session") && value.equals("true")) enableKillSession = true;
            if (property.equals("enable_schema_viewer") && value.equals("true")) enableSchemaViewer = true;
            if (property.equals("avoid_awr") && value.equals("true")) avoid_awr = true;
            if (property.equals("avoid_prompt_to_save_output_on_exit") && value.equals("true")) avoid_prompt_to_save_output_on_exit = true;
            if (property.equals("open_blog_on_first_use_of_a_release") && value.equals("true")) open_blog_on_first_use_of_a_release = true;
            if (property.equals("unlimited_snapshots") && value.equals("true")) unlimitedSnapshots = true;
            if (property.equals("allow_snapshots_to_span_restarts") && value.equals("true")) allowSnapshotsToSpanRestarts = true;
            if (property.equals("place_overview_charts_into_a_single_frame") && value.equals("true")) breakOutChartsTabsFrame = true;
            if (property.equals("enable_performance_review_options") && value.equals("true")) enablePerformanceReviewOptions = true;
            if (property.equals("additional_window_height")) setAdditonalWindowHeight(value);
            if (property.equals("additional_window_width")) setAdditionalWindowWidth(value);
            if (property.equals("cache_size")) setCacheSize(value);
            if (property.equals("dynamic_chart_domain_labels") & value.equals("false")) dynamicChartDomainLabels = false;
            if (property.equals("dynamic_chart_domain_label_font_size")) setDynamicChartDomainLabelFontSize(value);
            if (property.equals("stack_trace")) setStackTrace(value);
            if (property.equals("default_username_to_os_user") && value.equals("true")) ConnectWindow.setUserName();
            if (property.equals("axis_label_font_size")) setAxisLabelFontSize(value);
            if (property.equals("axis_label_tick_font_size")) setAxisLabelTickFontSize(value);
            if (property.equals("languate")) setLanguage(value);
            if (property.equals("locale")) setMyLocale(value);
            if (property.equals("date")) setDate(value);
            if (property.equals("date_timestamp")) setDateTimestamp(value);
            if (property.equals("allow_alter_session")) setAllowAlterSession(value);
            if (property.equals("allow_alter_system") && value.equals("true")) allow_alter_system=true;
            if (property.equals("allow_alter_database") && value.equals("true")) allow_alter_database=true;
            if (property.equals("extract_dba_jobs_in_perf_review")) setExtractDbaJobsInPerfReview(value);
            if (property.equals("extract_cputimebreakdown_in_perf_review")) setExtractCPUTimeBreakdownInPerfReview(value);
            if (property.equals("default_load_dir")) defaultLoadDir = value;
            if (property.equals("default_save_dir")) defaultSaveDir = value;
            if (property.equals("lob_sample_size")) setLobSampleSize(value);
            if (property.equals("lob_sample_size")) setLobSampleSize(value);
            if (property.equals("default_events")) setDefaultEvents(value);
            if (property.equals("cache_threshold")) setCacheThreshold(Integer.valueOf(value).intValue());
            if (property.equals("standard_button_default_size")) setStandardButtonDefaultSize(value);
            if (property.equals("awr_last_n_button_values")) setAWRLastnButtonValues(value);
            if (property.equals("awr_move_n_button_values")) setAWRMovenButtonValues(value);
            if (property.equals("max_resultset_size")) setMaxResultSetSize(value);
            if (property.equals("allow_dml") && value.equals("true")) allow_dml=true;
            if (property.equals("autosave_sql_detail") && value.equals("true")) setAutosave_sql_detail(true);
            if (property.equals("enable_awr_instance_selection") && value.equals("true")) enableAWRInstanceSelection=true;
            if (property.equals("cache_awr_data")) enableAWRCache(value);
            if (property.equals("cache_awr_retention")) setAWRCacheRetention(value);
            if (property.equals("automatically_Stop_Iterating_On_Lost_Connection") && !value.equals("true")) automaticStopIteratingOnLostConnection=false;
            if (property.equals("cache_results_with_no_rows") && !value.equals("true")) cacheResultsWithNoRows = false;
            if (property.equals("sql_execution_history_chart") && !value.equals("true")) sqlExecutionHistoryChart = false;
            if (property.equals("set_locale") && !value.equals("true")) setLocale = false;
          }
        }
        inputLine = propertiesFileReader.readLine();
      }
    }
    catch (Exception e) {
       ConnectWindow.displayError(e,"Error reading property file");
    }
  }

  /**
   * Writes the $RichMon_Base/RichMon/Config/RichMon.properties file with default setttings
   *
   * Users can modify the behaviour and appearance of RichMon by changing
   * the content of this file.
   *
   * @param configFile
   * @throws IOException
   */
  public static void createPropertiesFile(File configFile) throws IOException
  {
    boolean ok = configFile.createNewFile();

    // Write a default config set to the new config file
    BufferedWriter configFileWriter = new BufferedWriter(new FileWriter(configFile));

    configFileWriter.write("# Welcome to the RichMon Configuration File\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# To see a full description of any new parameters please see the richmon.blogspot.com entry for Nov 3rd 2005\n");
    configFileWriter.write("# To Re-generate this file (with default settings), just delete it and re-start RichMon.\n");
    configFileWriter.write("# It is advisable to do this after an upgrade so that you see any new parameters that have been added.\n");
    configFileWriter.write("# This file resides in $RICHMON_BASE/config (windows) | $RICHMON_BASE\\config (linux).\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Should RichMon exhibit strange behaviour, try setting cache_threshold below to 0 and max_resultset_size to 3000.\n");
    configFileWriter.write("# This dramatically reduces the chances of RichMon running short on memory.\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("reformat_sql_in_session_panels=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true any sql taken from the dictionary and shown on the session panel will be reformatted to improve readability\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("flip_single_row_output=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");    
    configFileWriter.write("# When true any query result that only has a single row will be displayed down the screen rather than accross for readability\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("enable_kill_session=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true the kill session menu item is enabled\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("enable_schema_viewer=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When false the schema viewer is not shown.  This can be useful when using RichMon over a slow dial up connection\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("avoid_awr=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set this to true if you prefer to use statspack in a 10g database rather than AWR\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("avoid_prompt_to_save_output_on_exit=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set this to false if you want RichMon to prompt you to save output before exiting\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("open_blog_on_first_use_of_a_release=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set this to false if you do not want to open your browser with the RichMon blog when you first use a new release of RichMon\n");
    configFileWriter.write("# Setting this to false is not advisable as you won't learn about the new features that are introduced\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("unlimited_snapshots=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When false you are limited to a range of 120 snapshots on the statspack/awr panel to prevent charts becoming too crowded\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("allow_snapshots_to_span_restarts=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Prevents you creating charts etc on the statspack/awr panel for periods of time that span an instance restart as this can distort the results\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("place_overview_charts_into_a_single_frame=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This can also be changed thru the menu\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Puts all the charts into seperate tabs in the same window, rather than having a seperate window for each chart\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("additional_window_height=600\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# The windows used for charts/tear off's etc will have this height\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("additional_window_widths=1024\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# The windows used for charts/tear off's etc will have this width\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("cache_size=100\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# The number of query results to be cached so that they can be reviewed again using the forward and back buttons\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("dynamic_chart_domain_label_font_size=7\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Default font size for the labels on dynamic charts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("dynamic_chart_domain_labels=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true labels are placed in the dynamic charts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("stack_trace=none\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Determines whether a stack trace is shown along with any error.  Valid values are none | partial | full\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("default_username_to_os_user=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If true your OS username will be the default username on the connect window rather than SYSTEM\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("axis_label_font_size=10\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set the size of the font used to label Y axis on all charts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("axis_label_tick_font_size=8\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set the size of the font used for tick labels on the Y axis on all charts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("language=en\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This determines the langauge used for displaying date values returned from a database\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("locale=GB\n");
    configFileWriter.write("date=null\n");
    configFileWriter.write("date_Timestamp=default\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# In combination these determine the format of a date datatype for display purposes. By example:\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    if locale=GB, date=null then 'select sysdate from dual' would produce the following output as dateTimestamp is set: \n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    dateTimestamp                sysdate\n");
    configFileWriter.write("#    -------------                -------\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    default                      11-May-2007 10:53:04 \n");
    configFileWriter.write("#    long                         11 May 2007 10:53:04 BST\n");
    configFileWriter.write("#    medium                       11-May-2007 10:53:04\n");
    configFileWriter.write("#    short                        11/05/07 10:53\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    if locale=GB, dateTimestamp=null then 'select sysdate from dual' would produce the following output as date is set: \n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    date                         sysdate\n");
    configFileWriter.write("#    ----                         -------\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#    default                      18-May-2007 \n");
    configFileWriter.write("#    long                         18 May 2007\n");
    configFileWriter.write("#    short                        18/05/07\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If both date and dateTimestamp are set then dateTimestamp will take precedence\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("allow_alter_session=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, 'alter session' commands can be issued from the scratch panel\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("allow_alter_system=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, 'alter system' commands can be issued from the scratch panel\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("enable_performance_review_options=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, the 'Perf Review' button appears on the statspack/awr panel\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("extract_dba_jobs_in_perf_review=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, a list of dba jobs is extracted for the performance review output.\n");
    configFileWriter.write("# On databases that have many tens of thousands of jobs this never finishes so should be set to false.");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("default_load_dir=$RICHMON_BASE\\scripts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When the load button on the scratch pane is used, it will default to this directory\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("default_save_dir=$RICHMON_BASE\\scripts\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When the save button on the scratch pane is used, it will default to this directory\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("lob_sample_size=1000\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# The number of rows from each table that has a lob column that should be evaluated to \n");
    configFileWriter.write("# determine the average lob size to be output in the performance review output\n");
    configFileWriter.write("# If set to 0 then no lob details will be output\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("default_events=db file sequential read;db file scattered read;latch free;read by other session;enq: TM - contention;log file sync;log file parallel write;db file parallel write;\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Set the default events to ensure that these events always have the same colour \n");
    configFileWriter.write("# in charts. If the database is earlier than 10g then 'read by other session' will be swapped for 'buffer busy waits'");
    configFileWriter.write("# and 'enq: TM - contention' will be swapped for 'enqueue'.  Entries are semi colon deliminated.\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("cache_threshold=50\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# On panels which show a 'Back' and 'Forward' button, the result of a query is cached so that you can go back and look at previous results. \n");
    configFileWriter.write("# This is really usefull, but if you have cached several very large resultsets it can cause strange behaviour due to memory shortages.\n");
    configFileWriter.write("# This strange behaviour usually manifests itself as visual issues with components not displaying properly.\n");
    configFileWriter.write("# To prevent this resultsets with more rows than this parameter are cached to disk rather than memory.\n");
    configFileWriter.write("# Since Version 17.39 the default value has changed from 5000 to 50.\n");    
    configFileWriter.write("#\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("standardButtonDefaultSize=120,25\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# This defines the width and height of many buttons in RichMon.  If you find buttons are missing you could reduce their size to ensure\n");
    configFileWriter.write("# they appear.  This can be a problem when running RichMon on very small screens, such as lightweight notebooks.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("awr_last_n_button_values=1,24,72,120\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Defines the buttons which set the snapshot start period n snapshots back from the selected end snapshot.  The first value defines the \n");
    configFileWriter.write("# bottom button and the last value defines the top button\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("awr_move_n_button_values=>>24,>>12,<<24,<<12\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# Defines the buttons which move the selected snapshot period n snapshots forwards or backwards.  The first value defines the \n");
    configFileWriter.write("# bottom button and the last value defines the top button\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#max_resultset_size=\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# RichMon will only display the first n rows of a query result.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("autosave_sql_detail=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, whenever a SQL_ID is clicked on, or a SQL_ID entered on the performance panel then the output that is displayed to \n");
    configFileWriter.write("# screen is also automatically saved to the $RICHMOB_BASE/output directory.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("enable_awr_instance_selection=false\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, the 'get snapshots' button on the AWR panels allows selection of the instance.\n");
    configFileWriter.write("# This is only useful when AWR data has been imported from another instance.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("cache_awr_data=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# When true, the results of some queries from the AWR panel are cached in $RICHMON_BASE/cache to speed up future queries.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("cache_awr_retention=7\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# The number of days that AWR query results will be cached for before becoming eligible for automatic removal\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("extract_CPUTimeBreakdown_in_perf_review=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If extracting this chart in the performance review output takes a long time set this parameter to false\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("automatically_Stop_Iterating_On_Lost_Connection=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If a lost connection is detected, stop iterating on all panels that iterate\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("cache_results_with_no_rows=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If set to false then queries returning no rows with no be cache, and therefore no show up in saved output\n");
    configFileWriter.write("# or visible when using the BACK and FORWARD buttons\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("sql_execution_history_chart=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If true then a chart showing SQL Execution History is present on the SQL Detail Panel for a SQL_ID\n");
    configFileWriter.write("# *************************************************************************************************************************\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("set_locale=true\n");
    configFileWriter.write("#\n");
    configFileWriter.write("#\n");
    configFileWriter.write("# If true then RichMon will attempt to set the database connections locale.  Set to false is you get Unrecognised Locale \n");
    configFileWriter.write("# errors when attempting to connect.\n");
    configFileWriter.write("# *************************************************************************************************************************\n");

    configFileWriter.close();
  }
  
  private static void setLanguage(String value) {
    language = value;
  }

  private static void setMyLocale(String value) {
    locale = value;
  }

  private static void setDate(String value) {
    date = value;
  }

  private static void setDateTimestamp(String value) {
    dateTimestamp = value;
  }

  public static String getLanguage() {
    return language;
  }

  public static String getMyLocale() {
    return locale;
  }

  public static String getDate() {
    return date;
  }

  public static String getDateTimestamp() {
    return dateTimestamp;
  }

  public static boolean isOverlayEventChart() {
    return overlayEventChart;
  }

  public static boolean isFlipResultSet() {
    return flipResultSet;
  }

  public static boolean isEnableKillSession() {
    return enableKillSession;
  }

  public static boolean isEnableSchemaViewer() {
    return enableSchemaViewer;
  }

  public static boolean isAvoid_awr() {
    return avoid_awr;
  }

  public static boolean isAvoid_prompt_to_save_output_on_exit() {
    return avoid_prompt_to_save_output_on_exit;
  }

  public static boolean isOpen_blog_on_first_use_of_a_release() {
    return open_blog_on_first_use_of_a_release;
  }

  public static boolean isUnlimitedSnapshots() {
    return unlimitedSnapshots;
  }

  public static boolean isAllowSnapshotsToSpanRestarts() {
    return allowSnapshotsToSpanRestarts;
  }

  public static boolean isBreakOutChartsTabsFrame() {
    return breakOutChartsTabsFrame;
  }

  public static void setPlaceOverviewChartsIntoASingleFrame(boolean set) {
    breakOutChartsTabsFrame = set;
  }

  private static void setAdditonalWindowHeight(String value) {
    additionalWindowHeight = Integer.valueOf(value).intValue();
  }

  private static void setAdditionalWindowWidth(String value) {
    additionalWindowWidth = Integer.valueOf(value).intValue();
  }

  public static int getAdditionalWindowHeight() {
    return additionalWindowHeight;
  }

  public static int getAdditionalWindowWidth() {
    return additionalWindowWidth;
  }

  public static void setCacheSize(String size) {
    cacheSize = Integer.valueOf(size).intValue();

    if (cacheSize < 10) cacheSize = 10;   // Minimum Cache Size is 10
  }

  public static int getCacheSize() {
    return cacheSize;
  }

  public static void setDynamicChartDomainLabelFontSize(String value) {
    dynamicChartDomainLabelFontSize = Integer.valueOf(value).intValue();
  }

  public static boolean getDynamicChartDomainLabels() {
    return dynamicChartDomainLabels;
  }

  public static int getDynamicChartDomainLabelFontSize() {
    return dynamicChartDomainLabelFontSize;
  }

  private static void setStackTrace(String stackTrace) {
    Properties.stackTrace = stackTrace;
  }

  public static String getStackTrace() {
    return stackTrace;
  }

  public static boolean isDefaultUserNameToOSUser() {
    return defaultUserNameToOSUser;
  }

  private static void setAxisLabelFontSize(String size) {
    axisLabelFontSize = Integer.valueOf(size).intValue();
  }

  private static void setAxisLabelTickFontSize(String size) {
    axisLabelTickFontSize = Integer.valueOf(size).intValue();
  }

  public static int getAxisLabelFontSize() {
    return axisLabelFontSize;
  }

  public static int getAxisLabelTickFontSize() {
    return axisLabelTickFontSize;
  }
  
  public static boolean getPete() {
    return pete;
  }

  private static void setAllowAlterSession(String param) {
    if (param.equals("true")) {
      allow_alter_session = true;
    }
    else {
      allow_alter_session = false;
    }
  }

  public static boolean isAllow_alter_session() {
    return allow_alter_session;
  }

  public static boolean isAllow_alter_system() {
    return allow_alter_system;
  }

  public static boolean isAllow_alter_database() {
    return allow_alter_database;
  }

  private static void setExtractDbaJobsInPerfReview(String value) {
    if (value.equals("true")) {
      extractDbaJobsInPerfReview = true;
    }
    else {
      extractDbaJobsInPerfReview = false;
    }
  }

  public static boolean isExtractDbaJobsInPerfReview() {
    return extractDbaJobsInPerfReview;
  }

  private static void setExtractCPUTimeBreakdownInPerfReview(String value) {
    if (value.equals("true")) {
      extractCPUTimeBreakdownInPerfReview = true;
    }
    else {
      extractCPUTimeBreakdownInPerfReview = false;
    }
  }

  public static boolean isExtractCPUTimeBreakdownInPerfReview() {
    return extractCPUTimeBreakdownInPerfReview;
  }


  public static void setLobSampleSize(String val) {
    int value = Integer.valueOf(val).intValue();

    lobSampleSize=value;
  }

  public static int getLobSampleSize() {
    return lobSampleSize;
  }

  public static void setDefaultEvents(String parameterValue) {
    StringBuffer tmpSB = new StringBuffer(parameterValue);
    Vector tmpV = new Vector();

    int i=0;

    while (tmpSB.indexOf(";",i) > 0) {
      tmpV.addElement(tmpSB.substring(i,tmpSB.indexOf(";",i)));
      i = tmpSB.indexOf(";",i) +1;
    }

    defaultEvents = new String[tmpV.size()];

    for (int j=0; j < tmpV.size(); j++) {
      defaultEvents[j] = String.valueOf(tmpV.elementAt(j));
    }

    if (debug) {
      System.out.println("parameterValue: " + parameterValue);
      for (int j=0; j < defaultEvents.length; j++) System.out.println("defaultEvent " + j + ": " + defaultEvents[j]);
    }
  }

  public static String[] getDefaultEvents() {
    return defaultEvents;
  }

  private static void setStandardButtonDefaultSize(String standardButtonDefaultSize) {
    Properties.standardButtonDefaultSize = standardButtonDefaultSize;
  }

  public static Dimension getStandardButtonDefaultSize() {
    int width = Integer.valueOf(standardButtonDefaultSize.substring(0, standardButtonDefaultSize.indexOf(","))).intValue();
    int height = Integer.valueOf(standardButtonDefaultSize.substring(standardButtonDefaultSize.indexOf(",") +1)).intValue();
    return new Dimension(width,height);
  }

  private static void setAWRLastnButtonValues(String value) {
    AWRLastnButtonValues = value;
  }

  public static String getAWRLastnButtonValues() {
    return AWRLastnButtonValues;
  }

  private static void setAWRMovenButtonValues(String value) {
    AWRMovenButtonValues = value;
  }

  public static String getAWRMovenButtonValues() {
    return AWRMovenButtonValues;
  }

  private static void setCacheThreshold(int value) {
    cacheThreshold = value;
  }

  public static void setReFormatSQLInSessionPanels(boolean reFormatSQL) {
    Properties.reFormatSQLInSessionPanels = reFormatSQL;
  }

  public static void setReFormatSQLInSQLPanels(boolean reFormatSQL) {
    Properties.reFormatSQLInSQLPanels = reFormatSQL;
  }
  
  public static boolean isReFormatSQLInSessionPanels() {
    return reFormatSQLInSessionPanels;
  }

  public static boolean isReFormatSQLInSQLPanels() {
    return reFormatSQLInSQLPanels;
  }
  public static boolean isEnablePerformanceReviewOptions() {
    return enablePerformanceReviewOptions;
  }

  public static int getCacheThreshold() {
    return cacheThreshold;
  }

  public static void setDefaultLoadDir(String defaultLoadDir) {
    Properties.defaultLoadDir = defaultLoadDir;
  }

  public static void setDefaultSaveDir(String defaultSaveDir) {
    Properties.defaultSaveDir = defaultSaveDir;
  }

  public static String getDefaultLoadDir() {
    if (ConnectWindow.isLinux()) {
      if (defaultLoadDir.equals("$RICHMON_BASE")) setDefaultLoadDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "//Scripts");
    }
    else {
      if (defaultLoadDir.equals("$RICHMON_BASE")) setDefaultLoadDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "\\Scripts");
    }

    return defaultLoadDir;
  }

  public static String getDefaultSaveDir() {
    if (ConnectWindow.isLinux()) {
      if (defaultSaveDir.equals("$RICHMON_BASE")) setDefaultSaveDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "//Scripts");
    }
    else {
      if (defaultSaveDir.equals("$RICHMON_BASE")) setDefaultSaveDir(javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory() + "\\Scripts");
    }

    return defaultSaveDir;
  }

  public static void setDefaultUserNameToOSUser(boolean defaultUserNameToOSUser) {
    Properties.defaultUserNameToOSUser = defaultUserNameToOSUser;
  }


  private static void setMaxResultSetSize(String value) {
    maxResultSetSize = Integer.valueOf(value).intValue();
  }

  public static int getMaxResultSetSize() {
    return maxResultSetSize;
  }
  
  public static void edit() {
    PropertiesEditor propertiesEditor = new PropertiesEditor();
  }
  
  public static boolean isAllowDML() {
    return allow_dml;
  }

  public static void setAutosave_sql_detail(boolean autosave_sql_detail) {
    Properties.autosave_sql_detail = autosave_sql_detail;
  }

  public static boolean isAutosave_sql_detail() {
    return autosave_sql_detail;
  }
  
  public static boolean getEnableAWRInstanceSelection() {
    return enableAWRInstanceSelection;
  }
  
  public static void enableAWRCache(String enable) {
    cache_awr_data=Boolean.valueOf(enable).booleanValue();
  }
  
  public static boolean isAWRCacheEnabled() {
    return cache_awr_data;
  }
  
  public static void setAWRCacheRetention(String retention) {
    cache_awr_retention = Integer.valueOf(retention).intValue();
  }
  
  public static int getAWRCacheRetention() {
    return cache_awr_retention;
  }
  
  public static void setAutomaticStopIteratingOnLostConnection(boolean automaticStopIteratingOnLostConnection) {
    Properties.automaticStopIteratingOnLostConnection = automaticStopIteratingOnLostConnection;
  }
  
  public static boolean isAutomaticStopIteratingOnLostConnection() {
    return automaticStopIteratingOnLostConnection;
  }
  
  public static boolean isCacheResultsWithNoRows() {
    return cacheResultsWithNoRows;
  }  
  
  public static void setCacheResultsWithNoRows(boolean cache) {
    cacheResultsWithNoRows = cache;
  }
  
  public static boolean isSqlExecutionHistoryChart() {
    return sqlExecutionHistoryChart;
  }
  
  public static boolean isSetLocale() {
    return setLocale;
  }
}


