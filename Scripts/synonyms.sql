select *
from dba_synonyms s
where s.synonym_name like upper( ? )
/