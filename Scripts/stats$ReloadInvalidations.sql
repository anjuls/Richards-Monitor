SELECT e.namespace || 'reloads'
,      e.reloads - b.reloads 
FROM stats$librarycache b
,    stats$librarycache e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.namespace = e.namespace 
  AND b.namespace IN ('SQL AREA', 'TABLE/PROCEDURE', 'BODY', 'TRIGGER') 
UNION ALL 
SELECT e.namespace || 'invalidations'
,      e.invalidations - b.invalidations 
FROM stats$librarycache b
,    stats$librarycache e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.namespace = e.namespace 
  AND b.namespace IN ('SQL AREA', 'TABLE/PROCEDURE', 'BODY', 'TRIGGER') 