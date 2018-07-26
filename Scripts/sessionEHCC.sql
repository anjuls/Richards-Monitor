SELECT s.value
,      n.name 
FROM gv$sesstat s
,    gv$statname n 
WHERE s.statistic# = n.statistic# (+) 
  AND n.inst_id = ?
  AND s.inst_id = ?
  AND s.sid = ?
  AND n.name like ('EHCC%')
/
