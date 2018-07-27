SELECT usn
,      slt
,      seq
,      state
,      undoblocksdone
,      undoblockstotal
,      pid
,      cputime
,      parentusn
,      parentslt
,      parentseq
,      xid
,      pxid
,      rcvservers 
FROM gv$fast_start_transactions f 
/