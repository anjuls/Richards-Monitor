SELECT i.instance_name
,      s.sid
,      s.username
,      l.type
,      l.id1
,      l.id2
,      l.lmode
,      l.request
,      p.spid 
FROM gv$lock l
,    gv$session s
,    gv$process p 
,    gv$instance i
WHERE l.id1 IN (SELECT ll.id1 
                FROM gv$lock ll 
                WHERE request > 0 ) 
  AND s.sid = l.sid 
  AND s.inst_id = l.inst_id 
  AND p.addr = s.paddr 
  AND p.inst_id = s.inst_id 
  AND i.inst_id = s.inst_id
ORDER BY l.id1
,        l.request