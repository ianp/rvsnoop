@echo off
TITLE RvSnoop_Start_Script
set _cp=C:\temp\rvsn00p\target\lib\rvsn00p.jar
set _cp=%_cp%;c:\tibco\adapter\sdk\java\Maverick4.jar
set _cp=%_cp%;c:\tibco\im\java\crimson.jar
rem set _cp=%_cp%;c:\tibco\rvscript\rvscript.jar
set _cp=%_cp%;c:\tibco\tibrv\lib\tibrvj.jar
set _cp=%_cp%;%classpath%

set l1="tcp:7500|7500||>"
set alllisteners=%l1%

rem start rvsn00p
rem echo Classpath =  %_cp%

rem to change the encoding character set change the -Dfile.encoding parameters
rem see http://java.sun.com/j2se/1.3/docs/guide/intl/encoding.doc.html
java -Xincgc -Dfile.encoding=UTF8 -classpath "%_cp%" rvsn00p.StartRvSnooper %l1% %*
echo remove the pause in the start script to get rid of this screen
pause

