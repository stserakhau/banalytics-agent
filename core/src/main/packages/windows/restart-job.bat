echo on

echo Banalyticcs Box restarting...

net session

set nssmpath=third-party\nssm

%nssmpath% status BanalyticsBox

echo Stopping Banalytics Box service

ping -n 3 127.0.0.1

%nssmpath% stop BanalyticsBox confirm

echo Stopping Banalyticcs Box service stopped

ping -n 3 127.0.0.1

%nssmpath% status BanalyticsBox

ping -n 3 127.0.0.1

echo Banalytics Box service starting

%nssmpath% start BanalyticsBox confirm

echo Banalyticcs Box service started

ping -n 3 127.0.0.1

%nssmpath% status BanalyticsBox

echo Banalyticcs Box restarted

exit 0