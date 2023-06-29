echo off

net session >nul 2>&1
if %errorLevel% == 0 (
    echo Success: Administrative permissions confirmed.
) else (
    echo Failure: Administrative permissions required.
    echo Do right click by file and choose "Run As Administrator"
    goto :exit
)

set scriptpath=%~dp0
set packagepath=%scriptpath%
set nssmpath=%packagepath%\third-party\nssm

%nssmpath% stop BanalyticsBox confirm

ping -n 3 127.0.0.1

%nssmpath% remove BanalyticsBox confirm

:exit


pause