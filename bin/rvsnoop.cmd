@echo off
rem Start script for RvSnoop @version@ on Windows platforms.
rem $Id$

setlocal

title RvSnoop @version@

goto :skip_function_defs

:add_to_cp_head
  set CP=%1;%CP%
  goto :eof

:add_to_cp_tail
  set CP=%CP%;%1
  goto :eof

:skip_function_defs

set RVSNOOP_HOME=%~dp0%..

if "no%TIBCO_HOME%"  equ "no" echo "Warning: TIBCO_HOME not set, using C:\TIBCO by default."
if "no%TIBCO_HOME%"  equ "no" set TIBCO_HOME=C:\TIBCO
if "no%$TIBRV_HOME%" equ "no" echo "Warning: TIBRV_HOME not set, using %TIBCO_HOME%\TIBRV by default."
if "no%$TIBRV_HOME%" equ "no" set TIBRV_HOME=%TIBCO_HOME%\tibrv

set PATH=%TIBRV_HOME%\bin;%PATH%

for /r %RVSNOOP_HOME%\lib %%A in (*.jar) do (call :add_to_cp_head %%A)

rem Look for optional libraries to add to the classpath.
if "no%TIBCO_TRA_HOME%" neq "no" set CP=%CP%;%TIBCO_TRA_HOME%\lib\TIBCOrt.jar
if "no%TIBSDK_HOME%"    neq "no" set CP=%CP%;%TIBSDK_HOME%\lib\Maverick4.jar
if "no%RVSCRIPT_HOME%"  neq "no" set CP=%CP%;%RVSCRIPT_HOME%\rvscript.jar
if "no%RVTEST_HOME%"    neq "no" for /r %RVTEST_HOME%\lib %%A in (*.jar) do (call :add_to_cp_tail %%A)

rem Add the default classpath if present.
if "no%CLASSPATH%" neq "no" set CP=%CP%;%CLASSPATH%

rem Change the "start javaw" on the next line to "java" to see a console log of any errors.
start javaw -Xmx128m -Drvsnoop.home=%RVSNOOP_HOME% -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=%RVSNOOP_HOME%\lib\commons-logging.properties -classpath "%CP%" org.rvsnoop.ui.RvSnoopApplication %*

endlocal