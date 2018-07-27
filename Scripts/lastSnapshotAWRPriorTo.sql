select to_char(end_interval_time,'dd/mm/yy hh24:mi:ss')
from dba_hist_snapshot
where end_interval_time = (select min(end_interval_time)
                   from dba_hist_snapshot
                   where end_interval_time > to_date(?,'dd/mm/yy hh24:mi:ss') - ?)
/