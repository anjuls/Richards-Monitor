SELECT origin
,      enabled
,      accepted
,      (case when last_verified is null then 'NON VERIFIED'
            else 'VERIFIED' end) Verified
,      COUNT(*) 
FROM dba_sql_plan_baselines 
GROUP BY origin
,      enabled
,      accepted
,      (case when last_verified is null then 'NON VERIFIED'
            else 'VERIFIED' end)
/
