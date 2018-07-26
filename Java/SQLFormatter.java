/*
 * SQLFormatter.java        12.16 05/07/04
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
 * Change History since 23/05/05
 * =============================
 * 
 * 23/05/05 Richard Wright Switched comment style to match that 
 *                         recommended in Sun's own coding conventions.
 * 03/06/05 Richard Wright Stopped anything in single or double quotes being 
 *                         turned lowercase
 * 07/06/05 Richard Wright Added the inUpdate boolean to ensure the update keyword 
 *                         is made uppercade
 * 07/06/05 Richard Wright Added lots of try-catch blocks and LogWriter output in prepareSQL()
 * 14/09/06 Richard Wright Modified the comment style and error handling
 * 08/01/07 Richard Wright Removed the exception produced when indenting 2 or 3
 *                         spaces until I understand whats causing it
 * 15/03/07 Richard Wright Modified pushBracketStack to ignore any errors
 * 11/09/07 Richard Wright Removed extranoues carriage return characters
 * 17/09/07 Richard Wright Removed more extranoues carriage return characters
 * 24/09/07 Richard Wright Improvements to the sql formatting
 * 14/01/08 Richard Wright Increased the wordstore from 10000 to 20000
 * 25/04/08 Richard Wright Made 'distinct' and 'having' uppercase and put the 
 *                         later on a newline
 * 13/10/08 Richard Wright Improved formatting of comments that begin '/*' and '--'
 * 
 */


package RichMon;

/**
 * Take a sql statement and format into something more readable.
 */
public class SQLFormatter  {

  boolean debug = false;
  
  int wordStoreSize = 20000;
  int numWords = 0;
  int charsThisLine = 0;
  int numLines = 0;
  int numCase = 0;
  
  int[] bracketStack = new int[wordStoreSize];  
  int bracketStackCounter = 0;
  
  boolean brackets = false;   
  int bracketDepth = 0;
  
  boolean inSingleQuotes = false;
  boolean inDoubleQuotes = false;
  boolean inSelect = false;
  boolean inFrom = false;
  boolean inWhere = false;
  boolean inInto = false;
  boolean inValues = false;
  boolean inInsert = false;
  boolean inSet = false;
  boolean inUpdate = false;
  boolean inOrderBy = false;
  boolean inGroupBy = false;
  boolean inFor = false;
  boolean inCase = false;
  boolean inComment = false;
  boolean inSingleLineComment = false;
  boolean inBetween = false;
  boolean inWith = false;

  String originalSQL;
  StringBuffer formattedSQL;
  String[] finalSQL = new String[wordStoreSize];
  
  String[] words = new String[wordStoreSize];
 
  String[] functionList =  { "ABS", "ACOS", "ASIN", "ATAN", "ATAN2", "BITAND", "CEIL", "COS", "COSH", 
                          "EXP", "FLOOR", "LN", "LOG", "MOD", "POWER", "ROUND", "SIGN", "SIN", 
                          "SINH", "SQRT", "TAN", "TANH", "TRUNC", "WIDTH_BUCKET", "CHR", "CONCAT", 
                          "INITCAP", "LOWER", "LPAD", "LTRIM", "NLS_INITCAP", "NLS_LOWER", "NLSSORT", 
                          "NLS_UPPER", "REPLACE", "RPAD", "RTRIM", "SOUNDEX", "SUBSTR", "TRANSLATE", 
                          "TREAT", "TRIM", "UPPER", "ASCII", "INSTR", "LENGTH", "ADD_MONTHS", 
                          "CURRENT_DATE", "CURRENT_TIMESTAMP", "DBTIMEZONE", "EXTRACT", "FROM_TZ", 
                          "LAST_DAY", "LOCALTIMESTAMP", "MONTHS_BETWEEN", "NEW_TIME", "NEXT_DAY", 
                          "NUMTODSINTERVAL", "NUMTOYMINTERVAL", "ROUND", "SESSIONTIMEZONE", 
                          "SYS_EXTRACT_UTC", "SYSDATE", "SYSTIMESTAMP", "TO_DSINTERVAL", 
                          "TO_TIMESTAMP", "TO_TIMESTAMP_TZ", "TO_YMINTERVAL", "TRUNC", "TZ_OFFSET", 
                          "ASCIISTR", "BIN_TO_NUM", "CAST", "CHARTOROWID", "COMPOSE", "CONVERT", 
                          "DECOMPOSE", "HEXTORAW", "NUMTODSINTERVAL", "NUMTOYMINTERVAL", "RAWTOHEX", 
                          "RAWTONHEX", "ROWIDTOCHAR", "ROWIDTONCHAR", 
                          "TO_CHAR", "TO_CHAR (number)", "TO_CLOB", "TO_DATE", 
                          "TO_DSINTERVAL", "TO_LOB", "TO_MULTI_BYTE", "TO_NCHAR", 
                          "TO_NCHAR (number)", "TO_NCLOB", "TO_NUMBER", 
                          "TO_SINGLE_BYTE", "TO_YMINTERVAL", "UNISTR", "BFILENAME", "COALESCE", 
                          "DECODE", "DEPTH", "DUMP", "EMPTY_BLOB, EMPTY_CLOB", "EXISTSNODE", 
                          "EXTRACT (XML)", "EXTRACTVALUE", "GREATEST", "LEAST", 
                          "NLS_CHARSET_DECL_LEN", "NLS_CHARSET_ID", "NLS_CHARSET_NAME", "NULLIF", 
                          "NVL", "NVL2", "PATH", "SYS_CONNECT_BY_PATH", "SYS_CONTEXT", 
                          "SYS_DBURIGEN", "SYS_EXTRACT_UTC", "SYS_GUID", "SYS_TYPEID", "SYS_XMLAGG", 
                          "SYS_XMLGEN", "UID", "UPDATEXML", "USER", "USERENV", "VSIZE", "XMLAGG", 
                          "XMLCOLATTVAL", "XMLCONCAT", "XMLFOREST", "XMLSEQUENCE", "XMLTRANSFORM", 
                          "AVG", "CORR", "COUNT", "COVAR_POP", "COVAR_SAMP", "CUME_DIST", 
                          "DENSE_RANK", "FIRST", "GROUP_ID", "GROUPING", "GROUPING_ID", "LAST", 
                          "MAX", "MIN", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "RANK", 
                          "STDDEV", "STDDEV_POP", "STDDEV_SAMP", "SUM", "VAR_POP", "VAR_SAMP", 
                          "VARIANCE", "DEREF", "MAKE_REF", "REF", "REFTOHEX", "VALUE"};
  
  String[] hints = {"ALL_ROWS","FIRST_ROWS","FULL","CLUSTER","HASH","INDEX",
                       "NO_INDEX","INDEX_ASC","INDEX_COMBINE","INDEX_JOIN",
                       "INDEX_DESC","INDEX_FFS","NO_INDEX_FFS","INDEX_SS",
                       "INDEX_SS_ASC","INDEX_SS_DESC","NO_INDEX_SS","NO_QUERY_TRANSFORMATION",
                       "USE_CONCAT","NO_EXPAND","REWRITE","NO_REWRITE",
                       "MERGE","NO_MERGE","STAR_TRANSFORMATION","NO_STAR_TRANSFORMATION",
                       "FACT","NO_FACT","UNNEST","NO_UNNEST","LEADING",
                       "ORDERED","USE_NL","NO_USE_NL","USE_NL_WITH_INDEX","USE_MERGE",
                       "NO_USE_MERGE","USE_HASH","NO_USE_HASH","PARALLEL","PARALLEL",
                       "PQ_DISTRIBUTE","PARALLEL_INDEX","NO_PARALLEL_INDEX","APPEND",
                       "NOAPPEND","CACHE","NOCACHE","PUSH_PRED","NO_PUSH_PRED",
                       "PUSH_SUBQ","NO_PUSH_SUBQ","QB_NAME","CURSOR_SHARING_EXACT",
                       "DRIVING_SITE","DYNAMIC_SAMPLING","MODEL_MIN_ANALYSIS" };
  
  String[] functions = new String[functionList.length];

  /**
   * Constructor.
   */
  public SQLFormatter() {
    for (int i = 0; i < functionList.length; i++) {
      functions[i] = functionList[i].toLowerCase();
    }

  }
  
  private void initialize() {
    formattedSQL = new StringBuffer();
    
    charsThisLine = 0;
    numLines = 0;
    
    bracketStack = new int[10000];  
    bracketStackCounter = 0;
  
    brackets = false;   
    bracketDepth = 0;
  
    inSingleQuotes = false;
    inDoubleQuotes = false;
    inSelect = false;
    inFrom = false;
    inWhere = false;
    inInto = false;
    inValues = false;
    inInsert = false;
    inSet = false;
    inUpdate = false;
    inOrderBy = false;
    inGroupBy = false;
    inFor = false;
    inCase = false;
    inComment = false;
    inSingleLineComment = false;
    inBetween = false;
    inWith = false;
  }


  /**
   * Format a sql statement
   * 
   * @param sql
   * @return
   */
  public String[] formatSQL(String sql) {
    originalSQL = sql;
    initialize();
    numWords = 0;
    
    finalSQL = new String[wordStoreSize];
    words = new String[wordStoreSize];
 
    // remove any tailing ';' or '/' trailing spaces 
    sql = sql.trim();
    int semiColon = sql.lastIndexOf(";");
    int lastCloseBracket = sql.lastIndexOf(")");   
    if ((semiColon >= 0) && (semiColon > lastCloseBracket) && ((sql.length() - semiColon) < 5)) {
      sql = sql.substring(0,sql.lastIndexOf(";"));
    }      
    
    int slash = sql.lastIndexOf("/");
    if ((slash >= 0) && (slash > lastCloseBracket) && ((sql.length() - slash) < 5)) {
      sql = sql.substring(0,sql.lastIndexOf("/"));
    }

          
    /**
     * Do not re-format the query if it does not start with 'select',
     * 'insert','update' or 'delete'.
     * 
     * Re-format the sql into a string[]
     */
    String tmpSQL = sql.toLowerCase();
//    sql = sql.toLowerCase();  -- 03/06/05 taken out because predicates in lower case lose value otherwise
    if (   !tmpSQL.startsWith("select") 
        && !tmpSQL.startsWith("insert") 
        && !tmpSQL.startsWith("delete") 
        && !tmpSQL.startsWith("update")
        && !tmpSQL.startsWith("with")
//        && !tmpSQL.startsWith("declare")
//        && !tmpSQL.startsWith("begin")
                                      ) {
      finalSQL[0] = originalSQL;
      numLines =1;
        
      // resize the finalSQL array to the correct size 
      String[] tmp = new String[numLines];
      System.arraycopy(finalSQL,0,tmp,0,numLines);
      finalSQL = tmp;
      
      return finalSQL;
    }
    

    breakSQLIntoWords(sql);
    
    // format the sql or pl/sql statement
    if (tmpSQL.startsWith("select") || tmpSQL.startsWith("insert") || tmpSQL.startsWith("update") || tmpSQL.startsWith("delete") || tmpSQL.startsWith("with")) {
      reformSQLStatement();
    }
    else {
      // it's pl/sql
      reformPLSQLStatement();
    }
    
    // split sql statement into lines 
    splitIntoLines(formattedSQL);
    
    // resize the finalSQL array to the correct size 
    String[] tmp = new String[numLines];
    System.arraycopy(finalSQL,0,tmp,0,numLines);
    finalSQL = tmp;
    
    // print out the formatted sql 
    if (debug) {
      System.out.println(formattedSQL);
    }
    
    return finalSQL;
  }

  private void breakSQLIntoWords(String sql) {
    /*
     * break the statement down in to individual elements
     *  - elements are deliminated by spaces and brackets
     */
    
    /*
     * loop thru each character and store words away
     * do not split words inside of brackets or single quotes
     */
    StringBuffer myWord = new StringBuffer();
    char myLastChar = ' ';
    char myNextChar = ' ';
    char myChar = ' ';
    
    for (int i=0; i < sql.length(); i++) {    
      if (sql.charAt(i) == '\u0000') {
        myChar = ' ';
      }
      else {
        myChar = sql.charAt(i);        
      }
      
      if (i+1 < sql.length()) {
        myNextChar = sql.charAt(i+1);
      }
      else {
        myNextChar = ' ';
      }
      
      // ascii 13 = carriage return    ascii 9 = tab
      if (myChar != '\n' && (int)myChar != 13 && (int)myChar != 9) {
        if ((myChar == ' ' || myChar == ',') && (//brackets == false && 
                                                 inSingleQuotes == false && 
                                                 inDoubleQuotes == false)) {  // check to see if this is the end of a word 
    
          if (myWord.length() > 0) {  // end of a word 
            storeWord(myWord.toString());
            myWord = new StringBuffer();
          }
    
          if (myChar == ',') {
            myWord.append(myChar);
            storeWord(myWord.toString());            
            myWord = new StringBuffer();
          }
          
        }
        else {
          // another char 
          String myString = String.valueOf(myChar);
          String myLastString = String.valueOf(myLastChar);

          if (myChar == '"') doubleQuote();
          if (myString.equals("'")) {
            singleQuote();
          }
          
          if ((myChar == ',' || myChar == '(' || myChar == ')') && !inSingleQuotes && !inDoubleQuotes) {
            storeWord(myWord.toString());
            myWord = new StringBuffer();
          }
          
          if (myLastChar == '|' && myString.equals("'")) {
            storeWord(myWord.toString());            
            myWord = new StringBuffer();                        
          }
          
          if (!inSingleQuotes && !inDoubleQuotes) {
            if (myChar == '=' && (myLastChar != '<' && myLastChar != '>' && myLastChar != '!' && myNextChar != '<' && myNextChar != '>')) {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
    
            if (myChar == '=' && myLastChar != '<' && myLastChar != '>' && myLastChar != '!') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
          
            if (myChar == '<' && myLastChar != '=' && myNextChar != '>' && myNextChar !='<' && myLastChar != '<') {
             storeWord(myWord.toString());
              myWord = new StringBuffer();
            }    
            
            if (myChar == '>' && myLastChar != '=' && myLastChar != '<' && myNextChar != '>' && myLastChar != '>') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
            
            if ((myChar == '!' || myChar == '+' || myChar == '-' || myChar == '/' || myChar == '*') && !(myChar == '/' && myNextChar == '*') && !(myChar == '*' && myLastChar == '/')
                                                                                                    && !(myChar == '+' && myLastChar == '*') && !(myChar == '/' && myLastChar =='*')
                                                                                                    && !(myChar == '-' && myLastChar == '-')) {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
            
            if (myChar == '|' && myNextChar == '|') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
            
            if (myLastChar == '|' && myChar != '|') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
          }
    
          // add the current character to the current word
//          if (debug) System.out.println("adding " + myChar);
          if ((int)myChar != 13) {    // throw carriage returns away
            if (!inSingleQuotes  && !inDoubleQuotes) {
//              if (debug) System.out.println("turning " + myChar + " to lowercase.");
              myChar = Character.toLowerCase(myChar);
              if (debug) System.out.println("added: " + myChar);
              myWord.append(myChar);   // make lowercase
            }
            else {
              if (debug) System.out.println("adding as is: " + myChar);
              myWord.append(myChar);   // leave the case as is
            }
          }
          
          if (!inSingleQuotes && !inDoubleQuotes) {
            if (myChar == '=' && (myLastChar != '<' && myLastChar != '>' && myNextChar != '<' && myNextChar != '>')) {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
            }
          }
          
          if (myWord.length() > 0) {
            if (myWord.charAt(0) == ')') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
              decrementBrackets();
            }                  
          }
          
          if (myWord.length() > 0) {
            if (myWord.charAt(0) == '(') {
              storeWord(myWord.toString());
              myWord = new StringBuffer();
              decrementBrackets();
            }          
          }
          
          if (!inSingleQuotes && !inDoubleQuotes) {
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '!' && myWord.charAt(1) == '=') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
              
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '<' && myWord.charAt(1) == '>') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
            
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '<' && myWord.charAt(1) == '=') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
            
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '>' && myWord.charAt(1) == '=') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
            
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '=' && myWord.charAt(1) == '<') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
            
            if (myWord.length() == 2) {
              if(myWord.charAt(0) == '=' && myWord.charAt(1) == '>') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();
              }
            }
            
            if (myChar == '<' && myNextChar != '=' && myNextChar != '>' && myNextChar != '<' && myLastChar != '<') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();              
            }
            
            if (myChar == '>' && myNextChar != '=' && myNextChar != '>') {
                storeWord(myWord.toString());
                myWord = new StringBuffer();              
            }
          }
        }
      }
      else {
        // new line char 
        if (myWord.length() > 0) {  // end of a word 
          storeWord(myWord.toString().trim());
          myWord = new StringBuffer();
        }
        
        if (inSingleLineComment) {
          storeWord("<<crinsinglelinecomment>>");
          myWord = new StringBuffer();
          inSingleLineComment = false;
        }
          
        if (inComment) {
          storeWord("<CR in Comment>");
          myWord = new StringBuffer();
        }
      }
      myLastChar = myChar;
    }
    
    // add the last word to the array or word 
    storeWord(myWord.toString());
    
    // print out the words array for examination 
    if (debug) {
      System.out.println("Dumping " + numWords + " Stored Words: ");
      for (int i=0; i < numWords; i++) {
        System.out.println(i + " [" + words[i] + "]");
      }
    }
  }
  
  /**
   * Increment the brackets counter
   */
  private void incrementBrackets() {
    bracketDepth++;

//    if (!inWhere) {
      if (bracketDepth > 0)  {
        brackets = true;
      }
      else {
        brackets = false;
      }
//    }
    
    pushBracketStack(charsThisLine);
  }
  
  /**
   * Decrement the brackets counter
   */
  private void decrementBrackets() {
    bracketDepth--;
    
//    if (!inWhere) {
      if (bracketDepth > 0) {
       brackets = true;    
      }
      else {
        brackets = false;
      }
//    }
    
    bracketStackCounter--;
  }
  
  /**
   * Reset the Brackets counter
   */
  private void resetBrackets() {
    bracketDepth=0;
    brackets = false;
  }
  
  /**
   * Reset the quotes boolean to false
   */
  private void resetQuotes() {
    inSingleQuotes = false;
    inDoubleQuotes = false;
  }
  
  /**
   * Toggle the inSingleQuotes boolean
   */
  private void singleQuote() {  
    if (inSingleQuotes) {
      inSingleQuotes = false;
    }
    else {
      inSingleQuotes = true;
    }
  }
  
  /**
   * Toggle the inDoubleQuotes boolean
   */
  private void doubleQuote() {  
    if (inDoubleQuotes) {
      inDoubleQuotes = false;
    }
    else {
      inDoubleQuotes = true;
    }
  }
  
  /**
   * Convert key words to uppercase and keep a counter of them
   * @param myWord 
   */
  private void storeWord(String myWord) {
    if (myWord.length() > 0) {
      if (((myWord.toLowerCase().equals("select") || 
        myWord.toLowerCase().equals("insert") || 
        myWord.toLowerCase().equals("update") || 
        myWord.toLowerCase().equals("delete") || 
        myWord.toLowerCase().equals("from") || 
        myWord.toLowerCase().equals("where") || 
        myWord.toLowerCase().equals("order") || 
        myWord.toLowerCase().equals("by") || 
        myWord.toLowerCase().equals("into") || 
        myWord.toLowerCase().equals("group") || 
        myWord.toLowerCase().equals("values") || 
        myWord.toLowerCase().equals("and") || 
        myWord.toLowerCase().equals("or") || 
        myWord.toLowerCase().equals("set") || 
        myWord.toLowerCase().equals("union") || 
        myWord.toLowerCase().equals("all") || 
        myWord.toLowerCase().equals("for") || 
        myWord.toLowerCase().equals("in") || 
        myWord.toLowerCase().equals("case") || 
        myWord.toLowerCase().equals("when") || 
        myWord.toLowerCase().equals("then") || 
        myWord.toLowerCase().equals("end") || 
        myWord.toLowerCase().equals("else") || 
        myWord.toLowerCase().equals("between") || 
        myWord.toLowerCase().equals("distinct") || 
        myWord.toLowerCase().equals("having") || 
        myWord.toLowerCase().equals("as") || 
        myWord.toLowerCase().equals("with") || 
        isFunction(myWord.toLowerCase())) && !inComment && !inSingleLineComment) || ( inComment && isHint(myWord.toUpperCase()))) {
          myWord = myWord.toUpperCase();
        }
      setBooleans(myWord,"");
      
      if (!inSingleQuotes && !inDoubleQuotes && !inSet &&
          !inSelect && !inFrom && !inWhere && !inValues && !inInsert && !inInto && !inUpdate && !inFor && !inWith) myWord = myWord.toLowerCase();
      words[numWords++] = myWord;    
    }
  }
  
  /**
   * Set boolean according to the key word
   * 
   * @param myWord 
   */
  private void setBooleans(String myWord, String lastWord) {
    if (myWord.equals("SELECT")) {
      inSelect = true;
      inValues = false;
      inInto = false;
      if (lastWord.equals(")") && inWith) inWith=false;
    }
    
    if (myWord.equals("FROM")) {
      inFrom = true;
      inSelect = false;
      inValues = false;
      inInto = false;
    }
    
    if (myWord.equals("WHERE")) {
      inWhere = true;
      inFrom = false;
      inValues = false;
      inInto = false;
    }
    
    if (myWord.equals("VALUES")) {
      inValues = true;
      inInto = false;
    }
    
    if (myWord.equals("INTO")) {
      inInto = true;
      inValues = false;
    }
    
    if (myWord.equals("INSERT")) inInsert = true;
    
    if (myWord.equals("SET")) inSet = true;
    
    if (myWord.equals("UPDATE")) inUpdate = true;
    
    if (myWord.equals("FOR")) inFor = true;
    
    if (myWord.equals("BY") && lastWord.equals("ORDER")) {
      inOrderBy = true;
      inFrom = false;
      inValues = false;
      inInto = false;
    }
    
    if (myWord.equals("BY") && lastWord.equals("GROUP")) {
      inGroupBy = true;
      inFrom = false;
      inValues = false;
      inInto = false;
    }
    
    if (myWord.equals("CASE")) {
      numCase++;
      inCase = true;
    }
    
    if (lastWord.equals("END") && inCase) {
      numCase--;
      if (numCase > 0) inCase = false;
    }
    
    if (myWord.equals("/*+") || myWord.equals("/*")) inComment = true;
    if (myWord.equals("*/")) inComment = false;
    
    if (myWord.equals("--")) inSingleLineComment = true;
    if (myWord.equals("<<crinsinglelinecomment>>")) inSingleLineComment = false;

    
    if (myWord.equals("BETWEEN")) inBetween = true;
    if (lastWord.equals("AND") && inBetween)  inBetween = false;
    
    if (myWord.equals("WITH")) inWith=true;
  }
  
  private boolean isFunction(String myWord) {
    for (int i = 0; i < functions.length; i++) {
      if (myWord.equals(functions[i])) return true;
    }

    return false;
  }  
  
  private boolean isHint(String myWord) {
    for (int i = 0; i < hints.length; i++) {
      if (myWord.equals(hints[i])) return true;
    }

    return false;
  }
  
  /**
   * Reform SQL 
   */
  private void reformSQLStatement() {
    resetBrackets();
    resetQuotes();
    resetBracketStack();
    initialize();

    if (debug) System.out.println("Reforming SQL");
    
    for (int i = 0; i < numWords; i++) {
      String myWord = words[i];
      
      String lastWord = "";
      if (i > 0) lastWord = words[i-1];
      
      String nextWord = "";
      if (i < numWords) {
        nextWord = words[i+1];
      }
      
      if (i == numWords -1) {
        nextWord = words[i];
      }
      
      if (myWord.equals("'")) singleQuote();

      // Does myWord start or end in double quotes 
      char[] myWordChars = myWord.toCharArray();
      char myChar = myWordChars[0];
      if (myChar == '"' || (myChar == ':' && myWordChars[1] == '"')) doubleQuote(); 
      myChar = myWordChars[myWordChars.length -1];
      if (myChar == '"') doubleQuote();

      
      if (debug) System.out.println("Processing myWord: " + myWord);
      
      // which section of a statement are we in
      setBooleans(myWord,lastWord);
      

      if (myWord.equals("(")) {
        incrementBrackets();
        if (debug) System.out.println("incrementBrackets()");
      }
      if (myWord.equals(")")) {
        decrementBrackets();
        if (debug) System.out.println("decrementBrackets()");
      }
      if (myWord.equals("\"")) {
        doubleQuote();
        if (debug) System.out.println("doubleQuote()");
      }
      if (myWord.equals("'")) {
        singleQuote();
        if (debug) System.out.println("singleQuote()");
      }


      /**
       * Put the 'SELECT' keyword on a newline if the previous word was 'ALL' or 'UNION' or 'MINUS'.
       *
       * This does not apply if the 'SELECT' keyword is the first word in the statement,
       * in which is it will already be the first word on the first line.
       */
      try {
        if ( i > 0 && myWord.equals("SELECT") && (lastWord.equals("ALL") || lastWord.equals("UNION") || lastWord.equals("MINUS"))) {
          if (debug) System.out.println(" i > 0 && myWord.equals(\"SELECT\") && (words[i-1].equals(\"ALL\") || words[i-1].equals(\"UNION\") || words[i-1].equals(\"MINUS\"))");
          formattedSQL.append("\n");
          charsThisLine=0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +0;
          addSpaces(numSpaces);                
         }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting SELECT onto a new line");
      }
      
      /**
       * If the current word is 'SELECT' and it is a subquery, then find the 
       * closing bracket that marks the end of the subquery.  Pass this subquery
       * into another instance of SQLFormatter and get that formatted, then plug 
       * it into the current statement (indenting as appropriate).
       */
      try {
//        if (myWord.equals("SELECT") && !inComment) {
        if ((bracketStackCounter > 0 && myWord.equals("SELECT") && !inComment) || (inSet && inSelect) || inUpdate && inSelect ||
             (myWord.equals("SELECT") && (lastWord.equals("UNION") || lastWord.equals("MINUS") || lastWord.equals("ALL")) && !inComment && !inSingleLineComment)) {
//        if ((bracketStackCounter > 0 && myWord.equals("SELECT") && !inComment) || (inSet && inSelect) || (inUpdate && inSelect)) {
//        if ((inFrom && inSelect) || (inSet && inSelect) || (inUpdate && inSelect)) {
          // this is a subquery, first find the start and end of the subquery
          int first = i;
          int numOpenBrackets = 0;
          while (!(numOpenBrackets == 0 && myWord.equals(")")) && i < numWords) {
            if (myWord.equals("(")) numOpenBrackets++;
            if (myWord.equals(")")) numOpenBrackets--;
            myWord = words[i++];
            if (debug) System.out.println("inside SQ: numOpenBrackets: " + numOpenBrackets + " : i : " + i + " : words[i] : " + words[i] + " : words[i-1] : " + words[i-1] + " :");
          }
          
          int last = i -1;
          
          StringBuffer subSelectSQL = new StringBuffer();
          for (int x=first; x < last; x++) {
            subSelectSQL.append(words[x]);
            subSelectSQL.append(" ");
          }
          SQLFormatter subSelect = new SQLFormatter();
          String [] formattedSubSelect = subSelect.formatSQL(subSelectSQL.toString());
          
          // insert the sub query into the current 
          if (debug) System.out.println("placing SQ : bracketStackCounter : " + bracketStackCounter);
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          
          for (int x=0; x < formattedSubSelect.length; x++) {
            if (x >0) addSpaces(numSpaces);
            formattedSQL.append(formattedSubSelect[x]);
            if (x < formattedSubSelect.length-1) formattedSQL.append("\n");
          }
          
          i = last;
          inSelect = false;
          decrementBrackets();
        }
        
        
      }
      catch (Exception e){
        ConsoleWindow.displayError(e,this,"Formatting sub-query");
      }

      try {
        if (myWord.equals("SELECT") && lastWord.equals(")") && !brackets) {
          inWith = false;
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      } catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting the SELECT onto a new line after the end of the WITH clause");
      }
      
      try {
        if (myWord.equals(",") && !brackets && !inComment && !inSingleLineComment && inWith) {
          if (debug) System.out.println("inSelect  && !inComment && !inSingleLineComment && inWith");
          formattedSQL.append(("\n"));
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma onto a new line to seperate elements of a with clause");
      }
      
      /**
       * Put the keyword 'SELECT' on a newline in the case of an
       * insert select statement.
       */
      try {
        if (inInsert && !inFrom && myWord.equals("SELECT")) {
          if (debug) System.out.println("inInsert && !inFrom && myWord.equals(\"SELECT\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting the SELECT onto a new line for an INSERT statement");
      }      
      
      /**
       * Put the keyword 'HAVING' on a newline 
       */
      try {
        if (myWord.equals("HAVING")) {
          if (debug) System.out.println("myWord.equals(\"HAVING\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting the HAVING onto a new line ");
      }
      
      /**
       * If a comma is found in a select statement, and it's not inside of 
       * brackets then put the comma on a new line.  Any indentation required is 
       * done later.
       */
      try {
        if (inSelect && myWord.equals(",") && !brackets && !inComment && !inSingleLineComment) {
//        if (inSelect && myWord.equals(",") && !brackets && !inSingleQuotes && !inDoubleQuotes && !inComment) {
          if (debug) System.out.println("inSelect  && !inComment");
          formattedSQL.append(("\n"));
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma in a SELECT onto a new line");
      }
      
      try {
        if (inSelect && lastWord.equals("*/") && !brackets) {
//        if (inSelect && lastWord.equals("*/") && !brackets && !inSingleQuotes && !inDoubleQuotes) {
          if (debug) System.out.println("inSelect && myWord.equals(\",\") && (!brackets)");
          formattedSQL.append(("\n"));
          charsThisLine = 0;
          int numSpaces = 7;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] + 8;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma in a SELECT onto a new line");
      }
      
      /**
       * If a comma is found in a from clause, put it on a newline.  In the 
       * case of a subquery it will then need indenting.
       */
      try {
        if (inFrom && myWord.equals(",")) {
          if (debug) System.out.println("inFrom && myWord.equals(\",\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma in a FROM clause onto a new line");
      }
      
      /**
       * Put the keyword 'SET' of an update statement onto a new line.
       */
      try {
        if (inSet && myWord.equals("SET")) {
          if (debug) System.out.println("inSet && myWord.equals(\"SET\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting SET in an UPDATE clause onto a new line");
      }
      
      /**
       * Place a comma found in the SET clause of an update statement onto a 
       * new line.
       */
      try {
        if (inUpdate && inSet && myWord.equals(",") && (!inSelect) && bracketStackCounter == 0) {
          if (debug) System.out.println("inUpdate && inSet && myWord.equals(\",\") && (!inSelect) && bracketStackCounter == 0");
            formattedSQL.append("\n");
            charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma in SET clause of an UPDATE onto a new line");
      }
      
      try {
        if (inUpdate && inSet && lastWord.equals(",") && (!inSelect) && bracketStackCounter == 0) {
          if (debug) System.out.println("inUpdate && inSet && lastWord.equals(\",\") && (!inSelect) && bracketStackCounter == 0");
            int numSpaces =2;
            if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +2;
            addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting after a comma in SET clause of an UPDATE");
      }
      /**
       * Put the 'OR' keyword in a where clause onto a new line.
       */
      try {
        if ((inWhere) && myWord.equals("OR")) {
          if (debug) System.out.println("(inWhere) && myWord.equals(\"OR\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting OR in a WHERE clause onto a new line");
      }
      
      /**
       * Put the 'AND' keyword in a where clause onto a new line.
       */
      
      try {
        if ((inWhere) && myWord.equals("AND") && !inBetween) {
          if (debug)
            System.out.println("(inWhere) && myWord.equals(\"AND\") && !inBetween");
          formattedSQL.append("\n");
          charsThisLine = 0;
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting AND in a WHERE clause onto a new line");
      }
      
      /**
       * The keywords 'FROM' & 'WHERE' & 'VALUES' always go on a new line.  
       * Indenting will occur is they are within brackets.
       */
      try {
        if (myWord.equals("FROM") || myWord.equals("WHERE") || myWord.equals("VALUES")) { 
          if (debug) System.out.println("myWord.equals(\"FROM\") || myWord.equals(\"WHERE\") || myWord.equals(\"VALUES\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting FROM WHERE VALUES onto a new line");
      }
      
      if (myWord.equals("FOR") && nextWord.toUpperCase().equals("UPDATE")) {
        if (debug) System.out.println("myWord.equals(\"FOR\") && nextWord.equals(\"UPDATE\")");
        formattedSQL.append("\n");
        charsThisLine = 0;
        int numSpaces =0;
        if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
        addSpaces(numSpaces);        
      }
      
      /**
       * A comma found inside the values clause of an insert statement will be 
       * put onto a new line and indented.
       */
      try {
        if (inValues && myWord.equals(",") && bracketStackCounter <= 1) {
          if (debug) System.out.println("inValues && myWord.equals(\",\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma inside VALUES clause of INSERT onto a new line");
      }
      
      /**
       * A comma found inside the order by clause will be 
       * put onto a new line and indented.
       */
      try {
        if (inOrderBy && myWord.equals(",") && bracketStackCounter == 0 && !inGroupBy) {
          if (debug) System.out.println("inOrderBy && myWord.equals(\",\") && bracketStackCounter == 0 && !inGroupBy");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces = 0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma inside Order By clause onto a new line");
      }      
      
      try {
        if (inOrderBy && lastWord.equals(",") && bracketStackCounter == 0 && !inGroupBy) {
          if (debug) System.out.println("inOrderBy && lastWord.equals(\",\") && !inGroupBy");
          int numSpaces = 7;
//          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"indenting after a comma inside Order By clause");
      }          
      
      try {
        if (inGroupBy && lastWord.equals(",") && bracketStackCounter == 0 && !inOrderBy) {
          if (debug) System.out.println("inGroupBy && lastWord.equals(\",\") && !inOrderBy");
          int numSpaces = 7;
//          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"indenting after a comma inside Group By clause");
      }      
      
      /**
       * A comma found inside the group by clause will be 
       * put onto a new line and indented.
       */
      try {
        if (inGroupBy && myWord.equals(",") && bracketStackCounter == 0 && !inOrderBy) {
          if (debug) System.out.println("inGroupBy && myWord.equals(\",\") && !inOrderBy");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces = 0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma inside Group By clause onto a new line");
      }      
      
      /**
       * A WHEN or ELSE keyword of CASE statement will be put onto a new line
       */
      try {
        if (inCase && (myWord.equals("WHEN") || myWord.equals("ELSE"))) {
          if (debug) System.out.println("myWord.equals(\"WHEN\") || myWord.equals(\"ELSE\"))");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces = 9;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting WHEN or ELSE in a case statement on a new line");
      }      
      
      /**
       * An END keyword of CASE statement will be put onto a new line
       */
      try {
        if (inCase && myWord.equals("END")) {
          if (debug) System.out.println("inCase && myWord.equals(\"END\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces = 7;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] +1;
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting END in a case statement on a new line");
      }
      
   
      /**
       * A comma found in the 'INTO' clause of an insert statement will be put
       * on a new line and indented.
       */
      try {
        if (inInto && inInsert && myWord.equals(",")) {
//        if (inInto && inInsert && myWord.equals(",") && !inSingleQuotes && !inDoubleQuotes) {
          if (debug) System.out.println(myWord + "     "+ "inInto && inInsert && myWord.equals(\",\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting comma in the INTO clause of an INSERT onto a new line");
      }
      
      /**
       * The 'UNION' keyword will always be place on a new line.
       */
      try {
        if (myWord.equals("UNION")) {
          if (debug) System.out.println("myWord.equals(\"UNION\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);        
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting UNION onto a new line");
      }
            
      /**
       * Put an 'ORDER BY' or 'GROUP BY' clause on a new line.
       */
      try {
        if ((myWord.equals("ORDER") || myWord.equals("GROUP")) && nextWord.equals("BY")) {
          if (debug) System.out.println("(myWord.equals(\"ORDER\") || myWord.equals(\"GROUP\")) && words[i +1].equals(\"BY\")");
          formattedSQL.append("\n");
          charsThisLine = 0;
          int numSpaces =0;
          if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1];
          addSpaces(numSpaces);                        
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Putting ORDER BY GROUP BY onto a new line");
      }
      
      /**
       * An 'OR' keyword will be indented to match the bracketing.
       */
      try {
        if ((inWhere) && myWord.equals("OR")) {
          if (debug) System.out.println("(inWhere) && myWord.equals(\"OR\")");
          if (bracketStackCounter == 0) { 
            addSpaces(3);
          }
          else {
            int numSpaces = bracketStack[bracketStackCounter -1] +1;   // sub query depth
            if (!brackets) numSpaces = numSpaces +3;  // an OR not inside brackets needs indenting against the WHERE

            addSpaces(numSpaces);
          }
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting OR");
      }
      
      /**
       * An 'AND' keyword will be indented to match the bracketing.
       */
      try {
        if ((inWhere) && myWord.equals("AND") && !inBetween) {
          if (debug) System.out.println("(inWhere) && myWord.equals(\"AND\")");
          if (bracketStackCounter == 0) { 
            addSpaces(2);
          }
          else {
            int numSpaces = 2;
            if (bracketStackCounter > 0) numSpaces = bracketStack[bracketStackCounter -1] + 1;
            addSpaces(numSpaces);
          }
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting AND");
      }
      
      try {
        if ((lastWord.endsWith("'") && !brackets && inSelect && !inCase) || (lastWord.endsWith("'") && (inComment || inSingleLineComment) && inSelect && !inCase)) {
          addSpaces(1);
          if (debug) System.out.println("(lastWord.endsWith(\"'\") && !brackets && inSelect) || (lastWord.endsWith(\"'\") && inComment && inSelect)");
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"added a space following a single quoted string");
      }
     
      /**
       * Add the current word to the formatted output.
       */
      try {
        if (!myWord.equals("<<crinsinglelinecomment>>")) {
          formattedSQL.append(myWord);
          charsThisLine = charsThisLine + myWord.length();
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"adding current word to formatted output");
      }
      
      /**
       * Indent any that follows a comma in a 'SELECT' clause.  The output 
       * should then look like
       * 
       * select a
       * ,      b
       * ,      c
       * 
       */
      try {
        if ((inSelect) && myWord.equals(",") && (!brackets) && !inComment && !inSingleLineComment) {
//        if ((inSelect) && myWord.equals(",") && (!brackets) && !inSingleQuotes && !inDoubleQuotes && !inComment) {
          addSpaces(5); 
          if (debug) System.out.println("(inSelect) && myWord.equals(\",\") && (!brackets)");
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting 5 spaces");
      }
      
      /**
       * Indent any table name in the from clause.  The output should then look 
       * like
       * 
       * from a
       * ,    b
       * ,    c
       * 
       */
      try {
        if ((inFrom) && myWord.equals(",")) {
          addSpaces(3);
          if (debug) System.out.println("(inFrom) && myWord.equals(\",\")");
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting 3 spaces");
      }
      
      /**
       * Indent after a comma in a 'SET' clause of the update statement. ie
       * 
       * update table x
       * set a='a'
       * ,   b='b'
       * ,   c='c'
       */
//      try {
//        if ((inSet) && (!inSelect) && myWord.equals(",") && bracketStackCounter == 0 && !inSingleQuotes && !inDoubleQuotes) {
//          addSpaces(2);
//          if (debug) System.out.println("inSet) && (!inSelect) && myWord.equals(\",\") && bracketStackCounter == 0 && !inSingleQuotes && !inDoubleQuotes");
//        }
//      }
//      catch (Exception e) {
//        ConsoleWindow.displayError(e,this,"Indenting 2 spaces");
//      }      
      
  //    try {
//        if (nextWord.equals("||") && myWord.endsWith("'")) {
     //     addSpaces(1);
//          if (debug) System.out.println("myWord.equals(\"||\") && myLastWord.endsWith(\"'\")");
//        }
//      }
//      catch (Exception e) {
//        ConsoleWindow.displayError(e,this,"added a space following a single quoted string");
//      }


      
      
           
      /**
       * The last word was an '('.  If the bracket contains more than 1 
       * predicate, the second predicate might contain 'AND' or 'OR'. 
       */
      try {
//        if ((inWhere) && myWord.equals("(") && (words[i +4].equals("AND")) && (!nextWord.equals("SELECT") && (!nextWord.equals("+"))) && !inSingleQuotes && !inDoubleQuotes) {
//          addSpaces(4);
//          if (debug) System.out.println("(inWhere) && myWord.equals(\"(\") && (words[i +4].equals(\"AND\")) && (!words[i +1].equals(\"SELECT\") && (!words[i +1].equals(\"+\")))");
//        }
        if (inWhere && myWord.equals("(")) {
//        if (inWhere && myWord.equals("(") && !inSingleQuotes && !inDoubleQuotes) {
          int numSpaces = readAhead(i);
          addSpaces(numSpaces);
          if (debug) System.out.println("inWhere && myWord.equals(\"(\") && !inSingleQuotes && !inDoubleQuotes");
        }

//        if ((inWhere) && myWord.equals("(") && (words[i +4].equals("OR")) && (!nextWord.equals("SELECT")) && (!nextWord.equals("+"))) {
//          addSpaces(3);
//          if (debug) System.out.println("(inWhere) && myWord.equals(\"(\") && (words[i +4].equals(\"OR\")) && (!words[i +1].equals(\"SELECT\")) && (!words[i +1].equals(\"+\"))");
//        }
      }
      catch (Exception e) {
      // No error is given here, until i figure out why it happens
//        ConsoleWindow.displayError(e,this,"Indenting either 2 or 3 spaces");
      }

      
      /**
       * 
       */
      try  {
        if ((inValues || inInto) && myWord.equals("(")) {
          addSpaces(2);
          if (debug) System.out.println("(inValues || inInto) && myWord.equals(\"(\")");
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Indenting 1 space");
      }
      
      /* follow each word with a space. 
       * 
       * Beware, there is a bug here.  Any word ending in ' will not be followed by a space, but this is wrong in the case of "where 'Y' in (select ..."
       */
    
      try {
        if ((inBetween && nextWord.equals("AND")) || (myWord.equals("SYSDATE") && !nextWord.equals(")")) || (!isFunction(myWord.toLowerCase()) || (isFunction(myWord.toLowerCase()) && (nextWord.equals("AND") || nextWord.equals("OR") || startsMathmatical(nextWord) )) ) && !myWord.equals("(") && !nextWord.equals(")") && !myWord.equals(":") && !nextWord.equals(",") && (myWord.charAt(myWord.length() -1) != '\'')) {
//        if ((inBetween && nextWord.equals("AND")) || (myWord.equals("SYSDATE") && !nextWord.equals(")")) || (!isFunction(myWord.toLowerCase()) || (isFunction(myWord.toLowerCase()) && (nextWord.equals("AND") || nextWord.equals("OR") || startsMathmatical(nextWord) )) ) && !myWord.equals("(") && !nextWord.equals(")") && !myWord.equals(":") && !nextWord.equals(",") && (myWord.charAt(myWord.length() -1) != '\'') && !inDoubleQuotes) {
          if (!(inComment && isHint(myWord.toUpperCase()) && nextWord.equals("("))) {
            formattedSQL.append(" ");
            charsThisLine = charsThisLine + 1;
          }
        }
        else {
            if (nextWord.equals("IN")) {
                formattedSQL.append(" ");
                charsThisLine = charsThisLine + 1;
            }
        }
      }
      catch (Exception e) {
        ConsoleWindow.displayError(e,this,"Append a following space");
        if (debug) System.out.println("myWord:" + myWord);
      }
      
 
    }
  }
  

  /**
   * Add any number of spaces to the current place in sql statement.
   * 
   * @param numSpaces 
   */
  private void addSpaces(int numSpaces) {
    for (int i = 0; i < numSpaces; i++) {
      formattedSQL.append(" ");
    }
    charsThisLine = charsThisLine + numSpaces;
  }
  
  /**
   * Add the position of a bracket to the stack
   * 
   * @param position 
   */
  private void pushBracketStack(int position) {
    try {
      bracketStack[bracketStackCounter++] = position;
    }
    catch (Exception e) {
      /*
       * If a piece of sql has an odd number of brackets, ignoring this errro allows RichMon to continue without error.
       * I know this sounds odd, but I saw it happen on scmrepdb.
       */
    }
  }
  
  /**
   * Take a position of the stack (position of a bracket)
   */
  private void popBracketStack() {
    bracketStackCounter--;
  }
  
  /**
   * Reset the stack
   */
  private void resetBracketStack() {
    bracketStack = new int[wordStoreSize];
    bracketStackCounter = 0;
  }
  
  /**
   * Split a sql statement in different lines
   * 
   * @param myFormattedSQL 
   */
  private void splitIntoLines(StringBuffer myFormattedSQL) {
    StringBuffer line = new StringBuffer();
    for (int i = 0; i < myFormattedSQL.length(); i++) {
      if (myFormattedSQL.charAt(i) != '\n') {
        // append to the current line 
        line.append(myFormattedSQL.charAt(i));
      }
      else {
        // add a line of sql 
        finalSQL[numLines++] = line.toString();
        line = new StringBuffer();
      }
      // add the final line 
    }
    finalSQL[numLines++] = line.toString();
  }
  
  private void reformPLSQLStatement() {
  }
  
  private boolean startsMathmatical(String myWord) {
    if (myWord.startsWith("+") || myWord.startsWith("-") || myWord.startsWith("/") || myWord.startsWith("*")) {
      return true;
    }
    else {
      return false;
    }
  }
  
  /**
   * Read Ahead thru the words array to decide how many spaces are required after an open bracket.
   */
  private int readAhead(int pos) {
    if (words[pos+1].equals("SELECT")) return 0;
    
    boolean localInBetween = false;
    boolean localInComment = false;
    int localNumBrackets = 0;
    
    for (int i = pos+1; i < words.length ; i++) {
      String localWord = words[i];
      String localLastWord = words[i-1];
      if (localWord.equals("(")) localNumBrackets++;
      if (localWord.equals(")")) localNumBrackets--;
      if (localNumBrackets < 0) return 0;  // reached relevant closing bracket
      if (localWord.equals("BETWEEN") && localNumBrackets == 0) localInBetween = true;
      if (localLastWord.equals("AND") && localInBetween && localNumBrackets == 0)  localInBetween = false;
      if ((localWord.equals("/*+") || localWord.equals("/*")) && localNumBrackets == 0) localInComment = true;
      if (localWord.equals("*/") && localInComment && localNumBrackets == 0) localInComment = false;


      if (localWord.equals("OR") && !localInComment) {
        return 3;
      }
      if (localWord.equals("AND") && !localInComment && !localInBetween) {
        return 4;
      }
    }
    
    // did not find AND or OR in future words
    return 0;
  }
}