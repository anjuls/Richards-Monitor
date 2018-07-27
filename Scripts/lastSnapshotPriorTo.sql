select to_char(snap_time,'dd/mm/yy hh24:mi:ss')
from stats$snapshot
where snap_time = (select min(snap_time)
                   from stats$snapshot
                   where snap_time > to_date(?,'dd/mm/yy hh24:mi:ss') - ?)
/