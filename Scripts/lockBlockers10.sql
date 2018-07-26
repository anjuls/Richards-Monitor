SELECT i.instance_name
,      s.sid
,      s.event
,      o.object_name
,      o.object_type 
FROM gv$session s
,    gv$session w
,    dba_objects o
,    gv$instance i
WHERE w.blocking_session = s.sid 
  AND s.inst_id = w.blocking_instance
  AND w.row_wait_obj# = o.object_id (+)
  AND i.inst_id = s.inst_id
/