SELECT segment_id
,      segment_name
,      dr.status
,      extents
,      next_extent /1024 /1024 "Next Extent (mb)"
,      optsize /1024 /1024 "Optimal (mb)"
,      shrinks
,      wraps
,      dr.tablespace_name 
FROM dba_rollback_segs dr
,    gv$rollstat rs 
WHERE dr.segment_id = rs.usn (+)
/
