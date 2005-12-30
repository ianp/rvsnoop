#!/bin/bash
# Start script for RvSn00p @version@ on Unix platforms.
# $Id$

export RVSNOOP_HOME=`dirname $0`/..

if [ -z "$TIBCO_HOME" ] ; then
    echo Warning: TIBCO_HOME not set, using /opt/tibco by default.
    export TIBCO_HOME=/opt/tibco
fi

if [ -z "$TIBRV_HOME" ] ; then
    echo Warning: TIBRV_HOME not set, using $TIBCO_HOME/tibrv by default.
    export TIBRV_HOME=$TIBCO_HOME/tibrv
fi

CP="$RVSNOOP_HOME/lib/rvsn00p.jar:$RVSNOOP_HOME/lib/xom-1.1.jar"
CP="$CP:$RVSNOOP_HOME/lib/forms-1.0.5.jar:$TIBRV_HOME/lib/tibrvj.jar"
LP="$TIBRV_HOME/bin:$TIBRV_HOME/lib"

if [ -n "$TIBCO_TRA_HOME" ] ; then
    CP=$CP:$TIBCO_TRA_HOME/lib/TIBCOrt.jar
fi

if [ -n "$TIBSDK_HOME" ] ; then
    CP=$CP:$TIBSDK_HOME/lib/Maverick4.jar
fi

if [ -n "$CLASSPATH" ] ; then
  CP=$CP:$CLASSPATH
fi

export LIBPATH=$LP         # AIX
export SHLIB_PATH=$LP      # HP-UX
export DYLIB_PATH=$LP      # Darwin
export LD_LIBRARY_PATH=$LP # Solaris & Linux

java -Xmx128m \
  -Drvsn00p.home="$RVSNOOP_HOME" \
  -Dfile.encoding=UTF-8 \
  -Djava.library.path="$LP" \
  -classpath "$CP" rvsn00p.StartRvSnooper $* &
