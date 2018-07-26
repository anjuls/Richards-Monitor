select e.tsname					     "Tablespace"
     , sum (e.phyrds - nvl(b.phyrds,0))                     reads
     , to_char(round(sum (e.phyrds - nvl(b.phyrds,0))/?,2),'999,999,990.99')                "Av Reads/sec"
     , to_char(round(decode( sum(e.phyrds - nvl(b.phyrds,0))
             , 0, 0
             , (sum(e.readtim - nvl(b.readtim,0)) /
                sum(e.phyrds  - nvl(b.phyrds,0)))*10),2),'999,999,990.99')       "Av Rd(ms)"
     , to_char(round(decode( sum(e.phyrds - nvl(b.phyrds,0))
             , 0, to_number(NULL)
             , sum(e.phyblkrd - nvl(b.phyblkrd,0)) /
               sum(e.phyrds   - nvl(b.phyrds,0)) ),2),'999,999,990.99')          "Av Blks per Rd"
     , sum (e.phywrts    - nvl(b.phywrts,0))                writes
     , to_char(round(sum (e.phywrts    - nvl(b.phywrts,0))/?,2),'999,999,990.99')           "Av Writes/s"
     , sum (e.wait_count - nvl(b.wait_count,0))             "Buffer Waits"
     , to_char(round(decode (sum(e.wait_count - nvl(b.wait_count, 0))
            , 0, 0
            , (sum(e.time       - nvl(b.time,0)) /
               sum(e.wait_count - nvl(b.wait_count,0)))*10),2),'999,999,990.99') "Av Buf|Wt(ms)"
  from stats$filestatxs e
     , stats$filestatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and b.dbid(+)            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and b.instance_number(+) = ?
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and b.tsname(+)          = e.tsname
   and b.filename(+)        = e.filename
   and ( (e.phyrds  - nvl(b.phyrds,0)  )  +
         (e.phywrts - nvl(b.phywrts,0) ) ) > 0
 group by e.tsname
union all
select e.tsname                                             "Tablespace"
     , sum (e.phyrds - nvl(b.phyrds,0))                     reads
     , to_char(round(sum (e.phyrds - nvl(b.phyrds,0))/?,2),'999,999,990.99')                "Av Reads/sec"
     , to_char(round(decode( sum(e.phyrds - nvl(b.phyrds,0))
             , 0, 0
             , (sum(e.readtim - nvl(b.readtim,0)) /
                sum(e.phyrds  - nvl(b.phyrds,0)))*10),2),'999,999,990.99')       "Av Rd(ms)"
     , to_char(round(decode( sum(e.phyrds - nvl(b.phyrds,0))
             , 0, to_number(NULL)
             , sum(e.phyblkrd - nvl(b.phyblkrd,0)) /
               sum(e.phyrds   - nvl(b.phyrds,0)) ),2),'999,999,990.99')          "Av Blks per Rd"
     , sum (e.phywrts    - nvl(b.phywrts,0))                writes
     , to_char(round(sum (e.phywrts    - nvl(b.phywrts,0))/?,2),'999,999,990.99')           "Av Writes/s"
     , sum (e.wait_count - nvl(b.wait_count,0))             "Buffer Waits"
     , to_char(round(decode (sum(e.wait_count - nvl(b.wait_count, 0))
            , 0, 0
            , (sum(e.time       - nvl(b.time,0)) /
               sum(e.wait_count - nvl(b.wait_count,0)))*10),2),'999,999,990.99') "Av Buf|Wt(ms)"
  from stats$tempstatxs e
     , stats$tempstatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and b.dbid(+)            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and b.instance_number(+) = ?
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and b.tsname(+)          = e.tsname
   and b.filename(+)        = e.filename
   and ( (e.phyrds  - nvl(b.phyrds,0)  )  +
         (e.phywrts - nvl(b.phywrts,0) ) ) > 0
 group by e.tsname
 order by 4   desc
 /
