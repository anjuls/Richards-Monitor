SELECT DISTINCT owner
FROM dba_hist_seg_stat_obj
WHERE dbid = ?
  AND owner not in ('** MISSING **')
ORDER BY 1
