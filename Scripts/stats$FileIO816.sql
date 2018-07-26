SELECT e.tsname
,      e.filename
,      e.phyrds - NVL(b.phyrds, 0) reads
,      TO_CHAR(ROUND((e.phyrds - NVL(b.phyrds, 0)) /?, 2), '999,999,990.99' ) "Av|Reads/s"
,      TO_CHAR(ROUND(DECODE((e.phyrds - NVL(b.phyrds, 0)), 0, TO_NUMBER(null), ((e.readtim - NVL(b.readtim, 0)) / (e.phyrds - NVL(b.phyrds, 0))) *10), 2), '999,999,990.99' ) "Av|Rd|(ms)"
,      TO_CHAR(ROUND(DECODE((e.phyrds - NVL(b.phyrds, 0)), 0, TO_NUMBER(null), (e.phyblkrd - NVL(b.phyblkrd, 0)) / (e.phyrds - NVL(b.phyrds, 0))), 2), '999,999,990.99' ) "Av|Blks/Rd"
,      e.phywrts - NVL(b.phywrts, 0) writes
,      TO_CHAR(ROUND((e.phywrts - NVL(b.phywrts, 0)) /?, 2), '999,999,990.99' ) "Av|Writes/s"
,      e.wait_count - NVL(b.wait_count, 0) waits
,      TO_CHAR(ROUND(DECODE((e.wait_count - NVL(b.wait_count, 0)), 0, TO_NUMBER(null), ((e.time - NVL(b.time, 0)) / (e.wait_count - NVL(b.wait_count, 0))) *10), 2), '999,999,990.99' ) "Av|BufWt|(ms)" 
FROM stats$filestatxs e
,    stats$filestatxs b 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = ? 
  AND e.instance_number = ? 
  AND b.instance_number (+) = e.instance_number 
  AND b.tsname (+) = e.tsname 
  AND b.filename (+) = e.filename 
  AND ((e.phyrds - NVL(b.phyrds, 0)) + (e.phywrts - NVL(b.phywrts, 0))) > 0 
ORDER BY tsname
,        filename 