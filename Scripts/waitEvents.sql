SELECT s.sid
,      s.username
,      sw.event
,      sw.p1
,      sw.p2
,      sw.p3
,      sw.wait_time
,      sw.seconds_in_wait
,      sw.state 
FROM gv$session_wait sw
,    gv$session s
,    gv$process p 
WHERE sw.sid = s.sid 
  AND s.paddr = p.addr 
  AND sw.inst_id = s.inst_id 
  AND s.inst_id = p.inst_id 
  AND sw.event != 'smon timer'
  AND sw.event != 'pmon timer'
  AND sw.event != 'rdbms ipc message'
  AND sw.event != 'Null event'
  AND sw.event != 'parallel query dequeue'
  AND sw.event != 'pipe get'
  AND sw.event != 'client message'
  AND sw.event != 'SQL*Net message to client'
  AND sw.event != 'SQL*Net message from client'
  AND sw.event != 'SQL*Net more data from client'
  AND sw.event != 'dispatcher timer'
  AND sw.event != 'virtual circuit status'
  AND sw.event != 'lock manager wait for remote message'
  AND sw.event != 'PX Idle Wait'
  AND sw.event != 'PX Deq: Execution Msg'
  AND sw.event != 'PX Deq: Table Q Normal'
  AND sw.event != 'wakeup time manager'
  AND sw.event != 'slave wait'
  AND sw.event != 'i/o slave wait'
  AND sw.event != 'jobq slave wait'
  AND sw.event != 'null event'
  AND sw.event != 'gcs remote message'
  AND sw.event != 'gcs for action'
  AND sw.event != 'ges remote message'
  AND sw.event != 'queue messages'
ORDER BY sw.seconds_in_wait desc
