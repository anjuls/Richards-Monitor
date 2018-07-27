select owner
,      segment_name "Segment Name"
,      block_id
,      sum(bytes)/1024 kb
,      sum(bytes)/1024/1024 mb
from dba_extents
where tablespace_name = ?
group by owner,segment_name,block_id
order by block_id
/