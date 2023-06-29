echo off
set scriptpath=%~dp0

%scriptpath%nssm status BanalyticsBox

echo Stopping Banalyticcs Box service

%scriptpath%nssm stop BanalyticsBox confirm

echo Stopping Banalyticcs Box service stopped

%scriptpath%nssm status BanalyticsBox

move /Y %scriptpath%modules\* %scriptpath%modules-backup

move /Y %scriptpath%modules-upgrade\* %scriptpath%modules

ping -n 3 127.0.0.1

echo Banalytics Box service starting

%scriptpath%nssm start BanalyticsBox confirm

echo Banalyticcs Box service started

ping -n 3 127.0.0.1

%scriptpath%nssm status BanalyticsBox

echo Banalyticcs Box upgrade done

rem echo Banalyticcs Box Upgrade service self stopping started

rem %scriptpath%nssm stop BanalyticsBoxUpgrade confirm

exit 0