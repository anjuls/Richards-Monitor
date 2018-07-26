SELECT begin_time
,      end_time
,      undotsn
,      undoblks
,      txncount
,      maxquerylen
,      maxqueryid
,      maxconcurrency
,      unxpstealcnt
,      unxpblkrelcnt
,      unxpblkreucnt
,      expstealcnt
,      expblkrelcnt
,      expblkreucnt
,      ssolderrcnt
,      nospaceerrcnt
,      activeblks
,      unexpiredblks
,      expiredblks
,      tuned_undoretention 
FROM gv$undostat u 
/