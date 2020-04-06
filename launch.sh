#!/bin/bash

base=$(pwd)
servers_dir=$1

if [ -z $1 ]; then
	servers_dir="TestServers/"
fi

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

cd $base

