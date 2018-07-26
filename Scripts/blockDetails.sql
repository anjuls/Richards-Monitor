select owner
,      segment_name
,      segment_type
,      tablespace_name
from dba_extents
where file_id = ?
and   ? between block_id and (block_id  + blocks -1)
/