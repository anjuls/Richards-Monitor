SELECT job "Job#"
,      last_date "Last Run"
,      DECODE(this_date, '%', 'Y', '' ) "Running"
,      next_date "Next Run"
,      failures "Failures"
,      broken
,      SUBSTR(what, 1, 70) "What"
,      log_user
,      priv_user
,      schema_user 
FROM dba_jobs 
ORDER BY job 
/