#!/bin/ksh
java -classpath rvsn00p.jar:/usr/local/tibco/tibrv/lib/tibrvj.jar:/usr/local/tibco/adapter/sdk/java/Maverick4.jar:/usr/local/tibco/im/java/crimson.jar:$CLASSPATH -Dfile.encoding=UTF8 rvsn00p.StartRvSnooper $* &

