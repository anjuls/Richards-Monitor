SELECT t.name "Temp File Name"
,      t.bytes /1024 /1024 "Size of Temp File (mb)"
,      NVL(su.bytes /1024 /1024, 0) "Used Space (mb)"
,      (t.bytes - NVL(su.bytes, 0)) /1024 /1024 "Free Space (mb)" 
FROM (SELECT s.inst_id
      ,      (SUM(blocks) * p.value) bytes
      ,      segrfno#
      ,      ts.ts# 
      FROM gv$sort_usage s
      ,    gv$parameter p
      ,    v$tablespace ts 
      WHERE p.name = 'db_block_size'
        AND s.tablespace = ts.name 
        AND s.inst_id = p.inst_id  
      GROUP BY s.inst_id
      ,        segrfno#
      ,        ts.ts#
      ,        p.value ) su
,    (SELECT ts#
      ,      rfile#
      ,      name
      ,      bytes 
      FROM v$tempfile ) t 
WHERE t.rfile# = su.segrfno# (+) 
  AND t.ts# = su.ts# (+) 
ORDER BY t.name 
/
      