#!/usr/bin/env bash
#
# Copy env variables to app module gradle properties file
#

set +x // dont print the next lines on run script
mkdir ~/.gradle
printenv | tr ' ' '\n' | grep ANDROID_NETWORK_TOOLS > ~/.gradle/gradle.properties
set -x
