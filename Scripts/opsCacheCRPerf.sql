select b1.inst_id "Instance Id"
,      b2.value "GCS CR Blocks Received"
,      b1.value "GCS CR Block Receive Time"
,      ((b1.value / b2.value ) * 10) "Avg CR Block Receive Time (ms)"
from gv$sysstat b1
,     gv$sysstat b2
where b1.name = 'global cache cr blocks receive time'
and   b2.name = 'global cache cr blocks received'
and   b1.inst_id = b2.inst_id
/
