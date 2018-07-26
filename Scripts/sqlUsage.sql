SELECT 'Single Use' "Usage"
,      ROUND(SUM(sharable_mem) /1024 /1024, 2) "Mb"
,      COUNT(*) "# of Statements" 
FROM gv$sqlarea 
WHERE executions <= 1 
  AND inst_id = ? 
UNION ALL 
SELECT 'Multiple Use' "Usage"
,      ROUND(SUM(sharable_mem) /1024 /1024, 2) "mb"
,      COUNT(*) "# of Statements" 
FROM gv$sqlarea 
WHERE executions > 1 
  AND inst_id = ? 
/