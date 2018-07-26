SELECT s.sql_exec_id
,      s.sid
,      s.sql_id
,      s2.event
,      s.sql_exec_start
,      s.status
,      s.elapsed_time
,      s.px_qcinst_id
,      s.px_qcsid
,      s.cpu_time
,      s.application_wait_time
,      s.concurrency_wait_time
,      s.cluster_wait_time
,      s.user_io_wait_time
,      s.plsql_exec_time
,      s.java_exec_time
,      s.disk_reads
,      s.buffer_gets
FROM gv$sql_monitor s
,    gv$session s2
WHERE s.status in ('EXECUTING','QUEUED')
  AND s.sid = s2.sid
  AND s.inst_id = s2.inst_id
  AND s.sql_exec_start = (SELECT MAX(sql_exec_start)
                        FROM gv$sql_monitor s3
                        WHERE s3.inst_id = s.inst_id
                          AND s3.sid = s.sid )
ORDER BY elapsed_time desc
