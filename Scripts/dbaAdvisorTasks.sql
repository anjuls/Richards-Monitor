SELECT * 
FROM dba_advisor_tasks 
WHERE lower(task_name) like lower(?)
/
