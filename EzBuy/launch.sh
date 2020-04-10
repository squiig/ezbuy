#!/bin/bash
# launch.sh
# author: cerrealic

servers_dir=$1

# init default
if [ -z $1 ]; then
	servers_dir="TestServers/"
fi

# if not a real directory
if ! [ -d $servers_dir ]; then
	echo "Usage: launch <relative folder>"
	exit 1
fi

cd $servers_dir

# Start the servers
for d in */ ; do
	cd $d
	start "start.bat"
	cd ..
done
