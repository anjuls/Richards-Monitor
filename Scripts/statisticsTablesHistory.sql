SELECT ob.owner
,      ob.object_name
,      ob.subobject_name
,      ob.object_type
,      obj#
,      to_date(to_char(savtime,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "Save Time"
,      rowcnt
,      blkcnt
,      avgrln
,      samplesize
,      analyzetime
,      cachedblk
,      cachehit
,      logicalread 
FROM sys.wri$_optstat_tab_history
,    dba_objects ob 
WHERE owner like UPPER(?) 
  AND object_name like UPPER(?) 
  AND object_type IN ('TABLE') 
  AND object_id = obj# 
/