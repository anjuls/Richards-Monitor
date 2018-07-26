SELECT p.snap_id snap
,      TO_NUMBER(p.value) /1024 /1024 "PGA Aggr Target (M)"
,      ROUND(mu.pat /1024 /1024,2) "Auto PGA Target (M)"
,      ROUND(mu.pga_alloc /1024,2) "PGA Memory Allocated (M)"
,      ROUND((mu.pga_used_auto + mu.pga_used_man) /1024 /1024,2) "Work Area PGA Used (M)"
,      ROUND(100 * (mu.pga_used_auto + mu.pga_used_man) / pga_alloc,2) "% of PGA W/A Allocated (M)"
,      ROUND(DECODE(mu.pga_used_auto + mu.pga_used_man, 0, 0, 100 * mu.pga_used_auto / (mu.pga_used_auto + mu.pga_used_man)),2) "% of W/A Auto Tuned"
,      ROUND(DECODE(mu.pga_used_auto + mu.pga_used_man, 0, 0, 100 * mu.pga_used_man / (mu.pga_used_auto + mu.pga_used_man)),2) "% of W/A Manual Controld"
,      mu.glob_mem_bnd /1024 "Global Memory Bound (K)"
FROM (SELECT SUM(CASE 
                 WHEN name = 'total PGA allocated'THEN VALUE
                 ELSE 0 
                 END) pga_alloc
      ,      SUM(CASE 
                 WHEN name = 'total PGA used for auto workareas'THEN VALUE
                 ELSE 0 
                 END) pga_used_auto
      ,      SUM(CASE 
                 WHEN name = 'total PGA used for manual workareas'THEN VALUE
                 ELSE 0 
                 END) pga_used_man
      ,      SUM(CASE 
                 WHEN name = 'global memory bound'THEN VALUE
                 ELSE 0 
                 END) glob_mem_bnd
      ,      SUM(CASE 
                 WHEN name = 'aggregate PGA auto target'THEN VALUE
                 ELSE 0 
                 END) pat 
      FROM dba_hist_pgastat pga 
      WHERE pga.snap_id = ? 
        AND pga.dbid = ? 
        AND pga.instance_number = ? ) mu
,    dba_hist_parameter p 
WHERE p.snap_id = ? 
  AND p.dbid = ? 
  AND p.instance_number = ? 
  AND p.parameter_name = 'pga_aggregate_target'
  AND p.value != '0'
UNION 
SELECT p.snap_id snap
,      TO_NUMBER(p.value) /1024 /1024 "PGA Aggr Target (M)"
,      ROUND(mu.pat /1024 /1024,2) "Auto PGA Target (M)"
,      ROUND(mu.pga_alloc /1024,2) "PGA Memory Allocated (M)"
,      ROUND((mu.pga_used_auto + mu.pga_used_man) /1024 /1024,2) "Work Area PGA Used (M)"
,      ROUND(100 * (mu.pga_used_auto + mu.pga_used_man) / pga_alloc,2) "% of PGA W/A Allocated (M)"
,      ROUND(DECODE(mu.pga_used_auto + mu.pga_used_man, 0, 0, 100 * mu.pga_used_auto / (mu.pga_used_auto + mu.pga_used_man)),2) "% of W/A Auto Tuned"
,      ROUND(DECODE(mu.pga_used_auto + mu.pga_used_man, 0, 0, 100 * mu.pga_used_man / (mu.pga_used_auto + mu.pga_used_man)),2) "% of W/A Manual Controld"
,      mu.glob_mem_bnd /1024 "Global Memory Bound (K)"
FROM (SELECT SUM(CASE 
                 WHEN name = 'total PGA allocated'THEN VALUE
                 ELSE 0 
                 END) pga_alloc
      ,      SUM(CASE 
                 WHEN name = 'total PGA used for auto workareas'THEN VALUE
                 ELSE 0 
                 END) pga_used_auto
      ,      SUM(CASE 
                 WHEN name = 'total PGA used for manual workareas'THEN VALUE
                 ELSE 0 
                 END) pga_used_man
      ,      SUM(CASE 
                 WHEN name = 'global memory bound'THEN VALUE
                 ELSE 0 
                 END) glob_mem_bnd
      ,      SUM(CASE 
                 WHEN name = 'aggregate PGA auto target'THEN VALUE
                 ELSE 0 
                 END) pat 
      FROM dba_hist_pgastat pga 
      WHERE pga.snap_id = ? 
        AND pga.dbid = ? 
        AND pga.instance_number = ? ) mu
,    dba_hist_parameter p 
WHERE p.snap_id = ? 
  AND p.dbid = ? 
  AND p.instance_number = ? 
  AND p.parameter_name = 'pga_aggregate_target'
  AND p.value != '0'
ORDER BY snap 

