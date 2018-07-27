select name,round(bytes/1024/1024,1)
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
select 'free memory (reserved pool)',round(sum(ksmchsiz)/1024/1024,1)
from sys.x$ksmspr
where inst_id = userenv('Instance')
and   ksmchcom = 'free memory' 
union all
select 'sessions', count(*)
from v$session
order by 1
/