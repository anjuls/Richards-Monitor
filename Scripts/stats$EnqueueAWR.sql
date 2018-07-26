SELECT /*+ ORDERED */ 
       e.eq_type || '-' || TO_CHAR(NVL(l.name, ' ')) || DECODE(UPPER(e.req_reason), 'CONTENTION', null, '-', null, ' ('|| e.req_reason || ')' ) "Enqueue Type (Request Reason)"
,      e.total_req# - NVL(b.total_req#, 0) requests
,      e.succ_req# - NVL(b.succ_req#, 0) "Succ Gets"
,      e.failed_req# - NVL(b.failed_req#, 0) "Failed Gets"
,      e.total_wait# - NVL(b.total_wait#, 0) waits
,      (e.cum_wait_time - NVL(b.cum_wait_time, 0)) /1000 "Wt Time (s)"
,      TO_CHAR(DECODE((e.total_wait# - NVL(b.total_wait#, 0)), 0, TO_NUMBER(null), ((e.cum_wait_time - NVL(b.cum_wait_time, 0)) / (e.total_wait# - NVL(b.total_wait#, 0)))), '999,999,999,990.99' ) "Av Wt Time(ms)" 
FROM dba_hist_enqueue_stat e
,    dba_hist_enqueue_stat b
,    v$lock_type l 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = ? 
  AND e.instance_number = ? 
  AND b.instance_number (+) = e.instance_number 
  AND b.eq_type (+) = e.eq_type 
  AND b.req_reason (+) = e.req_reason 
  AND e.total_wait# - NVL(b.total_wait#, 0) > 0 
  AND l.type (+) = e.eq_type 
ORDER BY 7 desc
,        5 desc 
