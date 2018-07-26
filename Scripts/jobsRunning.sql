SELECT sid
,      job
,      failures
,      last_date "Last Success"
,      this_date "Date Started" 
FROM dba_jobs_running j
