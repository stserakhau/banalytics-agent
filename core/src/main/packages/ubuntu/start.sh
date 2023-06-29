#!/bin/bash

scriptPath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
LOGS="$scriptPath/logs"
JAVA_CMD="$scriptPath/jdk-17/bin/java"
BANALYTICS_HOME="$scriptPath"

export BANALYTICS_HOME=$BANALYTICS_HOME  # setup environment variable

echo "Java command: '$JAVA_CMD'"
echo "Banalytics home directory: '$BANALYTICS_HOME'"

if [ -f "$BANALYTICS_HOME/modules-upgrade/modules-info.properties" ]
then
    echo "Applying software upgrade"
    echo "Clean backup"
    rm -r "$BANALYTICS_HOME/modules-backup"
    echo "Backup current module"
    mv "$BANALYTICS_HOME/modules" "$BANALYTICS_HOME/modules-backup"
    echo "Applying downloaded software upgrade"
    mv "$BANALYTICS_HOME/modules-upgrade" "$BANALYTICS_HOME/modules"
    mkdir "$BANALYTICS_HOME/modules-upgrade"
fi

startTime=$(date "+%Y.%m.%d-%H.%M.%S")

pulseaudio --start --log-target=journal
sudo chmod 777 /dev/video*
sudo chmod 777 /dev/snd/ -R

echo "Starting Banalytics Box"
$JAVA_CMD -XX:VMOptionsFile="$BANALYTICS_HOME/config/banalytics.vmoptions" -Dorg.bytedeco.javacpp.nopointergc=true --enable-preview -cp "$BANALYTICS_HOME/modules/banalytics-box.jar" -Dloader.path="$BANALYTICS_HOME/modules" -Dfile.encoding=UTF-8 org.springframework.boot.loader.PropertiesLauncher
