select *
from (select to_char(first_time,'dd-mon-yyyy HH24')
      ,      count(*)
      from v$loghist
      where thread# = ?
      group by to_char(first_time,'dd-mon-yyyy HH24')
      order by to_date(to_char(first_time,'dd-mon-yyyy HH24'),'dd-mon-yyyy HH24') desc)
where rownum < 101
/