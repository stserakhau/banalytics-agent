#!/bin/bash

scriptPath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
LOGS="$scriptPath/logs"
JAVA_CMD="$scriptPath/jdk/bin/java"
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

pulseaudio --start --log-target=syslog

echo "Starting Banalytics Box"

$JAVA_CMD --enable-preview -XX:VMOptionsFile="$BANALYTICS_HOME/config/banalytics.vmoptions" \
 -Dorg.bytedeco.javacpp.nopointergc=true -cp "$BANALYTICS_HOME/banalytics-box.jar" \
 -Dloader.path="$BANALYTICS_HOME/modules" -Dfile.encoding=UTF-8 org.springframework.boot.loader.PropertiesLauncher