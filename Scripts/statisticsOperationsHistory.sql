SELECT operation
,      target
,      to_date(to_char(start_time,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "Start Time"
,      to_date(to_char(end_time,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "End Time"
,      flags
FROM sys.wri$_optstat_opr
/