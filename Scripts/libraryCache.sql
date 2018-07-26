SELECT namespace
,      gets
,      gethits
,      TO_NUMBER(TO_CHAR(gethitratio, '990.90')) "Get Hit Ratio"
,      pins
,      pinhits
,      TO_NUMBER(TO_CHAR(pinhitratio, '990.90')) "Pin Hit Ratio"
,      reloads
,      invalidations
,      dlm_invalidations
,      dlm_invalidation_requests
,      dlm_lock_requests
,      dlm_pin_releases
,      dlm_pin_requests
FROM gv$librarycache l