SELECT endt "end time"
,      undob "undo blocks"
,      txcnt "num trans"
,      maxq "max qry lenth(s)"
,      maxc "max tx concurrency"
,      ROUND(mintun, 2) "min tuned retention"
,      snolno "snapshot too old"
,      blkst "uS/uR/uU//eS/eR/eU" 
FROM (SELECT undotsn
      ,      TO_CHAR(end_time, 'DD-Mon HH24:MI' ) endt
      ,      undoblks undob
      ,      txncount txcnt
      ,      maxquerylen maxq
      ,      maxconcurrency maxc
      ,      tuned_undoretention /60 mintun
      ,      ssolderrcnt || '/' || nospaceerrcnt snolno
      ,      unxpstealcnt || '/' || unxpblkrelcnt || '/' || unxpblkreucnt || '/' || expstealcnt || '/' || expblkrelcnt || '/' || expblkreucnt blkst 
      FROM dba_hist_undostat 
      WHERE dbid = :dbid 
        AND instance_number = :inst_num 
        AND end_time > TO_DATE(:btime, 'DD/MM/YY HH24:MI:SS') 
        AND begin_time < TO_DATE(:etime, 'DD/MM/YY HH24:MI:SS') 
      ORDER BY begin_time desc ) 
