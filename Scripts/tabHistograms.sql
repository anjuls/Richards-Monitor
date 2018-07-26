SELECT column_name
,      endpoint_number
,      endpoint_value
,      endpoint_actual_value
FROM DBA_TAB_HISTOGRAMS
WHERE owner = ?
  AND table_name = ?
/