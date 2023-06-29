echo off

net session >nul 2>&1
if %errorLevel% == 0 (
    echo Success: Administrative permissions are confirmed.
) else (
    echo Failure: Administrative permissions are required.
    echo Click the right mouse button on the file name and choose "Run As Administrator"
    goto :exit
)

echo ===================================================================
echo ============== Banalytics Box service installation ================
echo ==============                                     ================

set scriptpath=%~dp0
set packagepath=%scriptpath%

set b_box_version=0.0.0

echo ============== Downloading Banalytics Box module ================
%scriptpath%/third-party/curl -L -k https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/%b_box_version%/core-%b_box_version%.jar -o "%packagepath%\modules\banalytics-box.jar"


echo ==========                                             ============
echo ========== Configure Banalytic Box Windows Service     ============


set nssmpath=%packagepath%\third-party\nssm

echo "Banalytics Box home: %packagepath%"

%nssmpath% install BanalyticsBox "%packagepath%BanalyticsBox_Win64-start.bat"
%nssmpath% set BanalyticsBox AppDirectory %packagepath%
%nssmpath% set BanalyticsBox AppEnvironmentExtra BANALYTICS_HOME=%packagepath%
%nssmpath% set BanalyticsBox DisplayName Banalytics Box
%nssmpath% set BanalyticsBox Description Banalytics Box Service
%nssmpath% set BanalyticsBox Start SERVICE_AUTO_START
%nssmpath% set BanalyticsBox ObjectName LocalSystem
%nssmpath% set BanalyticsBox Type SERVICE_WIN32_OWN_PROCESS
%nssmpath% set BanalyticsBox AppPriority NORMAL_PRIORITY_CLASS
%nssmpath% set BanalyticsBox AppNoConsole 0
%nssmpath% set BanalyticsBox AppAffinity All
%nssmpath% set BanalyticsBox AppStopMethodSkip 0
%nssmpath% set BanalyticsBox AppStopMethodConsole 1500
%nssmpath% set BanalyticsBox AppStopMethodWindow 1500
%nssmpath% set BanalyticsBox AppStopMethodThreads 1500
%nssmpath% set BanalyticsBox AppThrottle 1500
%nssmpath% set BanalyticsBox AppExit Default Restart
%nssmpath% set BanalyticsBox AppRestartDelay 0
%nssmpath% set BanalyticsBox AppStdout %packagepath%\logs\banalytics-box.log
%nssmpath% set BanalyticsBox AppStderr %packagepath%\logs\banalytics-box-error.log
%nssmpath% set BanalyticsBox AppStdoutCreationDisposition 4
%nssmpath% set BanalyticsBox AppStderrCreationDisposition 4
%nssmpath% set BanalyticsBox AppRotateFiles 1
%nssmpath% set BanalyticsBox AppRotateOnline 0
%nssmpath% set BanalyticsBox AppRotateSeconds 86400
%nssmpath% set BanalyticsBox AppRotateBytes 1048576

echo ==========                                             ============
echo ==========     Banalytics Box service is installed     ============
echo ===================================================================
echo ==========  Starting Service...

%nssmpath% start BanalyticsBox

ping -n 2 127.0.0.1

echo ==========  Note: PIN code of the local console: default
echo ==========  Note: For safety, change the default pin to your own

ping -n 10 127.0.0.1

start https://localhost:8080

:exit

pause