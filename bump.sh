#!/bin/bash

if sed -i "s/^version:.*$/version: $1/" EzBuy/plugin.yml; then
	echo "Successfully bumped version to $1";
	exit 0;
fi

