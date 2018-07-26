SELECT status 
FROM DBA_ADVISOR_TASKS 
WHERE lower(task_name) like lower(?)
/
