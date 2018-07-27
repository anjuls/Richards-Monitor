select child#
from (select l.child#
      from v$latch_children l
      ,    v$latchname ln
      where ln.name = 'library cache'
      and   l.latch# = ln.latch#
      order by l.sleeps desc)
where rownum = 1
/