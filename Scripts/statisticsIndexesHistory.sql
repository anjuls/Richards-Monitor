SELECT ob.owner
,      ob.object_name
,      ob.subobject_name
,      ob.object_type
,      obj#
,      to_date(to_char(savtime,'dd-mon-yyyy hh24:mi:ss'),'dd-mon-yyyy hh24:mi:ss') "Save Time"
,      rowcnt
,      blevel
,      distkey
,      lblkkey
,      dblkkey
,      clufac
,      samplesize
,      analyzetime
,      guessq
,      cachedblk
,      cachehit
,      logicalread 
FROM sys.wri$_optstat_ind_history
,    dba_objects ob 
WHERE owner like UPPER(?) 
  AND object_name like UPPER(?) 
  AND object_type IN ('INDEX') 
  AND object_id = obj# 
/