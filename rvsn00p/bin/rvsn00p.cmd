@echo off
rem Start script for RvSn00p @version@ on Windows platforms.
rem $Id$

title RvSn00p @version@

set RVSNOOP_HOME=%~dp0%..

if "no%TIBCO_HOME%"  equ "no" echo "Warning: TIBCO_HOME not set, using C:\TIBCO by default."
if "no%TIBCO_HOME%"  equ "no" set TIBCO_HOME=C:\TIBCO
if "no%$TIBRV_HOME%" equ "no" echo "Warning: TIBRV_HOME not set, using %TIBCO_HOME%\TIBRV by default."
if "no%$TIBRV_HOME%" equ "no" set TIBRV_HOME=%TIBCO_HOME%\tibrv

set PATH=%TIBRV_HOME%\bin;%PATH%

for /r %RVSNOOP_HOME%\lib %%A in (*.jar) do set CP=%%A;%CP%

rem Look for optional libraries to add to the classpath.
if "no%TIBCO_TRA_HOME%" neq "no" set CP=%CP%;%TIBCO_TRA_HOME%\lib\TIBCOrt.jar
if "no%TIBSDK_HOME%"    neq "no" set CP=%CP%;%TIBSDK_HOME%\lib\Maverick4.jar
if "no%RVSCRIPT_HOME%"  neq "no" set CP=%CP%;%RVSCRIPT_HOME%\rvscript.jar
if "no%RVTEST_HOME%"    neq "no" for /r %RVTEST_HOME%\lib %%A in (*.jar) do set CP=%CP%;%%A

rem Add the default classpath if present.
if "no%CLASSPATH%" neq "no" set CP=%CP%;%CLASSPATH%

rem Change the "start javaw" on the next line to "java" to see a console log of any errors.
start javaw -Xmx128m -Drvsn00p.home=%RVSNOOP_HOME% -Dfile.encoding=UTF-8 -classpath "%CP%" rvsn00p.StartRvSnooper %*
