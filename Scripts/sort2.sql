select s.sid
,      sum(round((u.blocks * p.value)/1024,2))
FROM v$session s
,    v$sort_usage u
,    v$parameter p
WHERE u.session_addr = s.saddr
and   p.name = 'db_block_size'
group by s.sid
order by 1
/