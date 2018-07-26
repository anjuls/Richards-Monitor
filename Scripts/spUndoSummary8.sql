SELECT b.usn
,      e.gets - b.gets "Trans Table Gets"
,      TO_CHAR(TO_NUMBER(DECODE(e.gets, b.gets, null, (e.waits - b.waits) * 100 / (e.gets - b.gets))), '990.99' ) "Pct Waits"
,      TO_CHAR((e.writes - b.writes) /1024 /1024, '999,999,990.99' ) "Undo Mb Written"
,      e.wraps - b.wraps "Wraps"
,      e.shrinks - b.shrinks shrinks
,      e.extends - b.extends extends 
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
