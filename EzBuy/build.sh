#!/bin/bash
# EzBuy - build.sh
# author: cerrealic

# First, simply run the build task
gradle jar

# Then remove the current version of EzBuy installed on the testing server
find ../TestServers/Lobby/plugins -name 'EzBuy*.jar' -delete

# Then copy the latest version of EzBuy into the testing server's plugin folder
cd build/libs
cp -p $(ls -t EzBuy* | head -1) ../../../TestServers/Lobby/plugins
