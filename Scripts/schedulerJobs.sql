SELECT owner
,      job_name
,      program_name
,      job_type
,      state
,      run_count
,      max_runs
,      failure_count
,      to_char(last_start_date,'dd-mon-yyyy hh24:mi:ss') "last start date"
,      to_char(next_run_date,'dd-mon-yyyy hh24:mi:ss') "next run date"
,      system 
FROM dba_scheduler_jobs 