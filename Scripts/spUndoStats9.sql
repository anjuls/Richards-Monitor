SELECT endt "end time"
,      undob "undo blocks"
,      txcnt "num trans"
,      maxq "max qry lenth(s)"
,      maxc "max tx concurrency"
,      snol "snapshot too old"
,      nosp "out of space"
,      blkst "uS/uR/uU/eS/eR/eU" 
FROM (SELECT undotsn
      ,      TO_CHAR(end_time, 'DD-Mon HH24:MI' ) endt
      ,      undoblks undob
      ,      txncount txcnt
      ,      maxquerylen maxq
      ,      maxconcurrency maxc
      ,      ssolderrcnt snol
      ,      nospaceerrcnt nosp
      ,      unxpstealcnt || '/' || unxpblkrelcnt || '/' || unxpblkreucnt || '/' || expstealcnt || '/' || expblkrelcnt || '/' || expblkreucnt blkst 
      FROM stats$undostat 
      WHERE dbid = ? 
        AND instance_number = ? 
        AND end_time > TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
        AND begin_time < TO_DATE(?, 'DD/MM/YY HH24:MI:SS') 
      ORDER BY begin_time desc ) 
