SELECT w.inst_id
,      w.sid
,      w.event
,      o.object_name
,      o.object_type
,      w.blocking_session "Blocking Sid"
,      i.instance_name "Blocking Instance" 
FROM gv$session w
,    gv$instance i
,    dba_objects o 
WHERE w.blocking_instance = i.instance_number 
  AND w.row_wait_obj# = o.object_id (+)
/