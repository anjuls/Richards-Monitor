WITH fts AS (SELECT DISTINCT sql_id
             ,      plan_hash_value
             FROM dba_hist_sql_plan 
             WHERE options = 'FULL'
               AND object_type = 'TABLE'
               AND dbid = ?) 
, topsqlbuffergets AS (SELECT instance_number
                       ,      sql_id
                       ,      plan_hash_value
                       ,      SUM(buffer_gets_delta) buffergets
                       ,      SUM(executions_delta) exec
                       ,      ROUND(DECODE(SUM(executions_delta), 0, TO_NUMBER(null), (SUM(buffer_gets_delta) / SUM(executions_delta))),2) getsperexecution
                       ,      SUM(cpu_time_delta) /1000000 cput
                       ,      ROUND(SUM(elapsed_time_delta) /1000000,3) elap 
                       FROM dba_hist_sqlstat 
                       WHERE dbid = ?
                         AND snap_id >= ?
                         AND snap_id < ?
                         AND instance_number = ?
                       GROUP BY instance_number
                       ,        sql_id
                       ,        plan_hash_value 
                       ORDER BY buffergets ) 
SELECT tsbg.instance_number
,      fts.sql_id
,      fts.plan_hash_value
,      tsbg.buffergets "Buffer Gets"
,      tsbg.exec "Executions"
,      tsbg.getsperexecution "Buffer Gets Per Execution"
,      tsbg.cput "CPU Time"
,      tsbg.elap "Elapsed Time"
FROM fts
,    topsqlbuffergets tsbg 
WHERE fts.sql_id = tsbg.sql_id 
  AND fts.plan_hash_value = tsbg.plan_hash_value 
  AND tsbg.buffergets > 0
ORDER BY 6 desc 
/
