SELECT * 
FROM (SELECT  /*+ no_merge(s) no_merge(a) */
             s.stat_name "CPU Time Breakdown"
      ,      s.stat_val/100 "Time (s)"
      ,      ROUND(((s.stat_val / a.stat_val) *100), 2) "Percentage" 
      FROM (SELECT b.stat_name
            ,      ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('CPU used by this session', 'parse time cpu', 'recursive cpu usage') 
            ORDER BY stat_name ) s
      ,    (SELECT ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('CPU used by this session')) a 
      UNION ALL 
      SELECT /*+ no_merge(x) no_merge(y) no_merge(z) no_merge(a) */
             'CPU Other' stat_name
      ,      (x.stat_val - y.stat_val - z.stat_val)/100 "Time (s)"
      ,      ROUND((((x.stat_val - y.stat_val - z.stat_val) / a.stat_val) *100), 2) 
      FROM (SELECT ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('CPU used by this session')) x
      ,    (SELECT ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('parse time cpu')) y
      ,    (SELECT ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('recursive cpu usage')) z
      ,    (SELECT ROUND((e.value - b.value), 2) stat_val 
            FROM dba_hist_sysstat b
            ,    dba_hist_sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.stat_name = e.stat_name 
              AND b.stat_name IN ('CPU used by this session')) a ) 
ORDER BY 3 desc 
/
