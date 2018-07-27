select s.sid
,      u.tablespace
,      u.contents
,      u.extents
,      u.blocks
,      (u.blocks * p.value)/1024 "Size of sort in kb"
,      (u.blocks * p.value)/1024/1024 "Size of Sort in mb"
FROM v$session s
,    v$sort_usage u
,    v$parameter p
WHERE u.session_addr = s.saddr
and   p.name = 'db_block_size'
order by 5
/
