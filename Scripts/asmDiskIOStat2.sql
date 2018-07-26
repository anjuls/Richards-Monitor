SELECT SUM(reads) reads
,      SUM(writes) writes
,      SUM(read_time) readTime
,      SUM(write_time) writeTime
,      SUM(bytes_read) bytesRead
,      SUM(bytes_written) bytesWritten
FROM gv$asm_disk_iostat adio
WHERE inst_id = ?
/

