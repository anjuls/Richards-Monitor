SELECT /*+ ORDERED */ 
       e.eq_type "Enqueue Type"
,      e.total_req# - NVL(b.total_req#, 0) requests
,      e.succ_req# - NVL(b.succ_req#, 0) "Succ Gets"
,      e.failed_req# - NVL(b.failed_req#, 0) "Failed Gets"
,      e.total_wait# - NVL(b.total_wait#, 0) waits
,      (e.cum_wait_time - NVL(b.cum_wait_time, 0)) /1000 "Wt Time (s)"
,      TO_CHAR(DECODE((e.total_wait# - NVL(b.total_wait#, 0)), 0, TO_NUMBER(null), ((e.cum_wait_time - NVL(b.cum_wait_time, 0)) / (e.total_wait# - NVL(b.total_wait#, 0)))), '999,999,999,990.99' ) "Av Wt Time(ms)" 
FROM stats$enqueue_stat e
,    stats$enqueue_stat b 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = ? 
  AND e.instance_number = ? 
  AND b.instance_number (+) = e.instance_number 
  AND b.eq_type (+) = e.eq_type 
  AND e.total_wait# - NVL(b.total_wait#, 0) > 0 
ORDER BY 7 desc
,        5 desc 
