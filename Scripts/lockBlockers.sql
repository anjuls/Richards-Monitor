SELECT i.instance_name
,      l.sid
,      l.type
,      l.id1
,      l.id2
,      l.lmode
,      l.request
FROM gv$lock l 
,    gv$instance i
WHERE l.id1 IN (SELECT l2.id1 
                FROM v$lock l2
                WHERE l2.request > 0 ) 
  AND l.lmode > 0 
  AND i.inst_id = l.inst_id
ORDER BY l.sid 