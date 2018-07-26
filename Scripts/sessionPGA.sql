SELECT s.sid
,      sn.name
,      round(s.value / 1024,2)
FROM gv$sesstat s
,    gv$statname sn 
WHERE s.sid = ?
  AND s.inst_id = ?
  AND s.statistic# = sn.statistic# 
  and s.inst_id = sn.inst_id
  AND sn.name = 'session pga memory'
/
