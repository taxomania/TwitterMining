#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="--------------------"
LINEBREAK2="\t-----------"
echo -e $LINEBREAK $LINEBREAK2

let "COUNT=`find scripts -name *.sh | wc -l`"
printf 'Bash:\t%4d scripts\t' $COUNT

LINES=`wc -l \`find scripts -name *.sh\` | grep "total"`
let "COUNT4=${LINES/total/}"
printf '%5d lines\n' $COUNT4

let "COUNT2=`find pysrc -name *.py | wc -l`"
printf 'Python:\t%4d modules\t' $COUNT2

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT5=${LINES/total/}"
printf '%5d lines\n' $COUNT5

let "COUNT3=`find src -name *.java | wc -l`"
printf 'Java:\t%4d classes\t' $COUNT3

LINES=`wc -l \`find src -name *.java\` | grep "total"`
let "COUNT6=${LINES/total/}"
printf '%5d lines\n' $COUNT6

echo -e $LINEBREAK $LINEBREAK2

let "COUNT2 += COUNT3"
printf "Subtotal:%3d   files\t" $COUNT2

let "COUNT5 += COUNT6"
printf "%5d lines\n" $COUNT5

let "COUNT += COUNT2"
printf "Total:\t%4d   files\t" $COUNT

let "COUNT4 += COUNT5"
printf "%5d lines\n" $COUNT4

echo -e $LINEBREAK $LINEBREAK2
