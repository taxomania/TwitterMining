#!/bin/bash

echo "---------------------"

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT=${LINES/total/}"
printf 'Python:\t%7s lines\n' $COUNT

LINES=`wc -l \`find src -name *.java\` | grep "total"`
let "COUNT2=${LINES/total/}"
printf 'Java:\t%7s lines\n' $COUNT2

echo "---------------------"

let "COUNT += COUNT2"
printf "Total:\t%7s lines\n" $COUNT

echo "---------------------"