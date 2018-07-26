select owner, table_name, chain_cnt
from dba_tables
where chain_cnt > 0
order by 3
/