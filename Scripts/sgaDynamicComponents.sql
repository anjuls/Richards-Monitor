SELECT component 
,      current_size/1024/1024 "current size (mb)"
,      min_size/1024/1024 "min size (mb)" 
,      max_size/1024/1024 "max size (mb)" 
,      user_specified_size/1024/1024 "user specified size (mb)"
,      oper_count
,      last_oper_type
,      last_oper_mode
,      last_oper_time "last oper time"
,      granule_size
FROM gv$sga_dynamic_components s