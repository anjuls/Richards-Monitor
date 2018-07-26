select s.sid
,      u.tablespace
,      u.contents
,      sum(u.extents) extents
,      sum(u.blocks) blocks
,      round((sum(u.blocks) * p.value)/1024,2)
,      round((sum(u.blocks) * p.value)/1024/1024,2)
FROM gv$session s
,    gv$sort_usage u
,    gv$parameter p
WHERE s.sid like ?
and   s.inst_id = ?
and   s.status = 'ACTIVE'
and   u.session_addr = s.saddr
and   u.inst_id = s.inst_id
and   p.name = 'db_block_size'
and   p.inst_id = ?
group by s.sid,u.tablespace,u.contents,p.value
/