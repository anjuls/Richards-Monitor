select e.tsname	"Tablespace"
     , round(sum (e.phyrds - b.phyrds)/?,2) "Av Reads/sec"
     , round(decode( sum(e.phyrds - nvl(b.phyrds,0)),0, 0, (sum(e.readtim - nvl(b.readtim,0)) / sum(e.phyrds  - nvl(b.phyrds,0)))*10),2) "Av Rd(ms)"
  from dba_hist_filestatxs e
     , dba_hist_filestatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and e.tsname = ?
   and b.tsname(+)        = e.tsname
 group by e.tsname
union all
select e.tsname "Tablespace"
     , round(sum (e.phyrds - b.phyrds)/?,2) "Av Reads/sec"
     , round(decode( sum(e.phyrds - nvl(b.phyrds,0)), 0, 0, (sum(e.readtim - nvl(b.readtim,0)) / sum(e.phyrds  - nvl(b.phyrds,0)))*10),2) "Av Rd(ms)"
  from dba_hist_tempstatxs e
     , dba_hist_tempstatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and e.tsname = ?   
   and b.tsname(+)        = e.tsname
 group by e.tsname
 order by 1
 /
