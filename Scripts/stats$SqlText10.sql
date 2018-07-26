SELECT sql_text 
FROM stats$sqltext 
WHERE old_hash_value = ? 
ORDER BY piece 