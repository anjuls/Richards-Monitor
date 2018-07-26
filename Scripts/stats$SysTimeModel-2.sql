SELECT b.stat_name "Statistic Name"
,      ROUND((e.value - b.value)/1000000, 2) "Time(S)"
,      ROUND((((e.value - b.value)/1000000)/?)*100,2) "% of DB Time"
FROM dba_hist_sys_time_model b
,    dba_hist_sys_time_model e
WHERE b.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.stat_name = e.stat_name 
  AND b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.instance_number = ?
  AND b.instance_number = e.instance_number
  AND b.stat_name IN ('DB CPU', 'sql execute elapsed time', 'parse time elapsed', 'PL/SQL execution elapsed time', 'hard parse elapsed time9', 'PL/SQL compilation elapsed time', 'connection management call elapsed time', 'hard parse (sharing criteria) elapsed time', 'repeated bind elapsed time', 'sequence load elapsed time') 
UNION ALL 
SELECT b.stat_name "Statistic Name"
,      ROUND((e.value - b.value)/1000000, 2) "Time(S)"
,      ROUND((((e.value - b.value)/1000000)/?)*100,2) "% of DB Time"
FROM dba_hist_sys_time_model b
,    dba_hist_sys_time_model e
WHERE b.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.stat_name = e.stat_name 
  AND b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.instance_number = ?
  AND b.instance_number = e.instance_number
  AND b.stat_name IN ('DB time')
ORDER BY 3 desc
/
