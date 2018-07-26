SELECT tablespace_name tablespace
,      initial_extent
,      next_extent
,      max_extents
,      pct_increase
,      status
,      contents
,      logging 
FROM dba_tablespaces 
ORDER BY tablespace_name 
