SELECT component 
,      oper_type 
,      oper_mode 
,      parameter 
,      round(initial_size/1024/1024,2) "initial (mb)" 
,      round(target_size/1024/1024,2) "target (mb)" 
,      round(final_size/1024/1024,2) "final (mb)"
,      status
,      start_time "start time"
,      end_time "end time" 
FROM gv$sga_resize_ops s
ORDER BY start_time desc