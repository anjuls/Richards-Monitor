select name,trunc(bytes/1024/1024)
from v$sgastat
where pool='shared pool'
and name in
('library cache',
 'miscellaneous',
 'sql area',
 'free memory')
union all
select 'open cursors',count(*)
from v$open_cursor
union all
select 'average cursors per session',count(*)/count(distinct sid)
from v$open_cursor
union all
select 'sessions', count(*)
from v$session
order by 1
/