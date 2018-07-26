SELECT s.sid
,      s.status
,      u.tablespace
,      u.contents
,      SUM(u.extents) extents
,      SUM(u.blocks) blocks
,      SUM(ROUND((u.blocks * p.value) /1024, 2)) "Size of sort in kb"
,      SUM(ROUND((u.blocks * p.value) /1024 /1024, 2)) "Size of Sort in mb" 
FROM gv$session s
,    gv$sort_usage u
,    gv$parameter p 
WHERE u.session_addr = s.saddr 
  AND p.name = 'db_block_size'
  AND s.inst_id = u.inst_id 
  AND s.inst_id = p.inst_id 
GROUP BY s.sid
,        s.status
,        u.tablespace
,        u.contents 
ORDER BY SUM(u.extents)
/
