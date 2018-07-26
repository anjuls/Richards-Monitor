SELECT s.qcinst_id "Parent Inst_id"
,      s.qcsid "Parent Sid"
,      s.sid "Child Sid"
,      sess.sql_id
,      p.server_name slave
,      sess.username
,      sess.osuser "OS User"
,      p.status
,      TRUNC((NVL(blocks, 0) * par.value) /1024 /1024) "Sort Usage mb" 
FROM gv$px_process p
,    gv$px_session s
,    gv$sort_usage sort
,    gv$session sess
,    gv$parameter par 
WHERE p.sid = s.sid 
  AND s.sid = sess.sid 
  AND sess.saddr = sort.session_addr (+) 
  AND par.name = 'db_block_size'
  AND p.inst_id = s.inst_id 
  AND s.inst_id = sess.inst_id 
  AND sess.inst_id = sort.inst_id (+)
  AND s.inst_id = par.inst_id 
ORDER BY s.qcinst_id
,        s.qcsid
,        s.sid 
,        sess.sql_id
/