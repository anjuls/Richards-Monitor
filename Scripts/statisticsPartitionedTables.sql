select table_owner
,      table_name
,      partition_name
,      last_analyzed "Last Analyzed"
,      sample_size
,      num_rows
,      blocks
,      empty_blocks
,      avg_space
,      chain_cnt
,      avg_row_len
from dba_tab_partitions
where table_owner like upper(?)
order by 1,2,3
