#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="-------------------"
LINEBREAK2="-----------"
printf '%s   %s\n' $LINEBREAK $LINEBREAK2

let "COUNT1=`find pysrc -name *.py | wc -l`"
printf 'Python: %3d modules   ' $COUNT1

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT3=${LINES/total/}"
printf '%5d lines\n' $COUNT3

let "COUNT2=`find src -name *.java | wc -l`"
printf 'Java:\t%3d classes   ' $COUNT2

LINES=`wc -l \`find src -name *.java\` | grep "total"`
let "COUNT4=${LINES/total/}"
printf '%5d lines\n' $COUNT4

printf '%s   %s\n' $LINEBREAK $LINEBREAK2

let "COUNT1 += COUNT2"
printf "Total:\t%3d   files   " $COUNT1

let "COUNT3 += COUNT4"
printf "%5d lines\n" $COUNT3

printf '%s   %s\n' $LINEBREAK $LINEBREAK2
