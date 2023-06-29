#!/bin/bash

echo "================================================"
echo "========= Update Linux Environment ============="
sudo apt update && sudo apt upgrade -y


echo "================================================"
echo "======= Install dependencies Environment ======="
sudo apt install ca-certificates curl gnupg lsb-release net-tools htop zip unzip mc -y

echo "======= Install Pulse Audio"
sudo apt -y install v4l-utils libgl1-mesa-glx libgl1-mesa-dri mesa-utils libavcodec-dev libavformat-dev libswscale-dev \
     libv4l-dev libxvidcore-dev libx264-dev libjpeg-dev libpng-dev libtiff-dev libtbb2 libtbb-dev libdc1394-22-dev \
     libopenexr-dev libgstreamer-plugins-base1.0-dev libgstreamer1.0-dev pulseaudio alsa-utils pm-utils sox

sudo cp ./pulseaudio.service /etc/systemd/system/pulseaudio.service
sudo chmod -x /etc/systemd/system/pulseaudio.service
sudo systemctl --system enable --now pulseaudio.service

sudo usermod -a -G video $LOGNAME #add user to video group (access to USB cameras)

sudo bash  # add video & audio groups to root
usermod -a -G video $LOGNAME
usermod -a -G adm $LOGNAME
exit

scriptPath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "========= Banalytics Box home directory: '$scriptPath'"

currentUser=$(whoami)
echo "========= Banalytics Box user: '$currentUser'"

b_box_version=0.0.0
echo "========= Download Banalytics Box Core version: '$b_box_version'"
curl -L "https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/$b_box_version/core-$b_box_version.jar" -o "$scriptPath/modules/banalytics-box.jar"

sh service-install.sh

echo "=================================================================="
echo "========= To complete installation reboot OS ====================="
echo "=================================================================="
