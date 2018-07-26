select undotsn                      "undo ts#"
     , sum(undoblks)                "undo blocks"
     , sum(txncount)                "num trans"
     , max(maxquerylen)             "max qry lenth(s)"
     , max(maxconcurrency)          "max tx concurrency"
     , sum(ssolderrcnt)             "snapshot too old"
     , sum(nospaceerrcnt)           "out of space"
     , round(min(tuned_undoretention)/60,1)  "min tuned retention"
     , round(max(tuned_undoretention)/60,1)  "max tuned retention"
     , sum(unxpstealcnt)            "unexpired stolen blocks"
     , sum(unxpblkrelcnt)           "unexpired released blocks"
     , sum(unxpblkreucnt)           "unexpired reused blocks"
     , sum(expstealcnt)             "expired stolen blocks"
     , sum(expblkrelcnt)            "expired released blocks"
     , sum(expblkreucnt)            "expired reused blocks"
  from stats$undostat
 where dbid            = ?
   and instance_number = ?
   and end_time        >  to_date(?, 'DD/MM/YY HH24:MI:SS')
   and begin_time      <  to_date(?, 'DD/MM/YY HH24:MI:SS')
 group by undotsn
 /
