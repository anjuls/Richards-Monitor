SELECT SPACE_USED_TOTAL,
       SPACE_ALLOCATED_TOTAL
FROM dba_hist_seg_stat
WHERE dbid = ?
  AND instance_number = ?
  AND snap_id = ?
  AND obj# = ?
/
