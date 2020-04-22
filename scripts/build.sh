#!/bin/bash
# build.sh
# author: cerrealic
# $1 = app name

# First, simply run the build task
gradle jar

# Then remove the current version installed on the testing server
find server/plugins -name "$1*.jar" -delete

# Then copy the latest version into the testing server's plugin folder
cd build/libs
cp -p $(ls -t $1* | head -1) ../../server/plugins
