SELECT CASE 
         WHEN e.low_optimal_size >= 1024 *1024 *1024 *1024 THEN LPAD(ROUND(e.low_optimal_size /1024 /1024 /1024 /1024) || 'T', 7) 
         WHEN e.low_optimal_size >= 1024 *1024 *1024 THEN LPAD(ROUND(e.low_optimal_size /1024 /1024 /1024) || 'G', 7) 
         WHEN e.low_optimal_size >= 1024 *1024 THEN LPAD(ROUND(e.low_optimal_size /1024 /1024) || 'M', 7) 
         WHEN e.low_optimal_size >= 1024 THEN LPAD(ROUND(e.low_optimal_size /1024) || 'K', 7) 
         ELSE LPAD(e.low_optimal_size || 'B', 7) 
       END "Low Optimal"
,      CASE 
         WHEN e.high_optimal_size >= 1024 *1024 *1024 *1024 THEN LPAD(ROUND(e.high_optimal_size /1024 /1024 /1024 /1024) || 'T', 7) 
         WHEN e.high_optimal_size >= 1024 *1024 *1024 THEN LPAD(ROUND(e.high_optimal_size /1024 /1024 /1024) || 'G', 7) 
         WHEN e.high_optimal_size >= 1024 *1024 THEN LPAD(ROUND(e.high_optimal_size /1024 /1024) || 'M', 7) 
         WHEN e.high_optimal_size >= 1024 THEN LPAD(ROUND(e.high_optimal_size /1024) || 'K', 7) 
         ELSE e.high_optimal_size || 'B'
       END "High Optimal"
,      e.total_executions - NVL(b.total_executions, 0) "Total Executions"
,      e.optimal_executions - NVL(b.optimal_executions, 0) "Optimal Executions"
,      e.onepass_executions - NVL(b.onepass_executions, 0) "One Pass Executions"
,      e.multipasses_executions - NVL(b.multipasses_executions, 0) "Multi Pass Executions"
FROM dba_hist_sql_workarea_hstgrm e
,    dba_hist_sql_workarea_hstgrm b 
WHERE e.snap_id = ? 
  AND e.dbid = ? 
  AND e.instance_number = ? 
  AND b.snap_id (+) = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = e.instance_number 
  AND b.low_optimal_size (+) = e.low_optimal_size 
  AND b.high_optimal_size (+) = e.high_optimal_size 
  AND e.total_executions - NVL(b.total_executions, 0) > 0 
ORDER BY e.low_optimal_size 
