SELECT s.name
,      s.value 
FROM gv$sysstat s 
WHERE s.inst_id = ?
  AND s.statistic# IN (SELECT statistic# 
                       FROM v$statname 
                       WHERE name like 'Parallel operations %')
/