select owner
,      table_name
,      last_analyzed "Last Analyzed"
,      partitioned
,      sample_size
,      num_rows
,      blocks
,      empty_blocks
,      avg_space
,      chain_cnt
,      avg_row_len
,      avg_space_freelist_blocks
,      num_freelist_blocks
from dba_tables
where owner like upper(?)
order by 1,2
