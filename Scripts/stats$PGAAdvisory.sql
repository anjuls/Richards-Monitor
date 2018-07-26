SELECT ROUND(pga_target_for_estimate /1024 /1024, 0) "PGA Target for Est (MB)"
,      pga_target_factor "Size Factor"
,      ROUND(bytes_processed /1024 /1024, 2) "W/A MB Processed"
,      ROUND(estd_extra_bytes_rw /1024 /1024, 2) "Extra W/A MB To Read or Write"
,      estd_pga_cache_hit_percentage "Est PGA Cache Hit Percent"
,      estd_overalloc_count "Est Over Allocation Count" 
FROM stats$pga_target_advice e 
WHERE snap_id = ? 
  AND dbid = ? 
  AND instance_number = ? 
ORDER BY pga_target_for_estimate 
