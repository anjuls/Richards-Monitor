SELECT c.name
,      l.gets
,      l.misses
,      l.sleeps
,      l.immediate_gets
,      l.immediate_misses
,      ROUND((l.misses / (l.gets +.001)) *100, 2) "miss_ratio"
,      ROUND((l.immediate_misses / (l.immediate_gets +.001)) *100, 2) "immediate_miss_ratio"
,      b.pid 
FROM gv$latch l
,    gv$latchholder b
,    gv$latchname c 
WHERE l.addr = b.laddr (+) 
  AND l.latch# = c.latch# 
  AND l.inst_id = b.inst_id (+)
  AND l.inst_id = c.inst_id
ORDER BY l.latch# 
