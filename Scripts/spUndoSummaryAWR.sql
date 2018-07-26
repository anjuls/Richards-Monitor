SELECT undotsn "undo ts#"
,      SUM(undoblks) "undo blocks"
,      SUM(txncount) "num trans"
,      MAX(maxquerylen) "max qry lenth(s)"
,      MAX(maxconcurrency) "max tx concurrency"
,      SUM(ssolderrcnt) "snapshot too old"
,      SUM(nospaceerrcnt) "out of space"
,      ROUND(MIN(tuned_undoretention) /60, 1) "min tuned retention"
,      ROUND(MAX(tuned_undoretention) /60, 1) "max tuned retention"
,      SUM(unxpstealcnt) "unexpired stolen blocks"
,      SUM(unxpblkrelcnt) "unexpired released blocks"
,      SUM(unxpblkreucnt) "unexpired reused blocks"
,      SUM(expstealcnt) "expired stolen blocks"
,      SUM(expblkrelcnt) "expired released blocks"
,      SUM(expblkreucnt) "expired reused blocks" 
FROM dba_hist_undostat 
WHERE dbid = ? 
  AND instance_number = ? 
  AND end_time > TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
  AND begin_time < TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
GROUP BY undotsn 
