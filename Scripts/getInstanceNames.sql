SELECT d.dbid
,      d.instance_number
,      d.instance_name
,      s.snap_time
FROM stats$database_instance d
,    stats$snapshot s
WHERE d.dbid = s.dbid
  AND d.instance_number = s.instance_number
  AND s.snap_time = (SELECT MAX(snap_time)
                     FROM stats$snapshot
                     WHERE dbid = s.dbid
                       AND instance_number = s.instance_number )
  AND d.startup_time = (SELECT MAX(startup_time)
                        FROM stats$database_instance
                        WHERE dbid = d.dbid
                          AND instance_number = d.instance_number )
ORDER BY d.instance_number