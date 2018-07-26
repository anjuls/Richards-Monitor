select e.filename					     "File"
     , round((e.phyrds - b.phyrds)/?,2) "Av Reads/sec"
     , round(decode( e.phyrds - nvl(b.phyrds,0),0, 0, ((e.readtim - nvl(b.readtim,0)) / (e.phyrds  - nvl(b.phyrds,0)))*10),2) "Av Rd(ms)"
  from dba_hist_filestatxs e
     , dba_hist_filestatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and e.filename = ?
   and b.filename(+)        = e.filename
union all
select e.filename                                             "File"
     , round((e.phyrds - b.phyrds)/?,2) "Av Reads/sec"
     , round(decode( e.phyrds - nvl(b.phyrds,0),0, 0, (e.readtim - nvl(b.readtim,0) / e.phyrds  - nvl(b.phyrds,0))*10),2) "Av Rd(ms)"
  from dba_hist_tempstatxs e
     , dba_hist_tempstatxs b
 where b.snap_id(+)         = ?
   and e.snap_id            = ?
   and e.dbid               = ?
   and b.dbid(+)            = e.dbid
   and e.instance_number    = ?
   and b.instance_number(+) = e.instance_number
   and e.filename = ?
   and b.filename(+)        = e.filename
 order by 1
 /
