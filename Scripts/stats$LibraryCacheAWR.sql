SELECT e.namespace
,      e.gets - b.gets gets
,      ROUND(TO_NUMBER(DECODE(e.gets, b.gets, null, 100 - (e.gethits - b.gethits) * 100 / (e.gets - b.gets))), 2) "% of Get Misses"
,      e.pins - b.pins pins
,      ROUND(TO_NUMBER(DECODE(e.pins, b.pins, null, 100 - (e.pinhits - b.pinhits) * 100 / (e.pins - b.pins))), 2) "% of Pin Misses"
,      e.reloads - b.reloads reloads
,      e.invalidations - b.invalidations invalidations 
FROM dba_hist_librarycache b
,    dba_hist_librarycache e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.namespace = e.namespace 
  AND e.gets - b.gets > 0 
