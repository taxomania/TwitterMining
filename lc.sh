#!/bin/bash

echo "---------------------"

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT=${LINES/total/}"
printf 'Python:\t%7s lines\n' $COUNT

LINES=`wc -l \`find src -name *.java\` | grep "total"`
COUNT2=${LINES/total/}
let "LINES=COUNT2"
printf 'Java:\t%7s lines\n' $LINES

echo "---------------------"

let "COUNT += COUNT2"
printf "Total:\t%7s lines\n" $COUNT

echo "---------------------"