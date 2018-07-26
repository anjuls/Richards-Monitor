select * 
from dba_sequences
where sequence_owner = ?
and sequence_name like ?
/