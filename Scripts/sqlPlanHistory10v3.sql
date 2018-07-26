SELECT hs.snap_id "Snap_Id"
,      to_date(to_Char(hs.begin_interval_time,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "Interval Start Time"
,      ss.executions_delta execs
,      ss.plan_hash_value "Plan Hash Value"
,      ss.optimizer_cost "Cost"
,      to_char(round(( ss.elapsed_time_delta/decode(ss.executions_delta,0,1,ss.executions_delta)) /1000000,6),'9999999999999.999999') "Avg Exec Time in Secs"
,      to_char(round(( ss.cpu_time_delta/decode(ss.executions_delta,0,1,ss.executions_delta)) /1000000,6),'9999999999999.999999') "Avg CPU Time in Secs"
,      to_char(round(( ss.clwait_delta/decode(ss.executions_delta,0,1,ss.executions_delta)) /1000000,6),'9999999999999.999999') "Avg Cluster Wait Time in Secs"
,      ss.sharable_mem "Sharable Memory"
,      ss.fetches_delta fetches
,      ss.loads_delta loads
,      ss.invalidations_delta invalidations
,      ss.disk_reads_delta  "Disk Reads"
,      ss.buffer_gets_delta  "Buffer Gets"
,      ss.rows_processed_delta "Rows Processed"
FROM dba_hist_snapshot hs 
,    dba_hist_sqlstat ss 
WHERE hs.snap_id between ? and ?
  AND hs.snap_id = ss.snap_id 
  AND hs.dbid = ?
  AND ss.dbid = hs.dbid
  AND ss.instance_number = hs.instance_number
  AND ss.sql_id = ?
ORDER BY hs.snap_id
/
