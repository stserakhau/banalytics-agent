sudo apt update && sudo apt upgrade -y
sudo add-apt-repository ppa:linuxuprising/java -y
sudo apt update -y
sudo apt-get install oracle-java17-installer oracle-java17-set-default -y

#todo create banalytics user

wget -O "banalytics-box.jar" https://europe-central2-maven.pkg.dev/banalytics-portal-358017/maven-repo/com/banalytics/box/core/0.0.1-SNAPSHOT-linux-x86_64/core-0.0.1-SNAPSHOT-linux-x86_64.jar