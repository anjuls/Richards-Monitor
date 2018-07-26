SELECT b.usn
,      e.rssize "Segment Size"
,      e.aveactive "Avg Active"
,      TO_NUMBER(DECODE(e.optsize, -4096, null, e.optsize)) "Optimal Size"
,      e.hwmsize "Maximum Size" 
FROM stats$rollstat b
,    stats$rollstat e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND e.usn = b.usn 
ORDER BY e.usn 