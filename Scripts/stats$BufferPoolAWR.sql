SELECT e.name "Pool"
,      LPAD(CASE 
            WHEN e.set_msize <= 9999 THEN TO_CHAR(e.set_msize) || ' '
            WHEN TRUNC((e.set_msize) /1000) <= 9999 THEN TO_CHAR(TRUNC((e.set_msize) /1000)) || 'K'
            WHEN TRUNC((e.set_msize) /1000000) <= 9999 THEN TO_CHAR(TRUNC((e.set_msize) /1000000)) || 'M'
            WHEN TRUNC((e.set_msize) /1000000000) <= 9999 THEN TO_CHAR(TRUNC((e.set_msize) /1000000000)) || 'G'
            WHEN TRUNC((e.set_msize) /1000000000000) <= 9999 THEN TO_CHAR(TRUNC((e.set_msize) /1000000000000)) || 'T'
            ELSE SUBSTR(TO_CHAR(TRUNC((e.set_msize) /1000000000000000)) || 'P', 1, 5) 
            END, 7, ' ' ) "Buffers"
,      TO_CHAR(DECODE(e.db_block_gets - NVL(b.db_block_gets, 0) + e.consistent_gets - NVL(b.consistent_gets, 0), 0, TO_NUMBER(null), (100 * (1 - ((e.physical_reads - NVL(b.physical_reads, 0)) / (e.db_block_gets - NVL(b.db_block_gets, 0) + e.consistent_gets - NVL(b.consistent_gets, 0)))))), '990.99' ) "Pool Hit%"
,      e.db_block_gets - NVL(b.db_block_gets, 0) + e.consistent_gets - NVL(b.consistent_gets, 0) "Buffer Gets"
,      e.physical_reads - NVL(b.physical_reads, 0) "Physical Reads"
,      e.physical_writes - NVL(b.physical_writes, 0) "Physical Writes"
,      e.free_buffer_wait - NVL(b.free_buffer_wait, 0) "Free Buffer Waits"
,      e.write_complete_wait - NVL(b.write_complete_wait, 0) "Write Complete Wait"
,      e.buffer_busy_wait - NVL(b.buffer_busy_wait, 0) "Buffer Busy Wait" 
FROM dba_hist_buffer_pool_stat b
,    dba_hist_buffer_pool_stat e
,    (SELECT VALUE
      FROM v$parameter 
      WHERE name = 'db_block_size') p 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = ? 
  AND e.instance_number = ? 
  AND b.instance_number (+) = e.instance_number 
  AND b.id (+) = e.id 
ORDER BY e.name 


