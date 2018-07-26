SELECT resource_name
,      current_utilization
,      max_utilization
,      initial_allocation
,      limit_value 
FROM gv$resource_limit r
/