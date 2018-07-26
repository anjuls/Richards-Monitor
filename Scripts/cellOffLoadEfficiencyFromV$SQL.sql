SELECT executions
,      round((elapsed_time /1000000) /decode (NVL(executions, 0), 0, 1, executions) / DECODE(px_servers_executions, 0, 1, px_servers_executions /decode (NVL(executions, 0), 0, 1, executions)),2) "Avg Exec Time"
,      round(px_servers_executions /decode (NVL(executions, 0), 0, 1, executions),2) "AVG # PQ Slaves"
,      DECODE(io_cell_offload_eligible_bytes, 0, 0, 100 * (io_cell_offload_eligible_bytes -io_interconnect_bytes) /decode (io_cell_offload_eligible_bytes, 0, 1, io_cell_offload_eligible_bytes)) "IO SAVED % (SMART SCAN)"
FROM gv$sql s 
WHERE s.inst_id = ?
  and sql_id = ? 
  AND child_number = ? 
/