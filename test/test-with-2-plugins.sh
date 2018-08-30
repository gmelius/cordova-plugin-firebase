#!/bin/sh
set -x #echo on
set -e #exit on error

CORDOVA_VERSION=$1
PLATFORM=$2
PLATFORM_VERSION=$3
ADDITIONAL_PLUGIN_1=$4
ADDITIONAL_PLUGIN_2=$5

npm install

sh ./test/platform-add.sh $CORDOVA_VERSION $PLATFORM $PLATFORM_VERSION
sh ./test/plugin-add.sh $CORDOVA_VERSION $PLATFORM $PLATFORM_VERSION ..
sh ./test/plugin-add.sh $CORDOVA_VERSION $PLATFORM $PLATFORM_VERSION $ADDITIONAL_PLUGIN_1
sh ./test/plugin-add.sh $CORDOVA_VERSION $PLATFORM $PLATFORM_VERSION $ADDITIONAL_PLUGIN_2
sh ./test/platform-build.sh $CORDOVA_VERSION $PLATFORM $PLATFORM_VERSION