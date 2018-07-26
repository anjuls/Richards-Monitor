SELECT object_name
FROM dba_hist_seg_stat_obj
WHERE dbid = ?
  AND owner =?
ORDER BY 1
