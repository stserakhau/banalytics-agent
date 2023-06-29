echo on

ping -n 3 127.0.0.1

net session

set nssmpath=third-party\nssm

echo Stopping Banalytics Box service

%nssmpath% stop BanalyticsBox confirm

echo Stopping Banalytics Box service stopped

ping -n 3 127.0.0.1

move /Y modules\* modules-backup
move /Y banalytics-box.jar modules-backup

move /Y modules-upgrade\banalytics-box.jar banalytics-box.jar
move /Y modules-upgrade\* modules

echo Banalytics Box service starting

%nssmpath% start BanalyticsBox confirm

echo Banalytics Box service started

ping -n 3 127.0.0.1

echo Banalytics Box upgrade done

exit 0