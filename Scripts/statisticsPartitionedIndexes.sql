select index_owner
,      index_name
,      last_analyzed "Last Analyzed"
,      sample_size
,      num_rows
,      blevel
,      distinct_keys
,      leaf_blocks
,      avg_leaf_blocks_per_key
,      avg_data_blocks_per_key
from dba_ind_partitions
where index_owner like upper(?)
order by 1,2