SELECT id "Buffer Pool id"
,      name "Buffer Pool Name"
,      block_size
,      advice_status
,      size_for_estimate "Size for Estimate (mb)"
,      buffers_for_estimate
,      estd_physical_read_factor
,      estd_physical_reads 
FROM gv$db_cache_advice d
ORDER BY id
,        size_for_estimate