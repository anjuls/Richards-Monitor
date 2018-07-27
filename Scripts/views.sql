select vName
from (select decode(view_name,table_name,synonym_name,view_name) vName
      from all_views v,
           all_synonyms s 
      where v.view_name = s.table_name (+)) 
where (   vName like 'DBA_%'
       or vName like 'V$%')
  and vName not like 'DBA$%'
order by 1
/
