SELECT tablespace_name tablespace
,      initial_extent
,      next_extent
,      max_extents
,      pct_increase
,      status
,      contents
,      logging
,      extent_management
,      allocation_type 
,      segment_space_management
,      plugged_in
,      def_tab_compression
FROM dba_tablespaces 
ORDER BY tablespace_name 