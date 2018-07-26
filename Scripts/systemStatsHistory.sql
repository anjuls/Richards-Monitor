SELECT to_date(to_char(savtime,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "Save Time"
,      sname
,      pname
,      pval1 
FROM sys.wri$_optstat_aux_history 
ORDER by savtime
/
