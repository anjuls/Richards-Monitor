select e.value - b.value cpu
from stats$sys_time_model b
,    stats$sys_time_model e
,    stats$time_model_statname n
where n.stat_name = ?
  and b.stat_id = n.stat_id
  and e.stat_id = n.stat_id
  and b.snap_id = ?
  and e.snap_id = ?
  and b.dbid = ?
  and b.dbid = e.dbid
  and b.instance_number = e.instance_number
  and b.instance_number = ?
 /