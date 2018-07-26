select substr(version,0,instr(version,'.'))||substr(version,instr(version,'.')+1,1)||substr(version,instr(version,'.')+3,1)
from v$instance
/