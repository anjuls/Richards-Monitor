SELECT n.owner
,      n.tablespace_name
,      n.object_name
,      CASE 
         WHEN LENGTH(n.subobject_name) < 11 THEN n.subobject_name 
         ELSE SUBSTR(n.subobject_name, LENGTH(n.subobject_name) -9) 
       END subobject_name
,      n.object_type
,      r.physical_reads
,      SUBSTR(TO_CHAR(r.ratio * 100, '999.9MI'), 1, 5) ratio 
FROM stats$seg_stat_obj n
,    (SELECT * 
      FROM (SELECT e.dataobj#
            ,      e.obj#
            ,      e.ts#
            ,      e.dbid
            ,      e.physical_reads - NVL(b.physical_reads, 0) physical_reads
            ,      ratio_to_report (e.physical_reads - NVL(b.physical_reads, 0)) over () ratio 
            FROM stats$seg_stat e
            ,    stats$seg_stat b 
            WHERE b.snap_id (+) = ? 
              AND e.snap_id = ? 
              AND b.dbid (+) = ? 
              AND e.dbid = ? 
              AND b.instance_number (+) = ? 
              AND e.instance_number = ? 
              AND b.ts# (+) = e.ts# 
              AND b.obj# (+) = e.obj# 
              AND b.dataobj# (+) = e.dataobj# 
              AND e.physical_reads - NVL(b.physical_reads, 0) > 0 
            ORDER BY physical_reads desc ) d 
      WHERE rownum <= 50 ) r 
WHERE n.dataobj# = r.dataobj# 
  AND n.obj# = r.obj# 
  AND n.ts# = r.ts# 
  AND n.dbid = r.dbid 
ORDER BY physical_reads desc 
