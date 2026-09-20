@echo off
REM
REM Starts the visualizer. Double-click it, or run it from a terminal.
REM
REM The first run downloads Maven and about 30 MB of dependencies, so give it
REM a minute. Java 17 or newer is the only thing you need installed.

cd /d "%~dp0"

REM The wrapper falls back to JAVA_HOME when java is not on the PATH, so
REM either one is enough.
if defined JAVA_HOME goto run
where java >nul 2>&1
if not errorlevel 1 goto run

echo No Java found.
echo.
echo Install a JDK, version 17 or newer, from https://adoptium.net/
echo and open a new terminal so the change to PATH takes effect.
echo.
pause
exit /b 1

:run
REM Called by full path on purpose. Where NoDefaultCurrentDirectoryInExePath
REM is set, cmd does not search the working directory, and a bare
REM "mvnw.cmd" is reported as an unrecognised command.
call "%~dp0mvnw.cmd" javafx:run

REM Without this the window vanishes before you can read what went wrong.
if errorlevel 1 (
    echo.
    echo The app exited with an error. The output above says why.
    pause
)
