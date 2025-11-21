#!/bin/bash
export ANDROID_HOME=$HOME/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
cd LeFauxPassRN/android
./gradlew assembleDebug
