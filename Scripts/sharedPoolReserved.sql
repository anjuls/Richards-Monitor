SELECT requests
,      TO_CHAR(avg_used_size, '999,999,999,990.90' ) "Avg Used Size"
,      TO_CHAR(avg_free_size, '999,999,999,990.90' ) "Avg Free Size"
,      request_failures
,      last_failure_size
,      request_misses
,      max_free_size
,      max_miss_size
,      last_miss_size
,      max_used_size
,      aborted_request_threshold
,      free_count
,      free_space
,      used_count
,      used_space
,      aborted_requests
,      last_aborted_size
FROM gv$shared_pool_reserved s 
