select *
from dba_synonyms
where owner = ?
and synonym_name = ?
/