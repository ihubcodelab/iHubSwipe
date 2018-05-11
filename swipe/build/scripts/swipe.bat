@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  swipe startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and SWIPE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\jackson-databind-2.6.7.1.jar;%APP_HOME%\lib\commons-codec-1.9.jar;%APP_HOME%\lib\jmespath-java-1.11.228.jar;%APP_HOME%\lib\aws-java-sdk-s3-1.11.228.jar;%APP_HOME%\lib\swipe-1.0-SNAPSHOT.jar;%APP_HOME%\lib\org.apache.commons.io-2.4.jar;%APP_HOME%\lib\annotations-15.0.jar;%APP_HOME%\lib\aws-java-sdk-kms-1.11.228.jar;%APP_HOME%\lib\joda-time-2.8.1.jar;%APP_HOME%\lib\gson-2.7.jar;%APP_HOME%\lib\httpcore-4.4.4.jar;%APP_HOME%\lib\fx-gson-3.1.0.jar;%APP_HOME%\lib\jackson-core-2.6.7.jar;%APP_HOME%\lib\commons-io-2.4.jar;%APP_HOME%\lib\ion-java-1.0.2.jar;%APP_HOME%\lib\httpclient-4.5.2.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\jackson-dataformat-cbor-2.6.7.jar;%APP_HOME%\lib\jackson-annotations-2.6.0.jar;%APP_HOME%\lib\aws-java-sdk-dynamodb-1.11.228.jar;%APP_HOME%\lib\aws-java-sdk-core-1.11.228.jar

@rem Execute swipe
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SWIPE_OPTS%  -classpath "%CLASSPATH%" swipe.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable SWIPE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SWIPE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
