SELECT execution_name, execution_start, execution_end, status, execution_name

FROM dba_advisor_executions

WHERE task_name = ?

ORDER BY 2

/
