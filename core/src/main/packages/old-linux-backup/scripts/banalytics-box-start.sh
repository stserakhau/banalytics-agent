#!/bin/bash

java -cp /opt/modules/banalytics-box.jar -Xmx1024M -Xms1024M -Dloader.path="/opt/modules" -Dfile.encoding=UTF-8 org.springframework.boot.loader.PropertiesLauncher