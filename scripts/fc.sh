#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="---------------------"
echo $LINEBREAK

let "COUNT=`find scripts -name *.sh | wc -l`"
printf 'Bash:\t%5d scripts\n' $COUNT

let "COUNT2=`find pysrc -name *.py | wc -l`"
printf 'Python:\t%5d modules\n' $COUNT2

let "COUNT3=`find src -name *.java | wc -l`"
printf 'Java:\t%5d classes\n' $COUNT3

echo $LINEBREAK

let "COUNT += COUNT2 + COUNT3"
printf "Total:\t%5d   files\n" $COUNT

echo $LINEBREAK
