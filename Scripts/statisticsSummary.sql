SELECT owner
,      DECODE(last_analyzed, null, 'not analyzed', 'analyzed' ) "Status"
,      'Table' segment_type
,      COUNT(*) "#" 
FROM dba_tables 
WHERE owner not IN ('SYS', 'SYSTEM', 'PERFSTAT', 'VERITAS_I3', 'OUTLN') 
GROUP BY owner
,        DECODE(last_analyzed, null, 'not analyzed', 'analyzed') having COUNT(*) > 0 
UNION ALL 
SELECT owner
,      DECODE(last_analyzed, null, 'not analyzed', 'analyzed' ) "Status"
,      'Index' segment_type
,      COUNT(*) "#" 
FROM dba_indexes 
WHERE owner not IN ('SYS', 'SYSTEM', 'PERFSTAT', 'VERITAS_I3', 'OUTLN') 
GROUP BY owner
,        DECODE(last_analyzed, null, 'not analyzed', 'analyzed') having COUNT(*) > 0 
ORDER BY 1 