select sql_text
FROM (SELECT unique piece
      ,      sql_text 
      FROM gv$sqltext_with_newlines s 
      WHERE sql_id = ?
      ORDER BY piece ) 
/