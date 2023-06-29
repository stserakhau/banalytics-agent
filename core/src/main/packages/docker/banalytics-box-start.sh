#!/bin/bash

b_box_version=0.0.0
BANALYTICS_HOME="/opt/banalytics-box"
echo "Home directory: '$BANALYTICS_HOME"

mkdir "$BANALYTICS_HOME/modules"

if [ ! -f "$BANALYTICS_HOME/modules/banalytics-box.jar" ]
then
    echo "Downloading banalytics box package..."
    curl -L "https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/$b_box_version/core-$b_box_version.jar" -o "$BANALYTICS_HOME/modules/banalytics-box.jar"
fi

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

echo "Starting Banalytics Box"
java -cp "$BANALYTICS_HOME/modules/banalytics-box.jar" -Xmx1024M -Xms1024M -Dloader.path="$BANALYTICS_HOME/modules" -Dfile.encoding=UTF-8 org.springframework.boot.loader.PropertiesLauncher