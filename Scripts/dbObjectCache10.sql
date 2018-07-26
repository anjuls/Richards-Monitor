SELECT kept
,      namespace
,      locks
,      invalidations
,      pins
,      child_latch
,      loads
,      db_link
,      executions
,      owner
,      type
,      sharable_mem
,      name 
FROM gv$db_object_cache c