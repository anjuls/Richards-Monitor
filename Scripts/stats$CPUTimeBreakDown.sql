SELECT * 
FROM (SELECT s.name "CPU Time Breakdown"
      ,      s.value/100 "Time (s)"
      ,      ROUND(((s.value / a.value) *100), 2) "Percentage" 
      FROM (SELECT b.name
            ,      ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('CPU used by this session', 'parse time cpu', 'recursive cpu usage') 
            ORDER BY name ) s
      ,    (SELECT ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('CPU used by this session')) a 
      UNION ALL 
      SELECT 'CPU Other'name
      ,      (x.value - y.value - z.value) /100
      ,      ROUND((((x.value - y.value - z.value) / a.value) *100), 2) 
      FROM (SELECT ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('CPU used by this session')) x
      ,    (SELECT ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('parse time cpu')) y
      ,    (SELECT ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('recursive cpu usage')) z
      ,    (SELECT ROUND((e.value - b.value), 2) value 
            FROM stats$sysstat b
            ,    stats$sysstat e 
            WHERE b.snap_id = ? 
              AND e.snap_id = ? 
              AND b.dbid = ? 
              AND e.dbid = ? 
              AND b.instance_number = ? 
              AND e.instance_number = ? 
              AND b.name = e.name 
              AND b.name IN ('CPU used by this session')) a ) 
ORDER BY 3 desc 
/