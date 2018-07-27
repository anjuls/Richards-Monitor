SELECT resource_name
,      current_utilization
,      max_utilization
,      initial_allocation
,      limit_value 
FROM gv$resource_limit r
,    v$containers c
WHERE r.con_id = c.con_id
ORDER BY c.con_id
/