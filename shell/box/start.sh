#!/bin/bash

# java项目启动脚本
PACKAGE="xx.jar"
APP_NAME="xx-manage:8081"
JAVA_OPTS="java -noverify -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xms75m -Xmx75m -Xss512k -XX:+UseG1GC -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:/home/work/gc.log -verbose:gc -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/work/gc/heapdump.hprof -jar "${PACKAGE}
   
start(){
 echo '>>>>>>>>>>Hello.'
 ps -ef| grep ${APP_NAME}| grep -v "grep" |awk '{print $2}'|while read pid
 do
    echo '>>>>>>>>>>'${APP_NAME}' SERVER has bean started ,run processing PID:'${pid}
    kill 0      
 done
 echo '>>>>>>>>>>'${APP_NAME}' SERVER began to start.'
 process
 exec nohup ${JAVA_OPTS} ${APP_NAME} >/dev/null 2>&1 &
 # exec ${JAVA_OPTS} ${APP_NAME}
 echo '>>>>>>>>>>'${APP_NAME}' SERVER has bean started.'
 ps -ef| grep ${APP_NAME}| grep -v "grep" |awk '{print $2}'|while read pid
 do
    echo '>>>>>>>>>>Run Processing PID:'${pid}
 done

}  
 
stop(){  
 ps -ef| grep ${PACKAGE}| grep -v "grep" |awk '{print $2}'|while read pid
 do  
    echo '>>>>>>>>>>Run Processing PID:'${pid}
    kill -9 ${pid}
 done
 echo '>>>>>>>>>>'${APP_NAME}' SERVER began to close.'
 echo '>>>>>>>>>>'${APP_NAME}' SERVER has been closed.'
 echo '>>>>>>>>>>Bye-bye......'  
}

status(){
  pid=`ps -ef| grep ${APP_NAME}| grep -v "grep" |awk '{print $2}'`
  if [[ "$pid" != "" ]] ; then
    echo '>>>>>>>>>>'${APP_NAME}' SERVER is running,Run Processing PID:'${pid}
  else
    echo '>>>>>>>>>>'${APP_NAME}' SERVER is not run.'
  fi
} 

process(){
	b=''
	i=0
	while [ $i -le  100 ]
	do
	    printf "progress:[%-50s]%d%%\r" ${b} ${i}
	    sleep 0.1
	    i=`expr 2 + ${i}`
	    b=#$b
	done
	echo	
}
 
case "$1" in  
start)  
start $3
;;  
stop)  
stop  
;;    
restart)  
stop  
start $3  
;;
status)
status
;;  
*)  
printf 'Usage: %s {start|stop|restart|status}\n' "$prog"  
exit 1  
;;  
esac
