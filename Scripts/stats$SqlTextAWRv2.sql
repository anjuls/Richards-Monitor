SELECT dbms_lob.substr(sql_text, 0, 20000) || dbms_lob.substr(sql_text, 20000, 20000) 
FROM dba_hist_sqltext 
WHERE sql_id = ? 
  AND dbid = ? 