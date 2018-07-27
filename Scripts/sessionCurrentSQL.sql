select sql_text "Session Current SQL"
from   v$sqltext_with_newlines 
where hash_value = ?
  and address = ?
order by piece
/