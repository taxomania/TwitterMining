#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="-------------------"
LINEBREAK2="\t-----------"
echo -e $LINEBREAK $LINEBREAK2

let "COUNT=`find pysrc -name *.py | wc -l`"
printf 'Python:\t%3d modules\t' $COUNT

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT3=${LINES/total/}"
printf '%5d lines\n' $COUNT3

let "COUNT2=`find src -name *.java | wc -l`"
printf 'Java:\t%3d classes\t' $COUNT2

LINES=`wc -l \`find src -name *.java\` | grep "total"`
let "COUNT4=${LINES/total/}"
printf '%5d lines\n' $COUNT4

echo -e $LINEBREAK $LINEBREAK2

let "COUNT += COUNT2"
printf "Total:\t%3d   files\t" $COUNT

let "COUNT3 += COUNT4"
printf "%5d lines\n" $COUNT3

echo -e $LINEBREAK $LINEBREAK2
