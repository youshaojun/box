#! /bin/sh

# 清理cache buffer
# 可能造成数据丢失, 谨慎使用
free_cache_dir=/data/logs/free_cache_logs/free_cache-$(date "+%Y-%m-%d").log
used=`free -m | awk 'NR==2' | awk '{print $3}'`  
free=`free -m | awk 'NR==2' | awk '{print $4}'`
min_memory=1024
echo $(date "+%Y-%m-%d %H:%M:%S") ": memory usage before | [use：${used}MB][free：${free}MB]" >> $free_cache_dir
if [ $free -le $min_memory ] ; then
	echo $(date "+%Y-%m-%d %H:%M:%S") ": memory is less than "$min_memory", now start freeing cache......" >> $free_cache_dir
	sync && echo 1 > /proc/sys/vm/drop_caches  
        sync && echo 2 > /proc/sys/vm/drop_caches
       	sync && echo 3 > /proc/sys/vm/drop_caches  
	used_ok=`free -m | awk 'NR==2' | awk '{print $3}'`  
	free_ok=`free -m | awk 'NR==2' | awk '{print $4}'`  
	echo $(date "+%Y-%m-%d %H:%M:%S") ": memory usage after | [use：${used_ok}MB][free：${free_ok}MB]" >>  $free_cache_dir  
	echo $(date "+%Y-%m-%d %H:%M:%S") ": free cache over! " >> $free_cache_dir
fi  
exit 1 
