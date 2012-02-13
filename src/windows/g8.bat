@REM g8 launcher script
@REM 
@REM Envioronment:
@REM JAVA_HOME - location of a JDK home dir (optional)
@REM SBT_HOME - location of a JDK home dir (mandatory)
@REM SBT_OPTS  - JVM options (optional)


@setlocal

@echo off
set G8_HOME=%~dp0
set ERROR_CODE=0

@REM We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

@REM We use the value of the JAVA_OPTS environment variable if defined

set _JAVA_OPTS=%JAVA_OPTS%

if "%_JAVA_OPTS%"=="" set "_JAVA_OPTS=-Xmx128M -Dsbt.log.format=true"


if not "%SBT_HOME%"=="" goto SbtHomeSet

echo.
echo ERROR: Envionment variable SBT_HOME not set
echo Cannot find sbt-launch.jar to run g8.
echo.
goto error

:SbtHomeSet

if exist "%SBT_HOME%\sbt-launch.jar" goto :run

echo.
echo ERROR: SBT_HOME is set to an invalid directory.
echo SBT_HOME = %SBT_HOME%
echo.
goto error

:run

@REM WE MUST FIX THE BOOT PROPERTIES TO BE A JAVA URL
SETLOCAL ENABLEDELAYEDEXPANSION
set bootprop=%G8_HOME%giter8.properties
set bootprop=!bootprop: =%%20!
set bootprop=-Dsbt.boot.properties=file:/%bootprop:\=/%

"%_JAVACMD%" %_JAVA_OPTS% %SBT_OPTS% "%bootprop%" -cp "%SBT_HOME%sbt-launch.jar" xsbt.boot.Boot %*
if ERRORLEVEL 1 goto error
goto end

:error

set ERROR_CODE=1

:end

@endlocal

exit /B %ERROR_CODE%
