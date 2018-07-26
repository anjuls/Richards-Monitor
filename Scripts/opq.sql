
SELECT 'PQ QC Process' "Process Type"
,      COUNT(*) 
FROM gv$px_session 
WHERE sid = qcsid 
  AND inst_id = ?
GROUP BY 'PQ QC Process'
UNION ALL 
SELECT 'PQ Slave Process from inst ' || qcinst_id "Process Type"
,      COUNT(*) 
FROM gv$px_session 
WHERE sid != qcsid 
  AND inst_id = ?
GROUP BY 'PQ Slave Process from inst '|| qcinst_id 
ORDER BY 1
/