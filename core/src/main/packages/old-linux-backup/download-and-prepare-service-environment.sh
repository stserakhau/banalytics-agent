#!/bin/bash

echo "===================================================="
echo "============= Update Linux Environment ============="
sudo apt-get update && apt-get upgrade -y

echo "===================================================="
echo "============= Install Docker Container ============="
sudo apt-get install ca-certificates curl gnupg lsb-release -y
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo chmod a+r /etc/apt/keyrings/docker.gpg
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y
sudo docker run hello-world

echo "===================================================="
echo "========== Install Banalytics Box Image ============"


echo "==================================================================="
echo "============== Upgrade & Installation dependencies ================"
echo "==============                                     ================"
sudo apt update && sudo apt upgrade -y
sudo add-apt-repository ppa:linuxuprising/java -y
sudo apt update -y
sudo apt-get install oracle-java17-installer oracle-java17-set-default -y
sudo apt-get install net-tools htop zip unzip -y

scriptPath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "Installation directory: '$(scriptPath)'"

b_box_version=0.0.0

if ! test "$scriptPath/scripts.zip"; then
    curl -L "https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/%b_box_version%/core-%b_box_version%-ubuntu-scripts.zip" -o "$scriptPath/scripts.zip"
    unzip "$scriptPath/scripts.zip" -d "$scriptPath"
fi

if ! test "$scriptPath/banalytics-box.jar"; then
    curl -L "https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/$b_box_version/core-$b_box_version.jar" -o "$scriptPath/banalytics-box.jar"
fi

mkdir "$scriptPath/modules-backup"
mkdir "$scriptPath/modules"
mkdir "$scriptPath/modules-upgrade"

mv "$scriptPath/banalytics-box.jar" "$scriptPath/modules"
mv "$scriptPath/scripts/*.*" "$scriptPath"

del "$scriptPath/scripts.zip"

echo "==============                                     ================"
echo "============== Banalytics Box Environment Created  ================"
echo "==================================================================="
echo "============== Banalytics Box service installation ================"
echo "==============                                     ================"
echo "============== Banalytics Box Home $scriptPath"
echo "==================================================================="
echo "============== Creating Banalytics Box Service User ==============="
sudo useradd -m -d "$scriptPath" banalytics-service