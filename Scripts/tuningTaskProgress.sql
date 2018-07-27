SELECT ap.* 
FROM V$ADVISOR_PROGRESS ap
,    dba_advisor_tasks at
WHERE at.owner = ?
AND   lower(at.task_name) = lower(?)
AND   at.task_id = ap.task_id
/