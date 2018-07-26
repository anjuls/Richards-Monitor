SELECT sql_handle
,      plan_name
,      origin
,      module
,      created
,      accepted
,      last_verified
,      last_executed
,      sql_text 
FROM dba_sql_plan_baselines o 
WHERE last_executed is null
/
