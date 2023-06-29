#!/bin/bash

sudo cp ./banalytics-box.service /etc/systemd/system/banalytics-box.service
sudo chmod -x /etc/systemd/system/banalytics-box.service
sudo systemctl daemon-reload
sudo systemctl enable banalytics-box.service
sudo systemctl start banalytics-box.service
sudo systemctl status banalytics-box.service