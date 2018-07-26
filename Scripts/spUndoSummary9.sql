SELECT undotsn "undo ts#"
,      SUM(undoblks) "undo blocks"
,      SUM(txncount) "num trans"
,      MAX(maxquerylen) "max qry lenth(s)"
,      MAX(maxconcurrency) "max tx concurrency"
,      SUM(ssolderrcnt) "snapshot too old"
,      SUM(nospaceerrcnt) "out of space"
,      SUM(unxpstealcnt) "unexpired stolen blocks"
,      SUM(unxpblkrelcnt) "unexpired released blocks"
,      SUM(unxpblkreucnt) "unexpired reused blocks"
,      SUM(expstealcnt) "expired stolen blocks"
,      SUM(expblkrelcnt) "expired released blocks"
,      SUM(expblkreucnt) "expired reused blocks" 
FROM stats$undostat 
WHERE dbid = ? 
  AND instance_number = ? 
  AND end_time > TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
  AND begin_time < TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
GROUP BY undotsn 
