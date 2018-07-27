select b1.inst_id
,      (b1.value + b2.value) "GLOBAL LOCK GETS"
,      b3.value "GLOBAL LOCK GET TIME"
,      to_char(round((b3.value / (b1.value + b2.value) * 10),2),'999,999,990.99') "AVG GLOBAL LOCK GET TIME (ms)"
from gv$sysstat b1
,    gv$sysstat b2
,    gv$sysstat b3
where b1.name = 'global lock sync gets'
and   b2.name = 'global lock async gets'
and   b3.name = 'global lock get time'
and b1.inst_id = b2.inst_id
and b2.inst_id = b3.inst_id
/
