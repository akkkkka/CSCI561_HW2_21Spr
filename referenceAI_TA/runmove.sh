#!/bin/sh
# exit when any command fails
set -e
java -jar taagent.jar
java -jar referee.jar
mv next.txt input.txt
