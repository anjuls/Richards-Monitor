SELECT sw.waiters "# Waiting Sessions"
,      o.owner
,      o.segment_name
,      o.segment_type 
FROM dba_extents o
,    (SELECT inst_id
      ,      waiters
      ,      p1
      ,      p2
      ,      p3 
      FROM (SELECT COUNT(*) waiters
            ,      p1
            ,      p2
            ,      p3
            ,      inst_id 
            FROM gv$session_wait 
            WHERE event = 'buffer busy waits'
               OR event = 'read by other session'
            GROUP BY p1
            ,        p2
            ,        p3
            ,        inst_id ) ) sw 
WHERE o.file_id = p1 
  AND sw.p2 BETWEEN o.block_id AND o.block_id + (o.blocks -1)