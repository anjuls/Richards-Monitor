SELECT estd_lc_time_saved
,      estd_lc_memory_objects
,      estd_lc_size
,      shared_pool_size_factor
,      shared_pool_size_for_estimate
,      estd_lc_memory_object_hits
,      estd_lc_time_saved_factor 
FROM gv$shared_pool_advice s
