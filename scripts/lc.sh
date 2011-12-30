#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="---------------------"
echo $LINEBREAK

LINES=`wc -l \`find scripts -name *.sh\` | grep "total"`
let "COUNT=${LINES/total/}"
printf 'Bash:\t%7d lines\n' $COUNT

LINES=`wc -l \`find pysrc -name *.py\` | grep "total"`
let "COUNT2=${LINES/total/}"
printf 'Python:\t%7d lines\n' $COUNT2

LINES=`wc -l \`find src -name *.java\` | grep "total"`
let "COUNT3=${LINES/total/}"
printf 'Java:\t%7d lines\n' $COUNT3

echo $LINEBREAK

let "COUNT += COUNT2 + COUNT3"
printf "Total:\t%7d lines\n" $COUNT

echo $LINEBREAK
