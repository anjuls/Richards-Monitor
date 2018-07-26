SELECT decode(id,0,'',
lpad(' ',2*(level-1))||level||'.'||position)||' '||
operation||' '||options||' '||other_tag||' '||
object_name||' '||object_type||' '||
decode(id,0,'Cost = '||position||' Optimizer = '||optimizer) QUERY_PLAN
FROM plan_table
connect by prior id = parent_id
and statement_id = ?
start with id = 0 and statement_id = ?
/
