#!/bin/sh
### This script is an example, it doesn't update the time in the new input.txt it writes! ###
rm -f referee_board_data/*
rm input.txt output.txt current_ply.txt lastmatchange_ply.txt 2>/dev/null
cp input_gamestart.txt input.txt

while ./runmove.sh; do :; done
