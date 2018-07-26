SELECT pga_target_for_estimate
,      pga_target_factor
,      advice_status "advice"
,      bytes_processed
,      estd_extra_bytes_rw
,      estd_pga_cache_hit_percentage "estd pga cache hit %"
,      estd_overalloc_count 
FROM gv$pga_target_advice p 
