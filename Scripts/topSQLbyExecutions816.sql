SELECT execs "executions"
,      reads "disk reads"
,      ROUND(rpx, 2) "disk reads per execution"
,      ROUND(rel_pct, 2) "% of total execution"
,      hashval "hash value"
,      TRIM(sql_text) 
FROM (SELECT e.disk_reads - NVL(b.disk_reads, 0) reads
      ,      e.executions - NVL(b.executions, 0) execs
      ,      DECODE((e.executions - NVL(b.executions, 0)), 0, TO_NUMBER(null), (e.disk_reads - NVL(b.disk_reads, 0)) / (e.executions - NVL(b.executions, 0))) rpx
      ,      100* (e.executions - NVL(b.executions, 0)) /? rel_pct
      ,      e.hash_value hashval
      ,      e.sql_text sql_text 
      FROM stats$sql_summary b
      ,    stats$sql_summary e 
      WHERE b.snap_id (+) = ? 
        AND e.snap_id = ? 
        AND b.dbid (+) = ? 
        AND e.dbid = ? 
        AND b.instance_number (+) = ? 
        AND e.instance_number = ? 
        AND b.hash_value (+) = e.hash_value 
        AND b.address (+) = e.address 
        AND e.executions > NVL(b.executions, 0) 
      ORDER BY execs desc ) 
WHERE rownum <= 40;
