SELECT DECODE(id, 0, '', LPAD(' ', 2 * (DEPTH -1)) || DEPTH|| '.'|| position) || ' ' || operation || ' ' || options || ' ' || other_tag || ' ' || object_name || ' ' || DECODE(id, 0, 'Cost = '|| position || ' Optimizer = '|| optimizer) query_plan 
FROM gv$sql_plan sp
,    (SELECT MIN(child_number) "CHILD_NUMBER" 
      FROM gv$sql_plan 
      WHERE hash_value = ? 
        AND address = ? 
        AND inst_id = ? ) b 
CONNECT BY prior id = parent_id 
    AND hash_value = ? 
    AND address = ? 
    AND inst_id = ? 
    AND sp.child_number = b.child_number 
START WITH id = 0 
       AND hash_value = ? 
/
