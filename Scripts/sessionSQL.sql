select sql_text
from   gv$sqltext_with_newlines 
where hash_value = ?
  and address = ?
  and inst_id = ?
order by piece
/