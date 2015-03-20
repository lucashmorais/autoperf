#!/bin/bash

SCRIPT=$( readlink -f "$0" )
SCRIPT_PATH=$( dirname "$SCRIPT" )

ANTLR_PATH=$( readlink -f "../antlr/antlr-4.4-complete.jar" )
echo ">> Getting antlr from: $ANTLR_PATH"

## Setting environment variables
export CLASSPATH=$ANTLR_PATH

## Cleaning and copying
cd "$SCRIPT_PATH"
rm *.jar
rm -R ./Test ./Main ./Parser
cp -R ../src/* .

## Compiling
javac Main/*.java Test/*.java Parser/*.java

## Generating the jar file
jar cfm autoperf.jar manifest.txt Main/*.class Test/*.class Parser/*.class
