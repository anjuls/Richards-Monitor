select file#
,      block#
,      class#
,      status
,      sum(xnc)
from v$bh
where xnc > 0
group by file#,block#,class#,status
order by 5 desc
/