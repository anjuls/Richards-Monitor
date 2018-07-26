SELECT resource_name
,      MAX(max_utilization) "max_utilization"
,      initial_allocation
,      limit_value 
FROM stats$resource_limit 
WHERE snap_id BETWEEN ? AND ? 
  AND dbid = ? 
GROUP BY resource_name
,        initial_allocation
,        limit_value 
ORDER BY 1 
