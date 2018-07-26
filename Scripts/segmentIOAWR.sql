SELECT logical_reads_delta,
       physical_reads_delta,
       physical_writes_delta,
       physical_reads_direct_delta,
       physical_writes_direct_delta,
       table_scans_delta
FROM dba_hist_seg_stat
WHERE dbid = ?
  AND instance_number = ?
  AND snap_id = ?
  AND obj# = ?
/
