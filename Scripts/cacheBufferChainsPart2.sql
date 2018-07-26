select x.dbarfil "File"
,      x.dbablk  "Block"
from (select b.dbarfil
      ,      b.dbablk
      from x$bh b
      ,    v$latch_children l
      where l.child# = ?
      and   b.hladdr = l.addr
      order by b.tch desc) x
where rownum < 11
/