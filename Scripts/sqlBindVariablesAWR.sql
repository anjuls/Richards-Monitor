SELECT bhs.snap_id
,      to_char(hs.begin_interval_time,'dd-mon-yyyy hh24:mi:dd') "interval start time"
,      bhs.name
,      bhs.position
,      bhs.dup_position
,      bhs.datatype_string "data type"
,      bhs.was_captured
,      bhs.last_captured captured
,      bhs.value_string VALUE
FROM dba_hist_sqlbind bhs
,    dba_hist_snapshot hs 
WHERE bhs.dbid = ? 
  AND bhs.instance_number = ? 
  AND bhs.sql_id = ? 
  AND bhs.snap_id = hs.snap_id
  AND bhs.instance_number = hs.instance_number
ORDER BY snap_id desc,position
/