#!/bin/bash

echo "================================================"

currentUser=$(whoami)
echo "========= Banalytics Box user: '$currentUser'"

scriptPath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export BANALYTICS_HOME="$scriptPath"
export me="$currentUser"

echo "========= Banalytics Box home directory: '$BANALYTICS_HOME'"


b_box_version=0.0.0
echo "========= Download Banalytics Box Core version: '$b_box_version'"
wget -O "$scriptPath/banalytics-box.jar" "https://europe-central2-maven.pkg.dev/banalytics-production/maven-repo/com/banalytics/box/core/$b_box_version/core-$b_box_version.jar"

jdk_archive="$scriptPath/modules-download/jdk-17.tar.gz"
if [ ! -f $jdk_archive ]
then
    echo "Downloading JDK"
    wget -O $jdk_archive "https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz"
fi
if [ -d "$scriptPath/jdk" ]
then
    echo "JDK Folder exists"
else
    mkdir "$scriptPath/jdk"
fi

find $scriptPath/jdk -type d -empty -exec  tar --strip-components=1 -xvzf $jdk_archive -C $scriptPath/jdk \;

sudo apt install v4l-utils net-tools -y
sudo ln -s /usr/sbin/arp /usr/bin
sudo usermod -a -G video $LOGNAME
sudo usermod -a -G audio $LOGNAME

sh -c 'echo "'"$(cat banalytics-box.service.template)"'"' > banalytics-box.service

sudo cp ./banalytics-box.service /etc/systemd/system/banalytics-box.service
sudo chmod -x /etc/systemd/system/banalytics-box.service

sudo systemctl enable banalytics-box.service
sudo systemctl start banalytics-box.service

echo "=================================================================="
echo "=================   Installation completed.  ====================="
echo "====                                                          ===="
echo "====   Nearest 1..4 minutes Banalytics Box will download      ===="
echo "====        last updates and will be ready to use             ===="
echo "====                                                          ===="
echo "====   See you registration uuid in http://localhost:8080     ===="
echo "====                                                          ===="
echo "====                Initial password: default                 ===="
echo "====                                                          ===="
echo "====                                                          ===="
echo "=================================================================="

read -p "Press any key to open instance status page in browser"

xdg-open http://localhost:8080
