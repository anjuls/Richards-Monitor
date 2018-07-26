select e.value - b.value cpu
from dba_hist_sys_time_model b
,    dba_hist_sys_time_model e
where b.stat_name = ?
  and b.stat_name = e.stat_name
  and b.snap_id = ?
  and e.snap_id = ?
  and b.dbid = ?
  and b.dbid = e.dbid
  and b.instance_number = e.instance_number
  and b.instance_number = ?
 /
