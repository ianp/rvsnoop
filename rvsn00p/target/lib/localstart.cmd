@echo off
TITLE RvSnoop_Start_Script
set _cp=C:\temp\rvsn00p\target\lib\rvsn00p.jar
set _cp=%_cp%;c:\tibco\adapter\sdk401\java\Maverick4.jar
set _cp=%_cp%;c:\tibco\im\java\crimson.jar
set _cp=%_cp%;c:\tibco\rvscript\rvscript.jar
set _cp=%_cp%;c:\tibco\tibrv\lib\tibrvj.jar
set _cp=%_cp%;%classpath%

rem start rvsn00p
rem echo Classpath =  %_cp%
java -Xincgc -classpath "%_cp%" rvsn00p.StartRvSnooper %*
echo remove the pause in the start script to get rid of this screen
pause


