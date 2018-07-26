select sql_text
from   gv$sqltext_with_newlines 
where sql_id = ?
and inst_id = ?
order by piece
/